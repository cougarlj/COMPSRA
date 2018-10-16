/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.core.fun;

import edu.harvard.channing.compass.entity.CommonParameter;
import edu.harvard.channing.compass.core.Configuration;
import edu.harvard.channing.compass.entity.FileRecord;
import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class is used to perform the differentially expressed gene analysis.
 * @author Jiang Li
 * @version 1.0 
 * @since 2018-02-09
 */
public class DEG implements Callable{

    private static final Logger LOG = LogManager.getLogger(DEG.class.getName());
    
    public CommonParameter com;
    public boolean boolDEG=false;
    public String strClass;
    public String strCase;
    public String strCtrl;
    public String strNorm;
    public String strTest; 
    public boolean boolOrder=false;
    public String strOutput;
    public boolean boolMic=false;
    public boolean boolAnn=false;
    
    public ArrayList<String> altClass;
    public ArrayList<String> altCase;
    public ArrayList<String> altCtrl;    
    public ArrayList<FileRecord> altCaseFRD;
    public ArrayList<FileRecord> altCtrlFRD;  
    public ArrayList<String> altTest;
    
    public ArrayList<Dispatcher> detacher;
    public String strTool;
    public String strBlastDB;
    
    
    public DEG(CommonParameter com, String strClass, String strCase, String strCtrl, String strNorm, String strTest,boolean boolOrder,boolean boolMic,boolean boolAnn,String strTool,String strBlastDB) {
        this.com=com;
        this.strClass = strClass;
        this.strCase = strCase;
        this.strCtrl = strCtrl;
        this.strNorm = strNorm;
        this.strTest = strTest;
        this.boolOrder=boolOrder;
        this.boolMic=boolMic;
        this.boolAnn=boolAnn;
        this.strTool=strTool;
        this.strBlastDB=strBlastDB;
    }

    public void parseClass() {
        this.altClass = new ArrayList<>();
        String[] strItem = this.strClass.split(",|;");
        for (int i = 0; i < strItem.length; i++) {
            altClass.add(strItem[i]);
        }
    }
    
    
    public void parseSample() {
        if (boolOrder) {
            //case one: 1-5,8,9
            this.altCaseFRD = this.getFRD(this.strCase);
            this.altCtrlFRD = this.getFRD(this.strCtrl);
        } else {
            //case two: case1.txt,case2.txt,...,casen.txt. 
            String[] strItem = this.strCase.split(",|;");
            for (int i = 0; i < strItem.length; i++) {
                altCase.add(strItem[i]);
            }

            strItem = this.strCtrl.split(",|;");
            for (int i = 0; i < strItem.length; i++) {
                altCtrl.add(strItem[i]);
            }
        }
    }
       
    public ArrayList<FileRecord> getFRD(String strSample) {
        ArrayList<FileRecord> altFRD = new ArrayList<>();
        if("".equals(strSample) || null==strSample)   return altFRD;
        if("all".equals(strSample)){
            altFRD.addAll(this.com.altInput);
            return altFRD;
        }
        String[] strItem = strSample.split(",|;");
        for (String strItemOne : strItem) {
            if (strItemOne.contains("-")) {
                String[] strSub = strItemOne.split("-");
                for (int j = Integer.valueOf(strSub[0]); j <= Integer.valueOf(strSub[1]); j++) {
                    altFRD.add(this.com.altInput.get(j-1));
                }
            } else {
                altFRD.add(this.com.altInput.get(Integer.valueOf(strItemOne)-1));
            }
        }
        return altFRD;
    }
    
    public void parseTest(){
        this.altTest=new ArrayList<>();
        if(null==this.strTest)    return;
        String[] strItem=this.strTest.split(",|;");
        for(int i=0;i<strItem.length;i++)   altTest.add(strItem[i]);
    }
    
