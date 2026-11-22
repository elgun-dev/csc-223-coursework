package edu.csc223.planner;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static edu.csc223.planner.Assignment.Status.*;
import java.time.LocalDate;
import java.util.NoSuchElementException;

class PlannerTest {
    private final LocalDate due = LocalDate.of(2026, 10, 1);
    private PlannerService planner() {
        var p = new PlannerService(); p.addCourse(new Course(223, "CSC"));
        p.addModule(new StudyModule(3, 223, "Sequences", 3)); return p;
    }
    private Assignment item(int id) {
        return new Assignment(id, 223, 3, "Task " + id, due, 3, PENDING);
    }
    private PlannerService three() {
        var p = planner(); for (int i = 1; i <= 3; i++) { p.addAssignment(item(i)); }
        return p;
    }
    private void ids(PlannerService p, int... expected) {
        assertEquals(expected.length, p.size());
        assertArrayEquals(expected,
            java.util.Arrays.stream(p.snapshot()).mapToInt(Assignment::id).toArray());
    }
    @Test void validDomainConstructionAccessorsAndEquality() {
        var c = new Course(223, "CSC"); assertEquals(223, c.id());
        assertEquals("CSC", c.name()); assertEquals(new Course(223, "CSC"), c);
        var m = new StudyModule(3, 223, "Sequences", 3);
        assertEquals(223, m.courseId()); assertEquals(3, m.sequence());
        assertEquals(new StudyModule(3, 223, "Sequences", 3), m);
        assertEquals(item(1), item(1)); assertEquals(due, item(1).dueDate());
        assertNotEquals(item(1), item(1).withStatus(ACTIVE));
        assertTrue(item(1).toString().contains("Task 1"));
    }
    @Test void courseAndModuleValidation() {
        assertThrows(IllegalArgumentException.class, () -> new Course(0, "CSC"));
        assertThrows(IllegalArgumentException.class, () -> new Course(1, null));
        assertThrows(IllegalArgumentException.class, () -> new Course(1, " "));
        assertThrows(IllegalArgumentException.class,
            () -> new StudyModule(0, 223, "M", 1));
        assertThrows(IllegalArgumentException.class,
            () -> new StudyModule(1, 0, "M", 1));
        assertThrows(IllegalArgumentException.class,
            () -> new StudyModule(1, 223, "M", 0));
        assertThrows(IllegalArgumentException.class,
            () -> new StudyModule(1, 223, null, 1));
    }
    @Test void invalidAssignmentValues() {
        assertThrows(IllegalArgumentException.class,
            () -> new Assignment(0, 223, 3, "T", due, 3, PENDING));
        assertThrows(IllegalArgumentException.class,
            () -> new Assignment(1, 0, 3, "T", due, 3, PENDING));
        assertThrows(IllegalArgumentException.class,
            () -> new Assignment(1, 223, 0, "T", due, 3, PENDING));
        for (String title : new String[]{null, "", " "}) {
            assertThrows(IllegalArgumentException.class,
                () -> new Assignment(1, 223, 3, title, due, 3, PENDING));
        }
        for (int priority : new int[]{0, 6}) {
            assertThrows(IllegalArgumentException.class,
                () -> new Assignment(1, 223, 3, "T", due, priority, PENDING));
        }
        assertThrows(IllegalArgumentException.class, () -> item(1).rescheduled(null));
        assertThrows(IllegalArgumentException.class, () -> item(1).withStatus(null));
    }
    @Test void immutableControlledChangesPreserveIdentifiers() {
        var c = new Course(223, "CSC"); assertEquals(223, c.renamed("New").id());
        assertEquals("CSC", c.name());
        var m = new StudyModule(3, 223, "Old", 3);
        assertEquals(3, m.retitled("New").id()); assertEquals("Old", m.title());
        var a = item(1); assertEquals(1, a.rescheduled(due.plusDays(1)).id());
        assertEquals(PENDING, a.status()); assertEquals(ACTIVE, a.withStatus(ACTIVE).status());
    }
    @Test void overdueBoundaryAndCompletedBehavior() {
        assertFalse(item(1).isOverdue(due));
        assertTrue(item(1).isOverdue(due.plusDays(1)));
        assertFalse(item(1).withStatus(COMPLETE).isOverdue(due.plusDays(1)));
        assertThrows(IllegalArgumentException.class, () -> item(1).isOverdue(null));
    }
    @Test void priorityBoundariesAccepted() {
        assertEquals(1, new Assignment(1, 223, 3, "T", due, 1, PENDING).priority());
        assertEquals(5, new Assignment(1, 223, 3, "T", due, 5, PENDING).priority());
    }
    @Test void emptyTraversalAndSummary() {
        var p = planner(); ids(p); assertEquals(new PlannerService.ScanResult(0, 0), p.countPending());
    }
    @Test void addToEmptyAndRetrieve() {
        var p = planner(); p.addAssignment(item(1)); ids(p, 1);
        assertEquals(item(1), p.get(0)); assertEquals(item(1), p.findById(1));
    }
    @Test void multipleAddsAndSeveralGrowthEvents() {
        var p = planner();
        for (int i = 1; i <= 33; i++) { p.addAssignment(item(i)); }
        assertEquals(33, p.size());
        for (int i = 0; i < p.size(); i++) { assertEquals(i + 1, p.get(i).id()); }
        assertEquals(new PlannerService.ScanResult(33, 33), p.countPending());
    }
    @Test void missingIdentifiersDoNotChangeCollection() {
        var p = three();
        assertThrows(NoSuchElementException.class, () -> p.findById(99));
        assertThrows(NoSuchElementException.class, () -> p.removeById(99));
        assertThrows(NoSuchElementException.class, () -> p.update(99, item(99)));
        ids(p, 1, 2, 3);
    }
    @Test void updatesReturnPreviousAndPreserveOrder() {
        var p = three(); var revised = item(2).rescheduled(due.plusDays(2));
        assertEquals(item(2), p.update(2, revised)); assertEquals(revised, p.findById(2));
        assertEquals(item(1), p.changeStatus(1, COMPLETE)); ids(p, 1, 2, 3);
        assertEquals(new PlannerService.ScanResult(2, 3), p.countPending());
    }
    @Test void removeFirst() {
        var p = three(); assertEquals(item(1), p.removeById(1)); ids(p, 2, 3);
        assertThrows(NoSuchElementException.class, () -> p.findById(1));
    }
    @Test void removeMiddle() {
        var p = three(); assertEquals(item(2), p.removeById(2)); ids(p, 1, 3);
        assertThrows(NoSuchElementException.class, () -> p.findById(2));
    }
    @Test void removeFinal() {
        var p = three(); assertEquals(item(3), p.removeById(3)); ids(p, 1, 2);
        assertThrows(NoSuchElementException.class, () -> p.findById(3));
    }
    @Test void removeOnlyAndReuse() {
        var p = planner(); p.addAssignment(item(1)); p.removeById(1); ids(p);
        p.addAssignment(item(2)); ids(p, 2);
    }
    @Test void invalidIndicesAndIdsLeaveStateUnchanged() {
        var p = three();
        assertThrows(IndexOutOfBoundsException.class, () -> p.get(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> p.get(3));
        assertThrows(IllegalArgumentException.class, () -> p.findById(0));
        assertThrows(IllegalArgumentException.class, () -> p.removeById(-1));
        ids(p, 1, 2, 3);
    }
    @Test void duplicatesRejectedIncludingChangedFields() {
        var p = three();
        assertThrows(IllegalArgumentException.class,
            () -> p.addAssignment(item(1).withStatus(COMPLETE)));
        assertThrows(IllegalArgumentException.class, () -> p.addCourse(new Course(223, "Other")));
        assertThrows(IllegalArgumentException.class,
            () -> p.addModule(new StudyModule(3, 223, "Other", 4)));
        ids(p, 1, 2, 3); assertEquals(item(1), p.findById(1));
    }
    @Test void parentRelationshipsMustExistAndAgree() {
        var p = planner();
        assertThrows(IllegalArgumentException.class,
            () -> p.addModule(new StudyModule(4, 999, "Orphan", 4)));
        assertThrows(IllegalArgumentException.class,
            () -> p.addAssignment(new Assignment(1, 223, 99, "T", due, 3, PENDING)));
        assertThrows(IllegalArgumentException.class,
            () -> p.addAssignment(new Assignment(1, 999, 3, "T", due, 3, PENDING)));
        ids(p);
    }
    @Test void invalidUpdatesAreAtomic() {
        var p = three();
        assertThrows(IllegalArgumentException.class, () -> p.update(1, item(2)));
        assertThrows(IllegalArgumentException.class, () -> p.update(1, null));
        assertThrows(IllegalArgumentException.class, () -> p.changeStatus(1, null));
        assertThrows(IllegalArgumentException.class,
            () -> p.update(1, new Assignment(1, 223, 99, "T", due, 3, PENDING)));
        ids(p, 1, 2, 3); assertEquals(item(1), p.findById(1));
    }
    @Test void snapshotCannotModifyInternalStorage() {
        var p = three(); var copy = p.snapshot(); copy[0] = null;
        assertEquals(item(1), p.get(0)); ids(p, 1, 2, 3);
    }
    @Test void mixedStatusBaselineAndNoMatch() {
        var p = three(); p.changeStatus(2, ACTIVE); p.changeStatus(3, COMPLETE);
        assertEquals(new PlannerService.ScanResult(1, 3), p.countPending());
        p.changeStatus(1, COMPLETE);
        assertEquals(new PlannerService.ScanResult(0, 3), p.countPending());
    }
    @Test void nullAddsAreRejected() {
        var p = planner();
        assertThrows(IllegalArgumentException.class, () -> p.addCourse(null));
        assertThrows(IllegalArgumentException.class, () -> p.addModule(null));
        assertThrows(IllegalArgumentException.class, () -> p.addAssignment(null)); ids(p);
    }
    @Test void storageReleasesRemovedReferenceAndChecksBounds() throws Exception {
        var s = new SequentialStore<String>(); s.add("A"); s.add("B"); s.add("C");
        assertEquals("B", s.remove(1)); assertEquals(2, s.size());
        assertEquals("A", s.get(0)); assertEquals("C", s.get(1));
        var f = SequentialStore.class.getDeclaredField("elements"); f.setAccessible(true);
        Object[] data = (Object[]) f.get(s); assertNull(data[s.size()]);
        assertThrows(IndexOutOfBoundsException.class, () -> s.set(2, "X"));
        assertThrows(IllegalArgumentException.class, () -> s.add(null));
        assertThrows(IllegalArgumentException.class, () -> s.set(0, null));
        assertEquals(2, s.size()); assertEquals("A", s.get(0));
    }
}
