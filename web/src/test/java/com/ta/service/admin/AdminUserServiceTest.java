package com.ta.service.admin;

import com.ta.constant.ErrorCodes;
import com.ta.dto.admin.AdminUserCreateRequest;
import com.ta.dto.admin.AdminUserDeleteResponse;
import com.ta.dto.admin.AdminUserItemResponse;
import com.ta.model.StudentProfile;
import com.ta.model.User;
import com.ta.testsupport.AdminServiceTestSupport;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminUserServiceTest extends AdminServiceTestSupport {

    private AdminUserService service;

    @BeforeEach
    void seed() throws Exception {
        service = new AdminUserService();
        writeUsers(List.of(
                user(ADMIN_ID, "Admin", "admin@demo.test", "admin"),
                user("admin_backup", "Backup Admin", "backup@demo.test", "admin"),
                user(STUDENT_USER_ID, "Student One", "student1@demo.test", "student"),
                user(MO_ID, "Teacher One", "teacher1@demo.test", "teacher")
        ));
        writeStudents(List.of(profile(STUDENT_USER_ID, "Student One")));
    }

    @Test
    void createStudent_addsUserAndProfile() throws Exception {
        AdminUserCreateRequest request = new AdminUserCreateRequest();
        request.setRole("student");
        request.setName("New Student");
        request.setEmail("new.student@demo.test");
        request.setPassword("demo123");
        request.setStudentId("SNEW");
        request.setProgramme("MSc AI");

        AdminUserItemResponse response = service.createUser(servletContext, request);

        assertEquals("stu001", response.getId());
        assertEquals("New Student", response.getName());
        assertTrue(readUsers().stream().anyMatch(u -> "new.student@demo.test".equals(u.getEmail())));
        StudentProfile profile = readStudents().stream()
                .filter(p -> response.getId().equals(p.getUserId()))
                .findFirst()
                .orElseThrow();
        assertEquals("SNEW", profile.getStudentId());
        assertEquals("MSc AI", profile.getProgramme());
    }

    @Test
    void createUser_duplicateEmail_throws400() {
        AdminUserCreateRequest request = new AdminUserCreateRequest();
        request.setRole("teacher");
        request.setName("Duplicate");
        request.setEmail("teacher1@demo.test");
        request.setPassword("demo123");

        assertAdminBusinessException(
                () -> service.createUser(servletContext, request),
                ErrorCodes.VALIDATION_ERROR,
                HttpServletResponse.SC_BAD_REQUEST
        );
    }

    @Test
    void deleteStudent_removesUserAndProfile() throws Exception {
        AdminUserDeleteResponse response = service.deleteUser(servletContext, ADMIN_ID, STUDENT_USER_ID);

        assertTrue(response.isDeleted());
        assertFalse(readUsers().stream().anyMatch(u -> STUDENT_USER_ID.equals(u.getId())));
        assertFalse(readStudents().stream().anyMatch(p -> STUDENT_USER_ID.equals(p.getUserId())));
    }

    @Test
    void deleteCurrentAdmin_throws400() {
        assertAdminBusinessException(
                () -> service.deleteUser(servletContext, ADMIN_ID, ADMIN_ID),
                ErrorCodes.VALIDATION_ERROR,
                HttpServletResponse.SC_BAD_REQUEST
        );
    }

    @Test
    void deleteLastAdmin_throws400() throws Exception {
        writeUsers(List.of(user(ADMIN_ID, "Admin", "admin@demo.test", "admin")));

        assertAdminBusinessException(
                () -> service.deleteUser(servletContext, "someone_else", ADMIN_ID),
                ErrorCodes.VALIDATION_ERROR,
                HttpServletResponse.SC_BAD_REQUEST
        );
    }

    @Test
    void resetPassword_updatesPassword() throws Exception {
        service.resetPassword(servletContext, MO_ID, "newPass21");

        User teacher = readUsers().stream().filter(u -> MO_ID.equals(u.getId())).findFirst().orElseThrow();
        assertEquals("newPass21", teacher.getPassword());
    }
}
