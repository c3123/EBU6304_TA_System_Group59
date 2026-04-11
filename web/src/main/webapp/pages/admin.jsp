<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <title>Admin Dashboard - TA Recruitment Platform</title>
  <link rel="stylesheet" href="../assets/css/main.css" />
</head>
<body>
<div class="admin-portal">
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
          <p id="adminSubTitle">Welcome, Admin User</p>
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
      <div id="adminWorkloadCards" class="admin-feed"></div>
      <div class="card">
        <h3 class="admin-subtitle">Workload Monitoring (Table)</h3>
        <div class="table-wrap">
          <table>
            <thead><tr><th>Student ID</th><th>Name</th><th>Hired Jobs</th><th>Weekly Hours</th></tr></thead>
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
      <div id="adminUsersGrouped" class="admin-feed"></div>
      <div class="card">
        <h3 class="admin-subtitle">User Management (Table)</h3>
        <div class="table-wrap">
          <table>
            <thead><tr><th>Name</th><th>Email</th><th>Role</th><th>Action</th></tr></thead>
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
      <div id="adminJobsCards" class="admin-feed"></div>
      <div class="card">
        <h3 class="admin-subtitle">Job Overview (Table)</h3>
        <div class="table-wrap">
          <table>
            <thead><tr><th>Code</th><th>Position</th><th>Teacher</th><th>Status</th><th>Recruitment</th><th>Slots</th><th>Action</th></tr></thead>
            <tbody id="adminJobsBody"></tbody>
          </table>
        </div>
      </div>
    </section>

    <p id="adminNotice" class="notice"></p>
  </main>
</div>
<script src="../assets/js/common.js"></script>
<script src="../assets/js/admin.js"></script>
</body>
</html>
