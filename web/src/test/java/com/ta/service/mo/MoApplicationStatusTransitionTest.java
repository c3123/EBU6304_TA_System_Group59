package com.ta.service.mo;

import com.ta.constant.ErrorCodes;
import com.ta.model.ApplicationRecord;
import com.ta.model.JobPosting;
import com.ta.testsupport.MoTestSupport;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MoApplicationStatusTransitionTest extends MoTestSupport {

    private static ApplicationRecord recordWithStatus(String status) {
        ApplicationRecord r = application("app_1", JOB_ID, status, true);
        r.setDecisionFeedback("some feedback");
        return r;
    }

    private static JobPosting openJob() {
        return ownedJob(JOB_ID, false);
    }

    private static JobPosting closedJob() {
        return ownedJob(JOB_ID, true);
    }

    @Test
    void shortlistedToHired_succeeds() {
        ApplicationRecord record = recordWithStatus("shortlisted");
        MoApplicationService.applyMoApplicationStatusTransition(record, openJob(), "hired");
        assertEquals("hired", record.getStatus());
    }

    @Test
    void rejectedToViewed_clearsFeedback() {
        ApplicationRecord record = recordWithStatus("rejected");
        MoApplicationService.applyMoApplicationStatusTransition(record, openJob(), "viewed");
        assertEquals("viewed", record.getStatus());
        assertEquals("", record.getDecisionFeedback());
    }

    @Test
    void viewedToPending_clearsFeedback() {
        ApplicationRecord record = recordWithStatus("viewed");
        MoApplicationService.applyMoApplicationStatusTransition(record, openJob(), "pending");
        assertEquals("pending", record.getStatus());
        assertEquals("", record.getDecisionFeedback());
    }

    @Test
    void sameStatus_isNoOp() {
        ApplicationRecord record = recordWithStatus("shortlisted");
        record.setDecisionFeedback("keep");
        MoApplicationService.applyMoApplicationStatusTransition(record, openJob(), "shortlisted");
        assertEquals("shortlisted", record.getStatus());
        assertEquals("keep", record.getDecisionFeedback());
    }

    @Test
    void hiredToPending_allowedWhenRecruitmentClosed() {
        ApplicationRecord record = recordWithStatus("hired");
        MoApplicationService.applyMoApplicationStatusTransition(record, closedJob(), "pending");
        assertEquals("pending", record.getStatus());
        assertEquals("", record.getDecisionFeedback());
    }

    @Test
    void shortlistedToHired_whenRecruitmentClosed_throws() {
        ApplicationRecord record = recordWithStatus("shortlisted");
        assertMoBusinessException(
                () -> MoApplicationService.applyMoApplicationStatusTransition(record, closedJob(), "hired"),
                ErrorCodes.JOB_RECRUITMENT_CLOSED,
                HttpServletResponse.SC_BAD_REQUEST
        );
    }

    @Test
    void hiredToRejected_throwsFinal() {
        ApplicationRecord record = recordWithStatus("hired");
        assertMoBusinessException(
                () -> MoApplicationService.applyMoApplicationStatusTransition(record, openJob(), "rejected"),
                ErrorCodes.VALIDATION_ERROR,
                HttpServletResponse.SC_BAD_REQUEST
        );
    }

    @Test
    void rejectedToHired_requiresUndoFirst() {
        ApplicationRecord record = recordWithStatus("rejected");
        assertMoBusinessException(
                () -> MoApplicationService.applyMoApplicationStatusTransition(record, openJob(), "hired"),
                ErrorCodes.VALIDATION_ERROR,
                HttpServletResponse.SC_BAD_REQUEST
        );
    }

    @Test
    void pendingToViewed_notAllowedExceptUndoReject() {
        ApplicationRecord record = recordWithStatus("pending");
        assertMoBusinessException(
                () -> MoApplicationService.applyMoApplicationStatusTransition(record, openJob(), "viewed"),
                ErrorCodes.VALIDATION_ERROR,
                HttpServletResponse.SC_BAD_REQUEST
        );
    }

    @Test
    void pendingToPending_isNoOp() {
        ApplicationRecord record = recordWithStatus("pending");
        MoApplicationService.applyMoApplicationStatusTransition(record, openJob(), "pending");
        assertEquals("pending", record.getStatus());
    }

    @Test
    void shortlistedToRejected_succeeds() {
        ApplicationRecord record = recordWithStatus("shortlisted");
        MoApplicationService.applyMoApplicationStatusTransition(record, openJob(), "rejected");
        assertEquals("rejected", record.getStatus());
    }

    @Test
    void closedJob_blocksShortlistedToRejected() {
        ApplicationRecord record = recordWithStatus("shortlisted");
        assertMoBusinessException(
                () -> MoApplicationService.applyMoApplicationStatusTransition(record, closedJob(), "rejected"),
                ErrorCodes.JOB_RECRUITMENT_CLOSED,
                HttpServletResponse.SC_BAD_REQUEST
        );
    }
}
