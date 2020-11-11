/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.db;

import edu.harvard.channing.compass.core.Configuration;
import edu.harvard.channing.compass.core.Factory;
import edu.harvard.channing.compass.entity.DBTree;
import edu.harvard.channing.compass.utility.ReadFile;
import java.io.BufferedReader;
import java.util.HashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class is used to set miRBase.
 * @author Jiang Li
 * @version 1.0
 * @since 2018-02-06
 */
public class DB_miRBase extends DB{

    private static final Logger LOG = LogManager.getLogger(DB_miRBase.class.getName()); 
    public HashMap<String,String> hmpSEQ;
    public DB_miRBase(String strName,boolean boolCR) {
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
                strKey = "01010101";
                strObj=Configuration.BUNDLE_LOC+Configuration.hmpEndoDatabase.get(strKey).strLeafObj.split(";")[1];
                break;
            case "hg19":
                strKey = "01010102";
                strObj=Configuration.BUNDLE_LOC+Configuration.hmpEndoDatabase.get(strKey).strLeafObj.split(";")[0];
                break;
            case "mm10":
                strKey = "01010201";
                strObj=Configuration.BUNDLE_LOC+Configuration.hmpEndoDatabase.get(strKey).strLeafObj.split(";")[1];
                break;
            case "mm9":
                strKey = "01010202";
                strObj=Configuration.BUNDLE_LOC+Configuration.hmpEndoDatabase.get(strKey).strLeafObj.split(";")[0];
                break;
            case "rno6":
                strKey="01010301";
                strObj=Configuration.BUNDLE_LOC+Configuration.hmpEndoDatabase.get(strKey).strLeafObj.split(";")[1];
                break;
            case "rno5":
                strKey="01010302";
                strObj=Configuration.BUNDLE_LOC+Configuration.hmpEndoDatabase.get(strKey).strLeafObj.split(";")[0];
                break;            
            default:
                if (strRef.contains("hg")) {
                    strKey = "01010101";
                    strLiftOver = ReadFile.getLiftOverFile("hg38", strRef);//Trans from "hg38" to refGenome.
                } else if (strRef.contains("mm")) {
                    strKey = "01010201";
                    strLiftOver = ReadFile.getLiftOverFile("mm10", strRef);
                }else{
                    strKey="01010301";
                    strLiftOver = ReadFile.getLiftOverFile("rno6", strRef);
                }
                needLiftOver = true;
                LOG.info("LiftOver Info: " + strLiftOver + " is used!");
                break;
        }
        
        //Try Obj file first. 
        DBTree dbt=null;
        if (strObj != null) {           
            if(this.boolCR)    this.checkResource(strObj,false);
            dbt = ReadFile.readObj(this.strObj);
            if (dbt != null) {
                return dbt;              
            }
        }
        
        //If no obj file, try original text file. 
        if (strObj == null || dbt == null) {
            strDB = Configuration.BUNDLE_LOC+Configuration.hmpEndoDatabase.get(strKey).strLeafDownloadAnn;
            if(this.boolCR)    this.checkResource(strDB, false);         
            LOG.warn("The prebuilt database file "+this.strObj+" doesn't exist!");
            LOG.info("Try to build the database from local files "+strDB+" !");   
            
            //Add this part temporarily and will modify in new version.            
            hmpSEQ=ReadFile.readFA("miRBase_v21");
                        
            if (needLiftOver) {
                dbt=ReadFile.readGFF3(strKey, strLiftOver, strDB);
            } else {
                dbt=ReadFile.readGFF3(strKey, strDB);
            }
            dbt.strDB = "miRBase";
        }

        //Set isCovered marker. 
        this.pruneDB(dbt);
        
        //Write the obj file for use next. 
        this.writeObj(strObj, dbt);
        LOG.info("The prebuilt database was saved in "+strObj+" .");
        
        return dbt;
    }
}
