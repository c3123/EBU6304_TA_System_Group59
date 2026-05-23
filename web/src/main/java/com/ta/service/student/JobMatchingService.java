package com.ta.service.student;

import com.ta.model.JobPosting;
import com.ta.model.StudentProfile;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
        List<JobMatchResult> results = new ArrayList<>();
        if (jobs == null || jobs.isEmpty()) {
            return results;
        }

        for (JobPosting job : jobs) {
            if (job != null && isRecommendable(job)) {
                results.add(match(student, job));
            }
        }

        results.sort(Comparator
                .comparingDouble(JobMatchResult::getMatchScore).reversed()
                .thenComparing(result -> safeText(result.getJob().getTitle()), String.CASE_INSENSITIVE_ORDER)
                .thenComparing(result -> safeText(result.getJob().getId()), String.CASE_INSENSITIVE_ORDER));
        return results;
    }

    private boolean isRecommendable(JobPosting job) {
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
        return true;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }
}
