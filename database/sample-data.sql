-- ============================================================================
-- FOCUSROOM V2 - SAMPLE DATASET (MySQL 8.0+)
-- Database: focusroom_db
-- Password for all seed accounts is: password123
-- BCrypt Hash: $2a$10$w09b4j1W/LpA2G1mO1lB4.5JgD5d0r9n.a2L1tP9qO8vX7yZ6w.Km
-- ============================================================================

USE `focusroom_db`;

-- 1. USERS
-- Notice: First account is ADMIN, remaining are regular students
INSERT INTO `users` (`id`, `username`, `email`, `password_hash`, `full_name`, `bio`, `role`, `account_status`, `created_at`) VALUES
(1, 'admin', 'admin@focusroom.edu', '$2a$10$w09b4j1W/LpA2G1mO1lB4.5JgD5d0r9n.a2L1tP9qO8vX7yZ6w.Km', 'Administrator', 'Platform Supervisor & Faculty Coordinator', 'ROLE_ADMIN', 'ACTIVE', '2026-10-01 09:00:00'),
(2, 'alex_j', 'alex@focusroom.edu', '$2a$10$w09b4j1W/LpA2G1mO1lB4.5JgD5d0r9n.a2L1tP9qO8vX7yZ6w.Km', 'Alex Johnson', 'Computer Science Senior | Preparing for Database Systems Exam', 'ROLE_USER', 'ACTIVE', '2026-10-05 10:15:00'),
(3, 'sarah_m', 'sarah@focusroom.edu', '$2a$10$w09b4j1W/LpA2G1mO1lB4.5JgD5d0r9n.a2L1tP9qO8vX7yZ6w.Km', 'Sarah Miller', 'Software Engineering Major | Distributed Systems & Algorithms', 'ROLE_USER', 'ACTIVE', '2026-10-06 11:30:00'),
(4, 'david_k', 'david@focusroom.edu', '$2a$10$w09b4j1W/LpA2G1mO1lB4.5JgD5d0r9n.a2L1tP9qO8vX7yZ6w.Km', 'David Kim', 'Data Science & Machine Learning Enthusiast', 'ROLE_USER', 'ACTIVE', '2026-10-10 14:00:00'),
(5, 'elena_r', 'elena@focusroom.edu', '$2a$10$w09b4j1W/LpA2G1mO1lB4.5JgD5d0r9n.a2L1tP9qO8vX7yZ6w.Km', 'Elena Rostova', 'Mathematics & Cryptography Student', 'ROLE_USER', 'ACTIVE', '2026-10-12 16:45:00');

-- 2. STUDY ROOMS
INSERT INTO `study_rooms` (`id`, `name`, `description`, `subject`, `owner_id`, `visibility`, `access_code`, `max_participants`, `status`, `created_at`) VALUES
(1, 'DBMS Final Exam Prep & Review', 'Reviewing Relational Algebra, Normalization (1NF to BCNF), SQL Joins, and Transaction ACID properties.', 'Database Systems', 2, 'PUBLIC', NULL, 25, 'ACTIVE', '2026-10-15 09:00:00'),
(2, 'Algorithms & Competitive Coding', 'Focus room for solving dynamic programming and graph problems. 25-minute Pomodoro cycles.', 'Algorithms', 3, 'PUBLIC', NULL, 20, 'ACTIVE', '2026-10-16 14:00:00'),
(3, 'Operating Systems Concurrency', 'Studying thread synchronization, semaphores, mutex locks, and deadlock avoidance algorithms.', 'Operating Systems', 4, 'PUBLIC', NULL, 15, 'ACTIVE', '2026-10-18 10:30:00'),
(4, 'Graduate Thesis Research Group', 'Private collaboration space for graduate distributed systems research papers.', 'Research', 2, 'PRIVATE', 'thesis2026', 10, 'ACTIVE', '2026-10-20 18:00:00');

