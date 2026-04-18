package com.ta.dto.mo;

/**
 * PATCH /api/mo/applications/feedback
 */
public class MoApplicationFeedbackUpdateRequest {
    private String applicationId;
    private String decisionFeedback;

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getDecisionFeedback() {
        return decisionFeedback;
    }

    public void setDecisionFeedback(String decisionFeedback) {
        this.decisionFeedback = decisionFeedback;
    }
}
