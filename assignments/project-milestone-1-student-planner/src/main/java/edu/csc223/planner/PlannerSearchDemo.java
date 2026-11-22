package edu.csc223.planner;

import java.time.LocalDate;
import java.util.Arrays;

/** Run with optional arguments: positive ID and ID, DUE, or PRIORITY. */
public final class PlannerSearchDemo {
    private PlannerSearchDemo() { }
    public static PlannerService sample() {
        PlannerService p = new PlannerService();
        p.addCourse(new Course(223,"Data Structures"));
        p.addModule(new StudyModule(5,223,"Searching and Sorting",5));
        for (int id : new int[]{30,10,40,20}) {
            p.addAssignment(new Assignment(id,223,5,"Assignment " + id,
                    LocalDate.of(2026,11,10).plusDays(id % 30),
                    id == 10 || id == 40 ? 5 : 2, Assignment.Status.PENDING));
        }
        return p;
    }
    private static void display(String label, Assignment[] records) {
        System.out.println(label);
        for (Assignment a : records) System.out.printf(
                "ID %d | %s | due %s | priority %d | %s%n",
                a.id(), a.title(), a.dueDate(), a.priority(), a.status());
    }
    private static void result(String label, AssignmentSearch.Result r) {
        System.out.println(label + ": " + r.match().map(a -> "ID " + a.id()
                + " " + a.title()).orElse("NOT FOUND")
                + "; comparisons=" + r.comparisons());
    }
    public static void main(String[] args) {
        try {
            int id = args.length > 0 ? Integer.parseInt(args[0]) : 20;
            var criterion = args.length > 1
                    ? AssignmentOrderings.Criterion.valueOf(args[1].toUpperCase())
                    : AssignmentOrderings.Criterion.DUE;
            PlannerService p = sample();
            PlannerQueries query = new PlannerQueries(p);
            StudyWorkflow queue = new StudyWorkflow(p);
            queue.enqueueAssignment(30); queue.enqueueAssignment(10);
            Assignment[] original = p.snapshot();
            display("Before sorting", original);
            result("Sequential requested " + id, query.sequential(id));
            var ids = query.prepareIds();
            display("Established ID ordering for binary search", ids.snapshot());
            result("Binary requested " + id, AssignmentSearch.binary(ids,id));
            result("Sequential missing 999", query.sequential(999));
            result("Binary missing 999", AssignmentSearch.binary(ids,999));
            result("Known existing 30", AssignmentSearch.binary(ids,30));
            var a = query.ordered(criterion, PlannerQueries.Algorithm.INSERTION);
            var b = query.ordered(criterion, PlannerQueries.Algorithm.MERGE);
            display("Insertion view " + criterion, a.snapshot());
            display("Merge view " + criterion, b.snapshot());
            System.out.println("Insertion metrics: " + a.metrics());
            System.out.println("Merge metrics: " + b.metrics());
            boolean equal = Arrays.equals(a.snapshot(), b.snapshot());
            boolean unchanged = Arrays.equals(original, p.snapshot());
            if (!equal || !unchanged) throw new AssertionError("View corruption");
            System.out.println("Equivalent order: " + equal);
            System.out.println("Primary order unchanged: " + unchanged);
            System.out.println("FIFO queue: " + Arrays.toString(queue.queuedIds()));
            System.out.println("FIFO started ID: "
                    + queue.startNext().started().orElseThrow().id());
            System.out.println("Current status for 30: "
                    + query.binaryCurrent(30).match().orElseThrow().status());
        } catch (IllegalArgumentException ex) {
            System.out.println("Usage: PlannerSearchDemo [positive ID]"
                    + " [ID|DUE|PRIORITY]. " + ex.getMessage());
        }
    }
}
