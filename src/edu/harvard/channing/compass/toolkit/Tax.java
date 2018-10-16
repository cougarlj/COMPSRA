/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.toolkit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is used to save the taxon info. 
 * @author Jiang Li
 * @version 1.0 
 * @since 2018-01-12
 */
class Tax implements Serializable{
    public String tax_id;
    public String rank;
    public String parent_id;
    public Tax parent;
    public HashMap<String,Tax> children;
    public ArrayList<String> names;
    public ConcurrentHashMap<String,Integer> reads;

    Tax(String tax_id, String parent_id, String rank) {
        this.tax_id=tax_id;
        this.parent_id=parent_id;
        this.rank=rank;
        this.children=new HashMap<String,Tax>();
        this.names=new ArrayList<String>();
        this.reads=new ConcurrentHashMap<String,Integer>();
    }
    
    public String makeRecored(int type){
        String record;
        switch (type){
            case 0:
                record=this.tax_id+"("+this.names.get(0)+")";
                break;
            case 1:
                record=this.tax_id;
                break;
            case 2:
                record=this.names.get(0);
                break;
            case 3:
                record=this.rank;
                break;
            case 4:
                record=String.valueOf(this.countReads());
                break;
            case 5:
                record=String.valueOf(this.countHits());
                break;
            case 123:
                record=this.tax_id+"("+this.names.get(0)+")"+"["+this.rank+"]";
                break;
            case 1234:
                record=this.tax_id+"("+this.names.get(0)+")"+"["+this.rank+"]"+"<"+this.countReads()+">";
                break;
            case 125:
                record=this.tax_id+"("+this.names.get(0)+")"+"<"+this.countHits()+">";  
                break;
            case 14:
                record=this.tax_id+"<"+this.countReads()+">";
                break;
            case 134:
                record=this.tax_id+"["+this.rank+"]"+"<"+this.countReads()+">";
                break;
            case 124:
                record=this.tax_id+"("+this.names.get(0)+")"+"<"+this.countReads()+">";
                break;
            case 666:
                record=this.tax_id+"("+this.names.get(0)+")"+"["+this.rank+"]"+"\t--->\t"+this.countReads();
                break;
            default:
                record=this.tax_id+"("+this.names.get(0)+")";
                break;
        } 
        return record;
    }
 
    public int countReads(){
        return this.reads.keySet().size();
    }

    public int countHits(){
        int hit=0;
        for(Integer i:this.reads.values()){
            hit+=i;
        }
        return hit;
    }
    
    public String countMapping(){
        return "<"+this.countHits()+","+this.countReads()+">";
    }
    
    public String getAncestorInfo(int type){
        return this.parent==null?this.makeRecored(type):this.parent.makeRecored(type)+"|"+this.makeRecored(type);
    }
}
