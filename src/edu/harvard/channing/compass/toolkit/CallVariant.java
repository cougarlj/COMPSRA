/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.toolkit;

import edu.harvard.channing.compass.core.Configuration;
import edu.harvard.channing.compass.core.mut.Heap;
import edu.harvard.channing.compass.core.mut.MergeVariant;
import edu.harvard.channing.compass.utility.ReadFile;
import java.io.File;
import java.util.ArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author rejia
 */
public class CallVariant implements ToolKit {

    private static final Logger LOG = LogManager.getLogger(CallVariant.class.getName());

    public String strSAM;
    public String strFa;
    public String strRefID="hg38";
//    public Heap heap;
    public String strFileList;
    public ArrayList<Heap> altHeap;
    public boolean needMerge=false;
    public String strOut;
    
    
    
    public void callVariant(){
        altHeap=new ArrayList();
        if(strSAM!=null){
            String[] strSAMs=strSAM.split(",|;");
            for (String strFile : strSAMs) {
                Heap heap = new Heap(strFile);
                altHeap.add(heap);
            }
        }else{
            ArrayList<String> altFile=ReadFile.readFileList(strFileList);
            for(String strFile:altFile){
                Heap heap=new Heap(strFile);
                altHeap.add(heap);
            }
        }
        
//        heap.setRefGenome(this.strRefID, this.strFa);
        for (Heap heap : altHeap) {
            try {
                heap.setRefGenome(this.strRefID);
                heap.makeHeap();
                heap.outputHeap();
                heap.callVariant();
                LOG.info(heap.strFile + " was set.");
            } catch (Exception ex) {
                LOG.error(ex.getMessage());
                LOG.info(heap.strFile + " was failed!");
            }
        }
   
    }

    public void mergeVT(){
        ArrayList<String> altSamples=ReadFile.readFileList(strFileList);
        if(this.strOut==null){
            
        }
        MergeVariant mv=new MergeVariant(altSamples,this.strOut);
        mv.mergeVariant();
        
    }
    
    @Override
    public int runKit() {
        if (this.needMerge) {
            this.mergeVT();
        } else {
            this.callVariant();
        }

        return 1;
    }
    
    public static void main(String[] argv){
        Configuration config=new Configuration();
        CallVariant cv=new CallVariant();
        cv.strSAM="E:\\01Work\\miRNA\\data\\test_part.sam;E:\\01Work\\miRNA\\data\\test_part.sam;E:\\01Work\\miRNA\\data\\test_part.sam";
        cv.strFa="E:\\01Work\\miRNA\\data\\hg38.fa.gz";
        cv.callVariant();
        System.out.println();
    }

}
