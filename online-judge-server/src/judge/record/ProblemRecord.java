package judge.record;

import java.io.Serializable;

public class ProblemRecord implements Comparable<ProblemRecord>, Serializable {
    private String competition;
    private String contest;
    private String problemName;
    
    public ProblemRecord(String competition, String contest, String problemName) {
        this.competition = competition;
        this.contest = contest;
        this.problemName = problemName;
    }
    
    @Override
    public int compareTo(ProblemRecord o) {
        int a = contest.compareTo(o.contest);
        if(a == 0)
            return problemName.compareTo(o.problemName);
        return a;
    }
    
    public String toString() {
        return competition+", "+contest+", "+problemName;
    }
}
