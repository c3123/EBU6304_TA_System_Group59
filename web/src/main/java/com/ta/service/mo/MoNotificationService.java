package com.ta.service.mo;

import com.ta.constant.ErrorCodes;
import com.ta.dto.mo.MoNotificationItemResponse;
import com.ta.dto.mo.MoNotificationListResponse;
import com.ta.model.ApplicationRecord;
import com.ta.model.JobPosting;
import com.ta.model.NotificationRecord;
import com.ta.util.JsonUtility;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MoNotificationService {

    public MoNotificationListResponse list(ServletContext context, String moId) {
        try {
            List<JobPosting> jobs = JsonUtility.loadJobs(context);
            Map<String, JobPosting> ownedJobs = new HashMap<>();
            for (JobPosting job : jobs) {
                if (moId.equals(job.getTeacherId())) {
                    ownedJobs.put(job.getId(), job);
                }
            }

            List<ApplicationRecord> applications = JsonUtility.loadApplications(context);
            List<NotificationRecord> notifications = JsonUtility.loadNotifications(context);
            Map<String, NotificationRecord> byApplication = new HashMap<>();
            for (NotificationRecord r : notifications) {
                if (r.getApplicationId() != null) {
                    byApplication.put(r.getApplicationId(), r);
                }
            }

            boolean dirty = false;
            for (ApplicationRecord app : applications) {
                if (!app.isActive()) {
                    continue;
                }
                JobPosting job = ownedJobs.get(app.getJobId());
                if (job == null) {
                    continue;
                }
                if (byApplication.containsKey(app.getId())) {
                    continue;
                }
                NotificationRecord created = new NotificationRecord();
                created.setId("noti_" + app.getId());
                created.setMoId(moId);
                created.setJobId(app.getJobId());
                created.setApplicationId(app.getId());
                created.setApplicantName(app.getStudentName());
                created.setApplicationTime(app.getAppliedAt());
                created.setCreatedAt(Instant.now().toString());
                created.setRead(false);
                notifications.add(created);
                byApplication.put(app.getId(), created);
                dirty = true;
            }
            if (dirty) {
                JsonUtility.saveNotifications(context, notifications);
            }

            List<MoNotificationItemResponse> items = new ArrayList<>();
            int unread = 0;
            for (NotificationRecord record : notifications) {
                if (!moId.equals(record.getMoId())) {
                    continue;
                }
                MoNotificationItemResponse item = new MoNotificationItemResponse();
                item.setNotificationId(record.getId());
                item.setApplicantName(record.getApplicantName());
                item.setJobId(record.getJobId());
                JobPosting job = ownedJobs.get(record.getJobId());
                item.setJobName(job == null ? record.getJobId() : job.getTitle());
                item.setApplicationTime(record.getApplicationTime());
                item.setApplicationId(record.getApplicationId());
                item.setRead(record.isRead());
                if (!record.isRead()) {
                    unread += 1;
                }
                items.add(item);
            }

            MoNotificationListResponse response = new MoNotificationListResponse();
            response.setUnreadCount(unread);
            response.setItems(items);
            return response;
        } catch (IOException e) {
            throw new RuntimeException("Failed to list notifications.", e);
        }
    }

    public void markRead(ServletContext context, String moId, String notificationId) {
        if (notificationId == null || notificationId.isBlank()) {
            throw new MoBusinessException(ErrorCodes.VALIDATION_ERROR, "notificationId is required.", HttpServletResponse.SC_BAD_REQUEST);
        }
        try {
            List<NotificationRecord> notifications = JsonUtility.loadNotifications(context);
            NotificationRecord record = notifications.stream()
                    .filter(r -> notificationId.equals(r.getId()))
                    .findFirst()
                    .orElseThrow(() -> new MoBusinessException(ErrorCodes.APPLICATION_NOT_FOUND, "Notification not found.", HttpServletResponse.SC_NOT_FOUND));
            if (!moId.equals(record.getMoId())) {
                throw new MoBusinessException(ErrorCodes.FORBIDDEN_NOT_OWNER, "You can only mark your own notifications.", HttpServletResponse.SC_FORBIDDEN);
            }
            record.setRead(true);
            JsonUtility.saveNotifications(context, notifications);
        } catch (IOException e) {
            throw new RuntimeException("Failed to mark notification as read.", e);
        }
    }
}
