package com.ta.web.mo;

import com.ta.constant.ErrorCodes;
import com.ta.service.mo.MoBusinessException;
import com.ta.service.mo.MoHiringService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "MoHiringHistoryServlet", urlPatterns = {"/api/mo/hiring/history"})
public class MoHiringHistoryServlet extends MoBaseServlet {
    private final MoHiringService moHiringService = new MoHiringService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String moId = getMoIdFromSession(req);
            if (moId == null || moId.isBlank()) {
                writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, ErrorCodes.UNAUTHORIZED, "MO login required.");
                return;
            }
            String jobId = req.getParameter("jobId");
            Object data = moHiringService.getHistory(getServletContext(), moId, jobId);
            writeSuccess(resp, data);
        } catch (MoBusinessException ex) {
            writeError(resp, ex.getHttpStatus(), ex.getCode(), ex.getMessage());
        } catch (Exception ex) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", ex.getMessage());
        }
    }
}
