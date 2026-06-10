-- =============================================================================
-- AgileFlow Database Schema  (REFERENCE ONLY)
-- This file is superseded by Flyway migration V1__initial_schema.sql.
-- The application manages the schema automatically via Flyway on startup.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- users
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id            BIGSERIAL       PRIMARY KEY,
    username      VARCHAR(255)    NOT NULL UNIQUE,
    email         VARCHAR(255)    NOT NULL UNIQUE,
    display_name  VARCHAR(255)    NOT NULL,
    password_hash VARCHAR(255),
    active        BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

-- -----------------------------------------------------------------------------
-- projects
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS projects (
    id           BIGSERIAL    PRIMARY KEY,
    project_key  VARCHAR(255) NOT NULL UNIQUE,
    name         VARCHAR(255) NOT NULL,
    description  TEXT,
    lead_user_id BIGINT       REFERENCES users(id) ON DELETE SET NULL,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- -----------------------------------------------------------------------------
-- project_members
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS project_members (
    id         BIGSERIAL    PRIMARY KEY,
    project_id BIGINT       NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    user_id    BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role       VARCHAR(50)  NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    UNIQUE (project_id, user_id)
);

-- -----------------------------------------------------------------------------
-- workflow_statuses
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS workflow_statuses (
    id          BIGSERIAL    PRIMARY KEY,
    project_id  BIGINT       NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    name        VARCHAR(255) NOT NULL,
    category    VARCHAR(50)  NOT NULL,   -- TODO | IN_PROGRESS | DONE
    order_index INT          NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    UNIQUE (project_id, name)
);

-- -----------------------------------------------------------------------------
-- workflow_transitions
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS workflow_transitions (
    id             BIGSERIAL    PRIMARY KEY,
    project_id     BIGINT       NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    from_status_id BIGINT       NOT NULL REFERENCES workflow_statuses(id) ON DELETE CASCADE,
    to_status_id   BIGINT       NOT NULL REFERENCES workflow_statuses(id) ON DELETE CASCADE,
    name           VARCHAR(255) NOT NULL,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    UNIQUE (project_id, from_status_id, to_status_id)
);

-- -----------------------------------------------------------------------------
-- sprints
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sprints (
    id           BIGSERIAL    PRIMARY KEY,
    project_id   BIGINT       NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    name         VARCHAR(255) NOT NULL,
    goal         TEXT,
    state        VARCHAR(50)  NOT NULL DEFAULT 'FUTURE',  -- FUTURE | ACTIVE | COMPLETED
    start_date   DATE,
    end_date     DATE,
    completed_at TIMESTAMPTZ,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- -----------------------------------------------------------------------------
-- issues
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS issues (
    id              BIGSERIAL    PRIMARY KEY,
    project_id      BIGINT       NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    issue_key       VARCHAR(50)  NOT NULL,
    type            VARCHAR(50)  NOT NULL,   -- EPIC | STORY | TASK | BUG | SUBTASK
    title           VARCHAR(255) NOT NULL,
    description     TEXT,
    status_id       BIGINT       NOT NULL REFERENCES workflow_statuses(id),
    assignee_id     BIGINT       REFERENCES users(id) ON DELETE SET NULL,
    reporter_id     BIGINT       NOT NULL REFERENCES users(id),
    priority        VARCHAR(50)  NOT NULL DEFAULT 'MEDIUM',  -- LOW | MEDIUM | HIGH | CRITICAL
    story_points    INT,
    parent_issue_id BIGINT       REFERENCES issues(id) ON DELETE SET NULL,
    version         BIGINT       NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    UNIQUE (project_id, issue_key)
);

CREATE INDEX IF NOT EXISTS idx_issues_project_id  ON issues(project_id);
CREATE INDEX IF NOT EXISTS idx_issues_status_id   ON issues(status_id);
CREATE INDEX IF NOT EXISTS idx_issues_assignee_id ON issues(assignee_id);
CREATE INDEX IF NOT EXISTS idx_issues_fts
    ON issues USING GIN (to_tsvector('english', title || ' ' || COALESCE(description, '')));

-- -----------------------------------------------------------------------------
-- issue_links
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS issue_links (
    id              BIGSERIAL   PRIMARY KEY,
    source_issue_id BIGINT      NOT NULL REFERENCES issues(id) ON DELETE CASCADE,
    target_issue_id BIGINT      NOT NULL REFERENCES issues(id) ON DELETE CASCADE,
    link_type       VARCHAR(50) NOT NULL,   -- BLOCKS | RELATES_TO | DUPLICATES
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (source_issue_id, target_issue_id, link_type)
);

-- -----------------------------------------------------------------------------
-- sprint_issues
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sprint_issues (
    id         BIGSERIAL   PRIMARY KEY,
    sprint_id  BIGINT      NOT NULL REFERENCES sprints(id) ON DELETE CASCADE,
    issue_id   BIGINT      NOT NULL REFERENCES issues(id) ON DELETE CASCADE,
    added_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (sprint_id, issue_id)
);

-- -----------------------------------------------------------------------------
-- comments
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS comments (
    id         BIGSERIAL   PRIMARY KEY,
    issue_id   BIGINT      NOT NULL REFERENCES issues(id) ON DELETE CASCADE,
    author_id  BIGINT      NOT NULL REFERENCES users(id),
    body       TEXT        NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_comments_issue_id ON comments(issue_id);

-- -----------------------------------------------------------------------------
-- activity_logs  (append-only, no updates/deletes)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS activity_logs (
    id          BIGSERIAL   PRIMARY KEY,
    project_id  BIGINT      NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    issue_id    BIGINT      REFERENCES issues(id) ON DELETE SET NULL,
    actor_id    BIGINT      NOT NULL REFERENCES users(id),
    action_type VARCHAR(50) NOT NULL,
    field_name  VARCHAR(255),
    old_value   TEXT,
    new_value   TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_activity_logs_issue_id              ON activity_logs(issue_id);
CREATE INDEX IF NOT EXISTS idx_activity_logs_project_id_created_at ON activity_logs(project_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_activity_logs_actor_id              ON activity_logs(actor_id);

-- -----------------------------------------------------------------------------
-- custom_field_definitions
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS custom_field_definitions (
    id         BIGSERIAL    PRIMARY KEY,
    project_id BIGINT       NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    name       VARCHAR(255) NOT NULL,
    field_type VARCHAR(50)  NOT NULL,   -- TEXT | NUMBER | DROPDOWN | DATE
    options    TEXT,                    -- JSON array of options for DROPDOWN
    required   BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    UNIQUE (project_id, name)
);

-- -----------------------------------------------------------------------------
-- custom_field_values
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS custom_field_values (
    id                  BIGSERIAL      PRIMARY KEY,
    field_definition_id BIGINT         NOT NULL REFERENCES custom_field_definitions(id) ON DELETE CASCADE,
    issue_id            BIGINT         NOT NULL REFERENCES issues(id) ON DELETE CASCADE,
    value_text          TEXT,
    value_number        NUMERIC(19, 4),
    value_date          DATE,
    created_at          TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    UNIQUE (field_definition_id, issue_id)
);

-- -----------------------------------------------------------------------------
-- watchers
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS watchers (
    id         BIGSERIAL   PRIMARY KEY,
    issue_id   BIGINT      NOT NULL REFERENCES issues(id) ON DELETE CASCADE,
    user_id    BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (issue_id, user_id)
);

-- -----------------------------------------------------------------------------
-- notifications
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS notifications (
    id           BIGSERIAL   PRIMARY KEY,
    recipient_id BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type         VARCHAR(50) NOT NULL,   -- ASSIGNMENT | MENTION | STATUS_CHANGE | WATCHER_UPDATE
    issue_id     BIGINT      REFERENCES issues(id) ON DELETE SET NULL,
    payload      TEXT,
    read_at      TIMESTAMPTZ,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_notifications_recipient_id ON notifications(recipient_id);

-- -----------------------------------------------------------------------------
-- realtime_events  (WebSocket outbox)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS realtime_events (
    id         BIGSERIAL   PRIMARY KEY,
    project_id BIGINT      NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    event_type VARCHAR(50) NOT NULL,   -- ISSUE_CREATED | ISSUE_UPDATED | ISSUE_MOVED | COMMENT_ADDED | SPRINT_UPDATED
    payload    TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_realtime_events_project_id_id ON realtime_events(project_id, id);
