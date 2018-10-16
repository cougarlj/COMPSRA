/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.entity;

import edu.harvard.channing.compass.core.Configuration;
import edu.harvard.channing.compass.core.mic.Marker;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;

/**
 * This class is used to define the basic function and fields of each evolutionary node.
 * @author Jiang Li
 * @version 1.0
 * @since 2017-10-15
 */
public class BioNode implements Serializable{
    public String name;
    /**
     * 'k:kindom', 'p:phylum', 'c:class', 'o:order', 'f:family', 'g:genus', 's:species', 't:strains';
     */
    public char level;
    public BioNode parent;
    public HashMap<String,BioNode> children;
    public ArrayList<Marker> marker;
    public int size=0;
    public int SIZE=10000;
    public int count_self=0;
    public int count_desc=0;
//    public int hit=0;
    public int intGenomeLen;
    public float uncl_ab=-1;
    public float abundance=-1;
    public float re_abundance=0;
    public boolean misIdentified;
    public float THRESHOLD1=(float) 0.33;
    public float THRESHOLD2=(float) 0.7;
    public float quantile=(float) 0.1;
    public int min_cu_len=2000;
    public boolean subcl_uncl=false;
    
    public BioNode(String name,char level,BioNode parent) {
        this.name=name;
        this.level=level;
        this.parent=parent;
        this.children=new HashMap<String,BioNode>();
        this.marker=new ArrayList<Marker>();
    }

    public boolean add2PheloTree(String clue,Marker m) {
        String[] strTaxon=clue.split("\\|");
        
        if(strTaxon.length==1){
            if(this.children.containsKey(strTaxon[0])){
                m.owner=this.children.get(strTaxon[0]);
                this.children.get(strTaxon[0]).marker.add(m);
            }else{
                BioNode temp=new BioNode(strTaxon[0],strTaxon[0].charAt(0),this);
                m.owner=temp;
                temp.marker.add(m);
                this.children.put(strTaxon[0], temp);
            }           
        }else{
            if(this.children.containsKey(strTaxon[0])){
                this.children.get(strTaxon[0]).add2PheloTree(clue.substring(strTaxon[0].length()+1), m);
            }else{
                BioNode temp=new BioNode(strTaxon[0],strTaxon[0].charAt(0),this);
                this.children.put(strTaxon[0], temp);
                this.children.get(strTaxon[0]).add2PheloTree(clue.substring(strTaxon[0].length()+1), m);
            }
        }       
        return true;
    }   

    public boolean add2PheloTree(String clue,String len,HashMap<String,BioNode> clades){
        String[] strTaxon=clue.split("\\|");
        
        if(strTaxon.length==1){
            if(this.children.containsKey(strTaxon[0])){
                System.out.println("Something wrong with the Tree.");
            }else{
                BioNode temp=new BioNode(strTaxon[0],strTaxon[0].charAt(0),this);
                temp.setGenomeLen(len);
                this.children.put(strTaxon[0], temp);
                clades.put(strTaxon[0], temp);
            }           
        }else{
            if(this.children.containsKey(strTaxon[0])){
                this.children.get(strTaxon[0]).add2PheloTree(clue.substring(strTaxon[0].length()+1), len,clades);
            }else{
                BioNode temp=new BioNode(strTaxon[0],strTaxon[0].charAt(0),this);
                this.children.put(strTaxon[0], temp);
                clades.put(strTaxon[0], temp);
                this.children.get(strTaxon[0]).add2PheloTree(clue.substring(strTaxon[0].length()+1), len,clades);
            }
        }       
        return true;        
    }
    
    public void setCount(){
        if(this.subcl_uncl){
            this.count_self=this.parent.count_self;
            return;
        }
        if(this.uncl_ab>0){
            this.count_self=-1;
        }
        Iterator<Marker> itr=this.marker.iterator();
        while(itr.hasNext()){
            Marker tmp=itr.next();
            if(tmp.hit==0){
                itr.remove();
            }else{
                this.count_self+=tmp.hit;
                this.size+=tmp.len;               
            }
        }
        
        for(BioNode bnd:this.children.values()){
            bnd.setCount();
            this.count_desc+=(bnd.count_self+bnd.count_desc);
        }      
    }

