package com.ta.model;

public class StudentProfile {
    private String userId;
    private String studentId;
    private String name;
    private String email;
    private String programme;

    public StudentProfile() {
    }

    public StudentProfile(String userId, String studentId, String name, String email, String programme) {
        this.userId = userId;
        this.studentId = studentId;
        this.name = name;
        this.email = email;
        this.programme = programme;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProgramme() {
        return programme;
    }

    public void setProgramme(String programme) {
        this.programme = programme;
    }
}
