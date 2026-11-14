package com.studyplanner.ui.focus;

import com.studyplanner.app.Application;
import com.studyplanner.model.StudySession;
import com.studyplanner.model.StudyTask;
import com.studyplanner.model.Subject;
import com.studyplanner.model.UserSettings;
import com.studyplanner.ui.components.RoundedButton;
import com.studyplanner.util.ColorPalette;
import com.studyplanner.util.FontUtils;

import java.awt.*;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Dedicated Study Focus & Pomodoro screen with timer, session intervals, task binding, and stats.
 */
public class FocusPanel extends Panel {
    private enum SessionState {
        IDLE, RUNNING, PAUSED
    }

    private enum SessionType {
        FOCUS("Focus Session", 25, ColorPalette.PRIMARY),
        SHORT_BREAK("Short Break", 5, ColorPalette.SUCCESS),
        LONG_BREAK("Long Break", 15, ColorPalette.SECONDARY);

        final String title;
        final int defaultMinutes;
        final Color color;

        SessionType(String title, int defaultMinutes, Color color) {
            this.title = title;
            this.defaultMinutes = defaultMinutes;
            this.color = color;
        }
    }

    private SessionType currentType = SessionType.FOCUS;
    private SessionState state = SessionState.IDLE;
    private int remainingSeconds;
    private int targetSeconds;
    private int completedFocusCount = 0;
    private Date sessionStartTime;

    private Timer timer;
    private TimerCanvas timerCanvas;
    private RoundedButton startPauseBtn;
    private RoundedButton resetBtn;
    private RoundedButton skipBtn;

    private Choice subjectChoice;
    private Choice taskChoice;
    private Label statsLabel;

    public FocusPanel() {
        setLayout(new BorderLayout(14, 14));
        setBackground(ColorPalette.BG);

        resetToSessionType(SessionType.FOCUS);

        add(buildControlPanel(), BorderLayout.NORTH);

        timerCanvas = new TimerCanvas();
        add(timerCanvas, BorderLayout.CENTER);

        add(buildBottomPanel(), BorderLayout.SOUTH);

        refreshSelectors();
    }

    private Panel buildControlPanel() {
        Panel panel = new Panel(new BorderLayout(10, 10));
        panel.setBackground(ColorPalette.CARD_BG);

        Panel selectors = new Panel(new FlowLayout(FlowLayout.CENTER, 14, 10));
        selectors.setBackground(ColorPalette.CARD_BG);

        Label subLbl = new Label("Subject:");
        subLbl.setFont(FontUtils.bold(12));
        selectors.add(subLbl);
        subjectChoice = new Choice();
        selectors.add(subjectChoice);

        Label taskLbl = new Label("Task:");
        taskLbl.setFont(FontUtils.bold(12));
        selectors.add(taskLbl);
        taskChoice = new Choice();
        selectors.add(taskChoice);

        panel.add(selectors, BorderLayout.CENTER);
        return panel;
    }

    private Panel buildBottomPanel() {
        Panel panel = new Panel(new BorderLayout(10, 10));
        panel.setBackground(ColorPalette.CARD_BG);

        // Buttons
        Panel btnPanel = new Panel(new FlowLayout(FlowLayout.CENTER, 14, 12));
        btnPanel.setBackground(ColorPalette.CARD_BG);

        startPauseBtn = new RoundedButton("Start Focus", ColorPalette.PRIMARY, Color.WHITE, 10);
        startPauseBtn.addActionListener(e -> toggleStartPause());
        btnPanel.add(startPauseBtn);

        resetBtn = new RoundedButton("Reset", new Color(0xF1, 0xF5, 0xF9), ColorPalette.TEXT_PRIMARY, 10);
        resetBtn.addActionListener(e -> resetTimer());
        btnPanel.add(resetBtn);

        skipBtn = new RoundedButton("Skip Step", new Color(0xF1, 0xF5, 0xF9), ColorPalette.TEXT_PRIMARY, 10);
        skipBtn.addActionListener(e -> advanceSession(true));
        btnPanel.add(skipBtn);

        panel.add(btnPanel, BorderLayout.NORTH);

        // Stats note
        statsLabel = new Label("Today's Progress: 0 sessions completed (0 mins)", Label.CENTER);
        statsLabel.setFont(FontUtils.bold(12));
        statsLabel.setForeground(ColorPalette.TEXT_SECONDARY);
        panel.add(statsLabel, BorderLayout.SOUTH);

        return panel;
    }

