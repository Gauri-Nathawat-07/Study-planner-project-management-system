package com.studyplanner.ui.tasks;

import com.studyplanner.app.Application;
import com.studyplanner.model.Priority;
import com.studyplanner.model.StudyTask;
import com.studyplanner.model.Subject;
import com.studyplanner.model.TaskStatus;
import com.studyplanner.ui.components.RoundedButton;
import com.studyplanner.ui.components.TaskCard;
import com.studyplanner.util.ColorPalette;
import com.studyplanner.util.FontUtils;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

/**
 * Screen for managing tasks with live search, filters (Status, Priority, Subject), sorting, and CRUD.
 */
public class TasksPanel extends Panel implements TaskCard.TaskCardListener {
    private final Frame parentFrame;
    private TextField searchField;
    private Choice statusChoice;
    private Choice priorityChoice;
    private Choice subjectChoice;
    private Choice sortChoice;
    private Panel listContainer;
    private Label countLabel;

    public TasksPanel(Frame parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout(0, 14));
        setBackground(ColorPalette.BG);

        add(buildToolbar(), BorderLayout.NORTH);

        // Scrollable list area
        ScrollPane scrollPane = new ScrollPane(ScrollPane.SCROLLBARS_AS_NEEDED);
        listContainer = new Panel();
        listContainer.setLayout(new GridBagLayout());
        listContainer.setBackground(ColorPalette.BG);
        scrollPane.add(listContainer);

        add(scrollPane, BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        refreshTasks();
    }

