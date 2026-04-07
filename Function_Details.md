# Functional Specification: Sprint 1 - Core Recruitment Foundation

## 1. Introduction
This document specifies the detailed functional requirements for Iteration 1 (Sprint 1) of the BUPT International School TA Recruitment System. The primary focus of this iteration is to establish the core authentication infrastructure, applicant profile management, and basic job vacancy posting capabilities using a Java-based web architecture and text-based (JSON) data persistence.

## 2. Detailed Feature Specifications

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

### 2.4 Feature: Module Organiser Sprint 2 Workflow Extension

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
   - Submitted
   - Under Review
   - Accepted
   - Rejected

**Functional Requirement Details:**

- **Servlet Implementation:**
  - `ProfileServlet` handles profile creation and editing.
  - `CVUploadServlet` manages CV file uploads.
  - `JobServlet` retrieves job listings and job details.
  - `ApplicationServlet` handles application status retrieval.

- **Data Management:**
  - Applicant data (profile + CV path) is stored in a structured format (e.g., JSON).
  - Job data includes module name, requirements, workload, and schedule.
  - Application records store status values:
    - Submitted
    - Under Review
    - Accepted
    - Rejected

- **File Handling:**
  - The system must validate file format before upload.
  - Uploaded CV files are stored securely on the server.

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
  - A temporary development fallback using `X-MO-ID` header or `?moId=` query parameter may be enabled only for testing.
  - MO users can only access their own jobs and related applications.

**Assignee:** Wanhe Ji / Huishun Hu

**Completion Date:**

---
