package com.ta.model;

public class SystemSettings {
    private Integer workloadThresholdHours;
    /** Minimum percent of threshold hours for Normal level (Low is below this). */
    private Integer workloadNormalPercent;
    /** Minimum percent of threshold hours for Warning level (below overload). */
    private Integer workloadWarningPercent;
    private String updatedAt;

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
}
