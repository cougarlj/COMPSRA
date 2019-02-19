/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.utility;

import edu.harvard.channing.compass.entity.DBLeaf;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class is used to provide some functions of math.
 * @author Jiang Li
 * @version 1.0 
 * @since 2017-10-01
 */
public class MathTools {
    private static final Logger LOG = LogManager.getLogger(MathTools.class.getClass());
    /**
     * This function is used to judge the overlap of two region (a1,a2) and (b1,b2). 
     * @param a1 Start point of Region a. 
     * @param a2 End point of Region a.
     * @param b1 Start point of Region b.
     * @param b2 End point of Region b.
     * @return No overlap (z<=0), partial overlap (0<z<1), total overlap (z>=1). Note: The ratio is relative, say to make the value maximum by overlap/genome or overlap/read.
     */
    public static float compRegion(int a1, int a2, int b1, int b2){
        int la=a2-a1;
        int lb=b2-b1;
        float d1=(float)(b2-a1);
        float d2=(float)(a2-b1);
        return Math.max(Math.min(d1/la, d2/la), Math.min(d1/lb,d2/lb));
    }
    

    /**
     * This function is used to judge the overlap of two region (a1,a2) and (b1,b2). 
     * It can differ the situation that whether the reads are longer than the annotated region. 
     * When the read length is longer than the average length of miRNAs (~22nt), we don't think this read comes from miRNAs. 
     * (This point should be discussed,because we find lots of 1-more-longer reads.)
     * @param a1 Start point of Region a. 
     * @param a2 End point of Region a.
     * @param b1 Start point of Region b.
     * @param b2 End point of Region b.
     * @return z=0,no overlap;z<0,overlapped and read longer than region;z>0,overlapped and read shorter than region.
     */    
    public static float compRegion2(int a1, int a2, int b1, int b2){
        int la=a2-a1;
        int lb=b2-b1;
        float d1=(float)(b2-a1);
        float d2=(float)(a2-b1);
        float overlap=Math.max(0, Math.max(Math.min(d1/la, d2/la), Math.min(d1/lb,d2/lb)));
        if((la-lb)>=0){
            return 1*overlap;
        }else{
            return (-1)*overlap;
        }     
    }

    /**
     * This function is used to judge the overlap of two region (a1,a2) and (b1,b2). 
     * @param a1 Start point of Region a. 
     * @param a2 End point of Region a.
     * @param b1 Start point of Region b.
     * @param b2 End point of Region b.
     * Note: This function take region (b1,b2) as reference.
     * @return No overlap (z<=0), partial overlap (0<z<1), total overlap (z>=1). 
     */
    public static float compRegion3(int a1, int a2, int b1, int b2){
        if(b1==69933143){
            System.out.println();
        }
        int la=a2-a1;
        int lb=b2-b1;
        float d1=(float)(b2-a1);
        float d2=(float)(a2-b1);
        return Math.min(d1/lb,d2/lb);
    }
    
    public static void main(String[] argv){
        System.out.println(compRegion(69933142,69933162,69933143,69933165));
    }

    public static double[][] CpM(double[][] intProfile) {
        double[][] dblProfile = intProfile.clone();
        try {

            for (int i = 0; i < dblProfile[0].length; i++) {
                int intSum = 0;
                for (int j = 0; j < dblProfile.length; j++) {
                    intSum += dblProfile[j][i];
                }
                for (int j = 0; j < dblProfile.length; j++) {
                    dblProfile[j][i] = dblProfile[j][i] / intSum * 1e6;
                }
            }
        } catch (Exception ex) {
            LOG.error("CpM Normalization is failed! The result may be not right!");
            return intProfile;
        }
        return dblProfile;
    }

    public static double[][] QT(double[][] intProfile) {
        double[][] dblProfile = intProfile.clone();
        try {
            //To be Added.


        } catch (Exception ex) {
            LOG.error("Quantile Normalization is failed! The result may be not right!");
            return intProfile;
        }
        return dblProfile;
    }

    public static double[][] QTL(double[][] intProfile) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
