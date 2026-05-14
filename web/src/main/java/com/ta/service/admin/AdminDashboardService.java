package com.ta.service.admin;

import com.ta.constant.ErrorCodes;
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

            AdminDashboardResponse data = new AdminDashboardResponse();
            data.setTotalUsers(users.size());
            data.setTotalJobs(filteredJobs.size());
            data.setTotalApplications((int) applications.stream().filter(ApplicationRecord::isActive).count());
            data.setUsers(toUsers(users));
            data.setJobs(toJobs(filteredJobs, applications));
            data.setWorkload(toWorkload(applications, jobs, settings.getWorkloadThresholdHours(), hiringHistory));
            return data;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load admin dashboard.", e);
        }
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
        Map<String, Integer> applicantCountByJob = new LinkedHashMap<>();
        Map<String, Integer> hiredCountByJob = new LinkedHashMap<>();
        for (ApplicationRecord app : applications) {
            if (!app.isActive() || app.getJobId() == null || app.getJobId().isBlank()) {
                continue;
            }
            applicantCountByJob.put(app.getJobId(), applicantCountByJob.getOrDefault(app.getJobId(), 0) + 1);
            if ("hired".equalsIgnoreCase(app.getStatus())) {
                hiredCountByJob.put(app.getJobId(), hiredCountByJob.getOrDefault(app.getJobId(), 0) + 1);
            }
        }

        List<AdminDashboardJobItemResponse> items = new ArrayList<>();
        for (JobPosting job : jobs) {
            AdminDashboardJobItemResponse item = new AdminDashboardJobItemResponse();
            item.setId(job.getId());
            item.setModuleCode(job.getModuleCode());
            item.setTitle(job.getTitle());
            item.setTeacherName(job.getTeacherName());
            item.setDepartment(job.getDepartment());
            item.setStatus(job.getStatus());
            item.setPositions(job.getPositions());
            item.setApplicantCount(applicantCountByJob.getOrDefault(job.getId(), 0));
            item.setHiredCount(hiredCountByJob.getOrDefault(job.getId(), 0));
            item.setWeeklyHours(JobHoursUtil.resolveWeeklyHours(job));
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
        row.setModuleCode("\u2014");
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

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
