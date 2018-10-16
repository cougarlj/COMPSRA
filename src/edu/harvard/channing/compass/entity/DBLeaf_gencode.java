/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.entity;

/**
 * This class is specially used to save the one line information of GENCODE database.
 * @author Jiang Li
 * @version 1.0 
 * @since 2017-12-25
 */
public class DBLeaf_gencode extends DBLeaf_GFF3{

    @Override
    public String toRecord() {
//        return db+"\t"+chr+"\t"+start+"\t"+end+"\t"+strand+"\t"+name+"\t"+id+"\t"+type+"\t"+hit;
        return db + "\t" + name + "\t" + other + "\t" + chr + "\t" + start + "\t" + end + "\t" + strand + "\t" + hit;
    }
    
    @Override
    public String getRecord(int intThreshold) {
        //"DB\tName\tID\tCount\tFeature\tContig\tStart\tEnd\tStrand"
        if (hit < intThreshold) {
            return null;
        } else {
            return db + "\t" + name + "\t" + id + "\t" + hit + "\t" + type + "\t" + feature + "\t" + chr + "\t" + start + "\t" + end + "\t" + strand;
        }

    }
}
