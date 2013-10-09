
package judge.record;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class StudentRecord implements Serializable {
    private String name;
    private TreeMap<ProblemRecord, Integer> problems;
    public StudentRecord(String name) {
        problems = new TreeMap<>();
        this.name = name;
    }
    
    public void updateProblemRecord(ProblemRecord pr, int score) {
        if(!problems.containsKey(pr))
            problems.put(pr, 0);
        if(problems.get(pr) < score)
            problems.put(pr, score);
    }
    
    public String getName() {
        return name;
    }
    
    public TreeMap<ProblemRecord, Integer> getRecords() {
        return problems;
    }
    
    public String getReport() {
        String record = "";
        Set<Map.Entry<ProblemRecord, Integer>> set = problems.entrySet();
        for(Map.Entry e : set) {
            record += "      ";
            record += e.getKey();
            record += ": ";
            record += e.getValue();
            record += "\n";
        }
        return record;
    }
}
