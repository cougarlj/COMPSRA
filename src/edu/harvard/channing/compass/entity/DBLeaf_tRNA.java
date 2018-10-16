/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.entity;

/**
 * This class is specially used to save the one line information of tRNA database.
 * @author Jiang Li
 * @version 1.0 
 * @since 2017-10-05
 */
public class DBLeaf_tRNA extends DBLeaf{
    
    @Override
    public String toRecord(){
//        return db+"\t"+chr+"\t"+start+"\t"+end+"\t"+strand+"\t"+name+"\t"+other+"\t"+hit;
        return db + "\t" + name + "\t" + other + "\t" + chr + "\t" + start + "\t" + end + "\t" + strand + "\t" + hit;
    }       
    
}
