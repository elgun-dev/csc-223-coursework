package edu.csc223.planner;

import java.util.*;

/** Planner-owned schedule. Immutable entries share authoritative Assignment records. */
final class PrioritySchedule {
    record Entry(Assignment item,long sequence) {
        Entry {
            Objects.requireNonNull(item);
            if(sequence<0) throw new IllegalArgumentException("Negative sequence");
        }
    }
    // Preserve the project's established convention: 5 is highest priority.
    static final Comparator<Entry> ORDER=Comparator
            .comparingInt((Entry e)->e.item().priority()).reversed()
            .thenComparing(e->e.item().dueDate())
            .thenComparingLong(Entry::sequence).thenComparingInt(e->e.item().id());
    private BinaryHeap<Entry> heap=new BinaryHeap<>(ORDER);
    private final Map<Integer,Entry> entries=new HashMap<>();
    private long nextSequence;
    void offer(Assignment item) {
        Objects.requireNonNull(item);
        if(item.status()!=Assignment.Status.PENDING || entries.containsKey(item.id()))
            throw new IllegalArgumentException("Pending, unscheduled assignment required");
        if(nextSequence==Long.MAX_VALUE) throw new IllegalStateException("Sequence exhausted");
        var entry=new Entry(item,nextSequence++);
        heap.add(entry);entries.put(item.id(),entry);
    }
    Assignment peek() { return heap.isEmpty()?null:heap.peek().item(); }
    Assignment poll() {
        var entry=heap.removeRoot();
        if(entry==null) return null;
        entries.remove(entry.item().id());return entry.item();
    }
    int size() { return heap.size(); }
    boolean isEmpty() { return heap.isEmpty(); }
    void clear() { heap.clear();entries.clear(); }
    /** Existing enrollment survives an edit with its original arrival sequence. */
    void refresh(Assignment item) {
        var old=entries.get(item.id());
        if(old==null) return;
        if(item.status()!=Assignment.Status.PENDING) entries.remove(item.id());
        else entries.put(item.id(),new Entry(item,old.sequence()));
        rebuild();
    }
    boolean cancel(int id) {
        if(entries.remove(id)==null) return false;
        rebuild();return true;
    }
    private void rebuild() {
        heap=new BinaryHeap<>(ORDER,entries.values());
    }
    /** Detached, fully priority-ordered display; internal heap layout is not sorted. */
    List<Assignment> summary() {
        var copy=new BinaryHeap<Entry>(ORDER);
        for(var entry:heap.snapshot()) copy.add(entry);
        var result=new ArrayList<Assignment>();
        while(!copy.isEmpty()) result.add(copy.removeRoot().item());
        return List.copyOf(result);
    }
    boolean agrees(PlannerService planner) {
        if(!heap.isValidHeap() || heap.size()!=entries.size()) return false;
        var seen=new HashSet<Integer>();
        for(var entry:heap.snapshot()) {
            var a=entry.item();
            if(!seen.add(a.id()) || entries.get(a.id())!=entry
                    || !planner.containsId(a.id()) || planner.findById(a.id())!=a
                    || a.status()!=Assignment.Status.PENDING) return false;
        }
        return true;
    }
}
