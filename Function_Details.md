# Functional Specification: Sprint 1, Sprint 2 and Sprint 3

## 1. Introduction
This document specifies the detailed functional requirements for Sprint 1, Sprint 2 and Sprint 3 of the BUPT International School TA Recruitment System.

Sprint 1 focuses on the core recruitment foundation, including authentication, applicant profile management, and the basic Module Organiser recruitment workflow.

Sprint 2 extends the system with administrator-side management and the advanced Module Organiser operational workflow built on top of the Sprint 1 foundation.

Sprint 3 focuses on workflow optimization, reporting, self-service account maintenance, historical visibility, and non-functional improvement for final product delivery.

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
**Completion Date:**

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
**Completion Date:**

---

### 2.3 Feature: Module Organiser Recruitment Workflow System

**User Stories:**
- **MO_01:** As a Module Organiser (MO), I want to submit a TA demand for my course, so that I can request teaching assistants for the upcoming teaching period.
- **MO_02:** As a Module Organiser (MO), I want to view the approval progress of my submitted demands, so that I can know whether a position can be published.
- **MO_03:** As a Module Organiser (MO), I want to publish an approved TA position, so that applicants can view and apply for it.
- **MO_04:** As a Module Organiser (MO), I want to view active applications for my own positions, so that I can review candidates.
- **MO_05:** As a Module Organiser (MO), I want an application to automatically change from `pending` to `viewed` when I open its detail page, so that the review progress is traceable.
- **MO_06:** As a Module Organiser (MO), I want to withdraw a position only when there are no active applications, so that I do not invalidate positions that students are still applying for.

**Description:**
This feature provides the Sprint 1 MO-side workflow for teaching assistant recruitment. It covers two connected parts of the MO process handled by our two assignees:

1. **Demand and position workflow**
   - submit TA demand
   - view approval progress
   - publish approved positions
   - withdraw positions under the required constraints

2. **Applicant management workflow**
   - list applications for the current MO's own jobs only
   - view detailed application records
   - automatically update application status from `pending` to `viewed`
   - hide inactive / withdrawn application records from the visible list

This completes the basic Sprint 1 MO operational flow from demand creation to applicant review.

**Acceptance Criteria:**
1. The MO can submit a TA demand with course name, planned count, and expected workload range.
2. The system prevents duplicate submission when the same MO already has a `pending` demand for the same course.
3. The MO can view a list of their own submitted demands with approval status, publish state, and withdraw state.
4. Only demands with `approvalStatus = approved` can be published as visible positions.
5. The MO can provide publication details including location, requirements, and deadline.
6. The system rejects publishing attempts for non-approved jobs with a clear backend error.
7. The MO can only see applications belonging to jobs they own.
8. The applications list only displays records with `active = true`.
9. Opening an application detail page changes its status from `pending` to `viewed`.
10. A job cannot be withdrawn if there is any active application attached to it.
11. A job can be withdrawn successfully when no active application exists.
12. The MO frontend page supports direct testing in development mode and displays timestamps in a readable second-level format.

**Functional Requirement Details:**

- **Servlet Implementation:**
  - `MoDemandCreateServlet` handles demand creation.
  - `MoDemandListServlet` returns the current MO's submitted demand list.
  - `MoJobPublishServlet` handles publishing approved jobs.
  - `MoJobWithdrawServlet` handles job withdrawal.
  - `MoApplicationListServlet` lists active applications for the current MO.
  - `MoApplicationDetailServlet` returns application detail and updates status.
  - `AdminDemandReviewServlet` supports demand approval state updates for testing / admin review flow.

- **Service Layer:**
  - `MoDemandService` implements demand creation and progress querying.
  - `MoJobService` implements publish and withdraw rules.
  - `MoApplicationService` implements ownership filtering, active-only listing, and status update on detail view.
  - `MoBusinessException` standardizes MO business error handling.

- **Data Management:**
  - MO demand and job records are stored in `jobs.json`.
  - Application records are stored in `applications.json`.
  - New demand records include fields such as `approvalStatus`, `published`, and `withdrawn`.
  - Application records include fields such as `jobId`, `studentId`, `status`, and `active`.

