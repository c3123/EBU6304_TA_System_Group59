# EBU6304_TA_System_Group59

International School Teaching Assistant Recruitment System - a Java Servlet/JSP web application with JSON file persistence and no external database.

| GitHub Username | QMID |
| --- | --- |
| Chudadi-cfy | 231226613 |
| J-3469 | 231226598 |
| yeahyeah66 | 231226288 |
| qiaolezi2006-rgb | 231226299 |
| MA0204 | 231226587 |
| c3123 | 231226624 |

---

## Quick Start

### Requirements

- JDK 21 (`web/pom.xml` compiles with `--release 21`)
- Maven 3.8+
- Apache Tomcat 10.1+ (Jakarta Servlet 6)
- Node.js 20+ and npm, for Playwright browser E2E tests

### Project Layout

```text
web/
|- pom.xml
`- src/
   |- main/
   |  |- java/          # servlets, services, DTOs, models, utilities
   |  `- webapp/        # JSP pages, static assets, WEB-INF/data
   `- test/java/        # JUnit 5 unit tests
e2e/
|- scripts/             # Playwright data preparation
`- tests/               # Browser E2E and Tomcat integration tests
scripts/
|- dev-deploy.ps1       # sync exploded WAR to Tomcat while keeping runtime data
`- generate_seed_data.py # regenerate demo JSON dataset
docs/
|- Acceptance_Test_Checklist.md
`- JavaDocs.md
package.json            # Playwright E2E commands
playwright.config.ts    # Tomcat-backed browser test configuration
```

### Build

```powershell
mvn -f web/pom.xml clean package
```

WAR output:

```text
web/target/web.war
```

### Deploy To Tomcat

1. Copy `web/target/web.war` to Tomcat `webapps/`.
2. Start Tomcat.
3. Open `http://localhost:8080/web/`.

Optional exploded deployment for local development:

```powershell
.\scripts\dev-deploy.ps1 -TomcatWebappsPath "C:\path\to\tomcat\webapps"
```

---

## Demo Accounts

Primary demo logins use password `demo123`.

| Role | Login | Password |
| --- | --- | --- |
| Student | `student@demo.com` | `demo123` |
| Teacher / MO | `teacher@demo.com` | `demo123` |
| Admin | `admin@demo.com` | `demo123` |

The bundled seed data also includes 50 students, 20 teachers, and 1 admin. Extra demo accounts follow the patterns `student02@demo.qmul.ac.uk`, `student03@demo.qmul.ac.uk`, `teacher02@demo.qmul.ac.uk`, and so on, with password `demo123`.

To regenerate demo jobs, users, applications, and related JSON data:

```powershell
python scripts/generate_seed_data.py
```

---

## Project Status

The current main-line code integrates Sprint 1, Sprint 2, Sprint 3, and Sprint 4 work. Sprint 4 is not represented by a separate git tag; it is integrated through later pull requests and commits on `main`.

| Milestone | Evidence | Scope |
| --- | --- | --- |
| Sprint 1 | `v1.0-sprint1` | Authentication, base Servlet/JSP structure, TA profile foundation, basic MO job flow |
| Sprint 2 | `v2.0-sprint2` | Admin dashboard/user management, demand review, richer TA and MO recruitment workflows |
| Sprint 3 | `v3.0-Sprint3` | Workload settings, reports, password change, MO review tools, assigned-job visibility, documentation and testing |
| Sprint 4 | PRs and commits after Sprint 3 tag | Admin analytics/alerts, announcements, AI assistance, student calendar, MO decision feedback, applicant recommendation, final test/documentation hardening |

Recent main-line evidence from the Git history:

| PR / Commit | Branch / Author Area | Main contribution |
| --- | --- | --- |
| PR #98, `160b2c6` | `dev-Fangyu-Chu` | Improved student job selection and matching behavior |
| PR #97, `39abc01`, `73a72a9` | `dev-Tianxiao-Ma` | Admin job analysis charts, seed data, workload overload notifications, README refresh |
| PR #96, `bc64cdb` | `dev-Fangyu-Chu` | MO applicant AI recommendation support |
| PR #95, `9690635` | `dev-Tianzi-Xiong` | Student hired-job calendar |
| PR #93, `4abf5c0` | `dev-Sihan-Chen` | Expanded automated tests and final documentation readiness |
| PR #92, `562f86b` | `dev-Sihan-Chen` | Admin UI alignment with the student dashboard style |
| PRs #86-#87, `220adad`, `39abc01` | `dev-Tianxiao-Ma` | Sprint 4 admin charts, demand announcements, recruitment UI, workload notifications |
| PR #85, `b5453ab` | `dev-Sihan-Chen` | Admin demand review and alert workflow refinement |
| PRs #82-#84 | Student/Admin/AI branches | Student page improvements, admin extensions, AI bug fixes |

