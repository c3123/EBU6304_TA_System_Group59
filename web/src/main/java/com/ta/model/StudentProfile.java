package com.ta.model;

import java.util.ArrayList;
import java.util.List;

public class StudentProfile {
    private String userId;
    private String studentId;
    private String name;
    private String email;
    private String programme;
    private String skills;
    private String experience;
    private List<Attachment> attachments;

    public StudentProfile() {
        this.attachments = new ArrayList<>();
    }

    public StudentProfile(String userId, String studentId, String name, String email, String programme) {
        this.userId = userId;
        this.studentId = studentId;
        this.name = name;
        this.email = email;
        this.programme = programme;
        this.skills = "";
        this.experience = "";
        this.attachments = new ArrayList<>();
    }

    public StudentProfile(String userId, String studentId, String name, String email, String programme, String skills, String experience, List<Attachment> attachments) {
        this.userId = userId;
        this.studentId = studentId;
        this.name = name;
        this.email = email;
        this.programme = programme;
        this.skills = skills;
        this.experience = experience;
        this.attachments = attachments != null ? attachments : new ArrayList<>();
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

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments != null ? attachments : new ArrayList<>();
    }
}
