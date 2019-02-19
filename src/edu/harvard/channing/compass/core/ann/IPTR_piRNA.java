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
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.ValidationStringency;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class is used to annotate the piRNA information.
 *
 * @author Jiang Li
 * @version 1.0
 * @since 2017-09-24
 */
public class IPTR_piRNA extends IPTR {

    private static final Logger LOG = LogManager.getLogger(IPTR_piRNA.class.getName());

    public DBTree dbtCluster;

    public IPTR_piRNA(String strOut) {
        super(strOut);
        this.strCategory = "piRNA";
    }

    @Override
    public boolean buildForest() {
        dbtForest = new ArrayList<DBTree>();
        DB db = null;
        
        //Construct piRNABank
        db = Factory.getDB("piRNABank",this.boolCR);
        if (db != null) {
            dbtForest.add(db.getForest(this.refGenome));
        }        
        
        //Construct piRBase   
        db = Factory.getDB("piRBase",this.boolCR);
        if (db != null) {
            dbtForest.add(db.getForest(this.refGenome));
        }

        //Construct piRNACluster
        db = Factory.getDB("piRNACluster",this.boolCR);
        dbtCluster = db.getForest(this.refGenome);

        //Other databases can be added here. 

        return true;
    }

    @Override
    public boolean mergeTrees(){
        dbtMerge=new DBTree();
        dbtMerge.initTree("Merge");
        for(DBTree dis: this.dbtForest){
//            System.out.println("--------------------"+dis.strDB+"------------------------");
            for(String key:dis.hmpStart.keySet()){
                for(int pos: dis.hmpStart.get(key)){
                    for(DBLeaf dif : dis.hmpDB.get(key).get(pos)){
                        //Have annotation.
                        if(dif.hit>0){
//                            if(dif.other=="DQ571813"){
//                                System.out.println(dif.toRecord());
//                            }
                            dif.db=dis.strDB;
//                            dbtMerge.graftLeafByLocation(dif,this.fltOverlapRateRegion);
                            dbtMerge.graftLeafByIdentifier(dif,false);
                        }
                    }                 
                }
//                System.out.println(dis.strDB+"--->"+key);
            }
//            System.out.println("--------------------"+dis.strDB+"------------------------");
        }        
        return true;        
    }    
    
