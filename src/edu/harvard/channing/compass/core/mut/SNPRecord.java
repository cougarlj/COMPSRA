/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.core.mut;

import edu.harvard.channing.compass.core.Configuration;
import java.util.ArrayList;
import java.util.Arrays;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class is used to save the predicted SNP information.
 * @author Jiang Li
 * @version 1.0 
 * @since 2018-10-11
 */
public class SNPRecord {
    private static final Logger LOG = LogManager.getLogger(SNPRecord.class.getName());
    
    String chrom;
    int pos;
    String id=".";
    char ref='.';
    String alt;
    int total=0;
    int variant=0;
    float rate;
    int[] bases=new int[5];//ACGTN
    String strPileUp;
    String sample;
    String key; //chr1:40754376:A

    SNPRecord(String strContig, int intPos, char ref, String strBase) {
        this.chrom=strContig;
        this.pos=intPos;
        this.ref=ref;
        this.strPileUp=strBase;
    }


    SNPRecord(String strKey) {
        String[] strItem=strKey.split(":");
        this.chrom=strItem[0];
        this.pos=Integer.valueOf(strItem[1]);
        this.ref=strItem[2].charAt(0);
    }

    SNPRecord(String strLine, String strSample) {
        String[] strItem=strLine.split("\t");
        this.chrom=strItem[0];
        this.pos=Integer.valueOf(strItem[1]);
        this.ref=strItem[2].charAt(0);       
        this.alt=strItem[3];
        this.total=Integer.valueOf(strItem[4]);
        this.variant=Integer.valueOf(strItem[5]);
        this.rate=Float.valueOf(strItem[6]);
        String[] base=strItem[7].split(",");
        for(int i=0;i<5;i++){
            this.bases[i]=Integer.valueOf(base[i]);
        }
        
        this.sample=strSample;        
    }
    
    public String getPileUp(){
        return this.chrom+"\t"+this.pos+"\t"+this.ref+"\t"+this.strPileUp;
    }
    
    public static String getPileUpHead(){
        return "#chrom\tpos\tref\tbases";
    }
    
    public String getKey(){
        return this.chrom+":"+this.pos+":"+this.ref;
    }
    
    public boolean callVariant(){
        Arrays.fill(this.bases, 0);
        for(char base:this.strPileUp.toCharArray()){
            switch(base){
                case 'A':bases[0]++;break;
                case 'C':bases[1]++;break;
                case 'G':bases[2]++;break;
                case 'T':bases[3]++;break;
                default :bases[4]++;
            }
        }
        
        if('.'==ref){
            LOG.warn("Reference of the position: "+this.chrom+":"+this.pos+" was lost! It will not be considered!");
            return false;
        }
        if(this.strPileUp.length()==bases[Configuration.BASEORDER.get(ref)]){
            return false;
        }else{
            for(int i=0;i<bases.length;i++){
                this.total+=bases[i];
                if(Configuration.BASEORDERREV.get(i)!=this.ref){
                    if(bases[i]!=0){
                        this.variant+=bases[i];
                        if(this.alt==null){
                            this.alt=String.valueOf(Configuration.BASEORDERREV.get(i));
                        }else{
                            this.alt+=","+String.valueOf(Configuration.BASEORDERREV.get(i));
                        }
                    }
                }
            }
            this.rate=(float)this.variant/(float)this.total;           
            return true;
        }
    }
    
    public String getVariant(){
        StringBuilder sb=new StringBuilder();
        sb.append(this.chrom);
        sb.append("\t");
        sb.append(this.pos);
        sb.append("\t");
        sb.append(this.ref);
        sb.append("\t");
        sb.append(this.alt);
        sb.append("\t");
        sb.append(this.total);
        sb.append("\t");
        sb.append(this.variant);
        sb.append("\t");
        sb.append(this.rate);
        sb.append("\t");
        
        for(int i=0;i<bases.length;i++){
            sb.append(bases[i]);
            sb.append(",");
        }
        
        sb.deleteCharAt(sb.length()-1);
        
        return sb.toString();
        
    } 
    
    public static String getVariantHead(){
        return "#chrom\tpos\tref\talt\ttotal\tvariant\trate\tA,C,G,T,N";
    }

    public String getVariant2() {
        StringBuilder sb=new StringBuilder();
        sb.append("t=");
        sb.append(this.total);
        sb.append("|");
        sb.append("v=");
        sb.append(this.variant);
        sb.append("|");
        sb.append("r=");
        sb.append(this.rate);
        sb.append("|");
        sb.append("acgtn=");
        for (int i = 0; i < bases.length; i++) {
            sb.append(bases[i]);
            sb.append(",");
        }       
        sb.deleteCharAt(sb.length()-1);
        
        return sb.toString();
    }

    public void mergeSNP(SNPRecord tmp) {
        for(int i=0;i<5;i++)    this.bases[i]+=tmp.bases[i];
        this.alt=null;
        this.total=0;
        this.variant=0;
        for (int i = 0; i < this.bases.length; i++) {
            this.total += this.bases[i];
            if (Configuration.BASEORDERREV.get(i) != this.ref) {
                if (this.bases[i] != 0) {
                    this.variant += this.bases[i];
                    if (this.alt == null) {
                        this.alt = String.valueOf(Configuration.BASEORDERREV.get(i));
                    } else {
                        this.alt += "," + String.valueOf(Configuration.BASEORDERREV.get(i));
                    }
                }
            }
        }
        this.rate = (float) this.variant / (float) this.total;

    }
    
}
