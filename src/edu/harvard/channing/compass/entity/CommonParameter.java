/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.entity;

import edu.harvard.channing.compass.core.Configuration;
import edu.harvard.channing.compass.core.Factory;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The CommonParameter class keeps records of the basic parameters for circuRNA pipeline. 
 * @author Jiang Li
 * @version 1.0 
 * @since 2017-08-30
 */
public class CommonParameter {
    private static final Logger LOG = LogManager.getLogger(CommonParameter.class.getName());
    
    public String input;
//    public String output;
    public int intThread;
    public String strPrefix;
    public ArrayList<FileRecord> altInput;
    public String strRefGenome;
    public String strPro;
    public boolean boolCheckResource=false;

    public CommonParameter() {
        this.altInput=new ArrayList<FileRecord>();
    }

    
    public String setPrefix(String str){
        this.strPrefix=new File(str).getName().split("\\.")[0];
        return (strPrefix);        
    }
    public String getPrefix(){
        return this.strPrefix;
    }

    public void setInput() {
        String[] strInput=this.input.split(",|;");
        for(int i=0;i<strInput.length;i++){
            FileRecord frd=new FileRecord(strInput[i]);
            this.altInput.add(frd);           
        }  
    }


    public void setInput(String f) {
        BufferedReader br = Factory.getReader(f);
        try {
            String line;
            while ((line = br.readLine()) != null) {
                FileRecord frd = new FileRecord(line);
                this.altInput.add(frd);
            }
        } catch (IOException ex) {
            LOG.error(ex.getMessage());
        }
    }

    public void setOutput(String output) {
        for(FileRecord frd:this.altInput)   frd.setOutput(output);       
    }

    public void setOutput() {
        for(FileRecord frd:this.altInput)   frd.setOutput(); 
    }

    public void setRefGenome(String ref) {
        this.strRefGenome=ref;
        for(FileRecord frd:this.altInput){
            frd.strRef=ref;
        }
    }

    public String getComDir(){
        if(this.altInput==null || this.altInput.isEmpty())  return Configuration.OUTDIR;
        else{
            File fle=new File(altInput.get(0).output_dir);
            return fle.getParent();
        }
    }
}
