/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.core.aln;

import edu.harvard.channing.compass.core.Factory;
import edu.harvard.channing.compass.entity.CommonParameter;
import edu.harvard.channing.compass.entity.FileRecord;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class is used to perform the alignment function for COMPASS pipeline.
 * @author Jiang Li
 * @version 1.0 
 * @since 2017-08-30
 */
public class Alignment {
    private static final Logger LOG = LogManager.getLogger(Alignment.class.getName());
    
//    public QualityControl qc;
    public CommonParameter comParam;
    public boolean isOpen=false;
    public String strAlignTool="star";
    public String strIndex="last";
    public String strRefGenome="hg38";
    public String strParam;
    public boolean needIndex=false;
    
//    public boolean boolEndogenous=false;
////    public String strEndogenous="1,2,3,4,5,6";
//    public boolean boolExogenous=false;
//    public String strExogenous=null;
//    public String[] strOutput=null;
//    public ArrayList<String> strInput=null;
//    public String strIndex=null;

    public Alignment(CommonParameter comParam) {
        this.comParam = comParam;
    }

    public int manage() {
        if (!this.isOpen) {
            return -1;
        }
        this.showModule();
//        this.qc=qc;
        String message;
//        ExecutorService exe = Executors.newCachedThreadPool();
        ExecutorService exe=Executors.newFixedThreadPool(this.comParam.intThread);
        ArrayList<Future<String>> lstResult = new ArrayList<Future<String>>();

        for (int i = 0; i < this.comParam.altInput.size(); i++) {
            FileRecord frd=this.comParam.altInput.get(i);
            if("star".equals(this.strAlignTool.toLowerCase()))   frd.strSuffix="_STAR_Aligned.out.bam";
            frd.setAln(this);
            
            Iterator<String> itr=frd.output_aln.keySet().iterator();
            while (itr.hasNext()) {
                String strFQ = itr.next();
                Aligner alnr = Factory.getAligner(this.strAlignTool, strFQ, frd.output_aln.get(strFQ));
                if(this.comParam.boolCheckResource){
                    alnr.checkAligner(this.strAlignTool.toLowerCase());
                }   
                alnr.setPermission();
                if(this.needIndex){
                    alnr.buildGenomeIndex(this.strRefGenome);
                }  
                
                alnr.frd = frd;
                alnr.setParam(this.strParam);
                alnr.setRef(this.strRefGenome);
                alnr.setCommand();

                Future<String> future = exe.submit(alnr);
                lstResult.add(future);
            }
        }

        for (Future<String> ft : lstResult) {
            try {
                while (!ft.isDone());
                message = ft.get();
                System.out.println(message);
            } catch (InterruptedException ex) {
                LOG.info(ex.getMessage());
                return 0;
            } catch (ExecutionException ex) {
                LOG.info(ex.getMessage());
                return 0;
            } finally {
                exe.shutdown();             
            }
        }     
                   
        return 1;
    }

    public boolean setOutput(){
        // to be set.
        return false;
    }
    
    public void showModule(){
        StringBuilder sb=new StringBuilder();
        sb.append("\n+++++++++++++++++++++++++++++++\n");
        sb.append("+    Module  --->  Alignment  +");
        sb.append("\n+++++++++++++++++++++++++++++++\n");
        System.out.println(sb.toString());
    }
    
}
