/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.db;

import edu.harvard.channing.compass.core.Configuration;
import edu.harvard.channing.compass.entity.DBTree;
import edu.harvard.channing.compass.utility.ReadFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class is used to set circBase.
 * @author Jiang Li
 * @version 1.0
 * @since 2018-02-06
 */
public class DB_circBase extends DB{
    private static final Logger LOG = LogManager.getLogger(DB_circBase.class.getName());

    public DB_circBase(String strName) {
        super(strName);
    }

    public DB_circBase(String strName,boolean boolCR) {
        super(strName,boolCR);
    }
    
    @Override
    public DBTree getForest(String strRef) {
        String strDB;
        String strKey;
        String strLiftOver = null;
        boolean needLiftOver = false;
        
        switch(strRef){
            case "hg38":
                strKey="06010101";
                strObj=Configuration.BUNDLE_LOC+Configuration.hmpEndoDatabase.get(strKey).strLeafObj.split(";")[1];
                break;              
            case "hg19":
                strKey="06010101";
                strObj=Configuration.BUNDLE_LOC+Configuration.hmpEndoDatabase.get(strKey).strLeafObj.split(";")[0];
                break;
            case "mm10":
                strKey = "06010201";
                strObj=Configuration.BUNDLE_LOC+Configuration.hmpEndoDatabase.get(strKey).strLeafObj.split(";")[1];
                break;                
            case "mm9":
                strKey="06010201";
                strObj=Configuration.BUNDLE_LOC+Configuration.hmpEndoDatabase.get(strKey).strLeafObj.split(";")[0];
                break;
            default:
                if(strRef.contains("hg")){
                    strKey="06010101";
//                    strLiftOver = ReadFile.getLiftOverFile("hg19", strRef);
                }else{
                    strKey="06010201";
//                    strLiftOver = ReadFile.getLiftOverFile("mm9", strRef); 
                }                               
                needLiftOver = true;
                LOG.info("LiftOver Info: " + strLiftOver + " is used!");  
                break;         
        }        

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
                dbt=ReadFile.readColumnFile(strKey, strLiftOver, strDB, false, 1, 2, 3, 0, 4, 5);
            } else {
                dbt=ReadFile.readColumnFile(strKey, strDB, false, 1, 2, 3, 0, 4, 5);
            }
            dbt.strDB = "circRNA";
        }

        //Set isCovered marker. 
        this.pruneDB(dbt);
        
        //Write the obj file for use next. 
        this.writeObj(strObj, dbt);
        
        return dbt;
    }
    
    
}
