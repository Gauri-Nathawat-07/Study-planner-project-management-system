-- ============================================================
-- Study Planner & Project Management System
-- Simple & Beginner-Friendly Database Schema
-- Database Name: studyplanner_db
-- ============================================================

-- Step 1: Create Database and Use it
CREATE DATABASE IF NOT EXISTS studyplanner_db;
USE studyplanner_db;

-- Step 2: Drop existing tables (child tables dropped first)
DROP TABLE IF EXISTS study_sessions;
DROP TABLE IF EXISTS tasks;
DROP TABLE IF EXISTS projects;
DROP TABLE IF EXISTS subjects;
DROP TABLE IF EXISTS user_settings;

-- Step 3: Create Tables

-- 1. Subjects Table
CREATE TABLE subjects (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    color_hex VARCHAR(20) DEFAULT '#3B82F6',
    target_weekly_hours INT DEFAULT 5,
    total_study_minutes INT DEFAULT 0
);

-- 2. Projects Table
CREATE TABLE projects (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    start_date DATETIME,
    deadline DATETIME,
    priority VARCHAR(20) DEFAULT 'MEDIUM',
    status VARCHAR(20) DEFAULT 'NOT_STARTED'
);

-- 3. Tasks Table
CREATE TABLE tasks (
    id VARCHAR(50) PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    description VARCHAR(255),
    subject_id VARCHAR(50),
    project_id VARCHAR(50),
    priority VARCHAR(20) DEFAULT 'MEDIUM',
    status VARCHAR(20) DEFAULT 'PENDING',
    created_date DATETIME,
    due_date DATETIME,
    estimated_minutes INT DEFAULT 30,
    actual_minutes INT DEFAULT 0,
    tags VARCHAR(255),
    completion_date DATETIME,
    FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE SET NULL,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE SET NULL
);

-- 4. Study Sessions Table
CREATE TABLE study_sessions (
    id VARCHAR(50) PRIMARY KEY,
    task_id VARCHAR(50),
    subject_id VARCHAR(50),
    start_time DATETIME,
    end_time DATETIME,
    duration_minutes INT DEFAULT 0,
    session_type VARCHAR(20) DEFAULT 'FOCUS',
    notes VARCHAR(255),
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE SET NULL,
    FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE SET NULL
);

-- 5. User Settings Table
CREATE TABLE user_settings (
    id INT PRIMARY KEY,
    user_name VARCHAR(100) DEFAULT 'Scholar',
    focus_duration_minutes INT DEFAULT 25,
    short_break_duration_minutes INT DEFAULT 5,
    long_break_duration_minutes INT DEFAULT 15,
    weekly_study_hour_target INT DEFAULT 20,
    preferred_week_start_day INT DEFAULT 2,
    dark_mode INT DEFAULT 0,
    confirm_deletions INT DEFAULT 1
);

-- Step 4: Insert Starter / Sample Data

-- Default Settings
INSERT INTO user_settings (id, user_name, focus_duration_minutes, short_break_duration_minutes, long_break_duration_minutes, weekly_study_hour_target, preferred_week_start_day, dark_mode, confirm_deletions)
VALUES (1, 'Scholar', 25, 5, 15, 20, 2, 0, 1);

-- Sample Subjects
INSERT INTO subjects (id, name, description, color_hex, target_weekly_hours, total_study_minutes) VALUES
('sub-math-01', 'Mathematics', 'Calculus and Linear Algebra', '#3B82F6', 6, 25),
('sub-cs-02', 'Computer Science', 'Data Structures and Algorithms', '#8B5CF6', 8, 0),
('sub-phy-03', 'Physics', 'Mechanics and Thermodynamics', '#10B981', 4, 0);

-- Sample Project
INSERT INTO projects (id, name, description, start_date, deadline, priority, status) VALUES
('proj-01', 'Term Project', 'Machine Learning Research and Code', '2026-11-01 10:00:00', '2026-11-30 18:00:00', 'HIGH', 'IN_PROGRESS');

-- Sample Tasks
INSERT INTO tasks (id, title, description, subject_id, project_id, priority, status, created_date, due_date, estimated_minutes, actual_minutes, tags, completion_date) VALUES
('task-01', 'Complete Calculus Assignment', 'Problems on integration and limits', 'sub-math-01', NULL, 'HIGH', 'PENDING', '2026-11-10 09:00:00', '2026-11-20 23:59:00', 90, 0, 'homework,math', NULL),
('task-02', 'Implement Dijkstra Algorithm', 'Java graph implementation', 'sub-cs-02', 'proj-01', 'URGENT', 'PENDING', '2026-11-12 14:00:00', '2026-11-22 18:00:00', 120, 0, 'coding,java', NULL),
('task-03', 'Read Physics Chapter 4', 'Laws of thermodynamics', 'sub-phy-03', NULL, 'MEDIUM', 'PENDING', '2026-11-13 11:00:00', '2026-11-25 20:00:00', 45, 0, 'reading', NULL);

-- Sample Study Session
INSERT INTO study_sessions (id, task_id, subject_id, start_time, end_time, duration_minutes, session_type, notes) VALUES
('sess-01', 'task-01', 'sub-math-01', '2026-11-13 10:00:00', '2026-11-13 10:25:00', 25, 'FOCUS', 'Calculus practice session');
