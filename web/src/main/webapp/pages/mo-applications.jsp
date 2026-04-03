<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <title>MO Applicants - TA Recruitment Platform</title>
  <link rel="stylesheet" href="../assets/css/main.css" />
  <style>
    .two-col {
      display: grid;
      grid-template-columns: 2fr 1fr;
      gap: 16px;
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
      background: #fff5dd;
      color: #a05f00;
      border: 1px solid #ffe1a6;
    }
    .status-viewed {
      background: #e8f9ee;
      color: #0d8e44;
      border: 1px solid #b7ebca;
    }
    .click-row {
      cursor: pointer;
    }
    .click-row:hover {
      background: #f8fbff;
    }
    .click-row.active {
      background: #eef4ff;
    }
    .detail-grid {
      display: grid;
      grid-template-columns: 1fr;
      gap: 12px;
    }
    .detail-box {
      border: 1px solid var(--border);
      border-radius: 10px;
      padding: 10px;
      background: #fff;
    }
    @media (max-width: 900px) {
      .two-col {
        grid-template-columns: 1fr;
      }
    }
  </style>
</head>
<body>
<div class="app-shell">
  <div class="container">
    <div class="topbar">
      <strong>Module Organiser Portal - Applicants</strong>
      <div class="row">
        <a href="teacher.jsp">My Jobs</a>
        <a href="login.jsp">Logout</a>
      </div>
    </div>

    <div class="card" style="margin-bottom:16px">
      <h3>Applicant Management</h3>
      <p class="desc">View applications for your own jobs and track status transitions.</p>
      <div class="row">
        <div class="field" style="min-width:240px; margin:0;">
          <label for="jobIdInput">Filter by Job ID (optional)</label>
          <input id="jobIdInput" type="text" placeholder="e.g. 101" />
        </div>
        <button id="queryBtn" class="btn btn-primary" type="button">Query</button>
        <button id="resetBtn" class="btn btn-outline" type="button">Reset</button>
      </div>
      <p id="pageNotice" class="notice"></p>
    </div>

    <div class="two-col">
      <div class="card">
        <h3>Application List</h3>
        <div class="table-wrap">
          <table>
            <thead>
            <tr>
              <th>Name</th>
              <th>Student No</th>
              <th>Course Grade</th>
              <th>Applied At</th>
              <th>Status</th>
            </tr>
            </thead>
            <tbody id="applicationsBody"></tbody>
          </table>
        </div>
      </div>

      <div class="card">
        <h3>Application Detail</h3>
        <div id="detailEmpty" class="notice">Select one application record to view detail.</div>
        <div id="detailPanel" class="detail-grid" style="display:none;">
          <div class="detail-box"><strong>Application ID</strong><div id="dApplicationId"></div></div>
          <div class="detail-box"><strong>Job ID</strong><div id="dJobId"></div></div>
          <div class="detail-box"><strong>Student Name</strong><div id="dStudentName"></div></div>
          <div class="detail-box"><strong>Student No</strong><div id="dStudentNo"></div></div>
          <div class="detail-box"><strong>Course Grade</strong><div id="dCourseGrade"></div></div>
          <div class="detail-box"><strong>Applied At</strong><div id="dAppliedAt"></div></div>
          <div class="detail-box"><strong>Status</strong><div id="dStatus"></div></div>
          <div class="detail-box"><strong>Updated At</strong><div id="dUpdatedAt"></div></div>
        </div>
      </div>
    </div>
  </div>
</div>
<script src="../assets/js/common.js"></script>
<script src="../assets/js/mo-applications.js"></script>
</body>
</html>
