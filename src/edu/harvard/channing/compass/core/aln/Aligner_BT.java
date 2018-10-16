/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.core.aln;

import edu.harvard.channing.compass.core.Configuration;
import edu.harvard.channing.compass.entity.FileRecord;

/**
 * This class is used to implement the bowtie alignment function.
 * @author Jiang Li
 * @version 1.0 
 * @since 2017-10-25
 */
public class Aligner_BT extends Aligner {
    String strToolName="bowtie";
    String strToolPath="/udd/rejia/tool/";
    String strToolParam="-a --best --strata -v 1 -S -t --threads 4";
    String strToolRef="/udd/rejia/data/bowtie_db/GCA_000001405.15_GRCh38_no_alt_analysis_set";
    
    public Aligner_BT(String in, String out) {
        super(in,out);
//        this.frd=frd;
    }

    @Override
    public void setCommand() {
//        this.frd.setAln();
        if(this.strParam!=null) this.strToolParam=this.strParam;
        this.command=Configuration.PLUG_BUILT_IN.get(strToolName)+" "+this.strToolParam+" "+Configuration.BOWTIE_REF.get(strToolName+"_"+strRef)+" "+this.strInput+" "+this.strOutput;
    }

    @Override
    public void setRefDB(String strRefDB) {
        this.strToolRef=strRefDB;
    }
    
}
