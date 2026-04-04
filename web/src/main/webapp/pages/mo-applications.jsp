<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <title>Applicants - Module Organiser Portal</title>
  <link rel="stylesheet" href="../assets/css/main.css" />
  <style>
    .mo-tab svg {
      width: 16px;
      height: 16px;
      flex-shrink: 0;
    }
    .status-pill {
      display: inline-block;
      min-width: 72px;
      text-align: center;
      padding: 4px 10px;
      border-radius: 999px;
      font-size: 12px;
      font-weight: 600;
    }
    .status-pending {
      background: #fef3c7;
      color: #92400e;
      border: 1px solid #fcd34d;
    }
    .status-viewed {
      background: #d1fae5;
      color: #065f46;
      border: 1px solid #6ee7b7;
    }
    .mo-filter-row .field {
      margin-bottom: 0;
    }
  </style>
</head>
<body class="mo-portal">
<header class="mo-portal-header">
  <div class="mo-portal-header-inner">
    <div class="mo-portal-brand">
      <div class="mo-portal-icon" aria-hidden="true">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <rect x="2" y="7" width="20" height="14" rx="2" ry="2"></rect>
          <path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"></path>
        </svg>
      </div>
      <div>
        <h1>Module Organiser Portal</h1>
        <p>Applicant Management</p>
      </div>
    </div>
    <a class="mo-btn-logout" href="<%= request.getContextPath() %>/logout">Logout</a>
  </div>
</header>

<main class="mo-portal-main">
  <nav class="mo-tabs" aria-label="MO portal sections">
    <a class="mo-tab" href="teacher.jsp">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
        <rect x="2" y="7" width="20" height="14" rx="2" ry="2"></rect>
        <path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"></path>
      </svg>
      My Jobs
    </a>
    <span class="mo-tab active" aria-current="page">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
        <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"></path>
        <circle cx="9" cy="7" r="4"></circle>
        <path d="M22 21v-2a4 4 0 0 0-3-3.87"></path>
        <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
      </svg>
      Applicants
    </span>
  </nav>

  <div class="mo-applicants-head">
    <div>
      <h2 class="mo-section-title">Applicant Management</h2>
      <p class="mo-section-desc">Review applications for your positions. Layout follows the MO applicants prototype; iteration&nbsp;1 only supports pending/viewed and active applications.</p>
    </div>
    <button type="button" class="btn btn-outline" disabled title="Planned for a later iteration (not in iteration 1)">Export CSV</button>
  </div>

  <div class="mo-scope-note" role="note">
    <strong>Iteration 1.</strong>
    Workload analysis, coloured workload panels, and hire/shortlist/reject actions are shown in Figma prototypes for later work.
    Here you can browse active applicants, open <strong>View details</strong> to load the full record and move <strong>pending → viewed</strong> on the server.
  </div>

  <div class="card" style="margin-bottom:16px">
    <div class="row mo-filter-row" style="align-items:flex-end;">
      <div class="field" style="min-width:220px;">
        <label for="jobIdInput">Filter by Job ID (optional)</label>
        <input id="jobIdInput" type="text" placeholder="e.g. 101" />
      </div>
      <button id="queryBtn" class="btn btn-primary" type="button">Query</button>
      <button id="resetBtn" class="btn btn-outline" type="button">Reset</button>
    </div>
    <p id="pageNotice" class="notice"></p>
  </div>

  <div id="applicationsEmpty" class="mo-empty-state" style="display:none;" role="status">
    <svg class="mo-empty-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" aria-hidden="true">
      <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"></path>
      <circle cx="9" cy="7" r="4"></circle>
      <path d="M22 21v-2a4 4 0 0 0-3-3.87"></path>
      <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
    </svg>
    <h3>No Applications Yet</h3>
    <p>There are no active applications for this view. Withdrawn applications stay hidden; the list refreshes automatically.</p>
  </div>

  <div id="applicationsFeed" class="mo-applicant-feed" style="display:none;" aria-live="polite"></div>
</main>
<script src="../assets/js/common.js"></script>
<script src="../assets/js/mo-applications.js"></script>
</body>
</html>
