package com.ta.dto.admin;

public class AdminWorkloadSettingsResponse {
    private Integer workloadThresholdHours;
    private String updatedAt;
    private boolean saved;

    public Integer getWorkloadThresholdHours() {
        return workloadThresholdHours;
    }

    public void setWorkloadThresholdHours(Integer workloadThresholdHours) {
        this.workloadThresholdHours = workloadThresholdHours;
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
