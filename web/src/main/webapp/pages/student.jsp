<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>Student Portal</title>
  <link rel="stylesheet" href="../assets/css/main.css" />
  <style>
    html, body {
      height: 100%;
      overflow: hidden;
    }

    .app-shell {
      height: 80vh;
      overflow: hidden;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 16px;
      background: radial-gradient(circle at top left, #e7efff, #f7fbff 45%, #f4f7fb 100%);
    }

    .student-portal {
      width: 100%;
      max-width: 1240px;
      height: 100%;
      background: linear-gradient(180deg, #ffffff 0%, #f9fbff 100%);
      border: 1px solid #dbe2ee;
      border-radius: 18px;
      box-shadow: 0 24px 45px rgba(21, 43, 88, 0.12);
      overflow: hidden;
      display: flex;
      flex-direction: column;
    }

    .student-hero {
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 12px;
      padding: 20px 24px;
      border-bottom: 1px solid #dbe2ee;
      background: radial-gradient(circle at top right, #edf3ff, #ffffff 60%);
    }

    .student-title {
      margin: 0;
      font-size: 28px;
      line-height: 1.1;
    }

    .student-subtitle {
      margin: 6px 0 0;
      color: #475569;
      font-size: 14px;
    }

    .student-tabs {
      display: flex;
      gap: 8px;
      padding: 12px 16px;
      border-bottom: 1px solid #dbe2ee;
      background: #ffffff;
      overflow-x: auto;
    }

    .student-tab {
      border: 1px solid #dbe2ee;
      border-radius: 999px;
      background: #ffffff;
      color: #334155;
      font-weight: 600;
      padding: 10px 14px;
      cursor: pointer;
      white-space: nowrap;
    }

    .student-tab.active {
      background: #1e5eff;
      color: #ffffff;
      border-color: #1e5eff;
    }

    .student-content {
      padding: 22px;
      flex: 1;
      overflow: hidden;
      display: flex;
      flex-direction: column;
    }

    .student-panel {
      display: none;
      flex: 1;
      overflow: hidden;
    }

    .student-panel.active {
      display: flex;
      flex-direction: column;
    }

    .module-frame {
      width: 100%;
      max-width: 980px;
      margin: 0 auto;
      background: linear-gradient(180deg, #f4f8ff 0%, #eef4ff 100%);
      border: 1px solid #d4def0;
      border-radius: 16px;
      box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.9), 0 12px 24px rgba(14, 32, 66, 0.08);
      flex: 1;
      overflow-y: auto;
      padding: 18px;
    }

    .student-panel-header {
      text-align: center;
      margin-bottom: 18px;
      flex-shrink: 0;
    }

    .student-panel-header h2 {
      margin: 0;
      font-size: 24px;
    }

    .student-panel-header p {
      margin: 8px 0 0;
      color: #64748b;
    }

    .student-search {
      display: flex;
      gap: 10px;
      justify-content: center;
      flex-wrap: wrap;
      margin-bottom: 18px;
    }

    .student-search input,
    .student-search select {
      width: min(420px, 100%);
      background: #ffffff;
    }

    .jobs-grid,
    .apps-list,
    .profile-wrap {
      width: 100%;
      max-width: none;
      margin: 0;
    }

    .jobs-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
      gap: 14px;
    }

    .apps-list {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
      gap: 14px;
    }

    .job-card,
    .app-item,
    .profile-wrap {
      background: #ffffff;
      border: 1px solid #dbe2ee;
      border-radius: 14px;
      box-shadow: 0 6px 14px rgba(14, 32, 66, 0.08);
    }

    .jobs-grid,
    .apps-list {
      padding: 2px;
    }

    .job-card,
    .app-item {
      padding: 16px;
    }

    .job-card h3,
    .app-item h3 {
      margin: 0;
      font-size: 17px;
    }

    .app-item h3 {
      padding-right: 80px;
    }

    .job-meta,
    .app-meta {
      margin: 8px 0;
      color: #64748b;
      font-size: 13px;
    }

    .job-actions,
    .app-feedback {
      margin-top: 12px;
    }

    .app-feedback {
      background: #f8fbff;
      border: 1px solid #dbe2ee;
      border-radius: 10px;
      padding: 9px 10px;
      color: #334155;
      font-size: 13px;
    }

    .empty-state,
    .loading-state {
      max-width: 700px;
      margin: 0 auto;
      text-align: center;
      background: #ffffff;
      border: 1px dashed #cbd5e1;
      border-radius: 16px;
      padding: 34px 16px;
      color: #475569;
    }

    .hidden {
      display: none;
    }

    .profile-wrap {
      padding: 18px;
      border: none;
      box-shadow: none;
      background: transparent;
    }

    .app-item.status-pending {
      border-left: 5px solid #f59e0b;
    }

    .app-item.status-shortlisted {
      border-left: 5px solid #1e5eff;
    }

    .app-item.status-hired {
      border-left: 5px solid #16a34a;
    }

    .app-item.status-rejected {
      border-left: 5px solid #dc2626;
    }

    .app-item {
      position: relative;
      overflow: visible;
    }

    .withdraw-app-btn {
      position: absolute;
      top: 12px;
      right: 12px;
      padding: 6px 10px;
      background-color: #fee2e2;
      color: #991b1b;
      border: 1px solid #fecaca;
      border-radius: 4px;
      font-size: 12px;
      cursor: pointer;
      font-weight: 500;
      transition: all 0.2s;
      z-index: 10;
    }

    .withdraw-app-btn:hover {
      background-color: #fecaca;
      border-color: #fca5a5;
      box-shadow: 0 2px 4px rgba(220, 38, 38, 0.15);
    }

    .profile-grid {
      display: grid;
      grid-template-columns: repeat(2, minmax(0, 1fr));
      gap: 12px;
    }

    .profile-grid .field-wide {
      grid-column: 1 / -1;
    }

    .readonly {
      background: #f8fafc;
      color: #475569;
    }

    .portal-notice {
      margin-top: 12px;
      text-align: center;
      min-height: 20px;
      color: #1e5eff;
      font-weight: 600;
    }

    .job-actions {
      display: flex;
      gap: 8px;
      flex-wrap: wrap;
    }

    .job-detail-overlay {
      position: fixed;
      inset: 0;
      background: rgba(15, 23, 42, 0.45);
      display: none;
      align-items: center;
      justify-content: center;
      padding: 16px;
      z-index: 1000;
    }

    .job-detail-overlay.open {
      display: flex;
    }

    .job-detail-modal {
      width: min(860px, 100%);
      max-height: 80vh;
      overflow-y: auto;
      background: #ffffff;
      border: 1px solid #dbe2ee;
      border-radius: 16px;
      box-shadow: 0 24px 45px rgba(21, 43, 88, 0.22);
      padding: 18px;
    }

    .job-detail-head {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      gap: 12px;
      margin-bottom: 12px;
    }

    .job-detail-head h3 {
      margin: 0;
      font-size: 22px;
    }

    .job-detail-grid {
      display: grid;
      grid-template-columns: repeat(2, minmax(0, 1fr));
      gap: 10px;
      margin-bottom: 14px;
    }

    .job-detail-block {
      background: #f8fbff;
      border: 1px solid #dbe2ee;
      border-radius: 10px;
      padding: 10px;
    }

    .job-detail-label {
      display: block;
      font-size: 12px;
      color: #64748b;
      margin-bottom: 4px;
    }

    .job-detail-value {
      font-size: 14px;
      color: #0f172a;
      white-space: pre-wrap;
    }

    .job-detail-actions {
      display: flex;
      justify-content: flex-end;
      gap: 8px;
      margin-top: 12px;
    }

    @media (max-width: 760px) {
      .job-detail-grid {
        grid-template-columns: 1fr;
      }

      .student-content {
        padding: 14px;
      }

      .student-title {
        font-size: 23px;
      }

      .profile-grid {
        grid-template-columns: 1fr;
      }
    }
  </style>
</head>
<body>
<div class="app-shell">
  <div class="student-portal">
    <header class="student-hero">
      <div>
        <h1 class="student-title">Student Portal</h1>
        <p class="student-subtitle" id="studentWelcome">Welcome back.</p>
      </div>
      <a class="btn btn-outline" href="<%= request.getContextPath() %>/logout">Logout</a>
    </header>

    <nav class="student-tabs" aria-label="Student modules">
      <button type="button" class="student-tab active" data-tab="jobs">Available Jobs</button>
      <button type="button" class="student-tab" data-tab="applications">My Applications</button>
      <button type="button" class="student-tab" data-tab="profile">Profile</button>
    </nav>

    <main class="student-content">
      <section class="student-panel active" id="panel-jobs" aria-labelledby="Available Jobs">
        <div class="student-panel-header">
          <h2>Available Jobs</h2>
          <p id="jobsCountText">Preparing the latest positions...</p>
        </div>

        <div class="module-frame">
          <div class="student-search">
            <input id="jobSearchInput" type="text" placeholder="Search by module code or title" />
            <select id="jobStatusFilter">
              <option value="all">All Status</option>
              <option value="open">Open</option>
              <option value="closed">Closed</option>
            </select>
          </div>

          <div id="jobsLoading" class="loading-state">Loading job data...</div>
          <div id="jobsEmpty" class="empty-state hidden">No matching positions at the moment.</div>
          <div id="jobsList" class="jobs-grid hidden"></div>
        </div>
      </section>

      <section class="student-panel" id="panel-applications" aria-labelledby="My Applications">
        <div class="student-panel-header">
          <h2>My Applications</h2>
          <p id="appsCountText">Preparing your application history...</p>
        </div>

        <div class="module-frame">
          <div id="appsLoading" class="loading-state">Loading applications...</div>
          <div id="appsEmpty" class="empty-state hidden">You have not submitted any applications yet.</div>
          <div id="appsList" class="apps-list hidden"></div>
        </div>
      </section>

      <section class="student-panel" id="panel-profile" aria-labelledby="Profile">
        <div class="student-panel-header">
          <h2>Profile</h2>
          <p>Update your information and supporting documents.</p>
        </div>

        <div class="module-frame">
          <div class="profile-wrap">
            <div class="profile-grid">
              <div class="field">
                <label for="profileName">Full Name</label>
                <input id="profileName" type="text" />
              </div>
              <div class="field">
                <label for="profileEmail">Email</label>
                <input id="profileEmail" class="readonly" type="text" readonly />
              </div>
              <div class="field">
                <label for="profileStudentId">Student ID</label>
                <input id="profileStudentId" class="readonly" type="text" readonly />
              </div>
              <div class="field">
                <label for="profileProgramme">Programme</label>
                <input id="profileProgramme" class="readonly" type="text" readonly />
              </div>
              <div class="field field-wide">
                <label for="profileSkills">Skills</label>
                <input id="profileSkills" type="text" placeholder="For example: Java, SQL, Python" />
              </div>
              <div class="field field-wide">
                <label for="profileExperience">Experience</label>
                <textarea id="profileExperience" placeholder="Describe your relevant experience"></textarea>
              </div>
            </div>

            <!-- Attachments Section -->
            <div style="margin-top: 24px; border-top: 1px solid #e5e7eb; padding-top: 24px;">
              <h3 style="margin: 0 0 14px 0; font-size: 16px; font-weight: 600;">Supporting Documents</h3>
              <p style="margin: 0 0 14px 0; color: #6b7280; font-size: 13px;">Upload certificates, transcripts, or other supporting documents (Max 50MB total)</p>

              <!-- Upload Area -->
              <div id="uploadArea" style="border: 2px dashed #9ca3af; border-radius: 8px; padding: 20px; text-align: center; cursor: pointer; background-color: #f9fafb; transition: all 0.2s;">
                <p style="margin: 0 0 10px 0; font-size: 14px; font-weight: 500;">Drag & drop files or click to browse</p>
                <p style="margin: 0; font-size: 12px; color: #6b7280;">PDF, DOCX, XLSX, JPG, PNG (Max 50MB total)</p>
                <input id="fileInput" type="file" style="display: none;" />
              </div>

              <!-- Label Selection -->
              <div style="margin-top: 14px;">
                <label for="attachmentLabel" style="display: block; font-size: 13px; font-weight: 500; margin-bottom: 6px;">Document Type</label>
                <div style="display:grid;grid-template-columns:1fr 1fr;gap:10px;align-items:center;">
                  <select id="attachmentLabel" style="width: 100%; padding: 8px 12px; border: 1px solid #d1d5db; border-radius: 6px; font-size: 14px;">
                    <option value="Resume">Resume</option>
                    <option value="Certificate">Certificate</option>
                    <option value="Transcript">Transcript</option>
                    <option value="Custom">Custom...</option>
                  </select>
                  <input id="attachmentCustomLabel" type="text" placeholder="Type custom label" style="display:none;width: 100%; padding: 8px 12px; border: 1px solid #d1d5db; border-radius: 6px; font-size: 14px;" />
                </div>
              </div>

              <!-- Uploaded Files List -->
              <div style="margin-top: 16px;">
                <p style="margin: 0 0 10px 0; font-size: 13px; font-weight: 500;">Uploaded Documents</p>
                <div id="attachmentsList" style="border: 1px solid #e5e7eb; border-radius: 6px; max-height: 250px; overflow-y: auto;">
                  <p style="margin: 14px; text-align: center; color: #6b7280; font-size: 13px;">No documents uploaded yet</p>
                </div>
              </div>
            </div>

            <div class="row" style="margin-top: 20px; justify-content: center;">
              <button type="button" class="btn btn-primary" id="saveProfileBtn">Save Profile</button>
            </div>
          </div>
        </div>
      </section>

      <p id="studentNotice" class="portal-notice" aria-live="polite"></p>
    </main>
  </div>
</div>

      <p id="studentNotice" class="portal-notice" aria-live="polite"></p>
    </main>
  </div>
</div>

<div class="job-detail-overlay" id="jobDetailOverlay" aria-hidden="true">
  <div class="job-detail-modal" role="dialog" aria-modal="true" aria-labelledby="jobDetailTitle">
    <div class="job-detail-head">
      <div>
        <h3 id="jobDetailTitle">Job Detail</h3>
        <p class="notice" id="jobDetailSubtitle">Review details before applying.</p>
      </div>
      <button type="button" class="btn btn-outline" id="closeJobDetailBtn">Close</button>
    </div>

    <div class="job-detail-grid">
      <div class="job-detail-block"><span class="job-detail-label">Module</span><div class="job-detail-value" id="detailModule"></div></div>
      <div class="job-detail-block"><span class="job-detail-label">Teacher</span><div class="job-detail-value" id="detailTeacher"></div></div>
      <div class="job-detail-block"><span class="job-detail-label">Workload</span><div class="job-detail-value" id="detailHours"></div></div>
      <div class="job-detail-block"><span class="job-detail-label">Positions</span><div class="job-detail-value" id="detailPositions"></div></div>
      <div class="job-detail-block"><span class="job-detail-label">Deadline</span><div class="job-detail-value" id="detailDeadline"></div></div>
      <div class="job-detail-block"><span class="job-detail-label">Status</span><div class="job-detail-value" id="detailStatus"></div></div>
    </div>

    <div class="job-detail-block">
      <span class="job-detail-label">Requirements</span>
      <div class="job-detail-value" id="detailRequirements"></div>
    </div>

    <div class="job-detail-block" style="margin-top:10px;">
      <span class="job-detail-label">Submitted Profile Snapshot</span>
      <div class="job-detail-value" id="detailProfileSnapshot"></div>
    </div>

    <div class="job-detail-block" style="margin-top:10px;">
      <span class="job-detail-label">Attachments For This Application</span>
      <p class="notice" id="detailAttachmentHint" style="margin:0 0 8px 0;">At least one attachment is required. All are selected by default.</p>
      <div class="job-detail-value" id="detailAttachmentsList"></div>
    </div>

    <div class="job-detail-actions">
      <button type="button" class="btn btn-outline" id="detailCancelBtn">Cancel</button>
      <button type="button" class="btn btn-primary" id="detailApplyBtn">Submit Application</button>
    </div>
  </div>
</div>
<script src="../assets/js/common.js"></script>
<script src="../assets/js/student.js"></script>
</body>
</html>


