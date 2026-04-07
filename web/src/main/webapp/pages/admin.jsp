<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <title>Admin Dashboard - TA Recruitment Platform</title>
  <link rel="stylesheet" href="../assets/css/main.css" />
</head>
<body>
<div class="app-shell">
  <div class="container">
    <div class="topbar">
      <strong>Admin Dashboard</strong>
      <div class="row">
        <a href="<%= request.getContextPath() %>/logout">Logout</a>
      </div>
    </div>

    <div class="grid" style="margin-bottom:16px">
      <div class="card"><div>Total Jobs</div><div id="statJobs" class="stat">0</div></div>
      <div class="card"><div>Total Users</div><div id="statUsers" class="stat">0</div></div>
      <div class="card"><div>Total Applications</div><div id="statApps" class="stat">0</div></div>
    </div>

    <div class="card" style="margin-bottom:16px">
      <h3>User Management</h3>
      <div class="table-wrap">
        <table>
          <thead><tr><th>Name</th><th>Email</th><th>Role</th><th>Action</th></tr></thead>
          <tbody id="adminUsersBody"></tbody>
        </table>
      </div>
    </div>

    <div class="card">
      <h3>Job Overview</h3>
      <div class="table-wrap">
        <table>
          <thead><tr><th>Code</th><th>Position</th><th>Teacher</th><th>Status</th><th>Recruitment</th><th>Slots</th><th>Action</th></tr></thead>
          <tbody id="adminJobsBody"></tbody>
        </table>
      </div>
      <p id="adminNotice" class="notice"></p>
    </div>
  </div>
</div>
<script src="../assets/js/common.js"></script>
<script src="../assets/js/admin.js"></script>
</body>
</html>
