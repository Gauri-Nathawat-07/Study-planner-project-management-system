package com.studyplanner.ui.calendar;

import com.studyplanner.app.Application;
import com.studyplanner.model.Priority;
import com.studyplanner.model.StudyTask;
import com.studyplanner.model.Subject;
import com.studyplanner.ui.components.RoundedButton;
import com.studyplanner.ui.tasks.TaskDialog;
import com.studyplanner.util.ColorPalette;
import com.studyplanner.util.DateUtils;
import com.studyplanner.util.FontUtils;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

/**
 * Monthly interactive calendar with task dots, date selection, month navigation, and schedule inspection.
 */
public class CalendarPanel extends Panel {
    private final Frame parentFrame;
    private final Calendar currentCalendar;
    private Date selectedDate;

    private Label monthYearLabel;
    private CalendarCanvas calendarCanvas;
    private Panel selectedDateTaskPanel;
    private Label selectedDateTitle;

    public CalendarPanel(Frame parentFrame) {
        this.parentFrame = parentFrame;
        this.currentCalendar = Calendar.getInstance();
        DateUtils.clearTime(currentCalendar);
        currentCalendar.set(Calendar.DAY_OF_MONTH, 1);
        this.selectedDate = DateUtils.today();

        setLayout(new BorderLayout(14, 14));
        setBackground(ColorPalette.BG);

        add(buildToolbar(), BorderLayout.NORTH);

        Panel mainSplit = new Panel(new GridLayout(1, 2, 14, 0));
        mainSplit.setBackground(ColorPalette.BG);

        // Left: Month Grid Canvas
        calendarCanvas = new CalendarCanvas();
        mainSplit.add(calendarCanvas);

        // Right: Selected Date Tasks List
        mainSplit.add(buildDetailsPanel());

        add(mainSplit, BorderLayout.CENTER);
        refreshCalendar();
    }

    private Panel buildToolbar() {
        Panel bar = new Panel(new BorderLayout(12, 0));
        bar.setBackground(ColorPalette.CARD_BG);

        Panel left = new Panel(new FlowLayout(FlowLayout.LEFT, 14, 10));
        left.setBackground(ColorPalette.CARD_BG);

        RoundedButton prevBtn = new RoundedButton("< Prev", new Color(0xF1, 0xF5, 0xF9), ColorPalette.TEXT_PRIMARY, 6);
        prevBtn.addActionListener(e -> {
            currentCalendar.add(Calendar.MONTH, -1);
            refreshCalendar();
        });
        left.add(prevBtn);

        monthYearLabel = new Label();
        monthYearLabel.setFont(FontUtils.bold(16));
        monthYearLabel.setForeground(ColorPalette.TEXT_PRIMARY);
        left.add(monthYearLabel);

        RoundedButton nextBtn = new RoundedButton("Next >", new Color(0xF1, 0xF5, 0xF9), ColorPalette.TEXT_PRIMARY, 6);
        nextBtn.addActionListener(e -> {
            currentCalendar.add(Calendar.MONTH, 1);
            refreshCalendar();
        });
        left.add(nextBtn);

        RoundedButton todayBtn = new RoundedButton("Today", Color.WHITE, ColorPalette.PRIMARY, 6);
        todayBtn.addActionListener(e -> {
            Calendar now = Calendar.getInstance();
            currentCalendar.set(Calendar.YEAR, now.get(Calendar.YEAR));
            currentCalendar.set(Calendar.MONTH, now.get(Calendar.MONTH));
            currentCalendar.set(Calendar.DAY_OF_MONTH, 1);
            selectedDate = DateUtils.today();
            refreshCalendar();
        });
        left.add(todayBtn);

        bar.add(left, BorderLayout.WEST);
        return bar;
    }

    private Panel buildDetailsPanel() {
        Panel panel = new Panel(new BorderLayout(0, 10));
        panel.setBackground(ColorPalette.CARD_BG);

        Panel header = new Panel(new BorderLayout(8, 0));
        header.setBackground(ColorPalette.CARD_BG);

        selectedDateTitle = new Label("Tasks for " + DateUtils.formatDisplay(selectedDate));
        selectedDateTitle.setFont(FontUtils.bold(14));
        selectedDateTitle.setForeground(ColorPalette.TEXT_PRIMARY);
        header.add(selectedDateTitle, BorderLayout.WEST);

        RoundedButton addBtn = new RoundedButton("+ Add for Date", ColorPalette.PRIMARY, Color.WHITE, 6);
        addBtn.addActionListener(e -> {
            StudyTask t = new StudyTask();
            t.setDueDate(selectedDate);
            TaskDialog dlg = new TaskDialog(parentFrame, t);
            dlg.setVisible(true);
            if (dlg.isSaved()) refreshCalendar();
        });
        header.add(addBtn, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);

        ScrollPane sp = new ScrollPane(ScrollPane.SCROLLBARS_AS_NEEDED);
        selectedDateTaskPanel = new Panel(new GridBagLayout());
        selectedDateTaskPanel.setBackground(ColorPalette.CARD_BG);
        sp.add(selectedDateTaskPanel);
        panel.add(sp, BorderLayout.CENTER);

        return panel;
    }

