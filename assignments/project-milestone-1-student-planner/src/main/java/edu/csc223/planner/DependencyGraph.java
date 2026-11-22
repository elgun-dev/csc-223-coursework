package edu.csc223.planner;

import java.util.*;
/** Directed unweighted prerequisite -> dependent relationships, in insertion order.
 * Positive IDs only; self-loops rejected; multivertex cycles retained for auditing. */
public final class DependencyGraph {
    private final Map<Integer,LinkedHashSet<Integer>> outgoing=new LinkedHashMap<>();
    private int edges;
    private void positive(int id) {if(id<=0) throw new IllegalArgumentException("Positive ID required");}
    private LinkedHashSet<Integer> require(int id) {
        positive(id);var values=outgoing.get(id);
        if(values==null) throw new IllegalArgumentException("Unknown graph vertex "+id);
        return values;
    }
    public boolean addVertex(int id) {
        positive(id);return outgoing.putIfAbsent(id,new LinkedHashSet<>())==null;
    }
    public boolean containsVertex(int id) {positive(id);return outgoing.containsKey(id);}
    public boolean removeVertex(int id) {
        positive(id);var removed=outgoing.remove(id);if(removed==null) return false;
        edges-=removed.size();for(var set:outgoing.values()) if(set.remove(id)) edges--;
        return true;
    }
    public boolean addEdge(int prerequisite,int dependent) {
        var set=require(prerequisite);require(dependent);
        if(prerequisite==dependent) throw new IllegalArgumentException("Self prerequisite");
        if(!set.add(dependent)) return false;edges++;return true;
    }
    public boolean containsEdge(int from,int to) {
        positive(from);positive(to);return outgoing.containsKey(from) && outgoing.get(from).contains(to);
    }
    public boolean removeEdge(int from,int to) {
        positive(from);positive(to);
        if(!outgoing.containsKey(from) || !outgoing.get(from).remove(to)) return false;
        edges--;return true;
    }
    public Set<Integer> vertices() {return Collections.unmodifiableSet(new LinkedHashSet<>(outgoing.keySet()));}
    public Set<Integer> neighborsOf(int id) {return Collections.unmodifiableSet(new LinkedHashSet<>(require(id)));}
    public int vertexCount() {return outgoing.size();}
    public int edgeCount() {return edges;}
    public boolean isEmpty() {return outgoing.isEmpty();}
    public void clear() {outgoing.clear();edges=0;}
    public boolean isValid() {
        long count=0;
        for(var entry:outgoing.entrySet()) for(int neighbor:entry.getValue()) {
            if(neighbor==entry.getKey() || !outgoing.containsKey(neighbor)) return false;
            count++;
        }
        return count==edges && edges>=0;
    }
    /** Detached results; BFS levels are minimum hops, DFS levels are tree depth. */
    public record Traversal(int source,Set<Integer> vertices,List<Integer> order,
            Map<Integer,Integer> predecessor,Map<Integer,Integer> level,
            long edgesExamined,int peakFrontier) {
        public Traversal {
            vertices=Collections.unmodifiableSet(new LinkedHashSet<>(vertices));order=List.copyOf(order);
            predecessor=Collections.unmodifiableMap(new LinkedHashMap<>(predecessor));
            level=Collections.unmodifiableMap(new LinkedHashMap<>(level));
        }
        private void check(int target) {
            if(!vertices.contains(target)) throw new IllegalArgumentException("Unknown target");
        }
        public boolean reachable(int target) {check(target);return level.containsKey(target);}
        public int levelOf(int target) {check(target);return level.getOrDefault(target,-1);}
        public List<Integer> pathTo(int target) {
            check(target);if(!reachable(target)) return List.of();
            var path=new ArrayList<Integer>();var seen=new HashSet<Integer>();Integer current=target;
            while(current!=null && current!=source) {
                if(!seen.add(current)) throw new IllegalStateException("Cyclic predecessor chain");
                path.add(current);current=predecessor.get(current);
            }
            if(current==null) throw new IllegalStateException("Broken predecessor chain");
            path.add(source);Collections.reverse(path);return List.copyOf(path);
        }
    }
    public Traversal breadthFirst(int start) {
        require(start);var order=new ArrayList<Integer>();var previous=new LinkedHashMap<Integer,Integer>();
        var level=new LinkedHashMap<Integer,Integer>();var queue=new ArrayDeque<Integer>();
        queue.addLast(start);level.put(start,0);long examined=0;int peak=1;
        while(!queue.isEmpty()) {
            int current=queue.removeFirst();order.add(current);
            for(int next:outgoing.get(current)) {
                examined++;
                if(!level.containsKey(next)) {
                    level.put(next,level.get(current)+1);previous.put(next,current);queue.addLast(next);
                    peak=Math.max(peak,queue.size());
                }
            }
        }
        return new Traversal(start,vertices(),order,previous,level,examined,peak);
    }
    private record Frame(int vertex,Iterator<Integer> neighbors) { }
    public Traversal depthFirst(int start) {
        require(start);var order=new ArrayList<Integer>();var previous=new LinkedHashMap<Integer,Integer>();
        var level=new LinkedHashMap<Integer,Integer>();var stack=new ArrayDeque<Frame>();
        level.put(start,0);order.add(start);stack.push(new Frame(start,outgoing.get(start).iterator()));
        long examined=0;int peak=1;
        while(!stack.isEmpty()) {
            var frame=stack.peek();
            if(!frame.neighbors().hasNext()) {stack.pop();continue;}
            int next=frame.neighbors().next();examined++;
            if(!level.containsKey(next)) {
                level.put(next,level.get(frame.vertex())+1);previous.put(next,frame.vertex());order.add(next);
                stack.push(new Frame(next,outgoing.get(next).iterator()));peak=Math.max(peak,stack.size());
            }
        }
        return new Traversal(start,vertices(),order,previous,level,examined,peak);
    }
    /** Gray vertices are active ancestors, black vertices have finished. Scan all components. */
    public boolean hasCycle() {
        var color=new HashMap<Integer,Integer>();var stack=new ArrayDeque<Frame>();
        for(int root:outgoing.keySet()) {
            if(color.containsKey(root)) continue;
            color.put(root,1);stack.push(new Frame(root,outgoing.get(root).iterator()));
            while(!stack.isEmpty()) {
                var frame=stack.peek();
                if(!frame.neighbors().hasNext()) {color.put(frame.vertex(),2);stack.pop();continue;}
                int next=frame.neighbors().next();int state=color.getOrDefault(next,0);
                if(state==1) return true;
                if(state==0) {color.put(next,1);stack.push(new Frame(next,outgoing.get(next).iterator()));}
            }
        }
        return false;
    }
}
