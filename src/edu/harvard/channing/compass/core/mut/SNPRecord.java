/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.core.mut;

/**
 * This class is used to save the predicted SNP information.
 * @author Jiang Li
 * @version 1.0 
 * @since 2018-10-11
 */
public class SNPRecord {
    String chrom;
    int pos;
    String id;
    String ref=".";
    String alt;
    int total;
    int variant;
    float rate;
}
