package com.ta.web.mo;

import com.ta.constant.ErrorCodes;
import com.ta.service.mo.MoApplicationService;
import com.ta.service.mo.MoBusinessException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Route contract: GET /api/mo/applications/{applicationId}
 * Temporary mapping in this scaffold: GET /api/mo/applications/detail/{applicationId}
 * Responsibility: return detail and auto-transition status pending -> viewed.
 */
@WebServlet(name = "MoApplicationDetailServlet", urlPatterns = {"/api/mo/applications/detail/*"})
public class MoApplicationDetailServlet extends MoBaseServlet {
    private final MoApplicationService moApplicationService = new MoApplicationService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String moId = getMoIdFromSession(req);
            if (moId == null || moId.isBlank()) {
                writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, ErrorCodes.UNAUTHORIZED, "MO login required.");
                return;
            }

            String applicationId = getLastPathSegment(req);
            if (applicationId == null || applicationId.isBlank()) {
                writeError(resp, HttpServletResponse.SC_BAD_REQUEST, ErrorCodes.VALIDATION_ERROR, "applicationId is required.");
                return;
            }

            Object data = moApplicationService.getDetailAndMarkViewed(getServletContext(), moId, applicationId);
            writeSuccess(resp, data);
        } catch (MoBusinessException ex) {
            writeError(resp, ex.getHttpStatus(), ex.getCode(), ex.getMessage());
        } catch (Exception ex) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", ex.getMessage());
        }
    }
}