    public float evlAbundance(boolean avd, HashMap<String, BioNode> hmpClades) {
        if (this.abundance != -1) {
            return this.abundance;
        }
        
        float sum_ab = 0;
        for (BioNode bndChild : this.children.values()) {
            sum_ab += bndChild.evlAbundance(avd, hmpClades);
        }
        
        if("t__PRJNA14222".equals(this.name)){//check point.
            System.out.println("check!");
        }
        
        ArrayList<Marker> altKeep = new ArrayList<Marker>();
        ArrayList<Marker> altRemove = new ArrayList<Marker>();
        for (Marker m : this.marker) {
            boolean boolRemove = false;
//                if(m.hit==0)    continue;
            if (m.ext != null) {
                for (String ext : m.ext) {
                    BioNode bnd = hmpClades.get("t__" + ext);
                    while (bnd.children.size() == 1) {
                        ArrayList<BioNode> alt = new ArrayList<BioNode>(bnd.children.values());
                        bnd = alt.get(0);
                    }

                    int intMappedMarker = this.getMappedMarkerCount(bnd);
                    if (intMappedMarker != 0) {
                        if ((float) intMappedMarker / (float) bnd.marker.size() > this.THRESHOLD1) {
                            altRemove.add(m);
                            boolRemove = true;
                            m.boolDisEq = true;
                            break;
                        }
                    }
                }
            }

            if (!boolRemove) {
                altKeep.add(m);
            }
        }

        if (altRemove.size() != 0) {
            int ripr = 10;
            if (this.countOffspring() < 2) {
                ripr = 0;
            }
            if (this.getFullName().contains("k__Viruses")) {
                ripr = 0;
            }
            if (altKeep.size() < ripr) {
                if (altRemove.size() < (ripr - altKeep.size())) {
                    altKeep.addAll(altRemove);
                } else {
                    for (int i = 0; i < (ripr - altKeep.size()); i++) {
                        altKeep.add(altRemove.get(i));
                    }
                }
            }
        }

        Comparator cmp = new Comparator<Marker>() {
            @Override
            public int compare(Marker t1, Marker t2) {
                if ((float) t1.hit / (float) t1.len > (float) t2.hit / (float) t2.len) {
                    return 1;
                } else {
                    return -1;
                }

            }
        };
        altKeep.sort(cmp);
        int sumLen = 0;
        int sumHit = 0;
        for (Marker m : altKeep) {
            sumLen += m.len;
            sumHit += m.hit;
        }
        if (sumLen == 0) {
            sumLen = -1;
        }
        
        if(sumHit!=0){//checkpoint
            System.out.print(this.getFullName()+"-->"+sumHit+"\n");
        }
        if("t__PRJNA14222".equals(this.name)){
            System.out.println("check!");
        }
        int quant = (int) (this.quantile * altKeep.size());
        if (this.parent != null) {
            if (this.level == 't' && (this.parent.children.size() > 1 || this.parent.name.contains("_sp") || this.getFullName().contains("k__Viruses"))) {
                int intNZMC = 0;
                for (Marker m : altKeep) {
                    if (m.hit != 0) {
                        intNZMC++;
                    }
                }
                if (altKeep.size() == 0 || (float) intNZMC / (float) altKeep.size() < this.THRESHOLD2) {//This filter is very strict I think. 
                    this.abundance = 0;
                    return 0;
                }
            }
        }


        //only realize tavg_g method.
        float local_ab = 0;
        if (sumLen >= 0) {
            ArrayList<Marker> altKeepSub = new ArrayList<Marker>();
            for (int i = quant; i < altKeep.size() - quant; i++) {
                altKeepSub.add(altKeep.get(i));
            }
            int sumSubLen = 0;
            int sumSubHit = 0;
            for (Marker m : altKeepSub) {
                sumSubLen += m.len;
                sumSubHit += m.hit;
            }
            local_ab = (float) sumSubHit / (float) sumSubLen;

            if (sumSubHit != 0) {//checkpoint
                System.out.print(this.getFullName() + "-->" + sumSubHit+"\n");
            }          
            
        }

       
        
        this.abundance = local_ab;

        if (sumLen < this.min_cu_len && !this.children.isEmpty()) {
            this.abundance = sum_ab;
        }else if(local_ab<sum_ab){
            this.abundance=sum_ab;
        }
        
        if(this.abundance>sum_ab && !this.children.isEmpty()){
//            BioNode bnd=new BioNode(String.valueOf(this.level)+"__unclassified",this.level,this.parent);
//            bnd.abundance=this.abundance-sum_ab;
//            bnd.subcl_uncl=true;
//            this.parent.children.put(bnd.name, bnd);           
            this.uncl_ab=this.abundance-sum_ab;
        }             
        this.subcl_uncl=this.children.isEmpty() && !"st".contains(String.valueOf(this.level));
        
        return this.abundance;

    }
    
