package com.studyplanner.service;

import com.studyplanner.model.Project;
import com.studyplanner.model.ProjectStatus;
import com.studyplanner.model.StudyTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Service managing project entities and tracking completion progress.
 */
public class ProjectService {
    private final DataService dataService;
    private final List<Project> projects;

    public ProjectService(DataService dataService) {
        this.dataService = dataService;
        this.projects = new ArrayList<>(dataService.loadProjects());
    }

    public synchronized List<Project> getAllProjects() {
        return new ArrayList<>(projects);
    }

    public synchronized Project getProjectById(String id) {
        if (id == null) return null;
        for (Project p : projects) {
            if (id.equals(p.getId())) return p;
        }
        return null;
    }

    public synchronized void addProject(Project project) {
        if (project != null) {
            projects.add(project);
            saveAll();
        }
    }

    public synchronized void updateProject(Project project) {
        if (project == null) return;
        for (int i = 0; i < projects.size(); i++) {
            if (projects.get(i).getId().equals(project.getId())) {
                projects.set(i, project);
                saveAll();
                return;
            }
        }
    }

    public synchronized boolean deleteProject(String id) {
        if (id == null) return false;
        boolean removed = projects.removeIf(p -> id.equals(p.getId()));
        if (removed) {
            saveAll();
        }
        return removed;
    }

    public synchronized int calculateProgress(String projectId, TaskService taskService) {
        if (projectId == null || taskService == null) return 0;
        List<StudyTask> tasks = taskService.getTasksByProject(projectId);
        if (tasks.isEmpty()) return 0;

        int completed = 0;
        for (StudyTask t : tasks) {
            if (t.isCompleted()) completed++;
        }
        return (int) Math.round(((double) completed / tasks.size()) * 100);
    }

    public synchronized int getActiveProjectCount() {
        int count = 0;
        for (Project p : projects) {
            if (p.getStatus() != ProjectStatus.COMPLETED) {
                count++;
            }
        }
        return count;
    }

    public synchronized void saveAll() {
        dataService.saveProjects(projects);
    }
}
