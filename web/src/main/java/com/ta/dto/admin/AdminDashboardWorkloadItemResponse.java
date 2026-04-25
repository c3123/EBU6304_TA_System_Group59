package com.ta.dto.admin;

public class AdminDashboardWorkloadItemResponse {
    private String studentId;
    private String studentName;
    private int hiredCount;
    private int weeklyHours;
    private int thresholdHours;
    private boolean warning;

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public int getHiredCount() {
        return hiredCount;
    }

    public void setHiredCount(int hiredCount) {
        this.hiredCount = hiredCount;
    }

    public int getWeeklyHours() {
        return weeklyHours;
    }

    public void setWeeklyHours(int weeklyHours) {
        this.weeklyHours = weeklyHours;
    }

    public int getThresholdHours() {
        return thresholdHours;
    }

    public void setThresholdHours(int thresholdHours) {
        this.thresholdHours = thresholdHours;
    }

    public boolean isWarning() {
        return warning;
    }

    public void setWarning(boolean warning) {
        this.warning = warning;
    }
}
