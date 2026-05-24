<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.ta.model.SessionUser" %>
<%!
  private String html(String value) {
    if (value == null) {
      return "";
    }
    return value
      .replace("&", "&amp;")
      .replace("<", "&lt;")
      .replace(">", "&gt;")
      .replace("\"", "&quot;");
  }
%>
<%
  SessionUser currentUser = (SessionUser) session.getAttribute("currentUser");
  String teacherName = currentUser != null ? currentUser.getName() : "Module Organiser";
  String teacherEmail = currentUser != null ? currentUser.getEmail() : "";
  String teacherRole = currentUser != null ? currentUser.getRole() : "teacher";
  String teacherInitial = teacherName == null || teacherName.isEmpty() ? "M" : teacherName.substring(0, 1).toUpperCase();
%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <title>Profile - Module Organiser Portal</title>
  <link rel="stylesheet" href="../assets/css/main.css?v=teacher-profile" />
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
    }
    .mo-portal-icon,
    .profile-avatar-large {
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
      font-weight: 800;
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
    .mo-tabs {
      display: flex;
      flex-direction: column;
      gap: 12px;
      margin: 6px 0 0;
      padding: 0;
      border: 0;
      background: transparent;
    }
    .mo-tab {
      width: 100%;
      min-height: 48px;
      display: flex;
      align-items: center;
      justify-content: flex-start;
      gap: 10px;
      padding: 0 16px;
      color: #475569;
      border-radius: 8px;
      border: 1px solid transparent;
      background: transparent;
      font-size: 14px;
      font-weight: 700;
      text-decoration: none;
      box-shadow: none;
    }
    .mo-tab svg {
      width: 16px;
      height: 16px;
      color: #475569;
      flex-shrink: 0;
    }
    .mo-tab:hover,
    .mo-tab.active {
      color: #2563eb;
      background: #dfeaff;
      border-color: rgba(37, 99, 235, 0.06);
      box-shadow: inset 0 0 0 1px rgba(37, 99, 235, 0.06);
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
    .teacher-main {
      min-width: 0;
      display: flex;
      flex-direction: column;
    }
    .teacher-topbar {
      min-height: 82px;
      display: grid;
      grid-template-columns: minmax(0, 1fr);
      align-items: center;
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
    .teacher-profile-main {
      width: 100%;
      max-width: none;
      margin: 0;
      padding: 28px 44px 36px;
    }
    .profile-grid {
      display: grid;
      grid-template-columns: minmax(280px, 360px) minmax(0, 1fr);
      gap: 22px;
      align-items: start;
    }
    .profile-card {
      border: 1px solid #e4eaf4;
      border-radius: 8px;
      background: #ffffff;
      box-shadow: 0 12px 34px rgba(24, 45, 84, 0.06);
      padding: 22px;
    }
    .profile-summary-head {
      display: flex;
      gap: 14px;
      align-items: center;
      margin-bottom: 18px;
    }
    .profile-summary-head h2,
    .profile-card h3 {
      margin: 0 0 4px;
      color: #0f172a;
    }
    .profile-summary-head p,
    .profile-card .desc {
      margin: 0;
      color: #64748b;
      font-size: 13px;
      line-height: 1.45;
    }
    .profile-info-list {
      display: grid;
      gap: 10px;
    }
    .profile-info-item {
      padding: 12px;
      border-radius: 8px;
      background: #f8fbff;
      border: 1px solid #edf2f7;
    }
    .profile-info-item span {
      display: block;
      margin-bottom: 4px;
      font-size: 11px;
      font-weight: 800;
      color: #64748b;
      text-transform: uppercase;
      letter-spacing: .03em;
    }
    .profile-info-item strong {
      color: #0f172a;
      font-size: 14px;
      overflow-wrap: anywhere;
    }
    .profile-actions {
      margin-top: 16px;
      display: flex;
      gap: 10px;
      flex-wrap: wrap;
    }
    @media (max-width: 960px) {
      .teacher-shell {
        grid-template-columns: 1fr;
      }
      .mo-portal-header {
        position: static;
        height: auto;
      }
      .mo-portal-header-inner {
        min-height: 0;
      }
      .teacher-topbar,
      .teacher-profile-main {
        padding-left: 20px;
        padding-right: 20px;
      }
      .profile-grid {
        grid-template-columns: 1fr;
      }
    }
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
        <p>Account and security</p>
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
      <a class="mo-tab" href="<%= request.getContextPath() %>/pages/teacher-hired.jsp">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
          <path d="M4 19h16" />
          <path d="M7 19V10" />
          <path d="M12 19V5" />
          <path d="M17 19v-8" />
        </svg>
        <span>Hired TAs</span>
      </a>
      <a class="mo-tab active" href="<%= request.getContextPath() %>/pages/teacher-profile.jsp">
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
      <h1>Profile</h1>
      <p>Manage your account information and password.</p>
    </div>
  </header>

  <main class="teacher-profile-main">
    <div id="profileNotice" class="notice" style="margin-bottom:16px"></div>
    <section class="profile-grid">
      <article class="profile-card">
        <div class="profile-summary-head">
          <div class="profile-avatar-large" aria-hidden="true"><%= html(teacherInitial) %></div>
          <div>
            <h2><%= html(teacherName) %></h2>
            <p>Module Organiser account</p>
          </div>
        </div>
        <div class="profile-info-list">
          <div class="profile-info-item">
            <span>Name</span>
            <strong><%= html(teacherName) %></strong>
          </div>
          <div class="profile-info-item">
            <span>Email</span>
            <strong><%= html(teacherEmail) %></strong>
          </div>
          <div class="profile-info-item">
            <span>Role</span>
            <strong><%= html(teacherRole) %></strong>
          </div>
        </div>
      </article>

      <article class="profile-card">
        <h3>Change Password</h3>
        <p class="desc">Update your password for this account.</p>
        <form id="teacherChangePasswordForm">
          <div class="field">
            <label for="teacherOldPassword">Current Password</label>
            <input id="teacherOldPassword" type="password" required />
          </div>
          <div class="field">
            <label for="teacherNewPassword">New Password</label>
            <input id="teacherNewPassword" type="password" required />
          </div>
          <div class="field">
            <label for="teacherConfirmPassword">Confirm New Password</label>
            <input id="teacherConfirmPassword" type="password" required />
          </div>
          <div class="profile-actions">
            <button id="teacherChangePasswordBtn" class="btn btn-primary" type="submit">Change Password</button>
            <button class="btn btn-outline" type="reset">Clear</button>
          </div>
        </form>
      </article>
    </section>
  </main>
</div>
</div>
<script src="../assets/js/common.js?v=teacher-profile"></script>
<script src="../assets/js/teacher-profile.js?v=teacher-profile"></script>
</body>
</html>
