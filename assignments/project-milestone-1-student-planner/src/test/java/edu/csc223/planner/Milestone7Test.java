package edu.csc223.planner;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;
import java.util.*;

class Milestone7Test {
    private Assignment a(int id) { return PlannerTreeDemo.item(id); }
    private AssignmentTree tree(int... ids) {
        var t=new AssignmentTree();for(int id:ids) { assertTrue(t.add(a(id)));assertTrue(t.isValid()); }
        return t;
    }
    private List<Integer> order(AssignmentTree t,AssignmentTree.Order order) {
        return PlannerTreeDemo.ids(t.traverse(order));
    }
    private void state(AssignmentTree t,Integer... ids) {
        assertTrue(t.isValid());assertEquals(ids.length,t.size());
        assertEquals(List.of(ids),order(t,AssignmentTree.Order.INORDER));
        for(int id:ids) assertTrue(t.contains(id));
    }
    private void remove(int target,int[] initial,Integer... expected) {
        var t=tree(initial);assertTrue(t.remove(target));state(t,expected);assertFalse(t.contains(target));
    }
    private PlannerService planner(int... ids) {
        var p=PlannerTreeDemo.planner();for(int id:ids) { p.addAssignment(a(id));assertTrue(p.indexesAgree()); }
        return p;
    }
    @Test void initialEmpty() {
        var t=tree();state(t);assertTrue(t.isEmpty());assertEquals(-1,t.height());
        for(var o:AssignmentTree.Order.values()) assertTrue(t.traverse(o).isEmpty());
    }
    @Test void firstRootAndReference() {
        var t=tree();var item=a(40);t.add(item);assertSame(item,t.lookup(40).match().orElseThrow());
        assertEquals(0,t.height());state(t,40);
    }
    @Test void sevenNodeExactTraversals() {
        var t=tree(40,20,60,10,30,50,70);
        assertEquals(List.of(40,20,10,30,60,50,70),order(t,AssignmentTree.Order.PREORDER));
        assertEquals(List.of(10,20,30,40,50,60,70),order(t,AssignmentTree.Order.INORDER));
        assertEquals(List.of(10,30,20,50,70,60,40),order(t,AssignmentTree.Order.POSTORDER));
        assertEquals(List.of(40,20,60,10,30,50,70),order(t,AssignmentTree.Order.LEVEL));
        assertEquals(2,t.height());state(t,10,20,30,40,50,60,70);
    }
    @Test void searchAllPositionsAndMissing() {
        var t=tree(40,20,60,10);for(int id:new int[]{40,20,60,10}) assertTrue(t.contains(id));
        assertTrue(t.lookup(99).match().isEmpty());assertEquals(2,t.lookup(99).comparisons());
    }
    @Test void duplicatePreservesOriginalAndShape() {
        var t=tree(40,20,60);var old=t.lookup(20).match().orElseThrow();
        assertFalse(t.add(a(20).withStatus(Assignment.Status.ACTIVE)));
        assertSame(old,t.lookup(20).match().orElseThrow());state(t,20,40,60);
        assertEquals(List.of(40,20,60),order(t,AssignmentTree.Order.LEVEL));
    }
    @Test void invalidInputPreservesState() {
        var t=tree(40);assertThrows(IllegalArgumentException.class,()->t.add(null));
        assertThrows(IllegalArgumentException.class,()->t.lookup(0));
        assertThrows(IllegalArgumentException.class,()->t.remove(-1));
        assertThrows(IllegalArgumentException.class,()->t.replace(40,null));
        assertThrows(IllegalArgumentException.class,()->t.replace(40,a(50)));
        assertThrows(IllegalArgumentException.class,()->t.traverse(null));state(t,40);
    }
    @Test void ascendingShape() { assertEquals(6,tree(10,20,30,40,50,60,70).height()); }
    @Test void descendingShape() { assertEquals(6,tree(70,60,50,40,30,20,10).height()); }
    @Test void boundaryOperations() {
        var t=tree(40,20,60);assertEquals(20,t.minimum().id());assertEquals(60,t.maximum().id());
        var empty=tree();assertThrows(NoSuchElementException.class,empty::minimum);
        assertThrows(NoSuchElementException.class,empty::maximum);
    }
    @Test void leafRemoval() { remove(10,new int[]{40,20,60,10},20,40,60); }
    @Test void leftChildRemoval() { remove(20,new int[]{40,20,60,10},10,40,60); }
    @Test void rightChildRemoval() { remove(20,new int[]{40,20,60,30},30,40,60); }
    @Test void rootTwoChildrenSuccessorRightChild() {
        var t=tree(40,20,70,50,80,60);assertTrue(t.remove(40));state(t,20,50,60,70,80);
        assertEquals(List.of(50,20,70,60,80),order(t,AssignmentTree.Order.LEVEL));
    }
    @Test void immediateSuccessor() { remove(40,new int[]{40,20,60,70},20,60,70); }
    @Test void singletonAndOneChildRootRemoval() {
        remove(40,new int[]{40});remove(40,new int[]{40,20},20);remove(40,new int[]{40,60},60);
    }
    @Test void missingRemovalUnchanged() {
        var t=tree(40,20,60);var before=order(t,AssignmentTree.Order.LEVEL);
        assertFalse(t.remove(999));state(t,20,40,60);assertEquals(before,order(t,AssignmentTree.Order.LEVEL));
    }
    @Test void clearAndReuse() {
        var t=tree(40,20,60);t.clear();state(t);assertEquals(-1,t.height());t.clear();
        assertTrue(t.add(a(10)));state(t,10);
    }
    @Test void replacementAndMissingReplacement() {
        var t=tree(40,20,60);var item=a(20).withStatus(Assignment.Status.COMPLETE);
        assertEquals(20,t.replace(20,item).orElseThrow().id());
        assertSame(item,t.lookup(20).match().orElseThrow());assertTrue(t.replace(99,a(99)).isEmpty());
        state(t,20,40,60);assertEquals(List.of(40,20,60),order(t,AssignmentTree.Order.LEVEL));
    }
    @Test void inclusiveRangeAndBounds() {
        var t=tree(40,20,60,10,30,50,70);
        assertEquals(List.of(30,40,50,60),PlannerTreeDemo.ids(t.range(25,60)));
        assertEquals(List.of(40),PlannerTreeDemo.ids(t.range(40,40)));assertTrue(t.range(80,90).isEmpty());
        assertThrows(IllegalArgumentException.class,()->t.range(60,20));
        assertThrows(IllegalArgumentException.class,()->t.range(0,20));assertTrue(tree().range(1,2).isEmpty());
    }
    @Test void immutableDetachedTraversal() {
        var t=tree(40);var before=t.traverse(AssignmentTree.Order.INORDER);
        assertThrows(UnsupportedOperationException.class,before::clear);
        t.replace(40,a(40).withStatus(Assignment.Status.ACTIVE));
        assertEquals(Assignment.Status.PENDING,before.get(0).status());state(t,40);
    }
    @Test void serviceSynchronizesAllMutations() {
        var p=planner(40,20,60);var item=a(20).rescheduled(LocalDate.of(2026,12,1));
        p.update(20,item);assertTrue(p.indexesAgree());assertSame(item,p.findInTree(20));
        p.changeStatus(20,Assignment.Status.COMPLETE);assertTrue(p.indexesAgree());
        assertSame(p.findById(20),p.findInTree(20));p.removeById(40);assertTrue(p.indexesAgree());
        assertThrows(NoSuchElementException.class,()->p.findInTree(40));
        assertEquals(List.of(20,60),PlannerTreeDemo.ids(p.assignmentsInRange(1,100)));
        p.clearAssignments();assertTrue(p.indexesAgree());assertEquals(-1,p.treeHeight());
        p.addAssignment(a(50));assertTrue(p.indexesAgree());
    }
    @Test void serviceInvalidMutationsPreserveAllIndexes() {
        var p=planner(40);var original=p.findInTree(40);
        assertThrows(IllegalArgumentException.class,()->p.addAssignment(a(40)));
        assertThrows(IllegalArgumentException.class,()->p.update(40,a(50)));
        assertThrows(IllegalArgumentException.class,()->p.changeStatus(40,null));
        assertThrows(NoSuchElementException.class,()->p.removeById(999));
        assertTrue(p.indexesAgree());assertSame(original,p.findInTree(40));assertEquals(1,p.size());
    }
    @Test void workflowStatusAndStaleTicket() {
        var p=planner(40,20);var q=new StudyWorkflow(p);q.enqueueAssignment(40);q.enqueueAssignment(20);
        p.removeById(40);var result=q.startNext();assertEquals(1,result.discarded());
        assertEquals(20,result.started().orElseThrow().id());
        assertEquals(Assignment.Status.ACTIVE,p.findInTree(20).status());assertTrue(p.indexesAgree());
    }
    @Test void randomOperationsAgainstMap() {
        var t=tree();var reference=new HashMap<Integer,Assignment>();var random=new Random(223070);
        for(int step=0;step<1000;step++) {
            int id=1+random.nextInt(80);var value=a(id);
            if(random.nextBoolean()) assertEquals(reference.putIfAbsent(id,value)==null,t.add(value));
            else assertEquals(reference.remove(id)!=null,t.remove(id));
            assertEquals(reference.size(),t.size());assertTrue(t.isValid());
            var sorted=new ArrayList<>(reference.keySet());Collections.sort(sorted);
            assertEquals(sorted,order(t,AssignmentTree.Order.INORDER));
            for(var entry:reference.entrySet()) assertSame(entry.getValue(),t.lookup(entry.getKey()).match().orElseThrow());
        }
    }
}
