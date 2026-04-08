package com.ta.service.admin;

import com.ta.constant.ErrorCodes;
import com.ta.dto.admin.AdminDemandReviewResponse;
import com.ta.model.JobPosting;
import com.ta.util.JsonUtility;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

public class AdminDemandReviewService {

    public AdminDemandReviewResponse reviewDemand(ServletContext context, String jobId, String action) {
        String normalizedAction = normalizeAction(action);
        String targetStatus = toApprovalStatus(normalizedAction);

        try {
            List<JobPosting> jobs = JsonUtility.loadJobs(context);
            JobPosting job = jobs.stream()
                    .filter(j -> jobId.equals(j.getId()))
                    .findFirst()
                    .orElseThrow(() -> new AdminBusinessException(
                            ErrorCodes.JOB_NOT_FOUND,
                            "Demand not found.",
                            HttpServletResponse.SC_NOT_FOUND
                    ));

            if (!isDemandRecord(job)) {
                throw new AdminBusinessException(
                        ErrorCodes.VALIDATION_ERROR,
                        "Only demand records can be reviewed.",
                        HttpServletResponse.SC_BAD_REQUEST
                );
            }

            if (!"pending".equalsIgnoreCase(job.getApprovalStatus())) {
                throw new AdminBusinessException(
                        ErrorCodes.VALIDATION_ERROR,
                        "Only pending demands can be reviewed.",
                        HttpServletResponse.SC_BAD_REQUEST
                );
            }

            String now = Instant.now().toString();
            job.setApprovalStatus(targetStatus);
            if (job.getPublished() == null) {
                job.setPublished(false);
            }
            if (job.getWithdrawn() == null) {
                job.setWithdrawn(false);
            }
            job.setUpdatedAt(now);

            JsonUtility.saveJobs(context, jobs);

            AdminDemandReviewResponse response = new AdminDemandReviewResponse();
            response.setJobId(job.getId());
            response.setAction(normalizedAction);
            response.setApprovalStatus(job.getApprovalStatus());
            response.setPublished(job.getPublished());
            response.setWithdrawn(job.getWithdrawn());
            response.setReviewedAt(now);
            return response;
        } catch (IOException e) {
            throw new RuntimeException("Failed to review demand.", e);
        }
    }

    private String normalizeAction(String action) {
        if (action == null) {
            throw new AdminBusinessException(
                    ErrorCodes.VALIDATION_ERROR,
                    "action is required.",
                    HttpServletResponse.SC_BAD_REQUEST
            );
        }

        String normalized = action.trim().toLowerCase();
        if (!"approve".equals(normalized) && !"reject".equals(normalized)) {
            throw new AdminBusinessException(
                    ErrorCodes.VALIDATION_ERROR,
                    "action must be approve or reject.",
                    HttpServletResponse.SC_BAD_REQUEST
            );
        }
        return normalized;
    }

    private String toApprovalStatus(String normalizedAction) {
        return "approve".equals(normalizedAction) ? "approved" : "rejected";
    }

    private boolean isDemandRecord(JobPosting job) {
        return job.getApprovalStatus() != null || job.getPublished() != null || job.getWithdrawn() != null;
    }
}
