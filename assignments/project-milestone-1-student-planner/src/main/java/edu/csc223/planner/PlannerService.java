package edu.csc223.planner;

import java.util.NoSuchElementException;

/** Owns registries and enforces ID uniqueness and cross-record relationships. */
public final class PlannerService {
    private final SequentialStore<Course> courses = new SequentialStore<>();
    private final SequentialStore<StudyModule> modules = new SequentialStore<>();
    private final SequentialStore<Assignment> assignments = new SequentialStore<>();
    private final java.util.Map<Integer,Long> generations=new java.util.HashMap<>();
    private long nextGeneration;
    long generation(int id) {findById(id);return generations.get(id);}
    public java.util.List<Course> courses() {
        var result=new java.util.ArrayList<Course>();
        for(int i=0;i<courses.size();i++) result.add(courses.get(i));
        return java.util.List.copyOf(result);
    }
    public java.util.List<StudyModule> modules() {
        var result=new java.util.ArrayList<StudyModule>();
        for(int i=0;i<modules.size();i++) result.add(modules.get(i));
        return java.util.List.copyOf(result);
    }
    public void renameCourse(int id,String name) {
        int at=courses.find(c->c.id()==id);
        if(at<0) throw new NoSuchElementException("Course "+id);
        courses.set(at,courses.get(at).renamed(name));
    }
    public void retitleModule(int id,String title) {
        int at=modules.find(m->m.id()==id);
        if(at<0) throw new NoSuchElementException("Module "+id);
        modules.set(at,modules.get(at).retitled(title));
    }
    public void removeModule(int id) {
        int at=modules.find(m->m.id()==id);
        if(at<0) throw new NoSuchElementException("Module "+id);
        if(assignments.find(a->a.moduleId()==id)>=0) throw new IllegalArgumentException("Module contains assignments");
        modules.remove(at);
    }
    public void removeCourse(int id) {
        int at=courses.find(c->c.id()==id);
        if(at<0) throw new NoSuchElementException("Course "+id);
        if(modules.find(m->m.courseId()==id)>=0) throw new IllegalArgumentException("Course contains modules");
        courses.remove(at);
    }
    private AssignmentIndex index = new AssignmentIndex();
    private AssignmentTree tree = new AssignmentTree();
    private final DependencyGraph dependencies = new DependencyGraph();
    /** Advisory relationship analysis; does not change FIFO or priority eligibility. */
    public boolean addDependency(int prerequisite,int dependent) {
        return dependencies.addEdge(prerequisite,dependent);
    }
    public boolean removeDependency(int prerequisite,int dependent) {
        return dependencies.removeEdge(prerequisite,dependent);
    }
    public boolean hasDependency(int prerequisite,int dependent) {
        return dependencies.containsEdge(prerequisite,dependent);
    }
    public java.util.Set<Integer> dependencyVertices() { return dependencies.vertices(); }
    public java.util.Set<Integer> dependentsOf(int id) { return dependencies.neighborsOf(id); }
    public int dependencyVertexCount() { return dependencies.vertexCount(); }
    public int dependencyEdgeCount() { return dependencies.edgeCount(); }
    public boolean dependenciesEmpty() { return dependencies.isEmpty(); }
    public DependencyGraph.Traversal impactBfs(int start) { return dependencies.breadthFirst(start); }
    public DependencyGraph.Traversal impactDfs(int start) { return dependencies.depthFirst(start); }
    public java.util.List<Integer> dependencyPath(int from,int to) { return impactBfs(from).pathTo(to); }
    public boolean hasDependencyCycle() { return dependencies.hasCycle(); }
    /** Remove relationships but retain a vertex for every authoritative assignment. */
    public void resetDependencies() {
        dependencies.clear();for(Assignment a:snapshot()) dependencies.addVertex(a.id());
    }
    boolean dependenciesAgree() {
        if(!dependencies.isValid() || dependencies.vertexCount()!=size()) return false;
        for(Assignment a:snapshot()) if(!dependencies.containsVertex(a.id())) return false;
        return true;
    }
    private final PrioritySchedule schedule = new PrioritySchedule();
    private final java.util.List<Assignment> priorityHistory = new java.util.ArrayList<>();
    /** Enroll an existing pending assignment; duplicate enrollment is rejected. */
    public void scheduleAssignment(int id) { schedule.offer(findById(id)); }
    public java.util.Optional<Assignment> peekPriority() {
        return java.util.Optional.ofNullable(schedule.peek());
    }
    /** Start the next scheduled assignment and record the immutable ACTIVE snapshot. */
    public java.util.Optional<Assignment> startPriority() {
        Assignment item=schedule.poll();
        if(item==null) return java.util.Optional.empty();
        changeStatus(item.id(),Assignment.Status.ACTIVE);
        Assignment active=findById(item.id());priorityHistory.add(active);
        return java.util.Optional.of(active);
    }
    public int prioritySize() { return schedule.size(); }
    public boolean priorityIsEmpty() { return schedule.isEmpty(); }
    public void clearPriority() { schedule.clear(); }
    /** Cancel enrollment only; the assignment remains pending in the planner. */
    public boolean cancelPriority(int id) {
        if(id<=0) throw new IllegalArgumentException("Positive ID required");
        return schedule.cancel(id);
    }
    public java.util.List<Assignment> prioritySummary() { return schedule.summary(); }
    public java.util.List<Assignment> priorityHistory() { return java.util.List.copyOf(priorityHistory); }
    public Assignment updatePriority(int id,int value) {
        Assignment a=findById(id);
        return update(id,new Assignment(a.id(),a.courseId(),a.moduleId(),a.title(),
                a.dueDate(),value,a.status()));
    }
    boolean priorityAgrees() { return schedule.agrees(this); }
    public record ScanResult(int pendingCount, int examinedCount) { }

