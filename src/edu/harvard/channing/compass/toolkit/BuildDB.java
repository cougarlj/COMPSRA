/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.toolkit;

import edu.harvard.channing.compass.core.Configuration;
import edu.harvard.channing.compass.core.Factory;
import edu.harvard.channing.compass.db.DB;
import edu.harvard.channing.compass.entity.DBTree;
import edu.harvard.channing.compass.utility.ReadFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This Class is used to build COMPSRA built-in database.
 * @author rejia(Jiang Li)
 * @version 1.0.1 
 * @since 2020-06-26
 */
public class BuildDB implements ToolKit{
    private static final Logger LOG = LogManager.getLogger(BuildDB.class.getClass());

    public String strIn;
    public String strOut;
    public String strDB;
    public String strKey;
    public DBTree dbt;
    @Override
    public int runKit() {
        
        DB db=Factory.getDB(strDB, false);
        dbt=ReadFile.readGFF3(strKey, strIn);
        dbt.strDB=this.strDB;
        db.pruneDB(dbt);
        db.writeObj(strOut, dbt);
        LOG.info("The prebuilt database was saved in "+strOut+" .");
        
        return 1;
    }
 
    public static void main(String[] argv){
        Configuration config=new Configuration();
        BuildDB bdb=new BuildDB();
        bdb.strIn="E:\\01Work\\miRNA\\project\\COMPSRA\\bundle_v1\\db\\miRBase\\miRBase_rno5.gff3";
        bdb.strOut="E:\\01Work\\miRNA\\project\\COMPSRA\\bundle_v1\\db\\miRBase\\miRBase_rno5.obj";
        bdb.strDB="miRBase";
        bdb.strKey="01010302";
        bdb.runKit();
        System.out.println("CheckPoint.");
    }
}
