package edu.csc223.planner;
import java.time.LocalDate;
import java.util.*;
public final class ReleaseDemo {
    static ReleasePlanner fixture(){
        var p=new ReleasePlanner();p.addCourse(new Course(223,"Data Structures"));p.addCourse(new Course(208,"Empty course"));
        p.addModule(new StudyModule(10,223,"Release preparation",10));p.addModule(new StudyModule(11,223,"Empty module",11));
        int[] ranks={2,5,4,3};String[] titles={"Design outline","Implement planner","Test planner","Review notes"};
        for(int i=1;i<=4;i++)p.add(new Assignment(i,223,10,titles[i-1],LocalDate.of(2026,12,14),ranks[i-1],Assignment.Status.PENDING));
        p.addPrerequisite(1,2);p.addPrerequisite(2,3);return p;
    }
    public static void main(String[] args){
        var p=fixture();System.out.println("Student Assignment Planner "+ReleasePlanner.VERSION);
        System.out.println("Insertion order="+Arrays.stream(p.snapshot()).map(Assignment::id).toList());
        System.out.println("Title search planner="+p.searchTitle("planner").stream().map(Assignment::id).toList());
        System.out.println("Title order="+Arrays.stream(p.ordered(ReleasePlanner.Ordering.TITLE)).map(Assignment::id).toList());
        System.out.println("Hash ID 2="+p.find(2).title()+"; binary ID 2="+p.binaryFind(2).orElseThrow().id());
        System.out.println("BST range 2..3="+p.idRange(2,3).stream().map(Assignment::id).toList());
        for(var c:p.hierarchy()) {System.out.println("Course "+c.course().id());for(var m:c.modules())System.out.println("  Module "+m.module().id()+" assignments="+m.assignments().stream().map(Assignment::id).toList());}
        System.out.println("BFS="+p.bfs(1).order()+"; DFS="+p.dfs(1).order()+"; path="+p.path(1,3));
        System.out.println("Blockers of 3="+p.blockers(3)+"; isolated path="+p.path(1,4));
        try{p.addPrerequisite(3,1);}catch(IllegalArgumentException e){System.out.println("Rejected cycle: "+e.getMessage());}
        try{p.enqueue(2);}catch(IllegalArgumentException e){System.out.println("Rejected blocked FIFO: "+e.getMessage());}
        System.out.println("Recommended="+p.recommend().orElseThrow().id());
        p.enqueue(1);p.enqueue(4);System.out.println("FIFO starts="+p.startFifo().started().orElseThrow().id());p.complete(1);
        System.out.println("Unblocked recommendation="+p.recommend().orElseThrow().id());
        System.out.println("Priority starts="+p.startRecommended().orElseThrow().id());p.complete(2);
        System.out.println("Next priority starts="+p.startRecommended().orElseThrow().id());p.complete(3);
        System.out.println("FIFO starts="+p.startFifo().started().orElseThrow().id());p.complete(4);
        System.out.println("Empty recommendation="+p.recommend()+"; empty FIFO="+p.startFifo());
        p.remove(3);System.out.println("Removed 3; pending="+p.countPending()+"; validation="+p.validate());
    }
}
