# EBU6304_TA_System_Group59

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
This project uses a standard Maven Servlet/JSP web module under `web/`.

Project structure:

```text
web/
|- pom.xml
`- src/
   `- main/
      |- java/
      `- webapp/
```

Build the project:

```powershell
mvn -f web/pom.xml clean package
```

The WAR file will be generated at:

```text
web/target/web.war
```

Deploy `web/target/web.war` to Tomcat 10.1+ and open:

```text
http://localhost:8080/web/
```

## 1. Project Introduction 
### Project Overview
The **International School Teaching Assistant Recruitment System** is developed for BUPT International School to streamline their current manual, Excel-based TA recruitment process\. This project follows **Agile methodologies**, delivering functional software increments through multiple sprints. 

### Technical Stack
- **Backend:** Lightweight Java Servlet and JSP.
- **Data Persistence:** Plain text and JSON file storage (No database allowed).
- **Methodology:** Scrum/Agile development with continuous feedback.

### Sprint 1 Goal
To establish the system's foundational architecture and implement core functions including user authentication, TA profile management, and basic job posting for Module Organisers.

---

## 2. Sprint 1 Member Task Allocation
### **Group A: Core Architecture & Authentication**
**Members:** Sihan Chen & Tianxiao Ma

* **System Architecture Setup:**
    * Design and implement the project directory structure following the MVC pattern for a Java Servlet application.
    * Configure the deployment environment (e.g., Tomcat server setup).
* **JSON Data Utility:**
    * Develop a centralized `JSONUtility` class to handle all file I/O operations for `users.json`, `students.json`, and `jobs.json`.
* **Authentication Functionality:**
    * Implement `LoginServlet` and `LogoutServlet` to verify user credentials against the JSON data.
    * Create the **Login Page (JSP)** with role-based redirection logic (TA, MO, or Admin).

### **Group B: TA (Applicant) Module**
**Members:** Tianzi Xiong & Fangyu Chu

* **TA Profile Management:**
    * Design the **Applicant Profile Page** where students can enter and edit their basic information (Name, Student ID, Major, etc.).
    * Implement the backend logic to save and update student profiles in `students.json`.
* **Job Browsing (Basic):**
    * Develop the **Available Jobs Page** for TAs to view a list of open recruitment positions.
    * Implement the data retrieval logic to read open job vacancies from `jobs.json`.

### **Group C: MO (Module Organiser) Module**
**Members:** Wanhe Ji & Huishun Hu

* **Job Posting Functionality:**
    * Design the **Job Creation Form** for MOs to input module details, required skills, workload, and deadlines.
    * Implement the backend logic to validate inputs and append new job records to `jobs.json`.
* **MO Job Dashboard (Basic):**
    * Create a **Job Management Page** specifically for MOs to view and track the jobs they have personally posted.
    * Develop basic job status toggling (e.g., setting a job to "Open" or "Paused").

---

### **Summary Table for Sprint 1**

| Category | Tasks | Assignees | Priority |
| :--- | :--- | :--- | :--- |
| **Core** | Architecture, JSON Utility, Login/Logout | Sihan Chen, Tianxiao Ma | Must Have |
| **TA** | Profile Setup, Job List Viewing | Tianzi Xiong, Fangyu Chu | Must Have |
| **MO** | Job Posting Form, My Jobs Dashboard | Wanhe Ji, Huishun Hu | Must Have |

