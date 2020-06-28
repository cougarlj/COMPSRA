/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.core.mut;

import edu.harvard.channing.compass.core.Configuration;
import edu.harvard.channing.compass.core.Factory;
import edu.harvard.channing.compass.entity.DBTree;
import edu.harvard.channing.compass.utility.Download;
import edu.harvard.channing.compass.utility.ReadFile;
import edu.harvard.channing.compass.utility.StringTools;
import htsjdk.samtools.AlignmentBlock;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.ValidationStringency;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * This class is used to pile up the BAM/SAM file.
 * @author Jiang Li
 * @version 1.0 
 * @since 2018-11-27
 */
public class Heap {
    private static final org.apache.logging.log4j.Logger LOG = LogManager.getLogger(Heap.class.getName());
    
    public String strFile;
    public int mapq=0;
    public boolean needClip=false;
    public byte intBasePhred=33;
//    public ArrayList<String> altFile;
    
    public HashMap<String,HashMap<Integer,ArrayList<Byte>>> hmpHeap;
    public Genome gmRef;
    public ArrayList<SNPRecord> altSNP;
    public ArrayList<DBTree> dbt;

    public Heap(String strFile) {
//        altFile=new ArrayList();
//        String[] strFiles=strFile.split(",|;");
//        for(String strItem:strFiles)    altFile.add(strItem);
        this.strFile = strFile;
//        this.gmRef=new HumanGenome();
    }
    
//    public Heap(String strFile,boolean isList){
//        altFile=ReadFile.readFileList(strFile);
//        this.gmRef=new HumanGenome();
//    }
    
    public boolean makeHeap() {
        File fleIn = new File(strFile);
        if (!fleIn.exists()) {
            return false;
        }

        SamReader sr = SamReaderFactory.make().validationStringency(ValidationStringency.LENIENT).open(fleIn);
        for (SAMRecord srdRead : sr) {
            if (srdRead.getReadUnmappedFlag()) {
                continue;
            }

            if (needClip) {
                //Some microRNAs have 3' end modification, but currently we don't focus on this part of work.
            } else {
                List<AlignmentBlock> lstAB = srdRead.getAlignmentBlocks();
                byte[] bytRead = srdRead.getReadBases();
                byte[] bytQuality = srdRead.getBaseQualities();
                for (int i = 0; i < lstAB.size(); i++) {
//                    System.out.println(lstAB.get(i).getReadStart());
//                    System.out.println(lstAB.get(i).getReferenceStart());
//                    System.out.println(lstAB.get(i).getLength());

                    for (int j = 0; j < lstAB.get(i).getLength(); j++) {
//                        System.out.print((char)bytRead[lstAB.get(i).getReadStart()+j-1]);
                        if (bytQuality[lstAB.get(i).getReadStart() + j - 1] < this.intBasePhred) {
                            continue;
                        }
                        this.pileUp(srdRead.getContig(), lstAB.get(i).getReferenceStart() + j, bytRead[lstAB.get(i).getReadStart() + j - 1]);
                    }
//                    System.out.println();
                }
            }
        }
        return true;

    }
    
    public void releaseHeap(){
        hmpHeap.clear();
    }
 
    public void pileUp(String strContig, int intRefPos, byte btyBase) {
        if(this.hmpHeap==null){
            this.hmpHeap=new HashMap();
        }
        if(!this.hmpHeap.keySet().contains(strContig)){
            this.hmpHeap.put(strContig, new HashMap());
        }
        if(!this.hmpHeap.get(strContig).containsKey(intRefPos)){
            this.hmpHeap.get(strContig).put(intRefPos, new ArrayList());
        }
        
        this.hmpHeap.get(strContig).get(intRefPos).add(btyBase);
    }    
    
