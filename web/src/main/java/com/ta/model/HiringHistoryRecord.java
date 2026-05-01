package com.ta.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Persistent final hiring history and reopen audit record.
 */
public class HiringHistoryRecord {
    private String id;
    private String jobId;
    private String moId;
    /** finalize | reopen | manual_hire */
    private String action;
    private String submittedAt;
    private List<String> hiredApplicationIds = new ArrayList<>();
    private List<String> hiredStudentNames = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getMoId() {
        return moId;
    }

    public void setMoId(String moId) {
        this.moId = moId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(String submittedAt) {
        this.submittedAt = submittedAt;
    }

    public List<String> getHiredApplicationIds() {
        return hiredApplicationIds;
    }

    public void setHiredApplicationIds(List<String> hiredApplicationIds) {
        this.hiredApplicationIds = hiredApplicationIds == null ? new ArrayList<>() : hiredApplicationIds;
    }

    public List<String> getHiredStudentNames() {
        return hiredStudentNames;
    }

    public void setHiredStudentNames(List<String> hiredStudentNames) {
        this.hiredStudentNames = hiredStudentNames == null ? new ArrayList<>() : hiredStudentNames;
    }
}
