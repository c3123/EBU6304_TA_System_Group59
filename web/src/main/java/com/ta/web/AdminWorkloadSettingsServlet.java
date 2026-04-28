package com.ta.web;

import com.ta.dto.admin.AdminWorkloadSettingsRequest;
import com.ta.service.admin.AdminBusinessException;
import com.ta.service.admin.AdminWorkloadSettingsService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "AdminWorkloadSettingsServlet", urlPatterns = {"/api/admin/settings/workload-threshold"})
public class AdminWorkloadSettingsServlet extends AdminBaseServlet {
    private final AdminWorkloadSettingsService adminWorkloadSettingsService = new AdminWorkloadSettingsService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            if (!requireAdmin(req, resp)) {
                return;
            }
            writeSuccess(resp, adminWorkloadSettingsService.getSettings(getServletContext()));
        } catch (AdminBusinessException ex) {
            writeError(resp, ex.getHttpStatus(), ex.getCode(), ex.getMessage());
        } catch (Exception ex) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", ex.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            if (!requireAdmin(req, resp)) {
                return;
            }
            AdminWorkloadSettingsRequest request = readJson(req, AdminWorkloadSettingsRequest.class);
            writeSuccess(resp, adminWorkloadSettingsService.saveThreshold(
                    getServletContext(),
                    request == null ? null : request.getWorkloadThresholdHours()
            ));
        } catch (AdminBusinessException ex) {
            writeError(resp, ex.getHttpStatus(), ex.getCode(), ex.getMessage());
        } catch (Exception ex) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", ex.getMessage());
        }
    }
}