    public void refreshCalendar() {
        int month = currentCalendar.get(Calendar.MONTH);
        int year = currentCalendar.get(Calendar.YEAR);
        monthYearLabel.setText(DateUtils.getMonthName(month) + " " + year);

        calendarCanvas.repaint();
        refreshSelectedDateTasks();
    }

    private void refreshSelectedDateTasks() {
        selectedDateTitle.setText("Tasks for " + DateUtils.formatDisplay(selectedDate));
        selectedDateTaskPanel.removeAll();

        List<StudyTask> all = Application.getInstance().getTaskService().getAllTasks();
        List<StudyTask> matching = new ArrayList<>();
        for (StudyTask t : all) {
            if (t.getDueDate() != null && DateUtils.isSameDay(t.getDueDate(), selectedDate)) {
                matching.add(t);
            }
        }

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 8, 4, 8);

        if (matching.isEmpty()) {
            Panel empty = new Panel(new FlowLayout(FlowLayout.CENTER, 10, 40));
            empty.setBackground(ColorPalette.CARD_BG);
            Label emptyLbl = new Label("No deadlines scheduled for this day.");
            emptyLbl.setFont(FontUtils.regular(13));
            emptyLbl.setForeground(ColorPalette.TEXT_MUTED);
            empty.add(emptyLbl);
            selectedDateTaskPanel.add(empty, gbc);
        } else {
            for (StudyTask t : matching) {
                Panel row = new Panel(new BorderLayout(8, 0));
                row.setBackground(new Color(0xF8, 0xFA, 0xFC));
                row.setPreferredSize(new Dimension(300, 44));

                Subject s = Application.getInstance().getSubjectService().getSubjectById(t.getSubjectId());
                Label l = new Label((t.isCompleted() ? "[DONE] " : "[ ] ") + t.getTitle());
                l.setFont(FontUtils.bold(12));
                l.setForeground(t.isCompleted() ? ColorPalette.TEXT_MUTED : ColorPalette.TEXT_PRIMARY);
                row.add(l, BorderLayout.WEST);

                if (s != null) {
                    Label sLbl = new Label(s.getName());
                    sLbl.setFont(FontUtils.regular(11));
                    sLbl.setForeground(ColorPalette.fromHex(s.getColorHex(), ColorPalette.PRIMARY));
                    row.add(sLbl, BorderLayout.EAST);
                }
                selectedDateTaskPanel.add(row, gbc);
                gbc.gridy++;
            }
        }

        gbc.weighty = 1.0;
        Panel glue = new Panel();
        glue.setBackground(ColorPalette.CARD_BG);
        selectedDateTaskPanel.add(glue, gbc);

