package com.ta.dto.admin;

public class AdminDashboardWorkloadJobResponse {
    private String applicationId;
    private String jobId;
    private String moduleCode;
    private String title;
    private int weeklyHours;
    /** ISO timestamp: from hiring_history when available, else application appliedAt */
    private String hiredAt;

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

    public int getWeeklyHours() {
        return weeklyHours;
    }

    public void setWeeklyHours(int weeklyHours) {
        this.weeklyHours = weeklyHours;
    }

    public String getHiredAt() {
        return hiredAt;
    }

    public void setHiredAt(String hiredAt) {
        this.hiredAt = hiredAt;
    }
}
