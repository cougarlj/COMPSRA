/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.db;

import edu.harvard.channing.compass.core.Configuration;
import edu.harvard.channing.compass.entity.DBLeaf;
import edu.harvard.channing.compass.entity.DBTree;
import edu.harvard.channing.compass.utility.Download;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.zip.GZIPOutputStream;

/**
 * This class is used to set database.
 * @author Jiang Li
 * @version 1.0
 * @since 2018-02-06
 */
public abstract class DB {
    private static final Logger LOG = LogManager.getLogger(DB.class.getName());
    
    public String strName;
    public String strObj = null;
    public boolean boolCR=false;

    public DB(String strName) {
        this.strName = strName;
    }
    
    public DB(String strName,boolean boolCR) {
        this.strName = strName;
        this.boolCR=boolCR;
    }

    
    public abstract DBTree getForest(String strRef);
    
    public void writeObj(String strObj,DBTree dbt){
        ObjectOutputStream oos=null;
        try {
            oos = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(strObj)));
            oos.writeObject(dbt);
            oos.close();
        } catch (IOException ex) {
            LOG.error(ex.getMessage());
        } finally {
            try {
                oos.close();
            } catch (IOException ex) {
                LOG.error(ex.getMessage());
            }
        }
    }    
 
    public void pruneDB(DBTree db){
        try {
            for (String chr : db.hmpStart.keySet()) {
                int g_maxEnd = 0;
                for (int pos : db.hmpStart.get(chr)) {
                    ArrayList<DBLeaf> alt = db.hmpDB.get(chr).get(pos);

                    int l_maxEnd = 0;
                    for (DBLeaf dbf : alt) {
//                    if("FCGR2A".equals(dbf.name)){
//                        System.out.println(dbf.getRecord());
//                    }
                        if (dbf.end > l_maxEnd) {
                            l_maxEnd = dbf.end;
                        }
                    }

                    if (pos < g_maxEnd) {
                        for (DBLeaf dbf : alt) {
                            dbf.isCovered = true;
                        }
                    } else {
                        for (DBLeaf dbf : alt) {
                            if (dbf.end < l_maxEnd) {
                                dbf.isCovered = true;
                            }
                        }
                        g_maxEnd = l_maxEnd;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }
    
    public void checkResource(String strLoc, boolean needUnCompress){
        File fleLoc = new File(strLoc);
        if (!fleLoc.exists()) {
            Download ddJob=new Download(strLoc,needUnCompress);
            boolean boolFlag=ddJob.download();
            if(!boolFlag){
                LOG.info(strLoc+" Check ---> Fail!");
            }
        }
    }
    
}
