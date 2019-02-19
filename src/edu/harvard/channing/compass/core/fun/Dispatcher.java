/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.core.fun;

import edu.harvard.channing.compass.core.Factory;
import edu.harvard.channing.compass.core.Configuration;
import edu.harvard.channing.compass.utility.MathTools;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.commons.math3.stat.inference.MannWhitneyUTest;

/**
 * This class is used to abstract a certain kinds of RNA info from the annotation file.
 * @author Jiang Li
 * @version 1.0 
 * @since 2018-02-11
 */
public class Dispatcher implements Callable{

    private static final Logger LOG = LogManager.getLogger(Dispatcher.class.getName());
    
    public String strRNA;
    public String strNorm;
    public ArrayList<String> altTest;
    public ArrayList<String> altCase;
    public ArrayList<String> altCtrl;
    
    public Picker[] picker;
    public HashMap<String,ArrayList<Integer>> hmpProfile;
    public double[][] intProfile;
    public ArrayList<String> altID;
    public ArrayList<ArrayList<Double>> altPValue;
    
    public int intFirstKey=2;
    public int intSecondKey=1;
    public int intAssist=-1;
    public int intHit=3;
    
    public int intSize;
    public String strOutput;
    
    public boolean boolMic=false;

    public Dispatcher(String strRNA, String strNorm, ArrayList<String> altTest, ArrayList<String> altCase, ArrayList<String> altCtrl, String strOutput) {
        this.strRNA = Configuration.ANN_CATEGORY.get(strRNA).substring(1);
        this.strNorm = strNorm;
        this.altTest = altTest;
        this.altCase = altCase;
        this.altCtrl = altCtrl;
        this.strOutput=strOutput;
        this.intSize=altCase.size()+altCtrl.size();
    }

    Dispatcher(String strTool, String strDB, String strNorm, ArrayList<String> altTest, ArrayList<String> altCase, ArrayList<String> altCtrl, String strOutput,boolean boolMic) {
        if("metaphlan".equals(strTool))    this.strRNA="microbe_"+strTool;
        else    this.strRNA = "microbe_"+strTool+"_"+strDB;
        this.strNorm = strNorm;
        this.altTest = altTest;
        this.altCase = altCase;
        this.altCtrl = altCtrl;
        this.strOutput=strOutput;
        this.boolMic=boolMic;
        this.intSize=altCase.size()+altCtrl.size();
    }
 
    public void setColumn(){
        if("miRNA".equals(this.strRNA)){
//            this.intFirstKey=1;
//            this.intSecondKey=2;
//            this.intAssist=2;
        }else if("piRNA".equals(this.strRNA)){
            this.intFirstKey=1;
            this.intSecondKey=2;
        }else if(this.strRNA.startsWith("microbe_metaphlan")){
            this.intFirstKey=0;
            this.intHit=2; //if set 4, it will use relative enrichment value and a NumberFormatException will occur. 
        }else if(this.strRNA.startsWith("microbe_blast")){
            this.intFirstKey=0;
            this.intHit=2;
        }
        
    }
    
    public void runPicker(){
        
        picker=new Picker[intSize];
        int i=0;
        if (this.boolMic) {
            for (; i < altCase.size(); i++) {
                picker[i] = new Picker_Mic(altCase.get(i), intFirstKey, intSecondKey, intAssist, intHit);
            }
            for (int j = 0; j < altCtrl.size(); j++) {
                picker[i + j] = new Picker_Mic(altCtrl.get(j), intFirstKey, intSecondKey, intAssist, intHit);
            }
        } else {
            for (; i < altCase.size(); i++) {
                picker[i] = new Picker(altCase.get(i), intFirstKey, intSecondKey, intAssist, intHit);
            }
            for (int j = 0; j < altCtrl.size(); j++) {
                picker[i + j] = new Picker(altCtrl.get(j), intFirstKey, intSecondKey, intAssist, intHit);
            }
        }
      
        String message;
        ExecutorService exe=Executors.newCachedThreadPool();
        ArrayList<Future<String>> lstResult=new ArrayList<Future<String>>();
        
        for(int k=0;k<picker.length;k++){
            Future<String> future = exe.submit(picker[k]);
            lstResult.add(future);
        }
        
        for(Future<String> ft:lstResult){
            try {
                while(!ft.isDone());
                message=ft.get();
                LOG.info(message);
//                System.out.println(message);
            } catch (InterruptedException ex) {
                LOG.error(ex.getMessage());             
            } catch (ExecutionException ex) {
                LOG.error(ex.getMessage());
            }finally{
                exe.shutdown();
            }
        }               
    }
    
    public void convergePicker(){
        this.hmpProfile=new HashMap<String,ArrayList<Integer>>();
        for(int i=0;i<this.intSize;i++){
            for(Map.Entry<String,Integer> entry : this.picker[i].content.entrySet()){
                if(!this.hmpProfile.containsKey(entry.getKey())){
                    this.hmpProfile.put(entry.getKey(), new ArrayList<Integer>());
                    for(int j=0;j<i;j++)    this.hmpProfile.get(entry.getKey()).add(0);
                }
                this.hmpProfile.get(entry.getKey()).add(entry.getValue());
            }
            
            for(String strKey:this.hmpProfile.keySet()){
                if(this.hmpProfile.get(strKey).size()==i)    this.hmpProfile.get(strKey).add(0);
            }            
        }
                
        this.altID=new ArrayList<String>(this.hmpProfile.keySet());
        this.intProfile=new double[this.altID.size()][this.intSize];
    
        for(int i=0;i<this.altID.size();i++){
            for(int j=0;j<this.intSize;j++){
                try{
                    intProfile[i][j]=(double)this.hmpProfile.get(this.altID.get(i)).get(j);
                }catch(IndexOutOfBoundsException e){
                    System.out.println(this.hmpProfile.get(this.altID.get(i)).get(j));
                }
                
            }
        }
    }
    