---

## Implemented Features

### Shared

- Session login/logout with role-based redirection for `student`, `teacher`, and `admin`.
- Role-based access control for protected JSP pages and `/api/*` routes.
- Student self-registration.
- Self-service password change through `/api/account/change-password`.
- JSON persistence through `JsonUtility`; no database is required.

### Student / TA

- Profile management with personal details, programme, skills, and experience.
- Supporting document upload, download, delete, and application-time selection.
- Browse open jobs, inspect job details, apply once per job, and withdraw before final hiring.
- Track application status and receive notifications, including system announcements.
- View assigned/hired jobs with workload and schedule details.
- Hired-job calendar on the student portal.
- AI advisor with deterministic fallback when external AI is disabled or unavailable.
- Improved job matching and selection behavior based on skills and workload context.

### Module Organiser / Teacher

- Submit TA demands and track admin approval progress.
- Publish approved demands as student-visible jobs.
- Edit, offline, withdraw, reuse, and delete jobs under lifecycle constraints.
- Review applicants on `mo-applications.jsp`, including detail view and automatic `pending` to `viewed` transition.
- Single and batch status updates for pending/viewed/shortlisted/hired/rejected applicants.
- Private evaluation notes and decision feedback for internal review.
- Final hiring confirmation, recruitment closure, and hiring history.
- Applicant export in CSV/JSON-compatible workflows.
- MO notifications and announcement display.
- Applicant workload preview and MO applicant AI recommendation support.

### Administrator

- System overview with role distribution, job publication trend, application trend, and KPI cards.
- Workload monitoring with configurable threshold, Low/Normal/Warning/Overload levels, drilldown, and export.
- Bulk overload reminders to affected students.
- User management: create/delete users and reset passwords with safety constraints.
- Demand review: approve, reject, or return to pending, with optional rejection reason and teacher notification.
- Jobs view with status/department/teacher filters, job health labels, application drilldown, charts, CSV/TXT export, and JSON backup.
- Recruitment Results view with date filters, hiring-mix chart, department/vacancy analytics, and CSV export.
- Announcements broadcast to students, teachers, or all users.
- Alerts modal for workload, vacancy, deadline, and data-quality risks.
- Recruitment reopen for closed jobs.
- My Account password change.

---

## Sprint Delivery Summary

### Sprint 1 - Foundation

Goal: establish the base recruitment system and role-specific entry points.

| Area | Members | Delivered scope |
| --- | --- | --- |
| Core architecture and authentication | Sihan Chen, Tianxiao Ma | MVC-style Servlet/JSP project structure, Tomcat deployment setup, `JsonUtility`, `LoginServlet`, `LogoutServlet`, role-based login page |
| TA / Applicant module | Tianzi Xiong, Fangyu Chu | Applicant profile foundation and available job listing |
| MO / Teacher module | Wanhe Ji, Huishun Hu | Demand/job creation form, validation, and basic MO job dashboard |

### Sprint 2 - End-To-End Recruitment Operations

Goal: extend the foundation into a usable recruitment workflow across admin, TA, and MO roles.

| Area | Members | Delivered scope |
| --- | --- | --- |
| Admin and shared backend | Sihan Chen, Tianxiao Ma | Admin dashboard, workload monitoring, demand review, user create/delete/reset password, shared JSON consistency |
| TA workflow extension | Tianzi Xiong, Fangyu Chu | Profile persistence, attachments, application submission, withdrawal, and status tracking |
| MO workflow extension | Wanhe Ji, Huishun Hu | Demand lifecycle, job publishing/editing/offline controls, applicant review, hiring confirmation, notifications, hiring history |

### Sprint 3 - Reporting, Review Tools, And Final Delivery Readiness

