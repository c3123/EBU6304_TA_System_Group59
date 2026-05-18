package com.ta.dto.mo;

import java.util.ArrayList;
import java.util.List;

/**
 * Application list item for MO view.
 */
public class MoApplicationListItemResponse {
    private String applicationId;
    private String jobId;
    private String studentId;
    private String studentName;
    private String studentNo;
    private String courseGrade;
    private String appliedAt;
    private String status;
    /** Enriched from student profile when available */
    private String programme;
    private String skills;
    private String experience;
    private String evaluationNotes;
    private String decisionFeedback;
    /** Skill match vs job requirements (0.0–1.0), from JobMatchingService */
    private double matchScore;
    private List<String> matchedSkills = new ArrayList<>();
    private List<String> missingSkills = new ArrayList<>();
    private List<String> requiredSkills = new ArrayList<>();
    /** Skills detected from applicant profile text (normalized) */
    private List<String> detectedStudentSkills = new ArrayList<>();
    /** Partial credit links e.g. "MATLAB → Statistics (80%)" */
    private List<String> relatedMatches = new ArrayList<>();

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentNo() {
        return studentNo;
    }

    public void setStudentNo(String studentNo) {
        this.studentNo = studentNo;
    }

    public String getCourseGrade() {
        return courseGrade;
    }

    public void setCourseGrade(String courseGrade) {
        this.courseGrade = courseGrade;
    }

    public String getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(String appliedAt) {
        this.appliedAt = appliedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProgramme() {
        return programme;
    }

    public void setProgramme(String programme) {
        this.programme = programme;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public String getEvaluationNotes() {
        return evaluationNotes;
    }

    public void setEvaluationNotes(String evaluationNotes) {
        this.evaluationNotes = evaluationNotes;
    }

    public String getDecisionFeedback() {
        return decisionFeedback;
    }

    public void setDecisionFeedback(String decisionFeedback) {
        this.decisionFeedback = decisionFeedback;
    }

    public double getMatchScore() {
        return matchScore;
    }

    public void setMatchScore(double matchScore) {
        this.matchScore = matchScore;
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

    public List<String> getRequiredSkills() {
        return requiredSkills;
    }

    public void setRequiredSkills(List<String> requiredSkills) {
        this.requiredSkills = requiredSkills != null ? requiredSkills : new ArrayList<>();
    }

    public List<String> getDetectedStudentSkills() {
        return detectedStudentSkills;
    }

    public void setDetectedStudentSkills(List<String> detectedStudentSkills) {
        this.detectedStudentSkills = detectedStudentSkills != null ? detectedStudentSkills : new ArrayList<>();
    }

    public List<String> getRelatedMatches() {
        return relatedMatches;
    }

    public void setRelatedMatches(List<String> relatedMatches) {
        this.relatedMatches = relatedMatches != null ? relatedMatches : new ArrayList<>();
    }
}
