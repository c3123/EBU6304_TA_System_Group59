# EBU6304_TA_System_Group59

| GitHub Username | QMID |
| --- | --- |
| Chudadi-cfy | 231226613 |
| J-3469 | 231226598 |
| yeahyeah66 | 231226288 |
| qiaolezi2006-rgb | 231226299 |
| MA0204 | 231226587 |
| c3123 | 231226624 |

International School Teaching Assistant Recruitment System — a Java Servlet/JSP web application with JSON file persistence (no database).

---

## Quick Start

### Requirements

- **JDK 11+** (project `pom.xml` targets Java 11; JDK 17 is commonly used locally)
- **Maven 3.8+**
- **Apache Tomcat 10.1+** (Jakarta Servlet 6)

### Project layout

```text
web/
|- pom.xml
`- src/
   |- main/
   |  |- java/          # servlets, services, DTOs, models
   |  `- webapp/        # JSP pages, static assets, WEB-INF/data
   `- test/java/        # JUnit 5 unit tests
scripts/
|- dev-deploy.ps1       # sync exploded WAR to Tomcat (keeps runtime data)
`- generate_seed_data.py # regenerate demo JSON dataset
```

### Build

```powershell
mvn -f web/pom.xml clean package
```

WAR output: `web/target/web.war`

### Deploy (Tomcat)

1. Copy `web/target/web.war` to Tomcat `webapps/`.
2. Start Tomcat and open: `http://localhost:8080/web/`

Optional exploded deploy (preserves existing `WEB-INF/data` and uploads on the server):

```powershell
.\scripts\dev-deploy.ps1 -TomcatWebappsPath "C:\path\to\tomcat\webapps"
```

---

## Demo Accounts

Primary demo logins (password for all: `demo123`):

| Role | Login | Password |
| --- | --- | --- |
| Student | `student@demo.com` | `demo123` |
| Teacher / MO | `teacher@demo.com` | `demo123` |
| Admin | `admin@demo.com` | `demo123` |

The bundled JSON seed dataset also includes **50 students**, **20 teachers**, and **1 admin** (71 users total). Additional teacher/student accounts follow the pattern `teacher02@demo.qmul.ac.uk`, `student02@demo.qmul.ac.uk`, etc., all with password `demo123`.

To regenerate demo jobs/applications/users:

```powershell
python scripts/generate_seed_data.py
```

---

## Running Unit Tests

Tests use an isolated temporary JSON directory via `System.setProperty("ta.data.dir", ...)` and do **not** require Tomcat.

```powershell
mvn -f web/pom.xml test
```

The suite currently contains **99** JUnit 5 tests under `web/src/test/java`.

| Test class | Coverage |
| --- | --- |
| `MoApplicationStatusTransitionTest` | Application status state machine (normal, boundary, error transitions) |
| `MoApplicationStatusFilterTest` | MO list status filter parsing (`pending` includes `viewed`, `__none__`, etc.) |
| `MoApplicationServiceTest` | List/detail, status update (single & batch), evaluation notes, decision feedback |
| `MoApplicationExportServiceTest` | Applicant export CSV/JSON, scope filter, validation |
| `MoJobServiceTest` | MO job publish/offline/withdraw/reuse/edit/delete rules |
| `MoJobHistoryServiceTest` | Posted job history listing |
| `MoDemandServiceTest` | MO demand creation and listing |
| `MoNotificationServiceTest` | Notification list, backfill, announcements, mark-read |
| `StudentServiceTest` | Profile persistence, attachment upload/delete, apply/withdraw, assigned jobs, AI advisor fallback |
| `AccountServiceTest` | Password change success for student/MO/admin and validation failures |
| `AuthFilterTest` | Unauthenticated API access and wrong-role access for admin/MO/student routes |

Testing techniques: equivalence classes, boundary values, state-transition testing, role-access testing.

Manual browser checks before demo/submission are still recommended — see [docs/Acceptance_Test_Checklist.md](docs/Acceptance_Test_Checklist.md).

---

## JavaDocs

```powershell
mvn -f web/pom.xml javadoc:javadoc
```

Entry point: `web/target/site/apidocs/index.html`

Notes: [docs/JavaDocs.md](docs/JavaDocs.md)

---

## Implemented Features (current `main`)

### Shared

- Session login/logout with role-based redirection (`student`, `teacher`, `admin`)
- Self-service password change (`/api/account/change-password`)
- Student self-registration (`/register`)
- JSON persistence through `JsonUtility` (no external database)

### Student (TA)

- Profile management and attachment upload/download/delete
- Browse open jobs, view details, submit and withdraw applications
- Track application status and notifications (including system announcements)
- View assigned/hired jobs with weekly workload summary
- **Hired-job calendar** on the student portal (month view from job schedules)
- **AI advisor** endpoint with local fallback when external AI is unavailable

### Module Organiser (Teacher / MO)

- Submit TA **demands** for admin approval
- Publish, edit, offline, withdraw, reuse, and delete job postings (with lifecycle constraints)
- Dedicated applicant review UI (`mo-applications.jsp`) with workload preview
- Single/batch applicant status updates, evaluation notes, decision feedback
- Final hiring confirmation and recruitment closure
- Applicant export (CSV/JSON), notifications, job history

### Administrator

- **System Overview**: role distribution pie chart; daily job publication and application trend line charts; quick summary KPIs
- **Workload**: threshold configuration, overload/warning/normal levels, side-panel job breakdown, CSV/TXT export, **bulk overload reminder** to affected students
- **Users**: create/delete users, reset passwords
- **Demand Review**: approve/reject/pending with optional rejection reason; **auto system announcement** to the posting teacher on status change
- **Jobs**: filter by status/department/teacher; job-level application drilldown; **job analysis** pie charts (by department; by status: Pending / Reject / Open / Overdue); CSV/TXT export; JSON backup
- **Recruitment Results**: date-range filters, hiring-mix chart, department and vacancy analytics, CSV export
- **Announcements**: broadcast to students, teachers, or all
- **Alerts modal**: workload, vacancy, deadline, and data-quality risks
- Recruitment reopen for closed jobs
- My Account (password change)

