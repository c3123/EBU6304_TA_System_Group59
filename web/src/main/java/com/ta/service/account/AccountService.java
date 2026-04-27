package com.ta.service.account;

import com.ta.constant.ErrorCodes;
import com.ta.dto.account.ChangePasswordResponse;
import com.ta.model.User;
import com.ta.util.JsonUtility;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

public class AccountService {

    public ChangePasswordResponse changePassword(ServletContext context,
                                                 String userId,
                                                 String oldPassword,
                                                 String newPassword,
                                                 String confirmPassword) {
        String oldValue = trimToEmpty(oldPassword);
        String newValue = trimToEmpty(newPassword);
        String confirmValue = trimToEmpty(confirmPassword);

        if (userId == null || userId.isBlank()) {
            throw new AccountBusinessException(
                    ErrorCodes.UNAUTHORIZED,
                    "Login required.",
                    HttpServletResponse.SC_UNAUTHORIZED
            );
        }
        if (oldValue.isBlank() || newValue.isBlank() || confirmValue.isBlank()) {
            throw new AccountBusinessException(
                    ErrorCodes.VALIDATION_ERROR,
                    "oldPassword, newPassword, and confirmPassword are required.",
                    HttpServletResponse.SC_BAD_REQUEST
            );
        }
        if (!newValue.equals(confirmValue)) {
            throw new AccountBusinessException(
                    ErrorCodes.VALIDATION_ERROR,
                    "newPassword and confirmPassword must match.",
                    HttpServletResponse.SC_BAD_REQUEST
            );
        }
        if (oldValue.equals(newValue)) {
            throw new AccountBusinessException(
                    ErrorCodes.VALIDATION_ERROR,
                    "newPassword must differ from oldPassword.",
                    HttpServletResponse.SC_BAD_REQUEST
            );
        }

        try {
            List<User> users = JsonUtility.loadUsers(context);
            User user = users.stream()
                    .filter(item -> userId.equals(item.getId()))
                    .findFirst()
                    .orElseThrow(() -> new AccountBusinessException(
                            ErrorCodes.UNAUTHORIZED,
                            "Login required.",
                            HttpServletResponse.SC_UNAUTHORIZED
                    ));

            if (!oldValue.equals(user.getPassword())) {
                throw new AccountBusinessException(
                        ErrorCodes.VALIDATION_ERROR,
                        "oldPassword is incorrect.",
                        HttpServletResponse.SC_BAD_REQUEST
                );
            }

            user.setPassword(newValue);
            JsonUtility.saveUsers(context, users);

            ChangePasswordResponse response = new ChangePasswordResponse();
            response.setChanged(true);
            return response;
        } catch (IOException e) {
            throw new RuntimeException("Failed to change password.", e);
        }
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
