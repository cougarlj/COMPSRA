/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.core.ann;

import edu.harvard.channing.compass.core.Factory;
import edu.harvard.channing.compass.entity.CommonParameter;
import edu.harvard.channing.compass.entity.FileRecord;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class is used to perform the annotation function for COMPASS pipeline. 
 * @author Jiang Li
 * @version 1.0 
 * @since 2017-08-31
 */
public class Annotation {
    private static final Logger LOG = LogManager.getLogger(Annotation.class.getName());
    
    public CommonParameter comParam;
//    public Alignment align;
    
    public boolean isOpen=false;
    public String strAnn="1,2,3,4,5,6";
    public boolean boolRmSamMap=false;
    public boolean isInCluster=false; //for piRNA.
    public float fltOverlap=(float)1;
    public int intTd=1;
    public boolean needBAMOutput=false;
    public boolean boolShowUnAnn=false;
    public boolean useUMI=false;
    public boolean needDetail=false;

    public Annotation(CommonParameter comParam) {
        this.comParam = comParam;
    }

    public int manage() {
        if (!this.isOpen) {
            return -1;
        }
        this.showModule();
        
        String message;
//        ExecutorService exe=Executors.newCachedThreadPool();
        ExecutorService exe=Executors.newFixedThreadPool(this.comParam.intThread);
        ArrayList<Future<String>> lstResult=new ArrayList<Future<String>>();
        
        for (FileRecord frd : this.comParam.altInput) {
            frd.setAnn(strAnn);
            Annotator ann = Factory.getAnnotator("sam", strAnn);
            ann.setInput(frd);      
            ann.isInCluster=this.isInCluster;
            ann.fltOverlap=this.fltOverlap;
            ann.intTD=this.intTd;
            ann.boolRmSamMap=this.boolRmSamMap;
            ann.boolCR=this.comParam.boolCheckResource;
            ann.needBAMOutput=this.needBAMOutput;
            ann.boolShowUnAnn=this.boolShowUnAnn;
            ann.useUMI=this.useUMI;
            ann.needDetail=this.needDetail;
            
            Future<String> future = exe.submit(ann);
            lstResult.add(future);
        }
        
        for(Future<String> ft:lstResult){
       
            try {
                while (!ft.isDone());
                message = ft.get();
//                System.out.println(message);
            } catch (InterruptedException ex) {
                LOG.error(ex.getMessage());
                return 0;
            } catch (ExecutionException ex) {
                LOG.error(ex.getMessage());
                return 0;
            } finally {
//                System.out.println("\n--------------------Annotation-Report-End--------------------\n");
                exe.shutdown();
            }
        }
        return 1;
   
    }


    //For test class. 
    public static void main(String[] argv){
//        String liftover="E:\\01Work\\miRNA\\project\\CircuRNA\\database\\chain\\hg19ToHg38.over.chain.gz";
//        File fleLO=new File(liftover);    
//        LiftOver lorTrans=new LiftOver(fleLO);
//        Interval itvRegion=new Interval("chr20",18309716,18309740, "-".equals("+"),"piR-hsa-32187");
//        Interval itvNew=lorTrans.liftOver(itvRegion);        
//        System.out.println(itvNew.toString());
        
        
//        Configuration config=new Configuration();
//        CommonParameter com=null;
//        Alignment align=new Alignment(com);
//        align.strOutput=new String[]{"E:\\01Work\\miRNA\\project\\CircuRNA\\output\\HBRNA_AGTCAA_L001_R1_17to50_FitRead.part.sam"};
//        Annotation ann=new Annotation(com);
//        boolean boolFlag=ann.manage(align);
//        System.out.println(boolFlag);
        
//        String liftover="E:\\01Work\\miRNA\\project\\CircuRNA\\database\\chain\\hg19ToHg38.over.chain.gz";
//        String input="E:\\01Work\\miRNA\\project\\CircuRNA\\database\\ann\\piR_hg19_v1.0.bed.gz";
//        DBTree dis;
//        dis=readColumnFile(liftover, input, false, 0, 1, 2, 3, 5);
        System.out.println("Test Finished!");
        
    }        

    public void showModule(){
        StringBuilder sb = new StringBuilder();
        sb.append("\n+++++++++++++++++++++++++++++++\n");
        sb.append("+   Module  --->  Annotation  +");
        sb.append("\n+++++++++++++++++++++++++++++++\n");
        System.out.println(sb.toString());
        
    }    
   
}
