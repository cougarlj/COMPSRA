/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.core.ann;

import edu.harvard.channing.compass.core.Configuration;
import edu.harvard.channing.compass.core.Factory;
import htsjdk.samtools.SAMFileWriter;
import htsjdk.samtools.SAMFileWriterFactory;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.ValidationStringency;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This file is used to annotate the file with bowtie sam format.
 * @author Jiang Li
 * @version 1.0
 * @since 2017-09-23
 */
public class SAMAnnotator extends Annotator {
    private static final Logger LOG = LogManager.getLogger(SAMAnnotator.class.getName());
    
    public String strStyle;
    public IPTR[] iptr;
//    public boolean rmSAM = false;
    public StringBuilder sb;

    public SAMAnnotator(String strStyle) {
        this.strStyle = strStyle;
    }

    /**
     * This function is used to fulfill the whole annotation.
     * @return Info of result.
     */
    @Override
    public String Annotate() {
        try {
            Iterator<String> itr = this.frd.output_ann.keySet().iterator();
            while (itr.hasNext()) {
                String strIn = itr.next();
                
                //Initialize SAM Reader and Writer.
                File fleIn=new File(strIn);
                if(!fleIn.exists()){
                    fleIn=new File(strIn.replaceAll("$.sam", ".bam"));
                }
                SamReader sr = SamReaderFactory.make().validationStringency(ValidationStringency.LENIENT).open(fleIn);
                SAMFileWriter sfwUnMapped = new SAMFileWriterFactory().makeBAMWriter(sr.getFileHeader(), false, new File(this.frd.output_unmap.get(strIn)));
                SAMFileWriter sfwUnAnnotated=null;
                if (this.boolShowUnAnn) {
                    sfwUnAnnotated = new SAMFileWriterFactory().makeBAMWriter(sr.getFileHeader(), false, new File(this.frd.output_dir + this.frd.output_prefix + Configuration.ANN_OUT[1]));
                }
                
                //Initialize Interpreters.
                ArrayList<String> alt = this.frd.output_ann.get(strIn);
                this.iptr = new IPTR[alt.size()];
                for (int i = 0; i < alt.size(); i++) {
                    //prepare resource database.
                    iptr[i] = Factory.getInterpreter(alt.get(i));
                    iptr[i].fltOverlapRateRead = this.fltOverlap;
                    iptr[i].refGenome = frd.strRef;
                    iptr[i].isInCluster = this.isInCluster;
                    iptr[i].intThreshold=this.intTD;
                    iptr[i].boolCR=this.boolCR;
                    if (!iptr[i].buildForest()) {
                        return "Annotation Error: Failed to prepare the database resource for " + iptr[i].getCategoryName() + "!";
                    }
                    if(needBAMOutput){
                        iptr[i].setBAMOutput(sr);
                    }
                }

                //Start to annotate.
                for (SAMRecord srdRead : sr) {
                    boolean boolHit = false;
                    if (srdRead.getReadUnmappedFlag()) {
                        sfwUnMapped.addAlignment(srdRead);
                        continue;
                    }
                    for (int i = 0; i < iptr.length; i++) {
                        boolHit = boolHit | iptr[i].recordLeaf(srdRead);
                    }
                    if (!boolHit && sfwUnAnnotated!=null) {
                        sfwUnAnnotated.addAlignment(srdRead);
                    }
                }
                
                //Close FileStreams. 
                for(IPTR iptrRNA : iptr){
                    iptrRNA.closeBAMOutput();
                }
                sr.close();
                sfwUnMapped.close();
                if(sfwUnAnnotated!=null)    sfwUnAnnotated.close();

                //Start to integrate and output results.
                for (int i = 0; i < iptr.length; i++) {
                    boolean boolFlag = iptr[i].writeReport();
                    if (!boolFlag) {
//                LOG.info("Annotation Error: Fail to write report of "+iptr[i].getCategoryName()+" !");
                        return "Annotation Error: Fail to write report of " + iptr[i].getCategoryName() + " !";
                    }
                }
            }
        } catch (Exception e) {
            LOG.error(e.getMessage());
            e.printStackTrace();
        }
        
        //Remove SAM files for save disk space.  
        if(this.boolRmSamMap){
           for(String strF : this.frd.output_ann.keySet()){
               File fleSAM=new File(strF);
               if(fleSAM.exists()){
                   boolean boolF=fleSAM.delete();
                   if(boolF)    LOG.info("The file "+fleSAM.getName()+" was removed!");
               }
           }
        }

        return "Annotation Module was completed!";
    }

    public void ReportAnn() {
        sb = new StringBuilder();
        for (int i = 0; i < this.iptr.length; i++) {
            sb.append(this.iptr[i].getReport());
        }
        
        LOG.info(sb);
        
        try {
            BufferedWriter bw = Factory.getWriter(this.frd.output_dir + this.frd.output_prefix + "_AnnReport.txt");
            bw.write(sb.toString());
            bw.close();
        } catch (IOException ex) {
            LOG.error(ex.getMessage());
        }
        
    }
    
    @Override
    public Object call() throws Exception {
//        System.out.println("\n----Alignment-Report-For-("+this.frd.input+")----\n");
        
        String strFlag = this.Annotate();
        this.ReportAnn();
        
        return strFlag;
    }
    
    public static void main(String[] args) {
        try {
            String strIn = "E:\\01Work\\miRNA\\project\\COMPASS\\output\\S-001570892_ATCACG_L001_R1\\S-001570892_ATCACG_L001_R1_17to50_FitRead_STAR_Aligned.out.sam";
            File fleIn;
            fleIn = new File(strIn);
            if (!fleIn.exists()) {
                fleIn = new File(strIn.replaceAll(".sam", ".bam"));
            }
            SamReader sr = SamReaderFactory.make().validationStringency(ValidationStringency.LENIENT).open(fleIn);
            for (SAMRecord srdRead : sr) {
                System.out.println(srdRead.getReadName());
            }
            sr.close();
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(SAMAnnotator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
