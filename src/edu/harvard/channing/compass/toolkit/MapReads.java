/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.toolkit;

import edu.harvard.channing.compass.utility.FastReaderSeg;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class is used to map the aligned reads to taxonomy tree.
 * @author Jiang Li
 * @version 1.0
 * @since 2018-01-25
 */
public class MapReads implements Callable<String>{

    FastReaderSeg frs;
    ConcurrentHashMap<String, Tax> taxTree;
    ConcurrentHashMap<String,String> acc2tax;
    int intNoMapping=0;
    int intNotInTree=0;
    HashMap<String,ArrayList<Tax>> read2node;
    int index;
    ConcurrentHashMap<String,ArrayList<String>> hmpAcc;

    public MapReads(int index,FastReaderSeg frs, ConcurrentHashMap<String, Tax> taxTree, ConcurrentHashMap<String, String> acc2tax, ConcurrentHashMap<String,ArrayList<String>> hmpAcc) {
        this.index=index;
        this.frs = frs;
        this.taxTree = taxTree;
        this.acc2tax = acc2tax;
        this.hmpAcc=hmpAcc;
        read2node=new HashMap<String,ArrayList<Tax>>();
        
    }
    

    @Override
    public String call() throws Exception {
        
        byte[] currentLine = null;
        int[] intSep = new int[1024 * 1024]; //too small may cause outofboundary problem.
        while ((currentLine = frs.readLine(intSep)) != null) {
            if (currentLine[0] == 35) {
                continue;
            }

            String[] items = new String(currentLine).split("\t");
//            String strID = null;   
            String strRead = items[0];
            String strID = items[1];           
            
            ArrayList<String> altAcc = this.hmpAcc.get(strID);
            if(altAcc==null){
                altAcc=new ArrayList<>();
                altAcc.add(strID);
            }
            
            for (String strAcc : altAcc) {

                String taxID = this.acc2tax.get(strAcc);
                if (taxID == null) {
                    this.intNoMapping++;
                    continue;
                }
                Tax tax = this.taxTree.get(taxID);
                if (tax == null) {
                    this.intNotInTree++;
                    continue;
                }

                //Set Node. 
                if (tax.reads.containsKey(strRead)) {
                    int count = tax.reads.get(strRead);
                    tax.reads.put(strRead, count + 1);
                } else {
                    tax.reads.put(strRead, 1);
                }

                //Set Read.
                if (!this.read2node.containsKey(strRead)) {
                    this.read2node.put(strRead, new ArrayList<Tax>());
                    this.read2node.get(strRead).add(tax);
                } else {
                    this.read2node.get(strRead).add(tax);
                }

            }
        }
        
        return "Thread "+this.index+" was finished!";
    }



    
}
