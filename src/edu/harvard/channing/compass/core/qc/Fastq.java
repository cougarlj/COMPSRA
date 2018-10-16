/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.core.qc;

import edu.harvard.channing.compass.core.Factory;
import edu.harvard.channing.compass.entity.CommonParameter;
import edu.harvard.channing.compass.entity.FileRecord;
import edu.harvard.channing.compass.utility.StringTools;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.Callable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Fastq class is used to manage fastq file. One file One object.
 * @author Jiang Li
 * @version 1.0 
 * @since 2017-09-14
 */
public class Fastq implements Callable{
    private static final Logger LOG = LogManager.getLogger(Fastq.class.getName());
    public QualityControl qc;
    public FileRecord frd;
    /**
     * The default value is Phred+33, but the exact system should be set by user or estimated by the pipeline. To be improved. 
     */
    public int intPhred=33;
    
    public BufferedReader brInput;
    public BufferedWriter[] bosOutputs;
    public HashMap<Integer,Integer> hmpReadLength;
    public int intTotalRead=0;
    public int intRmBaseHead=0;
    public int intRmBaseTail=0;
//    public String strRmLengthRead="0";
    public int intRmQualityHead=0;
    public int intRmQualityTail=0;
    public int intRmQualityRead=0;
    public int intRmAdapter=0;
    public String[] strRange;
    public int[] intCount;
//    public String[] strRecord;
    public int intRawReadLength=50;
    public int intTolerance=1;
    
    
    public Fastq(FileRecord frd, QualityControl qc) {
        this.frd = frd;
        this.qc=qc;
               
        this.setReadLength();
        
        hmpReadLength=new HashMap<Integer,Integer>();
        for(int i=0;i<=intRawReadLength;i++) hmpReadLength.put(i,0);
        
        strRange=this.qc.strRmLengthRead.split(",|;");
        intCount=new int[1+this.strRange.length+1];
        for(int i=0;i<intCount.length;i++) intCount[i]=0;
//        strRecord=new String[intCount.length];
        
        this.frd.setQC(this);
    }
    
    /**
     * The main function for quality control. 
     * @return True for completing successfully and False for failing.
     */
    public boolean runQC() {
        brInput=getInput();
        if(brInput==null) return false;
        
        bosOutputs=getOutputs();
        if(bosOutputs==null) return false;
        
        Read read;
        while((read=getRead(brInput))!=null){
            if(qc.boolRmAdapter)    read.rmAdapter3prim(qc.strRmAdapter);
            if(qc.boolRmBias)   read.rmBias(qc.intRmBias);      

            if(qc.boolRmQualityHead)    read.rmQualityHead(qc.intRmQualityHead);
            if(qc.boolRmQualityTail)    read.rmQualityTail(qc.intRmQualityTail);
            if(qc.boolRmQualityRead)    read.rmQualityRead(qc.intRmQualityRead);     

            if(qc.boolRmBaseHead)   read.rmBaseHead(qc.intRmBaseHead);
            if(qc.boolRmBaseTail)   read.rmBaseTail(qc.intRmBaseTail);
            if(qc.boolRmLengthRead)   read.rmLengthRead(strRange);        
            
            this.hmpReadLength.put(read.getLength(), hmpReadLength.get(read.getLength())+1);
            
            this.setRead(read,bosOutputs);           
        }
        
        this.reportQC();
        
        try {
            brInput.close();
            for(int i=0;i<bosOutputs.length;i++){
//                if(i==1)    continue;
                bosOutputs[i].close();
            }
        } catch (IOException ex) {
            LOG.info("Fastq Error: Fail to close the files.");
        }
        
//        this.qc.strOutput=new ArrayList<String[]>();
//        this.qc.strOutput.add(strRecord);
        return true;
    }    
    
    /**
     * To require the Reader of input file according to the file extension.
     * @return Return the BufferedReader object.
     */
    public BufferedReader getInput() {       
        if (this.frd.input==null) {
            LOG.info("QualityControl Info: The input file doesn't exist!");
            return null;
        } else {
            return Factory.getReader(this.frd.input);
        }
    }
    