-- 3. ROOM MEMBERSHIPS
INSERT INTO `room_memberships` (`id`, `user_id`, `room_id`, `role`, `is_active`, `joined_at`, `last_active_at`) VALUES
(1, 2, 1, 'OWNER', 1, '2026-10-15 09:00:00', '2026-10-31 20:00:00'),
(2, 3, 1, 'MEMBER', 1, '2026-10-15 09:15:00', '2026-10-31 20:10:00'),
(3, 4, 1, 'MEMBER', 1, '2026-10-15 09:30:00', '2026-10-31 20:15:00'),
(4, 5, 1, 'MEMBER', 1, '2026-10-15 10:00:00', '2026-10-31 19:45:00'),
(5, 3, 2, 'OWNER', 1, '2026-10-16 14:00:00', '2026-10-31 18:30:00'),
(6, 2, 2, 'MEMBER', 1, '2026-10-16 14:10:00', '2026-10-31 18:40:00'),
(7, 4, 3, 'OWNER', 1, '2026-10-18 10:30:00', '2026-10-31 17:00:00'),
(8, 2, 4, 'OWNER', 1, '2026-10-20 18:00:00', '2026-10-31 16:30:00'),
(9, 3, 4, 'MEMBER', 1, '2026-10-20 18:05:00', '2026-10-31 16:35:00');

-- 4. STUDY SESSIONS (Historical & Recent Pomodoros)
-- Note: actual_duration_seconds reflects pure study time excluding paused duration
INSERT INTO `study_sessions` (`id`, `user_id`, `room_id`, `session_type`, `planned_duration_minutes`, `actual_duration_seconds`, `paused_duration_seconds`, `status`, `start_time`, `end_time`, `subject`, `notes`) VALUES
(1, 2, 1, 'FOCUS', 25, 1500, 0, 'COMPLETED', '2026-10-25 10:00:00', '2026-10-25 10:25:00', 'Database Systems', 'Reviewed B+ Tree indexing and primary vs secondary keys.'),
(2, 2, 1, 'SHORT_BREAK', 5, 300, 0, 'COMPLETED', '2026-10-25 10:25:00', '2026-10-25 10:30:00', 'Database Systems', 'Hydration break.'),
(3, 2, 1, 'FOCUS', 25, 1440, 60, 'COMPLETED', '2026-10-25 10:30:00', '2026-10-25 10:56:00', 'Database Systems', 'Solved queries with nested subqueries and GROUP BY HAVING.'),
(4, 2, 2, 'FOCUS', 25, 1500, 0, 'COMPLETED', '2026-10-27 14:00:00', '2026-10-27 14:25:00', 'Algorithms', 'Implemented Dijkstra algorithm with priority queue.'),
(5, 2, 1, 'FOCUS', 25, 1500, 0, 'COMPLETED', '2026-10-28 11:00:00', '2026-10-28 11:25:00', 'Database Systems', 'Normalized unnormalized relations to 3NF and BCNF.'),
(6, 2, 3, 'FOCUS', 25, 1500, 0, 'COMPLETED', '2026-10-29 15:30:00', '2026-10-29 15:55:00', 'Operating Systems', 'Reviewed Banker algorithm for deadlock detection.'),
(7, 2, 1, 'FOCUS', 25, 1500, 0, 'COMPLETED', '2026-10-30 09:00:00', '2026-10-30 09:25:00', 'Database Systems', 'Executed transaction rollback tests with savepoints.'),
(8, 2, 1, 'FOCUS', 25, 1500, 0, 'COMPLETED', '2026-10-31 09:30:00', '2026-10-31 09:55:00', 'Database Systems', 'Practiced Spring Data JPA JPQL queries.'),
(9, 3, 2, 'FOCUS', 25, 1500, 0, 'COMPLETED', '2026-10-30 16:00:00', '2026-10-30 16:25:00', 'Algorithms', 'Solved dynamic programming grid path problems.'),
(10, 4, 3, 'FOCUS', 25, 1500, 0, 'COMPLETED', '2026-10-31 11:00:00', '2026-10-31 11:25:00', 'Operating Systems', 'Studied virtual memory and page replacement algorithms.');

