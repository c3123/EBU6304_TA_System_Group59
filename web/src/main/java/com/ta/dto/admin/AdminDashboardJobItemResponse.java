package com.ta.dto.admin;

public class AdminDashboardJobItemResponse {
    private String id;
    private String moduleCode;
    private String title;
    private String teacherName;
    private String department;
    private String status;
    private Integer positions;
    private Integer applicantCount;
    private Integer hiredCount;
    private Integer weeklyHours;
    private String deadline;
    private String publishedAt;
    private String createdAt;
    private String requirements;
    private String schedule;
    private String location;
    private Boolean recruitmentClosed;
    private String closedAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getPositions() {
        return positions;
    }

    public void setPositions(Integer positions) {
        this.positions = positions;
    }

    public Integer getApplicantCount() {
        return applicantCount;
    }

    public void setApplicantCount(Integer applicantCount) {
        this.applicantCount = applicantCount;
    }

    public Integer getHiredCount() {
        return hiredCount;
    }

    public void setHiredCount(Integer hiredCount) {
        this.hiredCount = hiredCount;
    }

    public Integer getWeeklyHours() {
        return weeklyHours;
    }

    public void setWeeklyHours(Integer weeklyHours) {
        this.weeklyHours = weeklyHours;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public String getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(String publishedAt) {
        this.publishedAt = publishedAt;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getRequirements() {
        return requirements;
    }

    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }

    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
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
