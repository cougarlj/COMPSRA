/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.entity;

import edu.harvard.channing.compass.utility.StringTools;
import htsjdk.samtools.SAMRecord;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.commons.collections4.CollectionUtils;

/**
 * This class is used to save the database info used. One database one object. 
 * @author Jiang Li
 * @version 1.0 
 * @since 2017-08-30
 */
public class DBTree implements Serializable{
    private static final Logger LOG = LogManager.getLogger(DBTree.class.getName());
    
    public String strDB;
    public HashMap<String,HashMap<Integer,ArrayList<DBLeaf>>> hmpDB;
    public HashMap<String,ArrayList<Integer>> hmpStart;
    public HashMap<String,DBLeaf> hmpMerge;

    public void initTree(String db){
        this.hmpStart=new HashMap();
        this.hmpDB=new HashMap();
        this.hmpMerge=new HashMap();
        this.strDB=db;
    }
    
    public void putLeaf(String key, int pos, DBLeaf dif) {
        if(!this.hmpStart.containsKey(key)){
            this.hmpStart.put(key, new ArrayList());
            this.hmpDB.put(key, new HashMap());
        }
        int idx=Collections.binarySearch(this.hmpStart.get(key), pos);
        if(idx<0)   idx=-idx-1;
        this.hmpStart.get(key).add(idx,pos);
        this.hmpDB.get(key).put(pos, new ArrayList<DBLeaf>());
        this.hmpDB.get(key).get(pos).add(dif);
        dif.db=this.strDB;
    }

    public void mergeLeaf(String key, int idx, DBLeaf dif, String db) {
        int pos=this.hmpStart.get(key).get(idx);
        this.hmpDB.get(key).get(pos).add(dif);
        dif.db=db;
    }
    
    public boolean addLeaf(String key, DBLeaf dif){
        if (!this.hmpStart.containsKey(key)) {
            this.hmpStart.put(key, new ArrayList());
            this.hmpDB.put(key, new HashMap());
        }
        if(this.hmpStart.get(key).isEmpty()){
            this.hmpStart.get(key).add(dif.start);
            this.hmpDB.get(key).put(dif.start, new ArrayList());
            this.hmpDB.get(key).get(dif.start).add(dif);
            return true;
        }else{
            int pos=Collections.binarySearch(this.hmpStart.get(key), dif.start);
            if(pos>=0){
                //Already exist.
                for(int i=0;i<this.hmpDB.get(key).get(dif.start).size();i++){
                    if(this.hmpDB.get(key).get(dif.start).get(i).end==dif.end && this.hmpDB.get(key).get(dif.start).get(i).strand==dif.strand){
                        return false;
                    }
                }
                this.hmpDB.get(key).get(dif.start).add(dif);
                return true;
            }else{
                //Don't exist.
                pos=-pos-1;
                this.hmpStart.get(key).add(pos, dif.start);
                this.hmpDB.get(key).put(dif.start, new ArrayList());
                this.hmpDB.get(key).get(dif.start).add(dif);
                return true;
            }
        }
    }    
    
    public void writeSelf(String strInput) throws IOException{
        File fle=new File(strInput);
        BufferedWriter br=new BufferedWriter(new FileWriter(fle));
        for (String key : this.hmpStart.keySet()) {
            for (int pos : this.hmpStart.get(key)) {
//                br.write("----------------------------------------------------");
                br.newLine();
                for(DBLeaf dif: this.hmpDB.get(key).get(pos)){
                    br.write(dif.toRecord());
                    br.newLine();                    
                }
            }
        }     
    }

