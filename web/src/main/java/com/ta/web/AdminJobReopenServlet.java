package com.ta.web;

import com.ta.constant.ErrorCodes;
import com.ta.service.mo.MoBusinessException;
import com.ta.service.mo.MoHiringService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

@WebServlet(name = "AdminJobReopenServlet", urlPatterns = {"/api/admin/jobs/reopen/*"})
public class AdminJobReopenServlet extends HttpServlet {
    private final MoHiringService moHiringService = new MoHiringService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            Object role = req.getSession(false) == null ? null : req.getSession(false).getAttribute("userRole");
            if (role == null || !"admin".equalsIgnoreCase(String.valueOf(role))) {
                writeApi(resp, HttpServletResponse.SC_FORBIDDEN, false, ErrorCodes.FORBIDDEN_ADMIN_ONLY, "Admin only.", null);
                return;
            }
            String pathInfo = req.getPathInfo();
            String jobId = (pathInfo == null || pathInfo.isBlank() || "/".equals(pathInfo))
                    ? null
                    : pathInfo.substring(pathInfo.startsWith("/") ? 1 : 0);
            if (jobId == null || jobId.isBlank()) {
                writeApi(resp, HttpServletResponse.SC_BAD_REQUEST, false, ErrorCodes.VALIDATION_ERROR, "jobId is required.", null);
                return;
            }
            moHiringService.reopenByAdmin(getServletContext(), jobId);
            writeApi(resp, HttpServletResponse.SC_OK, true, "OK", "success", Map.of("jobId", jobId, "reopened", true));
        } catch (MoBusinessException ex) {
            writeApi(resp, ex.getHttpStatus(), false, ex.getCode(), ex.getMessage(), null);
        } catch (Exception ex) {
            writeApi(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, false, "INTERNAL_ERROR", ex.getMessage(), null);
        }
    }

    private void writeApi(HttpServletResponse resp,
                          int httpStatus,
                          boolean success,
                          String code,
                          String message,
                          Object data) throws IOException {
        com.ta.dto.mo.ApiResponse<Object> api = new com.ta.dto.mo.ApiResponse<>();
        api.setSuccess(success);
        api.setCode(code);
        api.setMessage(message);
        api.setData(data);
        resp.setStatus(httpStatus);
        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write(new com.google.gson.Gson().toJson(api));
    }
}
