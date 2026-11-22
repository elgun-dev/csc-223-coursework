package edu.csc223.planner;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;
import java.util.*;

class Milestone6Test {
    Assignment a(int id) { return PlannerIndexDemo.item(id); }
    AssignmentIndex index(int... ids) {
        AssignmentIndex m=new AssignmentIndex();
        for(int id:ids) m.insert(a(id)); return m;
    }
    PlannerService planner(int... ids) {
        PlannerService p=PlannerIndexDemo.planner();
        for(int id:ids) p.addAssignment(a(id)); return p;
    }
    @Test void newEmpty() {
        var m=index(); assertTrue(m.isEmpty());assertEquals(0,m.size());
        assertEquals(8,m.capacity());assertEquals(0,m.loadFactor());
    }
    @Test void singleInsertionRetainsReference() {
        var m=index();var value=a(500);m.insert(value);
        assertSame(value,m.lookup(500).match().orElseThrow());assertEquals(1,m.size());
    }
    @Test void multipleAndMissing() {
        var m=index(1,2,3);assertTrue(m.contains(2));assertFalse(m.contains(99));
        assertEquals(3,m.size());assertFalse(m.isEmpty());
    }
    @Test void duplicateRejectsWithoutReplacement() {
        var m=index(9);var before=m.lookup(9).match();
        assertThrows(IllegalArgumentException.class,()->m.insert(a(9).withStatus(Assignment.Status.ACTIVE)));
        assertEquals(before,m.lookup(9).match());assertEquals(1,m.size());
    }
    @Test void replaceReturnsOldAndPreservesSize() {
        var m=index(1);var old=m.lookup(1).match().orElseThrow();
        var value=old.rescheduled(LocalDate.of(2026,12,1));
        assertSame(old,m.replace(1,value).orElseThrow());
        assertSame(value,m.lookup(1).match().orElseThrow());assertEquals(1,m.size());
    }
    @Test void missingReplacementDoesNotInsert() {
        var m=index(1);assertTrue(m.replace(9,a(9)).isEmpty());assertEquals(1,m.size());
    }
    @Test void removalAndRepeatedRemoval() {
        var m=index(1);assertEquals(1,m.remove(1).orElseThrow().id());
        assertFalse(m.contains(1));assertTrue(m.remove(1).isEmpty());assertTrue(m.isEmpty());
    }
    @Test void collisionsSeparatelyRetrievable() {
        var m=index(1,9,17);assertEquals(8,m.capacity());
        assertEquals(3,m.lookup(1).comparisons());
        for(int id:new int[]{1,9,17}) assertEquals(id,m.lookup(id).match().orElseThrow().id());
        assertEquals(3,m.lookup(25).comparisons());
    }
    @Test void removeChainHeadMiddleTail() {
        for(int removed:new int[]{1,9,17}) {
            var m=index(1,9,17);m.remove(removed);
            for(int id:new int[]{1,9,17}) assertEquals(id!=removed,m.contains(id));
            assertEquals(2,m.size());
        }
    }
    @Test void growthThresholdAndPreservation() {
        var m=index(1,9,17,2,3,4);assertEquals(8,m.capacity());
        m.insert(a(5));assertEquals(16,m.capacity());assertEquals(7.0/16,m.loadFactor());
        for(int id:new int[]{1,9,17,2,3,4,5}) assertTrue(m.contains(id));
    }
    @Test void manyResizes() {
        var m=index();for(int i=1;i<=2000;i++) m.insert(a(i));
        assertEquals(4096,m.capacity());
        for(int i=1;i<=2000;i++) assertEquals(i,m.lookup(i).match().orElseThrow().id());
    }
    @Test void invalidInputsPreserveState() {
        var m=index(1);
        assertThrows(IllegalArgumentException.class,()->m.insert(null));
        assertThrows(IllegalArgumentException.class,()->m.replace(1,null));
        assertThrows(IllegalArgumentException.class,()->m.replace(1,a(2)));
        for(int bad:new int[]{0,-1,Integer.MIN_VALUE}) {
            assertThrows(IllegalArgumentException.class,()->m.lookup(bad));
            assertThrows(IllegalArgumentException.class,()->m.remove(bad));
        }
        assertEquals(1,m.size());assertTrue(m.contains(1));
    }
    @Test void largePositiveKeyUsesValueEquality() {
        var m=index(Integer.MAX_VALUE);int equal=Integer.parseInt("2147483647");
        assertTrue(m.contains(equal));assertEquals(1,m.size());
    }
    @Test void clearResetsCapacityAndAllowsReuse() {
        var m=index();for(int i=1;i<30;i++) m.insert(a(i));m.clear();
        assertEquals(8,m.capacity());assertTrue(m.isEmpty());assertFalse(m.contains(1));
        m.insert(a(1));assertTrue(m.contains(1));
    }
    @Test void snapshotCannotCorruptIndex() {
        var m=index(1,9);var values=m.snapshot();values[0]=null;
        assertEquals(2,m.snapshot().length);assertTrue(m.contains(1));assertTrue(m.contains(9));
    }
    @Test void serviceSynchronizesEveryMutation() {
        var p=planner(1,9,17);var replacement=a(9).rescheduled(LocalDate.of(2026,12,1));
        p.update(9,replacement);assertSame(replacement,p.get(1));
        assertSame(p.get(1),p.findById(9));
        p.changeStatus(9,Assignment.Status.COMPLETE);
        assertSame(p.get(1),p.findById(9));assertEquals(Assignment.Status.COMPLETE,p.findById(9).status());
        p.removeById(1);assertFalse(p.containsId(1));assertEquals(2,p.size());
        for(var value:p.snapshot()) assertSame(value,p.findById(value.id()));
    }
    @Test void serviceFailuresDoNotDesynchronize() {
        var p=planner(1);var old=p.findById(1);
        assertThrows(IllegalArgumentException.class,()->p.addAssignment(a(1)));
        var bad=new Assignment(2,223,999,"Bad",LocalDate.now(),3,Assignment.Status.PENDING);
        assertThrows(IllegalArgumentException.class,()->p.addAssignment(bad));
        assertThrows(IllegalArgumentException.class,()->p.update(1,bad));
        assertThrows(IllegalArgumentException.class,()->p.changeStatus(1,null));
        assertSame(old,p.findById(1));assertSame(old,p.get(0));assertEquals(1,p.size());
        assertFalse(p.containsId(2));
    }
    @Test void missingServiceOperationsKeepSize() {
        var p=planner(1);
        assertThrows(NoSuchElementException.class,()->p.findById(9));
        assertThrows(NoSuchElementException.class,()->p.removeById(9));
        assertThrows(NoSuchElementException.class,()->p.update(9,a(9)));
        assertEquals(1,p.size());
    }
    @Test void queueUsesCurrentIndexAndSkipsDeleted() {
        var p=planner(1,9);var q=new StudyWorkflow(p);
        q.enqueueAssignment(1);q.enqueueAssignment(9);p.removeById(1);
        var result=q.startNext();assertEquals(1,result.discarded());
        assertEquals(9,result.started().orElseThrow().id());
        assertEquals(Assignment.Status.ACTIVE,p.findById(9).status());
    }
    @Test void clearKeepsParentsAndQueueSkipsStale() {
        var p=planner(1);var q=new StudyWorkflow(p);q.enqueueAssignment(1);
        p.clearAssignments();assertTrue(p.isEmpty());assertFalse(p.containsId(1));
        assertTrue(q.startNext().started().isEmpty());p.addAssignment(a(2));assertTrue(p.containsId(2));
    }
    @Test void serviceSnapshotIsHistoricalAndDetached() {
        var p=planner(1);var before=p.snapshot();p.changeStatus(1,Assignment.Status.ACTIVE);
        assertEquals(Assignment.Status.PENDING,before[0].status());before[0]=null;
        assertEquals(Assignment.Status.ACTIVE,p.findById(1).status());
    }
    @Test void randomizedIndexAgainstReferenceMap() {
        var m=index();Map<Integer,Assignment> reference=new HashMap<>();
        Random random=new Random(22306);
        for(int step=0;step<3000;step++) {
            int id=1+random.nextInt(80);int op=random.nextInt(4);
            if(op==0) {
                if(reference.containsKey(id)) assertThrows(IllegalArgumentException.class,()->m.insert(a(id)));
                else { var value=a(id);m.insert(value);reference.put(id,value); }
            } else if(op==1) assertEquals(Optional.ofNullable(reference.remove(id)),m.remove(id));
            else if(op==2) {
                var value=a(id).withStatus(Assignment.Status.ACTIVE);
                assertEquals(Optional.ofNullable(reference.replace(id,value)),m.replace(id,value));
            } else assertEquals(Optional.ofNullable(reference.get(id)),m.lookup(id).match());
            assertEquals(reference.size(),m.size());
        }
        for(var entry:reference.entrySet()) assertEquals(entry.getValue(),m.lookup(entry.getKey()).match().orElseThrow());
    }
}
