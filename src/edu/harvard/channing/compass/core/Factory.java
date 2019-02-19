/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.core;

import edu.harvard.channing.compass.core.mic.MicTool;
import edu.harvard.channing.compass.core.ann.IPTR_circRNA;
import edu.harvard.channing.compass.core.ann.IPTR_miRNA;
import edu.harvard.channing.compass.core.ann.IPTR_tRNA;
import edu.harvard.channing.compass.core.ann.IPTR_piRNA;
import edu.harvard.channing.compass.core.ann.IPTR_GENCODE;
import edu.harvard.channing.compass.core.ann.IPTR_snRNA;
import edu.harvard.channing.compass.core.ann.IPTR_snoRNA;
import edu.harvard.channing.compass.core.ann.IPTR;
import edu.harvard.channing.compass.core.ann.Annotator;
import edu.harvard.channing.compass.core.aln.Aligner_BT2;
import edu.harvard.channing.compass.core.aln.Aligner;
import edu.harvard.channing.compass.core.aln.Aligner_STAR;
import edu.harvard.channing.compass.core.aln.Aligner_BT;
import edu.harvard.channing.compass.core.ann.IPTR_pre_miRNA;
import edu.harvard.channing.compass.core.ann.TextAnnotator;
import edu.harvard.channing.compass.core.ann.SAMAnnotator;
import edu.harvard.channing.compass.core.mic.Blast;
import edu.harvard.channing.compass.db.DB;
import edu.harvard.channing.compass.db.DB_GENCODE;
import edu.harvard.channing.compass.db.DB_GEN_snRNA;
import edu.harvard.channing.compass.db.DB_GEN_snoRNA;
import edu.harvard.channing.compass.db.DB_GtRNAdb;
import edu.harvard.channing.compass.db.DB_circBase;
import edu.harvard.channing.compass.db.DB_miRBase;
import edu.harvard.channing.compass.db.DB_piRBase;
import edu.harvard.channing.compass.db.DB_piRNABank;
import edu.harvard.channing.compass.db.DB_piRNACluster;
import edu.harvard.channing.compass.entity.DBLeaf;
import edu.harvard.channing.compass.entity.DBLeaf_circRNA;
import edu.harvard.channing.compass.entity.DBLeaf_gencode;
import edu.harvard.channing.compass.entity.DBLeaf_miRNA;
import edu.harvard.channing.compass.entity.DBLeaf_piRNA;
import edu.harvard.channing.compass.entity.DBLeaf_snRNA;
import edu.harvard.channing.compass.entity.DBLeaf_snoRNA;
import edu.harvard.channing.compass.entity.DBLeaf_tRNA;
import edu.harvard.channing.compass.core.mic.MetaPhlAn;
import edu.harvard.channing.compass.core.mut.Genome;
import edu.harvard.channing.compass.core.mut.HumanGenome;
import edu.harvard.channing.compass.db.DB_Ensembl_snRNA;
import edu.harvard.channing.compass.db.DB_pre_miRBase;
import edu.harvard.channing.compass.utility.LimitInputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class is used to create different kinds of objects. 
 * @author Jiang Li
 * @version 1.0 
 * @since 2017-09-23
 */
public class Factory {
    private static final Logger LOG = LogManager.getLogger(Factory.class.getName());
    /**
     * This function return the related Annotator.
     * @param strFlag SAM Annotator or TXT Annotator.
     * @param strStyle The database categories will be used in the Annotator. 01-miRNA, 02-piRNA, 03-tRNA, 04-snoRNA, 05-snRNA, 06-circRNA.
     * @return The related Annotator.
     */
    public static Annotator getAnnotator(String strFlag,String strStyle){
        if("sam".equals(strFlag)|| "bam".equals(strFlag))  return new SAMAnnotator(strStyle);
        else return new TextAnnotator(strStyle);
    }    

//    public static IPTR getInterpreter(String strStyle, Alignment align) {
//        if(null == strStyle)   return null;
//        else switch (strStyle) {
//            case "1":
//                return new IPTR_miRNA(align);
//            case "2":
//                return new IPTR_piRNA(align);
//            case "3":
//                return new IPTR_tRNA(align);
//            case "4":
//                return new IPTR_snoRNA(align);
//            case "5":
//                return new IPTR_snRNA(align);
//            case "6":
//                return new IPTR_circRNA(align);
//            default:
//                return null;
//        }
//    }
    
    public static DBLeaf createLeaf(String key){
        if(key.startsWith("01"))    return new DBLeaf_miRNA();
        else if(key.startsWith("02"))   return new DBLeaf_piRNA();
        else if(key.startsWith("03"))   return new DBLeaf_tRNA();
        else if(key.startsWith("04"))   return new DBLeaf_snoRNA();
        else if(key.startsWith("05"))   return new DBLeaf_snRNA();
        else if(key.startsWith("06"))   return new DBLeaf_circRNA();
        else if(key.startsWith("0002"))   return new DBLeaf_gencode();
        else    return new DBLeaf();       
    }
    
