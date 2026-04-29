package com.ta.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Application record model for MO iteration 1.
 *
 * Contract:
 * 1) appliedAt is the canonical application timestamp field.
 * 2) status may be pending | viewed | shortlisted | hired | rejected.
 * 3) active=true means visible in MO list; student withdraw sets active=false.
 * 4) selectedAttachmentIds: list of attachment IDs included in this application
 * 5) evaluationNotes: MO-only private notes (MO_05); persisted in applications.json
 * 6) decisionFeedback: MO/Admin-only short reason for hired/shortlisted/rejected (MO_10); max 200 chars enforced in service
 */
public class ApplicationRecord {
    private String id;
    private String jobId;
    private String studentId;
    private String studentName;
    private String studentNo;
    private String courseGrade;
    private String appliedAt;
    private String status;
    private boolean active;
    private List<String> selectedAttachmentIds;
    /** MO private evaluation notes (optional; Gson omits if null in older JSON files) */
    private String evaluationNotes;
    /** Hiring/rejection feedback visible to MO and Admin only */
    private String decisionFeedback;

    public ApplicationRecord() {
        this.selectedAttachmentIds = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<String> getSelectedAttachmentIds() {
        return selectedAttachmentIds;
    }

    public void setSelectedAttachmentIds(List<String> selectedAttachmentIds) {
        this.selectedAttachmentIds = selectedAttachmentIds != null ? selectedAttachmentIds : new ArrayList<>();
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
}
