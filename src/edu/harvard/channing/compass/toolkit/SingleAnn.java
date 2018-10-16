/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.toolkit;

import edu.harvard.channing.compass.core.Factory;
import edu.harvard.channing.compass.entity.DBLeaf;
import edu.harvard.channing.compass.entity.DBTree;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.ValidationStringency;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class is used to get the mapped reads for the input annotation.  
 * @author Jiang Li
 * @version 1.0 
 * @since 2018-05-26
 */
public class SingleAnn implements ToolKit{
    private static final Logger LOG = LogManager.getLogger(SingleAnn.class.getClass());
    
    /**
     * The input item should follow the format: chr2|176150329|176150351|+|hsa-miR-10b-5p|miRBase;chr21|16539838|16539859|+|hsa-let-7c-5p|miRBase. 
     */
    public String strAnn;
    public String strSamFile;
    public String strFQFile;
    public float fltOverlap=1;
    public DBTree dbt;
    BufferedWriter bw;

 
    
    public void buildTree(){
        this.dbt=new DBTree();
        this.dbt.initTree("Ann");
               
        String[] strSingle=strAnn.split(";");
        for(String strOne:strSingle){
            String[] strItem=strOne.trim().split("\\|");
            DBLeaf dbf=new DBLeaf();
            dbf.chr=strItem[0];
            dbf.start=Integer.valueOf(strItem[1]);
            dbf.end=Integer.valueOf(strItem[2]);
            dbf.strand=strItem[3];
            dbf.name=strItem[4];
            dbf.db=strItem[5];
            this.dbt.addLeaf(dbf.chr, dbf);
        }       
    }
    
    public void annTree() {
        try {
            SamReader sr = SamReaderFactory.make().validationStringency(ValidationStringency.LENIENT).open(new File(this.strSamFile));           
            bw=Factory.getWriter(strFQFile);
            
            for (SAMRecord srdRead : sr) {               
                if (srdRead.getReadUnmappedFlag()) {
                    continue;
                }
                boolean boolHit=this.dbt.findLeaf(srdRead, fltOverlap, true);
                if(boolHit){
                    this.sam2fq(srdRead);
                }
            }
            
            sr.close();
            bw.close();
        } catch (IOException ex) {
            LOG.error(ex.getMessage());
        }

    }
 
    public void sam2fq(SAMRecord srdRead){
        try {
            bw.write("@"+srdRead.getReadName());
            bw.newLine();
            bw.write(srdRead.getReadString());
            bw.newLine();
            bw.write("+");
            bw.newLine();
            bw.write(srdRead.getBaseQualityString());
            bw.newLine();
        } catch (IOException ex) {
            LOG.error(ex.getMessage());
        }
    }
    
    @Override
    public int runKit() {
        try {
            this.buildTree();
            this.annTree();
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            return 0;
        }
        return 1;
    }
    
}
