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

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            if (!requireAdmin(req, resp)) {
                return;
            }
            writeSuccess(resp, recruitmentOutcomeService.load(getServletContext()));
        } catch (Exception ex) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", ex.getMessage());
        }
    }
}