    /**
     * This function will be called by Class IPTR. 
     * @param sr
     * @param overlap
     * @param hit
     * @return 
     */
    public boolean findLeaf(SAMRecord sr,float overlap,boolean hit){
        boolean boolHit=false;
        int idx=0;
        
//        if(sr.getStart()==70789509){
//            System.out.println(this.strDB);
//        }
        
        try {
            idx= Collections.binarySearch(this.hmpStart.get(sr.getContig()), sr.getStart());
        } catch (NullPointerException e) {
//            LOG.info("The Contig " + sr.getContig() + " doesn't exist in the database resource file.");
            return false;
        }  
        
        if(idx>=0){
//            if(this.hmpStart.get(sr.getContig()).get(idx)==70789509){
//                System.out.println();
//            }
            boolHit=this.extendLeaf(idx, sr, overlap, hit);
//            sbHit=this.hitLeaf(sr, sr.getStart(), overlap, hit);
            
        }else{
            //Within two branches.
            idx=-idx-1;
            if(idx==0){
//                sbHit|=this.hitLeaf(sr, this.hmpStart.get(sr.getContig()).get(0), overlap, hit);
                boolHit=this.extendLeaf(0, sr, overlap, hit);
            }else if(idx==this.hmpStart.get(sr.getContig()).size()){
//                sbHit|=this.hitLeaf(sr, this.hmpStart.get(sr.getContig()).get(this.hmpStart.get(sr.getContig()).size()-1), overlap, hit);
                boolHit=this.extendLeaf(idx-1, sr, overlap, hit);
            }else{
//                if (this.hmpStart.get(sr.getContig()).get(idx) == 70789509 || this.hmpStart.get(sr.getContig()).get(idx - 1) == 70789509) {
//                    System.out.println();
//                }
                  boolHit=this.extendLeaf(idx, sr, overlap, hit);          
//                sbHit|=this.hitLeaf(sr,this.hmpStart.get(sr.getContig()).get(idx-1),overlap, hit);
//                sbHit|=this.hitLeaf(sr,this.hmpStart.get(sr.getContig()).get(idx),overlap, hit);
            }           
        }
        
        return boolHit;
    }

    public String findLeaf(String chrom,int pos){
        StringBuilder sbHit=new StringBuilder();
        int idx=0;
        
        try {
            idx= Collections.binarySearch(this.hmpStart.get(chrom), pos);
        } catch (NullPointerException e) {
            return null;
        }  
        
        if(idx>=0){
            sbHit.append(this.extendLeaf(chrom,idx,pos));
            sbHit.append(",");
//            sbHit=this.hitLeaf(sr, sr.getStart(), overlap, hit);
            
        } else {
            //Within two branches.
            idx = -idx - 1;
            String strTmp=null;
            if (idx == 0) {
                strTmp=this.extendLeaf(chrom, 0, pos);
                if(strTmp!=null){
                    sbHit.append(strTmp);
                    sbHit.append(",");
                }               
            } else if (idx == this.hmpStart.get(chrom).size()) {
                strTmp=this.extendLeaf(chrom, idx - 1, pos);
                if(strTmp!=null){
                sbHit.append(strTmp);
                sbHit.append(",");}
            } else {
                strTmp=this.extendLeaf(chrom, idx, pos);
                if(strTmp!=null){
                sbHit.append(strTmp);
                sbHit.append(",");}
            }
        }
        
        if(sbHit.length()==0)   return null;
        else    return sbHit.substring(0,sbHit.length()-1);
    }
    
