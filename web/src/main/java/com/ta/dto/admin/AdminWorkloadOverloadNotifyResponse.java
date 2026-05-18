package com.ta.dto.admin;

import java.util.ArrayList;
import java.util.List;

public class AdminWorkloadOverloadNotifyResponse {
    private int notifiedCount;
    private int thresholdHours;
    private List<String> studentIds = new ArrayList<>();

    public int getNotifiedCount() {
        return notifiedCount;
    }

    public void setNotifiedCount(int notifiedCount) {
        this.notifiedCount = notifiedCount;
    }

    public int getThresholdHours() {
        return thresholdHours;
    }

    public void setThresholdHours(int thresholdHours) {
        this.thresholdHours = thresholdHours;
    }

    public List<String> getStudentIds() {
        return studentIds;
    }

    public void setStudentIds(List<String> studentIds) {
        this.studentIds = studentIds == null ? new ArrayList<>() : studentIds;
    }
}
