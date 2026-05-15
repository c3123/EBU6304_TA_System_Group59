package com.ta.service.admin;

import com.ta.constant.ErrorCodes;
import com.ta.dto.admin.AdminDashboardAlertResponse;
import com.ta.dto.admin.AdminDashboardJobItemResponse;
import com.ta.dto.admin.AdminDashboardResponse;
import com.ta.dto.admin.AdminDashboardUserItemResponse;
import com.ta.dto.admin.AdminDashboardWorkloadJobResponse;
import com.ta.dto.admin.AdminDashboardWorkloadItemResponse;
import com.ta.model.ApplicationRecord;
import com.ta.model.HiringHistoryRecord;
import com.ta.model.JobPosting;
import com.ta.model.SystemSettings;
import com.ta.model.User;
import com.ta.util.JobHoursUtil;
import com.ta.util.JsonUtility;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class AdminDashboardService {
    private static final Set<String> ALLOWED_STATUS_FILTERS = Set.of("all", "draft", "open", "closed", "withdrawn");
    private static final int DEADLINE_WARNING_DAYS = 7;

    public AdminDashboardResponse loadDashboard(ServletContext context, String statusFilter, String departmentFilter) {
        try {
            List<User> users = JsonUtility.loadUsers(context);
            List<JobPosting> jobs = JsonUtility.loadJobs(context);
            List<ApplicationRecord> applications = JsonUtility.loadApplications(context);
            List<HiringHistoryRecord> hiringHistory = JsonUtility.loadHiringHistory(context);
            SystemSettings settings = JsonUtility.loadSystemSettings(context);

            String normalizedStatus = normalizeStatusFilter(statusFilter);
            String normalizedDepartment = normalizeDepartmentFilter(jobs, departmentFilter);
            List<JobPosting> filteredJobs = filterJobs(jobs, normalizedStatus, normalizedDepartment);
            List<AdminDashboardWorkloadItemResponse> workload = toWorkload(applications, jobs, settings.getWorkloadThresholdHours(), hiringHistory);

            AdminDashboardResponse data = new AdminDashboardResponse();
            data.setTotalUsers(users.size());
            data.setTotalJobs(jobs.size());
            data.setTotalApplications((int) applications.stream().filter(ApplicationRecord::isActive).count());
            populateOverview(data, users, jobs, applications, workload);
            data.setUsers(toUsers(users));
            data.setJobs(toJobs(filteredJobs, applications));
            data.setWorkload(workload);
            data.setAlerts(buildAlerts(jobs, applications, workload, settings.getWorkloadThresholdHours()));
            return data;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load admin dashboard.", e);
        }
    }

    private void populateOverview(AdminDashboardResponse data,
                                  List<User> users,
                                  List<JobPosting> jobs,
                                  List<ApplicationRecord> applications,
                                  List<AdminDashboardWorkloadItemResponse> workload) {
        int students = 0;
        int teachers = 0;
        int admins = 0;
        for (User user : users) {
            String role = trimToEmpty(user.getRole()).toLowerCase(Locale.ROOT);
            if ("student".equals(role)) {
                students++;
            } else if ("teacher".equals(role)) {
                teachers++;
            } else if ("admin".equals(role)) {
                admins++;
            }
        }

        Map<String, Integer> hiredCountByJob = countHiredByJob(applications);
        int activeJobs = 0;
        int closedJobs = 0;
        int draftJobs = 0;
        int withdrawnJobs = 0;
        int unfilledPositions = 0;
        for (JobPosting job : jobs) {
            if (Boolean.TRUE.equals(job.getWithdrawn())) {
                withdrawnJobs++;
                continue;
            }
            if (isClosed(job)) {
                closedJobs++;
            } else if ("draft".equalsIgnoreCase(trimToEmpty(job.getStatus()))) {
                draftJobs++;
            } else if ("open".equalsIgnoreCase(trimToEmpty(job.getStatus()))) {
                activeJobs++;
            }
            unfilledPositions += Math.max(job.getPositions() - hiredCountByJob.getOrDefault(job.getId(), 0), 0);
        }

        int hiredRecords = 0;
        int openApplications = 0;
        for (ApplicationRecord application : applications) {
            if (!application.isActive()) {
                continue;
            }
            String status = trimToEmpty(application.getStatus()).toLowerCase(Locale.ROOT);
            if ("hired".equals(status)) {
                hiredRecords++;
            }
            if (Set.of("pending", "viewed", "shortlisted").contains(status)) {
                openApplications++;
            }
        }

        int warningStudents = 0;
        int overloadedStudents = 0;
        for (AdminDashboardWorkloadItemResponse item : workload) {
            if ("warning".equalsIgnoreCase(item.getWorkloadLevel())) {
                warningStudents++;
            }
            if ("overload".equalsIgnoreCase(item.getWorkloadLevel())) {
                overloadedStudents++;
            }
        }

        data.setTotalStudents(students);
        data.setTotalTeachers(teachers);
        data.setTotalAdmins(admins);
        data.setTotalActiveJobs(activeJobs);
        data.setTotalClosedJobs(closedJobs);
        data.setTotalDraftJobs(draftJobs);
        data.setTotalWithdrawnJobs(withdrawnJobs);
        data.setTotalHiredRecords(hiredRecords);
        data.setTotalOpenApplications(openApplications);
        data.setTotalUnfilledPositions(unfilledPositions);
        data.setTotalWarningStudents(warningStudents);
        data.setTotalOverloadedStudents(overloadedStudents);
    }

    private String normalizeStatusFilter(String statusFilter) {
        String normalized = trimToEmpty(statusFilter).toLowerCase(Locale.ROOT);
        if (normalized.isBlank()) {
            return "all";
        }
        if (!ALLOWED_STATUS_FILTERS.contains(normalized)) {
            throw new AdminBusinessException(
                    ErrorCodes.VALIDATION_ERROR,
                    "status must be all, draft, open, closed, or withdrawn.",
                    HttpServletResponse.SC_BAD_REQUEST
            );
        }
        return normalized;
    }

    private String normalizeDepartmentFilter(List<JobPosting> jobs, String departmentFilter) {
        String normalized = trimToEmpty(departmentFilter);
        if (normalized.isBlank() || "all".equalsIgnoreCase(normalized)) {
            return "all";
        }

        Set<String> available = new LinkedHashSet<>();
        for (JobPosting job : jobs) {
            String department = trimToEmpty(job.getDepartment());
            if (!department.isBlank()) {
                available.add(department.toLowerCase(Locale.ROOT));
            }
        }

        if (!available.contains(normalized.toLowerCase(Locale.ROOT))) {
            throw new AdminBusinessException(
                    ErrorCodes.VALIDATION_ERROR,
                    "department filter is not supported by the current job data.",
                    HttpServletResponse.SC_BAD_REQUEST
            );
        }
        return normalized;
    }

    private List<JobPosting> filterJobs(List<JobPosting> jobs, String statusFilter, String departmentFilter) {
        List<JobPosting> filtered = new ArrayList<>();
        for (JobPosting job : jobs) {
            if (!matchesStatus(job, statusFilter)) {
                continue;
            }
            if (!matchesDepartment(job, departmentFilter)) {
                continue;
            }
            filtered.add(job);
        }
        filtered.sort(Comparator.comparing(JobPosting::getModuleCode, Comparator.nullsLast(String::compareToIgnoreCase))
                .thenComparing(JobPosting::getTitle, Comparator.nullsLast(String::compareToIgnoreCase)));
        return filtered;
    }

    private boolean matchesStatus(JobPosting job, String statusFilter) {
        if ("all".equals(statusFilter)) {
            return true;
        }

        boolean withdrawn = Boolean.TRUE.equals(job.getWithdrawn());
        boolean closed = Boolean.TRUE.equals(job.getRecruitmentClosed()) || "closed".equalsIgnoreCase(trimToEmpty(job.getStatus()));
        String status = trimToEmpty(job.getStatus()).toLowerCase(Locale.ROOT);

        switch (statusFilter) {
            case "withdrawn":
                return withdrawn;
            case "closed":
                return closed;
            case "draft":
                return !withdrawn && !closed && "draft".equals(status);
            case "open":
                return !withdrawn && !closed && "open".equals(status);
            default:
                return true;
        }
    }

    private boolean matchesDepartment(JobPosting job, String departmentFilter) {
        if ("all".equalsIgnoreCase(departmentFilter)) {
            return true;
        }
        return trimToEmpty(job.getDepartment()).equalsIgnoreCase(departmentFilter);
    }

    private List<AdminDashboardUserItemResponse> toUsers(List<User> users) {
        List<AdminDashboardUserItemResponse> items = new ArrayList<>();
        for (User user : users) {
            AdminDashboardUserItemResponse item = new AdminDashboardUserItemResponse();
            item.setId(user.getId());
            item.setName(user.getName());
            item.setEmail(user.getEmail());
            item.setRole(user.getRole());
            items.add(item);
        }
        return items;
    }

    private List<AdminDashboardJobItemResponse> toJobs(List<JobPosting> jobs, List<ApplicationRecord> applications) {
        Map<String, Integer> applicantCountByJob = countApplicantsByJob(applications);
        Map<String, Integer> hiredCountByJob = countHiredByJob(applications);

        List<AdminDashboardJobItemResponse> items = new ArrayList<>();
        for (JobPosting job : jobs) {
            int positions = job.getPositions();
            int applicantCount = applicantCountByJob.getOrDefault(job.getId(), 0);
            int hiredCount = hiredCountByJob.getOrDefault(job.getId(), 0);
            int unfilledCount = Math.max(positions - hiredCount, 0);
            AdminDashboardJobItemResponse item = new AdminDashboardJobItemResponse();
            item.setId(job.getId());
            item.setModuleCode(job.getModuleCode());
            item.setTitle(job.getTitle());
            item.setTeacherName(job.getTeacherName());
            item.setDepartment(job.getDepartment());
            item.setStatus(job.getStatus());
            item.setPositions(positions);
            item.setApplicantCount(applicantCount);
            item.setHiredCount(hiredCount);
            item.setUnfilledCount(unfilledCount);
            item.setFilledLabel(hiredCount + "/" + positions + " Filled");
            item.setWeeklyHours(JobHoursUtil.resolveWeeklyHours(job));
            item.setDaysUntilDeadline(resolveDaysUntilDeadline(job.getDeadline()));
            applyJobHealth(item, job, applicantCount, unfilledCount);
            item.setDeadline(job.getDeadline());
            item.setPublishedAt(job.getPublishedAt());
            item.setCreatedAt(job.getCreatedAt());
            item.setRequirements(job.getRequirements());
            item.setSchedule(job.getSchedule());
            item.setLocation(job.getLocation());
            item.setRecruitmentClosed(Boolean.TRUE.equals(job.getRecruitmentClosed()));
            item.setClosedAt(job.getClosedAt());
            items.add(item);
        }
        return items;
    }

    private List<AdminDashboardWorkloadItemResponse> toWorkload(List<ApplicationRecord> applications,
                                                                List<JobPosting> jobs,
                                                                Integer thresholdHours,
                                                                List<HiringHistoryRecord> hiringHistory) {
        int threshold = thresholdHours;
        Map<String, String> hiredAtByApplicationId = latestHiredSubmittedAtByApplicationId(hiringHistory);
        Map<String, Integer> jobHoursById = new LinkedHashMap<>();
        Map<String, JobPosting> jobById = new LinkedHashMap<>();
        for (JobPosting job : jobs) {
            if (job.getId() != null) {
                jobHoursById.put(job.getId(), JobHoursUtil.resolveWeeklyHours(job));
                jobById.put(job.getId(), job);
            }
        }

        Map<String, AdminDashboardWorkloadItemResponse> workloadByStudent = new LinkedHashMap<>();
        for (ApplicationRecord app : applications) {
            if (!"hired".equalsIgnoreCase(app.getStatus())) {
                continue;
            }
            if (app.getStudentId() == null || app.getStudentId().isBlank()) {
                continue;
            }

            int jobHours = jobHoursById.getOrDefault(app.getJobId(), 0);
            AdminDashboardWorkloadItemResponse item = workloadByStudent.computeIfAbsent(app.getStudentId(), key -> {
                AdminDashboardWorkloadItemResponse created = new AdminDashboardWorkloadItemResponse();
                created.setStudentId(key);
                created.setStudentName(app.getStudentName() == null ? key : app.getStudentName());
                created.setHiredCount(0);
                created.setWeeklyHours(0);
                created.setThresholdHours(threshold);
                created.setWarning(false);
                applyWorkloadLevel(created, threshold);
                return created;
            });

            item.setHiredCount(item.getHiredCount() + 1);
            item.setWeeklyHours(item.getWeeklyHours() + jobHours);
            item.setThresholdHours(threshold);
            JobPosting job = jobById.get(app.getJobId());
            if (job != null) {
                item.getAssignedJobs().add(toWorkloadJob(job, jobHours, app, hiredAtByApplicationId));
            } else if (app.getJobId() != null && !app.getJobId().isBlank()) {
                item.getAssignedJobs().add(toWorkloadJobMissing(app, hiredAtByApplicationId));
            }
            applyWorkloadLevel(item, threshold);
        }

        List<AdminDashboardWorkloadItemResponse> items = new ArrayList<>(workloadByStudent.values());
        items.sort(Comparator.comparingInt(AdminDashboardWorkloadItemResponse::getWeeklyHours).reversed());
        return items;
    }

    private Map<String, Integer> countApplicantsByJob(List<ApplicationRecord> applications) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (ApplicationRecord app : applications) {
            if (!app.isActive() || app.getJobId() == null || app.getJobId().isBlank()) {
                continue;
            }
            counts.put(app.getJobId(), counts.getOrDefault(app.getJobId(), 0) + 1);
        }
        return counts;
    }

    private Map<String, Integer> countHiredByJob(List<ApplicationRecord> applications) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (ApplicationRecord app : applications) {
            if (!app.isActive() || app.getJobId() == null || app.getJobId().isBlank()) {
                continue;
            }
            if ("hired".equalsIgnoreCase(app.getStatus())) {
                counts.put(app.getJobId(), counts.getOrDefault(app.getJobId(), 0) + 1);
            }
        }
        return counts;
    }

    private void applyJobHealth(AdminDashboardJobItemResponse item,
                                JobPosting job,
                                int applicantCount,
                                int unfilledCount) {
        if (Boolean.TRUE.equals(job.getWithdrawn())) {
            item.setHealthLevel("inactive");
            item.setHealthLabel("Withdrawn");
            return;
        }
        if (isClosed(job)) {
            item.setHealthLevel(unfilledCount > 0 ? "closed-unfilled" : "complete");
            item.setHealthLabel(unfilledCount > 0 ? "Closed with Vacancy" : "Complete");
            return;
        }
        if (!"open".equalsIgnoreCase(trimToEmpty(job.getStatus()))) {
            item.setHealthLevel("draft");
            item.setHealthLabel("Draft");
            return;
        }
        Integer days = item.getDaysUntilDeadline();
        if (unfilledCount > 0 && days != null && days < 0) {
            item.setHealthLevel("overdue");
            item.setHealthLabel("Overdue Vacancy");
            return;
        }
        if (unfilledCount > 0 && applicantCount == 0) {
            item.setHealthLevel("no-applicants");
            item.setHealthLabel("No Applicants");
            return;
        }
        if (unfilledCount > 0 && days != null && days <= DEADLINE_WARNING_DAYS) {
            item.setHealthLevel("deadline-risk");
            item.setHealthLabel("Deadline Risk");
            return;
        }
        if (unfilledCount > 0) {
            item.setHealthLevel("unfilled");
            item.setHealthLabel("Unfilled");
            return;
        }
        item.setHealthLevel("healthy");
        item.setHealthLabel("Filled");
    }

    private AdminDashboardWorkloadJobResponse toWorkloadJob(JobPosting job,
                                                            int weeklyHours,
                                                            ApplicationRecord app,
                                                            Map<String, String> hiredAtByApplicationId) {
        AdminDashboardWorkloadJobResponse row = new AdminDashboardWorkloadJobResponse();
        row.setApplicationId(app != null ? app.getId() : null);
        row.setJobId(job.getId());
        row.setModuleCode(job.getModuleCode());
        row.setTitle(job.getTitle());
        row.setWeeklyHours(weeklyHours);
        row.setHiredAt(resolveHiredAtForWorkloadRow(app, hiredAtByApplicationId));
        return row;
    }

    private AdminDashboardWorkloadJobResponse toWorkloadJobMissing(ApplicationRecord app,
                                                                   Map<String, String> hiredAtByApplicationId) {
        AdminDashboardWorkloadJobResponse row = new AdminDashboardWorkloadJobResponse();
        row.setApplicationId(app != null ? app.getId() : null);
        row.setJobId(app != null ? app.getJobId() : null);
        row.setModuleCode("-");
        String jobId = app != null ? trimToEmpty(app.getJobId()) : "";
        row.setTitle(jobId.isEmpty() ? "Job record missing" : "Job record missing (" + jobId + ")");
        row.setWeeklyHours(0);
        row.setHiredAt(resolveHiredAtForWorkloadRow(app, hiredAtByApplicationId));
        return row;
    }

    private Map<String, String> latestHiredSubmittedAtByApplicationId(List<HiringHistoryRecord> history) {
        Map<String, String> best = new HashMap<>();
        if (history == null) {
            return best;
        }
        for (HiringHistoryRecord rec : history) {
            if (rec == null || rec.getHiredApplicationIds() == null) {
                continue;
            }
            String submittedAt = trimToEmpty(rec.getSubmittedAt());
            if (submittedAt.isEmpty()) {
                continue;
            }
            for (String applicationId : rec.getHiredApplicationIds()) {
                if (applicationId == null || applicationId.isBlank()) {
                    continue;
                }
                String prev = best.get(applicationId);
                if (prev == null || submittedAt.compareTo(prev) > 0) {
                    best.put(applicationId, submittedAt);
                }
            }
        }
        return best;
    }

    private String resolveHiredAtForWorkloadRow(ApplicationRecord app, Map<String, String> hiredAtByApplicationId) {
        if (app == null) {
            return "";
        }
        if (app.getId() != null && !app.getId().isBlank()) {
            String fromHistory = trimToEmpty(hiredAtByApplicationId.get(app.getId()));
            if (!fromHistory.isEmpty()) {
                return fromHistory;
            }
        }
        return trimToEmpty(app.getAppliedAt());
    }

    private void applyWorkloadLevel(AdminDashboardWorkloadItemResponse item, int threshold) {
        int hours = item.getWeeklyHours();
        if (hours >= threshold) {
            item.setWorkloadLevel("overload");
            item.setWorkloadLabel("Overload");
            item.setWarning(true);
            return;
        }
        if (hours >= 15) {
            item.setWorkloadLevel("warning");
            item.setWorkloadLabel("Warning");
            item.setWarning(true);
            return;
        }
        if (hours >= 10) {
            item.setWorkloadLevel("normal");
            item.setWorkloadLabel("Normal");
            item.setWarning(false);
            return;
        }
        item.setWorkloadLevel("low");
        item.setWorkloadLabel("Low");
        item.setWarning(false);
    }

    private List<AdminDashboardAlertResponse> buildAlerts(List<JobPosting> jobs,
                                                           List<ApplicationRecord> applications,
                                                           List<AdminDashboardWorkloadItemResponse> workload,
                                                           int threshold) {
        List<AdminDashboardAlertResponse> alerts = new ArrayList<>();
        for (AdminDashboardWorkloadItemResponse item : workload) {
            if ("overload".equalsIgnoreCase(item.getWorkloadLevel())) {
                alerts.add(alert(
                        "workload-overload-" + safeId(item.getStudentId()),
                        "workload",
                        "danger",
                        "Overloaded TA",
                        item.getStudentName() + " has " + item.getWeeklyHours() + " hours/week, meeting or exceeding the " + threshold + "h threshold.",
                        "student",
                        item.getStudentId(),
                        item.getStudentName(),
                        null
                ));
            } else if ("warning".equalsIgnoreCase(item.getWorkloadLevel())) {
                alerts.add(alert(
                        "workload-warning-" + safeId(item.getStudentId()),
                        "workload",
                        "warning",
                        "Workload warning",
                        item.getStudentName() + " has " + item.getWeeklyHours() + " hours/week.",
                        "student",
                        item.getStudentId(),
                        item.getStudentName(),
                        null
                ));
            }
        }

        Map<String, Integer> applicantCountByJob = countApplicantsByJob(applications);
        Map<String, Integer> hiredCountByJob = countHiredByJob(applications);
        for (JobPosting job : jobs) {
            if (Boolean.TRUE.equals(job.getWithdrawn()) || isClosed(job) || !"open".equalsIgnoreCase(trimToEmpty(job.getStatus()))) {
                continue;
            }
            int applicants = applicantCountByJob.getOrDefault(job.getId(), 0);
            int hired = hiredCountByJob.getOrDefault(job.getId(), 0);
            int unfilled = Math.max(job.getPositions() - hired, 0);
            Integer days = resolveDaysUntilDeadline(job.getDeadline());
            String jobLabel = safeText(job.getModuleCode()) + " " + safeText(job.getTitle()).trim();
            if (unfilled > 0 && applicants == 0) {
                alerts.add(alert(
                        "job-no-applicants-" + safeId(job.getId()),
                        "recruitment",
                        "warning",
                        "Job has no applicants",
                        jobLabel.trim() + " has " + unfilled + " unfilled position(s) and no active applicants.",
                        "job",
                        job.getId(),
                        job.getTeacherName(),
                        job.getDeadline()
                ));
            }
            if (unfilled > 0 && days != null && days < 0) {
                alerts.add(alert(
                        "job-overdue-" + safeId(job.getId()),
                        "deadline",
                        "danger",
                        "Deadline passed with vacancy",
                        jobLabel.trim() + " still has " + unfilled + " unfilled position(s).",
                        "job",
                        job.getId(),
                        job.getTeacherName(),
                        job.getDeadline()
                ));
            } else if (unfilled > 0 && days != null && days <= DEADLINE_WARNING_DAYS) {
                alerts.add(alert(
                        "job-deadline-" + safeId(job.getId()),
                        "deadline",
                        "warning",
                        "Deadline approaching",
                        jobLabel.trim() + " has " + unfilled + " unfilled position(s) and " + days + " day(s) until deadline.",
                        "job",
                        job.getId(),
                        job.getTeacherName(),
                        job.getDeadline()
                ));
            }
            if (trimToEmpty(job.getDepartment()).isBlank() || trimToEmpty(job.getSchedule()).isBlank()) {
                alerts.add(alert(
                        "job-incomplete-" + safeId(job.getId()),
                        "data-quality",
                        "info",
                        "Incomplete job metadata",
                        jobLabel.trim() + " is missing department or schedule data.",
                        "job",
                        job.getId(),
                        job.getTeacherName(),
                        job.getDeadline()
                ));
            }
        }
        alerts.sort(Comparator.comparingInt(this::severityRank).thenComparing(AdminDashboardAlertResponse::getTitle));
        return alerts;
    }

    private AdminDashboardAlertResponse alert(String id,
                                              String type,
                                              String severity,
                                              String title,
                                              String message,
                                              String targetType,
                                              String targetId,
                                              String ownerName,
                                              String dueDate) {
        AdminDashboardAlertResponse alert = new AdminDashboardAlertResponse();
        alert.setId(id);
        alert.setType(type);
        alert.setSeverity(severity);
        alert.setTitle(title);
        alert.setMessage(message);
        alert.setTargetType(targetType);
        alert.setTargetId(targetId);
        alert.setOwnerName(ownerName);
        alert.setDueDate(dueDate);
        return alert;
    }

    private int severityRank(AdminDashboardAlertResponse alert) {
        String severity = trimToEmpty(alert.getSeverity()).toLowerCase(Locale.ROOT);
        if ("danger".equals(severity)) {
            return 0;
        }
        if ("warning".equals(severity)) {
            return 1;
        }
        return 2;
    }

    private boolean isClosed(JobPosting job) {
        return Boolean.TRUE.equals(job.getRecruitmentClosed()) || "closed".equalsIgnoreCase(trimToEmpty(job.getStatus()));
    }

    private Integer resolveDaysUntilDeadline(String deadline) {
        String value = trimToEmpty(deadline);
        if (value.isBlank()) {
            return null;
        }
        try {
            String datePart = value.length() >= 10 ? value.substring(0, 10) : value;
            LocalDate date = LocalDate.parse(datePart);
            return (int) java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), date);
        } catch (DateTimeParseException | StringIndexOutOfBoundsException ex) {
            return null;
        }
    }

    private String safeId(String value) {
        String safe = trimToEmpty(value);
        return safe.isBlank() ? "unknown" : safe.replaceAll("[^A-Za-z0-9_-]", "_");
    }

    private String safeText(String value) {
        return value == null ? "" : value + " ";
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