    public void findLeaf(String chrom, int pos, ArrayList<DBLeaf> altDBLeaf) {
        int idx = 0;

        try {
            idx = Collections.binarySearch(this.hmpStart.get(chrom), pos);
        } catch (NullPointerException e) {
            altDBLeaf = null;
            return;
        }

        if (idx >= 0) {
            this.extendLeaf(chrom, idx, pos,altDBLeaf);
        } else {
            //Within two branches.
            idx = -idx - 1;

            if (idx == 0) {
                this.extendLeaf(chrom, 0, pos, altDBLeaf);
            } else if (idx == this.hmpStart.get(chrom).size()) {
                this.extendLeaf(chrom, idx - 1, pos, altDBLeaf);
            } else {
                this.extendLeaf(chrom, idx, pos, altDBLeaf);
            }
        }

    }
 
    
    /**
     * This function will be called by class IPTR_piRNA. 
     * @param dif
     * @param overlap
     * @param hit
     * @return
     */
    public boolean findLeaf(DBLeaf dif,float overlap,boolean hit){
        boolean boolHit=false;
        int idx=0;
        try {
            idx= Collections.binarySearch(this.hmpStart.get(dif.chr), dif.start);
        } catch (NullPointerException e) {
//            LOG.info("The Contig " + dif.chr + " doesn't exist in the database resource file.");
            return false;
        }  
        
        if(idx>=0){
            boolHit=this.hitLeaf(dif, dif.start, overlap, hit);
        }else{
            //Within two branches.
            idx=-idx-1;
            if(idx==0){
                boolHit|=this.hitLeaf(dif, this.hmpStart.get(dif.chr).get(0), overlap, hit);
            }else if(idx==this.hmpStart.get(dif.chr).size()){
                boolHit|=this.hitLeaf(dif, this.hmpStart.get(dif.chr).get(this.hmpStart.get(dif.chr).size()-1), overlap, hit);
            }else{
                boolHit|=this.hitLeaf(dif,this.hmpStart.get(dif.chr).get(idx-1),overlap, hit);
                boolHit|=this.hitLeaf(dif,this.hmpStart.get(dif.chr).get(idx),overlap, hit);
            }           
        }       
        return boolHit;
    }

    
    public boolean hitLeaf(SAMRecord sr,int pos,float overlap, boolean hit){
        boolean boolHit = false;
        ArrayList<DBLeaf> alt = this.hmpDB.get(sr.getContig()).get(pos);
        for (int i = 0; i < alt.size(); i++) {
//            if ("hsa_piR_020388".equals(alt.get(i).name)) {
//                System.out.println(alt.get(i).getRecord()+" ---> ("+sr.getStart()+"    "+sr.getEnd()+")");
//            }
//            if ("hsa_piR_020388_TEST".equals(alt.get(i).name)) {
//                System.out.println(alt.get(i).getRecord() + " ---> (" + sr.getStart() + "    " + sr.getEnd() + ")");
//            }
            if("-".equals(alt.get(i).strand) && !sr.getReadNegativeStrandFlag()){
//                System.out.println(alt.get(i).getRecord());
                continue;               
            }
            if("+".equals(alt.get(i).strand) && sr.getReadNegativeStrandFlag()){
//                System.out.println(alt.get(i).getRecord());
                continue;
            }
            if (alt.get(i).hasOverlap(sr.getStart(), sr.getEnd(), overlap)) {
                if (hit) {
                    alt.get(i).hit++;
                    if(alt.get(i).lstReads==null)   alt.get(i).lstReads=new ArrayList();
                    alt.get(i).lstReads.add(sr.getReadName());
                }
                boolHit = true;
//                if(alt.get(i).end==70789539){
//                    System.out.println(this.strDB+"--->"+alt.get(i).toRecord()+" ---> "+sr.getReadName()+"["+sr.getStart()+"    "+sr.getEnd()+"]");
//                }
            }            
        }
        return boolHit;
    }

    public String hitLeaf(String chrom,int pos,int end){
        StringBuilder sbHit = new StringBuilder();
        ArrayList<DBLeaf> alt = this.hmpDB.get(chrom).get(pos);
        for (int i = 0; i < alt.size(); i++) {
            if (alt.get(i).within(end)) {
                sbHit.append(alt.get(i).name);
                sbHit.append(",");
            }            
        }
        if(sbHit.length()==0){
//            System.out.println(alt.get(0).name);
            return null;
        }
        else    return sbHit.substring(0, sbHit.length()-1);
    }    
    
    public void hitLeaf(String chrom, int pos, int end, ArrayList<DBLeaf> altDBLeaf) {
        ArrayList<DBLeaf> alt = this.hmpDB.get(chrom).get(pos);
        for (int i = 0; i < alt.size(); i++) {
            if (alt.get(i).within(end)) {
                altDBLeaf.add(alt.get(i));
            }
        }
    }
    
    /**
     * This function will be called in function graftLeafByLocation. 
     * @param dif
     * @param pos
     * @param overlap
     * @param hit
     * @return 
     */
    public boolean hitLeaf(DBLeaf dif ,int pos,float overlap, boolean hit){

        ArrayList<DBLeaf> alt = this.hmpDB.get(dif.chr).get(pos);
        for (int i = 0; i < alt.size(); i++) {
            if (alt.get(i).hasOverlap(dif.start, dif.end,overlap)){
                    if(hit) alt.get(i).hit++;
                return true;
            }
        }
        return false;
    }
    

