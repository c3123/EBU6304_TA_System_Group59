package com.ta.web;

import com.google.gson.Gson;
import com.ta.constant.ErrorCodes;
import com.ta.dto.mo.ApiResponse;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public abstract class AdminBaseServlet extends HttpServlet {
    protected static final Gson GSON = new Gson();

    protected boolean requireAdmin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Object role = req.getSession(false) == null ? null : req.getSession(false).getAttribute("userRole");
        if (role == null || !"admin".equalsIgnoreCase(String.valueOf(role))) {
            writeError(resp, HttpServletResponse.SC_FORBIDDEN, ErrorCodes.FORBIDDEN_ADMIN_ONLY, "Admin only.");
            return false;
        }
        return true;
    }

    protected String getCurrentAdminUserId(HttpServletRequest req) {
        Object userId = req.getSession(false) == null ? null : req.getSession(false).getAttribute("userId");
        return userId == null ? null : String.valueOf(userId);
    }

    protected <T> T readJson(HttpServletRequest req, Class<T> clazz) throws IOException {
        req.setCharacterEncoding(StandardCharsets.UTF_8.name());
        return GSON.fromJson(req.getReader(), clazz);
    }

    protected void writeSuccess(HttpServletResponse resp, Object data) throws IOException {
        writeApi(resp, HttpServletResponse.SC_OK, true, ErrorCodes.OK, "success", data);
    }

    protected void writeError(HttpServletResponse resp, int status, String code, String message) throws IOException {
        writeApi(resp, status, false, code, message, null);
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
