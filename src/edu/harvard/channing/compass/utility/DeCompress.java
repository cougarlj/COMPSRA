/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.utility;

import edu.harvard.channing.compass.core.Configuration;
import edu.harvard.channing.compass.core.Factory;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class is used to decompress files.
 * @author Jiang Li
 * @version 1.0
 * @since 2018-08-15
 */
public class DeCompress {
    private static final Logger LOG = LogManager.getLogger(DeCompress.class.getName());
    
    
    public static void deCompress(String strTarget){
        if(strTarget.endsWith("gz")){
            DeCompress.UnGZipFile(strTarget);
        }else if(strTarget.endsWith(".zip")){
            
        }else{
            LOG.warn("Unknown decompressed format : "+strTarget+" !");
        }
    }
    
    
    public static void UnGZipFile(String strTarget) {
        BufferedReader br = Factory.getReader(strTarget);
        BufferedWriter bw = Factory.getWriter(strTarget.substring(0, strTarget.lastIndexOf('.')));
        String strLine;
        try {
            while ((strLine = br.readLine()) != null) {
                bw.write(strLine);
                bw.newLine();
            }
            br.close();
            bw.close();
        } catch (IOException ex) {
            LOG.error("Fail to uncompress the file "+strTarget);
            LOG.error(ex.getMessage());
        }
    }   
    
    public static void UnZipFile(String strTarget){
        try {
            File fleTarget=new File(strTarget);
            ZipFile zip=new ZipFile(fleTarget);
            for(Enumeration<? extends ZipEntry> entries=zip.entries();entries.hasMoreElements();){
                ZipEntry entry=(ZipEntry) entries.nextElement();
                String strZipName=fleTarget.getParent()+File.separator+entry.getName();
                if(strZipName.endsWith("/")){
                    File fleOut=new File(strZipName);
                    fleOut.mkdirs();
                    continue;
                }
                
                InputStream in=zip.getInputStream(entry);
                FileOutputStream out=new FileOutputStream(strZipName);
                byte[] buf=new byte[Configuration.BUFF1];
                int len;
                while((len=in.read(buf))>0){
                    out.write(buf,0,len);
                }
                in.close();
                out.close();               
            }
        } catch (IOException ex) {
            LOG.error("Fail to uncompress the file "+strTarget);
            LOG.error(ex.getMessage());
        }
    }
    
    public static void UnTarGZ(String strTarget){
        File fleTarget=new File(strTarget);
        try {
            TarArchiveInputStream tis=new TarArchiveInputStream(new GzipCompressorInputStream(new BufferedInputStream(new FileInputStream(fleTarget))));
            TarArchiveEntry tae;
            while((tae=(TarArchiveEntry)tis.getNextTarEntry())!=null){
                if(tae.isDirectory()){
                    new File(fleTarget.getParentFile(),tae.getName()).mkdirs();
                }else{
                    FileOutputStream fos=new FileOutputStream(new File(fleTarget.getParentFile(),tae.getName()));                 
                    byte[] buf=new byte[Configuration.BUFF1];
                    int len;
                    while((len=tis.read(buf))!=-1){
                        fos.write(buf, 0, len);
                    }
                    fos.close();
                }
            }
            
        } catch (IOException ex) {
            LOG.error("Fail to uncompress the file "+strTarget);
            LOG.error(ex.getMessage());
        }
    }
    
    public static void main(String[] argvs){
        DeCompress dc=new DeCompress();
//        DeCompress.UnZipFile("E:\\01Work\\miRNA\\project\\COMPASS\\bundle_v1\\plug\\star\\STAR-2.6.1a.zip");
        DeCompress.UnTarGZ("E:\\01Work\\miRNA\\project\\COMPASS\\plug\\blast\\ncbi-blast-2.7.1+-x64-linux.tar.gz");
        
    }
    
}
