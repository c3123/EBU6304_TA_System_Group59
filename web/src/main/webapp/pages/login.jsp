<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.ta.model.SessionUser" %>
<%
  SessionUser currentUser = (SessionUser) session.getAttribute("currentUser");
  if (currentUser != null) {
    response.sendRedirect(request.getContextPath() + currentUser.getDashboardPath());
    return;
  }

  String error = request.getParameter("error");
  String success = request.getParameter("success");
  String message = "";
  String messageColor = "#475569";

  if ("missing".equals(error)) {
    message = "Please enter your user ID or email and password.";
    messageColor = "#dc2626";
  } else if ("auth".equals(error)) {
    message = "Please log in to continue.";
    messageColor = "#dc2626";
  } else if ("invalid".equals(error)) {
    message = "Invalid ID or Password.";
    messageColor = "#dc2626";
  } else if ("registered".equals(success)) {
    message = "Registration successful. Please sign in with your new account.";
    messageColor = "#16a34a";
  } else if ("logout".equals(success)) {
    message = "You have logged out successfully.";
    messageColor = "#16a34a";
  }
%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>Login - TA Recruitment Platform</title>
  <link rel="stylesheet" href="../assets/css/main.css" />
</head>
<body class="auth-page">
  <div class="auth-split-wrapper">
    <div class="auth-split-banner">
      <h1>TA Recruitment</h1>
      <p>Manage your teaching assistant applications and opportunities efficiently with our unified platform.</p>
      
      <div class="auth-feature-list">
        <div class="auth-feature">
          <div class="auth-feature-icon">✨</div>
          <div class="auth-feature-text">
            <h3>Easy Applications</h3>
            <p>Submit and track effortlessly</p>
          </div>
        </div>
        <div class="auth-feature">
          <div class="auth-feature-icon">🔔</div>
          <div class="auth-feature-text">
            <h3>Real-time Updates</h3>
            <p>Get instant notifications</p>
          </div>
        </div>
      </div>
    </div>
    
    <div class="auth-split-form">
      <h2>Sign In</h2>
      <form method="post" action="<%= request.getContextPath() %>/login">
        <div class="field">
          <label>User ID or Email</label>
          <input name="identifier" type="text" placeholder="stu001 or your.email@university.edu" />
        </div>
        <div class="field">
          <label>Password</label>
          <input name="password" type="password" placeholder="Enter your password" />
        </div>
        <div class="auth-btn-row">
          <button class="btn btn-primary btn-large" style="width:100%" type="submit">Login</button>
        </div>
        <div class="auth-links">
          Don't have an account? <a href="register.jsp">Create Account</a>
        </div>
        <div class="notice" style="color:<%= messageColor %>; text-align:center;"><%= message %></div>
      </form>

      <div class="notice" style="text-align:center; margin-top:30px; font-family:monospace; background:rgba(219,234,254,0.3); padding:10px; border-radius:8px; border: 1px dashed rgba(147,197,253,0.5);">
        Demo accounts:<br/>
        student@demo.com / demo123<br/>
        teacher@demo.com / demo123<br/>
        admin@demo.com / demo123
      </div>
    </div>
  </div>
</body>
</html>
