package com.ta.web;

import com.ta.service.admin.AdminBusinessException;
import com.ta.service.admin.AdminDemandReviewService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "AdminDemandsServlet", urlPatterns = {"/api/admin/demands"})
public class AdminDemandsServlet extends AdminBaseServlet {
    private final AdminDemandReviewService adminDemandReviewService = new AdminDemandReviewService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            if (!requireAdmin(req, resp)) {
                return;
            }
            writeSuccess(resp, adminDemandReviewService.listDemands(getServletContext(), req.getParameter("status")));
        } catch (AdminBusinessException ex) {
            writeError(resp, ex.getHttpStatus(), ex.getCode(), ex.getMessage());
        } catch (Exception ex) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", ex.getMessage());
        }
    }
}
