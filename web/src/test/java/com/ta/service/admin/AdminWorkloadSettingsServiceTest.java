package com.ta.service.admin;

import com.ta.constant.ErrorCodes;
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
        var response = service.saveThreshold(servletContext, 16);

        assertEquals(16, response.getWorkloadThresholdHours());
        assertTrue(response.isSaved());
        assertEquals(16, service.getSettings(servletContext).getWorkloadThresholdHours());
    }

    @Test
    void saveThreshold_nonPositive_throws400() {
        assertAdminBusinessException(
                () -> service.saveThreshold(servletContext, 0),
                ErrorCodes.VALIDATION_ERROR,
                HttpServletResponse.SC_BAD_REQUEST
        );
    }
}
