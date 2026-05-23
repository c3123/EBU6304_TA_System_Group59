package com.ta.model;

public class SystemSettings {
    private Integer workloadThresholdHours;
    private String updatedAt;

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
}
