
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
