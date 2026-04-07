package com.ta.dto.mo;

/**
 * POST /api/mo/applications/status — update application decision (MO-owned jobs only).
 */
public class MoApplicationStatusUpdateRequest {
    private String applicationId;
    /** shortlisted | hired | rejected */
    private String status;

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
