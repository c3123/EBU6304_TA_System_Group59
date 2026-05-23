package com.ta.service.admin;

import com.ta.constant.ErrorCodes;
import com.ta.testsupport.AdminServiceTestSupport;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminReportServiceTest extends AdminServiceTestSupport {

    private AdminReportService service;

    @BeforeEach
    void seed() throws Exception {
        service = new AdminReportService();
        writeUsers(List.of(
                user(ADMIN_ID, "Admin", "admin@demo.test", "admin"),
                user(STUDENT_USER_ID, "Student", "student@demo.test", "student")
        ));
        writeStudents(List.of(profile(STUDENT_USER_ID, "Student")));
        writeJobs(List.of(
                job("job_open", MO_ID, "open", "approved", true, false, false, 2, 10),
                job("job_closed", MO_ID, "closed", "approved", true, false, true, 1, 8)
        ));
        var hired = app("app_hired", "job_open", STUDENT_USER_ID, "hired", true);
        hired.setEvaluationNotes("Strong profile");
        hired.setDecisionFeedback("Accepted");
        writeApplications(List.of(hired));
        writeHiringHistory(List.of());
        writeNotifications(List.of(notification("noti_1", STUDENT_USER_ID, "student", false)));
        writeSystemSettings(settings(20));
    }

    @Test
    void weeklyCsvReport_respectsStatusFilter() {
        String csv = service.buildWeeklyRecruitmentReport(servletContext, "csv", "open", "all");

        assertTrue(csv.startsWith("jobId,moduleCode,title"));
        assertTrue(csv.contains("\"job_open\""));
        assertTrue(csv.contains("\"1\""));
        assertTrue(!csv.contains("job_closed"));
    }

    @Test
    void workloadReport_includesAssignedJobs() {
        String report = service.buildWorkloadReport(servletContext, "txt");

        assertTrue(report.contains("TA Workload Report"));
        assertTrue(report.contains("Student"));
        assertTrue(report.contains("Job job_open"));
    }

    @Test
    void applicationArchiveReport_includesMoPrivateFields() {
        String csv = service.buildApplicationArchiveReport(servletContext, "csv");

        assertTrue(csv.contains("evaluationNotes,decisionFeedback"));
        assertTrue(csv.contains("Strong profile"));
        assertTrue(csv.contains("Accepted"));
    }

    @Test
    void backupJson_containsAllDataFamilies() {
        String json = service.buildBackupJson(servletContext);

        assertTrue(json.contains("\"users\""));
        assertTrue(json.contains("\"students\""));
        assertTrue(json.contains("\"jobs\""));
        assertTrue(json.contains("\"applications\""));
        assertTrue(json.contains("\"systemSettings\""));
    }

    @Test
    void invalidReportFormat_throws400() {
        assertAdminBusinessException(
                () -> service.buildWeeklyRecruitmentReport(servletContext, "xlsx"),
                ErrorCodes.VALIDATION_ERROR,
                HttpServletResponse.SC_BAD_REQUEST
        );
    }

    @Test
    void resolveFileNameAndContentType_useFilterScope() {
        assertEquals("text/csv;charset=UTF-8", service.resolveContentType("csv"));
        assertTrue(service.resolveFileName("txt", "open", "Computer Science")
                .startsWith("weekly-report-status-open-dept-Computer-Science-"));
    }
}
