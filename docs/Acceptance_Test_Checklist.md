# Acceptance Test Checklist

This checklist is prepared for Sprint 3 hand test, regression check, and final demo rehearsal.

Status fields to fill during execution:

- Pass/Fail
- Evidence Note

Recommended evidence:

- screenshot
- short terminal note
- exported file name
- observed JSON file path

---

## 1. Environment and Runtime Data

| Test ID | Role | Preconditions | Steps | Expected Result | Pass/Fail | Evidence Note |
| --- | --- | --- | --- | --- | --- | --- |
| ENV-01 | Tester | Application is stopped. | 1. Confirm the presence of `users.json`, `students.json`, `jobs.json`, `applications.json`, `notifications.json`, `hiring_history.json`, and `system_settings.json` under `web/src/main/webapp/WEB-INF/data`.<br>2. Start Tomcat and open the system.<br>3. Log in with the demo accounts. | The system starts successfully and uses the JSON files under `WEB-INF/data`. |  |  |
| ENV-02 | Tester | Application is running. | 1. Perform a data-changing action, for example student applies for a job.<br>2. Open the corresponding JSON file under `web/src/main/webapp/WEB-INF/data`.<br>3. Verify the new record exists. | Changes made through the UI are written directly to the JSON files under `WEB-INF/data`. |  |  |
| ENV-03 | Tester | Application is running and one JSON file has already been updated. | 1. Restart Tomcat.<br>2. Reopen the same page.<br>3. Compare the page content with the JSON file under `WEB-INF/data`. | The modified data remains available after restart because `WEB-INF/data` is the single source of truth. |  |  |
| ENV-04 | Tester | Application is stopped. | 1. Back up one JSON file under `WEB-INF/data` if needed.<br>2. Delete or rename the file.<br>3. Start the application again. | Missing files are recreated automatically with the expected default structure. |  |  |

---

## 2. Authentication and Access Guard

| Test ID | Role | Preconditions | Steps | Expected Result | Pass/Fail | Evidence Note |
| --- | --- | --- | --- | --- | --- | --- |
| AUTH-01 | All Users | JSON files under `WEB-INF/data` contain the demo accounts. | 1. Open the login page.<br>2. Log in as Student using `student@demo.com / demo123`.<br>3. Repeat for Teacher and Admin accounts. | Each account is redirected to the correct role page. |  |  |
| AUTH-02 | All Users | Login page is open. | 1. Submit an incorrect password.<br>2. Submit an empty login form. | The system shows an error and does not create a session. |  |  |
| AUTH-03 | All Users | User is logged in. | 1. Click Logout.<br>2. Try reopening a protected page in the same browser tab. | Session is invalidated and the protected page is no longer accessible directly. |  |  |
| AUTH-04 | Tester | No valid session for the target role. | 1. Request a protected URL such as `/pages/admin.jsp` or `/api/admin/dashboard` without a matching session. | Access is rejected or redirected according to the current auth filter behavior. |  |  |

---

## 3. Shared Password Change

| Test ID | Role | Preconditions | Steps | Expected Result | Pass/Fail | Evidence Note |
| --- | --- | --- | --- | --- | --- | --- |
| PWD-01 | Student / MO / Admin | Role is logged in and the change-password feature is available. | 1. Submit the correct old password and a valid new password.<br>2. Log out.<br>3. Log in again using the new password. | Password is changed successfully and the new password works. |  |  |
| PWD-02 | Student / MO / Admin | Role is logged in. | 1. Submit an incorrect old password.<br>2. Keep the new password fields valid. | The request is rejected and the stored password is unchanged. |  |  |
| PWD-03 | Student / MO / Admin | Role is logged in. | 1. Submit mismatched `newPassword` and `confirmPassword`. | Validation error is shown and nothing is saved. |  |  |

---

## 4. Student Workflow

| Test ID | Role | Preconditions | Steps | Expected Result | Pass/Fail | Evidence Note |
| --- | --- | --- | --- | --- | --- | --- |
| STU-01 | Student | Student is logged in. | 1. Open the jobs panel.<br>2. Search and filter available jobs if the UI provides those controls.<br>3. Open one job detail. | Available jobs load successfully and details are visible. |  |  |
| STU-02 | Student | Student profile contains at least one uploaded attachment. | 1. Apply for an open job.<br>2. Select at least one attachment.<br>3. Submit the application. | A new application record is created and appears in the student's application list. |  |  |
| STU-03 | Student | Student has one active non-hired application. | 1. Withdraw that application from the application list. | The application disappears from the active list or is shown as no longer active according to the current UI behavior. |  |  |
| STU-04 | Student | Student has at least one hired application. | 1. Open the assigned jobs / my jobs / schedule view.<br>2. Inspect the listed assignment. | Only hired jobs are displayed, with module, organiser, weekly hours, schedule, location, and deadline. |  |  |
| STU-05 | Student | Student has no hired applications. | 1. Open the assigned jobs / schedule view. | The system shows a clear empty state instead of a broken table or blank page. |  |  |

