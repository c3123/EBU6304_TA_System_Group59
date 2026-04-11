<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.ta.model.SessionUser" %>
<%
  SessionUser currentUser = (SessionUser) session.getAttribute("currentUser");
  if (currentUser != null) {
    response.sendRedirect(request.getContextPath() + currentUser.getDashboardPath());
    return;
  }

  String error = request.getParameter("error");
  String message = "";
  String messageColor = "#475569";

  if ("missing".equals(error)) {
    message = "Please complete all required fields.";
    messageColor = "#dc2626";
  } else if ("student".equals(error)) {
    message = "Student role requires Student ID and Programme.";
    messageColor = "#dc2626";
  } else if ("email".equals(error)) {
    message = "This email is already registered.";
    messageColor = "#dc2626";
  } else if ("role".equals(error)) {
    message = "Only student and teacher accounts can be registered here.";
    messageColor = "#dc2626";
  }
%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>Register - TA Recruitment Platform</title>
  <link rel="stylesheet" href="../assets/css/main.css" />
</head>
<body class="auth-page">
  <div class="auth-split-wrapper">
    <div class="auth-split-banner">
      <h1>Join Us</h1>
      <p>Create your account and start exploring teaching assistant opportunities today.</p>
      
      <div class="auth-feature-list">
        <div class="auth-feature">
          <div class="auth-feature-icon">🚀</div>
          <div class="auth-feature-text">
            <h3>Quick Setup</h3>
            <p>Ready to go in minutes</p>
          </div>
        </div>
        <div class="auth-feature">
          <div class="auth-feature-icon">🛡️</div>
          <div class="auth-feature-text">
            <h3>Secure Profile</h3>
            <p>Your data is protected</p>
          </div>
        </div>
      </div>
    </div>
    
    <div class="auth-split-form">
      <h2>Create Account</h2>
      <form id="registerForm" method="post" action="<%= request.getContextPath() %>/register">
        <div class="field">
          <label>Role</label>
          <select id="role" name="role">
            <option value="student">Student</option>
            <option value="teacher">Teacher</option>
          </select>
        </div>

        <div style="display:grid; grid-template-columns:1fr 1fr; gap:16px;">
          <div class="field">
            <label>Full Name</label>
            <input id="name" name="name" type="text" placeholder="John Doe" />
          </div>
          <div class="field">
            <label>Email</label>
            <input id="email" name="email" type="email" placeholder="your@email.edu" />
          </div>
        </div>
        
        <div class="field">
          <label>Password</label>
          <input id="password" name="password" type="password" placeholder="Create a strong password" />
        </div>

        <div id="studentOnly" style="display:grid; grid-template-columns:1fr 1fr; gap:16px;">
          <div class="field">
            <label>Student ID</label>
            <input id="studentId" name="studentId" type="text" placeholder="e.g. 201234567" />
          </div>
          <div class="field">
            <label>Programme</label>
            <input id="programme" name="programme" type="text" placeholder="e.g. AI & CS" />
          </div>
        </div>

        <div class="auth-btn-row">
          <button class="btn btn-primary btn-large" style="width:100%" type="submit">Create Account</button>
        </div>
        
        <div class="auth-links">
          Already have an account? <a href="login.jsp">Sign in instead</a>
        </div>
        <div id="tip" class="notice" style="color:<%= messageColor %>; text-align:center;"><%= message %></div>
      </form>
    </div>
  </div>
<script src="../assets/js/common.js"></script>
<script src="../assets/js/register.js"></script>
</body>
</html>