    public void graftLeafByLocation(DBLeaf dif, float overlap) {
        if (!this.hmpStart.containsKey(dif.chr)) {
            this.hmpStart.put(dif.chr, new ArrayList());
            this.hmpDB.put(dif.chr, new HashMap());
        }
        if(this.hmpStart.get(dif.chr).isEmpty()){
            this.hmpStart.get(dif.chr).add(dif.start);
            this.hmpDB.get(dif.chr).put(dif.start, new ArrayList());
            this.hmpDB.get(dif.chr).get(dif.start).add(dif);

        }else{
            int idx=Collections.binarySearch(this.hmpStart.get(dif.chr), dif.start);
            if(idx>=0){
                //Already exist.
                this.hmpDB.get(dif.chr).get(dif.start).add(dif);
            }else{
                //Don't exist.
                boolean boolHit;
                idx=-idx-1;
                if (idx == 0) {
                    boolHit = this.hitLeaf(dif, this.hmpStart.get(dif.chr).get(0), overlap, false);
                    if(boolHit) this.hmpDB.get(dif.chr).get(this.hmpStart.get(dif.chr).get(0)).add(dif);
                    else    this.putLeaf(dif,idx);
                } else if (idx == this.hmpStart.get(dif.chr).size()) {
                    boolHit = this.hitLeaf(dif, this.hmpStart.get(dif.chr).get(this.hmpStart.get(dif.chr).size() - 1), overlap, false);
                    if(boolHit) this.hmpDB.get(dif.chr).get(this.hmpStart.get(dif.chr).get(this.hmpStart.get(dif.chr).size() - 1)).add(dif);
                    else    this.putLeaf(dif,idx);

                } else {
                    boolHit = this.hitLeaf(dif, this.hmpStart.get(dif.chr).get(idx - 1), overlap, false);
                    if(boolHit){
                        this.hmpDB.get(dif.chr).get(this.hmpStart.get(dif.chr).get(idx-1)).add(dif);
                        return;
                    }
                    boolHit = this.hitLeaf(dif, this.hmpStart.get(dif.chr).get(idx), overlap, false);
                    if(boolHit){
                        this.hmpDB.get(dif.chr).get(this.hmpStart.get(dif.chr).get(idx)).add(dif);
                        return;                        
                    }
                    this.putLeaf(dif,idx);
                }               
            }
        } 
        
        //Try another way. 
//        if(this.hmpMerge.keySet().contains(dif.name)){
//            DBLeaf tmp=this.hmpMerge.get(dif.name);
//            tmp.lstReads=(List)CollectionUtils.union(tmp.lstReads, dif.lstReads);
//            tmp.hit=tmp.lstReads.size();
//        }else{
//            this.hmpMerge.put(dif.name, dif);
//        }
    }
    
    public void graftLeafByIdentifier(DBLeaf dif, boolean flag, boolean useUMI) {
        if (flag) {
            if (this.hmpMerge.keySet().contains(dif.name)) {
                DBLeaf tmp = this.hmpMerge.get(dif.name);
                tmp.lstReads = (List) CollectionUtils.union(tmp.lstReads, dif.lstReads);
                if(useUMI){
                    tmp.hit=StringTools.countUMI(tmp.lstReads);
                }else{
                    tmp.hit = tmp.lstReads.size();
                }               
                if(!tmp.db.contains(dif.db)) tmp.db=tmp.db+"|"+dif.db;
                if(tmp.derives_from!=null)  tmp.derives_from=tmp.derives_from+"|"+dif.derives_from;
            } else {
                if(useUMI){
                    dif.hit=StringTools.countUMI(dif.lstReads);
                }
                this.hmpMerge.put(dif.name, dif);
            }
        } else {
            if ("-".equals(dif.other)) {
                this.graftLeafByIdentifier(dif, true,useUMI);
            } else {
                if (this.hmpMerge.keySet().contains(dif.other)) {
                    DBLeaf tmp = this.hmpMerge.get(dif.other);
                    tmp.lstReads = (List) CollectionUtils.union(tmp.lstReads, dif.lstReads);
                    if (useUMI) {
                        tmp.hit = StringTools.countUMI(tmp.lstReads);
                    } else {
                        tmp.hit = tmp.lstReads.size();
                    }
                    if(!tmp.db.contains(dif.db)) tmp.db=tmp.db+"|"+dif.db;
                } else {
                    if (useUMI) {
                        dif.hit = StringTools.countUMI(dif.lstReads);
                    }
                    this.hmpMerge.put(dif.other, dif);
                }
            }
        }

    }
 
