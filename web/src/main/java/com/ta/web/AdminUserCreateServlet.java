package com.ta.web;

import com.ta.dto.admin.AdminUserCreateRequest;
import com.ta.service.admin.AdminBusinessException;
import com.ta.service.admin.AdminUserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "AdminUserCreateServlet", urlPatterns = {"/api/admin/users"})
public class AdminUserCreateServlet extends AdminBaseServlet {
    private final AdminUserService adminUserService = new AdminUserService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            if (!requireAdmin(req, resp)) {
                return;
            }
            AdminUserCreateRequest request = readJson(req, AdminUserCreateRequest.class);
            writeSuccess(resp, adminUserService.createUser(getServletContext(), request));
        } catch (AdminBusinessException ex) {
            writeError(resp, ex.getHttpStatus(), ex.getCode(), ex.getMessage());
        } catch (Exception ex) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", ex.getMessage());
        }
    }
}
