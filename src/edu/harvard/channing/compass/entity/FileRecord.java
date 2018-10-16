/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.entity;

import edu.harvard.channing.compass.core.Configuration;
import edu.harvard.channing.compass.core.qc.Fastq;
import edu.harvard.channing.compass.core.aln.Alignment;
import edu.harvard.channing.compass.core.mic.Microbe;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class is used to record the basic information of input and output file. 
 * @author Jiang Li
 * @version 1.0 
 * @since 2017-10-23
 */
public class FileRecord {
    private static final Logger LOG = LogManager.getLogger(FileRecord.class.getName());
    
    public String input;
    public String input_prefix;
    public String output_dir;
    public String output_prefix;
    public String strRef;
    public String strToAln="last";
    public boolean isQC=false;
    public ArrayList<String> output_qc;
    public boolean isAln=false;
    public HashMap<String,String> output_aln;
    public HashMap<String,String> output_unmap;
    public boolean isAnn=false;
    public HashMap<String,ArrayList<String>> output_ann;
    public boolean isMic=false;
    public HashMap<String,HashMap<String,ArrayList<String>>> output_mic;
    public String strSuffix=".sam";
    
    public String[] strTool;
    public String[] strDB;
    

//    FileRecord(){
//       this.output_qc=new ArrayList<String>();
//       this.output_aln=new ArrayList<String>();
//       this.output_aln=new ArrayList<String>();
//       this.output_mic=new ArrayList<String>();
//   }

    public FileRecord(String strInput) {              
        this.input=strInput;  
        File fleInput=new File(strInput);
        this.input_prefix=fleInput.getName().split("\\.")[0];
    }

    public void setOutput(String output) {
        File fle = new File(output);
        if (fle.isDirectory()) {
            try {
                this.output_dir = fle.getCanonicalPath() + "/" + this.input_prefix + "/";
                this.output_prefix = this.input_prefix;
            } catch (IOException ex) {
                LOG.error(ex.getMessage());
            }
        } else {
            if (fle.getParentFile() == null) {
                this.output_dir = Configuration.OUTDIR + "/" + this.input_prefix + "/";
                this.output_prefix = fle.getName();
            } else {
                this.output_dir = fle.getParent() + "/";
                this.output_prefix = fle.getName();
            }
        }
        
        File fleOut = new File(this.output_dir);
        if (!fleOut.exists()) {
            fleOut.mkdirs();
        }
    }

    public void setOutput() {
        this.output_dir = Configuration.OUTDIR + "/" + this.input_prefix + "/";
        this.output_prefix = this.input_prefix;

        File fleOut = new File(this.output_dir);
        if (!fleOut.exists()) {
            fleOut.mkdirs();
        }
    }

    public void setQC(Fastq fq) {
//        String[] strRange=fq.qc.strRmLengthRead.split(",|;");
//        int[] intCount=new int[1+strRange.length+1];
//        for(int i=0;i<intCount.length;i++) intCount[i]=0;
        this.output_qc=new ArrayList<String>();
        this.isQC=true;
        
        String strFile="";
        for (int i = 0; i < fq.intCount.length; i++) {
            if (i == 0) {
                strFile =this.output_dir+this.output_prefix + Configuration.QC_OUT[0];
            } else if (i == 1) {
                strFile = this.output_dir+this.output_prefix + "_0to" + fq.strRange[i-1] + Configuration.QC_OUT[1];
            } else if (i == fq.strRange.length+1) {
                strFile = this.output_dir+this.output_prefix + "_" + fq.strRange[i - 2] + "to" + fq.intRawReadLength + Configuration.QC_OUT[1];
            } else {
                strFile = this.output_dir+this.output_prefix + "_" + fq.strRange[i - 2] + "to" + fq.strRange[i-1] + Configuration.QC_OUT[1];
            }
//                strRecord[i] = strFile;
            this.output_qc.add(strFile);
//            this.output_qc.put(strFile, this.input);
//                this.bosOutputs[i] = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(new File(strFile)))));
        }
    }

    public void setAln(Alignment aln) {
        this.output_aln=new HashMap<String,String>();
        this.isAln=true;
        this.strRef=aln.strRefGenome;
        if(isQC){
            //There should be a parameter to set which read group is used. To be finished...
            if("last".equals(aln.strIndex)){
                String str=this.output_qc.get(this.output_qc.size()-1);
                this.output_aln.put(str,str.split("\\.")[0]+this.strSuffix);
            }else{
                String[] strIndex=aln.strIndex.split(",|;");
                for(int i=0;i<strIndex.length;i++){
                    String str=this.output_qc.get(Integer.valueOf(strIndex[i]));
                    this.output_aln.put(str, str.split("\\.")[0]+this.strSuffix);
                }
//                this.output_unmap.put(str, str.split("\\.")[0]+Configuration.ANN_OUT[0]);
            }
        }else{
            this.output_aln.put(this.input,this.output_dir+this.output_prefix+this.strSuffix);
//            this.output_unmap.put(this.input.split("\\.")[0]+".sam", this.input.split("\\.")[0]+Configuration.ANN_OUT[0]);
        }
    }


