# Issue Management System (IMS) - Subsystems & Modules (Logical Microservices)

The Issue Management System (IMS) is architected as a **Modular Monolith**. While it runs as a unified Spring Boot application for ease of deployment, its backend is strictly partitioned into distinct logical subsystems (modules). Each module has its own domain logic, data models, repositories, and services, acting as a "microservice" in terms of separation of concerns.

Here is an architectural map of how these modules interact:

```mermaid
graph TD
    Client["React Frontend (Client)"] -->|Authentication / Login| Auth["Security & Auth Module (auth)"]
    Client -->|Create / Update Tickets| Ticket["Ticket Core Module (ticket)"]
    Client -->|View Dashboard / Export Reports| Report["Search & Reporting Module (reporting)"]
    
    Ticket -->|Auto-Route Team| Routing["Routing Engine (ticket.service.RoutingService)"]
    Ticket -->|Log Activities (Immutable)| Audit["Audit Trail Module (audit)"]
    Ticket -->|Trigger Alert Events (Async)| Notification["Notification Module (notification)"]
    
    Notification -->|Send Emails| SMTP["Gmail SMTP Server"]
```

---

## 1. Security & Authentication Module (`auth`)
**Purpose:** Serves as the gatekeeper for the entire application, handling user identity, token generation, access control, and registration flow.

*   **Key Responsibilities:**
    *   **User Registration & Lifecycle:** Creates users, hashes passwords using BCrypt, and handles verification.
    *   **Stateless Authentication:** Generates and validates JWTs (JSON Web Tokens) with a 1-hour expiration.
    *   **OTP Engine:** Generates secure, 6-digit One-Time Passwords (OTPs) stored with a 10-minute expiry for email verification and password resets.
    *   **Authorization:** Configures Spring Security and filters (e.g., `JwtAuthenticationFilter`) to enforce Role-Based Access Control (RBAC).
*   **Key Classes:**
    *   `SecurityConfig.java`: The core security configurations (disables CSRF, sets stateless sessions, defines public/private endpoints).
    *   `JwtService.java`: Responsible for encoding, decoding, signing, and validating JWT tokens.
    *   `JwtAuthenticationFilter.java`: Bouncer filter intercepting every incoming HTTP request to authenticate valid JWT requests.
    *   `OtpService.java`: Generates, stores, and validates OTP tokens.

---

## 2. Ticket Core Module (`ticket`)
**Purpose:** The central domain engine of the system. It governs the lifecycle, status, categories, priorities, and assignments of issues, along with collaborative features.

*   **Key Responsibilities:**
    *   **Ticket Lifecycle:** Manages status transitions: `OPEN` $\rightarrow$ `PENDING_ASSIGNMENT` $\rightarrow$ `ASSIGNED` $\rightarrow$ `IN_PROGRESS` $\rightarrow$ `RESOLVED` $\rightarrow$ `CLOSED`.
    *   **Collaboration System:**
        *   *Comments:* Public chat thread allowing communication between customers (Employees) and support staff. Supports file attachment metadata.
        *   *Remarks:* Staff-only, internal logs (e.g. routing reasons, manual assignments, resolution summaries) hidden from customers.
    *   **Team Assignment:** Allows manual support engineer assignment by Team Leads or Support Engineers.
*   **Key Classes:**
    *   `Ticket.java`: The core JPA Entity tracking titles, descriptions, categories, priorities, assignments, and timestamps.
    *   `TicketService.java`: Contains core business methods like `createTicket()`, `assignTicket()`, and `updateStatus()`.
    *   `CommentService.java` & `RemarksService.java`: Manages public comments and internal remarks, enforcing strict visibility rules.

---

## 3. Routing Engine (`ticket.service.RoutingService`)
**Purpose:** An intelligent utility embedded inside the Ticket Module that automates ticket categorization and department routing to minimize manual support overhead.

