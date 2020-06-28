/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.core.ann;

import edu.harvard.channing.compass.core.Factory;
import edu.harvard.channing.compass.db.DB;
import edu.harvard.channing.compass.core.Configuration;
import edu.harvard.channing.compass.entity.DBLeaf;
import edu.harvard.channing.compass.entity.DBTree;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.collections4.CollectionUtils;

/**
 * This class is used to annotate the miRNA information. 
 * @author Jiang Li
 * @version 1.0
 * @since 2017-09-24
 */
public class IPTR_miRNA extends IPTR {
    private static final Logger LOG = LogManager.getLogger(IPTR_miRNA.class.getName());

    public IPTR_miRNA(String strOut) {
        super(strOut);
        this.strCategory="miRNA";
    }
    
    @Override
    public boolean buildForest() {
        dbtForest=new ArrayList<>();
        
        //Construct miRBase           
        DB db=Factory.getDB("miRBase",this.boolCR);
        if(db!=null)   dbtForest.add(db.getForest(this.refGenome));        
                
        //Other databases can be added as follows!
        
        return true;
    }

    @Override
    public boolean writeReport(){
        try {
            BufferedWriter bw = null;
            bw = new BufferedWriter(new FileWriter(this.strOutput));
     
            //Merge annotation results to one Hashmap.
            boolean boolFlag = this.mergeTrees();                              
            if (!boolFlag) {
                LOG.info("Annotation Error: Fail to write report of " + this.strCategory + "!");
                return boolFlag;
            }
            
            //Deal with primary_transcript. 
            //mir=total-miRNA.
            this.pruneTree(dbtMerge); 
            
            //Start to build txt file.
//            bw.write("DB\tName\tID\tContig\tStart\tEnd\tStrand\tCount");//title="DB\tName\tID\tContig\tStart\tEnd\tStrand\tCount";
            bw.write("DB\tName\tID\tCount");
            bw.newLine();

            for(String key:this.dbtMerge.hmpMerge.keySet()){
                DBLeaf dif=this.dbtMerge.hmpMerge.get(key);
                String strRecord=dif.getRecord(intThreshold);
                if(strRecord==null) continue;
                bw.write(strRecord);
                bw.newLine();
            }           
           
            bw.close();
            LOG.info(this.strOutput+" was saved.");
            
            if (this.needDetail) {
                bw = new BufferedWriter(new FileWriter(this.strOutput+".detail"));
                for (String key : this.dbtMerge.hmpMerge.keySet()) {
                    DBLeaf dif = this.dbtMerge.hmpMerge.get(key);
                    String strRecord = dif.getDetail(intThreshold);
                    if (strRecord == null) {
                        continue;
                    }
                    bw.write(strRecord);
//                    bw.newLine();
                }
            }
            bw.close();        
            
            
            return boolFlag;
        } catch (IOException ex) {
            LOG.error(ex.getMessage());
            return false;
        }           
    }
       
 
    private void pruneTree(DBTree dbtMerge) {
        //Build index. 
        HashMap<String,ArrayList<DBLeaf>> hmpID2Leaf=new HashMap();                     
        for (DBLeaf dbf : dbtMerge.hmpMerge.values()) {
            try {
                if (dbf.derives_from != null) {
                    String strID[] = dbf.derives_from.split("\\|");
                    for (String str : strID) {
                        if (!hmpID2Leaf.containsKey(str)) {
                            hmpID2Leaf.put(str, new ArrayList<DBLeaf>());
                        }
                        hmpID2Leaf.get(str).add(dbf);
                    }
                }
            } catch (SecurityException ex) {
                LOG.error(ex.getMessage());
            }
        }
        
        //Reduce overlap. 
        for(Map.Entry<String,DBLeaf> entry : dbtMerge.hmpMerge.entrySet()){
            if(entry.getValue().derives_from==null){
                ArrayList<DBLeaf> altDBF=hmpID2Leaf.get(entry.getValue().id);
                if(altDBF!=null){
                    for(DBLeaf dbl:altDBF){
                        entry.getValue().lstReads=(List)CollectionUtils.subtract(entry.getValue().lstReads,dbl.lstReads);
                        entry.getValue().hit=entry.getValue().lstReads.size();
                    }
                }
            }
        }        
    }
        
    public static void main(String[] argv){
        try {
            Configuration config = new Configuration();
            IPTR_miRNA miRNA = new IPTR_miRNA("");
            miRNA.refGenome = "hg19";
            miRNA.buildForest();
 
            
            ObjectOutputStream oos = null;
            ObjectInputStream ois = null;
            
            
            oos = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(Configuration.strCurrDir+"/database/ann/miRBase/miRBase_"+miRNA.refGenome+".obj")));
            oos.writeObject(miRNA.dbtForest.get(0));
            oos.close();
            
            
            ois = new ObjectInputStream(new GZIPInputStream(new FileInputStream(Configuration.strCurrDir+"/database/ann/miRBase/miRBase_"+miRNA.refGenome+".obj")));
            DBTree dbt = (DBTree) ois.readObject();
            ois.close();              
        } catch (FileNotFoundException ex) {
            LOG.error(ex.getMessage());
        } catch (IOException ex) {
            LOG.error(ex.getMessage());
        } catch (ClassNotFoundException ex) {
            LOG.error(ex.getMessage());
        }
    }



}
