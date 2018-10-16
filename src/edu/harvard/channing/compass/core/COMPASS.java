/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.core;

//import org.apache.logging.log4j.Logger;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The main class of the COMPASS pipeline. 
 * @author Jiang Li
 * @version 1.0 
 * @since 2017-08-23
 */
public class COMPASS {
    /**
     * This is used to record the output of CircuRNA pipeline. 
     */
//    private static final Logger LOG = LogManager.getLogger(COMPASS.class.getName());

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        String strLog = System.getProperty("user.dir") + "/" + new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS").format(new Date()) + ".log";
        System.setProperty("MyLogFile0", strLog);
        
//        for (String envKeys : System.getenv().keySet()) {
//            System.out.println(envKeys + " ---> " + System.getenv().get(envKeys));
//        }     

//        Properties prop = System.getProperties();
//        for (String key : prop.stringPropertyNames()) {
//            System.out.println(key + " ---> " + prop.getProperty(key));
//        }

        //Set Configuration.      
        Configuration config=new Configuration();   
        
        //Step 1: Parse Options.
        Option option=new Option();
        Produce process=option.parseOption(args);
        if(process==null){
            //LOG.info("Error: The process is failed!");
            return;
        }

        //Step 2: Run ToolKit if used. 
        if(process.getClass().getName().contains("ProduceTK")){
            process.Dispatch();
            return;
        }

        //Step 3: Run Pipeline.
        System.setProperty("MyLogFile1",option.comParam.altInput.get(0).output_dir+"LOG.log");
        process.Dispatch();
        
        //Step 4: Say Byebye.
        StringBuilder sb = new StringBuilder();
        sb.append("\n+++++++++++++++++++++++++++++++++++++++\n");
        sb.append("+  The COMPASS pipeline was finished! +\n");
        sb.append("+           Welcome to use!           +\n");
        sb.append("+++++++++++++++++++++++++++++++++++++++\n");
        System.out.println(sb.toString());
        
    }
       
}
