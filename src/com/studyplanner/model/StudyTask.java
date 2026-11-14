package com.studyplanner.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Core model representing a student's task or assignment.
 */
public class StudyTask implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String title;
    private String description;
    private String subjectId;
    private String projectId;
    private Priority priority;
    private TaskStatus status;
    private Date createdDate;
    private Date dueDate;
    private int estimatedMinutes;
    private int actualMinutes;
    private List<String> tags;
    private Date completionDate;

    public StudyTask() {
        this.id = UUID.randomUUID().toString();
        this.priority = Priority.MEDIUM;
        this.status = TaskStatus.PENDING;
        this.createdDate = new Date();
        this.tags = new ArrayList<>();
        this.estimatedMinutes = 30;
    }

    public StudyTask(String title, String description, String subjectId, String projectId,
                     Priority priority, Date dueDate, int estimatedMinutes) {
        this();
        this.title = title;
        this.description = description;
        this.subjectId = subjectId;
        this.projectId = projectId;
        this.priority = priority != null ? priority : Priority.MEDIUM;
        this.dueDate = dueDate;
        this.estimatedMinutes = estimatedMinutes > 0 ? estimatedMinutes : 30;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description != null ? description : ""; }
    public void setDescription(String description) { this.description = description; }

    public String getSubjectId() { return subjectId; }
    public void setSubjectId(String subjectId) { this.subjectId = subjectId; }

    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }

    public Priority getPriority() { return priority != null ? priority : Priority.MEDIUM; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public TaskStatus getStatus() { return status != null ? status : TaskStatus.PENDING; }
    public void setStatus(TaskStatus status) {
        this.status = status;
        if (status == TaskStatus.COMPLETED && this.completionDate == null) {
            this.completionDate = new Date();
        } else if (status != TaskStatus.COMPLETED) {
            this.completionDate = null;
        }
    }

    public Date getCreatedDate() { return createdDate; }
    public void setCreatedDate(Date createdDate) { this.createdDate = createdDate; }

    public Date getDueDate() { return dueDate; }
    public void setDueDate(Date dueDate) { this.dueDate = dueDate; }

    public int getEstimatedMinutes() { return estimatedMinutes; }
    public void setEstimatedMinutes(int estimatedMinutes) { this.estimatedMinutes = estimatedMinutes; }

    public int getActualMinutes() { return actualMinutes; }
    public void setActualMinutes(int actualMinutes) { this.actualMinutes = actualMinutes; }

    public List<String> getTags() {
        if (tags == null) tags = new ArrayList<>();
        return tags;
    }
    public void setTags(List<String> tags) { this.tags = tags; }

    public Date getCompletionDate() { return completionDate; }
    public void setCompletionDate(Date completionDate) { this.completionDate = completionDate; }

    public boolean isCompleted() {
        return status == TaskStatus.COMPLETED;
    }
}
