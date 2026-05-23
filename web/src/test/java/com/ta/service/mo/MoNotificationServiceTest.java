package com.ta.service.mo;

import com.ta.constant.ErrorCodes;
import com.ta.dto.mo.MoNotificationItemResponse;
import com.ta.dto.mo.MoNotificationListResponse;
import com.ta.model.ApplicationRecord;
import com.ta.model.NotificationRecord;
import com.ta.testsupport.MoTestSupport;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MoNotificationServiceTest extends MoTestSupport {

    private final MoNotificationService service = new MoNotificationService();

    @BeforeEach
    void seedFixtures() throws Exception {
        writeJobs(defaultJobs());
        writeApplications(defaultApplications());
        writeNotifications(new ArrayList<>());
    }

    @Test
    void list_backfillsMissingWorkflowNotifications() throws Exception {
        MoNotificationListResponse response = service.list(servletContext, MO_ID);
        assertFalse(response.getItems().isEmpty());
        assertTrue(response.getUnreadCount() > 0);

        List<NotificationRecord> saved = readNotifications();
        assertTrue(saved.stream().anyMatch(n -> "noti_app_pending".equals(n.getId())));
    }

    @Test
    void list_doesNotDuplicateWhenApplicationIdAlreadyMapped() throws Exception {
        NotificationRecord existing = new NotificationRecord();
        existing.setId("noti_apply_custom");
        existing.setMoId(MO_ID);
        existing.setRecipientId(MO_ID);
        existing.setApplicationId("app_pending");
        existing.setJobId(JOB_ID);
        existing.setApplicantName("Test");
        existing.setApplicationTime("2025-01-01T00:00:00Z");
        existing.setRead(false);
        writeNotifications(List.of(existing));

        service.list(servletContext, MO_ID);
        List<NotificationRecord> saved = readNotifications();
        long forPending = saved.stream().filter(n -> "app_pending".equals(n.getApplicationId())).count();
        assertEquals(1, forPending);
        assertFalse(saved.stream().anyMatch(n -> "noti_app_pending".equals(n.getId())));
    }

    @Test
    void list_announcementMapsTitleAndJobName() throws Exception {
        NotificationRecord ann = new NotificationRecord();
        ann.setId("noti_ann_1");
        ann.setMoId(MO_ID);
        ann.setRecipientId(MO_ID);
        ann.setType("announcement");
        ann.setTitle("Workload alert");
        ann.setMessage("Threshold exceeded");
        ann.setCreatedAt("2025-02-01T12:00:00Z");
        ann.setRead(false);
        writeNotifications(List.of(ann));

        MoNotificationListResponse response = service.list(servletContext, MO_ID);
        MoNotificationItemResponse item = response.getItems().stream()
                .filter(i -> "noti_ann_1".equals(i.getNotificationId()))
                .findFirst()
                .orElseThrow();
        assertEquals("announcement", item.getType());
        assertEquals("Workload alert", item.getTitle());
        assertEquals("System announcement", item.getJobName());
    }

    @Test
    void list_isolatesOtherMoNotifications() throws Exception {
        NotificationRecord other = new NotificationRecord();
        other.setId("noti_other");
        other.setMoId(OTHER_MO_ID);
        other.setRecipientId(OTHER_MO_ID);
        other.setMessage("secret");
        other.setRead(false);
        writeNotifications(List.of(other));

        MoNotificationListResponse response = service.list(servletContext, MO_ID);
        assertTrue(response.getItems().stream().noneMatch(i -> "noti_other".equals(i.getNotificationId())));
    }

    @Test
    void list_sortsByApplicationTimeDescending() throws Exception {
        NotificationRecord older = workflowNoti("noti_old", "app_shortlisted", "2025-01-01T00:00:00Z");
        NotificationRecord newer = workflowNoti("noti_new", "app_pending", "2025-03-01T00:00:00Z");
        writeNotifications(List.of(older, newer));
        writeApplications(List.of(
                application("app_pending", JOB_ID, "pending", true),
                application("app_shortlisted", JOB_ID, "shortlisted", true)
        ));

        MoNotificationListResponse response = service.list(servletContext, MO_ID);
        List<String> times = response.getItems().stream()
                .map(MoNotificationItemResponse::getApplicationTime)
                .collect(Collectors.toList());
        for (int i = 0; i < times.size() - 1; i++) {
            assertTrue(times.get(i).compareTo(times.get(i + 1)) >= 0);
        }
    }

    @Test
    void markRead_persistsReadFlag() throws Exception {
        NotificationRecord n = workflowNoti("noti_read_test", "app_pending", "2025-01-01T00:00:00Z");
        writeNotifications(List.of(n));

        service.markRead(servletContext, MO_ID, "noti_read_test");
        NotificationRecord saved = readNotifications().stream()
                .filter(r -> "noti_read_test".equals(r.getId()))
                .findFirst()
                .orElseThrow();
        assertTrue(saved.isRead());
    }

    @Test
    void markRead_idempotentWhenAlreadyRead() throws Exception {
        NotificationRecord n = workflowNoti("noti_already", "app_pending", "2025-01-01T00:00:00Z");
        n.setRead(true);
        writeNotifications(List.of(n));

        service.markRead(servletContext, MO_ID, "noti_already");
        assertTrue(readNotifications().get(0).isRead());
    }

    @Test
    void markRead_blankId_throws400() {
        assertMoBusinessException(
                () -> service.markRead(servletContext, MO_ID, ""),
                ErrorCodes.VALIDATION_ERROR,
                HttpServletResponse.SC_BAD_REQUEST
        );
    }

    @Test
    void markRead_unknownId_throws404() {
        assertMoBusinessException(
                () -> service.markRead(servletContext, MO_ID, "missing"),
                ErrorCodes.APPLICATION_NOT_FOUND,
                HttpServletResponse.SC_NOT_FOUND
        );
    }

    @Test
    void markRead_otherMoNotification_throws403() throws Exception {
        NotificationRecord other = workflowNoti("noti_foreign", "app_pending", "2025-01-01T00:00:00Z");
        other.setMoId(OTHER_MO_ID);
        other.setRecipientId(OTHER_MO_ID);
        writeNotifications(List.of(other));

        assertMoBusinessException(
                () -> service.markRead(servletContext, MO_ID, "noti_foreign"),
                ErrorCodes.FORBIDDEN_NOT_OWNER,
                HttpServletResponse.SC_FORBIDDEN
        );
    }

    private static NotificationRecord workflowNoti(String id, String applicationId, String time) {
        NotificationRecord n = new NotificationRecord();
        n.setId(id);
        n.setMoId(MO_ID);
        n.setRecipientId(MO_ID);
        n.setApplicationId(applicationId);
        n.setJobId(JOB_ID);
        n.setApplicantName("Test Student");
        n.setApplicationTime(time);
        n.setMessage("applied");
        n.setRead(false);
        return n;
    }
}
