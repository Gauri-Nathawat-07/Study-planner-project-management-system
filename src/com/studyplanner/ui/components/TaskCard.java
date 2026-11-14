package com.studyplanner.ui.components;

import com.studyplanner.model.Priority;
import com.studyplanner.model.StudyTask;
import com.studyplanner.model.Subject;
import com.studyplanner.util.ColorPalette;
import com.studyplanner.util.DateUtils;
import com.studyplanner.util.FontUtils;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Modern card displaying a task, status badge, priority, subject dot, due date, and quick actions with double-buffering.
 */
public class TaskCard extends DoubleBufferedCanvas {
    public interface TaskCardListener {
        void onToggleComplete(StudyTask task);
        void onEdit(StudyTask task);
        void onDelete(StudyTask task);
    }

    private final StudyTask task;
    private final Subject subject;
    private TaskCardListener listener;
    private boolean isHovered = false;

    // Hit test zones
    private Rectangle checkZone = new Rectangle(12, 14, 20, 20);
    private Rectangle editZone = new Rectangle(0, 0, 0, 0);
    private Rectangle deleteZone = new Rectangle(0, 0, 0, 0);

    public TaskCard(StudyTask task, Subject subject, TaskCardListener listener) {
        this.task = task;
        this.subject = subject;
        this.listener = listener;

        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                Point p = e.getPoint();
                if (checkZone.contains(p)) {
                    if (listener != null) listener.onToggleComplete(task);
                } else if (editZone.contains(p)) {
                    if (listener != null) listener.onEdit(task);
                } else if (deleteZone.contains(p)) {
                    if (listener != null) listener.onDelete(task);
                } else {
                    if (listener != null) listener.onEdit(task);
                }
            }
        });
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(500, 78);
    }

    @Override
    protected void paintDoubleBuffered(Graphics2D g2, int w, int h) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Background
        g2.setColor(isHovered ? ColorPalette.CARD_HOVER : ColorPalette.CARD_BG);
        g2.fillRoundRect(0, 0, w - 1, h - 1, 10, 10);

        // Border
        boolean overdue = !task.isCompleted() && DateUtils.isOverdue(task.getDueDate());
        boolean dueToday = !task.isCompleted() && DateUtils.isDueToday(task.getDueDate());

        if (overdue) {
            g2.setColor(new Color(0xFE, 0xCA, 0xCA)); // Soft danger border
        } else if (dueToday) {
            g2.setColor(new Color(0xFD, 0xE6, 0x8A)); // Soft warning border
        } else {
            g2.setColor(ColorPalette.BORDER);
        }
        g2.drawRoundRect(0, 0, w - 1, h - 1, 10, 10);

        // Checkbox circle
        checkZone.setLocation(14, 18);
        g2.setColor(task.isCompleted() ? ColorPalette.SUCCESS : new Color(0xCB, 0xD5, 0xE1));
        if (task.isCompleted()) {
            g2.fillOval(checkZone.x, checkZone.y, checkZone.width, checkZone.height);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2.0f));
            g2.drawLine(checkZone.x + 5, checkZone.y + 10, checkZone.x + 9, checkZone.y + 14);
            g2.drawLine(checkZone.x + 9, checkZone.y + 14, checkZone.x + 15, checkZone.y + 6);
        } else {
            g2.setStroke(new BasicStroke(1.8f));
            g2.drawOval(checkZone.x, checkZone.y, checkZone.width, checkZone.height);
        }

        // Title
        g2.setFont(FontUtils.bold(14));
        if (task.isCompleted()) {
            g2.setColor(ColorPalette.TEXT_MUTED);
        } else {
            g2.setColor(ColorPalette.TEXT_PRIMARY);
        }

        String title = task.getTitle() != null ? task.getTitle() : "Untitled Task";
        g2.drawString(title, 44, 32);

        // Subtitle line (Subject color dot + name + Due date + Priority badge)
        int infoY = 56;
        int curX = 44;

        if (subject != null) {
            Color sColor = ColorPalette.fromHex(subject.getColorHex(), ColorPalette.PRIMARY);
            g2.setColor(sColor);
            g2.fillOval(curX, infoY - 8, 8, 8);
            curX += 12;

            g2.setFont(FontUtils.bold(11));
            g2.drawString(subject.getName(), curX, infoY);
            curX += g2.getFontMetrics().stringWidth(subject.getName()) + 14;
        }

        // Due date indicator
        if (task.getDueDate() != null) {
            String dueStr;
            Color dueColor;
            if (task.isCompleted()) {
                dueStr = "Done " + DateUtils.formatDisplay(task.getCompletionDate());
                dueColor = ColorPalette.TEXT_MUTED;
            } else if (overdue) {
                dueStr = "Overdue (" + DateUtils.formatDisplay(task.getDueDate()) + ")";
                dueColor = ColorPalette.DANGER;
            } else if (dueToday) {
                dueStr = "Due Today";
                dueColor = ColorPalette.WARNING;
            } else {
                dueStr = "Due " + DateUtils.formatDisplay(task.getDueDate());
                dueColor = ColorPalette.TEXT_SECONDARY;
            }

            g2.setFont(FontUtils.regular(11));
            g2.setColor(dueColor);
            g2.drawString(dueStr, curX, infoY);
            curX += g2.getFontMetrics().stringWidth(dueStr) + 14;
        }

        // Priority Badge
        Color pBg, pFg;
        Priority p = task.getPriority();
        if (p == Priority.URGENT) {
            pBg = new Color(0xFE, 0xE2, 0xE2);
            pFg = ColorPalette.DANGER;
        } else if (p == Priority.HIGH) {
            pBg = new Color(0xFE, 0xF3, 0xC7);
            pFg = ColorPalette.WARNING;
        } else if (p == Priority.LOW) {
            pBg = new Color(0xEC, 0xFD, 0xF5);
            pFg = ColorPalette.SUCCESS;
        } else {
            pBg = new Color(0xEB, 0xF5, 0xFF);
            pFg = ColorPalette.PRIMARY;
        }

        g2.setFont(FontUtils.bold(10));
        String pStr = p.getDisplayName().toUpperCase();
        int bWidth = g2.getFontMetrics().stringWidth(pStr) + 12;
        g2.setColor(pBg);
        g2.fillRoundRect(curX, infoY - 12, bWidth, 16, 8, 8);
        g2.setColor(pFg);
        g2.drawString(pStr, curX + 6, infoY);

        // Edit and Delete icons / buttons on right
        int btnW = 46;
        int btnH = 24;
        int by = (h - btnH) / 2;

        deleteZone = new Rectangle(w - 56, by, btnW, btnH);
        editZone = new Rectangle(w - 110, by, btnW, btnH);

        // Edit button
        g2.setColor(new Color(0xF1, 0xF5, 0xF9));
        g2.fillRoundRect(editZone.x, editZone.y, editZone.width, editZone.height, 6, 6);
        g2.setFont(FontUtils.bold(11));
        g2.setColor(ColorPalette.TEXT_SECONDARY);
        g2.drawString("Edit", editZone.x + 12, editZone.y + 16);

        // Delete button
        g2.setColor(new Color(0xFE, 0xF2, 0xF2));
        g2.fillRoundRect(deleteZone.x, deleteZone.y, deleteZone.width, deleteZone.height, 6, 6);
        g2.setColor(ColorPalette.DANGER);
        g2.drawString("Del", deleteZone.x + 14, deleteZone.y + 16);
    }
}
