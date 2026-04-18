package com.ta.dto.student;

import java.util.List;

public class StudentApplicationCreateRequest {
    private String jobId;
    private List<String> selectedAttachmentIds;

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public List<String> getSelectedAttachmentIds() {
        return selectedAttachmentIds;
    }

    public void setSelectedAttachmentIds(List<String> selectedAttachmentIds) {
        this.selectedAttachmentIds = selectedAttachmentIds;
    }
}
