<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
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
    <form id="registerForm">
      <div class="field">
        <label>Role</label>
        <select id="role">
          <option value="student">Student</option>
          <option value="teacher">Teacher</option>
        </select>
      </div>
      <div class="field">
        <label>Full Name</label>
        <input id="name" type="text" />
      </div>
      <div class="field">
        <label>Email</label>
        <input id="email" type="email" />
      </div>
      <div class="field">
        <label>Password</label>
        <input id="password" type="password" />
      </div>

      <div id="studentOnly">
        <div class="field">
          <label>Student ID</label>
          <input id="studentId" type="text" />
        </div>
        <div class="field">
          <label>Programme</label>
          <input id="programme" type="text" />
        </div>
      </div>

      <button class="btn btn-primary" type="submit">Register</button>
      <a style="margin-left:12px" href="login.jsp">Back to Login</a>
      <div id="tip" class="notice"></div>
    </form>
  </div>
</div>
<script src="../assets/js/common.js"></script>
<script src="../assets/js/register.js"></script>
</body>
</html>