    /**
     * This function is designed for IPTR_GENCODE. 
     * @param dif The DBLeaf will be grafted. 
     * @param strKey The key used to index the DBLeaf. 
     */
    public void graftLeafByIdentifier(DBLeaf dif, String strKey,boolean useUMI) {
        if (this.hmpMerge.keySet().contains(strKey)) {
            DBLeaf tmp = this.hmpMerge.get(strKey);
            tmp.lstReads = (List) CollectionUtils.union(tmp.lstReads, dif.lstReads);
            if (useUMI) {
                tmp.hit = StringTools.countUMI(tmp.lstReads);
            } else {
                tmp.hit = tmp.lstReads.size();
            }
            if (!tmp.db.contains(dif.db)) {
                tmp.db = tmp.db + "|" + dif.db;
            }
        } else {
            this.hmpMerge.put(strKey, dif);
        }
    }
    
    
    public void putLeaf(DBLeaf dif, int idx) {
        this.hmpStart.get(dif.chr).add(idx, dif.start);
        this.hmpDB.get(dif.chr).put(dif.start, new ArrayList());
        this.hmpDB.get(dif.chr).get(dif.start).add(dif);
    }
    
    public boolean extendLeaf(int idx,SAMRecord sr, float overlap, boolean hit){
        boolean boolHit=false;
        //Right Direction. [...)  
        ArrayList<Integer> altPos=this.hmpStart.get(sr.getContig());
        for(int i=idx;i<altPos.size();i++){
            int pos=altPos.get(i);
            if(pos>=sr.getEnd())    break;
//            ArrayList<DBLeaf> altLeaf=this.hmpDB.get(sr.getContig()).get(pos);
//            for(DBLeaf dif : altLeaf){
//                sbHit|=this.hitLeaf(sr, pos, overlap, hit);
//            }
            boolHit|=this.hitLeaf(sr, pos, overlap, hit);
        }
        
        //Left Direction. To annotate the node up to the independent one.   
        for(int i=idx-1;i>=0;i--){
            int pos=altPos.get(i);
            ArrayList<DBLeaf> altLeaf=this.hmpDB.get(sr.getContig()).get(pos);
            boolean isOverlapped=false;
            boolean isCovered=true;
            for(DBLeaf dif: altLeaf){
//                if ("FCGR2A".equals(dif.name)) {
//                    System.out.println(dif.getRecord() + " ---> (" + sr.getStart() + "    " + sr.getEnd() + ") " + dif.isCovered);
//                }              
                isOverlapped|=(dif.end>sr.getStart());
                isCovered&=dif.isCovered;
//                sbHit|=this.hitLeaf(sr, pos, overlap, hit);
            }
            
            if(isOverlapped){
                boolHit|=this.hitLeaf(sr, pos, overlap, hit);
            }
            
            if(!isCovered)   break;
            
        }
        return boolHit;
    }

