package com.ta.service.admin;

import com.ta.constant.ErrorCodes;
import com.ta.dto.admin.AdminDemandListResponse;
import com.ta.dto.admin.AdminDemandReviewResponse;
import com.ta.model.JobPosting;
import com.ta.testsupport.AdminServiceTestSupport;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminDemandReviewServiceTest extends AdminServiceTestSupport {

    private AdminDemandReviewService service;

    @BeforeEach
    void seed() throws Exception {
        service = new AdminDemandReviewService();
        writeUsers(List.of(
                user(ADMIN_ID, "Admin", "admin@demo.test", "admin"),
                user(MO_ID, "Teacher One", "teacher@demo.test", "teacher")
        ));
        JobPosting plainJob = job("plain_job", MO_ID, "open", null, true, false, false, 1, 4);
        plainJob.setPublished(null);
        plainJob.setWithdrawn(null);
        writeJobs(List.of(
                demand("demand_pending", MO_ID, "pending"),
                demand("demand_approved", MO_ID, "approved"),
                plainJob
        ));
        writeNotifications(List.of());
    }

    @Test
    void listDemands_defaultsToPendingOnly() {
        AdminDemandListResponse response = service.listDemands(servletContext, null);

        assertEquals(1, response.getItems().size());
        assertEquals("demand_pending", response.getItems().get(0).getJobId());
    }

    @Test
    void approveDemand_updatesJobAndNotifiesTeacher() throws Exception {
        AdminDemandReviewResponse response = service.reviewDemand(servletContext, "demand_pending", "approve");

        assertEquals("approve", response.getAction());
        assertEquals("approved", response.getApprovalStatus());
        JobPosting job = readJobs().stream().filter(j -> "demand_pending".equals(j.getId())).findFirst().orElseThrow();
        assertEquals("approved", job.getApprovalStatus());
        assertFalse(Boolean.TRUE.equals(job.getPublished()));
        assertTrue(readNotifications().stream().anyMatch(n ->
                MO_ID.equals(n.getRecipientId()) && n.getMessage().contains("New status: Approved")));
    }

    @Test
    void rejectDemand_trimsAndPersistsReason() throws Exception {
        AdminDemandReviewResponse response = service.reviewDemand(
                servletContext,
                "demand_pending",
                "reject",
                "  Missing workload detail  "
        );

        assertEquals("rejected", response.getApprovalStatus());
        assertEquals("Missing workload detail", response.getRejectionReason());
        assertEquals("Missing workload detail", readJobs().get(0).getRejectionReason());
    }

    @Test
    void invalidStatusFilter_throws400() {
        assertAdminBusinessException(
                () -> service.listDemands(servletContext, "archived"),
                ErrorCodes.VALIDATION_ERROR,
                HttpServletResponse.SC_BAD_REQUEST
        );
    }

    @Test
    void nonDemandRecordCannotBeReviewed() {
        assertAdminBusinessException(
                () -> service.reviewDemand(servletContext, "plain_job", "approve"),
                ErrorCodes.VALIDATION_ERROR,
                HttpServletResponse.SC_BAD_REQUEST
        );
    }
}
