package com.ta.model;

public class User {
    private String id;
    private String name;
    private String email;
    private String password;
    private String role;
    private String studentId;
    private String programme;

    public User() {
    }

    public User(String id, String name, String email, String password, String role, String studentId, String programme) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.studentId = studentId;
        this.programme = programme;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
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

    public boolean matchesIdentifier(String identifier) {
        if (identifier == null) {
            return false;
        }

        return identifier.equalsIgnoreCase(email) || identifier.equalsIgnoreCase(id);
    }

    public boolean isStudent() {
        return "student".equalsIgnoreCase(role);
    }
}