- **Frontend Integration:**
  - `teacher.jsp` is used as the MO job workflow page.
  - `teacher.js` connects the page to the MO backend APIs for demand submission, publishing, and withdrawal.
  - `mo-applications.jsp` and `mo-applications.js` support active applicant listing and application detail viewing.
  - The MO page keeps an entry point to the applicant management page.

- **Session / Access Control:**
  - MO identity is resolved from session-based login in normal use.
  - MO users can only access their own jobs and related applications.

**Assignee:** Wanhe Ji / Huishun Hu
**Completion Date:**

---

## 3. Sprint 2 Detailed Feature Specifications

### 3.1 Feature: Module Organiser Sprint 2 Workflow Extension

**User Stories:**
- **MO_04 (Extension):** As a Module Organiser (MO), I want to confirm final hiring from a candidate set and lock recruitment to read-only, so that final decisions are consistent and auditable.
- **MO_04 (Extension):** As a Module Organiser (MO), I want to view hiring history records, so that I can trace finalize/reopen operations and selected candidates.
- **MO_06 (Extension):** As a Module Organiser (MO), I want to edit or delete jobs under lifecycle constraints, so that I can correct draft information without breaking published records.
- **MO_06 (Extension):** As a Module Organiser (MO), I want to take published jobs offline through a controlled action, so that live posts can be closed safely.
- **MO_07:** As a Module Organiser (MO), I want to receive notifications for new applications and mark them as read, so that I can track incoming applications efficiently.

**Description:**
This Sprint 2 extension adds an end-to-end MO-side operational workflow on top of Sprint 1.  
It introduces final hiring confirmation with recruitment lock, auditable hiring history, stricter job lifecycle controls (edit/delete/offline), and a notification UI with read management.  
The implementation keeps role boundaries explicit: MO executes hiring/finalization and job lifecycle operations, while admin has a dedicated reopen endpoint for closed recruitment states.

**Acceptance Criteria:**
1. The Applicants page provides a per-job **Confirm Final Hiring** entry and modal-based final selection.
2. Final confirmation writes final statuses, switches the job to `Recruitment Closed`, and prevents further MO status changes on that job.
3. The Applicants page can open a **View History** modal and display operation records (`finalize` / `reopen`), timestamps, and hired candidate names.
4. The My Jobs page enforces lifecycle actions:
   - editable in non-closed and non-published draft-like states;
   - deletable only when not published/closed and without active applications;
   - published jobs can be taken offline through dedicated action.
5. The My Jobs page shows a notification entry with unread count and a list containing applicant name, job name, and application time.
6. Notification entries support **Mark as Read** and persist read state after refresh.
7. Admin dashboard can reopen recruitment-closed jobs via API, and reopen records are included in hiring history.
8. Applicants detail expansion is user-controlled and does not collapse unexpectedly during periodic refresh.

**Functional Requirement Details:**

- **Servlet Implementation (new/extended):**
  - `MoHiringFinalizeServlet` (`POST /api/mo/hiring/finalize`) for final confirmation submission.
  - `MoHiringStateServlet` (`GET /api/mo/hiring/state`) for per-job recruitment lock state.
  - `MoHiringHistoryServlet` (`GET /api/mo/hiring/history`) for job-level history records.
  - `MoJobEditServlet` (`POST /api/mo/jobs/edit/{jobId}`) for editable lifecycle updates.
  - `MoJobDeleteServlet` (`POST /api/mo/jobs/delete/{jobId}`) for controlled draft deletion.
  - `MoJobOfflineServlet` (`POST /api/mo/jobs/offline/{jobId}`) for take-offline operation.
  - `MoNotificationsServlet` (`GET /api/mo/notifications`) for MO notification retrieval.
  - `MoNotificationReadServlet` (`POST /api/mo/notifications/read/{notificationId}`) for read-state updates.
  - `AdminJobReopenServlet` (`POST /api/admin/jobs/reopen/{jobId}`) for admin-only reopen control.

