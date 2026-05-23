package com.ta.service.admin;

import com.ta.constant.ErrorCodes;
import com.ta.testsupport.AdminServiceTestSupport;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkloadOverloadAnnouncementServiceTest extends AdminServiceTestSupport {

    private WorkloadOverloadAnnouncementService service;

    @BeforeEach
    void seed() throws Exception {
        service = new WorkloadOverloadAnnouncementService();
        writeUsers(List.of(user(STUDENT_USER_ID, "Student", "student@demo.test", "student")));
        writeJobs(List.of(
                job("job_1", MO_ID, "open", "approved", true, false, false, 1, 10),
                job("job_2", MO_ID, "open", "approved", true, false, false, 1, 8)
        ));
        writeApplications(List.of(
                app("app_1", "job_1", STUDENT_USER_ID, "hired", true),
                app("app_2", "job_2", STUDENT_USER_ID, "hired", true)
        ));
        writeNotifications(List.of());
        writeSystemSettings(settings(18));
    }

    @Test
    void calculateWeeklyHours_sumsHiredApplications() throws Exception {
        assertEquals(18, service.calculateWeeklyHours(servletContext, STUDENT_USER_ID));
    }

    @Test
    void notifyIfNewlyOverloaded_sendsOnlyWhenCrossingThreshold() throws Exception {
        service.notifyIfNewlyOverloaded(servletContext, STUDENT_USER_ID, 10);
        service.notifyIfNewlyOverloaded(servletContext, STUDENT_USER_ID, 18);

        assertEquals(1, readNotifications().size());
        assertTrue(readNotifications().get(0).getMessage().contains("18 hours per week"));
    }

    @Test
    void notifyAllCurrentlyOverloaded_sendsBulkReminder() throws Exception {
        var response = service.notifyAllCurrentlyOverloaded(servletContext);

        assertEquals(1, response.getNotifiedCount());
        assertEquals(18, response.getThresholdHours());
        assertEquals(List.of(STUDENT_USER_ID), response.getStudentIds());
    }

    @Test
    void notifyAllCurrentlyOverloaded_whenNone_throws400() throws Exception {
        writeSystemSettings(settings(30));

        assertAdminBusinessException(
                () -> {
                    try {
                        service.notifyAllCurrentlyOverloaded(servletContext);
                    } catch (AdminBusinessException ex) {
                        throw ex;
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                },
                ErrorCodes.VALIDATION_ERROR,
                HttpServletResponse.SC_BAD_REQUEST
        );
    }
}
