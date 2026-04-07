package com.ta.dto.mo;

import java.util.ArrayList;
import java.util.List;

public class MoHiringHistoryResponse {
    private String jobId;
    private String jobName;
    private List<MoHiringHistoryItemResponse> items = new ArrayList<>();

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public List<MoHiringHistoryItemResponse> getItems() {
        return items;
    }

    public void setItems(List<MoHiringHistoryItemResponse> items) {
        this.items = items;
    }
}
