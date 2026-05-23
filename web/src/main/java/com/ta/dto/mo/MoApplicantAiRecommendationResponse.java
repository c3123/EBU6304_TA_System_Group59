package com.ta.dto.mo;

import java.util.ArrayList;
import java.util.List;

public class MoApplicantAiRecommendationResponse {
    private String studentId;
    private String jobId;
    private double skillMatchScore;
    private List<String> matchedSkills = new ArrayList<>();
    private List<String> missingSkills = new ArrayList<>();
    private int currentWorkloadHours;
    private int jobEstimatedHours;
    private int projectedWorkloadHours;
    private String workloadStatus;
    private String recommendationLevel;
    private String aiExplanation;
    private boolean fallback;

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public double getSkillMatchScore() {
        return skillMatchScore;
    }

    public void setSkillMatchScore(double skillMatchScore) {
        this.skillMatchScore = skillMatchScore;
    }

    public List<String> getMatchedSkills() {
        return matchedSkills;
    }

    public void setMatchedSkills(List<String> matchedSkills) {
        this.matchedSkills = matchedSkills != null ? matchedSkills : new ArrayList<>();
    }

    public List<String> getMissingSkills() {
        return missingSkills;
    }

    public void setMissingSkills(List<String> missingSkills) {
        this.missingSkills = missingSkills != null ? missingSkills : new ArrayList<>();
    }

    public int getCurrentWorkloadHours() {
        return currentWorkloadHours;
    }

    public void setCurrentWorkloadHours(int currentWorkloadHours) {
        this.currentWorkloadHours = currentWorkloadHours;
    }

    public int getJobEstimatedHours() {
        return jobEstimatedHours;
    }

    public void setJobEstimatedHours(int jobEstimatedHours) {
        this.jobEstimatedHours = jobEstimatedHours;
    }

    public int getProjectedWorkloadHours() {
        return projectedWorkloadHours;
    }

    public void setProjectedWorkloadHours(int projectedWorkloadHours) {
        this.projectedWorkloadHours = projectedWorkloadHours;
    }

    public String getWorkloadStatus() {
        return workloadStatus;
    }

    public void setWorkloadStatus(String workloadStatus) {
        this.workloadStatus = workloadStatus;
    }

    public String getRecommendationLevel() {
        return recommendationLevel;
    }

    public void setRecommendationLevel(String recommendationLevel) {
        this.recommendationLevel = recommendationLevel;
    }

    public String getAiExplanation() {
        return aiExplanation;
    }

    public void setAiExplanation(String aiExplanation) {
        this.aiExplanation = aiExplanation;
    }

    public boolean isFallback() {
        return fallback;
    }

    public void setFallback(boolean fallback) {
        this.fallback = fallback;
    }
}
