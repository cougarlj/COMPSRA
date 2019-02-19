/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.toolkit;

import edu.harvard.channing.compass.core.Configuration;
import static edu.harvard.channing.compass.core.Configuration.ADAPTER;
import edu.harvard.channing.compass.toolkit.ToolKit;
import edu.harvard.channing.compass.utility.Download;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Adapter will record the basic adapter information. 
 * @author Jiang Li
 * @version 1.0 
 * @since 2017-09-13
 */
public class Adapter implements ToolKit{
    private static final Logger LOG = LogManager.getLogger(Adapter.class.getClass());
    public HashMap<String,AdapterInfo> hmpAdapter;
    
    public Adapter() {
        this.setAdapter();       
    }
    
    public void setAdapter() {
        try {
            File fleAdapter=new File(Configuration.ADAPTER);
            if(!fleAdapter.exists()){
                Download dldAdapter=new Download(Configuration.ADAPTER);
                boolean boolFlag=dldAdapter.download();
                if(!boolFlag){
                    LOG.info("Fail to download adapter file.");
                    return;
                }
            }
            hmpAdapter = new <String, AdapterInfo>HashMap();
            BufferedReader brAdapter = new BufferedReader(new FileReader(Configuration.ADAPTER));
            String strLine;
            while ((strLine = brAdapter.readLine()) != null) {
                if (strLine.startsWith("#")) {
                    continue;
                }
                String[] strColumns = strLine.split("\t");

                AdapterInfo aptiEntity = new AdapterInfo(strColumns[0], strColumns[1], strColumns[2], strColumns[3], strColumns[4],strColumns[5]);
                hmpAdapter.put(strColumns[0], aptiEntity);
            }
            LOG.info("The adapter configuration has been read in! Use \"-tk -adp\" to look up.");
            LOG.info(hmpAdapter.size() + " adapters are prepared!");
        } catch (FileNotFoundException ex) {
            LOG.error(ex.getMessage());
            LOG.error("The adapter configuration file doesn't exist! ");
        } catch (IOException ex) {
            LOG.error((ex.getMessage()));
            LOG.error("Something wrong in the adapter configuration file!");
        }
    }
    
    public void showAdapter(){
        System.out.println("\n==================================================================");
        System.out.println("The referred adapter:");
        for(String key:this.hmpAdapter.keySet()){
            AdapterInfo aif=this.hmpAdapter.get(key);
            System.out.println(aif.strCompany+" <---> "+aif.strProduct+" <---> "+aif.strRA3);
        }
        System.out.println("==================================================================\n");
    }

    @Override
    public int runKit() {
        this.showAdapter();
        return 1;
    }
    
    public class AdapterInfo {
        public String strID;
        public String strProduct;
        public String strCompany;
        public String strRA5;
        public String strRA3;
        public String strSTP;

        public AdapterInfo(String strID, String strCompany, String strProduct, String strRA5, String strRA3, String strSTP) {
            this.strID = strID;
            this.strCompany=strCompany;
            this.strProduct = strProduct;
            this.strRA5 = strRA5;
            this.strRA3 = strRA3;
            this.strSTP = strSTP;
        }  
    }
}
