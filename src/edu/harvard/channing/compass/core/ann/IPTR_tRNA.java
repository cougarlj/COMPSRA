/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.core.ann;

import edu.harvard.channing.compass.core.Factory;
import edu.harvard.channing.compass.db.DB;
import edu.harvard.channing.compass.core.Configuration;
import edu.harvard.channing.compass.entity.DBTree;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * This class is used to annotate the tRNA information. 
 * @author Jiang Li
 * @version 1.0
 * @since 2017-10-07
 */
public class IPTR_tRNA extends IPTR{
    private static final Logger LOG = LogManager.getLogger(IPTR_tRNA.class.getName());

    public IPTR_tRNA(String strOut) {
        super(strOut);
        this.strCategory="tRNA";
    }

    @Override
    public boolean buildForest() {
        dbtForest=new ArrayList<DBTree>();
        
        //Construct GtRNAdb        
        DB db=Factory.getDB("GtRNAdb",this.boolCR);
        if(db!=null)   dbtForest.add(db.getForest(this.refGenome));
        

        //Other database can be added as follows!
        
        return true;
    }

    public static void main(String[] argv){
        try {
            Configuration config = new Configuration();
            IPTR_tRNA tRNA = new IPTR_tRNA("");
            tRNA.refGenome = "hg38";
            tRNA.buildForest();
            
            ObjectOutputStream oos = null;
            ObjectInputStream ois = null;
                       
            oos = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(Configuration.strCurrDir+"/database/ann/GtRNAdb/GtRNAdb_"+tRNA.refGenome+".obj")));
            oos.writeObject(tRNA.dbtForest.get(0));
            oos.close();          
            
            ois = new ObjectInputStream(new GZIPInputStream(new FileInputStream(Configuration.strCurrDir+"/database/ann/GtRNAdb/GtRNAdb_"+tRNA.refGenome+".obj")));
            DBTree dbt = (DBTree) ois.readObject();
            ois.close();   
                       
            System.out.println("HeHeHeHe");
        } catch (FileNotFoundException ex) {
            LOG.error(ex.getMessage());
        } catch (IOException ex) {
            LOG.error(ex.getMessage());
        } catch (ClassNotFoundException ex) {
            LOG.error(ex.getMessage());
        }
    } 


    
}
