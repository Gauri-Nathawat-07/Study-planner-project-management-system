package com.studyplanner.service;

import com.studyplanner.model.Subject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Aggregator service providing unified metrics and statistics for the dashboard and analytics.
 */
public class DashboardService {
    private final TaskService taskService;
    private final ProjectService projectService;
    private final SubjectService subjectService;
    private final StudySessionService studySessionService;

    public DashboardService(TaskService taskService, ProjectService projectService,
                            SubjectService subjectService, StudySessionService studySessionService) {
        this.taskService = taskService;
        this.projectService = projectService;
        this.subjectService = subjectService;
        this.studySessionService = studySessionService;
    }

    public int getTotalTasks() {
        return taskService.getAllTasks().size();
    }

    public int getPendingTasks() {
        return taskService.getPendingTasks().size();
    }

    public int getCompletedTasks() {
        return taskService.getCompletedTasks().size();
    }

    public int getOverdueTasks() {
        return taskService.getOverdueTasks().size();
    }

    public int getActiveProjects() {
        return projectService.getActiveProjectCount();
    }

    public int getTodayStudyMinutes() {
        return studySessionService.getTodayStudyMinutes();
    }

    public int getOverallCompletionPercentage() {
        int total = getTotalTasks();
        if (total == 0) return 0;
        return (int) Math.round(((double) getCompletedTasks() / total) * 100);
    }

    public Map<Subject, int[]> getSubjectProgress() {
        // Map: Subject -> int[]{completedTasks, totalTasks}
        Map<Subject, int[]> map = new HashMap<>();
        List<Subject> subjects = subjectService.getAllSubjects();
        for (Subject s : subjects) {
            int completed = 0;
            int total = 0;
            for (com.studyplanner.model.StudyTask t : taskService.getTasksBySubject(s.getId())) {
                total++;
                if (t.isCompleted()) completed++;
            }
            map.put(s, new int[]{completed, total});
        }
        return map;
    }
}
