/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.entity;

import java.util.ArrayList;

/**
 * This class is specially used to save the one line information of miRNA database.
 * @author Jiang Li
 * @version 1.0 
 * @since 2017-10-05
 */
public class DBLeaf_miRNA extends DBLeaf_GFF3{
 
    @Override
    public String toRecord() {
        return db + "\t" + name + "\t" + id + "\t" + chr + "\t" + start + "\t" + end + "\t" + strand + "\t" + hit ;
    }
}
