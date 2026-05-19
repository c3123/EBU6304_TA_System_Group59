<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>Student Portal</title>
  <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/main.css?v=student3" />
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
    .apps-list,
    .hired-list {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
      gap: 14px;
    }

    .job-card,
    .app-item,
    .hired-item,
    .profile-wrap {
      background: #fff;
      border: 1px solid #e5e7eb;
      border-radius: 8px;
      box-shadow: 0 4px 14px rgba(15, 23, 42, 0.06);
    }

    .job-card,
    .app-item,
    .hired-item {
      padding: 18px;
    }

    .job-card h3,
    .app-item h3,
    .hired-item h3 {
      margin: 0 0 6px;
      font-size: 17px;
      line-height: 1.35;
    }

    .app-item h3 {
      padding-right: 86px;
    }

    .job-meta,
    .app-meta,
    .hired-meta {
      margin: 8px 0;
      color: #64748b;
      font-size: 13px;
      line-height: 1.5;
    }

    .job-match {
      margin: 12px 0;
      padding: 10px 12px;
      border: 1px solid #e5e7eb;
      border-left-width: 4px;
      border-radius: 8px;
      background: #f8fafc;
    }

    .job-match-rate {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 12px;
      margin-bottom: 6px;
      font-size: 13px;
      color: #475569;
    }

    .job-match-rate strong {
      font-size: 18px;
      color: #0f172a;
    }

    .job-match p {
      margin: 4px 0;
      font-size: 12px;
      line-height: 1.45;
      color: #475569;
    }

    .job-match p span {
      font-weight: 600;
      color: #334155;
    }

    .job-match-strong {
      border-left-color: #16a34a;
      background: #f0fdf4;
    }

    .job-match-moderate {
      border-left-color: #f59e0b;
      background: #fffbeb;
    }

    .job-match-weak {
      border-left-color: #94a3b8;
    }

    .ai-advisor-card {
      margin-bottom: 16px;
    }

    .jobs-layout {
      display: grid;
      grid-template-columns: minmax(0, 1fr) minmax(280px, 340px);
      gap: 18px;
      align-items: start;
    }

    .jobs-main-column,
    .jobs-ai-column {
      min-width: 0;
    }

    .jobs-ai-column {
      position: sticky;
      top: 102px;
    }

    .jobs-ai-column .ai-advisor-card {
      margin-bottom: 0;
    }

    .ai-advisor-card h3 {
      margin: 0 0 8px;
      font-size: 16px;
    }

    .ai-advisor-form {
      display: grid;
      grid-template-columns: minmax(0, 1fr);
      gap: 12px;
      align-items: start;
    }

    .ai-advisor-form textarea {
      min-height: 150px;
      border-width: 1px;
      border-radius: 8px;
      resize: vertical;
    }

    .ai-advisor-form .btn {
      width: 100%;
      min-height: 42px;
      font-weight: 700;
    }

    .ai-advisor-answer {
      margin-top: 12px;
      padding: 10px 12px;
      border-radius: 8px;
      background: #f8fafc;
      color: #334155;
      font-size: 13px;
      line-height: 1.55;
      white-space: pre-wrap;
    }

    .ai-advisor-note {
      margin-top: 8px;
      color: #92400e;
      font-size: 12px;
    }

    .hired-summary {
      display: grid;
      grid-template-columns: minmax(180px, 240px) minmax(0, 1fr);
      gap: 14px;
      margin-bottom: 16px;
    }

    .hired-total-card {
      background: #0f172a;
      color: #fff;
      border-radius: 8px;
      padding: 18px;
      box-shadow: 0 4px 14px rgba(15, 23, 42, 0.12);
    }

    .hired-total-card span {
      display: block;
      color: #cbd5e1;
      font-size: 13px;
      margin-bottom: 8px;
    }

    .hired-total-card strong {
      display: block;
      font-size: 34px;
      line-height: 1;
      letter-spacing: 0;
    }

    .hired-summary-note {
      background: #fff;
      border: 1px solid #e5e7eb;
      border-radius: 8px;
      padding: 18px;
      color: #475569;
      font-size: 14px;
      line-height: 1.6;
      box-shadow: 0 4px 14px rgba(15, 23, 42, 0.06);
    }

    .hired-item {
      border-left: 5px solid #16a34a;
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

    .student-portal-header-actions {
      display: flex;
      align-items: center;
      gap: 10px;
      flex-wrap: wrap;
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
      margin: 0 0 16px;
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

    body.student-portal-page {
      min-height: 100vh;
      background: #f5f8ff;
      color: #111827;
    }

    .student-shell {
      min-height: 100vh;
      display: grid;
      grid-template-columns: 248px minmax(0, 1fr);
      background:
        linear-gradient(90deg, #eef5ff 0, #f8fbff 248px, #ffffff 248px, #f7faff 100%);
    }

    .student-sidebar {
      position: sticky;
      top: 0;
      height: 100vh;
      padding: 28px 20px;
      display: flex;
      flex-direction: column;
      gap: 28px;
      border-right: 1px solid rgba(219, 226, 238, 0.7);
      background: rgba(245, 250, 255, 0.82);
      backdrop-filter: blur(14px);
      -webkit-backdrop-filter: blur(14px);
    }

    .student-brand {
      display: flex;
      align-items: center;
      gap: 14px;
      padding: 6px 8px;
    }

    .student-brand-mark {
      width: 46px;
      height: 46px;
      border-radius: 50%;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      color: #2563eb;
      background: #eaf2ff;
      box-shadow: 0 10px 26px rgba(37, 99, 235, 0.12);
    }

    .student-brand-mark svg {
      width: 25px;
      height: 25px;
    }

    .student-brand h1 {
      margin: 0;
      font-size: 18px;
      line-height: 1.2;
      letter-spacing: 0;
    }

    .student-brand p {
      margin: 5px 0 0;
      color: #64748b;
      font-size: 13px;
    }

    .student-tabs {
      display: flex;
      flex-direction: column;
      gap: 12px;
      border: 0;
      background: transparent;
      padding: 0;
      margin: 34px 0 0;
    }

    .student-tab.mo-tab {
      width: 100%;
      min-height: 48px;
      justify-content: flex-start;
      gap: 13px;
      padding: 0 16px;
      border: 0;
      border-radius: 8px;
      background: transparent;
      color: #475569;
      font-weight: 700;
      box-shadow: none;
    }

    .student-tab.mo-tab svg {
      width: 18px;
      height: 18px;
      color: #475569;
    }

    .student-tab.mo-tab.active,
    .student-tab.mo-tab:hover {
      background: #dfeaff;
      color: #2563eb;
      text-decoration: none;
      box-shadow: inset 0 0 0 1px rgba(37, 99, 235, 0.06);
    }

    .student-tab.mo-tab.active svg,
    .student-tab.mo-tab:hover svg {
      color: #2563eb;
    }

    .student-profile-card {
      margin-top: auto;
      padding: 18px;
      border-radius: 8px;
      background: linear-gradient(180deg, #f7fbff 0%, #edf5ff 100%);
      border: 1px solid #e1eaff;
      box-shadow: 0 14px 30px rgba(37, 99, 235, 0.08);
    }

    .student-profile-card-visual {
      width: 86px;
      height: 76px;
      margin: 0 auto 14px;
      border-radius: 18px;
      background:
        linear-gradient(135deg, rgba(37, 99, 235, 0.95), rgba(124, 58, 237, 0.85)),
        linear-gradient(#fff, #fff);
      box-shadow: 0 12px 24px rgba(37, 99, 235, 0.18);
      position: relative;
    }

    .student-profile-card-visual::before,
    .student-profile-card-visual::after {
      content: "";
      position: absolute;
      background: #fff;
      border-radius: 6px;
      box-shadow: 0 8px 18px rgba(15, 23, 42, 0.08);
    }

    .student-profile-card-visual::before {
      width: 48px;
      height: 60px;
      left: -16px;
      top: 8px;
      transform: rotate(6deg);
    }

    .student-profile-card-visual::after {
      width: 42px;
      height: 34px;
      right: -10px;
      bottom: 8px;
      background: #3b82f6;
    }

    .student-profile-card h2 {
      margin: 0 0 9px;
      font-size: 14px;
    }

    .student-profile-card p {
      margin: 0 0 14px;
      color: #64748b;
      font-size: 12px;
      line-height: 1.55;
    }

    .profile-progress {
      height: 5px;
      background: #dbeafe;
      border-radius: 999px;
      overflow: hidden;
      margin: 0 0 8px;
    }

    .profile-progress span {
      display: block;
      width: 80%;
      height: 100%;
      background: #2563eb;
      border-radius: inherit;
    }

    .student-profile-card a {
      display: inline-flex;
      margin-top: 12px;
      font-size: 12px;
      font-weight: 800;
    }

    .student-main {
      min-width: 0;
      display: flex;
      flex-direction: column;
    }

    .student-topbar {
      min-height: 82px;
      display: grid;
      grid-template-columns: minmax(260px, 430px) minmax(0, 1fr) auto;
      align-items: center;
      gap: 20px;
      padding: 18px 44px;
      background: rgba(255, 255, 255, 0.88);
      border-bottom: 1px solid #e7edf7;
      backdrop-filter: blur(12px);
      -webkit-backdrop-filter: blur(12px);
    }

    .student-top-search {
      position: relative;
    }

    .student-top-search input {
      height: 44px;
      border: 1px solid #dfe7f3;
      border-radius: 8px;
      padding: 0 44px 0 18px;
      font-size: 14px;
      color: #334155;
      background: #fff;
      box-shadow: 0 8px 24px rgba(15, 23, 42, 0.04);
    }

    .student-top-search svg {
      position: absolute;
      right: 15px;
      top: 50%;
      width: 18px;
      height: 18px;
      transform: translateY(-50%);
      color: #64748b;
      pointer-events: none;
    }

    .student-top-actions {
      justify-self: end;
      display: flex;
      align-items: center;
      gap: 16px;
    }

    .student-notification-icon {
      width: 40px;
      height: 40px;
      border: 0;
      border-radius: 50%;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      color: #475569;
      background: transparent;
      position: relative;
      cursor: pointer;
    }

    .student-notification-icon:hover {
      background: #eef5ff;
      color: #2563eb;
    }

    .student-notification-icon svg {
      width: 20px;
      height: 20px;
    }

    .student-notification-icon .mo-notification-dot {
      position: absolute;
      top: 2px;
      right: 1px;
      min-width: 18px;
      height: 18px;
      margin: 0;
      border: 2px solid #fff;
      background: #2563eb;
      font-size: 10px;
    }

    .student-user-menu {
      display: flex;
      align-items: center;
      gap: 12px;
      padding-left: 16px;
      border-left: 1px solid #e7edf7;
      color: #111827;
      font-weight: 700;
      white-space: nowrap;
    }

    .student-avatar,
    .profile-avatar-large {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      border-radius: 50%;
      background: linear-gradient(135deg, #dbeafe, #eff6ff);
      color: #2563eb;
      font-weight: 800;
      overflow: hidden;
    }

    .student-avatar {
      width: 40px;
      height: 40px;
      box-shadow: 0 8px 18px rgba(15, 23, 42, 0.08);
    }

    .student-content {
      width: 100%;
      max-width: none;
      margin: 0;
      padding: 28px 44px 36px;
    }

    .student-panel-header {
      margin-bottom: 22px;
    }

    .student-panel-header .mo-section-title {
      font-size: 28px;
      line-height: 1.15;
      display: inline-flex;
      align-items: center;
      gap: 10px;
    }

    .student-panel-header .mo-section-desc {
      margin-top: 8px;
      font-size: 14px;
      color: #64748b;
    }

    .profile-wrap,
    .student-ui-card,
    .job-card,
    .app-item,
    .hired-item,
    .hired-summary-note,
    .student-filter-card.card,
    .ai-advisor-card.card {
      border: 1px solid #e4eaf4;
      border-radius: 8px;
      background: #fff;
      box-shadow: 0 12px 34px rgba(24, 45, 84, 0.06);
    }

    .profile-wrap {
      padding: 0;
      border: 0;
      box-shadow: none;
      background: transparent;
    }

    .student-profile-layout {
      display: grid;
      grid-template-columns: minmax(0, 1fr) 360px;
      gap: 18px;
      align-items: start;
    }

    .profile-left,
    .profile-right {
      display: grid;
      gap: 18px;
    }

    .student-ui-card {
      padding: 24px;
    }

    .student-ui-card h3 {
      margin: 0 0 22px;
      display: flex;
      align-items: center;
      gap: 12px;
      font-size: 17px;
    }

    .student-ui-card h3 svg,
    .profile-section-icon {
      width: 18px;
      height: 18px;
      color: #2563eb;
      flex-shrink: 0;
    }

    .profile-grid {
      grid-template-columns: repeat(2, minmax(0, 1fr));
      gap: 22px 34px;
    }

    .profile-grid .field {
      margin: 0;
    }

    .profile-grid label {
      color: #64748b;
      font-size: 12px;
      margin-bottom: 6px;
    }

    .profile-grid input,
    .profile-grid textarea {
      border: 1px solid transparent;
      border-radius: 8px;
      padding: 0;
      color: #334155;
      font-weight: 700;
      background: transparent;
      box-shadow: none;
    }

    .profile-grid input:not(.readonly),
    .profile-grid textarea {
      border-color: #dfe7f3;
      padding: 12px 14px;
      font-weight: 500;
      background: #fff;
    }

    .profile-grid textarea {
      min-height: 96px;
      resize: vertical;
    }

    .profile-readonly-item {
      display: grid;
      grid-template-columns: 42px minmax(0, 1fr);
      gap: 14px;
      align-items: center;
    }

    .profile-info-icon {
      width: 42px;
      height: 42px;
      border-radius: 50%;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      color: #2563eb;
      background: #f0f5ff;
    }

    .profile-info-icon svg {
      width: 18px;
      height: 18px;
    }

    .profile-status-pill {
      display: inline-flex;
      align-items: center;
      gap: 8px;
      width: fit-content;
      border-radius: 999px;
      padding: 7px 13px;
      font-size: 12px;
      color: #15803d;
      background: #dcfce7;
      font-weight: 800;
    }

    .profile-status-pill::before {
      content: "";
      width: 7px;
      height: 7px;
      border-radius: 50%;
      background: #16a34a;
    }

    .profile-skills-row {
      display: flex;
      flex-wrap: wrap;
      gap: 10px;
      margin-bottom: 12px;
    }

    .profile-skill-chip {
      display: inline-flex;
      align-items: center;
      gap: 8px;
      min-height: 32px;
      padding: 0 14px;
      border-radius: 999px;
      color: #2563eb;
      background: #e9f1ff;
      font-size: 13px;
      font-weight: 800;
    }

    .profile-skill-chip span {
      color: #7da2f8;
      font-size: 16px;
      line-height: 1;
    }

    .profile-summary-card {
      text-align: center;
      position: relative;
    }

    .profile-summary-card::before {
      content: "";
      position: absolute;
      left: 55px;
      top: 32px;
      width: 56px;
      height: 56px;
      opacity: 0.45;
      background-image: radial-gradient(#c7d2fe 1px, transparent 1px);
      background-size: 9px 9px;
    }

    .profile-avatar-large {
      width: 132px;
      height: 132px;
      margin: 10px auto 18px;
      font-size: 36px;
      border: 12px solid #e8f0ff;
      color: #1d4ed8;
      background: linear-gradient(135deg, #dbeafe, #f8fbff);
      position: relative;
      z-index: 1;
    }

    .profile-summary-card h3 {
      display: block;
      margin: 0 0 8px;
      font-size: 22px;
    }

    .profile-summary-card p {
      margin: 0;
      color: #64748b;
    }

    .profile-stats {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 0;
      margin-top: 22px;
      padding-top: 18px;
      border-top: 1px solid #e7edf7;
    }

    .profile-stat {
      display: grid;
      gap: 4px;
      align-content: center;
      min-height: 54px;
      border-right: 1px solid #e7edf7;
      color: #64748b;
      font-size: 12px;
    }

    .profile-stat:last-child {
      border-right: 0;
    }

    .profile-stat strong {
      color: #111827;
      font-size: 18px;
    }

    .student-documents {
      margin-top: 0;
      border-top: 0;
      padding-top: 0;
    }

    .student-upload-area {
      border-color: #c8d8f4;
      background: #fbfdff;
      min-height: 124px;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
    }

    .student-upload-area svg {
      width: 30px;
      height: 30px;
      margin-bottom: 10px;
      color: #2563eb;
    }

    #attachmentsList {
      border: 0;
      max-height: 280px;
      overflow-y: auto;
      background: transparent;
    }

    .student-attachment-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 12px;
      padding: 12px 0;
      border-bottom: 1px solid #edf2f7;
    }

    .student-attachment-row:last-child {
      border-bottom: 0;
    }

    .student-attachment-row .delete-attachment-btn {
      padding: 6px 10px;
      background-color: #fee2e2;
      color: #991b1b;
      border: none;
      border-radius: 6px;
      font-size: 12px;
      cursor: pointer;
    }

    .profile-actions {
      margin-top: 20px;
      justify-content: flex-start;
    }

    .profile-actions .btn {
      min-width: 136px;
      min-height: 44px;
      font-weight: 800;
    }

    .mo-notification-panel {
      position: fixed;
      top: 74px;
      right: 44px;
      width: min(390px, calc(100vw - 32px));
      z-index: 80;
      box-shadow: 0 18px 40px rgba(15, 23, 42, 0.12);
    }

    @media (max-width: 760px) {
      .student-shell {
        display: block;
      }

      .student-sidebar {
        position: static;
        height: auto;
        padding: 18px;
      }

      .student-tabs {
        margin-top: 10px;
      }

      .student-profile-card {
        display: none;
      }

      .student-topbar {
        grid-template-columns: 1fr;
        padding: 16px 18px;
      }

      .student-top-actions {
        justify-self: stretch;
        justify-content: space-between;
      }

      .student-content {
        padding: 22px 18px 30px;
      }

      .student-profile-layout {
        grid-template-columns: 1fr;
      }

      .student-panel-header {
        display: block;
      }

      .student-search {
        grid-template-columns: 1fr;
      }

      .jobs-layout {
        grid-template-columns: 1fr;
      }

      .jobs-ai-column {
        position: static;
      }

      .job-detail-grid {
        grid-template-columns: 1fr;
      }

      .hired-summary {
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
<body class="student-portal-page">
<div class="student-shell">
  <aside class="student-sidebar">
    <div class="student-brand">
      <div class="student-brand-mark" aria-hidden="true">
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
      <button type="button" class="mo-tab student-tab" data-tab="hired">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
        <path d="M16 21v-2a4 4 0 0 0-8 0v2"></path>
        <circle cx="12" cy="7" r="4"></circle>
        <path d="M20 8l-3 3-2-2"></path>
      </svg>
      My Jobs
      </button>
      <button type="button" class="mo-tab student-tab" data-tab="profile">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
        <path d="M20 21a8 8 0 0 0-16 0"></path>
        <circle cx="12" cy="7" r="4"></circle>
      </svg>
      Profile
      </button>
    </nav>

    <div class="student-profile-card">
      <div class="student-profile-card-visual" aria-hidden="true"></div>
      <h2>Complete Your Profile</h2>
      <p>A complete profile improves your chances of getting selected.</p>
      <div class="profile-progress" aria-hidden="true"><span></span></div>
      <p>80% Complete</p>
      <a href="#" data-tab-jump="profile">Update Profile -></a>
    </div>
  </aside>

  <div class="student-main">
    <header class="student-topbar">
      <div class="student-top-search">
        <input id="globalStudentSearch" type="text" placeholder="Search jobs, skills, or opportunities..." />
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
          <circle cx="11" cy="11" r="8"></circle>
          <path d="m21 21-4.35-4.35"></path>
        </svg>
      </div>
      <div></div>
      <div class="student-top-actions">
        <button id="studentNotificationBtn" class="student-notification-icon" type="button" aria-label="Notifications">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
            <path d="M18 8a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9"></path>
            <path d="M13.73 21a2 2 0 0 1-3.46 0"></path>
          </svg>
          <span id="studentNotificationDot" class="mo-notification-dot" style="display:none">0</span>
        </button>
        <div class="student-user-menu">
          <span class="student-avatar" id="studentAvatar">S</span>
          <span id="studentTopName">Student</span>
          <a class="mo-btn-logout" href="<%= request.getContextPath() %>/logout">Logout</a>
        </div>
      </div>
    </header>

<main class="student-content">
  <div id="studentNotificationPanel" class="mo-notification-panel"></div>

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

      <div class="mo-two-col">
        <div>
          <div id="jobsLoading" class="loading-state">Loading job data...</div>
          <div id="jobsEmpty" class="empty-state hidden">No matching positions at the moment.</div>
          <div id="jobsList" class="jobs-grid hidden"></div>
        </div>

        <aside>
          <div class="card ai-advisor-card">
            <h3>AI Job Advisor</h3>
            <div class="ai-advisor-form">
              <textarea id="aiAdvisorQuestion" placeholder="Ask: Which TA job is most suitable for me?"></textarea>
              <button id="aiAdvisorBtn" class="btn btn-primary" type="button">Ask AI Advisor</button>
            </div>
            <div id="aiAdvisorAnswer" class="ai-advisor-answer hidden"></div>
            <div id="aiAdvisorNote" class="ai-advisor-note hidden">AI service unavailable. Showing system-generated advice.</div>
          </div>
        </aside>
      </div>
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

  <section class="student-panel" id="panel-hired" aria-labelledby="My Jobs">
    <div class="student-panel-header mo-applicants-head">
      <div>
        <h2 class="mo-section-title">My Jobs</h2>
        <p class="mo-section-desc" id="hiredCountText">Preparing your confirmed workload...</p>
      </div>
    </div>

    <div class="module-frame">
      <div id="hiredLoading" class="loading-state">Loading jobs...</div>
      <div id="hiredContent" class="hidden">
        <div class="hired-summary">
          <div class="hired-total-card">
            <span>Total weekly workload</span>
            <strong id="hiredTotalHours">0h</strong>
          </div>
          <div class="hired-summary-note" id="hiredSummaryNote">
            Confirmed TA jobs are counted from applications with Hired status.
          </div>
        </div>
        <div id="hiredEmpty" class="empty-state hidden">You do not have any jobs yet.</div>
        <div id="hiredList" class="hired-list hidden"></div>
      </div>
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
        <div class="student-profile-layout">
          <div class="profile-left">
            <div class="student-ui-card">
              <h3>
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
                  <path d="M20 21a8 8 0 0 0-16 0"></path>
                  <circle cx="12" cy="7" r="4"></circle>
                </svg>
                Personal Information
              </h3>
              <div class="profile-grid">
                <div class="field profile-readonly-item">
                  <span class="profile-info-icon" aria-hidden="true">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21a8 8 0 0 0-16 0"></path><circle cx="12" cy="7" r="4"></circle></svg>
                  </span>
                  <span>
                    <label for="profileName">Full Name</label>
                    <input id="profileName" type="text" />
                  </span>
                </div>
                <div class="field profile-readonly-item">
                  <span class="profile-info-icon" aria-hidden="true">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="5" width="18" height="14" rx="2"></rect><path d="m3 7 9 6 9-6"></path></svg>
                  </span>
                  <span>
                    <label for="profileEmail">Email</label>
                    <input id="profileEmail" class="readonly" type="text" readonly />
                  </span>
                </div>
                <div class="field profile-readonly-item">
                  <span class="profile-info-icon" aria-hidden="true">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="4" y="5" width="16" height="14" rx="2"></rect><path d="M8 9h8M8 13h5"></path></svg>
                  </span>
                  <span>
                    <label for="profileStudentId">Student ID</label>
                    <input id="profileStudentId" class="readonly" type="text" readonly />
                  </span>
                </div>
                <div class="field profile-readonly-item">
                  <span class="profile-info-icon" aria-hidden="true">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 10 12 5 2 10l10 5 10-5Z"></path><path d="M6 12v5c3 2 9 2 12 0v-5"></path></svg>
                  </span>
                  <span>
                    <label for="profileProgramme">Programme</label>
                    <input id="profileProgramme" class="readonly" type="text" readonly />
                  </span>
                </div>
                <div class="field profile-readonly-item">
                  <span class="profile-info-icon" aria-hidden="true">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.8 19.8 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6A19.8 19.8 0 0 1 2.08 4.18 2 2 0 0 1 4.06 2h3a2 2 0 0 1 2 1.72c.12.9.32 1.77.59 2.61a2 2 0 0 1-.45 2.11L8 9.64a16 16 0 0 0 6.36 6.36l1.2-1.2a2 2 0 0 1 2.11-.45c.84.27 1.71.47 2.61.59A2 2 0 0 1 22 16.92Z"></path></svg>
                  </span>
                  <span>
                    <label for="profilePhone">Phone</label>
                    <input id="profilePhone" type="text" inputmode="numeric" maxlength="11" pattern="[0-9]{11}" placeholder="Enter 11-digit phone number" />
                  </span>
                </div>
                <div class="field profile-readonly-item">
                  <span class="profile-info-icon" aria-hidden="true" style="color:#16a34a;background:#e8f8ef;">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="4"></circle></svg>
                  </span>
                  <span>
                    <label>Status</label>
                    <span class="profile-status-pill">Active Student</span>
                  </span>
                </div>
              </div>
            </div>

            <div class="student-ui-card">
              <h3>
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
                  <path d="m12 3 1.8 5.2L19 10l-5.2 1.8L12 17l-1.8-5.2L5 10l5.2-1.8L12 3Z"></path>
                </svg>
                Skills
              </h3>
              <div class="profile-skills-row" id="profileSkillChips"></div>
              <div class="field field-wide">
                <label for="profileSkills">Edit Skills</label>
                <input id="profileSkills" type="text" placeholder="Example: Java, Python, SQL, Communication, Database Design" />
                <p class="field-help">Enter technical and soft skills separated by commas.</p>
              </div>

              <h3 style="margin-top:26px;">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
                  <rect x="3" y="7" width="18" height="13" rx="2"></rect>
                  <path d="M8 7V5a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                </svg>
                Experience
              </h3>
              <div class="field field-wide">
                <label for="profileExperience">Experience</label>
                <textarea id="profileExperience" placeholder="Describe your relevant experience..."></textarea>
              </div>
              <div class="row profile-actions">
                <button type="button" class="btn btn-primary" id="saveProfileBtn">Save Changes</button>
              </div>
            </div>
          </div>

          <div class="profile-right">
            <div class="student-ui-card profile-summary-card">
              <div class="profile-avatar-large" id="profileAvatarLarge">S</div>
              <h3 id="profileCardName">Student</h3>
              <p id="profileCardProgramme">Programme</p>
              <div class="profile-stats">
                <div class="profile-stat"><strong id="profileApplicationsStat">0</strong><span>Applications</span></div>
                <div class="profile-stat"><strong id="profileOffersStat">0</strong><span>Offers</span></div>
              </div>
            </div>

            <div class="student-ui-card student-documents">
              <h3>
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
                  <path d="M3 7h6l2 2h10v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2Z"></path>
                  <path d="M3 7V5a2 2 0 0 1 2-2h4l2 2h4"></path>
                </svg>
                Supporting Documents
              </h3>
              <p class="notice">Upload certificates, transcripts, or other supporting documents.</p>

              <div id="uploadArea" class="student-upload-area">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
                  <path d="M12 16V4"></path>
                  <path d="m7 9 5-5 5 5"></path>
                  <path d="M20 16.5a4.5 4.5 0 0 1-2.2 8.5H7a5 5 0 0 1-1-9.9"></path>
                </svg>
                <p style="margin: 0 0 4px 0; font-size: 14px; font-weight: 800; color:#2563eb;">Click to upload</p>
                <p style="font-size: 12px; color: #64748b; margin:0;">or drag and drop<br />Max 50MB total</p>
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
                <div id="attachmentsList">
                  <p style="margin: 14px; text-align: center; color: #6b7280; font-size: 13px;">No documents uploaded yet</p>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="student-ui-card student-documents" style="margin-top:18px;">
          <h3>Change Password</h3>
          <p class="notice">Update your own account password.</p>
          <div class="profile-grid" style="margin-top: 16px;">
            <div class="field">
              <label for="studentOldPassword">Current Password</label>
              <input id="studentOldPassword" type="password" />
            </div>
            <div class="field">
              <label for="studentNewPassword">New Password</label>
              <input id="studentNewPassword" type="password" />
            </div>
            <div class="field field-wide">
              <label for="studentConfirmPassword">Confirm New Password</label>
              <input id="studentConfirmPassword" type="password" />
            </div>
          </div>
          <div class="row" style="margin-top: 16px; justify-content: center;">
            <button type="button" class="btn btn-outline" id="studentChangePasswordBtn">Change Password</button>
          </div>
        </div>
      </div>
    </div>
  </section>

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
<script src="<%= request.getContextPath() %>/assets/js/common.js?v=student3"></script>
<script src="<%= request.getContextPath() %>/assets/js/student.js?v=student3"></script>
</body>
</html>


