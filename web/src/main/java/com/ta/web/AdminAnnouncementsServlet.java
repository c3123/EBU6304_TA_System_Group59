package com.ta.web;

import com.ta.dto.admin.AdminAnnouncementCreateRequest;
import com.ta.service.admin.AdminAnnouncementService;
import com.ta.service.admin.AdminBusinessException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "AdminAnnouncementsServlet", urlPatterns = {"/api/admin/announcements"})
public class AdminAnnouncementsServlet extends AdminBaseServlet {
    private final AdminAnnouncementService announcementService = new AdminAnnouncementService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            if (!requireAdmin(req, resp)) {
                return;
            }
            AdminAnnouncementCreateRequest request = readJson(req, AdminAnnouncementCreateRequest.class);
            writeSuccess(resp, announcementService.create(getServletContext(), request));
        } catch (AdminBusinessException ex) {
            writeError(resp, ex.getHttpStatus(), ex.getCode(), ex.getMessage());
        } catch (Exception ex) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", ex.getMessage());
        }
    }
}