Goal: improve reporting, review productivity, account maintenance, and test/documentation coverage.

| Area | Members | Delivered scope |
| --- | --- | --- |
| Admin monitoring/reporting | Sihan Chen, Tianxiao Ma | Workload threshold settings, weekly recruitment report export, job filtering, dashboard improvements |
| Shared account and quality | Sihan Chen, Tianxiao Ma | Self-service password change, access-control tests, JavaDocs, acceptance checklist |
| TA workflow polish | Tianzi Xiong, Fangyu Chu | Assigned jobs, schedule visibility, notifications, AI advisor baseline |
| MO review productivity | Wanhe Ji, Huishun Hu | Status filters, batch status updates, evaluation notes, posted-job history, applicant export |

### Sprint 4 - Final Enhancements

Goal: complete remaining backlog items and add final analytics, AI assistance, alerts, and demo-readiness improvements.

| Area | Members / Branch Evidence | Delivered scope |
| --- | --- | --- |
| Admin analytics and oversight | Sihan Chen, Tianxiao Ma; PRs #85, #87, #97 | Demand review refinement, workload levels and drilldown, filtered reports, recruitment results, charts, alerts, backup/export, overload notifications |
| Student final experience | Tianzi Xiong, Fangyu Chu; PRs #89, #95, #98 | Student page improvements, hired-job calendar, AI advisor polish, improved job matching/selection, attachment download fixes |
| MO final review features | Wanhe Ji, Huishun Hu; PRs #88, #91 plus later integration | Decision feedback, applicant workload colors, job history/reuse, export/test coverage |
| AI-assisted review | Fangyu Chu branch evidence; PR #96 | MO applicant AI recommendation based on skill match and projected workload |
| Final readiness | Sihan Chen and team; PR #93 | Expanded service tests, documentation alignment, JavaDocs notes, acceptance-test checklist |

---

## Runtime Data Storage

All runtime data lives under `web/src/main/webapp/WEB-INF/data/`.

| File | Purpose |
| --- | --- |
| `users.json` | Login accounts and roles |
| `students.json` | Student profiles, skills, experience, and attachment metadata |
| `jobs.json` | Demands and job postings |
| `applications.json` | Application records, statuses, notes, and decision feedback |
| `notifications.json` | Student/MO/admin notifications and announcements |
| `hiring_history.json` | Final hiring and reopen audit events |
| `system_settings.json` | Workload threshold and related settings |

Uploaded student files are stored under `WEB-INF/uploads/students/`, which is created at runtime.

Initialization rules:

- Existing JSON files are read and written in place.
- Missing list-based files are created as `[]`.
- Missing `system_settings.json` is initialized with `workloadThresholdHours: 20`.
- Demo content can be reset by restoring or regenerating the JSON files and optionally clearing `WEB-INF/uploads`.

---

## Testing

JUnit 5 tests use isolated temporary JSON data directories through `System.setProperty("ta.data.dir", ...)`; Tomcat is not required for unit tests.

```powershell
mvn -f web/pom.xml test
```

The automated suite currently contains **165** JUnit 5 tests covering Admin, MO, Student, shared account behavior, and servlet access control. The Maven build is configured for Java 21 bytecode. The Surefire configuration also enables Byte Buddy's experimental mode so the suite can still run on newer local JDKs when necessary.

