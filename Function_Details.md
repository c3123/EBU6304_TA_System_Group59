
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
- [cite_start]**Data Logic:** The system must use a JSON parser (e.g., GSON) to read `id`, `password`, and `role` fields from `users.json` [cite: 43-44].
- **Session Management:** `HttpSession` must be used to persist user identity across the web application.

**Assignee:** Sihan Chen/ Tianxiao Ma
**Completion Date:** 

---
