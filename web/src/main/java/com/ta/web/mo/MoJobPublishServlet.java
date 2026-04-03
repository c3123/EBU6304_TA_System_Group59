package com.ta.web.mo;

import com.ta.constant.ErrorCodes;
import com.ta.dto.mo.MoJobPublishRequest;
import com.ta.dto.mo.MoJobPublishResponse;
import com.ta.service.mo.MoBusinessException;
import com.ta.service.mo.MoJobService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Route contract: POST /api/mo/jobs/{jobId}/publish
 * Temporary mapping in this scaffold: POST /api/mo/jobs/publish/{jobId}
 * Responsibility: publish approved demand and lock deadline afterward.
 */
@WebServlet(name = "MoJobPublishServlet", urlPatterns = {"/api/mo/jobs/publish/*"})
public class MoJobPublishServlet extends MoBaseServlet {
    private final MoJobService moJobService = new MoJobService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String moId = getMoIdFromSession(req);
            if (moId == null || moId.isBlank()) {
                writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, ErrorCodes.UNAUTHORIZED, "MO login required.");
                return;
            }

            String jobId = getLastPathSegment(req);
            if (jobId == null || jobId.isBlank()) {
                writeError(resp, HttpServletResponse.SC_BAD_REQUEST, ErrorCodes.VALIDATION_ERROR, "jobId is required.");
                return;
            }

            MoJobPublishRequest request = readJson(req, MoJobPublishRequest.class);
            if (request == null) {
                writeError(resp, HttpServletResponse.SC_BAD_REQUEST, ErrorCodes.VALIDATION_ERROR, "Request body is required.");
                return;
            }

            MoJobPublishResponse data = moJobService.publishJob(getServletContext(), moId, jobId, request);
            writeSuccess(resp, data);
        } catch (MoBusinessException ex) {
            writeError(resp, ex.getHttpStatus(), ex.getCode(), ex.getMessage());
        } catch (Exception ex) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", ex.getMessage());
        }
    }
}
