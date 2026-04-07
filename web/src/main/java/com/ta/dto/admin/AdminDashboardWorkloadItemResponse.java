package com.ta.dto.admin;

public class AdminDashboardWorkloadItemResponse {
    private String studentId;
    private String studentName;
    private int hiredCount;
    private int weeklyHours;

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
}
