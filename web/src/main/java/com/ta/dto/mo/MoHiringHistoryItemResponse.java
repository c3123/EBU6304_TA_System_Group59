package com.ta.dto.mo;

import java.util.ArrayList;
import java.util.List;

public class MoHiringHistoryItemResponse {
    private String action;
    private String submittedAt;
    private List<String> hiredStudentNames = new ArrayList<>();

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

    public List<String> getHiredStudentNames() {
        return hiredStudentNames;
    }

    public void setHiredStudentNames(List<String> hiredStudentNames) {
        this.hiredStudentNames = hiredStudentNames;
    }
}
