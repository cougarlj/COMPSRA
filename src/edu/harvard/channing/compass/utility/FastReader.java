/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.utility;

import edu.harvard.channing.compass.core.Configuration;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * This class is used to speed up the I/O part of the pipeline.
 * @author Jiang Li
 * @version 1.0
 * @since 2018-01-18
 */
public class FastReader {
    public String strFile;
    public int thread;
    public ByteBuffer byteBuff;
    public int buff_size=1024*1024; //1M
    public FileChannel fc;
    public long pos[];
    public RandomAccessFile raf;
    public boolean notBGZF = false;
    public byte[] buf = new byte[this.buff_size];
    public FastReaderSeg[] frs;
    public byte delimiter=' ';

    public FastReader(String strFile, int n_thread,byte delimiter) {
        this.strFile=strFile;
        this.thread = n_thread;
        this.delimiter=delimiter;
        pos = new long[n_thread + 1];
    }

    
    public int adjustPos() throws IOException {
        File inputFile=new File(strFile);
        for (int i = 0; i < this.thread; i++) {
            pos[i] = (inputFile.length() / this.thread * i);
        }
        pos[pos.length - 1] = inputFile.length();

        if (inputFile.length() < 1024 * 1024) {
            this.thread = 1;
            pos[1] = pos[pos.length - 1];
            return this.thread;
        }
                
        if(!strFile.endsWith("gz"))  return this.thread;
        
        raf = new RandomAccessFile(inputFile.getCanonicalFile(), "r");
        for (int i = 1; i < this.thread; i++) {
            raf.seek(pos[i]);
            boolean boolNoFound = true;
            int n_buf = -1;//if n_buf>0,we can consider this file is not bgzf format, but the gzip format. 
            do {
                raf.read(buf);
                n_buf++;
                if (n_buf > 0) {
                    notBGZF = true;
                    this.thread = 1;
                    pos[1] = pos[pos.length - 1];
                    // System.out.println("The file is gzip-format, not bgzip-format!");
                    break;
                }
                for (int id = 0; id < buf.length - 4; id++) {
                    if (buf[id] == 31 && buf[id + 1] == -117 && buf[id + 2] == 8 && buf[id + 3] == 4 && buf[id + 4] == 0) { //This should be used unsigned number or others. 
                        pos[i] += (id + n_buf * buf.length);
                        boolNoFound = false;
                        break;
                    }
                }
            } while (boolNoFound);
            if (notBGZF) {
                break;
            }
        }
        raf.close();
        
        //For file with small size and many threads. 
        for (int i = 0; i < (this.thread - 1); i++) {
            if (pos[i] == pos[i + 1]) {
                this.thread = 1;
                pos[1] = pos[pos.length - 1];
            }
        }
        
        return this.thread;
    }

    public void creatFRS() throws IOException {
        frs = new FastReaderSeg[this.thread];
        for (int i = 0; i < this.thread; i++) {
            frs[i] = new FastReaderSeg(strFile, i, this.pos[i], this.pos[i + 1], this.delimiter);
             System.out.println("FastReaderSeg" + i + ": created!");
            if (i > 0) {
                frs[i - 1].bytEndLine = frs[i].getStartLine();
            }
        }
    }






    
    
}
