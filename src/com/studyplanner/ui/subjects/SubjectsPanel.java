package com.studyplanner.ui.subjects;

import com.studyplanner.app.Application;
import com.studyplanner.model.StudyTask;
import com.studyplanner.model.Subject;
import com.studyplanner.ui.components.ProgressBar;
import com.studyplanner.ui.components.RoundedButton;
import com.studyplanner.util.ColorPalette;
import com.studyplanner.util.FontUtils;
import com.studyplanner.util.ValidationUtils;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

/**
 * Screen displaying subjects/courses, progress, custom color picking, and weekly goal settings.
 */
public class SubjectsPanel extends Panel {
    private final Frame parentFrame;
    private Panel subjectsContainer;

    public SubjectsPanel(Frame parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout(0, 14));
        setBackground(ColorPalette.BG);

        add(buildToolbar(), BorderLayout.NORTH);

        ScrollPane scrollPane = new ScrollPane(ScrollPane.SCROLLBARS_AS_NEEDED);
        subjectsContainer = new Panel();
        subjectsContainer.setLayout(new GridBagLayout());
        subjectsContainer.setBackground(ColorPalette.BG);
        scrollPane.add(subjectsContainer);
        add(scrollPane, BorderLayout.CENTER);

        refreshSubjects();
    }

    private Panel buildToolbar() {
        Panel bar = new Panel(new BorderLayout(12, 10));
        bar.setBackground(ColorPalette.CARD_BG);

        Panel left = new Panel(new FlowLayout(FlowLayout.LEFT, 16, 12));
        left.setBackground(ColorPalette.CARD_BG);
        Label title = new Label("Courses & Academic Subjects");
        title.setFont(FontUtils.bold(14));
        left.add(title);
        bar.add(left, BorderLayout.WEST);

        Panel right = new Panel(new FlowLayout(FlowLayout.RIGHT, 14, 12));
        right.setBackground(ColorPalette.CARD_BG);
        RoundedButton addBtn = new RoundedButton("+ New Subject", ColorPalette.PRIMARY, Color.WHITE, 8);
        addBtn.addActionListener(e -> openSubjectDialog(null));
        right.add(addBtn);
        bar.add(right, BorderLayout.EAST);

        return bar;
    }

    public void refreshSubjects() {
        subjectsContainer.removeAll();
        List<Subject> subjects = Application.getInstance().getSubjectService().getAllSubjects();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 16, 6, 16);

        for (Subject s : subjects) {
            subjectsContainer.add(createSubjectCard(s), gbc);
            gbc.gridy++;
        }

        if (subjects.isEmpty()) {
            Panel empty = new Panel(new FlowLayout(FlowLayout.CENTER, 10, 40));
            empty.setBackground(ColorPalette.BG);
            Label emptyLbl = new Label("No subjects configured. Click '+ New Subject' to begin tracking courses!");
            emptyLbl.setFont(FontUtils.bold(14));
            emptyLbl.setForeground(ColorPalette.TEXT_SECONDARY);
            empty.add(emptyLbl);
            subjectsContainer.add(empty, gbc);
        }

        gbc.weighty = 1.0;
        Panel glue = new Panel();
        glue.setBackground(ColorPalette.BG);
        subjectsContainer.add(glue, gbc);

        subjectsContainer.validate();
        subjectsContainer.repaint();
    }

    private Panel createSubjectCard(Subject subject) {
        Panel card = new Panel(new BorderLayout(10, 8));
        card.setBackground(ColorPalette.CARD_BG);
        card.setPreferredSize(new Dimension(600, 105));

        Color subColor = ColorPalette.fromHex(subject.getColorHex(), ColorPalette.PRIMARY);

        // Header strip
        Panel top = new Panel(new BorderLayout(8, 0));
        top.setBackground(ColorPalette.CARD_BG);

        Panel titleBox = new Panel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        titleBox.setBackground(ColorPalette.CARD_BG);

        // Color badge indicator
        Canvas colorDot = new Canvas() {
            @Override
            public Dimension getPreferredSize() { return new Dimension(14, 14); }
            @Override
            public void paint(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(subColor);
                g2.fillOval(0, 0, 14, 14);
            }
        };
        titleBox.add(colorDot);

        Label title = new Label(subject.getName());
        title.setFont(FontUtils.bold(15));
        title.setForeground(ColorPalette.TEXT_PRIMARY);
        titleBox.add(title);
        top.add(titleBox, BorderLayout.WEST);

        Panel actionBox = new Panel(new FlowLayout(FlowLayout.RIGHT, 6, 2));
        actionBox.setBackground(ColorPalette.CARD_BG);

        RoundedButton editBtn = new RoundedButton("Edit", new Color(0xF1, 0xF5, 0xF9), ColorPalette.TEXT_PRIMARY, 6);
        editBtn.addActionListener(e -> openSubjectDialog(subject));
        actionBox.add(editBtn);

        RoundedButton delBtn = new RoundedButton("Delete", new Color(0xFE, 0xF2, 0xF2), ColorPalette.DANGER, 6);
        delBtn.addActionListener(e -> confirmDeleteSubject(subject));
        actionBox.add(delBtn);

        top.add(actionBox, BorderLayout.EAST);
        card.add(top, BorderLayout.NORTH);

        // Stats & Progress
        List<StudyTask> tasks = Application.getInstance().getTaskService().getTasksBySubject(subject.getId());
        int completed = 0;
        for (StudyTask t : tasks) {
            if (t.isCompleted()) completed++;
        }
        int progress = tasks.isEmpty() ? 0 : (int) Math.round(((double) completed / tasks.size()) * 100);

        Panel center = new Panel(new BorderLayout(0, 4));
        center.setBackground(ColorPalette.CARD_BG);

        Label desc = new Label(subject.getDescription().isEmpty() ? "No description" : subject.getDescription());
        desc.setFont(FontUtils.regular(12));
        desc.setForeground(ColorPalette.TEXT_SECONDARY);
        center.add(desc, BorderLayout.NORTH);

        ProgressBar pBar = new ProgressBar(progress, subColor);
        center.add(pBar, BorderLayout.CENTER);
        card.add(center, BorderLayout.CENTER);

        // Bottom: Study Time & Task Count
        Panel bottom = new Panel(new FlowLayout(FlowLayout.LEFT, 10, 2));
        bottom.setBackground(ColorPalette.CARD_BG);

        long hours = subject.getTotalStudyMinutes() / 60;
        long mins = subject.getTotalStudyMinutes() % 60;
        Label timeLbl = new Label("Logged Study: " + hours + "h " + mins + "m  (Goal: " + subject.getTargetWeeklyHours() + "h/wk)");
        timeLbl.setFont(FontUtils.regular(11));
        timeLbl.setForeground(ColorPalette.TEXT_SECONDARY);
        bottom.add(timeLbl);

        Label taskLbl = new Label("| Tasks: " + completed + "/" + tasks.size() + " completed");
        taskLbl.setFont(FontUtils.regular(11));
        taskLbl.setForeground(ColorPalette.TEXT_MUTED);
        bottom.add(taskLbl);

        card.add(bottom, BorderLayout.SOUTH);
        return card;
    }

    private void openSubjectDialog(Subject subject) {
        Dialog dlg = new Dialog(parentFrame, subject == null ? "Add Subject" : "Edit Subject", true);
        dlg.setLayout(new BorderLayout(10, 10));
        dlg.setSize(400, 360);
        dlg.setLocationRelativeTo(parentFrame);
        dlg.setBackground(ColorPalette.BG);

        Panel form = new Panel(new GridBagLayout());
        form.setBackground(ColorPalette.CARD_BG);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(6, 16, 6, 16);
        c.weightx = 1.0;
        int row = 0;

        c.gridy = row++;
        Label nameLbl = new Label("Subject Name *");
        nameLbl.setFont(FontUtils.bold(12));
        form.add(nameLbl, c);

        c.gridy = row++;
        TextField nameField = new TextField();
        if (subject != null) nameField.setText(subject.getName());
        form.add(nameField, c);

        c.gridy = row++;
        Label colorLbl = new Label("Accent Color");
        colorLbl.setFont(FontUtils.bold(12));
        form.add(colorLbl, c);

        c.gridy = row++;
        Choice colorChoice = new Choice();
        colorChoice.add("Blue (#3B82F6)");
        colorChoice.add("Purple (#8B5CF6)");
        colorChoice.add("Green (#10B981)");
        colorChoice.add("Amber (#F59E0B)");
        colorChoice.add("Red (#EF4444)");
        colorChoice.add("Pink (#EC4899)");
        colorChoice.add("Cyan (#06B6D4)");
        colorChoice.add("Indigo (#6366F1)");
        form.add(colorChoice, c);

        c.gridy = row++;
        Label hoursLbl = new Label("Weekly Target Study Hours");
        hoursLbl.setFont(FontUtils.bold(12));
        form.add(hoursLbl, c);

        c.gridy = row++;
        TextField hoursField = new TextField();
        hoursField.setText(subject != null ? String.valueOf(subject.getTargetWeeklyHours()) : "5");
        form.add(hoursField, c);

        c.gridy = row++;
        Label descLbl = new Label("Description");
        descLbl.setFont(FontUtils.bold(12));
        form.add(descLbl, c);

        c.gridy = row++;
        TextField descField = new TextField();
        if (subject != null) descField.setText(subject.getDescription());
        form.add(descField, c);

        dlg.add(form, BorderLayout.CENTER);

        Panel bp = new Panel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        RoundedButton cancel = new RoundedButton("Cancel", Color.WHITE, ColorPalette.TEXT_PRIMARY, 6);
        cancel.addActionListener(e -> dlg.dispose());
        bp.add(cancel);

        RoundedButton save = new RoundedButton("Save", ColorPalette.PRIMARY, Color.WHITE, 6);
        save.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (ValidationUtils.isBlank(name)) return;

            int targetHours = ValidationUtils.parsePositiveInt(hoursField.getText(), 5);
            String hex = ColorPalette.toHex(ColorPalette.PRESET_COLORS[colorChoice.getSelectedIndex()]);
            String desc = descField.getText().trim();

            if (subject == null) {
                Subject newSub = new Subject(name, desc, hex, targetHours);
                Application.getInstance().getSubjectService().addSubject(newSub);
            } else {
                subject.setName(name);
                subject.setDescription(desc);
                subject.setColorHex(hex);
                subject.setTargetWeeklyHours(targetHours);
                Application.getInstance().getSubjectService().updateSubject(subject);
            }
            Application.getInstance().notifyDataChanged();
            dlg.dispose();
        });
        bp.add(save);
        dlg.add(bp, BorderLayout.SOUTH);

        dlg.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dlg.dispose();
            }
        });
        dlg.setVisible(true);
    }

    private void confirmDeleteSubject(Subject subject) {
        Dialog dlg = new Dialog(parentFrame, "Delete Subject", true);
        dlg.setLayout(new BorderLayout(10, 10));
        dlg.setSize(380, 150);
        dlg.setLocationRelativeTo(parentFrame);
        dlg.setBackground(ColorPalette.CARD_BG);

        Label msg = new Label("Delete subject '" + subject.getName() + "'?\n(Associated tasks will be kept as unassigned)", Label.CENTER);
        msg.setFont(FontUtils.bold(12));
        dlg.add(msg, BorderLayout.CENTER);

        Panel bp = new Panel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        RoundedButton cancel = new RoundedButton("Cancel", Color.WHITE, ColorPalette.TEXT_PRIMARY, 6);
        cancel.addActionListener(e -> dlg.dispose());
        RoundedButton ok = new RoundedButton("Delete", ColorPalette.DANGER, Color.WHITE, 6);
        ok.addActionListener(e -> {
            Application.getInstance().getTaskService().unassignSubject(subject.getId());
            Application.getInstance().getSubjectService().deleteSubject(subject.getId());
            Application.getInstance().notifyDataChanged();
            dlg.dispose();
        });
        bp.add(cancel);
        bp.add(ok);
        dlg.add(bp, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }
}
