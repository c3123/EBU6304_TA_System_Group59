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
  <title>Login - TA Recruitment Platform</title>
  <link rel="stylesheet" href="../assets/css/main.css" />
</head>
<body>
<div class="app-shell">
  <div class="form-card">
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
      <button class="btn btn-primary" type="submit">Login</button>
      <a style="margin-left:12px" href="register.jsp">Create Account</a>
      <div class="notice" style="color:<%= messageColor %>;"><%= message %></div>
    </form>

    <p class="notice">
      Demo accounts:<br/>
      student@demo.com / demo123<br/>
      teacher@demo.com / demo123<br/>
      admin@demo.com / demo123
    </p>
  </div>
</div>
</body>
</html>
