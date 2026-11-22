package edu.csc223.planner;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import java.time.LocalDate;
import static edu.csc223.planner.Assignment.Status.*;
class Milestone8Test {
    private PlannerService p() { return PlannerPriorityDemo.planner(); }
    private Assignment a(int id,int priority,int day) { return PlannerPriorityDemo.item(id,priority,day); }
    private void add(PlannerService p,Assignment a) {p.addAssignment(a);p.scheduleAssignment(a.id());check(p);}
    private void check(PlannerService p) {assertTrue(p.priorityAgrees());assertTrue(p.indexesAgree());}
    @Test void empty() {
        var p=p();assertTrue(p.priorityIsEmpty());assertEquals(0,p.prioritySize());
        assertTrue(p.peekPriority().isEmpty());assertTrue(p.startPriority().isEmpty());check(p);
    }
    @Test void singletonAndHistory() {
        var p=p();add(p,a(1,2,19));assertEquals(1,p.peekPriority().orElseThrow().id());
        assertEquals(1,p.prioritySize());var active=p.startPriority().orElseThrow();
        assertEquals(ACTIVE,active.status());assertSame(active,p.findById(1));
        assertSame(active,p.findInTree(1));assertEquals(List.of(active),p.priorityHistory());
        assertTrue(p.priorityIsEmpty());assertEquals(1,p.size());check(p);
    }
    @Test void sevenExactOrder() {
        var p=p();int[] pr={2,5,3,5,3,3,5},day={19,21,21,20,21,20,21};
        for(int i=0;i<7;i++) add(p,a(i+1,pr[i],day[i]));
        for(int id:new int[]{4,2,7,6,3,5,1}) {assertEquals(id,p.startPriority().orElseThrow().id());check(p);}
        assertTrue(p.priorityIsEmpty());
    }
    @Test void arrivalBreaksTiesBeforeIdentifier() {
        var p=p();add(p,a(9,3,19));add(p,a(1,3,19));
        assertEquals(9,p.startPriority().orElseThrow().id());assertEquals(1,p.startPriority().orElseThrow().id());
    }
    @Test void identifierResolvesEqualSequence() {
        var x=new PrioritySchedule.Entry(a(1,3,19),7);var y=new PrioritySchedule.Entry(a(9,3,19),7);
        assertTrue(PrioritySchedule.ORDER.compare(x,y)<0);
    }
    @Test void promotionRebuilds() {
        var p=p();add(p,a(1,1,19));add(p,a(2,4,19));p.updatePriority(1,5);
        assertEquals(1,p.peekPriority().orElseThrow().id());check(p);
    }
    @Test void demotionRebuilds() {
        var p=p();add(p,a(1,5,19));add(p,a(2,4,19));p.updatePriority(1,1);
        assertEquals(2,p.peekPriority().orElseThrow().id());check(p);
    }
    @Test void dueDateEditAndSequencePreserved() {
        var p=p();add(p,a(9,3,20));add(p,a(1,3,19));
        p.update(9,p.findById(9).rescheduled(LocalDate.of(2026,11,19)));
        assertEquals(9,p.peekPriority().orElseThrow().id());check(p);
    }
    @Test void duplicateAndInvalidEnrollmentPreserveState() {
        var p=p();add(p,a(1,3,19));assertThrows(IllegalArgumentException.class,()->p.scheduleAssignment(1));
        assertThrows(NoSuchElementException.class,()->p.scheduleAssignment(999));
        p.addAssignment(a(2,3,19).withStatus(COMPLETE));
        assertThrows(IllegalArgumentException.class,()->p.scheduleAssignment(2));assertEquals(1,p.prioritySize());check(p);
    }
    @Test void invalidPriorityLeavesRecordsAndScheduleUnchanged() {
        var p=p();var a=a(1,3,19);add(p,a);
        assertThrows(IllegalArgumentException.class,()->p.updatePriority(1,0));
        assertSame(a,p.findById(1));assertSame(a,p.peekPriority().orElseThrow());check(p);
    }
    @Test void externalStatusRemovesEnrollmentWithoutAutomaticReentry() {
        var p=p();add(p,a(1,3,19));p.changeStatus(1,COMPLETE);assertTrue(p.priorityIsEmpty());
        p.changeStatus(1,PENDING);assertTrue(p.priorityIsEmpty());p.scheduleAssignment(1);check(p);
    }
    @Test void removalCannotLeaveStaleIdEvenAfterReuse() {
        var p=p();add(p,a(1,3,19));p.removeById(1);assertTrue(p.priorityIsEmpty());
        p.addAssignment(a(1,5,19));assertTrue(p.startPriority().isEmpty());check(p);
    }
    @Test void cancelPreservesDomainStateAndReenrollmentIsNewArrival() {
        var p=p();add(p,a(1,3,19));add(p,a(2,3,19));assertTrue(p.cancelPriority(1));
        assertFalse(p.cancelPriority(1));assertEquals(PENDING,p.findById(1).status());p.scheduleAssignment(1);
        assertEquals(2,p.startPriority().orElseThrow().id());check(p);
    }
    @Test void clearAndReuse() {
        var p=p();add(p,a(1,3,19));p.clearPriority();assertTrue(p.priorityIsEmpty());
        assertEquals(PENDING,p.findById(1).status());p.scheduleAssignment(1);check(p);
        p.clearAssignments();assertTrue(p.priorityIsEmpty());assertEquals(0,p.size());check(p);
    }
    @Test void fifoStartRemovesPriorityEnrollment() {
        var p=p();add(p,a(1,3,19));var q=new StudyWorkflow(p);q.enqueueAssignment(1);q.startNext();
        assertTrue(p.priorityIsEmpty());assertTrue(p.priorityHistory().isEmpty());check(p);
    }
    @Test void priorityStartMakesFifoEntryIneligible() {
        var p=p();add(p,a(1,3,19));var q=new StudyWorkflow(p);q.enqueueAssignment(1);p.startPriority();
        var result=q.startNext();assertEquals(1,result.discarded());assertTrue(result.started().isEmpty());check(p);
    }
    @Test void summaryAndHistoryCannotMutateStorage() {
        var p=p();add(p,a(1,3,19));var view=p.prioritySummary();
        assertThrows(UnsupportedOperationException.class,view::clear);p.startPriority();assertEquals(1,view.size());
        assertThrows(UnsupportedOperationException.class,()->p.priorityHistory().clear());check(p);
    }
    private void orderTest(int kind) {
        var h=new BinaryHeap<PrioritySchedule.Entry>(PrioritySchedule.ORDER);var data=new ArrayList<PrioritySchedule.Entry>();
        var random=new Random(22308);
        for(int i=1;i<=300;i++) data.add(new PrioritySchedule.Entry(a(i,1+random.nextInt(5),1+random.nextInt(28)),i));
        if(kind==0) data.sort(PrioritySchedule.ORDER);else if(kind==1) data.sort(PrioritySchedule.ORDER.reversed());
        for(var e:data) {h.add(e);assertTrue(h.isValidHeap());}
        data.sort(PrioritySchedule.ORDER);
        for(var expected:data) {assertSame(expected,h.removeRoot());assertTrue(h.isValidHeap());}
        assertTrue(h.isEmpty());
    }
    @Test void ascendingWithGrowth() {orderTest(0);}
    @Test void descendingWithGrowth() {orderTest(1);}
    @Test void randomWithGrowth() {orderTest(2);}
    @Test void heapNullsAndSnapshot() {
        assertThrows(NullPointerException.class,()->new BinaryHeap<Integer>(null));
        var h=new BinaryHeap<Integer>(Comparator.naturalOrder());
        assertThrows(NullPointerException.class,()->h.add(null));h.add(2);var snapshot=h.snapshot();h.add(1);
        assertEquals(List.of(2),snapshot);assertThrows(UnsupportedOperationException.class,snapshot::clear);
        assertTrue(h.isValidHeap());
    }
    @Test void randomizedPriorityEditsMatchSortedView() {
        var p=p();for(int i=1;i<=40;i++) add(p,a(i,3,19));
        var random=new Random(80);
        for(int step=0;step<200;step++) {
            int id=1+random.nextInt(40);p.updatePriority(id,1+random.nextInt(5));check(p);
            var expected=new ArrayList<>(Arrays.asList(p.snapshot()));
            expected.sort(Comparator.comparingInt(Assignment::priority).reversed().thenComparingInt(Assignment::id));
            assertEquals(expected,p.prioritySummary());
        }
    }
}
