package com.ta.service.mo;

import com.ta.constant.ErrorCodes;
import com.ta.dto.mo.MoDemandItemResponse;
import com.ta.dto.mo.MoJobEditRequest;
import com.ta.dto.mo.MoJobPublishRequest;
import com.ta.dto.mo.MoJobPublishResponse;
import com.ta.dto.mo.MoJobWithdrawResponse;
import com.ta.model.ApplicationRecord;
import com.ta.model.JobPosting;
import com.ta.testsupport.MoTestSupport;
import com.ta.util.JsonUtility;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MoJobServiceTest extends MoTestSupport {

    private MoJobService service;

    @BeforeEach
    void setUpService() throws Exception {
        service = new MoJobService();
        writeApplications(new ArrayList<>());
    }

    @Test
    void publishJob_setsOpenPublishedFieldsForApprovedDemand() throws Exception {
        JobPosting job = approvedDraftJob(JOB_ID, MO_ID);
        writeJobs(List.of(job));

        MoJobPublishResponse response = service.publishJob(servletContext, MO_ID, JOB_ID, publishRequest());
        JobPosting saved = JsonUtility.loadJobs(servletContext).get(0);

        assertEquals(JOB_ID, response.getJobId());
        assertTrue(Boolean.TRUE.equals(response.getPublished()));
        assertEquals("2026-06-30", response.getDeadline());
        assertEquals("offline", response.getLocation());
        assertEquals("Tue 10:00-12:00", response.getSchedule());
        assertEquals("Java, SQL", response.getRequirements());
        assertTrue(Boolean.TRUE.equals(saved.getPublished()));
        assertFalse(Boolean.TRUE.equals(saved.getWithdrawn()));
        assertEquals("open", saved.getStatus());
        assertNotNull(saved.getPublishedAt());
        assertNotNull(saved.getUpdatedAt());
    }

    @Test
    void publishJob_rejectsUnapprovedDemand() throws Exception {
        JobPosting job = approvedDraftJob(JOB_ID, MO_ID);
        job.setApprovalStatus("pending");
        writeJobs(List.of(job));

        assertMoBusinessException(
                () -> service.publishJob(servletContext, MO_ID, JOB_ID, publishRequest()),
                ErrorCodes.JOB_NOT_APPROVED,
                HttpServletResponse.SC_BAD_REQUEST
        );
    }

    @Test
    void publishJob_rejectsOtherTeachersJob() throws Exception {
        writeJobs(List.of(approvedDraftJob(JOB_ID, OTHER_MO_ID)));

        assertMoBusinessException(
                () -> service.publishJob(servletContext, MO_ID, JOB_ID, publishRequest()),
                ErrorCodes.FORBIDDEN_NOT_OWNER,
                HttpServletResponse.SC_FORBIDDEN
        );
    }

    @Test
    void publishJob_rejectsAlreadyPublishedJob() throws Exception {
        JobPosting job = approvedDraftJob(JOB_ID, MO_ID);
        job.setPublished(true);
        writeJobs(List.of(job));

        assertMoBusinessException(
                () -> service.publishJob(servletContext, MO_ID, JOB_ID, publishRequest()),
                ErrorCodes.JOB_ALREADY_PUBLISHED,
                HttpServletResponse.SC_BAD_REQUEST
        );
    }

    @Test
    void takeOffline_withPublishedJobMarksOfflineAndWithdrawn() throws Exception {
        JobPosting job = publishedJob(JOB_ID, MO_ID);
        writeJobs(List.of(job));

        MoJobWithdrawResponse response = service.takeOffline(servletContext, MO_ID, JOB_ID);
        JobPosting saved = JsonUtility.loadJobs(servletContext).get(0);

        assertEquals(JOB_ID, response.getJobId());
        assertTrue(Boolean.TRUE.equals(response.getWithdrawn()));
        assertNotNull(response.getWithdrawnAt());
        assertFalse(Boolean.TRUE.equals(saved.getPublished()));
        assertTrue(Boolean.TRUE.equals(saved.getWithdrawn()));
        assertEquals("offline", saved.getStatus());
    }

    @Test
    void takeOffline_rejectsDraftJob() throws Exception {
        writeJobs(List.of(approvedDraftJob(JOB_ID, MO_ID)));

        assertMoBusinessException(
                () -> service.takeOffline(servletContext, MO_ID, JOB_ID),
                ErrorCodes.VALIDATION_ERROR,
                HttpServletResponse.SC_BAD_REQUEST
        );
    }

    @Test
    void editJob_updatesDraftAndResetsApprovalToPending() throws Exception {
        JobPosting job = approvedDraftJob(JOB_ID, MO_ID);
        job.setApprovalStatus("approved");
        job.setReviewedAt("2026-05-01T00:00:00Z");
        job.setRejectionReason("old reason");
        writeJobs(List.of(job));

        MoDemandItemResponse response = service.editJob(servletContext, MO_ID, JOB_ID, editRequest());
        JobPosting saved = JsonUtility.loadJobs(servletContext).get(0);

        assertEquals(JOB_ID, response.getJobId());
        assertEquals("Advanced Java", saved.getTitle());
        assertEquals("Advanced Java", saved.getModuleCode());
        assertEquals("Engineering", saved.getDepartment());
        assertEquals(3, saved.getPositions());
        assertEquals(5, saved.getHourMin());
        assertEquals(9, saved.getHourMax());
        assertEquals("Spring, Testing", saved.getRequirements());
        assertFalse(Boolean.TRUE.equals(saved.getPublished()));
        assertFalse(Boolean.TRUE.equals(saved.getWithdrawn()));
        assertEquals("draft", saved.getStatus());
        assertEquals("pending", saved.getApprovalStatus());
        assertEquals(null, saved.getReviewedAt());
        assertEquals(null, saved.getRejectionReason());
    }

    @Test
    void editJob_allowsPublishedFullyStaffedJobAndKeepsItPublished() throws Exception {
        JobPosting job = publishedJob(JOB_ID, MO_ID);
        job.setPositions(1);
        job.setRecruitmentClosed(true);
        writeJobs(List.of(job));
        writeApplications(List.of(application("app_hired", JOB_ID, "hired", true)));

        service.editJob(servletContext, MO_ID, JOB_ID, editRequest());

        JobPosting saved = JsonUtility.loadJobs(servletContext).get(0);
        assertEquals("Advanced Java", saved.getTitle());
        assertEquals(5, saved.getHourMin());
        assertEquals(9, saved.getHourMax());
        assertTrue(Boolean.TRUE.equals(saved.getPublished()));
        assertFalse(Boolean.TRUE.equals(saved.getRecruitmentClosed()));
        assertEquals("open", saved.getStatus());
    }

    @Test
    void deleteDraftJob_removesJobAndDeactivatesApplications() throws Exception {
        JobPosting job = approvedDraftJob(JOB_ID, MO_ID);
        ApplicationRecord active = application("app_active", JOB_ID, "pending", true);
        ApplicationRecord inactive = application("app_inactive", JOB_ID, "pending", false);
        ApplicationRecord otherJob = application("app_other", OTHER_JOB_ID, "pending", true);
        writeJobs(List.of(job, approvedDraftJob(OTHER_JOB_ID, MO_ID)));
        writeApplications(List.of(active, inactive, otherJob));

        service.deleteDraftJob(servletContext, MO_ID, JOB_ID);

        List<JobPosting> jobs = JsonUtility.loadJobs(servletContext);
        List<ApplicationRecord> applications = JsonUtility.loadApplications(servletContext);
        assertEquals(1, jobs.size());
        assertEquals(OTHER_JOB_ID, jobs.get(0).getId());
        assertFalse(applications.stream().filter(a -> "app_active".equals(a.getId())).findFirst().orElseThrow().isActive());
        assertFalse(applications.stream().filter(a -> "app_inactive".equals(a.getId())).findFirst().orElseThrow().isActive());
        assertTrue(applications.stream().filter(a -> "app_other".equals(a.getId())).findFirst().orElseThrow().isActive());
    }

    @Test
    void deleteDraftJob_rejectsPublishedJob() throws Exception {
        writeJobs(List.of(publishedJob(JOB_ID, MO_ID)));

        assertMoBusinessException(
                () -> service.deleteDraftJob(servletContext, MO_ID, JOB_ID),
                ErrorCodes.VALIDATION_ERROR,
                HttpServletResponse.SC_BAD_REQUEST
        );
    }

    @Test
    void reuseJob_copiesExistingJobAsNewPendingDraft() throws Exception {
        JobPosting source = publishedJob(JOB_ID, MO_ID);
        source.setRecruitmentClosed(true);
        source.setClosedAt("2026-05-10T00:00:00Z");
        writeJobs(List.of(source));

        MoDemandItemResponse response = service.reuseJob(servletContext, MO_ID, JOB_ID);
        List<JobPosting> jobs = JsonUtility.loadJobs(servletContext);
        JobPosting copied = jobs.stream().filter(j -> !JOB_ID.equals(j.getId())).findFirst().orElseThrow();

        assertEquals(2, jobs.size());
        assertNotEquals(JOB_ID, response.getJobId());
        assertEquals(response.getJobId(), copied.getId());
        assertEquals(MO_ID, copied.getTeacherId());
        assertEquals(source.getTitle(), copied.getTitle());
        assertEquals(source.getDepartment(), copied.getDepartment());
        assertEquals("pending", copied.getApprovalStatus());
        assertEquals("draft", copied.getStatus());
        assertFalse(Boolean.TRUE.equals(copied.getPublished()));
        assertFalse(Boolean.TRUE.equals(copied.getWithdrawn()));
        assertFalse(Boolean.TRUE.equals(copied.getRecruitmentClosed()));
        assertEquals(null, copied.getDeadline());
        assertEquals(null, copied.getClosedAt());
        assertNotNull(copied.getCreatedAt());
    }

    private static MoJobPublishRequest publishRequest() {
        MoJobPublishRequest request = new MoJobPublishRequest();
        request.setLocation("offline");
        request.setRequirements("Java, SQL");
        request.setDeadline("2026-06-30");
        request.setSchedule(" Tue 10:00-12:00 ");
        return request;
    }

    private static MoJobEditRequest editRequest() {
        MoJobEditRequest request = new MoJobEditRequest();
        request.setCourseName("Advanced Java");
        request.setDepartment("Engineering");
        request.setPlannedCount(3);
        request.setHourMin(5);
        request.setHourMax(9);
        request.setRequirements(" Spring, Testing ");
        return request;
    }

    private static JobPosting approvedDraftJob(String jobId, String teacherId) {
        JobPosting job = new JobPosting();
        job.setId(jobId);
        job.setTeacherId(teacherId);
        job.setTeacherName("Teacher");
        job.setTitle("Software Engineering");
        job.setModuleCode("Software Engineering");
        job.setDepartment("Computer Science");
        job.setPositions(2);
        job.setHourMin(4);
        job.setHourMax(8);
        job.setRequirements("Java");
        job.setApprovalStatus("approved");
        job.setPublished(false);
        job.setWithdrawn(false);
        job.setStatus("draft");
        job.setRecruitmentClosed(false);
        job.setCreatedAt("2026-05-01T00:00:00Z");
        job.setUpdatedAt("2026-05-01T00:00:00Z");
        return job;
    }

    private static JobPosting publishedJob(String jobId, String teacherId) {
        JobPosting job = approvedDraftJob(jobId, teacherId);
        job.setPublished(true);
        job.setWithdrawn(false);
        job.setStatus("open");
        job.setLocation("online");
        job.setDeadline("2026-06-01");
        job.setSchedule("Mon 09:00-11:00");
        job.setPublishedAt("2026-05-02T00:00:00Z");
        return job;
    }
}
