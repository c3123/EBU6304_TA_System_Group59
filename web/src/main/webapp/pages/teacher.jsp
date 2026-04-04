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
        <p>Welcome</p>
      </div>
    </div>
    <a class="mo-btn-logout" href="login.jsp">Logout</a>
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

  <div style="display:flex;flex-wrap:wrap;justify-content:space-between;align-items:flex-start;gap:16px;margin-bottom:20px;">
    <div>
      <h2 class="mo-section-title" style="margin-bottom:4px">My Posted Jobs</h2>
      <p class="mo-section-desc" style="margin-bottom:0">Manage your teaching assistant positions (Sprint 1: job posting &amp; dashboard).</p>
    </div>
    <a class="btn btn-primary" href="#" onclick="alert('Job creation: connect demand and publish APIs when ready.'); return false;">Post New Job</a>
  </div>

  <div class="card">
    <h3 style="margin-top:0">Dashboard placeholder</h3>
    <p class="desc">This page matches the prototype shell (tabs + section header). Wire job cards here to list only your posted jobs and status toggles (Open / Paused) when backend endpoints are ready.</p>
    <div class="row" style="margin-top:12px;">
      <a class="btn btn-outline" href="mo-applications.jsp">View Applicants</a>
    </div>
  </div>
</main>
</body>
</html>