    public String extendLeaf(String chrom, int idx ,int end){
        StringBuilder sbHit=new StringBuilder();
        //Right Direction. [...)  
        ArrayList<Integer> altPos=this.hmpStart.get(chrom);
        for(int i=idx;i<altPos.size();i++){
            int pos=altPos.get(i);
            if(pos>end)    break;
            String strTmp=this.hitLeaf(chrom, pos, end);
            if (strTmp != null) {
                sbHit.append(strTmp);
                sbHit.append(",");
            }
        }
        
        //Left Direction. To annotate the node up to the independent one.   
        for(int i=idx-1;i>=0;i--){
            int pos=altPos.get(i);
            ArrayList<DBLeaf> altLeaf=this.hmpDB.get(chrom).get(pos);
            boolean isOverlapped=false;
            boolean isCovered=true;
            for(DBLeaf dif: altLeaf){
         
                isOverlapped|=(dif.end>end);
                isCovered&=dif.isCovered;

            }
            
            if (isOverlapped) {
                String strTmp = this.hitLeaf(chrom, pos, end);
                if (strTmp != null) {
                    sbHit.append(strTmp);
                    sbHit.append(",");
                }
            }
            
            if(!isCovered)   break;
            
        }
        if(sbHit.length()==0)   return null;
        else    return sbHit.substring(0, sbHit.length()-1);
    }
    
    public void extendLeaf(String chrom, int idx, int end, ArrayList<DBLeaf> altDBLeaf) {

        //Right Direction. [...)  
        ArrayList<Integer> altPos = this.hmpStart.get(chrom);
        for (int i = idx; i < altPos.size(); i++) {
            int pos = altPos.get(i);
            if (pos > end) {
                break;
            }
            this.hitLeaf(chrom, pos, end, altDBLeaf);
        }

        //Left Direction. To annotate the node up to the independent one.   
        for (int i = idx - 1; i >= 0; i--) {
            int pos = altPos.get(i);
            ArrayList<DBLeaf> altLeaf = this.hmpDB.get(chrom).get(pos);
            boolean isOverlapped = false;
            boolean isCovered = true;
            for (DBLeaf dif : altLeaf) {
                isOverlapped |= (dif.end > end);
                isCovered &= dif.isCovered;
            }

            if (isOverlapped) {
                this.hitLeaf(chrom, pos, end, altDBLeaf);
            }

            if (!isCovered) {
                break;
            }
        }
    }
    
    
    public StringBuilder getReport() {

        int totalAnn=0;
        int hitAnn=0;
        int unhitAnn=0;
        int hitRead=0;
//        int tmp=0;
        
        for (String key : this.hmpStart.keySet()) {
            for (int pos : this.hmpStart.get(key)) {
                for(DBLeaf dif: this.hmpDB.get(key).get(pos)){
                    totalAnn++;
                    if(dif.hit>0)  hitAnn++;
                    else    unhitAnn++;
                    hitRead+=dif.hit;
//                    if(dif.lstReads!=null)  tmp+=dif.lstReads.size();//should be equal with hitRead.
                    
                }
            }
        }
        
        StringBuilder sb=new StringBuilder();
        sb.append("[").append(this.strDB).append("]\n");
        sb.append("Total Annotation Items: ").append(totalAnn).append("\n");
        sb.append("Annotated Items (covered by least one read): ").append(hitAnn).append("\n");
        sb.append("Unannotated Items: ").append(unhitAnn).append("\n");
        sb.append("Reads Support the Annotation: ").append(hitRead).append("\n");
//        sb.append("Tmp: ").append(tmp).append("\n");
        
        return sb;
    }

    public StringBuilder getReportMerge() {
        
        int totalAnn=0;
        int hitAnn=0;
        int hitRead=0;
//        int tmp=0;
        
        for (String key : this.hmpMerge.keySet()) {
            DBLeaf dif = this.hmpMerge.get(key);
            totalAnn++;
            if (dif.hit > 0) {
                hitAnn++;
            } 
            hitRead += dif.hit;
//            tmp += dif.lstReads.size();//should be equal with hitRead.
        }
        
        StringBuilder sb=new StringBuilder();
        sb.append("[").append(this.strDB).append("]\n");
        sb.append("Total Annotation Items: ").append(totalAnn).append("\n");
        sb.append("Annotated Items (covered by least one read): ").append(hitAnn).append("\n");
        sb.append("Reads Support the Annotation: ").append(hitRead).append("\n");
//        sb.append("Tmp: ").append(tmp).append("\n");
        
        return sb;
    }
}
