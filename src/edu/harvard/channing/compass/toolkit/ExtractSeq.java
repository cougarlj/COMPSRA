/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.toolkit;

import edu.harvard.channing.compass.core.Factory;
import edu.harvard.channing.compass.utility.FastReaderSeg;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Callable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class is used to extract sequence from blast database.
 * @author Jiang Li
 * @version 1.0
 * @since 2018-01-23
 */
public class ExtractSeq implements Callable<String> {
    private final static Logger LOG=LogManager.getLogger(ExtractSeq.class.getName());
    FastReaderSeg frs;
    int index;
    String strOutput;
    ArrayList<String> accSet;
    boolean boolHit=false;
    boolean boolHit2=false;
    BufferedOutputStream bos;
    ArrayList<byte[]> altBufferHead;
    ArrayList<byte[]> altBufferSeq;

    public ExtractSeq(FastReaderSeg frs, int index, String strOutput) {
        this.frs = frs;
        this.index = index;
        this.strOutput = strOutput;
        bos=Factory.getOutputStream(strOutput);
        this.altBufferHead=new ArrayList<byte[]>();
        this.altBufferSeq=new ArrayList<byte[]>();
    }
 
    public void setAcc(ArrayList<String> accSet){
        this.accSet=accSet;
    }

    @Override
    public String call() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        System.out.println(df.format(new Date())+" ---> "+"FastReaderSeg "+this.index+" was running!");
        Runtime run=Runtime.getRuntime();
        System.out.println(this.index+" Memory--->(total: "+run.totalMemory()+") (free: "+run.freeMemory()+") (used: "+(run.totalMemory()-run.freeMemory())+") ");
        try {
            byte[] currentLine = null;
            int[] intSep = new int[1024*1024]; //too small may cause outofboundary problem.
            int[] intSH = new int[1024*1024]; //too small may cause outofboundary problem.
//            long longCount=0;
            while ((currentLine = frs.readLine(intSep, intSH)) != null) {
                if (currentLine[0] == 62) {
                    //output multiple head.
                    if (!altBufferHead.isEmpty()) {
                        for (int i = 0; i < altBufferHead.size(); i++) {
                            try {
                                //                        bos.write((byte) '>');
                                bos.write(altBufferHead.get(i));
                                bos.write((byte) '\n');
                                for (int j = 1; j < altBufferSeq.size(); j++) {
                                    bos.write(altBufferSeq.get(j));
                                    bos.write((byte) '\n');
                                }
                            } catch (IOException ex) {
                                LOG.error(ex.getMessage());
                            }
                        }
                        altBufferHead.clear();
                        altBufferSeq.clear();
                    }

                    //judge multiple head.
                    boolHit = false;
//                boolHit2=false;
                    String strAcc;
                    if(currentLine[intSep[1]-2]==(byte)'.') strAcc=strAcc = new String(currentLine, 1, intSep[1] - 3);
                    else    strAcc = new String(currentLine, 1, intSep[1]-1);
//                    System.out.println(strAcc);
                    if (this.accSet.contains(strAcc)) {
//                        System.out.println(strAcc);
                        boolHit = true;
                    }
                    if (intSH[0] > 1) {
                        for (int i = 1; i < intSH[0]; i++) {
                            int index = this.findBigger(intSep, intSH[i]);
                            String strAccTmp;
                            if(currentLine[index-2]==(byte)'.') strAccTmp = new String(currentLine, intSH[i] + 1, index - intSH[i] - 3);
                            else    strAccTmp = new String(currentLine, intSH[i] + 1, index - intSH[i] - 1);
                            if (this.accSet.contains(strAccTmp)) {
//                            boolHit2=true;
                                byte[] bytNewHead = new byte[currentLine.length];
                                bytNewHead[0] = (byte) 62;
                                if(currentLine[currentLine.length-1]==(byte)'\r'){
                                    System.arraycopy(currentLine, intSH[i] + 1, bytNewHead, 1, currentLine.length - intSH[i] - 1);
                                    System.arraycopy(currentLine, 1, bytNewHead, currentLine.length - intSH[i], intSH[i] - 1);

                                    bytNewHead[bytNewHead.length - 1] = bytNewHead[currentLine.length - intSH[i] - 1];
                                    bytNewHead[currentLine.length - intSH[i] - 1] = 1;                                 
                                }else{
                                    System.arraycopy(currentLine, intSH[i] + 1, bytNewHead, 1, currentLine.length - intSH[i]-1);
                                    System.arraycopy(currentLine, 1, bytNewHead, currentLine.length - intSH[i]+1, intSH[i] - 1);

//                                    bytNewHead[bytNewHead.length - 1] = bytNewHead[currentLine.length - intSH[i] - 1];
                                    bytNewHead[currentLine.length - intSH[i] ] = 1;                               
                                }

//                                System.out.println(new String(currentLine));
//                                System.out.println(new String(bytNewHead));
                                altBufferHead.add(bytNewHead);
                            }
                        }
                    }
                }
                if (boolHit) {
                    bos.write(currentLine);
                    bos.write((byte) '\n');
                }
                
                //if multiple head, save seq.
                if (!altBufferHead.isEmpty()) {
                    altBufferSeq.add(currentLine);
                }
//                if(longCount++%1000==0)    System.out.println(this.index+" Memory--->(total: "+run.totalMemory()+") (free: "+run.freeMemory()+") (used: "+(run.totalMemory()-run.freeMemory())+") ");
            }
            bos.close();

        } catch (IOException ex) {
            LOG.error(ex.getMessage());
            System.out.println(this.index+" Memory--->(total: "+run.totalMemory()+") (free: "+run.freeMemory()+") (used: "+(run.totalMemory()-run.freeMemory())+") ");
        }catch (Exception ex){
            System.out.println(this.index+"--->The ExtractSeq was crashed!!!");
            System.out.println(this.index+" Memory--->(total: "+run.totalMemory()+") (free: "+run.freeMemory()+") (used: "+(run.totalMemory()-run.freeMemory())+") ");
        }
        
        System.out.println(df.format(new Date())+" ---> "+"FastReaderSeg "+this.index+" was finished!");
        System.out.println(this.index+" Memory--->(total: "+run.totalMemory()+") (free: "+run.freeMemory()+") (used: "+(run.totalMemory()-run.freeMemory())+") ");
        return this.strOutput + " was set.";
    }
    
    public int findBigger(int[] a, int b){
        for(int i=1;i<a.length;i++){
            if(a[i]>b)  return a[i];
        }
        return -1;
    }

}
