
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class USACOImporter {
    public static void main(String[] args) throws Throwable {
        //Stuff to enter for the importer to work
        String URLofProblemPage="http://usaco.org/index.php?page=open13problems";
        String prefix="USACO.US Open 2013.";
        String rootDir="C:\\Judge\\Testing Data\\";
        int goldProblems = 3;
        int silverProblems = 3;
        int bronzeProblems = 4;
        
        //Functional code
        URL problemPage=new URL(URLofProblemPage);
        int[] probLayout={goldProblems,silverProblems,bronzeProblems};
        String st=readAll(problemPage);
        st=st.substring(st.indexOf("<div class=\"panel\">"));
        String[] divs=new String[]{"G","S","B"};
        int divOn=0;
        int probOn=0;
        while(st.contains("href='")) {
            probOn++;
            if(probOn>probLayout[divOn]) {
                probOn=1;
                divOn++;
            }
            System.out.println(divOn+": "+probOn);
            //Find the name:
            String name=st.substring(st.indexOf("<b>")+3);
            st=name;
            name=name.substring(0,name.indexOf("</b>"));
            if(name.contains("("))
                name=name.substring(0,name.indexOf("("));
            if(name.contains("/"))
                name=name.substring(0,name.indexOf("/"));
            name=name.trim();
            //Find problem page:
            String defpage=st.substring(st.indexOf("href='")+6);
            st=defpage;
            defpage=defpage.substring(0,defpage.indexOf("'"));
            //Find the test page:
            String testpage=st.substring(st.indexOf("href='")+6);
            st=testpage;
            testpage=testpage.substring(0,testpage.indexOf("'"));
            //Find the solution page
            String solpage=st.substring(st.indexOf("href='")+6);
            st=solpage;
            solpage=solpage.substring(0,solpage.indexOf("'"));
            
            //Processing time...
            //Add namefile
            File directory=new File(rootDir+prefix+divs[divOn]+probOn+" - "+name);
            directory.mkdir();
            File nameFile=new File(rootDir+prefix+divs[divOn]+probOn+" - "+name+"\\name.txt");
            FileOutputStream fos=new FileOutputStream(nameFile);
            String tName=readAll(defpage);
            tName=tName.substring(tName.indexOf("PROBLEM NAME: ")+14);
            tName=tName.substring(0,tName.indexOf("INPUT"));
            tName=tName.trim();
            fos.write(tName.getBytes());
            fos.close();
            //Download test cases
            ZipInputStream zis=new ZipInputStream(new URL("http://usaco.org/"+testpage).openStream());
            ZipEntry ze;
            while((ze=zis.getNextEntry())!=null) {
                FileOutputStream zos=new FileOutputStream(rootDir+prefix+divs[divOn]+probOn+" - "+name+"\\"+ze.getName());
                byte[] buffer=new byte[4096];
                int len;
                while((len=zis.read(buffer))!=-1) {
                    zos.write(buffer,0,len);
                }
                zos.close();
            }
            zis.close();
            renamePath(rootDir+prefix+divs[divOn]+probOn+" - "+name+"\\");
        }
    }
    public static void renamePath(String path){
        File dir=new File(path);
        dir.mkdir();
        for(File f:dir.listFiles()) {
            if(f.getName().startsWith("I"))
                f.renameTo(new File(path+f.getName().split("\\.")[1]+".in"));
            if(f.getName().startsWith("O"))
                f.renameTo(new File(path+f.getName().split("\\.")[1]+".out"));
        }
    }
    public static String readAll(URL url) throws Throwable{
        InputStream is=url.openStream();
        int len;
        byte[] buffer=new byte[4096];
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        while((len=is.read(buffer))!=-1) {
            baos.write(buffer,0,len);
        }
        String st=baos.toString();
        return st;
    }
    public static String readAll(String st) throws Throwable{
        return readAll(new URL("http://usaco.org/"+st));
    }
}
