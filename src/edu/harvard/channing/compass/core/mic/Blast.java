/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.core.mic;

import edu.harvard.channing.compass.core.Configuration;
import edu.harvard.channing.compass.utility.Download;
import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class is used to handle the microbes identification by Blast method.
 * @author Jiang Li
 * @version 1.0 
 * @since 2018-02-20
 */
public class Blast extends MicTool{
    private static final Logger LOG = LogManager.getLogger(Blast.class.getName());
    
//    public String strIn;
//    public String strOut;
    public String strDB;
//    public String strFmt=" -outfmt \"6 qacc sacc evalue sstrand bitscore length pident nident qstart qend sstart send \"";
    public String strFmt=" -outfmt 6";
    public int intThread=4;
    public double dblEValue=0.001;
    

    public Blast(String strIn, String strOut) {
        this.strInput=strIn;
        this.strOutput=strOut;
    }

    public Blast(String strDB) {
        this.strDB=strDB;
    }

    @Override
    public void setDB(String strDB) {
        this.strDB=strDB;
    }

    @Override
    public String getDB(){
        return this.strDB;
    }
    
    public void makeDB() {
        String[] strDB=this.strDB.split(",|;");
        for(String db:strDB){
            String strFA=Configuration.NT.get(db);
            String strCmd=Configuration.PLUG_BUILT_IN.get("makeblastdb");
            String strCmdLine=strCmd+" -in "+strFA+" -dbtype nucl -title nt_"+db+" -parse_seqids";
            
            int intFlag=this.detectDBIndex(strFA);
            if(intFlag==0){
                String strResult=this.runCmd(strCmdLine);
                if(strResult!=null) System.out.println(strResult);
            }
                       
        }
    }

    public String blastDB(){
        String strDB=Configuration.NT.get(this.strDB);       
        String strCmd=Configuration.PLUG_BUILT_IN.get("blastn");
        String strCmdLine=strCmd+" -db "+strDB+" -query "+this.strFA+" -out "+this.strOutput+" -evalue "+this.dblEValue+this.strFmt+" -num_threads "+this.intThread;
//        String strCmdLine=strCmd+" -db "+strDB+" -query "+this.strFA+" -out "+this.strOutput+" -evalue "+this.dblEValue+" -num_threads "+this.intThread;
        String strResult=this.runCmd(strCmdLine);
        if(strResult!=null) LOG.info(strResult);
        return strResult;
    }
    
    public String runCmd(String strCmdLine){
        try {
            System.out.println("To run: " + strCmdLine+"\n");
//            String params=URLEncoder.encode(strCmdLine, "utf-8");
            Process ps = Runtime.getRuntime().exec(strCmdLine);
            if (ps != null) {
                BufferedReader br = new BufferedReader(new InputStreamReader(ps.getInputStream()));
                BufferedReader err = new BufferedReader(new InputStreamReader(ps.getErrorStream()));
                int status = ps.waitFor();
                String line;
                while ((line = br.readLine()) != null) {
                    System.out.println(line);
                }
                while ((line = err.readLine()) != null) {
                    System.out.println(line);
                }
                if (status != 0) {
                    String tmp="Fail to call: " + strCmdLine+"\n";                  
                    return tmp;
                } else {
                    String tmp="Succeed to call: " + strCmdLine+"\n";
                    this.boolBlast=true;
                    return tmp;
                }
            } else {
                return "Error: No process assigned to the command: " + strCmdLine+"\n";
            }
        } catch (IOException ex) {
            LOG.error(ex.getMessage());
            
        } catch (InterruptedException ex) {
           LOG.error(ex.getMessage());
        }
        return null;
    }          



    public int detectDBIndex(String strDB) {
        File fleDB=new File(strDB);
        if(!fleDB.exists()){
            LOG.info("The database "+strDB+" doesn't exist! Please download it!");
            return -1;
        }
        File fleDir=fleDB.getParentFile();
        File[] fleTargets=fleDir.listFiles(new FilenameFilter(){
            @Override
            public boolean accept(File file, String string) {
                return string.endsWith(".nhr");
            }
        });
        
        for(int i=0;i<fleTargets.length;i++){
            if(fleTargets[i].getName().contains(fleDB.getName())) return 1;
        }
        
        return 0;
    }
 
    
//    public void parseBlast(){
//        
//    }
    
    @Override
    public Object call() throws Exception {
        this.prepareFA();
        String strFlat = this.blastDB();
//        if(strFlat!=null)   this.parseBlast();
        return strFlat;
    }
    
    public static void checkBlast(){
        File fleTool=new File(Configuration.PLUG_BUILT_IN.get("blastn"));
        if(!fleTool.exists()){
            String strLoc=Configuration.PLUG+"/blast/"+Configuration.BLAST;
            String strURL=Configuration.DOWNLOAD.get("blast");
            Download dd=new Download(strURL,strLoc,true);
            boolean boolFlag=dd.download();
            if(!boolFlag){
                LOG.info("We will try it from the NCBI website.");
                strURL=Configuration.DOWNLOAD.get("blast2");
                dd=new Download(strURL,strLoc,true);
                boolFlag=dd.download();            
            }
            if (!boolFlag) {
                LOG.error("Faile to download the file " + strURL);
                LOG.info("Please download BLAST by yourself.");
            }
        }
    }
    
    public static void main(String[] argv){
        Configuration config=new Configuration();
        Blast bt=new Blast("archaea");
        bt.checkBlast();
//        bt.makeDB();
        
    }    
}
