package com.studyplanner.service;

import com.studyplanner.model.Priority;
import com.studyplanner.model.StudyTask;
import com.studyplanner.model.TaskStatus;
import com.studyplanner.util.DateUtils;

import java.util.*;

/**
 * Service managing task CRUD, search, filtering, and status transitions.
 */
public class TaskService {
    private final DataService dataService;
    private final List<StudyTask> tasks;

    public TaskService(DataService dataService) {
        this.dataService = dataService;
        this.tasks = new ArrayList<>(dataService.loadTasks());
    }

    public synchronized List<StudyTask> getAllTasks() {
        return new ArrayList<>(tasks);
    }

    public synchronized StudyTask getTaskById(String id) {
        if (id == null) return null;
        for (StudyTask t : tasks) {
            if (id.equals(t.getId())) return t;
        }
        return null;
    }

    public synchronized void addTask(StudyTask task) {
        if (task != null) {
            tasks.add(task);
            saveAll();
        }
    }

    public synchronized void updateTask(StudyTask task) {
        if (task == null) return;
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).getId().equals(task.getId())) {
                tasks.set(i, task);
                saveAll();
                return;
            }
        }
    }

    public synchronized boolean deleteTask(String id) {
        if (id == null) return false;
        boolean removed = tasks.removeIf(t -> id.equals(t.getId()));
        if (removed) {
            saveAll();
        }
        return removed;
    }

    public synchronized void markCompleted(String id) {
        StudyTask t = getTaskById(id);
        if (t != null) {
            t.setStatus(TaskStatus.COMPLETED);
            saveAll();
        }
    }

    public synchronized void reopenTask(String id) {
        StudyTask t = getTaskById(id);
        if (t != null) {
            t.setStatus(TaskStatus.PENDING);
            saveAll();
        }
    }

    public synchronized List<StudyTask> getTasksBySubject(String subjectId) {
        List<StudyTask> result = new ArrayList<>();
        if (subjectId == null) return result;
        for (StudyTask t : tasks) {
            if (subjectId.equals(t.getSubjectId())) {
                result.add(t);
            }
        }
        return result;
    }

    public synchronized List<StudyTask> getTasksByProject(String projectId) {
        List<StudyTask> result = new ArrayList<>();
        if (projectId == null) return result;
        for (StudyTask t : tasks) {
            if (projectId.equals(t.getProjectId())) {
                result.add(t);
            }
        }
        return result;
    }

    public synchronized List<StudyTask> getTasksDueToday() {
        List<StudyTask> result = new ArrayList<>();
        for (StudyTask t : tasks) {
            if (t.getDueDate() != null && DateUtils.isDueToday(t.getDueDate())) {
                result.add(t);
            }
        }
        return result;
    }

    public synchronized List<StudyTask> getOverdueTasks() {
        List<StudyTask> result = new ArrayList<>();
        for (StudyTask t : tasks) {
            if (!t.isCompleted() && t.getDueDate() != null && DateUtils.isOverdue(t.getDueDate())) {
                result.add(t);
            }
        }
        return result;
    }

    public synchronized List<StudyTask> getPendingTasks() {
        List<StudyTask> result = new ArrayList<>();
        for (StudyTask t : tasks) {
            if (!t.isCompleted()) {
                result.add(t);
            }
        }
        return result;
    }

    public synchronized List<StudyTask> getCompletedTasks() {
        List<StudyTask> result = new ArrayList<>();
        for (StudyTask t : tasks) {
            if (t.isCompleted()) {
                result.add(t);
            }
        }
        return result;
    }

    public synchronized List<StudyTask> filterTasks(String query, TaskStatus statusFilter,
                                                   Priority priorityFilter, String subjectIdFilter,
                                                   String sortBy) {
        List<StudyTask> result = new ArrayList<>();
        String q = query != null ? query.trim().toLowerCase() : "";

        for (StudyTask t : tasks) {
            if (!q.isEmpty()) {
                boolean matchTitle = t.getTitle() != null && t.getTitle().toLowerCase().contains(q);
                boolean matchDesc = t.getDescription() != null && t.getDescription().toLowerCase().contains(q);
                if (!matchTitle && !matchDesc) continue;
            }
            if (statusFilter != null && t.getStatus() != statusFilter) {
                continue;
            }
            if (priorityFilter != null && t.getPriority() != priorityFilter) {
                continue;
            }
            if (subjectIdFilter != null && !subjectIdFilter.isEmpty() && !subjectIdFilter.equals("ALL")) {
                if (!subjectIdFilter.equals(t.getSubjectId())) {
                    continue;
                }
            }
            result.add(t);
        }

        // Sorting
        if ("Due Date".equalsIgnoreCase(sortBy)) {
            result.sort((a, b) -> {
                if (a.getDueDate() == null && b.getDueDate() == null) return 0;
                if (a.getDueDate() == null) return 1;
                if (b.getDueDate() == null) return -1;
                return a.getDueDate().compareTo(b.getDueDate());
            });
        } else if ("Priority".equalsIgnoreCase(sortBy)) {
            result.sort((a, b) -> Integer.compare(b.getPriority().ordinal(), a.getPriority().ordinal()));
        } else if ("Title".equalsIgnoreCase(sortBy)) {
            result.sort((a, b) -> String.valueOf(a.getTitle()).compareToIgnoreCase(String.valueOf(b.getTitle())));
        } else {
            // Default Created Date desc
            result.sort((a, b) -> {
                if (a.getCreatedDate() == null || b.getCreatedDate() == null) return 0;
                return b.getCreatedDate().compareTo(a.getCreatedDate());
            });
        }

        return result;
    }

    public synchronized void unassignSubject(String subjectId) {
        boolean changed = false;
        for (StudyTask t : tasks) {
            if (subjectId.equals(t.getSubjectId())) {
                t.setSubjectId(null);
                changed = true;
            }
        }
        if (changed) saveAll();
    }

    public synchronized void unassignProject(String projectId) {
        boolean changed = false;
        for (StudyTask t : tasks) {
            if (projectId.equals(t.getProjectId())) {
                t.setProjectId(null);
                changed = true;
            }
        }
        if (changed) saveAll();
    }

    public synchronized void saveAll() {
        dataService.saveTasks(tasks);
    }
}
