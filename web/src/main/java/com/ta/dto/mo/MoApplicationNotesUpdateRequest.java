package com.ta.dto.mo;

/**
 * PATCH /api/mo/applications/notes
 */
public class MoApplicationNotesUpdateRequest {
    private String applicationId;
    private String evaluationNotes;

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getEvaluationNotes() {
        return evaluationNotes;
    }

    public void setEvaluationNotes(String evaluationNotes) {
        this.evaluationNotes = evaluationNotes;
    }
}
