/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.core.mut;

import java.util.HashMap;

/**
 * This Interface is used to define an index.
 * @author Jiang Li
 * @version 1.0 
 * @since 2018-11-24
 */
public interface Index {
    
    public void buildIndex();
    public HashMap getIndex();
    public void setIndex();
    public void checkFormat();
    public char findIndex();
   
}
