package com.ta.model;

import com.google.gson.annotations.SerializedName;

/**
 * Job posting / demand model used by MO iteration 1.
 *
 * Unified field contract:
 * 1) approvalStatus: pending | approved | rejected
 * 2) published: true after MO publishes approved demand
 * 3) withdrawn: true when MO withdraws (only if no active applications)
 * 4) location: online | offline (filled when publishing)
 * 5) deadline: locked after published=true
 */
public class JobPosting {
    private String id;
    private String teacherId;
    private String teacherName;
    private String moduleCode;
    private String title;
    private int hours;
    private int positions;
    private String status;
    private String deadline;

    // MO iteration 1 agreed fields
    private Integer hourMin;
    private Integer hourMax;
    private String department;
    private String schedule;
    private String approvalStatus;
    private Boolean published;
    private Boolean withdrawn;
    private String location;
    @SerializedName(value = "requirements", alternate = {"requirement"})
    private String requirements;
    private String createdAt;
    private String updatedAt;
    /** MO_04: set true after final hiring confirmation. */
    private Boolean recruitmentClosed;
    /** ISO timestamp when recruitment is closed. */
    private String closedAt;

    public JobPosting() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(String teacherId) {
        this.teacherId = teacherId;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public String getModuleCode() {
        return moduleCode;
    }

    public void setModuleCode(String moduleCode) {
        this.moduleCode = moduleCode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getHours() {
        return hours;
    }

    public void setHours(int hours) {
        this.hours = hours;
    }

    public int getPositions() {
        return positions;
    }

    public void setPositions(int positions) {
        this.positions = positions;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public Integer getHourMin() {
        return hourMin;
    }

    public void setHourMin(Integer hourMin) {
        this.hourMin = hourMin;
    }

    public Integer getHourMax() {
        return hourMax;
    }

    public void setHourMax(Integer hourMax) {
        this.hourMax = hourMax;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    public String getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(String approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public Boolean getPublished() {
        return published;
    }

    public void setPublished(Boolean published) {
        this.published = published;
    }

    public Boolean getWithdrawn() {
        return withdrawn;
    }

    public void setWithdrawn(Boolean withdrawn) {
        this.withdrawn = withdrawn;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getRequirements() {
        return requirements;
    }

    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Boolean getRecruitmentClosed() {
        return recruitmentClosed;
    }

    public void setRecruitmentClosed(Boolean recruitmentClosed) {
        this.recruitmentClosed = recruitmentClosed;
    }

    public String getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(String closedAt) {
        this.closedAt = closedAt;
    }
}
