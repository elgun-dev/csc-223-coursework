package edu.csc223.planner;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Deterministic comparator counts, not elapsed-time measurements. */
public final class TreeExperiment {
    private static void balanced(int low,int high,List<Integer> order) {
        if(low>high) return;int middle=low+(high-low)/2;
        order.add(middle);balanced(low,middle-1,order);balanced(middle+1,high,order);
    }
    public static void main(String[] args) throws Exception {
        var rows=new ArrayList<String>();
        rows.add("size,order,height,condition,target,trial,comparisons,found");
        for(int n:new int[]{31,63,127,255,511}) {
            for(String kind:new String[]{"ascending","descending","balanced"}) {
                List<Integer> order=new ArrayList<>();
                if(kind.equals("balanced")) balanced(1,n,order);
                else for(int i=1;i<=n;i++) order.add(kind.equals("ascending")?i:n+1-i);
                var tree=new AssignmentTree();
                for(int id:order) tree.add(PlannerTreeDemo.item(id));
                if(!tree.isValid() || tree.size()!=n) throw new AssertionError("Invalid tree");
                int height=tree.height();
                int[] targets={1,(n+1)/2,n,n+1};
                String[] conditions={"lowest","middle","highest","missing-high"};
                for(int c=0;c<4;c++) for(int trial=1;trial<=3;trial++) {
                    var result=tree.lookup(targets[c]);
                    if(result.match().isPresent()!=(c<3)
                            || (c<3 && result.match().orElseThrow().id()!=targets[c]))
                        throw new AssertionError("Incorrect search");
                    rows.add(n+","+kind+","+height+","+conditions[c]+","+targets[c]
                            +","+trial+","+result.comparisons()+","+result.match().isPresent());
                }
            }
        }
        Path folder=Path.of(args.length==0?"results":args[0]);Files.createDirectories(folder);
        Files.write(folder.resolve("tree.csv"),rows);
        System.out.println("Verified trial rows: "+(rows.size()-1));
    }
}
