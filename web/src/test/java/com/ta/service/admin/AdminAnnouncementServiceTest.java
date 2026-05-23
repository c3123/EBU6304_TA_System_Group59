package com.ta.service.admin;

import com.ta.constant.ErrorCodes;
import com.ta.dto.admin.AdminAnnouncementCreateRequest;
import com.ta.dto.admin.AdminAnnouncementCreateResponse;
import com.ta.testsupport.AdminServiceTestSupport;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminAnnouncementServiceTest extends AdminServiceTestSupport {

    private AdminAnnouncementService service;

    @BeforeEach
    void seed() throws Exception {
        service = new AdminAnnouncementService();
        writeUsers(List.of(
                user(ADMIN_ID, "Admin", "admin@demo.test", "admin"),
                user(STUDENT_USER_ID, "Student", "student@demo.test", "student"),
                user(MO_ID, "Teacher", "teacher@demo.test", "teacher")
        ));
        writeNotifications(List.of());
    }

    @Test
    void createAnnouncement_fansOutToStudentsAndTeachers() throws Exception {
        AdminAnnouncementCreateRequest request = new AdminAnnouncementCreateRequest();
        request.setTitle("System update");
        request.setBody("Demo message");
        request.setTargetRole("all");

        AdminAnnouncementCreateResponse response = service.create(servletContext, request);

        assertEquals("all", response.getTargetRole());
        assertEquals(2, response.getRecipientCount());
        assertEquals(2, readNotifications().size());
        assertTrue(readNotifications().stream().allMatch(n -> "announcement".equals(n.getType())));
        assertEquals(1, service.list(servletContext).getItems().size());
    }

    @Test
    void notifyTeacher_ignoresNonTeacherRecipient() throws Exception {
        service.notifyTeacher(servletContext, STUDENT_USER_ID, "Teacher only", "Message");

        assertTrue(readNotifications().isEmpty());
    }

    @Test
    void notifyStudent_addsStudentAnnouncement() throws Exception {
        service.notifyStudent(servletContext, STUDENT_USER_ID, "Student only", "Message");

        assertEquals(1, readNotifications().size());
        assertEquals(STUDENT_USER_ID, readNotifications().get(0).getRecipientId());
        assertEquals("student", readNotifications().get(0).getRecipientRole());
    }

    @Test
    void invalidTargetRole_throws400() {
        AdminAnnouncementCreateRequest request = new AdminAnnouncementCreateRequest();
        request.setTitle("Bad");
        request.setBody("Message");
        request.setTargetRole("admin");

        assertAdminBusinessException(
                () -> {
                    try {
                        service.create(servletContext, request);
                    } catch (AdminBusinessException ex) {
                        throw ex;
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                },
                ErrorCodes.VALIDATION_ERROR,
                HttpServletResponse.SC_BAD_REQUEST
        );
    }
}
