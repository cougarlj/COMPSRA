/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.core.aln;

import edu.harvard.channing.compass.core.Configuration;
import edu.harvard.channing.compass.core.Factory;
import edu.harvard.channing.compass.entity.FileRecord;
import edu.harvard.channing.compass.utility.Download;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * This abstract class is used to define the aligner for COMPASS pipeline.
 * @author Jiang Li
 * @version 1.0 
 * @since 2017-08-30
 */
public abstract class Aligner implements Callable {
    private static final Logger LOG = LogManager.getLogger(Aligner.class.getClass());
    FileRecord frd;
//    Alignment aln;
    String strInput;
    String strOutput;
    String command;
    String strParam;
    String strRef;
    StringBuilder sb;
    
    public Aligner(String in,String out){
        this.strInput=in;
        this.strOutput=out;
        this.sb=new StringBuilder();
    }
    
    public abstract void setCommand();
    
    @Override
    public Object call() throws Exception {
//        System.out.println("\n----Alignment-Report-For-("+this.strInput+")----\n");
        
        LOG.info("To run: " + this.command);
        Process ps = Runtime.getRuntime().exec(this.command);
        if (ps != null) {
            BufferedReader br = new BufferedReader(new InputStreamReader(ps.getInputStream()));
            BufferedReader err = new BufferedReader(new InputStreamReader(ps.getErrorStream()));
            int status = ps.waitFor();
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                sb.append(line);
            }
            while ((line = err.readLine()) != null) {
                System.out.println(line);
                sb.append(line);
            }
            
            this.reportAln();
            
            if (status != 0) {
                String tmp="Fail to call: " + this.command;
                return tmp;
            } else {
                String tmp="Succeed to call: " + this.command;
                return tmp;
            }
        } else {
            this.reportAln();
            return "Error: No process assigned to the command: " + this.command;
        }
    }   

    public void setParam(String strParam) {
//        this.strParam=strParam;
        //e.g:[--runMode,alignReads;--alignIntronMin,21;--outSAMattributes,Standard]
        if(strParam==null)  return;
        StringBuilder sbParam=new StringBuilder();
        if(!strParam.startsWith("[") || !strParam.endsWith("]")){
            strParam=null;
            return;
        }else{
            String strP=strParam.substring(1, strParam.length()-1);
            String[] strItems=strP.split(";");
            for(String strItem:strItems){
                sbParam.append(strItem.replace(',', ' '));
                sbParam.append(' ');
            }
        }
        this.strParam=sbParam.toString();
    }

    public void setRef(String strRefGenome) {
        this.strRef=strRefGenome;
    }
    
    public abstract void setRefDB(String strRefDB);
    
    public void reportAln(){       
        try {
            BufferedWriter bw = Factory.getWriter(this.frd.output_dir + this.frd.output_prefix + "_AlnReport.txt");
            bw.write(sb.toString());
            bw.close();
        } catch (IOException ex) {
            LOG.error(ex.getMessage());
        }               
    }
    
    public void checkAligner(String strTool){
        File fleTool=new File(Configuration.PLUG_BUILT_IN.get(strTool));
        if(!fleTool.exists()){
            String strLoc=Configuration.PLUG+"/"+strTool+"/"+Configuration.STAR;
            String strURL=Configuration.DOWNLOAD.get(strTool);
            Download dd=new Download(strURL,strLoc,true);
            boolean boolFlag=dd.download();
            if(!boolFlag){
                LOG.error("Faile to download the file " +strURL);
                LOG.info("Please download STAR by yourself.");
            }
        }
        
    }
}
