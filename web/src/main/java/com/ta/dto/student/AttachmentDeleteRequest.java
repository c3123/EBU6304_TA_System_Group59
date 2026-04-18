package com.ta.dto.student;

public class AttachmentDeleteRequest {
    private String attachmentId;

    public AttachmentDeleteRequest() {
    }

    public AttachmentDeleteRequest(String attachmentId) {
        this.attachmentId = attachmentId;
    }

    public String getAttachmentId() {
        return attachmentId;
    }

    public void setAttachmentId(String attachmentId) {
        this.attachmentId = attachmentId;
    }
}