| Area | Test class | Coverage |
| --- | --- |
| Admin | `AdminAnnouncementServiceTest` | Broadcast fan-out, student/teacher targeting, announcement validation |
| Admin | `AdminApplicationArchiveServiceTest` | Application archive joins, filters, MO private fields |
| Admin | `AdminDashboardServiceTest` | Dashboard counts, workload levels, filters, alerts |
| Admin | `AdminDemandReviewServiceTest` | Demand listing, approve/reject/pending review, teacher notifications |
| Admin | `AdminRecruitmentOutcomeServiceTest` | Recruitment outcome KPIs, date window filtering, CSV export |
| Admin | `AdminReportServiceTest` | Weekly reports, workload reports, application archive reports, backup JSON |
| Admin | `AdminUserServiceTest` | Create/delete/reset users, student profile synchronization, admin safety rules |
| Admin | `AdminWorkloadSettingsServiceTest` | Workload threshold load/save validation |
| Admin | `WorkloadOverloadAnnouncementServiceTest` | Workload calculation, overload transition notices, bulk reminders |
| MO | `MoApplicationStatusTransitionTest` | Application status state machine |
| MO | `MoApplicationStatusFilterTest` | MO status filter parsing and edge cases |
| MO | `MoApplicationServiceTest` | List/detail, status update, batch update, notes, decision feedback, hiring rules |
| MO | `MoApplicationExportServiceTest` | Applicant export, scope filtering, validation |
| MO | `MoDemandServiceTest` | Demand creation, pending approval, duplicate blocking, validation |
| MO | `MoJobServiceTest` | Publish, edit, offline, withdraw, reuse, delete lifecycle constraints |
| MO | `MoJobHistoryServiceTest` | Posted-job history rows and ownership filtering |
| MO | `MoNotificationServiceTest` | Notification list, backfill, announcements, mark-read |
| MO | `ApplicantRecommendationServiceTest` | Applicant recommendation scoring and fallback behavior |
| Student | `JobMatchingServiceTest` | Recommendable job filtering, match ordering, related skill hints |
| Student | `SkillMatchScorerTest` | Exact skill matches, strict-skill misses, related partial credit |
| Student | `StudentNotificationServiceTest` | Student notification listing, announcement mapping, mark-read ownership |
| Student | `StudentServiceTest` | Profile persistence/fallback, attachments, job listing, apply/withdraw, assigned jobs, AI advisor fallback |
| Shared | `AccountServiceTest` | Password change success and validation failures |
| Shared / Access control | `AuthFilterTest` | Unauthenticated and wrong-role access handling |

Testing techniques include equivalence classes, boundary values, state-transition testing, role-access testing, and temporary data isolation.

### Browser E2E / Tomcat Integration

Playwright tests under `e2e/tests` build the WAR, start Tomcat 10.1 through Maven Cargo on port `18080`, deploy the app under `/web`, copy seed JSON into an isolated `web/target/e2e-data` directory, and run browser/API checks against the real JSP application.

Install the Node dependencies and Chromium browser once:

```powershell
npm install
npx playwright install chromium
```

Run the full browser E2E suite:

```powershell
npm run e2e
```

Current coverage: **13 Playwright tests** covering protected-page redirects, unauthenticated and wrong-role API access, invalid login, successful Student/MO/Admin login, role API smoke tests, and key Admin, Student, and MO applicant page rendering.

To run against an already-running Tomcat instance instead of Cargo:

```powershell
$env:E2E_BASE_URL = "http://localhost:8080/web"
npm run e2e:against-running
```

Remaining manual checks:

- File download contents, chart visual correctness, and longer data-changing demo flows are still validated through the manual acceptance checklist.

Manual browser checks before demo/submission are documented in [docs/Acceptance_Test_Checklist.md](docs/Acceptance_Test_Checklist.md).

---

## JavaDocs

Generate JavaDocs from the repository root:

```powershell
mvn -f web/pom.xml javadoc:javadoc
```

Entry point:

```text
web/target/site/apidocs/index.html
```

Code documentation notes are maintained in [docs/JavaDocs.md](docs/JavaDocs.md).

---

## Documentation

| Document | Description |
| --- | --- |
| [Function_Details.md](Function_Details.md) | Full functional specification for Sprint 1, Sprint 2, Sprint 3, and Sprint 4 |
| [docs/Acceptance_Test_Checklist.md](docs/Acceptance_Test_Checklist.md) | Manual acceptance checklist |
| [docs/JavaDocs.md](docs/JavaDocs.md) | JavaDoc generation and code documentation notes |

---

## Project Background

The International School Teaching Assistant Recruitment System supports BUPT International School in replacing a manual, Excel-based TA recruitment process. The project follows Agile/Scrum delivery across multiple sprints and keeps persistence intentionally simple through JSON text files.

Technical stack:

- Backend: Java 21 Servlet + JSP on Jakarta EE / Tomcat 10.1+
- Frontend: JSP, vanilla JavaScript, CSS, and Chart.js for admin charts
- Persistence: JSON files and local upload folders
- Testing: JUnit 5, Mockito, Playwright, and Maven Cargo Tomcat integration
