/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.core.mic;

import edu.harvard.channing.compass.core.Factory;
import edu.harvard.channing.compass.entity.CommonParameter;
import edu.harvard.channing.compass.core.Configuration;
import edu.harvard.channing.compass.entity.FileRecord;
import edu.harvard.channing.compass.toolkit.Taxonomy;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class is used to perform the identification of microbes.
 * @author Jiang Li
 * @version 1.0 
 * @since 2017-10-19
 */
public class Microbe {
    private static final Logger LOG = LogManager.getLogger(Microbe.class.getName());
    
    public CommonParameter comParam;   
    public boolean isOpen=false;
    public String strTool="blast";
    public String strIndex="last";
    public String strBlastDB="nt";
    public ArrayList<MicTool> altMT;
    public Taxonomy tomy;
    
    public Microbe(CommonParameter comParam){
        this.comParam=comParam;
    }
    
    public int manage() {
        if (!isOpen) {
            return -1;
        }
        this.showModule();
        
        String message;
//        ExecutorService exe=Executors.newCachedThreadPool();
        ExecutorService exe=Executors.newFixedThreadPool(this.comParam.intThread);
        ArrayList<Future<String>> lstResult=new ArrayList<Future<String>>();
                              
        String[] strTool = this.strTool.split(",|;");
        String[] strDB = this.strBlastDB.split(",|;");
        this.altMT=new ArrayList();
        
        for (FileRecord frd : this.comParam.altInput) {
            frd.setMic(this);        
            for(Iterator<String> itr=frd.output_mic.keySet().iterator();itr.hasNext();){
                String strIn=itr.next();
                for(String tool:strTool){
                    if("metaphlan".equals(tool.toLowerCase())){
                        MicTool mt=Factory.getMicTool(tool, strIn, frd.output_mic.get(strIn).get(tool.toLowerCase()).get(0));
                        this.altMT.add(mt);
                    }
                    if("blast".equals(tool.toLowerCase())){
                        for(int i=0;i<strDB.length;i++){
                            MicTool mt=Factory.getMicTool(tool, strIn, frd.output_mic.get(strIn).get(tool.toLowerCase()).get(i).replace(".txt", ".tmp"));
                            mt.setDB(strDB[i]);
                            this.altMT.add(mt);
                        }
                        
                    }
                }          
            }
        }
  
        if(this.strTool.toLowerCase().contains("blast")){            
            //The DB sould be made index first. 
            Blast bt=new Blast(this.strBlastDB);
            bt.checkBlast();
            bt.makeDB();
        }
        
        for (int i = 0; i < this.altMT.size(); i++) {
            Future<String> future = exe.submit(altMT.get(i));
            lstResult.add(future);
        }
        
        for(Future<String> ft:lstResult){
            try {
                while(!ft.isDone());
                message=ft.get();
//                System.out.println(message);
            } catch (InterruptedException ex) {
                LOG.error(ex.getMessage());
                return 0;
            } catch (ExecutionException ex) {
                LOG.error(ex.getMessage());
                return 0;
            }finally{
                exe.shutdown();
            }
        }       
        

        if(this.strTool.toLowerCase().contains("blast")){
            this.tomy=new Taxonomy(Configuration.NT.get("names"),Configuration.NT.get("nodes"),Configuration.NT.get("A2T"));
            this.tomy.strSurfix[5]="";
            this.tomy.setAcc2Tax(Configuration.NT.get("A2T"));
            this.tomy.init();
            for(MicTool mt:this.altMT){
                if(mt.getClass().equals(MetaPhlAn.class))   continue;
                this.tomy.strBlastResult=mt.strOutput;                
                this.tomy.strOut=mt.strOutput.replace(".tmp", ".txt");
                this.tomy.taxid=this.tomy.ComUse.get(mt.getDB());
                this.tomy.map2tree();
                this.tomy.freshTree();
            }
        }        
        return 1;
    }
    
    public void showModule(){
        StringBuilder sb = new StringBuilder();
        sb.append("\n+++++++++++++++++++++++++++++++\n");
        sb.append("+    Module  --->  Microbe    +");
        sb.append("\n+++++++++++++++++++++++++++++++\n");
        System.out.println(sb.toString());
 
    }    
    
}
