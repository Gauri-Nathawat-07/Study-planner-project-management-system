package com.studyplanner.ui.tasks;

import com.studyplanner.app.Application;
import com.studyplanner.model.Priority;
import com.studyplanner.model.Project;
import com.studyplanner.model.StudyTask;
import com.studyplanner.model.Subject;
import com.studyplanner.model.TaskStatus;
import com.studyplanner.ui.components.RoundedButton;
import com.studyplanner.util.ColorPalette;
import com.studyplanner.util.DateUtils;
import com.studyplanner.util.FontUtils;
import com.studyplanner.util.ValidationUtils;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Modal AWT Dialog for creating and editing tasks with complete input validation.
 */
public class TaskDialog extends Dialog {
    private final Frame parent;
    private final StudyTask editingTask;
    private boolean saved = false;

    private TextField titleField;
    private TextArea descriptionArea;
    private Choice subjectChoice;
    private Choice projectChoice;
    private Choice priorityChoice;
    private Choice statusChoice;
    private TextField dueDateField; // yyyy-MM-dd
    private TextField estimatedMinutesField;
    private Label errorLabel;

    private final List<Subject> subjects;
    private final List<Project> projects;

    public TaskDialog(Frame parent, StudyTask taskToEdit) {
        super(parent, taskToEdit == null ? "Create New Task" : "Edit Task", true);
        this.parent = parent;
        this.editingTask = taskToEdit;
        this.subjects = Application.getInstance().getSubjectService().getAllSubjects();
        this.projects = Application.getInstance().getProjectService().getAllProjects();

        setLayout(new BorderLayout(10, 10));
        setBackground(ColorPalette.BG);
        setSize(480, 560);
        setLocationRelativeTo(parent);

        add(buildFormPanel(), BorderLayout.CENTER);
        add(buildButtonPanel(), BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
    }

    private Panel buildFormPanel() {
        Panel panel = new Panel(new GridBagLayout());
        panel.setBackground(ColorPalette.CARD_BG);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(6, 18, 6, 18);
        c.weightx = 1.0;

        int row = 0;

        // Title
        c.gridy = row++;
        Label lblTitle = new Label("Task Title *");
        lblTitle.setFont(FontUtils.bold(12));
        lblTitle.setForeground(ColorPalette.TEXT_PRIMARY);
        panel.add(lblTitle, c);

        c.gridy = row++;
        titleField = new TextField();
        titleField.setFont(FontUtils.regular(13));
        if (editingTask != null && editingTask.getTitle() != null) {
            titleField.setText(editingTask.getTitle());
        }
        panel.add(titleField, c);

        // Subject & Project Row
        c.gridy = row++;
        Panel rowSubjProj = new Panel(new GridLayout(1, 2, 12, 0));
        rowSubjProj.setBackground(ColorPalette.CARD_BG);

        Panel pSubj = new Panel(new BorderLayout(0, 4));
        pSubj.setBackground(ColorPalette.CARD_BG);
        Label lblSubj = new Label("Subject");
        lblSubj.setFont(FontUtils.bold(12));
        pSubj.add(lblSubj, BorderLayout.NORTH);
        subjectChoice = new Choice();
        subjectChoice.add("None");
        int selSubjIdx = 0;
        for (int i = 0; i < subjects.size(); i++) {
            Subject s = subjects.get(i);
            subjectChoice.add(s.getName());
            if (editingTask != null && s.getId().equals(editingTask.getSubjectId())) {
                selSubjIdx = i + 1;
            }
        }
        subjectChoice.select(selSubjIdx);
        pSubj.add(subjectChoice, BorderLayout.CENTER);
        rowSubjProj.add(pSubj);

        Panel pProj = new Panel(new BorderLayout(0, 4));
        pProj.setBackground(ColorPalette.CARD_BG);
        Label lblProj = new Label("Project");
        lblProj.setFont(FontUtils.bold(12));
        pProj.add(lblProj, BorderLayout.NORTH);
        projectChoice = new Choice();
        projectChoice.add("None");
        int selProjIdx = 0;
        for (int i = 0; i < projects.size(); i++) {
            Project p = projects.get(i);
            projectChoice.add(p.getName());
            if (editingTask != null && p.getId().equals(editingTask.getProjectId())) {
                selProjIdx = i + 1;
            }
        }
        projectChoice.select(selProjIdx);
        pProj.add(projectChoice, BorderLayout.CENTER);
        rowSubjProj.add(pProj);
        panel.add(rowSubjProj, c);

        // Priority & Status Row
        c.gridy = row++;
        Panel rowPrioStat = new Panel(new GridLayout(1, 2, 12, 0));
        rowPrioStat.setBackground(ColorPalette.CARD_BG);

        Panel pPrio = new Panel(new BorderLayout(0, 4));
        pPrio.setBackground(ColorPalette.CARD_BG);
        Label lblPrio = new Label("Priority");
        lblPrio.setFont(FontUtils.bold(12));
        pPrio.add(lblPrio, BorderLayout.NORTH);
        priorityChoice = new Choice();
        for (Priority pr : Priority.values()) {
            priorityChoice.add(pr.getDisplayName());
        }
        if (editingTask != null) {
            priorityChoice.select(editingTask.getPriority().getDisplayName());
        } else {
            priorityChoice.select(Priority.MEDIUM.getDisplayName());
        }
        pPrio.add(priorityChoice, BorderLayout.CENTER);
        rowPrioStat.add(pPrio);

        Panel pStat = new Panel(new BorderLayout(0, 4));
        pStat.setBackground(ColorPalette.CARD_BG);
        Label lblStat = new Label("Status");
        lblStat.setFont(FontUtils.bold(12));
        pStat.add(lblStat, BorderLayout.NORTH);
        statusChoice = new Choice();
        for (TaskStatus st : TaskStatus.values()) {
            statusChoice.add(st.getDisplayName());
        }
        if (editingTask != null) {
            statusChoice.select(editingTask.getStatus().getDisplayName());
        } else {
            statusChoice.select(TaskStatus.PENDING.getDisplayName());
        }
        pStat.add(statusChoice, BorderLayout.CENTER);
        rowPrioStat.add(pStat);
        panel.add(rowPrioStat, c);

        // Due Date & Estimated Minutes Row
        c.gridy = row++;
        Panel rowDateMin = new Panel(new GridLayout(1, 2, 12, 0));
        rowDateMin.setBackground(ColorPalette.CARD_BG);

        Panel pDate = new Panel(new BorderLayout(0, 4));
        pDate.setBackground(ColorPalette.CARD_BG);
        Label lblDate = new Label("Due Date (YYYY-MM-DD)");
        lblDate.setFont(FontUtils.bold(12));
        pDate.add(lblDate, BorderLayout.NORTH);
        dueDateField = new TextField();
        if (editingTask != null && editingTask.getDueDate() != null) {
            dueDateField.setText(DateUtils.formatISO(editingTask.getDueDate()));
        } else {
            dueDateField.setText(DateUtils.formatISO(new Date()));
        }
        pDate.add(dueDateField, BorderLayout.CENTER);
        rowDateMin.add(pDate);

        Panel pMin = new Panel(new BorderLayout(0, 4));
        pMin.setBackground(ColorPalette.CARD_BG);
        Label lblMin = new Label("Est. Minutes");
        lblMin.setFont(FontUtils.bold(12));
        pMin.add(lblMin, BorderLayout.NORTH);
        estimatedMinutesField = new TextField();
        estimatedMinutesField.setText(editingTask != null ? String.valueOf(editingTask.getEstimatedMinutes()) : "45");
        pMin.add(estimatedMinutesField, BorderLayout.CENTER);
        rowDateMin.add(pMin);
        panel.add(rowDateMin, c);

        // Description
        c.gridy = row++;
        Label lblDesc = new Label("Description");
        lblDesc.setFont(FontUtils.bold(12));
        panel.add(lblDesc, c);

        c.gridy = row++;
        descriptionArea = new TextArea("", 4, 30, TextArea.SCROLLBARS_VERTICAL_ONLY);
        if (editingTask != null && editingTask.getDescription() != null) {
            descriptionArea.setText(editingTask.getDescription());
        }
        panel.add(descriptionArea, c);

        // Error message label
        c.gridy = row++;
        errorLabel = new Label("");
        errorLabel.setFont(FontUtils.bold(11));
        errorLabel.setForeground(ColorPalette.DANGER);
        panel.add(errorLabel, c);

        return panel;
    }

    private Panel buildButtonPanel() {
        Panel panel = new Panel(new FlowLayout(FlowLayout.RIGHT, 14, 12));
        panel.setBackground(ColorPalette.BG);

        RoundedButton cancelBtn = new RoundedButton("Cancel", Color.WHITE, ColorPalette.TEXT_PRIMARY, 8);
        cancelBtn.addActionListener(e -> dispose());
        panel.add(cancelBtn);

        RoundedButton saveBtn = new RoundedButton("Save Task", ColorPalette.PRIMARY, Color.WHITE, 8);
        saveBtn.addActionListener(e -> onSave());
        panel.add(saveBtn);

        return panel;
    }

    private void onSave() {
        String title = titleField.getText().trim();
        if (ValidationUtils.isBlank(title)) {
            errorLabel.setText("Please enter a task title.");
            return;
        }

        Date dueDate = null;
        String dateStr = dueDateField.getText().trim();
        if (!dateStr.isEmpty()) {
            dueDate = DateUtils.parseISO(dateStr);
            if (dueDate == null) {
                errorLabel.setText("Invalid date format. Please use YYYY-MM-DD.");
                return;
            }
        }

        int estMin = ValidationUtils.parsePositiveInt(estimatedMinutesField.getText(), 30);

        String chosenSubjectId = null;
        int subIdx = subjectChoice.getSelectedIndex();
        if (subIdx > 0 && subIdx <= subjects.size()) {
            chosenSubjectId = subjects.get(subIdx - 1).getId();
        }

        String chosenProjectId = null;
        int projIdx = projectChoice.getSelectedIndex();
        if (projIdx > 0 && projIdx <= projects.size()) {
            chosenProjectId = projects.get(projIdx - 1).getId();
        }

        Priority prio = Priority.fromString(priorityChoice.getSelectedItem());
        TaskStatus stat = TaskStatus.fromString(statusChoice.getSelectedItem());
        String desc = descriptionArea.getText().trim();

        if (editingTask == null) {
            StudyTask newTask = new StudyTask(title, desc, chosenSubjectId, chosenProjectId, prio, dueDate, estMin);
            newTask.setStatus(stat);
            Application.getInstance().getTaskService().addTask(newTask);
        } else {
            editingTask.setTitle(title);
            editingTask.setDescription(desc);
            editingTask.setSubjectId(chosenSubjectId);
            editingTask.setProjectId(chosenProjectId);
            editingTask.setPriority(prio);
            editingTask.setStatus(stat);
            editingTask.setDueDate(dueDate);
            editingTask.setEstimatedMinutes(estMin);
            Application.getInstance().getTaskService().updateTask(editingTask);
        }

        saved = true;
        Application.getInstance().notifyDataChanged();
        dispose();
    }

    public boolean isSaved() {
        return saved;
    }
}
