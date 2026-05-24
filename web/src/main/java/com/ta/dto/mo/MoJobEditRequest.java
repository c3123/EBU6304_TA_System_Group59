package com.ta.dto.mo;

public class MoJobEditRequest {
    private String courseName;
    private String department;
    private Integer plannedCount;
    private Integer hourMin;
    private Integer hourMax;
    private String requirements;
    private String schedule;
    private String location;
    private String deadline;

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public Integer getPlannedCount() {
        return plannedCount;
    }

    public void setPlannedCount(Integer plannedCount) {
        this.plannedCount = plannedCount;
    }

    public Integer getHourMin() {
        return hourMin;
    }

    public void setHourMin(Integer hourMin) {
        this.hourMin = hourMin;
    }

    public Integer getHourMax() {
        return hourMax;
    }

    public void setHourMax(Integer hourMax) {
        this.hourMax = hourMax;
    }

    public String getRequirements() {
        return requirements;
    }

    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }

    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }
}
