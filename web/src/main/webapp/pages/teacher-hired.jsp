<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <title>Hired Students - Module Organiser Portal</title>
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
      background: linear-gradient(90deg, #eef5ff 0, #f8fbff 248px, #ffffff 248px, #f7faff 100%);
    }
    .teacher-main { min-width: 0; }
    .teacher-topbar {
      min-height: 82px;
      display: grid;
      grid-template-columns: minmax(0, 1fr) auto;
      align-items: center;
      gap: 20px;
      padding: 18px 44px;
      background: rgba(255,255,255,.88);
      border-bottom: 1px solid #e7edf7;
    }
    .teacher-topbar h1 { margin: 0; font-size: 22px; color: #0f172a; }
    .teacher-topbar p { margin: 5px 0 0; color: #64748b; font-size: 13px; }
    .mo-portal-main { padding: 28px 44px 36px; }
    .hired-toolbar {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 14px;
      flex-wrap: wrap;
      margin-bottom: 16px;
    }
    .hired-filter {
      display: inline-flex;
      align-items: center;
      gap: 10px;
      flex-wrap: wrap;
    }
    .hired-filter select {
      min-width: 220px;
      border: 1px solid #dbe2ee;
      border-radius: 8px;
      padding: 9px 10px;
      background: #fff;
    }
    .history-toggle {
      display: inline-flex;
      align-items: center;
      gap: 8px;
      color: #334155;
      font-size: 13px;
      font-weight: 700;
    }
    .hired-grid {
      display: grid;
      gap: 16px;
    }
    .course-group {
      border: 1px solid #e4eaf4;
      border-radius: 8px;
      background: #fff;
      padding: 16px;
      box-shadow: 0 12px 34px rgba(24,45,84,.05);
    }
    .course-group-head {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 12px;
      margin-bottom: 12px;
      padding-bottom: 12px;
      border-bottom: 1px solid #edf2f7;
    }
    .course-group-head h3 { margin: 0; color: #0f172a; font-size: 17px; }
    .course-count { color: #64748b; font-size: 13px; font-weight: 700; }
    .course-ta-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(700px, 1fr));
      gap: 16px;
      align-items: stretch;
    }
    .hired-card {
      display: flex;
      flex-direction: column;
      height: 100%;
      border: 1px solid #e4eaf4;
      border-radius: 8px;
      background: #fbfdff;
      padding: 18px;
      box-sizing: border-box;
    }
    .hired-card-head {
      display: flex;
      align-items: flex-start;
      justify-content: space-between;
      gap: 12px;
      margin-bottom: 12px;
    }
    .hired-card h3 {
      margin: 0;
      color: #0f172a;
      font-size: 17px;
    }
    .hired-meta {
      margin: 4px 0 0;
      color: #64748b;
      font-size: 13px;
    }
    .hired-detail-grid {
      display: grid;
      grid-template-columns: repeat(3, minmax(0, 1fr));
      gap: 12px;
      margin: 14px 0 16px;
    }
    .hired-detail-grid > div {
      min-height: 72px;
      padding: 12px 14px;
      border: 1px solid #e8eef7;
      border-radius: 10px;
      background: #f8fbff;
      box-sizing: border-box;
      display: flex;
      flex-direction: column;
      justify-content: center;
    }
    .hired-detail-grid span {
      display: block;
      color: #64748b;
      font-size: 11px;
      font-weight: 700;
      text-transform: uppercase;
      letter-spacing: .03em;
    }
    .hired-detail-grid div div {
      color: #111827;
      font-size: 14px;
      margin-top: 5px;
      overflow-wrap: anywhere;
      line-height: 1.35;
    }
    .hired-card .hired-detail-grid {
      flex: 1;
      align-content: start;
    }
    .hired-card .hired-card-head {
      min-height: 62px;
    }
    @media (max-width: 900px) {
      .course-ta-grid {
        grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
      }
      .hired-detail-grid {
        grid-template-columns: repeat(2, minmax(0, 1fr));
      }
    }
    @media (max-width: 640px) {
      .hired-detail-grid {
        grid-template-columns: 1fr;
      }
    }
    .status-pill {
      display: inline-block;
      min-width: 78px;
      text-align: center;
      padding: 4px 10px;
      border-radius: 999px;
      font-size: 12px;
      font-weight: 700;
    }
    .status-hired { background:#d1fae5; color:#065f46; border:1px solid #6ee7b7; }
    .status-resigned,
    .status-dismissed { background:#f1f5f9; color:#475569; border:1px solid #cbd5e1; }
    .dismiss-overlay {
      position: fixed;
      inset: 0;
      display: none;
      align-items: center;
      justify-content: center;
      padding: 24px;
      background: rgba(15,23,42,.44);
      z-index: 50;
    }
    .dismiss-overlay.open { display: flex; }
    .dismiss-modal {
      width: min(520px, 100%);
      border-radius: 8px;
      background: #fff;
      box-shadow: 0 24px 70px rgba(15,23,42,.24);
      padding: 20px;
    }
    .dismiss-modal-head {
      display: flex;
      align-items: flex-start;
      justify-content: space-between;
      gap: 16px;
      margin-bottom: 14px;
    }
    .dismiss-modal-head h3 { margin: 0; color: #0f172a; font-size: 19px; }
    .dismiss-modal-head p { margin: 5px 0 0; color: #64748b; font-size: 13px; }
    .dismiss-reason {
      width: 100%;
      resize: vertical;
      border: 1px solid #dbe2ee;
      border-radius: 8px;
      padding: 10px;
      font: inherit;
      box-sizing: border-box;
    }
    .dismiss-actions {
      display: flex;
      justify-content: flex-end;
      gap: 10px;
      margin-top: 16px;
    }
    .empty-state {
      text-align: center;
      background: #fff;
      border: 2px dashed #d1d5db;
      border-radius: 8px;
      padding: 42px 24px;
      color: #64748b;
    }
    .mo-portal-header {
      position: sticky;
      top: 0;
      height: 100vh;
      background: rgba(245,250,255,.82);
      padding: 28px 20px;
      border-right: 1px solid rgba(219,226,238,.7);
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
    .mo-portal-brand { display: flex; align-items: center; gap: 12px; padding: 6px 8px; }
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
    .mo-portal-icon svg { width: 23px; height: 23px; }
    .mo-portal-brand h1 { margin: 0; font-size: 18px; }
    .mo-portal-brand p { margin: 5px 0 0; color: #64748b; font-size: 13px; }
    .mo-tabs { display: flex; flex-direction: column; gap: 12px; margin: 6px 0 0; padding: 0; }
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
    .mo-tab svg { width: 16px; height: 16px; flex-shrink: 0; color: #475569; }
    .mo-tab:hover, .mo-tab.active {
      color: #2563eb;
      background: #dfeaff;
      border-color: rgba(37, 99, 235, 0.06);
      box-shadow: inset 0 0 0 1px rgba(37, 99, 235, 0.06);
    }
    .mo-tab:hover svg, .mo-tab.active svg { color: #2563eb; }
    .mo-btn-logout { margin-top: auto; width: 100%; justify-content: center; border-radius: 8px; background: #dfeaff; color: #1d4ed8; box-shadow: none; }
  </style>
</head>
<body class="mo-portal teacher-portal-page">
<div class="teacher-shell">
<aside class="mo-portal-header" aria-label="Teacher navigation">
  <div class="mo-portal-header-inner">
    <div class="mo-portal-brand">
      <div class="mo-portal-icon" aria-hidden="true">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
          <path d="M4 7h16" />
          <path d="M6 7v12h12V7" />
          <path d="M9 11h6" />
          <path d="M9 15h6" />
        </svg>
      </div>
      <div>
        <h1>Module Organiser</h1>
        <p>Hired TA Management</p>
      </div>
    </div>

    <nav class="mo-tabs" aria-label="Teacher modules">
      <a class="mo-tab" href="<%= request.getContextPath() %>/pages/teacher.jsp">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
          <path d="M4 10.5L12 4l8 6.5" />
          <path d="M6 9.5V20h12V9.5" />
          <path d="M10 20v-6h4v6" />
        </svg>
        <span>My Jobs</span>
      </a>
      <a class="mo-tab" href="<%= request.getContextPath() %>/pages/mo-applications.jsp">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
          <path d="M7 7h10" />
          <path d="M7 12h10" />
          <path d="M7 17h6" />
          <path d="M5 5h14v14H5z" />
        </svg>
        <span>Applications</span>
      </a>
      <a class="mo-tab active" href="<%= request.getContextPath() %>/pages/teacher-hired.jsp">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
          <path d="M4 19h16" />
          <path d="M7 19V10" />
          <path d="M12 19V5" />
          <path d="M17 19v-8" />
        </svg>
        <span>Hired TAs</span>
      </a>
      <a class="mo-tab" href="<%= request.getContextPath() %>/pages/teacher-profile.jsp">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
          <path d="M12 12a4 4 0 1 0-4-4 4 4 0 0 0 4 4z" />
          <path d="M4.5 20a7.5 7.5 0 0 1 15 0" />
        </svg>
        <span>Profile</span>
      </a>
    </nav>

    <a class="mo-btn-logout" href="<%= request.getContextPath() %>/logout">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" style="width:16px;height:16px;flex-shrink:0;">
        <path d="M10 17l5-5-5-5" />
        <path d="M15 12H4" />
        <path d="M20 4v16" />
      </svg>
      <span>Logout</span>
    </a>
  </div>
</aside>
<div class="teacher-main">
    <header class="teacher-topbar">
      <div>
        <h1>Hired Students</h1>
        <p>Manage current TAs separately from applicants. Former TAs stay in history when you choose to view them.</p>
      </div>
      <button id="refreshHiredStudentsBtn" class="btn btn-outline" type="button">Refresh</button>
    </header>

    <main class="mo-portal-main">
      <div id="hiredNotice" class="notice" style="margin-bottom:16px"></div>
      <section class="card" style="padding:18px;margin-bottom:18px;">
        <div class="hired-toolbar">
          <div>
            <h2 style="margin:0;color:#0f172a;font-size:18px;">Hired TAs by Course</h2>
            <p style="margin:6px 0 0;color:#64748b;font-size:13px;">Current Hired records are shown by default.</p>
          </div>
          <div class="hired-filter">
            <label class="history-toggle" for="showHiredHistoryToggle">
              <input id="showHiredHistoryToggle" type="checkbox" />
              Show history
            </label>
            <label for="hiredJobFilter" style="font-weight:700;color:#334155;">Job</label>
            <select id="hiredJobFilter">
              <option value="">All jobs</option>
            </select>
          </div>
        </div>
        <div id="hiredStudentsEmpty" class="empty-state" style="display:none;">No hired students found.</div>
        <div id="hiredStudentsFeed" class="hired-grid"></div>
      </section>
    </main>
  </div>
</div>
<div class="dismiss-overlay" id="dismissDialogOverlay" aria-hidden="true">
  <div class="dismiss-modal" role="dialog" aria-modal="true" aria-labelledby="dismissDialogTitle">
    <div class="dismiss-modal-head">
      <div>
        <h3 id="dismissDialogTitle">Dismiss TA</h3>
        <p id="dismissDialogSubtitle">Are you sure you want to dismiss this TA from the position?</p>
      </div>
      <button id="closeDismissDialogBtn" class="btn btn-outline" type="button">Close</button>
    </div>
    <p class="notice" style="margin-bottom:10px;">Student: <strong id="dismissStudentName">Selected TA</strong></p>
    <label for="dismissReasonInput" style="display:block;font-weight:700;color:#334155;margin-bottom:6px;">Reason</label>
    <textarea id="dismissReasonInput" class="dismiss-reason" rows="4" maxlength="200" placeholder="Briefly explain why this TA is being dismissed."></textarea>
    <div class="dismiss-actions">
      <button id="cancelDismissBtn" class="btn btn-outline" type="button">Cancel</button>
      <button id="confirmDismissBtn" class="btn btn-primary" type="button">Dismiss TA</button>
    </div>
  </div>
</div>
<script src="../assets/js/common.js?v=teacher-student-style"></script>
<script src="../assets/js/teacher-hired.js?v=teacher-student-style"></script>
</body>
</html>
