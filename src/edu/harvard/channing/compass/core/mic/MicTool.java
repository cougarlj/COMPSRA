/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.core.mic;

import edu.harvard.channing.compass.core.Factory;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.ValidationStringency;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

/**
 * This class is used to call the tools used for profiling microbes.
 * @author Jiang Li
 * @version 1.0
 * @since 2017-12-18
 */
public abstract class MicTool implements Callable{
    private static final Logger LOG= (Logger) LogManager.getLogger(MicTool.class);
    /**
     * The unmapped SAM file.
     */
    public String strInput;
    /**
     * The output of Microbe Module.
     */
    public String strOutput;
    /**
     * The SAM file that aligned to the MetaPhlAn database.
     */
    public String strSAMout;
    /**
     * The unmappped fastq file transformed from the unmapped SAM file.
     */
    public String strFQ;
    /**
     * The unmapped fasta file transformed from the unmapped SAM file.
     */
    public String strFA;
    
    public int intCount = 0;
    public int mapq = 0;
    public boolean boolBlast=false;
    public String strDB;
    public StringBuilder sb;
    
    public boolean prepareFQ(){
        this.strFQ=this.strInput.split("\\.")[0]+".fastq";
        if(new File(this.strFQ).exists())   return true;
        boolean flag=this.samTofq(this.strInput, strFQ, 2);    
        return flag;
    }    
     
    
    /**
     * This function is used to transform the sam file into fastq file.
     * @param strSAM The original sam file. 
     * @param strFQ The targeted fastq file. 
     * @param style 0:total reads, 1:mapped reads, 2:unmapped reads. 
     * @return True:successfully, False: failed. 
     */
    public boolean samTofq(String strSAM, String strFQ, int style) {
        try {
            SamReader sr = SamReaderFactory.make().validationStringency(ValidationStringency.SILENT).open(new File(strSAM));
            BufferedWriter bw = Factory.getWriter(strFQ);
            switch (style) {
                case 0:
                    for (SAMRecord srdRead : sr) {
                        bw.write(srdRead.getReadName());
                        bw.newLine();
                        bw.write(srdRead.getReadString());
                        bw.newLine();
                        bw.write("+");
                        bw.newLine();
                        bw.write(srdRead.getBaseQualityString());
                        bw.newLine();
                    }   break;
                case 1:
                    for (SAMRecord srdRead : sr) {
                        if (!srdRead.getReadUnmappedFlag()) {
                            bw.write(srdRead.getReadName());
                            bw.newLine();
                            bw.write(srdRead.getReadString());
                            bw.newLine();
                            bw.write("+");
                            bw.newLine();
                            bw.write(srdRead.getBaseQualityString());
                            bw.newLine();
                        }
                    }   break;
                case 2:
                    for (SAMRecord srdRead : sr) {
                        if (srdRead.getReadUnmappedFlag()) {
                            bw.write("@"+srdRead.getReadName());
                            bw.newLine();
                            bw.write(srdRead.getReadString());
                            bw.newLine();
                            bw.write("+");
                            bw.newLine();
                            bw.write(srdRead.getBaseQualityString());
                            bw.newLine();
                        }
                    }   break;
                default:
                    bw.close();
                    return false;
            }
            bw.close();
        } catch (IOException ex) {
            LOG.error(ex.getMessage());
            return false;
        }
        return true;
    }

    public void setDB(String strDB) {
        this.strDB=strDB;
    }
    
    public String getDB(){
        return this.strDB;
    }
   
    public boolean prepareFA(){
        this.strFA=this.strInput.split("\\.")[0]+".fa";
        if(new File(this.strFA).exists())   return true;
        boolean flag=this.samTofa(this.strInput,this.strFA);
        return flag;
    }

    public boolean samTofa(String strSAM, String strFA) {
        SamReader sr = SamReaderFactory.make().validationStringency(ValidationStringency.SILENT).open(new File(strSAM));
        BufferedWriter bw = Factory.getWriter(strFA);
        for (SAMRecord srdRead : sr) {
            try {
                bw.write(">"+srdRead.getReadName());
                bw.newLine();
                bw.write(srdRead.getReadString());
                bw.newLine();
            } catch (IOException ex) {
                LOG.error(ex.getMessage());
                return false;
            }
        }
        return true;
    }
    
}
