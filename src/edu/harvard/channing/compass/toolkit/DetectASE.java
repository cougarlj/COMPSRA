/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.toolkit;

import edu.harvard.channing.compass.core.Factory;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.ValidationStringency;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Detect ASE from bam files. 
 * @author rejia
 * @version 1.2
 * @since 2020-11-10 
 */
public class DetectASE implements ToolKit{
    private static final Logger LOG = LogManager.getLogger(DetectASE.class.getClass());
    
    public String strFileList;
    public ArrayList<HashMap<String,Integer>> altASE;
    public HashMap<String,Integer> hmpName;

    
    
    @Override
    public int runKit() {
        this.altASE=new ArrayList();
        this.hmpName=new HashMap();
        
        try {
            BufferedReader br=Factory.getReader(strFileList);
            String strLine=br.readLine().trim();
            while(strLine!=null){
                File fleSample=new File(strLine);
                HashMap hmpSample=new HashMap();
                SamReader sr = SamReaderFactory.make().validationStringency(ValidationStringency.LENIENT).open(fleSample);
                for (SAMRecord srdRead : sr){
                    
                    
                }
                
                
                strLine=br.readLine();
            }
            
            

            
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(DetectASE.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0;
    }
    
}
