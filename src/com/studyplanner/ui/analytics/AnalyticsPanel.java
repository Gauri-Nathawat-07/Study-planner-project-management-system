package com.studyplanner.ui.analytics;

import com.studyplanner.app.Application;
import com.studyplanner.model.Project;
import com.studyplanner.model.Subject;
import com.studyplanner.ui.components.ProgressBar;
import com.studyplanner.ui.components.StatCard;
import com.studyplanner.util.ColorPalette;
import com.studyplanner.util.FontUtils;

import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * Visual reports, weekly study time bar chart, subject breakdowns, and task completion analytics.
 */
public class AnalyticsPanel extends Panel {
    private StatCard completionCard;
    private StatCard sessionsCard;
    private StatCard studyTimeCard;
    private StatCard overdueCard;

    private WeeklyBarChart weeklyBarChart;
    private Panel projectProgressPanel;

    public AnalyticsPanel() {
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
        gbc.insets = new Insets(8, 14, 8, 14);

        // Top Metrics
        gbc.gridy = 0;
        content.add(buildTopStats(), gbc);

        // Weekly Study Chart
        gbc.gridy = 1;
        weeklyBarChart = new WeeklyBarChart();
        content.add(weeklyBarChart, gbc);

        // Projects & Subject Breakdown Split
        gbc.gridy = 2;
        content.add(buildBreakdownSection(), gbc);

        add(scrollPane, BorderLayout.CENTER);
        refreshAnalytics();
    }

    private Panel buildTopStats() {
        Panel panel = new Panel(new GridLayout(1, 4, 12, 0));
        panel.setBackground(ColorPalette.BG);

        completionCard = new StatCard("Completion Rate", "0%", "Tasks completed", ColorPalette.SUCCESS);
        sessionsCard = new StatCard("Focus Sessions", "0", "Total pomodoros logged", ColorPalette.PRIMARY);
        studyTimeCard = new StatCard("Today's Study", "0m", "Active focus time", ColorPalette.INFO);
        overdueCard = new StatCard("Overdue Tasks", "0", "Needs attention", ColorPalette.DANGER);

        panel.add(completionCard);
        panel.add(sessionsCard);
        panel.add(studyTimeCard);
        panel.add(overdueCard);
        return panel;
    }

    private Panel buildBreakdownSection() {
        Panel panel = new Panel(new GridLayout(1, 2, 14, 0));
        panel.setBackground(ColorPalette.BG);

        // Project Progress Card
        Panel projCard = new Panel(new BorderLayout(0, 10));
        projCard.setBackground(ColorPalette.CARD_BG);
        projCard.setPreferredSize(new Dimension(300, 200));

        Label pTitle = new Label("  Project Milestones");
        pTitle.setFont(FontUtils.bold(14));
        pTitle.setForeground(ColorPalette.TEXT_PRIMARY);
        projCard.add(pTitle, BorderLayout.NORTH);

        projectProgressPanel = new Panel(new GridBagLayout());
        projectProgressPanel.setBackground(ColorPalette.CARD_BG);
        ScrollPane sp = new ScrollPane(ScrollPane.SCROLLBARS_AS_NEEDED);
        sp.add(projectProgressPanel);
        projCard.add(sp, BorderLayout.CENTER);
        panel.add(projCard);

        // Subject Study Hours Card
        panel.add(new SubjectTimeCard());

        return panel;
    }

    public void refreshAnalytics() {
        int rate = Application.getInstance().getDashboardService().getOverallCompletionPercentage();
        completionCard.updateData(rate + "%", rate >= 75 ? "Excellent pace!" : "Keep going!");

        int totalSessions = Application.getInstance().getStudySessionService().getAllSessions().size();
        sessionsCard.updateData(String.valueOf(totalSessions), "Logged study intervals");

        int minsToday = Application.getInstance().getStudySessionService().getTodayStudyMinutes();
        studyTimeCard.updateData((minsToday / 60) + "h " + (minsToday % 60) + "m", "Recorded today");

        int overdue = Application.getInstance().getDashboardService().getOverdueTasks();
        overdueCard.updateData(String.valueOf(overdue), overdue > 0 ? "Requires action" : "All up to date");

        weeklyBarChart.repaint();
        refreshProjectProgress();
    }

