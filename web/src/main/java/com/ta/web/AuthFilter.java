package com.ta.web;

import com.ta.model.SessionUser;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebFilter(filterName = "AuthFilter", urlPatterns = {"/pages/*", "/api/*"})
public class AuthFilter extends HttpFilter implements Filter {
    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        String servletPath = req.getServletPath();
        String pathInfo = req.getPathInfo();
        String path = buildPath(servletPath, pathInfo);

        if (isPublicPath(path)) {
            chain.doFilter(req, resp);
            return;
        }

        HttpSession session = req.getSession(false);
        SessionUser currentUser = session == null ? null : (SessionUser) session.getAttribute("currentUser");

        if (currentUser == null) {
            if (isApiRequest(path)) {
                resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Login required");
            } else {
                resp.sendRedirect(req.getContextPath() + "/pages/login.jsp?error=auth");
            }
            return;
        }

        if (!hasAccess(currentUser.getRole(), path)) {
            if (isApiRequest(path)) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
            } else {
                resp.sendRedirect(req.getContextPath() + dashboardPathFor(currentUser.getRole()));
            }
            return;
        }

        chain.doFilter(req, resp);
    }

    private String buildPath(String servletPath, String pathInfo) {
        if (pathInfo == null) {
            return servletPath;
        }
        return servletPath + pathInfo;
    }

    private boolean isPublicPath(String path) {
        return "/pages/login.jsp".equals(path) || "/pages/register.jsp".equals(path);
    }

    private boolean isApiRequest(String path) {
        return path.startsWith("/api/");
    }

    private boolean hasAccess(String role, String path) {
        String normalizedRole = role == null ? "" : role.trim().toLowerCase();

        if (path.startsWith("/api/mo/") || "/pages/teacher.jsp".equals(path) || "/pages/mo-applications.jsp".equals(path)) {
            return "teacher".equals(normalizedRole);
        }

        if (path.startsWith("/api/admin/") || "/pages/admin.jsp".equals(path)) {
            return "admin".equals(normalizedRole);
        }

        if ("/pages/student.jsp".equals(path)) {
            return "student".equals(normalizedRole);
        }

        return true;
    }

    private String dashboardPathFor(String role) {
        String normalizedRole = role == null ? "" : role.trim().toLowerCase();
        if ("admin".equals(normalizedRole)) {
            return "/pages/admin.jsp";
        }
        if ("teacher".equals(normalizedRole)) {
            return "/pages/teacher.jsp";
        }
        return "/pages/student.jsp";
    }
}
