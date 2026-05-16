package com.ta.service.student;

import com.ta.constant.ErrorCodes;
import com.ta.dto.mo.MoNotificationItemResponse;
import com.ta.dto.mo.MoNotificationListResponse;
import com.ta.model.JobPosting;
import com.ta.model.NotificationRecord;
import com.ta.util.JsonUtility;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StudentNotificationService {

    private static final String TYPE_ANNOUNCEMENT = "announcement";

    public MoNotificationListResponse list(ServletContext context, String studentId) {
        try {
            List<JobPosting> jobs = JsonUtility.loadJobs(context);
            Map<String, String> jobTitles = new HashMap<>();
            for (JobPosting job : jobs) {
                if (job.getId() != null) {
                    jobTitles.put(job.getId(), job.getTitle());
                }
            }

            List<NotificationRecord> notifications = JsonUtility.loadNotifications(context);
            List<MoNotificationItemResponse> items = new ArrayList<>();
            int unread = 0;
            for (NotificationRecord record : notifications) {
                if (!ownsNotification(studentId, record)) {
                    continue;
                }
                MoNotificationItemResponse item = new MoNotificationItemResponse();
                item.setNotificationId(record.getId());
                item.setRead(record.isRead());
                if (isAnnouncement(record)) {
                    item.setType(TYPE_ANNOUNCEMENT);
                    item.setTitle(trimToEmpty(record.getTitle()));
                    item.setMessage(record.getMessage());
                    item.setJobName("System announcement");
                    item.setApplicationTime(firstNonBlank(record.getCreatedAt(), record.getApplicationTime()));
                } else {
                    item.setType("workflow");
                    item.setMessage(record.getMessage());
                    String jobId = record.getJobId();
                    item.setJobId(jobId);
                    String title = jobId == null ? null : jobTitles.get(jobId);
                    item.setJobName(title == null || title.isBlank() ? jobId : title);
                    item.setApplicationTime(firstNonBlank(record.getCreatedAt(), record.getApplicationTime()));
                }
                if (!record.isRead()) {
                    unread += 1;
                }
                items.add(item);
            }

            items.sort((a, b) -> String.valueOf(b.getApplicationTime()).compareTo(String.valueOf(a.getApplicationTime())));
            MoNotificationListResponse response = new MoNotificationListResponse();
            response.setUnreadCount(unread);
            response.setItems(items);
            return response;
        } catch (IOException e) {
            throw new RuntimeException("Failed to list student notifications.", e);
        }
    }

    public void markRead(ServletContext context, String studentId, String notificationId) {
        if (notificationId == null || notificationId.isBlank()) {
            throw new StudentBusinessException(
                    ErrorCodes.VALIDATION_ERROR,
                    "notificationId is required.",
                    HttpServletResponse.SC_BAD_REQUEST
            );
        }
        try {
            List<NotificationRecord> notifications = JsonUtility.loadNotifications(context);
            NotificationRecord record = notifications.stream()
                    .filter(r -> notificationId.equals(r.getId()))
                    .findFirst()
                    .orElseThrow(() -> new StudentBusinessException(
                            ErrorCodes.APPLICATION_NOT_FOUND,
                            "Notification not found.",
                            HttpServletResponse.SC_NOT_FOUND
                    ));
            if (!ownsNotification(studentId, record)) {
                throw new StudentBusinessException(
                        ErrorCodes.FORBIDDEN_NOT_OWNER,
                        "You can only mark your own notifications.",
                        HttpServletResponse.SC_FORBIDDEN
                );
            }
            record.setRead(true);
            JsonUtility.saveNotifications(context, notifications);
        } catch (IOException e) {
            throw new RuntimeException("Failed to mark notification as read.", e);
        }
    }

    private boolean ownsNotification(String studentId, NotificationRecord record) {
        if (!studentId.equals(record.getRecipientId())) {
            return false;
        }
        String role = trimToEmpty(record.getRecipientRole()).toLowerCase(Locale.ROOT);
        return "student".equals(role);
    }

    private boolean isAnnouncement(NotificationRecord record) {
        return TYPE_ANNOUNCEMENT.equalsIgnoreCase(trimToEmpty(record.getType()));
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            String trimmed = trimToEmpty(value);
            if (!trimmed.isBlank()) {
                return trimmed;
            }
        }
        return "";
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