    public void addCourse(Course course) {
        require(course);
        if (courses.find(c -> c.id() == course.id()) >= 0) {
            throw new IllegalArgumentException("Duplicate course ID");
        }
        courses.add(course);
    }
    public void addModule(StudyModule module) {
        require(module);
        if (modules.find(m -> m.id() == module.id()) >= 0
                || courses.find(c -> c.id() == module.courseId()) < 0) {
            throw new IllegalArgumentException("Duplicate module or missing course");
        }
        modules.add(module);
    }
    public int size() { return assignments.size(); }
    public Assignment get(int index) { return assignments.get(index); }
    /** Current ID lookup through the synchronized hash index. */
    public Assignment findById(int id) {
        return index.lookup(id).match().orElseThrow(
                () -> new NoSuchElementException("Assignment " + id));
    }
    public boolean containsId(int id) { return index.contains(id); }
    public boolean isEmpty() { return size()==0; }
    /** Removes all assignments; keeps course/module registries intact. */
    public void clearAssignments() {
        while(size()>0) assignments.remove(size()-1);
        index.clear();tree.clear();schedule.clear();dependencies.clear();generations.clear();
    }

    public void addAssignment(Assignment item) {
        validateParents(item);
        if (index.contains(item.id())) {
            throw new IllegalArgumentException("Duplicate assignment ID");
        }
        if(nextGeneration==Long.MAX_VALUE) throw new IllegalStateException("Generation space exhausted");
        assignments.add(item);
        generations.put(item.id(),nextGeneration++);
        index.insert(item);tree.add(item);dependencies.addVertex(item.id());
    }
    /** Atomically replaces a record without changing its ID or list position. */
    public Assignment update(int id, Assignment replacement) {
        int index = indexOfId(id);
        validateParents(replacement);
        if (replacement.id() != id) {
            throw new IllegalArgumentException("Assignment ID cannot change");
        }
        Assignment old = assignments.set(index, replacement);
        this.index.replace(id, replacement);
        tree.replace(id, replacement);
        schedule.refresh(replacement);
        return old;
    }
    public Assignment changeStatus(int id, Assignment.Status status) {
        int index = indexOfId(id);
        Assignment replacement = assignments.get(index).withStatus(status);
        Assignment old = assignments.set(index, replacement);
        this.index.replace(id, replacement);
        tree.replace(id, replacement);
        schedule.refresh(replacement);
        return old;
    }
    public Assignment removeById(int id) {
        Assignment old = assignments.remove(indexOfId(id));
        generations.remove(id);index.remove(id);tree.remove(id);schedule.cancel(id);dependencies.removeVertex(id); return old;
    }

    /** Current tree-based lookup; absent IDs throw NoSuchElementException. */
    public Assignment findInTree(int id) {
        return tree.lookup(id).match().orElseThrow(
                () -> new NoSuchElementException("Assignment " + id));
    }
    public int treeSize() { return tree.size(); }
    public int treeHeight() { return tree.height(); }
    public Assignment minimumId() { return tree.minimum(); }
    public Assignment maximumId() { return tree.maximum(); }
    public java.util.List<Assignment> traverse(AssignmentTree.Order order) {
        return tree.traverse(order);
    }
    public java.util.List<Assignment> assignmentsInRange(int lower,int upper) {
        return tree.range(lower,upper);
    }
    /** Reconstruct derived ID indexes from authoritative immutable records.
     * Prepare both replacements before publishing; preserve graph and workflow state. */
    public void rebuildIndexes() {
        var freshIndex=new AssignmentIndex();
        var freshTree=new AssignmentTree();
        for(Assignment a:snapshot()) {freshIndex.insert(a);freshTree.add(a);}
        if(freshIndex.size()!=size() || freshTree.size()!=size() || !freshTree.isValid())
            throw new IllegalStateException("Prepared indexes do not validate");
        index=freshIndex;tree=freshTree;
    }
    /** Test diagnostic: structure, sizes, and shared references all agree. */
    boolean indexesAgree() {
        if(!tree.isValid() || tree.size()!=size() || index.size()!=size()) return false;
        for(Assignment item:snapshot()) {
            if(tree.lookup(item.id()).match().orElse(null)!=item
                    || index.lookup(item.id()).match().orElse(null)!=item) return false;
        }
        return true;
    }
    /** Returns a detached array of immutable records in insertion order. */
    public Assignment[] snapshot() {
        Assignment[] copy = new Assignment[size()];
        for (int i = 0; i < size(); i++) { copy[i] = assignments.get(i); }
        return copy;
    }
    /** Milestone 2 baseline: exactly one status comparison per assignment. */
    public ScanResult countPending() {
        int pending = 0;
        for (int i = 0; i < size(); i++) {
            if (assignments.get(i).status() == Assignment.Status.PENDING) { pending++; }
        }
        return new ScanResult(pending, size());
    }
    private int indexOfId(int id) {
        if (id <= 0) { throw new IllegalArgumentException("Positive ID required"); }
        int index = assignments.find(a -> a.id() == id);
        if (index < 0) { throw new NoSuchElementException("Assignment " + id); }
        return index;
    }
    private void validateParents(Assignment item) {
        require(item);
        int index = modules.find(m -> m.id() == item.moduleId());
        if (index < 0 || modules.get(index).courseId() != item.courseId()) {
            throw new IllegalArgumentException("Missing module or course mismatch");
        }
    }
    private void require(Object item) {
        if (item == null) { throw new IllegalArgumentException("Record required"); }
    }
}
