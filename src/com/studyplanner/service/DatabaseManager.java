package com.studyplanner.service;

import com.studyplanner.model.*;
import com.studyplanner.util.DateUtils;

import java.io.File;
import java.io.FileInputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Robust MySQL Database Manager.
 * Connects via MySQL Connector/J driver to local or remote MySQL servers.
 * Automatically loads connection parameters from db.properties if available.
 */
public class DatabaseManager {
    private static final String PROP_FILE = "db.properties";
    private static DatabaseManager instance;

    private String host = "localhost";
    private int port = 3306;
    private String database = "studyplanner_db";
    private String user = "root";
    private String password = "";
    private boolean useSSL = false;
    private boolean allowPublicKeyRetrieval = true;
    private String serverTimezone = "UTC";

    private boolean connected = false;

    public DatabaseManager() {
        loadProperties();
        initDatabase();
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    private void loadProperties() {
        File file = new File(PROP_FILE);
        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                Properties props = new Properties();
                props.load(fis);
                host = props.getProperty("db.host", host).trim();
                port = Integer.parseInt(props.getProperty("db.port", String.valueOf(port)).trim());
                database = props.getProperty("db.database", database).trim();
                user = props.getProperty("db.user", user).trim();
                password = props.getProperty("db.password", password);
                useSSL = Boolean.parseBoolean(props.getProperty("db.useSSL", "false").trim());
                allowPublicKeyRetrieval = Boolean.parseBoolean(props.getProperty("db.allowPublicKeyRetrieval", "true").trim());
                serverTimezone = props.getProperty("db.serverTimezone", "UTC").trim();
            } catch (Exception e) {
                System.err.println("Notice: Could not parse db.properties, using defaults (" + e.getMessage() + ")");
            }
        }
    }

    public String getJdbcUrl() {
        return "jdbc:mysql://" + host + ":" + port + "/" + database +
                "?useSSL=" + useSSL +
                "&allowPublicKeyRetrieval=" + allowPublicKeyRetrieval +
                "&serverTimezone=" + serverTimezone +
                "&createDatabaseIfNotExist=true";
    }

    /**
     * Obtains a new connection to the MySQL database.
     */
    public Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
            } catch (ClassNotFoundException ignored) {}
        }
        return DriverManager.getConnection(getJdbcUrl(), user, password);
    }

    public boolean isConnected() {
        return connected;
    }

    public String getDatabaseInfo() {
        return host + ":" + port + "/" + database + " (" + user + ")";
    }

    /**
     * Initializes database tables in MySQL.
     */
    public synchronized void initDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            connected = true;

            // 1. Subjects table
            stmt.execute("CREATE TABLE IF NOT EXISTS subjects (" +
                    "id VARCHAR(64) PRIMARY KEY, " +
                    "name VARCHAR(255) NOT NULL, " +
                    "description TEXT, " +
                    "color_hex VARCHAR(16) NOT NULL DEFAULT '#3B82F6', " +
                    "target_weekly_hours INT DEFAULT 5, " +
                    "total_study_minutes BIGINT DEFAULT 0" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");

            // 2. Projects table
            stmt.execute("CREATE TABLE IF NOT EXISTS projects (" +
                    "id VARCHAR(64) PRIMARY KEY, " +
                    "name VARCHAR(255) NOT NULL, " +
                    "description TEXT, " +
                    "start_date DATETIME, " +
                    "deadline DATETIME, " +
                    "priority VARCHAR(32) NOT NULL DEFAULT 'MEDIUM', " +
                    "status VARCHAR(32) NOT NULL DEFAULT 'NOT_STARTED'" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");

            // 3. Tasks table
            stmt.execute("CREATE TABLE IF NOT EXISTS tasks (" +
                    "id VARCHAR(64) PRIMARY KEY, " +
                    "title VARCHAR(255) NOT NULL, " +
                    "description TEXT, " +
                    "subject_id VARCHAR(64), " +
                    "project_id VARCHAR(64), " +
                    "priority VARCHAR(32) NOT NULL DEFAULT 'MEDIUM', " +
                    "status VARCHAR(32) NOT NULL DEFAULT 'PENDING', " +
                    "created_date DATETIME, " +
                    "due_date DATETIME, " +
                    "estimated_minutes INT DEFAULT 30, " +
                    "actual_minutes INT DEFAULT 0, " +
                    "tags TEXT, " +
                    "completion_date DATETIME, " +
                    "CONSTRAINT fk_tasks_subject FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE SET NULL ON UPDATE CASCADE, " +
                    "CONSTRAINT fk_tasks_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE SET NULL ON UPDATE CASCADE" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");

            // 4. Study Sessions table
            stmt.execute("CREATE TABLE IF NOT EXISTS study_sessions (" +
                    "id VARCHAR(64) PRIMARY KEY, " +
                    "task_id VARCHAR(64), " +
                    "subject_id VARCHAR(64), " +
                    "start_time DATETIME, " +
                    "end_time DATETIME, " +
                    "duration_minutes INT DEFAULT 0, " +
                    "session_type VARCHAR(32) DEFAULT 'FOCUS', " +
                    "notes TEXT, " +
                    "CONSTRAINT fk_sessions_task FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE SET NULL ON UPDATE CASCADE, " +
                    "CONSTRAINT fk_sessions_subject FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE SET NULL ON UPDATE CASCADE" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");

            // 5. User Settings table
            stmt.execute("CREATE TABLE IF NOT EXISTS user_settings (" +
                    "id INT PRIMARY KEY, " +
                    "user_name VARCHAR(128) DEFAULT 'Scholar', " +
                    "focus_duration_minutes INT DEFAULT 25, " +
                    "short_break_duration_minutes INT DEFAULT 5, " +
                    "long_break_duration_minutes INT DEFAULT 15, " +
                    "weekly_study_hour_target INT DEFAULT 20, " +
                    "preferred_week_start_day INT DEFAULT 2, " +
                    "dark_mode TINYINT(1) DEFAULT 0, " +
                    "confirm_deletions TINYINT(1) DEFAULT 1" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");

            System.out.println("[MySQL] Connected successfully to " + getDatabaseInfo());
        } catch (SQLException e) {
            connected = false;
            System.err.println("[MySQL Warning] Could not connect to MySQL server at " + host + ":" + port +
                    " (" + e.getMessage() + "). Check that MySQL (e.g. XAMPP) is running. Operating in local cache mode.");
        }
    }

    // ==========================================
    // SUBJECTS CRUD
    // ==========================================

    public synchronized List<Subject> loadSubjects() {
        List<Subject> list = new ArrayList<>();
        if (!connected) return list;
        String sql = "SELECT id, name, description, color_hex, target_weekly_hours, total_study_minutes FROM subjects";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Subject s = new Subject();
                s.setId(rs.getString("id"));
                s.setName(rs.getString("name"));
                s.setDescription(rs.getString("description"));
                s.setColorHex(rs.getString("color_hex"));
                s.setTargetWeeklyHours(rs.getInt("target_weekly_hours"));
                s.setTotalStudyMinutes(rs.getLong("total_study_minutes"));
                list.add(s);
            }
        } catch (SQLException e) {
            System.err.println("[MySQL Error] Loading subjects: " + e.getMessage());
        }
        return list;
    }

    public synchronized void saveSubjects(List<Subject> subjects) {
        if (!connected) return;
        String sql = "REPLACE INTO subjects (id, name, description, color_hex, target_weekly_hours, total_study_minutes) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            if (subjects == null || subjects.isEmpty()) {
                try (Statement delStmt = conn.createStatement()) {
                    delStmt.execute("DELETE FROM subjects");
                }
            } else {
                StringBuilder deleteNotIn = new StringBuilder("DELETE FROM subjects WHERE id NOT IN (");
                for (int i = 0; i < subjects.size(); i++) {
                    deleteNotIn.append(i == 0 ? "?" : ", ?");
                }
                deleteNotIn.append(")");
                try (PreparedStatement psDel = conn.prepareStatement(deleteNotIn.toString())) {
                    for (int i = 0; i < subjects.size(); i++) {
                        psDel.setString(i + 1, subjects.get(i).getId());
                    }
                    psDel.executeUpdate();
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (Subject s : subjects) {
                    ps.setString(1, s.getId());
                    ps.setString(2, s.getName());
                    ps.setString(3, s.getDescription());
                    ps.setString(4, s.getColorHex());
                    ps.setInt(5, s.getTargetWeeklyHours());
                    ps.setLong(6, s.getTotalStudyMinutes());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            conn.commit();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            System.err.println("[MySQL Error] Saving subjects: " + e.getMessage());
        }
    }

    // ==========================================
    // PROJECTS CRUD
    // ==========================================

    public synchronized List<Project> loadProjects() {
        List<Project> list = new ArrayList<>();
        if (!connected) return list;
        String sql = "SELECT id, name, description, start_date, deadline, priority, status FROM projects";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Project p = new Project();
                p.setId(rs.getString("id"));
                p.setName(rs.getString("name"));
                p.setDescription(rs.getString("description"));
                p.setStartDate(rs.getTimestamp("start_date"));
                p.setDeadline(rs.getTimestamp("deadline"));
                p.setPriority(Priority.fromString(rs.getString("priority")));
                p.setStatus(ProjectStatus.fromString(rs.getString("status")));
                list.add(p);
            }
        } catch (SQLException e) {
            System.err.println("[MySQL Error] Loading projects: " + e.getMessage());
        }
        return list;
    }

    public synchronized void saveProjects(List<Project> projects) {
        if (!connected) return;
        String sql = "REPLACE INTO projects (id, name, description, start_date, deadline, priority, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            if (projects == null || projects.isEmpty()) {
                try (Statement delStmt = conn.createStatement()) {
                    delStmt.execute("DELETE FROM projects");
                }
            } else {
                StringBuilder deleteNotIn = new StringBuilder("DELETE FROM projects WHERE id NOT IN (");
                for (int i = 0; i < projects.size(); i++) {
                    deleteNotIn.append(i == 0 ? "?" : ", ?");
                }
                deleteNotIn.append(")");
                try (PreparedStatement psDel = conn.prepareStatement(deleteNotIn.toString())) {
                    for (int i = 0; i < projects.size(); i++) {
                        psDel.setString(i + 1, projects.get(i).getId());
                    }
                    psDel.executeUpdate();
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (Project p : projects) {
                    ps.setString(1, p.getId());
                    ps.setString(2, p.getName());
                    ps.setString(3, p.getDescription());
                    ps.setTimestamp(4, p.getStartDate() != null ? new Timestamp(p.getStartDate().getTime()) : null);
                    ps.setTimestamp(5, p.getDeadline() != null ? new Timestamp(p.getDeadline().getTime()) : null);
                    ps.setString(6, p.getPriority().name());
                    ps.setString(7, p.getStatus().name());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            conn.commit();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            System.err.println("[MySQL Error] Saving projects: " + e.getMessage());
        }
    }

    // ==========================================
    // TASKS CRUD
    // ==========================================

    public synchronized List<StudyTask> loadTasks() {
        List<StudyTask> list = new ArrayList<>();
        if (!connected) return list;
        String sql = "SELECT id, title, description, subject_id, project_id, priority, status, " +
                "created_date, due_date, estimated_minutes, actual_minutes, tags, completion_date FROM tasks";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                StudyTask t = new StudyTask();
                t.setId(rs.getString("id"));
                t.setTitle(rs.getString("title"));
                t.setDescription(rs.getString("description"));
                t.setSubjectId(rs.getString("subject_id"));
                t.setProjectId(rs.getString("project_id"));
                t.setPriority(Priority.fromString(rs.getString("priority")));
                t.setStatus(TaskStatus.fromString(rs.getString("status")));
                t.setCreatedDate(rs.getTimestamp("created_date"));
                t.setDueDate(rs.getTimestamp("due_date"));
                t.setEstimatedMinutes(rs.getInt("estimated_minutes"));
                t.setActualMinutes(rs.getInt("actual_minutes"));
                t.setCompletionDate(rs.getTimestamp("completion_date"));

                String tagsStr = rs.getString("tags");
                if (tagsStr != null && !tagsStr.trim().isEmpty()) {
                    t.setTags(new ArrayList<>(Arrays.asList(tagsStr.split(","))));
                }
                list.add(t);
            }
        } catch (SQLException e) {
            System.err.println("[MySQL Error] Loading tasks: " + e.getMessage());
        }
        return list;
    }

    public synchronized void saveTasks(List<StudyTask> tasks) {
        if (!connected) return;
        String sql = "REPLACE INTO tasks (id, title, description, subject_id, project_id, priority, status, " +
                "created_date, due_date, estimated_minutes, actual_minutes, tags, completion_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            if (tasks == null || tasks.isEmpty()) {
                try (Statement delStmt = conn.createStatement()) {
                    delStmt.execute("DELETE FROM tasks");
                }
            } else {
                StringBuilder deleteNotIn = new StringBuilder("DELETE FROM tasks WHERE id NOT IN (");
                for (int i = 0; i < tasks.size(); i++) {
                    deleteNotIn.append(i == 0 ? "?" : ", ?");
                }
                deleteNotIn.append(")");
                try (PreparedStatement psDel = conn.prepareStatement(deleteNotIn.toString())) {
                    for (int i = 0; i < tasks.size(); i++) {
                        psDel.setString(i + 1, tasks.get(i).getId());
                    }
                    psDel.executeUpdate();
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (StudyTask t : tasks) {
                    ps.setString(1, t.getId());
                    ps.setString(2, t.getTitle());
                    ps.setString(3, t.getDescription());
                    ps.setString(4, t.getSubjectId());
                    ps.setString(5, t.getProjectId());
                    ps.setString(6, t.getPriority().name());
                    ps.setString(7, t.getStatus().name());
                    ps.setTimestamp(8, t.getCreatedDate() != null ? new Timestamp(t.getCreatedDate().getTime()) : null);
                    ps.setTimestamp(9, t.getDueDate() != null ? new Timestamp(t.getDueDate().getTime()) : null);
                    ps.setInt(10, t.getEstimatedMinutes());
                    ps.setInt(11, t.getActualMinutes());

                    String tagsStr = (t.getTags() != null && !t.getTags().isEmpty())
                            ? String.join(",", t.getTags()) : "";
                    ps.setString(12, tagsStr);
                    ps.setTimestamp(13, t.getCompletionDate() != null ? new Timestamp(t.getCompletionDate().getTime()) : null);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            conn.commit();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            System.err.println("[MySQL Error] Saving tasks: " + e.getMessage());
        }
    }

    // ==========================================
    // STUDY SESSIONS CRUD
    // ==========================================

    public synchronized List<StudySession> loadSessions() {
        List<StudySession> list = new ArrayList<>();
        if (!connected) return list;
        String sql = "SELECT id, task_id, subject_id, start_time, end_time, duration_minutes, session_type, notes FROM study_sessions";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                StudySession s = new StudySession();
                s.setId(rs.getString("id"));
                s.setTaskId(rs.getString("task_id"));
                s.setSubjectId(rs.getString("subject_id"));
                s.setStartTime(rs.getTimestamp("start_time"));
                s.setEndTime(rs.getTimestamp("end_time"));
                s.setDurationMinutes(rs.getInt("duration_minutes"));
                s.setSessionType(rs.getString("session_type"));
                s.setNotes(rs.getString("notes"));
                list.add(s);
            }
        } catch (SQLException e) {
            System.err.println("[MySQL Error] Loading sessions: " + e.getMessage());
        }
        return list;
    }

    public synchronized void saveSessions(List<StudySession> sessions) {
        if (!connected) return;
        String sql = "REPLACE INTO study_sessions (id, task_id, subject_id, start_time, end_time, duration_minutes, session_type, notes) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            if (sessions == null || sessions.isEmpty()) {
                try (Statement delStmt = conn.createStatement()) {
                    delStmt.execute("DELETE FROM study_sessions");
                }
            } else {
                StringBuilder deleteNotIn = new StringBuilder("DELETE FROM study_sessions WHERE id NOT IN (");
                for (int i = 0; i < sessions.size(); i++) {
                    deleteNotIn.append(i == 0 ? "?" : ", ?");
                }
                deleteNotIn.append(")");
                try (PreparedStatement psDel = conn.prepareStatement(deleteNotIn.toString())) {
                    for (int i = 0; i < sessions.size(); i++) {
                        psDel.setString(i + 1, sessions.get(i).getId());
                    }
                    psDel.executeUpdate();
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (StudySession s : sessions) {
                    ps.setString(1, s.getId());
                    ps.setString(2, s.getTaskId());
                    ps.setString(3, s.getSubjectId());
                    ps.setTimestamp(4, s.getStartTime() != null ? new Timestamp(s.getStartTime().getTime()) : null);
                    ps.setTimestamp(5, s.getEndTime() != null ? new Timestamp(s.getEndTime().getTime()) : null);
                    ps.setInt(6, s.getDurationMinutes());
                    ps.setString(7, s.getSessionType());
                    ps.setString(8, s.getNotes());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            conn.commit();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            System.err.println("[MySQL Error] Saving sessions: " + e.getMessage());
        }
    }

    // ==========================================
    // USER SETTINGS CRUD
    // ==========================================

    public synchronized UserSettings loadSettings() {
        UserSettings settings = new UserSettings();
        if (!connected) return settings;
        String sql = "SELECT user_name, focus_duration_minutes, short_break_duration_minutes, " +
                "long_break_duration_minutes, weekly_study_hour_target, preferred_week_start_day, " +
                "dark_mode, confirm_deletions FROM user_settings WHERE id = 1";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                settings.setUserName(rs.getString("user_name"));
                settings.setFocusDurationMinutes(rs.getInt("focus_duration_minutes"));
                settings.setShortBreakDurationMinutes(rs.getInt("short_break_duration_minutes"));
                settings.setLongBreakDurationMinutes(rs.getInt("long_break_duration_minutes"));
                settings.setWeeklyStudyHourTarget(rs.getInt("weekly_study_hour_target"));
                settings.setPreferredWeekStartDay(rs.getInt("preferred_week_start_day"));
                settings.setDarkMode(rs.getInt("dark_mode") == 1);
                settings.setConfirmDeletions(rs.getInt("confirm_deletions") == 1);
            }
        } catch (SQLException e) {
            System.err.println("[MySQL Error] Loading settings: " + e.getMessage());
        }
        return settings;
    }

    public synchronized void saveSettings(UserSettings s) {
        if (!connected || s == null) return;
        String sql = "REPLACE INTO user_settings (id, user_name, focus_duration_minutes, " +
                "short_break_duration_minutes, long_break_duration_minutes, weekly_study_hour_target, " +
                "preferred_week_start_day, dark_mode, confirm_deletions) " +
                "VALUES (1, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getUserName());
            ps.setInt(2, s.getFocusDurationMinutes());
            ps.setInt(3, s.getShortBreakDurationMinutes());
            ps.setInt(4, s.getLongBreakDurationMinutes());
            ps.setInt(5, s.getWeeklyStudyHourTarget());
            ps.setInt(6, s.getPreferredWeekStartDay());
            ps.setInt(7, s.isDarkMode() ? 1 : 0);
            ps.setInt(8, s.isConfirmDeletions() ? 1 : 0);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[MySQL Error] Saving settings: " + e.getMessage());
        }
    }

    /**
     * Clear all records in MySQL database.
     */
    public synchronized void resetAllData() {
        if (!connected) return;
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM study_sessions;");
            stmt.execute("DELETE FROM tasks;");
            stmt.execute("DELETE FROM projects;");
            stmt.execute("DELETE FROM subjects;");
            stmt.execute("DELETE FROM user_settings;");
        } catch (SQLException e) {
            System.err.println("[MySQL Error] Resetting database: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        System.out.println("Testing MySQL Connection...");
        DatabaseManager db = DatabaseManager.getInstance();
        System.out.println("Target: " + db.getDatabaseInfo());
        System.out.println("Connected: " + db.isConnected());
    }
}
