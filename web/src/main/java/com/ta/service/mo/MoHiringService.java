package com.ta.service.mo;

import com.ta.constant.ErrorCodes;
import com.ta.dto.mo.MoHiringHistoryItemResponse;
import com.ta.dto.mo.MoHiringHistoryResponse;
import com.ta.dto.mo.MoHiringStateItemResponse;
import com.ta.dto.mo.MoHiringStateResponse;
import com.ta.model.ApplicationRecord;
import com.ta.model.HiringHistoryRecord;
import com.ta.model.JobPosting;
import com.ta.model.NotificationRecord;
import com.ta.model.User;
import com.ta.util.JsonUtility;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Final hiring workflow: finalize, state and history.
 */
public class MoHiringService {

    public MoHiringStateResponse getState(ServletContext context, String moId) {
        try {
            List<JobPosting> jobs = JsonUtility.loadJobs(context);
            MoHiringStateResponse response = new MoHiringStateResponse();
            List<MoHiringStateItemResponse> items = new ArrayList<>();
            for (JobPosting job : jobs) {
                if (!moId.equals(job.getTeacherId())) {
                    continue;
                }
                MoHiringStateItemResponse item = new MoHiringStateItemResponse();
                item.setJobId(job.getId());
                item.setRecruitmentClosed(Boolean.TRUE.equals(job.getRecruitmentClosed()));
                item.setClosedAt(job.getClosedAt());
                items.add(item);
            }
            response.setItems(items);
            return response;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load hiring state.", e);
        }
    }

    public MoHiringHistoryResponse getHistory(ServletContext context, String moId, String jobId) {
        if (jobId == null || jobId.isBlank()) {
            throw new MoBusinessException(ErrorCodes.VALIDATION_ERROR, "jobId is required.", HttpServletResponse.SC_BAD_REQUEST);
        }
        try {
            List<JobPosting> jobs = JsonUtility.loadJobs(context);
            JobPosting job = findOwnedJob(jobs, moId, jobId);
            List<HiringHistoryRecord> all = JsonUtility.loadHiringHistory(context);

            MoHiringHistoryResponse response = new MoHiringHistoryResponse();
            response.setJobId(job.getId());
            response.setJobName(job.getTitle());

            List<MoHiringHistoryItemResponse> items = new ArrayList<>();
            for (HiringHistoryRecord record : all) {
                if (!jobId.equals(record.getJobId())) {
                    continue;
                }
                MoHiringHistoryItemResponse item = new MoHiringHistoryItemResponse();
                item.setAction(record.getAction());
                item.setSubmittedAt(record.getSubmittedAt());
                item.setHiredStudentNames(record.getHiredStudentNames());
                items.add(item);
            }
            response.setItems(items);
            return response;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load hiring history.", e);
        }
    }

    public MoHiringHistoryItemResponse finalizeHiring(ServletContext context, String moId, String jobId, List<String> hiredApplicationIds) {
        if (jobId == null || jobId.isBlank()) {
            throw new MoBusinessException(ErrorCodes.VALIDATION_ERROR, "jobId is required.", HttpServletResponse.SC_BAD_REQUEST);
        }
        try {
            List<JobPosting> jobs = JsonUtility.loadJobs(context);
            JobPosting job = findOwnedJob(jobs, moId, jobId);
            if (Boolean.TRUE.equals(job.getRecruitmentClosed())) {
                throw new MoBusinessException(ErrorCodes.JOB_RECRUITMENT_CLOSED, "Recruitment already closed.", HttpServletResponse.SC_BAD_REQUEST);
            }

            List<ApplicationRecord> applications = JsonUtility.loadApplications(context);
            Set<String> selected = new HashSet<>(hiredApplicationIds == null ? List.of() : hiredApplicationIds);
            List<String> hiredNames = new ArrayList<>();
            List<String> hiredIds = new ArrayList<>();
            List<NotificationRecord> notifications = JsonUtility.loadNotifications(context);

            for (ApplicationRecord app : applications) {
                if (!app.isActive() || !jobId.equals(app.getJobId())) {
                    continue;
                }
                if (!"shortlisted".equalsIgnoreCase(app.getStatus())) {
                    continue;
                }
                if (selected.contains(app.getId())) {
                    app.setStatus("hired");
                    hiredIds.add(app.getId());
                    String studentName = app.getStudentName() == null ? app.getStudentId() : app.getStudentName();
                    hiredNames.add(studentName);
                    notifications.add(buildNotification(
                            "noti_hired_" + app.getId(),
                            app.getStudentId(),
                            "student",
                            jobId,
                            app.getId(),
                            studentName,
                            app.getAppliedAt(),
                            "You have been hired for " + safeJobName(job) + "."
                    ));
                } else {
                    app.setStatus("rejected");
                }
            }

            List<User> users = JsonUtility.loadUsers(context);
            for (User user : users) {
                if (!"admin".equalsIgnoreCase(user.getRole())) {
                    continue;
                }
                notifications.add(buildNotification(
                        "noti_admin_finalize_" + jobId + "_" + user.getId(),
                        user.getId(),
                        "admin",
                        jobId,
                        null,
                        null,
                        nowIso(),
                        "Final hiring submitted for " + safeJobName(job) + "."
                ));
            }

            String now = Instant.now().toString();
            job.setRecruitmentClosed(true);
            job.setClosedAt(now);
            job.setStatus("closed");
            job.setPublished(false);
            job.setUpdatedAt(now);

            List<HiringHistoryRecord> history = JsonUtility.loadHiringHistory(context);
            HiringHistoryRecord record = new HiringHistoryRecord();
            record.setId("hist_" + UUID.randomUUID().toString().replace("-", ""));
            record.setAction("finalize");
            record.setJobId(jobId);
            record.setMoId(moId);
            record.setSubmittedAt(now);
            record.setHiredApplicationIds(hiredIds);
            record.setHiredStudentNames(hiredNames);
            history.add(record);

            JsonUtility.saveApplications(context, applications);
            JsonUtility.saveJobs(context, jobs);
            JsonUtility.saveHiringHistory(context, history);
            JsonUtility.saveNotifications(context, notifications);

            MoHiringHistoryItemResponse response = new MoHiringHistoryItemResponse();
            response.setAction(record.getAction());
            response.setSubmittedAt(record.getSubmittedAt());
            response.setHiredStudentNames(record.getHiredStudentNames());
            return response;
        } catch (IOException e) {
            throw new RuntimeException("Failed to finalize hiring.", e);
        }
    }