    private Panel buildToolbar() {
        Panel bar = new Panel(new BorderLayout(12, 10));
        bar.setBackground(ColorPalette.CARD_BG);

        // Top line: search and "+ Add Task" button
        Panel topRow = new Panel(new BorderLayout(12, 0));
        topRow.setBackground(ColorPalette.CARD_BG);

        Panel searchPanel = new Panel(new BorderLayout(8, 0));
        searchPanel.setBackground(ColorPalette.CARD_BG);
        Label sLbl = new Label("Search:");
        sLbl.setFont(FontUtils.bold(12));
        searchPanel.add(sLbl, BorderLayout.WEST);

        searchField = new TextField();
        searchField.setFont(FontUtils.regular(13));
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                refreshTasks();
            }
        });
        searchPanel.add(searchField, BorderLayout.CENTER);
        topRow.add(searchPanel, BorderLayout.CENTER);

        RoundedButton addBtn = new RoundedButton("+ New Task", ColorPalette.PRIMARY, Color.WHITE, 8);
        addBtn.addActionListener(e -> openTaskDialog(null));
        topRow.add(addBtn, BorderLayout.EAST);
        bar.add(topRow, BorderLayout.NORTH);

        // Bottom line: filters and sorting
        Panel filterRow = new Panel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        filterRow.setBackground(ColorPalette.CARD_BG);

        filterRow.add(createLabeledChoice("Status:", statusChoice = new Choice()));
        statusChoice.add("All Statuses");
        for (TaskStatus s : TaskStatus.values()) statusChoice.add(s.getDisplayName());
        statusChoice.addItemListener(e -> refreshTasks());

        filterRow.add(createLabeledChoice("Priority:", priorityChoice = new Choice()));
        priorityChoice.add("All Priorities");
        for (Priority p : Priority.values()) priorityChoice.add(p.getDisplayName());
        priorityChoice.addItemListener(e -> refreshTasks());

        filterRow.add(createLabeledChoice("Subject:", subjectChoice = new Choice()));
        populateSubjectChoices();
        subjectChoice.addItemListener(e -> refreshTasks());

        filterRow.add(createLabeledChoice("Sort By:", sortChoice = new Choice()));
        sortChoice.add("Due Date");
        sortChoice.add("Priority");
        sortChoice.add("Title");
        sortChoice.add("Recently Created");
        sortChoice.addItemListener(e -> refreshTasks());

        bar.add(filterRow, BorderLayout.SOUTH);
        return bar;
    }

    private Panel createLabeledChoice(String labelText, Choice choice) {
        Panel p = new Panel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        p.setBackground(ColorPalette.CARD_BG);
        Label lbl = new Label(labelText);
        lbl.setFont(FontUtils.bold(11));
        lbl.setForeground(ColorPalette.TEXT_SECONDARY);
        p.add(lbl);
        p.add(choice);
        return p;
    }

    private void populateSubjectChoices() {
        subjectChoice.removeAll();
        subjectChoice.add("All Subjects");
        for (Subject s : Application.getInstance().getSubjectService().getAllSubjects()) {
            subjectChoice.add(s.getName());
        }
    }

    private Panel buildFooter() {
        Panel footer = new Panel(new FlowLayout(FlowLayout.LEFT, 16, 8));
        footer.setBackground(ColorPalette.CARD_BG);
        countLabel = new Label("Showing 0 tasks");
        countLabel.setFont(FontUtils.regular(12));
        countLabel.setForeground(ColorPalette.TEXT_SECONDARY);
        footer.add(countLabel);
        return footer;
    }

    public void refreshTasks() {
        String query = searchField != null ? searchField.getText() : "";

        TaskStatus statusFilter = null;
        if (statusChoice != null && statusChoice.getSelectedIndex() > 0) {
            statusFilter = TaskStatus.fromString(statusChoice.getSelectedItem());
        }

        Priority prioFilter = null;
        if (priorityChoice != null && priorityChoice.getSelectedIndex() > 0) {
            prioFilter = Priority.fromString(priorityChoice.getSelectedItem());
        }

        String subjectIdFilter = null;
        if (subjectChoice != null && subjectChoice.getSelectedIndex() > 0) {
            List<Subject> allSubs = Application.getInstance().getSubjectService().getAllSubjects();
            int idx = subjectChoice.getSelectedIndex() - 1;
            if (idx >= 0 && idx < allSubs.size()) {
                subjectIdFilter = allSubs.get(idx).getId();
            }
        }

        String sortBy = sortChoice != null ? sortChoice.getSelectedItem() : "Due Date";

        List<StudyTask> filtered = Application.getInstance().getTaskService()
                .filterTasks(query, statusFilter, prioFilter, subjectIdFilter, sortBy);

        listContainer.removeAll();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 12, 4, 12);

        if (filtered.isEmpty()) {
            Panel emptyPanel = new Panel(new FlowLayout(FlowLayout.CENTER, 10, 40));
            emptyPanel.setBackground(ColorPalette.BG);
            Label emptyLbl = new Label("No matching tasks found. Click '+ New Task' to add one!");
            emptyLbl.setFont(FontUtils.bold(14));
            emptyLbl.setForeground(ColorPalette.TEXT_SECONDARY);
            emptyPanel.add(emptyLbl);
            listContainer.add(emptyPanel, gbc);
        } else {
            for (StudyTask t : filtered) {
                Subject subj = Application.getInstance().getSubjectService().getSubjectById(t.getSubjectId());
                TaskCard card = new TaskCard(t, subj, this);
                listContainer.add(card, gbc);
                gbc.gridy++;
            }
        }

        // Push everything to the top
        gbc.weighty = 1.0;
        Panel glue = new Panel();
        glue.setBackground(ColorPalette.BG);
        listContainer.add(glue, gbc);

        countLabel.setText("Showing " + filtered.size() + " tasks");
        listContainer.validate();
        listContainer.repaint();
    }

    private void openTaskDialog(StudyTask task) {
        TaskDialog dialog = new TaskDialog(parentFrame, task);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            refreshTasks();
        }
    }

    @Override
    public void onToggleComplete(StudyTask task) {
        if (task.isCompleted()) {
            Application.getInstance().getTaskService().reopenTask(task.getId());
        } else {
            Application.getInstance().getTaskService().markCompleted(task.getId());
        }
        Application.getInstance().notifyDataChanged();
    }

    @Override
    public void onEdit(StudyTask task) {
        openTaskDialog(task);
    }

    @Override
    public void onDelete(StudyTask task) {
        boolean confirm = Application.getInstance().getUserSettings().isConfirmDeletions();
        if (confirm) {
            Dialog confirmDialog = new Dialog(parentFrame, "Confirm Delete", true);
            confirmDialog.setLayout(new BorderLayout(12, 12));
            confirmDialog.setSize(340, 160);
            confirmDialog.setLocationRelativeTo(parentFrame);
            confirmDialog.setBackground(ColorPalette.CARD_BG);

            Label msg = new Label("Delete task '" + task.getTitle() + "'?", Label.CENTER);
            msg.setFont(FontUtils.bold(13));
            confirmDialog.add(msg, BorderLayout.CENTER);

            Panel bp = new Panel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
            RoundedButton cancel = new RoundedButton("Cancel", Color.WHITE, ColorPalette.TEXT_PRIMARY, 6);
            cancel.addActionListener(e -> confirmDialog.dispose());
            RoundedButton ok = new RoundedButton("Delete", ColorPalette.DANGER, Color.WHITE, 6);
            ok.addActionListener(e -> {
                Application.getInstance().getTaskService().deleteTask(task.getId());
                Application.getInstance().notifyDataChanged();
                confirmDialog.dispose();
            });
            bp.add(cancel);
            bp.add(ok);
            confirmDialog.add(bp, BorderLayout.SOUTH);
            confirmDialog.setVisible(true);
        } else {
            Application.getInstance().getTaskService().deleteTask(task.getId());
            Application.getInstance().notifyDataChanged();
        }
    }
}
