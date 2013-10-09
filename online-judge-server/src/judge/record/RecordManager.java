package judge.record;

import java.io.*;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;

public class RecordManager {
    private ArrayList<StudentRecord> students;
    public RecordManager(String recordLocation) throws Exception {
        students = new ArrayList<>();
        if(!new File(recordLocation).exists())
            return;
        FileInputStream fis = new FileInputStream(recordLocation);
        ObjectInputStream ois = new ObjectInputStream(fis);
        int records = ois.readInt();
        for(int i=0;i<records;i++)
            students.add((StudentRecord)ois.readObject());
        ois.close();
        fis.close();
    }
    
    public void addScore(String student, ProblemRecord pr, int score) {
        boolean found = false;
        for(int i=0;i<students.size();i++)
            if(students.get(i).getName().contentEquals(student)) {
                found = true;
                students.get(i).updateProblemRecord(pr, score);
            }
        if(!found) {
            StudentRecord toAdd = new StudentRecord(student);
            toAdd.updateProblemRecord(pr, score);
            students.add(toAdd);
        }
    }
    
    public void saveDatabase(String recordLocation) throws Exception {
        FileOutputStream fos = new FileOutputStream(recordLocation);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeInt(students.size());
        for(StudentRecord s : students)
            oos.writeObject(s);
        oos.close();
        fos.close();
        FileOutputStream report = new FileOutputStream(recordLocation+"report.txt");
        report.write(getReport().getBytes());
        report.close();
    }
    
    public String getReport() {
        String report = "";
        for(StudentRecord sr : students) {
            report+= sr.getName()+":";
            report+= "\n";
            report += sr.getReport();
            report += "\n";
        }
        return report;
    }
}