    public void setAnn(String strAnn) {
        this.output_ann=new HashMap<String,ArrayList<String>>();
        this.isAnn=true;
        if(this.output_unmap==null) this.output_unmap=new HashMap<String,String>();
        
        if(isAln){
            for(String str :this.output_aln.values()){
                this.output_ann.put(str, new ArrayList<String>());
                String[] strItem=strAnn.split(",");
                for(String item:strItem){
                    this.output_ann.get(str).add(str.split("\\.")[0]+Configuration.ANN_CATEGORY.get(item)+".txt");
                }
                this.output_unmap.put(str, str.split("\\.")[0]+Configuration.ANN_OUT[0]);
            }
        }else{
            //Under this situation, this.input should be the sam file. And -out should give the whole path with prefix. 
            this.output_ann.put(this.input, new ArrayList<String>());
            String[] strItem = strAnn.split(",");
            for (String item : strItem) {
                this.output_ann.get(this.input).add(this.input.split("\\.")[0]+Configuration.ANN_CATEGORY.get(item)+".txt");
            }
            this.output_unmap.put(this.input,this.input.split("\\.")[0]+Configuration.ANN_OUT[0]);
        }
    }
    
    
    public String getAlnInput() {
        if(isQC)    return this.output_qc.get(this.output_qc.size()-1);
        else    return this.input;
    }    

    public void setMic(Microbe mic) {
        this.isMic=true;
        this.output_mic=new HashMap<String,HashMap<String,ArrayList<String>>>();
        
//        HashMap<String,ArrayList<String>> hmpTemp=new HashMap();
//        hmpTemp.put("metaphlan", new ArrayList<String>());
//        hmpTemp.put("blast", new ArrayList<String>());
        
        this.strTool=mic.strTool.split(",|;");
        this.strDB=mic.strBlastDB.split(",|;");
        
        if(isAnn){
            Iterator<String> itr=this.output_unmap.values().iterator();
            while(itr.hasNext()){
                String strSAM=itr.next();
                
                HashMap<String,ArrayList<String>> hmpTemp=new HashMap();                
                for(int i=0;i<strTool.length;i++){
                    if("metaphlan".equals(strTool[i].toLowerCase())){
                        hmpTemp.put("metaphlan", new ArrayList<String>());
                        hmpTemp.get("metaphlan").add(strSAM.split("\\.")[0]+Configuration.MIC_OUT[0]);
                    }
                    if("blast".equals(strTool[i].toLowerCase())){
                        hmpTemp.put("blast", new ArrayList<String>());
                        for(int j=0;j<strDB.length;j++){
                            hmpTemp.get("blast").add(strSAM.split("\\.")[0]+"_blast_"+strDB[j]+"_tree.txt");                            
                        }
                    }
                }
                this.output_mic.put(strSAM, hmpTemp);       
                
            }           
        }else if(isAln){
            Iterator<String> itr=this.output_aln.values().iterator();
            while(itr.hasNext()){
                String strSAM=itr.next();

                HashMap<String,ArrayList<String>> hmpTemp=new HashMap();                
                for(int i=0;i<strTool.length;i++){
                    if("metaphlan".equals(strTool[i].toLowerCase())){
                        hmpTemp.put("metaphlan", new ArrayList<String>());
                        hmpTemp.get("metaphlan").add(strSAM.split("\\.")[0]+Configuration.ANN_OUT[0].split("\\.")[0]+Configuration.MIC_OUT[0]);
                    }
                    if("blast".equals(strTool[i].toLowerCase())){
                        hmpTemp.put("blast", new ArrayList<String>());
                        for(int j=0;j<strDB.length;j++){
                            hmpTemp.get("blast").add(strSAM.split("\\.")[0]+Configuration.ANN_OUT[0].split("\\.")[0]+"_blast_"+strDB[j]+"_tree.txt");                            
                        }
                    }
                }
                this.output_mic.put(strSAM.split("\\.")[0]+Configuration.ANN_OUT[0], hmpTemp);  
//                this.output_mic.put(strSAM.split("\\.")[0]+Configuration.ANN_OUT[0],strSAM.split("\\.")[0]+Configuration.ANN_OUT[0].split("\\.")[0]+Configuration.MIC_OUT[0]);
            }              
        }else if(isQC){
            if("last".equals(mic.strIndex)){
                String strFQ=this.output_qc.get(this.output_qc.size()-1);
                
                HashMap<String,ArrayList<String>> hmpTemp=new HashMap();                
                for(int i=0;i<strTool.length;i++){
                    if("metaphlan".equals(strTool[i].toLowerCase())){
                        hmpTemp.put("metaphlan", new ArrayList<String>());
                        hmpTemp.get("metaphlan").add(strFQ.split("\\.")[0]+Configuration.MIC_OUT[0]);
                    }
                    if("blast".equals(strTool[i].toLowerCase())){
                        hmpTemp.put("blast", new ArrayList<String>());
                        for(int j=0;j<strDB.length;j++){
                            hmpTemp.get("blast").add(strFQ.split("\\.")[0]+"_blast_"+strDB[j]+"_tree.txt");                            
                        }
                    }
                }
                this.output_mic.put(strFQ, hmpTemp);                  
                
//                this.output_mic.put(str,str.split("\\.")[0]+Configuration.MIC_OUT[0]);
            }else{
                String[] strIndex = mic.strIndex.split(",|;");
                for (int k = 0; k < strIndex.length; k++) {
                    String strFQ = this.output_qc.get(Integer.valueOf(strIndex[k]));

                    HashMap<String, ArrayList<String>> hmpTemp = new HashMap();
                    for (int i = 0; i < strTool.length; i++) {
                        if ("metaphlan".equals(strTool[i].toLowerCase())) {
                            hmpTemp.put("metaphlan", new ArrayList<String>());
                            hmpTemp.get("metaphlan").add(strFQ.split("\\.")[0] + Configuration.MIC_OUT[0]);
                        }
                        if ("blast".equals(strTool[i].toLowerCase())) {
                            hmpTemp.put("blast", new ArrayList<String>());
                            for (int j = 0; j < strDB.length; j++) {
                                hmpTemp.get("blast").add(strFQ.split("\\.")[0] + "_blast_" + strDB[j] + "_tree.txt");
                            }
                        }
                    }
                    this.output_mic.put(strFQ, hmpTemp);
                }
            }
        }else{
            
            HashMap<String, ArrayList<String>> hmpTemp = new HashMap();
            for (int i = 0; i < strTool.length; i++) {
                if ("metaphlan".equals(strTool[i].toLowerCase())) {
                    hmpTemp.put("metaphlan", new ArrayList<String>());
                    hmpTemp.get("metaphlan").add(this.output_dir + this.output_prefix + Configuration.MIC_OUT[0]);
                }
                if ("blast".equals(strTool[i].toLowerCase())) {
                    hmpTemp.put("blast", new ArrayList<String>());
                    for (int j = 0; j < strDB.length; j++) {
                        hmpTemp.get("blast").add(this.input.split("\\.")[0] + "_blast_" + strDB[j] + "_tree.txt");
                    }
                }
            }
            this.output_mic.put(this.input, hmpTemp);         
            
//            this.output_unmap.put(this.input, this.input.split("\\.")[0]+Configuration.ANN_OUT[0]);
//            this.output_mic.put(this.input, this.output_dir+this.output_prefix+Configuration.MIC_OUT[0]);
        }
    }

