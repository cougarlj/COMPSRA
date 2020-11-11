/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.toolkit;

import edu.harvard.channing.compass.core.Configuration;
import edu.harvard.channing.compass.core.Factory;
import edu.harvard.channing.compass.db.DB;
import edu.harvard.channing.compass.entity.DBLeaf;
import edu.harvard.channing.compass.entity.DBTree;
import edu.harvard.channing.compass.entity.SNP;
import edu.harvard.channing.compass.utility.ReadFile;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import org.apache.commons.math3.util.CombinatoricsUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * BuildMutantDB will assist researcher to build a mutant miRNA database.
 * @author Jiang Li
 * @version 1.2 
 * @since 2020-10-25
 */
public class BuildMutantDB implements ToolKit{
    private static final Logger LOG = LogManager.getLogger(BuildMutantDB.class.getClass());
    
    public String strFa;
    public String strVCF;
    public String strGFF3;
    public String strOut;
    public boolean boolU2T=true;
    
    public LinkedHashMap<String,String> lhmpSeq;
    public DBTree dbt;
    public ArrayList<String> altFA;
    public LinkedHashMap<String,ArrayList<SNP>> lhmpSNP;
    public LinkedHashMap<String,String> lhmpIndex;
    
    public void BuildSeqMap() {
        File fleFa=new File(this.strFa);
        if(!fleFa.exists()){
            LOG.error("File doesn't exist : "+this.strFa);
            System.exit(0);           
        }
        
        this.lhmpSeq=new LinkedHashMap();
        BufferedReader br=Factory.getReader(this.strFa);
        String strLine;
        try {
            strLine = br.readLine().trim();
            while(strLine!=null){
                if(strLine.startsWith(">")){
                    String strID=strLine.split(" ")[0].substring(1).trim();
                    String strSeq=br.readLine().trim();
                    if(this.boolU2T){
                        this.lhmpSeq.put(strID, strSeq.replace("U", "T")); 
                    }else{
                        this.lhmpSeq.put(strID, strSeq); 
                    }                     
                }
                strLine=br.readLine();
            }
        } catch (Exception ex) {
            LOG.error(ex.getMessage().toString());
            LOG.error("Fail to read file: "+this.strFa);
        }      
    }
    

    public void BuildDBTree(){
        try{
            this.dbt=ReadFile.readGFF3(strGFF3);
            DB db=Factory.getDB("miRBase", false);
            db.pruneDB(this.dbt);        
        }catch(Exception ex){
            LOG.error("Fail to read file: "+this.strGFF3);
            LOG.error(ex.getMessage().toString());
        }              
    }
    
    public void MakeReferenceFa(){
        this.altFA=new ArrayList();
        for(Entry <String,String> entry:this.lhmpSeq.entrySet()){
            this.altFA.add(">"+entry.getKey()+"\n"+entry.getValue());
        }       
    }

    
    public void CollectSNP(){
        this.lhmpSNP=new LinkedHashMap<>();
        BufferedReader br = Factory.getReader(this.strVCF);
        String strLine;
        try{
            strLine = br.readLine().trim();
            while(strLine!=null){
                if(strLine.startsWith("#")){
                    strLine=br.readLine();
                }else{
                    String[] strItem=strLine.split("\t");                                      
                    String chrom=strItem[0].trim();
                    int pos=Integer.valueOf(strItem[1].trim());
                    
                    ArrayList <DBLeaf> altDBLeaf=new ArrayList();
                    dbt.findLeaf(chrom, pos,altDBLeaf);
                    
                    if(altDBLeaf==null || altDBLeaf.size()==0){
                        LOG.info("The SNP "+chrom+":"+pos+" isn't covered in the GFF3 file!");
                    }else{
                        for(int i=0;i<altDBLeaf.size();i++){
                            DBLeaf dbf=altDBLeaf.get(i);
                            
                            if(this.lhmpSeq.containsKey(dbf.name)){
                                SNP snp=new SNP(chrom,pos,strItem[3],strItem[4],dbf); 
                                if (this.lhmpSNP.containsKey(dbf.name+"_"+dbf.start)) {                                                                       
                                    this.lhmpSNP.get(dbf.name+"_"+dbf.start).add(snp);
                                } else {
                                    ArrayList<SNP> altSNP=new ArrayList();
                                    altSNP.add(snp);
                                    this.lhmpSNP.put(dbf.name+"_"+dbf.start, altSNP);
                                }                               
                            }else{                                
                                LOG.info("Sequence doesn't exist for "+dbf.name+"."); 
                            }                           
                        }                        
                    }                    
                    strLine=br.readLine();
                }
            }     
        }catch (IOException ex) {
            LOG.error("Fail to read file: "+this.strVCF);
        } 
        
    }
    
