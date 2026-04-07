package com.ta.dto.mo;

/**
 * Demand list item for MO progress tracking.
 */
public class MoDemandItemResponse {
    private String jobId;
    private String moId;
    private String courseName;
    private Integer plannedCount;
    private Integer hourMin;
    private Integer hourMax;
    /** Weekly hours from legacy job.hours when hourMin/hourMax absent */
    private Integer hours;
    private String approvalStatus;
    private Boolean published;
    private Boolean withdrawn;
    private Boolean recruitmentClosed;
    private String closedAt;
    private String createdAt;
    private String updatedAt;

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getMoId() {
        return moId;
    }

    public void setMoId(String moId) {
        this.moId = moId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public Integer getPlannedCount() {
        return plannedCount;
    }

    public void setPlannedCount(Integer plannedCount) {
        this.plannedCount = plannedCount;
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

    public Integer getHours() {
        return hours;
    }

    public void setHours(Integer hours) {
        this.hours = hours;
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
}