    private void refreshProjectProgress() {
        projectProgressPanel.removeAll();
        List<Project> projects = Application.getInstance().getProjectService().getAllProjects();

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(6, 12, 6, 12);

        for (Project p : projects) {
            Panel row = new Panel(new BorderLayout(0, 4));
            row.setBackground(ColorPalette.CARD_BG);

            Label name = new Label(p.getName());
            name.setFont(FontUtils.bold(12));
            row.add(name, BorderLayout.NORTH);

            int progress = Application.getInstance().getProjectService().calculateProgress(
                    p.getId(), Application.getInstance().getTaskService());
            ProgressBar pb = new ProgressBar(progress, ColorPalette.PRIMARY);
            row.add(pb, BorderLayout.CENTER);

            projectProgressPanel.add(row, c);
            c.gridy++;
        }

        c.weighty = 1.0;
        Panel glue = new Panel();
        glue.setBackground(ColorPalette.CARD_BG);
        projectProgressPanel.add(glue, c);

        projectProgressPanel.validate();
        projectProgressPanel.repaint();
    }

    private class WeeklyBarChart extends com.studyplanner.ui.components.DoubleBufferedCanvas {
        private final String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(500, 220);
        }

        @Override
        protected void paintDoubleBuffered(Graphics2D g2, int w, int h) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // Background Card
            g2.setColor(ColorPalette.CARD_BG);
            g2.fillRoundRect(0, 0, w - 1, h - 1, 12, 12);
            g2.setColor(ColorPalette.BORDER);
            g2.drawRoundRect(0, 0, w - 1, h - 1, 12, 12);

            // Chart Title
            g2.setFont(FontUtils.bold(14));
            g2.setColor(ColorPalette.TEXT_PRIMARY);
            g2.drawString("Weekly Study Time (Minutes per Day)", 20, 28);

            int[] data = Application.getInstance().getStudySessionService().getWeeklyMinutesByDay(
                    Application.getInstance().getUserSettings().getPreferredWeekStartDay());

            int maxVal = 60; // min scale 1 hr
            for (int val : data) {
                if (val > maxVal) maxVal = val;
            }

            int chartTop = 50;
            int chartBottom = h - 35;
            int chartHeight = chartBottom - chartTop;

            int colWidth = (w - 60) / 7;
            int barWidth = Math.min(colWidth - 20, 36);

            for (int i = 0; i < 7; i++) {
                int cx = 30 + i * colWidth + (colWidth - barWidth) / 2;
                int barH = (int) Math.round(((double) data[i] / maxVal) * chartHeight);
                int barY = chartBottom - barH;

                // Bar
                g2.setColor(ColorPalette.PRIMARY);
                g2.fillRoundRect(cx, barY, barWidth, barH, 6, 6);

                // Minutes value above bar
                if (data[i] > 0) {
                    g2.setFont(FontUtils.bold(10));
                    g2.setColor(ColorPalette.PRIMARY);
                    String valStr = data[i] + "m";
                    int vx = cx + (barWidth - g2.getFontMetrics().stringWidth(valStr)) / 2;
                    g2.drawString(valStr, vx, barY - 4);
                }

                // Day label
                g2.setFont(FontUtils.regular(11));
                g2.setColor(ColorPalette.TEXT_SECONDARY);
                int tx = cx + (barWidth - g2.getFontMetrics().stringWidth(days[i])) / 2;
                g2.drawString(days[i], tx, chartBottom + 18);
            }
        }
    }

    private class SubjectTimeCard extends com.studyplanner.ui.components.DoubleBufferedCanvas {
        @Override
        public Dimension getPreferredSize() {
            return new Dimension(300, 200);
        }

        @Override
        protected void paintDoubleBuffered(Graphics2D g2, int w, int h) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            g2.setColor(ColorPalette.CARD_BG);
            g2.fillRoundRect(0, 0, w - 1, h - 1, 12, 12);
            g2.setColor(ColorPalette.BORDER);
            g2.drawRoundRect(0, 0, w - 1, h - 1, 12, 12);

            g2.setFont(FontUtils.bold(14));
            g2.setColor(ColorPalette.TEXT_PRIMARY);
            g2.drawString("Subject Time Breakdown", 16, 26);

            List<Subject> subjects = Application.getInstance().getSubjectService().getAllSubjects();
            int y = 52;
            for (Subject s : subjects) {
                if (y > h - 20) break;
                Color sc = ColorPalette.fromHex(s.getColorHex(), ColorPalette.PRIMARY);
                g2.setColor(sc);
                g2.fillOval(16, y - 8, 10, 10);

                g2.setFont(FontUtils.bold(12));
                g2.setColor(ColorPalette.TEXT_PRIMARY);
                g2.drawString(s.getName(), 34, y);

                long hours = s.getTotalStudyMinutes() / 60;
                long mins = s.getTotalStudyMinutes() % 60;
                String timeStr = hours + "h " + mins + "m";

                g2.setFont(FontUtils.regular(11));
                g2.setColor(ColorPalette.TEXT_SECONDARY);
                g2.drawString(timeStr, w - g2.getFontMetrics().stringWidth(timeStr) - 18, y);

                y += 26;
            }
        }
    }
}