    @Override
    public boolean writeReport() {
        try {
            BufferedWriter bw = null;
            bw = new BufferedWriter(new FileWriter(strOutput));

            //Merge annotation results to one Hashmap.
            boolean boolFlag = this.mergeTrees();
            if (!boolFlag) {
                LOG.info("Annotation Error: Fail to write report of " + this.strCategory + "!");
                return boolFlag;
            }

            //Start to build txt file.
//            bw.write("DB\tName\tID\tContig\tStart\tEnd\tStrand\tCount\tInCluster");//title="DB\tName\tID\tContig\tStart\tEnd\tStrand\tCount\tInCluster";
            bw.write("DB\tName\tID\tCount\tInCluster");
            bw.newLine();

            for (String key : this.dbtMerge.hmpMerge.keySet()) {
                DBLeaf dif = this.dbtMerge.hmpMerge.get(key);
                dif.setCluster(this.dbtCluster.findLeaf(dif, this.fltOverlapRateRegion, false));
                if (this.isInCluster) {
                    if (dif.getCluster()) {
                        String strRecord = dif.getRecord(intThreshold);
                        if (strRecord == null) {
                            continue;
                        }
                        bw.write(strRecord);
                        bw.newLine();
                    }
                } else {
                    String strRecord = dif.getRecord(intThreshold);
                    if (strRecord == null) {
                        continue;
                    }
                    bw.write(strRecord);
                    bw.newLine();
                }
            }                     
            bw.close();
            LOG.info(this.strOutput+" was saved.");
            
            //For Debug Purpose.
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

    public static void main(String[] argv) {
//        try {
        Configuration config = new Configuration();
        IPTR_piRNA piRNA = new IPTR_piRNA("");
        piRNA.refGenome = "hg38";
        piRNA.buildForest();
        for(DBLeaf dbf:piRNA.dbtForest.get(0).hmpDB.get("chr16").get(70789509)){
            System.out.println(dbf.toRecord());
        }
        
        for (DBLeaf dbf : piRNA.dbtForest.get(1).hmpDB.get("chr16").get(70789509)) {
            System.out.println(dbf.toRecord());
        }    
//        DBTree dbt;
//        dbt=new DBTree();
//        dbt.initTree("piRBase");
//        DBLeaf_piRNA dbf=new DBLeaf_piRNA();
//        dbf.chr="chr5";
//        dbf.start=86372854;
//        dbf.end=86372884;
//        dbf.name="piR-hsa-2107";
//        dbf.other="DQ571813";
//        dbt.hmpStart=new HashMap();
        


        SamReader sr = SamReaderFactory.make().validationStringency(ValidationStringency.LENIENT).open(new File("E:\\01Work\\miRNA\\project\\COMPASS\\output\\S-001570893_CGATGT_L001_R1\\S-001570893_CGATGT_L001_R1_17to50_FitRead_STAR_Aligned.out.sam"));

        for (SAMRecord srdRead : sr) {
            boolean boolHit = false;
            if (srdRead.getReadUnmappedFlag()) {
                continue;
            }

            boolHit = piRNA.recordLeaf(srdRead);
        }
        
        for(DBLeaf dbf:piRNA.dbtForest.get(0).hmpDB.get("chr16").get(70789509)){
            System.out.println(dbf.toRecord());
        }
        
        for (DBLeaf dbf : piRNA.dbtForest.get(1).hmpDB.get("chr16").get(70789509)) {
            System.out.println(dbf.toRecord());
        }  
        
//            piRNA.mergeTrees();
        piRNA.strOutput = "E:\\01Work\\miRNA\\project\\COMPASS\\output\\temp.txt";
        piRNA.writeReport();

//            ObjectOutputStream oos = null;
//            ObjectInputStream ois = null;
//            
//            
//            oos = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(Configuration.strCurrDir + "/database/ann/piRBase/piRBase_" + piRNA.refGenome + ".obj")));
//            oos.writeObject(piRNA.dbtForest.get(0));
//            oos.close();
//            oos = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(Configuration.strCurrDir + "/database/ann/piRNABank/piRNABank_" + piRNA.refGenome + ".obj")));
//            oos.writeObject(piRNA.dbtForest.get(1));
//            oos.close();
//            oos = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(Configuration.strCurrDir + "/database/ann/piRNACluster/piRNACluster_" + piRNA.refGenome + ".obj")));
//            oos.writeObject(piRNA.dbtCluster);
//            oos.close();
//
//            DBTree dbt;
//            ois = new ObjectInputStream(new GZIPInputStream(new FileInputStream(Configuration.strCurrDir+"/database/ann/piRBase/piRBase_" + piRNA.refGenome + ".obj")));
//            dbt = (DBTree) ois.readObject();
//            ois.close(); 
//            ois = new ObjectInputStream(new GZIPInputStream(new FileInputStream(Configuration.strCurrDir + "/database/ann/piRNABank/piRNABank_" + piRNA.refGenome + ".obj")));
//            dbt = (DBTree) ois.readObject();
//            ois.close(); 
//            ois = new ObjectInputStream(new GZIPInputStream(new FileInputStream(Configuration.strCurrDir + "/database/ann/piRNACluster/piRNACluster_" + piRNA.refGenome + ".obj")));
//            dbt = (DBTree) ois.readObject();
//            ois.close();   
        System.out.println("HeHeHeHe");
//        } catch (FileNotFoundException ex) {
//            LogManager.getLogger(IPTR_miRNA.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (IOException ex) {
//            LogManager.getLogger(IPTR_miRNA.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (ClassNotFoundException ex) {
//            LogManager.getLogger(IPTR_miRNA.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }

}
