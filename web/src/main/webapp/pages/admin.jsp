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
      <div class="admin-header-actions">
        <button id="adminAlertsButton" class="admin-alert-trigger" type="button" aria-haspopup="dialog" aria-controls="adminAlertsModal" aria-label="Open administrator alerts">
          <span aria-hidden="true">!</span>
          <strong id="adminAlertsBadge">0</strong>
        </button>
        <a class="admin-btn-logout" href="<%= request.getContextPath() %>/logout">Logout</a>
      </div>
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

  <main class="admin-portal-main">
    <nav class="admin-tabs" role="tablist" aria-label="Admin sections">
      <button class="admin-tab active" data-admin-tab="overview" role="tab" aria-selected="true">System Overview</button>
      <button class="admin-tab" data-admin-tab="workload" role="tab" aria-selected="false">Workload</button>
      <button class="admin-tab" data-admin-tab="users" role="tab" aria-selected="false">Users</button>
      <button class="admin-tab" data-admin-tab="demands" role="tab" aria-selected="false">Demand Review</button>
      <button class="admin-tab" data-admin-tab="jobs" role="tab" aria-selected="false">Jobs</button>
      <button class="admin-tab" data-admin-tab="announcements" role="tab" aria-selected="false">Announcements</button>
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
      <div class="admin-outcome-hero">
        <div class="admin-outcome-hero-text">
          <p class="admin-outcome-pill"><span class="admin-sr-only">Mode: </span>Read-only · Leadership view</p>
          <h2 class="admin-section-title admin-outcome-title">Recruitment Results</h2>
          <p class="admin-section-desc admin-outcome-lede">Single-screen summary of hiring pressure: KPIs, department mix, and the largest role-level gaps. No edits are available on this tab.</p>
          <p class="admin-outcome-generated">Snapshot generated at <time id="adminOutcomeGeneratedAt" datetime="">—</time> <span class="admin-outcome-generated-note">(server UTC, shown in your time zone)</span></p>
          <div class="admin-outcome-toolbar" role="group" aria-label="Recruitment outcome snapshot controls">
            <div class="admin-outcome-toolbar-dates">
              <div class="field">
                <label for="adminOutcomeJobSince">Job reference from</label>
                <input id="adminOutcomeJobSince" type="date" />
              </div>
              <div class="field">
                <label for="adminOutcomeJobUntil">Job reference to</label>
                <input id="adminOutcomeJobUntil" type="date" />
              </div>
            </div>
            <div class="admin-outcome-toolbar-actions">
              <button type="button" id="adminOutcomeBackBtn" class="btn btn-outline">Back to Overview</button>
              <button type="button" id="adminOutcomeApplyRangeBtn" class="btn btn-primary">Apply range</button>
              <button type="button" id="adminOutcomeClearRangeBtn" class="btn btn-outline">All jobs</button>
              <button type="button" id="adminOutcomeRefreshBtn" class="btn btn-outline">Refresh snapshot</button>
              <button type="button" id="adminOutcomeExportCsvBtn" class="btn btn-outline">Export CSV</button>
            </div>
            <p id="adminOutcomeFilterHint" class="admin-outcome-filter-hint desc">Filters use each job's reference day: published date if set, otherwise created, otherwise last updated. Jobs with no parseable date stay included when a range is active.</p>
          </div>
        </div>
      </div>
      <div class="admin-outcome-board">
      <div class="card" style="margin-bottom:16px;">
        <h3 class="admin-subtitle">Key indicators</h3>
        <p class="desc">Aggregated across all non-withdrawn job postings and active applications. Total vacancies = sum of (positions - hired) per job.</p>
      </div>
      <div class="admin-stats-grid admin-outcome-kpi-grid">
        <article class="admin-stat-card admin-stat-card--outcome">
          <p class="admin-stat-label">Total position slots</p>
          <p id="adminOutcomeTotalSlots" class="admin-stat-value">0</p>
          <p class="admin-stat-sub">Sum of headcount targets (non-withdrawn jobs)</p>
        </article>
        <article class="admin-stat-card admin-stat-card--outcome">
          <p class="admin-stat-label">Closed recruitment</p>
          <p id="adminOutcomeClosedJobs" class="admin-stat-value">0</p>
          <p class="admin-stat-sub">Jobs with recruitment closed or status closed</p>
        </article>
        <article class="admin-stat-card admin-stat-card--outcome">
          <p class="admin-stat-label">Still recruiting</p>
          <p id="adminOutcomeRecruitingJobs" class="admin-stat-value">0</p>
          <p class="admin-stat-sub">Open jobs not yet closed</p>
        </article>
        <article class="admin-stat-card admin-stat-card--outcome">
          <p class="admin-stat-label">Total applications</p>
          <p id="adminOutcomeTotalApplications" class="admin-stat-value">0</p>
          <p class="admin-stat-sub">Active application records</p>
        </article>
        <article class="admin-stat-card admin-stat-card--outcome">
          <p class="admin-stat-label">Total hired</p>
          <p id="adminOutcomeTotalHired" class="admin-stat-value">0</p>
          <p class="admin-stat-sub">Active applications marked hired</p>
        </article>
        <article class="admin-stat-card admin-stat-card--outcome">
          <p class="admin-stat-label">Total vacancies</p>
          <p id="adminOutcomeTotalVacancies" class="admin-stat-value">0</p>
          <p class="admin-stat-sub">Unfilled slots (positions minus hired)</p>
        </article>
      </div>
      <div class="card admin-outcome-card">
        <h3 class="admin-subtitle">Overall hiring mix</h3>
        <p class="desc">Visual split of hired seats and remaining vacancies against total position slots.</p>
        <div id="adminOutcomeMixChart" class="admin-outcome-mix-chart"></div>
      </div>
      <div class="card admin-outcome-card">
        <h3 class="admin-subtitle">By department</h3>
        <p class="desc">Hired counts and unfilled slots grouped by each job posting's department field. Blank values are rolled up as 未填.</p>
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
<script src="../assets/js/common.js"></script>
<script src="../assets/js/admin.js"></script>
</body>
</html>
