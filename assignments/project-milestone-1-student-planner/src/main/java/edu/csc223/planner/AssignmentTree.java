package edu.csc223.planner;

import java.util.List;
import java.util.Optional;

/** ID-ordered derived index; records are immutable and shared with the service. */
public final class AssignmentTree {
    private record Entry(int id,Assignment value) implements Comparable<Entry> {
        public int compareTo(Entry other) { return Integer.compare(id,other.id); }
    }
    public enum Order { PREORDER, INORDER, POSTORDER, LEVEL }
    public record Lookup(Optional<Assignment> match,int comparisons) { }
    private final BinarySearchTree<Entry> tree=new BinarySearchTree<>();
    private Entry key(int id) {
        if(id<=0) throw new IllegalArgumentException("Positive ID required");
        return new Entry(id,null); // Private search-only key, never inserted.
    }
    private Entry entry(Assignment value) {
        if(value==null) throw new IllegalArgumentException("Record required");
        return new Entry(value.id(),value);
    }
    public int size() { return tree.size(); }
    public boolean isEmpty() { return tree.isEmpty(); }
    public int height() { return tree.height(); }
    /** Duplicate IDs return false and preserve the original reference. */
    public boolean add(Assignment value) { return tree.add(entry(value)); }
    public Lookup lookup(int id) {
        var result=tree.lookup(key(id));
        return new Lookup(result.match().map(Entry::value),result.comparisons());
    }
    public boolean contains(int id) { return lookup(id).match().isPresent(); }
    /** Missing returns empty; a changed ID is rejected before mutation. */
    public Optional<Assignment> replace(int id,Assignment value) {
        return tree.replace(key(id),entry(value)).map(Entry::value);
    }
    public boolean remove(int id) { return tree.remove(key(id)); }
    public Assignment minimum() { return tree.minimum().value(); }
    public Assignment maximum() { return tree.maximum().value(); }
    /** Returns a detached, unmodifiable list of current immutable values. */
    public List<Assignment> traverse(Order order) {
        if(order==null) throw new IllegalArgumentException("Traversal required");
        List<Entry> values=switch(order) {
            case PREORDER -> tree.preorder();case INORDER -> tree.inorder();
            case POSTORDER -> tree.postorder();case LEVEL -> tree.levelOrder();
        };
        return values.stream().map(Entry::value).toList();
    }
    /** Inclusive ID range; requires positive, increasing bounds. */
    public List<Assignment> range(int lower,int upper) {
        return tree.range(key(lower),key(upper)).stream().map(Entry::value).toList();
    }
    public void clear() { tree.clear(); }
    boolean isValid() { return tree.isValidBst(); }
}
