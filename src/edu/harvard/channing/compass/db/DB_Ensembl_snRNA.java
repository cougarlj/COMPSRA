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
 * This class is used to annotate the snRNA information from Ensembl database. 
 * @author Jiang Li
 * @version 1.0
 * @since 2018-05-08
 */
public class DB_Ensembl_snRNA extends DB{
    private static final Logger LOG = LogManager.getLogger(DB_Ensembl_snRNA.class.getName());
    
    public DB_Ensembl_snRNA(String strName) {
        super(strName);
    }
    
    public DB_Ensembl_snRNA(String strName,boolean boolCR) {
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
                strKey="05020101";
                strObj=Configuration.BUNDLE_LOC+Configuration.hmpEndoDatabase.get(strKey).strLeafObj.split(";")[1];
                break;
            case "hg19":
                strKey="05020102";
                strObj=Configuration.BUNDLE_LOC+Configuration.hmpEndoDatabase.get(strKey).strLeafObj.split(";")[0];
                break;
            case "mm10":
                strKey="05020201";
                break;
            case "mm9":
                strKey="05020202";
                break;
            default:
                if(strRef.contains("hg")){
                    strKey="05020101";
                    strLiftOver = ReadFile.getLiftOverFile("hg38", strRef);
                }else{
                    strKey="05020201";
                    strLiftOver = ReadFile.getLiftOverFile("mm10", strRef); 
                }                               
                needLiftOver = true;
                LOG.info("LiftOver Info: " + strLiftOver + " is used!");  
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
            try {
                strDB = Configuration.BUNDLE_LOC + Configuration.hmpEndoDatabase.get(strKey).strLeafDownloadAnn;
            } catch (Exception e) {
                LOG.error(e.getMessage());
                return null;
            }
            
            if(this.boolCR) this.checkResource(strDB,false);
            LOG.warn("The prebuilt database file "+this.strObj+" doesn't exist!");
            LOG.info("We will try to build the database from local files "+strDB+" !");       
            
            //Currently, only hg19 and hg38 are supported. 
            if (needLiftOver) {
                dbt=ReadFile.readGFF3(strKey, strLiftOver, strDB);
                LOG.info("LiftOver Info: " + strLiftOver + " is used!\n");
            } else {
                dbt=ReadFile.readGFF3(strKey, strDB);
            }
            dbt.strDB = "ENS_snRNA";
        }

        //Set isCovered marker. 
        this.pruneDB(dbt);
        
        //Write the obj file for use next. 
        this.writeObj(strObj, dbt);
        LOG.info("The prebuilt database was saved in "+strObj+" .");
        
        return dbt;
    }
    
}
