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
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>Admin Dashboard - TA Recruitment Platform</title>
  <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/main.css?v=admin-student-style" />
</head>
<body class="admin-portal-page">
<div class="admin-portal admin-shell" data-current-user-id="<%= currentUserId %>" data-current-user-name="<%= currentUserName %>">
  <aside class="admin-sidebar" aria-label="Administrator navigation">
    <div class="admin-portal-brand">
      <div class="admin-portal-icon" aria-hidden="true">
        <svg viewBox="0 0 24 24" fill="none">
          <path d="M4 7.5L12 3L20 7.5L12 12L4 7.5Z" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round"></path>
          <path d="M6.5 10V15.5C6.5 17.2 9 18.6 12 18.6C15 18.6 17.5 17.2 17.5 15.5V10" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"></path>
        </svg>
      </div>
      <div>
        <h1>Administrator Portal</h1>
        <p id="adminSubTitle">Welcome, <%= currentUserName %></p>
      </div>
    </div>

    <nav class="admin-tabs" role="tablist" aria-label="Admin sections">
      <button class="admin-tab active" data-admin-tab="overview" role="tab" aria-selected="true">
        <svg viewBox="0 0 24 24" fill="none" aria-hidden="true"><path d="M4 13H10V20H4V13Z" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round"></path><path d="M14 4H20V20H14V4Z" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round"></path><path d="M4 4H10V9H4V4Z" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round"></path></svg>
        System Overview
      </button>
      <button class="admin-tab" data-admin-tab="workload" role="tab" aria-selected="false">
        <svg viewBox="0 0 24 24" fill="none" aria-hidden="true"><path d="M4 19V5" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"></path><path d="M8 17V11" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"></path><path d="M12 17V7" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"></path><path d="M16 17V9" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"></path><path d="M20 17V13" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"></path></svg>
        Workload
      </button>
      <button class="admin-tab" data-admin-tab="users" role="tab" aria-selected="false">
        <svg viewBox="0 0 24 24" fill="none" aria-hidden="true"><path d="M8.5 11.2C10.2 11.2 11.5 9.9 11.5 8.2C11.5 6.5 10.2 5.2 8.5 5.2C6.8 5.2 5.5 6.5 5.5 8.2C5.5 9.9 6.8 11.2 8.5 11.2Z" stroke="currentColor" stroke-width="1.8"></path><path d="M3.8 19C4.5 16.4 6.2 15.1 8.5 15.1C10.8 15.1 12.5 16.4 13.2 19" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"></path><path d="M15 10.5C16.4 10.5 17.5 9.4 17.5 8C17.5 6.6 16.4 5.5 15 5.5" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"></path><path d="M15.5 15.2C17.8 15.5 19.3 16.8 20 19" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"></path></svg>
        Users
      </button>
      <button class="admin-tab" data-admin-tab="demands" role="tab" aria-selected="false">
        <svg viewBox="0 0 24 24" fill="none" aria-hidden="true"><path d="M6 3.8H18C18.7 3.8 19.2 4.3 19.2 5V19C19.2 19.7 18.7 20.2 18 20.2H6C5.3 20.2 4.8 19.7 4.8 19V5C4.8 4.3 5.3 3.8 6 3.8Z" stroke="currentColor" stroke-width="1.8"></path><path d="M8 9H16" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"></path><path d="M8 13H14" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"></path><path d="M8 17H12" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"></path></svg>
        Demand Review
      </button>
      <button class="admin-tab" data-admin-tab="jobs" role="tab" aria-selected="false">
        <svg viewBox="0 0 24 24" fill="none" aria-hidden="true"><path d="M9 7V5.5C9 4.7 9.7 4 10.5 4H13.5C14.3 4 15 4.7 15 5.5V7" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"></path><path d="M5 7H19C19.7 7 20.2 7.5 20.2 8.2V18.8C20.2 19.5 19.7 20 19 20H5C4.3 20 3.8 19.5 3.8 18.8V8.2C3.8 7.5 4.3 7 5 7Z" stroke="currentColor" stroke-width="1.8"></path><path d="M9.5 12H14.5" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"></path></svg>
        Jobs
      </button>
      <button class="admin-tab" data-admin-tab="announcements" role="tab" aria-selected="false">
        <svg viewBox="0 0 24 24" fill="none" aria-hidden="true"><path d="M5 14V10L16 5V19L5 14Z" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round"></path><path d="M5 14L7 20H10L8 15" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round"></path><path d="M18 10.2C19.1 10.8 19.7 11.4 19.7 12C19.7 12.6 19.1 13.2 18 13.8" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"></path></svg>
        Announcements
      </button>
      <button class="admin-tab" data-admin-tab="account" role="tab" aria-selected="false">
        <svg viewBox="0 0 24 24" fill="none" aria-hidden="true"><path d="M12 12C14.2 12 16 10.2 16 8C16 5.8 14.2 4 12 4C9.8 4 8 5.8 8 8C8 10.2 9.8 12 12 12Z" stroke="currentColor" stroke-width="1.8"></path><path d="M5.5 20C6.4 16.8 8.6 15.2 12 15.2C15.4 15.2 17.6 16.8 18.5 20" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"></path></svg>
        My Account
      </button>
    </nav>

    <div class="admin-profile-card">
      <div class="admin-profile-card-visual" aria-hidden="true"></div>
      <h2>Admin Control Center</h2>
      <p>Monitor workload, demand approvals, users, and recruitment health.</p>
      <div class="admin-profile-progress"><span></span></div>
      <button id="adminSidebarOutcomeBtn" type="button">View Results -></button>
    </div>
  </aside>

  <div class="admin-main">
    <header class="admin-topbar">
      <div></div>
      <div class="admin-header-actions">
        <button id="adminAlertsButton" class="admin-alert-trigger" type="button" aria-haspopup="dialog" aria-controls="adminAlertsModal" aria-label="Open administrator alerts">
          <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <path d="M18 9.5C18 6.2 15.3 3.5 12 3.5C8.7 3.5 6 6.2 6 9.5V13.7L4.6 16.2C4.3 16.8 4.7 17.5 5.4 17.5H18.6C19.3 17.5 19.7 16.8 19.4 16.2L18 13.7V9.5Z" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round"></path>
            <path d="M10 20C10.5 20.6 11.2 20.9 12 20.9C12.8 20.9 13.5 20.6 14 20" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"></path>
          </svg>
          <strong id="adminAlertsBadge">0</strong>
        </button>
        <div class="admin-user-menu">
          <span class="admin-avatar">AD</span>
          <strong><%= currentUserName %></strong>
        </div>
        <a class="admin-btn-logout" href="<%= request.getContextPath() %>/logout">Logout</a>
      </div>
    </header>

  <div id="adminAlertsModal" class="admin-modal admin-hidden" role="dialog" aria-modal="true" aria-labelledby="adminAlertsModalTitle">
    <div class="admin-modal-backdrop" data-alerts-close></div>
    <div class="admin-modal-card admin-alert-modal-card">
      <div class="admin-modal-head">
        <div>
          <h2 id="adminAlertsModalTitle" class="admin-section-title">Administrator Alerts</h2>
          <p class="admin-section-desc">Workload, vacancy, deadline, and data-quality risk cases.</p>
        </div>
        <button id="adminAlertsCloseBtn" type="button" class="btn btn-outline" data-alerts-close>Close</button>
      </div>
      <div id="adminAlertsList" class="admin-alert-list"></div>
    </div>
  </div>

  <main class="admin-portal-main admin-content">

    <section class="admin-panel" data-admin-panel="overview">
      <div class="admin-headline">
        <h2 class="admin-section-title">System Overview</h2>
      </div>
      <div class="admin-overview-charts">
        <div class="card admin-chart-card">
          <h3 class="admin-subtitle">Users by role</h3>
          <p class="desc">Distribution of student, teacher, and admin accounts.</p>
          <div class="admin-chart-canvas-wrap">
            <canvas id="adminOverviewUsersPie" aria-label="Users by role pie chart"></canvas>
          </div>
        </div>
        <div class="card admin-chart-card">
          <h3 class="admin-subtitle">Daily job publications</h3>
          <p class="desc">Published TA positions per day (last 30 days).</p>
          <div class="admin-chart-canvas-wrap admin-chart-canvas-wrap--line">
            <canvas id="adminOverviewJobsLine" aria-label="Daily job publications line chart"></canvas>
          </div>
        </div>
        <div class="card admin-chart-card admin-chart-card--wide">
          <h3 class="admin-subtitle">Daily application trend</h3>
          <p class="desc">New active applications submitted per day (last 30 days).</p>
          <div class="admin-chart-canvas-wrap admin-chart-canvas-wrap--line">
            <canvas id="adminOverviewAppsLine" aria-label="Daily application trend line chart"></canvas>
          </div>
        </div>
      </div>
      <div class="card">
        <h3 class="admin-subtitle">Quick Summary</h3>
        <p class="desc">Use the tabs above to manage workload, users, jobs, demand review, and announcements.</p>
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
        <div class="row" style="margin-top:16px;">
          <button id="adminOverviewOutcomeBtn" type="button" class="btn btn-primary">View Recruitment Results</button>
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
        <p class="desc">Set the overload hour limit. Normal (50%) and Warning (75%) levels are fixed in system settings.</p>
        <form id="adminThresholdForm">
          <div class="admin-form-grid">
            <div class="field">
              <label for="adminThresholdHours">Threshold Hours (Overload)</label>
              <input id="adminThresholdHours" type="number" min="1" required />
            </div>
          </div>
          <div class="row" style="margin-top:16px;">
            <button id="adminThresholdSaveBtn" type="submit" class="btn btn-primary">Save Threshold</button>
            <button id="adminNotifyOverloadBtn" type="button" class="btn btn-outline">Notify All Overload Students</button>
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
      <div class="card admin-user-search-card" style="margin-bottom:16px;">
        <h3 class="admin-subtitle">Find Users</h3>
        <div class="admin-user-search-row">
          <div class="field">
            <label for="adminUserSearchInput">Search</label>
            <input id="adminUserSearchInput" type="search" placeholder="Search by name, email, role, or ID" autocomplete="off" />
          </div>
          <div class="row admin-user-search-actions">
            <button id="adminUserSearchBtn" type="button" class="btn btn-primary">Search</button>
            <button id="adminUserSearchClearBtn" type="button" class="btn btn-outline">Clear</button>
          </div>
        </div>
        <p id="adminUserSearchMeta" class="admin-list-meta">Showing all users.</p>
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

    <section class="admin-panel admin-hidden" data-admin-panel="demands">
      <div class="admin-headline">
        <div>
          <h2 class="admin-section-title">Demand Approval Workbench</h2>
          <p class="admin-section-desc">Review Module Organiser demand submissions before they can be published as TA jobs.</p>
        </div>
      </div>
      <div class="card" style="margin-bottom:16px;">
        <h3 class="admin-subtitle">Demand Filters</h3>
        <div class="admin-form-grid">
          <div class="field">
            <label for="adminDemandStatusFilter">Approval Status</label>
            <select id="adminDemandStatusFilter">
              <option value="all">All</option>
              <option value="pending">Pending</option>
              <option value="approved">Approved</option>
              <option value="rejected">Rejected</option>
            </select>
          </div>
        </div>
        <div class="row" style="margin-top:16px;">
          <button id="adminDemandRefreshBtn" type="button" class="btn btn-primary">Refresh Demands</button>
        </div>
      </div>
      <div id="adminDemandCards" class="admin-feed"></div>
      <div class="card">
        <h3 class="admin-subtitle">Demand Review Table</h3>
        <div class="table-wrap">
          <table>
            <thead><tr><th>Module</th><th>Title</th><th>Organiser</th><th>Submitted</th><th>Current Status</th><th>Review Decision</th></tr></thead>
            <tbody id="adminDemandBody"></tbody>
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
      <div class="admin-jobs-charts card" style="margin-bottom:16px;">
        <h3 class="admin-subtitle">Job analysis</h3>
        <p class="desc">Job counts for the current filters: by department and by lifecycle status (pending, reject, open, overdue).</p>
        <div class="admin-jobs-charts-row">
          <div class="admin-chart-card admin-chart-card--compact">
            <h4 class="admin-chart-mini-title">By department</h4>
            <div class="admin-chart-canvas-wrap">
              <canvas id="adminJobsDeptPie" aria-label="Jobs by department"></canvas>
            </div>
          </div>
          <div class="admin-chart-card admin-chart-card--compact">
            <h4 class="admin-chart-mini-title">By status</h4>
            <div class="admin-chart-canvas-wrap">
              <canvas id="adminJobsStatusPie" aria-label="Jobs by status"></canvas>
            </div>
          </div>
        </div>
      </div>
      <div id="adminJobApplicationsPanel" class="card admin-job-applications-panel admin-hidden" style="margin-bottom:16px;">
        <div class="admin-panel-inline-head">
          <div>
            <h3 id="adminJobApplicationsTitle" class="admin-subtitle">Job Applications</h3>
            <p class="admin-list-meta">Read-only applicant drilldown for the selected job.</p>
          </div>
          <button id="adminJobApplicationsCloseBtn" type="button" class="btn btn-outline">Close</button>
        </div>
        <div class="admin-form-grid">
          <div class="field">
            <label for="adminJobApplicationStatusFilter">Application Status</label>
            <select id="adminJobApplicationStatusFilter">
              <option value="all">All</option>
              <option value="pending">Pending</option>
              <option value="viewed">Viewed</option>
              <option value="shortlisted">Shortlisted</option>
              <option value="hired">Hired</option>
              <option value="rejected">Rejected</option>
            </select>
          </div>
        </div>
        <div class="row" style="margin-top:16px;">
          <button id="adminJobApplicationsApplyBtn" type="button" class="btn btn-primary">Apply Status Filter</button>
          <button id="adminJobApplicationsCsvBtn" type="button" class="btn btn-outline">Export Job CSV</button>
          <button id="adminJobApplicationsTxtBtn" type="button" class="btn btn-outline">Export Job TXT</button>
        </div>
        <div id="adminJobApplicationCards" class="admin-feed" style="margin-top:16px;"></div>
        <div class="table-wrap">
          <table>
            <thead><tr><th>Applicant</th><th>Student No.</th><th>Applied</th><th>Status</th><th>Notes / Feedback</th></tr></thead>
            <tbody id="adminJobApplicationBody"></tbody>
          </table>
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

    <section class="admin-panel admin-hidden admin-panel--outcome-board" data-admin-panel="recruitment-outcome">
      <div class="admin-outcome-header">
        <div class="admin-outcome-header-text">
          <p class="admin-outcome-pill"><span class="admin-sr-only">Mode: </span>Read-only · Leadership view</p>
          <h2 class="admin-section-title admin-outcome-title">Recruitment Results</h2>
          <p class="admin-outcome-generated">Snapshot generated at <time id="adminOutcomeGeneratedAt" datetime="">—</time> <span class="admin-outcome-generated-note">(server UTC, shown in your time zone)</span></p>
        </div>
        <button type="button" id="adminOutcomeBackBtn" class="btn btn-outline admin-outcome-back-btn">Back to Overview</button>
      </div>
      <div class="admin-outcome-toolbar" role="group" aria-label="Recruitment outcome actions">
        <div class="admin-outcome-toolbar-actions">
          <button type="button" id="adminOutcomeExportCsvBtn" class="btn btn-outline">Export CSV</button>
        </div>
      </div>
      <div class="card admin-outcome-card admin-outcome-mix-card">
        <h3 class="admin-subtitle">Overall hiring mix</h3>
        <p class="desc">Visual split of hired seats and remaining vacancies against total position slots.</p>
        <div id="adminOutcomeMixChart" class="admin-outcome-mix-chart"></div>
      </div>
      <div class="admin-outcome-board">
      <div class="card" style="margin-bottom:16px;">
        <h3 class="admin-subtitle">Key indicators</h3>
        <p class="desc">Aggregated across all non-withdrawn job postings. Hired + vacancies = total slots (filled seats are capped per job headcount).</p>
      </div>
      <div class="admin-stats-grid admin-outcome-kpi-grid">
        <article class="admin-stat-card admin-stat-card--outcome">
          <p class="admin-stat-label">Total slots</p>
          <p id="adminOutcomeTotalSlots" class="admin-stat-value">0</p>
          <p class="admin-stat-sub">Sum of headcount targets (non-withdrawn jobs)</p>
        </article>
        <article class="admin-stat-card admin-stat-card--outcome">
          <p class="admin-stat-label">Hired</p>
          <p id="adminOutcomeTotalHired" class="admin-stat-value">0</p>
          <p class="admin-stat-sub">Filled position slots across all jobs</p>
        </article>
        <article class="admin-stat-card admin-stat-card--outcome">
          <p class="admin-stat-label">Vacancies</p>
          <p id="adminOutcomeTotalVacancies" class="admin-stat-value">0</p>
          <p class="admin-stat-sub">Unfilled slots (positions minus hired per job)</p>
        </article>
        <article class="admin-stat-card admin-stat-card--outcome">
          <p class="admin-stat-label">Open jobs</p>
          <p id="adminOutcomeOpenJobs" class="admin-stat-value">0</p>
          <p class="admin-stat-sub">Jobs currently open and accepting applications</p>
        </article>
        <article class="admin-stat-card admin-stat-card--outcome">
          <p class="admin-stat-label">Application records</p>
          <p id="adminOutcomeTotalApplications" class="admin-stat-value">0</p>
          <p class="admin-stat-sub">Total active application records</p>
        </article>
      </div>
      <div class="card admin-outcome-card">
        <h3 class="admin-subtitle">By department</h3>
        <p class="desc">Hired counts and unfilled slots grouped by each job posting's department field. Blank values are rolled up as Unspecified.</p>
        <p class="admin-outcome-dept-legend" role="note">
          <span><span class="admin-outcome-legend-swatch admin-outcome-legend-swatch--hired" aria-hidden="true"></span>Hired</span>
          <span><span class="admin-outcome-legend-swatch admin-outcome-legend-swatch--vac" aria-hidden="true"></span>Vacancies</span>
        </p>
        <div id="adminOutcomeDeptChart" class="admin-outcome-dept-chart"></div>
      </div>
      <div class="card admin-outcome-card">
        <h3 class="admin-subtitle">Top positions by vacancy</h3>
        <p class="desc" id="adminOutcomeVacancyHelp">Non-withdrawn jobs with unfilled slots, ranked for MO follow-up.</p>
        <div class="table-wrap admin-outcome-table-wrap">
          <table aria-describedby="adminOutcomeVacancyHelp">
            <thead>
              <tr>
                <th scope="col">#</th>
                <th scope="col">Module</th>
                <th scope="col">Title</th>
                <th scope="col">Department</th>
                <th scope="col">Organiser</th>
                <th scope="col">Positions</th>
                <th scope="col">Hired</th>
                <th scope="col">Vacancies</th>
              </tr>
            </thead>
            <tbody id="adminOutcomeVacancyBody"></tbody>
          </table>
        </div>
      </div>
      </div>
    </section>

    <section class="admin-panel admin-hidden" data-admin-panel="announcements">
      <div class="admin-headline">
        <div>
          <h2 class="admin-section-title">System Announcements</h2>
          <p class="admin-section-desc">Broadcast a site message to students, module organisers, or both. Each recipient receives a notification record in notifications.json.</p>
        </div>
      </div>
      <div class="card">
        <h3 class="admin-subtitle">Send announcement</h3>
        <form id="adminAnnouncementForm">
          <div class="admin-form-grid">
            <div class="field" style="grid-column: 1 / -1;">
              <label for="adminAnnouncementTitle">Title</label>
              <input id="adminAnnouncementTitle" name="title" type="text" maxlength="200" required placeholder="e.g. Semester recruitment schedule update" />
            </div>
            <div class="field" style="grid-column: 1 / -1;">
              <label for="adminAnnouncementBody">Body</label>
              <textarea id="adminAnnouncementBody" name="body" rows="6" maxlength="4000" required placeholder="Message shown to recipients"></textarea>
            </div>
            <div class="field">
              <label for="adminAnnouncementTarget">Target role</label>
              <select id="adminAnnouncementTarget" name="targetRole" required>
                <option value="student">Students</option>
                <option value="teacher">Module organisers (teachers)</option>
                <option value="all">All students and teachers</option>
              </select>
            </div>
          </div>
          <div class="row" style="margin-top:16px;">
            <button id="adminAnnouncementSendBtn" type="submit" class="btn btn-primary">Send announcement</button>
          </div>
        </form>
        <p id="adminAnnouncementResult" class="desc" style="margin-top:12px;"></p>
      </div>
      <div class="card" style="margin-top:16px;">
        <div class="admin-headline" style="margin-bottom:12px;">
          <div>
            <h3 class="admin-subtitle" style="margin:0;">Sent announcements</h3>
            <p class="desc" style="margin:4px 0 0;">History of admin broadcasts (newest first).</p>
          </div>
          <button id="adminAnnouncementRefreshBtn" type="button" class="btn btn-outline">Refresh</button>
        </div>
        <div id="adminAnnouncementHistory" class="admin-list">
          <p class="admin-empty-text">Loading history...</p>
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
</div>
<script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.1/dist/chart.umd.min.js"></script>
<script src="../assets/js/common.js"></script>
<script src="../assets/js/admin.js"></script>
</body>
</html>
