package com.ta.service.mo;

import com.ta.constant.ErrorCodes;
import com.ta.dto.mo.MoDemandCreateRequest;
import com.ta.dto.mo.MoDemandItemResponse;
import com.ta.dto.mo.MoDemandListResponse;
import com.ta.model.JobPosting;
import com.ta.util.JsonUtility;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * MO demand service boundary.
 *
 * Ownership: A side (demand and publish chain).
 * Rules:
 * 1) One MO cannot submit same course when pending demand exists.
 * 2) New demand is created as approved so it can be published directly.
 */
public class MoDemandService {

    public MoDemandListResponse listMyDemands(ServletContext context, String moId) {
        try {
            List<JobPosting> jobs = JsonUtility.loadJobs(context);
            List<MoDemandItemResponse> items = new ArrayList<>();

            for (JobPosting job : jobs) {
                if (moId.equals(job.getTeacherId())) {
                    items.add(toDemandItem(job));
                }
            }

            MoDemandListResponse response = new MoDemandListResponse();
            response.setItems(items);
            return response;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load demands.", e);
        }
    }

    public String createDemand(ServletContext context, String moId, MoDemandCreateRequest request) {
        validateCreateRequest(request);

        try {
            List<JobPosting> jobs = JsonUtility.loadJobs(context);

            for (JobPosting job : jobs) {
                if (!moId.equals(job.getTeacherId())) {
                    continue;
                }
                if (isPending(job) && sameCourse(job, request.getCourseName())) {
                    throw new MoBusinessException(
                            ErrorCodes.HAS_PENDING_SAME_COURSE,
                            "You already have a pending demand for this course.",
                            HttpServletResponse.SC_BAD_REQUEST
                    );
                }
            }

            String now = Instant.now().toString();
            JobPosting job = new JobPosting();
            job.setId("job_" + UUID.randomUUID().toString().replace("-", ""));
            job.setTeacherId(moId);
            job.setTitle(request.getCourseName());
            job.setModuleCode(request.getCourseName());
            job.setPositions(request.getPlannedCount());
            job.setHourMin(request.getHourMin());
            job.setHourMax(request.getHourMax());

            job.setApprovalStatus("approved");
            job.setPublished(false);
            job.setWithdrawn(false);
            job.setStatus("draft");
            job.setLocation(null);
            job.setRequirements(null);
            job.setDeadline(null);
            job.setCreatedAt(now);
            job.setUpdatedAt(now);

            jobs.add(job);
            JsonUtility.saveJobs(context, jobs);

            return job.getId();
        } catch (IOException e) {
            throw new RuntimeException("Failed to save demand.", e);
        }
    }

    private void validateCreateRequest(MoDemandCreateRequest request) {
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
        item.setPublishedAt(job.getPublishedAt());
        return item;
    }

    private boolean sameCourse(JobPosting job, String courseName) {
        String title = job.getTitle();
        return title != null && title.equalsIgnoreCase(courseName);
    }

    private boolean isPending(JobPosting job) {
        return "pending".equalsIgnoreCase(job.getApprovalStatus());
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
