package com.ta.dto.mo;

public class MoHiringStateItemResponse {
    private String jobId;
    private Boolean recruitmentClosed;
    private String closedAt;

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public Boolean getRecruitmentClosed() {
        return recruitmentClosed;
    }

    public void setRecruitmentClosed(Boolean recruitmentClosed) {
        this.recruitmentClosed = recruitmentClosed;
    }

    public String getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(String closedAt) {
        this.closedAt = closedAt;
    }
}
