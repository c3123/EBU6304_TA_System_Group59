package com.ta.web;

import com.google.gson.Gson;
import com.ta.constant.ErrorCodes;
import com.ta.dto.account.ChangePasswordRequest;
import com.ta.dto.mo.ApiResponse;
import com.ta.service.account.AccountBusinessException;
import com.ta.service.account.AccountService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@WebServlet(name = "AccountChangePasswordServlet", urlPatterns = {"/api/account/change-password"})
public class AccountChangePasswordServlet extends HttpServlet {
    private static final Gson GSON = new Gson();
    private final AccountService accountService = new AccountService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String userId = req.getSession(false) == null ? null : String.valueOf(req.getSession(false).getAttribute("userId"));
            if (userId == null || userId.isBlank()) {
                writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, ErrorCodes.UNAUTHORIZED, "Login required.");
                return;
            }

            req.setCharacterEncoding(StandardCharsets.UTF_8.name());
            ChangePasswordRequest request = GSON.fromJson(req.getReader(), ChangePasswordRequest.class);
            writeSuccess(resp, accountService.changePassword(
                    getServletContext(),
                    userId,
                    request == null ? null : request.getOldPassword(),
                    request == null ? null : request.getNewPassword(),
                    request == null ? null : request.getConfirmPassword()
            ));
        } catch (AccountBusinessException ex) {
            writeError(resp, ex.getHttpStatus(), ex.getCode(), ex.getMessage());
        } catch (Exception ex) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", ex.getMessage());
        }
    }

    private void writeSuccess(HttpServletResponse resp, Object data) throws IOException {
        writeApi(resp, HttpServletResponse.SC_OK, true, ErrorCodes.OK, "success", data);
    }

    private void writeError(HttpServletResponse resp, int status, String code, String message) throws IOException {
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
