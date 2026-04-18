package com.ta.web.mo;

import com.ta.constant.ErrorCodes;
import com.ta.service.mo.MoBusinessException;
import com.ta.service.mo.MoNotificationService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

@WebServlet(name = "MoNotificationReadServlet", urlPatterns = {"/api/mo/notifications/read/*"})
public class MoNotificationReadServlet extends MoBaseServlet {
    private final MoNotificationService moNotificationService = new MoNotificationService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String moId = getMoIdFromSession(req);
            if (moId == null || moId.isBlank()) {
                writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, ErrorCodes.UNAUTHORIZED, "MO login required.");
                return;
            }
            String notificationId = getLastPathSegment(req);
            moNotificationService.markRead(getServletContext(), moId, notificationId);
            writeSuccess(resp, Map.of("notificationId", notificationId, "read", true));
        } catch (MoBusinessException ex) {
            writeError(resp, ex.getHttpStatus(), ex.getCode(), ex.getMessage());
        } catch (Exception ex) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", ex.getMessage());
        }
    }
}