---

## 5. Module Organiser Workflow

| Test ID | Role | Preconditions | Steps | Expected Result | Pass/Fail | Evidence Note |
| --- | --- | --- | --- | --- | --- | --- |
| MO-01 | Module Organiser | MO is logged in and owns at least one job with active applicants. | 1. Open the applicant list.<br>2. Mark one application as shortlisted.<br>3. Save a review note on the same record. | Status and note are saved and remain visible after refresh. |  |  |
| MO-02 | Module Organiser | Same as MO-01. | 1. Filter applications by status.<br>2. Switch between at least two status values. | The list refreshes correctly and only matching records remain visible. |  |  |
| MO-03 | Module Organiser | MO owns current and/or historical jobs. | 1. Open the posted-job history view.<br>2. Inspect at least one record. | Each history item shows job metadata including status, applicant count, hired count, created time, and deadline. |  |  |
| MO-04 | Module Organiser | MO owns applicants and the export feature is available. | 1. Export applicants as CSV.<br>2. Open the exported file. | The file downloads successfully and contains the required columns. |  |  |
| MO-05 | Module Organiser | Same as MO-04. | 1. Export applicants as JSON.<br>2. Open the exported file. | The JSON file downloads successfully and contains the expected applicant fields. |  |  |
| MO-06 | Module Organiser | MO owns one job with shortlisted applicants. | 1. Confirm final hiring.<br>2. Refresh the applicant page.<br>3. Try changing a status again. | Recruitment becomes closed and the page is effectively read-only for further review changes on that job. |  |  |

---

## 6. Admin Workflow

| Test ID | Role | Preconditions | Steps | Expected Result | Pass/Fail | Evidence Note |
| --- | --- | --- | --- | --- | --- | --- |
| ADM-01 | Admin | Admin is logged in. | 1. Open workload settings.<br>2. Save threshold `20`.<br>3. Reload the page.<br>4. Inspect `web/src/main/webapp/WEB-INF/data/system_settings.json`. | Saved threshold is loaded again and persisted directly in `system_settings.json`. |  |  |
| ADM-02 | Admin | Runtime data contains hired records with different weekly-hour totals. | 1. Open the workload page.<br>2. Compare records against the threshold. | TAs over the threshold are marked with a visible warning label. |  |  |
| ADM-03 | Admin | Admin dashboard contains jobs from more than one department or status. | 1. Filter the dashboard by department.<br>2. Filter again by status. | Job results are narrowed correctly without breaking dashboard rendering. |  |  |
| ADM-04 | Admin | Weekly report export feature is available. | 1. Export the weekly report as CSV or TXT.<br>2. Open the downloaded file. | The file includes job title, organiser, status, hired count, and unfilled count. |  |  |
| ADM-05 | Admin | At least one recruitment-closed job exists. | 1. Reopen the closed job from Admin.<br>2. Refresh Admin and MO pages. | Job is reopened successfully and the state change is visible in both places. |  |  |

---

## 7. Regression Checks

| Test ID | Role | Preconditions | Steps | Expected Result | Pass/Fail | Evidence Note |
| --- | --- | --- | --- | --- | --- | --- |
| REG-01 | Student | Student is logged in. | 1. Open and edit profile data.<br>2. Save changes.<br>3. Refresh the page. | Sprint 1/2 profile management still works after Sprint 3 changes. |  |  |
| REG-02 | Student | Student is logged in. | 1. Upload one attachment.<br>2. Delete that attachment.<br>3. Refresh the page. | Attachment upload and delete still work. |  |  |
| REG-03 | MO | MO is logged in. | 1. Create a demand.<br>2. Publish a job if allowed.<br>3. View applicants. | Sprint 1/2 MO demand, publish, and review chain still works. |  |  |
| REG-04 | Admin | Admin is logged in. | 1. Open dashboard.<br>2. Create a user.<br>3. Reset password or delete a user if safe. | Sprint 2 admin management functions still work after Sprint 3 documentation/runtime updates. |  |  |
| REG-05 | Tester | JSON data has been modified during any prior test. | 1. Restart Tomcat.<br>2. Re-check the modified records.<br>3. Confirm they still match the files under `WEB-INF/data`. | JSON data remains stable across restart and is still loaded from `WEB-INF/data`. |  |  |

---

## 8. Suggested Execution Order

Recommended rehearsal order:

1. `ENV-01` to `ENV-04`
2. `AUTH-01` to `AUTH-04`
3. `PWD-01` to `PWD-03`
4. `STU-01` to `STU-05`
5. `MO-01` to `MO-06`
6. `ADM-01` to `ADM-05`
7. `REG-01` to `REG-05`

This order minimizes confusion between file checks, role switching, and regression evidence collection.
