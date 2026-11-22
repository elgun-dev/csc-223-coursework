package edu.csc223.planner;

import java.time.LocalDate;
public final class PlannerGraphDemo {
    static PlannerService planner() {
        var p=new PlannerService();p.addCourse(new Course(223,"Data Structures"));
        p.addModule(new StudyModule(9,223,"Graphs",9));return p;
    }
    static Assignment item(int id,String title) {
        return new Assignment(id,223,9,title,LocalDate.of(2026,12,3),3,Assignment.Status.PENDING);
    }
    static PlannerService example() {
        var p=planner();String[] names={"Read graph model","Build adjacency list","Trace BFS",
                "Trace DFS","Validate paths","Analyze network","Write report","Review vocabulary"};
        for(int i=0;i<names.length;i++) p.addAssignment(item(i+1,names[i]));
        for(int[] edge:new int[][]{{1,2},{1,3},{2,4},{3,5},{4,6},{5,6},{6,7},{7,3}})
            p.addDependency(edge[0],edge[1]);
        return p;
    }
    public static void main(String[] args) {
        var p=example();int start=args.length>0?Integer.parseInt(args[0]):1;
        int target=args.length>1?Integer.parseInt(args[1]):7;
        System.out.println("Prerequisite -> dependent; directed; unweighted; insertion order");
        for(int id:p.dependencyVertices()) System.out.println(id+" "+p.findById(id).title()+" -> "+p.dependentsOf(id));
        System.out.println("Vertices="+p.dependencyVertexCount()+"; edges="+p.dependencyEdgeCount());
        var b=p.impactBfs(start);var d=p.impactDfs(start);
        System.out.println("BFS from "+start+": "+b.order());System.out.println("DFS from "+start+": "+d.order());
        System.out.println("Path to "+target+": "+b.pathTo(target)+"; hops="+b.levelOf(target));
        System.out.println("Path 1 -> 8: "+p.dependencyPath(1,8));
        System.out.println("Path 7 -> 1: "+p.dependencyPath(7,1));
        System.out.println("Cycle detected="+p.hasDependencyCycle()+"; review contradictory prerequisites");
        p.removeDependency(7,3);System.out.println("After correcting 7 -> 3: cycle="+p.hasDependencyCycle());
        p.update(2,item(2,"Revised adjacency implementation"));
        System.out.println("Updated label="+p.findById(2).title()+"; neighbors="+p.dependentsOf(2));
        p.scheduleAssignment(2);p.startPriority();
        System.out.println("Priority starts 2; state="+p.findInTree(2).status()+"; relationship 1 -> 2="+p.hasDependency(1,2));
        p.removeById(4);System.out.println("After deleting 4: vertices="+p.dependencyVertexCount()+"; edges="+p.dependencyEdgeCount());
        System.out.println("Updated path 1 -> 7: "+p.dependencyPath(1,7));
        System.out.println("Indexes="+p.indexesAgree()+"; schedule="+p.priorityAgrees()+"; graph="+p.dependenciesAgree());
        try {p.impactBfs(999);} catch(IllegalArgumentException ex) {System.out.println("Missing start: "+ex.getMessage());}
        p.resetDependencies();System.out.println("Reset relations: vertices="+p.dependencyVertexCount()+"; edges="+p.dependencyEdgeCount());
        p.clearAssignments();System.out.println("Clear all: graph empty="+p.dependenciesEmpty());
    }
}
