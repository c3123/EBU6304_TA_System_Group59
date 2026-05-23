package com.ta.service.account;

import com.ta.constant.ErrorCodes;
import com.ta.model.User;
import com.ta.testsupport.MoTestSupport;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountServiceTest extends MoTestSupport {

    private AccountService service;

    @BeforeEach
    void seedUsers() throws Exception {
        service = new AccountService();
        writeUsers(List.of(
                user("stu001", "student", "student@demo.test"),
                user("mo001", "teacher", "teacher@demo.test"),
                user("admin001", "admin", "admin@demo.test")
        ));
    }

    @Test
    void changePassword_studentTeacherAndAdmin_success() throws Exception {
        assertTrue(service.changePassword(servletContext, "stu001", "oldPass1", "newPass1", "newPass1").isChanged());
        assertTrue(service.changePassword(servletContext, "mo001", "oldPass1", "newPass2", "newPass2").isChanged());
        assertTrue(service.changePassword(servletContext, "admin001", "oldPass1", "newPass3", "newPass3").isChanged());

        assertEquals("newPass1", findUser("stu001").getPassword());
        assertEquals("newPass2", findUser("mo001").getPassword());
        assertEquals("newPass3", findUser("admin001").getPassword());
    }

    @Test
    void changePassword_wrongOldPassword_rejected() {
        AccountBusinessException ex = assertThrows(
                AccountBusinessException.class,
                () -> service.changePassword(servletContext, "stu001", "wrong", "newPass1", "newPass1")
        );
        assertEquals(ErrorCodes.VALIDATION_ERROR, ex.getCode());
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, ex.getHttpStatus());
    }

    @Test
    void changePassword_confirmMismatch_rejected() {
        AccountBusinessException ex = assertThrows(
                AccountBusinessException.class,
                () -> service.changePassword(servletContext, "stu001", "oldPass1", "newPass1", "newPass2")
        );
        assertEquals(ErrorCodes.VALIDATION_ERROR, ex.getCode());
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, ex.getHttpStatus());
    }

    @Test
    void changePassword_newPasswordSameAsOld_rejected() {
        AccountBusinessException ex = assertThrows(
                AccountBusinessException.class,
                () -> service.changePassword(servletContext, "stu001", "oldPass1", "oldPass1", "oldPass1")
        );
        assertEquals(ErrorCodes.VALIDATION_ERROR, ex.getCode());
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, ex.getHttpStatus());
    }

    @Test
    void changePassword_missingLogin_rejected401() {
        AccountBusinessException ex = assertThrows(
                AccountBusinessException.class,
                () -> service.changePassword(servletContext, "", "oldPass1", "newPass1", "newPass1")
        );
        assertEquals(ErrorCodes.UNAUTHORIZED, ex.getCode());
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, ex.getHttpStatus());
    }

    private User findUser(String id) throws Exception {
        return readUsers().stream()
                .filter(user -> id.equals(user.getId()))
                .findFirst()
                .orElseThrow();
    }

    private User user(String id, String role, String email) {
        User user = new User();
        user.setId(id);
        user.setName(id);
        user.setEmail(email);
        user.setRole(role);
        user.setPassword("oldPass1");
        return user;
    }
}
