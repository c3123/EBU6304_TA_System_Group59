# Functional Specification: Sprint 1, Sprint 2, Sprint 3 and Sprint 4

## 1. Introduction
This document specifies the detailed functional requirements for Sprint 1, Sprint 2, Sprint 3 and Sprint 4 of the BUPT International School TA Recruitment System.

Sprint 1 focuses on the core recruitment foundation, including authentication, applicant profile management, and the basic Module Organiser recruitment workflow.

Sprint 2 extends the system with administrator-side management and the advanced Module Organiser operational workflow built on top of the Sprint 1 foundation.

Sprint 3 focuses on workflow optimization, reporting, self-service account maintenance, historical visibility, and non-functional improvement for final product delivery.

Sprint 4 covers remaining Module Organiser backlog items scheduled after Sprint 3 (for example internal hiring/rejection reasons) and small follow-up enhancements that were not listed in the original Sprint 3 screenshot backlog.

**Module Organiser (MO) consolidated documentation:** All MO backlog items from Sprint 1 through Sprint 4 are also described in one place in **Section 6 — Module Organiser (MO) Consolidated Specification**, so the MO subsystem can be read end-to-end without following multiple scattered subsections.

## 2. Sprint 1 Detailed Feature Specifications

### 2.1 Feature: User Authentication System
**User Stories:**
- **GEN_01:** As a user, I want to log in with my ID and password, so that I can access my specific dashboard (TA, MO, or Admin).
- **GEN_02:** As a user, I want to log out of the system, so that my session is terminated securely.

**Description:**
A centralized login portal that validates credentials and directs users to their respective role-based dashboards (Applicant UI, Module Organiser UI, or Admin UI).

**Acceptance Criteria:**
1. The system verifies credentials against a local `users.json` file.
2. Incorrect login attempts display a specific error message: "Invalid ID or Password."
3. Successful login creates a session and redirects the user to the correct dashboard based on their role.
4. Clicking "Logout" invalidates the session and returns the user to the login screen.

**Functional Requirement Details:**
- **Servlet Implementation:** `LoginServlet` handles POST requests for authentication.
- **Data Logic:** The system must use a JSON parser (e.g., GSON) to read `id`, `password`, and `role` fields from `users.json`.
- **Session Management:** `HttpSession` must be used to persist user identity across the web application.

**Assignee:** Sihan Chen / Tianxiao Ma
**Completion Date:** 2026-03-22

---

### 2.2 Feature: Applicant Core Recruitment Workflow System

**User Stories:**
**TA_01:** As an Applicant (TA), I want to create an applicant profile, so that I can apply for TA positions.
**TA_02:** As an Applicant (TA), I want to edit my personal information, so that Module Organisers can understand my background.
**TA_03:** As an Applicant (TA), I want to upload my CV, so that I can show my experience and skills.
**TA_05:** As an Applicant (TA), I want to browse available TA jobs, so that I can find opportunities that suit me.
**TA_07:** As an Applicant (TA), I want to view job details, so that I can understand the requirements before applying.
**TA_09:** As an Applicant (TA), I want to check my application status, so that I know whether I have been selected.

**Description:**
This feature provides a complete workflow for applicants to participate in the TA recruitment process. It allows users to create and manage their profiles, explore available TA opportunities, review detailed job information, and track the status of their applications.

The system ensures that applicants can smoothly complete the entire process from initial registration to post-application tracking, forming the foundation of the recruitment system.

**Acceptance Criteria:**
1. The applicant can successfully create a profile with required personal information.
2. The applicant can edit and update profile information at any time.
3. The applicant can upload a CV file in supported formats (PDF/DOC), and the file can be stored and retrieved.
4. The system displays a list of available TA jobs with essential information.
5. The applicant can view detailed job descriptions including requirements, workload, and duration.
6. The applicant can check application status with clear labels:
   - Submitted / Pending Review
   - Shortlisted
   - Accepted
   - Rejected

**Functional Requirement Details:**

- **Servlet Implementation:**
  - `StudentProfileServlet` handles profile retrieval and editing.
  - `StudentAttachmentUploadServlet` manages supporting document uploads.
  - `StudentAttachmentDeleteServlet` manages supporting document deletion.
  - `StudentJobsServlet` retrieves job listings.
  - `StudentApplicationsServlet` handles application submission and status retrieval.

- **Data Management:**
  - Applicant data (profile + CV path) is stored in a structured format (e.g., JSON).
  - Job data includes module name, requirements, workload, and schedule.
  - Application records are persisted with backend status values such as:
    - `pending`
    - `viewed`
    - `shortlisted`
    - `hired`
    - `rejected`
  - The applicant UI maps these backend status values into user-facing labels for status tracking.

- **File Handling:**
  - The system must validate file format and total upload size before upload.
  - Uploaded supporting documents are stored securely on the server.

- **Session Management:**
  - `HttpSession` is used to maintain applicant login state.
  - User-specific data is retrieved based on session identity.

**Assignee:** Fangyu Chu / Tianzi Xiong
**Completion Date:** 2026-03-22

---

### 2.3 Feature: Module Organiser Recruitment Workflow System

**User Stories (Sprint 1 baseline):** **MO_01**–**MO_06** — demand submission, approval visibility, publish, application listing, `pending`→`viewed` on detail open, and constrained withdrawal.

**Full specification:** See **Section 6 — Module Organiser (MO) Consolidated Specification**, **6.2 Sprint 1 — Demand, Publish, and Application Review Baseline**, for complete description, acceptance criteria, servlets, services, persistence, and UI files.

**Assignee:** Wanhe Ji / Huishun Hu  
**Completion Date:** 2026-03-22

---

## 3. Sprint 2 Detailed Feature Specifications

### 3.1 Feature: Module Organiser Sprint 2 Workflow Extension

**User Stories (summary):** Final hiring confirmation and recruitment lock, hiring history (`finalize` / `reopen`), job edit/delete/offline under lifecycle rules, MO notifications with read state (**MO_07**), and admin reopen of closed recruitment.

**Full specification:** See **Section 6**, **6.3 Sprint 2 — Final Hiring, Job Lifecycle, Notifications, and Admin Reopen**, including implementation branch notes (`dev-Huishun-hu`), servlets, services, `hiring_history.json` / `notifications.json`, and frontend integration on `mo-applications.jsp` / `teacher.jsp` / `admin.jsp`.

