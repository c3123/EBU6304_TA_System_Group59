package com.ta.web.mo;

import com.google.gson.Gson;
import com.ta.dto.mo.ApiResponse;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Common JSON helpers for MO API servlets.
 */
public abstract class MoBaseServlet extends HttpServlet {
    protected static final Gson GSON = new Gson();

    /**
     * DEV-ONLY SWITCH (remove or set false before delivery).
     *
     * Why this exists:
     * - Enables quick API testing without full login/session flow.
     *
     * How to remove before release:
     * 1) Set this to false, OR
     * 2) Delete the whole "DEV-ONLY FALLBACK" block in getMoIdFromSession(...).
     */
    private static final boolean ENABLE_DEV_MO_ID_FALLBACK = true;

    protected <T> T readJson(HttpServletRequest req, Class<T> clazz) throws IOException {
        req.setCharacterEncoding(StandardCharsets.UTF_8.name());
        return GSON.fromJson(req.getReader(), clazz);
    }

    protected void writeSuccess(HttpServletResponse resp, Object data) throws IOException {
        writeApi(resp, HttpServletResponse.SC_OK, true, "OK", "success", data);
    }

    protected void writeError(HttpServletResponse resp, int status, String code, String message) throws IOException {
        writeApi(resp, status, false, code, message, null);
    }

    protected String getMoIdFromSession(HttpServletRequest req) {
        if (req.getSession(false) != null) {
            Object moId = req.getSession(false).getAttribute("moId");
            if (moId != null) {
                return String.valueOf(moId);
            }
            Object userId = req.getSession(false).getAttribute("userId");
            if (userId != null) {
                return String.valueOf(userId);
            }
        }

        // ===== DEV-ONLY FALLBACK START =====
        // For local testing without login/session:
        // 1) Header: X-MO-ID: mo001
        // 2) Query param: ?moId=mo001
        // Remove this block before production delivery.
        if (ENABLE_DEV_MO_ID_FALLBACK) {
            String headerMoId = req.getHeader("X-MO-ID");
            if (headerMoId != null && !headerMoId.isBlank()) {
                return headerMoId;
            }

            String queryMoId = req.getParameter("moId");
            if (queryMoId != null && !queryMoId.isBlank()) {
                return queryMoId;
            }
        }
        // ===== DEV-ONLY FALLBACK END =====

        return null;
    }

    protected String getLastPathSegment(HttpServletRequest req) {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.isBlank() || "/".equals(pathInfo)) {
            return null;
        }
        String normalized = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;
        int idx = normalized.lastIndexOf('/');
        return idx >= 0 ? normalized.substring(idx + 1) : normalized;
    }

    private void writeApi(HttpServletResponse resp,
                          int httpStatus,
                          boolean success,
                          String code,
                          String message,
                          Object data) throws IOException {
        ApiResponse<Object> api = new ApiResponse<>();
        api.setSuccess(success);
        api.setCode(code);
        api.setMessage(message);
        api.setData(data);

        resp.setStatus(httpStatus);
        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write(GSON.toJson(api));
    }
}