    public void reopenByAdmin(ServletContext context, String jobId) {
        if (jobId == null || jobId.isBlank()) {
            throw new MoBusinessException(ErrorCodes.VALIDATION_ERROR, "jobId is required.", HttpServletResponse.SC_BAD_REQUEST);
        }
        try {
            List<JobPosting> jobs = JsonUtility.loadJobs(context);
            JobPosting job = jobs.stream()
                    .filter(j -> jobId.equals(j.getId()))
                    .findFirst()
                    .orElseThrow(() -> new MoBusinessException(ErrorCodes.JOB_NOT_FOUND, "Job not found.", HttpServletResponse.SC_NOT_FOUND));
            if (!Boolean.TRUE.equals(job.getRecruitmentClosed())) {
                return;
            }
            String now = Instant.now().toString();
            job.setRecruitmentClosed(false);
            job.setClosedAt(null);
            job.setStatus("open");
            job.setPublished(true);
            job.setUpdatedAt(now);

            List<HiringHistoryRecord> history = JsonUtility.loadHiringHistory(context);
            HiringHistoryRecord record = new HiringHistoryRecord();
            record.setId("hist_" + UUID.randomUUID().toString().replace("-", ""));
            record.setAction("reopen");
            record.setJobId(jobId);
            record.setMoId(job.getTeacherId());
            record.setSubmittedAt(now);
            history.add(record);

            JsonUtility.saveJobs(context, jobs);
            JsonUtility.saveHiringHistory(context, history);
        } catch (IOException e) {
            throw new RuntimeException("Failed to reopen hiring.", e);
        }
    }

    private NotificationRecord buildNotification(String id,
                                                 String recipientId,
                                                 String recipientRole,
                                                 String jobId,
                                                 String applicationId,
                                                 String applicantName,
                                                 String applicationTime,
                                                 String message) {
        NotificationRecord record = new NotificationRecord();
        record.setId(id);
        record.setRecipientId(recipientId);
        record.setRecipientRole(recipientRole);
        record.setJobId(jobId);
        record.setApplicationId(applicationId);
        record.setApplicantName(applicantName);
        record.setApplicationTime(applicationTime);
        record.setMessage(message);
        record.setCreatedAt(nowIso());
        record.setRead(false);
        return record;
    }

    private String safeJobName(JobPosting job) {
        if (job == null || job.getTitle() == null || job.getTitle().isBlank()) {
            return "the TA job";
        }
        return job.getTitle();
    }

    private String nowIso() {
        return Instant.now().toString();
    }

    private JobPosting findOwnedJob(List<JobPosting> jobs, String moId, String jobId) {
        JobPosting job = jobs.stream()
                .filter(j -> jobId.equals(j.getId()))
                .findFirst()
                .orElseThrow(() -> new MoBusinessException(ErrorCodes.JOB_NOT_FOUND, "Job not found.", HttpServletResponse.SC_NOT_FOUND));
        if (!moId.equals(job.getTeacherId())) {
            throw new MoBusinessException(ErrorCodes.FORBIDDEN_NOT_OWNER, "You can only operate your own jobs.", HttpServletResponse.SC_FORBIDDEN);
        }
        return job;
    }
}
