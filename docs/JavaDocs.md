# JavaDocs and Code Documentation

This document summarizes the main Java packages and explains how to generate the JavaDoc HTML included in the final software evidence.

## Generate JavaDocs

Run from the repository root:

```powershell
mvn -f web/pom.xml "-Dmaven.repo.local=.m2repo" javadoc:javadoc
```

Generated documentation is written to:

```text
web/target/site/apidocs/index.html
```

The generated `target` directory is ignored by Git. For the final `Software_group59.zip`, generate the JavaDocs before packaging the software evidence if HTML JavaDocs are required.

## Package Overview

| Package | Responsibility |
| --- | --- |
| `com.ta.model` | Plain Java data models persisted to JSON files, such as users, students, jobs, applications, notifications, and hiring history. |
| `com.ta.dto` | Request and response DTOs used by servlet endpoints. Subpackages separate `admin`, `mo`, `student`, and shared account contracts. |
| `com.ta.service` | Business logic for account, admin, MO, and student workflows. Services read/write JSON data through `JsonUtility` and enforce role-specific rules. |
| `com.ta.web` | Servlet controllers and role-specific base servlets. These classes map HTTP endpoints to service methods and return JSON responses or JSP navigation. |
| `com.ta.util` | Shared infrastructure utilities for JSON persistence, file uploads, weekly-hour calculation, and response formatting support. |
| `com.ta.constant` | Shared constants such as error codes. |

## Main Service Areas

- `AccountService` implements shared self-service password changes.
- `AdminDashboardService`, `AdminDemandReviewService`, `AdminReportService`, `AdminUserService`, and related admin services implement dashboard monitoring, demand approval, reporting, backup, announcements, and user management.
- `MoDemandService`, `MoJobService`, `MoApplicationService`, `MoHiringService`, and `MoNotificationService` implement Module Organiser demand, publishing, applicant review, hiring, and notification workflows.
- `StudentService`, `JobMatchingService`, `SkillMatchScorer`, and AI advisor services implement student profile, job browsing, applications, assigned jobs, notifications, and recommendation support.

## Endpoint Documentation Source

Functional endpoint documentation is maintained in `Function_Details.md`. The implemented servlet mappings are the source of truth for final API verification:

- Student APIs use `/api/student/*`.
- MO APIs use `/api/mo/*`.
- Admin APIs use `/api/admin/*`.
- Shared account API uses `/api/account/change-password`.

## Testing Documentation

Automated service tests live under:

```text
web/src/test/java
```

The current automated tests cover:

- MO applicant management, export, status filtering, status transitions, notes/feedback, and notifications.
- Student profile persistence, attachment upload/delete, apply/withdraw, assigned jobs, and AI advisor fallback.
- Shared password change success and validation failures.
- Servlet access control for unauthenticated and wrong-role API access.

Manual browser and Tomcat smoke acceptance testing remains documented in:

```text
docs/Acceptance_Test_Checklist.md
```
