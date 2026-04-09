package com.ta.service.admin;

import com.ta.constant.ErrorCodes;
import com.ta.dto.admin.AdminPasswordResetResponse;
import com.ta.dto.admin.AdminUserCreateRequest;
import com.ta.dto.admin.AdminUserDeleteResponse;
import com.ta.dto.admin.AdminUserItemResponse;
import com.ta.model.StudentProfile;
import com.ta.model.User;
import com.ta.util.JsonUtility;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

public class AdminUserService {

    public AdminUserItemResponse createUser(ServletContext context, AdminUserCreateRequest request) {
        String role = trimToEmpty(request == null ? null : request.getRole()).toLowerCase();
        String name = trimToEmpty(request == null ? null : request.getName());
        String email = trimToEmpty(request == null ? null : request.getEmail());
        String password = trimToEmpty(request == null ? null : request.getPassword());
        String studentId = trimToEmpty(request == null ? null : request.getStudentId());
        String programme = trimToEmpty(request == null ? null : request.getProgramme());

        validateCreateRequest(role, name, email, password, studentId, programme);

        try {
            List<User> users = JsonUtility.loadUsers(context);
            boolean emailExists = users.stream().anyMatch(user -> email.equalsIgnoreCase(user.getEmail()));
            if (emailExists) {
                throw new AdminBusinessException(
                        ErrorCodes.VALIDATION_ERROR,
                        "Email already exists.",
                        HttpServletResponse.SC_BAD_REQUEST
                );
            }

            if ("student".equals(role)) {
                boolean studentIdExists = users.stream()
                        .filter(User::isStudent)
                        .anyMatch(user -> studentId.equalsIgnoreCase(trimToEmpty(user.getStudentId())));
                if (studentIdExists) {
                    throw new AdminBusinessException(
                            ErrorCodes.VALIDATION_ERROR,
                            "Student ID already exists.",
                            HttpServletResponse.SC_BAD_REQUEST
                    );
                }
            }

            String userId = nextUserId(users, role);
            User user = new User();
            user.setId(userId);
            user.setName(name);
            user.setEmail(email);
            user.setPassword(password);
            user.setRole(role);
            user.setStudentId("student".equals(role) ? studentId : "");
            user.setProgramme("student".equals(role) ? programme : "");
            users.add(user);
            JsonUtility.saveUsers(context, users);

            if ("student".equals(role)) {
                List<StudentProfile> profiles = JsonUtility.loadStudents(context);
                StudentProfile profile = new StudentProfile();
                profile.setUserId(userId);
                profile.setStudentId(studentId);
                profile.setName(name);
                profile.setEmail(email);
                profile.setProgramme(programme);
                profile.setSkills("");
                profile.setExperience("");
                profiles.add(profile);
                JsonUtility.saveStudents(context, profiles);
            }

            return toUserItem(user);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create user.", e);
        }
    }

    public AdminUserDeleteResponse deleteUser(ServletContext context, String currentAdminUserId, String targetUserId) {
        String normalizedTargetUserId = trimToEmpty(targetUserId);
        if (normalizedTargetUserId.isBlank()) {
            throw new AdminBusinessException(
                    ErrorCodes.VALIDATION_ERROR,
                    "userId is required.",
                    HttpServletResponse.SC_BAD_REQUEST
            );
        }
        if (normalizedTargetUserId.equalsIgnoreCase(trimToEmpty(currentAdminUserId))) {
            throw new AdminBusinessException(
                    ErrorCodes.VALIDATION_ERROR,
                    "You cannot delete your own account.",
                    HttpServletResponse.SC_BAD_REQUEST
            );
        }

        try {
            List<User> users = JsonUtility.loadUsers(context);
            User target = users.stream()
                    .filter(user -> normalizedTargetUserId.equalsIgnoreCase(user.getId()))
                    .findFirst()
                    .orElseThrow(() -> new AdminBusinessException(
                            ErrorCodes.VALIDATION_ERROR,
                            "User not found.",
                            HttpServletResponse.SC_NOT_FOUND
                    ));

            if ("admin".equalsIgnoreCase(target.getRole())) {
                long adminCount = users.stream().filter(user -> "admin".equalsIgnoreCase(user.getRole())).count();
                if (adminCount <= 1) {
                    throw new AdminBusinessException(
                            ErrorCodes.VALIDATION_ERROR,
                            "Cannot delete the last admin account.",
                            HttpServletResponse.SC_BAD_REQUEST
                    );
                }
            }

            users.removeIf(user -> normalizedTargetUserId.equalsIgnoreCase(user.getId()));
            JsonUtility.saveUsers(context, users);

            if (target.isStudent()) {
                List<StudentProfile> profiles = JsonUtility.loadStudents(context);
                profiles.removeIf(profile -> normalizedTargetUserId.equalsIgnoreCase(profile.getUserId()));
                JsonUtility.saveStudents(context, profiles);
            }

            AdminUserDeleteResponse response = new AdminUserDeleteResponse();
            response.setUserId(normalizedTargetUserId);
            response.setDeleted(true);
            return response;
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete user.", e);
        }
    }

