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
 * Route contract: GET /api/mo/applications?jobId={optional}
 * Responsibility: list applications for current MO's own jobs only.
 */
@WebServlet(name = "MoApplicationListServlet", urlPatterns = {"/api/mo/applications"})
public class MoApplicationListServlet extends MoBaseServlet {
    private final MoApplicationService moApplicationService = new MoApplicationService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String moId = getMoIdFromSession(req);
            if (moId == null || moId.isBlank()) {
                writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, ErrorCodes.UNAUTHORIZED, "MO login required.");
                return;
            }

            String jobId = req.getParameter("jobId");
            String status = req.getParameter("status");
            Object data = moApplicationService.listApplications(getServletContext(), moId, jobId, status);
            writeSuccess(resp, data);
        } catch (MoBusinessException ex) {
            writeError(resp, ex.getHttpStatus(), ex.getCode(), ex.getMessage());
        } catch (Exception ex) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", ex.getMessage());
        }
    }
}
