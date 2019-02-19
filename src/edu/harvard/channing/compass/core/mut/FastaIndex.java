/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.core.mut;

import edu.harvard.channing.compass.core.Configuration;
import java.io.File;
import java.util.HashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class is used to fulfill the index function.
 * @author Jiang Li
 * @version 1.0 
 * @since 2018-11-24
 */
public class FastaIndex implements Index{
    private static final Logger LOG = LogManager.getLogger(FastaIndex.class.getName());
    
    public HashMap<String,HashMap<Integer,Integer>> hmpIndex;
    public String strFile;
    public boolean isGZ=false;
    public int intDist=10000;

    public FastaIndex(String strFile) {
        this.strFile = strFile;
    }
    
    

    @Override
    public void buildIndex() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public HashMap getIndex() {
        return hmpIndex;
    }

    @Override
    public void setIndex() {
        String strOut=this.strFile+Configuration.strIndexSurffix;
        File fleOut=new File(strOut);
        
    }

    @Override
    public void checkFormat() {
        if(this.strFile.endsWith(".gz")){
            this.isGZ=true;
        }else{
            this.isGZ=false;
        }
    }

    @Override
    public char findIndex() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
