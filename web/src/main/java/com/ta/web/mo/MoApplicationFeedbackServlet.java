package com.ta.web.mo;

import com.ta.constant.ErrorCodes;
import com.ta.dto.mo.MoApplicationFeedbackUpdateRequest;
import com.ta.service.mo.MoApplicationService;
import com.ta.service.mo.MoBusinessException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

/**
 * PATCH /api/mo/applications/feedback — decision feedback for hired/shortlisted/rejected (MO_10).
 */
@WebServlet(name = "MoApplicationFeedbackServlet", urlPatterns = {"/api/mo/applications/feedback"})
public class MoApplicationFeedbackServlet extends MoBaseServlet {
    private final MoApplicationService moApplicationService = new MoApplicationService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handle(req, resp);
    }

    private void handle(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String moId = getMoIdFromSession(req);
            if (moId == null || moId.isBlank()) {
                writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, ErrorCodes.UNAUTHORIZED, "MO login required.");
                return;
            }

            MoApplicationFeedbackUpdateRequest body = readJson(req, MoApplicationFeedbackUpdateRequest.class);
            if (body == null || body.getApplicationId() == null || body.getApplicationId().isBlank()) {
                writeError(resp, HttpServletResponse.SC_BAD_REQUEST, ErrorCodes.VALIDATION_ERROR, "applicationId is required.");
                return;
            }

            moApplicationService.updateDecisionFeedback(
                    getServletContext(),
                    moId,
                    body.getApplicationId(),
                    body.getDecisionFeedback()
            );
            writeSuccess(resp, Map.of("success", true));
        } catch (MoBusinessException ex) {
            writeError(resp, ex.getHttpStatus(), ex.getCode(), ex.getMessage());
        } catch (Exception ex) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", ex.getMessage());
        }
    }
}
