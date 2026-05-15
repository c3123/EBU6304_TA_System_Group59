package com.ta.service.admin;

import com.ta.dto.admin.AdminRecruitmentOutcomeResponse;
import com.ta.model.ApplicationRecord;
import com.ta.model.JobPosting;
import com.ta.util.JsonUtility;
import jakarta.servlet.ServletContext;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Aggregates jobs and applications for the leadership recruitment-outcome dashboard.
 * Counting rules align with {@link AdminDashboardService#populateOverview} for consistency.
 */
public class AdminRecruitmentOutcomeService {

    public AdminRecruitmentOutcomeResponse load(ServletContext context) throws IOException {
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

        AdminRecruitmentOutcomeResponse response = new AdminRecruitmentOutcomeResponse();
        response.setTotalPositionSlots(totalPositionSlots);
        response.setClosedJobs(closedJobs);
        response.setRecruitingJobs(recruitingJobs);
        response.setTotalApplications(totalApplications);
        response.setTotalHired(totalHired);
        response.setTotalVacancies(totalVacancies);
        return response;
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
