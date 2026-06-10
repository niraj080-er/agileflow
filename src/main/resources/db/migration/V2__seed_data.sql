-- =============================================================================
-- V2__seed_data.sql
-- Development seed data – realistic AgileFlow demo dataset
-- All user passwords: "password"  (BCrypt strength-10)
-- =============================================================================

-- -----------------------------------------------------------------------------
-- users
-- id | username    | display_name   | role context
--  1 | alice_dev   | Alice Chen     | Senior developer / project lead
--  2 | bob_dev     | Bob Smith      | Developer
--  3 | carol_qa    | Carol White    | QA engineer
--  4 | dan_pm      | Dan Patel      | Product manager
--  5 | eve_dev     | Eve Johnson    | Developer
-- -----------------------------------------------------------------------------
INSERT INTO users (id, username, email, display_name, password_hash, active)
VALUES
  (1, 'alice_dev', 'alice@agileflow.dev', 'Alice Chen',
   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', true),
  (2, 'bob_dev',   'bob@agileflow.dev',   'Bob Smith',
   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', true),
  (3, 'carol_qa',  'carol@agileflow.dev', 'Carol White',
   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', true),
  (4, 'dan_pm',    'dan@agileflow.dev',   'Dan Patel',
   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', true),
  (5, 'eve_dev',   'eve@agileflow.dev',   'Eve Johnson',
   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', true);

SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));

-- -----------------------------------------------------------------------------
-- projects
-- id=1  AGILE – AgileFlow Platform (led by dan_pm)
-- id=2  SHOP  – E-Commerce Backend  (led by alice_dev)
-- -----------------------------------------------------------------------------
INSERT INTO projects (id, project_key, name, description, lead_user_id)
VALUES
  (1, 'AGILE', 'AgileFlow Platform',
   'Core platform for agile project management — sprints, issues, and workflows.',
   4),
  (2, 'SHOP', 'E-Commerce Backend',
   'REST API backend for the online shop: catalogue, cart, orders, and payments.',
   1);

SELECT setval('projects_id_seq', (SELECT MAX(id) FROM projects));

-- -----------------------------------------------------------------------------
-- project_members
-- AGILE: all five users
-- SHOP:  alice (OWNER), bob (DEVELOPER), carol (DEVELOPER)
-- -----------------------------------------------------------------------------
INSERT INTO project_members (project_id, user_id, role)
VALUES
  (1, 4, 'OWNER'),
  (1, 1, 'DEVELOPER'),
  (1, 2, 'DEVELOPER'),
  (1, 3, 'DEVELOPER'),
  (1, 5, 'DEVELOPER'),
  (2, 1, 'OWNER'),
  (2, 2, 'DEVELOPER'),
  (2, 3, 'DEVELOPER');

-- -----------------------------------------------------------------------------
-- workflow_statuses
-- AGILE (project_id=1): ids 1-4
-- SHOP  (project_id=2): ids 5-8
-- -----------------------------------------------------------------------------
INSERT INTO workflow_statuses (id, project_id, name, category, order_index)
VALUES
  -- AGILE
  (1, 1, 'To Do',       'TODO',        1),
  (2, 1, 'In Progress', 'IN_PROGRESS', 2),
  (3, 1, 'In Review',   'IN_PROGRESS', 3),
  (4, 1, 'Done',        'DONE',        4),
  -- SHOP
  (5, 2, 'Backlog',     'TODO',        1),
  (6, 2, 'In Progress', 'IN_PROGRESS', 2),
  (7, 2, 'Testing',     'IN_PROGRESS', 3),
  (8, 2, 'Done',        'DONE',        4);

SELECT setval('workflow_statuses_id_seq', (SELECT MAX(id) FROM workflow_statuses));

-- -----------------------------------------------------------------------------
-- workflow_transitions
-- AGILE: ids 1-5
-- SHOP:  ids 6-9
-- -----------------------------------------------------------------------------
INSERT INTO workflow_transitions (id, project_id, from_status_id, to_status_id, name)
VALUES
  -- AGILE transitions
  (1, 1, 1, 2, 'Start'),
  (2, 1, 2, 3, 'Submit for Review'),
  (3, 1, 3, 4, 'Approve'),
  (4, 1, 3, 2, 'Request Changes'),
  (5, 1, 2, 1, 'Stop Work'),
  -- SHOP transitions
  (6, 2, 5, 6, 'Start'),
  (7, 2, 6, 7, 'Submit for Testing'),
  (8, 2, 7, 8, 'Pass'),
  (9, 2, 7, 6, 'Fail – Reopen');

SELECT setval('workflow_transitions_id_seq', (SELECT MAX(id) FROM workflow_transitions));

-- -----------------------------------------------------------------------------
-- sprints
-- AGILE Sprint 1 – COMPLETED (past)
-- AGILE Sprint 2 – ACTIVE    (current)
-- AGILE Sprint 3 – FUTURE
-- SHOP  Sprint 1 – ACTIVE    (current)
-- SHOP  Sprint 2 – FUTURE
-- -----------------------------------------------------------------------------
INSERT INTO sprints (id, project_id, name, goal, state, start_date, end_date, completed_at)
VALUES
  (1, 1, 'AGILE Sprint 1',
   'Bootstrap authentication module and core issue CRUD.',
   'COMPLETED', '2026-01-06', '2026-01-17',
   '2026-01-17 17:00:00+00'),
  (2, 1, 'AGILE Sprint 2',
   'Complete issue management APIs and start sprint functionality.',
   'ACTIVE', '2026-01-20', '2026-01-31', NULL),
  (3, 1, 'AGILE Sprint 3',
   'Real-time notifications and search improvements.',
   'FUTURE', NULL, NULL, NULL),
  (4, 2, 'SHOP Sprint 1',
   'Deliver product catalogue APIs (listing + search).',
   'ACTIVE', '2026-01-20', '2026-02-02', NULL),
  (5, 2, 'SHOP Sprint 2',
   'Shopping cart and checkout flow.',
   'FUTURE', NULL, NULL, NULL);

SELECT setval('sprints_id_seq', (SELECT MAX(id) FROM sprints));

-- -----------------------------------------------------------------------------
-- issues
-- AGILE issues use statuses 1-4; SHOP issues use statuses 5-8
-- -----------------------------------------------------------------------------
INSERT INTO issues (id, project_id, issue_key, type, title, description,
                    status_id, assignee_id, reporter_id, priority, story_points,
                    parent_issue_id, version)
VALUES
  -- ── AGILE project ──────────────────────────────────────────────────────────
  (1,  1, 'AGILE-1', 'EPIC',
   'User Authentication Module',
   'Implement complete user authentication: registration, login, JWT issuance, and session management.',
   4, 1, 4, 'HIGH', 13, NULL, 0),

  (2,  1, 'AGILE-2', 'STORY',
   'Login API',
   'POST /auth/login endpoint – validate credentials, issue JWT access + refresh tokens.',
   4, 1, 4, 'HIGH', 5, 1, 0),

  (3,  1, 'AGILE-3', 'STORY',
   'Registration API',
   'POST /auth/register – create user, validate unique email/username, hash password with BCrypt.',
   4, 2, 4, 'HIGH', 5, 1, 0),

  (4,  1, 'AGILE-4', 'TASK',
   'Integrate JWT library and configure token settings',
   'Add jjwt dependency, create JwtService, and externalise secret + expiry to application.properties.',
   4, 1, 4, 'MEDIUM', 2, NULL, 0),

  (5,  1, 'AGILE-5', 'BUG',
   'Session timeout not honoured – token expires after 2 min',
   'Reported by QA: access token expires ~2 minutes after login instead of the configured 30 minutes. Reproducible on all browsers.',
   2, 2, 3, 'HIGH', 3, NULL, 0),

  (6,  1, 'AGILE-6', 'EPIC',
   'Issue Management',
   'Full CRUD for issues: create, read, update, transition status, link issues, and manage sub-tasks.',
   2, NULL, 4, 'MEDIUM', 21, NULL, 0),

  (7,  1, 'AGILE-7', 'STORY',
   'Create Issue API',
   'POST /projects/{projectKey}/issues – validate payload, generate issue key, persist and broadcast.',
   4, 1, 4, 'HIGH', 8, 6, 0),

  (8,  1, 'AGILE-8', 'STORY',
   'Update Issue API',
   'PATCH /issues/{issueKey} – support partial updates to title, description, assignee, priority, and story points.',
   2, 5, 4, 'MEDIUM', 5, 6, 0),

  (9,  1, 'AGILE-9', 'SUBTASK',
   'Add input validation to the update-issue endpoint',
   'Use @Valid + custom constraints; return 400 with field-level error messages.',
   2, 5, 5, 'MEDIUM', 2, 8, 0),

  (10, 1, 'AGILE-10', 'TASK',
   'Write integration tests for Issue API',
   'Cover create, read, update, and transition happy-paths plus key error cases using @SpringBootTest.',
   1, 3, 4, 'MEDIUM', 3, NULL, 0),

  -- ── SHOP project ───────────────────────────────────────────────────────────
  (11, 2, 'SHOP-1', 'EPIC',
   'Product Catalogue',
   'APIs for browsing and searching the product catalogue: listing with filters, pagination, and full-text search.',
   6, NULL, 1, 'HIGH', 34, NULL, 0),

  (12, 2, 'SHOP-2', 'STORY',
   'Product listing API',
   'GET /products – paginated list with category and price-range filters.',
   8, 2, 1, 'HIGH', 8, 11, 0),

  (13, 2, 'SHOP-3', 'STORY',
   'Product full-text search',
   'GET /products/search?q= – PostgreSQL tsvector search across name and description.',
   7, 2, 1, 'HIGH', 8, 11, 0),

  (14, 2, 'SHOP-4', 'BUG',
   'Search endpoint returns duplicate products',
   'When a product matches both name and description tsvector, it appears twice in results. Missing DISTINCT or GROUP BY.',
   6, 2, 3, 'CRITICAL', 2, NULL, 0),

  (15, 2, 'SHOP-5', 'TASK',
   'Add cursor-based pagination to product list',
   'Replace offset/limit with keyset pagination for performance at scale.',
   5, NULL, 1, 'LOW', 3, NULL, 0);

SELECT setval('issues_id_seq', (SELECT MAX(id) FROM issues));

-- -----------------------------------------------------------------------------
-- issue_links
-- -----------------------------------------------------------------------------
INSERT INTO issue_links (source_issue_id, target_issue_id, link_type)
VALUES
  (5,  2,  'RELATES_TO'),   -- timeout bug relates to Login API
  (14, 13, 'BLOCKS');       -- duplicate bug blocks search story

-- -----------------------------------------------------------------------------
-- sprint_issues
-- Sprint 1 (COMPLETED): AGILE-2,3,4,7  – all done, sprint was successful
-- Sprint 2 (ACTIVE):    AGILE-5,8,9,10
-- Sprint 4 (ACTIVE):    SHOP-2,3,4
-- Sprint 5 (FUTURE):    SHOP-5
-- -----------------------------------------------------------------------------
INSERT INTO sprint_issues (sprint_id, issue_id)
VALUES
  -- AGILE Sprint 1
  (1, 2),
  (1, 3),
  (1, 4),
  (1, 7),
  -- AGILE Sprint 2
  (2, 5),
  (2, 8),
  (2, 9),
  (2, 10),
  -- SHOP Sprint 1
  (4, 12),
  (4, 13),
  (4, 14),
  -- SHOP Sprint 2
  (5, 15);

-- -----------------------------------------------------------------------------
-- comments
-- -----------------------------------------------------------------------------
INSERT INTO comments (issue_id, author_id, body)
VALUES
  (2, 1,
   'Implementation complete. JWT integration working with RS256. Both access (30 min) and refresh (7 days) tokens verified in Postman.'),
  (5, 3,
   'Reproduced consistently: token expires ~2 minutes after login regardless of the configured expiry. Steps: login → wait 2 min → call any secured endpoint → 401 Unauthorized.'),
  (5, 2,
   'Root cause found: JwtService reads expiry from a hardcoded constant instead of the @Value property. Fix is a one-liner – raising a PR now.'),
  (8, 5,
   'Started implementation. Handling partial PATCH with @JsonMergePatch. Will add optimistic locking via @Version to prevent lost updates.'),
  (13, 3,
   'Basic search queries pass. Edge cases still failing: multi-word phrases and special characters. Logged as separate subtask.'),
  (14, 3,
   'Root cause confirmed: JOIN on product_tags produces one row per tag match. Adding DISTINCT to the SELECT resolves duplicates in manual testing.');

-- -----------------------------------------------------------------------------
-- activity_logs
-- -----------------------------------------------------------------------------
INSERT INTO activity_logs (project_id, issue_id, actor_id, action_type, field_name, old_value, new_value)
VALUES
  -- AGILE-2 created and closed
  (1, 2, 4, 'ISSUE_CREATED',    NULL,        NULL,          NULL),
  (1, 2, 1, 'STATUS_CHANGED',   'status',    'To Do',       'In Progress'),
  (1, 2, 1, 'STATUS_CHANGED',   'status',    'In Progress', 'Done'),
  -- AGILE-5 reported and assigned
  (1, 5, 3, 'ISSUE_CREATED',    NULL,        NULL,          NULL),
  (1, 5, 4, 'ASSIGNEE_CHANGED', 'assignee',  NULL,          'bob_dev'),
  (1, 5, 2, 'STATUS_CHANGED',   'status',    'To Do',       'In Progress'),
  -- SHOP-3 progressed to Testing
  (2, 13, 1, 'ISSUE_CREATED',   NULL,        NULL,          NULL),
  (2, 13, 2, 'STATUS_CHANGED',  'status',    'Backlog',     'In Progress'),
  (2, 13, 2, 'STATUS_CHANGED',  'status',    'In Progress', 'Testing'),
  -- SHOP-4 critical bug raised
  (2, 14, 3, 'ISSUE_CREATED',   NULL,        NULL,          NULL),
  (2, 14, 1, 'PRIORITY_CHANGED','priority',  'HIGH',        'CRITICAL'),
  (2, 14, 1, 'ASSIGNEE_CHANGED','assignee',  NULL,          'bob_dev');

-- -----------------------------------------------------------------------------
-- watchers
-- -----------------------------------------------------------------------------
INSERT INTO watchers (issue_id, user_id)
VALUES
  (5,  4),   -- dan_pm watches the timeout bug
  (5,  3),   -- carol_qa watches the timeout bug
  (14, 1),   -- alice watches the critical duplicate bug
  (14, 3);   -- carol watches the critical duplicate bug

-- -----------------------------------------------------------------------------
-- custom_field_definitions  (one per project for demonstration)
-- -----------------------------------------------------------------------------
INSERT INTO custom_field_definitions (id, project_id, name, field_type, options, required)
VALUES
  (1, 1, 'Team',         'DROPDOWN', '["Platform","Mobile","Infra"]', false),
  (2, 1, 'Release Tag',  'TEXT',     NULL,                             false),
  (3, 2, 'Service Area', 'DROPDOWN', '["Catalogue","Cart","Orders","Payments"]', false);

SELECT setval('custom_field_definitions_id_seq', (SELECT MAX(id) FROM custom_field_definitions));
