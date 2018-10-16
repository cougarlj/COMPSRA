/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.entity;

import edu.harvard.channing.compass.core.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class is used to save the one line information in gff3 files.
 *
 * @author Jiang Li
 * @version 1.0
 * @since 2017-10-04
 */
public class DBLeaf_GFF3 extends DBLeaf {

    private static final Logger LOG = LogManager.getLogger(DBLeaf_GFF3.class.getName());

    public String source;   
    public String score;
    public String phase;
    public String alias;
    public String parent;
    public String target;
    public String gap;
//    public String derives_from;
    public String note;
    public String dbxref;
    public String ontology_term;
    public boolean is_circular;
    public String type;
    public String gene_id;

    @Override
    public void initLeaf(String strLine, String key) {
        String[] strCol = strLine.split("\t");
//        this.chr = strCol[0].toLowerCase();
//        this.chr = strCol[0];
        this.chr = Configuration.getContig(key, strCol[0].toLowerCase());
        this.source = strCol[1];
        this.feature = strCol[2];
        this.start = Integer.parseInt(strCol[3]);
        this.end = Integer.parseInt(strCol[4]);
        this.score = strCol[5];
        this.strand = strCol[6];
        this.phase = strCol[7];

        String[] attr = strCol[8].split(";");
        for (int i = 0; i < attr.length; i++) {
            if (attr[i].contains("ID")) {
                this.id = attr[i].trim().substring(3);
            } else if (attr[i].contains("Name")) {
                this.name = attr[i].trim().substring(5);
            } else if (attr[i].contains("Alias")) {
                this.alias = attr[i].trim().substring(6);
            } else if (attr[i].contains("Parent")) {
                this.parent = attr[i].trim().substring(7);
            } else if (attr[i].contains("Target")) {
                this.target = attr[i].trim().substring(7);
            } else if (attr[i].contains("Gap")) {
                this.gap = attr[i].trim().substring(4);
            } else if (attr[i].contains("Derives_from")) {
                this.derives_from = attr[i].trim().substring(13);
            } else if (attr[i].contains("Note")) {
                this.note = attr[i].trim().substring(5);
            } else if (attr[i].contains("Dbxref")) {
                this.dbxref = attr[i].trim().substring(7);
            } else if (attr[i].contains("Ontology_term")) {
                this.ontology_term = attr[i].trim().substring(14);
            } //some key=value from GENCODE file.
            else if (attr[i].contains("gene_name")) {
                this.name = attr[i].trim().substring(10);
            } else if (attr[i].contains("gene_type")) {
                this.type = attr[i].trim().substring(10);
            } else if (attr[i].contains("gene_id")) {
                this.gene_id = attr[i].trim().substring(8);
            }
//            else{
//                LOG.info("GFF3 Error: The gff3 file is not a stander one!");
//            }
        }
    }

    @Override
    public String toRecord() {
//        return db+"\t"+chr+"\t"+start+"\t"+end+"\t"+strand+"\t"+name+"\t"+id+"\t"+hit;
        return db + "\t" + name + "\t" + other + "\t" + chr + "\t" + start + "\t" + end + "\t" + strand + "\t" + hit;
    }

}
