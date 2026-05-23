package com.ta.service.mo;

import com.ta.dto.mo.MoApplicantAiRecommendationRequest;
import com.ta.dto.mo.MoApplicantAiRecommendationResponse;
import com.ta.model.ApplicationRecord;
import com.ta.model.JobPosting;
import com.ta.model.StudentProfile;
import com.ta.service.student.SkillExtractionService;
import com.ta.service.student.ai.AiAdvisorClient;
import com.ta.testsupport.MoTestSupport;
import com.ta.util.JsonUtility;
import jakarta.servlet.ServletContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApplicantRecommendationServiceTest extends MoTestSupport {
    private ApplicantRecommendationService service;

    @BeforeEach
    void setUp() throws Exception {
        service = new ApplicantRecommendationService(new SkillExtractionService(), new FailingAiAdvisorClient());
        JsonUtility.saveHiringHistory(servletContext, new ArrayList<>());
    }

    @Test
    void fullyMatchingApplicant_returnsHighlyRecommendedWhenBalanced() throws Exception {
        seed("Java, SQL", job("job_ai", "Java, SQL", 4), List.of(application("app_ai", "job_ai", "pending", true)));

        MoApplicantAiRecommendationResponse response = service.recommend(servletContext, MO_ID, request("job_ai", "app_ai"));

        assertEquals(1.0, response.getSkillMatchScore());
        assertEquals(List.of("Java", "SQL"), response.getMatchedSkills());
        assertEquals(List.of(), response.getMissingSkills());
        assertEquals(0, response.getCurrentWorkloadHours());
        assertEquals(4, response.getProjectedWorkloadHours());
        assertEquals("Balanced", response.getWorkloadStatus());
        assertEquals("Highly Recommended", response.getRecommendationLevel());
    }

    @Test
    void missingSomeRequiredSkills_returnsMissingSkillsAndRecommended() throws Exception {
        seed("Java", job("job_ai", "Java, SQL", 4), List.of(application("app_ai", "job_ai", "pending", true)));

        MoApplicantAiRecommendationResponse response = service.recommend(servletContext, MO_ID, request("job_ai", "app_ai"));

        assertEquals(0.5, response.getSkillMatchScore());
        assertEquals(List.of("Java"), response.getMatchedSkills());
        assertEquals(List.of("SQL"), response.getMissingSkills());
        assertEquals("Not Recommended", response.getRecommendationLevel());
    }

    @Test
    void workloadBalanced_whenProjectedHoursAtOrBelowEight() throws Exception {
        seedWithExistingHiredHours(4, 4);

        MoApplicantAiRecommendationResponse response = service.recommend(servletContext, MO_ID, request("job_ai", "app_ai"));

        assertEquals(4, response.getCurrentWorkloadHours());
        assertEquals(8, response.getProjectedWorkloadHours());
        assertEquals("Balanced", response.getWorkloadStatus());
    }

    @Test
    void workloadNearLimit_whenProjectedHoursBetweenNineAndTwelve() throws Exception {
        seedWithExistingHiredHours(8, 4);

        MoApplicantAiRecommendationResponse response = service.recommend(servletContext, MO_ID, request("job_ai", "app_ai"));

        assertEquals(8, response.getCurrentWorkloadHours());
        assertEquals(12, response.getProjectedWorkloadHours());
        assertEquals("Near Limit", response.getWorkloadStatus());
        assertEquals("Recommended", response.getRecommendationLevel());
    }

    @Test
    void workloadOverloaded_whenProjectedHoursAboveTwelve() throws Exception {
        seedWithExistingHiredHours(10, 4);

        MoApplicantAiRecommendationResponse response = service.recommend(servletContext, MO_ID, request("job_ai", "app_ai"));

        assertEquals(10, response.getCurrentWorkloadHours());
        assertEquals(14, response.getProjectedWorkloadHours());
        assertEquals("Overloaded", response.getWorkloadStatus());
        assertEquals("Use with Caution", response.getRecommendationLevel());
    }

    @Test
    void aiUnavailable_returnsDeterministicFallbackExplanation() throws Exception {
        seed("Java, SQL", job("job_ai", "Java, SQL", 4), List.of(application("app_ai", "job_ai", "pending", true)));

        MoApplicantAiRecommendationResponse response = service.recommend(servletContext, MO_ID, request("job_ai", "app_ai"));

        assertTrue(response.isFallback());
        assertEquals("The applicant matches 100% of the required skills. Their projected workload is 4 hours per week, which is considered Balanced.", response.getAiExplanation());
    }

    @Test
    void emptyRequiredSkills_doesNotDivideByZero() throws Exception {
        seed("Java, SQL", job("job_ai", "", 4), List.of(application("app_ai", "job_ai", "pending", true)));

        MoApplicantAiRecommendationResponse response = service.recommend(servletContext, MO_ID, request("job_ai", "app_ai"));

        assertEquals(0.0, response.getSkillMatchScore());
        assertEquals(List.of(), response.getMatchedSkills());
        assertEquals(List.of(), response.getMissingSkills());
        assertEquals("Not Recommended", response.getRecommendationLevel());
    }

    private void seedWithExistingHiredHours(int existingHours, int newJobHours) throws Exception {
        JobPosting target = job("job_ai", "Java, SQL", newJobHours);
        JobPosting existing = job("job_existing", "Java", existingHours);
        ApplicationRecord pending = application("app_ai", "job_ai", "pending", true);
        ApplicationRecord hired = application("app_existing", "job_existing", "hired", true);
        seed("Java, SQL", target, List.of(pending, hired), existing);
    }

    private void seed(String skills, JobPosting targetJob, List<ApplicationRecord> applications, JobPosting... extraJobs) throws Exception {
        StudentProfile profile = studentProfile();
        profile.setSkills(skills);
        List<JobPosting> jobs = new ArrayList<>();
        jobs.add(targetJob);
        jobs.addAll(List.of(extraJobs));
        writeJobs(jobs);
        writeApplications(new ArrayList<>(applications));
        writeStudents(List.of(profile));
    }

    private JobPosting job(String id, String requirements, int hours) {
        JobPosting job = ownedJob(id, false);
        job.setRequirements(requirements);
        job.setHours(hours);
        return job;
    }

    private MoApplicantAiRecommendationRequest request(String jobId, String applicationId) {
        MoApplicantAiRecommendationRequest request = new MoApplicantAiRecommendationRequest();
        request.setJobId(jobId);
        request.setApplicationId(applicationId);
        return request;
    }

    private static class FailingAiAdvisorClient extends AiAdvisorClient {
        @Override
        public AiAdvisorResult ask(ServletContext context, String systemPrompt, String userPayload) {
            return AiAdvisorResult.failure();
        }
    }
}
