package com.ta.web;

import com.ta.service.admin.AdminBusinessException;
import com.ta.service.admin.AdminReportService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@WebServlet(name = "AdminWorkloadReportExportServlet", urlPatterns = {"/api/admin/reports/workload"})
public class AdminWorkloadReportExportServlet extends AdminBaseServlet {
    private final AdminReportService adminReportService = new AdminReportService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            if (!requireAdmin(req, resp)) {
                return;
            }

            String format = req.getParameter("format");
            String content = adminReportService.buildWorkloadReport(getServletContext(), format);
            String normalizedFormat = format == null ? "" : format.trim().toLowerCase();

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
            resp.setContentType(adminReportService.resolveContentType(normalizedFormat));
            resp.setHeader("Content-Disposition", "attachment; filename=\"workload-report." + normalizedFormat + "\"");
            resp.getWriter().write(content);
        } catch (AdminBusinessException ex) {
            writeError(resp, ex.getHttpStatus(), ex.getCode(), ex.getMessage());
        } catch (Exception ex) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", ex.getMessage());
        }
    }
}
