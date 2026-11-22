package edu.csc223.planner;

import java.util.NoSuchElementException;
import java.util.Optional;
import static edu.csc223.planner.Assignment.Status.*;

/** Single-threaded FIFO study session; authoritative assignments stay in planner. */
public final class StudyWorkflow {
    private final PlannerService planner;
    private record Waiting(int id,long generation) { }
    private final LinkedQueue<Waiting> waiting = new LinkedQueue<>();
    private final java.util.function.IntPredicate allowed;
    public record StartResult(Optional<Assignment> started, int discarded) { }

    public StudyWorkflow(PlannerService planner) {
        this(planner,id->true);
    }
    public StudyWorkflow(PlannerService planner,java.util.function.IntPredicate allowed) {
        if(allowed==null) throw new IllegalArgumentException("Eligibility rule required");
        this.allowed=allowed;
        if (planner == null) { throw new IllegalArgumentException("Planner required"); }
        this.planner = planner;
    }
    public int size() { return waiting.size(); }
    public boolean isEmpty() { return waiting.isEmpty(); }
    public void enqueueAssignment(int id) {
        Assignment item = planner.findById(id);
        if (item.status() != PENDING || !allowed.test(id)) {
            throw new IllegalArgumentException("Only pending assignments may enter");
        }
        if (java.util.Arrays.stream(queuedIds()).anyMatch(value->value==id)) {
            throw new IllegalArgumentException("Assignment is already queued");
        }
        waiting.enqueue(new Waiting(id,planner.generation(id)));
    }
    /** Returns queued IDs, including any stale entries, without exposing storage. */
    public int[] queuedIds() {
        Object[] raw = waiting.snapshot();
        int[] copy = new int[raw.length];
        for (int i = 0; i < raw.length; i++) { copy[i] = ((Waiting) raw[i]).id(); }
        return copy;
    }
    /** Non-mutating view of the earliest currently eligible assignment. */
    public Optional<Assignment> inspectNext() {
        for (Object raw : waiting.snapshot()) {
            Optional<Assignment> item = eligible((Waiting)raw);
            if (item.isPresent()) { return item; }
        }
        return Optional.empty();
    }
    /** Skips stale entries; starts the earliest eligible record and removes its ID. */
    public StartResult startNext() {
        int discarded = 0;
        while (!waiting.isEmpty()) {
            Waiting entry = waiting.peek();
            int id=entry.id();
            Optional<Assignment> item = eligible(entry);
            if (item.isEmpty()) {
                waiting.dequeue(); discarded++; continue;
            }
            Assignment started = item.get().withStatus(ACTIVE);
            planner.changeStatus(id, ACTIVE);
            waiting.dequeue();
            return new StartResult(Optional.of(started), discarded);
        }
        return new StartResult(Optional.empty(), discarded);
    }
    /** Cancels waiting entries without changing assignment statuses. */
    public void reset() { waiting.clear(); }
    private Optional<Assignment> eligible(Waiting entry) {
        int id=entry.id();
        try {
            Assignment item = planner.findById(id);
            return planner.generation(id)==entry.generation() && item.status() == PENDING && allowed.test(id) ? Optional.of(item) : Optional.empty();
        } catch (NoSuchElementException ex) {
            return Optional.empty();
        }
    }
}
