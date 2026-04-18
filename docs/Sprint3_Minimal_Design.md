# Sprint 3 Minimal Design

## 1. Purpose

This document defines the minimum interface and data design for Sprint 3.

Design level:

- cover all Sprint 3 stories at a practical implementation level
- keep core behavior decision-complete
- leave obvious secondary enhancements extensible instead of over-designing them now

This design must remain compatible with the existing Servlet/JSP architecture, JSON file persistence, and `ApiResponse` response wrapper style.

---

## 2. Runtime Data Strategy

Runtime data is stored directly under the web application data directory:

```text
web/src/main/webapp/WEB-INF/data/
```

Managed JSON files:

- `users.json`
- `students.json`
- `jobs.json`
- `applications.json`
- `notifications.json`
- `hiring_history.json`
- `system_settings.json`

Initialization rules:

1. Existing files under `WEB-INF/data` are used directly.
2. If a required file is missing, it is created automatically in `WEB-INF/data`.
3. List-based files are initialized as `[]`.
4. `system_settings.json` is initialized with the default settings object if missing.

Default settings object:

```json
{
  "workloadThresholdHours": 20,
  "updatedAt": "ISO-8601"
}
```

Compatibility rules:

- old JSON records must remain readable without migration scripts
- missing new fields default to `null`, empty string, or false-like behavior
- new code must not require historical records to be rewritten
- no duplicated runtime copy is introduced; the single source of truth remains `WEB-INF/data`

---

## 3. Shared Calculation Rules

### Weekly Hours

The canonical weekly-hours value used by Admin workload, TA assigned jobs, and related Sprint 3 displays is:

1. use `job.hours` when `hours > 0`
2. otherwise use `round((hourMin + hourMax) / 2)`
3. otherwise fall back to `0`

This rule avoids inconsistent workload calculations between old and new job records.

### Status Vocabulary

The application status set remains:

- `pending`
- `viewed`
- `shortlisted`
- `hired`
- `rejected`

Student-facing labels may map these backend values into UI-friendly wording, but persistence keeps the existing backend vocabulary.

---

## 4. Data Model Additions

### `JobPosting`

New optional fields:

- `department: String`
- `schedule: String`

Usage:

- `department` is entered during MO demand create / edit
- `schedule` is entered during MO publish / edit

Compatibility:

- older records may omit both fields
- UI displays `-` when the value is absent
- Admin department filter only lists non-empty department values found in current job data

### `ApplicationRecord`

New optional fields:

- `moNote: String`
- `decisionUpdatedAt: String`

Usage:

- `moNote` stores organiser-only review notes
- `decisionUpdatedAt` stores the last MO decision timestamp when status or note changes

Compatibility:

- old records may omit both fields
- absent `moNote` is treated as an empty note

### `SystemSettings`

Object structure:

```json
{
  "workloadThresholdHours": 20,
  "updatedAt": "ISO-8601"
}
```

Validation:

- `workloadThresholdHours` must be a positive integer

---

## 5. API Design

All JSON APIs continue to use the existing `ApiResponse` envelope unless the endpoint returns a direct downloadable file.

### 5.1 Admin

#### `GET /api/admin/dashboard?status={all|draft|open|closed|withdrawn}&department={all|value}`

Purpose:

- extend the current dashboard with optional job filtering

Query rules:

- both query parameters are optional
- omitted values behave as `all`
- unsupported values return validation error

Response:

- keep the existing dashboard response shape unchanged
- filtering only narrows the returned job list and dashboard-derived job statistics
- workload and user lists remain available in the same response payload

Data sources:

- `users.json`
- `jobs.json`
- `applications.json`

#### `GET /api/admin/settings/workload-threshold`

Purpose:

- load the current workload threshold

Response data:

```json
{
  "workloadThresholdHours": 20,
  "updatedAt": "2026-04-16T08:00:00Z"
}
```

Data source:

- `system_settings.json`

#### `POST /api/admin/settings/workload-threshold`

Purpose:

- save the global workload threshold

Request body:

```json
{
  "workloadThresholdHours": 20
}
```

Response data:

```json
{
  "workloadThresholdHours": 20,
  "updatedAt": "2026-04-16T08:00:00Z",
  "saved": true
}
```

