package edu.csc223.planner;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static edu.csc223.planner.Assignment.Status.*;
import java.time.LocalDate;
import java.util.NoSuchElementException;

class StudyWorkflowTest {
    private PlannerService planner() {
        var p = new PlannerService(); p.addCourse(new Course(223, "CSC"));
        p.addModule(new StudyModule(4, 223, "Workflows", 4));
        for (int i = 1; i <= 4; i++) {
            p.addAssignment(new Assignment(i, 223, 4, "Task " + i,
                LocalDate.of(2026, 10, 15), i, PENDING));
        }
        return p;
    }
    @Test void emptyWorkflowHasDefinedInspectionAndProcessing() {
        var w = new StudyWorkflow(planner()); assertTrue(w.isEmpty());
        assertEquals(0, w.size()); assertTrue(w.inspectNext().isEmpty());
        var r = w.startNext(); assertTrue(r.started().isEmpty());
        assertEquals(0, r.discarded()); assertArrayEquals(new int[0], w.queuedIds());
    }
    @Test void oneEntryIsInspectedWithoutChangingState() {
        var p = planner(); var w = new StudyWorkflow(p); w.enqueueAssignment(1);
        assertEquals(p.findById(1), w.inspectNext().orElseThrow());
        assertEquals(1, w.size()); assertEquals(PENDING, p.findById(1).status());
    }
    @Test void fourAssignmentsStartInEntryOrderAndUpdatePrimaryRecords() {
        var p = planner(); var w = new StudyWorkflow(p);
        for (int id : new int[]{3, 1, 4, 2}) { w.enqueueAssignment(id); }
        assertArrayEquals(new int[]{3, 1, 4, 2}, w.queuedIds());
        int remaining = 4;
        for (int id : new int[]{3, 1, 4, 2}) {
            var r = w.startNext(); assertEquals(id, r.started().orElseThrow().id());
            assertEquals(ACTIVE, r.started().orElseThrow().status());
            assertEquals(ACTIVE, p.findById(id).status());
            assertEquals(0, r.discarded()); assertEquals(--remaining, w.size());
        }
        assertTrue(w.isEmpty()); assertEquals(4, p.size());
        assertEquals(new PlannerService.ScanResult(0, 4), p.countPending());
    }
    @Test void duplicateEntryIsRejectedWithoutChangingOrder() {
        var w = new StudyWorkflow(planner()); w.enqueueAssignment(1); w.enqueueAssignment(2);
        assertThrows(IllegalArgumentException.class, () -> w.enqueueAssignment(1));
        assertArrayEquals(new int[]{1, 2}, w.queuedIds());
    }
    @Test void invalidOrMissingIdsAreRejectedWithoutInsertion() {
        var w = new StudyWorkflow(planner());
        assertThrows(IllegalArgumentException.class, () -> w.enqueueAssignment(0));
        assertThrows(NoSuchElementException.class, () -> w.enqueueAssignment(99));
        assertTrue(w.isEmpty());
    }
    @Test void activeAndCompletedAssignmentsCannotEnter() {
        var p = planner(); p.changeStatus(1, ACTIVE); p.changeStatus(2, COMPLETE);
        var w = new StudyWorkflow(p);
        assertThrows(IllegalArgumentException.class, () -> w.enqueueAssignment(1));
        assertThrows(IllegalArgumentException.class, () -> w.enqueueAssignment(2));
        assertTrue(w.isEmpty()); assertEquals(ACTIVE, p.findById(1).status());
    }
    @Test void resetCancelsWaitingWithoutChangingDomainStatus() {
        var p = planner(); var w = new StudyWorkflow(p);
        w.enqueueAssignment(1); w.enqueueAssignment(2); w.reset();
        assertTrue(w.isEmpty()); assertEquals(PENDING, p.findById(1).status());
        w.enqueueAssignment(3); assertEquals(3, w.startNext().started().orElseThrow().id());
    }
    @Test void completionWhileWaitingIsSkippedAndInspectionDoesNotMutate() {
        var p = planner(); var w = new StudyWorkflow(p);
        w.enqueueAssignment(1); w.enqueueAssignment(2); p.changeStatus(1, COMPLETE);
        assertEquals(2, w.inspectNext().orElseThrow().id()); assertEquals(2, w.size());
        var r = w.startNext(); assertEquals(1, r.discarded());
        assertEquals(2, r.started().orElseThrow().id()); assertTrue(w.isEmpty());
        assertEquals(COMPLETE, p.findById(1).status());
    }
    @Test void deletedAssignmentIsSkippedWithoutRecreatingIt() {
        var p = planner(); var w = new StudyWorkflow(p);
        w.enqueueAssignment(1); w.enqueueAssignment(2); p.removeById(1);
        var r = w.startNext(); assertEquals(1, r.discarded());
        assertEquals(2, r.started().orElseThrow().id()); assertEquals(3, p.size());
        assertThrows(NoSuchElementException.class, () -> p.findById(1));
    }
    @Test void allStaleEntriesProduceEmptyResultAndReusableQueue() {
        var p = planner(); var w = new StudyWorkflow(p);
        w.enqueueAssignment(1); w.enqueueAssignment(2);
        p.changeStatus(1, ACTIVE); p.changeStatus(2, COMPLETE);
        assertTrue(w.inspectNext().isEmpty()); assertEquals(2, w.size());
        var r = w.startNext(); assertTrue(r.started().isEmpty());
        assertEquals(2, r.discarded()); assertTrue(w.isEmpty());
        w.enqueueAssignment(3); assertEquals(3, w.startNext().started().orElseThrow().id());
    }
    @Test void inspectionUsesLatestDomainRecord() {
        var p = planner(); var w = new StudyWorkflow(p); w.enqueueAssignment(1);
        var updated = p.findById(1).rescheduled(LocalDate.of(2026, 11, 1));
        p.update(1, updated); assertEquals(updated, w.inspectNext().orElseThrow());
        assertEquals(updated.dueDate(), w.startNext().started().orElseThrow().dueDate());
    }
    @Test void queuedIdSnapshotCannotCorruptQueue() {
        var w = new StudyWorkflow(planner()); w.enqueueAssignment(1);
        var copy = w.queuedIds(); copy[0] = 99;
        assertArrayEquals(new int[]{1}, w.queuedIds());
    }
    @Test void nullPlannerIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new StudyWorkflow(null));
    }
}
