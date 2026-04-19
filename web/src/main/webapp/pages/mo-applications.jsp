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
      background: #fef3c7;
      color: #92400e;
      border: 1px solid #fcd34d;
    }
    .status-shortlisted {
      background: #dbeafe;
      color: #1e40af;
      border: 1px solid #93c5fd;
    }
    .status-hired {
      background: #d1fae5;
      color: #065f46;
      border: 1px solid #6ee7b7;
    }
    .status-rejected {
      background: #fee2e2;
      color: #991b1b;
      border: 1px solid #fca5a5;
    }
    .mo-filter-row .field {
      margin-bottom: 0;
    }
    .mo-legend {
      background: #eff6ff;
      border: 1px solid #bfdbfe;
      border-radius: 12px;
      padding: 14px 16px;
      margin-bottom: 20px;
    }
    .mo-legend h3 {
      margin: 0 0 10px;
      font-size: 15px;
      color: #1e3a5f;
    }
    .mo-legend-row {
      display: flex;
      flex-wrap: wrap;
      gap: 16px;
      font-size: 13px;
      color: #1e40af;
    }
    .mo-legend-dot {
      display: inline-block;
      width: 10px;
      height: 10px;
      border-radius: 999px;
      margin-right: 6px;
      vertical-align: middle;
    }
    .mo-job-group-bar {
      display: flex;
      justify-content: space-between;
      align-items: baseline;
      flex-wrap: wrap;
      gap: 8px;
      margin-bottom: 12px;
    }
    .mo-job-group-bar .mo-pos-hrs {
      font-size: 13px;
      color: #64748b;
    }
    .mo-app-card-proto {
      border-radius: 12px;
      margin-bottom: 16px;
      padding: 20px;
      background: #fff;
      border: 1px solid #e5e7eb;
      box-shadow: 0 4px 14px rgba(15, 23, 42, 0.06);
    }
    .mo-app-card-proto.mo-wl-low { border: 2px solid #3b82f6; }
    .mo-app-card-proto.mo-wl-normal { border: 2px solid #22c55e; }
    .mo-app-card-proto.mo-wl-warn { border: 2px solid #eab308; }
    .mo-app-card-proto.mo-wl-over { border: 2px solid #ef4444; }
    .mo-app-card-proto.mo-wl-neutral { border: 1px solid #e5e7eb; }
    .mo-wl-panel {
      border-radius: 10px;
      padding: 14px;
      margin: 14px 0;
    }
    .mo-wl-panel.mo-wl-low { background: #eff6ff; border: 2px solid #3b82f6; }
    .mo-wl-panel.mo-wl-normal { background: #ecfdf5; border: 2px solid #22c55e; }
    .mo-wl-panel.mo-wl-warn { background: #fefce8; border: 2px solid #eab308; }
    .mo-wl-panel.mo-wl-over { background: #fef2f2; border: 2px solid #ef4444; }
    .mo-wl-grid {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 12px;
      font-size: 13px;
    }
    @media (max-width: 640px) {
      .mo-wl-grid { grid-template-columns: 1fr; }
    }
    .mo-wl-big {
      font-size: 1.5rem;
      font-weight: 700;
    }
    .mo-wl-actions {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
      margin-top: 14px;
      margin-bottom: 16px;
      align-items: center;
      min-height: 44px;
    }
    .mo-wl-actions .btn-success {
      font-weight: 600;
    }
    .mo-job-tools {
      display: flex;
      gap: 8px;
      align-items: center;
      flex-wrap: wrap;
    }
    .mo-closed-flag {
      display: inline-flex;
      align-items: center;
      padding: 4px 10px;
      border-radius: 999px;
      border: 1px solid #fca5a5;
      background: #fee2e2;
      color: #991b1b;
      font-size: 12px;
      font-weight: 700;
    }
    .mo-modal-mask {
      position: fixed;
      inset: 0;
      background: rgba(15, 23, 42, 0.45);
      display: none;
      align-items: center;
      justify-content: center;
      z-index: 999;
      padding: 20px;
    }
    .mo-modal-mask.open {
      display: flex;
    }
    .mo-modal {
      width: min(860px, 96vw);
      max-height: 88vh;
      overflow: auto;
      border-radius: 12px;
      background: #fff;
      border: 1px solid #dbe2ee;
      box-shadow: 0 24px 48px rgba(15, 23, 42, 0.25);
      padding: 18px;
    }
    .mo-modal-head {
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 10px;
      margin-bottom: 10px;
    }
    .mo-modal-list {
      border: 1px solid #e5e7eb;
      border-radius: 10px;
      padding: 10px;
      background: #f8fafc;
      margin-bottom: 12px;
    }
    .mo-modal-row {
      display: grid;
      grid-template-columns: 28px 1fr auto;
      gap: 8px;
      align-items: center;
      border-bottom: 1px dashed #dbe2ee;
      padding: 8px 0;
      font-size: 14px;
    }
    .mo-modal-row:last-child {
      border-bottom: none;
    }
    /* Reference layout: light gray page, white content column (cards stay white on white main) */
    body.mo-portal.mo-applicants-figma {
      background: #f1f5f9 !important;
      min-height: 100vh;
    }
    .mo-applicants-figma .mo-portal-header {
      background: #fff;
      border-bottom: 1px solid #e5e7eb;
    }
    .mo-applicants-figma .mo-portal-main {
      background: #ffffff;
      border-radius: 12px;
      box-shadow: 0 1px 3px rgba(15, 23, 42, 0.06);
      margin-top: 8px;
      margin-bottom: 32px;
    }
    .mo-applicants-figma .mo-tabs {
      border-bottom-color: #e5e7eb;
    }
    .mo-status-toolbar {
      display: flex;
      flex-wrap: wrap;
      align-items: center;
      gap: 14px 20px;
      margin-bottom: 14px;
    }
    .mo-status-toolbar label {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      font-size: 14px;
      color: #334155;
      cursor: pointer;
    }
    .mo-batch-bar {
      display: none;
      flex-wrap: wrap;
      align-items: center;
      gap: 10px;
      padding: 12px 14px;
      margin-bottom: 14px;
      border-radius: 10px;
      background: #f8fafc;
      border: 1px solid #e2e8f0;
    }
    .mo-batch-bar.visible {
      display: flex;
    }
    .mo-app-card-proto.mo-app-card-selected {
      box-shadow: 0 0 0 2px #3b82f6, 0 4px 14px rgba(15, 23, 42, 0.08);
    }
    .mo-field-inline {
      display: flex;
      align-items: flex-start;
      gap: 8px;
      margin: 10px 0 0;
      flex-wrap: wrap;
    }
    .mo-field-inline label {
      font-size: 12px;
      font-weight: 600;
      color: #64748b;
      min-width: 120px;
      padding-top: 6px;
    }
    .mo-field-inline input[type="text"],
    .mo-field-inline textarea {
      flex: 1;
      min-width: 160px;
      max-width: 100%;
      font-size: 13px;
      padding: 6px 10px;
      border-radius: 8px;
      border: 1px solid #e2e8f0;
    }
    .mo-field-inline textarea { min-height: 44px; resize: vertical; }
    .mo-field-inline.mo-field-error input,
    .mo-field-inline.mo-field-error textarea {
      border-color: #dc2626;
    }
    .mo-save-indicator {
      font-size: 12px;
      min-width: 22px;
      color: #64748b;
    }
    .mo-save-indicator.ok { color: #16a34a; }
    .mo-save-indicator.err { color: #dc2626; cursor: help; }
    .mo-fb-count {
      font-size: 11px;
      color: #94a3b8;
      align-self: flex-end;
    }
    .mo-status-select {
      font-size: 13px;
      padding: 4px 8px;
      border-radius: 8px;
      border: 1px solid #cbd5e1;
      background: #fff;
    }
    .mo-privacy-hint {
      font-size: 12px;
      color: #64748b;
      margin: 0 0 8px;
    }
  </style>
</head>
<body class="mo-portal mo-applicants-figma">
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
      <p class="mo-section-desc">Review applications with workload analysis. Decisions are saved on the server. Rejected can be undone with <strong>Undo reject</strong> (restores to viewed).</p>
    </div>
    <button type="button" id="exportCsvBtn" class="btn btn-outline">Export CSV</button>
  </div>

  <div class="mo-legend">
    <h3>Workload Status Legend</h3>
    <div class="mo-legend-row">
      <span><span class="mo-legend-dot" style="background:#3b82f6"></span> Low (&lt;10h/week)</span>
      <span><span class="mo-legend-dot" style="background:#22c55e"></span> Normal (10–14h/week)</span>
      <span><span class="mo-legend-dot" style="background:#eab308"></span> Warning (15–19h/week)</span>
      <span><span class="mo-legend-dot" style="background:#ef4444"></span> Overload (≥20h/week)</span>
    </div>
  </div>

  <div class="card" style="margin-bottom:16px">
    <p class="mo-privacy-hint">Decision feedback is only visible to Module Organisers and Administrators.</p>
    <div class="mo-status-toolbar" id="statusFilterBar">
      <span style="font-weight:600;font-size:14px;color:#0f172a;">Filter by Status:</span>
      <label><input type="checkbox" id="filterPending" checked /> Pending</label>
      <label><input type="checkbox" id="filterShortlisted" checked /> Shortlisted</label>
      <label><input type="checkbox" id="filterRejected" checked /> Rejected</label>
      <label><input type="checkbox" id="filterHired" checked /> Hired</label>
    </div>
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

  <div id="batchBar" class="mo-batch-bar" aria-live="polite">
    <span id="batchBarLabel" style="font-weight:600;color:#0f172a"></span>
    <button type="button" id="batchShortlistBtn" class="btn btn-success" style="font-weight:600">Mark as Shortlisted</button>
    <button type="button" id="batchRejectBtn" class="btn btn-outline" style="color:#b91c1c;border-color:#fecaca">Mark as Rejected</button>
    <button type="button" id="batchPendingBtn" class="btn btn-outline">Mark as Pending</button>
  </div>

  <div id="applicationsEmpty" class="mo-empty-state" style="display:none;" role="status">
    <svg class="mo-empty-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" aria-hidden="true">
      <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"></path>
      <circle cx="9" cy="7" r="4"></circle>
      <path d="M22 21v-2a4 4 0 0 0-3-3.87"></path>
      <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
    </svg>
    <h3>No applicants found</h3>
    <p>There are no active applications for this view. Try adjusting job or status filters.</p>
  </div>

  <div id="applicationsFeed" class="mo-applicant-feed" style="display:none;" aria-live="polite"></div>

  <div id="finalHiringModal" class="mo-modal-mask" aria-hidden="true">
    <div class="mo-modal">
      <div class="mo-modal-head">
        <h3 style="margin:0">Confirm Final Hiring</h3>
        <button id="finalHiringCloseBtn" type="button" class="btn btn-outline">Close</button>
      </div>
      <p class="notice" style="margin:0 0 12px">Job: <strong id="finalHiringJobLabel">-</strong>. Choose final hires from shortlisted applicants, then confirm submission.</p>
      <div id="finalHiringList" class="mo-modal-list"></div>
      <div class="row">
        <button id="finalHiringConfirmBtn" type="button" class="btn btn-primary">Confirm & Submit</button>
      </div>
    </div>
  </div>

  <div id="historyModal" class="mo-modal-mask" aria-hidden="true">
    <div class="mo-modal">
      <div class="mo-modal-head">
        <h3 style="margin:0">Hiring History</h3>
        <button id="historyCloseBtn" type="button" class="btn btn-outline">Close</button>
      </div>
      <p class="notice" style="margin:0 0 12px">Job: <strong id="historyJobLabel">-</strong>.</p>
      <div id="historyList" class="mo-modal-list"></div>
    </div>
  </div>
</main>
<script src="../assets/js/common.js?v=mo6"></script>
<script src="../assets/js/mo-applications.js?v=mo9"></script>
</body>
</html>
