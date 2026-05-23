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

        try {
            SystemSettings settings = JsonUtility.loadSystemSettings(context);
            settings.setWorkloadThresholdHours(request.getWorkloadThresholdHours());
            settings.setUpdatedAt(Instant.now().toString());
            JsonUtility.saveSystemSettings(context, settings);
            return toResponse(settings, true);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save system settings.", e);
        }
    }

    private AdminWorkloadSettingsResponse toResponse(SystemSettings settings, boolean saved) {
        AdminWorkloadSettingsResponse response = new AdminWorkloadSettingsResponse();
        response.setWorkloadThresholdHours(WorkloadLevelUtil.resolveThresholdHours(settings.getWorkloadThresholdHours()));
        response.setWorkloadNormalPercent(WorkloadLevelUtil.DEFAULT_NORMAL_PERCENT);
        response.setWorkloadWarningPercent(WorkloadLevelUtil.DEFAULT_WARNING_PERCENT);
        response.setUpdatedAt(settings.getUpdatedAt());
        response.setSaved(saved);
        return response;
    }
}
