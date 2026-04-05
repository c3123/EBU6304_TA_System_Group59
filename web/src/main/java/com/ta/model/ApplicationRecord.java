package com.ta.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Application record model for MO iteration 1.
 *
 * Contract:
 * 1) status: pending | viewed
 * 2) active=true means visible in MO list; student withdraw sets active=false.
 * 3) selectedAttachmentIds: list of attachment IDs included in this application
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
}
