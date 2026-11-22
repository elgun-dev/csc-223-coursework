package edu.csc223.planner;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

public final class PlannerTreeDemo {
    static Assignment item(int id) {
        return new Assignment(id,223,7,"Assignment "+id,LocalDate.of(2026,11,12),
                3,Assignment.Status.PENDING);
    }
    static PlannerService planner() {
        var p=new PlannerService();p.addCourse(new Course(223,"Data Structures"));
        p.addModule(new StudyModule(7,223,"Trees",7));return p;
    }
    static List<Integer> ids(List<Assignment> values) {
        return values.stream().map(Assignment::id).toList();
    }
    public static void main(String[] args) {
        var p=planner();for(int id:new int[]{40,20,60,10,30,50,70}) p.addAssignment(item(id));
        System.out.println("Size="+p.treeSize()+"; height="+p.treeHeight());
        for(var order:AssignmentTree.Order.values())
            System.out.println(order+": "+ids(p.traverse(order)));
        System.out.println("Found 30: "+p.findInTree(30));
        try { p.findInTree(999); }
        catch(NoSuchElementException e) { System.out.println("Missing 999: NOT FOUND"); }
        try { p.addAssignment(item(30)); }
        catch(IllegalArgumentException e) { System.out.println("Duplicate 30: rejected; size="+p.size()); }
        System.out.println("Minimum="+p.minimumId().id()+"; maximum="+p.maximumId().id());
        System.out.println("Range 25..60: "+ids(p.assignmentsInRange(25,60)));
        p.update(30,item(30).rescheduled(LocalDate.of(2026,12,1)));
        System.out.println("Updated due date through tree: "+p.findInTree(30).dueDate());
        var q=new StudyWorkflow(p);q.enqueueAssignment(10);q.enqueueAssignment(30);
        p.removeById(10);System.out.println("Removed leaf 10");
        p.removeById(20);System.out.println("Removed one-child node 20");
        p.removeById(40);System.out.println("Removed two-child root 40");
        var result=q.startNext();
        System.out.println("Queue discarded="+result.discarded()+"; started="+result.started().orElseThrow().id());
        System.out.println("Current tree status 30: "+p.findInTree(30).status());
        System.out.println("Final inorder: "+ids(p.traverse(AssignmentTree.Order.INORDER)));
        System.out.println("Final size="+p.size()+"; height="+p.treeHeight()+"; synchronized="+p.indexesAgree());
        p.clearAssignments();
        System.out.println("After clear: size="+p.treeSize()+"; height="+p.treeHeight()+"; synchronized="+p.indexesAgree());
    }
}