---

## Runtime Data Storage

All runtime data lives under `web/src/main/webapp/WEB-INF/data/`:

| File | Purpose |
| --- | --- |
| `users.json` | Login accounts and roles |
| `students.json` | Student profiles, skills, attachments metadata |
| `jobs.json` | Demands and job postings |
| `applications.json` | Application records |
| `notifications.json` | MO/student/admin notifications and announcements |
| `hiring_history.json` | Final hiring events |
| `system_settings.json` | Workload threshold (`workloadThresholdHours`, default 20) |

Uploaded student files are stored under `WEB-INF/uploads/students/` (created at runtime).

Initialization rules:

- Existing files are read/written in place.
- Missing list files are created as `[]`.
- Missing `system_settings.json` is initialized with `workloadThresholdHours: 20`.

To reset demo content, restore or regenerate the JSON files above (and optionally clear `WEB-INF/uploads`).

---

## Release Tags & Documentation

| Tag | Scope |
| --- | --- |
| `v1.0-sprint1` | Sprint 1 baseline |
| `v2.0-sprint2` | Sprint 2 baseline |
| `v3.0-Sprint3` | Sprint 3 delivery tag |

Sprint 4 backlog items (for example MO internal rejection reasons) are integrated on `main` where implemented; there is no separate Sprint 4 git tag.

| Document | Description |
| --- | --- |
| [Function_Details.md](Function_Details.md) | Full functional specification (Sprint 1–4) |
| [docs/Sprint3_Minimal_Design.md](docs/Sprint3_Minimal_Design.md) | Sprint 3 interface/data design notes |
| [docs/Acceptance_Test_Checklist.md](docs/Acceptance_Test_Checklist.md) | Manual acceptance checklist |
| [docs/JavaDocs.md](docs/JavaDocs.md) | JavaDoc generation notes |

---

## 1. Project Introduction

### Project Overview

The **International School Teaching Assistant Recruitment System** supports BUPT International School in replacing a manual, Excel-based TA recruitment process. The project follows Agile/Scrum delivery across multiple sprints.

### Technical Stack

- **Backend:** Java Servlet + JSP (Jakarta EE / Tomcat 10.1+)
- **Persistence:** JSON text files and local upload folders (no database)
- **Frontend:** JSP + vanilla JavaScript + Chart.js (admin charts)
- **Testing:** JUnit 5 + Mockito

### Sprint 1 Goal

Foundational architecture: authentication, TA profile management, basic MO job posting, and job browsing.

### Sprint 2 Progress

Administrator control and end-to-end recruitment operations:

- administrator dashboard and workload monitoring
- administrator demand review and user management
- MO applicant review, hiring decisions, and job lifecycle control
- student profile persistence, attachments, and application management

### Final Delivery Status

`main` integrates Sprint 1–3 tagged deliverables plus Sprint 4 follow-ups (MO enhancements, admin reporting/charts, announcements, AI advisor, student hired-job calendar, automated workload notifications, and expanded demo dataset).

---

## 2. Sprint 1 Member Task Allocation

### Group A: Core Architecture & Authentication
**Members:** Sihan Chen & Tianxiao Ma

- MVC-style Servlet project structure and Tomcat deployment setup
- Centralized `JsonUtility` for JSON file I/O (`users.json`, `students.json`, `jobs.json`, …)
- `LoginServlet` / `LogoutServlet` and role-based login page

### Group B: TA (Applicant) Module
**Members:** Tianzi Xiong & Fangyu Chu

- Applicant profile page backed by `students.json`
- Available jobs listing from `jobs.json`

### Group C: MO (Module Organiser) Module
**Members:** Wanhe Ji & Huishun Hu

- Job/demand creation form and validation
- MO job dashboard for own postings
- Basic job lifecycle control (draft / open / offline — not a separate "paused" state)

### Sprint 1 Summary

| Category | Tasks | Assignees | Priority |
| --- | --- | --- | --- |
| Core | Architecture, JsonUtility, login/logout | Sihan Chen, Tianxiao Ma | Must Have |
| TA | Profile setup, job list viewing | Tianzi Xiong, Fangyu Chu | Must Have |
| MO | Job posting form, my jobs dashboard | Wanhe Ji, Huishun Hu | Must Have |

---

## 3. Sprint 2 Member Task Allocation

### Group A: Administrator Module & Shared Backend Integration
**Members:** Sihan Chen & Tianxiao Ma

- Administrator dashboard, workload monitoring, demand review, recruitment reopen
- Admin user create/delete/reset password
- Shared JSON consistency across roles

### Group B: TA Workflow Extension
**Members:** Tianzi Xiong & Fangyu Chu

- Profile persistence and attachment management
- Apply, withdraw, and track applications

### Group C: MO Workflow Extension
**Members:** Wanhe Ji & Huishun Hu

- Demand lifecycle, publishing, applicant review, hiring confirmation
- MO notifications and hiring history support

### Sprint 2 Summary

| Category | Tasks | Assignees | Priority |
| --- | --- | --- | --- |
| Admin / Shared | Dashboard, workload, demand review, user management | Sihan Chen, Tianxiao Ma | Must Have |
| TA Extension | Profile, attachments, applications | Tianzi Xiong, Fangyu Chu | Must Have |
| MO Extension | Demand lifecycle, applicant review, hiring | Wanhe Ji, Huishun Hu | Must Have |
