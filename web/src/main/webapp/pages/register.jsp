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
  <title>Register - TA Recruitment Platform</title>
  <link rel="stylesheet" href="../assets/css/main.css" />
</head>
<body>
<div class="app-shell">
  <div class="form-card">
    <h2>Create Account</h2>
    <form id="registerForm" method="post" action="<%= request.getContextPath() %>/register">
      <div class="field">
        <label>Role</label>
        <select id="role" name="role">
          <option value="student">Student</option>
          <option value="teacher">Teacher</option>
        </select>
      </div>
      <div class="field">
        <label>Full Name</label>
        <input id="name" name="name" type="text" />
      </div>
      <div class="field">
        <label>Email</label>
        <input id="email" name="email" type="email" />
      </div>
      <div class="field">
        <label>Password</label>
        <input id="password" name="password" type="password" />
      </div>

      <div id="studentOnly">
        <div class="field">
          <label>Student ID</label>
          <input id="studentId" name="studentId" type="text" />
        </div>
        <div class="field">
          <label>Programme</label>
          <input id="programme" name="programme" type="text" />
        </div>
      </div>

      <button class="btn btn-primary" type="submit">Register</button>
      <a style="margin-left:12px" href="login.jsp">Back to Login</a>
      <div id="tip" class="notice" style="color:<%= messageColor %>;"><%= message %></div>
    </form>
  </div>
</div>
<script src="../assets/js/common.js"></script>
<script src="../assets/js/register.js"></script>
</body>
</html>
