<%@ page import="com.ta.model.SessionUser" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
  SessionUser currentUser = (SessionUser) session.getAttribute("currentUser");
  String currentUserId = currentUser == null ? "" : currentUser.getId();
  String currentUserName = currentUser == null ? "Admin User" : currentUser.getName();
%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <title>Admin Dashboard - TA Recruitment Platform</title>
  <link rel="stylesheet" href="../assets/css/main.css" />
</head>
<body>
<div class="admin-portal" data-current-user-id="<%= currentUserId %>" data-current-user-name="<%= currentUserName %>">
  <header class="admin-portal-header">
    <div class="admin-portal-header-inner">
      <div class="admin-portal-brand">
        <div class="admin-portal-icon" aria-hidden="true">
          <svg viewBox="0 0 24 24" fill="none">
            <rect x="3" y="4" width="18" height="16" rx="2" stroke="currentColor" stroke-width="1.8"></rect>
            <path d="M8 14L10.5 11.5L13 13.5L16.5 9.5" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"></path>
          </svg>
        </div>
        <div>
          <h1>Administrator Portal</h1>
          <p id="adminSubTitle">Welcome, <%= currentUserName %></p>
        </div>
      </div>
      <a class="admin-btn-logout" href="<%= request.getContextPath() %>/logout">Logout</a>
    </div>
  </header>

  <main class="admin-portal-main">
    <nav class="admin-tabs" role="tablist" aria-label="Admin sections">
      <button class="admin-tab active" data-admin-tab="overview" role="tab" aria-selected="true">System Overview</button>
      <button class="admin-tab" data-admin-tab="workload" role="tab" aria-selected="false">Workload</button>
      <button class="admin-tab" data-admin-tab="users" role="tab" aria-selected="false">Users</button>
      <button class="admin-tab" data-admin-tab="jobs" role="tab" aria-selected="false">Jobs</button>
      <button class="admin-tab" data-admin-tab="account" role="tab" aria-selected="false">My Account</button>
    </nav>

    <section class="admin-panel" data-admin-panel="overview">
      <div class="admin-headline">
        <h2 class="admin-section-title">System Overview</h2>
      </div>
      <div class="admin-stats-grid">
        <article class="admin-stat-card">
          <p class="admin-stat-label">Total Jobs</p>
          <p id="statJobs" class="admin-stat-value">0</p>
          <p class="admin-stat-sub">All published positions</p>
        </article>
        <article class="admin-stat-card">
          <p class="admin-stat-label">Total Users</p>
          <p id="statUsers" class="admin-stat-value">0</p>
          <p class="admin-stat-sub">Students + Teachers + Admins</p>
        </article>
        <article class="admin-stat-card">
          <p class="admin-stat-label">Total Applications</p>
          <p id="statApps" class="admin-stat-value">0</p>
          <p class="admin-stat-sub">Current application records</p>
        </article>
      </div>

      <div class="card">
        <h3 class="admin-subtitle">Quick Summary</h3>
        <p class="desc">Use the tabs above to manage workload, users, and jobs.</p>
        <div class="admin-summary-grid">
          <div class="admin-summary-item">
            <span id="overviewOpenJobs">0</span>
            <small>Open Jobs</small>
          </div>
          <div class="admin-summary-item">
            <span id="overviewClosedJobs">0</span>
            <small>Closed Jobs</small>
          </div>
          <div class="admin-summary-item">
            <span id="overviewHiredCount">0</span>
            <small>Total Hired Slots</small>
          </div>
        </div>
      </div>
    </section>

    <section class="admin-panel admin-hidden" data-admin-panel="workload">
      <div class="admin-headline">
        <div>
          <h2 class="admin-section-title">TA Workload Statistics</h2>
          <p class="admin-section-desc">View all teaching assistants' weekly hours and assignments.</p>
        </div>
      </div>
      <div class="card" style="margin-bottom:16px;">
        <h3 class="admin-subtitle">Workload Threshold</h3>
        <form id="adminThresholdForm">
          <div class="admin-form-grid">
            <div class="field">
              <label for="adminThresholdHours">Threshold Hours</label>
              <input id="adminThresholdHours" type="number" min="1" required />
            </div>
            <div class="field">
              <label for="adminThresholdUpdatedAt">Last Updated</label>
              <input id="adminThresholdUpdatedAt" type="text" readonly />
            </div>
          </div>
          <div class="row" style="margin-top:16px;">
            <button id="adminThresholdSaveBtn" type="submit" class="btn btn-primary">Save Threshold</button>
          </div>
        </form>
      </div>
      <div id="adminWorkloadCards" class="admin-feed"></div>
      <div class="card">
        <h3 class="admin-subtitle">Workload Monitoring (Table)</h3>
        <div class="table-wrap">
          <table>
            <thead><tr><th>Student ID</th><th>Name</th><th>Hired Jobs</th><th>Weekly Hours</th><th>Threshold</th><th>Warning</th></tr></thead>
            <tbody id="adminWorkloadBody"></tbody>
          </table>
        </div>
      </div>
    </section>

    <section class="admin-panel admin-hidden" data-admin-panel="users">
      <div class="admin-headline">
        <div>
          <h2 class="admin-section-title">User Management</h2>
          <p class="admin-section-desc">Review account roles and perform admin actions.</p>
        </div>
      </div>
      <div class="card" style="margin-bottom:16px;">
        <h3 class="admin-subtitle">Create User</h3>
        <form id="adminCreateUserForm">
          <div class="admin-form-grid">
            <div class="field">
              <label for="adminCreateRole">Role</label>
              <select id="adminCreateRole" name="role" required>
                <option value="student">Student</option>
                <option value="teacher">Teacher</option>
                <option value="admin">Admin</option>
              </select>
            </div>
            <div class="field">
              <label for="adminCreateName">Name</label>
              <input id="adminCreateName" name="name" type="text" required />
            </div>
            <div class="field">
              <label for="adminCreateEmail">Email</label>
              <input id="adminCreateEmail" name="email" type="email" required />
            </div>
            <div class="field">
              <label for="adminCreatePassword">Password</label>
              <input id="adminCreatePassword" name="password" type="password" required />
            </div>
            <div class="field" id="adminStudentIdField">
              <label for="adminCreateStudentId">Student ID</label>
              <input id="adminCreateStudentId" name="studentId" type="text" />
            </div>
            <div class="field" id="adminProgrammeField">
              <label for="adminCreateProgramme">Programme</label>
              <input id="adminCreateProgramme" name="programme" type="text" />
            </div>
          </div>
          <div class="row" style="margin-top:16px;">
            <button id="adminCreateUserBtn" type="submit" class="btn btn-primary">Create User</button>
          </div>
        </form>
      </div>
      <div id="adminUsersGrouped" class="admin-feed"></div>
      <div class="card">
        <h3 class="admin-subtitle">User Management (Table)</h3>
        <div class="table-wrap">
          <table>
            <thead><tr><th>Name</th><th>Email</th><th>Role</th><th>ID</th><th>Action</th></tr></thead>
            <tbody id="adminUsersBody"></tbody>
          </table>
        </div>
      </div>
    </section>

    <section class="admin-panel admin-hidden" data-admin-panel="jobs">
      <div class="admin-headline">
        <div>
          <h2 class="admin-section-title">Job Management</h2>
          <p class="admin-section-desc">View and manage all teaching assistant positions.</p>
        </div>
      </div>
      <div class="card" style="margin-bottom:16px;">
        <h3 class="admin-subtitle">Job Filters and Export</h3>
        <div class="admin-form-grid">
          <div class="field">
            <label for="adminJobStatusFilter">Status</label>
            <select id="adminJobStatusFilter">
              <option value="all">All</option>
              <option value="draft">Draft</option>
              <option value="open">Open</option>
              <option value="closed">Closed</option>
              <option value="withdrawn">Withdrawn</option>
            </select>
          </div>
          <div class="field">
            <label for="adminJobDepartmentFilter">Department</label>
            <input id="adminJobDepartmentFilter" type="text" list="adminDepartmentOptions" placeholder="all" />
            <datalist id="adminDepartmentOptions"></datalist>
          </div>
        </div>
        <div class="row" style="margin-top:16px;">
          <button id="adminApplyFiltersBtn" type="button" class="btn btn-primary">Apply Filters</button>
          <button id="adminResetFiltersBtn" type="button" class="btn btn-outline">Reset</button>
          <button id="adminExportCsvBtn" type="button" class="btn btn-outline">Export CSV</button>
          <button id="adminExportTxtBtn" type="button" class="btn btn-outline">Export TXT</button>
        </div>
      </div>
      <div id="adminJobsCards" class="admin-feed"></div>
      <div class="card">
        <h3 class="admin-subtitle">Job Overview (Table)</h3>
        <div class="table-wrap">
          <table>
            <thead><tr><th>Code</th><th>Position</th><th>Department</th><th>Teacher</th><th>Status</th><th>Recruitment</th><th>Slots</th><th>Action</th></tr></thead>
            <tbody id="adminJobsBody"></tbody>
          </table>
        </div>
      </div>
    </section>

    <section class="admin-panel admin-hidden" data-admin-panel="account">
      <div class="admin-headline">
        <div>
          <h2 class="admin-section-title">My Account</h2>
          <p class="admin-section-desc">Change your own password without using the admin reset flow.</p>
        </div>
      </div>
      <div class="card">
        <h3 class="admin-subtitle">Change Password</h3>
        <form id="adminChangePasswordForm">
          <div class="admin-form-grid">
            <div class="field">
              <label for="adminOldPassword">Current Password</label>
              <input id="adminOldPassword" type="password" required />
            </div>
            <div class="field">
              <label for="adminNewPassword">New Password</label>
              <input id="adminNewPassword" type="password" required />
            </div>
            <div class="field">
              <label for="adminConfirmPassword">Confirm New Password</label>
              <input id="adminConfirmPassword" type="password" required />
            </div>
          </div>
          <div class="row" style="margin-top:16px;">
            <button id="adminChangePasswordBtn" type="submit" class="btn btn-primary">Change Password</button>
          </div>
        </form>
      </div>
    </section>

    <p id="adminNotice" class="notice"></p>
  </main>
</div>
<script src="../assets/js/common.js"></script>
<script src="../assets/js/admin.js"></script>
</body>
</html>
