package edu.csc223.planner;
/** Test-only executable corruption/recovery evidence; no production mutation hook. */
public final class FinalValidationEvidence {
    public static void main(String[] args) throws Exception {
        var p=ReleaseDemo.fixture();System.out.println("Version="+ReleasePlanner.VERSION);
        System.out.println("Before="+p.validate());
        FinalReleaseTest.corruptHash(p,2);
        System.out.println("After controlled hash removal="+p.validate());
        System.out.println("After rebuild="+p.rebuildDerivedIndexes());
        System.out.println("Restored ID 2="+p.find(2).title());
        System.out.println("Preserved path="+p.path(1,3));
        System.out.println("Recommendation="+p.recommend().orElseThrow().id());
    }
}
