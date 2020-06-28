/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.toolkit;

import edu.harvard.channing.compass.core.Factory;
import edu.harvard.channing.compass.core.fun.Picker;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.python.google.common.base.Strings;

/**
 *
 * @author rejia
 */
public class Merge implements ToolKit{
    
    private static final Logger LOG = LogManager.getLogger(Merge.class.getClass());
    
    public String strFile;
    public String strOutput;
    public boolean boolHead=true;
    public int intKey=0;
    public int intCount=2;
    public int intSize;
    
    public ArrayList<Picker> picker;
    public HashMap<String,ArrayList<Integer>> hmpProfile;
    public double[][] intProfile;
    public ArrayList<String> altID;

    @Override
    public int runKit() {
        try {
            BufferedReader br = Factory.getReader(this.strFile);
            picker = new ArrayList();
            for (String strF = br.readLine().trim(); strF != null; strF = br.readLine()) {
                picker.add(new Picker(strF, this.intKey, -1, -1, this.intCount));
            }

            String message;
            ExecutorService exe = Executors.newCachedThreadPool();
            ArrayList<Future<String>> lstResult = new ArrayList<Future<String>>();

            for (int k = 0; k < picker.size(); k++) {
                Future<String> future = exe.submit(picker.get(k));
                lstResult.add(future);
            }

            for (Future<String> ft : lstResult) {
                try {
                    while (!ft.isDone());
                    message = ft.get();
                    LOG.info(message);
//                System.out.println(message);
                } catch (InterruptedException ex) {
                    LOG.error(ex.getMessage());
                } catch (ExecutionException ex) {
                    LOG.error(ex.getMessage());
                } finally {
                    exe.shutdown();
                }
            }
            
            this.convergePicker();
            this.outputResult();

        } catch (IOException ex) {
            LOG.error("Fail to read file " + this.strFile);
            return 0;
        }
        return 1;
    }
    
    public void convergePicker() {
        this.intSize=this.picker.size();
        this.hmpProfile = new HashMap<String, ArrayList<Integer>>();
        for (int i = 0; i < this.intSize; i++) {
            for (Map.Entry<String, Integer> entry : this.picker.get(i).content.entrySet()) {
                if (!this.hmpProfile.containsKey(entry.getKey())) {
                    this.hmpProfile.put(entry.getKey(), new ArrayList<Integer>());
                    for (int j = 0; j < i; j++) {
                        this.hmpProfile.get(entry.getKey()).add(0);
                    }
                }
                this.hmpProfile.get(entry.getKey()).add(entry.getValue());
            }

            for (String strKey : this.hmpProfile.keySet()) {
                if (this.hmpProfile.get(strKey).size() == i) {
                    this.hmpProfile.get(strKey).add(0);
                }
            }
        }

        this.altID = new ArrayList<String>(this.hmpProfile.keySet());
        this.intProfile = new double[this.altID.size()][this.intSize];

        for (int i = 0; i < this.altID.size(); i++) {
            for (int j = 0; j < this.intSize; j++) {
                try {
                    intProfile[i][j] = (double) this.hmpProfile.get(this.altID.get(i)).get(j);
                } catch (IndexOutOfBoundsException e) {
                    System.out.println(this.hmpProfile.get(this.altID.get(i)).get(j));
                }

            }
        }
    }
 
        public void outputResult() {
        BufferedWriter bw = Factory.getWriter(strOutput);

        try {
            bw.write("ID"+"\t");
            
            for (int i = 0; i < this.intSize; i++) {                
                bw.write(this.picker.get(i).strInput+"\t");
            }

            bw.newLine();
            
            for(int i=0;i<this.intProfile.length;i++){
                bw.write(this.altID.get(i)+"\t");
                for(int j=0;j<this.intProfile[i].length;j++){
                    bw.write(this.intProfile[i][j]+"\t");
                }
                bw.newLine();
            }
            
            bw.close();
        } catch (IOException ex) {
            LOG.error(ex.getMessage());
        }finally{
            
        }
    }
    
    public static void main(String[] argv){
        Merge mergeTmp=new Merge();
        mergeTmp.strFile="E:\\01Work\\miRNA\\project\\COMPSRA\\output\\CAMP\\CAMP_492_virus_sample.txt";
        mergeTmp.strOutput="E:\\01Work\\miRNA\\project\\COMPSRA\\output\\CAMP\\CAMP_virus.txt";
        mergeTmp.runKit();
    }
    
}
