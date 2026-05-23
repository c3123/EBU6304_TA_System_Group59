package com.ta.service.admin;

import com.ta.constant.ErrorCodes;
import com.ta.dto.admin.AdminWorkloadSettingsRequest;
import com.ta.dto.admin.AdminWorkloadSettingsResponse;
import com.ta.model.SystemSettings;
import com.ta.util.JsonUtility;
import com.ta.util.WorkloadLevelUtil;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.Instant;

public class AdminWorkloadSettingsService {

    public AdminWorkloadSettingsResponse getSettings(ServletContext context) {
        try {
            return toResponse(JsonUtility.loadSystemSettings(context), false);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load system settings.", e);
        }
    }

    public AdminWorkloadSettingsResponse saveSettings(ServletContext context, AdminWorkloadSettingsRequest request) {
        if (request == null || request.getWorkloadThresholdHours() == null || request.getWorkloadThresholdHours() <= 0) {
            throw new AdminBusinessException(
                    ErrorCodes.VALIDATION_ERROR,
                    "workloadThresholdHours must be a positive integer.",
                    HttpServletResponse.SC_BAD_REQUEST
            );
        }

        int normalPercent = request.getWorkloadNormalPercent() == null
                ? WorkloadLevelUtil.DEFAULT_NORMAL_PERCENT
                : request.getWorkloadNormalPercent();
        int warningPercent = request.getWorkloadWarningPercent() == null
                ? WorkloadLevelUtil.DEFAULT_WARNING_PERCENT
                : request.getWorkloadWarningPercent();

        if (normalPercent < 1 || normalPercent > 98) {
            throw new AdminBusinessException(
                    ErrorCodes.VALIDATION_ERROR,
                    "workloadNormalPercent must be between 1 and 98.",
                    HttpServletResponse.SC_BAD_REQUEST
            );
        }
        if (warningPercent < 2 || warningPercent > 99) {
            throw new AdminBusinessException(
                    ErrorCodes.VALIDATION_ERROR,
                    "workloadWarningPercent must be between 2 and 99.",
                    HttpServletResponse.SC_BAD_REQUEST
            );
        }
        if (warningPercent <= normalPercent) {
            throw new AdminBusinessException(
                    ErrorCodes.VALIDATION_ERROR,
                    "workloadWarningPercent must be greater than workloadNormalPercent.",
                    HttpServletResponse.SC_BAD_REQUEST
            );
        }

        try {
            SystemSettings settings = JsonUtility.loadSystemSettings(context);
            settings.setWorkloadThresholdHours(request.getWorkloadThresholdHours());
            settings.setWorkloadNormalPercent(normalPercent);
            settings.setWorkloadWarningPercent(warningPercent);
            settings.setUpdatedAt(Instant.now().toString());
            JsonUtility.saveSystemSettings(context, settings);
            return toResponse(settings, true);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save system settings.", e);
        }
    }

    /** @deprecated use {@link #saveSettings(ServletContext, AdminWorkloadSettingsRequest)} */
    public AdminWorkloadSettingsResponse saveThreshold(ServletContext context, Integer thresholdHours) {
        AdminWorkloadSettingsRequest request = new AdminWorkloadSettingsRequest();
        request.setWorkloadThresholdHours(thresholdHours);
        return saveSettings(context, request);
    }

    private AdminWorkloadSettingsResponse toResponse(SystemSettings settings, boolean saved) {
        int threshold = WorkloadLevelUtil.resolveThresholdHours(settings.getWorkloadThresholdHours());
        int normalPercent = WorkloadLevelUtil.resolveNormalPercent(settings.getWorkloadNormalPercent());
        int warningPercent = WorkloadLevelUtil.resolveWarningPercent(normalPercent, settings.getWorkloadWarningPercent());

        AdminWorkloadSettingsResponse response = new AdminWorkloadSettingsResponse();
        response.setWorkloadThresholdHours(threshold);
        response.setWorkloadNormalPercent(normalPercent);
        response.setWorkloadWarningPercent(warningPercent);
        response.setUpdatedAt(settings.getUpdatedAt());
        response.setSaved(saved);
        return response;
    }
}
