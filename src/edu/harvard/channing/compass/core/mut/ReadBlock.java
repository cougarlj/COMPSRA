/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.core.mut;

import htsjdk.samtools.SAMRecord;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class is used to deal with the read block.
 * @author Jiang Li
 * @version 1.0 
 * @since 2018-10-10
 */
public class ReadBlock {
    private static final Logger LOG = LogManager.getLogger(ReadBlock.class.getName());
    
    public int intStart;
    public ArrayList<SAMRecord> altSAM;
    public LinkedHashMap<Integer,ArrayList<Character>> lhmPileUp;
    public LinkedHashMap<Integer,SNPRecord> lhmSNP;
    public int intMatch;
    public String strRef;

    public ReadBlock(int start) {
        this.intStart=start;
        this.altSAM=new ArrayList();
    }
    
    
    public void parseBlock(){
        //Build Read Pile. 
        this.lhmPileUp=new LinkedHashMap();
        for(SAMRecord srd: altSAM){
            String strRead=srd.getReadString();
//            String strCIGAR=this.padCIGAR(srd.getCigarString());
            for(int i=0;i<strRead.length();i++){
                if(!lhmPileUp.containsKey(srd.getUnclippedStart()+i)){
                    lhmPileUp.put(intStart+i, new ArrayList());
                }
                lhmPileUp.get(intStart+i).add(strRead.charAt(i));                
            }
        }
        
        //Recognize SNP. 
        for(Iterator <Map.Entry<Integer,ArrayList<Character>>> itr=lhmPileUp.entrySet().iterator();itr.hasNext();){
            Map.Entry<Integer,ArrayList<Character>> item=itr.next();
            if(this.checkSNP(item.getValue())){
                
            }
        }       
    }
    
    public String padCIGAR(String strCIGAR){
        
        for(int i=0;i<strCIGAR.length();i++){
            //To be coded. 
        }
        
        return null;
    }
    
    public void putRead(SAMRecord srd){
        altSAM.add(srd);
    }
    
    public boolean checkSNP(ArrayList<Character> altBase){
        boolean boolFlag=false;
        //To be coded.
        
        return boolFlag;
    }
}
