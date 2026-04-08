package com.ta.web;

import com.ta.model.SessionUser;
import com.ta.model.User;
import com.ta.util.JsonUtility;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "LoginServlet", urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.sendRedirect(req.getContextPath() + "/pages/login.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String identifier = trim(req.getParameter("identifier"));
        String password = trim(req.getParameter("password"));

        if (identifier.isEmpty() || password.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/pages/login.jsp?error=missing");
            return;
        }

        List<User> users = JsonUtility.loadUsers(getServletContext());
        User matchedUser = users.stream()
                .filter(user -> user.matchesIdentifier(identifier) && password.equals(user.getPassword()))
                .findFirst()
                .orElse(null);

        if (matchedUser == null) {
            resp.sendRedirect(req.getContextPath() + "/pages/login.jsp?error=invalid");
            return;
        }

        HttpSession oldSession = req.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }

        HttpSession session = req.getSession(true);
        SessionUser sessionUser = SessionUser.fromUser(matchedUser);
        session.setAttribute("currentUser", sessionUser);
        session.setAttribute("userId", matchedUser.getId());
        session.setAttribute("userRole", matchedUser.getRole());

        if ("teacher".equalsIgnoreCase(matchedUser.getRole())) {
            session.setAttribute("moId", matchedUser.getId());
        }

        resp.sendRedirect(req.getContextPath() + sessionUser.getDashboardPath());
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
