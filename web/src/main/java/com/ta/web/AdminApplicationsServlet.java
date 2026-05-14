package com.ta.web;

import com.ta.service.admin.AdminApplicationArchiveService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * GET /api/admin/applications?jobId= — read-only list with MO decision fields for archiving (MO_10).
 */
@WebServlet(name = "AdminApplicationsServlet", urlPatterns = {"/api/admin/applications"})
public class AdminApplicationsServlet extends AdminBaseServlet {
    private final AdminApplicationArchiveService adminApplicationArchiveService = new AdminApplicationArchiveService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireAdmin(req, resp)) {
            return;
        }
        try {
            writeSuccess(resp, adminApplicationArchiveService.listArchive(
                    getServletContext(),
                    req.getParameter("status"),
                    req.getParameter("jobId"),
                    req.getParameter("teacher"),
                    req.getParameter("student")
            ));
        } catch (Exception ex) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", ex.getMessage());
        }
    }
}
