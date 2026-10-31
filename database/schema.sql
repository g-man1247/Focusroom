-- ============================================================================
-- FOCUSROOM V2 - RELATIONAL DATABASE SCHEMA (MySQL 8.0+)
-- Database: focusroom_db
-- ============================================================================

-- 1. DATABASE CREATION (DDL)
CREATE DATABASE IF NOT EXISTS `focusroom_db`
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE `focusroom_db`;

-- Drop existing tables in reverse dependency order if executing fresh
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS `notifications`;
DROP TABLE IF EXISTS `chat_messages`;
DROP TABLE IF EXISTS `tasks`;
DROP TABLE IF EXISTS `study_sessions`;
DROP TABLE IF EXISTS `room_memberships`;
DROP TABLE IF EXISTS `study_rooms`;
DROP TABLE IF EXISTS `users`;
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================================
-- Table: users
-- Represents authenticated students and administrators
-- Demonstrates: PRIMARY KEY, UNIQUE, NOT NULL, CHECK, DEFAULT, INDEX
-- ============================================================================
CREATE TABLE `users` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `username` VARCHAR(50) NOT NULL,
    `email` VARCHAR(100) NOT NULL,
    `password_hash` VARCHAR(255) NOT NULL,
    `full_name` VARCHAR(100) DEFAULT NULL,
    `bio` VARCHAR(255) DEFAULT NULL,
    `profile_image` VARCHAR(255) DEFAULT NULL,
    `role` VARCHAR(20) NOT NULL DEFAULT 'ROLE_USER',
    `account_status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `pk_users` PRIMARY KEY (`id`),
    CONSTRAINT `uk_users_username` UNIQUE (`username`),
    CONSTRAINT `uk_users_email` UNIQUE (`email`),
    CONSTRAINT `chk_users_role` CHECK (`role` IN ('ROLE_USER', 'ROLE_ADMIN')),
    CONSTRAINT `chk_users_status` CHECK (`account_status` IN ('ACTIVE', 'SUSPENDED', 'DEACTIVATED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX `idx_user_role` ON `users` (`role`);
CREATE INDEX `idx_user_status` ON `users` (`account_status`);

-- ============================================================================
-- Table: study_rooms
-- Virtual digital study environments created and hosted by users
-- Demonstrates: FOREIGN KEY with ON DELETE CASCADE, ENUM checks, INDEX
-- ============================================================================
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
        REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `chk_room_visibility` CHECK (`visibility` IN ('PUBLIC', 'PRIVATE')),
    CONSTRAINT `chk_room_status` CHECK (`status` IN ('ACTIVE', 'ARCHIVED', 'CLOSED')),
    CONSTRAINT `chk_room_capacity` CHECK (`max_participants` >= 2 AND `max_participants` <= 100)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX `idx_room_subject` ON `study_rooms` (`subject`);
CREATE INDEX `idx_room_visibility` ON `study_rooms` (`visibility`);
CREATE INDEX `idx_room_status` ON `study_rooms` (`status`);
CREATE INDEX `idx_room_owner` ON `study_rooms` (`owner_id`);

-- ============================================================================
-- Table: room_memberships
-- Associative entity mapping Many-to-Many relationship between User and StudyRoom
-- Demonstrates: Composite UNIQUE Constraint, Foreign Keys, Active presence
-- ============================================================================
CREATE TABLE `room_memberships` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `room_id` BIGINT NOT NULL,
    `role` VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
    `is_active` BOOLEAN NOT NULL DEFAULT TRUE,
    `joined_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `last_active_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `left_at` DATETIME DEFAULT NULL,
    CONSTRAINT `pk_room_memberships` PRIMARY KEY (`id`),
    CONSTRAINT `uk_user_room` UNIQUE (`user_id`, `room_id`),
    CONSTRAINT `fk_membership_user` FOREIGN KEY (`user_id`) 
        REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_membership_room` FOREIGN KEY (`room_id`) 
        REFERENCES `study_rooms` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `chk_membership_role` CHECK (`role` IN ('OWNER', 'MODERATOR', 'MEMBER'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX `idx_membership_user` ON `room_memberships` (`user_id`);
CREATE INDEX `idx_membership_room` ON `room_memberships` (`room_id`);
CREATE INDEX `idx_membership_active` ON `room_memberships` (`is_active`);

-- ============================================================================
-- Table: study_sessions
-- Authoritative Pomodoro and study tracking records
-- Demonstrates: Foreign Key, Duration calculation, Timestamps
-- ============================================================================
CREATE TABLE `study_sessions` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `room_id` BIGINT DEFAULT NULL,
    `session_type` VARCHAR(20) NOT NULL DEFAULT 'FOCUS',
    `planned_duration_minutes` INT NOT NULL DEFAULT 25,
    `actual_duration_seconds` BIGINT NOT NULL DEFAULT 0,
    `paused_duration_seconds` BIGINT NOT NULL DEFAULT 0,
    `status` VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS',
    `start_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `end_time` DATETIME DEFAULT NULL,
    `last_pause_time` DATETIME DEFAULT NULL,
    `subject` VARCHAR(50) DEFAULT NULL,
    `notes` TEXT DEFAULT NULL,
    CONSTRAINT `pk_study_sessions` PRIMARY KEY (`id`),
    CONSTRAINT `fk_session_user` FOREIGN KEY (`user_id`) 
        REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_session_room` FOREIGN KEY (`room_id`) 
        REFERENCES `study_rooms` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT `chk_session_type` CHECK (`session_type` IN ('FOCUS', 'SHORT_BREAK', 'LONG_BREAK')),
    CONSTRAINT `chk_session_status` CHECK (`status` IN ('IN_PROGRESS', 'PAUSED', 'COMPLETED', 'CANCELLED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX `idx_session_user` ON `study_sessions` (`user_id`);
CREATE INDEX `idx_session_room` ON `study_sessions` (`room_id`);
CREATE INDEX `idx_session_status` ON `study_sessions` (`status`);
CREATE INDEX `idx_session_start_time` ON `study_sessions` (`start_time`);
CREATE INDEX `idx_session_user_status` ON `study_sessions` (`user_id`, `status`);

-- ============================================================================
-- Table: tasks
-- Academic task management and deadlines for students
-- Demonstrates: Date types, Status enums, User isolation
-- ============================================================================
CREATE TABLE `tasks` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `title` VARCHAR(150) NOT NULL,
    `description` TEXT DEFAULT NULL,
    `subject` VARCHAR(50) DEFAULT NULL,
    `priority` VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    `due_date` DATE DEFAULT NULL,
    `completed_at` DATETIME DEFAULT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `pk_tasks` PRIMARY KEY (`id`),
    CONSTRAINT `fk_task_user` FOREIGN KEY (`user_id`) 
        REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `chk_task_priority` CHECK (`priority` IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT')),
    CONSTRAINT `chk_task_status` CHECK (`status` IN ('PENDING', 'IN_PROGRESS', 'COMPLETED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX `idx_task_user` ON `tasks` (`user_id`);
CREATE INDEX `idx_task_status` ON `tasks` (`status`);
CREATE INDEX `idx_task_priority` ON `tasks` (`priority`);
CREATE INDEX `idx_task_due_date` ON `tasks` (`due_date`);
CREATE INDEX `idx_task_user_status` ON `tasks` (`user_id`, `status`);

-- ============================================================================
-- Table: chat_messages
-- Real-time and persisted chat log for study rooms
-- Demonstrates: Text storage, Foreign keys, Chronological indexing
-- ============================================================================
CREATE TABLE `chat_messages` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `room_id` BIGINT NOT NULL,
    `sender_id` BIGINT NOT NULL,
    `message` TEXT NOT NULL,
    `message_type` VARCHAR(20) NOT NULL DEFAULT 'CHAT',
    `sent_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `pk_chat_messages` PRIMARY KEY (`id`),
    CONSTRAINT `fk_chat_room` FOREIGN KEY (`room_id`) 
        REFERENCES `study_rooms` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_chat_sender` FOREIGN KEY (`sender_id`) 
        REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `chk_chat_message_type` CHECK (`message_type` IN ('CHAT', 'JOIN', 'LEAVE', 'SYSTEM'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX `idx_chat_room` ON `chat_messages` (`room_id`);
CREATE INDEX `idx_chat_sender` ON `chat_messages` (`sender_id`);
CREATE INDEX `idx_chat_sent_at` ON `chat_messages` (`sent_at`);

-- ============================================================================
-- Table: notifications
-- Milestone, deadline, and study alerts
-- Demonstrates: Boolean flags, URL storage, Chronological query index
-- ============================================================================
CREATE TABLE `notifications` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `title` VARCHAR(100) NOT NULL,
    `message` TEXT NOT NULL,
    `type` VARCHAR(30) NOT NULL DEFAULT 'SYSTEM',
    `is_read` BOOLEAN NOT NULL DEFAULT FALSE,
    `link_url` VARCHAR(255) DEFAULT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `pk_notifications` PRIMARY KEY (`id`),
    CONSTRAINT `fk_notification_user` FOREIGN KEY (`user_id`) 
        REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `chk_notification_type` CHECK (`type` IN ('SESSION_COMPLETED', 'DEADLINE_ALERT', 'ROOM_UPDATE', 'MILESTONE', 'SYSTEM'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX `idx_notification_user` ON `notifications` (`user_id`);
CREATE INDEX `idx_notification_read` ON `notifications` (`is_read`);
CREATE INDEX `idx_notification_user_read` ON `notifications` (`user_id`, `is_read`);
