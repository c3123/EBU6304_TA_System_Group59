package com.ta.service.admin;

import com.ta.constant.ErrorCodes;
import com.ta.dto.admin.AdminRecruitmentOutcomeDepartmentRow;
import com.ta.dto.admin.AdminRecruitmentOutcomeResponse;
import com.ta.dto.admin.AdminRecruitmentOutcomeVacancyRow;
import com.ta.model.ApplicationRecord;
import com.ta.model.JobPosting;
import com.ta.util.IsoTime;
import com.ta.util.JsonUtility;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.text.Collator;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Aggregates jobs and applications for the leadership recruitment-outcome dashboard.
 * Counting rules align with {@link AdminDashboardService#populateOverview} for consistency.
 */
public class AdminRecruitmentOutcomeService {

    private static final String DEPARTMENT_UNKNOWN = "Unspecified";

    public AdminRecruitmentOutcomeResponse load(ServletContext context,
                                                int vacancyTopLimit,
                                                String jobSinceRaw,
                                                String jobUntilRaw) throws IOException {
        LocalDate jobSince = parseOptionalIsoDate("jobSince", jobSinceRaw);
        LocalDate jobUntil = parseOptionalIsoDate("jobUntil", jobUntilRaw);
        if (jobSince != null && jobUntil != null && jobSince.isAfter(jobUntil)) {
            throw new AdminBusinessException(
                    ErrorCodes.VALIDATION_ERROR,
                    "jobSince must be on or before jobUntil.",
                    HttpServletResponse.SC_BAD_REQUEST
            );
        }

        List<JobPosting> allJobs = JsonUtility.loadJobs(context);
        List<JobPosting> jobsWindow = applyJobReferenceDateWindow(allJobs, jobSince, jobUntil);
        Set<String> jobIds = new HashSet<>();
        for (JobPosting j : jobsWindow) {
            if (j.getId() != null && !j.getId().isBlank()) {
                jobIds.add(j.getId());
            }
        }

        List<ApplicationRecord> allApplications = JsonUtility.loadApplications(context);
        boolean dateFilterActive = jobSince != null || jobUntil != null;
        List<ApplicationRecord> applications = dateFilterActive
                ? filterApplicationsToJobIds(allApplications, jobIds)
                : new ArrayList<>(allApplications);

        Map<String, Integer> hiredByJob = countHiredByJob(applications);

        int totalPositionSlots = 0;
        int closedJobs = 0;
        int recruitingJobs = 0;
        int totalVacancies = 0;

        for (JobPosting job : jobsWindow) {
            if (Boolean.TRUE.equals(job.getWithdrawn())) {
                continue;
            }
            totalPositionSlots += Math.max(job.getPositions(), 0);
            if (isClosed(job)) {
                closedJobs++;
            } else if ("open".equalsIgnoreCase(trimToEmpty(job.getStatus()))) {
                recruitingJobs++;
            }
            int hired = hiredByJob.getOrDefault(job.getId(), 0);
            totalVacancies += Math.max(job.getPositions() - hired, 0);
        }

        int totalApplications = 0;
        for (ApplicationRecord application : applications) {
            if (!application.isActive()) {
                continue;
            }
            totalApplications++;
        }

        // Filled slots (capped per job) so hired + vacancies always equals total position slots.
        int totalHired = totalPositionSlots - totalVacancies;

        int topLimit = normalizeVacancyTopLimit(vacancyTopLimit);
        AdminRecruitmentOutcomeResponse response = new AdminRecruitmentOutcomeResponse();
        response.setVacancyTopLimit(topLimit);
        response.setJobSince(jobSince == null ? "" : jobSince.toString());
        response.setJobUntil(jobUntil == null ? "" : jobUntil.toString());
        response.setTotalPositionSlots(totalPositionSlots);
        response.setClosedJobs(closedJobs);
        response.setRecruitingJobs(recruitingJobs);
        response.setTotalApplications(totalApplications);
        response.setTotalHired(totalHired);
        response.setTotalVacancies(totalVacancies);
        response.setDepartments(buildDepartmentRows(jobsWindow, hiredByJob));
        response.setTopVacancyJobs(buildTopVacancyJobs(jobsWindow, hiredByJob, topLimit));
        response.setGeneratedAt(IsoTime.utcNowSeconds());
        return response;
    }

    public String buildRecruitmentOutcomeCsv(AdminRecruitmentOutcomeResponse data) {
        StringBuilder sb = new StringBuilder();
        sb.append('\uFEFF');
        appendCsvRow(sb, "meta", "generatedAt", csvValue(data.getGeneratedAt()));
        appendCsvRow(sb, "meta", "jobSince", csvValue(data.getJobSince()));
        appendCsvRow(sb, "meta", "jobUntil", csvValue(data.getJobUntil()));
        appendCsvRow(sb, "meta", "vacancyTopLimit", String.valueOf(data.getVacancyTopLimit()));
        appendCsvRow(sb, "kpi", "totalPositionSlots", String.valueOf(data.getTotalPositionSlots()));
        appendCsvRow(sb, "kpi", "closedJobs", String.valueOf(data.getClosedJobs()));
        appendCsvRow(sb, "kpi", "recruitingJobs", String.valueOf(data.getRecruitingJobs()));
        appendCsvRow(sb, "kpi", "totalApplications", String.valueOf(data.getTotalApplications()));
        appendCsvRow(sb, "kpi", "totalHired", String.valueOf(data.getTotalHired()));
        appendCsvRow(sb, "kpi", "totalVacancies", String.valueOf(data.getTotalVacancies()));
        appendCsvRow(sb, "dept_header", "department", "hiredCount", "vacancyCount");
        for (AdminRecruitmentOutcomeDepartmentRow row : data.getDepartments()) {
            appendCsvRow(sb, "dept", row.getDepartment(), String.valueOf(row.getHiredCount()), String.valueOf(row.getVacancyCount()));
        }
        appendCsvRow(sb, "vac_header", "rank", "jobId", "moduleCode", "title", "department", "teacherName", "positions", "hiredCount", "vacancyCount");
        List<AdminRecruitmentOutcomeVacancyRow> top = data.getTopVacancyJobs();
        for (int i = 0; i < top.size(); i++) {
            AdminRecruitmentOutcomeVacancyRow row = top.get(i);
            appendCsvRow(sb, "vac",
                    String.valueOf(i + 1),
                    csvValue(row.getJobId()),
                    csvValue(row.getModuleCode()),
                    csvValue(row.getTitle()),
                    csvValue(row.getDepartment()),
                    csvValue(row.getTeacherName()),
                    String.valueOf(row.getPositions()),
                    String.valueOf(row.getHiredCount()),
                    String.valueOf(row.getVacancyCount()));
        }
        return sb.toString();
    }

    private static String csvValue(String v) {
        return v == null ? "" : v;
    }

    private static void appendCsvRow(StringBuilder sb, String... cells) {
        for (int i = 0; i < cells.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(escapeCsvCell(cells[i]));
        }
        sb.append('\n');
    }

    private static String escapeCsvCell(String value) {
        String v = value == null ? "" : value;
        boolean needQuotes = v.contains(",") || v.contains("\"") || v.contains("\n") || v.contains("\r");
        String t = v.replace("\"", "\"\"");
        if (needQuotes) {
            return "\"" + t + "\"";
        }
        return t;
    }

    private List<JobPosting> applyJobReferenceDateWindow(List<JobPosting> all,
                                                         LocalDate since,
                                                         LocalDate until) {
        if (since == null && until == null) {
            return new ArrayList<>(all);
        }
        List<JobPosting> out = new ArrayList<>();
        for (JobPosting job : all) {
            if (jobMatchesReferenceWindow(job, since, until)) {
                out.add(job);
            }
        }
        return out;
    }

    /**
     * Jobs with no parseable reference date are always included when a window is set (legacy rows).
     */
    private boolean jobMatchesReferenceWindow(JobPosting job, LocalDate since, LocalDate until) {
        if (since == null && until == null) {
            return true;
        }
        LocalDate ref = resolveJobReferenceDate(job);
        if (ref == null) {
            return true;
        }
        if (since != null && ref.isBefore(since)) {
            return false;
        }
        return until == null || !ref.isAfter(until);
    }

    private LocalDate resolveJobReferenceDate(JobPosting job) {
        LocalDate d = parseIsoDatePrefix(job.getPublishedAt());
        if (d != null) {
            return d;
        }
        d = parseIsoDatePrefix(job.getCreatedAt());
        if (d != null) {
            return d;
        }
        return parseIsoDatePrefix(job.getUpdatedAt());
    }

    private LocalDate parseIsoDatePrefix(String raw) {
        String t = trimToEmpty(raw);
        if (t.isBlank()) {
            return null;
        }
        try {
            String datePart = t.length() >= 10 ? t.substring(0, 10) : t;
            return LocalDate.parse(datePart);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    private LocalDate parseOptionalIsoDate(String paramName, String raw) {
        String t = trimToEmpty(raw);
        if (t.isBlank()) {
            return null;
        }
        try {
            String datePart = t.length() >= 10 ? t.substring(0, 10) : t;
            return LocalDate.parse(datePart);
        } catch (DateTimeParseException ex) {
            throw new AdminBusinessException(
                    ErrorCodes.VALIDATION_ERROR,
                    paramName + " must be yyyy-MM-dd.",
                    HttpServletResponse.SC_BAD_REQUEST
            );
        }
    }

    private List<ApplicationRecord> filterApplicationsToJobIds(List<ApplicationRecord> apps, Set<String> jobIds) {
        List<ApplicationRecord> out = new ArrayList<>();
        for (ApplicationRecord a : apps) {
            if (a.getJobId() == null || a.getJobId().isBlank()) {
                continue;
            }
            if (jobIds.contains(a.getJobId())) {
                out.add(a);
            }
        }
        return out;
    }

    private int normalizeVacancyTopLimit(int requested) {
        if (requested < 1) {
            return 10;
        }
        return Math.min(requested, 50);
    }

    private List<AdminRecruitmentOutcomeVacancyRow> buildTopVacancyJobs(List<JobPosting> jobs,
                                                                        Map<String, Integer> hiredByJob,
                                                                        int limit) {
        List<AdminRecruitmentOutcomeVacancyRow> rows = new ArrayList<>();
        for (JobPosting job : jobs) {
            if (Boolean.TRUE.equals(job.getWithdrawn())) {
                continue;
            }
            if (job.getId() == null || job.getId().isBlank()) {
                continue;
            }
            int hired = hiredByJob.getOrDefault(job.getId(), 0);
            int vac = Math.max(job.getPositions() - hired, 0);
            if (vac <= 0) {
                continue;
            }
            AdminRecruitmentOutcomeVacancyRow row = new AdminRecruitmentOutcomeVacancyRow();
            row.setJobId(job.getId());
            row.setModuleCode(trimToEmpty(job.getModuleCode()));
            row.setTitle(trimToEmpty(job.getTitle()));
            row.setDepartment(departmentLabel(job));
            row.setTeacherName(trimToEmpty(job.getTeacherName()));
            row.setPositions(Math.max(job.getPositions(), 0));
            row.setHiredCount(hired);
            row.setVacancyCount(vac);
            rows.add(row);
        }
        rows.sort(Comparator
                .comparingInt(AdminRecruitmentOutcomeVacancyRow::getVacancyCount).reversed()
                .thenComparing(r -> trimToEmpty(r.getModuleCode()), String.CASE_INSENSITIVE_ORDER)
                .thenComparing(r -> trimToEmpty(r.getTitle()), String.CASE_INSENSITIVE_ORDER));
        if (rows.size() > limit) {
            return new ArrayList<>(rows.subList(0, limit));
        }
        return rows;
    }

    private List<AdminRecruitmentOutcomeDepartmentRow> buildDepartmentRows(List<JobPosting> jobs,
                                                                         Map<String, Integer> hiredByJob) {
        Map<String, Integer> hiredByDept = new LinkedHashMap<>();
        for (JobPosting job : jobs) {
            if (Boolean.TRUE.equals(job.getWithdrawn())) {
                continue;
            }
            if (job.getId() == null || job.getId().isBlank()) {
                continue;
            }
            int positions = Math.max(job.getPositions(), 0);
            int hired = hiredByJob.getOrDefault(job.getId(), 0);
            int filled = Math.min(hired, positions);
            String dept = departmentLabel(job);
            hiredByDept.put(dept, hiredByDept.getOrDefault(dept, 0) + filled);
        }

        Map<String, Integer> vacancyByDept = new LinkedHashMap<>();
        for (JobPosting job : jobs) {
            if (Boolean.TRUE.equals(job.getWithdrawn())) {
                continue;
            }
            String dept = departmentLabel(job);
            int hired = hiredByJob.getOrDefault(job.getId(), 0);
            int vac = Math.max(job.getPositions() - hired, 0);
            vacancyByDept.put(dept, vacancyByDept.getOrDefault(dept, 0) + vac);
        }

        Set<String> keys = new LinkedHashSet<>();
        keys.addAll(hiredByDept.keySet());
        keys.addAll(vacancyByDept.keySet());

        Collator collator = Collator.getInstance(Locale.CHINA);
        List<String> ordered = new ArrayList<>(keys);
        ordered.sort((a, b) -> {
            boolean ua = DEPARTMENT_UNKNOWN.equals(a);
            boolean ub = DEPARTMENT_UNKNOWN.equals(b);
            if (ua != ub) {
                return ua ? 1 : -1;
            }
            return collator.compare(a, b);
        });

        List<AdminRecruitmentOutcomeDepartmentRow> rows = new ArrayList<>();
        for (String dept : ordered) {
            AdminRecruitmentOutcomeDepartmentRow row = new AdminRecruitmentOutcomeDepartmentRow();
            row.setDepartment(dept);
            row.setHiredCount(hiredByDept.getOrDefault(dept, 0));
            row.setVacancyCount(vacancyByDept.getOrDefault(dept, 0));
            rows.add(row);
        }
        return rows;
    }

    private String departmentLabel(JobPosting job) {
        if (job == null) {
            return DEPARTMENT_UNKNOWN;
        }
        String raw = trimToEmpty(job.getDepartment());
        if (raw.isEmpty()) {
            return DEPARTMENT_UNKNOWN;
        }
        return raw;
    }

    private Map<String, Integer> countHiredByJob(List<ApplicationRecord> applications) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (ApplicationRecord app : applications) {
            if (!app.isActive() || app.getJobId() == null || app.getJobId().isBlank()) {
                continue;
            }
            if ("hired".equalsIgnoreCase(trimToEmpty(app.getStatus()))) {
                counts.put(app.getJobId(), counts.getOrDefault(app.getJobId(), 0) + 1);
            }
        }
        return counts;
    }

    private boolean isClosed(JobPosting job) {
        return Boolean.TRUE.equals(job.getRecruitmentClosed())
                || "closed".equalsIgnoreCase(trimToEmpty(job.getStatus()));
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
