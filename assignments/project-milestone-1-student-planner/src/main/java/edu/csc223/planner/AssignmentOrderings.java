package edu.csc223.planner;

import java.util.Comparator;

/** Reusable view orderings; stored insertion order remains unchanged. */
public final class AssignmentOrderings {
    private AssignmentOrderings() { }
    public enum Criterion { ID, DUE, PRIORITY }
    public static final Comparator<Assignment> BY_ID =
            Comparator.comparingInt(Assignment::id);
    public static final Comparator<Assignment> BY_DUE_THEN_ID =
            Comparator.comparing(Assignment::dueDate).thenComparing(BY_ID);
    public static final Comparator<Assignment> BY_PRIORITY_THEN_ID =
            Comparator.comparingInt(Assignment::priority).reversed()
                    .thenComparing(BY_ID);
    public static Comparator<Assignment> forCriterion(Criterion criterion) {
        return switch (java.util.Objects.requireNonNull(criterion)) {
            case ID -> BY_ID;
            case DUE -> BY_DUE_THEN_ID;
            case PRIORITY -> BY_PRIORITY_THEN_ID;
        };
    }
}
