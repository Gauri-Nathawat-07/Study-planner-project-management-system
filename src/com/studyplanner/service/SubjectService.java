package com.studyplanner.service;

import com.studyplanner.model.Subject;

import java.util.ArrayList;
import java.util.List;

/**
 * Service managing subjects/courses, color associations, and study hour tallies.
 */
public class SubjectService {
    private final DataService dataService;
    private final List<Subject> subjects;

    public SubjectService(DataService dataService) {
        this.dataService = dataService;
        this.subjects = new ArrayList<>(dataService.loadSubjects());

        // Initialize starter subjects if totally empty
        if (this.subjects.isEmpty()) {
            initDefaults();
        }
    }

    private void initDefaults() {
        subjects.add(new Subject("Mathematics", "Calculus and Linear Algebra", "#3B82F6", 6));
        subjects.add(new Subject("Computer Science", "Algorithms and Data Structures", "#8B5CF6", 8));
        subjects.add(new Subject("Physics", "Mechanics and Thermodynamics", "#10B981", 4));
        saveAll();
    }

    public synchronized List<Subject> getAllSubjects() {
        return new ArrayList<>(subjects);
    }

    public synchronized Subject getSubjectById(String id) {
        if (id == null) return null;
        for (Subject s : subjects) {
            if (id.equals(s.getId())) return s;
        }
        return null;
    }

    public synchronized void addSubject(Subject subject) {
        if (subject != null) {
            subjects.add(subject);
            saveAll();
        }
    }

    public synchronized void updateSubject(Subject subject) {
        if (subject == null) return;
        for (int i = 0; i < subjects.size(); i++) {
            if (subjects.get(i).getId().equals(subject.getId())) {
                subjects.set(i, subject);
                saveAll();
                return;
            }
        }
    }

    public synchronized boolean deleteSubject(String id) {
        if (id == null) return false;
        boolean removed = subjects.removeIf(s -> id.equals(s.getId()));
        if (removed) {
            saveAll();
        }
        return removed;
    }

    public synchronized void addStudyMinutes(String subjectId, int minutes) {
        Subject s = getSubjectById(subjectId);
        if (s != null && minutes > 0) {
            s.addStudyMinutes(minutes);
            saveAll();
        }
    }

    public synchronized void saveAll() {
        dataService.saveSubjects(subjects);
    }
}