Validation:

- value must be a positive integer

#### `GET /api/admin/reports/weekly?format={csv|txt}`

Purpose:

- export a weekly recruitment summary report

Behavior:

- returns downloadable attachment output
- no `ApiResponse` envelope is required for the file body

Required report content:

- module / job title
- organiser name
- job status
- positions
- hired count
- unfilled count
- recruitment closed flag

Data sources:

- `jobs.json`
- `applications.json`

### 5.2 Shared

#### `POST /api/account/change-password`

Purpose:

- authenticated self-service password change

Request body:

```json
{
  "oldPassword": "current-value",
  "newPassword": "new-value",
  "confirmPassword": "new-value"
}
```

Response data:

```json
{
  "changed": true
}
```

Validation:

- all fields are required
- `oldPassword` must match the current user record
- `newPassword` and `confirmPassword` must match
- `newPassword` must differ from `oldPassword`

Data source:

- `users.json`

### 5.3 Module Organiser

#### `POST /api/mo/applications/status`

Purpose:

- extend the current application-status endpoint with organiser notes

Request body:

```json
{
  "applicationId": "app_001",
  "status": "shortlisted",
  "note": "Strong Java basics and prior tutoring experience."
}
```

Response data:

- keep the current updated-application response style
- response may include the saved note and updated timestamp if convenient, but it must at least preserve current consumer compatibility

Validation:

- current MO must own the target application's job
- recruitment-closed jobs remain read-only
- `status` must stay within the existing allowed values
- note is optional, but if present must be stored exactly as submitted after trimming outer whitespace

Data source:

- `applications.json`

#### `GET /api/mo/jobs/history`

Purpose:

- return a history summary of all jobs owned by the current MO

Response data:

```json
{
  "items": [
    {
      "jobId": "job_001",
      "moduleCode": "EBU6304",
      "title": "Teaching Assistant",
      "department": "Computer Science",
      "status": "closed",
      "applicantCount": 6,
      "hiredCount": 2,
      "published": true,
      "recruitmentClosed": true,
      "createdAt": "2026-04-01T08:00:00Z",
      "deadline": "2026-04-20"
    }
  ]
}
```

Data sources:

- `jobs.json`
- `applications.json`

#### `GET /api/mo/applications/export?format={csv|json}&jobId={optional}&status={optional}`

Purpose:

- export applicant data owned by the current MO

Rules:

- `format` is required and must be `csv` or `json`
- `jobId` is optional
- `status` is optional and filters by current application status

Export columns / fields:

- application ID
- job ID
- job title
- student name
- student number
- programme
- applied time
- status
- skills
- `moNote`

Data sources:

- `applications.json`
- `jobs.json`
- `students.json`

### 5.4 Student

#### `GET /api/student/my-jobs`

Purpose:

- return confirmed assignments for the current student

Response data:

```json
{
  "items": [
    {
      "applicationId": "app_001",
      "jobId": "job_001",
      "moduleCode": "EBU6304",
      "title": "Teaching Assistant",
      "teacherName": "Dr. Smith",
      "weeklyHours": 10,
      "schedule": "Wed 14:00-16:00",
      "location": "offline",
      "deadline": "2026-04-20",
      "recruitmentClosed": true
    }
  ]
}
```

Rules:

- only `hired` applications are returned
- withdrawn, rejected, pending, viewed, and shortlisted records are excluded

Data sources:

- `applications.json`
- `jobs.json`

---

## 6. Input Ownership

Field ownership is fixed as follows:

- `department`
  - entered when MO creates or edits a demand
- `schedule`
  - entered when MO publishes or edits a job
- `moNote`
  - entered by MO during application review
- `workloadThresholdHours`
  - entered by Admin through the settings API

This prevents later ambiguity about where Sprint 3 data should come from.

---

## 7. Non-Goals for This Minimal Design

The following are intentionally left out of the Sprint 3 minimal design:

- database migration or ORM support
- batch applicant editing beyond what can reuse the current status-update flow
- complex scheduling objects beyond a single `schedule` string
- advanced report templating or Excel export
- password complexity policy beyond basic validation
