package com.ta.web.mo;

import com.ta.constant.ErrorCodes;
import com.ta.dto.mo.MoApplicationBatchStatusRequest;
import com.ta.service.mo.MoApplicationService;
import com.ta.service.mo.MoBusinessException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * POST /api/mo/applications/batch/status — batch update status (transactional single save).
 */
@WebServlet(name = "MoApplicationBatchStatusServlet", urlPatterns = {"/api/mo/applications/batch/status"})
public class MoApplicationBatchStatusServlet extends MoBaseServlet {
    private final MoApplicationService moApplicationService = new MoApplicationService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String moId = getMoIdFromSession(req);
            if (moId == null || moId.isBlank()) {
                writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, ErrorCodes.UNAUTHORIZED, "MO login required.");
                return;
            }

            MoApplicationBatchStatusRequest body = readJson(req, MoApplicationBatchStatusRequest.class);
            if (body == null || body.getIds() == null || body.getIds().isEmpty()) {
                writeError(resp, HttpServletResponse.SC_BAD_REQUEST, ErrorCodes.VALIDATION_ERROR, "ids must be a non-empty array.");
                return;
            }

            Object data = moApplicationService.batchUpdateApplicationStatus(
                    getServletContext(),
                    moId,
                    body.getIds(),
                    body.getStatus()
            );
            writeSuccess(resp, data);
        } catch (MoBusinessException ex) {
            writeError(resp, ex.getHttpStatus(), ex.getCode(), ex.getMessage());
        } catch (Exception ex) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", ex.getMessage());
        }
    }
}
