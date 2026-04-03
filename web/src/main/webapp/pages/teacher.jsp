<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <title>MO Portal - TA Recruitment Platform</title>
  <link rel="stylesheet" href="../assets/css/main.css" />
</head>
<body>
<div class="app-shell">
  <div class="container">
    <div class="topbar">
      <strong>Module Organiser Portal</strong>
      <div class="row">
        <a href="login.jsp">Logout</a>
      </div>
    </div>

    <div class="grid">
      <div class="card">
        <h3>My Jobs</h3>
        <p class="desc">Job posting and lifecycle management page.</p>
        <button class="btn btn-outline" type="button" onclick="alert('My Jobs page is under integration.')">Open My Jobs</button>
      </div>

      <div class="card">
        <h3>Applicants</h3>
        <p class="desc">View active applicants and auto-track pending/viewed status.</p>
        <a class="btn btn-primary" href="mo-applications.jsp">Open Applicant Management</a>
      </div>
    </div>
  </div>
</div>
</body>
</html>