    public AdminPasswordResetResponse resetPassword(ServletContext context, String targetUserId, String newPassword) {
        String normalizedTargetUserId = trimToEmpty(targetUserId);
        String normalizedPassword = trimToEmpty(newPassword);
        if (normalizedTargetUserId.isBlank() || normalizedPassword.isBlank()) {
            throw new AdminBusinessException(
                    ErrorCodes.VALIDATION_ERROR,
                    "userId and newPassword are required.",
                    HttpServletResponse.SC_BAD_REQUEST
            );
        }

        try {
            List<User> users = JsonUtility.loadUsers(context);
            User target = users.stream()
                    .filter(user -> normalizedTargetUserId.equalsIgnoreCase(user.getId()))
                    .findFirst()
                    .orElseThrow(() -> new AdminBusinessException(
                            ErrorCodes.VALIDATION_ERROR,
                            "User not found.",
                            HttpServletResponse.SC_NOT_FOUND
                    ));

            target.setPassword(normalizedPassword);
            JsonUtility.saveUsers(context, users);

            AdminPasswordResetResponse response = new AdminPasswordResetResponse();
            response.setUserId(target.getId());
            response.setEmail(target.getEmail());
            response.setReset(true);
            return response;
        } catch (IOException e) {
            throw new RuntimeException("Failed to reset password.", e);
        }
    }

    private void validateCreateRequest(String role,
                                       String name,
                                       String email,
                                       String password,
                                       String studentId,
                                       String programme) {
        if (!"student".equals(role) && !"teacher".equals(role) && !"admin".equals(role)) {
            throw new AdminBusinessException(
                    ErrorCodes.VALIDATION_ERROR,
                    "role must be student, teacher, or admin.",
                    HttpServletResponse.SC_BAD_REQUEST
            );
        }
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            throw new AdminBusinessException(
                    ErrorCodes.VALIDATION_ERROR,
                    "name, email, and password are required.",
                    HttpServletResponse.SC_BAD_REQUEST
            );
        }
        if ("student".equals(role) && (studentId.isBlank() || programme.isBlank())) {
            throw new AdminBusinessException(
                    ErrorCodes.VALIDATION_ERROR,
                    "studentId and programme are required for student accounts.",
                    HttpServletResponse.SC_BAD_REQUEST
            );
        }
    }

    private String nextUserId(List<User> users, String role) {
        String prefix;
        switch (role) {
            case "student":
                prefix = "stu";
                break;
            case "teacher":
                prefix = "mo";
                break;
            case "admin":
                prefix = "adm";
                break;
            default:
                throw new IllegalArgumentException("Unexpected role: " + role);
        }

        int max = 0;
        for (User user : users) {
            String id = user.getId();
            if (id == null || !id.toLowerCase().startsWith(prefix)) {
                continue;
            }
            String suffix = id.substring(prefix.length());
            try {
                max = Math.max(max, Integer.parseInt(suffix));
            } catch (NumberFormatException ignored) {
                // Ignore malformed IDs.
            }
        }
        return prefix + String.format("%03d", max + 1);
    }

    private AdminUserItemResponse toUserItem(User user) {
        AdminUserItemResponse response = new AdminUserItemResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setStudentId(user.getStudentId());
        response.setProgramme(user.getProgramme());
        return response;
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
