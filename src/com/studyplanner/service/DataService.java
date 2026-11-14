package com.studyplanner.service;

import com.studyplanner.model.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Persistence service managing MySQL database operations with automated
 * local cache synchronization and migration.
 */
public class DataService {
    private static final String DATA_DIR = "data";
    private static final String TASKS_FILE = DATA_DIR + File.separator + "tasks.dat";
    private static final String PROJECTS_FILE = DATA_DIR + File.separator + "projects.dat";
    private static final String SUBJECTS_FILE = DATA_DIR + File.separator + "subjects.dat";
    private static final String SESSIONS_FILE = DATA_DIR + File.separator + "sessions.dat";
    private static final String SETTINGS_FILE = DATA_DIR + File.separator + "settings.dat";

    private final DatabaseManager dbManager;

    public DataService() {
        File dir = new File(DATA_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        this.dbManager = DatabaseManager.getInstance();
        syncWithDatabase();
    }

    public DatabaseManager getDatabaseManager() {
        return dbManager;
    }

    /**
     * Synchronize MySQL database with local cache.
     * If MySQL is connected and empty, migrates existing records to MySQL.
     * If MySQL has records, updates local cache.
     */
    private void syncWithDatabase() {
        if (!dbManager.isConnected()) {
            return;
        }

        try {
            // Subjects
            List<Subject> dbSubjects = dbManager.loadSubjects();
            if (dbSubjects.isEmpty()) {
                List<Subject> localSubjects = loadLocalSubjects();
                if (!localSubjects.isEmpty()) {
                    System.out.println("[MySQL Sync] Migrating " + localSubjects.size() + " subjects to MySQL...");
                    dbManager.saveSubjects(localSubjects);
                }
            } else {
                writeLocalFile(SUBJECTS_FILE, new ArrayList<>(dbSubjects));
            }

            // Projects
            List<Project> dbProjects = dbManager.loadProjects();
            if (dbProjects.isEmpty()) {
                List<Project> localProjects = loadLocalProjects();
                if (!localProjects.isEmpty()) {
                    System.out.println("[MySQL Sync] Migrating " + localProjects.size() + " projects to MySQL...");
                    dbManager.saveProjects(localProjects);
                }
            } else {
                writeLocalFile(PROJECTS_FILE, new ArrayList<>(dbProjects));
            }

            // Tasks
            List<StudyTask> dbTasks = dbManager.loadTasks();
            if (dbTasks.isEmpty()) {
                List<StudyTask> localTasks = loadLocalTasks();
                if (!localTasks.isEmpty()) {
                    System.out.println("[MySQL Sync] Migrating " + localTasks.size() + " tasks to MySQL...");
                    dbManager.saveTasks(localTasks);
                }
            } else {
                writeLocalFile(TASKS_FILE, new ArrayList<>(dbTasks));
            }

            // Study Sessions
            List<StudySession> dbSessions = dbManager.loadSessions();
            if (dbSessions.isEmpty()) {
                List<StudySession> localSessions = loadLocalSessions();
                if (!localSessions.isEmpty()) {
                    System.out.println("[MySQL Sync] Migrating " + localSessions.size() + " sessions to MySQL...");
                    dbManager.saveSessions(localSessions);
                }
            } else {
                writeLocalFile(SESSIONS_FILE, new ArrayList<>(dbSessions));
            }

            // Settings
            UserSettings localSettings = loadLocalSettings();
            if (localSettings != null) {
                dbManager.saveSettings(localSettings);
            }
        } catch (Exception e) {
            System.err.println("[MySQL Sync Notice] " + e.getMessage());
        }
    }

    // ==========================================
    // DATA ACCESS OPERATIONS
    // ==========================================

    public synchronized List<StudyTask> loadTasks() {
        if (dbManager.isConnected()) {
            List<StudyTask> list = dbManager.loadTasks();
            if (!list.isEmpty()) {
                writeLocalFile(TASKS_FILE, new ArrayList<>(list));
                return list;
            }
        }
        return loadLocalTasks();
    }

    public synchronized void saveTasks(List<StudyTask> tasks) {
        if (dbManager.isConnected()) {
            dbManager.saveTasks(tasks);
        }
        writeLocalFile(TASKS_FILE, new ArrayList<>(tasks));
    }

    public synchronized List<Project> loadProjects() {
        if (dbManager.isConnected()) {
            List<Project> list = dbManager.loadProjects();
            if (!list.isEmpty()) {
                writeLocalFile(PROJECTS_FILE, new ArrayList<>(list));
                return list;
            }
        }
        return loadLocalProjects();
    }

    public synchronized void saveProjects(List<Project> projects) {
        if (dbManager.isConnected()) {
            dbManager.saveProjects(projects);
        }
        writeLocalFile(PROJECTS_FILE, new ArrayList<>(projects));
    }

    public synchronized List<Subject> loadSubjects() {
        if (dbManager.isConnected()) {
            List<Subject> list = dbManager.loadSubjects();
            if (!list.isEmpty()) {
                writeLocalFile(SUBJECTS_FILE, new ArrayList<>(list));
                return list;
            }
        }
        return loadLocalSubjects();
    }

    public synchronized void saveSubjects(List<Subject> subjects) {
        if (dbManager.isConnected()) {
            dbManager.saveSubjects(subjects);
        }
        writeLocalFile(SUBJECTS_FILE, new ArrayList<>(subjects));
    }

    public synchronized List<StudySession> loadSessions() {
        if (dbManager.isConnected()) {
            List<StudySession> list = dbManager.loadSessions();
            if (!list.isEmpty()) {
                writeLocalFile(SESSIONS_FILE, new ArrayList<>(list));
                return list;
            }
        }
        return loadLocalSessions();
    }

    public synchronized void saveSessions(List<StudySession> sessions) {
        if (dbManager.isConnected()) {
            dbManager.saveSessions(sessions);
        }
        writeLocalFile(SESSIONS_FILE, new ArrayList<>(sessions));
    }

    public synchronized UserSettings loadSettings() {
        if (dbManager.isConnected()) {
            UserSettings s = dbManager.loadSettings();
            if (s != null && s.getUserName() != null) {
                writeLocalFile(SETTINGS_FILE, s);
                return s;
            }
        }
        UserSettings local = loadLocalSettings();
        return local != null ? local : new UserSettings();
    }

    public synchronized void saveSettings(UserSettings settings) {
        if (dbManager.isConnected()) {
            dbManager.saveSettings(settings);
        }
        writeLocalFile(SETTINGS_FILE, settings != null ? settings : new UserSettings());
    }

    public synchronized void resetAllData() {
        if (dbManager.isConnected()) {
            dbManager.resetAllData();
        }
        saveTasks(new ArrayList<>());
        saveProjects(new ArrayList<>());
        saveSubjects(new ArrayList<>());
        saveSessions(new ArrayList<>());
        saveSettings(new UserSettings());
    }

    // ==========================================
    // LOCAL CACHE FILE HELPERS
    // ==========================================

    @SuppressWarnings("unchecked")
    private List<StudyTask> loadLocalTasks() {
        Object obj = readLocalFile(TASKS_FILE);
        if (obj instanceof List) return (List<StudyTask>) obj;
        return new ArrayList<>();
    }

    @SuppressWarnings("unchecked")
    private List<Project> loadLocalProjects() {
        Object obj = readLocalFile(PROJECTS_FILE);
        if (obj instanceof List) return (List<Project>) obj;
        return new ArrayList<>();
    }

    @SuppressWarnings("unchecked")
    private List<Subject> loadLocalSubjects() {
        Object obj = readLocalFile(SUBJECTS_FILE);
        if (obj instanceof List) return (List<Subject>) obj;
        return new ArrayList<>();
    }

    @SuppressWarnings("unchecked")
    private List<StudySession> loadLocalSessions() {
        Object obj = readLocalFile(SESSIONS_FILE);
        if (obj instanceof List) return (List<StudySession>) obj;
        return new ArrayList<>();
    }

    private UserSettings loadLocalSettings() {
        Object obj = readLocalFile(SETTINGS_FILE);
        if (obj instanceof UserSettings) return (UserSettings) obj;
        return null;
    }

    private Object readLocalFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists() || file.length() == 0) return null;
        try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)))) {
            return ois.readObject();
        } catch (Exception ignored) {
            return null;
        }
    }

    private void writeLocalFile(String filePath, Object obj) {
        File file = new File(filePath);
        File tempFile = new File(filePath + ".tmp");
        try {
            if (file.getParentFile() != null && !file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            try (ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(tempFile)))) {
                oos.writeObject(obj);
                oos.flush();
            }
            if (file.exists()) file.delete();
            tempFile.renameTo(file);
        } catch (IOException ignored) {}
    }
}
