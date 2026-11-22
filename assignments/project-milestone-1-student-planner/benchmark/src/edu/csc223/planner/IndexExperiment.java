package edu.csc223.planner;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Counts actual ID equality comparisons, excluding setup and bucket arithmetic. */
public final class IndexExperiment {
    public static void main(String[] args) throws Exception {
        Path directory=Path.of(args.length==0?"results":args[0]);
        Files.createDirectories(directory);
        List<String> rows=new ArrayList<>();
        rows.add("size,layout,condition,target,trial,sequential,hash,found,capacity,load");
        for(int n:new int[]{100,250,500,1000,2000}) {
            int capacity=8; while(n>capacity*0.75) capacity*=2;
            for(String layout:new String[]{"consecutive","collision-heavy"}) {
                AssignmentIndex index=new AssignmentIndex();
                Assignment[] data=new Assignment[n];
                for(int i=0;i<n;i++) {
                    int id=layout.equals("consecutive")?i+1:1+i*capacity;
                    data[i]=PlannerIndexDemo.item(id);index.insert(data[i]);
                }
                int missing=layout.equals("consecutive")?n+1:1+n*capacity;
                int[] targets={data[0].id(),data[n/2].id(),data[n-1].id(),missing};
                String[] labels={"first","middle","last","missing"};
                for(int c=0;c<targets.length;c++) for(int trial=1;trial<=3;trial++) {
                    var sequential=AssignmentSearch.sequential(data,targets[c]);
                    var hashed=index.lookup(targets[c]);
                    if(!sequential.match().equals(hashed.match())
                            || hashed.match().isPresent()!=(c<3))
                        throw new AssertionError("Lookup disagreement");
                    rows.add(n+","+layout+","+labels[c]+","+targets[c]+","+trial+","
                            +sequential.comparisons()+","+hashed.comparisons()+","
                            +hashed.match().isPresent()+","+index.capacity()+","+index.loadFactor());
                }
            }
        }
        Files.write(directory.resolve("retrieval.csv"),rows);
        System.out.println("Verified retrieval trial rows: " + (rows.size()-1));
    }
}
