package edu.csc223.planner;

/** Algorithm-specific movements; depth counts nonempty recursive frames.
 * swaps excludes self-swaps; shifts counts insertion right shifts;
 * copies counts merge writes to auxiliary and original arrays.
 * Insertion key placement is not a shift; swaps are not also copies.
 */
public record SortMetrics(long comparisons, long swaps, long shifts,
                          long copies, int maximumDepth) { }
