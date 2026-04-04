package com.ta.web.mo;

import com.ta.constant.ErrorCodes;
import com.ta.dto.mo.MoDemandCreateRequest;
import com.ta.service.mo.MoBusinessException;
import com.ta.service.mo.MoDemandService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Route contract: POST /api/mo/demands
 * Responsibility: create MO demand with pending approval status.
 *
 * Input JSON:
 * - courseName
 * - plannedCount
 * - hourMin
 * - hourMax
 */
@WebServlet(name = "MoDemandCreateServlet", urlPatterns = {"/api/mo/demands"})
public class MoDemandCreateServlet extends MoBaseServlet {
    private final MoDemandService moDemandService = new MoDemandService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String moId = getMoIdFromSession(req);
            if (moId == null || moId.isBlank()) {
                writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, ErrorCodes.UNAUTHORIZED, "MO login required.");
                return;
            }

            MoDemandCreateRequest request = readJson(req, MoDemandCreateRequest.class);
            if (request == null) {
                writeError(resp, HttpServletResponse.SC_BAD_REQUEST, ErrorCodes.VALIDATION_ERROR, "Request body is required.");
                return;
            }

            String jobId = moDemandService.createDemand(getServletContext(), moId, request);
            Map<String, String> data = new HashMap<>();
            data.put("jobId", jobId);
            writeSuccess(resp, data);
        } catch (MoBusinessException ex) {
            writeError(resp, ex.getHttpStatus(), ex.getCode(), ex.getMessage());
        } catch (Exception ex) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", ex.getMessage());
        }
    }
}
