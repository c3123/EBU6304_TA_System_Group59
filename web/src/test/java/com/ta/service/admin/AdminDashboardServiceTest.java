package com.ta.service.admin;

import com.ta.constant.ErrorCodes;
import com.ta.dto.admin.AdminDashboardJobItemResponse;
import com.ta.dto.admin.AdminDashboardResponse;
import com.ta.dto.admin.AdminDashboardWorkloadItemResponse;
import com.ta.testsupport.AdminServiceTestSupport;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminDashboardServiceTest extends AdminServiceTestSupport {

    private AdminDashboardService service;

    @BeforeEach
    void seed() throws Exception {
        service = new AdminDashboardService();
        writeUsers(List.of(
                user(ADMIN_ID, "Admin", "admin@demo.test", "admin"),
                user(MO_ID, "Teacher", "teacher@demo.test", "teacher"),
                user(STUDENT_USER_ID, "Student One", "student1@demo.test", "student")
        ));
        writeJobs(List.of(
                job("job_open", MO_ID, "open", "approved", true, false, false, 2, 10),
                job("job_closed", MO_ID, "closed", "approved", true, false, true, 1, 12),
                job("job_withdrawn", MO_ID, "open", "approved", true, true, false, 1, 4)
        ));
        writeApplications(List.of(
                app("app_hired_1", "job_open", STUDENT_USER_ID, "hired", true),
                app("app_pending_1", "job_open", STUDENT_2_ID, "pending", true),
                app("app_inactive", "job_open", STUDENT_2_ID, "pending", false)
        ));
        writeHiringHistory(List.of(hiringHistory("hist_1", "job_open", List.of("app_hired_1"))));
        writeSystemSettings(settings(10));
    }

    @Test
    void loadDashboard_aggregatesCountsWorkloadAndAlerts() {
        AdminDashboardResponse response = service.loadDashboard(servletContext, "all", "all");

        assertEquals(3, response.getTotalUsers());
        assertEquals(3, response.getTotalJobs());
        assertEquals(2, response.getTotalApplications());
        assertEquals(1, response.getTotalStudents());
        assertEquals(1, response.getTotalTeachers());
        assertEquals(1, response.getTotalAdmins());
        assertEquals(1, response.getTotalActiveJobs());
        assertEquals(1, response.getTotalClosedJobs());
        assertEquals(1, response.getTotalWithdrawnJobs());
        assertEquals(1, response.getTotalHiredRecords());
        assertEquals(1, response.getTotalOpenApplications());
        assertEquals(1, response.getTotalOverloadedStudents());

        AdminDashboardWorkloadItemResponse workload = response.getWorkload().get(0);
        assertEquals(STUDENT_USER_ID, workload.getStudentId());
        assertEquals(10, workload.getWeeklyHours());
        assertEquals("overload", workload.getWorkloadLevel());
        assertEquals("2026-05-15T12:00:00Z", workload.getAssignedJobs().get(0).getHiredAt());
        assertTrue(response.getAlerts().stream().anyMatch(a -> "workload".equals(a.getType())));
    }

    @Test
    void loadDashboard_filtersJobsByStatusAndDepartment() {
        AdminDashboardResponse response = service.loadDashboard(servletContext, "open", "Computer Science");

        assertEquals(1, response.getJobs().size());
        AdminDashboardJobItemResponse job = response.getJobs().get(0);
        assertEquals("job_open", job.getId());
        assertEquals(2, job.getApplicantCount());
        assertEquals(1, job.getHiredCount());
        assertEquals(1, job.getUnfilledCount());
    }

    @Test
    void invalidStatusFilter_throws400() {
        assertAdminBusinessException(
                () -> service.loadDashboard(servletContext, "paused", "all"),
                ErrorCodes.VALIDATION_ERROR,
                HttpServletResponse.SC_BAD_REQUEST
        );
    }

    @Test
    void unsupportedDepartment_throws400() {
        assertAdminBusinessException(
                () -> service.loadDashboard(servletContext, "all", "Math"),
                ErrorCodes.VALIDATION_ERROR,
                HttpServletResponse.SC_BAD_REQUEST
        );
    }
}
