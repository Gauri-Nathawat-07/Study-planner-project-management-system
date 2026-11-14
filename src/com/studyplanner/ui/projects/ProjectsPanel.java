package com.studyplanner.ui.projects;

import com.studyplanner.app.Application;
import com.studyplanner.model.Project;
import com.studyplanner.model.ProjectStatus;
import com.studyplanner.model.StudyTask;
import com.studyplanner.ui.components.ProgressBar;
import com.studyplanner.ui.components.RoundedButton;
import com.studyplanner.util.ColorPalette;
import com.studyplanner.util.DateUtils;
import com.studyplanner.util.FontUtils;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Screen displaying project cards, associated milestone progress, status filters, and management options.
 */
public class ProjectsPanel extends Panel {
    private final Frame parentFrame;
    private Choice statusFilterChoice;
    private Panel cardsContainer;
    private Label countLabel;

    public ProjectsPanel(Frame parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout(0, 14));
        setBackground(ColorPalette.BG);

        add(buildToolbar(), BorderLayout.NORTH);

        ScrollPane scrollPane = new ScrollPane(ScrollPane.SCROLLBARS_AS_NEEDED);
        cardsContainer = new Panel();
        cardsContainer.setLayout(new GridBagLayout());
        cardsContainer.setBackground(ColorPalette.BG);
        scrollPane.add(cardsContainer);
        add(scrollPane, BorderLayout.CENTER);

        add(buildFooter(), BorderLayout.SOUTH);

