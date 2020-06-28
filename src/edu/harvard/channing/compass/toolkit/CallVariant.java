/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.toolkit;

import edu.harvard.channing.compass.core.Configuration;
import edu.harvard.channing.compass.core.Factory;
import edu.harvard.channing.compass.core.mut.Genome;
import edu.harvard.channing.compass.core.mut.Heap;
import edu.harvard.channing.compass.core.mut.HumanGenome;
import edu.harvard.channing.compass.core.mut.MergeVariant;
import edu.harvard.channing.compass.db.DB;
import edu.harvard.channing.compass.entity.DBTree;
import edu.harvard.channing.compass.utility.Download;
import edu.harvard.channing.compass.utility.ReadFile;
import java.io.File;
import java.util.ArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author rejia
 */
public class CallVariant implements ToolKit {

    private static final Logger LOG = LogManager.getLogger(CallVariant.class.getName());

    public String strSAM;
    public String strFa;
    public String strRefID="hg38";
//    public Heap heap;
    public String strDB;
    public String strRegion;
    public String strFileList;
    public ArrayList<Heap> altHeap;
    public boolean needMerge=false;
    public String strOut;
    public boolean isGVT=true;
    public Genome gmRef;
    public ArrayList<DBTree> dbt;
    
    
    public void callVariant(){
        altHeap=new ArrayList();
        if(strSAM!=null){
            String[] strSAMs=strSAM.split(",|;");
            for (String strFile : strSAMs) {
                Heap heap = new Heap(strFile);
                altHeap.add(heap);
            }
        }else{
            ArrayList<String> altFile=ReadFile.readFileList(strFileList);
            for(String strFile:altFile){
                Heap heap=new Heap(strFile);
                altHeap.add(heap);
            }
        }
        
        //Build Reference Genome. 
        this.gmRef=new HumanGenome();
        this.setRefGenome(this.strRefID);
        
        //Build Database.
        this.initDB(this.strDB);
        
        for (Heap heap : altHeap) {
            try {
                heap.setRefGenome(this.gmRef);
                heap.setDB(this.dbt);
                heap.makeHeap();
                heap.outputHeap();
                heap.callVariant(this.isGVT);
                heap.releaseHeap();
                LOG.info(heap.strFile + " was set.");
            } catch (Exception ex) {
                LOG.error(ex.getMessage());
                LOG.info(heap.strFile + " was failed!");
            }
        }
   
    }
    
    public int initDB(String strDB){
        this.dbt=new ArrayList();
        if (strDB == null) {
            if (strRegion == null) {
                LOG.error("There is no database and region file set.");
                return 0;
            } else {
                dbt.add(ReadFile.readGFF3(strRegion));
            }
        } else {
            String[] strItem = this.strDB.split(",|;");
            for (String strTmp : strItem) {
                DB db = Factory.getDB(strTmp, true);
                DBTree dbT = db.getForest(this.strRefID);
                this.dbt.add(dbT);
            }
        }
        return 1;
    }

    public void mergeVT(){
        ArrayList<String> altSamples=ReadFile.readFileList(strFileList);
        if(this.strOut==null){
            //?
        }
        MergeVariant mv=new MergeVariant(altSamples,this.strOut);
        mv.mergeVariant();
        
    }

    public void setRefGenome(String strID){
        this.gmRef=Factory.getGenome(strID);
        if(this.gmRef.isLoaded())   return;
        File fleRef=new File(Configuration.REF.get(strID));
        if(fleRef.exists()){
            this.gmRef.readGenome(Configuration.REF.get(strID));
        }else{
            Download dd=new Download(Configuration.REF.get(strID));
            boolean boolFlag=dd.download();
            if(boolFlag){
                this.gmRef.readGenome(Configuration.REF.get(strID));
            }else{
                LOG.info("Try to build reference genome from local fasta file");
                File fleFQ=new File(Configuration.REF.get(strID+".cps"));
                if(!fleFQ.exists()){
                    dd=new Download(Configuration.REF.get(strID+".cps"));
                    boolFlag=dd.download();
                    if(!boolFlag)   return;
                }
                this.setRefGenome(strID, Configuration.REF.get(strID+".cps"));
                this.gmRef.writeGenome(Configuration.REF.get(strID));
            }
        }

    }

    public void setRefGenome(String strID, String strFasta) {
        this.gmRef = Factory.getGenome(strID);
        this.gmRef.setStrFasta(strFasta);
        this.gmRef.buildGenome();
    }

    @Override
    public int runKit() {
        if (this.needMerge) {
            this.mergeVT();
        } else {
            this.callVariant();
        }

        return 1;
    }
    
    public static void main(String[] argv){
        Configuration config=new Configuration();
        CallVariant cv=new CallVariant();
        cv.strSAM="E:\\01Work\\miRNA\\data\\test_part.sam;E:\\01Work\\miRNA\\data\\test_part.sam;E:\\01Work\\miRNA\\data\\test_part.sam";
        cv.strFa="E:\\01Work\\miRNA\\data\\hg38.fa.gz";
        cv.strDB="miRBase";
        cv.callVariant();
        System.out.println();
    }

}
