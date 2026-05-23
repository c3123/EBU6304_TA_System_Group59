package com.ta.web;

import com.ta.model.SessionUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthFilterTest {

    private AuthFilter filter;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain chain;

    @BeforeEach
    void setUp() {
        filter = new AuthFilter();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        chain = mock(FilterChain.class);
        when(request.getContextPath()).thenReturn("/web");
    }

    @Test
    void apiWithoutLogin_returns401() throws Exception {
        when(request.getServletPath()).thenReturn("/api/admin/dashboard");
        when(request.getSession(false)).thenReturn(null);

        filter.doFilter(request, response, chain);

        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Login required");
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void wrongRoleForAdminApi_returns403() throws Exception {
        when(request.getServletPath()).thenReturn("/api/admin/dashboard");
        HttpSession session = session("student");
        when(request.getSession(false)).thenReturn(session);

        filter.doFilter(request, response, chain);

        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void wrongRoleForMoApi_returns403() throws Exception {
        when(request.getServletPath()).thenReturn("/api/mo/applications");
        HttpSession session = session("student");
        when(request.getSession(false)).thenReturn(session);

        filter.doFilter(request, response, chain);

        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void wrongRoleForStudentApi_returns403() throws Exception {
        when(request.getServletPath()).thenReturn("/api/student/profile");
        HttpSession session = session("teacher");
        when(request.getSession(false)).thenReturn(session);

        filter.doFilter(request, response, chain);

        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void studentPageRequiresStudentRole() throws Exception {
        when(request.getServletPath()).thenReturn("/pages/student.jsp");
        HttpSession session = session("teacher");
        when(request.getSession(false)).thenReturn(session);

        filter.doFilter(request, response, chain);

        verify(response).sendRedirect("/web/pages/teacher.jsp");
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void correctAdminRole_allowsAdminApi() throws Exception {
        when(request.getServletPath()).thenReturn("/api/admin/dashboard");
        HttpSession session = session("admin");
        when(request.getSession(false)).thenReturn(session);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    void publicLoginPage_allowsWithoutSession() throws Exception {
        when(request.getServletPath()).thenReturn("/pages/login.jsp");

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    private HttpSession session(String role) {
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("currentUser"))
                .thenReturn(new SessionUser(role + "_id", "Name", role + "@demo.test", role));
        return session;
    }
}