**Assignee:** Wanhe Ji / Huishun Hu  
**Branch (historical):** dev-Huishun-hu  
**Completion Date:** 2026-04-07

---

### 3.2 Feature: Administrator Oversight and User Management System

**User Stories:**
- **ADM_01:** As an Admin, I want to view a system-wide dashboard, so that I can understand the overall recruitment situation.
- **ADM_02:** As an Admin, I want to monitor TA workload based on hired records, so that I can identify students with high weekly commitments.
- **ADM_03:** As an Admin, I want to create and delete user accounts, so that I can manage access for students, MOs, and administrators.
- **ADM_06:** As an Admin, I want to reset a user's password, so that I can help users recover access when needed.

**Description:**
This Sprint 2 feature extends the administrator module from a read-only dashboard into a controllable administration workflow.

The scope of this iteration contains two coordinated parts:

1. **Administrator overview and monitoring**
   - show total users, jobs, and active applications;
   - list user records and job records;
   - calculate workload for hired students using weekly job hours;
   - support controlled recruitment reopen action for closed jobs.

2. **Administrator account management backend**
   - create new student / teacher / admin accounts;
   - delete existing accounts with safety constraints;
   - reset user passwords through admin-only APIs;
   - keep `users.json` and `students.json` synchronized for student accounts.

This provides the shared admin-side control layer for Sprint 2. In this iteration, both the backend management APIs and the core admin UI integration are implemented, including dashboard loading, workload display, user creation, account deletion, password reset, and recruitment reopen interaction.

**Acceptance Criteria:**
1. The admin dashboard displays system-level statistics:
   - total jobs
   - total users
   - total active applications
2. The admin dashboard lists user records with name, email, and role.
3. The admin dashboard lists job records with module code, title, teacher, status, recruitment state, and slots.
4. The admin dashboard displays workload monitoring data for hired students, including hired job count and weekly hours.
5. The system provides an admin-only API to create a new user account with role-based validation.
6. The system provides an admin-only API to delete a user account, with protection against deleting the current admin account or the last remaining admin.
7. The system provides an admin-only API to reset a user's password.
8. Creating a student account also creates a corresponding student profile record in `students.json`.
9. Deleting a student account also removes the corresponding student profile record from `students.json`.
10. Admin-only APIs reject non-admin access requests.

**Functional Requirement Details:**

- **Servlet Implementation:**
  - `AdminDashboardServlet` (`GET /api/admin/dashboard`) returns dashboard statistics, user list, job list, and workload data.
  - `AdminJobReopenServlet` (`POST /api/admin/jobs/reopen/{jobId}`) supports admin-side reopening of recruitment-closed jobs.
  - `AdminUserCreateServlet` (`POST /api/admin/users`) handles account creation.
  - `AdminUserDeleteServlet` (`DELETE /api/admin/users/{userId}`) handles account deletion.
  - `AdminUserResetPasswordServlet` (`POST /api/admin/users/reset-password/{userId}`) handles password reset.
  - `AdminBaseServlet` provides shared admin-only guard and JSON request/response helpers.

- **Service Layer:**
  - `AdminUserService` implements:
    - user creation;
    - user deletion;
    - password reset;
    - student-profile synchronization when student accounts are created or removed.
  - `AdminDemandReviewService` supports demand approval / rejection as part of the admin review workflow.

- **Data Management:**
  - `users.json` remains the source of truth for system account records.
  - `students.json` stores student profile records and must stay consistent with student user accounts.
  - `applications.json` and `jobs.json` are used to compute workload monitoring results.
  - Workload is calculated from applications whose status is `hired`, using the weekly `hours` field from the corresponding job.

- **Frontend Integration:**
  - `admin.jsp` provides the admin dashboard page shell.
  - `admin.js` loads dashboard statistics, user list, job list, and workload data from `/api/admin/dashboard`.
  - `admin.jsp` and `admin.js` provide create-user form handling and connect delete/reset-password buttons directly to the admin management APIs.
  - The admin UI refreshes dashboard data after successful management actions and blocks invalid delete actions for the current admin account and the last remaining admin.

- **Validation / Business Rules:**
  - `role` for account creation must be one of: `student`, `teacher`, `admin`.
  - `name`, `email`, and `password` are required for all new accounts.
  - Student creation additionally requires `studentId` and `programme`.
  - Email addresses must be unique.
  - Student IDs must be unique among student accounts.
  - The current admin user cannot delete their own account.
  - The last remaining admin account cannot be deleted.

- **Session / Access Control:**
  - All `/api/admin/*` routes require admin session identity.
  - Non-admin users must not access admin dashboard or admin management APIs.

**Assignee:** Sihan Chen / Tianxiao Ma  
**Branch:** dev-Sihan-Chen  
**Completion Date:** 2026-04-08

---
### 3.3 Feature: Applicant Job Application and Management System

**User Stories:**

-**TA_04:** Update CV
-**TA_06:** Filter Jobs
-**TA_08:** Apply for Job
-**TA_10:** Withdraw Application
-**TA_11:** View Assigned TA Jobs

**Functional Requirement Details**

-**Servlet Implementation**

  -StudentProfileServlet (GET/PUT /api/student/profile)
    Retrieves and updates the current student's profile, including skills, experience, phone, programme, and attachment metadata.
  -StudentAttachmentUploadServlet (POST /api/student/attachments)
    Uploads supporting documents attached to the student's profile.
  -StudentAttachmentDeleteServlet (DELETE /api/student/attachments/{attachmentId})
    Removes a supporting document from the current student's profile.
  -StudentJobsServlet (GET /api/student/jobs)
    Retrieves open jobs and job-matching fields for the logged-in student.
  -StudentApplicationsServlet (GET/POST /api/student/applications, DELETE /api/student/applications/{applicationId})
    Lists applications, submits a new job application, and withdraws an existing application before final hiring.
  -StudentAssignedJobsServlet (GET /api/student/my-jobs)
    Returns hired/accepted jobs assigned to the current student, including schedule, location, deadline, and weekly hours.
  -StudentAiAdvisorServlet (POST /api/student/ai-advisor)
    Returns AI-assisted or rule-based job guidance for the current student.
  -StudentNotificationsServlet (GET /api/student/notifications)
    Lists student-visible notifications.
  -StudentNotificationReadServlet (POST /api/student/notifications/read/{notificationId})
    Marks a student notification as read.

