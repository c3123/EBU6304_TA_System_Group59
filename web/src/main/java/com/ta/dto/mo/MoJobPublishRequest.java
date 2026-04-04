package com.ta.dto.mo;

/**
 * POST /api/mo/jobs/{jobId}/publish request body.
 */
public class MoJobPublishRequest {
    private String location;
    private String requirements;
    private String deadline;

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getRequirements() {
        return requirements;
    }

    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }
}
