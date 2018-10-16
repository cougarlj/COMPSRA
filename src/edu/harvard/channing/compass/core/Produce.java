/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.core;

import edu.harvard.channing.compass.core.fun.Function;
import edu.harvard.channing.compass.core.mic.Microbe;
import edu.harvard.channing.compass.core.ann.Annotation;
import edu.harvard.channing.compass.core.aln.Alignment;
import edu.harvard.channing.compass.core.qc.QualityControl;
import edu.harvard.channing.compass.entity.FileRecord;
import edu.harvard.channing.compass.utility.Download;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class is used to control the COMPASS pipeline step by step. 
 * @author Jiang Li
 * @version 1.0 
 * @since 2017-08-31
 */
public class Produce {
    private static final Logger LOG = LogManager.getLogger(Produce.class.getName());
    
    /**
     * qc is the main object for QualityControl.
     */
    public QualityControl qc;
    /**
     * align is the main object for Alignment.
     */
    public Alignment align;
    /**
     * ann is the main object for Annotation.
     */
    public Annotation ann;
    /**
     * mic is the main object for Microbe.
     */
    public Microbe mic;
    /**
     * fun is the main object for Function.
     */
    public Function fun;
    /**
     * dld is used to download all the needed resources on net. 
     */
    public Download dld;
    
    /**
     * The constructor of Process class.
     * @param qc QualityControl Module.
     * @param align Alignment Module.
     * @param ann Annotation Module.
     * @param fun FunctionAnalysis Module.
     */
    public Produce(QualityControl qc, Alignment align, Annotation ann, Microbe mic, Function fun) {
        this.qc = qc;
        this.align = align;
        this.ann = ann;
        this.mic=mic;
        this.fun = fun;
    }
    
    /**
     * The main function to adjust and control the process of different modules. 
     */
    public void Dispatch(){
        int intFlag;
        //checkResource();
        
//        qc.showModule();
        intFlag=qc.manage();
        this.report("QC", intFlag);
        
//        align.showModule();
        intFlag=align.manage();
        this.report("Alignment", intFlag);    
        
//        ann.showModule();
        intFlag=ann.manage();
        this.report("Annotation", intFlag);              
        
//        mic.showModule();
        intFlag=mic.manage();
        this.report("Microbe", intFlag);
        
//        fun.showModule();
        intFlag=fun.manage();
        this.report("Function", intFlag);
        
//        for(FileRecord fr:this.qc.comParam.altInput){
//            System.out.println("---------------------------------------");
//            System.out.println(fr.input);
//            System.out.println(fr.output_qc.toString());
//            System.out.println(fr.output_aln.toString());
//            System.out.println(fr.output_ann.toString());
//            System.out.println(fr.output_mic.toString());                 
//            System.out.println("---------------------------------------");
//        }
//        LOG.info("All the processes are runing well!");
    }

    public void checkResource() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
       
    
    public void report(String module,int flag){
        switch (flag) {
            case -1: {
                LOG.info(module+" module was not performed.\n");
                break;
            }
            case 1: {
                LOG.info(module+" module was completed.\n");
                break;
            }
            case 0:{
                LOG.info("Error: "+module+" module fails!\n");
                break;
            }
        }        
    }
}
