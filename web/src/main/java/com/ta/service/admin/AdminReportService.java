package com.ta.service.admin;

import com.ta.constant.ErrorCodes;
import com.ta.model.ApplicationRecord;
import com.ta.model.JobPosting;
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