    public void MakeMutantFa() {
        this.lhmpIndex=new LinkedHashMap();
        for (Entry<String, ArrayList<SNP>> entry : this.lhmpSNP.entrySet()) {
            String strID_RAW=entry.getKey();
            String strID=strID_RAW.split("_")[0];
            ArrayList<SNP> altSNPs=entry.getValue();
//            if(strID.startsWith("hsa-miR-5192")){
//                System.out.println("Check Point:2!");
//            }
            int intCount=altSNPs.size();
            for(int i=1;i<=intCount;i++){
                Iterator <int[]> itrComb=CombinatoricsUtils.combinationsIterator(intCount, i);
                int[] intComb;
                while(itrComb.hasNext()){
                    intComb=itrComb.next();
                    ArrayList<SNP> altTmp=new ArrayList();
                    StringBuilder sbCode=new StringBuilder();
                    StringBuilder sbValue=new StringBuilder();
                    for(int j=0;j<intComb.length;j++){
                        SNP snpTmp=altSNPs.get(intComb[j]);
                        altTmp.add(snpTmp);
                        
                        sbCode.append(intComb[j]);
                        sbCode.append(":");
                        
                        sbValue.append(snpTmp.chrom+":"+snpTmp.pos+":"+snpTmp.ref+":"+snpTmp.alt+":"+snpTmp.dbf.strand);
                        sbValue.append("_");
                    }
                    
                    sbCode.deleteCharAt(sbCode.length()-1);
                    sbValue.deleteCharAt(sbValue.length()-1);
                    
                    String strSeq=this.MakeMutantSeq(strID, altTmp);
                    String strOutFa=">" + strID_RAW + "_(" + sbCode.toString() + ")\n" + strSeq;                   
                    this.altFA.add(strOutFa);      
                    this.lhmpIndex.put(strID_RAW + "_(" + sbCode.toString() + ")", strID+"_"+sbValue.toString());
                    System.out.println(strOutFa);
                }               
            }   
        }        
    }
    
    public void OutputMutantFa() {
        BufferedWriter bw = Factory.getWriter(this.strOut);
        BufferedWriter bw2=Factory.getWriter(this.strOut+".map");
        try {
            for (int i = 0; i < this.altFA.size(); i++) {
                bw.write(this.altFA.get(i));
                bw.newLine();
            }
            bw.close();
            
            for(Entry<String, String> entry : this.lhmpIndex.entrySet()){
                bw2.write(entry.getKey()+"\t"+entry.getValue());
                bw2.newLine();
            }
            bw.close();
            
        } catch (IOException ex) {
            LOG.error(ex.getMessage().toString());
        }
        
    }
    
    public String MakeMutantSeq(String strName, ArrayList<SNP> altCandidate) {   
        
        StringBuilder sb = new StringBuilder(this.lhmpSeq.get(strName));
       
        for (int i = 0; i < altCandidate.size(); i++) {
            SNP snp=altCandidate.get(i);
            DBLeaf dbf=altCandidate.get(i).dbf;
            
            char chrRef_VCF = snp.ref.charAt(0);
            char chrAlt_VCF = snp.alt.charAt(0);

            if (dbf.strand.equals("+")) {
                int intLoc = snp.pos - dbf.start;
                char chrRef_DB = sb.charAt(intLoc);
                if (chrRef_DB == chrRef_VCF) {
//                                        System.out.println(dbf.start+"--"+pos+"--"+dbf.end+"--"+intLoc);                  
                    sb.setCharAt(intLoc, chrAlt_VCF);
//                    String strNewRNA = ">" + dbf.name + "_" + snp.chrom + ":" + snp.pos + ":" + chrRef_VCF + ":" + chrAlt_VCF + ":" + dbf.strand + "\n" + sb.toString();
//                    this.altFA.add(strNewRNA);
//                                        System.out.println(strNewRNA);
                } else {
                    LOG.warn(dbf.name + "_" + snp.chrom + ":" + snp.pos + ":" + chrRef_VCF + ":" + chrAlt_VCF + " --> Reference allele between miRBase (" + chrRef_DB + ") and genome (" + chrRef_VCF + ") is not equal!");
                    return null;
                }
            } else {
                int intLoc = dbf.end - snp.pos;
                char chrRef_DB = sb.charAt(intLoc);
                if (Configuration.CBP.get(chrRef_DB) == chrRef_VCF) {
                    sb.setCharAt(intLoc, Configuration.CBP.get(chrAlt_VCF));
//                    String strNewRNA = ">" + dbf.name + "_" + snp.chrom + ":" + snp.pos + ":" + chrRef_VCF + ":" + chrAlt_VCF + ":" + dbf.strand + "\n" + sb.toString();
//                    this.altFA.add(strNewRNA);
//                                        System.out.println(strNewRNA);
                } else {
                    LOG.warn(dbf.name + "_" + snp.chrom + ":" + snp.pos + ":" + chrRef_VCF + ":" + chrAlt_VCF + " --> Reference allele between miRBase (" + Configuration.CBP.get(chrRef_DB) + ") and genome (" + chrRef_VCF + ") is not equal!");
                    return null;
                }
            }
        }
        return sb.toString();
    }
    
    @Override
    public int runKit() {
        this.BuildSeqMap();
        this.BuildDBTree();
        this.MakeReferenceFa();
        this.CollectSNP();
        this.MakeMutantFa();
        this.OutputMutantFa();
        return 1;
    }

    public static void main(String[] argv){
        Configuration config=new Configuration();
        BuildMutantDB bmdb=new BuildMutantDB();
        bmdb.strFa="E:\\01Work\\ASE\\result\\result_v26\\miRBase_V22_mature_hsa_regular.fa";
        bmdb.strVCF="E:\\01Work\\ASE\\result\\result_v26\\CRA_Mature_newID_sort_snp_het_clean_pass.vcf";
        bmdb.strGFF3="E:\\01Work\\ASE\\result\\result_v26\\hsa.gff3";
        bmdb.strOut="E:\\01Work\\ASE\\result\\result_v26\\miRBase_V22_mature_hsa_mutant.fa";
        bmdb.runKit();

    }
    
    
}
