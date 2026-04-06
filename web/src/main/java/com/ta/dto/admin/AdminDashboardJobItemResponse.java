package com.ta.dto.admin;

public class AdminDashboardJobItemResponse {
    private String id;
    private String moduleCode;
    private String title;
    private String teacherName;
    private String status;
    private Integer positions;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getModuleCode() {
        return moduleCode;
    }

    public void setModuleCode(String moduleCode) {
        this.moduleCode = moduleCode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getPositions() {
        return positions;
    }

    public void setPositions(Integer positions) {
        this.positions = positions;
    }
}
