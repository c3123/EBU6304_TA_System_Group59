package com.ta.web.mo;

import com.ta.constant.ErrorCodes;
import com.ta.dto.admin.AdminDemandReviewRequest;
import com.ta.dto.admin.AdminDemandReviewResponse;
import com.ta.service.admin.AdminBusinessException;
import com.ta.service.admin.AdminDemandReviewService;
import com.ta.web.AdminBaseServlet;
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
public class AdminDemandReviewServlet extends AdminBaseServlet {
    private final AdminDemandReviewService adminDemandReviewService = new AdminDemandReviewService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            if (!requireAdmin(req, resp)) {
                return;
            }
            String jobId = getLastPathSegment(req);
            String action = req.getParameter("action");

            if (jobId == null || jobId.isBlank() || action == null || action.isBlank()) {
                writeError(resp, HttpServletResponse.SC_BAD_REQUEST, ErrorCodes.VALIDATION_ERROR, "jobId and action are required.");
                return;
            }

            AdminDemandReviewRequest request = readReviewRequest(req);
            AdminDemandReviewResponse data = adminDemandReviewService.reviewDemand(
                    getServletContext(),
                    jobId,
                    action,
                    request == null ? null : request.getReason()
            );
            writeSuccess(resp, data);
        } catch (AdminBusinessException ex) {
            writeError(resp, ex.getHttpStatus(), ex.getCode(), ex.getMessage());
        } catch (Exception ex) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", ex.getMessage());
        }
    }

    private AdminDemandReviewRequest readReviewRequest(HttpServletRequest req) throws IOException {
        if (req.getContentLength() == 0) {
            return null;
        }
        return readJson(req, AdminDemandReviewRequest.class);
    }

    private String getLastPathSegment(HttpServletRequest req) {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.isBlank() || "/".equals(pathInfo)) {
            return null;
        }
        String normalized = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;
        int idx = normalized.lastIndexOf('/');
        return idx >= 0 ? normalized.substring(idx + 1) : normalized;
    }
}
