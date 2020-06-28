/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.core.fun;

import edu.harvard.channing.compass.core.Factory;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.Callable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class is used to abstract useful info from annotation file.
 * @author Jiang Li
 * @version 1.0 
 * @since 2018-02-09
 */
public class Picker implements Callable{
    
    private static final Logger LOG = LogManager.getLogger(Picker.class.getName());
    
    public String strInput;
    public int intFirstKey;
    public int intSecondKey;
    public int intAssist;
    public int intHit;
    
    public HashMap<String,Integer> content;
    
    
    public Picker(String strInput, int intFirstKey, int intSecondKey, int intAssist, int intHit) {
        this.strInput=strInput;
        this.intFirstKey=intFirstKey;
        this.intSecondKey=intSecondKey;
        this.intAssist=intAssist;
        this.intHit=intHit;
        
        content=new HashMap<>();
    }
    
    public boolean pick(){
        try {
            BufferedReader br=Factory.getReader(strInput);
            String strLine=br.readLine();
            for(strLine=br.readLine();strLine!=null;strLine=br.readLine()){
                String[] strItem=strLine.split("\t");                
//                if(strItem.length<8)   continue;
//                String strLoc=strItem[3]+":"+strItem[4]+"-"+strItem[5];
                String strKey;
                if("-".equals(strItem[intFirstKey].trim())){
                    if (intAssist != -1) {
                        strKey = strItem[intSecondKey].trim() + "|" + strItem[intAssist].trim() ;
                    } else {
                        strKey = strItem[intSecondKey].trim();
                    }
                    
//                    if(intAssist!=-1)   strKey=strItem[intSecondKey].trim()+"|"+strItem[intAssist].trim()+"\t"+strLoc;
//                    else    strKey=strItem[intSecondKey].trim()+"\t"+strLoc;
//                    if(content.containsKey(strKey))   System.out.println(strItem[intSecondKey].trim()+" was duplicated.");
                    content.put(strKey, Integer.valueOf(strItem[intHit].trim()));
                }else{
                    if (intAssist != -1) {
                        strKey = strItem[intFirstKey].trim() + "|" + strItem[intAssist].trim();
                    } else {
                        strKey = strItem[intFirstKey].trim();
                    }
//                    if(intAssist!=-1) strKey=strItem[intFirstKey].trim()+"|"+strItem[intAssist].trim()+"\t"+strLoc;
//                    else    strKey=strItem[intFirstKey].trim()+"\t"+strLoc;
                    content.put(strKey, Integer.valueOf(strItem[intHit].trim()));
                }
            }
        } catch (IOException ex) {
            LOG.error(ex.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public Object call() throws Exception {
        boolean boolFlag=this.pick();
        if(boolFlag)    return "The file "+this.strInput+" was extracted.";
        else    return "The file "+this.strInput+" was failed to be extracted.";
    }
    
    public static void main(String[] argv){
        String strInput="E:\\01Work\\miRNA\\project\\COMPASS\\output\\HBRNA_AGTCAA_L001_R1\\HBRNA_AGTCAA_L001_R1_17to50_FitRead_STAR_Aligned_miRNA.txt";
        Picker picker=new Picker(strInput,2,1,-1,3);
        picker.pick();
        System.out.println("HeHeDa!");
    }
    
}
