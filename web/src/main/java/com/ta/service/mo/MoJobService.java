package com.ta.service.mo;

import com.ta.constant.ErrorCodes;
import com.ta.dto.mo.MoJobPublishRequest;
import com.ta.dto.mo.MoJobPublishResponse;
import com.ta.dto.mo.MoJobWithdrawResponse;
import com.ta.model.ApplicationRecord;
import com.ta.model.JobPosting;
import com.ta.util.JsonUtility;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

/**
 * MO job lifecycle service boundary.
 *
 * Ownership: A side (publish/withdraw rules).
 * Rules:
 * 1) Only approved demand can be published.
 * 2) Deadline cannot be modified after publish.
 * 3) Job can be withdrawn only when no active applications exist.
 */
public class MoJobService {

    public MoJobPublishResponse publishJob(ServletContext context, String moId, String jobId, MoJobPublishRequest request) {
        validatePublishRequest(request);

        try {
            List<JobPosting> jobs = JsonUtility.loadJobs(context);
            JobPosting job = findOwnedJob(jobs, moId, jobId);

            if (!"approved".equalsIgnoreCase(job.getApprovalStatus())) {
                throw new MoBusinessException(
                        ErrorCodes.JOB_NOT_APPROVED,
                        "Only approved demand can be published.",
                        HttpServletResponse.SC_BAD_REQUEST
                );
            }

            if (Boolean.TRUE.equals(job.getPublished())) {
                throw new MoBusinessException(
                        ErrorCodes.JOB_ALREADY_PUBLISHED,
                        "Job has already been published.",
                        HttpServletResponse.SC_BAD_REQUEST
                );
            }

            if (Boolean.TRUE.equals(job.getWithdrawn())) {
                throw new MoBusinessException(
                        ErrorCodes.VALIDATION_ERROR,
                        "Withdrawn job cannot be published.",
                        HttpServletResponse.SC_BAD_REQUEST
                );
            }

            String now = Instant.now().toString();
            job.setLocation(request.getLocation());
            job.setRequirements(request.getRequirements());
            job.setDeadline(request.getDeadline());
            job.setPublished(true);
            job.setUpdatedAt(now);

            JsonUtility.saveJobs(context, jobs);

            MoJobPublishResponse response = new MoJobPublishResponse();
            response.setJobId(job.getId());
            response.setPublished(true);
            response.setPublishedAt(now);
            response.setDeadline(job.getDeadline());
            response.setLocation(job.getLocation());
            response.setRequirements(job.getRequirements());
            return response;
        } catch (IOException e) {
            throw new RuntimeException("Failed to publish job.", e);
        }
    }

    public MoJobWithdrawResponse withdrawJob(ServletContext context, String moId, String jobId) {
        try {
            List<JobPosting> jobs = JsonUtility.loadJobs(context);
            JobPosting job = findOwnedJob(jobs, moId, jobId);

            List<ApplicationRecord> applications = JsonUtility.loadApplications(context);
            boolean hasActiveApplications = applications.stream()
                    .anyMatch(a -> jobId.equals(a.getJobId()) && a.isActive());

            if (hasActiveApplications) {
                throw new MoBusinessException(
                        ErrorCodes.HAS_APPLICATIONS_CANNOT_WITHDRAW,
                        "Job has active applications and cannot be withdrawn.",
                        HttpServletResponse.SC_BAD_REQUEST
                );
            }

            String now = Instant.now().toString();
            job.setWithdrawn(true);
            job.setUpdatedAt(now);

            JsonUtility.saveJobs(context, jobs);

            MoJobWithdrawResponse response = new MoJobWithdrawResponse();
            response.setJobId(jobId);
            response.setWithdrawn(true);
            response.setWithdrawnAt(now);
            return response;
        } catch (IOException e) {
            throw new RuntimeException("Failed to withdraw job.", e);
        }
    }

    private JobPosting findOwnedJob(List<JobPosting> jobs, String moId, String jobId) {
        JobPosting job = jobs.stream()
                .filter(j -> jobId.equals(j.getId()))
                .findFirst()
                .orElseThrow(() -> new MoBusinessException(
                        ErrorCodes.JOB_NOT_FOUND,
                        "Job not found.",
                        HttpServletResponse.SC_NOT_FOUND
                ));

        if (!moId.equals(job.getTeacherId())) {
            throw new MoBusinessException(
                    ErrorCodes.FORBIDDEN_NOT_OWNER,
                    "You can only operate your own jobs.",
                    HttpServletResponse.SC_FORBIDDEN
            );
        }

        return job;
    }

    private void validatePublishRequest(MoJobPublishRequest request) {
        if (request == null || isBlank(request.getLocation()) || isBlank(request.getRequirements()) || isBlank(request.getDeadline())) {
            throw new MoBusinessException(
                    ErrorCodes.VALIDATION_ERROR,
                    "location, requirements and deadline are required.",
                    HttpServletResponse.SC_BAD_REQUEST
            );
        }

        if (!"online".equalsIgnoreCase(request.getLocation()) && !"offline".equalsIgnoreCase(request.getLocation())) {
            throw new MoBusinessException(
                    ErrorCodes.VALIDATION_ERROR,
                    "location must be online or offline.",
                    HttpServletResponse.SC_BAD_REQUEST
            );
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
