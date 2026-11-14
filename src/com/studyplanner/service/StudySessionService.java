package com.studyplanner.service;

import com.studyplanner.model.StudySession;
import com.studyplanner.util.DateUtils;

import java.util.*;

/**
 * Service managing Pomodoro and focus session history.
 */
public class StudySessionService {
    private final DataService dataService;
    private final List<StudySession> sessions;

    public StudySessionService(DataService dataService) {
        this.dataService = dataService;
        this.sessions = new ArrayList<>(dataService.loadSessions());
    }

    public synchronized List<StudySession> getAllSessions() {
        return new ArrayList<>(sessions);
    }

    public synchronized void addSession(StudySession session) {
        if (session != null) {
            sessions.add(session);
            saveAll();
        }
    }

    public synchronized int getTodayStudyMinutes() {
        int total = 0;
        for (StudySession s : sessions) {
            if ("FOCUS".equalsIgnoreCase(s.getSessionType()) && DateUtils.isDueToday(s.getStartTime())) {
                total += s.getDurationMinutes();
            }
        }
        return total;
    }

    public synchronized int getTodaySessionCount() {
        int count = 0;
        for (StudySession s : sessions) {
            if ("FOCUS".equalsIgnoreCase(s.getSessionType()) && DateUtils.isDueToday(s.getStartTime())) {
                count++;
            }
        }
        return count;
    }

    public synchronized int getThisWeekStudyMinutes(int weekStartDay) {
        int total = 0;
        for (StudySession s : sessions) {
            if ("FOCUS".equalsIgnoreCase(s.getSessionType()) && DateUtils.isThisWeek(s.getStartTime(), weekStartDay)) {
                total += s.getDurationMinutes();
            }
        }
        return total;
    }

    public synchronized int[] getWeeklyMinutesByDay(int weekStartDay) {
        // Returns 7 values (Mon through Sun or Sun through Sat)
        int[] result = new int[7];
        Calendar c = Calendar.getInstance();
        DateUtils.clearTime(c);
        c.set(Calendar.DAY_OF_WEEK, weekStartDay);
        if (c.getTime().after(DateUtils.today())) {
            c.add(Calendar.DAY_OF_YEAR, -7);
        }

        Date[] days = new Date[7];
        for (int i = 0; i < 7; i++) {
            days[i] = c.getTime();
            c.add(Calendar.DAY_OF_YEAR, 1);
        }

        for (StudySession s : sessions) {
            if (!"FOCUS".equalsIgnoreCase(s.getSessionType()) || s.getStartTime() == null) continue;
            for (int i = 0; i < 7; i++) {
                if (DateUtils.isSameDay(s.getStartTime(), days[i])) {
                    result[i] += s.getDurationMinutes();
                    break;
                }
            }
        }
        return result;
    }

    public synchronized Map<String, Integer> getMinutesBySubject() {
        Map<String, Integer> map = new HashMap<>();
        for (StudySession s : sessions) {
            if ("FOCUS".equalsIgnoreCase(s.getSessionType()) && s.getSubjectId() != null) {
                map.put(s.getSubjectId(), map.getOrDefault(s.getSubjectId(), 0) + s.getDurationMinutes());
            }
        }
        return map;
    }

    public synchronized void saveAll() {
        dataService.saveSessions(sessions);
    }
}