-- 5. TASKS
INSERT INTO `tasks` (`id`, `user_id`, `title`, `description`, `subject`, `priority`, `status`, `due_date`, `completed_at`, `created_at`) VALUES
(1, 2, 'Complete DBMS Lab Assignment 4', 'Write DDL schema, create composite indexes, and verify referential integrity constraints.', 'Database Systems', 'HIGH', 'COMPLETED', '2026-10-28', '2026-10-28 17:00:00', '2026-10-20 09:00:00'),
(2, 2, 'Prepare BCNF Normalization Proofs', 'Derive functional dependencies for student enrollment schema and eliminate partial dependencies.', 'Database Systems', 'URGENT', 'PENDING', '2026-11-02', NULL, '2026-10-26 14:00:00'),
(3, 2, 'Implement Spring Security Authentication', 'Configure BCrypt password hashing and DaoAuthenticationProvider.', 'Software Engineering', 'MEDIUM', 'COMPLETED', '2026-10-29', '2026-10-29 19:30:00', '2026-10-24 10:00:00'),
(4, 2, 'Review Operating Systems Semaphores', 'Practice producer-consumer problem with bounded buffers in Java.', 'Operating Systems', 'MEDIUM', 'PENDING', '2026-11-05', NULL, '2026-10-28 11:00:00'),
(5, 2, 'Read Distributed Transactions Paper', 'Two-phase commit protocol and Raft consensus algorithms overview.', 'Research', 'LOW', 'PENDING', '2026-11-10', NULL, '2026-10-30 15:00:00'),
(6, 3, 'Implement Red-Black Tree Balancing', 'Handle left and right rotation cases for binary search tree insertion.', 'Algorithms', 'HIGH', 'PENDING', '2026-11-03', NULL, '2026-10-29 12:00:00');

-- 6. CHAT MESSAGES
INSERT INTO `chat_messages` (`id`, `room_id`, `sender_id`, `message`, `message_type`, `sent_at`) VALUES
(1, 1, 2, 'Welcome everyone to the DBMS final review room! We are starting with 3NF and BCNF normalization.', 'CHAT', '2026-10-31 09:00:00'),
(2, 1, 3, 'joined the study room', 'JOIN', '2026-10-31 09:01:00'),
(3, 1, 3, 'Hey Alex! Ready for the Pomodoro focus block. I have questions on lossless join decomposition.', 'CHAT', '2026-10-31 09:02:00'),
(4, 1, 4, 'joined the study room', 'JOIN', '2026-10-31 09:05:00'),
(5, 1, 4, 'Hello team! Let us start the 25-minute timer now.', 'CHAT', '2026-10-31 09:06:00'),
(6, 1, 2, 'Starting the synchronized timer now! Let us focus.', 'CHAT', '2026-10-31 09:06:30');

-- 7. NOTIFICATIONS
INSERT INTO `notifications` (`id`, `user_id`, `title`, `message`, `type`, `is_read`, `link_url`, `created_at`) VALUES
(1, 2, 'Focus Session Completed!', 'Great job! You logged 25 minutes of active study on Database Systems.', 'SESSION_COMPLETED', 1, '/analytics', '2026-10-31 09:55:00'),
(2, 2, 'Task Due Soon: Prepare BCNF Normalization Proofs', 'Your task is due on 2026-11-02. Stay on track!', 'DEADLINE_ALERT', 0, '/tasks', '2026-10-31 10:00:00'),
(3, 2, 'Study Milestone Achieved!', 'You have maintained a consecutive study streak of 7 days!', 'MILESTONE', 0, '/analytics', '2026-10-31 10:05:00'),
(4, 3, 'Focus Session Completed!', 'Great job! You logged 25 minutes of active study on Algorithms.', 'SESSION_COMPLETED', 1, '/analytics', '2026-10-30 16:25:00');
