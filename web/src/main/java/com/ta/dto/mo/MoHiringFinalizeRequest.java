package com.ta.dto.mo;

import java.util.ArrayList;
import java.util.List;

public class MoHiringFinalizeRequest {
    private String jobId;
    private List<String> hiredApplicationIds = new ArrayList<>();

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public List<String> getHiredApplicationIds() {
        return hiredApplicationIds;
    }

    public void setHiredApplicationIds(List<String> hiredApplicationIds) {
        this.hiredApplicationIds = hiredApplicationIds == null ? new ArrayList<>() : hiredApplicationIds;
    }
}
