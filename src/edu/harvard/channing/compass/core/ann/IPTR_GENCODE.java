/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.core.ann;

import edu.harvard.channing.compass.core.Factory;
import edu.harvard.channing.compass.db.DB;
import edu.harvard.channing.compass.core.Configuration;
import edu.harvard.channing.compass.entity.DBLeaf;
import edu.harvard.channing.compass.entity.DBTree;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.zip.GZIPInputStream;

/**
 * This class is used to annotate the reads by GENCODE annotation. 
 * @author Jiang Li
 * @version 1.0
 * @since 2017-12-25
 */
public class IPTR_GENCODE extends IPTR{
    private static final Logger LOG = LogManager.getLogger(IPTR_GENCODE.class.getName());

    public IPTR_GENCODE(String strOut) {
        super(strOut);
        this.strCategory="GENCODE";
    }

    @Override
    public boolean buildForest() {
        dbtForest=new ArrayList<DBTree>();
        
        //Construct gencode           
        DB db=Factory.getDB("GENCODE",this.boolCR);
        if(db!=null)   dbtForest.add(db.getForest(this.refGenome)); 
//           db.pruneDB(dbtForest.get(0));
        return true;
    }

    public boolean mergeTrees(){
        dbtMerge=new DBTree();
        dbtMerge.initTree("Merge");
        for(DBTree dis: this.dbtForest){
            for(String key:dis.hmpStart.keySet()){
                for(int pos: dis.hmpStart.get(key)){
                    for(DBLeaf dif : dis.hmpDB.get(key).get(pos)){
                        //Have annotation.
                        if(dif.hit>0){
                            dif.db=dis.strDB;
                            dbtMerge.graftLeafByIdentifier(dif,dif.id,this.useUMI);
                        }
                    }                 
                }
            }
        }        
        return true;        
    }
    
    @Override
    public boolean writeReport() {
        try {
            BufferedWriter br = null;
            br = new BufferedWriter(new FileWriter(this.strOutput));

            //Merge annotation results to one Hashmap.
            boolean boolFlag = this.mergeTrees();
            if (!boolFlag) {
                LOG.info("Annotation Error: Fail to write report of " + this.strCategory + "!");
                return boolFlag;
            }

            //Start to build txt file.
            br.write("#DB\tName\tID\tCount\tType\tFeature\tContig\tStart\tEnd\tStrand");
            br.newLine();
            ArrayList<String> altKeys = new ArrayList(this.dbtMerge.hmpMerge.keySet());
            Collections.sort(altKeys);
            for (String key : altKeys) {
                DBLeaf dif = this.dbtMerge.hmpMerge.get(key);
                String strRecord = dif.getRecord(intThreshold);
                if (strRecord == null) {
                    continue;
                }
                br.write(strRecord);
                br.newLine();
            }
            
            br.close();
            return boolFlag;
        } catch (IOException ex) {
            LOG.error(ex.getMessage());
            return false;
        } 
    }

    
    
    
    public static void main(String[] argv){
        try {
            Configuration config = new Configuration();
            IPTR_GENCODE gencode = new IPTR_GENCODE("");
            gencode.refGenome = "hg19";
//            gencode.buildForest();
 
            
            ObjectOutputStream oos = null;
            ObjectInputStream ois = null;
            
            
//            oos = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(Configuration.strCurrDir+"/database/ann/GENCODE/gencode_"+gencode.refGenome+".obj")));
//            oos.writeObject(gencode.dbtForest.get(0));
//            oos.close();
//            
            
            ois = new ObjectInputStream(new GZIPInputStream(new FileInputStream(Configuration.strCurrDir+"/database/ann/GENCODE/gencode_"+gencode.refGenome+".obj")));
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