        refreshProjects();
    }

    private Panel buildToolbar() {
        Panel bar = new Panel(new BorderLayout(12, 10));
        bar.setBackground(ColorPalette.CARD_BG);

        Panel left = new Panel(new FlowLayout(FlowLayout.LEFT, 14, 12));
        left.setBackground(ColorPalette.CARD_BG);
        Label fLbl = new Label("Filter by Status:");
        fLbl.setFont(FontUtils.bold(12));
        left.add(fLbl);

        statusFilterChoice = new Choice();
        statusFilterChoice.add("All Statuses");
        for (ProjectStatus s : ProjectStatus.values()) statusFilterChoice.add(s.getDisplayName());
        statusFilterChoice.addItemListener(e -> refreshProjects());
        left.add(statusFilterChoice);
        bar.add(left, BorderLayout.WEST);

        Panel right = new Panel(new FlowLayout(FlowLayout.RIGHT, 14, 12));
        right.setBackground(ColorPalette.CARD_BG);
        RoundedButton addBtn = new RoundedButton("+ New Project", ColorPalette.PRIMARY, Color.WHITE, 8);
        addBtn.addActionListener(e -> openProjectDialog(null));
        right.add(addBtn);
        bar.add(right, BorderLayout.EAST);

        return bar;
    }

    private Panel buildFooter() {
        Panel footer = new Panel(new FlowLayout(FlowLayout.LEFT, 16, 8));
        footer.setBackground(ColorPalette.CARD_BG);
        countLabel = new Label("Showing 0 projects");
        countLabel.setFont(FontUtils.regular(12));
        countLabel.setForeground(ColorPalette.TEXT_SECONDARY);
        footer.add(countLabel);
        return footer;
    }

    public void refreshProjects() {
        cardsContainer.removeAll();

        ProjectStatus filterStatus = null;
        if (statusFilterChoice != null && statusFilterChoice.getSelectedIndex() > 0) {
            filterStatus = ProjectStatus.fromString(statusFilterChoice.getSelectedItem());
        }

        List<Project> allProjects = Application.getInstance().getProjectService().getAllProjects();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 16, 6, 16);

        int count = 0;
        for (Project p : allProjects) {
            if (filterStatus != null && p.getStatus() != filterStatus) continue;

            cardsContainer.add(createProjectCard(p), gbc);
            gbc.gridy++;
            count++;
        }

        if (count == 0) {
            Panel empty = new Panel(new FlowLayout(FlowLayout.CENTER, 10, 40));
            empty.setBackground(ColorPalette.BG);
            Label emptyLbl = new Label("No projects found. Click '+ New Project' to get organized!");
            emptyLbl.setFont(FontUtils.bold(14));
            emptyLbl.setForeground(ColorPalette.TEXT_SECONDARY);
            empty.add(emptyLbl);
            cardsContainer.add(empty, gbc);
        }

        gbc.weighty = 1.0;
        Panel glue = new Panel();
        glue.setBackground(ColorPalette.BG);
        cardsContainer.add(glue, gbc);

        countLabel.setText("Showing " + count + " projects");
        cardsContainer.validate();
        cardsContainer.repaint();
    }

    private Panel createProjectCard(Project project) {
        Panel card = new Panel(new BorderLayout(10, 10));
        card.setBackground(ColorPalette.CARD_BG);
        card.setPreferredSize(new Dimension(600, 105));

        int progress = Application.getInstance().getProjectService().calculateProgress(
                project.getId(), Application.getInstance().getTaskService());
        List<StudyTask> tasks = Application.getInstance().getTaskService().getTasksByProject(project.getId());

        // Top Row: Title, Status Badge, Edit/Delete
        Panel top = new Panel(new BorderLayout(8, 0));
        top.setBackground(ColorPalette.CARD_BG);

        Panel titleBox = new Panel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        titleBox.setBackground(ColorPalette.CARD_BG);
        Label title = new Label(project.getName());
        title.setFont(FontUtils.bold(15));
        title.setForeground(ColorPalette.TEXT_PRIMARY);
        titleBox.add(title);

        Label statusLbl = new Label("[" + project.getStatus().getDisplayName() + "]");
        statusLbl.setFont(FontUtils.bold(11));
        statusLbl.setForeground(project.getStatus() == ProjectStatus.COMPLETED ? ColorPalette.SUCCESS : ColorPalette.PRIMARY);
        titleBox.add(statusLbl);
        top.add(titleBox, BorderLayout.WEST);

        Panel actionBox = new Panel(new FlowLayout(FlowLayout.RIGHT, 6, 2));
        actionBox.setBackground(ColorPalette.CARD_BG);

        RoundedButton editBtn = new RoundedButton("Edit", new Color(0xF1, 0xF5, 0xF9), ColorPalette.TEXT_PRIMARY, 6);
        editBtn.addActionListener(e -> openProjectDialog(project));
        actionBox.add(editBtn);

        RoundedButton delBtn = new RoundedButton("Delete", new Color(0xFE, 0xF2, 0xF2), ColorPalette.DANGER, 6);
        delBtn.addActionListener(e -> confirmDeleteProject(project));
        actionBox.add(delBtn);

        top.add(actionBox, BorderLayout.EAST);
        card.add(top, BorderLayout.NORTH);

        // Center: Description and Progress Bar
        Panel center = new Panel(new BorderLayout(0, 4));
        center.setBackground(ColorPalette.CARD_BG);

        Label desc = new Label(project.getDescription().isEmpty() ? "No description provided." : project.getDescription());
        desc.setFont(FontUtils.regular(12));
        desc.setForeground(ColorPalette.TEXT_SECONDARY);
        center.add(desc, BorderLayout.NORTH);

        ProgressBar pBar = new ProgressBar(progress, ColorPalette.PRIMARY);
        center.add(pBar, BorderLayout.CENTER);
        card.add(center, BorderLayout.CENTER);

        // Bottom: Deadline and Task count
        Panel bottom = new Panel(new FlowLayout(FlowLayout.LEFT, 10, 2));
        bottom.setBackground(ColorPalette.CARD_BG);

        String deadText = project.getDeadline() != null ? "Deadline: " + DateUtils.formatDisplay(project.getDeadline()) : "No deadline set";
        Label deadLbl = new Label(deadText);
        deadLbl.setFont(FontUtils.regular(11));
        deadLbl.setForeground(DateUtils.isOverdue(project.getDeadline()) ? ColorPalette.DANGER : ColorPalette.TEXT_MUTED);
        bottom.add(deadLbl);

        Label taskCountLbl = new Label("|  Associated Tasks: " + tasks.size());
        taskCountLbl.setFont(FontUtils.regular(11));
        taskCountLbl.setForeground(ColorPalette.TEXT_MUTED);
        bottom.add(taskCountLbl);

        card.add(bottom, BorderLayout.SOUTH);
        return card;
    }

    private void openProjectDialog(Project project) {
        ProjectDialog dialog = new ProjectDialog(parentFrame, project);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            refreshProjects();
        }
    }

    private void confirmDeleteProject(Project project) {
        Dialog confirmDialog = new Dialog(parentFrame, "Delete Project", true);
        confirmDialog.setLayout(new BorderLayout(10, 10));
        confirmDialog.setSize(380, 160);
        confirmDialog.setLocationRelativeTo(parentFrame);
        confirmDialog.setBackground(ColorPalette.CARD_BG);

        Label msg = new Label("Delete project '" + project.getName() + "'?\n(Associated tasks will be preserved as unassigned)", Label.CENTER);
        msg.setFont(FontUtils.bold(12));
        confirmDialog.add(msg, BorderLayout.CENTER);

        Panel bp = new Panel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        RoundedButton cancel = new RoundedButton("Cancel", Color.WHITE, ColorPalette.TEXT_PRIMARY, 6);
        cancel.addActionListener(e -> confirmDialog.dispose());
        RoundedButton ok = new RoundedButton("Delete", ColorPalette.DANGER, Color.WHITE, 6);
        ok.addActionListener(e -> {
            Application.getInstance().getTaskService().unassignProject(project.getId());
            Application.getInstance().getProjectService().deleteProject(project.getId());
            Application.getInstance().notifyDataChanged();
            confirmDialog.dispose();
        });
        bp.add(cancel);
        bp.add(ok);
        confirmDialog.add(bp, BorderLayout.SOUTH);
        confirmDialog.setVisible(true);
    }
}