- **Service Layer (new/extended):**
  - `MoHiringService`:
    - finalize hiring decision set and close recruitment;
    - expose recruitment state;
    - aggregate history records for Applicants history modal;
    - support admin reopen with history append.
  - `MoJobService`:
    - add edit/delete/offline methods with lifecycle guard checks;
    - retain ownership and validation constraints.
  - `MoNotificationService`:
    - generate/merge notification records from active applications;
    - compute unread count and map to MO-facing response;
    - persist mark-as-read actions.
  - `MoApplicationService`:
    - block status update endpoint when `recruitmentClosed=true`.

- **Data Model & Persistence:**
  - `JobPosting` extended with:
    - `recruitmentClosed` (Boolean),
    - `closedAt` (ISO timestamp).
  - New persistence files:
    - `hiring_history.json` for finalize/reopen records,
    - `notifications.json` for MO notification state.
  - `JsonUtility` extended with load/save methods for both new datasets.

- **Frontend Integration:**
  - `mo-applications.jsp` + `mo-applications.js`:
    - final hiring modal;
    - history modal;
    - recruitment closed flag in job group bar;
    - read-only action behavior after closure;
    - detail expand persistence (manual collapse control).
  - `teacher.jsp` + `teacher.js`:
    - edit/delete/take-offline controls per job;
    - notification button, unread badge, notification panel;
    - mark-as-read interaction.
  - `admin.jsp` + `admin.js`:
    - recruitment column visibility;
    - reopen action button for closed jobs.

- **Branch / Ownership Note:**
  - Implementation branch: `dev-Huishun-hu`.
  - Contributor signature for this Sprint 2 extension: `yeahyeah66`.

**Assignee:** yeahyeah66 (Huishun Hu)  
**Branch:** dev-Huishun-hu  
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

  -ApplicantCVUpdateServlet (POST /api/applicant/cv/update)
    Allows applicants to upload a new CV and replace the existing CV file.
  -ApplicantJobFilterServlet (GET /api/applicant/jobs/filter)
    Retrieves job listings based on filter criteria such as module name, time, or requirements.
  -ApplicationSubmitServlet (POST /api/applicant/apply/{jobId})
    Handles submission of job applications and records them in the system.
  -ApplicationWithdrawServlet (POST /api/applicant/withdraw/{applicationId})
    Allows applicants to withdraw submitted applications before acceptance.
  -ApplicantAssignedJobsServlet (GET /api/applicant/my-jobs)
    Returns the list of accepted jobs assigned to the applicant.

-**Service Layer**

  -ApplicantProfileService
    Handles CV updating and validation.
  -JobFilterService
    Implements filtering logic based on module name, time, or skills.
  -ApplicationService
    Handles application submission, withdrawal, and retrieval of assigned jobs.

-**Data Management**
  -students.json
    Stores applicant profile data and CV file paths.
  -jobs.json
    Stores job details including module name, schedule, and requirements.
  -applications.json
    Stores application records and status values (Submitted, Accepted, Rejected, Withdrawn).
  -Updating a CV replaces the existing CV path.
  -Submitting an application creates a new application record.
  -Withdrawn applications update their status to Withdrawn.
  -Accepted applications are displayed in My TA Jobs.

-**Frontend Integration**

  -applicantProfile.jsp
    Provides CV upload and replacement interface.
  -jobList.jsp
    Displays available jobs and filtering options.
  -application.jsp
    Allows applicants to submit job applications.
  -myApplications.jsp
    Displays submitted applications with withdrawal options.
  -myTAJobs.jsp
    Displays accepted TA job assignments.

-**Validation / Business Rules**

  -Only PDF files are allowed for CV uploads.
  -Applicants can apply only once per job.
  -Applications can only be withdrawn before acceptance.
  -Multiple filtering criteria can be applied simultaneously.
  -Only applications with status Accepted appear in My TA Jobs.

