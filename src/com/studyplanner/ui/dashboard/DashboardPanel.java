package com.studyplanner.ui.dashboard;

import com.studyplanner.app.Application;
import com.studyplanner.model.StudyTask;
import com.studyplanner.model.Subject;
import com.studyplanner.service.DashboardService;
import com.studyplanner.ui.components.RoundedButton;
import com.studyplanner.ui.components.StatCard;
import com.studyplanner.ui.components.TaskCard;
import com.studyplanner.ui.tasks.TaskDialog;
import com.studyplanner.util.ColorPalette;
import com.studyplanner.util.DateUtils;
import com.studyplanner.util.FontUtils;

import java.awt.*;
import java.util.Date;
import java.util.List;

/**
 * Modern central dashboard showcasing metric cards, weekly study bar chart, today's tasks, and quick actions.
 */
public class DashboardPanel extends Panel implements TaskCard.TaskCardListener {
    private final Frame parentFrame;

    // Stat Cards
    private StatCard totalTasksCard;
    private StatCard pendingTasksCard;
    private StatCard completedTasksCard;
    private StatCard overdueTasksCard;
    private StatCard activeProjectsCard;
    private StatCard todayStudyCard;

    private WeeklyMiniChart weeklyChart;
    private Panel todayTasksContainer;
    private Label welcomeLabel;

    public DashboardPanel(Frame parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout(14, 14));
        setBackground(ColorPalette.BG);