    public void refreshSelectors() {
        subjectChoice.removeAll();
        subjectChoice.add("General Study (No Subject)");
        for (Subject s : Application.getInstance().getSubjectService().getAllSubjects()) {
            subjectChoice.add(s.getName());
        }

        taskChoice.removeAll();
        taskChoice.add("None (Independent Session)");
        for (StudyTask t : Application.getInstance().getTaskService().getPendingTasks()) {
            taskChoice.add(t.getTitle());
        }

        updateStatsLabel();
    }

    private void updateStatsLabel() {
        int todayMins = Application.getInstance().getStudySessionService().getTodayStudyMinutes();
        int count = Application.getInstance().getStudySessionService().getTodaySessionCount();
        statsLabel.setText("Today's Progress: " + count + " focus sessions completed (" + todayMins + " minutes logged)");
    }

    private void resetToSessionType(SessionType type) {
        this.currentType = type;
        this.state = SessionState.IDLE;

        UserSettings settings = Application.getInstance().getUserSettings();
        int mins = type.defaultMinutes;
        if (type == SessionType.FOCUS) mins = settings.getFocusDurationMinutes();
        else if (type == SessionType.SHORT_BREAK) mins = settings.getShortBreakDurationMinutes();
        else if (type == SessionType.LONG_BREAK) mins = settings.getLongBreakDurationMinutes();

        this.remainingSeconds = mins * 60;
        this.targetSeconds = this.remainingSeconds;

        if (startPauseBtn != null) {
            startPauseBtn.setText("Start " + (type == SessionType.FOCUS ? "Focus" : "Break"));
            startPauseBtn.setBgColor(type.color);
        }
        if (timerCanvas != null) timerCanvas.repaint();
    }

    private synchronized void toggleStartPause() {
        if (state == SessionState.IDLE || state == SessionState.PAUSED) {
            startTimer();
        } else if (state == SessionState.RUNNING) {
            pauseTimer();
        }
    }

