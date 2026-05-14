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
      <button class="admin-tab" data-admin-tab="archive" role="tab" aria-selected="false">Application Archive</button>
      <button class="admin-tab" data-admin-tab="alerts" role="tab" aria-selected="false">Alerts</button>
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
        <article class="admin-stat-card">
          <p class="admin-stat-label">Students</p>
          <p id="statStudents" class="admin-stat-value">0</p>
          <p class="admin-stat-sub">Registered TA applicants</p>
        </article>
        <article class="admin-stat-card">
          <p class="admin-stat-label">Module Organisers</p>
          <p id="statTeachers" class="admin-stat-value">0</p>
          <p class="admin-stat-sub">Teacher accounts</p>
        </article>
        <article class="admin-stat-card">
          <p class="admin-stat-label">Open Applications</p>
          <p id="statOpenApps" class="admin-stat-value">0</p>
          <p class="admin-stat-sub">Pending, viewed, or shortlisted</p>
        </article>
      </div>

      <div class="card">
        <h3 class="admin-subtitle">Quick Summary</h3>
        <p class="desc">Use the tabs above to manage workload, users, jobs, archive, and alerts.</p>
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
          <div class="admin-summary-item">
            <span id="overviewUnfilledPositions">0</span>
            <small>Unfilled Positions</small>
          </div>
          <div class="admin-summary-item">
            <span id="overviewRiskStudents">0</span>
            <small>Workload Risk Students</small>
          </div>
          <div class="admin-summary-item">
            <span id="overviewAlerts">0</span>
            <small>Active Alerts</small>
          </div>
        </div>
      </div>

      <div class="card" style="margin-top:16px;">
        <h3 class="admin-subtitle">Priority Alerts</h3>
        <div id="adminAlertsPreview" class="admin-alert-list"></div>
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
            <button id="adminExportWorkloadCsvBtn" type="button" class="btn btn-outline">Export Workload CSV</button>
            <button id="adminExportWorkloadTxtBtn" type="button" class="btn btn-outline">Export Workload TXT</button>
          </div>
        </form>
      </div>
      <div id="adminWorkloadCards" class="admin-feed"></div>
      <div id="adminWorkloadLegend" class="admin-workload-legend"></div>
      <div class="card">
        <h3 class="admin-subtitle">Workload Monitoring (Table)</h3>
        <div id="adminWorkloadLayout" class="admin-workload-layout">
          <div class="admin-workload-main-col">
            <div class="table-wrap">
              <table aria-describedby="adminWorkloadHelp">
                <thead><tr><th class="admin-workload-col-expand" scope="col">Details</th><th>Student ID</th><th>Name</th><th>Hired Jobs</th><th>Weekly Hours</th><th>Threshold</th><th>Level</th></tr></thead>
                <tbody id="adminWorkloadBody"></tbody>
              </table>
            </div>
            <p id="adminWorkloadHelp" class="admin-workload-help">Press Escape to close. Use Show or click the row to expand. On wide screens, detail also opens in the side panel.</p>
          </div>
          <aside id="adminWorkloadDrawer" class="admin-workload-drawer admin-hidden" aria-label="Hired position breakdown">
            <div class="admin-workload-drawer-head">
              <h3 id="adminWorkloadDrawerTitle" class="admin-workload-drawer-title">Details</h3>
              <button type="button" id="adminWorkloadDrawerClose" class="btn btn-outline">Close</button>
            </div>
            <div id="adminWorkloadDrawerBody" class="admin-workload-drawer-body"></div>
          </aside>
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
            <select id="adminJobDepartmentFilter">
              <option value="all">All Departments</option>
            </select>
          </div>
          <div class="field">
            <label for="adminJobTeacherFilter">Module Organiser</label>
            <select id="adminJobTeacherFilter">
              <option value="all">All Teachers</option>
            </select>
          </div>
        </div>
        <div class="row" style="margin-top:16px;">
          <button id="adminApplyFiltersBtn" type="button" class="btn btn-primary">Apply Filters</button>
          <button id="adminResetFiltersBtn" type="button" class="btn btn-outline">Reset</button>
          <button id="adminExportCsvBtn" type="button" class="btn btn-outline">Export CSV</button>
          <button id="adminExportTxtBtn" type="button" class="btn btn-outline">Export TXT</button>
          <button id="adminBackupBtn" type="button" class="btn btn-outline">Backup JSON</button>
        </div>
      </div>
      <div id="adminJobsCards" class="admin-feed"></div>
      <div class="card">
        <h3 class="admin-subtitle">Job Overview (Table)</h3>
        <div class="table-wrap">
          <table>
            <thead><tr><th>Code</th><th>Position</th><th>Department</th><th>Teacher</th><th>Applicants</th><th>Hired</th><th>Status</th><th>Action</th></tr></thead>
            <tbody id="adminJobsBody"></tbody>
          </table>
        </div>
      </div>
    </section>

    <section class="admin-panel admin-hidden" data-admin-panel="archive">
      <div class="admin-headline">
        <div>
          <h2 class="admin-section-title">Application Archive</h2>
          <p class="admin-section-desc">Read-only audit view of applications, MO notes, and decision feedback.</p>
        </div>
      </div>
      <div class="card" style="margin-bottom:16px;">
        <h3 class="admin-subtitle">Archive Filters and Export</h3>
        <div class="admin-form-grid">
          <div class="field">
            <label for="adminArchiveStatusFilter">Status</label>
            <select id="adminArchiveStatusFilter">
              <option value="all">All</option>
              <option value="pending">Pending</option>
              <option value="viewed">Viewed</option>
              <option value="shortlisted">Shortlisted</option>
              <option value="hired">Hired</option>
              <option value="rejected">Rejected</option>
            </select>
          </div>
          <div class="field">
            <label for="adminArchiveJobFilter">Job</label>
            <select id="adminArchiveJobFilter">
              <option value="all">All Jobs</option>
            </select>
          </div>
          <div class="field">
            <label for="adminArchiveTeacherFilter">Module Organiser</label>
            <select id="adminArchiveTeacherFilter">
              <option value="all">All Teachers</option>
            </select>
          </div>
          <div class="field">
            <label for="adminArchiveStudentFilter">Student Search</label>
            <input id="adminArchiveStudentFilter" type="text" placeholder="Name, user ID, or student no." />
          </div>
        </div>
        <div class="row" style="margin-top:16px;">
          <button id="adminArchiveApplyBtn" type="button" class="btn btn-primary">Apply Archive Filters</button>
          <button id="adminArchiveResetBtn" type="button" class="btn btn-outline">Reset</button>
          <button id="adminArchiveExportCsvBtn" type="button" class="btn btn-outline">Export Archive CSV</button>
          <button id="adminArchiveExportTxtBtn" type="button" class="btn btn-outline">Export Archive TXT</button>
        </div>
      </div>
      <div id="adminArchiveCards" class="admin-feed"></div>
      <div class="card">
        <h3 class="admin-subtitle">Archive Table</h3>
        <div class="table-wrap">
          <table>
            <thead><tr><th>Application</th><th>Student</th><th>Job</th><th>Organiser</th><th>Status</th><th>Applied</th><th>Feedback</th></tr></thead>
            <tbody id="adminArchiveBody"></tbody>
          </table>
        </div>
      </div>
    </section>

    <section class="admin-panel admin-hidden" data-admin-panel="alerts">
      <div class="admin-headline">
        <div>
          <h2 class="admin-section-title">Administrator Alerts</h2>
          <p class="admin-section-desc">Follow up workload, vacancy, deadline, and data-quality risk cases.</p>
        </div>
      </div>
      <div id="adminAlertsList" class="admin-alert-list"></div>
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
