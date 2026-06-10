# AgileFlow

A production-ready backend foundation for a Jira-like project management platform built with **Spring Boot 4** (Java 21). It implements a modular monolith with clean hexagonal boundaries, covering issues, sprints, workflows, comments, real-time WebSocket events, full-text search, activity auditing, and custom fields.

---

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Module Overview](#module-overview)
- [Database Schema](#database-schema)
- [API Reference](#api-reference)
- [Real-time (WebSocket)](#real-time-websocket)
- [Getting Started](#getting-started)
- [Running with Docker](#running-with-docker)
- [Error Handling](#error-handling)

---

## Features

- **Issue Management** — Full CRUD for issues with types (Epic, Story, Task, Bug, Subtask), priorities, story points, parent-child hierarchy, and issue links (BLOCKS, RELATES_TO, DUPLICATES)
- **Sprint Management** — Create, start, and complete sprints with state machine enforcement (FUTURE → ACTIVE → COMPLETED)
- **Workflow Engine** — Per-project workflow statuses and configurable allowed transitions; invalid transitions are rejected at the API level
- **Comments** — Threaded comments on issues
- **Activity Audit Trail** — Immutable append-only log of every field change on an issue
- **Custom Fields** — Per-project custom field definitions (TEXT, NUMBER, DROPDOWN, DATE) with per-issue values
- **Watchers** — Subscribe to issue updates
- **Notifications** — User notification records for ASSIGNMENT, MENTION, STATUS_CHANGE, WATCHER_UPDATE
- **Full-Text Search** — PostgreSQL GIN-indexed full-text search across issues
- **Real-time Events** — STOMP-over-WebSocket broadcasting of live project events (ISSUE_CREATED, ISSUE_UPDATED, ISSUE_MOVED, COMMENT_ADDED, SPRINT_UPDATED)
- **Cursor Pagination** — All list endpoints use cursor-based pagination for stable, efficient traversal
- **Optimistic Locking** — Concurrent update conflicts detected via a version column on issues
- **OpenAPI/Swagger UI** — Auto-generated interactive API documentation

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.0.6 |
| Web | Spring MVC (REST) |
| Security | Spring Security (stateless) |
| Persistence | Spring Data JPA + Hibernate 7 |
| Database | PostgreSQL 16 |
| Real-time | Spring WebSocket (STOMP) |
| API Docs | springdoc-openapi 3.0.0 (Swagger UI) |
| Build | Maven (Maven Wrapper included) |
| Containerization | Docker + Docker Compose |
| Testing | JUnit 5, Spring Boot Test |
| Utilities | Lombok |

---

## Architecture

The project follows a **Hexagonal (Ports & Adapters) architecture** inside a modular monolith. Each top-level module is isolated into four layers:

```
┌─────────────────────────────────────────────────────────────┐
│                          API Layer                          │
│         Controllers · Request/Response DTOs · HTTP only     │
└────────────────────────┬────────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────────┐
│                    Application Layer                        │
│         Use Cases · Orchestration · Transactions            │
│         Ports (interfaces the domain depends on)            │
└────────────────────────┬────────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────────┐
│                      Domain Layer                           │
│     Entities · Value Objects · Business Rules               │
│     Hierarchy Policy · Transition Validator                 │
└────────────────────────┬────────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────────┐
│                  Infrastructure Layer                       │
│        JPA Repositories · WebSocket · Search                │
└─────────────────────────────────────────────────────────────┘
```

### Design Principles

- **Dependency Rule** — Domain has zero framework dependencies. Application layer depends only on domain + ports. Infrastructure implements ports.
- **Cross-module communication** — Modules communicate through application service interfaces and ports (`ActivityLogPort`, `RealtimeBroadcaster`, `SearchPort`, `WorkflowTransitionValidator`) — never direct entity access across module boundaries.
- **No Open Session in View** — `spring.jpa.open-in-view=false` enforced. All lazy loading resolved within service transactions.
- **Immutable audit log** — `activity_logs` table is append-only. No updates, no deletes.
- **Outbox pattern** — Real-time events are persisted to `realtime_events` table before WebSocket broadcast, enabling replay.

### Package Structure

```
com.swiggy.agileflow
├── activity/
│   ├── api/                  ActivityLogResponse, ProjectActivityController
│   ├── application/          ActivityFeedService, ActivityLogPort, ActivityLogRecorder
│   ├── domain/               ActivityLog, ActivityActionType
│   └── infrastructure/       ActivityLogRepository
├── comment/
│   ├── api/                  AddCommentRequest, CommentResponse, IssueCommentController
│   ├── application/          CommentService
│   ├── domain/               Comment
│   └── infrastructure/       CommentRepository
├── common/
│   ├── config/               SecurityConfig, OpenApiConfig, JpaAuditingConfig
│   ├── domain/               AuditableEntity, BaseEntity
│   ├── error/                GlobalExceptionHandler, ApiError, BusinessRuleException,
│   │                         ConflictException, NotFoundException
│   └── pagination/           CursorPage, Cursors
├── issue/
│   ├── api/                  IssueController, CreateIssueRequest, UpdateIssueRequest,
│   │                         TransitionIssueRequest, IssueResponse
│   ├── application/          IssueService
│   ├── domain/               Issue, IssueLink, IssueType, Priority, IssueHierarchyPolicy
│   └── infrastructure/       IssueRepository, IssueLinkRepository
├── notification/
│   └── domain/               Notification (entity)
├── project/
│   ├── api/                  BoardController, ProjectIssueController,
│   │                         BoardResponse, BoardColumnResponse
│   ├── application/          BoardService
│   ├── domain/               Project, ProjectMember, CustomFieldDefinition,
│   │                         CustomFieldValue, FieldType
│   └── infrastructure/       ProjectRepository, ProjectMemberRepository,
│                             CustomFieldDefinitionRepository, CustomFieldValueRepository
├── realtime/
│   ├── application/          RealtimeBroadcaster, OutboxRealtimeBroadcaster
│   ├── domain/               RealtimeEvent, RealtimeEventType
│   └── infrastructure/       RealtimeEventRepository, WebSocketConfig
├── search/
│   ├── api/                  SearchController
│   ├── application/          SearchService, FullTextSearchService,
│   │                         IssueSearchCriteria, SearchPort
│   └── infrastructure/       IssueSearchRepository
├── sprint/
│   ├── api/                  SprintController, ProjectSprintController,
│   │                         StartSprintRequest, CompleteSprintRequest, SprintResponse
│   ├── application/          SprintService
│   ├── domain/               Sprint, SprintIssue, SprintState
│   └── infrastructure/       SprintRepository, SprintIssueRepository
├── user/
│   ├── domain/               User
│   └── infrastructure/       UserRepository
├── watcher/
│   ├── domain/               WatcherSubscription
│   └── infrastructure/       WatcherRepository
└── workflow/
    ├── application/          DefaultWorkflowTransitionValidator
    ├── domain/               WorkflowStatus, WorkflowTransition,
    │                         WorkflowTransitionValidator, StatusCategory
    └── infrastructure/       WorkflowStatusRepository, WorkflowTransitionRepository
```

---

## Module Overview

### Issue Module
Central module. Supports five issue types with enforced hierarchy:

```
EPIC
└── STORY
    └── TASK / BUG
        └── SUBTASK
```

Issues have a `version` column for optimistic locking — all update requests must include the current version to prevent lost updates.

### Workflow Module
Each project defines its own set of `WorkflowStatus` entries (with a category: TODO, IN_PROGRESS, DONE) and the allowed `WorkflowTransition` pairs between them. The `WorkflowTransitionValidator` port is called by `IssueService` before every status change.

### Sprint Module
Sprint lifecycle is governed by a state machine:

```
FUTURE  ──start()──►  ACTIVE  ──complete()──►  COMPLETED
```

Only one sprint can be ACTIVE per project at a time. Issues are linked to sprints via the `sprint_issues` join table.

### Realtime Module
Uses the **outbox pattern**: before broadcasting, events are written to `realtime_events`. The `OutboxRealtimeBroadcaster` then pushes to WebSocket clients. Missed events can be replayed via the cursor-indexed table.

### Search Module
Delegates to PostgreSQL full-text search via a GIN index on the issues table. `IssueSearchCriteria` supports filtering by project, type, status, assignee, and free-text query.

### Activity Module
Every mutation on an issue (field changes, status transitions, assignments) is recorded as an immutable row in `activity_logs` via the `ActivityLogPort`. The `ProjectActivityController` exposes a feed endpoint.

---

## Database Schema

The schema is managed directly by Hibernate (`ddl-auto` configured via `.env`). The following tables make up the core data model:

### Entity Relationship Overview

```
users ──────────────────────────────────────────────┐
  │                                                  │
  │ (lead_user_id)                                   │
  ▼                                                  │
projects ──── project_members (role: LEAD/MEMBER) ──┘
  │
  ├── workflow_statuses ── workflow_transitions
  │
  ├── sprints ──── sprint_issues ──┐
  │                                │
  └── issues ◄────────────────────┘
        │  ├── parent_issue_id (self-ref hierarchy)
        │  ├── issue_links (BLOCKS/RELATES_TO/DUPLICATES)
        │  ├── comments
        │  ├── activity_logs
        │  ├── custom_field_values ── custom_field_definitions
        │  ├── watchers
        │  └── notifications
        │
        └── realtime_events (outbox for WebSocket broadcast)
```

### Core Tables

| Table | Purpose |
|---|---|
| `users` | User accounts (email, display_name, password_hash) |
| `projects` | Project metadata with unique project key (e.g. `AGF`) |
| `project_members` | Role-based membership (LEAD, MEMBER) |
| `workflow_statuses` | Per-project statuses with category (TODO, IN_PROGRESS, DONE) |
| `workflow_transitions` | Allowed from_status → to_status pairs per project |
| `issues` | Core issue records with type, priority, story points, hierarchy |
| `issue_links` | Non-hierarchical issue relationships |
| `sprints` | Sprint records with state and date range |
| `sprint_issues` | Issue-to-sprint membership |
| `comments` | Issue comments |
| `activity_logs` | Immutable audit trail of all issue changes |
| `custom_field_definitions` | Per-project custom field schema (TEXT/NUMBER/DROPDOWN/DATE) |
| `custom_field_values` | Per-issue custom field values |
| `watchers` | Issue watch subscriptions |
| `notifications` | User notification records |
| `realtime_events` | WebSocket outbox with cursor-replay support |

---

## API Reference

Interactive documentation is available at **http://localhost:8090/swagger-ui.html** once the app is running.

### Endpoints

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/issues` | List issues (cursor-paginated, filter by project/type/status) |
| `POST` | `/api/issues` | Create a new issue |
| `GET` | `/api/issues/{id}` | Get issue by ID |
| `PATCH` | `/api/issues/{id}` | Update issue fields (requires `version` for optimistic locking) |
| `PATCH` | `/api/issues/{id}/transitions` | Transition issue to a new status |
| `GET` | `/api/issues/{id}/comments` | List comments on an issue |
| `POST` | `/api/issues/{id}/comments` | Add a comment to an issue |
| `GET` | `/api/projects/{id}/issues` | List issues scoped to a project |
| `GET` | `/api/projects/{id}/board` | Get kanban board view (issues grouped by status) |
| `GET` | `/api/projects/{id}/sprints` | List sprints for a project |
| `POST` | `/api/projects/{id}/sprints` | Create a sprint |
| `POST` | `/api/sprints/{id}/start` | Start a sprint (FUTURE → ACTIVE) |
| `POST` | `/api/sprints/{id}/complete` | Complete a sprint (ACTIVE → COMPLETED) |
| `GET` | `/api/projects/{id}/activity` | Get project activity feed |
| `GET` | `/api/search` | Full-text search across issues |

### Example Requests

**Create an issue:**
```bash
curl -X POST http://localhost:8090/api/issues \
  -H 'Content-Type: application/json' \
  -d '{
    "projectId": 1,
    "type": "TASK",
    "title": "Wire up CI pipeline",
    "reporterId": 1,
    "assigneeId": 2,
    "priority": "HIGH",
    "storyPoints": 3
  }'
```

**Update an issue (version required for optimistic locking):**
```bash
curl -X PATCH http://localhost:8090/api/issues/1 \
  -H 'Content-Type: application/json' \
  -d '{
    "version": 0,
    "title": "Wire up CI/CD pipeline",
    "assigneeId": 3
  }'
```

**Transition issue status:**
```bash
curl -X PATCH http://localhost:8090/api/issues/1/transitions \
  -H 'Content-Type: application/json' \
  -d '{"version": 1, "statusId": 2}'
```

**List issues with cursor pagination:**
```bash
# First page
curl 'http://localhost:8090/api/issues?projectId=1&limit=20'

# Next page (use cursor from previous response)
curl 'http://localhost:8090/api/issues?projectId=1&limit=20&cursor=<nextCursor>'
```

**Full-text search:**
```bash
curl 'http://localhost:8090/api/search?q=platform+foundation&projectId=1'
```

**Start a sprint:**
```bash
curl -X POST http://localhost:8090/api/sprints/1/start \
  -H 'Content-Type: application/json' \
  -d '{"startDate": "2026-06-01", "endDate": "2026-06-14"}'
```

**Get kanban board:**
```bash
curl http://localhost:8090/api/projects/1/board
```

---

## Real-time (WebSocket)

The server uses **STOMP over WebSocket** for live project event broadcasting.

**Connection endpoint:** `ws://localhost:8090/ws`

**Subscribe to a project's event stream:**
```
/topic/projects/{projectId}
```

**Event types broadcasted:**

| Event | Trigger |
|---|---|
| `ISSUE_CREATED` | New issue added to project |
| `ISSUE_UPDATED` | Issue fields changed |
| `ISSUE_MOVED` | Issue moved between statuses or sprints |
| `COMMENT_ADDED` | Comment posted on an issue |
| `SPRINT_UPDATED` | Sprint started or completed |

**JavaScript client example:**
```javascript
const client = new Client({
  brokerURL: 'ws://localhost:8090/ws',
  onConnect: () => {
    client.subscribe('/topic/projects/1', (message) => {
      const event = JSON.parse(message.body);
      console.log(event.type, event.payload);
    });
  }
});
client.activate();
```

Events are persisted to the `realtime_events` table before broadcast (outbox pattern), enabling replay of missed events after reconnection.

---

## Environment Configuration

All configuration is managed through the `.env` file in the project root. `application.properties` reads exclusively from these variables — there are no hardcoded defaults in the codebase.


## Getting Started

### Prerequisites

- Java 21+
- Maven 3.9+ (or use the included `./mvnw` wrapper)
- PostgreSQL 16 (or Docker to run it as a container)


### Verify the application is running by accessing the following URLs:

| URL | Description |
|---|---|
| http://localhost:8090/swagger-ui.html | Interactive API documentation |
| http://localhost:8090/v3/api-docs | OpenAPI JSON spec |
| http://localhost:8090/api/issues?projectId=1 | List issues |

---

## Running with Docker

Run the full stack (PostgreSQL + backend) in Docker:

```bash
# Build the JAR and Docker image
./build.sh

# Start all services
docker-compose up -d

# Check service health
docker-compose ps

# View logs
docker-compose logs -f agileflow-backend
docker-compose logs -f postgres

# Stop services (data is preserved in the postgres_data volume)
docker-compose down

# Stop and remove all data
docker-compose down -v
```


## Error Handling

All errors return a consistent JSON body:

```json
{
  "timestamp": "2026-06-10T08:00:00Z",
  "status": 422,
  "error": "Unprocessable Entity",
  "message": "Transition from 'In Review' to 'To Do' is not allowed.",
  "path": "/api/issues/3/transitions",
  "details": []
}
```

| Status | When |
|---|---|
| `400` | Malformed request body or validation failure |
| `404` | Project, issue, sprint, or user not found |
| `409` | Optimistic lock conflict or unique constraint violation |
| `422` | Business rule violation (invalid transition, illegal issue hierarchy) |
| `500` | Unexpected server error |
