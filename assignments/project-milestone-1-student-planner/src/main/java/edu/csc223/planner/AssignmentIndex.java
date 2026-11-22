package edu.csc223.planner;

import java.util.Optional;

/** Separate-chaining ID map. Single-threaded; null records are rejected. */
public final class AssignmentIndex {
    private static final class Node {
        Assignment value; Node next;
        Node(Assignment value, Node next) { this.value=value; this.next=next; }
    }
    private Node[] buckets=new Node[8];
    private int size;
    public record Lookup(Optional<Assignment> match, int comparisons) { }
    public int size() { return size; }
    public boolean isEmpty() { return size==0; }
    public int capacity() { return buckets.length; }
    public double loadFactor() { return (double)size/capacity(); }
    private int slot(int id) { return Math.floorMod(Integer.hashCode(id),capacity()); }
    private void valid(int id) {
        if(id<=0) throw new IllegalArgumentException("Positive ID required");
    }
    /** Counts actual node-key comparisons; bucket calculation is excluded. */
    public Lookup lookup(int id) {
        valid(id); int count=0;
        for(Node p=buckets[slot(id)];p!=null;p=p.next) {
            count++;
            if(p.value.id()==id) return new Lookup(Optional.of(p.value),count);
        }
        return new Lookup(Optional.empty(),count);
    }
    public boolean contains(int id) { return lookup(id).match().isPresent(); }
    /** Rejects duplicates without replacement. Rehashes before exceeding 0.75. */
    public void insert(Assignment value) {
        if(value==null) throw new IllegalArgumentException("Record required");
        if(contains(value.id())) throw new IllegalArgumentException("Duplicate assignment ID");
        if(size+1>capacity()*0.75) grow();
        int slot=slot(value.id()); buckets[slot]=new Node(value,buckets[slot]); size++;
    }
    /** Returns old value, or empty when missing; never inserts on update. */
    public Optional<Assignment> replace(int id,Assignment value) {
        valid(id);
        if(value==null || value.id()!=id)
            throw new IllegalArgumentException("Replacement must retain ID");
        for(Node p=buckets[slot(id)];p!=null;p=p.next) {
            if(p.value.id()==id) {
                Assignment old=p.value; p.value=value; return Optional.of(old);
            }
        }
        return Optional.empty();
    }
    public Optional<Assignment> remove(int id) {
        valid(id); int slot=slot(id); Node previous=null;
        for(Node p=buckets[slot];p!=null;p=p.next) {
            if(p.value.id()==id) {
                if(previous==null) buckets[slot]=p.next; else previous.next=p.next;
                size--; return Optional.of(p.value);
            }
            previous=p;
        }
        return Optional.empty();
    }
    /** Unordered defensive array of immutable records. */
    public Assignment[] snapshot() {
        Assignment[] result=new Assignment[size]; int i=0;
        for(Node bucket:buckets)
            for(Node p=bucket;p!=null;p=p.next) result[i++]=p.value;
        return result;
    }
    public void clear() { buckets=new Node[8]; size=0; }
    private void grow() {
        if(capacity()>Integer.MAX_VALUE/2)
            throw new IllegalStateException("Maximum table capacity reached");
        Node[] old=buckets; buckets=new Node[old.length*2];
        for(Node bucket:old) {
            for(Node p=bucket;p!=null;) {
                Node next=p.next; int slot=slot(p.value.id());
                p.next=buckets[slot]; buckets[slot]=p; p=next;
            }
        }
    }
}
