package edu.csc223.planner;

import java.nio.file.*;
import java.io.*;
import java.util.*;
/** Operation counts from actual traversals, excluding graph generation and output. */
public final class GraphExperiment {
    static DependencyGraph generate(int n,String condition) {
        var g=new DependencyGraph();for(int i=1;i<=n;i++) g.addVertex(i);
        if(condition.equals("dense")) {
            for(int i=1;i<=n;i++) for(int j=i+1;j<=n;j++) g.addEdge(i,j);
        } else if(condition.equals("sparse")) {
            for(int i=1;i<n;i++) g.addEdge(i,i+1);
            for(int i=1;i<n-1;i++) g.addEdge(i,i+2);
        } else {
            for(int i=1;i<n;i++) if(i!=n/2) g.addEdge(i,i+1);
        }
        return g;
    }
    public static void main(String[] args) throws Exception {
        Path dir=Path.of(args.length==0?"results":args[0]);Files.createDirectories(dir);
        try(var out=new PrintWriter(Files.newBufferedWriter(dir.resolve("graph.csv")))) {
            out.println("vertices,edges,condition,trial,algorithm,reached,examined,peak");
            for(int n:new int[]{32,64,128,256,512}) for(String condition:List.of("sparse","dense","split")) {
                var g=generate(n,condition);
                if(!g.isValid()) throw new AssertionError("Invalid graph");
                for(int trial=1;trial<=3;trial++) for(String algorithm:List.of("BFS","DFS")) {
                    var result=algorithm.equals("BFS")?g.breadthFirst(1):g.depthFirst(1);
                    int reached=condition.equals("split")?n/2:n;
                    long examined=condition.equals("split")?n/2-1:g.edgeCount();
                    if(result.order().size()!=reached || result.edgesExamined()!=examined)
                        throw new AssertionError("Traversal work mismatch");
                    out.printf(Locale.ROOT,"%d,%d,%s,%d,%s,%d,%d,%d%n",n,g.edgeCount(),condition,trial,
                            algorithm,result.order().size(),result.edgesExamined(),result.peakFrontier());
                }
            }
        }
        System.out.println("90 verified traversal measurements written to "+dir.resolve("graph.csv"));
    }
}
