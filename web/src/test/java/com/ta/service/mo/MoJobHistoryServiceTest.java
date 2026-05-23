package com.ta.service.mo;

import com.ta.dto.mo.MoJobHistoryItemResponse;
import com.ta.dto.mo.MoJobHistoryResponse;
import com.ta.model.ApplicationRecord;
import com.ta.model.JobPosting;
import com.ta.testsupport.MoTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MoJobHistoryServiceTest extends MoTestSupport {

    private MoJobHistoryService service;

    @BeforeEach
    void setUpService() {
        service = new MoJobHistoryService();
    }

    @Test
    void listHistory_returnsOnlyCurrentTeachersJobsWithApplicantAndHireCounts() throws Exception {
        JobPosting ownPublished = job("job_published", MO_ID, "Published Module", "2026-05-03T00:00:00Z");
        ownPublished.setPublished(true);
        ownPublished.setStatus("open");
        ownPublished.setPublishedAt("2026-05-05T00:00:00Z");
        JobPosting ownDraft = job("job_draft", MO_ID, "Draft Module", "2026-05-01T00:00:00Z");
        ownDraft.setApprovalStatus("pending");
        JobPosting other = job("job_other", OTHER_MO_ID, "Other Module", "2026-05-04T00:00:00Z");
        other.setPublished(true);
        writeJobs(List.of(ownDraft, other, ownPublished));

        ApplicationRecord app1 = application("app_1", "job_published", "pending", true);
        ApplicationRecord app2 = application("app_2", "job_published", "hired", true);
        ApplicationRecord inactive = application("app_inactive", "job_published", "hired", false);
        ApplicationRecord draftApp = application("app_3", "job_draft", "hired", true);
        ApplicationRecord otherApp = application("app_other", "job_other", "hired", true);
        writeApplications(List.of(app1, app2, inactive, draftApp, otherApp));

        MoJobHistoryResponse response = service.listHistory(servletContext, MO_ID);

        assertEquals(2, response.getItems().size());
        assertTrue(response.getItems().stream().noneMatch(i -> "job_other".equals(i.getJobId())));
        MoJobHistoryItemResponse published = find(response, "job_published");
        assertEquals("Published Module", published.getCourseName());
        assertEquals("open", published.getStatus());
        assertTrue(Boolean.TRUE.equals(published.getPublished()));
        assertFalse(Boolean.TRUE.equals(published.getWithdrawn()));
        assertEquals(2, published.getApplicantCount());
        assertEquals(1, published.getHireCount());
        assertEquals("2026-05-05T00:00:00Z", published.getReleaseTime());

        MoJobHistoryItemResponse draft = find(response, "job_draft");
        assertEquals("pending", draft.getStatus());
        assertEquals(1, draft.getApplicantCount());
        assertEquals(1, draft.getHireCount());
        assertEquals("2026-05-01T00:00:00Z", draft.getReleaseTime());
    }

    @Test
    void listHistory_resolvesWithdrawnAndClosedStatuses() throws Exception {
        JobPosting withdrawn = job("job_withdrawn", MO_ID, "Withdrawn Module", "2026-05-01T00:00:00Z");
        withdrawn.setWithdrawn(true);
        withdrawn.setPublished(false);
        withdrawn.setStatus("offline");
        JobPosting closed = job("job_closed", MO_ID, "Closed Module", "2026-05-02T00:00:00Z");
        closed.setPublished(false);
        closed.setRecruitmentClosed(true);
        closed.setClosedAt("2026-05-06T00:00:00Z");
        writeJobs(List.of(withdrawn, closed));
        writeApplications(List.of());

        MoJobHistoryResponse response = service.listHistory(servletContext, MO_ID);

        assertEquals("withdrawn", find(response, "job_withdrawn").getStatus());
        assertEquals("recruitment_closed", find(response, "job_closed").getStatus());
        assertTrue(Boolean.TRUE.equals(find(response, "job_closed").getRecruitmentClosed()));
    }

    @Test
    void listHistory_sortsByReleaseTimeDescending() throws Exception {
        JobPosting older = job("job_old", MO_ID, "Old Module", "2026-05-01T00:00:00Z");
        JobPosting newer = job("job_new", MO_ID, "New Module", "2026-05-04T00:00:00Z");
        JobPosting published = job("job_pub", MO_ID, "Published Module", "2026-05-02T00:00:00Z");
        published.setPublished(true);
        published.setUpdatedAt("2026-05-06T00:00:00Z");
        writeJobs(List.of(older, newer, published));
        writeApplications(List.of());

        MoJobHistoryResponse response = service.listHistory(servletContext, MO_ID);

        assertEquals("job_pub", response.getItems().get(0).getJobId());
        assertEquals("job_new", response.getItems().get(1).getJobId());
        assertEquals("job_old", response.getItems().get(2).getJobId());
    }

    private static MoJobHistoryItemResponse find(MoJobHistoryResponse response, String jobId) {
        return response.getItems().stream()
                .filter(item -> jobId.equals(item.getJobId()))
                .findFirst()
                .orElseThrow();
    }

    private static JobPosting job(String id, String teacherId, String title, String createdAt) {
        JobPosting job = new JobPosting();
        job.setId(id);
        job.setTeacherId(teacherId);
        job.setTitle(title);
        job.setDepartment("Computer Science");
        job.setApprovalStatus("pending");
        job.setPublished(false);
        job.setWithdrawn(false);
        job.setRecruitmentClosed(false);
        job.setStatus("draft");
        job.setCreatedAt(createdAt);
        job.setUpdatedAt(createdAt);
        job.setDeadline("2026-06-30");
        return job;
    }
}
