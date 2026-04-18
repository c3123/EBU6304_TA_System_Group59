<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>Student Portal</title>
  <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/main.css?v=student2" />
  <style>
    body.student-portal-page {
      background: #f9fafb;
    }

    .student-portal-page .mo-tab svg,
    .student-portal-page .student-portal-icon svg {
      width: 16px;
      height: 16px;
      flex-shrink: 0;
    }

    .student-portal-page .student-portal-icon svg {
      width: 22px;
      height: 22px;
    }

    .student-panel {
      display: none;
    }

    .student-panel.active {
      display: block;
    }

    .student-panel-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      gap: 16px;
      margin-bottom: 16px;
    }

    .student-panel-header h2 {
      margin: 0 0 4px;
    }

    .student-panel-header p {
      margin: 0;
    }

    .module-frame {
      width: 100%;
    }

    .student-filter-card {
      margin-bottom: 16px;
    }

    .student-portal-page .btn,
    .student-filter-card.card {
      border-radius: 8px;
    }

    .student-search {
      display: grid;
      grid-template-columns: minmax(220px, 1fr) minmax(150px, 180px) minmax(150px, 180px);
      gap: 12px;
      align-items: end;
    }

    .student-search input,
    .student-search select {
      border-width: 1px;
      border-radius: 8px;
      min-height: 42px;
    }

    .jobs-grid,
    .apps-list {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
      gap: 14px;
    }

    .job-card,
    .app-item,
    .profile-wrap {
      background: #fff;
      border: 1px solid #e5e7eb;
      border-radius: 8px;
      box-shadow: 0 4px 14px rgba(15, 23, 42, 0.06);
    }

    .job-card,
    .app-item {
      padding: 18px;
    }

    .job-card h3,
    .app-item h3 {
      margin: 0 0 6px;
      font-size: 17px;
      line-height: 1.35;
    }

    .app-item h3 {
      padding-right: 86px;
    }

    .job-meta,
    .app-meta {
      margin: 8px 0;
      color: #64748b;
      font-size: 13px;
      line-height: 1.5;
    }

    .job-actions {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
      margin-top: 14px;
      padding-top: 14px;
      border-top: 1px solid #e5e7eb;
    }

    .app-feedback {
      margin-top: 12px;
      background: #f9fafb;
      border: 1px solid #e5e7eb;
      border-radius: 8px;
      padding: 10px 12px;
      color: #334155;
      font-size: 13px;
      line-height: 1.5;
    }

    .empty-state,
    .loading-state {
      text-align: center;
      background: #fff;
      border: 2px dashed #d1d5db;
      border-radius: 8px;
      padding: 42px 24px;
      color: #64748b;
    }

    .hidden {
      display: none;
    }

    .profile-wrap {
      padding: 20px;
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
      gap: 14px;
    }

    .profile-grid .field-wide {
      grid-column: 1 / -1;
    }

    .readonly {
      background: #f8fafc;
      color: #475569;
    }

    .student-documents {
      margin-top: 24px;
      border-top: 1px solid #e5e7eb;
      padding-top: 24px;
    }

    .student-documents h3 {
      margin: 0 0 6px;
      font-size: 17px;
    }

    .student-documents p {
      margin: 0;
    }

    .student-upload-area {
      margin-top: 16px;
      border: 2px dashed #cbd5e1;
      border-radius: 8px;
      padding: 22px;
      text-align: center;
      cursor: pointer;
      background-color: #f9fafb;
      transition: all 0.2s ease;
    }

    .student-upload-area:hover {
      border-color: #2563eb;
      background: #eff6ff;
    }

    .student-document-label {
      margin-top: 16px;
    }

    .student-document-label-grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 10px;
      align-items: center;
    }

    .student-document-label select,
    .student-document-label input {
      border-width: 1px;
      border-radius: 8px;
      padding: 10px 12px;
      font-size: 14px;
    }

    .student-attachments-block {
      margin-top: 16px;
    }

    #attachmentsList {
      border: 1px solid #e5e7eb;
      border-radius: 8px;
      max-height: 250px;
      overflow-y: auto;
      background: #fff;
    }

    .portal-notice {
      margin-top: 12px;
      text-align: center;
      min-height: 20px;
      color: #1e5eff;
      font-weight: 600;
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
      border-radius: 8px;
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
      border-radius: 8px;
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
      .student-panel-header {
        display: block;
      }

      .student-search {
        grid-template-columns: 1fr;
      }

      .job-detail-grid {
        grid-template-columns: 1fr;
      }

      .profile-grid {
        grid-template-columns: 1fr;
      }

      .student-document-label-grid {
        grid-template-columns: 1fr;
      }
    }
  </style>
