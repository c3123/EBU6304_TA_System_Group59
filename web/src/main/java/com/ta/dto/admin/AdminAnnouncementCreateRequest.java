package com.ta.dto.admin;

/**
 * Admin broadcast announcement to students, teachers, or both.
 */
public class AdminAnnouncementCreateRequest {
    private String title;
    private String body;
    /** student | teacher | all */
    private String targetRole;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTargetRole() {
        return targetRole;
    }

    public void setTargetRole(String targetRole) {
        this.targetRole = targetRole;
    }
}
