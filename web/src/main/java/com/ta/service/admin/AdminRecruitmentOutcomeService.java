package com.ta.service.admin;

import com.ta.dto.admin.AdminRecruitmentOutcomeDepartmentRow;
import com.ta.dto.admin.AdminRecruitmentOutcomeResponse;
import com.ta.dto.admin.AdminRecruitmentOutcomeVacancyRow;
import com.ta.model.ApplicationRecord;
import com.ta.model.JobPosting;
import com.ta.util.JsonUtility;
import jakarta.servlet.ServletContext;

import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
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

    private static final String DEPARTMENT_UNKNOWN = "\u672a\u586b";

    public AdminRecruitmentOutcomeResponse load(ServletContext context, int vacancyTopLimit) throws IOException {
        List<JobPosting> jobs = JsonUtility.loadJobs(context);
        List<ApplicationRecord> applications = JsonUtility.loadApplications(context);
        Map<String, Integer> hiredByJob = countHiredByJob(applications);

        int totalPositionSlots = 0;
        int closedJobs = 0;
        int recruitingJobs = 0;
        int totalVacancies = 0;

        for (JobPosting job : jobs) {
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
        int totalHired = 0;
        for (ApplicationRecord application : applications) {
            if (!application.isActive()) {
                continue;
            }
            totalApplications++;
            if ("hired".equalsIgnoreCase(trimToEmpty(application.getStatus()))) {
                totalHired++;
            }
        }

        int topLimit = normalizeVacancyTopLimit(vacancyTopLimit);
        AdminRecruitmentOutcomeResponse response = new AdminRecruitmentOutcomeResponse();
        response.setVacancyTopLimit(topLimit);
        response.setTotalPositionSlots(totalPositionSlots);
        response.setClosedJobs(closedJobs);
        response.setRecruitingJobs(recruitingJobs);
        response.setTotalApplications(totalApplications);
        response.setTotalHired(totalHired);
        response.setTotalVacancies(totalVacancies);
        response.setDepartments(buildDepartmentRows(jobs, applications, hiredByJob));
        response.setTopVacancyJobs(buildTopVacancyJobs(jobs, hiredByJob, topLimit));
        return response;
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
                                                                           List<ApplicationRecord> applications,
                                                                           Map<String, Integer> hiredByJob) {
        Map<String, JobPosting> jobById = new LinkedHashMap<>();
        for (JobPosting job : jobs) {
            if (job.getId() != null && !job.getId().isBlank()) {
                jobById.put(job.getId(), job);
            }
        }

        Map<String, Integer> hiredByDept = new LinkedHashMap<>();
        for (ApplicationRecord app : applications) {
            if (!app.isActive() || app.getJobId() == null || app.getJobId().isBlank()) {
                continue;
            }
            if (!"hired".equalsIgnoreCase(trimToEmpty(app.getStatus()))) {
                continue;
            }
            JobPosting job = jobById.get(app.getJobId());
            String dept = departmentLabel(job);
            hiredByDept.put(dept, hiredByDept.getOrDefault(dept, 0) + 1);
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
