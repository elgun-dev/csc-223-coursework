package edu.csc223.planner;

import java.util.Comparator;
import java.util.Objects;

/** In-place generic sorts. Each accepts values and an ordering comparator,
 * returns fresh metrics, and modifies values. Null arrays/comparators throw
 * NPE. Null elements require a null-aware comparator. No sorted precondition.
 * Comparator failures may leave a partial sort. Counters are local per call.
 * Comparisons count comparator calls only; movement definitions are in
 * SortMetrics. Auxiliary allocation is included in the method's runtime.
 */
public final class SortingAlgorithms {
    private SortingAlgorithms() { }
    private static final class Counts {
        long comparisons, swaps, shifts, copies;
        int depth;
        SortMetrics result() {
            return new SortMetrics(comparisons, swaps, shifts, copies, depth);
        }
    }
    private static void validate(Object[] a, Comparator<?> c) {
        Objects.requireNonNull(a, "values");
        Objects.requireNonNull(c, "comparator");
    }
    private static <E> int compare(E a, E b, Comparator<? super E> c,
                                  Counts m) {
        m.comparisons++;
        return c.compare(a, b);
    }

    /** Stable insertion; only strictly greater elements shift right.
     * Uses O(1) auxiliary storage; maximumDepth is always zero.
     */
    public static <E> SortMetrics insertionSort(E[] values,
            Comparator<? super E> comparator) {
        validate(values, comparator);
        Counts m = new Counts();
        for (int i = 1; i < values.length; i++) {
            E key = values[i];
            int j = i - 1;
            while (j >= 0) {
                if (compare(values[j], key, comparator, m) <= 0) break;
                values[j + 1] = values[j];
                m.shifts++;
                j--;
            }
            values[j + 1] = key;
        }
        return m.result();
    }

    /** Stable top-down merge; equality selects left first.
     * One auxiliary array; every merge copies out and back.
     * Empty depth is zero; singleton/root nonempty depth is one.
     */
    public static <E> SortMetrics mergeSort(E[] values,
            Comparator<? super E> comparator) {
        validate(values, comparator);
        Counts m = new Counts();
        Object[] aux = new Object[values.length];
        merge(values, aux, 0, values.length - 1, comparator, m, 1);
        return m.result();
    }
    private static <E> void merge(E[] a, Object[] aux, int lo, int hi,
            Comparator<? super E> c, Counts m, int depth) {
        if (lo > hi) return;
        m.depth = Math.max(m.depth, depth);
        if (lo == hi) return;
        int mid = lo + (hi - lo) / 2;
        merge(a, aux, lo, mid, c, m, depth + 1);
        merge(a, aux, mid + 1, hi, c, m, depth + 1);
        int left = lo, right = mid + 1;
        for (int out = lo; out <= hi; out++) {
            if (left > mid) aux[out] = a[right++];
            else if (right > hi) aux[out] = a[left++];
            else if (compare(a[left], a[right], c, m) <= 0)
                aux[out] = a[left++];
            else aux[out] = a[right++];
            m.copies++;
        }
        for (int out = lo; out <= hi; out++) {
            // Each auxiliary value originated in this E[] input.
            @SuppressWarnings("unchecked") E value = (E) aux[out];
            a[out] = value;
            m.copies++;
        }
    }

}
