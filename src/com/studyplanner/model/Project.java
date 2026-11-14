package com.studyplanner.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Model representing an academic or personal project with milestone tasks.
 */
public class Project implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private String description;
    private Date startDate;
    private Date deadline;
    private Priority priority;
    private ProjectStatus status;
    private List<String> taskIds;

    public Project() {
        this.id = UUID.randomUUID().toString();
        this.priority = Priority.MEDIUM;
        this.status = ProjectStatus.NOT_STARTED;
        this.startDate = new Date();
        this.taskIds = new ArrayList<>();
    }

    public Project(String name, String description, Date startDate, Date deadline, Priority priority) {
        this();
        this.name = name;
        this.description = description;
        this.startDate = startDate != null ? startDate : new Date();
        this.deadline = deadline;
        this.priority = priority != null ? priority : Priority.MEDIUM;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description != null ? description : ""; }
    public void setDescription(String description) { this.description = description; }

    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }

    public Date getDeadline() { return deadline; }
    public void setDeadline(Date deadline) { this.deadline = deadline; }

    public Priority getPriority() { return priority != null ? priority : Priority.MEDIUM; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public ProjectStatus getStatus() { return status != null ? status : ProjectStatus.NOT_STARTED; }
    public void setStatus(ProjectStatus status) { this.status = status; }

    public List<String> getTaskIds() {
        if (taskIds == null) taskIds = new ArrayList<>();
        return taskIds;
    }
    public void setTaskIds(List<String> taskIds) { this.taskIds = taskIds; }
}
