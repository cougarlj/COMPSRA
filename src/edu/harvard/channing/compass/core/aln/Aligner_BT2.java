/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.core.aln;

import edu.harvard.channing.compass.core.Configuration;
import edu.harvard.channing.compass.entity.FileRecord;

/**
 * This class is used to run bowtie2.
 * @author Jiang Li
 * @version 1.0
 * @since 2017-12-08
 */
public class Aligner_BT2 extends Aligner {
    
    String strToolName="bowtie2";
    String strToolPath;
    String strToolParam="--sam-no-hd --sam-no-sq --no-unal --very-sensitive";
    String strToolRef="/database/ann/MetaPhlAn/db_v20/db_v20/mpa_v20_m200";
    
    public Aligner_BT2(String in, String out) {
        super(in,out);
//        this.frd=frd;
    }

    @Override
    public void setCommand() {
        if(this.strParam!=null) this.strToolParam=this.strParam;
        this.command=Configuration.PLUG_BUILT_IN.get(strToolName)+" "+this.strToolParam+" -x "+strToolRef+" -U "+this.strInput+" -S "+this.strOutput;

    }

    @Override
    public void setRefDB(String strRefDB) {
        this.strToolRef=strRefDB;
    }
    
}
