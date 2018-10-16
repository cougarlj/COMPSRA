/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.db;

import edu.harvard.channing.compass.core.ann.IPTR_snoRNA;
import edu.harvard.channing.compass.core.Configuration;
import edu.harvard.channing.compass.entity.DBTree;
import edu.harvard.channing.compass.utility.ReadFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class is used to annotate the snoRNA information. 
 * @author Jiang Li
 * @version 1.0
 * @since 2017-10-07
 */
public class DB_GEN_snoRNA extends DB{

    private static final Logger LOG = LogManager.getLogger(IPTR_snoRNA.class.getName());
    
    public DB_GEN_snoRNA(String strName) {
        super(strName);
    }
    
    public DB_GEN_snoRNA(String strName,boolean boolCR) {
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
                strKey="04010101";
                strObj=Configuration.BUNDLE_LOC+Configuration.hmpEndoDatabase.get(strKey).strLeafObj.split(";")[1];
                break;
            case "hg19":
                strKey="04010102";
                strObj=Configuration.BUNDLE_LOC+Configuration.hmpEndoDatabase.get(strKey).strLeafObj.split(";")[0];
                break;
            case "mm10":
                strKey="04010201";
                strObj=Configuration.BUNDLE_LOC+Configuration.hmpEndoDatabase.get(strKey).strLeafObj.split(";")[1];
                break;
            case "mm9":
                strKey="04010202";
                strObj=Configuration.BUNDLE_LOC+Configuration.hmpEndoDatabase.get(strKey).strLeafObj.split(";")[0];
                break;
            default:
                if(strRef.contains("hg")){
                    strKey="04010101";
                    strLiftOver = ReadFile.getLiftOverFile("hg38", strRef);
                }                                                         
                else{
                    strKey="04010201";
                    strLiftOver = ReadFile.getLiftOverFile("mm10", strRef); 
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
            
            
            if (needLiftOver) {
                dbt=ReadFile.readGFF3(strKey, strLiftOver, strDB);
                LOG.info("LiftOver Info: " + strLiftOver + " is used!\n");
            } else {
                dbt=ReadFile.readGFF3(strKey, strDB);
            }
            dbt.strDB = "GEN_snoRNA";
        }


        //Set isCovered marker. 
        this.pruneDB(dbt);
        
        //Write the obj file for use next. 
        this.writeObj(strObj, dbt);
        
        return dbt;
    }
    
}
