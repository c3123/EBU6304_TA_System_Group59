package com.ta.web;

import com.ta.service.admin.AdminRecruitmentOutcomeService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "AdminRecruitmentOutcomeServlet", urlPatterns = {"/api/admin/recruitment-outcome"})
public class AdminRecruitmentOutcomeServlet extends AdminBaseServlet {
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
            writeSuccess(resp, recruitmentOutcomeService.load(getServletContext(), vacancyTop));
        } catch (Exception ex) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", ex.getMessage());
        }
    }
}
