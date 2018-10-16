/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.core.mic;

import edu.harvard.channing.compass.core.aln.Aligner_BT2;
import edu.harvard.channing.compass.entity.BioNode;
import edu.harvard.channing.compass.core.Configuration;
import edu.harvard.channing.compass.utility.ReadFile;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.ValidationStringency;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class is used to call MetaPhlAn.
 * @author Jiang Li
 * @version 1.0
 * @since 2017-10-15
 */
public class MetaPhlAn extends MicTool{
    private final static Logger LOG=LogManager.getLogger(MetaPhlAn.class.getName());
    
    public String strName="life";
    public HashMap<String, Marker> marker;
    public BioNode domain;
    public HashMap<String,BioNode> clades;
    public boolean avoid_disqm=true;


    public MetaPhlAn(String strIn, String strOut) {
        this.strInput=strIn;
        this.strOutput=strOut;
        this.strSAMout=this.strInput.split("\\.")[0]+"_bowtie2"+".sam";
        this.marker=new HashMap<String,Marker>();      
        this.clades=new HashMap<String,BioNode>();
    }
    
    public void buildPheloTree(){
        this.domain=ReadFile.buildTree_MetaPhlAn(clades);
//        this.domain=ReadFile.readMarker(this.marker);
        this.marker=ReadFile.readMarker();
        this.mapMarker2Tree();
    }
    
    public void mapMarker2Tree() {

        for(Marker marker : this.marker.values()){
            this.addMarker2Tree(marker.taxon,marker,this.domain);
        }
    }

    public void addMarker2Tree(String taxon,Marker m,BioNode bnd){
        String[] cludes=taxon.split("\\|");
        if(cludes.length==1){
            BioNode bndTmp=bnd.children.get(cludes[0]);
            while(bndTmp.children.size()==1){
                bndTmp=new ArrayList<BioNode>(bndTmp.children.values()).get(0);
            }
            bndTmp.marker.add(m);            
        }else{
            BioNode bndTmp=bnd.children.get(cludes[0]);
            this.addMarker2Tree(taxon.substring(cludes[0].length()+1),m,bndTmp);
        }
    }
    
    public void mapRead2DB(){
        try {
            //run bowtie2 part. Take strInput as input file.
            //strSAMout=***
            Aligner_BT2 bt2=new Aligner_BT2(this.strFQ,this.strSAMout);
            bt2.setParam("--sam-no-hd --sam-no-sq --no-unal --very-sensitive");
            bt2.setRefDB(Configuration.METAPHLAN.get("db"));
            bt2.setCommand();
            bt2.call();
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
        }       
    }
    
    public boolean mapRead2Tree(){
        try {
//            SamReader sr=SamReaderFactory.makeDefault().open(new File(strSAMout));
            SamReader sr=SamReaderFactory.make().validationStringency(ValidationStringency.SILENT).open(new File(strSAMout));
            for (SAMRecord srdRead : sr){
                if(srdRead.getMappingQuality()<this.mapq)   continue;             
                if(!this.marker.containsKey(srdRead.getContig()))  continue;
                this.marker.get(srdRead.getContig()).hit++;
                this.intCount++;
            }          
            sr.close();
        } catch (IOException ex) {
            LOG.error(ex.getMessage());
            return false;
        }
        return true;
    }    

    public void assignReads() {
        this.domain.setCount();
    }
    
    public boolean writeReport(){
        try {
            BufferedWriter br = null;
            br = new BufferedWriter(new FileWriter(strOutput));
            br.write(this.domain.makeRecord());
            br.close();
        } catch (IOException ex) {
            LOG.error(ex.getMessage());
            return false;
        }
        return true;
    }

//    public void avoidDisqm(BioNode bnd){
//        if(bnd.children.size()==1){
//            
//        }else{
//            
//        }
//    }
    
    public void evlAbundance() {
        this.domain.evlAbundance(avoid_disqm, clades);
    }
    
    public void evlRtvAbundance(){
        this.domain.evlRtvAbundance(this.domain.abundance);
    }
    
    @Override
    public Object call() throws Exception {
        this.buildPheloTree();
        this.prepareFQ();
        this.mapRead2DB();
        this.mapRead2Tree();
//        if(this.avoid_disqm)    this.avoidDisqm(this.domain);
        this.evlAbundance();
        this.evlRtvAbundance();
        this.assignReads();
        this.writeReport();
        return "MetaPhlAn Microbe Module was completed!";
    }
     
 
    public static void main(String[] args) {
        Configuration con=new Configuration();
//        String strSAMout="E:\\01Work\\miRNA\\project\\CircuRNA\\output\\S-001570893_CGATGT_L001_R1_17to50_FitRead_UnMapped_bowtie2.sam";
//        String strOutput="E:\\01Work\\miRNA\\project\\CircuRNA\\output\\S-001570893_CGATGT_L001_R1_17to50_FitRead_UnMapped_bowtie2_MetaPhlAn.txt";
//        MetaPhlAn life=new MetaPhlAn(strSAMout,strOutput);
//        life.strSAMout="E:\\01Work\\miRNA\\project\\CircuRNA\\output\\HBRNA_AGTCAA_L001_R1_17to50_FitRead_UnMapped_bowtie2.sam";
//        life.strOutput="E:\\01Work\\miRNA\\project\\CircuRNA\\output\\HBRNA_AGTCAA_L001_R1_17to50_FitRead_UnMapped_bowtie2_MetaPhlAn.txt";

//        life.strSAMout="E:\\01Work\\miRNA\\project\\CircuRNA\\output\\S-001570892_ATCACG_L001_R1_17to50_FitRead_UnMapped_bowtie2.sam";
//        life.strOutput="E:\\01Work\\miRNA\\project\\CircuRNA\\output\\S-001570892_ATCACG_L001_R1_17to50_FitRead_UnMapped_bowtie2_MetaPhlAn.txt";

//        life.buildPheloTree();
//        life.mapRead2Tree();
//        life.assignReads();
//        life.writeReport();
        
//        String strSAM="E:\\01Work\\miRNA\\project\\CircuRNA\\output\\HBRNA_AGTCAA_L001_R1_17to50_FitRead_part.sam";
////        String strFQ="E:\\01Work\\miRNA\\project\\CircuRNA\\output\\HBRNA_AGTCAA_L001_R1_17to50_FitRead_part.fastq";
//        String strOutput="E:\\01Work\\miRNA\\project\\CircuRNA\\output\\S-001570893_CGATGT_L001_R1_17to50_FitRead_UnMapped_bowtie2_MetaPhlAn.txt";
//        MetaPhlAn life=new MetaPhlAn(strSAM,strOutput);
//        try {
//            life.call();
//        } catch (Exception ex) {
//            LogManager.getLogger(MetaPhlAn.class.getName()).log(Level.SEVERE, null, ex);
//        }
////        life.samTofq(strSAM, strFQ, 2);
        String strSAM="E:\\01Work\\miRNA\\project\\COMPASS\\output\\S-001570893_CGATGT_L001_R1_17to50_FitRead_STAR_Aligned_UnMapped.fastq";
        String strOutput="E:\\01Work\\miRNA\\project\\COMPASS\\output\\S-001570893_metaphlan.txt";      
        MetaPhlAn mpla=new MetaPhlAn(strSAM,strOutput);
        mpla.buildPheloTree();
        mpla.mapRead2Tree();
        mpla.evlAbundance();
        mpla.evlRtvAbundance();
        mpla.assignReads();
        mpla.writeReport();


        System.out.println("HeHeDa!");
        
        
        
        
        
    }        



}
