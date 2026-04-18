<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <title>My Jobs - Module Organiser Portal</title>
  <link rel="stylesheet" href="../assets/css/main.css" />
  <style>
    .mo-tab svg {
      width: 16px;
      height: 16px;
      flex-shrink: 0;
    }
    .mo-job-layout {
      display: grid;
      grid-template-columns: minmax(320px, 420px) minmax(0, 1fr);
      gap: 18px;
      align-items: start;
    }
    .mo-form-card h3,
    .mo-feed-card h3 {
      margin: 0 0 8px;
    }
    .mo-form-card p,
    .mo-feed-card p {
      margin-top: 0;
    }
    .mo-demand-meta {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
      gap: 10px;
      margin: 12px 0;
    }
    .mo-demand-meta span,
    .mo-publish-grid span {
      display: block;
      font-size: 12px;
      color: #64748b;
      margin-bottom: 4px;
    }
    .mo-demand-actions {
      display: flex;
      flex-wrap: wrap;
      gap: 10px;
      margin-top: 14px;
    }
    .mo-publish-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
      gap: 12px;
      margin-top: 14px;
    }
    .mo-status-pill {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      min-width: 96px;
      border-radius: 999px;
      padding: 4px 10px;
      font-size: 12px;
      font-weight: 700;
      text-transform: capitalize;
    }
    .mo-status-pending {
      background: #fef3c7;
      color: #92400e;
      border: 1px solid #fcd34d;
    }
    .mo-status-approved,
    .mo-status-published {
      background: #d1fae5;
      color: #065f46;
      border: 1px solid #6ee7b7;
    }
    .mo-status-rejected,
    .mo-status-withdrawn {
      background: #fee2e2;
      color: #991b1b;
      border: 1px solid #fca5a5;
    }
    .mo-job-card {
      border: 1px solid #e5e7eb;
      border-radius: 12px;
      background: #fff;
      padding: 18px;
      margin-bottom: 14px;
      box-shadow: 0 8px 24px rgba(15, 23, 42, 0.05);
    }
    .mo-job-card:last-child {
      margin-bottom: 0;
    }
    .mo-job-card-head {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      gap: 12px;
    }
    .mo-job-card-head h4 {
      margin: 0 0 6px;
      font-size: 18px;
    }
    .mo-job-card-head p {
      margin: 0;
      color: #64748b;
      font-size: 13px;
    }
    .mo-job-card .notice {
      margin-bottom: 0;
    }
    .mo-inline-form {
      margin-top: 16px;
      padding-top: 16px;
      border-top: 1px dashed #dbe2ee;
      display: none;
    }
    .mo-inline-form.open {
      display: block;
    }
    .mo-inline-form .row {
      margin-top: 12px;
    }
    .mo-empty-tip {
      text-align: center;
      padding: 42px 24px;
      border: 2px dashed #dbe2ee;
      border-radius: 14px;
      background: #fff;
      color: #64748b;
    }
    .mo-notification-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 12px;
      margin-bottom: 14px;
    }
    .mo-notification-dot {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      min-width: 20px;
      height: 20px;
      border-radius: 999px;
      background: #dc2626;
      color: #fff;
      font-size: 11px;
      font-weight: 700;
      padding: 0 6px;
      margin-left: 6px;
    }
    .mo-notification-panel {
      border: 1px solid #e5e7eb;
      background: #fff;
      border-radius: 10px;
      padding: 10px;
      margin-bottom: 14px;
      display: none;
      max-height: 260px;
      overflow: auto;
    }
    .mo-notification-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 8px;
      border-bottom: 1px dashed #e2e8f0;
      padding: 8px 0;
    }
    .mo-notification-item:last-child {
      border-bottom: none;
    }
    @media (max-width: 960px) {
      .mo-job-layout {
        grid-template-columns: 1fr;
      }
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
        <p>Demand submission, publishing and withdrawal</p>
      </div>
    </div>
    <a class="mo-btn-logout" href="<%= request.getContextPath() %>/logout">Logout</a>
  </div>
</header>

<main class="mo-portal-main">
  <nav class="mo-tabs" aria-label="MO portal sections">
    <span class="mo-tab active" aria-current="page">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
        <rect x="2" y="7" width="20" height="14" rx="2" ry="2"></rect>
        <path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"></path>
      </svg>
      My Jobs
    </span>
    <a class="mo-tab" href="mo-applications.jsp">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
        <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"></path>
        <circle cx="9" cy="7" r="4"></circle>
        <path d="M22 21v-2a4 4 0 0 0-3-3.87"></path>
        <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
      </svg>
      Applicants
    </a>
  </nav>

  <div class="mo-applicants-head">
    <div>
      <h2 class="mo-section-title">My Job Workflow</h2>
      <p class="mo-section-desc">Use the A-side backend workflow here: submit a demand, wait for approval, then complete publishing details or withdraw when there are no active applications.</p>
    </div>
    <div class="row">
      <button id="notificationBtn" class="btn btn-outline" type="button">Notifications <span id="notificationDot" class="mo-notification-dot" style="display:none">0</span></button>
      <button id="reloadBtn" class="btn btn-outline" type="button">Refresh</button>
    </div>
  </div>
  <div id="notificationPanel" class="mo-notification-panel"></div>

  <div id="globalNotice" class="notice" style="margin-bottom:16px"></div>

  <section class="mo-job-layout">
    <div class="card mo-form-card">
      <h3>Submit New Demand</h3>
      <p class="desc">Create a new teaching assistant demand. Repeated pending requests for the same course are blocked by the backend.</p>

      <form id="demandForm">
        <div class="field">
          <label for="courseName">Course Name</label>
          <input id="courseName" type="text" placeholder="e.g. EBU6304 Software Engineering" required />
        </div>
        <div class="field">
          <label for="plannedCount">Planned TA Count</label>
          <input id="plannedCount" type="number" min="1" placeholder="e.g. 2" required />
        </div>
        <div class="mo-publish-grid">
          <div class="field">
            <label for="hourMin">Expected Hours (Min)</label>
            <input id="hourMin" type="number" min="1" placeholder="e.g. 8" required />
          </div>
          <div class="field">
            <label for="hourMax">Expected Hours (Max)</label>
            <input id="hourMax" type="number" min="1" placeholder="e.g. 12" required />
          </div>
        </div>
        <div class="row" style="margin-top:12px;">
          <button class="btn btn-primary" type="submit">Submit Demand</button>
          <button class="btn btn-outline" type="reset">Clear</button>
        </div>
      </form>
    </div>

    <div class="card mo-feed-card">
      <h3>My Demand Progress</h3>
      <p class="desc">Approved jobs can be published here. Withdraw is only allowed when there are no active applications.</p>
      <div id="jobsNotice" class="notice"></div>
      <div id="jobsEmpty" class="mo-empty-tip" style="display:none;">No MO jobs found yet. Submit your first demand from the panel on the left.</div>
      <div id="jobsFeed"></div>
    </div>
  </section>
</main>
<script src="../assets/js/common.js?v=mo3"></script>
<script src="../assets/js/teacher.js?v=mo3"></script>
</body>
</html>
