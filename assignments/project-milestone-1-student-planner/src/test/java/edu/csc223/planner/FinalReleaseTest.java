package edu.csc223.planner;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import java.time.LocalDate;
/** Reflection is used only in tests to simulate inaccessible corrupt state. */
class FinalReleaseTest {
    static PlannerService service(ReleasePlanner p) throws Exception {
        var f=ReleasePlanner.class.getDeclaredField("service");f.setAccessible(true);return (PlannerService)f.get(p);
    }
    static void corruptHash(ReleasePlanner p,int id) throws Exception {
        var f=PlannerService.class.getDeclaredField("index");f.setAccessible(true);((AssignmentIndex)f.get(service(p))).remove(id);
    }
    @Test void validatorDetectsCorruptionAndRebuildRestores() throws Exception {
        var p=ReleaseDemo.fixture();var before=p.snapshot();var path=p.path(1,3);
        corruptHash(p,2);assertFalse(p.validate().valid());assertTrue(p.validate().issues().contains("INDEX_MISMATCH"));
        assertThrows(NoSuchElementException.class,()->p.find(2));
        assertTrue(p.rebuildDerivedIndexes().valid());assertArrayEquals(before,p.snapshot());assertEquals(path,p.path(1,3));
        assertSame(before[1],p.find(2));assertEquals(4,p.recommend().orElseThrow().id());
    }
    @Test void rebuildPreservesEnqueuedGenerationAndCurrentEdits(){
        var p=ReleaseDemo.fixture();p.enqueue(1);var old=p.find(1);
        p.update(1,new Assignment(1,223,10,"Updated",old.dueDate(),5,old.status()));
        assertTrue(p.rebuildDerivedIndexes().valid());assertEquals("Updated",p.startFifo().started().orElseThrow().title());
    }
    @Test void multiplePrerequisitesMustAllFinish(){
        var p=ReleaseDemo.fixture();p.addPrerequisite(4,2);p.complete(1);
        assertFalse(p.isEligible(2));assertEquals(Set.of(4),p.blockers(2));p.complete(4);
        assertTrue(p.isEligible(2));assertEquals(2,p.recommend().orElseThrow().id());
    }
    @Test void duplicateFailurePreservesCompleteVisibleState(){
        var p=ReleaseDemo.fixture();p.enqueue(1);var records=p.snapshot();var order=p.queuedIds();var hierarchy=p.hierarchy();
        assertThrows(IllegalArgumentException.class,()->p.add(p.find(1)));
        assertArrayEquals(records,p.snapshot());assertArrayEquals(order,p.queuedIds());assertEquals(hierarchy,p.hierarchy());assertTrue(p.validate().valid());
    }
    @Test void recommendationReflectsEditsWithoutStaleCandidate(){
        var p=ReleaseDemo.fixture();assertEquals(4,p.recommend().orElseThrow().id());var old=p.find(1);
        p.update(1,new Assignment(1,223,10,old.title(),old.dueDate(),5,old.status()));
        assertEquals(1,p.recommend().orElseThrow().id());p.complete(1);assertEquals(2,p.recommend().orElseThrow().id());
    }
    @Test void clearAndReuseCannotReviveFifoEntry(){
        var p=PlannerPriorityDemo.planner();p.addAssignment(PlannerPriorityDemo.item(1,3,19));var q=new StudyWorkflow(p);q.enqueueAssignment(1);
        p.clearAssignments();p.addAssignment(PlannerPriorityDemo.item(1,3,19));assertTrue(q.startNext().started().isEmpty());
        assertEquals(Assignment.Status.PENDING,p.findById(1).status());
    }
}