-**Session / Access Control**

  -All /api/applicant/* endpoints require an authenticated applicant session.
  -Non-authenticated users cannot access applicant APIs.
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

**Assignee:**
**Completion Date:**

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

**Assignee:**
**Completion Date:**

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
  - manual response-time checks should be performed for login, dashboard loading, job browsing, application listing, and export flows.
  - regression checks should confirm that optimization changes do not alter business logic or access control.
  - test evidence should be recorded for the final report and demonstration preparation.

- **Data Management:**
  - optimization must remain compatible with the required plain-text / JSON persistence approach.
  - no database, cache server, or heavyweight framework dependency may be introduced.

**Assignee:**
**Completion Date:**

---

### 4.4 Feature: Module Organiser Sprint 3 Productivity and Review Enhancement

**User Stories:**
- **MO_05:** As a Module Organiser (MO), I want to mark applicants as shortlisted, rejected, or pending and add evaluation notes, so that I can track review decisions consistently.
- **MO_08:** As a Module Organiser (MO), I want to view a history list of all posted TA jobs, so that I can trace past recruitment and reuse job information later.
- **MO_09:** As a Module Organiser (MO), I want to export my applicant list as a CSV/JSON file, so that I can review candidate information offline.

**Description:**
This Sprint 3 MO feature set focuses on review productivity, historical visibility, and offline analysis support.

The scope of this iteration contains three coordinated parts:

1. **Application review enhancement**
   - support richer review-state management;
   - store organiser-side evaluation notes;
   - allow status-based filtering and batch-oriented review workflow.

2. **Posted job history**
   - show current and past job records in a unified history view;
   - expose status, applicant count, hire count, publish time, and deadline;
   - support quick reuse of historical job information for future recruitment cycles.

3. **Applicant data export**
   - export current or filtered applicant lists;
   - support plain-text-compatible output formats such as CSV / JSON;
   - improve offline review and evidence preparation.

**Acceptance Criteria:**
1. The MO can mark applicant status as shortlisted, rejected, or pending/viewed through the application page.
2. The MO can save a private evaluation note for each applicant, and the note persists after refresh.
3. The application list can be filtered by review status.
4. The MO can open a history list of posted jobs sorted by release time and see summary metadata for each job.
5. The MO can open historical job details and review related applicant/hiring summary data.
6. The MO can export all applicants or a filtered subset in CSV or JSON format.
7. Exported files include applicant name, applicant ID, major/programme, application time, status, and skills.

**Functional Requirement Details:**

- **Servlet Implementation (new/extended):**
  - `MoApplicationStatusServlet` (`POST /api/mo/applications/status`) extended to support note persistence and batch review operations.
  - `MoJobHistoryServlet` (`GET /api/mo/jobs/history`) for posted-job history list and summary retrieval.
  - `MoApplicantExportServlet` (`GET /api/mo/applications/export`) for CSV / JSON export generation.

- **Service Layer (new/extended):**
  - `MoApplicationService`:
    - persist organiser-side review notes;
    - filter and group application records by status;
    - support consistent review-state updates.
  - `MoJobService`:
    - provide history-oriented job summaries;
    - prepare reusable job metadata for future reposting.
  - `MoExportService`:
    - transform visible applicant data into downloadable CSV / JSON output;
    - keep export content aligned with current MO ownership rules.

- **Data Management:**
  - `applications.json` is extended to store MO-side evaluation note fields and review timestamps where needed.
  - `jobs.json` remains the source of truth for current and historical job metadata.
  - Exported files are generated dynamically and do not replace source JSON data.

- **Frontend Integration:**
  - `mo-applications.jsp` + `mo-applications.js` add:
    - status filter controls;
    - note entry / autosave or save action;
    - export trigger for current applicant scope.
  - `teacher.jsp` + `teacher.js` or a dedicated history page add:
    - posted-job history list;
    - job summary view;
    - quick reuse / copy entry for historical jobs.

- **Validation / Business Rules:**
  - Evaluation notes are organiser-visible and admin-visible only; they are not shown to applicants.
  - Export results must include only jobs owned by the current MO.
  - Batch review actions must preserve ownership checks and recruitment-closed rules.
  - Historical records must remain readable even after recruitment is closed.

- **Session / Access Control:**
  - All `/api/mo/*` endpoints require authenticated MO identity.
  - MOs can only review, export, and access history for their own jobs and applications.

**Assignee:**
**Completion Date:**

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

**Assignee:**
**Completion Date:**