*   **Key Responsibilities:**
    *   **Automated Rules Matching:** Scans newly created tickets' titles and descriptions against active keyword patterns.
    *   **Confidence Scoring:** Automatically assigns a confidence score to each rule match.
    *   **Auto-routing:** If a match meets or exceeds the confidence threshold (e.g., $\ge 70\%$), it routes the ticket to the specified target team (e.g. `DEVELOPMENT`, `QA`, `BILLING`).
*   **Key Classes:**
    *   `RoutingService.java`: Evaluates rules and runs the routing logic.
    *   `RoutingRule.java`: Database entity representing rule patterns (e.g., *"If category is BUG and title contains 'crash', route to DEVELOPMENT with 85% confidence"*).

---

## 4. Audit Trail Module (`audit`)
**Purpose:** Implements security auditing and compliance by keeping an immutable record of actions taken on support tickets.

*   **Key Responsibilities:**
    *   **Immutable Logs:** Captures every lifecycle event (creation, assignment, status change, re-route).
    *   **User Attribution:** Records *who* performed the action, *when* it was done, and *what* specific details changed.
    *   **Append-Only Store:** The database records are never updated or deleted, providing a reliable historical trail.
*   **Key Classes:**
    *   `AuditLog.java`: The JPA entity storing the audit information.
    *   `AuditLogService.java`: Exposes methods to append new audit events.

---

## 5. Notification Module (`notification`)
**Purpose:** Keeps users engaged and updated in real-time by handling internal alerts and outbound email dispatches asynchronously.

*   **Key Responsibilities:**
    *   **Asynchronous Processing:** Utilizes `@Async` background threads so that email delivery delays do not block user API requests.
    *   **Multi-Channel Alerts:** Dispatches in-app alerts and Gmail SMTP emails.
    *   **Event-Driven Triggers:** Notifies support engineers when they are assigned a ticket, and employees when their ticket status changes.
    *   **Mention Detection:** Scans remarks for `@username` patterns and alerts tagged colleagues.
    *   **Robust Retry Logic:** Incorporates exponential backoff to retry failed email deliveries up to 3 times before saving a failure state.
*   **Key Classes:**
    *   `NotificationService.java`: Coordinates in-app alerts, email dispatches, and retry operations.
    *   `EmailNotificationService.java`: Handles SMTP configs and low-level template rendering for OTPs.

---

## 6. Search & Reporting Module (`reporting`)
**Purpose:** Provides analytics, data visualization, and reporting capabilities for management, along with advanced filtering for dashboard interfaces.

*   **Key Responsibilities:**
    *   **Dynamic Search:** Compiles complex queries dynamically based on multi-parameter filters (keywords, categories, statuses, dates, creators).
    *   **Visibility Enforcer:** Restricts search results dynamically based on roles (Employees only see their own tickets, Support Engineers see assigned tickets, Team Leads/Managers see everything).
    *   **Metrics & Dashboards:** Aggregates statistics for charts and performance tracking.
    *   **Document Exports:** Generates clean **PDF** reports or **Excel** spreadsheets of ticket lists for external audit and documentation.
*   **Key Classes:**
    *   `TicketSearchService.java`: Coordinates search execution.
    *   `TicketSpecificationBuilder.java`: Builds dynamic JPA criteria predicates.
    *   `DashboardService.java`: Calculates status metrics and KPI percentages.

---

## 7. Common Subsystem (`common`)
**Purpose:** Contains global utilities, cross-cutting configurations, and startup procedures shared by all other modules.

*   **Key Responsibilities:**
    *   **Database Migrations:** Modifies PostgreSQL checks on startup and migrates legacy user roles.
    *   **Global Exception Handling:** Catches and standardizes API error responses.
    *   **Routing Support:** Handles single-page application (SPA) routing redirection.
*   **Key Classes:**
    *   `DatabaseFixer.java`: Drops old database check constraints and normalizes legacy roles upon startup.
    *   `CorsConfig.java`: Enables Cross-Origin Resource Sharing.
    *   `ForwardController.java`: Reroutes deep frontend URLs (e.g. `/tickets/new`) back to `index.html` to avoid 404 errors in React router.
