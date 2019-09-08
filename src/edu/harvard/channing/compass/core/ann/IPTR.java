/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.core.ann;

import edu.harvard.channing.compass.entity.DBLeaf;
import edu.harvard.channing.compass.entity.DBTree;
import htsjdk.samtools.SAMFileWriter;
import htsjdk.samtools.SAMFileWriterFactory;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SamReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This abstract class is used to define the basic action of interpreter.
 * @author Jiang Li
 * @version 1.0
 * @since 2017-09-24
 */
public abstract class IPTR {
    private static final Logger LOG = LogManager.getLogger(IPTR.class.getName());
    
    public int DB_N;
    public String strCategory;
//    public Alignment align;
    public String refGenome;
    public boolean needLiftOver=false;
    public String strLiftOver;
    public ArrayList<DBTree> dbtForest;
    public float fltOverlapRateRead=1;
    public float fltOverlapRateRegion=1;
    public DBTree dbtMerge;     
    public String strOutput;
    public String strObj;
    public boolean isInCluster=false;
    public int intThreshold=1;
    public boolean boolCR=false;
    public boolean needBAMOutput=false;
    public SAMFileWriter sfwSpecialRNA;
    public boolean needDetail=false;
    public boolean useUMI=false;
    
    IPTR(String strFile){
        this.strOutput=strFile;
    }
 
    public abstract boolean buildForest();
    
    public String getCategoryName(){
        return this.strCategory;
    }
    
    public boolean recordLeaf(SAMRecord sr) {
        boolean boolFlag=false;
        for (int i=0;i<this.dbtForest.size();i++) {
//            System.out.println(this.dbtForest.get(i).strDB);
            boolFlag|=this.dbtForest.get(i).findLeaf(sr,this.fltOverlapRateRead,true);
        }
        if(boolFlag && needBAMOutput){
            this.sfwSpecialRNA.addAlignment(sr);
        }
        return boolFlag;
    }
    
    public boolean writeReport(){
        try {
            BufferedWriter bw = null;
            bw = new BufferedWriter(new FileWriter(this.strOutput));

            //Merge annotation results to one Hashmap.
            boolean boolFlag = this.mergeTrees();
            if (!boolFlag) {
                LOG.info("Annotation Error: Fail to write report of " + this.strCategory + "!");
                return boolFlag;
            }

            //Start to build txt file.
//            bw.write("DB\tName\tID\tContig\tStart\tEnd\tStrand\tCount");//title="DB\tName\tID\tContig\tStart\tEnd\tStrand\tCount";
            bw.write("DB\tName\tID\tCount");
            bw.newLine();

            for(String key:this.dbtMerge.hmpMerge.keySet()){
                DBLeaf dif=this.dbtMerge.hmpMerge.get(key);
                String strRecord=dif.getRecord(intThreshold);
                if(strRecord==null) continue;
                bw.write(strRecord);
                bw.newLine();
            }                  
            bw.close();
            LOG.info(this.strOutput+" was saved.");
            
            if (this.needDetail) {
                bw = new BufferedWriter(new FileWriter(this.strOutput+".detail"));
                for (String key : this.dbtMerge.hmpMerge.keySet()) {
                    DBLeaf dif = this.dbtMerge.hmpMerge.get(key);
                    String strRecord = dif.getDetail(intThreshold);
                    if (strRecord == null) {
                        continue;
                    }
                    bw.write(strRecord);
//                    bw.newLine();
                }
            }
            bw.close();
            
            return boolFlag;
        } catch (IOException ex) {
            LOG.error(ex.getMessage());
            return false;
        }           
    }
    
    public boolean mergeTrees(){
        dbtMerge=new DBTree();
        dbtMerge.initTree("Merge");
        for(DBTree dis: this.dbtForest){
//            System.out.println("--------------------"+dis.strDB+"------------------------");
            for(String key:dis.hmpStart.keySet()){
                for(int pos: dis.hmpStart.get(key)){
                    for(DBLeaf dif : dis.hmpDB.get(key).get(pos)){
                        //Have annotation.
                        if(dif.hit>0){
//                            if("hsa-miR-223-3p".equals(dif.name)){
//                                System.out.println(dif.getRecord(intThreshold));
//                            }
                            dif.db=dis.strDB;
//                            dbtMerge.graftLeafByLocation(dif,this.fltOverlapRateRegion);
                            dbtMerge.graftLeafByIdentifier(dif,true,this.useUMI);
                        }
                    }                 
                }
//                System.out.println(dis.strDB+"--->"+key);
            }
//            System.out.println("--------------------"+dis.strDB+"------------------------");
        }        
        return true;        
    }
    
//    public void setOverlapRead(float overlap){
//        this.fltOverlapRateRead=overlap;
//    }

    public StringBuilder getReport() {
        StringBuilder sb=new StringBuilder();
        sb.append("\n{").append(this.strCategory).append("}:\n");
        for(int i=0;i<this.dbtForest.size();i++){
            sb.append(dbtForest.get(i).getReport());  
        }
        if(this.dbtForest.size()>1) this.dbtMerge.getReportMerge();
        return sb;
    }
 
    public void setBAMOutput(SamReader sr){
        this.needBAMOutput=true;
        this.sfwSpecialRNA=new SAMFileWriterFactory().makeBAMWriter(sr.getFileHeader(), false, new File(this.strOutput.replace(".txt",".bam")));
    }
    
    public void closeBAMOutput(){
        if(this.sfwSpecialRNA!=null){
            this.sfwSpecialRNA.close();
        }       
    }
}
