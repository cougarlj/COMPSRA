/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.core.ann;

import edu.harvard.channing.compass.entity.FileRecord;
import java.util.concurrent.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This interface define the public function of Annotator.
 * @author Jiang Li
 * @version 1.0 
 * @since 2017-09-23
 */
public abstract class Annotator implements Callable {
    private static final Logger LOG = LogManager.getLogger(Annotator.class.getName());
    
    public FileRecord frd;
    public boolean isInCluster=false;
    public float fltOverlap=1;
    public int intTD=1;
    public boolean boolRmSamMap=false;
    public boolean boolCR=false;
    public boolean needBAMOutput=false;
    public boolean boolShowUnAnn=false;
    public boolean useUMI=false;
    
    public void setInput(FileRecord frd){
        this.frd=frd;
    }
    public abstract String Annotate();

    
}
