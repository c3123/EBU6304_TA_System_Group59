# JavaDocs and Code Documentation

This document summarizes the main Java packages in the `web` module and explains how to generate the JavaDoc HTML included in the final software evidence.

## Generate JavaDocs

Run from the repository root:

```powershell
mvn -f web/pom.xml "-Dmaven.repo.local=.m2repo" javadoc:javadoc
```

Generated documentation is written to:

```text
web/target/site/apidocs/index.html
```

The generated `target` directory is ignored by Git. For the final `Software_group59.zip`, generate JavaDocs before packaging the software evidence if HTML documentation is required.

The Maven project is configured for Java 21 via `web/pom.xml`:

- `maven.compiler.release=21`
- `maven-javadoc-plugin` source set to Java 21
- `junit-jupiter` version `5.10.2`
- `mockito` version `5.12.0`
- Tomcat 10.1 is used for integration tests through the Cargo plugin

## Package Overview

The main Java source root is `web/src/main/java/com/ta`.

| Package | Responsibility |
| --- | --- |
| `com.ta.model` | Domain and persistence models persisted to JSON files, including `User`, `StudentProfile`, `JobPosting`, `ApplicationRecord`, `NotificationRecord`, `HiringHistoryRecord`, and related entities. |
| `com.ta.dto` | Request and response DTOs for servlet APIs. Subpackages separate contracts for `admin`, `mo`, `student`, and shared `account` operations. |
| `com.ta.service` | Business logic layer. Contains role-specific services for account, admin, MO, and student workflows, plus shared service utilities. |
| `com.ta.service.account` | Account management business logic, including password change and authentication-related operations. |
| `com.ta.service.admin` | Administrator workflows such as dashboards, demand review, reporting, announcements, user management, workload settings, backup/export, and application archive. |
| `com.ta.service.mo` | Module Organiser workflows for demand creation, job publishing, application review, hiring, notifications, export, and applicant recommendation. |
| `com.ta.service.student` | Student workflows including profile management, job matching, applications, assigned jobs, notifications, skill matching, and AI recommendation support. |
| `com.ta.service.student.ai` | AI advisor integration and skill extraction support for student recommendation features. |
| `com.ta.web` | Web controllers and shared servlet infrastructure for authentication, login, logout, registration, home page, and admin-level servlet handling. |
| `com.ta.web.mo` | MO-specific servlet endpoints under `/api/mo/*` and MO page operations. |
| `com.ta.web.student` | Student-specific servlet endpoints under `/api/student/*` and student page operations. |
| `com.ta.util` | Shared utilities for JSON persistence, file uploads, time handling, weekly-hour calculation, workload scoring, and response formatting. |
| `com.ta.constant` | Global constants and error code definitions used across the application. |

## Layered Architecture

The project follows a layered Java Web architecture to separate presentation, business logic, persistence, and utility responsibilities.

### Web Layer (`com.ta.web`)
The web layer contains servlet controllers responsible for handling HTTP requests and responses. These servlets validate incoming requests, perform access control checks, call service-layer methods, and return JSP pages or JSON responses.

### Service Layer (`com.ta.service`)
The service layer implements the core business logic of the system. Services coordinate workflows such as job publishing, application review, hiring, profile management, notifications, and workload analysis.

### DTO Layer (`com.ta.dto`)
DTO (Data Transfer Object) classes are used to transfer structured request and response data between the frontend and backend layers. Separate DTO packages are maintained for admin, MO, student, and account operations.

### Model Layer (`com.ta.model`)
The model layer defines the domain entities persisted to JSON files, including users, student profiles, jobs, applications, notifications, and hiring records.

### Utility Layer (`com.ta.util`)
The utility layer provides reusable infrastructure support such as JSON persistence, file upload handling, time calculation, workload scoring, and response formatting.

The layered structure improves maintainability, readability, modularity, and testing isolation across the application.

## Main Service Areas

- `com.ta.service.account.AccountService` handles self-service password change and account-related validation.
- `com.ta.service.admin.AdminDashboardService`, `AdminDemandReviewService`, `AdminReportService`, and `AdminUserService` implement administrator monitoring, demand approval, reporting, backup/export, announcement posting, user administration, and workload rules.
- `com.ta.service.admin.WorkloadOverloadAnnouncementService` and `AdminWorkloadSettingsService` implement workload alerts and workload-setting logic.
- `com.ta.service.mo.MoDemandService`, `MoJobService`, `MoApplicationService`, `MoHiringService`, `MoNotificationService`, `MoApplicationExportService`, and `ApplicantRecommendationService` manage MO demand creation, job lifecycle, application review, hiring, notifications, exports, and recommendation flows.
- `com.ta.service.student.StudentService`, `JobMatchingService`, `SkillMatchScorer`, `SkillExtractionService`, and AI advisor services support student profile persistence, job browsing, matching, application rules, assigned job tracking, notifications, skill scoring, and generated recommendations.

## Endpoint Documentation Source

Functional endpoint documentation is maintained in `Function_Details.md`. The implemented servlet mappings are the source of truth for final API verification.

- Student APIs use `/api/student/*` and are implemented in `web/src/main/java/com/ta/web/student`.
- MO APIs use `/api/mo/*` and are implemented in `web/src/main/java/com/ta/web/mo`.
- Admin APIs use `/api/admin/*` and are implemented in `web/src/main/java/com/ta/web`.
- Shared account API uses `/api/account/change-password` and is implemented in `web/src/main/java/com/ta/web/AccountChangePasswordServlet.java`.

## Testing Documentation

Automated service tests live under:

```text
web/src/test/java
```

The `web` module currently contains 27 JUnit 5 test classes and 186 JUnit test methods covering the following areas:

- Admin dashboard aggregation, demand review, user management, reports, announcements, recruitment outcome analytics, application archive, workload settings, backup/export, and overload reminders.
- MO applicant management, export, status filtering, status transitions, notes and feedback, hiring rules, job lifecycle, job history, applicant recommendation, and notifications.
- Student profile persistence and fallback, attachment upload and delete validation, job matching, job listing, apply and withdraw rules, assigned job management, notifications, skill scoring, and AI advisor behavior.
- Shared password change success and validation failure cases.
- Servlet access control for unauthenticated users and wrong-role requests.

Browser E2E and Tomcat integration tests live under:

```text
e2e/tests
```

The Playwright suite contains 13 tests. It prepares isolated JSON seed data, starts Tomcat 10.1 through Maven Cargo, deploys the WAR under `/web`, and verifies role login, access control, key role APIs, and Admin/Student/MO page smoke rendering.

Manual acceptance checks remain documented in:

```text
docs/Acceptance_Test_Checklist.md
```

## Notes for submission

- This document is a hand-written summary of the JavaDoc generation process and package responsibilities, not the generated HTML itself.
- If the final evidence requires the HTML output, include the contents of `web/target/site/apidocs` after running the Maven command.
- The most reliable source for API details remains `Function_Details.md` plus the servlet classes in `web/src/main/java/com/ta/web`.
