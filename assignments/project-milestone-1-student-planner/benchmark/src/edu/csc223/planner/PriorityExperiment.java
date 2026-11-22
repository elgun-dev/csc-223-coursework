package edu.csc223.planner;

import java.util.*;
import java.nio.file.*;
import java.io.*;

/** Counts comparator calls, not elapsed time; data creation and verification are excluded. */
public final class PriorityExperiment {
    static final class Counter implements Comparator<PrioritySchedule.Entry> {
        long calls;
        public int compare(PrioritySchedule.Entry a,PrioritySchedule.Entry b) {
            calls++;return PrioritySchedule.ORDER.compare(a,b);
        }
    }
    public static void main(String[] args) throws Exception {
        Path dir=Path.of(args.length==0?"results":args[0]);Files.createDirectories(dir);
        try(var out=new PrintWriter(Files.newBufferedWriter(dir.resolve("priority.csv")))) {
            out.println("n,order,trial,method,insert,peek,remove,total");
            for(int n:new int[]{128,256,512,1024,2048})
                for(String order:List.of("ascending","descending","random"))
                    for(int trial=1;trial<=3;trial++) {
                        var random=new Random(2230800L+trial);
                        var data=new ArrayList<PrioritySchedule.Entry>();
                        for(int i=1;i<=n;i++) data.add(new PrioritySchedule.Entry(
                                PlannerPriorityDemo.item(i,1+random.nextInt(5),1+random.nextInt(28)),i));
                        if(order.equals("ascending")) data.sort(PrioritySchedule.ORDER);
                        if(order.equals("descending")) data.sort(PrioritySchedule.ORDER.reversed());
                        var expected=new ArrayList<>(data);expected.sort(PrioritySchedule.ORDER);
                        for(String method:List.of("heap","list")) {
                            var c=new Counter();var result=new ArrayList<PrioritySchedule.Entry>();
                            long insert,peek,remove;PrioritySchedule.Entry first;
                            if(method.equals("heap")) {
                                var h=new BinaryHeap<PrioritySchedule.Entry>(c);
                                for(var e:data) h.add(e);insert=c.calls;c.calls=0;
                                first=h.peek();peek=c.calls;c.calls=0;
                                while(!h.isEmpty()) result.add(h.removeRoot());remove=c.calls;
                            } else {
                                var list=new ArrayList<PrioritySchedule.Entry>();list.addAll(data);
                                insert=c.calls;c.calls=0;first=list.get(best(list,c));peek=c.calls;c.calls=0;
                                while(!list.isEmpty()) {
                                    int at=best(list,c);var e=list.get(at);int last=list.size()-1;
                                    list.set(at,list.get(last));list.remove(last);result.add(e);
                                }
                                remove=c.calls;
                            }
                            if(!first.equals(expected.get(0)) || !result.equals(expected))
                                throw new AssertionError("Incorrect processing order");
                            out.printf(Locale.ROOT,"%d,%s,%d,%s,%d,%d,%d,%d%n",n,order,trial,method,
                                    insert,peek,remove,insert+peek+remove);
                        }
                    }
        }
        System.out.println("90 verified rows written to "+dir.resolve("priority.csv"));
    }
    private static int best(List<PrioritySchedule.Entry> list,Counter c) {
        int best=0;
        for(int i=1;i<list.size();i++) if(c.compare(list.get(i),list.get(best))<0) best=i;
        return best;
    }
}
