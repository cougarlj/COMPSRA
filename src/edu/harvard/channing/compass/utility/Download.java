/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.utility;

import edu.harvard.channing.compass.core.Factory;
import edu.harvard.channing.compass.core.Configuration;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.commons.io.FileUtils;

/**
 * This class is used to download resource.
 * @author Jiang Li
 * @version 1.0
 * @since 2018-08-15
 */
public class Download {
    private static final Logger LOG = LogManager.getLogger(Download.class.getName());
    
    public String strTargetURL;
    public String strLocalFile;
    public boolean needUncompress=false;

    public Download(String strTargetURL, String strLocalFile, boolean needUncompress) {
        this.strTargetURL = strTargetURL;
        this.strLocalFile = strLocalFile;
        this.needUncompress = needUncompress;
    }
    
    public Download(String strLocalFile,boolean needUncompress){
        this.strLocalFile=strLocalFile;
        File fleLocal=new File(strLocalFile);
        this.strTargetURL=Configuration.DOWNLOAD.get(fleLocal.getName());
        this.needUncompress=needUncompress;
    }
    
    public Download(String strLocalFile){
        this.strLocalFile=strLocalFile;
        File fleLocal=new File(strLocalFile);
        this.strTargetURL=Configuration.DOWNLOAD.get(fleLocal.getName());
    }    
    
    public boolean download() {
        try {
            LOG.info("Start to download the resource: "+this.strTargetURL);
            File fleLocal=new File(this.strLocalFile);
            if(!fleLocal.getParentFile().exists()){
                fleLocal.getParentFile().mkdirs();
            }
            FileUtils.copyURLToFile(new URL(this.strTargetURL), new File(this.strLocalFile), 10000, 10000);//10s    
            LOG.info("Finish to download the resource: "+this.strTargetURL);
            
            if (this.needUncompress) {
                if (strLocalFile.endsWith(".tar.gz")) {
                    DeCompress.UnTarGZ(this.strLocalFile);
                } else if (strLocalFile.endsWith(".gz")) {
                    DeCompress.UnGZipFile(this.strLocalFile);
                } else if (strLocalFile.endsWith(".zip")) {
                    DeCompress.UnZipFile(this.strLocalFile);
                } else {
                    LOG.info("Couldn't uncompress the file " + strLocalFile);
                }
            }
            LOG.info("File "+this.strLocalFile+" was uncompressed.");
        } catch (IOException e) {
            LOG.info("Fail to download the resource: "+this.strTargetURL);
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
//    public static void UnGZipFile(String strTarget) {
//        BufferedReader br = Factory.getReader(strTarget);
//        BufferedWriter bw = Factory.getWriter(strTarget.substring(0, strTarget.lastIndexOf('.')));
//        String strLine;
//        try {
//            while ((strLine = br.readLine()) != null) {
//                bw.write(strLine);
//                bw.newLine();
//            }
//            br.close();
//            bw.close();
//        } catch (IOException ex) {
//            LOG.info("Fail to uncompress the file "+strTarget);
//            LOG.error(ex.getMessage());
//        }
//    }
    
    public static void main(String[] argv){
        Configuration config=new Configuration();
        Download dld=new Download("E:\\01Work\\miRNA\\project\\COMPASS\\bundle_v1\\prebuilt_db\\miRBase_hg19.obj",true);
        dld.download();
        System.out.println("Download is finished.");
    }

}
