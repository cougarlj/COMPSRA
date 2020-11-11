/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.entity;

/**
 * Simple version of SNP
 * @author rejia
 * @version 1.2
 * @since 2020-11-05 
 */
public class SNP {
    public String chrom;
    public int pos;
    public String id;
    public String ref;
    public String alt;
    public String strand;
    public int qual;
    public String filter;
    public String info;
    public String format;
    public String gt;  
    public DBLeaf dbf;
    
    public SNP(String chrom, int pos, String ref, String alt,DBLeaf dbf){
        this.chrom=chrom;
        this.pos=pos;
        this.ref=ref;
        this.alt=alt;
        this.dbf=dbf;
    }
}
