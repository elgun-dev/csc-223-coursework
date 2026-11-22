package edu.csc223.planner;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/** Comparator-oriented heap: a negative comparison means higher priority.
 * Duplicates are retained. Keys and comparator behavior must remain stable.
 * Null comparator/elements throw NullPointerException. Not thread-safe. */
public final class BinaryHeap<E> {
    private final List<E> elements=new ArrayList<>(4);
    private final Comparator<? super E> comparator;
    public BinaryHeap(Comparator<? super E> comparator) {
        this.comparator=Objects.requireNonNull(comparator,"Comparator required");
    }
    /** Copies all values, then repairs internal nodes bottom-up in O(n) time.
     * Invalid input prevents publication of the new heap. Input is not modified. */
    public BinaryHeap(Comparator<? super E> comparator, java.util.Collection<? extends E> source) {
        this(comparator);
        for(E item:Objects.requireNonNull(source,"Source required"))
            elements.add(Objects.requireNonNull(item,"Element required"));
        for(int i=size()/2-1;i>=0;i--) moveDown(i);
    }
    /** Appends to preserve complete shape, then repairs the ancestor path. */
    public void add(E element) {
        elements.add(Objects.requireNonNull(element,"Element required"));
        moveUp(size()-1);
    }
    /** Returns the current root without removal, or null when empty. */
    public E peek() { return isEmpty()?null:elements.get(0); }
    /** Removes the root, or returns null when empty. */
    public E removeRoot() {
        if(isEmpty()) return null;
        E root=elements.get(0);
        E last=elements.remove(size()-1);
        if(!isEmpty()) { elements.set(0,last);moveDown(0); }
        return root;
    }
    public int size() { return elements.size(); }
    public boolean isEmpty() { return elements.isEmpty(); }
    /** Removes references to all active elements; retained capacity may remain. */
    public void clear() { elements.clear(); }
    /** Tests every child-parent relationship; no element may be null. */
    public boolean isValidHeap() {
        if(!isEmpty() && elements.get(0)==null) return false;
        for(int child=1;child<size();child++) {
            E value=elements.get(child);
            if(value==null || hasHigherPriority(value,elements.get(parentIndex(child))))
                return false;
        }
        return true;
    }
    /** Defensive, unmodifiable heap-order snapshot; NOT a sorted traversal. */
    public List<E> snapshot() { return List.copyOf(elements); }
    private int parentIndex(int index) { return (index-1)/2; }
    private int leftChildIndex(int index) { return 2*index+1; }
    private int rightChildIndex(int index) { return 2*index+2; }
    private boolean hasHigherPriority(E first,E second) {
        return comparator.compare(first,second)<0;
    }
    private void moveUp(int index) {
        while(index>0) {
            int parent=parentIndex(index);
            if(!hasHigherPriority(elements.get(index),elements.get(parent))) break;
            swap(index,parent);index=parent;
        }
    }
    private void moveDown(int index) {
        // Only internal nodes have children; this guard also bounds index arithmetic.
        while(index<size()/2) {
            int left=leftChildIndex(index),right=rightChildIndex(index);
            int best=left;
            if(right<size() && hasHigherPriority(elements.get(right),elements.get(left)))
                best=right;
            if(!hasHigherPriority(elements.get(best),elements.get(index))) break;
            swap(index,best);index=best;
        }
    }
    private void swap(int first,int second) {
        E saved=elements.get(first);
        elements.set(first,elements.get(second));elements.set(second,saved);
    }
}
