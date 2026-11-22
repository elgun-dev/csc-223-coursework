package edu.csc223.planner;

import java.util.Objects;
import java.util.Optional;

/** ID searches return a matching immutable record or Optional.empty.
 * Null arrays, null encountered records, and null views throw NPE.
 * Nonpositive target IDs throw IllegalArgumentException. No input mutation.
 */
public final class AssignmentSearch {
    private AssignmentSearch() { }
    public record Result(Optional<Assignment> match, long comparisons) { }
    private static void target(int id) {
        if (id <= 0) throw new IllegalArgumentException("Positive ID required");
    }
    /** Returns first matching ID in values; counts one key comparison per
     * visited record. No ordering precondition; nulls are checked on access.
     */
    public static Result sequential(Assignment[] values, int id) {
        Objects.requireNonNull(values); target(id);
        long count = 0;
        for (Assignment item : values) {
            Objects.requireNonNull(item);
            count++;
            if (Integer.compare(item.id(), id) == 0)
                return new Result(Optional.of(item), count);
        }
        return new Result(Optional.empty(), count);
    }
    /** Immutable detached ID-sorted snapshot. Creation establishes the
     * precondition; callers cannot supply a different comparator or mutate it.
     * It is a historical view, not automatically refreshed after planner edits.
     */
    public static final class IdView {
        private final Assignment[] values;
        private final SortMetrics preparation;
        private IdView(Assignment[] source) {
            values = Objects.requireNonNull(source).clone();
            for (Assignment item : values) Objects.requireNonNull(item);
            preparation = SortingAlgorithms.mergeSort(values,
                    AssignmentOrderings.BY_ID);
        }
        public SortMetrics preparation() { return preparation; }
        public int size() { return values.length; }
        /** Defensive copy for display; costs O(n) time and space. */
        public Assignment[] snapshot() { return values.clone(); }
    }
    /** Copies source, rejects all null records, and sorts by ascending ID. */
    public static IdView prepare(Assignment[] source) {
        return new IdView(source);
    }
    /** Any equivalent ID may be returned for an external duplicate-ID array.
     * PlannerService forbids duplicate IDs, so integrated matches are unique.
     * Counts midpoint comparisons only, excluding view preparation.
     */
    public static Result binary(IdView view, int id) {
        Objects.requireNonNull(view); target(id);
        int lo = 0, hi = view.values.length - 1;
        long count = 0;
        while (lo <= hi) {
            int mid = lo + (hi - lo) / 2;
            Assignment item = view.values[mid];
            int comparison = Integer.compare(item.id(), id);
            count++;
            if (comparison == 0) return new Result(Optional.of(item), count);
            if (comparison < 0) lo = mid + 1;
            else hi = mid - 1;
        }
        return new Result(Optional.empty(), count);
    }
}
