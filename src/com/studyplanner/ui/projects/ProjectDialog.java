package com.studyplanner.ui.projects;

import com.studyplanner.app.Application;
import com.studyplanner.model.Priority;
import com.studyplanner.model.Project;
import com.studyplanner.model.ProjectStatus;
import com.studyplanner.ui.components.RoundedButton;
import com.studyplanner.util.ColorPalette;
import com.studyplanner.util.DateUtils;
import com.studyplanner.util.FontUtils;
import com.studyplanner.util.ValidationUtils;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Date;

/**
 * Modal AWT dialog for creating and modifying project milestones.
 */
public class ProjectDialog extends Dialog {
    private final Project projectToEdit;
    private boolean saved = false;

    private TextField nameField;
    private TextArea descArea;
    private TextField startDateField;
    private TextField deadlineField;
    private Choice priorityChoice;
    private Choice statusChoice;
    private Label errorLabel;

    public ProjectDialog(Frame parent, Project projectToEdit) {
        super(parent, projectToEdit == null ? "Create New Project" : "Edit Project", true);
        this.projectToEdit = projectToEdit;

        setLayout(new BorderLayout(10, 10));
        setBackground(ColorPalette.BG);
        setSize(460, 500);
        setLocationRelativeTo(parent);

        add(buildForm(), BorderLayout.CENTER);
        add(buildButtons(), BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
    }

    private Panel buildForm() {
        Panel panel = new Panel(new GridBagLayout());
        panel.setBackground(ColorPalette.CARD_BG);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(6, 18, 6, 18);
        c.weightx = 1.0;
        int row = 0;

        // Name
        c.gridy = row++;
        Label lblName = new Label("Project Name *");
        lblName.setFont(FontUtils.bold(12));
        panel.add(lblName, c);

        c.gridy = row++;
        nameField = new TextField();
        nameField.setFont(FontUtils.regular(13));
        if (projectToEdit != null && projectToEdit.getName() != null) {
            nameField.setText(projectToEdit.getName());
        }
        panel.add(nameField, c);

        // Dates Row
        c.gridy = row++;
        Panel dateRow = new Panel(new GridLayout(1, 2, 12, 0));
        dateRow.setBackground(ColorPalette.CARD_BG);

        Panel pStart = new Panel(new BorderLayout(0, 4));
        pStart.setBackground(ColorPalette.CARD_BG);
        Label lblStart = new Label("Start Date (YYYY-MM-DD)");
        lblStart.setFont(FontUtils.bold(11));
        pStart.add(lblStart, BorderLayout.NORTH);
        startDateField = new TextField();
        startDateField.setText(projectToEdit != null && projectToEdit.getStartDate() != null ?
                DateUtils.formatISO(projectToEdit.getStartDate()) : DateUtils.formatISO(new Date()));
        pStart.add(startDateField, BorderLayout.CENTER);
        dateRow.add(pStart);

        Panel pDead = new Panel(new BorderLayout(0, 4));
        pDead.setBackground(ColorPalette.CARD_BG);
        Label lblDead = new Label("Deadline (YYYY-MM-DD)");
        lblDead.setFont(FontUtils.bold(11));
        pDead.add(lblDead, BorderLayout.NORTH);
        deadlineField = new TextField();
        deadlineField.setText(projectToEdit != null && projectToEdit.getDeadline() != null ?
                DateUtils.formatISO(projectToEdit.getDeadline()) : DateUtils.formatISO(DateUtils.addDays(new Date(), 14)));
        pDead.add(deadlineField, BorderLayout.CENTER);
        dateRow.add(pDead);
        panel.add(dateRow, c);

        // Priority & Status Row
        c.gridy = row++;
        Panel optRow = new Panel(new GridLayout(1, 2, 12, 0));
        optRow.setBackground(ColorPalette.CARD_BG);

        Panel pPrio = new Panel(new BorderLayout(0, 4));
        pPrio.setBackground(ColorPalette.CARD_BG);
        Label lblPrio = new Label("Priority");
        lblPrio.setFont(FontUtils.bold(11));
        pPrio.add(lblPrio, BorderLayout.NORTH);
        priorityChoice = new Choice();
        for (Priority p : Priority.values()) priorityChoice.add(p.getDisplayName());
        if (projectToEdit != null) priorityChoice.select(projectToEdit.getPriority().getDisplayName());
        pPrio.add(priorityChoice, BorderLayout.CENTER);
        optRow.add(pPrio);

        Panel pStat = new Panel(new BorderLayout(0, 4));
        pStat.setBackground(ColorPalette.CARD_BG);
        Label lblStat = new Label("Status");
        lblStat.setFont(FontUtils.bold(11));
        pStat.add(lblStat, BorderLayout.NORTH);
        statusChoice = new Choice();
        for (ProjectStatus ps : ProjectStatus.values()) statusChoice.add(ps.getDisplayName());
        if (projectToEdit != null) statusChoice.select(projectToEdit.getStatus().getDisplayName());
        pStat.add(statusChoice, BorderLayout.CENTER);
        optRow.add(pStat);
        panel.add(optRow, c);

        // Description
        c.gridy = row++;
        Label lblDesc = new Label("Description");
        lblDesc.setFont(FontUtils.bold(12));
        panel.add(lblDesc, c);

        c.gridy = row++;
        descArea = new TextArea("", 4, 30, TextArea.SCROLLBARS_VERTICAL_ONLY);
        if (projectToEdit != null && projectToEdit.getDescription() != null) {
            descArea.setText(projectToEdit.getDescription());
        }
        panel.add(descArea, c);

        // Error message
        c.gridy = row++;
        errorLabel = new Label("");
        errorLabel.setFont(FontUtils.bold(11));
        errorLabel.setForeground(ColorPalette.DANGER);
        panel.add(errorLabel, c);

        return panel;
    }

    private Panel buildButtons() {
        Panel panel = new Panel(new FlowLayout(FlowLayout.RIGHT, 14, 12));
        panel.setBackground(ColorPalette.BG);

        RoundedButton cancel = new RoundedButton("Cancel", Color.WHITE, ColorPalette.TEXT_PRIMARY, 8);
        cancel.addActionListener(e -> dispose());
        panel.add(cancel);

        RoundedButton save = new RoundedButton("Save Project", ColorPalette.PRIMARY, Color.WHITE, 8);
        save.addActionListener(e -> onSave());
        panel.add(save);

        return panel;
    }

    private void onSave() {
        String name = nameField.getText().trim();
        if (ValidationUtils.isBlank(name)) {
            errorLabel.setText("Project name is required.");
            return;
        }

        Date startDate = DateUtils.parseISO(startDateField.getText().trim());
        if (startDate == null) {
            errorLabel.setText("Invalid start date format (YYYY-MM-DD).");
            return;
        }

        Date deadline = null;
        String deadStr = deadlineField.getText().trim();
        if (!deadStr.isEmpty()) {
            deadline = DateUtils.parseISO(deadStr);
            if (deadline == null) {
                errorLabel.setText("Invalid deadline format (YYYY-MM-DD).");
                return;
            }
        }

        Priority prio = Priority.fromString(priorityChoice.getSelectedItem());
        ProjectStatus stat = ProjectStatus.fromString(statusChoice.getSelectedItem());
        String desc = descArea.getText().trim();

        if (projectToEdit == null) {
            Project newProj = new Project(name, desc, startDate, deadline, prio);
            newProj.setStatus(stat);
            Application.getInstance().getProjectService().addProject(newProj);
        } else {
            projectToEdit.setName(name);
            projectToEdit.setDescription(desc);
            projectToEdit.setStartDate(startDate);
            projectToEdit.setDeadline(deadline);
            projectToEdit.setPriority(prio);
            projectToEdit.setStatus(stat);
            Application.getInstance().getProjectService().updateProject(projectToEdit);
        }

        saved = true;
        Application.getInstance().notifyDataChanged();
        dispose();
    }

    public boolean isSaved() {
        return saved;
    }
}
