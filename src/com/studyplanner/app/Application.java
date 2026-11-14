package com.studyplanner.app;

import com.studyplanner.model.UserSettings;
import com.studyplanner.service.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Central singleton orchestrating services, settings, and view notification listeners.
 */
public class Application {
    private static Application instance;

    private DataService dataService;
    private TaskService taskService;
    private ProjectService projectService;
    private SubjectService subjectService;
    private StudySessionService studySessionService;
    private DashboardService dashboardService;
    private UserSettings userSettings;

    private final List<Runnable> changeListeners = new ArrayList<>();

    private Application() {}

    public static synchronized Application getInstance() {
        if (instance == null) {
            instance = new Application();
        }
        return instance;
    }

    public synchronized void init() {
        dataService = new DataService();
        taskService = new TaskService(dataService);
        projectService = new ProjectService(dataService);
        subjectService = new SubjectService(dataService);
        studySessionService = new StudySessionService(dataService);
        dashboardService = new DashboardService(taskService, projectService, subjectService, studySessionService);
        userSettings = dataService.loadSettings();

        // Seed demo data if application is empty on first run
        seedInitialDataIfEmpty();
    }

    private void seedInitialDataIfEmpty() {
        if (taskService.getAllTasks().isEmpty()) {
            java.util.List<com.studyplanner.model.Subject> subs = subjectService.getAllSubjects();
            String mathId = subs.size() > 0 ? subs.get(0).getId() : null;
            String csId = subs.size() > 1 ? subs.get(1).getId() : null;

            // Project
            com.studyplanner.model.Project p1 = new com.studyplanner.model.Project(
                "Final Term Research",
                "Machine Learning survey paper and code reproduction",
                new java.util.Date(),
                com.studyplanner.util.DateUtils.addDays(new java.util.Date(), 14),
                com.studyplanner.model.Priority.HIGH
            );
            projectService.addProject(p1);

            // Tasks
            com.studyplanner.model.StudyTask t1 = new com.studyplanner.model.StudyTask(
                "Complete Calculus Problem Set #4",
                "Integrals by parts and trigonometric substitution questions 1 through 15",
                mathId,
                null,
                com.studyplanner.model.Priority.HIGH,
                new java.util.Date(), // Due today
                90
            );
            taskService.addTask(t1);

            com.studyplanner.model.StudyTask t2 = new com.studyplanner.model.StudyTask(
                "Implement Dijkstra's Algorithm in Java",
                "Write graph adjacency list representation and priority queue traversal",
                csId,
                p1.getId(),
                com.studyplanner.model.Priority.URGENT,
                com.studyplanner.util.DateUtils.addDays(new java.util.Date(), 2),
                120
            );
            taskService.addTask(t2);

            com.studyplanner.model.StudyTask t3 = new com.studyplanner.model.StudyTask(
                "Review Physics Thermodynamics Notes",
                "Carnot engine cycles and entropy definitions",
                subs.size() > 2 ? subs.get(2).getId() : null,
                null,
                com.studyplanner.model.Priority.MEDIUM,
                com.studyplanner.util.DateUtils.addDays(new java.util.Date(), 5),
                45
            );
            taskService.addTask(t3);

            // Sample completed focus session
            com.studyplanner.model.StudySession s1 = new com.studyplanner.model.StudySession(
                t1.getId(),
                mathId,
                new java.util.Date(),
                new java.util.Date(),
                25,
                "FOCUS",
                "Worked on Calculus integrals"
            );
            studySessionService.addSession(s1);
        }
    }

    public TaskService getTaskService() { return taskService; }
    public ProjectService getProjectService() { return projectService; }
    public SubjectService getSubjectService() { return subjectService; }
    public StudySessionService getStudySessionService() { return studySessionService; }
    public DashboardService getDashboardService() { return dashboardService; }
    public DataService getDataService() { return dataService; }
    public DatabaseManager getDatabaseManager() { return dataService != null ? dataService.getDatabaseManager() : DatabaseManager.getInstance(); }

    public synchronized UserSettings getUserSettings() { return userSettings; }
    public synchronized void updateUserSettings(UserSettings settings) {
        this.userSettings = settings;
        dataService.saveSettings(settings);
        notifyDataChanged();
    }

    public synchronized void addChangeListener(Runnable listener) {
        if (listener != null && !changeListeners.contains(listener)) {
            changeListeners.add(listener);
        }
    }

    public synchronized void notifyDataChanged() {
        for (Runnable r : new ArrayList<>(changeListeners)) {
            try {
                r.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void resetAllData() {
        dataService.resetAllData();
        init();
        notifyDataChanged();
    }
}
