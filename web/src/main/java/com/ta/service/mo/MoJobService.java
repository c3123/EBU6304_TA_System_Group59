package com.ta.service.mo;

import com.ta.constant.ErrorCodes;
import com.ta.dto.mo.MoDemandItemResponse;
import com.ta.dto.mo.MoJobEditRequest;
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
 * 1) MO can publish a demand directly without admin approval.
 * 2) Deadline cannot be modified after publish.
 * 3) Published jobs can be taken offline and republished later.
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

            if (Boolean.TRUE.equals(job.getRecruitmentClosed())) {
                throw new MoBusinessException(
                        ErrorCodes.JOB_RECRUITMENT_CLOSED,
                        "Recruitment closed jobs cannot be published.",
                        HttpServletResponse.SC_BAD_REQUEST
                );
            }

            String now = Instant.now().toString();
            job.setLocation(request.getLocation());
            job.setRequirements(request.getRequirements());
            job.setDeadline(request.getDeadline());
            job.setPublished(true);
            job.setWithdrawn(false);
            job.setStatus("open");
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

            String now = Instant.now().toString();
            job.setWithdrawn(true);
            job.setPublished(false);
            job.setStatus("offline");
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

    public MoDemandItemResponse editJob(ServletContext context, String moId, String jobId, MoJobEditRequest request) {
        validateEditRequest(request);
        try {
            List<JobPosting> jobs = JsonUtility.loadJobs(context);
            JobPosting job = findOwnedJob(jobs, moId, jobId);
            ensureEditable(job);
            String now = Instant.now().toString();
            job.setTitle(request.getCourseName().trim());
            job.setModuleCode(request.getCourseName().trim());
            job.setPositions(request.getPlannedCount());
            job.setHourMin(request.getHourMin());
            job.setHourMax(request.getHourMax());
            job.setPublished(false);
            job.setWithdrawn(false);
            job.setStatus("draft");
            job.setUpdatedAt(now);
            JsonUtility.saveJobs(context, jobs);
            return toDemandItem(job);
        } catch (IOException e) {
            throw new RuntimeException("Failed to edit job.", e);
        }
    }

    public void deleteDraftJob(ServletContext context, String moId, String jobId) {
        try {
            List<JobPosting> jobs = JsonUtility.loadJobs(context);
            JobPosting job = findOwnedJob(jobs, moId, jobId);
            if (Boolean.TRUE.equals(job.getPublished())) {
                throw new MoBusinessException(
                        ErrorCodes.VALIDATION_ERROR,
                        "Published jobs cannot be deleted. Use take offline first.",
                        HttpServletResponse.SC_BAD_REQUEST
                );
            }
            if (Boolean.TRUE.equals(job.getRecruitmentClosed())) {
                throw new MoBusinessException(
                        ErrorCodes.JOB_RECRUITMENT_CLOSED,
                        "Recruitment closed jobs cannot be deleted.",
                        HttpServletResponse.SC_BAD_REQUEST
                );
            }

            List<ApplicationRecord> applications = JsonUtility.loadApplications(context);
            boolean applicationsChanged = false;
            for (ApplicationRecord application : applications) {
                if (!jobId.equals(application.getJobId())) {
                    continue;
                }
                if (application.isActive()) {
                    application.setActive(false);
                    applicationsChanged = true;
                }
            }

            jobs.removeIf(j -> jobId.equals(j.getId()));
            JsonUtility.saveJobs(context, jobs);
            if (applicationsChanged) {
                JsonUtility.saveApplications(context, applications);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete job.", e);
        }
    }

    public MoJobWithdrawResponse takeOffline(ServletContext context, String moId, String jobId) {
        try {
            List<JobPosting> jobs = JsonUtility.loadJobs(context);
            JobPosting job = findOwnedJob(jobs, moId, jobId);
            if (!Boolean.TRUE.equals(job.getPublished())) {
                throw new MoBusinessException(
                        ErrorCodes.VALIDATION_ERROR,
                        "Only published jobs can be taken offline.",
                        HttpServletResponse.SC_BAD_REQUEST
                );
            }
            if (Boolean.TRUE.equals(job.getRecruitmentClosed())) {
                throw new MoBusinessException(
                        ErrorCodes.JOB_RECRUITMENT_CLOSED,
                        "Recruitment closed jobs cannot be taken offline.",
                        HttpServletResponse.SC_BAD_REQUEST
                );
            }
            return withdrawJob(context, moId, jobId);
        } catch (IOException e) {
            throw new RuntimeException("Failed to take job offline.", e);
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

    private void validateEditRequest(MoJobEditRequest request) {
        if (request == null
                || isBlank(request.getCourseName())
                || request.getPlannedCount() == null
                || request.getHourMin() == null
                || request.getHourMax() == null) {
            throw new MoBusinessException(
                    ErrorCodes.VALIDATION_ERROR,
                    "courseName, plannedCount, hourMin, hourMax are required.",
                    HttpServletResponse.SC_BAD_REQUEST
            );
        }
        if (request.getPlannedCount() <= 0 || request.getHourMin() <= 0 || request.getHourMax() <= 0 || request.getHourMin() > request.getHourMax()) {
            throw new MoBusinessException(
                    ErrorCodes.VALIDATION_ERROR,
                    "Invalid plannedCount or hour range.",
                    HttpServletResponse.SC_BAD_REQUEST
            );
        }
    }

    private void ensureEditable(JobPosting job) {
        if (Boolean.TRUE.equals(job.getRecruitmentClosed())) {
            throw new MoBusinessException(
                    ErrorCodes.JOB_RECRUITMENT_CLOSED,
                    "Recruitment closed jobs cannot be edited.",
                    HttpServletResponse.SC_BAD_REQUEST
            );
        }
        if (Boolean.TRUE.equals(job.getPublished())) {
            throw new MoBusinessException(
                    ErrorCodes.VALIDATION_ERROR,
                    "Published jobs cannot be edited directly. Take offline first.",
                    HttpServletResponse.SC_BAD_REQUEST
            );
        }
    }

    private MoDemandItemResponse toDemandItem(JobPosting job) {
        MoDemandItemResponse item = new MoDemandItemResponse();
        item.setJobId(job.getId());
        item.setMoId(job.getTeacherId());
        item.setCourseName(job.getTitle());
        item.setPlannedCount(job.getPositions());
        item.setHourMin(job.getHourMin());
        item.setHourMax(job.getHourMax());
        if (job.getHours() > 0) {
            item.setHours(job.getHours());
        }
        item.setApprovalStatus(job.getApprovalStatus());
        item.setStatus(job.getStatus());
        item.setPublished(job.getPublished());
        item.setWithdrawn(job.getWithdrawn());
        item.setRecruitmentClosed(Boolean.TRUE.equals(job.getRecruitmentClosed()));
        item.setClosedAt(job.getClosedAt());
        item.setCreatedAt(job.getCreatedAt());
        item.setUpdatedAt(job.getUpdatedAt());
        return item;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