-**Service Layer**

  -StudentService
    Handles profile updates, attachment metadata, job listing, application submission, withdrawal, and assigned job retrieval.
  -JobMatchingService / SkillMatchScorer
    Computes structured job-fit information based on student skills and job requirements.
  -StudentNotificationService
    Maps notification records into student-facing notification responses.
  -AiAdvisorService
    Combines structured matching context with optional external AI advice and deterministic fallback guidance.

-**Data Management**
  -students.json
    Stores applicant profile data, skills, experience, phone, programme, and supporting document metadata.
  -jobs.json
    Stores job details including module name, schedule, and requirements.
  -applications.json
    Stores application records and backend status values such as `pending`, `viewed`, `shortlisted`, `hired`, `rejected`, plus `active` for withdrawn records.
  -Submitting an application creates a new active application record.
  -Withdrawn applications are marked inactive rather than physically removed.
  -Hired applications are displayed in My Jobs / Assigned Jobs.

-**Frontend Integration**

  -student.jsp
    Provides the student dashboard for profile editing, attachment upload/delete, job browsing, job details, applications, assigned jobs, notifications, password change, and AI advisor access.
  -student.js
    Connects the student dashboard to `/api/student/*` endpoints and renders job matching, application status, profile, assigned jobs, and notifications.

-**Validation / Business Rules**

  -Supporting document upload accepts the file types allowed by `FileStorageUtil`.
  -Applicants can apply only once per job.
  -Applications can only be withdrawn before acceptance.
  -Multiple filtering criteria can be applied simultaneously.
  -Only applications with status `hired` appear in My Jobs / Assigned Jobs.

