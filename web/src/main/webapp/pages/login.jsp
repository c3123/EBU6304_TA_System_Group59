<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
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
    <form id="loginForm">
      <div class="field">
        <label>Email</label>
        <input id="email" type="email" placeholder="your.email@university.edu" />
      </div>
      <div class="field">
        <label>Password</label>
        <input id="password" type="password" placeholder="Enter your password" />
      </div>
      <button class="btn btn-primary" type="submit">Login</button>
      <a style="margin-left:12px" href="register.jsp">Create Account</a>
      <div id="tip" class="notice"></div>
    </form>

    <p class="notice">
      Demo accounts:<br/>
      student@demo.com / demo123<br/>
      teacher@demo.com / demo123<br/>
      admin@demo.com / demo123
    </p>
  </div>
</div>
<script src="../assets/js/common.js"></script>
<script src="../assets/js/login.js"></script>
</body>
</html>
