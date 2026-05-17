package com.ta.service.admin;

import com.ta.constant.ErrorCodes;
import com.ta.dto.admin.AdminDemandItemResponse;
import com.ta.dto.admin.AdminDemandListResponse;
import com.ta.dto.admin.AdminDemandReviewResponse;
import com.ta.model.JobPosting;
import com.ta.model.User;
import com.ta.util.JsonUtility;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class AdminDemandReviewService {
    private static final Set<String> ALLOWED_STATUS_FILTERS = Set.of("all", "pending", "approved", "rejected");
    private static final int MAX_REJECTION_REASON_LENGTH = 200;
    private final AdminAnnouncementService announcementService = new AdminAnnouncementService();

    public AdminDemandListResponse listDemands(ServletContext context, String statusFilter) {
        String normalizedStatus = normalizeStatusFilter(statusFilter);
        try {
            List<JobPosting> jobs = JsonUtility.loadJobs(context);
            List<User> users = JsonUtility.loadUsers(context);
            Map<String, String> userNameById = new LinkedHashMap<>();
            for (User user : users) {
                if (user.getId() != null) {
                    userNameById.put(user.getId(), user.getName());
                }
            }

            List<AdminDemandItemResponse> items = new ArrayList<>();
            for (JobPosting job : jobs) {
                if (!isDemandRecord(job) || !matchesStatus(job, normalizedStatus)) {
                    continue;
                }
                items.add(toDemandItem(job, userNameById));
            }

            items.sort(Comparator
                    .comparing(AdminDemandItemResponse::getCreatedAt, Comparator.nullsLast(String::compareTo))
                    .reversed());

            AdminDemandListResponse response = new AdminDemandListResponse();
            response.setItems(items);
            return response;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load admin demand list.", e);
        }
    }

    public AdminDemandReviewResponse reviewDemand(ServletContext context, String jobId, String action, String reason) {
        String normalizedAction = normalizeAction(action);
        String targetStatus = toApprovalStatus(normalizedAction);
        String normalizedReason = normalizeReason(normalizedAction, reason);

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

            String previousStatus = trimToEmpty(job.getApprovalStatus()).toLowerCase(Locale.ROOT);
            if (previousStatus.isBlank()) {
                previousStatus = "pending";
            }

            String now = Instant.now().toString();
            job.setApprovalStatus(targetStatus);
            job.setReviewedAt(now);
            job.setRejectionReason("reject".equals(normalizedAction) ? normalizedReason : "");
            if (job.getPublished() == null) {
                job.setPublished(false);
            }
            if (job.getWithdrawn() == null) {
                job.setWithdrawn(false);
            }
            job.setUpdatedAt(now);

            JsonUtility.saveJobs(context, jobs);

            if (!previousStatus.equalsIgnoreCase(targetStatus)) {
                notifyTeacherDemandStatusChange(context, job, previousStatus, targetStatus, normalizedReason);
            }

            AdminDemandReviewResponse response = new AdminDemandReviewResponse();
            response.setJobId(job.getId());
            response.setAction(normalizedAction);
            response.setApprovalStatus(job.getApprovalStatus());
            response.setPublished(job.getPublished());
            response.setWithdrawn(job.getWithdrawn());
            response.setReviewedAt(now);
            response.setRejectionReason(job.getRejectionReason());
            return response;
        } catch (IOException e) {
            throw new RuntimeException("Failed to review demand.", e);
        }
    }

    public AdminDemandReviewResponse reviewDemand(ServletContext context, String jobId, String action) {
        return reviewDemand(context, jobId, action, null);
    }

    private String normalizeStatusFilter(String statusFilter) {
        String normalized = statusFilter == null ? "" : statusFilter.trim().toLowerCase(Locale.ROOT);
        if (normalized.isBlank()) {
            return "pending";
        }
        if (!ALLOWED_STATUS_FILTERS.contains(normalized)) {
            throw new AdminBusinessException(
                    ErrorCodes.VALIDATION_ERROR,
                    "status must be pending, approved, rejected, or all.",
                    HttpServletResponse.SC_BAD_REQUEST
            );
        }
        return normalized;
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
        if ("approved".equals(normalized)) {
            normalized = "approve";
        } else if ("rejected".equals(normalized)) {
            normalized = "reject";
        }
        if (!"approve".equals(normalized) && !"reject".equals(normalized) && !"pending".equals(normalized)) {
            throw new AdminBusinessException(
                    ErrorCodes.VALIDATION_ERROR,
                    "action must be pending, approve, or reject.",
                    HttpServletResponse.SC_BAD_REQUEST
            );
        }
        return normalized;
    }

    private String normalizeReason(String normalizedAction, String reason) {
        if (!"reject".equals(normalizedAction)) {
            return "";
        }
        String normalized = reason == null ? "" : reason.trim();
        if (normalized.length() > MAX_REJECTION_REASON_LENGTH) {
            throw new AdminBusinessException(
                    ErrorCodes.VALIDATION_ERROR,
                    "rejection reason must be 200 characters or fewer.",
                    HttpServletResponse.SC_BAD_REQUEST
            );
        }
        return normalized;
    }

    private String toApprovalStatus(String normalizedAction) {
        if ("approve".equals(normalizedAction)) {
            return "approved";
        }
        if ("reject".equals(normalizedAction)) {
            return "rejected";
        }
        return "pending";
    }

    private boolean isDemandRecord(JobPosting job) {
        return job.getApprovalStatus() != null || job.getPublished() != null || job.getWithdrawn() != null;
    }

    private boolean matchesStatus(JobPosting job, String statusFilter) {
        if ("all".equals(statusFilter)) {
            return true;
        }
        return statusFilter.equalsIgnoreCase(job.getApprovalStatus());
    }

    private AdminDemandItemResponse toDemandItem(JobPosting job, Map<String, String> userNameById) {
        AdminDemandItemResponse item = new AdminDemandItemResponse();
        item.setJobId(job.getId());
        item.setMoId(job.getTeacherId());
        item.setTeacherName(firstNonBlank(job.getTeacherName(), userNameById.get(job.getTeacherId()), job.getTeacherId()));
        item.setModuleCode(job.getModuleCode());
        item.setTitle(job.getTitle());
        item.setDepartment(job.getDepartment());
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
        item.setSchedule(job.getSchedule());
        item.setLocation(job.getLocation());
        item.setDeadline(job.getDeadline());
        item.setRequirements(job.getRequirements());
        item.setCreatedAt(job.getCreatedAt());
        item.setUpdatedAt(job.getUpdatedAt());
        item.setReviewedAt(job.getReviewedAt());
        item.setRejectionReason(job.getRejectionReason());
        return item;
    }

    private String firstNonBlank(String first, String second, String fallback) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        if (second != null && !second.isBlank()) {
            return second;
        }
        return fallback == null ? "" : fallback;
    }

    private void notifyTeacherDemandStatusChange(ServletContext context,
                                                 JobPosting job,
                                                 String previousStatus,
                                                 String targetStatus,
                                                 String rejectionReason) throws IOException {
        String teacherId = trimToEmpty(job.getTeacherId());
        if (teacherId.isBlank()) {
            return;
        }
        String module = firstNonBlank(job.getModuleCode(), job.getTitle(), job.getId());
        String title = "Demand review update: " + module;
        StringBuilder body = new StringBuilder();
        body.append("Your TA demand for ").append(module).append(" was reviewed.\n");
        body.append("Previous status: ").append(capitalizeStatus(previousStatus)).append("\n");
        body.append("New status: ").append(capitalizeStatus(targetStatus)).append(".");
        if ("rejected".equalsIgnoreCase(targetStatus) && rejectionReason != null && !rejectionReason.isBlank()) {
            body.append("\nReason: ").append(rejectionReason.trim());
        }
        announcementService.notifyTeacher(context, teacherId, title, body.toString());
    }

    private String capitalizeStatus(String status) {
        String value = trimToEmpty(status);
        if (value.isBlank()) {
            return "Pending";
        }
        return value.substring(0, 1).toUpperCase(Locale.ROOT) + value.substring(1).toLowerCase(Locale.ROOT);
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