-**Session / Access Control**

  -All /api/student/* endpoints require an authenticated student session.
  -Non-authenticated users cannot access student APIs.
  -Each operation validates the applicant identity before processing.

**Assignee:** Tianzi Xiong / Fangyu Chu  
**Completion Date:** 2026-04-11

---

## 4. Sprint 3 Detailed Feature Specifications

### 4.1 Feature: Administrator Sprint 3 Monitoring and Reporting Extension

**User Stories:**
- **ADM_04:** As an Admin, I want to compare each TA's actual workload with a configurable maximum threshold and mark overloaded TAs with a warning label, so that I can identify risk cases immediately.
- **ADM_05:** As an Admin, I want to generate and export a weekly recruitment summary report, so that I can report hiring progress to school management.
- **ADM_07:** As an Admin, I want to filter jobs by department or status, so that I can review targeted vacancy groups more efficiently.

**Description:**
This Sprint 3 administrator extension improves operational visibility and reporting on top of the Sprint 2 dashboard foundation.

The scope of this iteration contains three connected capabilities:

1. **Configurable workload warning control**
   - define and save a global weekly workload threshold;
   - compare hired workload totals against the threshold in real time;
   - highlight overloaded TAs clearly in the workload view.

2. **Recruitment summary reporting**
   - generate weekly summary output from current jobs and hiring data;
   - export report content in plain-text-compatible file formats;
   - support management-facing recruitment progress review.

3. **Focused job filtering**
   - filter job records by department and recruitment status;
   - narrow dashboard tables/cards to a target subset;
   - make large job lists easier to manage during final delivery.

**Acceptance Criteria:**
1. The admin can define a global workload threshold value and save it persistently.
2. The workload page compares each TA's current weekly hours against the saved threshold.
3. TAs whose workload exceeds the threshold are marked with a visible warning label.
4. The admin can trigger export of a weekly recruitment report in `.txt` or `.csv` format.
5. The exported report includes job title, organiser, hiring status, filled slots, and unfilled slots.
6. The admin can filter job records by department and status without reloading the whole page manually.
7. Filter results refresh both summary and detailed job views consistently.

**Functional Requirement Details:**

- **Servlet Implementation (new/extended):**
  - `AdminWorkloadSettingsServlet` (`GET/POST /api/admin/settings/workload-threshold`) for loading and saving the global workload threshold.
  - `AdminRecruitmentReportExportServlet` (`GET /api/admin/reports/weekly`) for generating downloadable weekly recruitment summaries.
  - `AdminDashboardServlet` (`GET /api/admin/dashboard`) extended to support optional filter parameters such as `department` and `status`.

- **Service Layer (new/extended):**
  - `AdminDashboardService`:
    - load dashboard data with filter-aware job aggregation;
    - compare workload totals against the configured threshold;
    - map workload records into normal / warning states.
  - `AdminReportService`:
    - collect weekly recruitment statistics;
    - format export content as text or CSV;
    - ensure exported output stays compatible with file-based submission rules.

- **Data Management:**
  - `applications.json` and `jobs.json` remain the primary source for workload and hiring statistics.
  - A new lightweight settings file such as `system_settings.json` stores the workload threshold persistently.
  - Exported report files are generated from current runtime data and do not require database support.
  - Department metadata, when used for filtering, must be stored in the job record structure consistently.

- **Frontend Integration:**
  - `admin.jsp` adds:
    - threshold setting input;
    - report export entry;
    - job filter controls.
  - `admin.js` adds:
    - threshold save/load interaction;
    - filter-driven dashboard refresh;
    - export trigger and success/failure feedback.

- **Validation / Business Rules:**
  - The workload threshold must be a positive numeric value.
  - Export requests must only include jobs and hiring records visible to administrators.
  - Filter values must be validated against supported department and status options.
  - Empty filter results must still return a successful response with an empty dataset.

- **Session / Access Control:**
  - All `/api/admin/*` routes remain admin-only.
  - Non-admin users must not be able to read or modify threshold settings or export reports.

**Assignee:** Sihan Chen / Tianxiao Ma
**Completion Date:** 2026-05-21

---

### 4.2 Feature: User Self-Service Password Change System

**User Stories:**
- **ALL_03:** As a user, I want to change my password, so that I can keep my account secure without relying on an administrator.

**Description:**
This Sprint 3 shared feature adds self-service credential maintenance for authenticated users.

The feature allows users from different roles to update their own password through a common account endpoint while preserving current session-based access control. It complements the administrator reset capability from Sprint 2 by adding a normal user-owned password management flow.

**Acceptance Criteria:**
1. An authenticated user can submit their current password and a new password.
2. The system validates the current password before making any change.
3. The new password is written back to `users.json` successfully.
4. The user receives a clear success or failure message after submission.
5. Invalid old passwords are rejected without changing stored credentials.

**Functional Requirement Details:**

- **Servlet Implementation (new):**
  - `ChangePasswordServlet` (`POST /api/account/change-password`) for authenticated self-service password updates.

- **Service Layer (new):**
  - `AccountService`:
    - verify current user identity from session;
    - validate old password;
    - apply the password update to the matching user record.

- **Data Management:**
  - `users.json` remains the source of truth for credential records.
  - Password changes update the current user's existing record only.
  - No extra persistence file is required for this feature.

- **Frontend Integration:**
  - Each role portal may expose a common account settings entry or inline password-change form.
  - Shared frontend logic should submit old/new password values to the common account API and show validation feedback.

- **Validation / Business Rules:**
  - `oldPassword`, `newPassword`, and confirmation input are required.
  - The new password must be different from the old password.
  - Blank or invalid password changes must be rejected with a specific error message.

- **Session / Access Control:**
  - Only authenticated users can call `/api/account/change-password`.
  - Users can only change their own password and cannot modify another account through this endpoint.

**Assignee:** Sihan Chen / Tianxiao Ma
**Completion Date:** 2026-05-21

---

### 4.3 Feature: Performance and Loading Experience Enhancement

**User Stories:**
- **NFR_01:** As a stakeholder, I want the system to load file-based data quickly and provide loading feedback, so that users do not experience noticeable lag during normal operation.

**Description:**
This Sprint 3 non-functional feature improves system responsiveness and user feedback for file-based workflows.

The goal is to keep the application usable under realistic project data volume while making loading, refresh, and export operations visibly understandable to users. The improvement should support the final demonstration, testing tasks, and day-to-day use of the file-based architecture.

**Acceptance Criteria:**
1. Normal dashboard and list-loading operations complete within an acceptable short delay under demonstration-scale data volume.
2. Long-running file reads or exports show a clear loading or progress state on the frontend.
3. Large or empty datasets do not cause the interface to freeze or become unresponsive.
4. Performance-related improvements are documented and can be demonstrated during final delivery.

**Functional Requirement Details:**

- **Backend Optimization Scope:**
  - reduce repeated file reads where safe within a single request flow;
  - keep JSON parsing and response mapping simple and predictable;
  - avoid unnecessary duplicate aggregation passes on jobs, users, and applications.

- **Frontend Integration:**
  - key pages such as `student.jsp`, `teacher.jsp`, `mo-applications.jsp`, and `admin.jsp` must display loading, empty, and error states clearly.
  - export and report actions must show in-progress feedback until the operation completes.
  - periodic refresh logic must avoid disruptive re-rendering of user-expanded content where possible.

- **Testing / Verification:**
  - automated regression suite is maintained under `web/src/test/java` and currently contains 165 JUnit 5 tests.
  - browser E2E and Tomcat integration tests are maintained under `e2e/tests` and currently contain 13 Playwright tests.
  - automated tests cover Admin services, MO services, Student services, shared account behavior, and servlet access control using isolated temporary JSON data directories.
  - manual response-time checks should be performed for login, dashboard loading, job browsing, application listing, and export flows.
  - regression checks should confirm that optimization changes do not alter business logic or access control.
  - test evidence should be recorded for the final report and demonstration preparation.

- **Data Management:**
  - optimization must remain compatible with the required plain-text / JSON persistence approach.
  - no database, cache server, or heavyweight framework dependency may be introduced.

**Assignee:** Team-wide non-functional polish
**Completion Date:** 2026-05-21

---

### 4.4 Feature: Module Organiser Sprint 3 Productivity and Review Enhancement

**User Stories (summary):** **MO_05** (status, notes, filters, batch), **MO_08** (posted jobs / demands list), **MO_09** (CSV export).

**Full specification:** See **Section 6**, **6.4 Sprint 3 — Review Productivity, Posted Jobs Visibility, and CSV Export**.

**Assignee:** Wanhe Ji / Huishun Hu  
**Completion Date:** 2026-04

---

### 4.5 Feature: Applicant Sprint 3 Assignment and Schedule Visibility

**User Stories:**
- **TA_12:** As an Applicant (TA), I want to view my assigned TA jobs and schedule, so that I can understand workload and avoid conflicts with my classes.

**Description:**
This Sprint 3 applicant feature extends the student-side workflow beyond application tracking into post-hiring visibility.

The feature provides a dedicated view of accepted jobs, associated schedule information, and workload-related summary so that hired students can understand their confirmed commitments clearly after recruitment decisions are finalized.

**Acceptance Criteria:**
1. Accepted jobs are displayed in a dedicated applicant-side view.
2. Each accepted job shows key schedule/workload information such as module, organiser, weekly hours, and time arrangement.
3. Only applications with a final accepted/hired state appear in the assigned-jobs view.
4. The assigned-jobs page updates correctly after hiring results change.

**Functional Requirement Details:**

- **Servlet Implementation (new/extended):**
  - `StudentAssignedJobsServlet` (`GET /api/student/my-jobs`) for retrieving hired job assignments and schedule-related data.
  - `StudentApplicationsServlet` may be extended to expose a clearer separation between active applications and accepted assignments if needed.

- **Service Layer (new/extended):**
  - `StudentService`:
    - collect applications whose status is `hired`;
    - join hired application records with corresponding job metadata;
    - map schedule, module, organiser, and weekly-hour information into a student-facing response.

- **Data Management:**
  - `applications.json` provides the hiring outcome through final application status.
  - `jobs.json` provides module, organiser, workload, and schedule/time-slot information for assigned jobs.
  - Schedule-related fields must remain consistent across job posting and assigned-job display.

- **Frontend Integration:**
  - `student.jsp` + `student.js` add a dedicated assigned-jobs or schedule panel.
  - The applicant UI should distinguish clearly between:
    - active applications still under review;
    - confirmed TA assignments already accepted.

- **Validation / Business Rules:**
  - Only final hired assignments are shown in the assigned-jobs view.
  - Withdrawn, rejected, or pending applications must not appear in the assigned-jobs list.
  - If no hired jobs exist, the UI should show a clear empty state instead of a broken table or panel.

- **Session / Access Control:**
  - All `/api/student/*` routes require authenticated student identity.
  - Students can only view their own assigned jobs and schedule data.

**Assignee:** Tianzi Xiong / Fangyu Chu
**Completion Date:** 2026-05-21

---

## 5. Sprint 4 Detailed Feature Specifications

### 5.1 Feature: Module Organiser Sprint 4 — Hiring/Rejection Reasons (MO_10) (cross-reference)

**User Stories (summary):** **MO_10** (decision feedback for `hired` / `shortlisted` / `rejected`), admin read-only archiving, and related **manual hire** history behaviour.

**Full specification:** See **Section 6**, **6.5 Sprint 4 — Decision Feedback (MO_10), Admin Archiving, Follow-Ups, and colour-coded workload preview** (workload UI is an extra Sprint 4 enhancement not on the MO backlog screenshot).

**Assignee:** Wanhe Ji / Huishun Hu  
**Completion Date:** 2026-04

---

### 5.2 Feature: Administrator Sprint 4 Management and Oversight Extension

**User Stories:**
- **ADM_08:** As an Admin, I want to view enhanced overview statistics, so that I can quickly understand current user, job, application, vacancy, and recruitment progress across the system.
- **ADM_09:** As an Admin, I want to monitor recruitment health by job, so that I can identify posts with too few applicants, unfilled vacancies, or approaching deadlines.
- **ADM_10:** As an Admin, I want workload risk to be displayed with clear levels such as Low, Normal, Warning, and Overload, so that I can distinguish normal workload from potential over-assignment instead of relying on one generic threshold only.
- **ADM_11:** As an Admin, I want to inspect applications from a specific job, so that I can audit applicant status and MO notes without exposing a separate global archive screen.
- **ADM_12:** As an Admin, I want to export and back up recruitment data in simple file formats, so that the school can keep offline records and share summaries with management.
- **ADM_13:** As an Admin, I want to receive system alerts from a header alert modal, so that risks are visible on demand without occupying the main dashboard area.
- **ADM_14:** As an Admin, I want a demand approval workbench with editable pending / approved / rejected decisions, so that demand review can be corrected or reset from one place.
- **ADM_15:** As an Admin, I want to drill down from a job to its applications, so that I can audit applicant status and MO notes for a specific post without leaving the administrator portal.
- **ADM_16:** As an Admin, I want recruitment exports to respect the current job filters, so that exported reports match what is shown on screen.

**Description:**
This Sprint 4 administrator extension builds on the Sprint 2 and Sprint 3 admin dashboard. Sprint 2 provides the baseline dashboard and account management. Sprint 3 adds workload threshold settings, department/status filtering, reports, and shared account maintenance. Sprint 4 refines the administrator role into a stronger monitoring and audit role.

The scope of this feature is not to replace the Module Organiser workflow. The admin module remains an oversight layer. Administrators can inspect system-wide data, identify risk, export records, and follow up with organisers, while MO-owned actions such as applicant review, hiring decisions, and feedback editing stay in the MO workflow.

**Acceptance Criteria:**

1. **Enhanced administrator overview**
   - The dashboard shows total users, students, Module Organisers, admins, jobs, active jobs, closed jobs, applications, hired records, and unfilled positions.
   - Statistics are computed from the current JSON storage files and refresh after dashboard reload.
   - Empty or missing data files do not break the dashboard; the UI displays zero values or empty states.
   - Overview cards use consistent labels and do not require administrators to inspect raw JSON files.

2. **Recruitment health monitoring**
   - Each job card shows module code, title, organiser, publish date, status, number of positions, applicant count, hired count, filled/unfilled state, deadline, and weekly hours.
   - The job list can be filtered by status, department, and Module Organiser.
   - Jobs with zero applicants, unfilled positions, or approaching deadlines can be visually identified.
   - Closed or withdrawn jobs remain readable for audit purposes but are clearly labelled.

3. **Workload risk management**
   - Workload rows display each hired student's total weekly hours and assigned positions.
   - Workload is classified into four visible levels:
     - Low: below 10 hours/week.
     - Normal: 10 to 14 hours/week.
     - Warning: 15 hours/week up to below the configured overload threshold.
     - Overload: greater than or equal to the configured overload threshold.
   - The configured overload threshold remains editable by the admin and is persisted in `system_settings.json`.
   - Workload cards and legend use the same classification rules so that the UI and backend remain consistent.
   - Assigned job chips show the module/job title and weekly hours used in the calculation.

4. **Single-job application audit**
   - The standalone application archive page is not exposed in the administrator navigation.
   - The admin can still open applications from an individual job row or card.
   - The selected job drilldown shows applicant identity, student number, status, applied time, evaluation notes, and decision feedback where available.
   - The drilldown is read-only; administrators must not edit MO evaluation notes or decision feedback.
   - Withdrawn, rejected, hired, shortlisted, and pending records remain distinguishable.

5. **Data export and backup**
   - The admin can export weekly recruitment reports in CSV and TXT formats.
   - Exported reports include at least job title, organiser, status, hired count, available positions, and unfilled positions.
   - Export actions do not modify live recruitment data.
   - The system can be extended to export workload reports, job-level application drilldowns, and JSON backups using the same admin-only access model.

6. **Administrator alerts**
   - The admin dashboard can surface alerts for overload risk, unfilled jobs, jobs close to deadline, jobs with no applicants, and unusual incomplete records.
   - Alerts include enough context for follow-up, such as job title, organiser, student name, current workload, or deadline.
   - Alerts are opened from a warning button next to Logout, with a badge showing the active alert count.
   - The alert modal supports close button, backdrop click, and Escape key dismissal.
   - If no alerts exist, the page shows a clear "No alerts" state.

7. **Demand approval workbench**
   - The admin can view demand records by approval status, especially newly submitted `pending` demands.
   - Demand rows show module code, title, submitting organiser, submitted time, planned count, hour range, demand notes where provided, and current approval status.
   - The admin can set each demand to `pending`, `approved`, or `rejected` from the workbench.
   - Setting a demand to `approved` allows the MO to publish it through the existing publish workflow.
   - Setting a demand to `pending` or `rejected` prevents MO publishing until it is approved again.
   - The admin can optionally provide a short rejection reason when selecting `rejected`.
   - Rejected demand reasons are persisted and visible to the submitting MO in the demand progress view.

8. **Recruitment results overview entry**
   - Recruitment Results is not shown as a top-level tab.
   - The System Overview panel provides a clear button to open the Recruitment Results view.
   - Recruitment Results retains KPI cards and adds visual charts, including department hired/vacancy bars and an overall hiring mix chart.
   - Empty recruitment result datasets show a clear empty chart state instead of broken graphics.

9. **Single-job application drilldown**
   - Each admin job row or card provides a read-only "View Applications" action.
   - The drilldown shows only applications for the selected job, including applicant name, student number, applied time, status, evaluation notes, and decision feedback.
   - The drilldown can be filtered by application status without exposing a separate global application page.
   - The admin can export the selected job's application rows in CSV or TXT format.

10. **Filtered weekly export**
   - Weekly recruitment report exports include the same `status` and `department` filters used by the jobs dashboard.
   - Exporting with no filters or `all` filters still produces the full report.
   - Export file names include the selected filter scope and export date where possible.
   - Invalid filter or format values return a validation error instead of a misleading report.

**Functional Requirement Details:**

- **Servlet Implementation:**
  - `AdminDashboardServlet` (`GET /api/admin/dashboard`) remains the main dashboard endpoint and may be extended with additional overview, recruitment health, workload, and alert fields.
  - `AdminWorkloadSettingsServlet` (`GET/POST /api/admin/settings/workload-threshold`) remains responsible for loading and saving the overload threshold.
  - `AdminRecruitmentReportExportServlet` (`GET /api/admin/reports/weekly?format=csv|txt`) remains responsible for downloadable weekly recruitment summaries.
  - `AdminApplicationsServlet` (`GET /api/admin/applications?jobId=&status=`) provides read-only application rows for the selected job drilldown.
  - `AdminDemandsServlet` (`GET /api/admin/demands?status=pending|approved|rejected|all`) provides the administrator demand approval workbench data.
  - `AdminDemandReviewServlet` (`POST /api/admin/demands/review/{jobId}?action=pending|approve|reject`) sets demand approval status; reject accepts an optional JSON body containing `reason`.
  - `AdminWorkloadReportExportServlet` (`GET /api/admin/reports/workload?format=csv|txt`) exports workload rows and assigned job details.
  - `AdminApplicationArchiveExportServlet` (`GET /api/admin/reports/applications?format=csv|txt&jobId=&status=`) remains available for exporting a single job's filtered application rows.
  - `AdminBackupExportServlet` (`GET /api/admin/reports/backup`) exports a JSON backup bundle for the current file-based data.
  - `AdminRecruitmentOutcomeServlet` (`GET /api/admin/recruitment-outcome`) provides the Recruitment Results view launched from System Overview.
  - Administrator alerts are returned through `GET /api/admin/dashboard` so the header alert modal shares the dashboard data source.

- **Service Layer:**
  - `AdminDashboardService` computes overview statistics, job health fields, workload rows, and risk labels from `jobs.json`, `applications.json`, `users.json`, and `students.json`.
  - `AdminDemandReviewService` lists demand records and applies approval or rejection decisions while preserving MO-owned job publishing rules.
  - `AdminReportService` prepares CSV and TXT export content using the same job and application aggregation rules as the dashboard, including current job filters for weekly reports.
  - Single-job application drilldown logic reads existing `ApplicationRecord` fields without changing MO-owned decision data.
  - Workload calculation must reuse the shared weekly-hours rule:
    - if `job.hours > 0`, use `hours`;
    - otherwise, if both `hourMin` and `hourMax` exist, use `round((min + max) / 2)`;
    - otherwise, use `0`.

- **Data Management:**
  - `jobs.json` stores job lifecycle, department, schedule, organiser, position, deadline, workload, approval review time, and optional rejection reason fields.
  - `applications.json` stores application status, active flag, timestamps, evaluation notes, decision feedback, and hiring status.
  - `users.json` and `students.json` provide user and student profile details needed for dashboard labels.
  - `system_settings.json` stores administrator-configured workload settings.
  - Sprint 4 admin features must remain compatible with old JSON records that do not contain newer optional fields such as `department`, `schedule`, or decision feedback.

- **Frontend Integration:**
  - `admin.jsp` provides separate dashboard areas for overview, workload, users, jobs, demand review, announcements, account settings, and a Recruitment Results view opened from System Overview.
  - `admin.js` loads dashboard data, applies status/department/organiser filters, renders workload level cards, handles demand approval actions, opens single-job application drilldowns, and triggers CSV/TXT export downloads.
  - Admin alerts are opened through the header warning button instead of a persistent overview card or top-level tab.
  - The standalone Application Archive tab is removed; job-level application drilldown remains available from Jobs.
  - Workload UI must show level labels, weekly hours, assigned positions, and a legend matching backend classification.
  - Job cards should display business-friendly recruitment health information instead of only raw table columns.
  - Empty states must be explicit for no jobs, no workload records, no job applications, no recruitment result data, and no alerts.

- **Validation / Business Rules:**
  - All admin APIs require an authenticated admin session.
  - Admin monitoring views must not expose student-only private upload files unless the job-level application drilldown explicitly requires a downloadable attachment link.
  - Application drilldown and report APIs are read-only and must not change job status, application status, or MO feedback.
  - Filters must be optional; when no filter is supplied, the dashboard returns the full admin-visible dataset.
  - Invalid export formats must return a clear validation error instead of generating an ambiguous file.
  - Demand records can be switched between `pending`, `approved`, and `rejected` by admins.
  - Only `approved` demand records can be published by MOs.
  - Rejection reason text is optional but must remain short enough for display in the MO demand progress view.

- **Session / Access Control:**
  - Non-admin users must be rejected from `/api/admin/*`.
  - Student and teacher sessions must not be able to access admin reports, job application drilldown, workload settings, or alert data.
  - Admin actions that modify settings, such as threshold saving, must use POST and must validate the submitted value before persistence.

**Assignee:** Sihan Chen / Tianxiao Ma  
**Completion Date:** 2026-05-21

---

## 6. Module Organiser (MO) — Consolidated Specification (Sprint 1 to Sprint 4)

This section is the **single end-to-end functional specification** for the Module Organiser role. Sprint-numbered subsections **2.3**, **3.1**, **4.4**, and **5.1** elsewhere in this document are short cross-references that point here.

**Assignees:** **Wanhe Ji / Huishun Hu** — joint ownership of the MO subsystem (demand and job lifecycle on the teacher portal, applicant review and hiring workflows on the applications page, notifications, exports, and internal decision feedback). Individual commits may sit on branches such as `dev-Huishun-hu`; integration and acceptance follow team process.

**Primary UI:** `teacher.jsp` + `teacher.js` (My Jobs / demands / lifecycle actions / notifications entry), `mo-applications.jsp` + `mo-applications.js` (Applicants: filters, status, batch, finalize, history modal, notes, CSV). **API prefix:** `/api/mo/*` (session-protected for the teacher/MO role).

---

### 6.1 Data and Cross-Cutting Rules

- **Persistence (file-based, no database):** `jobs.json` (demands and postings), `applications.json` (applications plus MO-only `evaluationNotes` and `decisionFeedback`), `hiring_history.json` (finalize / reopen / manual hire audit trail), `notifications.json` (MO notification read state), plus shared `users.json` / `students.json` for profiles surfaced in MO lists.
- **Ownership:** Every MO API resolves the current organiser from session and enforces that only jobs owned by that MO (`teacherId` / MO id) are readable or mutable.
- **Applicant visibility:** MO-only text (`evaluationNotes`, `decisionFeedback`) must not appear on student-facing pages; administrators may read aggregated application rows for archiving (`GET /api/admin/applications`).

---

### 6.2 Sprint 1 — Demand, Publish, and Application Review Baseline (MO_01–MO_06)

**User Stories:**
- **MO_01:** Submit a TA demand for a course.
- **MO_02:** View approval progress of submitted demands.
- **MO_03:** Publish an **approved** demand as a visible TA position.
- **MO_04:** View **active** applications for **own** jobs only.
- **MO_05 (Sprint 1):** Opening application detail promotes status from `pending` to `viewed`.
- **MO_06:** Withdraw a position only when there are **no** active applications.

**Description:** Sprint 1 establishes the MO pipeline from demand creation through publishing and first-pass applicant review. The teacher portal drives demand and publish/withdraw; the applications page lists candidates for owned jobs and supports detail view with automatic `pending`→`viewed` transition.

**Acceptance Criteria (summary):**
1. Demand submission includes course / headcount / workload intent; duplicate pending demands for the same course are rejected or blocked per business rules.
2. Demand list shows approval state, publish state, and withdraw state for the current MO.
3. Only `approvalStatus = approved` jobs can be published; otherwise the API returns a clear error.
4. Publish supplies student-visible fields (e.g. location, requirements, deadline) per `MoJobPublishServlet` contract.
5. Application list returns **active only** (`active = true`) and **owned jobs only**.
6. Detail read updates `pending` → `viewed` where applicable.
7. Withdraw is rejected when any active application exists for the job.

**Functional Requirement Details:**

- **Servlets:** `MoDemandCreateServlet` (`POST /api/mo/demands`), `MoDemandListServlet` (`GET /api/mo/demands/list`), `MoJobPublishServlet` (`POST /api/mo/jobs/publish/{jobId}`), `MoJobWithdrawServlet` (`POST /api/mo/jobs/withdraw/{jobId}`), `MoApplicationListServlet` (`GET /api/mo/applications`), `MoApplicationDetailServlet` (`GET /api/mo/applications/detail/{applicationId}`), and `AdminDemandReviewServlet` for admin approval during testing.
- **Services:** `MoDemandService`, `MoJobService`, `MoApplicationService` (ownership + active filter + detail status transition), `MoBusinessException` for consistent errors.
- **Frontend:** `teacher.jsp` / `teacher.js` for demands and publish/withdraw; `mo-applications.jsp` / `mo-applications.js` for listing and detail.

---

### 6.3 Sprint 2 — Final Hiring, Job Lifecycle, Notifications, and Admin Reopen

**User Stories (summary):** Extended **MO_04** / **MO_06** — finalize hiring and lock recruitment; view hiring **history**; edit/delete/offline jobs under lifecycle rules; **MO_07** notifications; admin **reopen** closed recruitment.

**Description:** Adds recruitment closure (`recruitmentClosed`, `closedAt` on `JobPosting`), auditable `hiring_history.json`, MO notifications in `notifications.json`, stricter edit/delete/offline rules on `MoJobService`, and `MoHiringService` for finalize and history. `MoApplicationService` blocks ordinary status changes when a job is closed (per implemented checks). Admin reopen is exposed from the admin dashboard and appends history.

**Key endpoints:** `POST /api/mo/hiring/finalize`, `GET /api/mo/hiring/state`, `GET /api/mo/hiring/history`, `POST /api/mo/jobs/edit/{jobId}`, `POST /api/mo/jobs/delete/{jobId}`, `POST /api/mo/jobs/offline/{jobId}`, `GET /api/mo/notifications`, `POST /api/mo/notifications/read/{notificationId}`, `POST /api/admin/jobs/reopen/{jobId}`.

**Frontend:** Final hiring modal and history modal on `mo-applications.jsp`; job cards with edit/delete/offline and notification UI on `teacher.jsp`; reopen control on `admin.jsp`.

**Branch note:** Much of the Sprint 2 MO extension was developed on **`dev-Huishun-hu`** (contributor tag `yeahyeah66` in earlier notes); **Wanhe Ji** shares MO requirements ownership and integration testing for the same subsystem.

---

### 6.4 Sprint 3 — Review Productivity, Posted Jobs Visibility, and CSV Export (MO_05 / MO_08 / MO_09)

**User Stories:**
- **MO_05 (Sprint 3 scope):** Mark applicants Shortlisted / Rejected / Pending-style states, add **evaluation notes**, support **single and batch** status updates, **filter** the list by status, and keep UI state consistent after saves.
- **MO_08:** View a **history-style** list of posted TA jobs/demands (draft / published / withdrawn / recruitment closed) for reuse and audit.
- **MO_09:** Export applicant data for offline analysis (implemented as **browser CSV** from the current filtered rows).

**Description:** Builds on Sprint 1–2 with richer review tooling: multi-checkbox status filter (`status` query: comma-separated; `pending` includes both `pending` and `viewed`; `__none__` when no box is ticked returns **no** rows to avoid accidental “show all”), batch bar + `POST /api/mo/applications/batch/status`, `POST /api/mo/applications/status`, and `POST /api/mo/applications/notes` for `evaluationNotes`. After status changes, the UI **reloads the list** so filters and rows stay aligned. **MO_08** is covered by `GET /api/mo/demands/list` on the teacher portal. **MO_09** uses client-side `exportCsv()` producing UTF-8 CSV (columns include application id, job id, student name, status, timestamps, notes — exact header as implemented in `mo-applications.js`); list JSON already exposes richer profile fields if the team extends CSV later.

**Service notes:** `MoApplicationService.listApplications` implements `parseStatusFilter` / `matchesStatusFilter` (legacy “three statuses mean no filter” behaviour was removed to fix wrong rows when e.g. “Hired” is unchecked). Direct status change to `hired` can append a **`manual_hire`** record for history consistency (see 6.5).

**Backlog alignment (product board — Sprint 3, MO_05 / MO_08 / MO_09):** The Sprint 3 MO rows on the team backlog are the wording baseline; the table below maps each acceptance line to the current build.

| Backlog item | Acceptance theme | In current system |
|--------------|------------------|-------------------|
| **MO_05** | Single and batch status marking | **Yes** — per-row control + `POST /api/mo/applications/status`; batch + `POST /api/mo/applications/batch/status`. |
| **MO_05** | Private evaluation notes | **Yes** — `evaluationNotes`, `POST /api/mo/applications/notes`; not exposed to applicants. |
| **MO_05** | Filter list by marked status | **Yes** — checkboxes + `status` query (`__none__` when no box selected). |
| **MO_05** | Auto-save without data loss | **Yes** — notes auto-save via debounced POST; status changes save immediately and reload the list after success. |
| **MO_08** | History list: sort, name, status, applicant count, hire count, release time, deadline | **Yes** — `GET /api/mo/jobs/history` returns history rows with status, applicant count, hire count, release time, and deadline; teacher portal also shows demand/job progress cards. |
| **MO_08** | Drill into historical job + application/hiring data | **Yes** — MO can move from job/history context into the applications and hiring history views for owned jobs. |
| **MO_08** | One-click copy to create new job | **Yes** — `POST /api/mo/jobs/reuse` supports reuse/copy workflow for creating a new demand from an existing job. |
| **MO_09** | Export respects filters | **Yes** — CSV from currently loaded filtered rows. |
| **MO_09** | Core columns: name, ID, major, time, status, skills | **Yes** — applicant export service includes the core applicant identity, programme/major, application time, status, and skills columns. |
| **MO_09** | Plain text, no DB | **Yes**. |
| **MO_09** | Usable in Excel | **Yes** — UTF-8 CSV. |

---

### 6.5 Sprint 4 — Decision Feedback (MO_10), Admin Archiving, Follow-Ups, and Applicant Workload Colours

**User Stories:**
- **MO_10:** Submit short **hiring / rejection / shortlist reasons** for internal school traceability (MO and admin only, not shown to applicants).

**Description:** `decisionFeedback` on `ApplicationRecord`, maintained by `MoApplicationService.updateDecisionFeedback` (eligibility: status in `hired` | `shortlisted` | `rejected`; **max 200 characters** — backlog text sometimes says “200 words”; the running code uses **characters**). Persisted in `applications.json`. MO UI posts to `POST /api/mo/applications/feedback`. Administrators use `GET /api/admin/applications` to read rows including `evaluationNotes` and `decisionFeedback` for archiving. **Manual hire history** (`manual_hire` in `hiring_history.json`) keeps audit aligned when hire happens outside the main finalize modal.

**Backlog alignment (product board — Sprint 4, MO_10):**

| Backlog line | In current system |
|--------------|-------------------|
| Short feedback up to **200 words** | Implementation enforces **200 characters**; align backlog wording with code or extend limit if course requires “words”. |
| Visible only to MO and administrators | **Yes** — not on applicant APIs; admin list includes field for archiving. |
| Linked to applicant + synced to admin backend | **Yes** — stored on application row; `GET /api/admin/applications` for read/archive. |

**Sprint 4 — Additional enhancement (not on the MO backlog screenshot): colour-coded weekly workload preview on Applicants**

This feature **was not** one of MO_05–MO_10 in the shared board; it is documented here under Sprint 4 as a team-added UX improvement.

- **Where:** `mo-applications.jsp` (styles: `mo-wl-low` / `mo-wl-normal` / `mo-wl-warn` / `mo-wl-over`, neutral border) and `mo-applications.js` (`workloadTier`, `currentHiredHoursElsewhere`, `jobWeeklyHours`, `wlPanelClass` / `wlCardClass`).
- **Behaviour:** For each applicant card, the UI estimates **current** weekly hours on other hired posts and **projected total if this applicant were hired** for the current job, then assigns a tier (**Low / Normal / Warning / Overload**) with **colour-coded** card border and summary panel backgrounds.
- **Safety hint:** The UI can surface a prominent warning when hiring would exceed a **20 hours/week** style limit (wording as implemented in the script).
- **Scope note:** This is a **front-end assistance** layer on the MO applications page; it does not replace admin workload policy elsewhere (e.g. ADM_04 in §4.1).

---

### 6.6 MO API Surface (Reference)

| Area | Method | Path (representative) | Role |
|------|--------|------------------------|------|
| Demands | POST | `/api/mo/demands` | Create demand |
| Demands | GET | `/api/mo/demands/list` | List own demands / jobs |
| Publish / withdraw | POST | `/api/mo/jobs/publish/*`, `/api/mo/jobs/withdraw/*` | Job lifecycle |
| Applications | GET | `/api/mo/applications` | List (optional `jobId`, `status`) |
| Applications | GET | `/api/mo/applications/detail/*` | Detail + `pending`→`viewed` |
| Status | POST | `/api/mo/applications/status` | Single status update |
| Status | POST | `/api/mo/applications/batch/status` | Batch status update |
| Notes | POST | `/api/mo/applications/notes` | Save `evaluationNotes` |
| Feedback | POST | `/api/mo/applications/feedback` | Save `decisionFeedback` |
| Hiring | POST | `/api/mo/hiring/finalize` | Finalize hiring |
| Hiring | GET | `/api/mo/hiring/state`, `/api/mo/hiring/history` | State + history |
| Jobs | POST | `/api/mo/jobs/edit/*`, `delete/*`, `offline/*` | Lifecycle |
| Notifications | GET | `/api/mo/notifications` | List |
| Notifications | POST | `/api/mo/notifications/read/*` | Mark read |
| Admin | GET | `/api/admin/applications` | Admin list (archiving) |

Paths follow the concrete `@WebServlet` mappings in the `com.ta.web.mo` package (some routes use `/*` pathInfo patterns as in the scaffold).

---

### 6.7 Completion

**Assignees:** Wanhe Ji / Huishun Hu  
**Completion window:** 2026-04 (align with project demonstration; adjust per team records).

