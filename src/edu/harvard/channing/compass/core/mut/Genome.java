/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.core.mut;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class is used to define the genome.
 * @author Jiang Li
 * @version 1.0 
 * @since 2018-11-24
 */
public abstract class Genome {
    private static final Logger LOG = LogManager.getLogger(Genome.class.getClass());
    
    public String strID="hg38";
    public String strFasta;
    public int intLines=10000;
    public int intStart=1;
    
    public static HashMap<String,LinkedHashMap<Integer,String>> hmpGenome;

    public Genome(String strID) {
        this.strID = strID;
    }
    
    
    public Genome(){
        
    }
    
    public abstract boolean buildGenome();
    public abstract boolean buildGenomeMain();

    public void setStrFasta(String strFasta) {
        this.strFasta = strFasta;
    }
    
    public abstract char findBase(String chr,int pos);
    
    public Comparator<Integer> cmp=new Comparator<Integer>(){
        @Override
        public int compare(Integer t, Integer t1) {
            return t.compareTo(t1);
        }
        
    };

    public boolean isLoaded() {
        if(this.hmpGenome==null || this.hmpGenome.isEmpty())    return false;
        else    return true;
    }

    public void readGenome(String strGenome) {
        ObjectInputStream ois = null;
        try {
            File fleObj = new File(strGenome);
            if (!fleObj.exists()) {
                LOG.error("The reference genome file: "+strGenome+" doesn't esist!");
                return;
            }
            ois = new ObjectInputStream(new GZIPInputStream(new FileInputStream(strGenome)));
            hmpGenome = (HashMap<String,LinkedHashMap<Integer,String>>) ois.readObject();
            ois.close();
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            LOG.error("Fail to find the prebuilt configuration obj file!");
        } finally {
            try {
                ois.close();
            } catch (IOException ex) {
                LOG.error(ex.getMessage());
            }
        }
    }

    public void writeGenome(String strGenome) {
        ObjectOutputStream oos=null;
        try {
            oos = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(strGenome)));
            oos.writeObject(hmpGenome);
            oos.close();
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
}
