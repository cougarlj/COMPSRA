/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.db;

import edu.harvard.channing.compass.core.Configuration;
import edu.harvard.channing.compass.entity.DBLeaf;
import edu.harvard.channing.compass.entity.DBTree;
import edu.harvard.channing.compass.utility.ReadFile;
import java.util.HashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class is used to set piRBAse.
 * @author Jiang Li
 * @version 1.0
 * @since 2018-02-06
 */
public class DB_piRBase extends DB{
    private static final Logger LOG = LogManager.getLogger(DB_piRBase.class.getName());       
    
    public DB_piRBase(String strName) {
        super(strName);
    }
    
    public DB_piRBase(String strName,boolean boolCR) {
        super(strName,boolCR);
    }
    
    public DBTree getForest(String strRef){
        String strDB;       
        String strKey;
        String strLiftOver = null;
        boolean needLiftOver=false;
        
        switch (strRef) {
            case "hg38":
                strKey = "02020101";
                strObj=Configuration.BUNDLE_LOC+Configuration.hmpEndoDatabase.get(strKey).strLeafObj.split(";")[1];
                break;
            case "hg19":
                strKey = "02020101";
                strObj=Configuration.BUNDLE_LOC+Configuration.hmpEndoDatabase.get(strKey).strLeafObj.split(";")[0];
                break;
            case "mm10":
                strKey = "02020201";
                strObj=Configuration.BUNDLE_LOC+Configuration.hmpEndoDatabase.get(strKey).strLeafObj.split(";")[1];
                break;
            case "mm9":
                strKey = "02020201";
                strObj=Configuration.BUNDLE_LOC+Configuration.hmpEndoDatabase.get(strKey).strLeafObj.split(";")[0];
                break;
            default:
                if (strRef.contains("hg")) {
                    strKey = "02020101";
//                    strLiftOver = ReadFile.getLiftOverFile("hg19", strRef);//Trans from "hg19" to refGenome.
                } else {
                    strKey = "02020201";
//                    strLiftOver = ReadFile.getLiftOverFile("mm9", strRef);//Trans from "mm9" to refGenome.
                }
//                needLiftOver = true;
//                LOG.info("LiftOver Info: " + strLiftOver + " is used!");
                break;                
        }                     
       
        //Try Obj file first. 
        DBTree dbt=null;
        if (strObj != null) {
            if(this.boolCR) this.checkResource(strObj,false);
            dbt = ReadFile.readObj(strObj);
            if (dbt != null) {
                return dbt;
            }
        }
        
        if (strObj == null || dbt == null) {
            strDB = Configuration.BUNDLE_LOC+Configuration.hmpEndoDatabase.get(strKey).strLeafDownloadAnn;
            if(this.boolCR) this.checkResource(strDB,false);
            LOG.warn("The prebuilt database file "+this.strObj+" doesn't exist!");
            LOG.info("Try to build the database from local files "+strDB+" !");    
                       
            if (strRef.startsWith("hg")) {
                if (!strRef.contains("hg19")) {
                    needLiftOver = true;
                    strLiftOver = ReadFile.getLiftOverFile("hg19", strRef);
                    LOG.info("LiftOver Info: " + strLiftOver + " is used!\n");
                }
            } else if (strRef.startsWith("mm")) {
                if (!strRef.contains("mm9")) {
                    needLiftOver = true;
                    strLiftOver = ReadFile.getLiftOverFile("mm9", strRef);
                    LOG.info("LiftOver Info: " + strLiftOver + " is used!\n");
                }
            }            
            
            if (needLiftOver) {
                dbt= ReadFile.readColumnFile(strKey, strLiftOver, strDB, false, 0, 1, 2, 3, 5, 3,1);
            } else {
                dbt= ReadFile.readColumnFile(strKey, strDB, false, 0, 1, 2, 3, 5, 3,1);
            }
            dbt.strDB=this.strName;            
        }

        //Add accession ID . 
        if (strRef.contains("hg")) {
            HashMap<String, String> hmpAcc = ReadFile.getHMP(Configuration.OTHERS.get("piRBaseID_hg.txt"), 0, 1);
            for (String key : dbt.hmpStart.keySet()) {
                for (int pos : dbt.hmpStart.get(key)) {
                    for (DBLeaf dif : dbt.hmpDB.get(key).get(pos)) {
                        dif.other = hmpAcc.get(dif.name);
                    }
                }
            }
        }else if (strRef.contains("mm")){
            //To be finished. 
        }


        //Set isCovered marker. 
        this.pruneDB(dbt);
        
        //Write the obj file for use next. 
        this.writeObj(strObj, dbt);
        LOG.info("The prebuilt database was saved in "+strObj+" .");
        
        return dbt;
    }
 
    
    
}
