package com.ta.dto.mo;

public class MoHiredStudentDismissRequest {
    private String applicationId;
    private String reason;

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
