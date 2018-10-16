/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.entity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class is specially used to save the one line information of piRNA database.
 * @author Jiang Li
 * @version 1.0 
 * @since 2017-10-04
 */
public class DBLeaf_piRNA extends DBLeaf{
    private static final Logger LOG = LogManager.getLogger(DBLeaf_piRNA.class.getName());    
    public boolean isInCluster=false;    
        
    @Override
    public String toRecord(){
        return db+"\t"+name+"\t"+other+"\t"+chr+"\t"+start+"\t"+end+"\t"+strand+"\t"+hit+"\t"+isInCluster;
    }    
 
    @Override
    public String getRecord(int intThreshold){
        if(hit<intThreshold){
            return null;
        }else{
            return db+"\t"+name+"\t"+other+"\t"+hit+"\t"+isInCluster;
        }      
    }
    
    @Override
    public void setCluster(boolean inCluster){
        this.isInCluster=inCluster;
    }
    
    @Override
    public boolean getCluster(){
        return this.isInCluster;
    }

}
