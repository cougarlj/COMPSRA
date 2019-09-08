/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.core.aln;

import edu.harvard.channing.compass.core.Configuration;
import edu.harvard.channing.compass.core.Factory;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class is used to call STAR. 
 * @author Jiang Li
 * @version 1.0
 * @since 2017-08-30
 */
public class Aligner_STAR extends Aligner{
    private static final Logger LOG = LogManager.getLogger(Aligner_STAR.class.getClass());
    String strToolName="star";
    String strToolPath;
    
    //In default, COMPASS follow the exceRpt settings. 
    String strToolParam="--runThreadN 4 --runMode alignReads  --outSAMtype BAM Unsorted  --outSAMattributes Standard  --readFilesCommand zcat  --outSAMunmapped Within  --outReadsUnmapped None  --alignEndsType Local --outFilterMismatchNmax 1 --alignIntronMax 1  --alignIntronMin 2 --outFilterMultimapScoreRange 1  --outFilterScoreMinOverLread 0.66  --outFilterMatchNminOverLread 0.66  --outFilterMismatchNoverLmax 0.05  --outFilterMatchNmin 16  --outFilterMultimapNmax 1000000";
    
    public Aligner_STAR(String in, String out) {
        //input: HBRNA_AGTCAA_L001_R1_test_8to17_FitRead.fastq.gz
        //output: HBRNA_AGTCAA_L001_R1_test_17to50_FitRead_STAR_Aligned.out.sam
        //prefix: HBRNA_AGTCAA_L001_R1_test_17to50_FitRead_STAR_
        super(in, out.replace("Aligned.out.sam", ""));
    }

    @Override
    public void setCommand() {
        if(this.strParam!=null) this.strToolParam=this.strParam;
        this.command=Configuration.PLUG_BUILT_IN.get(strToolName)+" "+this.strToolParam+" --genomeDir "+Configuration.STAR_REF.get(strToolName+"_"+strRef)+" --readFilesIn "+this.strInput+" --outFileNamePrefix "+this.strOutput.replace("Aligned.out.bam", "")+"\n";
    }

    @Override
    public void setRefDB(String strRefDB) {
//        this.strToolRef=strRefDB;
    }
    
    @Override
    public void setPermission(){
        File fleSTAR=new File(Configuration.PLUG_BUILT_IN.get(strToolName));
        if(!fleSTAR.exists()){
            LOG.error("STAR was not installed correctly!");
            System.exit(1);
        }else{
            boolean boolFlag=fleSTAR.setExecutable(true);
            if(!boolFlag){
                LOG.warn("Failed to set STAR executable permission! Please set it by: chmod 755 "+Configuration.PLUG_BUILT_IN.get(strToolName)+" !");
            }
        }        
    }
    
    @Override
    public void buildGenomeIndex(String strRef){
        try {
            String strCmd=Configuration.PLUG_BUILT_IN.get(strToolName)+" --runMode genomeGenerate"+" --runThreadN 4 "+" --genomeDir "+Configuration.STAR_REF.get(strToolName+"_"+strRef)+" --genomeFastaFiles ";
            String strFa;
            if("hg19".equals(strRef)){
                //Should be multiple chromosome files.
                strFa=Configuration.STAR_REF.get("star_hg19")+"/hg19.fa.gz";
            }else if("hg38".equals(strRef)){
                strFa=Configuration.STAR_REF.get("star_hg38")+"/hg38.fa";
            }else if("mm9".equals(strRef)){
                strFa=Configuration.STAR_REF.get("star_mm9")+"/mm9.fa.gz";
            }else if("mm10".equals(strRef)){
                strFa=Configuration.STAR_REF.get("star_mm10")+"/mm10.fa.gz";
            }else{
                strFa=null;
            }
            strCmd=strCmd+strFa;
            this.runCmd(strCmd);
        } catch (Exception ex) {
            LOG.error("Fail to build index for "+strRef);
            System.exit(1);
        }
    }
    
    @Override
    public void setParam(String strParam) {
        if(strParam==null)  return;
        ArrayList <String> altParam=new ArrayList();
        BufferedReader br=Factory.getReader(Configuration.STAR_PARAM);
        if(br==null){
            this.strParam=null;
            return;                
        }else{
            try {
                for(String strLine=br.readLine();strLine!=null;strLine=br.readLine()){
                    altParam.add(strLine.trim());                  
                }
            } catch (IOException ex) {
                LOG.error("Fail to read the STAR parameter file: "+Configuration.STAR_PARAM);
                this.strParam=null;
                return;
            }
        }
        
        try{
            int intIdx=Integer.valueOf(strParam);
            if(intIdx>altParam.size()){
                this.strParam=null;
            }else{
                this.strParam=altParam.get(intIdx);
            }
        }catch(Exception ex){
            LOG.error("STAR parameter index error. The default parameter is used.");
            this.strParam=null;
        }       
        
    }
    
}
