package com.ta.dto.admin;

public class AdminWorkloadSettingsRequest {
    private Integer workloadThresholdHours;

    public Integer getWorkloadThresholdHours() {
        return workloadThresholdHours;
    }

    public void setWorkloadThresholdHours(Integer workloadThresholdHours) {
        this.workloadThresholdHours = workloadThresholdHours;
    }
}