    private void startTimer() {
        state = SessionState.RUNNING;
        if (sessionStartTime == null) {
            sessionStartTime = new Date();
        }
        startPauseBtn.setText("Pause");
        startPauseBtn.setBgColor(ColorPalette.WARNING);

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (remainingSeconds > 0) {
                    remainingSeconds--;
                    EventQueue.invokeLater(() -> timerCanvas.repaint());
                } else {
                    cancel();
                    EventQueue.invokeLater(() -> onTimerCompleted());
                }
            }
        }, 1000, 1000);
    }

    private void pauseTimer() {
        state = SessionState.PAUSED;
        if (timer != null) timer.cancel();
        startPauseBtn.setText("Resume");
        startPauseBtn.setBgColor(ColorPalette.PRIMARY);
        timerCanvas.repaint();
    }

    private void resetTimer() {
        if (timer != null) timer.cancel();
        resetToSessionType(currentType);
    }

    private void onTimerCompleted() {
        if (currentType == SessionType.FOCUS) {
            logCompletedSession();
            completedFocusCount++;
        }
        advanceSession(false);
    }

    private void advanceSession(boolean skipped) {
        if (timer != null) timer.cancel();

        if (currentType == SessionType.FOCUS) {
            if (completedFocusCount % 4 == 0 && completedFocusCount > 0) {
                resetToSessionType(SessionType.LONG_BREAK);
            } else {
                resetToSessionType(SessionType.SHORT_BREAK);
            }
        } else {
            resetToSessionType(SessionType.FOCUS);
        }
    }

    private void logCompletedSession() {
        int durationMinutes = targetSeconds / 60;
        String chosenSubjectId = null;
        int subIdx = subjectChoice.getSelectedIndex();
        List<Subject> subs = Application.getInstance().getSubjectService().getAllSubjects();
        if (subIdx > 0 && subIdx <= subs.size()) {
            chosenSubjectId = subs.get(subIdx - 1).getId();
        }

        String chosenTaskId = null;
        int taskIdx = taskChoice.getSelectedIndex();
        List<StudyTask> pending = Application.getInstance().getTaskService().getPendingTasks();
        if (taskIdx > 0 && taskIdx <= pending.size()) {
            chosenTaskId = pending.get(taskIdx - 1).getId();
        }

        StudySession session = new StudySession(
                chosenTaskId,
                chosenSubjectId,
                sessionStartTime != null ? sessionStartTime : new Date(),
                new Date(),
                durationMinutes,
                "FOCUS",
                "Pomodoro focus session"
        );
        Application.getInstance().getStudySessionService().addSession(session);

        if (chosenSubjectId != null) {
            Application.getInstance().getSubjectService().addStudyMinutes(chosenSubjectId, durationMinutes);
        }

        sessionStartTime = null;
        Application.getInstance().notifyDataChanged();
        updateStatsLabel();
    }

    public void cleanup() {
        if (timer != null) {
            timer.cancel();
        }
    }

    private class TimerCanvas extends com.studyplanner.ui.components.DoubleBufferedCanvas {
        @Override
        protected void paintDoubleBuffered(Graphics2D g2, int w, int h) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // Background Card
            g2.setColor(ColorPalette.CARD_BG);
            g2.fillRoundRect(0, 0, w - 1, h - 1, 12, 12);
            g2.setColor(ColorPalette.BORDER);
            g2.drawRoundRect(0, 0, w - 1, h - 1, 12, 12);

            // Progress Ring
            int size = Math.min(w, h) - 100;
            if (size < 120) size = 120;
            int cx = (w - size) / 2;
            int cy = (h - size) / 2;

            // Track arc
            g2.setColor(new Color(0xEE, 0xF2, 0xF6));
            g2.setStroke(new BasicStroke(16.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawOval(cx, cy, size, size);

            // Progress arc
            double fraction = targetSeconds > 0 ? ((double) remainingSeconds / targetSeconds) : 0;
            int angle = (int) Math.round(fraction * 360);
            g2.setColor(currentType.color);
            g2.drawArc(cx, cy, size, size, 90, angle);

            // Center Text: MM:SS
            int mins = remainingSeconds / 60;
            int secs = remainingSeconds % 60;
            String timeText = String.format("%02d:%02d", mins, secs);

            g2.setFont(FontUtils.bold(44));
            g2.setColor(ColorPalette.TEXT_PRIMARY);
            FontMetrics fm = g2.getFontMetrics();
            int tx = (w - fm.stringWidth(timeText)) / 2;
            int ty = (h / 2) + 10;
            g2.drawString(timeText, tx, ty);

            // Subtitle: Session type & step
            g2.setFont(FontUtils.bold(14));
            g2.setColor(currentType.color);
            String title = currentType.title.toUpperCase();
            int sx = (w - g2.getFontMetrics().stringWidth(title)) / 2;
            g2.drawString(title, sx, ty - 45);

            String stepText = "Focus Cycle #" + (completedFocusCount + 1);
            g2.setFont(FontUtils.regular(12));
            g2.setColor(ColorPalette.TEXT_MUTED);
            int kx = (w - g2.getFontMetrics().stringWidth(stepText)) / 2;
            g2.drawString(stepText, kx, ty + 35);
        }
    }
}
