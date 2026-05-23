package com.ta.service.student;

import com.ta.constant.ErrorCodes;
import com.ta.model.NotificationRecord;
import com.ta.testsupport.AdminServiceTestSupport;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StudentNotificationServiceTest extends AdminServiceTestSupport {

    private StudentNotificationService service;

    @BeforeEach
    void seed() throws Exception {
        service = new StudentNotificationService();
        writeJobs(List.of(job("job_1", MO_ID, "open", "approved", true, false, false, 1, 8)));
        NotificationRecord workflow = notification("noti_workflow", STUDENT_USER_ID, "student", false);
        workflow.setJobId("job_1");
        workflow.setCreatedAt("2026-05-11T10:00:00Z");
        NotificationRecord announcement = notification("noti_announcement", STUDENT_USER_ID, "student", true);
        announcement.setType("announcement");
        announcement.setTitle("Announcement");
        announcement.setCreatedAt("2026-05-12T10:00:00Z");
        NotificationRecord other = notification("noti_other", MO_ID, "mo", false);
        writeNotifications(List.of(workflow, announcement, other));
    }

    @Test
    void list_returnsOnlyStudentNotificationsSortedByTime() {
        var response = service.list(servletContext, STUDENT_USER_ID);

        assertEquals(1, response.getUnreadCount());
        assertEquals(2, response.getItems().size());
        assertEquals("noti_announcement", response.getItems().get(0).getNotificationId());
        assertEquals("announcement", response.getItems().get(0).getType());
        assertEquals("System announcement", response.getItems().get(0).getJobName());
        assertEquals("Job job_1", response.getItems().get(1).getJobName());
    }

    @Test
    void markRead_updatesOwnedNotification() throws Exception {
        service.markRead(servletContext, STUDENT_USER_ID, "noti_workflow");

        assertTrue(readNotifications().stream()
                .filter(n -> "noti_workflow".equals(n.getId()))
                .findFirst()
                .orElseThrow()
                .isRead());
    }

    @Test
    void markRead_otherRecipient_throws403AndDoesNotMutate() throws Exception {
        StudentBusinessException ex = assertThrows(
                StudentBusinessException.class,
                () -> service.markRead(servletContext, STUDENT_USER_ID, "noti_other")
        );

        assertEquals(ErrorCodes.FORBIDDEN_NOT_OWNER, ex.getCode());
        assertEquals(HttpServletResponse.SC_FORBIDDEN, ex.getHttpStatus());
        assertFalse(readNotifications().stream()
                .filter(n -> "noti_other".equals(n.getId()))
                .findFirst()
                .orElseThrow()
                .isRead());
    }
}