        selectedDateTaskPanel.validate();
        selectedDateTaskPanel.repaint();
    }

    private class CalendarCanvas extends com.studyplanner.ui.components.DoubleBufferedCanvas {
        private final String[] daysOfWeek = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

        public CalendarCanvas() {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    handleClick(e.getX(), e.getY());
                }
            });
        }

        private void handleClick(int x, int y) {
            int w = getWidth();
            int h = getHeight();
            int headerH = 34;

            int cellW = w / 7;
            int cellH = (h - headerH) / 6;
            if (cellH <= 0 || cellW <= 0) return;

            if (y < headerH) return;

            int col = x / cellW;
            int row = (y - headerH) / cellH;
            if (col < 0 || col > 6 || row < 0 || row > 5) return;

            int firstDayOfWeekOffset = getFirstDayOffset();
            int cellIndex = row * 7 + col;
            int dayNumber = cellIndex - firstDayOfWeekOffset + 1;

            int maxDays = currentCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            if (dayNumber >= 1 && dayNumber <= maxDays) {
                Calendar c = (Calendar) currentCalendar.clone();
                c.set(Calendar.DAY_OF_MONTH, dayNumber);
                selectedDate = c.getTime();
                repaint();
                refreshSelectedDateTasks();
            }
        }

        private String[] getDaysOfWeek() {
            boolean startsSunday = Application.getInstance().getUserSettings().getPreferredWeekStartDay() == Calendar.SUNDAY;
            return startsSunday ?
                    new String[]{"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"} :
                    new String[]{"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        }

        private int getFirstDayOffset() {
            Calendar c = (Calendar) currentCalendar.clone();
            c.set(Calendar.DAY_OF_MONTH, 1);
            int dayOfWeek = c.get(Calendar.DAY_OF_WEEK); // 1 = Sun, 2 = Mon ... 7 = Sat
            boolean startsSunday = Application.getInstance().getUserSettings().getPreferredWeekStartDay() == Calendar.SUNDAY;
            if (startsSunday) {
                return dayOfWeek - 1; // Sun=0, Mon=1, etc.
            } else {
                return (dayOfWeek == Calendar.SUNDAY) ? 6 : (dayOfWeek - 2); // Mon=0, Tue=1, ..., Sun=6
            }
        }

        @Override
        protected void paintDoubleBuffered(Graphics2D g2, int w, int h) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // Background card
            g2.setColor(ColorPalette.CARD_BG);
            g2.fillRoundRect(0, 0, w - 1, h - 1, 10, 10);
            g2.setColor(ColorPalette.BORDER);
            g2.drawRoundRect(0, 0, w - 1, h - 1, 10, 10);

            int headerH = 34;
            int cellW = w / 7;
            int cellH = (h - headerH) / 6;

            // Header days
            String[] days = getDaysOfWeek();
            g2.setFont(FontUtils.bold(12));
            g2.setColor(ColorPalette.TEXT_SECONDARY);
            for (int i = 0; i < 7; i++) {
                int tx = i * cellW + (cellW - g2.getFontMetrics().stringWidth(days[i])) / 2;
                g2.drawString(days[i], tx, 22);
            }

            g2.setColor(ColorPalette.BORDER);
            g2.drawLine(0, headerH, w, headerH);

            int offset = getFirstDayOffset();
            int maxDays = currentCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);

            List<StudyTask> allTasks = Application.getInstance().getTaskService().getAllTasks();

            for (int row = 0; row < 6; row++) {
                for (int col = 0; col < 7; col++) {
                    int cellIndex = row * 7 + col;
                    int dayNumber = cellIndex - offset + 1;

                    int cx = col * cellW;
                    int cy = headerH + row * cellH;

                    if (dayNumber >= 1 && dayNumber <= maxDays) {
                        Calendar cellCal = (Calendar) currentCalendar.clone();
                        cellCal.set(Calendar.DAY_OF_MONTH, dayNumber);
                        Date cellDate = cellCal.getTime();

                        boolean isToday = DateUtils.isSameDay(cellDate, DateUtils.today());
                        boolean isSelected = DateUtils.isSameDay(cellDate, selectedDate);

                        // Selection highlight
                        if (isSelected) {
                            g2.setColor(new Color(0xDB, 0xEA, 0xFE)); // Soft blue highlight
                            g2.fillRoundRect(cx + 4, cy + 4, cellW - 8, cellH - 8, 8, 8);
                        }

                        // Today badge
                        if (isToday) {
                            g2.setColor(ColorPalette.PRIMARY);
                            g2.fillOval(cx + (cellW - 24) / 2, cy + 6, 24, 24);
                            g2.setColor(Color.WHITE);
                            g2.setFont(FontUtils.bold(12));
                        } else {
                            g2.setFont(FontUtils.regular(12));
                            g2.setColor(ColorPalette.TEXT_PRIMARY);
                        }

                        String numStr = String.valueOf(dayNumber);
                        int tx = cx + (cellW - g2.getFontMetrics().stringWidth(numStr)) / 2;
                        int ty = isToday ? cy + 22 : cy + 20;
                        g2.drawString(numStr, tx, ty);

                        // Task dots for deadlines
                        int taskCount = 0;
                        for (StudyTask t : allTasks) {
                            if (t.getDueDate() != null && DateUtils.isSameDay(t.getDueDate(), cellDate)) {
                                taskCount++;
                            }
                        }

                        if (taskCount > 0) {
                            int dotY = cy + cellH - 12;
                            int dotSize = 6;
                            int dotsToDraw = Math.min(taskCount, 3);
                            int startDotX = cx + (cellW - (dotsToDraw * 8)) / 2;

                            for (int d = 0; d < dotsToDraw; d++) {
                                g2.setColor(d == 0 ? ColorPalette.PRIMARY : ColorPalette.WARNING);
                                g2.fillOval(startDotX + d * 8, dotY, dotSize, dotSize);
                            }
                        }
                    }
                }
            }
        }
    }
}
