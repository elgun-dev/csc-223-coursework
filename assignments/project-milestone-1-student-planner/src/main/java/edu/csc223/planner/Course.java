package edu.csc223.planner;

public record Course(int id, String name) {
    public Course {
        if (id <= 0 || name == null || name.isBlank()) {
            throw new IllegalArgumentException("Positive course ID and name required");
        }
    }
    public Course renamed(String newName) { return new Course(id, newName); }
}
