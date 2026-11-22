package edu.csc223.planner;

import java.nio.file.Path;
import java.nio.file.Files;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Random;
import java.util.HashMap;

/** Deterministic operation-count experiment, not an elapsed-time benchmark.
 * Three repeats per condition validate reproducible counts. No library sort.
 */
public final class PlannerExperiment {
    private PlannerExperiment() { }
    private static final int[] SIZES={100,250,500,1000,2000};
    private static Assignment[] dataset(int n,String kind) {
        var a=new Assignment[n];var random=new Random(223050L+n);
        for(int i=0;i<n;i++) a[i]=new Assignment(i+1,223,5,"Task "+(i+1),
                LocalDate.of(2026,11,1).plusDays(kind.equals("duplicates")?i%4:i),
                1+i%5,Assignment.Status.PENDING);
        if(kind.equals("reverse")) {
            for(int i=0;i<n/2;i++) swap(a,i,n-1-i);
        } else if(kind.equals("random") || kind.equals("duplicates")) {
            for(int i=n-1;i>0;i--) swap(a,i,random.nextInt(i+1));
        }
        return a;
    }
    private static void swap(Assignment[] a,int i,int j) {
        var value=a[i];a[i]=a[j];a[j]=value;
    }
    private static void verify(Assignment[] source,Assignment[] result) {
        var counts=new HashMap<Assignment,Integer>();
        for(var a:source) counts.merge(a,1,Integer::sum);
        for(int i=0;i<result.length;i++) {
            if(i>0 && AssignmentOrderings.BY_DUE_THEN_ID.compare(result[i-1],result[i])>0)
                throw new AssertionError("Unsorted");
            counts.merge(result[i],-1,Integer::sum);
        }
        if(source.length!=result.length || counts.values().stream().anyMatch(v->v!=0))
            throw new AssertionError("Element loss");
    }
    public static void main(String[] args) throws Exception {
        Path out=Path.of(args.length==0?"results":args[0]);Files.createDirectories(out);
        try(var csv=new PrintWriter(Files.newBufferedWriter(out.resolve("sort.csv")))) {
            csv.println("size,distribution,algorithm,trial,comparisons,shifts,copies,depth");
            for(int n:SIZES) for(String kind:new String[]{"random","sorted","reverse","duplicates"}) {
                var source=dataset(n,kind);
                for(int trial=1;trial<=3;trial++) {
                    var a=source.clone();var b=source.clone();
                    var x=SortingAlgorithms.insertionSort(a,AssignmentOrderings.BY_DUE_THEN_ID);
                    var y=SortingAlgorithms.mergeSort(b,AssignmentOrderings.BY_DUE_THEN_ID);
                    verify(source,a);verify(source,b);
                    if(!Arrays.equals(a,b)) throw new AssertionError("Different orders");
                    String[] names={"insertion","merge"};SortMetrics[] metrics={x,y};
                    for(int i=0;i<2;i++) {
                        var m=metrics[i];csv.printf("%d,%s,%s,%d,%d,%d,%d,%d%n",
                                n,kind,names[i],trial,m.comparisons(),m.shifts(),m.copies(),m.maximumDepth());
                    }
                }
            }
        }
        try(var csv=new PrintWriter(Files.newBufferedWriter(out.resolve("search.csv")))) {
            csv.println("size,condition,algorithm,trial,comparisons,found,preparationComparisons");
            for(int n:SIZES) {
                var original=dataset(n,"random");
                var view=AssignmentSearch.prepare(original);
                // Same sorted source and keys isolate search-method costs.
                var equivalent=view.snapshot();
                String[] kinds={"first","middle","last","missing"};
                int[] targets={1,n/2,n,n+1};
                for(int t=0;t<targets.length;t++) for(int trial=1;trial<=3;trial++) {
                    var a=AssignmentSearch.sequential(equivalent,targets[t]);
                    var b=AssignmentSearch.binary(view,targets[t]);
                    if(!a.match().equals(b.match()) || a.match().isPresent()!=(targets[t]<=n))
                        throw new AssertionError("Search mismatch");
                    String[] names={"sequential","binary"};
                    AssignmentSearch.Result[] results={a,b};
                    for(int i=0;i<2;i++) csv.printf("%d,%s,%s,%d,%d,%s,%d%n",
                            n,kinds[t],names[i],trial,results[i].comparisons(),
                            results[i].match().isPresent(),view.preparation().comparisons());
                }
            }
        }
        System.out.println("Verified 120 sort records and 120 search records.");
        System.out.println("Counts only; no elapsed-time or speed measurements.");
        System.out.println("Runtime "+System.getProperty("java.runtime.version"));
    }
}
