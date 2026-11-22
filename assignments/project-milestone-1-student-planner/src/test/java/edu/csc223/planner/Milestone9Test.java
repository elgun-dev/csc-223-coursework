package edu.csc223.planner;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
class Milestone9Test {
    private DependencyGraph g(int n) {
        var g=new DependencyGraph();for(int i=1;i<=n;i++) g.addVertex(i);return g;
    }
    private void check(PlannerService p) {
        assertTrue(p.dependenciesAgree());assertTrue(p.indexesAgree());assertTrue(p.priorityAgrees());
    }
    @Test void emptyGraph() {
        var g=g(0);assertTrue(g.isEmpty());assertEquals(0,g.edgeCount());assertFalse(g.hasCycle());assertTrue(g.isValid());
    }
    @Test void vertexAddAndDuplicate() {
        var g=g(0);assertTrue(g.addVertex(1));assertTrue(g.addVertex(2));assertFalse(g.addVertex(1));
        assertEquals(2,g.vertexCount());assertTrue(g.containsVertex(2));assertTrue(g.isValid());
    }
    @Test void directedEdgesAndDuplicates() {
        var g=g(2);assertTrue(g.addEdge(1,2));assertFalse(g.addEdge(1,2));assertFalse(g.containsEdge(2,1));
        assertEquals(1,g.edgeCount());assertEquals(Set.of(2),g.neighborsOf(1));assertTrue(g.isValid());
    }
    @Test void missingEndpointsAndInvalidIds() {
        var g=g(2);assertThrows(IllegalArgumentException.class,()->g.addEdge(1,9));
        assertThrows(IllegalArgumentException.class,()->g.addVertex(0));
        assertThrows(IllegalArgumentException.class,()->g.neighborsOf(9));assertEquals(0,g.edgeCount());
    }
    @Test void selfLoopRejected() {
        var g=g(1);assertThrows(IllegalArgumentException.class,()->g.addEdge(1,1));assertTrue(g.isValid());
    }
    @Test void removeVertexCleansAllIncidentEdges() {
        var g=g(3);g.addEdge(1,2);g.addEdge(2,1);g.addEdge(2,3);g.addEdge(3,1);
        assertTrue(g.removeVertex(2));assertFalse(g.removeVertex(2));assertEquals(1,g.edgeCount());
        assertEquals(Set.of(1),g.neighborsOf(3));assertTrue(g.isValid());
    }
    @Test void removeEdgeClearReuse() {
        var g=g(2);g.addEdge(1,2);assertTrue(g.removeEdge(1,2));assertFalse(g.removeEdge(1,2));
        g.clear();assertTrue(g.isEmpty());assertEquals(0,g.edgeCount());g.addVertex(3);assertTrue(g.isValid());
    }
    @Test void snapshotsAreDetachedAndProtected() {
        var g=g(3);g.addEdge(1,2);var neighbors=g.neighborsOf(1);var vertices=g.vertices();
        assertThrows(UnsupportedOperationException.class,neighbors::clear);
        assertThrows(UnsupportedOperationException.class,vertices::clear);g.addEdge(1,3);g.addVertex(4);
        assertEquals(Set.of(2),neighbors);assertEquals(3,vertices.size());
    }
    @Test void knownEightVertexBfs() {
        var p=PlannerGraphDemo.example();assertEquals(List.of(1,2,3,4,5,6,7),p.impactBfs(1).order());check(p);
    }
    @Test void knownEightVertexDfs() {
        var p=PlannerGraphDemo.example();assertEquals(List.of(1,2,4,6,7,3,5),p.impactDfs(1).order());check(p);
    }
    @Test void isolatedAndDisconnected() {
        var p=PlannerGraphDemo.example();assertEquals(List.of(8),p.impactBfs(8).order());
        assertEquals(List.of(8),p.impactDfs(8).order());assertFalse(p.impactBfs(1).reachable(8));
    }
    @Test void cycleVisitedExactlyOnce() {
        var p=PlannerGraphDemo.example();assertTrue(p.hasDependencyCycle());
        assertEquals(7,new HashSet<>(p.impactBfs(1).order()).size());assertEquals(7,p.impactDfs(1).order().size());
        assertEquals(8,p.impactBfs(1).edgesExamined());assertEquals(8,p.impactDfs(1).edgesExamined());
    }
    @Test void missingStartAndTarget() {
        var p=PlannerGraphDemo.example();assertThrows(IllegalArgumentException.class,()->p.impactBfs(999));
        assertThrows(IllegalArgumentException.class,()->p.impactDfs(999));
        assertThrows(IllegalArgumentException.class,()->p.dependencyPath(1,999));
    }
    @Test void reachableShortestChainHasRealEdges() {
        var p=PlannerGraphDemo.example();var b=p.impactBfs(1);var path=b.pathTo(7);
        assertEquals(List.of(1,2,4,6,7),path);assertEquals(4,b.levelOf(7));
        for(int i=1;i<path.size();i++) assertTrue(p.hasDependency(path.get(i-1),path.get(i)));
    }
    @Test void unreachableAndSourcePaths() {
        var p=PlannerGraphDemo.example();assertEquals(List.of(),p.dependencyPath(1,8));
        assertEquals(-1,p.impactBfs(1).levelOf(8));assertEquals(List.of(1),p.dependencyPath(1,1));
        assertEquals(0,p.impactBfs(1).levelOf(1));assertEquals(List.of(),p.dependencyPath(7,1));
    }
    @Test void dagCrossEdgesAndDisconnectedCycle() {
        var g=g(6);g.addEdge(1,2);g.addEdge(1,3);g.addEdge(2,4);g.addEdge(3,4);
        assertFalse(g.hasCycle());g.addEdge(5,6);g.addEdge(6,5);assertTrue(g.hasCycle());
    }
    @Test void removeContradictoryRelationship() {
        var p=PlannerGraphDemo.example();assertTrue(p.removeDependency(7,3));assertFalse(p.hasDependencyCycle());check(p);
    }
    @Test void domainAddSynchronizesIsolatedVertex() {
        var p=PlannerGraphDemo.planner();p.addAssignment(PlannerGraphDemo.item(1,"Work"));
        assertEquals(Set.of(1),p.dependencyVertices());assertEquals(0,p.dependencyEdgeCount());check(p);
    }
    @Test void domainEditPreservesIdsAndRelationships() {
        var p=PlannerGraphDemo.example();p.update(2,PlannerGraphDemo.item(2,"Changed"));
        assertTrue(p.hasDependency(1,2));assertEquals("Changed",p.findInTree(2).title());
        assertThrows(IllegalArgumentException.class,()->p.update(2,PlannerGraphDemo.item(99,"Wrong ID")));check(p);
    }
    @Test void statusAndPriorityUpdatesPreserveGraph() {
        var p=PlannerGraphDemo.example();p.scheduleAssignment(2);p.updatePriority(2,5);p.startPriority();
        assertEquals(Assignment.Status.ACTIVE,p.findById(2).status());assertEquals(8,p.dependencyEdgeCount());check(p);
    }
    @Test void domainRemovalCannotLeaveStaleEdgesOnIdReuse() {
        var p=PlannerGraphDemo.example();p.scheduleAssignment(4);p.removeById(4);
        assertFalse(p.hasDependency(2,4));assertFalse(p.hasDependency(4,6));assertEquals(6,p.dependencyEdgeCount());
        p.addAssignment(PlannerGraphDemo.item(4,"New record"));assertTrue(p.dependentsOf(4).isEmpty());check(p);
    }
    @Test void relationResetAndDomainClear() {
        var p=PlannerGraphDemo.example();p.resetDependencies();assertEquals(8,p.dependencyVertexCount());
        assertEquals(0,p.dependencyEdgeCount());check(p);p.clearAssignments();assertTrue(p.dependenciesEmpty());check(p);
    }
    @Test void traversalSnapshotsAndPredecessors() {
        var p=PlannerGraphDemo.example();var d=p.impactDfs(1);
        for(var e:d.predecessor().entrySet()) {
            assertTrue(p.hasDependency(e.getValue(),e.getKey()));assertEquals(d.levelOf(e.getValue())+1,d.levelOf(e.getKey()));
        }
        assertThrows(UnsupportedOperationException.class,()->d.order().clear());
        assertThrows(UnsupportedOperationException.class,()->d.predecessor().clear());
        p.clearAssignments();assertEquals(7,d.order().size());
    }
    @Test void longChainAvoidsCallStackOverflow() {
        var g=g(5000);for(int i=1;i<5000;i++) g.addEdge(i,i+1);
        assertEquals(4999,g.depthFirst(1).levelOf(5000));assertFalse(g.hasCycle());
    }
}
