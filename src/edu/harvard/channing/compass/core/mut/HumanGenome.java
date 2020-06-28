/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.core.mut;

import edu.harvard.channing.compass.core.Configuration;
import edu.harvard.channing.compass.core.Factory;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class is used to fulfill the human genome.
 * @author Jiang Li
 * @version 1.0 
 * @since 2018-11-25
 */
public class HumanGenome extends Genome{
    private static final Logger LOG = LogManager.getLogger(HumanGenome.class.getClass());
    
    public HumanGenome(String strID) {
        super(strID);
    }

    public HumanGenome() {
        super();
    }

    @Override
    public boolean buildGenome() {
        try {
            File fleFasta=new File(this.strFasta);
            if(!fleFasta.exists()){
                LOG.error("The fasta file "+this.strFasta+" doesn't exist!");
                return false;
            }
            this.hmpGenome=new HashMap();
            BufferedReader br=Factory.getReader(this.strFasta);
            String strLine=null;
            String strContig=null;
            boolean boolFlag=false;
            int intCount=0;
            int intLastPos=this.intStart;
            StringBuffer sb=new StringBuffer();
            while((strLine=br.readLine())!=null){
                if(strLine.startsWith(">")){
                    if(boolFlag){
                        this.hmpGenome.get(strContig).put(intLastPos,sb.toString().toUpperCase());
                        sb=new StringBuffer();
                        intCount=0;
                        intLastPos=this.intStart;
                    }
                    strContig=strLine.split("\t|,|;|\\|")[0].substring(1);
                    this.hmpGenome.put(strContig, new LinkedHashMap());
                    boolFlag=true;
                    System.out.println(strContig);
                    continue;
                }
                sb.append(strLine);
                intCount++;
                if(intCount==this.intLines){
                    this.hmpGenome.get(strContig).put(intLastPos,sb.toString().toUpperCase());
                    intLastPos+=sb.length();
                    intCount=0;
                    sb=new StringBuffer();
                }               
            }
            this.hmpGenome.get(strContig).put(intLastPos,sb.toString().toUpperCase());
        } catch (IOException ex) {
            LOG.error(ex.getMessage());
        }
        return true;
    }

    @Override
    public boolean buildGenomeMain() {
        try {
            File fleFasta=new File(this.strFasta);
            if(!fleFasta.exists()){
                LOG.error("The fasta file "+this.strFasta+" doesn't exist!");
                return false;
            }
            this.hmpGenome=new HashMap();
            BufferedReader br=Factory.getReader(this.strFasta);
            String strLine=null;
            String strContig=null;
            boolean boolFlag=false;
            int intCount=0;
            int intLastPos=this.intStart;
            StringBuffer sb=new StringBuffer();
            while((strLine=br.readLine())!=null){              
                if(strLine.startsWith(">")){
                    if(boolFlag){
                        this.hmpGenome.get(strContig).put(intLastPos,sb.toString().toUpperCase());
                        sb=new StringBuffer();
                        intCount=0;
                        intLastPos=this.intStart;
                    }
                    strContig=strLine.split("\t|,|;|\\|")[0].substring(1);
//                    System.out.println(strContig);
                    if(strContig.equals("chr12"))    return true; //Only for test. Becasue the whole genom is too large for my computer. 
                    if(Configuration.UCSC_CONTIG.keySet().contains(strContig)){
                        boolFlag=true;
                    }else{
                        boolFlag=false;
                        continue;
                    }
                    this.hmpGenome.put(strContig, new LinkedHashMap());
                    System.out.println(strContig);
                    continue;
                }
                if (boolFlag) {
                    sb.append(strLine);
                    intCount++;
                    if (intCount == this.intLines) {
                        this.hmpGenome.get(strContig).put(intLastPos, sb.toString().toUpperCase());
                        intLastPos += sb.length();
                        intCount = 0;
                        sb = new StringBuffer();
                    }
                }
             
            }
            if(boolFlag){
                this.hmpGenome.get(strContig).put(intLastPos, sb.toString().toUpperCase());
            }
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public char findBase(String chr, int pos) {
        char base = '.';
        try {
            if (this.hmpGenome == null || this.hmpGenome.isEmpty()) {
//                LOG.error("The genome doesn't exist!");
                return base;
            }

            LinkedHashMap<Integer, String> lhmpContig = this.hmpGenome.get(chr);
            ArrayList<Integer> lstKey = new ArrayList(lhmpContig.keySet());
            int intIdx = Collections.binarySearch(lstKey, pos, cmp);
            if (intIdx < 0) {
                int point = -intIdx - 1 - 1;
                base = lhmpContig.get(lstKey.get(point)).charAt(pos - lstKey.get(point));
            } else if (intIdx == lstKey.size()) {
                base = lhmpContig.get(lstKey.get(intIdx - 1)).charAt(pos - lstKey.get(intIdx));
            } else {
                base = lhmpContig.get(lstKey.get(intIdx)).charAt(pos - lstKey.get(intIdx));
            }

        } catch (Exception ex) {
            LOG.warn("No reference for the position: "+chr+":"+pos);
        }
        return base;
    }
    
    public static void main(String[] argv){
        System.out.print(new Date());
        Configuration config=new Configuration();
        HumanGenome hg=new HumanGenome("hg38");
        hg.setStrFasta("E:\\01Work\\miRNA\\data\\hg38.fa.gz");
//        hg.setStrFasta("E:\\01Work\\miRNA\\data\\hg38_test.fa");
        boolean boolStatus=hg.buildGenomeMain();
        System.out.println();
        for (int i = 10000; i < 10020; i++) {
            char base = hg.findBase("chr1", i); 
            System.out.println(base);
        }//NTAACCCTAACCCTAACCCT
        char base=hg.findBase("chr1", 11111); //hg38:G
        System.out.println(base);
        base=hg.findBase("chr10",11111); //hg38:T
        System.out.println(base);
        System.out.print(new Date());
    }

}