</head>
<body class="mo-portal student-portal-page">
<header class="mo-portal-header">
  <div class="mo-portal-header-inner">
    <div class="mo-portal-brand">
      <div class="mo-portal-icon student-portal-icon" aria-hidden="true">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M22 10v6M2 10l10-5 10 5-10 5-10-5z"></path>
          <path d="M6 12v5c3 2 9 2 12 0v-5"></path>
        </svg>
      </div>
      <div>
        <h1>Student Portal</h1>
        <p id="studentWelcome">Welcome back.</p>
      </div>
    </div>
    <a class="mo-btn-logout" href="<%= request.getContextPath() %>/logout">Logout</a>
  </div>
</header>

<main class="mo-portal-main student-content">
  <nav class="mo-tabs student-tabs" aria-label="Student modules">
    <button type="button" class="mo-tab student-tab active" data-tab="jobs">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
        <rect x="3" y="4" width="18" height="16" rx="2"></rect>
        <path d="M7 8h10M7 12h6M7 16h8"></path>
      </svg>
      Available Jobs
    </button>
    <button type="button" class="mo-tab student-tab" data-tab="applications">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
        <path d="M9 11l3 3L22 4"></path>
        <path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"></path>
      </svg>
      My Applications
    </button>
    <button type="button" class="mo-tab student-tab" data-tab="profile">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
        <path d="M20 21a8 8 0 0 0-16 0"></path>
        <circle cx="12" cy="7" r="4"></circle>
      </svg>
      Profile
    </button>
  </nav>

  <section class="student-panel active" id="panel-jobs" aria-labelledby="Available Jobs">
    <div class="student-panel-header mo-applicants-head">
      <div>
        <h2 class="mo-section-title">Available Jobs</h2>
        <p class="mo-section-desc" id="jobsCountText">Preparing the latest positions...</p>
      </div>
    </div>

    <div class="module-frame">
      <div class="card student-filter-card">
        <div class="student-search">
          <input id="jobSearchInput" type="text" placeholder="Search by module code or title" />
          <select id="jobStatusFilter">
            <option value="all">All Status</option>
            <option value="open">Open</option>
            <option value="closed">Closed</option>
          </select>
          <select id="jobHoursFilter">
            <option value="all">All Hours</option>
            <option value="<=10">&lt;=10h</option>
            <option value=">10">&gt;10h</option>
          </select>
        </div>
      </div>

      <div id="jobsLoading" class="loading-state">Loading job data...</div>
      <div id="jobsEmpty" class="empty-state hidden">No matching positions at the moment.</div>
      <div id="jobsList" class="jobs-grid hidden"></div>
    </div>
  </section>

  <section class="student-panel" id="panel-applications" aria-labelledby="My Applications">
    <div class="student-panel-header mo-applicants-head">
      <div>
        <h2 class="mo-section-title">My Applications</h2>
        <p class="mo-section-desc" id="appsCountText">Preparing your application history...</p>
      </div>
    </div>

    <div class="module-frame">
      <div id="appsLoading" class="loading-state">Loading applications...</div>
      <div id="appsEmpty" class="empty-state hidden">You have not submitted any applications yet.</div>
      <div id="appsList" class="apps-list hidden"></div>
    </div>
  </section>

  <section class="student-panel" id="panel-profile" aria-labelledby="Profile">
    <div class="student-panel-header mo-applicants-head">
      <div>
        <h2 class="mo-section-title">Profile</h2>
        <p class="mo-section-desc">Update your information and supporting documents.</p>
      </div>
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

        <div class="student-documents">
          <h3>Supporting Documents</h3>
          <p class="notice">Upload certificates, transcripts, or other supporting documents. Max 50MB total.</p>

          <div id="uploadArea" class="student-upload-area">
            <p style="margin: 0 0 10px 0; font-size: 14px; font-weight: 600;">Drag and drop files or click to browse</p>
            <p style="font-size: 12px; color: #6b7280;">PDF, DOCX, XLSX, JPG, PNG</p>
            <input id="fileInput" type="file" style="display: none;" />
          </div>

          <div class="student-document-label">
            <label for="attachmentLabel">Document Type</label>
            <div class="student-document-label-grid">
              <select id="attachmentLabel">
                <option value="Resume">Resume</option>
                <option value="Certificate">Certificate</option>
                <option value="Transcript">Transcript</option>
                <option value="Custom">Custom...</option>
              </select>
              <input id="attachmentCustomLabel" type="text" placeholder="Type custom label" style="display:none;" />
            </div>
          </div>

          <div class="student-attachments-block">
            <p style="margin: 0 0 10px 0; font-size: 13px; font-weight: 600;">Uploaded Documents</p>
            <div id="attachmentsList">
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
<script src="<%= request.getContextPath() %>/assets/js/common.js?v=student2"></script>
<script src="<%= request.getContextPath() %>/assets/js/student.js?v=student2"></script>
</body>
</html>