    public void evaluatePhred(){
        //to be completed.
    }
    
    public Read getRead(BufferedReader br){            
        try {
            Read read=new Read();
            for(int i=0;i<4;i++){
                String strTemp=br.readLine();
                if(strTemp!=null)   read.strRead[i]=strTemp;
                else return null;
            }
            read.intStart=0;
            read.intEnd=read.strRead[1].length();
            this.intTotalRead++;
            return read;
        } catch (IOException ex) {
            LOG.info("Fastq File Error: Fail to get read!");
            return null;
        }       
    }

    public BufferedWriter[] getOutputs(){
        this.bosOutputs=new BufferedWriter[this.intCount.length];
        for (int i = 0; i < this.frd.output_qc.size(); i++) {
            this.bosOutputs[i] = Factory.getWriter(this.frd.output_qc.get(i));
        }
        return this.bosOutputs;       
    }
    
    public void setRead(Read read,BufferedWriter[] bw) {
        if(!read.hasGoodQuality){
            read.writeRead(bw[0]);
            this.intCount[0]++;
        }else{
            read.writeRead(bw[read.intGroup]);
            this.intCount[read.intGroup]++;
        }
    }

    public void reportQC() {

        StringBuilder sb = new StringBuilder();

//        System.out.println("\n----QC-Report-For-("+this.frd.input+")----\n");
        
        sb.append("\n#").append("File:").append(this.frd.input).append("\n");
        sb.append("#").append(this.intTotalRead).append(" reads are processed.").append("\n");
        sb.append("#").append(this.intRmAdapter).append(" reads are trimed with 3'adapter.").append("\n");
        sb.append("#").append(this.intRmQualityRead).append(" reads are removed because of low quality (<").append(this.qc.intRmQualityRead).append(").").append("\n");
        sb.append("#").append(this.intRmBaseHead).append(" reads are trimed from 5'end with ").append(this.qc.intRmBaseHead).append(" bases.").append("\n");
        sb.append("#").append(this.intRmBaseTail).append(" reads are trimed from 3'end with ").append(this.qc.intRmBaseTail).append(" bases.").append("\n");
        sb.append("#").append(this.intRmQualityHead).append(" reads are trimed from 5'end bacause of low quality.").append("\n");
        sb.append("#").append(this.intRmQualityTail).append(" reads are trimed from 3'end bacause of low quality.").append("\n");

        for (int i = 0; i < this.intCount.length; i++) {
            sb.append("#").append(this.intCount[i]).append(" reads are saved int the file: ").append(this.frd.output_qc.get(i)).append("\n");
        }

        sb.append("#").append("The length distribution of the trimed reads is listed:").append("\n");
        sb.append("#").append("Length\tCount\tPercentage").append("\n");

        DecimalFormat df = new DecimalFormat("0.00%");
        for (int i = 0; i < this.hmpReadLength.size(); i++) {
            float fltPercentage = this.hmpReadLength.get(i) / (float) this.intTotalRead;
            sb.append(String.valueOf(i)).append("\t").append(String.valueOf(this.hmpReadLength.get(i))).append("\t").append(df.format(fltPercentage)).append("\n");
        }

//        for (int i = 0; i < this.hmpReadLength.size(); i++) {
//            float fltPercentage = this.hmpReadLength.get(i) / (float) this.intTotalRead;
//            System.out.println(String.valueOf(i) + "\t" + String.valueOf(this.hmpReadLength.get(i)) + "\t" + df.format(fltPercentage));
//        }
        
        LOG.info(sb.toString());
//        System.out.println("\n--------------------QC-Report-End--------------------\n");
//        System.out.println(sb.toString());
       
//        for (int i = 0; i < this.intCount.length; i++) {
//            System.out.println(this.intCount[i] + " reads are saved int the file: " + this.frd.output_qc.get(i));
//        }
        
        try {
            BufferedWriter bw = Factory.getWriter(this.frd.output_dir + this.frd.output_prefix + "_QCReport.txt");
            bw.write(sb.toString());
            bw.close();
        } catch (IOException ex) {
            LOG.error(ex.getMessage());
        }
    }

