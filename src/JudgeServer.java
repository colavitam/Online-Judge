
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Calendar;

public class JudgeServer implements Codes{
    final static String testDirectory = "C:\\Judge\\";
    final static String javaCompile = "\"C:\\Program Files (x86)\\Java\\jdk1.7.0_02\\bin\\javac\"";
    final static String javaExec = "\"C:\\Program Files (x86)\\Java\\jdk1.7.0_02\\bin\\java\"";
    final static long USACO_TIME_LIMIT = 2000;
    final static long OTHER_TIME_LIMIT = 30000;
    public static void main(String[] args) {
        while(true) { //Ultimate failsafe
            InputStream read=null;
            OutputStream sendInfo=null;
            String dirName=null;
            Socket socket=null;
            ServerSocket servSocket=null;
            try {
                servSocket=new ServerSocket(13786);
                System.out.print("Awaiting connection... ");
                socket=servSocket.accept();
                System.out.print("Now judging... ");
                dirName=testDirectory+System.nanoTime()+"\\";
                File newDir=new File(dirName);
                newDir.mkdir();
                ByteArrayOutputStream baos=new ByteArrayOutputStream();
                read=socket.getInputStream();
                int len = -1;
                byte[] buffer=new byte[4096];
                while((len = read.read(buffer)) != -1) {
                    baos.write(buffer,0,len);
                }
                socket.shutdownInput();
                String fileContents=baos.toString();
                String[] sects=fileContents.split("===END HEADER===");
                String header=sects[0];
                String[] headerVals=header.split("===SPLIT HEADER===");
                String name=headerVals[0];
                FileOutputStream fos=new FileOutputStream(dirName+name);
                fos.write(fileContents.substring(fileContents.indexOf("===END HEADER===")+16).getBytes());
                fos.close();
                //It's judging time...
                sendInfo=socket.getOutputStream();
                sendInfo.write(JUDGING_INIT);
                //Compile...
                String language=headerVals[4];
                switch(language) {
                    case "Java":
                        ProcessBuilder builder=new ProcessBuilder(new String[]{javaCompile,dirName+name});
                        Process comp = builder.start();
                        int compileResult=comp.waitFor();
                        if(compileResult != 0) {
                            sendInfo.write(COMPILE_FAIL);
                            sendInfo.write(JUDGING_ABORT);
                            System.out.println("Compile fail.");
                            break;
                        }
                        sendInfo.write(COMPILE_PASS);
                        boolean success=true;
                        int tests=testCount(headerVals);
                        if(tests==0) {
                            sendInfo.write(INVALID_PROBLEM);
                            sendInfo.write(JUDGING_ABORT);
                            System.out.println("Invalid problem.");
                            break;
                        }
                        outerLoop:
                        for(int i=1;i<=tests;i++) {
                            //Move files into place...
                            prepare(headerVals,dirName,i);
                            //Execute program
                            builder=new ProcessBuilder(new String[]{javaExec,name.split("\\.")[0]});
                            new File(dirName+"input.in").createNewFile();
                            new File(dirName+"output.out").createNewFile();
                            builder.redirectInput(new File(dirName+"input.in"));
                            builder.redirectOutput(new File(dirName+"output.out"));
                            builder.directory(new File(dirName));
                            Process run = builder.start();
                            long startTime=Calendar.getInstance().getTimeInMillis();
                            while(true) {
                                try {
                                    run.exitValue();
                                }
                                catch(Exception e) {
                                    long timeDif=Calendar.getInstance().getTimeInMillis()-startTime;
                                    if(!timeOkay(headerVals,timeDif)) {
                                        sendInfo.write(TEST_FAIL_TIMEOUT);
                                        success=false;
                                        run.destroy();
                                        System.out.println("T.");
                                        break outerLoop;
                                    }
                                    continue;
                                }
                                break;
                            }
                            //Program completed!
                            boolean correct=checkResult(headerVals,dirName,i);
                            if(correct) {
                                sendInfo.write(TEST_PASS);
                                System.out.print(i+" ");
                            }
                            else {
                                sendInfo.write(TEST_FAIL_WRONG);
                                System.out.print("X ");
                                success=false;
                                break;
                            }
                        }
                        if(success) {
                            sendInfo.write(TESTS_GOOD);
                            System.out.println("... Pass.");
                        }
                        else {
                            sendInfo.write(TESTS_BAD);
                            System.out.println("... Fail.");
                        }
                        break;
                    case "Python":
                        break;
                    case "C++":
                        break;
                }
            }
            catch(Exception e) {
                try {
                    sendInfo.write(JUDGING_ERROR);
                    sendInfo.write(JUDGING_ABORT);
                }
                catch(Exception ex) {
                    
                }
                e.printStackTrace();
            }
            finally {
                try {
                    socket.close();
                    servSocket.close();
                    read.close();
                    sendInfo.close();
                    File dir=new File(dirName);
                    for(File subfile : dir.listFiles()) {
                        subfile.delete();
                    }
                    dir.delete();
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public static boolean timeOkay(String[] headers,long timeInMillis) {
        switch(headers[1]) {
            case "USACO":
                return timeInMillis<=USACO_TIME_LIMIT;
            case "WPI":
                return timeInMillis<=OTHER_TIME_LIMIT;
            case "FSU":
                return timeInMillis<=OTHER_TIME_LIMIT;
            case "PClassic":
                return timeInMillis<=OTHER_TIME_LIMIT;
        }
        return true;
    }
    public static int testCount(String[] headers) throws IOException {
        for(int i=0;i<Integer.MAX_VALUE;i++) {
            File databank=new File(testDirectory+"Testing Data\\"+headers[1]+"."+headers[2]+"."+headers[3]+"\\"+(i+1)+".in");
            if(!databank.exists()) {
                return i;
            }
        }
        return 0;
    }
    public static String getInput(String[] headers, int testNumber) throws IOException{
        File databank=new File(testDirectory+"Testing Data\\"+headers[1]+"."+headers[2]+"."+headers[3]+"\\"+testNumber+".in");
        return readAll(databank);
    }
    public static String getUSACOName(String[] headers) throws IOException {
        File databank=new File(testDirectory+"Testing Data\\"+headers[1]+"."+headers[2]+"."+headers[3]+"\\name.txt");
        return readAll(databank);
    }
    public static void prepare(String[] headers, String dirName, int testNumber) throws IOException{
        String origin=headers[1];
        switch(origin) {
            case "USACO":
                File tbout=new File(dirName+getUSACOName(headers)+".out");
                if(tbout.exists())
                    tbout.delete();
                FileOutputStream fos=new FileOutputStream(dirName+getUSACOName(headers)+".in");
                fos.write(getInput(headers,testNumber).getBytes());
                fos.close();
                break;
            default:
                File tbout2=new File(dirName+"output.out");
                if(tbout2.exists())
                    tbout2.delete();
                FileOutputStream fos2=new FileOutputStream(dirName+"input.in");
                fos2.write(getInput(headers,testNumber).getBytes());
                fos2.close();
                break;
        }
    }
    public static String readAll(File file) throws IOException{
        FileInputStream fis=new FileInputStream(file);
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        int len = 0;
        byte[] buffer=new byte[4096];
        while((len = fis.read(buffer)) != -1) {
            baos.write(buffer,0,len);
        }
        fis.close();
        return baos.toString();
    }
    public static String expectedResult(String[] headers,String dirName,int testNumber) throws IOException{
        File databank=new File(testDirectory+"Testing Data\\"+headers[1]+"."+headers[2]+"."+headers[3]+"\\"+testNumber+".out");
        return readAll(databank);
    }
    public static boolean checkResult(String[] headers,String dirName, int testNumber) throws IOException{
        String origin=headers[1];
        switch(origin) {
            case "USACO":
                File result=new File(dirName+getUSACOName(headers)+".out");
                if(!result.exists())
                    return false;
                String st=readAll(result).replaceAll("\r", "");
                String expected=expectedResult(headers, dirName, testNumber).replaceAll("\r","");
                if(st.contentEquals(expected)) {
                    return true;
                }
                return false;
            default:
                File result2=new File(dirName+"output.out");
                if(!result2.exists())
                    return false;
                String st2=readAll(result2).replaceAll("\r", "");
                String expected2=expectedResult(headers, dirName, testNumber).replaceAll("\r","");
                if(st2.contentEquals(expected2)) {
                    return true;
                }
                return false;
        }
    }
}