    public static BufferedReader getReader(String input) {
        File fleIn = new File(input);
        try {
            if (!fleIn.exists()) {
                LOG.info("Read File Error: Fail to read file: " + input);
            } else {

                if (fleIn.getName().endsWith(".gz")) {
                    return new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(fleIn))));
                } else if (fleIn.getName().endsWith(".zip")) {
                    ZipInputStream zipIn = new ZipInputStream(new BufferedInputStream(new FileInputStream(fleIn)));
                    return new BufferedReader(new InputStreamReader(new ZipFile(fleIn).getInputStream(zipIn.getNextEntry())));
                } else if (fleIn.getName().endsWith(".bz2")) {
//                    BZip2CompressorInputStream gis=new BZip2CompressorInputStream(new FileInputStream(input));
                    return new BufferedReader(new InputStreamReader(new BZip2CompressorInputStream(new FileInputStream(input))));
                } else {
                    return new BufferedReader(new InputStreamReader(new FileInputStream(fleIn)));
                }

            }
        } catch (FileNotFoundException ex) {
            LOG.error(ex.getMessage());
        } catch (IOException ex) {
            LOG.error(ex.getMessage());
        }
        return null;
    }
    
    public static BufferedWriter getWriter(String output) {
        File fleOut = new File(output);
        if(!fleOut.getParentFile().exists()) fleOut.getParentFile().mkdirs();
        try {
            if (fleOut.getName().endsWith(".gz")) {
                return new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(fleOut))));
            } else {
                return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fleOut)));
            }
        } catch (FileNotFoundException ex) {
            LOG.error(ex.getMessage());
        } catch (IOException ex) {
            LOG.error(ex.getMessage());
        }
        return null;
    }

    public static InputStream getInputStream(String input,long start, long end){
        File fleIn = new File(input); 
        try {
            if (!fleIn.exists()) {
                LOG.info("Read File Error: Fail to read file: " + input);
            } else {
                InputStream is = new BufferedInputStream(new FileInputStream(input));
                if (end <= 0) {
                    end = fleIn.length();
                }
                if (start <= 0) {
                    start = 0;
                }
                LimitInputStream cis = new LimitInputStream(is, end);
                cis.skip(start);
                
                if(fleIn.getName().endsWith(".gz")){
                    return new GZIPInputStream(cis);
                }else{
                    return cis;                 
                }

            }
        } catch (FileNotFoundException ex) {
            LOG.error(ex.getMessage());
        } catch (IOException ex) {
            LOG.error(ex.getMessage());
        }
        return null;        
      
    }

    public static BufferedOutputStream getOutputStream(String output){
        File fleOut = new File(output); 
        try {
            if (!fleOut.exists()) fleOut.getParentFile().mkdirs();;
           
            if (output.endsWith("gz")) {
                return new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(output)));
            } else {
                return new BufferedOutputStream(new FileOutputStream(output));
            }           
        } catch (FileNotFoundException ex) {
            LOG.error(ex.getMessage());
        } catch (IOException ex) {
            LOG.error(ex.getMessage());
        }
        return null;   
        
    }
    
    public static Aligner getAligner(String tool,String in,String out) {
        if(null == tool)  return null;
        else switch (tool) {
            case "bowtie":
                return new Aligner_BT(in,out);
            case "bowtie2":
                return new Aligner_BT2(in,out);
            case "star":
                return new Aligner_STAR(in,out);
            default:
                return null;
        }
    }

    public static IPTR getInterpreter(String strFileName) {
        if(strFileName.contains("_miRNA"))  return new IPTR_miRNA(strFileName);
        else if(strFileName.contains("_pre-miRNA")) return new IPTR_pre_miRNA(strFileName);
        else if(strFileName.contains("_piRNA")) return new IPTR_piRNA(strFileName);
        else if(strFileName.contains("_snRNA")) return new IPTR_snRNA(strFileName);
        else if(strFileName.contains("_snoRNA")) return new IPTR_snoRNA(strFileName);
        else if(strFileName.contains("_tRNA")) return new IPTR_tRNA(strFileName);
        else if(strFileName.contains("_circRNA")) return new IPTR_circRNA(strFileName);
        else if(strFileName.contains("_gencode"))   return new IPTR_GENCODE(strFileName);
        else return null;
                
    }

    public static MicTool getMicTool(String strTool, String strIn, String strOut) {
        if("metaphlan".equals(strTool.toLowerCase()))    return new MetaPhlAn(strIn,strOut);
        if("blast".equals(strTool.toLowerCase()))    return new Blast(strIn,strOut);
        else return null;
    }
    
    public static DB getDB(String strName, boolean boolCR){
        switch(strName){
            case "miRBase": return new DB_miRBase(strName,boolCR);   
            case "miRBase_pre": return new DB_pre_miRBase(strName,boolCR);
            case "piRNABank": return new DB_piRNABank(strName,boolCR);                           
            case "piRBase": return new DB_piRBase(strName,boolCR);                            
            case "piRNACluster": return new DB_piRNACluster(strName,boolCR);                           
            case "GtRNAdb": return new DB_GtRNAdb(strName,boolCR);
            case "circBase": return new DB_circBase(strName,boolCR);
            case "GENCODE": return new DB_GENCODE(strName,boolCR);
            case "GEN_snoRNA": return new DB_GEN_snoRNA(strName,boolCR);
            case "GEN_snRNA": return new DB_GEN_snRNA(strName,boolCR);
            case "ENS_snRNA": return new DB_Ensembl_snRNA(strName,boolCR);
            default: return null;
        }
    }
    
    public static Genome getGenome(String strGenome){
        switch(strGenome){
            case "hg38": return new HumanGenome(strGenome);
            default: return null;
        }
    }
}