    public void outputHeap(){
        try {
            String strOut=this.strFile.split("\\.")[0]+".heap";
            BufferedWriter bw=Factory.getWriter(strOut);
            bw.write(SNPRecord.getPileUpHead());
            bw.newLine();
            
            this.altSNP=new ArrayList();
            ArrayList<String> altContig=new ArrayList(this.hmpHeap.keySet());
            Collections.sort(altContig);
            for(int i=0;i<altContig.size();i++){
                String strContig=altContig.get(i);
                ArrayList<Integer> altPos=new ArrayList(this.hmpHeap.get(strContig).keySet());
                Collections.sort(altPos);
                for(int j=0;j<altPos.size();j++){
                    try {
                        int intPos=altPos.get(j);
                        String strBase=StringTools.byte2string(this.hmpHeap.get(strContig).get(intPos));
                        SNPRecord snpr=new SNPRecord(strContig,intPos,this.gmRef.findBase(strContig,intPos),strBase);
                        snpr.setAnn(this.getAnn(strContig, intPos));
                        this.altSNP.add(snpr);
                        bw.write(snpr.getPileUp());
                        bw.newLine();
                    } catch (IOException ex) {
                        LOG.error(ex.getMessage());
                    }
                }
            }
            bw.close();
        } catch (IOException ex) {
            LOG.error(ex.getMessage());
        }
    }
    
    public boolean callVariant(boolean isGVT){
        try {
            if(this.altSNP==null){
                LOG.error("Please pile up the SAM/BAM file first!");
                return false;
            }
            
            String strOut;
            if(isGVT){
                strOut = this.strFile.split("\\.")[0] + ".gvt";
            }else{
                strOut = this.strFile.split("\\.")[0] + ".vt";
            }
            
            BufferedWriter bw = Factory.getWriter(strOut);
            bw.write(SNPRecord.getVariantHead());
            bw.newLine();
            
            for(SNPRecord snpr:this.altSNP){
                if(snpr.callVariant(isGVT)){                  
                        bw.write(snpr.getVariant());
                        bw.newLine();               
                }
            }
            bw.close();           
        } catch (IOException ex) {
            LOG.error(ex.getMessage());
            return false;
        }
        return true;
    }
    
    public void setRefGenome(Genome gmRef) {
        this.gmRef=gmRef;
    }
    
    public void setRefGenome(String strID,String strFasta){
        this.gmRef=Factory.getGenome(strID);
        this.gmRef.setStrFasta(strFasta);
        this.gmRef.buildGenome();
    }
    
    public void setRefGenome(String strID){
        this.gmRef=Factory.getGenome(strID);
        if(this.gmRef.isLoaded())   return;
        File fleRef=new File(Configuration.REF.get(strID));
        if(fleRef.exists()){
            this.gmRef.readGenome(Configuration.REF.get(strID));
        }else{
            Download dd=new Download(Configuration.REF.get(strID));
            boolean boolFlag=dd.download();
            if(boolFlag){
                this.gmRef.readGenome(Configuration.REF.get(strID));
            }else{
                LOG.info("Try to build reference genome from local fasta file");
                File fleFQ=new File(Configuration.REF.get(strID+".cps"));
                if(!fleFQ.exists()){
                    dd=new Download(Configuration.REF.get(strID+".cps"));
                    boolFlag=dd.download();
                    if(!boolFlag)   return;
                }
                this.setRefGenome(strID, Configuration.REF.get(strID+".cps"));
                this.gmRef.writeGenome(Configuration.REF.get(strID));
            }
        }

    }

    public void setDB(ArrayList<DBTree> dbt) {
        this.dbt=dbt;
    }

    public String getAnn(String chrom, int pos) {
        StringBuilder sbAnn = new StringBuilder();

        for (int i = 0; i < this.dbt.size(); i++) {
            String strAnn = dbt.get(i).findLeaf(chrom, pos);
            if (strAnn == null) {
                continue;
            } else {
                sbAnn.append(strAnn);
                sbAnn.append(";");
            }
        }
        
        if(sbAnn.length()==0)   return ".";
        else    return sbAnn.toString();      
    }
    
    public static void main(String[] argv){
        Heap hpTest=new Heap("E:\\01Work\\miRNA\\data\\test_part.sam");
        hpTest.makeHeap();
        hpTest.outputHeap();
        System.out.println();
    }






}
