package com.ta.service.mo;

import com.ta.dto.mo.MoJobHistoryItemResponse;
import com.ta.dto.mo.MoJobHistoryResponse;
import com.ta.model.ApplicationRecord;
import com.ta.model.JobPosting;
import com.ta.util.JsonUtility;
import jakarta.servlet.ServletContext;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MoJobHistoryService {

    public MoJobHistoryResponse listHistory(ServletContext context, String moId) {
        try {
            List<JobPosting> jobs = JsonUtility.loadJobs(context);
            List<ApplicationRecord> applications = JsonUtility.loadApplications(context);
            Map<String, Integer> applicantCountByJobId = new HashMap<>();
            Map<String, Integer> hireCountByJobId = new HashMap<>();

            for (ApplicationRecord application : applications) {
                if (application.getJobId() == null || !application.isActive()) {
                    continue;
                }
                applicantCountByJobId.merge(application.getJobId(), 1, Integer::sum);
                if ("hired".equalsIgnoreCase(application.getStatus())) {
                    hireCountByJobId.merge(application.getJobId(), 1, Integer::sum);
                }
            }

            List<MoJobHistoryItemResponse> items = jobs.stream()
                    .filter(job -> moId.equals(job.getTeacherId()))
                    .map(job -> toHistoryItem(job, applicantCountByJobId, hireCountByJobId))
                    .sorted(Comparator.comparing(MoJobHistoryItemResponse::getReleaseTime, Comparator.nullsLast(String::compareTo)).reversed())
                    .toList();

            MoJobHistoryResponse response = new MoJobHistoryResponse();
            response.setItems(items);
            return response;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load MO job history.", e);
        }
    }

    private MoJobHistoryItemResponse toHistoryItem(JobPosting job,
                                                   Map<String, Integer> applicantCountByJobId,
                                                   Map<String, Integer> hireCountByJobId) {
        MoJobHistoryItemResponse item = new MoJobHistoryItemResponse();
        item.setJobId(job.getId());
        item.setCourseName(job.getTitle());
        item.setDepartment(job.getDepartment());
        item.setStatus(resolveDisplayStatus(job));
        item.setPublished(Boolean.TRUE.equals(job.getPublished()));
        item.setWithdrawn(Boolean.TRUE.equals(job.getWithdrawn()));
        item.setRecruitmentClosed(Boolean.TRUE.equals(job.getRecruitmentClosed()));
        item.setApplicantCount(applicantCountByJobId.getOrDefault(job.getId(), 0));
        item.setHireCount(hireCountByJobId.getOrDefault(job.getId(), 0));
        item.setReleaseTime(resolveReleaseTime(job));
        item.setDeadline(job.getDeadline());
        item.setCreatedAt(job.getCreatedAt());
        item.setUpdatedAt(job.getUpdatedAt());
        return item;
    }

    private String resolveDisplayStatus(JobPosting job) {
        if (Boolean.TRUE.equals(job.getRecruitmentClosed())) {
            return "recruitment_closed";
        }
        if (Boolean.TRUE.equals(job.getWithdrawn())) {
            return "withdrawn";
        }
        if (Boolean.TRUE.equals(job.getPublished())) {
            return job.getStatus() == null || job.getStatus().isBlank() ? "open" : job.getStatus();
        }
        if (job.getApprovalStatus() != null && !job.getApprovalStatus().isBlank()) {
            return job.getApprovalStatus();
        }
        return job.getStatus() == null || job.getStatus().isBlank() ? "draft" : job.getStatus();
    }

    private String resolveReleaseTime(JobPosting job) {
        if (job.getPublishedAt() != null && !job.getPublishedAt().isBlank()) {
            return job.getPublishedAt();
        }
        if (Boolean.TRUE.equals(job.getPublished()) && job.getUpdatedAt() != null && !job.getUpdatedAt().isBlank()) {
            return job.getUpdatedAt();
        }
        return job.getCreatedAt();
    }
}
