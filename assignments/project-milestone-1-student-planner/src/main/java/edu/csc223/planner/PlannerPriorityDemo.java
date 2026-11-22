package edu.csc223.planner;

import java.time.LocalDate;
public final class PlannerPriorityDemo {
    static PlannerService planner() {
        var p=new PlannerService();p.addCourse(new Course(223,"Data Structures"));
        p.addModule(new StudyModule(8,223,"Priority Scheduling",8));return p;
    }
    static Assignment item(int id,int priority,int day) {
        return new Assignment(id,223,8,"Assignment "+id,LocalDate.of(2026,11,day),
                priority,Assignment.Status.PENDING);
    }
    static void show(String label,PlannerService p) {
        System.out.println(label+": "+p.prioritySummary().stream().map(a->
                a.id()+"/p"+a.priority()+"/"+a.dueDate()).toList());
    }
    public static void main(String[] args) {
        var p=planner();int[] priorities={2,5,3,5,3,3,5};
        int[] days={19,21,21,20,21,20,21};
        for(int i=0;i<7;i++) {p.addAssignment(item(i+1,priorities[i],days[i]));p.scheduleAssignment(i+1);}
        show("Initial order",p);
        System.out.println("Peek="+p.peekPriority().orElseThrow().id()+"; count="+p.prioritySize());
        while(!p.priorityIsEmpty()) {
            var a=p.startPriority().orElseThrow();
            System.out.println("Started="+a.id()+"; status="+a.status()+"; pending="+p.prioritySize()
                    +"; indexes="+p.indexesAgree()+"; schedule="+p.priorityAgrees());
        }
        System.out.println("Empty peek="+p.peekPriority()+"; start="+p.startPriority());
        System.out.println("History count="+p.priorityHistory().size()+"; retained="+p.size());
        p.changeStatus(1,Assignment.Status.PENDING);p.changeStatus(3,Assignment.Status.PENDING);
        p.scheduleAssignment(1);p.scheduleAssignment(3);show("Before priority edit",p);
        p.updatePriority(1,5);show("After priority edit",p);
        p.update(3,p.findById(3).rescheduled(LocalDate.of(2026,11,18)));
        p.updatePriority(3,5);show("After deadline and priority edits",p);
        p.removeById(3);show("After deleting assignment 3",p);
        var fifo=new StudyWorkflow(p);fifo.enqueueAssignment(1);fifo.startNext();
        System.out.println("FIFO started 1; priority count="+p.prioritySize()+"; synchronized="+p.priorityAgrees());
        p.changeStatus(2,Assignment.Status.PENDING);p.scheduleAssignment(2);p.clearPriority();
        System.out.println("Schedule cleared; assignment 2="+p.findById(2).status());
        p.scheduleAssignment(2);p.clearAssignments();
        System.out.println("All cleared: primary="+p.size()+"; scheduled="+p.prioritySize());
    }
}
