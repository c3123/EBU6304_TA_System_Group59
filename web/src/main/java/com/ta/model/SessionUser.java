package com.ta.model;

import java.io.Serializable;

public class SessionUser implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String id;
    private final String name;
    private final String email;
    private final String role;

    public SessionUser(String id, String name, String email, String role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public static SessionUser fromUser(User user) {
        return new SessionUser(user.getId(), user.getName(), user.getEmail(), user.getRole());
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public String getDashboardPath() {
        if ("admin".equalsIgnoreCase(role)) {
            return "/pages/admin.jsp";
        }
        if ("teacher".equalsIgnoreCase(role)) {
            return "/pages/teacher.jsp";
        }
        return "/pages/student.jsp";
    }
}
