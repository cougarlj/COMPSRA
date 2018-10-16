/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.entity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class is designed to keep record of the database used in COMPASS pipeline. 
 * @author Jiang Li
 * @version 1.0 
 * @since 2017-08-30
 */
public class DatabaseInfo {
    private static final Logger LOG = LogManager.getLogger(DatabaseInfo.class.getName());
    
    //Identification
    public String strKeyID;
    
    //Database Properties.
    public String strDatabaseID;
    public String strDatabaseName;
    public String strDatabaseRelease;
    public String strDatabaseWebpage;
    
    //DataLeaf Properties.
    public String strLeafID;
    public String strRefVersion;
    public String strLeafSpecies;
    public String strLeafTaxonomy;
    public String strLeafDownloadGenome;
    public boolean boolIsLeafBuildIn;
    public String strLeafBuildPath;
    public String strLeafDependency;
    public String strLeafDownloadAnn;
    public String strLeafObj;

    
}
