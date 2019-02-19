/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.core.ann;

import edu.harvard.channing.compass.core.Factory;
import edu.harvard.channing.compass.db.DB;
import java.util.ArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author rejia
 */
public class IPTR_pre_miRNA extends IPTR_miRNA{
    private static final Logger LOG = LogManager.getLogger(IPTR_pre_miRNA.class.getName());
    
    public IPTR_pre_miRNA(String strOut) {
        super(strOut);
        this.strCategory="pre-miRNA";
    }

    @Override
    public boolean buildForest() {
        dbtForest = new ArrayList<>();

        //Construct miRBase_pre           
        DB db = Factory.getDB("miRBase_pre", this.boolCR);
        if (db != null) {
            dbtForest.add(db.getForest(this.refGenome));
        }

        //Other database can be added as follows!
        
        return true;
    }
    
}
