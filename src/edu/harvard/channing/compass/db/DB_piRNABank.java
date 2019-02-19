/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.db;

import edu.harvard.channing.compass.core.Configuration;
import edu.harvard.channing.compass.entity.DBLeaf;
import edu.harvard.channing.compass.entity.DBLeaf_piRNA;
import edu.harvard.channing.compass.entity.DBTree;
import edu.harvard.channing.compass.utility.ReadFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class is used to set piRNABank.
 * @author Jiang Li
 * @version 1.0
 * @since 2018-02-06
 */
public class DB_piRNABank extends DB {
    private static final Logger LOG = LogManager.getLogger(DB_piRNABank.class.getName()); 
    
    public DB_piRNABank(String strName) {
        super(strName);
    }
    
    public DB_piRNABank(String strName,boolean boolCR) {
        super(strName,boolCR);
    }
    
    @Override
    public DBTree getForest(String strRef) {
        String strDB;
        String strKey;
        String strLiftOver = null;
        boolean needLiftOver = false;
        switch (strRef) {
            
            case "hg38":
                strKey = "02010101";
                strObj=Configuration.BUNDLE_LOC+Configuration.hmpEndoDatabase.get(strKey).strLeafObj.split(";")[1];
                break;
            case "hg19":
                strKey = "02010101";
                strObj=Configuration.BUNDLE_LOC+Configuration.hmpEndoDatabase.get(strKey).strLeafObj.split(";")[0];
                break;
            case "mm10":
                strKey = "02010201";
                strObj=Configuration.BUNDLE_LOC+Configuration.hmpEndoDatabase.get(strKey).strLeafObj.split(";")[1];
                break;
            case "mm9":
                strKey = "02010201";
                strObj=Configuration.BUNDLE_LOC+Configuration.hmpEndoDatabase.get(strKey).strLeafObj.split(";")[0];
                break;
            default:
                if (strRef.contains("hg")) {
                    strKey = "02010101";
//                    strLiftOver = ReadFile.getLiftOverFile("hg18", strRef);//Trans from "hg18" to refGenome.
                } else {
                    strKey = "02010201";
//                    strLiftOver = ReadFile.getLiftOverFile("mm8", strRef);//Trans from "mm8" to refGenome.
                }
//                needLiftOver = true;
//                LOG.info("LiftOver Info: " + strLiftOver + " is used!");
                break;                   
        }              
        
        //Try Obj file first. 
        DBTree dbt=null;
        if (strObj != null) {
            if(this.boolCR) this.checkResource(strObj,false);
            dbt = ReadFile.readObj(this.strObj);            
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
                if (!strRef.contains("hg18")) {
                    needLiftOver = true;
                    strLiftOver = ReadFile.getLiftOverFile("hg18", strRef);
                    LOG.info("LiftOver Info: " + strLiftOver + " is used!\n");
                }
            } else if (strRef.startsWith("mm")) {
                if (!strRef.contains("mm8")) {
                    needLiftOver = true;
                    strLiftOver = ReadFile.getLiftOverFile("mm8", strRef);
                    LOG.info("LiftOver Info: " + strLiftOver + " is used!\n");
                }
            }

            
            if (needLiftOver) {
                dbt=ReadFile.readColumnFile(strKey, strLiftOver, strDB, false, 1, 2, 3, 0, 4, 5);
            } else {
                dbt=ReadFile.readColumnFile(strKey, strDB, false, 1, 2, 3, 0, 4, 5);
            }
            dbt.strDB = "piRNABank";
            
        }

        //Set isCovered marker. 
        this.pruneDB(dbt);
        
        //Write the obj file for use next. 
        this.writeObj(strObj, dbt);
        LOG.info("The prebuilt database was saved in "+strObj+" .");
        
        return dbt;
    }
    
}
