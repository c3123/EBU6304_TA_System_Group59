package com.ta.web;

import com.ta.model.StudentProfile;
import com.ta.model.User;
import com.ta.util.JsonUtility;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "RegisterServlet", urlPatterns = {"/register"})
public class RegisterServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.sendRedirect(req.getContextPath() + "/pages/register.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String role = trim(req.getParameter("role")).toLowerCase();
        String name = trim(req.getParameter("name"));
        String email = trim(req.getParameter("email"));
        String password = trim(req.getParameter("password"));
        String studentId = trim(req.getParameter("studentId"));
        String programme = trim(req.getParameter("programme"));

        if (!"student".equals(role) && !"teacher".equals(role)) {
            resp.sendRedirect(req.getContextPath() + "/pages/register.jsp?error=role");
            return;
        }

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/pages/register.jsp?error=missing");
            return;
        }

        if ("student".equals(role) && (studentId.isEmpty() || programme.isEmpty())) {
            resp.sendRedirect(req.getContextPath() + "/pages/register.jsp?error=student");
            return;
        }

        List<User> users = JsonUtility.loadUsers(getServletContext());
        boolean emailExists = users.stream().anyMatch(user -> email.equalsIgnoreCase(user.getEmail()));
        if (emailExists) {
            resp.sendRedirect(req.getContextPath() + "/pages/register.jsp?error=email");
            return;
        }

        String userId = nextUserId(users, role);
        User user = new User();
        user.setId(userId);
        user.setName(name);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole(role);
        user.setStudentId("student".equals(role) ? studentId : "");
        user.setProgramme("student".equals(role) ? programme : "");
        users.add(user);
        JsonUtility.saveUsers(getServletContext(), users);

        if ("student".equals(role)) {
            List<StudentProfile> students = JsonUtility.loadStudents(getServletContext());
            StudentProfile profile = new StudentProfile();
            profile.setUserId(userId);
            profile.setStudentId(studentId);
            profile.setName(name);
            profile.setEmail(email);
            profile.setProgramme(programme);
            students.add(profile);
            JsonUtility.saveStudents(getServletContext(), students);
        }

        resp.sendRedirect(req.getContextPath() + "/pages/login.jsp?success=registered");
    }

    private String nextUserId(List<User> users, String role) {
        String prefix = "student".equals(role) ? "stu" : "mo";
        int max = 0;
        for (User user : users) {
            String id = user.getId();
            if (id == null || !id.toLowerCase().startsWith(prefix)) {
                continue;
            }
            String suffix = id.substring(prefix.length());
            try {
                max = Math.max(max, Integer.parseInt(suffix));
            } catch (NumberFormatException ignored) {
                // Ignore malformed legacy IDs and continue with the next one.
            }
        }
        return prefix + String.format("%03d", max + 1);
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
