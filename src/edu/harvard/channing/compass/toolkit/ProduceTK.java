/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.toolkit;

import edu.harvard.channing.compass.core.Produce;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class is used to manage the ToolKit. 
 * @author Jiang Li
 * @version 1.0 
 * @since 2018-01-12
 */
public class ProduceTK extends Produce{
    private static final Logger LOG = LogManager.getLogger(ProduceTK.class.getName());
    
    public ToolKit tk;
    public int intFlag;
    
    public ProduceTK() {
        super(null,null,null,null,null);
    }

    public void setTK(ToolKit tk){
        this.tk=tk;
    }
    /**
     * The main function to adjust and control the process of different modules. 
     */
    @Override
    public void Dispatch(){   
        if(this.tk==null){
           LOG.error("No ToolKit was set!");
           return;
        }
        this.showModule();
        int intFlag=tk.runKit();
        this.report("ToolKit", intFlag);  
        
    }
    
    public void showModule() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n+++++++++++++++++++++++++++++++\n");
        sb.append("+      COMPASS ---> ToolKit   +");
        sb.append("\n+++++++++++++++++++++++++++++++\n");
        System.out.println(sb.toString());

    }
  
}
