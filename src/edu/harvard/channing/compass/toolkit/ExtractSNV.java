/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.toolkit;

import edu.harvard.channing.compass.core.Configuration;
import static edu.harvard.channing.compass.core.Configuration.DB;
import edu.harvard.channing.compass.core.Factory;
import edu.harvard.channing.compass.db.DB;
import edu.harvard.channing.compass.entity.DBTree;
import edu.harvard.channing.compass.utility.ReadFile;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class is used to extract SNVs from VCF files according to a given range file or COMPASS prebuilt database.  
 * @author Jiang Li
 * @version 1.0 
 * @since 2019-03-01
 */
public class ExtractSNV implements ToolKit{
    private static final Logger LOG = LogManager.getLogger(ExtractSNV.class.getClass());
    
    public String strVCF;
    public String strRegion;
    public String strDB;
    public String strOut;
    public File fleVCF;
    public File fleOut;
    public String strRef="hg38";
    public ArrayList<DBTree> dbt;
    public String strFilter="PASS";
    public int intQuality=0;
    
    
    @Override
    public int runKit() {
        try {
            dbt=new ArrayList();
            
            if(strVCF==null){
                LOG.error("The input VCF file is empty.");
                return 0;
            }
            fleVCF=new File(strVCF);
            
            if(strDB==null){
                if(strRegion==null){
                    LOG.error("There is no region file set.");
                    return 0;
                }else{
                    dbt.add(ReadFile.readGFF3(strRegion));
                }
            }else{
                String[] strItem=this.strDB.split(",|;");
                for(String strTmp:strItem){
                    DB db=Factory.getDB(strTmp, true);
                    DBTree dbT=db.getForest(strRef);
                    this.dbt.add(dbT);
                }
            }
            
            if(strOut==null){
                strOut=strVCF.split("\\.")[0]+".extract.vcf.gz";
            }
            fleOut=new File(strOut);
            
            BufferedReader br=Factory.getReader(this.strVCF);
            BufferedWriter bw=Factory.getWriter(this.strOut);
            for (String strLine = br.readLine(); strLine != null; strLine=br.readLine()) {
                if (strLine.startsWith("##")) {
                    continue;
                } else if(strLine.startsWith("#")) {
                    String[] strVCFHead=strLine.split("\t");
                    StringBuffer sbHeader=new StringBuffer();
//                    sbHeader.append("CHROM\tPOS\tANN\tREF\tALT\tQUAL\tAVGDP\tAC\tAN\tAF\tGC\tGN");
                    sbHeader.append("CHROM\tPOS\tANN\tREF\tALT\tQUAL");
                    for(int i=9;i<strVCFHead.length;i++){
                        sbHeader.append("\t");
                        sbHeader.append(strVCFHead[i]);                       
                    }
                    bw.write(sbHeader.toString());
                    bw.newLine();
                } else {
                    String[] strCol=strLine.split("\t");
                    if(!strCol[6].equals(strFilter)){
                        continue;
                    }
                    
                    String chrom=strCol[0];
                    int point=Integer.valueOf(strCol[1]);
                    StringBuilder sbAnn=new StringBuilder();
                    String strAnn=null;
                    for(int i=0;i<this.dbt.size();i++){
                        strAnn=dbt.get(i).findLeaf(chrom, point);
                        if(strAnn==null){
                            continue;
                        }else{
                            sbAnn.append(strAnn);
                            sbAnn.append(";");
                        }                        
                    }
                    
                    if(sbAnn.length()==0){
                        continue;
                    }else{
//                        String[] strVCFInfo=strCol[7].split(";");
//                        String strInfo=strVCFInfo[0].substring(6)+"\t"+strVCFInfo[1].substring(3)+"\t"+strVCFInfo[2].substring(3)+"\t"+strVCFInfo[3].substring(3)+"\t"+strVCFInfo[4].substring(3)+"\t"+strVCFInfo[5].substring(3);                        
                        StringBuilder sbLine=new StringBuilder();
                        sbLine.append(chrom);
                        sbLine.append("\t");
                        sbLine.append(point);
                        sbLine.append("\t");
                        sbLine.append(sbAnn);
                        sbLine.append("\t");
                        sbLine.append(strCol[3]);
                        sbLine.append("\t");
                        sbLine.append(strCol[4]);
                        sbLine.append("\t");
                        sbLine.append(strCol[5]);
//                        sbLine.append("\t");
//                        sbLine.append(strInfo);
                        for(int i=9;i<strCol.length;i++){
                            sbLine.append("\t");
                            sbLine.append(strCol[i]);
                        }
                        
                        bw.write(sbLine.toString());
                        bw.newLine();
                    }
                    
                }
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
        ExtractSNV esnvTest=new ExtractSNV();
        esnvTest.strVCF="E:\\01Work\\ASE\\data\\test.vcf";
        esnvTest.strOut="E:\\01Work\\ASE\\data\\test_extract.txt";
        esnvTest.strRef="hg38";
        esnvTest.strDB="miRBase";
        esnvTest.runKit();
    }
    
}
