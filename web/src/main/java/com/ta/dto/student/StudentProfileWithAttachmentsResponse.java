package com.ta.dto.student;

import com.ta.model.Attachment;
import java.util.List;

public class StudentProfileWithAttachmentsResponse {
    private String userId;
    private String name;
    private String email;
    private String studentId;
    private String programme;
    private List<Attachment> attachments;

    public StudentProfileWithAttachmentsResponse() {
    }

    public StudentProfileWithAttachmentsResponse(String userId, String name, String email, String studentId, String programme, List<Attachment> attachments) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.studentId = studentId;
        this.programme = programme;
        this.attachments = attachments;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getProgramme() {
        return programme;
    }

    public void setProgramme(String programme) {
        this.programme = programme;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }
}
