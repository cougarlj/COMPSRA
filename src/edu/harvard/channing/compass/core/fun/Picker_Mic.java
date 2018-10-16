/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.core.fun;

import edu.harvard.channing.compass.core.Factory;
import java.io.BufferedReader;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class is used to abstract useful info from microbal annotation file.
 * @author Jiang Li
 * @version 1.0 
 * @since 2018-02-23
 */
public class Picker_Mic extends Picker{

    private static final Logger LOG = LogManager.getLogger(Picker_Mic.class.getName());
    
    public Picker_Mic(String strInput, int intFirstKey, int intSecondKey, int intAssist, int intHit) {
        super(strInput, intFirstKey, intSecondKey, intAssist, intHit);
    }

    @Override
    public boolean pick(){
        try {
            BufferedReader br=Factory.getReader(strInput);
            String strLine;
            for(strLine=br.readLine();strLine!=null;strLine=br.readLine()){
                String[] strItem=strLine.split("\t");                
                content.put(strItem[this.intFirstKey], Integer.valueOf(strItem[this.intHit]));
            }
        } catch (IOException ex) {
            LOG.error(ex.getMessage());
            return false;
        }
        return true;
    }        
    
}