    public void setReadLength() {
        try {
            BufferedReader br=Factory.getReader(this.frd.input);
            br.readLine();
            this.intRawReadLength=br.readLine().length();
            br.close();
            LOG.info("The length of raw read is: "+this.intRawReadLength+".");
        } catch (IOException ex) {
            LOG.info("Failed to detect the read length! The default value is "+this.intRawReadLength+".");
        }catch (NullPointerException e){
            LOG.info("Failed to detect the read length! The default value is "+this.intRawReadLength+".");
        }
    }

    @Override
    public Object call() throws Exception {
        boolean boolFlag=this.runQC();
        if(boolFlag)    return "File "+this.frd.input+" QC pass!";
        else    return "File "+this.frd.input+" QC fail!";
    }


    /**
     * This class is used to provide QC function for reads. 
     */
    public class Read{

        public int intStart;
        public int intEnd;
        public int intLength=-1;
        public int intGroup=0;
        public boolean isAvailable=true;
        public boolean hasAdapter3Prim=true;
        public boolean hasGoodQuality=true;
        
        public String[] strRead=new String[4];

        /**
         * This method is used to get the valid length of the read. If the length was not calculated, call setLength() to get it.
         * @return The valid length of the read.
         */
        private int getLength(){
            if(this.intLength==-1)   this.setLength();
            return this.intLength;
        }
        /**
         * This method is used to set the valid length of the read.
         */
        private void setLength(){
            this.intLength=this.intEnd-this.intStart;
            if(this.intLength<=0)    this.intLength=0;    
        }
        /**
         * This method is used to get the valid sequence of the read. 
         * @return The valid read sequence.
         */
        private String getSeq(){
            if(this.intEnd<=this.intStart)  return null;
            else    return strRead[1].substring(this.intStart, this.intEnd);
        }
        /**
         * This method is used to get the valid quality score of the read.
         * @return The valid quality score of read.
         */
        private String getQuality(){
            if(this.intEnd<=this.intStart)  return null;
            else    return strRead[3].substring(this.intStart,this.intEnd);
        }
        /**
         * This method is used to remove N bases of the read directly from the head.
         * @param N The bases to be removed from the head.
         */
        private void rmBaseHead(int N) {
            this.intStart=this.intStart+N;
            intRmBaseHead++;
        }
        /**
         * This method is used to remove N bases of the read directly from the tail.
         * @param N 
         */
        private void rmBaseTail(int N) {
            this.intEnd=this.intEnd-N;
            intRmBaseTail++;
        }
        /**
         * This method is used to divide the reads into different groups according to the length region strRange.
         * @param strRange 
         */
        private void rmLengthRead(String[] strRange) {
            if(this.intLength==-1)  this.setLength();
            for(int i=0;i<strRange.length;i++){
                if(this.intLength<Integer.valueOf(strRange[i])){
                    this.intGroup=i+1;
                    return;
                }
            }
            this.intGroup=strRange.length+1;       
        }
        /**
         * This method is used to remove the bases with quality scores smaller than Q from the head.
         * @param Q 
         */
        private void rmQualityHead(int Q) {
            int intBadBase=0;

            for(int i=this.intStart;i<this.intEnd;i++){
                if((this.strRead[3].charAt(i)-intPhred)<Q) intBadBase++;
                else    break;
            }
            if(intBadBase!=0){
                this.intStart+=intBadBase;
                intRmQualityHead++;
            }               
        }
        /**
         * This method is used to remove the bases with quality scores smaller than Q from the tail.
         * @param Q 
         */
        private void rmQualityTail(int Q) {
            int intBadBase=0;
            for(int i=this.intEnd-1;i>=this.intStart;i--){
                if((this.strRead[3].charAt(i)-intPhred)<Q) intBadBase++;
                else    break;
            }
            if(intBadBase!=0){
                this.intEnd-=intBadBase;
                intRmQualityTail++;
            }              
        }
        /**
         * This method is used to remove the reads with average quality scores smaller than Q.
         * @param Q 
         */
        private void rmQualityRead(int Q) {
            int intTotalQ=0;

            int intN=0;
            for(int i=this.intStart;i<this.intEnd;i++){
                intTotalQ+=this.strRead[3].charAt(i);
                intN++;
            }
//            if(intN==0) return;
            if(intTotalQ<=(intN*(Q+intPhred))){
                this.hasGoodQuality=false;  
                intRmQualityRead++;
            }
       
        }
        /**
         * This method is used to remove the 3'adapter. 
         * @param strAdapter The sequences of adapter.
         */
        private void rmAdapter3prim(String strAdapter) {
            int intPos=StringTools.FindLongestSuffix(this.strRead[1], strAdapter,intTolerance);
            if(intPos==0){
                this.hasAdapter3Prim=false;
            }else{
                this.intEnd=intPos;
                intRmAdapter++;
            }
        }
        /**
         * This method is used to remove the random bases attached to the adapter within the library preparation. 
         * @param N The random bases introduced within the library preparation especially for illumina platform.
         */
        private void rmBias(int N) {
            this.intStart=this.intStart+N;
            this.intEnd=this.intEnd-N;
        }

