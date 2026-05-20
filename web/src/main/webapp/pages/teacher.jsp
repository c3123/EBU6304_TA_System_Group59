<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <title>My Jobs - Module Organiser Portal</title>
  <link rel="stylesheet" href="../assets/css/main.css?v=teacher-student-style" />
  <style>
    body.teacher-portal-page {
      min-height: 100vh;
      margin: 0;
      background: #f5f8ff;
      color: #111827;
    }

    .teacher-shell {
      min-height: 100vh;
      display: grid;
      grid-template-columns: 248px minmax(0, 1fr);
      background:
        linear-gradient(90deg, #eef5ff 0, #f8fbff 248px, #ffffff 248px, #f7faff 100%);
    }

    .mo-portal-header {
      position: sticky;
      top: 0;
      height: 100vh;
      background: rgba(245, 250, 255, 0.82);
      color: #111827;
      padding: 28px 20px;
      border-right: 1px solid rgba(219, 226, 238, 0.7);
      box-shadow: none;
      backdrop-filter: blur(14px);
      -webkit-backdrop-filter: blur(14px);
      z-index: 10;
    }

    .mo-portal-header-inner {
      min-height: calc(100vh - 56px);
      display: flex;
      flex-direction: column;
      align-items: stretch;
      justify-content: flex-start;
      gap: 28px;
      max-width: none;
      margin: 0;
      padding: 0;
    }

    .mo-portal-brand {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 6px 8px;
      border-bottom: 0;
    }

    .mo-portal-icon {
      width: 46px;
      height: 46px;
      border-radius: 50%;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      color: #2563eb;
      background: #eaf2ff;
      box-shadow: 0 10px 26px rgba(37, 99, 235, 0.12);
      flex: 0 0 auto;
    }

    .mo-portal-icon svg {
      width: 23px;
      height: 23px;
    }

    .mo-portal-brand h1 {
      margin: 0;
      font-size: 18px;
      line-height: 1.2;
    }

    .mo-portal-brand p {
      margin: 5px 0 0;
      color: #64748b;
      font-size: 13px;
    }

    .teacher-main {
      min-width: 0;
      display: flex;
      flex-direction: column;
    }

    .teacher-topbar {
      min-height: 82px;
      display: grid;
      grid-template-columns: minmax(0, 1fr) auto;
      align-items: center;
      gap: 20px;
      padding: 18px 44px;
      background: rgba(255, 255, 255, 0.88);
      border-bottom: 1px solid #e7edf7;
      backdrop-filter: blur(12px);
      -webkit-backdrop-filter: blur(12px);
    }

    .teacher-topbar h1 {
      margin: 0;
      font-size: 22px;
      line-height: 1.2;
      color: #0f172a;
    }

    .teacher-topbar p {
      margin: 5px 0 0;
      color: #64748b;
      font-size: 13px;
      line-height: 1.4;
    }

    .teacher-top-actions {
      display: inline-flex;
      align-items: center;
      gap: 10px;
      flex-wrap: wrap;
      justify-content: flex-end;
    }

    .mo-portal-main {
      width: 100%;
      max-width: none;
      margin: 0;
      padding: 28px 44px 36px;
    }

    .mo-tabs {
      display: flex;
      flex-direction: column;
      gap: 12px;
      margin: 6px 0 0;
      padding: 0;
    }

    .mo-tab svg {
      width: 16px;
      height: 16px;
      flex-shrink: 0;
    }
    .mo-tab {
      width: 100%;
      min-height: 48px;
      display: flex;
      align-items: center;
      gap: 10px;
      justify-content: flex-start;
      padding: 0 16px;
      color: #475569;
      border-radius: 8px;
      border: 1px solid transparent;
      background: transparent;
      font-size: 14px;
      font-weight: 700;
      text-decoration: none;
    }
    .mo-tab:hover,
    .mo-tab.active {
      color: #2563eb;
      background: #dfeaff;
      border-color: rgba(37, 99, 235, 0.06);
      box-shadow: inset 0 0 0 1px rgba(37, 99, 235, 0.06);
    }
    .mo-tab svg {
      color: #475569;
    }
    .mo-tab:hover svg,
    .mo-tab.active svg {
      color: #2563eb;
    }
    .mo-btn-logout {
      margin-top: auto;
      width: 100%;
      justify-content: center;
      border-radius: 8px;
      background: #dfeaff;
      color: #1d4ed8;
      box-shadow: none;
    }
    .teacher-dashboard-intro {
      margin-bottom: 18px;
    }
    .teacher-summary-grid {
      display: grid;
      grid-template-columns: repeat(5, minmax(140px, 1fr));
      gap: 14px;
      margin-bottom: 22px;
    }
    .teacher-summary-card {
      min-height: 112px;
      padding: 16px;
      border: 1px solid #e4eaf4;
      border-radius: 8px;
      background: #ffffff;
      box-shadow: 0 12px 34px rgba(24, 45, 84, 0.06);
      display: flex;
      flex-direction: column;
      justify-content: space-between;
      gap: 12px;
    }
    .teacher-summary-card span {
      font-size: 12px;
      font-weight: 800;
      color: #64748b;
      text-transform: uppercase;
      letter-spacing: .03em;
    }
    .teacher-summary-card strong {
      font-size: 30px;
      line-height: 1;
      color: #0f172a;
    }
    .teacher-summary-card p {
      margin: 0;
      font-size: 12px;
      color: #64748b;
    }
    .teacher-summary-card.summary-blue {
      border-top: 3px solid #2563eb;
    }
    .teacher-summary-card.summary-green {
      border-top: 3px solid #16a34a;
    }
    .teacher-summary-card.summary-yellow {
      border-top: 3px solid #f59e0b;
    }
    .teacher-summary-card.summary-gray {
      border-top: 3px solid #94a3b8;
    }
    .mo-job-layout {
      display: grid;
      grid-template-columns: minmax(300px, 0.32fr) minmax(0, 0.68fr);
      gap: 22px;
      align-items: start;
      --workflow-panel-height: 760px;
    }
    .card,
    .mo-form-card,
    .mo-feed-card,
    .mo-history-card {
      border: 1px solid #e4eaf4;
      border-radius: 8px;
      background: #ffffff;
      box-shadow: 0 12px 34px rgba(24, 45, 84, 0.06);
    }
    .mo-form-card,
    .mo-feed-card,
    .mo-history-card {
      padding: 22px;
    }
    .mo-form-card {
      position: sticky;
      top: 110px;
      align-self: start;
      min-height: var(--workflow-panel-height);
    }
    .mo-feed-card {
      height: var(--workflow-panel-height);
      display: flex;
      flex-direction: column;
      min-height: 0;
    }
    .mo-feed-card #jobsNotice {
      flex: 0 0 auto;
    }
    .mo-feed-card #jobsEmpty,
    .mo-feed-card #jobsFeed {
      min-height: 0;
    }
    .mo-feed-card #jobsFeed {
      flex: 1 1 auto;
      overflow-y: auto;
      padding-right: 8px;
      margin-right: -8px;
      scrollbar-gutter: stable;
    }
    .mo-feed-card #jobsFeed::-webkit-scrollbar {
      width: 8px;
    }
    .mo-feed-card #jobsFeed::-webkit-scrollbar-thumb {
      background: #cbd5e1;
      border-radius: 999px;
    }
    .teacher-section-head {
      display: flex;
      align-items: flex-start;
      justify-content: space-between;
      gap: 14px;
      margin-bottom: 16px;
    }
    .teacher-section-head h3 {
      margin: 0 0 4px;
      color: #0f172a;
    }
    .teacher-section-head p {
      margin: 0;
      color: #64748b;
      font-size: 13px;
      line-height: 1.45;
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
      min-width: auto;
      border-radius: 999px;
      padding: 4px 9px;
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
      border: 1px solid #e4eaf4;
      border-radius: 8px;
      background: #fff;
      padding: 16px;
      margin-bottom: 14px;
      box-shadow: 0 10px 24px rgba(24, 45, 84, 0.05);
    }
    .mo-job-card:last-child {
      margin-bottom: 0;
    }
    .mo-job-card-head {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      gap: 12px;
      margin-bottom: 12px;
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
    .mo-job-badges {
      display: flex;
      flex-wrap: wrap;
      justify-content: flex-end;
      gap: 6px;
      max-width: 360px;
    }
    .mo-job-main-grid {
      display: grid;
      grid-template-columns: repeat(4, minmax(0, 1fr));
      gap: 10px;
      padding: 12px;
      border-radius: 8px;
      background: #f8fbff;
      border: 1px solid #edf2f7;
      margin-bottom: 12px;
    }
    .mo-job-main-grid span,
    .mo-job-detail-grid span {
      display: block;
      margin-bottom: 4px;
      font-size: 11px;
      font-weight: 800;
      color: #64748b;
      text-transform: uppercase;
      letter-spacing: .03em;
    }
    .mo-job-main-grid strong,
    .mo-job-detail-grid strong {
      display: block;
      color: #0f172a;
      font-size: 13px;
      line-height: 1.35;
      overflow-wrap: anywhere;
    }
    .mo-job-detail-grid {
      display: grid;
      grid-template-columns: repeat(3, minmax(0, 1fr));
      gap: 10px;
      padding: 0 2px 4px;
      margin-bottom: 8px;
    }
    .mo-rejection-note {
      margin: 8px 0 0;
      padding: 9px 10px;
      border-radius: 8px;
      background: #fef2f2;
      color: #b91c1c;
      font-size: 13px;
    }
    .mo-job-card .notice {
      margin-bottom: 0;
    }
    .mo-more-actions {
      position: relative;
    }
    .mo-more-actions summary {
      list-style: none;
      cursor: pointer;
    }
    .mo-more-actions summary::-webkit-details-marker {
      display: none;
    }
    .mo-more-actions-menu {
      position: absolute;
      right: 0;
      top: calc(100% + 6px);
      z-index: 20;
      width: 180px;
      padding: 8px;
      border: 1px solid #dbe2ee;
      border-radius: 8px;
      background: #ffffff;
      box-shadow: 0 18px 34px rgba(15, 23, 42, 0.14);
      display: grid;
      gap: 6px;
    }
    .mo-more-actions-menu .btn {
      width: 100%;
      justify-content: flex-start;
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
      border: 1px dashed #cbd5e1;
      border-radius: 8px;
      background: #f8fbff;
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
      border: 1px solid #e4eaf4;
      background: #fff;
      border-radius: 8px;
      padding: 12px;
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
    .mo-notification-item--announcement {
      align-items: flex-start;
    }
    .mo-notification-badge {
      display: inline-block;
      margin-bottom: 6px;
      padding: 2px 8px;
      border-radius: 999px;
      font-size: 10px;
      font-weight: 700;
      text-transform: uppercase;
      letter-spacing: 0.04em;
      background: #dbeafe;
      color: #1d4ed8;
    }
    .mo-notification-announcement-title {
      margin: 0 0 4px;
      font-size: 14px;
      font-weight: 700;
      color: #0f172a;
    }
    .mo-notification-announcement-body {
      margin: 0;
      font-size: 13px;
      color: #334155;
      line-height: 1.45;
      white-space: pre-wrap;
    }
    .mo-history-card {
      margin-top: 24px;
      box-shadow: 0 8px 22px rgba(24, 45, 84, 0.045);
    }
    .mo-history-head {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      gap: 12px;
      flex-wrap: wrap;
      margin-bottom: 12px;
    }
    .mo-history-table-wrap {
      overflow-x: auto;
      border: 1px solid #e4eaf4;
      border-radius: 8px;
      background: #fff;
    }
    .mo-history-table {
      width: 100%;
      border-collapse: collapse;
      min-width: 920px;
    }
    .mo-history-table th,
    .mo-history-table td {
      padding: 14px 16px;
      border-bottom: 1px solid #e5e7eb;
      text-align: left;
      vertical-align: top;
      font-size: 14px;
    }
    .mo-history-table th {
      background: #fbfdff;
      color: #334155;
      font-size: 12px;
      text-transform: uppercase;
      letter-spacing: .03em;
    }
    .mo-history-table tr:last-child td {
      border-bottom: none;
    }
    .mo-history-actions {
      display: flex;
      flex-wrap: wrap;
      gap: 6px;
    }
    .mo-history-actions .btn {
      padding: 6px 10px;
      font-size: 12px;
    }
    .mo-history-counts {
      display: inline-flex;
      gap: 8px;
      align-items: center;
    }
    .mo-history-counts span {
      border-radius: 999px;
      padding: 3px 8px;
      background: #f1f5f9;
      color: #334155;
      font-size: 12px;
      font-weight: 700;
    }
    .mo-modal-mask {
      position: fixed;
      inset: 0;
      display: none;
      align-items: center;
      justify-content: center;
      background: rgba(15, 23, 42, 0.45);
      z-index: 1000;
      padding: 20px;
    }
    .mo-modal-mask.open {
      display: flex;
    }
    .mo-modal {
      width: min(980px, 96vw);
      max-height: 88vh;
      overflow: auto;
      border-radius: 8px;
      background: #fff;
      border: 1px solid #dbe2ee;
      box-shadow: 0 24px 48px rgba(15, 23, 42, 0.25);
      padding: 18px;
    }
    .mo-modal-head {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      gap: 12px;
      margin-bottom: 12px;
    }
    .mo-modal-head h3 {
      margin: 0 0 4px;
    }
    .mo-details-table {
      width: 100%;
      border-collapse: collapse;
      min-width: 780px;
    }
    .mo-details-table th,
    .mo-details-table td {
      padding: 10px 12px;
      border-bottom: 1px solid #e5e7eb;
      text-align: left;
      font-size: 13px;
      vertical-align: top;
    }
    .mo-details-table th {
      background: #f8fafc;
      color: #334155;
    }
    .mo-table-scroll {
      overflow-x: auto;
      border: 1px solid #e4eaf4;
      border-radius: 8px;
    }
    @media (max-width: 960px) {
      .teacher-shell {
        grid-template-columns: 1fr;
      }
      .mo-portal-header {
        position: static;
        min-height: 0;
      }
      .mo-portal-header-inner {
        min-height: 0;
      }
      .teacher-topbar,
      .mo-portal-main {
        padding-left: 20px;
        padding-right: 20px;
      }
      .mo-job-layout {
        grid-template-columns: 1fr;
      }
      .mo-form-card {
        position: static;
        min-height: 0;
      }
      .mo-feed-card {
        height: auto;
      }
      .mo-feed-card #jobsFeed {
        overflow: visible;
        padding-right: 0;
        margin-right: 0;
      }
      .teacher-summary-grid {
        grid-template-columns: repeat(2, minmax(0, 1fr));
      }
      .mo-job-main-grid,
      .mo-job-detail-grid {
        grid-template-columns: repeat(2, minmax(0, 1fr));
      }
    }
    @media (max-width: 640px) {
      .teacher-topbar {
        grid-template-columns: 1fr;
      }
      .teacher-top-actions {
        justify-content: flex-start;
      }
      .teacher-summary-grid,
      .mo-job-main-grid,
      .mo-job-detail-grid {
        grid-template-columns: 1fr;
      }
      .mo-job-card-head {
        display: block;
      }
      .mo-job-badges {
        justify-content: flex-start;
        margin-top: 10px;
      }
    }
  </style>
</head>
<body class="mo-portal teacher-portal-page">
<div class="teacher-shell">
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
      <a class="mo-tab" href="teacher-profile.jsp">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
          <path d="M20 21a8 8 0 0 0-16 0"></path>
          <circle cx="12" cy="7" r="4"></circle>
        </svg>
        Profile
      </a>
    </nav>
    <a class="mo-btn-logout" href="<%= request.getContextPath() %>/logout">Logout</a>
  </div>
</header>

<div class="teacher-main">
<header class="teacher-topbar">
  <div>
    <h1>My Job Workflow</h1>
    <p>Submit demands, publish approved jobs, and manage your recruitment workflow.</p>
  </div>
  <div class="teacher-top-actions">
    <button id="notificationBtn" class="btn btn-outline" type="button">Notifications <span id="notificationDot" class="mo-notification-dot" style="display:none">0</span></button>
    <button id="reloadBtn" class="btn btn-outline" type="button">Refresh</button>
  </div>
</header>

<main class="mo-portal-main">
  <div id="notificationPanel" class="mo-notification-panel"></div>

  <div id="globalNotice" class="notice" style="margin-bottom:16px"></div>

  <section class="teacher-summary-grid" aria-label="Workflow summary">
    <article class="teacher-summary-card summary-blue">
      <span>Total Jobs</span>
      <strong id="summaryTotalJobs">0</strong>
      <p>Current workflow records</p>
    </article>
    <article class="teacher-summary-card summary-green">
      <span>Published Jobs</span>
      <strong id="summaryPublishedJobs">0</strong>
      <p>Visible to students</p>
    </article>
    <article class="teacher-summary-card summary-yellow">
      <span>Pending Demands</span>
      <strong id="summaryPendingDemands">0</strong>
      <p>Awaiting approval</p>
    </article>
    <article class="teacher-summary-card summary-blue">
      <span>Total Applicants</span>
      <strong id="summaryTotalApplicants">0</strong>
      <p>From job history</p>
    </article>
    <article class="teacher-summary-card summary-green">
      <span>Hired</span>
      <strong id="summaryHired">0</strong>
      <p>Confirmed hires</p>
    </article>
  </section>

  <section class="mo-job-layout">
    <div class="card mo-form-card">
      <div class="teacher-section-head">
        <div>
          <h3>Submit New Demand</h3>
          <p>Quick action panel for creating a teaching assistant demand.</p>
        </div>
      </div>

      <form id="demandForm">
        <div class="field">
          <label for="courseName">Course Name</label>
          <input id="courseName" type="text" placeholder="e.g. EBU6304 Software Engineering" required />
        </div>
        <div class="field">
          <label for="department">Department</label>
          <input id="department" type="text" placeholder="e.g. Computer Science" required />
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
        <div class="field">
          <label for="demandRequirements">Demand Notes</label>
          <textarea id="demandRequirements" maxlength="500" placeholder="Brief workload or skill notes for admin review"></textarea>
        </div>
        <div class="row" style="margin-top:12px;">
          <button class="btn btn-primary" type="submit">Submit Demand</button>
          <button class="btn btn-outline" type="reset">Clear</button>
        </div>
      </form>

    </div>

    <div class="card mo-feed-card">
      <div class="teacher-section-head">
        <div>
          <h3>My Demand Progress</h3>
          <p>Approved jobs can be published here. Offline actions remain locked when active applications exist.</p>
        </div>
      </div>
      <div id="jobsNotice" class="notice"></div>
      <div id="jobsEmpty" class="mo-empty-tip" style="display:none;">No MO jobs found yet. Submit your first demand from the panel on the left.</div>
      <div id="jobsFeed"></div>
    </div>
  </section>
  <section class="card mo-history-card">
    <div class="mo-history-head">
      <div>
        <h3>Job History</h3>
        <p class="desc">Secondary archive for released jobs, applicant statistics, reuse, and exports.</p>
      </div>
      <button id="historyReloadBtn" class="btn btn-outline" type="button">Refresh History</button>
    </div>
    <div id="historyNotice" class="notice"></div>
    <div id="historyEmpty" class="mo-empty-tip" style="display:none;">No job history found yet.</div>
    <div class="mo-history-table-wrap">
      <table class="mo-history-table" aria-label="Job history table">
        <thead>
          <tr>
            <th>Job Title</th>
            <th>Status</th>
            <th>Applicants</th>
            <th>Hired</th>
            <th>Release Time</th>
            <th>Deadline</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody id="historyTableBody"></tbody>
      </table>
    </div>
  </section>

  <div id="historyDetailsModal" class="mo-modal-mask" role="dialog" aria-modal="true" aria-labelledby="historyDetailsTitle">
    <div class="mo-modal">
      <div class="mo-modal-head">
        <div>
          <h3 id="historyDetailsTitle">Job Details</h3>
          <p id="historyDetailsSubtitle" class="desc"></p>
        </div>
        <button id="historyDetailsCloseBtn" class="btn btn-outline" type="button">Close</button>
      </div>
      <div id="historyDetailsNotice" class="notice"></div>
      <div class="row" style="margin-bottom:12px;">
        <button id="modalExportAllCsvBtn" class="btn btn-outline" type="button">Export All CSV</button>
        <button id="modalExportShortlistedCsvBtn" class="btn btn-outline" type="button">Export Shortlisted CSV</button>
        <button id="modalExportAllJsonBtn" class="btn btn-outline" type="button">Export All JSON</button>
        <button id="modalExportShortlistedJsonBtn" class="btn btn-outline" type="button">Export Shortlisted JSON</button>
      </div>
      <div class="mo-table-scroll">
        <table class="mo-details-table" aria-label="Applicant detail table">
          <thead>
            <tr>
              <th>Name</th>
              <th>Applicant ID</th>
              <th>Major</th>
              <th>Application Time</th>
              <th>Status</th>
              <th>Skills</th>
            </tr>
          </thead>
          <tbody id="historyDetailsBody"></tbody>
        </table>
      </div>
    </div>
  </div>
</main>
</div>
</div>
<script src="../assets/js/common.js?v=teacher-student-style"></script>
<script src="../assets/js/teacher.js?v=teacher-student-style"></script>
</body>
</html>
