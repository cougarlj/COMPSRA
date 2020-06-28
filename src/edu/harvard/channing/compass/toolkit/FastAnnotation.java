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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This Class is used to give a fast annotation of a position in the genome. 
 * @author Jiang Li
 */
public class FastAnnotation implements ToolKit{
    private static final Logger LOG = LogManager.getLogger(FastAnnotation.class.getClass());
    
    public String strIn;
    public String strOut;
    public String strDB;
    public String strGFF3;
    public String strRef="hg38";
    ArrayList<DBTree> dbt;
    
    public int initDB(String strDB) {
        this.dbt = new ArrayList();
        if (strDB == null) {
            if (strGFF3 == null) {
                LOG.error("There is no database and region file set.");
                return 0;
            } else {
                dbt.add(ReadFile.readGFF3(strGFF3));
            }
        } else {
            String[] strItem = this.strDB.split(",|;");
            for (String strTmp : strItem) {
                DB db = Factory.getDB(strTmp, true);
                DBTree dbT = db.getForest(this.strRef);
                this.dbt.add(dbT);
            }
        }
        return 1;
    }
    
    public String getAnn(String chrom, int pos) {
        StringBuilder sbAnn = new StringBuilder();

        for (int i = 0; i < this.dbt.size(); i++) {
            String strAnn = dbt.get(i).findLeaf(chrom, pos);
            if (strAnn == null) {
                continue;
            } else {
                sbAnn.append(strAnn);
                sbAnn.append(";");
            }
        }
        
        if(sbAnn.length()==0)   return ".";
        else    return sbAnn.toString();      
    }    
    

    @Override
    public int runKit() {
        try {
            
            this.initDB(this.strDB);
            
            BufferedReader br=Factory.getReader(this.strIn);
            if(this.strOut==null){
                this.strOut=this.strIn.split("\\.")[0]+".ann.txt";
            }
            BufferedWriter bw=Factory.getWriter(this.strOut);
            bw.write(br.readLine().trim()+"\tann");
            bw.newLine();
            for (String strLine = br.readLine(); strLine != null; strLine=br.readLine()) {
                String[] strLoc=strLine.trim().split("\t");
                String strAnn=this.getAnn(strLoc[0],Integer.valueOf(strLoc[1]));
                bw.write(strLine.trim()+"\t"+strAnn);
                bw.newLine();
            }
            
            br.close();
            bw.close();
        } catch (IOException ex) {
            LOG.error(ex.getMessage());
            return 0;
        }

        return 1;
    }
    
    public static void main(String[] argv){
        Configuration config=new Configuration();
        FastAnnotation faTest=new FastAnnotation();
        faTest.strIn="E:\\01Work\\ASE\\result\\result_v4\\CRA_354_miRNA_Filter_location.txt";
        faTest.strDB="miRBase";
        faTest.runKit();
        System.out.println("Finish!");
    }
    
}
