<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <title>Applicants - Module Organiser Portal</title>
  <link rel="stylesheet" href="../assets/css/main.css?v=teacher-student-style" />
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
    .mo-toolbar-extra {
      display: flex;
      flex-wrap: wrap;
      align-items: center;
      gap: 14px 20px;
      margin-bottom: 14px;
      padding-top: 4px;
      border-top: 1px solid #e2e8f0;
    }
    .mo-toolbar-extra label {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      font-size: 14px;
      color: #334155;
      cursor: pointer;
    }
    .mo-sort-select {
      font-size: 13px;
      padding: 4px 8px;
      border-radius: 8px;
      border: 1px solid #cbd5e1;
      background: #fff;
    }
    .mo-match-badge {
      display: inline-block;
      padding: 3px 10px;
      border-radius: 999px;
      font-size: 12px;
      font-weight: 700;
      white-space: nowrap;
    }
    .mo-match-high { background: #d1fae5; color: #065f46; border: 1px solid #6ee7b7; }
    .mo-match-mid { background: #fef3c7; color: #92400e; border: 1px solid #fcd34d; }
    .mo-match-low { background: #f1f5f9; color: #64748b; border: 1px solid #cbd5e1; }
    .mo-skill-fit {
      border-radius: 10px;
      padding: 14px 16px;
      margin: 12px 0;
      background: #f8fafc;
      border: 1px solid #dbe2ee;
      font-size: 13px;
    }
    .mo-skill-fit-head {
      display: flex;
      flex-wrap: wrap;
      align-items: center;
      justify-content: space-between;
      gap: 10px;
      margin-bottom: 10px;
    }
    .mo-skill-fit-title {
      font-weight: 700;
      color: #0f172a;
      margin: 0;
      font-size: 14px;
    }
    .mo-match-score-big {
      font-size: 1.35rem;
      font-weight: 800;
      line-height: 1.2;
    }
    .mo-match-score-big.mo-match-high { color: #059669; }
    .mo-match-score-big.mo-match-mid { color: #d97706; }
    .mo-match-score-big.mo-match-low { color: #64748b; }
    .mo-match-bar {
      height: 8px;
      border-radius: 999px;
      background: #e2e8f0;
      overflow: hidden;
      margin: 8px 0 12px;
    }
    .mo-match-bar-fill {
      height: 100%;
      border-radius: 999px;
      transition: width 0.2s ease;
    }
    .mo-match-bar-fill.mo-match-high { background: #22c55e; }
    .mo-match-bar-fill.mo-match-mid { background: #eab308; }
    .mo-match-bar-fill.mo-match-low { background: #94a3b8; }
    .mo-skill-compare {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 10px 16px;
      margin-bottom: 10px;
    }
    @media (max-width: 560px) {
      .mo-skill-compare { grid-template-columns: 1fr; }
    }
    .mo-skill-compare-cell .mo-app-lbl {
      display: block;
      margin-bottom: 4px;
    }
    .mo-skill-tag.neutral { background: #e0e7ff; color: #3730a3; }
    .mo-skill-fit-formula {
      font-size: 12px;
      color: #475569;
      margin: 0 0 10px;
      line-height: 1.45;
    }
    .mo-skill-fit-hint {
      font-size: 12px;
      color: #64748b;
      margin: 8px 0 0;
      padding-top: 8px;
      border-top: 1px dashed #e2e8f0;
    }
    .mo-skill-tags {
      display: flex;
      flex-wrap: wrap;
      gap: 6px;
      margin: 4px 0 8px;
    }
    .mo-skill-tag {
      display: inline-block;
      padding: 2px 8px;
      border-radius: 6px;
      font-size: 12px;
      font-weight: 600;
    }
    .mo-skill-tag.matched { background: #d1fae5; color: #065f46; }
    .mo-skill-tag.missing { background: #fee2e2; color: #991b1b; }
    .mo-skill-tag.related { background: #fef3c7; color: #92400e; border: 1px solid #fcd34d; }
    .mo-skill-fit-summary {
      font-size: 12px;
      color: #64748b;
      margin: 0;
    }
    .mo-job-stats {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
      margin: 0 0 12px;
    }
    .mo-stat-pill {
      display: inline-block;
      padding: 3px 10px;
      border-radius: 999px;
      font-size: 12px;
      font-weight: 600;
      background: #f1f5f9;
      color: #475569;
      border: 1px solid #e2e8f0;
    }
    .mo-fb-template {
      font-size: 12px;
      padding: 4px 8px;
      border-radius: 8px;
      border: 1px solid #cbd5e1;
      background: #fff;
      max-width: 220px;
    }
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
    .mo-applicants-figma .mo-portal-header {
      position: sticky;
      top: 0;
      height: 100vh;
      min-height: 0;
      background: rgba(245, 250, 255, 0.82);
      color: #111827;
      padding: 28px 20px;
      border: 0;
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
      color: #111827;
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
    .mo-applicants-figma .mo-portal-main {
      width: 100%;
      max-width: none;
      margin: 0;
      padding: 28px 44px 36px;
      background: transparent;
      border-radius: 0;
      box-shadow: none;
    }
    .mo-applicants-figma .mo-tabs {
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
    .mo-applicants-head,
    .card,
    .mo-app-card-proto {
      border-radius: 8px;
    }
    .mo-app-card-proto,
    .card {
      border: 1px solid #e4eaf4;
      box-shadow: 0 12px 34px rgba(24, 45, 84, 0.06);
    }
    .app-summary-grid {
      display: grid;
      grid-template-columns: repeat(6, minmax(130px, 1fr));
      gap: 14px;
      margin-bottom: 16px;
    }
    .app-summary-card {
      min-height: 104px;
      padding: 15px;
      border: 1px solid #e4eaf4;
      border-radius: 8px;
      background: #ffffff;
      box-shadow: 0 12px 34px rgba(24, 45, 84, 0.06);
      display: flex;
      flex-direction: column;
      justify-content: space-between;
      gap: 10px;
    }
    .app-summary-card span {
      font-size: 11px;
      font-weight: 800;
      color: #64748b;
      text-transform: uppercase;
      letter-spacing: .03em;
    }
    .app-summary-card strong {
      font-size: 28px;
      line-height: 1;
      color: #0f172a;
    }
    .app-summary-card p {
      margin: 0;
      font-size: 12px;
      color: #64748b;
    }
    .summary-blue { border-top: 3px solid #2563eb; }
    .summary-green { border-top: 3px solid #16a34a; }
    .summary-yellow { border-top: 3px solid #f59e0b; }
    .summary-red { border-top: 3px solid #ef4444; }
    .compact-legend {
      display: flex;
      align-items: center;
      flex-wrap: wrap;
      gap: 10px 16px;
      padding: 11px 14px;
      margin-bottom: 16px;
      border-radius: 8px;
      border: 1px solid #dbeafe;
      background: #f8fbff;
      color: #475569;
      font-size: 13px;
    }
    .compact-legend strong {
      color: #0f172a;
      font-size: 13px;
    }
    .app-filter-card {
      padding: 14px 16px;
      margin-bottom: 16px;
    }
    .mo-legend {
      display: flex;
      align-items: center;
      flex-wrap: wrap;
      gap: 10px 16px;
      padding: 11px 14px;
      margin-bottom: 16px;
      border-radius: 8px;
      border: 1px solid #dbeafe;
      background: #f8fbff;
      color: #475569;
      font-size: 13px;
    }
    .mo-legend h3 {
      margin: 0;
      color: #0f172a;
      font-size: 13px;
      white-space: nowrap;
    }
    .mo-legend-row {
      display: flex;
      align-items: center;
      flex-wrap: wrap;
      gap: 10px 16px;
    }
    .mo-status-toolbar,
    .mo-toolbar-extra,
    .mo-filter-row {
      display: flex;
      align-items: center;
      flex-wrap: wrap;
      gap: 10px 14px;
      margin: 0;
      padding: 0;
      border: 0;
    }
    .mo-toolbar-extra {
      margin-top: 10px;
    }
    .mo-filter-row {
      margin-top: 10px;
      justify-content: flex-end;
    }
    .app-filter-toolbar {
      display: grid;
      grid-template-columns: minmax(260px, 1.2fr) minmax(180px, .55fr) auto minmax(180px, .6fr) auto;
      gap: 12px;
      align-items: end;
    }
    .filter-status-strip,
    .filter-inline-control,
    .filter-actions {
      display: flex;
      align-items: center;
      gap: 10px;
      flex-wrap: wrap;
    }
    .filter-status-strip {
      align-self: center;
    }
    .filter-status-strip > span,
    .filter-inline-control > span {
      font-size: 12px;
      font-weight: 800;
      color: #64748b;
      text-transform: uppercase;
      letter-spacing: .03em;
    }
    .filter-status-strip label,
    .filter-inline-control label {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      margin: 0;
      font-size: 13px;
      color: #334155;
      white-space: nowrap;
    }
    .filter-job-search {
      margin: 0;
    }
    .filter-job-search input {
      min-height: 40px;
    }
    .filter-actions {
      justify-content: flex-end;
    }
    .review-workspace {
      display: grid;
      grid-template-columns: minmax(260px, 320px) minmax(0, 1fr);
      gap: 16px;
      align-items: start;
    }
    .job-review-list,
    .applicant-review-panel {
      border: 1px solid #e4eaf4;
      border-radius: 8px;
      background: #ffffff;
      box-shadow: 0 12px 34px rgba(24, 45, 84, 0.06);
    }
    .job-review-list {
      padding: 12px;
      position: sticky;
      top: 106px;
      max-height: calc(100vh - 136px);
      overflow: auto;
    }
    .job-review-title {
      margin: 4px 4px 10px;
      font-size: 13px;
      font-weight: 800;
      color: #64748b;
      text-transform: uppercase;
      letter-spacing: .03em;
    }
    .job-review-item {
      width: 100%;
      display: block;
      text-align: left;
      border: 1px solid transparent;
      border-radius: 8px;
      background: transparent;
      padding: 12px;
      margin-bottom: 8px;
      cursor: pointer;
      color: #0f172a;
    }
    .job-review-item:hover,
    .job-review-item.active {
      background: #eef6ff;
      border-color: #dbeafe;
    }
    .job-review-item h3 {
      margin: 0 0 8px;
      font-size: 14px;
      line-height: 1.3;
    }
    .job-review-meta {
      display: flex;
      flex-wrap: wrap;
      gap: 6px;
      color: #64748b;
      font-size: 12px;
    }
    .job-review-counts {
      display: flex;
      flex-wrap: wrap;
      gap: 5px;
      margin-top: 9px;
    }
    .job-review-counts span {
      padding: 2px 7px;
      border-radius: 999px;
      background: #f1f5f9;
      color: #475569;
      font-size: 11px;
      font-weight: 700;
    }
    .applicant-review-panel {
      min-width: 0;
      padding: 16px;
    }
    .review-panel-head {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      gap: 14px;
      flex-wrap: wrap;
      padding-bottom: 14px;
      margin-bottom: 14px;
      border-bottom: 1px solid #edf2f7;
    }
    .review-panel-head h3 {
      margin: 0 0 6px;
      color: #0f172a;
    }
    .review-panel-head p {
      margin: 0;
      color: #64748b;
      font-size: 13px;
    }
    .review-panel-actions {
      display: flex;
      align-items: center;
      flex-wrap: wrap;
      justify-content: flex-end;
      gap: 8px;
    }
    .applicant-card-list {
      display: grid;
      gap: 12px;
    }
    .mo-app-card-proto {
      padding: 16px;
      margin-bottom: 0;
      box-shadow: 0 8px 24px rgba(24, 45, 84, 0.05);
    }
    .applicant-card-top {
      display: grid;
      grid-template-columns: auto minmax(0, 1fr) auto auto;
      gap: 12px;
      align-items: center;
      margin-bottom: 10px;
    }
    .applicant-card-top h4 {
      margin: 0;
      display: flex;
      align-items: center;
      gap: 8px;
      flex-wrap: wrap;
      color: #0f172a;
    }
    .applicant-card-subrow {
      display: flex;
      align-items: center;
      flex-wrap: wrap;
      gap: 8px 12px;
      margin-bottom: 10px;
      color: #64748b;
      font-size: 13px;
    }
    .workload-badge {
      display: inline-flex;
      align-items: center;
      border-radius: 999px;
      padding: 3px 9px;
      font-size: 12px;
      font-weight: 800;
      border: 1px solid #cbd5e1;
      background: #f8fafc;
      color: #475569;
    }
    .workload-badge.low { background: #eff6ff; color: #1d4ed8; border-color: #bfdbfe; }
    .workload-badge.normal { background: #ecfdf5; color: #047857; border-color: #a7f3d0; }
    .workload-badge.warn { background: #fffbeb; color: #92400e; border-color: #fcd34d; }
    .workload-badge.over { background: #fef2f2; color: #b91c1c; border-color: #fecaca; }
    .skill-compact {
      padding: 11px 12px;
      border-radius: 8px;
      background: #f8fbff;
      border: 1px solid #edf2f7;
      margin-bottom: 10px;
    }
    .skill-compact-head {
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 10px;
      margin-bottom: 8px;
    }
    .skill-compact-head span {
      font-size: 12px;
      font-weight: 800;
      color: #64748b;
      text-transform: uppercase;
      letter-spacing: .03em;
    }
    .skill-chip-row {
      display: flex;
      flex-wrap: wrap;
      gap: 6px;
      margin-top: 8px;
    }
    .applicant-detail-drawer {
      margin-top: 10px;
      border-radius: 8px;
      background: #fbfdff;
      border: 1px solid #edf2f7;
      padding: 0;
    }
    .applicant-detail-drawer summary {
      cursor: pointer;
      padding: 10px 12px;
      font-size: 13px;
      font-weight: 800;
      color: #2563eb;
    }
    .applicant-detail-body {
      padding: 0 12px 12px;
    }
    .mo-app-expand {
      margin-top: 10px;
    }
    @media (max-width: 960px) {
      .teacher-shell {
        grid-template-columns: 1fr;
      }
      .mo-applicants-figma .mo-portal-header {
        position: static;
        height: auto;
      }
      .mo-portal-header-inner {
        min-height: 0;
      }
      .teacher-topbar,
      .mo-applicants-figma .mo-portal-main {
        padding-left: 20px;
        padding-right: 20px;
      }
      .app-summary-grid {
        grid-template-columns: repeat(2, minmax(0, 1fr));
      }
      .app-filter-toolbar,
      .review-workspace {
        grid-template-columns: 1fr;
      }
      .job-review-list {
        position: static;
        max-height: none;
      }
    }
    @media (max-width: 640px) {
      .teacher-topbar {
        grid-template-columns: 1fr;
      }
      .teacher-top-actions,
      .filter-actions,
      .review-panel-actions {
        justify-content: flex-start;
      }
      .app-summary-grid,
      .applicant-card-top {
        grid-template-columns: 1fr;
      }
    }
  </style>
</head>
<body class="mo-portal mo-applicants-figma teacher-portal-page">
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
        <p>Applicant Management</p>
      </div>
    </div>
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
    <h1>Applicant Management</h1>
    <p>Review applications, compare skills, and update hiring decisions.</p>
  </div>
  <div class="teacher-top-actions">
    <button type="button" id="exportCsvBtn" class="btn btn-primary">Export CSV</button>
  </div>
</header>

<main class="mo-portal-main">
  <section class="app-summary-grid" aria-label="Applicant summary">
    <article class="app-summary-card summary-blue">
      <span>Total Applications</span>
      <strong id="summaryTotalApplications">0</strong>
      <p>Visible after filters</p>
    </article>
    <article class="app-summary-card summary-yellow">
      <span>Pending</span>
      <strong id="summaryPendingApplications">0</strong>
      <p>Awaiting review</p>
    </article>
    <article class="app-summary-card summary-blue">
      <span>Shortlisted</span>
      <strong id="summaryShortlistedApplications">0</strong>
      <p>Ready for final hiring</p>
    </article>
    <article class="app-summary-card summary-green">
      <span>Hired</span>
      <strong id="summaryHiredApplications">0</strong>
      <p>Confirmed candidates</p>
    </article>
    <article class="app-summary-card summary-green">
      <span>High Match</span>
      <strong id="summaryHighMatchApplications">0</strong>
      <p>60% or above</p>
    </article>
    <article class="app-summary-card summary-red">
      <span>Overloaded</span>
      <strong id="summaryOverloadedApplications">0</strong>
      <p>20h/week or above</p>
    </article>
  </section>

  <div class="mo-legend">
    <h3>Workload Status Legend</h3>
    <div class="mo-legend-row">
      <span><span class="mo-legend-dot" style="background:#3b82f6"></span> Low (&lt;10h/week)</span>
      <span><span class="mo-legend-dot" style="background:#22c55e"></span> Normal (10–14h/week)</span>
      <span><span class="mo-legend-dot" style="background:#eab308"></span> Warning (15–19h/week)</span>
      <span><span class="mo-legend-dot" style="background:#ef4444"></span> Overload (≥20h/week)</span>
    </div>
  </div>

  <div class="card app-filter-card">
    <p class="mo-privacy-hint">Decision feedback is only visible to Module Organisers and Administrators.</p>
    <div class="mo-status-toolbar" id="statusFilterBar">
      <span style="font-weight:600;font-size:14px;color:#0f172a;">Filter by Status:</span>
      <label><input type="checkbox" id="filterPending" checked /> Pending</label>
      <label><input type="checkbox" id="filterShortlisted" checked /> Shortlisted</label>
      <label><input type="checkbox" id="filterRejected" checked /> Rejected</label>
      <label><input type="checkbox" id="filterHired" checked /> Hired</label>
    </div>
    <div class="mo-toolbar-extra">
      <label>
        <span style="font-weight:600;">Sort within job:</span>
        <select id="sortMode" class="mo-sort-select">
          <option value="applied">Applied date</option>
          <option value="match">Best match</option>
          <option value="workload">Workload risk</option>
        </select>
      </label>
      <label><input type="checkbox" id="filterHighMatch" /> High match only (≥60%)</label>
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
</div>
</div>
<script src="../assets/js/common.js?v=teacher-student-style"></script>
<script src="../assets/js/mo-applications.js?v=teacher-ai-rec"></script>
</body>
</html>
