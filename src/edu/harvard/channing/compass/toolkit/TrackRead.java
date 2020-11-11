/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.toolkit;

import edu.harvard.channing.compass.core.Configuration;
import edu.harvard.channing.compass.core.Factory;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This Class is used to track the reads in annotation.
 * @author rejia(Jiang Li)
 * @version 1.0.2 
 * @since 2020-07-04
 */
public class TrackRead implements ToolKit{
    private static final Logger LOG = LogManager.getLogger(TrackRead.class.getClass());
    
    public String strRNAClass="miRNA,piRNA,snRNA,snoRNA,tRNA";
    public String strFileIn;
    public String strFileOut;
    public ArrayList<String> altFile;
    public ArrayList<String> altRNAClass;
    public HashMap<String,ArrayList<String>> hmpCounter;
    public boolean keepUnique=false;
    public String strBlank="NA";

    private void ParseRNAClass() {
        altRNAClass=new ArrayList<String>(Arrays.asList(strRNAClass.split(",|;")));
    }

    private void ParseFileIn() {
        BufferedReader br=Factory.getReader(strFileIn);
        this.altFile=new ArrayList();
        try {
            for(String strIn=br.readLine();strIn!=null;strIn=br.readLine()){
                altFile.add(strIn.trim());
            }
        } catch (IOException ex) {
            LOG.error(ex.getMessage().toString());
        }
    }

    private void InitiateCounter() {
        this.hmpCounter=new HashMap<String,ArrayList<String>>();
    }
    
    private ArrayList<String> getBlock(){
        ArrayList<String> altRead=new ArrayList();
        for(int i=0;i<altRNAClass.size();i++){
            altRead.add("");
        }
        return altRead;
    }
 
    private void CountReads() {
        for(int i=0;i<this.altFile.size();i++){
            try {
                BufferedReader br=Factory.getReader(altFile.get(i));
                
                int intReadAnn = 1;
                int intReadIndex;
                if("piRNA".equals(this.altRNAClass.get(i)) || "piwiRNA".equals(this.altRNAClass.get(i)))  intReadIndex=5;
                else    intReadIndex=4;
                
                String strLine=br.readLine().trim();
                while(strLine!=null){
                    String[] strItems=strLine.split("\t");

                    
                    if(hmpCounter.containsKey(strItems[intReadIndex])){
                        ArrayList<String> altTmp=hmpCounter.get(strItems[intReadIndex]);
                        if(altTmp.get(i)==""){
                            altTmp.set(i, strItems[intReadAnn]);
                        }else{
                            altTmp.set(i, altTmp.get(i)+";"+strItems[intReadAnn]);
                        }
                        hmpCounter.put(strItems[intReadIndex], altTmp);
                    }else{
                        ArrayList<String> altTmp=this.getBlock();
                        altTmp.set(i, strItems[intReadAnn]);
                        hmpCounter.put(strItems[intReadIndex], altTmp);
                    }
                    strLine=br.readLine();
                }
                LOG.info(altFile.get(i)+" was set!");
            } catch (IOException ex) {
                LOG.error(ex.getMessage().toString());
            }
            
        }
    }

    private void OutputResult() {
        try {
            BufferedWriter bw = Factory.getWriter(strFileOut);
            for (Map.Entry<String, ArrayList<String>> entry : this.hmpCounter.entrySet()) {
                String strLine = getLine(entry.getKey(), entry.getValue());
                if(null!=strLine){
                    bw.write(strLine);
                    bw.newLine();
                }                
            }
            bw.close();
        } catch (IOException ex) {
            LOG.error(ex.getMessage().toString());
        }      
    }

    private String getLine(String strRead, ArrayList<String> altAnn) {
        int intWithAnn=0;
        boolean boolMulti=false;
        StringBuilder sb=new StringBuilder();
        sb.append(strRead);
        sb.append("\t");
        for(int i=0;i<altAnn.size();i++){
            if(altAnn.get(i)==""){
                sb.append(strBlank);                
            }else{
                intWithAnn++;
                if(altAnn.get(i).contains(";"))   boolMulti=true;
                sb.append(altAnn.get(i));
            }
            sb.append("\t");
        }
        sb.deleteCharAt(sb.length()-1);
        
        if(this.keepUnique){
            return sb.toString();
        } else{
            if(intWithAnn>1 || boolMulti==true){
                return sb.toString();
            }else{
                return null;
            }
        }
    }
    
    @Override
    public int runKit() {
        ParseRNAClass();
        ParseFileIn();
        InitiateCounter();
        CountReads();
        OutputResult();
        
        return 1;
    }

    public static void main(String[] argv){
        Configuration config=new Configuration();
        TrackRead trd=new TrackRead();
        trd.strFileIn="E:\\01Work\\miRNA\\project\\COMPSRA\\test\\TrackRead_test_input.txt";
        trd.strFileOut="E:\\01Work\\miRNA\\project\\COMPSRA\\test\\TrackRead_test_output.txt";
        trd.strRNAClass="miRNA,piRNA,snRNA,snoRNA,tRNA";
        trd.keepUnique=false;
        trd.runKit();
        System.out.println("Well Done!");
    }



    
}
