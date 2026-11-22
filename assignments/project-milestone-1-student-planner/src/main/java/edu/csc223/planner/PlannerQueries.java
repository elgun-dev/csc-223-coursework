package edu.csc223.planner;

import java.util.Objects;

/** FR-03 search and ordered views; primary storage and queue are untouched.
 * Not thread-safe. Each operation obtains a fresh planner snapshot.
 */
public final class PlannerQueries {
    private final PlannerService planner;
    public enum Algorithm { INSERTION, MERGE }
    public PlannerQueries(PlannerService planner) {
        this.planner = Objects.requireNonNull(planner);
    }
    /** Snapshot O(n) precedes the sequential search. */
    public AssignmentSearch.Result sequential(int id) {
        return AssignmentSearch.sequential(planner.snapshot(), id);
    }
    /** Establish ID order once; caller may reuse until fresh data is needed. */
    public AssignmentSearch.IdView prepareIds() {
        return AssignmentSearch.prepare(planner.snapshot());
    }
    /** One-shot current search includes copying and sorting, not just O(log n). */
    public AssignmentSearch.Result binaryCurrent(int id) {
        return AssignmentSearch.binary(prepareIds(), id);
    }
    public static final class OrderedView {
        private final Assignment[] values;
        private final SortMetrics metrics;
        private OrderedView(Assignment[] values, SortMetrics metrics) {
            this.values=values; this.metrics=metrics;
        }
        public Assignment[] snapshot() { return values.clone(); }
        public SortMetrics metrics() { return metrics; }
    }
    /** Sorts a fresh copy; null criterion/algorithm throws NPE. */
    public OrderedView ordered(AssignmentOrderings.Criterion criterion,
                               Algorithm algorithm) {
        var comparator = AssignmentOrderings.forCriterion(criterion);
        Objects.requireNonNull(algorithm);
        Assignment[] values = planner.snapshot();
        SortMetrics metrics = algorithm == Algorithm.INSERTION
                ? SortingAlgorithms.insertionSort(values, comparator)
                : SortingAlgorithms.mergeSort(values, comparator);
        return new OrderedView(values, metrics);
    }
}
