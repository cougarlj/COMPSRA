/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.toolkit;

import edu.harvard.channing.compass.core.Factory;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPOutputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class is used to process fasta file.  
 * @author Jiang Li
 * @version 1.0 
 * @since 2018-03-16
 */
public class Fasta implements ToolKit{
    private static final Logger LOG = LogManager.getLogger(Fasta.class.getClass());
    
    public String fa;
    public String strOut;
    public boolean boolRedup=false;
    public boolean boolBuildIdx=false;
    
    public void ReDupl(String input, String output) {
        int count=0;
        try {
            ArrayList<String> altTitle = new ArrayList<>();
            BufferedReader br = Factory.getReader(input);
            BufferedWriter bw = Factory.getWriter(output);
            
            String strLine;
            boolean isDupl = false;
            while ((strLine = br.readLine()) != null) {
                if (strLine.startsWith(">")) {
                    if (altTitle.contains(strLine)) {
                        isDupl = true;
                        count++;
//                        System.out.println(strLine);
                    } else {
                        altTitle.add(strLine);
                        isDupl = false;
                    }
                }
                if (!isDupl) {
                    bw.write(strLine);
                    bw.newLine();
                }
            }
        } catch (IOException ex) {
            LOG.error(ex.getMessage());
        }
        LOG.info(count+" duplicated items were removed!");
    }

    public void BuildDB(String input,String output,char sep){
        ConcurrentHashMap<String,ArrayList<String>> hmpDB=new ConcurrentHashMap<>();
        try {            
            BufferedReader br=Factory.getReader(input);            
            for(String strLine=br.readLine();strLine!=null;strLine=br.readLine()){
                if(!strLine.startsWith(">"))    continue;               
                String[] strItems=strLine.split(String.valueOf(sep));
                if(strItems.length<=1)  continue;  //For save memory. 
                String strKey=strItems[0].split(" ")[0].trim().replaceFirst(">", "");
                for(int i=0;i<strItems.length;i++){
                    String strAcc=strItems[i].split(" ")[0].trim().replaceFirst(">", "");
                    if(hmpDB.containsKey(strKey)){
                        hmpDB.get(strKey).add(strAcc);
                    }else{
                        hmpDB.put(strKey, new ArrayList<>());
                        hmpDB.get(strKey).add(strAcc);  
                    }
                }
//                System.out.println(hmpDB.get(strKey));
            }
        } catch (IOException ex) {
            LOG.error(ex.getMessage());
        }
        
        LOG.info("The information was extracted and will be save as an obj file.");
        
        ObjectOutputStream oos=null;
        try {
            oos = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(output)));
            oos.writeObject(hmpDB);
        } catch (IOException ex) {
            LOG.error(ex.getMessage());
        } finally {
            try {
                oos.close();
            } catch (IOException ex) {
                LOG.error(ex.getMessage());
            }
        }        
        
    }
    
    @Override
    public int runKit() {
        if(this.boolRedup){
            this.ReDupl(this.fa, this.strOut);
        }
        
        if(this.boolBuildIdx){
            this.BuildDB(this.fa, this.strOut,(char)1);
        }
        return 1;
    }
    
}
