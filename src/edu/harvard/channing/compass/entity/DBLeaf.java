/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.entity;

import edu.harvard.channing.compass.utility.MathTools;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class is used to save the information of database annotation files.
 * @author Jiang Li
 * @version 1.0 
 * @since 2017-09-25
 */
public class DBLeaf implements Serializable{
    private static final Logger LOG = LogManager.getLogger(DBLeaf.class.getName());
    
    public String db;
    public String name;
    public String id;
    public String chr;
    public int start;
    public int end;
    public String strand=".";
    public String other="-";
    public String feature;
    public String derives_from;
    public int hit=0;
    public boolean isCovered=false;
    
    public List<String> lstReads;


    public void initLeaf(String strLine,String key){
        //Used when needed.
    }
    
    public int getLength() {
        return this.end-this.start;
    }
    
    public String toRecord(){
//        return db+"\t"+chr+"\t"+start+"\t"+end+"\t"+strand+"\t"+name+"\t"+hit;
        return db + "\t" + name + "\t" + other + "\t" + chr + "\t" + start + "\t" + end + "\t" + strand + "\t" + hit;
    }
    
    public String getRecord(int intThreshold) {
//        return db+"\t"+name+"\t"+hit;         
        if (hit < intThreshold) {
            return null;
        } else {
            return db + "\t" + name + "\t" + other + "\t" + hit;
        }
    }
    
    public String getDetail(int intThreshold){
        if (hit < intThreshold) {
            return null;
        } else {
            StringBuilder sb=new StringBuilder();
            for(String strRead:this.lstReads){
                sb.append(this.getRecord(intThreshold)).append("\t").append(strRead).append("\n");
            }
            return sb.toString();
        }        
    }
    public boolean hasOverlap(int start, int end, float overlap) {
        float rate=MathTools.compRegion(this.start, this.end, start, end);
        if(rate>=overlap)   return true;
        else    return false;
    }
    
    public boolean within(int point){
        if(point>=this.start && point<=this.end ){
            return true;
        }else{
            return false;
        }
    }
    public void setCluster(boolean inCluster){
        //Only used in piRNA part.
    };
    public boolean getCluster(){
        //Only used in piRNA part.
        return false;
    }
    
}
