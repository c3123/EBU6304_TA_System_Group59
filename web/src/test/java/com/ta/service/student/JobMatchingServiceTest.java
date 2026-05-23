package com.ta.service.student;

import com.ta.model.JobPosting;
import com.ta.model.StudentProfile;
import com.ta.testsupport.AdminServiceTestSupport;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JobMatchingServiceTest extends AdminServiceTestSupport {

    private final JobMatchingService service = new JobMatchingService();

    @Test
    void getRecommendedJobs_filtersUnpublishedWithdrawnRejectedAndClosedJobs() {
        StudentProfile profile = profile(STUDENT_USER_ID, "Student");
        JobPosting strong = job("job_a", MO_ID, "open", "approved", true, false, false, 1, 8);
        strong.setTitle("A Java Lab");
        strong.setRequirements("Java, Python");
        JobPosting weak = job("job_b", MO_ID, "open", "approved", true, false, false, 1, 8);
        weak.setTitle("B Research Lab");
        weak.setRequirements("Research");
        JobPosting draft = job("job_c", MO_ID, "draft", "approved", true, false, false, 1, 8);
        JobPosting rejected = job("job_d", MO_ID, "open", "rejected", true, false, false, 1, 8);
        JobPosting unpublished = job("job_e", MO_ID, "open", "approved", false, false, false, 1, 8);
        JobPosting withdrawn = job("job_f", MO_ID, "open", "approved", true, true, false, 1, 8);

        List<JobMatchResult> results = service.getRecommendedJobs(profile, List.of(strong, weak, draft, rejected, unpublished, withdrawn));

        assertEquals(2, results.size());
        assertEquals("job_a", results.get(0).getJob().getId());
        assertEquals(1.0, results.get(0).getMatchScore());
        assertEquals("job_b", results.get(1).getJob().getId());
    }

    @Test
    void match_exposesMatchedMissingAndRelatedSkills() {
        StudentProfile profile = profile(STUDENT_USER_ID, "Student");
        profile.setSkills("Python, Communication");
        JobPosting job = job("job_ml", MO_ID, "open", "approved", true, false, false, 1, 8);
        job.setRequirements("Machine Learning, Teaching, Java");

        JobMatchResult result = service.match(profile, job);

        assertTrue(result.getRelatedMatches().stream().anyMatch(h -> h.toDisplayLabel().contains("Machine Learning")));
        assertTrue(result.getRelatedMatches().stream().anyMatch(h -> h.toDisplayLabel().contains("Teaching")));
        assertTrue(result.getMissingSkills().contains("Java"));
        assertTrue(result.getMatchScore() > 0.0);
    }
}
