package edu.csc223.planner;

import java.time.LocalDate;

public record Assignment(int id, int courseId, int moduleId, String title,
                         LocalDate dueDate, int priority, Status status) {
    public enum Status { PENDING, ACTIVE, COMPLETE }
    public Assignment {
        if (id <= 0 || courseId <= 0 || moduleId <= 0
                || title == null || title.isBlank() || dueDate == null
                || priority < 1 || priority > 5 || status == null) {
            throw new IllegalArgumentException("Valid assignment fields required");
        }
    }
    public Assignment withStatus(Status value) {
        return new Assignment(id, courseId, moduleId, title, dueDate, priority, value);
    }
    public Assignment rescheduled(LocalDate value) {
        return new Assignment(id, courseId, moduleId, title, value, priority, status);
    }
    public boolean isOverdue(LocalDate today) {
        if (today == null) { throw new IllegalArgumentException("Date required"); }
        return status != Status.COMPLETE && dueDate.isBefore(today);
    }
}
