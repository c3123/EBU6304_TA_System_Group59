package com.ta.service.student;

import com.ta.model.JobPosting;

import java.util.ArrayList;
import java.util.List;

public class JobMatchResult {
    private JobPosting job;
    private List<String> studentSkills;
    private List<String> requiredSkills;
    private List<String> matchedSkills;
    private List<String> missingSkills;
    private double matchScore;

    public JobMatchResult() {
        this.studentSkills = new ArrayList<>();
        this.requiredSkills = new ArrayList<>();
        this.matchedSkills = new ArrayList<>();
        this.missingSkills = new ArrayList<>();
    }

    public JobMatchResult(JobPosting job,
                          List<String> studentSkills,
                          List<String> requiredSkills,
                          List<String> matchedSkills,
                          List<String> missingSkills,
                          double matchScore) {
        this.job = job;
        this.studentSkills = studentSkills != null ? studentSkills : new ArrayList<>();
        this.requiredSkills = requiredSkills != null ? requiredSkills : new ArrayList<>();
        this.matchedSkills = matchedSkills != null ? matchedSkills : new ArrayList<>();
        this.missingSkills = missingSkills != null ? missingSkills : new ArrayList<>();
        this.matchScore = matchScore;
    }

    public JobPosting getJob() {
        return job;
    }

    public void setJob(JobPosting job) {
        this.job = job;
    }

    public List<String> getStudentSkills() {
        return studentSkills;
    }

    public void setStudentSkills(List<String> studentSkills) {
        this.studentSkills = studentSkills != null ? studentSkills : new ArrayList<>();
    }

    public List<String> getRequiredSkills() {
        return requiredSkills;
    }

    public void setRequiredSkills(List<String> requiredSkills) {
        this.requiredSkills = requiredSkills != null ? requiredSkills : new ArrayList<>();
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

    public double getMatchScore() {
        return matchScore;
    }

    public void setMatchScore(double matchScore) {
        this.matchScore = matchScore;
    }
}
