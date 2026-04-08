package com.ta.web;

import com.ta.dto.admin.AdminPasswordResetRequest;
import com.ta.service.admin.AdminBusinessException;
import com.ta.service.admin.AdminUserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "AdminUserResetPasswordServlet", urlPatterns = {"/api/admin/users/reset-password/*"})
public class AdminUserResetPasswordServlet extends AdminBaseServlet {
    private final AdminUserService adminUserService = new AdminUserService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            if (!requireAdmin(req, resp)) {
                return;
            }
            String userId = extractLastPathSegment(req);
            AdminPasswordResetRequest request = readJson(req, AdminPasswordResetRequest.class);
            writeSuccess(resp, adminUserService.resetPassword(
                    getServletContext(),
                    userId,
                    request == null ? null : request.getNewPassword()
            ));
        } catch (AdminBusinessException ex) {
            writeError(resp, ex.getHttpStatus(), ex.getCode(), ex.getMessage());
        } catch (Exception ex) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", ex.getMessage());
        }
    }

    private String extractLastPathSegment(HttpServletRequest req) {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.isBlank() || "/".equals(pathInfo)) {
            return null;
        }
        String normalized = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;
        int lastSlash = normalized.lastIndexOf('/');
        return lastSlash >= 0 ? normalized.substring(lastSlash + 1) : normalized;
    }
}
