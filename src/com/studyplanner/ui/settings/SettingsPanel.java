package com.studyplanner.ui.settings;

import com.studyplanner.app.Application;
import com.studyplanner.model.UserSettings;
import com.studyplanner.ui.components.RoundedButton;
import com.studyplanner.util.ColorPalette;
import com.studyplanner.util.FontUtils;
import com.studyplanner.util.ValidationUtils;

import java.awt.*;
import java.util.Calendar;

/**
 * Settings screen allowing custom Pomodoro durations, weekly targets, theme toggles, and data reset.
 */
public class SettingsPanel extends Panel {
    private final Frame parentFrame;

    private TextField userNameField;
    private TextField focusDurationField;
    private TextField shortBreakField;
    private TextField longBreakField;
    private TextField weeklyTargetField;
    private Choice weekStartChoice;
    private Checkbox confirmDeleteCheck;
    private Label statusFeedbackLabel;

    public SettingsPanel(Frame parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout(14, 14));
        setBackground(ColorPalette.BG);

        ScrollPane scrollPane = new ScrollPane(ScrollPane.SCROLLBARS_AS_NEEDED);
        Panel content = new Panel(new GridBagLayout());
        content.setBackground(ColorPalette.BG);
        scrollPane.add(content);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 20, 10, 20);

        gbc.gridy = 0;
        content.add(buildFormCard(), gbc);

        gbc.gridy = 1;
        content.add(buildDatabaseInfoCard(), gbc);

        gbc.gridy = 2;
        content.add(buildDangerZoneCard(), gbc);

        add(scrollPane, BorderLayout.CENTER);
        loadCurrentSettings();
    }

    private Panel buildDatabaseInfoCard() {
        Panel card = new Panel(new BorderLayout(10, 8));
        card.setBackground(ColorPalette.CARD_BG);
        card.setPreferredSize(new Dimension(500, 115));

        Panel header = new Panel(new FlowLayout(FlowLayout.LEFT, 18, 12));
        header.setBackground(ColorPalette.CARD_BG);
        Label title = new Label("Database & Relational Storage (MySQL)");
        title.setFont(FontUtils.bold(14));
        title.setForeground(ColorPalette.TEXT_PRIMARY);
        header.add(title);
        card.add(header, BorderLayout.NORTH);

        Panel body = new Panel(new GridLayout(2, 2, 12, 6));
        body.setBackground(ColorPalette.CARD_BG);

        Label typeLbl = new Label("  Engine: MySQL Database (schema.sql)");
        typeLbl.setFont(FontUtils.regular(12));
        typeLbl.setForeground(ColorPalette.TEXT_SECONDARY);
        body.add(typeLbl);

        boolean isConn = Application.getInstance().getDatabaseManager().isConnected();
        Label statusLbl = new Label(isConn ? "  Status: Connected to MySQL" : "  Status: Local Cache (MySQL offline)");
        statusLbl.setFont(FontUtils.bold(12));
        statusLbl.setForeground(isConn ? ColorPalette.SUCCESS : ColorPalette.WARNING);
        body.add(statusLbl);

        Label pathLbl = new Label("  Target: " + Application.getInstance().getDatabaseManager().getDatabaseInfo());
        pathLbl.setFont(FontUtils.regular(12));
        pathLbl.setForeground(ColorPalette.TEXT_SECONDARY);
        body.add(pathLbl);

        Label driverLbl = new Label("  Driver: MySQL Connector/J (lib/)");
        driverLbl.setFont(FontUtils.regular(12));
        driverLbl.setForeground(ColorPalette.TEXT_MUTED);
        body.add(driverLbl);

        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private Panel buildFormCard() {
        Panel card = new Panel(new GridBagLayout());
        card.setBackground(ColorPalette.CARD_BG);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(6, 20, 6, 20);
        c.weightx = 1.0;
        int row = 0;

        // Section Title
        c.gridy = row++;
        Label sec1 = new Label("Preferences & Study Configuration");
        sec1.setFont(FontUtils.bold(15));
        sec1.setForeground(ColorPalette.TEXT_PRIMARY);
        card.add(sec1, c);

        // Name
        c.gridy = row++;
        Label nameLbl = new Label("User Display Name");
        nameLbl.setFont(FontUtils.bold(12));
        card.add(nameLbl, c);

        c.gridy = row++;
        userNameField = new TextField();
        userNameField.setFont(FontUtils.regular(13));
        card.add(userNameField, c);

        // Pomodoro Row
        c.gridy = row++;
        Panel pomoRow = new Panel(new GridLayout(1, 3, 12, 0));
        pomoRow.setBackground(ColorPalette.CARD_BG);

        Panel pFoc = new Panel(new BorderLayout(0, 4));
        pFoc.setBackground(ColorPalette.CARD_BG);
        Label lblFoc = new Label("Focus (mins)");
        lblFoc.setFont(FontUtils.bold(11));
        pFoc.add(lblFoc, BorderLayout.NORTH);
        focusDurationField = new TextField();
        pFoc.add(focusDurationField, BorderLayout.CENTER);
        pomoRow.add(pFoc);

        Panel pSb = new Panel(new BorderLayout(0, 4));
        pSb.setBackground(ColorPalette.CARD_BG);
        Label lblSb = new Label("Short Break (mins)");
        lblSb.setFont(FontUtils.bold(11));
        pSb.add(lblSb, BorderLayout.NORTH);
        shortBreakField = new TextField();
        pSb.add(shortBreakField, BorderLayout.CENTER);
        pomoRow.add(pSb);

        Panel pLb = new Panel(new BorderLayout(0, 4));
        pLb.setBackground(ColorPalette.CARD_BG);
        Label lblLb = new Label("Long Break (mins)");
        lblLb.setFont(FontUtils.bold(11));
        pLb.add(lblLb, BorderLayout.NORTH);
        longBreakField = new TextField();
        pLb.add(longBreakField, BorderLayout.CENTER);
        pomoRow.add(pLb);
        card.add(pomoRow, c);

        // Weekly Target & Week Start
        c.gridy = row++;
        Panel goalRow = new Panel(new GridLayout(1, 2, 12, 0));
        goalRow.setBackground(ColorPalette.CARD_BG);

        Panel pGoal = new Panel(new BorderLayout(0, 4));
        pGoal.setBackground(ColorPalette.CARD_BG);
        Label lblGoal = new Label("Weekly Target (Hours)");
        lblGoal.setFont(FontUtils.bold(11));
        pGoal.add(lblGoal, BorderLayout.NORTH);
        weeklyTargetField = new TextField();
        pGoal.add(weeklyTargetField, BorderLayout.CENTER);
        goalRow.add(pGoal);

        Panel pStart = new Panel(new BorderLayout(0, 4));
        pStart.setBackground(ColorPalette.CARD_BG);
        Label lblStart = new Label("Preferred Week Start Day");
        lblStart.setFont(FontUtils.bold(11));
        pStart.add(lblStart, BorderLayout.NORTH);
        weekStartChoice = new Choice();
        weekStartChoice.add("Monday");
        weekStartChoice.add("Sunday");
        pStart.add(weekStartChoice, BorderLayout.CENTER);
        goalRow.add(pStart);
        card.add(goalRow, c);

        // Confirmation Checkbox
        c.gridy = row++;
        confirmDeleteCheck = new Checkbox("Show confirmation prompt before deleting items");
        confirmDeleteCheck.setFont(FontUtils.regular(12));
        card.add(confirmDeleteCheck, c);

        // Save Button & Feedback
        c.gridy = row++;
        Panel btnRow = new Panel(new FlowLayout(FlowLayout.LEFT, 0, 10));
        btnRow.setBackground(ColorPalette.CARD_BG);
        RoundedButton saveBtn = new RoundedButton("Save Settings", ColorPalette.PRIMARY, Color.WHITE, 8);
        saveBtn.addActionListener(e -> saveSettings());
        btnRow.add(saveBtn);

        statusFeedbackLabel = new Label("");
        statusFeedbackLabel.setFont(FontUtils.bold(12));
        statusFeedbackLabel.setForeground(ColorPalette.SUCCESS);
        btnRow.add(statusFeedbackLabel);
        card.add(btnRow, c);

        return card;
    }

    private Panel buildDangerZoneCard() {
        Panel card = new Panel(new BorderLayout(10, 10));
        card.setBackground(ColorPalette.CARD_BG);

        Panel inner = new Panel(new FlowLayout(FlowLayout.LEFT, 18, 14));
        inner.setBackground(ColorPalette.CARD_BG);

        Label lbl = new Label("Danger Zone: Reset application data back to fresh install state");
        lbl.setFont(FontUtils.bold(12));
        lbl.setForeground(ColorPalette.DANGER);
        inner.add(lbl);

        RoundedButton resetBtn = new RoundedButton("Reset All Data", ColorPalette.DANGER, Color.WHITE, 6);
        resetBtn.addActionListener(e -> confirmResetData());
        inner.add(resetBtn);

        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    public void loadCurrentSettings() {
        UserSettings s = Application.getInstance().getUserSettings();
        userNameField.setText(s.getUserName());
        focusDurationField.setText(String.valueOf(s.getFocusDurationMinutes()));
        shortBreakField.setText(String.valueOf(s.getShortBreakDurationMinutes()));
        longBreakField.setText(String.valueOf(s.getLongBreakDurationMinutes()));
        weeklyTargetField.setText(String.valueOf(s.getWeeklyStudyHourTarget()));

        if (s.getPreferredWeekStartDay() == Calendar.SUNDAY) {
            weekStartChoice.select("Sunday");
        } else {
            weekStartChoice.select("Monday");
        }
        confirmDeleteCheck.setState(s.isConfirmDeletions());
    }

    private void saveSettings() {
        UserSettings s = Application.getInstance().getUserSettings();
        s.setUserName(userNameField.getText().trim());
        s.setFocusDurationMinutes(ValidationUtils.parsePositiveInt(focusDurationField.getText(), 25));
        s.setShortBreakDurationMinutes(ValidationUtils.parsePositiveInt(shortBreakField.getText(), 5));
        s.setLongBreakDurationMinutes(ValidationUtils.parsePositiveInt(longBreakField.getText(), 15));
        s.setWeeklyStudyHourTarget(ValidationUtils.parsePositiveInt(weeklyTargetField.getText(), 20));

        if ("Sunday".equalsIgnoreCase(weekStartChoice.getSelectedItem())) {
            s.setPreferredWeekStartDay(Calendar.SUNDAY);
        } else {
            s.setPreferredWeekStartDay(Calendar.MONDAY);
        }
        s.setConfirmDeletions(confirmDeleteCheck.getState());

        Application.getInstance().updateUserSettings(s);
        statusFeedbackLabel.setText("  Settings saved successfully!");
    }

    private void confirmResetData() {
        Dialog dlg = new Dialog(parentFrame, "Reset Application Data", true);
        dlg.setLayout(new BorderLayout(10, 10));
        dlg.setSize(380, 160);
        dlg.setLocationRelativeTo(parentFrame);
        dlg.setBackground(ColorPalette.CARD_BG);

        Label msg = new Label("Are you sure? This will wipe all tasks, projects, and study logs.", Label.CENTER);
        msg.setFont(FontUtils.bold(12));
        dlg.add(msg, BorderLayout.CENTER);

        Panel bp = new Panel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        RoundedButton cancel = new RoundedButton("Cancel", Color.WHITE, ColorPalette.TEXT_PRIMARY, 6);
        cancel.addActionListener(e -> dlg.dispose());
        RoundedButton wipe = new RoundedButton("Yes, Reset Everything", ColorPalette.DANGER, Color.WHITE, 6);
        wipe.addActionListener(e -> {
            Application.getInstance().resetAllData();
            loadCurrentSettings();
            dlg.dispose();
        });
        bp.add(cancel);
        bp.add(wipe);
        dlg.add(bp, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }
}