    public void detachRNA(){
        this.detacher=new ArrayList<>();
        for(int i=0;i<this.altClass.size();i++){
            String strRNA=this.altClass.get(i);
            
            //For case part. 
            ArrayList<ArrayList<String>> altCaseArray=new ArrayList<>();
            if (this.altCase == null || this.altCase.isEmpty()) {
                ArrayList<ArrayList<String>> altCaseTmp = new ArrayList();
                for (FileRecord frd : this.altCaseFRD) {
                    altCaseTmp.add(frd.getProfileAnn(strRNA, this.boolAnn));
                }
                if (!altCaseTmp.isEmpty()) {
                    for (int j = 0; j < altCaseTmp.get(0).size(); j++) {
                        ArrayList<String> altOne = new ArrayList<>();
                        for (int k = 0; k < altCaseTmp.size(); k++) {
                            if (!altCaseTmp.get(k).isEmpty()) {
                                altOne.add(altCaseTmp.get(k).get(j));
                            }
                        }
                        altCaseArray.add(altOne);
                    }
                }
            } else {
                altCaseArray.add(this.altCase);
            }
            
            //For ctrl part. 
            ArrayList<ArrayList<String>> altCtrlArray=new ArrayList<>();
            if(this.altCtrl==null || this.altCtrl.isEmpty()){                
                ArrayList<ArrayList<String>> altCtrlTmp=new ArrayList();
                for(FileRecord frd:this.altCtrlFRD){
                    altCtrlTmp.add(frd.getProfileAnn(strRNA,this.boolAnn));
                }
                if (!altCtrlTmp.isEmpty()) {
                    for (int j = 0; j < altCtrlTmp.get(0).size(); j++) {
                        ArrayList<String> altOne = new ArrayList<>();
                        for (int k = 0; k < altCtrlTmp.size(); k++) {
                            if (!altCtrlTmp.get(k).isEmpty()) {
                                altOne.add(altCtrlTmp.get(k).get(j));
                            }
                        }
                        altCtrlArray.add(altOne);
                    }
                }

            }else{
                altCtrlArray.add(altCtrl);
            }
            
            //Detacher           
            
            if (!altCaseArray.isEmpty()) {          
                for (int s = 0; s < altCaseArray.size(); s++) {
                    if(altCtrlArray.size()==s)  altCtrlArray.add(new ArrayList<String>());
                    this.strOutput=this.com.getComDir()+"/"+this.com.strPro+"_"+s+"_"+Configuration.ANN_CATEGORY.get(strRNA).substring(1)+".txt";
                    this.detacher.add(new Dispatcher(strRNA, this.strNorm, this.altTest, altCaseArray.get(s), altCtrlArray.get(s), this.strOutput));
                }
            } else {
                for(int s=0;s<altCtrlArray.size();s++){
                    if(altCaseArray.size()==s)  altCaseArray.add(new ArrayList<String>());
                    this.strOutput=this.com.getComDir()+"/"+this.com.strPro+"_"+s+"_"+Configuration.ANN_CATEGORY.get(strRNA).substring(1)+".txt";
                    this.detacher.add(new Dispatcher(strRNA, this.strNorm, this.altTest, altCaseArray.get(s), altCtrlArray.get(s), this.strOutput));
                }
            }          
        }
        
        if(this.boolMic){
            String[] strMicTool=this.strTool.split(",|;");
            String[] strMicDB=this.strBlastDB.split(",|;");
            for(String strTool:strMicTool){
                for(String strDB:strMicDB){
                    //Case Part. 
                    ArrayList<ArrayList<String>> altCaseArray=new ArrayList<>();
                    ArrayList<ArrayList<String>> altCaseTmp = new ArrayList<>();
                    for (FileRecord frd : this.altCaseFRD) {
                        altCaseTmp.add(frd.getProfileMic(strTool.toLowerCase(),strDB.toLowerCase(),this.boolMic));
                    }

                    if (!altCaseTmp.isEmpty()) {
                        for (int j = 0; j < altCaseTmp.get(0).size(); j++) {
                            ArrayList<String> altOne = new ArrayList<>();
                            for (int k = 0; k < altCaseTmp.size(); k++) {
                                if (!altCaseTmp.get(k).isEmpty()) {
                                    altOne.add(altCaseTmp.get(k).get(j));
                                }
                            }
                            altCaseArray.add(altOne);
                        }
                    }

 
                    ArrayList<ArrayList<String>> altCtrlArray=new ArrayList<>();
                    ArrayList<ArrayList<String>> altCtrlTmp = new ArrayList();
                    for (FileRecord frd : this.altCtrlFRD) {
                        altCtrlTmp.add(frd.getProfileMic(strTool.toLowerCase(),strDB.toLowerCase(),this.boolMic));
                    }
                    if (!altCtrlTmp.isEmpty()) {
                        for (int j = 0; j < altCtrlTmp.get(0).size(); j++) {
                            ArrayList<String> altOne = new ArrayList<>();
                            for (int k = 0; k < altCtrlTmp.size(); k++) {
                                if (!altCtrlTmp.get(k).isEmpty()) {
                                    altOne.add(altCtrlTmp.get(k).get(j));
                                }
                            }
                            altCtrlArray.add(altOne);
                        }
                    }

                    
                    if (!altCaseArray.isEmpty()) {
                        for (int s = 0; s < altCaseArray.size(); s++) {
                            if (altCtrlArray.size() == s) {
                                altCtrlArray.add(new ArrayList<String>());
                            }
                            if ("metaphlan".equals(strTool)) {
                                this.strOutput = this.com.getComDir() + "/" + this.com.strPro+"_"+s + "_" + strTool.toLowerCase() + ".txt";
                            } else {
                                this.strOutput = this.com.getComDir()+ "/" + this.com.strPro+"_"+ s + "_microbe_"  + strTool.toLowerCase() + "_" + strDB.toLowerCase() + ".txt";
                            }
                            this.detacher.add(new Dispatcher(strTool, strDB, this.strNorm, this.altTest, altCaseArray.get(s), altCtrlArray.get(s), this.strOutput, true));
                        }
                    } else {
                        for (int s = 0; s < altCtrlArray.size(); s++) {
                            if (altCaseArray.size() == s) {
                                altCaseArray.add(new ArrayList<String>());
                            }
                            if ("metaphlan".equals(strTool)) {
                                this.strOutput = this.com.getComDir() + "/" + this.com.strPro+"_"+s + "_" + strTool.toLowerCase() + ".txt";
                            } else {
                                this.strOutput = this.com.getComDir() + "/" + this.com.strPro+"_"+ s + "_microbe_"  + strTool.toLowerCase() + "_" + strDB.toLowerCase() + ".txt";
                            }
                            this.detacher.add(new Dispatcher(strTool, strDB, this.strNorm, this.altTest, altCaseArray.get(s), altCtrlArray.get(s), this.strOutput, true));
                        }
                    }                
                }
            }
        }              
    }
 
    public void runDEG(){
        String message;
        ExecutorService exe=Executors.newCachedThreadPool();
        ArrayList<Future<String>> lstResult=new ArrayList<Future<String>>();
        
        for(int i=0;i<this.detacher.size();i++){
            Future<String> future = exe.submit(this.detacher.get(i));
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


    @Override
    public Object call() throws Exception {
        try {
            this.showModule();
            this.parseClass();
            LOG.info("Class Parse ---> OK!");
            this.parseTest();
            LOG.info("Test Parse ---> OK!");
            this.parseSample();
            LOG.info("Sample Parse ---> OK!");
            this.detachRNA();
            LOG.info("Detach RNA Process ---> OK!");
            this.runDEG();
            LOG.info("Run DEG  ---> OK!");
        } catch (Exception e) {
            LOG.info("DEG was failed to run.");
            return "DEG failed!";
        }
        return "DEG was finished!";
    }

    public void showModule(){
        StringBuilder sb = new StringBuilder();
        sb.append("\n-----------\n");
        sb.append("|   DEG   |");
        sb.append("\n-----------\n");
        LOG.info(sb.toString());
        
    }      
}