    public void normProfile(){
        //Do something for intProfile;
        if("cpm".equals(this.strNorm)){
            this.intProfile=MathTools.CpM(this.intProfile);
        }else if("qt".equals(this.strNorm)){
            this.intProfile=MathTools.QT(this.intProfile);
        }else if("qtl".equals(this.strNorm)){
            this.intProfile=MathTools.QTL(this.intProfile);
        }else{
            return;
        }
    }
    
    public void testProfile(){
        altPValue=new ArrayList<ArrayList<Double>>(); 
        
        if(this.altTest==null || this.altTest.isEmpty())    return;
        for(int i=0;i<this.altTest.size();i++){
            if("mwu".equals(altTest.get(i))){
                ArrayList<Double> altMWU=new ArrayList<Double>();
                MannWhitneyUTest mwu=new MannWhitneyUTest();
                for(int j=0;j<this.intProfile.length;j++){
                    double[] dblCase=new double[this.altCase.size()];
                    double[] dblCtrl=new double[this.altCtrl.size()];
                    System.arraycopy(this.intProfile[j], 0, dblCase, 0, dblCase.length);
                    System.arraycopy(this.intProfile[j], this.altCase.size(), dblCtrl, 0, dblCtrl.length);
                    double p=mwu.mannWhitneyUTest(dblCase, dblCtrl);
                    altMWU.add(p);
                }
                this.altPValue.add(altMWU);
            }
            if("ks".equals(altTest.get(i))){
                //to be finished. 
            }
             
        }
    }
    
    public void outputResult() {
        BufferedWriter bw = Factory.getWriter(strOutput);

        try {
//            if(this.boolMic)    bw.write(this.strRNA+"\t");
//            else    bw.write(this.strRNA+"\t");
            bw.write(this.strRNA+"\t");
            
            for (int i = 0; i < this.altCase.size(); i++) {                
                bw.write(new File(this.altCase.get(i)).getName()+"\t");
            }
            for (int i=0;i<this.altCtrl.size();i++){
                bw.write(new File(this.altCtrl.get(i)).getName()+"\t");
            }
            for (int i=0;i<this.altTest.size();i++){
                bw.write(this.altTest.get(i)+"\t");
            }
            bw.newLine();
            
            for(int i=0;i<this.intProfile.length;i++){
                bw.write(this.altID.get(i)+"\t");
                for(int j=0;j<this.intProfile[i].length;j++){
                    bw.write(this.intProfile[i][j]+"\t");
                }
                for(int j=0;j<this.altPValue.size();j++){
                    bw.write(this.altPValue.get(j).get(i)+"\t");
                }
                bw.newLine();
            }
            
            bw.close();
        } catch (IOException ex) {
            LOG.error(ex.getMessage());
        }finally{
            
        }
    }
    
    
    @Override
    public Object call() throws Exception {
        this.setColumn();
        this.runPicker();
        this.convergePicker();
        this.normProfile();
        this.testProfile();
        this.outputResult();    
        return "The "+this.strRNA+" detacher was completed.";
    }
    
    public static void main(String[] argv){
        Configuration config=new Configuration();
        ArrayList<String> altCase=new ArrayList<String>();
        ArrayList<String> altCtrl=new ArrayList<String>();
        ArrayList<String> altTest=new ArrayList<String>();
        
        altCase.add("E:\\01Work\\miRNA\\project\\COMPASS\\output\\S-001570892_ATCACG_L001_R1\\S-001570892_ATCACG_L001_R1_17to50_FitRead_STAR_Aligned_miRNA.txt");
        altCase.add("E:\\01Work\\miRNA\\project\\COMPASS\\output\\S-001570893_CGATGT_L001_R1\\S-001570893_CGATGT_L001_R1_17to50_FitRead_STAR_Aligned_miRNA.txt");
        altCtrl.add("E:\\01Work\\miRNA\\project\\COMPASS\\output\\HBRNA_AGTCAA_L001_R1\\HBRNA_AGTCAA_L001_R1_17to50_FitRead_STAR_Aligned_circRNA.txt");
//        altCase.add("E:\\01Work\\microbe\\data\\ncbi_part_tree2.txt");
//        altCtrl.add("E:\\01Work\\microbe\\data\\ncbi_part_tree2.txt");
//        altCase.add("E:\\01Work\\miRNA\\project\\COMPASS\\output\\HBRNA_AGTCAA_L001_R1\\HBRNA_AGTCAA_L001_R1_17to50_FitRead_STAR_Aligned_UnMapped_MetaPhlAn.txt");
//        altCtrl.add("E:\\01Work\\miRNA\\project\\COMPASS\\output\\HBRNA_AGTCAA_L001_R1\\HBRNA_AGTCAA_L001_R1_17to50_FitRead_STAR_Aligned_UnMapped_MetaPhlAn.txt");
        altTest.add("mwu");
        
//        Dispatcher dtc=new Dispatcher("metaphlan","fungi","cpm",altTest,altCase,altCtrl,"E:\\01Work\\miRNA\\project\\COMPASS\\output\\blast_fungi.txt",true);
        Dispatcher dtc=new Dispatcher("1","none",altTest,altCase,altCtrl,"E:\\01Work\\miRNA\\project\\COMPASS\\output\\test_deg2.txt");
//        dtc.strOutput="E:\\01Work\\miRNA\\project\\COMPASS\\output\\piRNA.txt";
        dtc.setColumn();
        dtc.runPicker();
        dtc.convergePicker();
        dtc.normProfile();
        dtc.testProfile();       
        dtc.outputResult();
        
    }
    
}
