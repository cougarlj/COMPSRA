/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.toolkit;

import edu.harvard.channing.compass.core.Configuration;
import edu.harvard.channing.compass.utility.Download;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This Class is used to Download the resource for COMPASS independently. 
 * @author Jiang Li
 * @version 1.0 
 * @since 2018-09-11
 */
public class DownloadResource implements ToolKit{
    private static final Logger LOG = LogManager.getLogger(DownloadResource.class.getName());
    public String strResource;
    public boolean boolListResource=false;
    /**
     * This HashMap can map from user resource key to a list of local resource address. 
     */
    HashMap<String,ArrayList<Download>> hmpResource;
//    HashMap<String,ArrayList<String>> hmpResource;    
    ArrayList<Download> altDownload;


    
    

    @Override
    public int runKit() {
        buildMap();
        if (boolListResource) {
            this.listResource();
        } else {
            parseResource();
            downloadResource();
        }
        return 1;
    }
    /**
     * This Function is used to package resources.
     */
    public void buildMap(){
        hmpResource=new HashMap();
        
        //For annotation module.
        ArrayList<Download> altTmp=new ArrayList();
        altTmp.add(new Download(Configuration.BUNDLE_LOC+Configuration.hmpEndoDatabase.get("01010102").strLeafObj.split(";")[0],false));//miRBase_hg19
        hmpResource.put("miRNA_hg19", altTmp);       
        altTmp=new ArrayList();
        altTmp.add(new Download(Configuration.BUNDLE_LOC+Configuration.hmpEndoDatabase.get("01010101").strLeafObj.split(";")[1],false));//miRBase_hg38
        hmpResource.put("miRNA_hg38", altTmp);
        altTmp=new ArrayList();
        altTmp.add(new Download(Configuration.BUNDLE_LOC+Configuration.hmpEndoDatabase.get("01010202").strLeafObj.split(";")[0],false));//miRBase_mm9
        hmpResource.put("miRNA_mm9", altTmp);        
        altTmp=new ArrayList();
        altTmp.add(new Download(Configuration.BUNDLE_LOC+Configuration.hmpEndoDatabase.get("01010201").strLeafObj.split(";")[1],false));//miRBase_mm10
        hmpResource.put("miRNA_mm10", altTmp);    
        altTmp=new ArrayList();
        altTmp.add(new Download(Configuration.BUNDLE_LOC+Configuration.hmpEndoDatabase.get("01010302").strLeafObj.split(";")[0],false));//miRBase_rno5
        hmpResource.put("miRNA_rno5", altTmp);        
        altTmp=new ArrayList();
        altTmp.add(new Download(Configuration.BUNDLE_LOC+Configuration.hmpEndoDatabase.get("01010301").strLeafObj.split(";")[1],false));//miRBase_rno6
        hmpResource.put("miRNA_rno6", altTmp);         

        altTmp=new ArrayList();
        altTmp.add(new Download(Configuration.BUNDLE_LOC+Configuration.hmpEndoDatabase.get("02010101").strLeafObj.split(";")[0],false));//piRNABank_hg18 -> piRNABank_hg19
        altTmp.add(new Download(Configuration.BUNDLE_LOC+Configuration.hmpEndoDatabase.get("02020101").strLeafObj.split(";")[0],false));//pirBase_hg19       
        altTmp.add(new Download(Configuration.BUNDLE_LOC+Configuration.hmpEndoDatabase.get("02030101").strLeafObj.split(";")[0],false));//piRNACluster_hg38 -> piRNACluster_hg19
        hmpResource.put("piRNA_hg19", altTmp);
        altTmp=new ArrayList();
        altTmp.add(new Download(Configuration.BUNDLE_LOC+Configuration.hmpEndoDatabase.get("02010101").strLeafObj.split(";")[1],false));//piRNABank_hg18 -> piRNABank_hg38
        altTmp.add(new Download(Configuration.BUNDLE_LOC+Configuration.hmpEndoDatabase.get("02020101").strLeafObj.split(";")[1],false));//piRBase_hg19 -> piRBase_hg38
        altTmp.add(new Download(Configuration.BUNDLE_LOC+Configuration.hmpEndoDatabase.get("02030101").strLeafObj.split(";")[1],false));//piRNACluster_hg38        
        hmpResource.put("piRNA_hg38", altTmp);
        altTmp=new ArrayList();
        altTmp.add(new Download(Configuration.BUNDLE_LOC+Configuration.hmpEndoDatabase.get("02010201").strLeafObj.split(";")[0],false));//piRNABank_mm8 -> piRNABank_mm9
        altTmp.add(new Download(Configuration.BUNDLE_LOC+Configuration.hmpEndoDatabase.get("02020201").strLeafObj.split(";")[0],false));//pirBase_mm9       
        altTmp.add(new Download(Configuration.BUNDLE_LOC+Configuration.hmpEndoDatabase.get("02030201").strLeafObj.split(";")[0],false));//piRNACluster_mm10 -> piRNACluster_mm9
        hmpResource.put("piRNA_mm9", altTmp);
        altTmp=new ArrayList();
        altTmp.add(new Download(Configuration.BUNDLE_LOC+Configuration.hmpEndoDatabase.get("02010201").strLeafObj.split(";")[1],false));//piRNABank_mm8 -> piRNABank_mm10
        altTmp.add(new Download(Configuration.BUNDLE_LOC+Configuration.hmpEndoDatabase.get("02020201").strLeafObj.split(";")[1],false));//piRBase_mm9 -> piRBase_mm10       
        altTmp.add(new Download(Configuration.BUNDLE_LOC+Configuration.hmpEndoDatabase.get("02030201").strLeafObj.split(";")[1],false));//piRNACluster_mm10        
        hmpResource.put("piRNA_mm10", altTmp);

        
        altTmp=new ArrayList();
        altTmp.add(new Download(Configuration.BUNDLE_LOC+Configuration.hmpEndoDatabase.get("03010101").strLeafObj.split(";")[0],false));//GtRNAdb_hg19       
        hmpResource.put("tRNA_hg19", altTmp);   
        altTmp=new ArrayList();
        altTmp.add(new Download(Configuration.BUNDLE_LOC+Configuration.hmpEndoDatabase.get("03010101").strLeafObj.split(";")[1],false));//GtRNAdb_hg19 -> GtRNAdb_hg38         
        hmpResource.put("tRNA_hg38", altTmp);
        altTmp=new ArrayList();
        altTmp.add(new Download(Configuration.BUNDLE_LOC+Configuration.hmpEndoDatabase.get("03010201").strLeafObj.split(";")[0],false));//GtRNAdb_mm9       
        hmpResource.put("tRNA_mm9", altTmp);  
        altTmp=new ArrayList();
        altTmp.add(new Download(Configuration.BUNDLE_LOC+Configuration.hmpEndoDatabase.get("03010201").strLeafObj.split(";")[1],false));//GtRNAdb_mm9 -> GtRNAdb_mm10         
        hmpResource.put("tRNA_mm10", altTmp);

        
        altTmp=new ArrayList();
        altTmp.add(new Download(Configuration.BUNDLE_LOC+Configuration.hmpEndoDatabase.get("04010102").strLeafObj.split(";")[0],false));//GENCODE_hg19
        hmpResource.put("snoRNA_hg19", altTmp);       
        altTmp=new ArrayList();
        altTmp.add(new Download(Configuration.BUNDLE_LOC+Configuration.hmpEndoDatabase.get("04010101").strLeafObj.split(";")[1],false));//GENCODE_hg38
        hmpResource.put("snoRNA_hg38", altTmp);        
        altTmp=new ArrayList();
        altTmp.add(new Download(Configuration.BUNDLE_LOC+Configuration.hmpEndoDatabase.get("04010202").strLeafObj.split(";")[0],false));//GENCODE_mm9
        hmpResource.put("snoRNA_mm9", altTmp);       
        altTmp=new ArrayList();
        altTmp.add(new Download(Configuration.BUNDLE_LOC+Configuration.hmpEndoDatabase.get("04010201").strLeafObj.split(";")[1],false));//GENCODE_mm10
        hmpResource.put("snoRNA_mm10", altTmp);  
        
        
        altTmp=new ArrayList();
        altTmp.add(new Download(Configuration.BUNDLE_LOC+Configuration.hmpEndoDatabase.get("05010102").strLeafObj.split(";")[0],false));//GENCODE_hg19
        hmpResource.put("snRNA_hg19", altTmp);       
        altTmp=new ArrayList();
        altTmp.add(new Download(Configuration.BUNDLE_LOC+Configuration.hmpEndoDatabase.get("05010101").strLeafObj.split(";")[1],false));//GENCODE_hg38
        hmpResource.put("snRNA_hg38", altTmp);  
        altTmp=new ArrayList();
        altTmp.add(new Download(Configuration.BUNDLE_LOC+Configuration.hmpEndoDatabase.get("05010202").strLeafObj.split(";")[0],false));//GENCODE_mm9
        hmpResource.put("snRNA_mm9", altTmp);       
        altTmp=new ArrayList();
        altTmp.add(new Download(Configuration.BUNDLE_LOC+Configuration.hmpEndoDatabase.get("05010201").strLeafObj.split(";")[1],false));//GENCODE_mm10
        hmpResource.put("snRNA_mm10", altTmp);
        
                
        altTmp=new ArrayList();
        altTmp.add(new Download(Configuration.BUNDLE_LOC+Configuration.hmpEndoDatabase.get("06010101").strLeafObj.split(";")[0],false));//circBase_hg19       
        hmpResource.put("circRNA_hg19", altTmp);    
        altTmp=new ArrayList();
        altTmp.add(new Download(Configuration.BUNDLE_LOC+Configuration.hmpEndoDatabase.get("06010101").strLeafObj.split(";")[1],false));//circBase_hg19 -> circBase_hg38              
        hmpResource.put("circRNA_hg38", altTmp);
        altTmp=new ArrayList();
        altTmp.add(new Download(Configuration.BUNDLE_LOC+Configuration.hmpEndoDatabase.get("06010201").strLeafObj.split(";")[0],false));//circBase_mm9       
        hmpResource.put("circRNA_mm9", altTmp);     
        altTmp=new ArrayList();
        altTmp.add(new Download(Configuration.BUNDLE_LOC+Configuration.hmpEndoDatabase.get("06010201").strLeafObj.split(";")[1],false));//circBase_mm9 -> circBase_mm10         
        hmpResource.put("circRNA_mm10", altTmp);     
        
        //For Alignment Module.
        altTmp=new ArrayList();
        altTmp.add(new Download(Configuration.BUNDLE_LOC+ "/plug/star/" + Configuration.STAR,true));
        hmpResource.put("star", altTmp);
        altTmp=new ArrayList();
        altTmp.add(new Download(Configuration.STAR_REF.get("star_hg19")+"/hg19.fa.gz",true));
        hmpResource.put("star_hg19", altTmp);        
        altTmp=new ArrayList();
        altTmp.add(new Download(Configuration.STAR_REF.get("star_hg38")+"/hg38.fa.gz",true));
        hmpResource.put("star_hg38", altTmp);
        altTmp=new ArrayList();
        altTmp.add(new Download(Configuration.STAR_REF.get("star_mm9")+"/mm9.fa.gz",false));
        hmpResource.put("star_mm9", altTmp);
        altTmp=new ArrayList();
        altTmp.add(new Download(Configuration.STAR_REF.get("star_mm10")+"/mm10.fa.gz",false));
        hmpResource.put("star_mm10", altTmp);
        altTmp=new ArrayList();
        altTmp.add(new Download(Configuration.STAR_REF.get("star_rno5")+"/rn5.fa.gz",true));
        hmpResource.put("star_rno5", altTmp);        
        altTmp=new ArrayList();
        altTmp.add(new Download(Configuration.STAR_REF.get("star_rno6")+"/rn6.fa.gz",true));
        hmpResource.put("star_rno6", altTmp);
        
        //For Microbe Module.
        altTmp=new ArrayList();
        altTmp.add(new Download(Configuration.BUNDLE_LOC+"/plug/blast/" + Configuration.BLAST,true));        
        hmpResource.put("blast", altTmp);
        altTmp=new ArrayList();
        altTmp.add(new Download(Configuration.BUNDLE_LOC+"/db/nt/nt_archaea.fasta.gz",true));        
        hmpResource.put("blast_archaea", altTmp);         
        altTmp=new ArrayList();
        altTmp.add(new Download(Configuration.BUNDLE_LOC+"/db/nt/nt_bacteria.fasta.gz",true));        
        hmpResource.put("blast_bacteria", altTmp); 
        altTmp=new ArrayList();
        altTmp.add(new Download(Configuration.BUNDLE_LOC+"/db/nt/nt_fungi.fasta.gz",true));        
        hmpResource.put("blast_fungi", altTmp); 
        altTmp=new ArrayList();
        altTmp.add(new Download(Configuration.BUNDLE_LOC+"/db/nt/nt_viruses.fasta.gz",true));        
        hmpResource.put("blast_viruses", altTmp); 
        altTmp=new ArrayList();
        altTmp.add(new Download(Configuration.BUNDLE_LOC+"/db/nt/names.dmp",false));//names    
        altTmp.add(new Download(Configuration.BUNDLE_LOC+"/db/nt/nodes.dmp",false));//nodes
        altTmp.add(new Download(Configuration.BUNDLE_LOC+"/prebuilt_db/acc2tax.obj",false));//A2T
        altTmp.add(new Download(Configuration.BUNDLE_LOC+"/prebuilt_db/nt_genome.obj",false));//acc
        altTmp.add(new Download(Configuration.BUNDLE_LOC+"/prebuilt_db/taxonomy.obj",false));//taxonomy
        hmpResource.put("blast_taxonomy", altTmp); 
        
        //for test purpose
        altTmp=new ArrayList();
        altTmp.add(new Download("E:\\00Temp\\test",false));
        hmpResource.put("test", altTmp);
    }
    
    public void parseResource(){
        altDownload=new ArrayList();
        if(this.strResource==null){
            LOG.error("No resource was set!");
            return;
        }
        String[] strItems=this.strResource.split(",|;");
        for(String strItem:strItems){
            for(Download dldObject:this.hmpResource.get(strItem)){
                this.altDownload.add(dldObject);
            }            
        }
    }
    
    public void downloadResource(){
        for(Download dldObject:this.altDownload){
            dldObject.download();
        }
    }
    
    public void listResource(){
        Iterator<Map.Entry<String,ArrayList<Download>>> itrResource=this.hmpResource.entrySet().iterator();
        while(itrResource.hasNext()){
            Map.Entry<String,ArrayList<Download>> entry=itrResource.next();
            for(Download dld:entry.getValue()){
                System.out.println(entry.getKey()+" --> "+dld.strTargetURL);
            }
        }
    }
    
}
