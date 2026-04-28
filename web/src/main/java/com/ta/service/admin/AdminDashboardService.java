package com.ta.service.admin;

import com.ta.constant.ErrorCodes;
import com.ta.dto.admin.AdminDashboardJobItemResponse;
import com.ta.dto.admin.AdminDashboardResponse;
import com.ta.dto.admin.AdminDashboardUserItemResponse;
import com.ta.dto.admin.AdminDashboardWorkloadItemResponse;
import com.ta.model.ApplicationRecord;
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
            SystemSettings settings = JsonUtility.loadSystemSettings(context);

            String normalizedStatus = normalizeStatusFilter(statusFilter);
            String normalizedDepartment = normalizeDepartmentFilter(jobs, departmentFilter);
            List<JobPosting> filteredJobs = filterJobs(jobs, normalizedStatus, normalizedDepartment);

            AdminDashboardResponse data = new AdminDashboardResponse();
            data.setTotalUsers(users.size());
            data.setTotalJobs(filteredJobs.size());
            data.setTotalApplications((int) applications.stream().filter(ApplicationRecord::isActive).count());
            data.setUsers(toUsers(users));
            data.setJobs(toJobs(filteredJobs));
            data.setWorkload(toWorkload(applications, jobs, settings.getWorkloadThresholdHours()));
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

    private List<AdminDashboardJobItemResponse> toJobs(List<JobPosting> jobs) {
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
            item.setRecruitmentClosed(Boolean.TRUE.equals(job.getRecruitmentClosed()));
            item.setClosedAt(job.getClosedAt());
            items.add(item);
        }
        return items;
    }

    private List<AdminDashboardWorkloadItemResponse> toWorkload(List<ApplicationRecord> applications,
                                                                List<JobPosting> jobs,
                                                                Integer thresholdHours) {
        int threshold = thresholdHours == null || thresholdHours <= 0 ? 20 : thresholdHours;
        Map<String, Integer> jobHoursById = new LinkedHashMap<>();
        for (JobPosting job : jobs) {
            if (job.getId() != null) {
                jobHoursById.put(job.getId(), JobHoursUtil.resolveWeeklyHours(job));
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
                return created;
            });

            item.setHiredCount(item.getHiredCount() + 1);
            item.setWeeklyHours(item.getWeeklyHours() + jobHours);
            item.setThresholdHours(threshold);
            item.setWarning(item.getWeeklyHours() > threshold);
        }

        List<AdminDashboardWorkloadItemResponse> items = new ArrayList<>(workloadByStudent.values());
        items.sort(Comparator.comparingInt(AdminDashboardWorkloadItemResponse::getWeeklyHours).reversed());
        return items;
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