    public ArrayList<String> getProfileAnn(String strRNA, boolean boolAnn) {
        ArrayList<String> altProfile = new ArrayList();
        if (this.isAnn) {
            for (String strKey : this.output_ann.keySet()) {
                for (String strFile : this.output_ann.get(strKey)) {
                    if (strFile.endsWith(Configuration.ANN_CATEGORY.get(strRNA) + ".txt")) {
                        altProfile.add(strFile);
                    }
                }
            }
        } else if (boolAnn) {
            File fleOutDir=new File(this.output_dir);
            File[] fleTargets = fleOutDir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File file, String string) {
                    return string.endsWith(Configuration.ANN_CATEGORY.get(strRNA) + ".txt");
                }
            });
//            for(File fle:fleOutDir.listFiles()){
//                altProfile.add(fle.getAbsolutePath());
//            }
            if(fleTargets.length!=0)    altProfile.add(fleTargets[0].getAbsolutePath());
        } else {
            altProfile.add(this.input);
        }
        return altProfile;
    }
    
    public ArrayList<String> getProfileMic(String strTool, String strDB,boolean boolMic){
        ArrayList<String> altProfile = new ArrayList();
        if (this.isMic) {
            for(String strKey:this.output_mic.keySet()){
                if("metaphlan".equals(strTool.toLowerCase())){
                    altProfile.add(this.output_mic.get(strKey).get("metaphlan").get(0));
                }else{
                    ArrayList<String> altBlast=this.output_mic.get(strKey).get("blast");
                    String strSuffix=strTool.toLowerCase()+"_"+strDB.toLowerCase() + "_tree.txt";
                    for(String strBlast:altBlast){
                        if(strBlast.endsWith(strSuffix))    altProfile.add(strBlast);
                    }
                }
            }            
        } else if (boolMic) {
            File fleOutDir=new File(this.output_dir);
            String strSuffix;
            if("metaphlan".equals(strTool.toLowerCase())){
                strSuffix="_metaphlan.txt";
            }else{
                strSuffix=strTool.toLowerCase()+"_"+strDB.toLowerCase() + "_tree.txt";
            }
            
            File[] fleTargets = fleOutDir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File file, String string) {
                    return string.endsWith(strTool.toLowerCase()+"_"+strDB.toLowerCase() + "_tree.txt");
                }
            });
            for(File fle:fleTargets){
                altProfile.add(fle.getAbsolutePath());
            }
        } else {
            altProfile.add(this.input);           
        }  
        return altProfile;
    }
}
