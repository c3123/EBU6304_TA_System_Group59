package com.ta.web.student;

import com.google.gson.Gson;
import com.ta.dto.mo.ApiResponse;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public abstract class StudentBaseServlet extends HttpServlet {
    protected static final Gson GSON = new Gson();

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

    protected String getStudentUserId(HttpServletRequest req) {
        if (req.getSession(false) != null) {
            Object role = req.getSession(false).getAttribute("userRole");
            if (role != null && "student".equalsIgnoreCase(String.valueOf(role))) {
                Object userId = req.getSession(false).getAttribute("userId");
                if (userId != null) {
                    return String.valueOf(userId);
                }
            }
        }

        return null;
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