        public void writeRead(BufferedWriter bw) {
            try {
                bw.write(this.strRead[0]);
                bw.newLine();
                bw.write(this.strRead[1], this.intStart, this.getLength());
                bw.newLine();
                bw.write(this.strRead[2]);
                bw.newLine();
                bw.write(this.strRead[3], this.intStart, this.getLength());
                bw.newLine();
            } catch (IOException ex) {
                LOG.info("Fastq Error: Fail to write reads into files.");
            }        
        }      
    }
    
    
    public static void main(String[] args){
        //Test the class.
        System.out.println("--------------------Test Start--------------------");
        System.out.println(new Date());
        
        CommonParameter cp=new CommonParameter();
//        cp.output="E:\\01Work\\miRNA\\project\\COMPASS\\output\\Test";
        QualityControl qc=new QualityControl(cp);
        qc.boolRmAdapter=true;
        qc.strRmAdapter="TGGAATTCTCGGGTGCCAAGG";
        qc.boolRmBias=true;
        qc.intRmBias=4;
        qc.boolRmQualityHead = true;
        qc.intRmQualityHead = 20;
        qc.boolRmQualityTail = true;
        qc.intRmQualityTail = 20;
        qc.boolRmQualityRead = true;
        qc.intRmQualityRead = 20;
//        qc.boolRmBaseHead=true;
//        qc.intRmBaseHead=4;
//        qc.boolRmBaseTail=true;
//        qc.intRmBaseTail=16;
        qc.boolRmLengthRead=true;
        qc.strRmLengthRead="8,17";
        FileRecord frd=new FileRecord("E:\\01Work\\miRNA\\data\\13Samples_Batch\\raw_data\\HBRNA_AGTCAA_L001_R1.fastq.gz");
        frd.setOutput("E:\\01Work\\miRNA\\project\\COMPASS\\output\\Test");
        frd.setOutput();
        Fastq fq1=new Fastq(frd,qc);
//        Fastq fq2=new Fastq("E:\\01Work\\miRNA\\data\\13Samples_Batch\\raw_data\\S-001570897_GCCAAT_L002_R1.fastq.gz",qc);
//        Fastq fq3=new Fastq("E:\\01Work\\miRNA\\project\\COMPASS\\test\\test.fastq",qc);
//        Fastq fq4=new Fastq("E:\\01Work\\miRNA\\project\\COMPASS\\output\\HBRNA_AGTCAA_L001_R1_ShortRead.fastq.gz",qc);
//        boolean flag=fq.runQC();
        System.out.println(fq1.runQC());
//        System.out.println(flag);
        
        
        System.out.println(new Date());
        System.out.println("--------------------Test Finishes--------------------");
    }
    
    
}
