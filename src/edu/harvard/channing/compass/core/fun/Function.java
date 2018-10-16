/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.core.fun;

import edu.harvard.channing.compass.entity.CommonParameter;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class is used to perform the functional parts of COMPASS pipeline. 
 * @author Jiang Li
 * @version 1.0 
 * @since 2017-08-31
 */
public class Function {
    private static final Logger LOG = LogManager.getLogger(Function.class.getName());
    
    public CommonParameter comParam;
    public boolean isOpen=false;
    //For DEG.
    public boolean boolDEG=false;
    public boolean boolMerge=false;
    public String strClass="1,2,3,4,5,6";
    public String strCase="";
    public String strCtrl="";
    public String strNorm="none";
    public String strTest="mwu";
    public boolean boolOrder=true;
    public boolean boolMic=false;
    public boolean boolAnn=false;
    public String strTool;
    public String strBlastDB;
    

    public Function(CommonParameter comParam) {
        this.comParam = comParam;
    }

    
    public int manage() {
        if (!this.isOpen) {
            return -1;
        }
        this.showModule();

        String message;
        ExecutorService exe=Executors.newCachedThreadPool();
        ArrayList<Future<String>> lstResult=new ArrayList<>();
        
        if(this.boolDEG){
            DEG deg=new DEG(this.comParam,this.strClass,this.strCase,this.strCtrl,this.strNorm,this.strTest,this.boolOrder,this.boolMic,this.boolAnn,strTool,strBlastDB);
            Future<String> future = exe.submit(deg);
            lstResult.add(future);
        }      
        
        if (this.boolMerge) {
            Merge mge = new Merge(this.comParam,this.strClass,this.strCase,null,null,null,this.boolOrder,this.boolMic,this.boolAnn,strTool,strBlastDB);
            Future<String> future = exe.submit(mge);
            lstResult.add(future);
        }
        
        for(Future<String> ft:lstResult){
            try {
                while(!ft.isDone());
                message=ft.get();
//                System.out.println(message);
            } catch (InterruptedException | ExecutionException ex) {
                LOG.error(ex.getMessage());
                return 0;
            }finally{
                exe.shutdown();
            }
        }
        
        return 1;
    }

    public void showModule(){
        StringBuilder sb = new StringBuilder();
        sb.append("\n+++++++++++++++++++++++++++++++\n");
        sb.append("+   Module  --->  Functions   +");
        sb.append("\n+++++++++++++++++++++++++++++++\n");
        System.out.println(sb.toString());       
    }    
    
}
