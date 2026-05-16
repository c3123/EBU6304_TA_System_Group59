package com.ta.service.admin;

import com.ta.constant.ErrorCodes;
import com.ta.dto.admin.AdminAnnouncementCreateRequest;
import com.ta.dto.admin.AdminAnnouncementCreateResponse;
import com.ta.dto.admin.AdminAnnouncementListResponse;
import com.ta.dto.admin.AdminAnnouncementSummaryItem;
import com.ta.model.NotificationRecord;
import com.ta.model.User;
import com.ta.util.IsoTime;
import com.ta.util.JsonUtility;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Fans out one admin announcement into per-recipient notification rows in notifications.json.
 */
public class AdminAnnouncementService {

    private static final Set<String> ALLOWED_TARGETS = Set.of("student", "teacher", "all");
    private static final int TITLE_MAX = 200;
    private static final int BODY_MAX = 4000;

    public AdminAnnouncementCreateResponse create(ServletContext context, AdminAnnouncementCreateRequest request) throws IOException {
        String title = trim(request == null ? null : request.getTitle());
        String body = trim(request == null ? null : request.getBody());
        String targetRole = normalizeTarget(request == null ? null : request.getTargetRole());

        if (title.isBlank()) {
            throw validationError("title is required.");
        }
        if (title.length() > TITLE_MAX) {
            throw validationError("title must be at most " + TITLE_MAX + " characters.");
        }
        if (body.isBlank()) {
            throw validationError("body is required.");
        }
        if (body.length() > BODY_MAX) {
            throw validationError("body must be at most " + BODY_MAX + " characters.");
        }

        List<User> recipients = resolveRecipients(JsonUtility.loadUsers(context), targetRole);
        if (recipients.isEmpty()) {
            throw validationError("No recipients match the selected target role.");
        }

        String announcementId = "ann_" + System.currentTimeMillis();
        String createdAt = IsoTime.utcNowSeconds();
        List<NotificationRecord> notifications = JsonUtility.loadNotifications(context);

        for (User user : recipients) {
            notifications.add(toAnnouncementRecord(announcementId, title, body, createdAt, user));
        }
        JsonUtility.saveNotifications(context, notifications);

        AdminAnnouncementCreateResponse response = new AdminAnnouncementCreateResponse();
        response.setAnnouncementId(announcementId);
        response.setTargetRole(targetRole);
        response.setRecipientCount(recipients.size());
        response.setCreatedAt(createdAt);
        return response;
    }

    public AdminAnnouncementListResponse list(ServletContext context) throws IOException {
        List<NotificationRecord> notifications = JsonUtility.loadNotifications(context);
        Map<String, List<NotificationRecord>> byAnnouncement = new LinkedHashMap<>();
        for (NotificationRecord record : notifications) {
            if (!isAnnouncementRecord(record)) {
                continue;
            }
            String announcementId = trim(record.getAnnouncementId());
            if (announcementId.isBlank()) {
                continue;
            }
            byAnnouncement.computeIfAbsent(announcementId, key -> new ArrayList<>()).add(record);
        }

        List<AdminAnnouncementSummaryItem> items = new ArrayList<>();
        for (Map.Entry<String, List<NotificationRecord>> entry : byAnnouncement.entrySet()) {
            List<NotificationRecord> rows = entry.getValue();
            if (rows.isEmpty()) {
                continue;
            }
            NotificationRecord sample = rows.get(0);
            AdminAnnouncementSummaryItem item = new AdminAnnouncementSummaryItem();
            item.setAnnouncementId(entry.getKey());
            item.setTitle(trim(sample.getTitle()));
            item.setBodyPreview(previewBody(sample.getMessage()));
            item.setTargetRole(inferTargetRole(rows));
            item.setRecipientCount(rows.size());
            item.setCreatedAt(firstNonBlank(sample.getCreatedAt(), sample.getApplicationTime()));
            items.add(item);
        }

        items.sort(Comparator.comparing(AdminAnnouncementSummaryItem::getCreatedAt).reversed());
        AdminAnnouncementListResponse response = new AdminAnnouncementListResponse();
        response.setItems(items);
        return response;
    }

    private List<User> resolveRecipients(List<User> users, String targetRole) {
        List<User> recipients = new ArrayList<>();
        for (User user : users) {
            if (user.getId() == null || user.getId().isBlank()) {
                continue;
            }
            String role = trim(user.getRole()).toLowerCase(Locale.ROOT);
            if ("student".equals(targetRole) && "student".equals(role)) {
                recipients.add(user);
            } else if ("teacher".equals(targetRole) && "teacher".equals(role)) {
                recipients.add(user);
            } else if ("all".equals(targetRole) && ("student".equals(role) || "teacher".equals(role))) {
                recipients.add(user);
            }
        }
        return recipients;
    }

    private NotificationRecord toAnnouncementRecord(String announcementId,
                                                      String title,
                                                      String body,
                                                      String createdAt,
                                                      User user) {
        String role = trim(user.getRole()).toLowerCase(Locale.ROOT);
        String recipientRole = "teacher".equals(role) ? "mo" : "student";

        NotificationRecord record = new NotificationRecord();
        record.setId(announcementId + "_" + user.getId());
        record.setAnnouncementId(announcementId);
        record.setType("announcement");
        record.setTitle(title);
        record.setMessage(body);
        record.setCreatedAt(createdAt);
        record.setApplicationTime(createdAt);
        record.setRead(false);
        record.setRecipientId(user.getId());
        record.setRecipientRole(recipientRole);
        record.setMoId(user.getId());
        record.setApplicantName("System");
        return record;
    }

    private String normalizeTarget(String raw) {
        String normalized = trim(raw).toLowerCase(Locale.ROOT);
        if (normalized.isBlank()) {
            throw validationError("targetRole is required (student, teacher, or all).");
        }
        if (!ALLOWED_TARGETS.contains(normalized)) {
            throw validationError("targetRole must be student, teacher, or all.");
        }
        return normalized;
    }

    private boolean isAnnouncementRecord(NotificationRecord record) {
        return "announcement".equalsIgnoreCase(trim(record.getType()));
    }

    private String inferTargetRole(List<NotificationRecord> rows) {
        boolean hasStudent = false;
        boolean hasTeacher = false;
        for (NotificationRecord row : rows) {
            String role = trim(row.getRecipientRole()).toLowerCase(Locale.ROOT);
            if ("student".equals(role)) {
                hasStudent = true;
            }
            if ("mo".equals(role) || "teacher".equals(role)) {
                hasTeacher = true;
            }
        }
        if (hasStudent && hasTeacher) {
            return "all";
        }
        if (hasTeacher) {
            return "teacher";
        }
        return "student";
    }

    private String previewBody(String body) {
        String text = trim(body);
        if (text.length() <= 160) {
            return text;
        }
        return text.substring(0, 157) + "...";
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            String trimmed = trim(value);
            if (!trimmed.isBlank()) {
                return trimmed;
            }
        }
        return "";
    }

    private AdminBusinessException validationError(String message) {
        return new AdminBusinessException(
                ErrorCodes.VALIDATION_ERROR,
                message,
                HttpServletResponse.SC_BAD_REQUEST
        );
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
