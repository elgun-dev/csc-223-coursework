package edu.csc223.planner;

import java.time.LocalDate;
import static edu.csc223.planner.Assignment.Status.*;

public final class StudentAssignmentPlanner {
    public static void main(String[] args) {
        try { dispatch(args); }
        catch(IllegalArgumentException | java.util.NoSuchElementException e) {
            System.err.println("Input error: "+e.getMessage());System.err.println("Use --help for valid modes.");
            System.exit(2);
        }
    }
    private static void dispatch(String[] args) {
        if(args.length==0 || args[0].equals("--release")){ReleaseDemo.main(new String[0]);return;}
        if(args[0].equals("--version")){System.out.println(ReleasePlanner.VERSION);return;}
        if(args[0].equals("--help")){
            System.out.println("Default / --release: integrated strict workflow demonstration");
            System.out.println("--version | --help | --legacy | --search | --index | --tree | --priority | --graph [start target]");
            System.out.println("Other modes are historical component diagnostics; graph edges there are advisory.");return;
        }
        if(!java.util.Set.of("--legacy","--search","--index","--tree","--priority","--graph").contains(args[0]))
            throw new IllegalArgumentException("Unknown mode "+args[0]);
        if (args.length > 0 && args[0].equals("--graph")) {
            PlannerGraphDemo.main(java.util.Arrays.copyOfRange(args,1,args.length));
            return;
        }
        if (args.length > 0 && args[0].equals("--priority")) {
            PlannerPriorityDemo.main(java.util.Arrays.copyOfRange(args,1,args.length));
            return;
        }
        if (args.length > 0 && args[0].equals("--tree")) {
            PlannerTreeDemo.main(java.util.Arrays.copyOfRange(args,1,args.length));
            return;
        }
        if (args.length > 0 && args[0].equals("--index")) {
            PlannerIndexDemo.main(java.util.Arrays.copyOfRange(args,1,args.length));
            return;
        }
        if (args.length > 0 && args[0].equals("--search")) {
            PlannerSearchDemo.main(java.util.Arrays.copyOfRange(args,1,args.length));
            return;
        }
        PlannerService planner = new PlannerService();
        Course course = new Course(223, "Data Structures");
        StudyModule module = new StudyModule(3, 223, "Sequential Collections", 3);
        planner.addCourse(course); planner.addModule(module);
        LocalDate due = LocalDate.of(2026, 10, 1);
        planner.addAssignment(new Assignment(1, 223, 3, "Guided lab", due, 3, PENDING));
        planner.addAssignment(new Assignment(2, 223, 3, "Challenge", due, 5, PENDING));
        planner.addAssignment(new Assignment(3, 223, 3, "Reflection", due, 2, PENDING));
        planner.addAssignment(new Assignment(4, 223, 3, "Review", due, 4, PENDING));
        System.out.println("Course: " + course.name());
        System.out.println("Module: " + module.title());
        StudyWorkflow workflow = new StudyWorkflow(planner);
        for (int id = 1; id <= 4; id++) { workflow.enqueueAssignment(id); }
        System.out.println("Queued entries: " + workflow.size());
        for (int id : workflow.queuedIds()) {
            Assignment item = planner.findById(id);
            System.out.println("Waiting: " + item.id() + " | " + item.title());
        }
        System.out.println("Next ID: " + workflow.inspectNext().orElseThrow().id());
        while (!workflow.isEmpty()) {
            var result = workflow.startNext();
            result.started().ifPresent(item -> System.out.println(
                "Started: " + item.id() + " | " + planner.findById(item.id()).status()));
        }
        System.out.println("Empty inspection: " + workflow.inspectNext());
        System.out.println("Empty processing: " + workflow.startNext());
        System.out.println("Primary records retained: " + planner.size());
        System.out.println("Pending summary: " + planner.countPending());
        planner.changeStatus(1, PENDING); planner.changeStatus(2, PENDING);
        workflow.enqueueAssignment(1); workflow.enqueueAssignment(2);
        planner.changeStatus(1, COMPLETE);
        var result = workflow.startNext();
        System.out.println("After external completion: discarded=" + result.discarded()
            + ", started=" + result.started().orElseThrow().id());
    }
}
