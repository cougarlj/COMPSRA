/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.core.qc;

import edu.harvard.channing.compass.entity.CommonParameter;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class is used to perform the Quality Control function for COMPASS pipeline.
 * @author Jiang Li
 * @version 1.0
 * @since 2017-08-30
 */
public class QualityControl {

    private static final Logger LOG = LogManager.getLogger(QualityControl.class.getName());

    public CommonParameter comParam;
    public boolean isOpen=false;
    public boolean boolRmAdapter = false;
    public String strRmAdapter = null;
    public boolean boolRmQualityHead = false;
    public int intRmQualityHead = 20;
    public boolean boolRmQualityTail = false;
    public int intRmQualityTail = 20;
    public boolean boolRmQualityRead = false;
    public int intRmQualityRead = 20;
    public boolean boolRmBias=false;
    public int intRmBias=0;

    public boolean boolRmBaseHead = false;
    public int intRmBaseHead = 0;
    public boolean boolRmBaseTail = false;
    public int intRmBaseTail = 0;
    public boolean boolRmLengthRead = false;
    public String strRmLengthRead = "0";
    
//    public int intRawReadLength=50;
    
//    public Fastq[] fqInput=null;
//    public ArrayList<String[]> strOutput=null;

    
    public QualityControl(CommonParameter comParam) {
        this.comParam = comParam;
    }
    
    /**
     * This function is used to control the fastq qc process. 
     * @return True for completing and False for failing.
     */
    public int manage() {
        if (!this.isOpen) {
            return -1;
        }
        this.showModule();
//        String[] strIn=this.comParam.CommonParameter.this.strInput.split(",|;|:");
        String message;
        ExecutorService exe = Executors.newCachedThreadPool();
        ArrayList<Future<String>> lstResult = new ArrayList<Future<String>>();
        for (int i = 0; i < this.comParam.altInput.size(); i++) {
            Fastq fq = new Fastq(this.comParam.altInput.get(i), this);           
            Future<String> future = exe.submit(fq);
            lstResult.add(future);
        }

        for (Future<String> ft : lstResult) {
            try {
                while (!ft.isDone());
                message = ft.get();
                System.out.println(message);
            } catch (InterruptedException ex) {
                LOG.info(ex.getMessage());
            } catch (ExecutionException ex) {
                LOG.info(ex.getMessage());
            } finally {
                exe.shutdown();
            }
        }
        
        return 1;
    }  


    public void showModule(){
        StringBuilder sb=new StringBuilder();
        sb.append("\n+++++++++++++++++++++++++++++++\n");
        sb.append("+  Module ---> QualityControl +");
        sb.append("\n+++++++++++++++++++++++++++++++\n");
        System.out.println(sb.toString());
    }

}
