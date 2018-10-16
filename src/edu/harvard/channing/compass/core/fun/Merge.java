/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.core.fun;

import edu.harvard.channing.compass.entity.CommonParameter;
import java.util.concurrent.Callable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class is used to merge samples according to different categories.
 * @author Jiang Li
 * @version 1.0 
 * @since 2018-05-01
 */
public class Merge extends DEG implements Callable{
    private static final Logger LOG = LogManager.getLogger(Function.class.getName());

    public Merge(CommonParameter com, String strClass, String strCase, String strCtrl, String strNorm, String strTest, boolean boolOrder, boolean boolMic, boolean boolAnn, String strTool, String strBlastDB) {
        super(com, strClass, strCase, strCtrl, strNorm, strTest, boolOrder, boolMic, boolAnn, strTool, strBlastDB);
    }

    @Override
    public Object call() throws Exception {
        try {
            this.showModule();
            this.parseClass();
            LOG.info("Class Parse ---> OK!");
            this.parseTest();
            this.parseSample();
            LOG.info("Sample Parse ---> OK!");
            this.detachRNA();
            LOG.info("Detach RNA Process ---> OK!");
            this.runDEG();
            LOG.info("Run Merge  ---> OK!");
        } catch (Exception e) {
            LOG.info("Merge was failed to run.");
            return "Merge failed!";
        }
        return "DEG was finished!";
    }
    
    @Override
    public void showModule(){
        StringBuilder sb = new StringBuilder();
        sb.append("\n\n-----------\n");
        sb.append("|   Merge   |");
        sb.append("\n-----------\n");
        LOG.info(sb.toString());
        
    }          
    
}
