package com.ta.dto.admin;

public class AdminWorkloadSettingsRequest {
    private Integer workloadThresholdHours;
    private Integer workloadNormalPercent;
    private Integer workloadWarningPercent;

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
}
