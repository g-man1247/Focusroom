# FOCUSROOM V2 — DBMS LABORATORY CONCEPTS & VIVA EXPLANATION GUIDE

This document serves as an academic and technical reference demonstrating how classical and advanced Database Management System (DBMS) concepts are implemented in **Focusroom V2**.

---

## 1. Data Definition Language (DDL)

DDL commands define and modify the database structure and schema objects.

### A. CREATE DATABASE & CREATE TABLE
In Focusroom, `focusroom_db` and its 7 relational tables are instantiated with explicit character encodings, primary keys, and foreign keys.

```sql
CREATE DATABASE IF NOT EXISTS `focusroom_db`
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

CREATE TABLE `study_rooms` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL,
    `description` TEXT DEFAULT NULL,
    `subject` VARCHAR(50) NOT NULL,
    `owner_id` BIGINT NOT NULL,
    `visibility` VARCHAR(20) NOT NULL DEFAULT 'PUBLIC',
    `access_code` VARCHAR(50) DEFAULT NULL,
    `max_participants` INT NOT NULL DEFAULT 25,
    `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `pk_study_rooms` PRIMARY KEY (`id`),
    CONSTRAINT `fk_room_owner` FOREIGN KEY (`owner_id`) 
        REFERENCES `users` (`id`) ON DELETE CASCADE
);
```

### B. ALTER TABLE
Demonstrating schema evolution:
```sql
-- Adding index dynamically or modifying constraints
ALTER TABLE `study_rooms` 
    ADD CONSTRAINT `chk_room_capacity` 
    CHECK (`max_participants` >= 2 AND `max_participants` <= 100);
```

### C. DROP TABLE (Safe Dependency Order)
```sql
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS `notifications`, `chat_messages`, `tasks`, `study_sessions`, `room_memberships`, `study_rooms`, `users`;
SET FOREIGN_KEY_CHECKS = 1;
```

---

## 2. Data Manipulation Language (DML)

DML commands manipulate the actual records within relations.

### A. INSERT
Used when students register, create tasks, or start sessions:
```sql
INSERT INTO `tasks` (`user_id`, `title`, `subject`, `priority`, `status`, `due_date`)
VALUES (2, 'Review Database Indexes', 'DBMS', 'HIGH', 'PENDING', '2026-11-02');
```

### B. UPDATE
Used when pausing a Pomodoro session or marking tasks complete:
```sql
-- Marking a task completed
UPDATE `tasks`
SET `status` = 'COMPLETED', `completed_at` = CURRENT_TIMESTAMP
WHERE `id` = 10 AND `user_id` = 2;

-- Bulk marking all unread notifications as read
UPDATE `notifications` 
SET `is_read` = TRUE 
WHERE `user_id` = 2 AND `is_read` = FALSE;
```

### C. DELETE
Used when deleting a task or leaving an archived room:
```sql
DELETE FROM `tasks` WHERE `id` = 5 AND `user_id` = 2;
```

### D. SELECT
Powering personalized dashboards and queries:
```sql
SELECT `id`, `name`, `subject`, `max_participants`
FROM `study_rooms`
WHERE `status` = 'ACTIVE' AND `visibility` = 'PUBLIC'
ORDER BY `created_at` DESC;
```

---

## 3. Database Constraints & Referential Integrity

Focusroom enforces multi-level relational constraints:

1. **PRIMARY KEY (`pk_*`)**: Guarantees entity integrity and uniqueness of each tuple (`users.id`, `study_rooms.id`, etc.).
2. **FOREIGN KEY (`fk_*`)**: Enforces referential integrity. Deleting an owner user cascades (`ON DELETE CASCADE`) to their rooms and tasks, preventing dangling foreign pointers.
3. **UNIQUE (`uk_*`)**:
   - `users.email` and `users.username` prevent duplicate accounts.
   - `room_memberships(user_id, room_id)` prevents duplicate memberships for the same user in a single room.
4. **NOT NULL**: Ensures essential columns (e.g., `password_hash`, `title`, `start_time`) cannot hold null values.
5. **CHECK Constraints**: Enforces valid domain values for enumerations and range boundaries (`max_participants BETWEEN 2 AND 100`).
6. **DEFAULT**: Automatically assigns sensible values (e.g., `is_active = TRUE`, `status = 'PENDING'`).

---

## 4. Complex SQL Queries & Joins

### A. LEFT JOIN with Aggregation (Active Room Discovery)
Finds all active public rooms along with their current live participant count:

```sql
SELECT 
    r.id, 
    r.name, 
    r.subject, 
    r.max_participants,
    COUNT(m.id) AS active_students
FROM `study_rooms` r
LEFT JOIN `room_memberships` m 
    ON r.id = m.room_id AND m.is_active = TRUE
