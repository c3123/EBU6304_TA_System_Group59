package com.ta.web.mo;

import com.ta.constant.ErrorCodes;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Optional route contract for integration testing only:
 * POST /api/admin/demands/{jobId}/approve
 * POST /api/admin/demands/{jobId}/reject
 *
 * Temporary mapping in this scaffold:
 * POST /api/admin/demands/review/{jobId}?action=approve|reject
 */
@WebServlet(name = "AdminDemandReviewServlet", urlPatterns = {"/api/admin/demands/review/*"})
public class AdminDemandReviewServlet extends MoBaseServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String jobId = getLastPathSegment(req);
            String action = req.getParameter("action");

            if (jobId == null || jobId.isBlank() || action == null || action.isBlank()) {
                writeError(resp, HttpServletResponse.SC_BAD_REQUEST, ErrorCodes.VALIDATION_ERROR, "jobId and action are required.");
                return;
            }

            // TODO approve/reject demand in jobs.json
            writeSuccess(resp, null);
        } catch (Exception ex) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", ex.getMessage());
        }
    }
}
