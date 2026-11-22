package edu.csc223.planner;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

class Milestone5Test {
    private Assignment item(int id, int day, int priority) {
        return new Assignment(id,223,5,"Work " + id,
                LocalDate.of(2026,11,1).plusDays(day),priority,
                Assignment.Status.PENDING);
    }
    private Assignment[] records() {
        return new Assignment[]{item(10,3,1),item(20,1,5),item(30,1,2)};
    }
    private void both(int id, Assignment[] a, boolean found) {
        var seq=AssignmentSearch.sequential(a,id);
        var bin=AssignmentSearch.binary(AssignmentSearch.prepare(a),id);
        assertEquals(found,seq.match().isPresent());
        assertEquals(found,bin.match().isPresent());
        if(found) { assertEquals(id,seq.match().orElseThrow().id());
            assertEquals(id,bin.match().orElseThrow().id()); }
    }
    @Test void firstTarget() { both(10,records(),true); }
    @Test void middleTarget() { both(20,records(),true); }
    @Test void lastTarget() { both(30,records(),true); }
    @Test void missingLowInteriorHigh() {
        for(int id:new int[]{1,15,99}) both(id,records(),false);
    }
    @Test void emptySearchCounts() {
        var a=new Assignment[0]; both(1,a,false);
        assertEquals(0,AssignmentSearch.sequential(a,1).comparisons());
        assertEquals(0,AssignmentSearch.binary(
                AssignmentSearch.prepare(a),1).comparisons());
    }
    @Test void singletonPresentAndMissing() {
        var a=new Assignment[]{item(10,1,1)};
        both(10,a,true);both(11,a,false);
    }
    @Test void duplicateIdContracts() {
        var first=item(5,1,1);var second=item(5,2,2);
        var a=new Assignment[]{first,second,item(9,1,1)};
        assertSame(first,AssignmentSearch.sequential(a,5).match().orElseThrow());
        var match=AssignmentSearch.binary(AssignmentSearch.prepare(a),5)
                .match().orElseThrow();
        assertTrue(match==first || match==second);
        var p=PlannerSearchDemo.sample();
        assertThrows(IllegalArgumentException.class,
                () -> p.addAssignment(item(10,1,1)));
    }
    @Test void binaryPreparationEstablishesAndProtectsOrder() {
        Assignment[] a={item(30,1,1),item(10,1,1),item(20,1,1)};
        var before=a.clone();var view=AssignmentSearch.prepare(a);
        assertArrayEquals(before,a);
        assertArrayEquals(new int[]{10,20,30},Arrays.stream(view.snapshot())
                .mapToInt(Assignment::id).toArray());
        a[0]=item(99,1,1);view.snapshot()[0]=item(88,1,1);
        assertEquals(30,AssignmentSearch.binary(view,30).match().orElseThrow().id());
    }
    @Test void invalidSearchInputs() {
        assertThrows(NullPointerException.class,
                () -> AssignmentSearch.sequential(null,1));
        assertThrows(NullPointerException.class,() -> AssignmentSearch.prepare(null));
        assertThrows(NullPointerException.class,
                () -> AssignmentSearch.prepare(new Assignment[]{null}));
        assertThrows(NullPointerException.class,
                () -> AssignmentSearch.sequential(new Assignment[]{null},1));
        assertThrows(NullPointerException.class,() -> AssignmentSearch.binary(null,1));
        for(int id:new int[]{0,-1}) {
            assertThrows(IllegalArgumentException.class,
                    () -> AssignmentSearch.sequential(records(),id));
            assertThrows(IllegalArgumentException.class,() ->
                    AssignmentSearch.binary(AssignmentSearch.prepare(records()),id));
        }
    }
    @Test void exactSearchCounts() {
        assertEquals(3,AssignmentSearch.sequential(records(),30).comparisons());
        var v=AssignmentSearch.prepare(records());
        assertEquals(1,AssignmentSearch.binary(v,20).comparisons());
        assertEquals(2,AssignmentSearch.binary(v,99).comparisons());
    }
    private void checkSort(Assignment[] source,Comparator<Assignment> c) {
        var before=source.clone();var a=source.clone();var b=source.clone();
        var expected=source.clone();Arrays.sort(expected,c);
        SortingAlgorithms.insertionSort(a,c);SortingAlgorithms.mergeSort(b,c);
        assertArrayEquals(expected,a);assertArrayEquals(expected,b);
        assertArrayEquals(before,source);
        for(int i=1;i<a.length;i++) assertTrue(c.compare(a[i-1],a[i])<=0);
        // Exact reference result proves preservation, including multiplicity.
    }
    @Test void unorderedSort() { checkSort(records(),AssignmentOrderings.BY_DUE_THEN_ID); }
    @Test void alreadySorted() { checkSort(records(),AssignmentOrderings.BY_ID); }
    @Test void reverseSorted() {
        var a=records();var temp=a[0];a[0]=a[2];a[2]=temp;
        checkSort(a,AssignmentOrderings.BY_ID);
    }
    @Test void emptyAndSingletonSorts() {
        checkSort(new Assignment[0],AssignmentOrderings.BY_ID);
        checkSort(new Assignment[]{item(1,1,1)},AssignmentOrderings.BY_ID);
    }
    @Test void duplicateFullKeysRetainObjectsAndStability() {
        var a=item(5,1,1);var b=item(5,1,2);var c=item(6,1,1);
        for(boolean merge:new boolean[]{false,true}) {
            Assignment[] values={a,c,b};
            if(merge) SortingAlgorithms.mergeSort(values,AssignmentOrderings.BY_DUE_THEN_ID);
            else SortingAlgorithms.insertionSort(values,AssignmentOrderings.BY_DUE_THEN_ID);
            assertSame(a,values[0]);assertSame(b,values[1]);assertSame(c,values[2]);
        }
    }
    @Test void equalPrimarySecondaryAndPriorityDirection() {
        var a=new Assignment[]{item(30,2,5),item(10,2,5),item(20,1,1)};
        checkSort(a,AssignmentOrderings.BY_DUE_THEN_ID);
        checkSort(a,AssignmentOrderings.BY_PRIORITY_THEN_ID);
        SortingAlgorithms.mergeSort(a,AssignmentOrderings.BY_PRIORITY_THEN_ID);
        assertArrayEquals(new int[]{10,30,20},Arrays.stream(a).mapToInt(Assignment::id).toArray());
    }
    @Test void deterministicRandomReferenceCoverage() {
        var random=new Random(22305);
        for(int n=0;n<75;n++) {
            var a=new Assignment[n];
            for(int i=0;i<n;i++) a[i]=item(i+1,random.nextInt(4),1+random.nextInt(5));
            checkSort(a,AssignmentOrderings.BY_DUE_THEN_ID);
            checkSort(a,AssignmentOrderings.BY_PRIORITY_THEN_ID);
        }
    }
    @Test void sortInvalidInputsAndExactMetrics() {
        assertThrows(NullPointerException.class,
                () -> SortingAlgorithms.insertionSort(null,AssignmentOrderings.BY_ID));
        assertThrows(NullPointerException.class,
                () -> SortingAlgorithms.mergeSort(null,AssignmentOrderings.BY_ID));
        assertThrows(NullPointerException.class,() -> SortingAlgorithms.insertionSort(records(),null));
        assertThrows(NullPointerException.class,() -> SortingAlgorithms.mergeSort(records(),null));
        var a=new Assignment[]{item(2,1,1),item(1,1,1)};
        assertEquals(new SortMetrics(1,0,1,0,0),
                SortingAlgorithms.insertionSort(a.clone(),AssignmentOrderings.BY_ID));
        assertEquals(new SortMetrics(1,0,0,4,2),
                SortingAlgorithms.mergeSort(a.clone(),AssignmentOrderings.BY_ID));
    }
    @Test void orderedViewsPreservePrimaryAndQueue() {
        var p=PlannerSearchDemo.sample();var before=p.snapshot();
        var queue=new StudyWorkflow(p);queue.enqueueAssignment(30);queue.enqueueAssignment(10);
        var query=new PlannerQueries(p);
        for(var criterion:AssignmentOrderings.Criterion.values()) {
            var a=query.ordered(criterion,PlannerQueries.Algorithm.INSERTION).snapshot();
            var b=query.ordered(criterion,PlannerQueries.Algorithm.MERGE).snapshot();
            assertArrayEquals(a,b); a[0]=item(99,1,1);
            assertArrayEquals(before,p.snapshot());
        }
        assertArrayEquals(new int[]{30,10},queue.queuedIds());
        assertEquals(30,queue.startNext().started().orElseThrow().id());
    }
    @Test void freshVersusHistoricalSnapshots() {
        var p=PlannerSearchDemo.sample();var query=new PlannerQueries(p);
        var old=query.prepareIds();p.changeStatus(30,Assignment.Status.COMPLETE);
        assertEquals(Assignment.Status.PENDING,
                AssignmentSearch.binary(old,30).match().orElseThrow().status());
        assertEquals(Assignment.Status.COMPLETE,
                query.binaryCurrent(30).match().orElseThrow().status());
        p.removeById(30);assertTrue(query.binaryCurrent(30).match().isEmpty());
        assertTrue(query.sequential(30).match().isEmpty());
    }
    @Test void invalidQuerySelections() {
        assertThrows(NullPointerException.class,() -> new PlannerQueries(null));
        var q=new PlannerQueries(PlannerSearchDemo.sample());
        assertThrows(NullPointerException.class,
                () -> q.ordered(null,PlannerQueries.Algorithm.MERGE));
        assertThrows(NullPointerException.class,
                () -> q.ordered(AssignmentOrderings.Criterion.ID,null));
    }
}
