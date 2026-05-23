package com.ta.service.student;

import com.ta.model.ApplicationRecord;
import com.ta.model.JobPosting;
import com.ta.model.StudentProfile;
import com.ta.util.JsonUtility;
import jakarta.servlet.ServletContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Deterministic matching layer for TA job recommendations.
 *
 * This service converts student skills and job requirements into structured
 * match context. A future AI-assisted recommendation feature can use these
 * results as grounded input for explanations without changing the base score.
 */
public class JobMatchingService {
    private final SkillExtractionService skillExtractionService;
    private final SkillMatchScorer skillMatchScorer;

    public JobMatchingService() {
        this(new SkillExtractionService(), new SkillMatchScorer());
    }

    public JobMatchingService(SkillExtractionService skillExtractionService) {
        this(skillExtractionService, new SkillMatchScorer());
    }

    public JobMatchingService(SkillExtractionService skillExtractionService, SkillMatchScorer skillMatchScorer) {
        this.skillExtractionService = skillExtractionService != null
                ? skillExtractionService
                : new SkillExtractionService();
        this.skillMatchScorer = skillMatchScorer != null ? skillMatchScorer : new SkillMatchScorer();
    }

    public JobMatchResult match(StudentProfile student, JobPosting job) {
        List<String> studentSkills = skillExtractionService.extractSkillsFromStudent(student);
        List<String> requiredSkills = skillExtractionService.extractSkillsFromJob(job);

        SkillMatchScorer.SkillMatchOutcome outcome = skillMatchScorer.score(requiredSkills, studentSkills);

        return new JobMatchResult(
                job,
                studentSkills,
                requiredSkills,
                outcome.getMatchedSkills(),
                outcome.getMissingSkills(),
                outcome.getRelatedMatches(),
                outcome.getMatchScore());
    }

    public List<JobMatchResult> matchJobs(StudentProfile student, List<JobPosting> jobs) {
        List<JobMatchResult> results = new ArrayList<>();
        if (jobs == null || jobs.isEmpty()) {
            return results;
        }

        for (JobPosting job : jobs) {
            if (job != null) {
                results.add(match(student, job));
            }
        }
        return results;
    }

    public List<JobMatchResult> getRecommendedJobs(StudentProfile student, List<JobPosting> jobs) {
        return getRecommendedJobs(null, student, jobs);
    }

    public List<JobMatchResult> getRecommendedJobs(ServletContext context, StudentProfile student, List<JobPosting> jobs) {
        List<JobMatchResult> results = new ArrayList<>();
        if (jobs == null || jobs.isEmpty()) {
            return results;
        }

        Map<String, Integer> hiredByJobId = loadHiredCountByJobId(context);

        for (JobPosting job : jobs) {
            if (job != null && isRecommendable(job, hiredByJobId)) {
                results.add(match(student, job));
            }
        }

        results.sort(Comparator
                .comparingDouble(JobMatchResult::getMatchScore).reversed()
                .thenComparing(result -> safeText(result.getJob().getTitle()), String.CASE_INSENSITIVE_ORDER)
                .thenComparing(result -> safeText(result.getJob().getId()), String.CASE_INSENSITIVE_ORDER));
        return results;
    }

    private Map<String, Integer> loadHiredCountByJobId(ServletContext context) {
        if (context == null) {
            return Map.of();
        }
        try {
            Map<String, Integer> counts = new HashMap<>();
            for (ApplicationRecord app : JsonUtility.loadApplications(context)) {
                if (!app.isActive() || app.getJobId() == null) {
                    continue;
                }
                if (!"hired".equalsIgnoreCase(safeText(app.getStatus()))) {
                    continue;
                }
                counts.merge(app.getJobId(), 1, Integer::sum);
            }
            return counts;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load applications for job recommendations.", e);
        }
    }

    private boolean isRecommendable(JobPosting job, Map<String, Integer> hiredByJobId) {
        if (Boolean.TRUE.equals(job.getRecruitmentClosed())) {
            return false;
        }
        if (hasText(job.getStatus()) && !"open".equalsIgnoreCase(job.getStatus())) {
            return false;
        }
        if (hasText(job.getApprovalStatus()) && !"approved".equalsIgnoreCase(job.getApprovalStatus())) {
            return false;
        }
        if (job.getPublished() != null && !Boolean.TRUE.equals(job.getPublished())) {
            return false;
        }
        if (job.getWithdrawn() != null && Boolean.TRUE.equals(job.getWithdrawn())) {
            return false;
        }
        int positions = job.getPositions();
        if (positions > 0 && job.getId() != null) {
            int hired = hiredByJobId.getOrDefault(job.getId(), 0);
            if (hired >= positions) {
                return false;
            }
        }
        return true;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }
}
