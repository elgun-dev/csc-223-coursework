package edu.csc223.planner;
import java.util.*;
/** Strict release facade. The service stays private so advisory legacy graph
 * mutation cannot bypass release preconditions. Single-threaded, in-memory. */
public final class ReleasePlanner {
    public static final String VERSION="v1.0.0";
    private final PlannerService service=new PlannerService();
    private final StudyWorkflow fifo=new StudyWorkflow(service,this::isEligible);
    private static final Comparator<Assignment> RECOMMEND=Comparator
        .comparingInt(Assignment::priority).reversed().thenComparing(Assignment::dueDate)
        .thenComparingInt(Assignment::id);
    public void addCourse(Course c){service.addCourse(c);}
    public void addModule(StudyModule m){service.addModule(m);}
    public void add(Assignment a){service.addAssignment(a);}
    public Assignment find(int id){return service.findById(id);}
    public Assignment[] snapshot(){return service.snapshot();}
    public void renameCourse(int id,String title){service.renameCourse(id,title);}
    public void retitleModule(int id,String title){service.retitleModule(id,title);}
    public void removeCourse(int id){service.removeCourse(id);}
    public void removeModule(int id){service.removeModule(id);}
    public void update(int id,Assignment replacement){
        var old=find(id);
        if(replacement==null || replacement.id()!=id || replacement.status()!=old.status())
            throw new IllegalArgumentException("Edit must preserve ID and status; use start/complete");
        service.update(id,replacement);
    }
    public void remove(int id){
        find(id);
        // Avoid silently deleting a prerequisite constraint in the strict workflow.
        if(!service.dependentsOf(id).isEmpty()) throw new IllegalArgumentException("Remove outgoing dependencies first");
        service.removeById(id);
    }
    public void addPrerequisite(int p,int d){
        find(p);find(d);
        if(find(d).status()!=Assignment.Status.PENDING)
            throw new IllegalArgumentException("Cannot change prerequisites of started/completed work");
        if(p==d || service.hasDependency(p,d) || !service.dependencyPath(d,p).isEmpty())
            throw new IllegalArgumentException("Self, duplicate, or cycle-forming dependency");
        service.addDependency(p,d);
    }
    public boolean removePrerequisite(int p,int d){
        find(p);find(d);
        if(find(d).status()!=Assignment.Status.PENDING)
            throw new IllegalArgumentException("Cannot change prerequisites of started/completed work");
        return service.removeDependency(p,d);
    }
    public Set<Integer> directPrerequisites(int id){
        find(id);var result=new LinkedHashSet<Integer>();
        for(int p:service.dependencyVertices()) if(service.hasDependency(p,id)) result.add(p);
        return Collections.unmodifiableSet(result);
    }
    public boolean isEligible(int id){
        if(find(id).status()!=Assignment.Status.PENDING) return false;
        for(int p:directPrerequisites(id)) if(find(p).status()!=Assignment.Status.COMPLETE) return false;
        return true;
    }
    /** All unfinished ancestors, not merely one path or just immediate parents. */
    public Set<Integer> blockers(int id){
        find(id);var result=new LinkedHashSet<Integer>();var seen=new HashSet<Integer>();
        var queue=new ArrayDeque<Integer>();queue.add(id);seen.add(id);
        while(!queue.isEmpty()) for(int p:directPrerequisites(queue.remove())) if(seen.add(p)){
            if(find(p).status()!=Assignment.Status.COMPLETE) result.add(p);queue.add(p);
        }
        return Collections.unmodifiableSet(result);
    }
    /** One edge scan builds the blocked set; heap is a fresh derived snapshot. */
    public Optional<Assignment> recommend(){
        var blocked=new HashSet<Integer>();
        for(var a:snapshot()) if(a.status()!=Assignment.Status.COMPLETE) blocked.addAll(service.dependentsOf(a.id()));
        var ready=new ArrayList<Assignment>();
        for(var a:snapshot()) if(a.status()==Assignment.Status.PENDING && !blocked.contains(a.id())) ready.add(a);
        return Optional.ofNullable(new BinaryHeap<>(RECOMMEND,ready).peek());
    }
    public Optional<Assignment> startRecommended(){
        var result=recommend();if(result.isEmpty()) return result;
        int id=result.get().id();service.changeStatus(id,Assignment.Status.ACTIVE);return Optional.of(find(id));
    }
    public void complete(int id){
        var a=find(id);
        if(a.status()!=Assignment.Status.ACTIVE && !isEligible(id)) throw new IllegalArgumentException("Task is not active or eligible");
        service.changeStatus(id,Assignment.Status.COMPLETE);
    }
    public void enqueue(int id){fifo.enqueueAssignment(id);}
    public StudyWorkflow.StartResult startFifo(){return fifo.startNext();}
    public Optional<Assignment> peekFifo(){return fifo.inspectNext();}
    public int[] queuedIds(){return fifo.queuedIds();}
    public PlannerService.ScanResult countPending(){return service.countPending();}
    public List<Assignment> searchTitle(String text){
        if(text==null || text.isBlank()) throw new IllegalArgumentException("Search text required");
        String key=text.toLowerCase(Locale.ROOT);
        return Arrays.stream(snapshot()).filter(a->a.title().toLowerCase(Locale.ROOT).contains(key)).toList();
    }
    public List<Assignment> filter(Integer course,Assignment.Status status){
        if(course!=null && service.courses().stream().noneMatch(c->c.id()==course)) throw new IllegalArgumentException("Unknown course");
        return Arrays.stream(snapshot()).filter(a->course==null || a.courseId()==course)
            .filter(a->status==null || a.status()==status).toList();
    }
    public enum Ordering { ID,TITLE,DUE,PRIORITY }
    public Assignment[] ordered(Ordering order){
        Comparator<Assignment> c=switch(Objects.requireNonNull(order)){
            case ID->AssignmentOrderings.BY_ID;case TITLE->Comparator.comparing(Assignment::title).thenComparingInt(Assignment::id);
            case DUE->AssignmentOrderings.BY_DUE_THEN_ID;case PRIORITY->AssignmentOrderings.BY_PRIORITY_THEN_ID;};
        var copy=snapshot();SortingAlgorithms.mergeSort(copy,c);return copy;
    }
    public Optional<Assignment> binaryFind(int id){return new PlannerQueries(service).binaryCurrent(id).match();}
    public List<Assignment> idRange(int low,int high){return service.assignmentsInRange(low,high);}
    public DependencyGraph.Traversal bfs(int id){return service.impactBfs(id);}
    public DependencyGraph.Traversal dfs(int id){return service.impactDfs(id);}
    public List<Integer> path(int from,int to){return service.dependencyPath(from,to);}
    public record ModuleBranch(StudyModule module,List<Assignment> assignments){public ModuleBranch{assignments=List.copyOf(assignments);}}
    public record CourseBranch(Course course,List<ModuleBranch> modules){public CourseBranch{modules=List.copyOf(modules);}}
    /** A real domain hierarchy, distinct from the auxiliary ID search tree. */
    public List<CourseBranch> hierarchy(){
        var byModule=new HashMap<Integer,List<Assignment>>();
        for(var a:snapshot()) byModule.computeIfAbsent(a.moduleId(),k->new ArrayList<>()).add(a);
        var byCourse=new HashMap<Integer,List<ModuleBranch>>();
        for(var m:service.modules()) byCourse.computeIfAbsent(m.courseId(),k->new ArrayList<>())
            .add(new ModuleBranch(m,byModule.getOrDefault(m.id(),List.of())));
        return service.courses().stream().map(c->new CourseBranch(c,byCourse.getOrDefault(c.id(),List.of()))).toList();
    }
    public record Integrity(boolean valid,List<String> issues){public Integrity{issues=List.copyOf(issues);}}
    public Integrity rebuildDerivedIndexes(){service.rebuildIndexes();return validate();}
    public Integrity validate(){
        var issues=new ArrayList<String>();
        if(!service.indexesAgree())issues.add("INDEX_MISMATCH");
        if(!service.dependenciesAgree())issues.add("GRAPH_MISMATCH");
        if(!service.priorityAgrees())issues.add("MANUAL_SCHEDULE_MISMATCH");
        if(service.hasDependencyCycle())issues.add("DEPENDENCY_CYCLE");
        for(var a:snapshot()) if(service.modules().stream().noneMatch(m->m.id()==a.moduleId() && m.courseId()==a.courseId()))issues.add("PARENT_MISMATCH:"+a.id());
        return new Integrity(issues.isEmpty(),issues);
    }
}
