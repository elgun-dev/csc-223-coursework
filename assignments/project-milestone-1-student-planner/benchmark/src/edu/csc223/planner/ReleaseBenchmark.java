package edu.csc223.planner;
import java.util.*;
import java.time.LocalDate;
/** Compare the exact old and new bulk-build paths on identical scheduler entries. */
public final class ReleaseBenchmark {
    static volatile int sink;
    static final class Counted implements Comparator<PrioritySchedule.Entry> {
        long count;
        public int compare(PrioritySchedule.Entry a,PrioritySchedule.Entry b){count++;return PrioritySchedule.ORDER.compare(a,b);}
    }
    static List<PrioritySchedule.Entry> data(int n,String condition){
        var values=new ArrayList<PrioritySchedule.Entry>();
        for(int i=0;i<n;i++){
            var a=new Assignment(i+1,223,8,"Task "+i,LocalDate.of(2026,12,14).plusDays(i%31),i%5+1,Assignment.Status.PENDING);
            values.add(new PrioritySchedule.Entry(a,i));
        }
        values.sort(PrioritySchedule.ORDER);
        if(condition.equals("reverse"))Collections.reverse(values);
        if(condition.equals("random"))Collections.shuffle(values,new Random(22310+n));
        return values;
    }
    static void measure(int n,String condition,int trial,boolean optimized,boolean print){
        var data=data(n,condition);var cmp=new Counted();long start=System.nanoTime();
        BinaryHeap<PrioritySchedule.Entry> heap=optimized?new BinaryHeap<>(cmp,data):HeapBuildBaseline.build(cmp,data);
        long ns=System.nanoTime()-start,comparisons=cmp.count;
        var oracle=new ArrayList<>(data);oracle.sort(PrioritySchedule.ORDER);
        if(!heap.isValidHeap())throw new AssertionError("Heap invariant");
        for(var expected:oracle)if(!expected.equals(heap.removeRoot()))throw new AssertionError("Order mismatch");
        if(!heap.isEmpty())throw new AssertionError("Extra values");sink=oracle.get(0).item().id();
        if(print)System.out.printf(Locale.ROOT,"%d,%s,%d,%s,%d,%d%n",n,condition,trial,optimized?"bottom_up":"insertion",comparisons,ns);
    }
    public static void main(String[] args){
        for(int i=0;i<5;i++){measure(1024,"random",i,false,false);measure(1024,"random",i,true,false);}
        System.out.println("size,condition,trial,version,comparisons,elapsed_ns");
        for(int n:new int[]{16,64,256,1024,4096})for(String c:List.of("ordered","reverse","random"))for(int t=1;t<=5;t++){
            measure(n,c,t,t%2==0,true);measure(n,c,t,t%2!=0,true);
        }
    }
}