    public void evlRtvAbundance(float total) {
        this.re_abundance = this.abundance / total;
        if (this.children.isEmpty()) {
            if (this.subcl_uncl) {
                BioNode bndTmp = new BioNode(Configuration.TAXONOMY.charAt(Configuration.TAXONOMY.indexOf(String.valueOf(this.level))+1) + "__unclassified", this.level, this.parent);
                bndTmp.abundance = this.abundance;
                this.children.put(bndTmp.name, bndTmp);
            }
            
        } else {
            ArrayList<BioNode> tmp=new ArrayList<BioNode>();
            for (BioNode bnd : this.children.values()) {
                if (this.uncl_ab > 0) {                   
                    BioNode bndTmp = new BioNode(String.valueOf(this.level) + "__unclassified", this.level, this.parent);
                    bnd.abundance = this.uncl_ab;
                    tmp.add(bndTmp);
                }
            }
            if(!tmp.isEmpty()){
                for(BioNode bnd:tmp){
                    this.children.put(bnd.name, bnd);
                }
            }
            for (BioNode bnd : this.children.values()) {
                bnd.evlRtvAbundance(total);
            }
        }
    }
    
    public String getFullName(){
        if(this.parent==null)   return this.name;
        return this.parent.getFullName()+"|"+this.name;
    }
    
    public int countOffspring(){
        int sum=1;
        for(BioNode bnd: this.children.values())    sum+=bnd.countOffspring();
        return sum;
    }
    
    public int getMappedMarkerCount(){
        int intMapped=0;
        for(Marker m : this.marker) if(m.hit!=0)    intMapped++;
        return intMapped;
    }

    public int getMappedMarkerCount(BioNode bnd){
        int intMapped=0;
        for(Marker m : bnd.marker) if(m.hit!=0)    intMapped++;
        return intMapped;        
    }
    
    public String makeRecord() {
        String record = "";

//        if (this.count_desc != 0) {
//            if (this.size >= this.SIZE) {
//                record += this.getBranch() + "\t-->\t" + this.count_self + "\n";
//            } else {
//                record += this.getBranch() + "\t-->\t" + this.count_desc + "\n";   
//            }
//            if (this.count_self  > this.count_desc) {
//                record += this.getBranch() + "|unclassified" + "\t-->\t" + (this.count_self - this.count_desc) + "\n";
//            }
//            for (String str : this.children.keySet()) {
//                record += this.children.get(str).makeRecord();
//            }
//        }else{
//            if(this.count_self!=0)  record += this.getBranch() + "\t-->\t" + this.count_self + "\n";
//        }

//        if(this.count_desc != 0){
//            record += this.getBranch() + "\t-->\t" + this.count_desc + "\t-->\t"+this.re_abundance*100+ "\n";
//            for (String str : this.children.keySet()) {
//                record += this.children.get(str).makeRecord();
//            }            
//        }else{
//            if(this.count_self!=0)  record += this.getBranch() + "\t-->\t" + this.count_self +"\t-->\t"+this.re_abundance*100+ "\n";
//        }
        
        if (this.re_abundance > 0) {
            if(this.count_desc!=0){
                record += this.getBranch() + "\t-->\t(" + this.count_desc + ";" + this.re_abundance * 100 + ")\n";
            }else{
                if(this.count_self!=0)  record += this.getBranch() + "\t-->\t(" + this.count_self + ";" + this.re_abundance * 100 + ")\n";
            }
            
            for (String str : this.children.keySet()) {
                record += this.children.get(str).makeRecord();
            }
        }


        return record;
    }

    private String getBranch() {
//        return this.parent.getBranch()+"|"+this.name;
        return this.parent==null?this.name:this.parent.getBranch()+"|"+this.name;
    }

    private void setGenomeLen(String len) {
        this.intGenomeLen=Integer.valueOf(len);
    }
}
