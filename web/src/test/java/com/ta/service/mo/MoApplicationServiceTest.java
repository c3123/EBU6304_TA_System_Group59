package com.ta.service.mo;

import com.google.gson.Gson;
import com.ta.constant.ErrorCodes;
import com.ta.dto.mo.MoApplicationDetailResponse;
import com.ta.dto.mo.MoApplicationListItemResponse;
import com.ta.dto.mo.MoApplicationListResponse;
import com.ta.model.ApplicationRecord;
import com.ta.model.HiringHistoryRecord;
import com.ta.model.JobPosting;
import com.ta.model.StudentProfile;
import com.ta.service.student.JobMatchResult;
import com.ta.service.student.JobMatchingService;
import com.ta.testsupport.MoTestSupport;
import com.ta.util.JsonUtility;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class MoApplicationServiceTest extends MoTestSupport {

    private static final Gson GSON = new Gson();

    @Mock
    private JobMatchingService jobMatchingService;

    private MoApplicationService service;

    @BeforeEach
    void seedAndMock() throws Exception {
        service = new MoApplicationService(jobMatchingService);
        JobMatchResult match = new JobMatchResult();
        match.setMatchScore(0.75);
        match.setMatchedSkills(List.of("Java"));
        match.setMissingSkills(List.of("Python"));
        match.setRequiredSkills(List.of("Java", "Python"));
        match.setStudentSkills(List.of("Java"));
        lenient().when(jobMatchingService.match(any(StudentProfile.class), any(JobPosting.class))).thenReturn(match);

        writeJobs(defaultJobs());
        writeApplications(defaultApplications());
        writeStudents(List.of(studentProfile()));
        JsonUtility.saveHiringHistory(servletContext, new ArrayList<>());
    }

    @Test
    void listApplications_returnsOnlyOwnedActiveApplications() {
        MoApplicationListResponse response = service.listApplications(servletContext, MO_ID, null, null);
        List<String> ids = response.getItems().stream()
                .map(MoApplicationListItemResponse::getApplicationId)
                .collect(Collectors.toList());
        assertEquals(5, ids.size());
        assertTrue(ids.contains("app_pending"));
        assertTrue(ids.contains("app_hired"));
        assertFalse(ids.contains("app_other_job"));
        assertFalse(ids.contains("app_inactive"));
    }

    @Test
    void listApplications_includesMatchScoreFromMock() {
        MoApplicationListResponse response = service.listApplications(servletContext, MO_ID, null, null);
        MoApplicationListItemResponse item = response.getItems().stream()
                .filter(i -> "app_pending".equals(i.getApplicationId()))
                .findFirst()
                .orElseThrow();
        assertEquals(0.75, item.getMatchScore());
        assertEquals(List.of("Java"), item.getMatchedSkills());
    }

    @Test
    void listApplications_jobIdFilter() {
        MoApplicationListResponse response = service.listApplications(servletContext, MO_ID, JOB_ID, null);
        assertTrue(response.getItems().stream().allMatch(i -> JOB_ID.equals(i.getJobId())));
    }

    @Test
    void listApplications_statusNone_returnsEmpty() {
        MoApplicationListResponse response = service.listApplications(
                servletContext, MO_ID, null, MoApplicationService.STATUS_FILTER_NONE_SENTINEL);
        assertTrue(response.getItems().isEmpty());
    }

    @Test
    void listApplications_otherMoJobId_throws403() {
        assertMoBusinessException(
                () -> service.listApplications(servletContext, MO_ID, OTHER_JOB_ID, null),
                ErrorCodes.FORBIDDEN_NOT_OWNER,
                HttpServletResponse.SC_FORBIDDEN
        );
    }

    @Test
    void listApplications_unknownJobId_throws404() {
        assertMoBusinessException(
                () -> service.listApplications(servletContext, MO_ID, "job_missing", null),
                ErrorCodes.JOB_NOT_FOUND,
                HttpServletResponse.SC_NOT_FOUND
        );
    }

    @Test
    void getDetailAndMarkViewed_pendingBecomesViewed() throws Exception {
        MoApplicationDetailResponse detail = service.getDetailAndMarkViewed(servletContext, MO_ID, "app_pending");
        assertEquals("viewed", detail.getStatus());
        assertNotNull(detail.getUpdatedAt());
        assertEquals("viewed", readApplications().stream()
                .filter(a -> "app_pending".equals(a.getId()))
                .findFirst()
                .orElseThrow()
                .getStatus());
    }

    @Test
    void getDetailAndMarkViewed_alreadyViewed_noStatusChange() throws Exception {
        MoApplicationDetailResponse detail = service.getDetailAndMarkViewed(servletContext, MO_ID, "app_viewed");
        assertEquals("viewed", detail.getStatus());
        assertEquals(null, detail.getUpdatedAt());
    }

    @Test
    void getDetailAndMarkViewed_inactive_throws404() {
        assertMoBusinessException(
                () -> service.getDetailAndMarkViewed(servletContext, MO_ID, "app_inactive"),
                ErrorCodes.APPLICATION_NOT_FOUND,
                HttpServletResponse.SC_NOT_FOUND
        );
    }

    @Test
    void getDetailAndMarkViewed_otherMoJob_throws403() {
        assertMoBusinessException(
                () -> service.getDetailAndMarkViewed(servletContext, MO_ID, "app_other_job"),
                ErrorCodes.FORBIDDEN_NOT_OWNER,
                HttpServletResponse.SC_FORBIDDEN
        );
    }

    @Test
    void getDetailAndMarkViewed_missingId_throws404() {
        assertMoBusinessException(
                () -> service.getDetailAndMarkViewed(servletContext, MO_ID, "app_missing"),
                ErrorCodes.APPLICATION_NOT_FOUND,
                HttpServletResponse.SC_NOT_FOUND
        );
    }

    @Test
    void updateApplicationStatus_shortlistedToHired_appendsHistory() throws Exception {
        service.updateApplicationStatus(servletContext, MO_ID, "app_shortlisted", "hired");
        List<HiringHistoryRecord> history = JsonUtility.loadHiringHistory(servletContext);
        assertEquals(1, history.size());
        assertEquals("manual_hire", history.get(0).getAction());
        assertEquals(List.of("app_shortlisted"), history.get(0).getHiredApplicationIds());
    }

    @Test
    void updateApplicationStatus_invalidStatus_throws400() {
        assertMoBusinessException(
                () -> service.updateApplicationStatus(servletContext, MO_ID, "app_pending", "invalid"),
                ErrorCodes.VALIDATION_ERROR,
                HttpServletResponse.SC_BAD_REQUEST
        );
    }

    @Test
    void updateApplicationStatus_closedJob_shortlistedToHired_throws() throws Exception {
        List<JobPosting> jobs = defaultJobs();
        jobs.get(0).setRecruitmentClosed(true);
        writeJobs(jobs);

        assertMoBusinessException(
                () -> service.updateApplicationStatus(servletContext, MO_ID, "app_shortlisted", "hired"),
                ErrorCodes.JOB_RECRUITMENT_CLOSED,
                HttpServletResponse.SC_BAD_REQUEST
        );
    }

    @Test
    void batchUpdateApplicationStatus_updatesAll() throws Exception {
        Map<String, Object> result = service.batchUpdateApplicationStatus(
                servletContext, MO_ID, List.of("app_pending", "app_viewed"), "shortlisted");
        assertEquals(2, result.get("updated"));
        List<ApplicationRecord> apps = readApplications();
        assertEquals("shortlisted", findApp(apps, "app_pending").getStatus());
        assertEquals("shortlisted", findApp(apps, "app_viewed").getStatus());
    }

    @Test
    void batchUpdateApplicationStatus_emptyIds_throws400() {
        assertMoBusinessException(
                () -> service.batchUpdateApplicationStatus(servletContext, MO_ID, List.of(), "shortlisted"),
                ErrorCodes.VALIDATION_ERROR,
                HttpServletResponse.SC_BAD_REQUEST
        );
    }

    @Test
    void batchUpdateApplicationStatus_oneFailure_rollsBackAll() throws Exception {
        String before = GSON.toJson(readApplications());
        assertMoBusinessException(
                () -> service.batchUpdateApplicationStatus(
                        servletContext, MO_ID, List.of("app_pending", "app_hired"), "shortlisted"),
                ErrorCodes.VALIDATION_ERROR,
                HttpServletResponse.SC_BAD_REQUEST
        );
        assertEquals(before, GSON.toJson(readApplications()));
    }

    @Test
    void updateEvaluationNotes_writesNotes() throws Exception {
        service.updateEvaluationNotes(servletContext, MO_ID, "app_pending", "Strong candidate");
        assertEquals("Strong candidate", findApp(readApplications(), "app_pending").getEvaluationNotes());
    }

    @Test
    void updateEvaluationNotes_emptyNotesAllowed() throws Exception {
        service.updateEvaluationNotes(servletContext, MO_ID, "app_pending", "");
        assertEquals("", findApp(readApplications(), "app_pending").getEvaluationNotes());
    }

    @Test
    void updateEvaluationNotes_closedJob_throws400() throws Exception {
        List<JobPosting> jobs = defaultJobs();
        jobs.get(0).setRecruitmentClosed(true);
        writeJobs(jobs);

        assertMoBusinessException(
                () -> service.updateEvaluationNotes(servletContext, MO_ID, "app_pending", "note"),
                ErrorCodes.JOB_RECRUITMENT_CLOSED,
                HttpServletResponse.SC_BAD_REQUEST
        );
    }

    @Test
    void updateDecisionFeedback_hiredWithin200Chars() throws Exception {
        String feedback = "x".repeat(200);
        service.updateDecisionFeedback(servletContext, MO_ID, "app_hired", feedback);
        assertEquals(feedback, findApp(readApplications(), "app_hired").getDecisionFeedback());
    }

    @Test
    void updateDecisionFeedback_201Chars_throws400() {
        assertMoBusinessException(
                () -> service.updateDecisionFeedback(servletContext, MO_ID, "app_hired", "x".repeat(201)),
                ErrorCodes.VALIDATION_ERROR,
                HttpServletResponse.SC_BAD_REQUEST
        );
    }

    @Test
    void updateDecisionFeedback_pendingStatus_throws400() {
        assertMoBusinessException(
                () -> service.updateDecisionFeedback(servletContext, MO_ID, "app_pending", "no"),
                ErrorCodes.VALIDATION_ERROR,
                HttpServletResponse.SC_BAD_REQUEST
        );
    }

    @Test
    void updateDecisionFeedback_closedJob_throws400() throws Exception {
        List<JobPosting> jobs = defaultJobs();
        jobs.get(0).setRecruitmentClosed(true);
        writeJobs(jobs);

        assertMoBusinessException(
                () -> service.updateDecisionFeedback(servletContext, MO_ID, "app_hired", "ok"),
                ErrorCodes.JOB_RECRUITMENT_CLOSED,
                HttpServletResponse.SC_BAD_REQUEST
        );
    }

    private static ApplicationRecord findApp(List<ApplicationRecord> apps, String id) {
        return apps.stream().filter(a -> id.equals(a.getId())).findFirst().orElseThrow();
    }

    @Test
    void updateApplicationStatus_secondHire_throwsWhenAtPositionLimit() throws Exception {
        List<JobPosting> jobs = defaultJobs();
        jobs.get(0).setPositions(1);
        writeJobs(jobs);

        assertMoBusinessException(
                () -> service.updateApplicationStatus(servletContext, MO_ID, "app_shortlisted", "hired"),
                ErrorCodes.JOB_RECRUITMENT_CLOSED,
                HttpServletResponse.SC_BAD_REQUEST
        );
    }

    @Test
    void updateApplicationStatus_firstHire_closesJobWhenFull() throws Exception {
        List<JobPosting> jobs = defaultJobs();
        jobs.get(0).setPositions(1);
        writeJobs(jobs);

        List<ApplicationRecord> apps = new ArrayList<>(defaultApplications());
        apps.removeIf(a -> "app_hired".equals(a.getId()));
        writeApplications(apps);

        service.updateApplicationStatus(servletContext, MO_ID, "app_shortlisted", "hired");

        JobPosting job = readJobs().stream()
                .filter(j -> JOB_ID.equals(j.getId()))
                .findFirst()
                .orElseThrow();
        assertTrue(Boolean.TRUE.equals(job.getRecruitmentClosed()));
        assertEquals("closed", job.getStatus());
        assertFalse(Boolean.TRUE.equals(job.getPublished()));
    }

    @Test
    void batchUpdateApplicationStatus_twoHiresExceedsLimit_rollsBackAll() throws Exception {
        List<JobPosting> jobs = defaultJobs();
        jobs.get(0).setPositions(1);
        writeJobs(jobs);

        List<ApplicationRecord> apps = new ArrayList<>(defaultApplications());
        apps.removeIf(a -> "app_hired".equals(a.getId()));
        writeApplications(apps);
        String before = GSON.toJson(readApplications());

        assertMoBusinessException(
                () -> service.batchUpdateApplicationStatus(
                        servletContext, MO_ID, List.of("app_pending", "app_shortlisted"), "hired"),
                ErrorCodes.JOB_RECRUITMENT_CLOSED,
                HttpServletResponse.SC_BAD_REQUEST
        );
        assertEquals(before, GSON.toJson(readApplications()));
    }

    @Test
    void batchUpdateApplicationStatus_singleHire_closesJobWhenFull() throws Exception {
        List<JobPosting> jobs = defaultJobs();
        jobs.get(0).setPositions(1);
        writeJobs(jobs);

        List<ApplicationRecord> apps = new ArrayList<>(defaultApplications());
        apps.removeIf(a -> "app_hired".equals(a.getId()));
        writeApplications(apps);

        service.batchUpdateApplicationStatus(servletContext, MO_ID, List.of("app_shortlisted"), "hired");

        JobPosting job = readJobs().stream()
                .filter(j -> JOB_ID.equals(j.getId()))
                .findFirst()
                .orElseThrow();
        assertTrue(Boolean.TRUE.equals(job.getRecruitmentClosed()));
        assertEquals("hired", findApp(readApplications(), "app_shortlisted").getStatus());
    }
}
