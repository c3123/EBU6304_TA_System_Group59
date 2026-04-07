package com.ta.web.mo;

import com.ta.constant.ErrorCodes;
import com.ta.dto.mo.MoHiringFinalizeRequest;
import com.ta.service.mo.MoBusinessException;
import com.ta.service.mo.MoHiringService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "MoHiringFinalizeServlet", urlPatterns = {"/api/mo/hiring/finalize"})
public class MoHiringFinalizeServlet extends MoBaseServlet {
    private final MoHiringService moHiringService = new MoHiringService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String moId = getMoIdFromSession(req);
            if (moId == null || moId.isBlank()) {
                writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, ErrorCodes.UNAUTHORIZED, "MO login required.");
                return;
            }
            MoHiringFinalizeRequest body = readJson(req, MoHiringFinalizeRequest.class);
            if (body == null) {
                writeError(resp, HttpServletResponse.SC_BAD_REQUEST, ErrorCodes.VALIDATION_ERROR, "Request body is required.");
                return;
            }
            Object data = moHiringService.finalizeHiring(getServletContext(), moId, body.getJobId(), body.getHiredApplicationIds());
            writeSuccess(resp, data);
        } catch (MoBusinessException ex) {
            writeError(resp, ex.getHttpStatus(), ex.getCode(), ex.getMessage());
        } catch (Exception ex) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", ex.getMessage());
        }
    }
}
