package com.ta.service.admin;

import com.ta.constant.ErrorCodes;
import com.ta.dto.admin.AdminWorkloadSettingsRequest;
import com.ta.testsupport.AdminServiceTestSupport;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminWorkloadSettingsServiceTest extends AdminServiceTestSupport {

    private AdminWorkloadSettingsService service;

    @BeforeEach
    void seed() throws Exception {
        service = new AdminWorkloadSettingsService();
        writeSystemSettings(settings(20));
    }

    @Test
    void getSettings_returnsPersistedThreshold() {
        assertEquals(20, service.getSettings(servletContext).getWorkloadThresholdHours());
    }

    @Test
    void saveThreshold_persistsPositiveValue() {
        var request = new AdminWorkloadSettingsRequest();
        request.setWorkloadThresholdHours(16);

        var response = service.saveSettings(servletContext, request);

        assertEquals(16, response.getWorkloadThresholdHours());
        assertTrue(response.isSaved());
        assertEquals(16, service.getSettings(servletContext).getWorkloadThresholdHours());
    }

    @Test
    void saveThreshold_nonPositive_throws400() {
        var request = new AdminWorkloadSettingsRequest();
        request.setWorkloadThresholdHours(0);

        assertAdminBusinessException(
                () -> service.saveSettings(servletContext, request),
                ErrorCodes.VALIDATION_ERROR,
                HttpServletResponse.SC_BAD_REQUEST
        );
    }
}
