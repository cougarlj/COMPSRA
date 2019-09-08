/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.toolkit;

import edu.harvard.channing.compass.core.Configuration;
import edu.harvard.channing.compass.core.Factory;
import edu.harvard.channing.compass.core.qc.Fastq;
import edu.harvard.channing.compass.core.qc.Fastq.Read;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class is used to extract UMI reads from the given fastq file. 
 * @author Jiang Li
 * @version 1.0 
 * @since 2019-03-06
 */
public class UMI implements ToolKit{
    private static final Logger LOG = LogManager.getLogger(UMI.class.getClass());
    
    public String strInput;
    public String strOutput;
    public String strAdapter="AACTGTAGGCACCATCAAT";
    public int intUMI=12;
    public int tolerance=2;
    public HashMap<String,Read> hmpUMI;
    public Fastq fqRawFile;   
    public boolean boolMark=true;
    public boolean boolExtract=false;
    public int intReadCount=0;
    public int intFailedReadCount=0;
    
    
    
    @Override
    public int runKit() {
        try {
            File fleInput=new File(strInput);
            if(!fleInput.exists()){
                LOG.error("The input file is empty!");
                return 0;
            }
            
            if(strOutput==null){
                strOutput=strInput.split("\\.")[0]+"_UMI.fastq.gz";
            }
            
            fqRawFile=new Fastq();
            fqRawFile.setInput(strInput);
            fqRawFile.setOutput(strOutput);
            
            this.hmpUMI=new HashMap();
            Read read;
            while((read=fqRawFile.getRead(fqRawFile.brInput))!=null){
                this.intReadCount++;
                String strUMICode=read.getUMICode(strAdapter, intUMI,tolerance);
                if (boolMark) {
                    read.addUMIMarker(strUMICode);
                    if (boolExtract) {
                        if (strUMICode == null) {
                            this.intFailedReadCount++;
                            continue;
                        }
                    }
                    if(strUMICode==null)    this.intFailedReadCount++;
                    read.writeRead(fqRawFile.bwOutput);
                } else {
                    if (boolExtract) {
                        if (strUMICode == null) {
                            this.intFailedReadCount++;
                            continue;
                        }
                        read.writeRead(fqRawFile.bwOutput);
                    } else {
                        //This case is not practical. So we can design to output the unique reads with UMI code. 
                        if (strUMICode == null) {
                            this.intFailedReadCount++;
                            continue;
                        } else {
                            if (this.hmpUMI.containsKey(strUMICode)) {
                                if (read.betterThan(this.hmpUMI.get(strUMICode))) {
                                    read.intUMIDup = this.hmpUMI.get(strUMICode).intUMIDup + 1;
                                    this.hmpUMI.put(strUMICode, read);
                                } else {
                                    this.hmpUMI.get(strUMICode).intUMIDup++;
                                }
                            } else {
                                this.hmpUMI.put(strUMICode, read);
                            }
                        }
                    }

                }
            }
            
            if(!boolMark && !boolExtract){
            for(Map.Entry<String,Read> entry:hmpUMI.entrySet()){
                entry.getValue().writeRead(fqRawFile.getOutput());
            }            
        }


            this.fqRawFile.brInput.close();
            this.fqRawFile.bwOutput.close();
            
            LOG.info("Total Read: "+this.intReadCount);
            LOG.info("Failed Read: "+this.intFailedReadCount);

            return 1;
        } catch (IOException ex) {
            LOG.error(ex.getMessage());
            return 0;
        }
        
    }
    
        public static void main(String[] argv){
        Configuration config=new Configuration();
        UMI eumiTest=new UMI();
        eumiTest.strInput="E:\\01Work\\QIAGEN\\data\\UMI_Test.fastq";
        eumiTest.strOutput="E:\\01Work\\QIAGEN\\data\\UMI_Test_Filter.fastq";
        eumiTest.runKit();
    }
    
}
