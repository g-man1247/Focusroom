# Focusroom V2 — Entity-Relationship (ER) Diagram & Schema Architecture

## 1. Relational Database Design Overview

The database for Focusroom V2 (`focusroom_db`) is designed using **Third Normal Form (3NF)** and **Boyce-Codd Normal Form (BCNF)** principles. It models a virtual study environment where students can collaborate in digital rooms, synchronize focus sessions, manage academic tasks, exchange messages, and track productivity.

---

## 2. Mermaid Entity-Relationship Diagram

```mermaid
erDiagram
    USERS ||--o{ STUDY_ROOMS : "owns / hosts"
    USERS ||--o{ ROOM_MEMBERSHIPS : "joins"
    USERS ||--o{ STUDY_SESSIONS : "records"
    USERS ||--o{ TASKS : "manages"
    USERS ||--o{ CHAT_MESSAGES : "sends"
    USERS ||--o{ NOTIFICATIONS : "receives"

    STUDY_ROOMS ||--o{ ROOM_MEMBERSHIPS : "contains"
    STUDY_ROOMS ||--o{ CHAT_MESSAGES : "hosts"
    STUDY_ROOMS ||--o{ STUDY_SESSIONS : "associates"

    USERS {
        bigint id PK
        varchar(50) username UK
        varchar(100) email UK
        varchar(255) password_hash
        varchar(100) full_name
        varchar(255) bio
        varchar(255) profile_image
        varchar(20) role
        varchar(20) account_status
        datetime created_at
        datetime updated_at
    }

    STUDY_ROOMS {
        bigint id PK
        varchar(100) name
        text description
        varchar(50) subject
        bigint owner_id FK
        varchar(20) visibility
        varchar(50) access_code
        int max_participants
        varchar(20) status
        datetime created_at
        datetime updated_at
    }

    ROOM_MEMBERSHIPS {
        bigint id PK
        bigint user_id FK
        bigint room_id FK
        varchar(20) role
        boolean is_active
        datetime joined_at
        datetime last_active_at
        datetime left_at
    }

    STUDY_SESSIONS {
        bigint id PK
        bigint user_id FK
        bigint room_id FK
        varchar(20) session_type
        int planned_duration_minutes
        bigint actual_duration_seconds
        bigint paused_duration_seconds
        varchar(20) status
        datetime start_time
        datetime end_time
        datetime last_pause_time
        varchar(50) subject
        text notes
    }

    TASKS {
        bigint id PK
        bigint user_id FK
        varchar(150) title
        text description
        varchar(50) subject
        varchar(20) priority
        varchar(20) status
        date due_date
        datetime completed_at
        datetime created_at
        datetime updated_at
    }

    CHAT_MESSAGES {
        bigint id PK
        bigint room_id FK
        bigint sender_id FK
        text message
        varchar(20) message_type
        datetime sent_at
    }

    NOTIFICATIONS {
        bigint id PK
        bigint user_id FK
        varchar(100) title
        text message
        varchar(30) type
        boolean is_read
        varchar(255) link_url
        datetime created_at
    }
```

---

## 3. Relational Cardinalities and Structural Integrity

| Relationship | Cardinality | Parent Entity | Child Entity | Foreign Key Constraint | Deletion Behavior |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Room Ownership** | 1 : N | `users` | `study_rooms` | `fk_room_owner` | `ON DELETE CASCADE` |
| **Room Membership** | M : N | `users` & `study_rooms` | `room_memberships` | `fk_membership_user`, `fk_membership_room` | `ON DELETE CASCADE` |
| **Study Sessions** | 1 : N | `users` | `study_sessions` | `fk_session_user` | `ON DELETE CASCADE` |
| **Room Study Link**| 1 : N (Optional) | `study_rooms` | `study_sessions` | `fk_session_room` | `ON DELETE SET NULL` |
| **Task Ownership** | 1 : N | `users` | `tasks` | `fk_task_user` | `ON DELETE CASCADE` |
| **Room Chat** | 1 : N | `study_rooms` | `chat_messages` | `fk_chat_room` | `ON DELETE CASCADE` |
| **Message Sender** | 1 : N | `users` | `chat_messages` | `fk_chat_sender` | `ON DELETE CASCADE` |
| **Notifications** | 1 : N | `users` | `notifications` | `fk_notification_user` | `ON DELETE CASCADE` |

---

## 4. Key Constraints and Integrity Guarantees

1. **Unique Constraints (`UK`)**:
   - `users(username)`: Prevents duplicate login handles across the entire system.
   - `users(email)`: Guarantees unique email identity.
   - `room_memberships(user_id, room_id)`: Composite unique constraint preventing a student from registering duplicate active memberships in the same room.

2. **Domain Integrity (`CHECK` Constraints)**:
   - `users.role IN ('ROLE_USER', 'ROLE_ADMIN')`
   - `users.account_status IN ('ACTIVE', 'SUSPENDED', 'DEACTIVATED')`
   - `study_rooms.visibility IN ('PUBLIC', 'PRIVATE')`
   - `study_rooms.status IN ('ACTIVE', 'ARCHIVED', 'CLOSED')`
   - `study_rooms.max_participants >= 2 AND study_rooms.max_participants <= 100`
   - `tasks.priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT')`
   - `tasks.status IN ('PENDING', 'IN_PROGRESS', 'COMPLETED')`
   - `study_sessions.session_type IN ('FOCUS', 'SHORT_BREAK', 'LONG_BREAK')`
   - `study_sessions.status IN ('IN_PROGRESS', 'PAUSED', 'COMPLETED', 'CANCELLED')`

3. **Referential Integrity**:
   - Foreign keys enforce parent-child relational validity. If an owner account is removed, dependent study rooms and tasks cascade safely without creating orphaned records.