        ScrollPane scrollPane = new ScrollPane(ScrollPane.SCROLLBARS_AS_NEEDED);
        Panel mainContent = new Panel(new GridBagLayout());
        mainContent.setBackground(ColorPalette.BG);
        scrollPane.add(mainContent);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 14, 8, 14);

        // 1. Welcome & Quick Actions Banner
        gbc.gridy = 0;
        mainContent.add(buildWelcomeBanner(), gbc);

        // 2. Metrics Grid (6 StatCards)
        gbc.gridy = 1;
        mainContent.add(buildMetricsGrid(), gbc);

        // 3. Middle Section: Weekly Chart + Today's Schedule
        gbc.gridy = 2;
        mainContent.add(buildScheduleAndChartSection(), gbc);

        add(scrollPane, BorderLayout.CENTER);
        refreshDashboard();
    }

    private Panel buildWelcomeBanner() {
        Panel banner = new Panel(new BorderLayout(12, 10));
        banner.setBackground(ColorPalette.CARD_BG);

        Panel left = new Panel(new BorderLayout(0, 4));
        left.setBackground(ColorPalette.CARD_BG);

        welcomeLabel = new Label("Welcome back!");
        welcomeLabel.setFont(FontUtils.bold(18));
        welcomeLabel.setForeground(ColorPalette.TEXT_PRIMARY);
        left.add(welcomeLabel, BorderLayout.NORTH);

        Label subLbl = new Label("Here is your study overview and upcoming milestones for today.");
        subLbl.setFont(FontUtils.regular(12));
        subLbl.setForeground(ColorPalette.TEXT_SECONDARY);
        left.add(subLbl, BorderLayout.SOUTH);
        banner.add(left, BorderLayout.CENTER);

        // Quick Action Buttons
        Panel actions = new Panel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        actions.setBackground(ColorPalette.CARD_BG);

        RoundedButton addTaskBtn = new RoundedButton("+ New Task", ColorPalette.PRIMARY, Color.WHITE, 8);
        addTaskBtn.addActionListener(e -> {
            TaskDialog dlg = new TaskDialog(parentFrame, null);
            dlg.setVisible(true);
            if (dlg.isSaved()) refreshDashboard();
        });
        actions.add(addTaskBtn);

        banner.add(actions, BorderLayout.EAST);
        return banner;
    }

    private Panel buildMetricsGrid() {
        Panel grid = new Panel(new GridLayout(2, 3, 12, 12));
        grid.setBackground(ColorPalette.BG);

        totalTasksCard = new StatCard("Total Tasks", "0", "All courses", ColorPalette.PRIMARY);
        pendingTasksCard = new StatCard("Pending Tasks", "0", "To complete", ColorPalette.WARNING);
        completedTasksCard = new StatCard("Completed", "0", "Completed", ColorPalette.SUCCESS);
        overdueTasksCard = new StatCard("Overdue", "0", "Needs action", ColorPalette.DANGER);
        activeProjectsCard = new StatCard("Active Projects", "0", "Ongoing", ColorPalette.SECONDARY);
        todayStudyCard = new StatCard("Today's Study", "0m", "Active focus", ColorPalette.INFO);

        grid.add(totalTasksCard);
        grid.add(pendingTasksCard);
        grid.add(completedTasksCard);
        grid.add(overdueTasksCard);
        grid.add(activeProjectsCard);
        grid.add(todayStudyCard);

        return grid;
    }

    private Panel buildScheduleAndChartSection() {
        Panel split = new Panel(new GridLayout(1, 2, 14, 0));
        split.setBackground(ColorPalette.BG);

        // Left: Weekly Bar Chart Canvas
        weeklyChart = new WeeklyMiniChart();
        split.add(weeklyChart);

        // Right: Today's Tasks
        Panel todayPanel = new Panel(new BorderLayout(0, 8));
        todayPanel.setBackground(ColorPalette.CARD_BG);
        todayPanel.setPreferredSize(new Dimension(400, 260));

        Panel header = new Panel(new BorderLayout());
        header.setBackground(ColorPalette.CARD_BG);
        Label title = new Label("  Today's Priority Tasks");
        title.setFont(FontUtils.bold(14));
        title.setForeground(ColorPalette.TEXT_PRIMARY);
        header.add(title, BorderLayout.WEST);
        todayPanel.add(header, BorderLayout.NORTH);

        ScrollPane sp = new ScrollPane(ScrollPane.SCROLLBARS_AS_NEEDED);
        todayTasksContainer = new Panel(new GridBagLayout());
        todayTasksContainer.setBackground(ColorPalette.CARD_BG);
        sp.add(todayTasksContainer);
        todayPanel.add(sp, BorderLayout.CENTER);

        split.add(todayPanel);
        return split;
    }

    public void refreshDashboard() {
        DashboardService ds = Application.getInstance().getDashboardService();
        String uName = Application.getInstance().getUserSettings().getUserName();
        welcomeLabel.setText("Welcome back, " + uName + "!");

        totalTasksCard.updateData(String.valueOf(ds.getTotalTasks()), "Overall assignments");
        pendingTasksCard.updateData(String.valueOf(ds.getPendingTasks()), "In progress");
        completedTasksCard.updateData(String.valueOf(ds.getCompletedTasks()), ds.getOverallCompletionPercentage() + "% completion rate");
        overdueTasksCard.updateData(String.valueOf(ds.getOverdueTasks()), ds.getOverdueTasks() > 0 ? "Action required" : "Zero overdue");
        activeProjectsCard.updateData(String.valueOf(ds.getActiveProjects()), "Active milestones");

        int minsToday = ds.getTodayStudyMinutes();
        todayStudyCard.updateData((minsToday / 60) + "h " + (minsToday % 60) + "m", "Logged focus time");

        weeklyChart.repaint();
        refreshTodayTasks();
    }

    private void refreshTodayTasks() {
        todayTasksContainer.removeAll();
        List<StudyTask> todayTasks = Application.getInstance().getTaskService().getTasksDueToday();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 8, 4, 8);

        if (todayTasks.isEmpty()) {
            Panel empty = new Panel(new FlowLayout(FlowLayout.CENTER, 10, 40));
            empty.setBackground(ColorPalette.CARD_BG);
            Label emptyLbl = new Label("No tasks due today. Enjoy your study time!");
            emptyLbl.setFont(FontUtils.regular(12));
            emptyLbl.setForeground(ColorPalette.TEXT_MUTED);
            empty.add(emptyLbl);
            todayTasksContainer.add(empty, gbc);
        } else {
            for (StudyTask t : todayTasks) {
                Subject s = Application.getInstance().getSubjectService().getSubjectById(t.getSubjectId());
                TaskCard card = new TaskCard(t, s, this);
                todayTasksContainer.add(card, gbc);
                gbc.gridy++;
            }
        }

        gbc.weighty = 1.0;
        Panel glue = new Panel();
        glue.setBackground(ColorPalette.CARD_BG);
        todayTasksContainer.add(glue, gbc);

        todayTasksContainer.validate();
        todayTasksContainer.repaint();
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
        TaskDialog dlg = new TaskDialog(parentFrame, task);
        dlg.setVisible(true);
        if (dlg.isSaved()) refreshDashboard();
    }

    @Override
    public void onDelete(StudyTask task) {
        Application.getInstance().getTaskService().deleteTask(task.getId());
        Application.getInstance().notifyDataChanged();
    }

    private class WeeklyMiniChart extends com.studyplanner.ui.components.DoubleBufferedCanvas {
        private final String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(400, 260);
        }

        @Override
        protected void paintDoubleBuffered(Graphics2D g2, int w, int h) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            g2.setColor(ColorPalette.CARD_BG);
            g2.fillRoundRect(0, 0, w - 1, h - 1, 10, 10);
            g2.setColor(ColorPalette.BORDER);
            g2.drawRoundRect(0, 0, w - 1, h - 1, 10, 10);

            g2.setFont(FontUtils.bold(14));
            g2.setColor(ColorPalette.TEXT_PRIMARY);
            g2.drawString("Weekly Study Overview", 16, 26);

            int[] data = Application.getInstance().getStudySessionService().getWeeklyMinutesByDay(
                    Application.getInstance().getUserSettings().getPreferredWeekStartDay());

            int max = 60;
            for (int v : data) if (v > max) max = v;

            int chartTop = 50;
            int chartBottom = h - 30;
            int chartH = chartBottom - chartTop;

            int colW = (w - 40) / 7;
            int barW = Math.min(colW - 14, 28);

            for (int i = 0; i < 7; i++) {
                int cx = 20 + i * colW + (colW - barW) / 2;
                int barHeight = (int) Math.round(((double) data[i] / max) * chartH);
                int barY = chartBottom - barHeight;

                g2.setColor(ColorPalette.PRIMARY);
                g2.fillRoundRect(cx, barY, barW, barHeight, 4, 4);

                g2.setFont(FontUtils.regular(10));
                g2.setColor(ColorPalette.TEXT_SECONDARY);
                int tx = cx + (barW - g2.getFontMetrics().stringWidth(days[i])) / 2;
                g2.drawString(days[i], tx, chartBottom + 16);
            }
        }
    }
}
