package com.ta.dto.admin;

public class AdminWorkloadSettingsResponse {
    private Integer workloadThresholdHours;
    private Integer workloadNormalPercent;
    private Integer workloadWarningPercent;
    private String updatedAt;
    private boolean saved;

    public Integer getWorkloadThresholdHours() {
        return workloadThresholdHours;
    }

    public void setWorkloadThresholdHours(Integer workloadThresholdHours) {
        this.workloadThresholdHours = workloadThresholdHours;
    }

    public Integer getWorkloadNormalPercent() {
        return workloadNormalPercent;
    }

    public void setWorkloadNormalPercent(Integer workloadNormalPercent) {
        this.workloadNormalPercent = workloadNormalPercent;
    }

    public Integer getWorkloadWarningPercent() {
        return workloadWarningPercent;
    }

    public void setWorkloadWarningPercent(Integer workloadWarningPercent) {
        this.workloadWarningPercent = workloadWarningPercent;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isSaved() {
        return saved;
    }

    public void setSaved(boolean saved) {
        this.saved = saved;
    }
}
