package com.ta.web;

import com.ta.service.admin.AdminBusinessException;
import com.ta.service.admin.AdminDashboardService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "AdminDashboardServlet", urlPatterns = {"/api/admin/dashboard"})
public class AdminDashboardServlet extends AdminBaseServlet {
    private final AdminDashboardService adminDashboardService = new AdminDashboardService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            if (!requireAdmin(req, resp)) {
                return;
            }

            writeSuccess(resp, adminDashboardService.loadDashboard(
                    getServletContext(),
                    req.getParameter("status"),
                    req.getParameter("department")
            ));
        } catch (AdminBusinessException ex) {
            writeError(resp, ex.getHttpStatus(), ex.getCode(), ex.getMessage());
        } catch (Exception ex) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", ex.getMessage());
        }
    }
}
