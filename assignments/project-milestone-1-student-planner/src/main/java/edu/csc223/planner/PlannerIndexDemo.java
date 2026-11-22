package edu.csc223.planner;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.NoSuchElementException;

/** Repeatable application pathway using the real planner service and queue. */
public final class PlannerIndexDemo {
    static Assignment item(int id) {
        return new Assignment(id,223,6,"Work " + id,LocalDate.of(2026,11,5),
                3,Assignment.Status.PENDING);
    }
    static PlannerService planner() {
        PlannerService p=new PlannerService();
        p.addCourse(new Course(223,"Data Structures"));
        p.addModule(new StudyModule(6,223,"Associative Collections",6));
        return p;
    }
    public static void main(String[] args) {
        PlannerService p=planner();
        for(int id:new int[]{1,9,17}) p.addAssignment(item(id));
        System.out.println("Added IDs: " + Arrays.toString(
                Arrays.stream(p.snapshot()).mapToInt(Assignment::id).toArray()));
        System.out.println("ID 9: " + p.findById(9));
        try { p.addAssignment(item(9)); }
        catch(IllegalArgumentException e) { System.out.println("Duplicate: " + e.getMessage()); }
        try { p.findById(999); }
        catch(NoSuchElementException e) { System.out.println("NOT FOUND: " + e.getMessage()); }
        p.update(9,item(9).rescheduled(LocalDate.of(2026,11,12)));
        System.out.println("Updated ID 9 due: " + p.findById(9).dueDate());
        StudyWorkflow workflow=new StudyWorkflow(p);
        workflow.enqueueAssignment(1); workflow.enqueueAssignment(9);
        p.removeById(1);
        System.out.println("Removed ID 1; present=" + p.containsId(1) + "; size=" + p.size());
        var started=workflow.startNext();
        System.out.println("Queue discarded=" + started.discarded()
                + "; started=" + started.started().orElseThrow().id()
                + "; indexed status=" + p.findById(9).status());
        for(Assignment a:p.snapshot())
            System.out.println("Summary: " + a.id()+" | "+a.title()+" | "+a.status());
        AssignmentIndex demonstration=new AssignmentIndex();
        for(int id:new int[]{1,9,17}) demonstration.insert(item(id));
        System.out.println("Same bucket: 1, 9, 17 at capacity 8; ID 1 comparisons="
                + demonstration.lookup(1).comparisons());
        for(int id:new int[]{2,3,4,5}) demonstration.insert(item(id));
        System.out.println("After seventh insert: capacity="+demonstration.capacity()
                + "; size="+demonstration.size()+"; load="+demonstration.loadFactor());
        p.clearAssignments();
        System.out.println("After clear: size="+p.size()+"; empty="+p.isEmpty());
    }
}
