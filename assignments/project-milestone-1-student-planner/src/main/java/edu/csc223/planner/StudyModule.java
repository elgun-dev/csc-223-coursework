package edu.csc223.planner;

public record StudyModule(int id, int courseId, String title, int sequence) {
    public StudyModule {
        if (id <= 0 || courseId <= 0 || sequence <= 0
                || title == null || title.isBlank()) {
            throw new IllegalArgumentException("Valid module fields required");
        }
    }
    public StudyModule retitled(String value) {
        return new StudyModule(id, courseId, value, sequence);
    }
}
