package com.studyplanner.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Robust date formatting and calendar calculation utilities.
 */
public class DateUtils {
    public static final SimpleDateFormat ISO_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    public static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final SimpleDateFormat DISPLAY_FORMAT = new SimpleDateFormat("MMM d, yyyy");
    public static final SimpleDateFormat FULL_FORMAT = new SimpleDateFormat("EEEE, MMMM d, yyyy");
    public static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("h:mm a");

    public static synchronized String formatISO(Date date) {
        if (date == null) return "";
        return ISO_FORMAT.format(date);
    }

    public static synchronized String formatDateTime(Date date) {
        if (date == null) return "";
        return DATETIME_FORMAT.format(date);
    }

    public static synchronized Date parseDateTime(String text) {
        if (text == null || text.trim().isEmpty()) return null;
        try {
            return DATETIME_FORMAT.parse(text.trim());
        } catch (ParseException e) {
            return parseISO(text);
        }
    }

    public static synchronized String formatDisplay(Date date) {
        if (date == null) return "-";
        return DISPLAY_FORMAT.format(date);
    }

    public static synchronized String formatFull(Date date) {
        if (date == null) return "-";
        return FULL_FORMAT.format(date);
    }

    public static synchronized Date parseISO(String text) {
        if (text == null || text.trim().isEmpty()) return null;
        try {
            return ISO_FORMAT.parse(text.trim());
        } catch (ParseException e) {
            return null;
        }
    }

    public static Date today() {
        Calendar c = Calendar.getInstance();
        clearTime(c);
        return c.getTime();
    }

    public static void clearTime(Calendar c) {
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
    }

    public static boolean isSameDay(Date d1, Date d2) {
        if (d1 == null || d2 == null) return false;
        Calendar c1 = Calendar.getInstance();
        c1.setTime(d1);
        Calendar c2 = Calendar.getInstance();
        c2.setTime(d2);
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
               c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
    }

    public static boolean isDueToday(Date dueDate) {
        return isSameDay(dueDate, today());
    }

    public static boolean isOverdue(Date dueDate) {
        if (dueDate == null) return false;
        Calendar due = Calendar.getInstance();
        due.setTime(dueDate);
        clearTime(due);

        Calendar now = Calendar.getInstance();
        clearTime(now);

        return due.before(now);
    }

    public static boolean isThisWeek(Date date, int startDayOfWeek) {
        if (date == null) return false;
        Calendar start = Calendar.getInstance();
        clearTime(start);
        start.set(Calendar.DAY_OF_WEEK, startDayOfWeek);
        if (start.getTime().after(today())) {
            start.add(Calendar.DAY_OF_YEAR, -7);
        }

        Calendar end = (Calendar) start.clone();
        end.add(Calendar.DAY_OF_YEAR, 7);

        return !date.before(start.getTime()) && date.before(end.getTime());
    }

    public static String getDayName(Date date) {
        if (date == null) return "";
        return new SimpleDateFormat("EEEE").format(date);
    }

    public static String getMonthName(int monthZeroIndexed) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.MONTH, monthZeroIndexed);
        c.set(Calendar.DAY_OF_MONTH, 1);
        return new SimpleDateFormat("MMMM").format(c.getTime());
    }

    public static Date addDays(Date date, int days) {
        if (date == null) return null;
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DAY_OF_MONTH, days);
        return c.getTime();
    }
}
