package edu.csc223.planner;
import java.util.*;
/** Milestone 8/9 repeated-insertion rebuild, retained solely for comparison. */
public final class HeapBuildBaseline {
    private HeapBuildBaseline() { }
    public static <E> BinaryHeap<E> build(Comparator<? super E> order,Collection<? extends E> values) {
        var heap=new BinaryHeap<E>(order);
        for(E value:values) heap.add(value);
        return heap;
    }
}
