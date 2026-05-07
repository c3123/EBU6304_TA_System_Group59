package com.ta.web.mo;

import com.ta.constant.ErrorCodes;
import com.ta.dto.mo.MoDemandItemResponse;
import com.ta.service.mo.MoBusinessException;
import com.ta.service.mo.MoJobService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "MoJobReuseServlet", urlPatterns = {"/api/mo/jobs/reuse"})
public class MoJobReuseServlet extends MoBaseServlet {
    private final MoJobService moJobService = new MoJobService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String moId = getMoIdFromSession(req);
            if (moId == null || moId.isBlank()) {
                writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, ErrorCodes.UNAUTHORIZED, "MO login required.");
                return;
            }

            String jobId = req.getParameter("jobId");
            if (jobId == null || jobId.isBlank()) {
                writeError(resp, HttpServletResponse.SC_BAD_REQUEST, ErrorCodes.VALIDATION_ERROR, "jobId is required.");
                return;
            }

            MoDemandItemResponse data = moJobService.reuseJob(getServletContext(), moId, jobId);
            writeSuccess(resp, data);
        } catch (MoBusinessException ex) {
            writeError(resp, ex.getHttpStatus(), ex.getCode(), ex.getMessage());
        } catch (Exception ex) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", ex.getMessage());
        }
    }
}