WHERE r.status = 'ACTIVE' AND r.visibility = 'PUBLIC'
GROUP BY r.id, r.name, r.subject, r.max_participants
ORDER BY active_students DESC, r.created_at DESC;
```
*Application location: `StudyRoomRepository.java` (`findPopularActiveRooms`)*

### B. Aggregate Functions: SUM, COUNT, AVG
Calculating student productivity statistics:
```sql
-- Total study hours and completed sessions
SELECT 
    COALESCE(SUM(actual_duration_seconds), 0) / 3600.0 AS total_hours,
    COUNT(id) AS total_completed_sessions,
    AVG(actual_duration_seconds) / 60.0 AS avg_session_minutes
FROM `study_sessions`
WHERE `user_id` = 2 AND `status` = 'COMPLETED';
```
*Application location: `StudySessionRepository.java` (`getTotalStudySecondsByUser`)*

### C. GROUP BY with HAVING Clause
Identifies subjects where the student has spent a significant amount of study time (e.g. over 1 hour / 3600 seconds):

```sql
SELECT 
    COALESCE(subject, 'General') AS study_subject,
    SUM(actual_duration_seconds) AS total_seconds,
    COUNT(id) AS session_count
FROM `study_sessions`
WHERE `user_id` = 2 AND `status` = 'COMPLETED'
GROUP BY subject
HAVING SUM(actual_duration_seconds) > 3600
ORDER BY total_seconds DESC;
```
*Application location: `StudySessionRepository.java` (`getSignificantSubjectsByUser`)*

### D. Nested Subquery (Correlated/Non-Correlated)
Retrieves all focus sessions whose duration exceeded the user's personal average session duration:

```sql
SELECT s.id, s.subject, s.actual_duration_seconds, s.start_time
FROM `study_sessions` s
WHERE s.user_id = 2 
  AND s.status = 'COMPLETED' 
  AND s.actual_duration_seconds > (
      SELECT AVG(s2.actual_duration_seconds) 
      FROM `study_sessions` s2 
      WHERE s2.user_id = 2 AND s2.status = 'COMPLETED'
  )
ORDER BY s.actual_duration_seconds DESC;
```
*Application location: `StudySessionRepository.java` (`findAboveAverageSessionsByUser`)*

---

## 5. Database Normalization Analysis

The schema satisfies normalization criteria up to **Boyce-Codd Normal Form (BCNF)**:

### 1NF (First Normal Form)
- All attribute values are atomic (no arrays or repeating groups).
- Each column has a distinct name and a primary key exists for every relation.

### 2NF (Second Normal Form)
- Relation is in 1NF.
- There are **no partial dependencies**: all non-key attributes are fully functionally dependent on the entire primary key. In `room_memberships`, attributes `role` and `is_active` depend on `id`, or in natural keys on the composite candidate key `(user_id, room_id)`.

### 3NF (Third Normal Form)
- Relation is in 2NF.
- There are **no transitive dependencies** ($X \rightarrow Y$ and $Y \rightarrow Z$ where $Z$ is non-prime).
- For example, User details (`username`, `email`) are stored exclusively in `users`, and referenced by `user_id` in `study_sessions`, rather than duplicating student information across sessions.

### BCNF (Boyce-Codd Normal Form)
- For every non-trivial functional dependency $X \rightarrow Y$, $X$ is a superkey.
- In `room_memberships`, candidate keys are `id` and `(user_id, room_id)`. All determinants are candidate keys.

---

## 6. Indexing Strategy & Query Optimization

Indexes are critical in relational databases to reduce disk I/O from $O(N)$ full table scans to $O(\log N)$ B-Tree traversals.

| Index Name | Table | Columns | Purpose |
| :--- | :--- | :--- | :--- |
| `idx_user_username` | `users` | `username` | Fast authentication lookup during login |
| `idx_user_email` | `users` | `email` | Unique registration lookup |
| `idx_room_subject` | `study_rooms` | `subject` | Accelerated room search filtering |
| `idx_room_status` | `study_rooms` | `status` | Active room queries |
| `idx_membership_active` | `room_memberships` | `is_active` | Counting live room participants |
| `idx_session_user_status`| `study_sessions` | `user_id, status` | Composite index for user dashboard queries |
| `idx_task_user_status` | `tasks` | `user_id, status` | Composite index for pending tasks filter |
| `idx_chat_sent_at` | `chat_messages` | `sent_at` | Chronological pagination of room chat |

---

## 7. Transactions and ACID Properties

All critical state transitions in Focusroom are wrapped with Spring's `@Transactional` annotation, which coordinates database transactions through MySQL InnoDB:

1. **Atomicity**:
   When `createRoom` executes, both the room record in `study_rooms` and the initial host membership in `room_memberships` are committed together. If inserting the membership fails, the entire transaction rolls back, preventing rooms without owners.

2. **Consistency**:
   Constraints such as foreign keys and room capacity checks (`chk_room_capacity`) preserve consistency before and after execution.

3. **Isolation**:
   MySQL InnoDB defaults to `REPEATABLE READ`, preventing dirty reads and non-repeatable reads during concurrent focus timer updates and task edits.

4. **Durability**:
   Once a Pomodoro session completes, transaction logs are flushed to MySQL disk (write-ahead logging via InnoDB Redo Log), ensuring study duration persists across server restarts.
