package com.ta.web;

import com.ta.dto.admin.AdminRecruitmentOutcomeResponse;
import com.ta.service.admin.AdminBusinessException;
import com.ta.service.admin.AdminRecruitmentOutcomeService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@WebServlet(name = "AdminRecruitmentOutcomeExportServlet", urlPatterns = {"/api/admin/recruitment-outcome/export"})
public class AdminRecruitmentOutcomeExportServlet extends AdminBaseServlet {
    private final AdminRecruitmentOutcomeService recruitmentOutcomeService = new AdminRecruitmentOutcomeService();

    private static int parseVacancyTop(String raw) {
        if (raw == null || raw.isBlank()) {
            return 10;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException ex) {
            return 10;
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            if (!requireAdmin(req, resp)) {
                return;
            }
            int vacancyTop = parseVacancyTop(req.getParameter("vacancyTop"));
            AdminRecruitmentOutcomeResponse data = recruitmentOutcomeService.load(
                    getServletContext(),
                    vacancyTop,
                    req.getParameter("jobSince"),
                    req.getParameter("jobUntil"));
            String csv = recruitmentOutcomeService.buildRecruitmentOutcomeCsv(data);

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
            resp.setContentType("text/csv;charset=UTF-8");
            resp.setHeader("Content-Disposition", "attachment; filename=\"recruitment-outcome-snapshot.csv\"");
            resp.getWriter().write(csv);
        } catch (AdminBusinessException ex) {
            writeError(resp, ex.getHttpStatus(), ex.getCode(), ex.getMessage());
        } catch (Exception ex) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", ex.getMessage());
        }
    }
}
