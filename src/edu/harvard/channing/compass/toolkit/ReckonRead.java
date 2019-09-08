/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.toolkit;

import edu.harvard.channing.compass.core.Configuration;
import edu.harvard.channing.compass.core.Factory;
import edu.harvard.channing.compass.core.qc.Fastq;
import edu.harvard.channing.compass.core.qc.Fastq.Read;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class is used to check the read length info.
 * @author Jiang Li
 * @version 1.0 
 * @since 2019-03-29
 */
public class ReckonRead implements ToolKit{
    private static final Logger LOG = LogManager.getLogger(ReckonRead.class.getName());
    public String strLength;
    public String strInput;
    public String strOutput;
    public HashMap<Integer,HashMap<String,Integer>> hmpRead;

    
    
    @Override
    public int runKit() {
        try {
            
            String[] strItems = strLength.split(",|;");           
            List<Integer> lstLength = new ArrayList();
            for(String strL:strItems)   lstLength.add(Integer.valueOf(strL));
            
            Fastq fqRawFile=new Fastq();
            fqRawFile.setInput(strInput);
//            fqRawFile.setOutput(strOutput);
            hmpRead=new HashMap();
            Read read;
            while ((read=fqRawFile.getRead(fqRawFile.brInput))!=null) {
                int intL=read.getLength();
                if(!lstLength.contains(intL)){
                    continue;
                }
                
                if(hmpRead.containsKey(intL)){
                    if(hmpRead.get(intL).keySet().contains(read.getSeq())){
                        int intTMP=hmpRead.get(intL).get(read.getSeq())+1;
                        hmpRead.get(intL).put(read.getSeq(), intTMP);
                    }else{
                        hmpRead.get(intL).put(read.getSeq(), 1);
                    }
                }else{
                    HashMap<String,Integer> hmpTMP=new HashMap();
                    hmpTMP.put(read.getSeq(),1 );
                    hmpRead.put(intL, hmpTMP);
                }
                
            }
            
            ArrayList<Integer> altLen=new ArrayList(hmpRead.keySet());
            Collections.sort(altLen);
            if(this.strOutput==null){
                this.strOutput=this.strInput.split("\\.")[0]+"_ReadSeqStat"+".txt";
            }else{
                this.strOutput=this.strOutput.split("\\.")[0]+"_ReadSeqStat"+".txt";
            }
            BufferedWriter bw=Factory.getWriter(this.strOutput);
            
            for(int i=0;i<altLen.size();i++){
                Map<String,Integer> map=hmpRead.get(altLen.get(i));
                LinkedHashMap<String,Integer> lhmSort=new LinkedHashMap();
                map.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).forEachOrdered(x -> lhmSort.put(x.getKey(), x.getValue()));
                for(Entry<String,Integer> entry:lhmSort.entrySet()){                    
                    bw.write(altLen.get(i)+"\t"+entry.getKey()+"\t"+entry.getValue());
                    bw.newLine();
                } 
                
            }
            bw.close();
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            return 0;
        }
        return 1;
    }
    
    public static void main(String[] argv){
        Configuration config=new Configuration();
        ReckonRead rkrTmp=new ReckonRead();
        rkrTmp.strLength="3,4,5,6,7";
//        rkrTmp.strInput="E:\\01Work\\miRNA\\project\\COMPASS\\output\\ST-01026317_S-001773485\\ST-01026317_S-001773485_0to8_FitRead.fastq.gz";
//        rkrTmp.strLength="8,9,10,11";
//        rkrTmp.strInput="E:\\01Work\\miRNA\\project\\COMPASS\\output\\ST-01026317_S-001773485\\ST-01026317_S-001773485_8to17_FitRead.fastq.gz";
//        rkrTmp.strLength="30,31,32,33,34";
//        rkrTmp.strInput="E:\\01Work\\miRNA\\project\\COMPASS\\output\\ST-01026317_S-001737758\\ST-01026317_S-001737758_R1_17to50_FitRead.fastq.gz";
//        rkrTmp.strInput="E:\\01Work\\miRNA\\project\\COMPASS\\output\\ST-01026349_S-001737762\\ST-01026349_S-001737762_R1_17to50_FitRead.fastq.gz";
        rkrTmp.strInput="E:\\01Work\\miRNA\\project\\COMPASS\\output\\ST-01026349_S-001773489\\ST-01026349_S-001773489_0to8_FitRead.fastq.gz";
        rkrTmp.runKit();       
    }
}
