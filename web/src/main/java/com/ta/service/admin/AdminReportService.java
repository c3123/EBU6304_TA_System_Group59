package com.ta.service.admin;

import com.ta.constant.ErrorCodes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ta.dto.admin.AdminApplicationArchiveItemResponse;
import com.ta.dto.admin.AdminApplicationArchiveResponse;
import com.ta.dto.admin.AdminDashboardResponse;
import com.ta.dto.admin.AdminDashboardWorkloadItemResponse;
import com.ta.dto.admin.AdminDashboardWorkloadJobResponse;
import com.ta.model.ApplicationRecord;
import com.ta.model.HiringHistoryRecord;
import com.ta.model.JobPosting;
import com.ta.model.NotificationRecord;
import com.ta.model.StudentProfile;
import com.ta.model.SystemSettings;
import com.ta.model.User;
import com.ta.util.JsonUtility;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdminReportService {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final AdminDashboardService adminDashboardService = new AdminDashboardService();
    private final AdminApplicationArchiveService adminApplicationArchiveService = new AdminApplicationArchiveService();

    public String buildWeeklyRecruitmentReport(ServletContext context, String format) {
        String normalizedFormat = normalizeFormat(format);
        try {
            List<JobPosting> jobs = JsonUtility.loadJobs(context);
            List<ApplicationRecord> applications = JsonUtility.loadApplications(context);
            Map<String, Integer> hiredCountByJob = countHiredByJob(applications);

            jobs.sort(Comparator.comparing(JobPosting::getModuleCode, Comparator.nullsLast(String::compareToIgnoreCase))
                    .thenComparing(JobPosting::getTitle, Comparator.nullsLast(String::compareToIgnoreCase)));

            if ("txt".equals(normalizedFormat)) {
                return toTextReport(jobs, hiredCountByJob);
            }
            return toCsvReport(jobs, hiredCountByJob);
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate weekly recruitment report.", e);
        }
    }

    public String resolveFileName(String format) {
        return "weekly-recruitment-report." + normalizeFormat(format);
    }

    public String resolveContentType(String format) {
        String normalizedFormat = normalizeFormat(format);
        if ("txt".equals(normalizedFormat)) {
            return "text/plain;charset=UTF-8";
        }
        return "text/csv;charset=UTF-8";
    }

    public String buildWorkloadReport(ServletContext context, String format) {
        String normalizedFormat = normalizeFormat(format);
        AdminDashboardResponse dashboard = adminDashboardService.loadDashboard(context, "all", "all");
        if ("txt".equals(normalizedFormat)) {
            return workloadToText(dashboard.getWorkload());
        }
        return workloadToCsv(dashboard.getWorkload());
    }

    public String buildApplicationArchiveReport(ServletContext context, String format) {
        String normalizedFormat = normalizeFormat(format);
        AdminApplicationArchiveResponse archive = adminApplicationArchiveService.listArchive(context, "all", "all", "all", "all");
        if ("txt".equals(normalizedFormat)) {
            return applicationsToText(archive.getItems());
        }
        return applicationsToCsv(archive.getItems());
    }

    public String buildBackupJson(ServletContext context) {
        try {
            Map<String, Object> backup = new LinkedHashMap<>();
            List<User> users = JsonUtility.loadUsers(context);
            List<StudentProfile> students = JsonUtility.loadStudents(context);
            List<JobPosting> jobs = JsonUtility.loadJobs(context);
            List<ApplicationRecord> applications = JsonUtility.loadApplications(context);
            List<HiringHistoryRecord> hiringHistory = JsonUtility.loadHiringHistory(context);
            List<NotificationRecord> notifications = JsonUtility.loadNotifications(context);
            SystemSettings settings = JsonUtility.loadSystemSettings(context);
            backup.put("users", users);
            backup.put("students", students);
            backup.put("jobs", jobs);
            backup.put("applications", applications);
            backup.put("hiringHistory", hiringHistory);
            backup.put("notifications", notifications);
            backup.put("systemSettings", settings);
            return GSON.toJson(backup);
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate admin backup.", e);
        }
    }

    private String normalizeFormat(String format) {
        String normalized = format == null ? "" : format.trim().toLowerCase(Locale.ROOT);
        if ("csv".equals(normalized) || "txt".equals(normalized)) {
            return normalized;
        }
        throw new AdminBusinessException(
                ErrorCodes.VALIDATION_ERROR,
                "format must be csv or txt.",
                HttpServletResponse.SC_BAD_REQUEST
        );
    }

    private Map<String, Integer> countHiredByJob(List<ApplicationRecord> applications) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (ApplicationRecord app : applications) {
            if (!"hired".equalsIgnoreCase(app.getStatus())) {
                continue;
            }
            if (app.getJobId() == null || app.getJobId().isBlank()) {
                continue;
            }
            counts.put(app.getJobId(), counts.getOrDefault(app.getJobId(), 0) + 1);
        }
        return counts;
    }

    private String toCsvReport(List<JobPosting> jobs, Map<String, Integer> hiredCountByJob) {
        List<String> lines = new ArrayList<>();
        lines.add("jobId,moduleCode,title,organiser,status,positions,hiredCount,unfilledCount,recruitmentClosed");
        for (JobPosting job : jobs) {
            int hiredCount = hiredCountByJob.getOrDefault(job.getId(), 0);
            int unfilledCount = Math.max(job.getPositions() - hiredCount, 0);
            lines.add(csvRow(
                    job.getId(),
                    job.getModuleCode(),
                    job.getTitle(),
                    job.getTeacherName(),
                    job.getStatus(),
                    String.valueOf(job.getPositions()),
                    String.valueOf(hiredCount),
                    String.valueOf(unfilledCount),
                    String.valueOf(Boolean.TRUE.equals(job.getRecruitmentClosed()))
            ));
        }
        return String.join("\r\n", lines);
    }

    private String toTextReport(List<JobPosting> jobs, Map<String, Integer> hiredCountByJob) {
        List<String> lines = new ArrayList<>();
        lines.add("Weekly Recruitment Report");
        lines.add("");
        for (JobPosting job : jobs) {
            int hiredCount = hiredCountByJob.getOrDefault(job.getId(), 0);
            int unfilledCount = Math.max(job.getPositions() - hiredCount, 0);
            lines.add("Job ID: " + safe(job.getId()));
            lines.add("Module: " + safe(job.getModuleCode()));
            lines.add("Title: " + safe(job.getTitle()));
            lines.add("Organiser: " + safe(job.getTeacherName()));
            lines.add("Status: " + safe(job.getStatus()));
            lines.add("Positions: " + job.getPositions());
            lines.add("Hired Count: " + hiredCount);
            lines.add("Unfilled Count: " + unfilledCount);
            lines.add("Recruitment Closed: " + Boolean.TRUE.equals(job.getRecruitmentClosed()));
            lines.add("");
        }
        return String.join(System.lineSeparator(), lines);
    }

    private String workloadToCsv(List<AdminDashboardWorkloadItemResponse> workload) {
        List<String> lines = new ArrayList<>();
        lines.add("studentId,studentName,hiredCount,weeklyHours,thresholdHours,workloadLevel,assignedJobs");
        for (AdminDashboardWorkloadItemResponse item : workload) {
            lines.add(csvRow(
                    item.getStudentId(),
                    item.getStudentName(),
                    String.valueOf(item.getHiredCount()),
                    String.valueOf(item.getWeeklyHours()),
                    String.valueOf(item.getThresholdHours()),
                    item.getWorkloadLabel(),
                    assignedJobsText(item.getAssignedJobs())
            ));
        }
        return String.join("\r\n", lines);
    }

    private String workloadToText(List<AdminDashboardWorkloadItemResponse> workload) {
        List<String> lines = new ArrayList<>();
        lines.add("TA Workload Report");
        lines.add("");
        for (AdminDashboardWorkloadItemResponse item : workload) {
            lines.add("Student: " + safe(item.getStudentName()) + " (" + safe(item.getStudentId()) + ")");
            lines.add("Weekly Hours: " + item.getWeeklyHours());
            lines.add("Level: " + safe(item.getWorkloadLabel()));
            lines.add("Assigned Jobs: " + assignedJobsText(item.getAssignedJobs()));
            lines.add("");
        }
        return String.join(System.lineSeparator(), lines);
    }

    private String applicationsToCsv(List<AdminApplicationArchiveItemResponse> items) {
        List<String> lines = new ArrayList<>();
        lines.add("applicationId,jobId,moduleCode,title,organiser,studentId,studentNo,studentName,status,appliedAt,evaluationNotes,decisionFeedback");
        for (AdminApplicationArchiveItemResponse item : items) {
            lines.add(csvRow(
                    item.getApplicationId(),
                    item.getJobId(),
                    item.getModuleCode(),
                    item.getTitle(),
                    item.getTeacherName(),
                    item.getStudentId(),
                    item.getStudentNo(),
                    item.getStudentName(),
                    item.getStatus(),
                    item.getAppliedAt(),
                    item.getEvaluationNotes(),
                    item.getDecisionFeedback()
            ));
        }
        return String.join("\r\n", lines);
    }

    private String applicationsToText(List<AdminApplicationArchiveItemResponse> items) {
        List<String> lines = new ArrayList<>();
        lines.add("Application Archive Report");
        lines.add("");
        for (AdminApplicationArchiveItemResponse item : items) {
            lines.add("Application ID: " + safe(item.getApplicationId()));
            lines.add("Student: " + safe(item.getStudentName()) + " (" + safe(item.getStudentNo()) + ")");
            lines.add("Job: " + safe(item.getModuleCode()) + " " + safe(item.getTitle()));
            lines.add("Organiser: " + safe(item.getTeacherName()));
            lines.add("Status: " + safe(item.getStatus()));
            lines.add("Applied At: " + safe(item.getAppliedAt()));
            lines.add("Evaluation Notes: " + safe(item.getEvaluationNotes()));
            lines.add("Decision Feedback: " + safe(item.getDecisionFeedback()));
            lines.add("");
        }
        return String.join(System.lineSeparator(), lines);
    }

    private String assignedJobsText(List<AdminDashboardWorkloadJobResponse> jobs) {
        if (jobs == null || jobs.isEmpty()) {
            return "";
        }
        List<String> parts = new ArrayList<>();
        for (AdminDashboardWorkloadJobResponse job : jobs) {
            parts.add(safe(job.getModuleCode()) + " " + safe(job.getTitle()) + " (" + job.getWeeklyHours() + "h/week)");
        }
        return String.join("; ", parts);
    }

    private String csvRow(String... values) {
        List<String> escaped = new ArrayList<>();
        for (String value : values) {
            String safeValue = safe(value).replace("\"", "\"\"");
            escaped.add("\"" + safeValue + "\"");
        }
        return String.join(",", escaped);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
