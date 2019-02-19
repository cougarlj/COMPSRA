/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.core.mut;

import edu.harvard.channing.compass.core.Factory;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class is used to merge the variant (*.vt) files.
 *
 * @author Jiang Li
 * @version 1.0
 * @since 2018-12-06
 */
public class MergeVariant {

    private static final Logger LOG = LogManager.getLogger(MergeVariant.class.getName());

    public ArrayList<String> altSamples;
    public String strOut;
    public HashMap<String, ArrayList<SNPRecord>> hmpMerge;

    public MergeVariant(ArrayList<String> altSamples, String strOut) {
        this.altSamples = altSamples;
        this.strOut = strOut;
    }

    public void mergeVariant() {
        hmpMerge = new HashMap();
        try {
            for (String strSample : altSamples) {
                BufferedReader br = Factory.getReader(strSample);
                String strLine = br.readLine();
                while ((strLine = br.readLine()) != null) {
                    SNPRecord snp = new SNPRecord(strLine, new File(strSample).getName());
                    if (!hmpMerge.containsKey(snp.getKey())) {
                        hmpMerge.put(snp.getKey(), new ArrayList());
                    }
                    hmpMerge.get(snp.getKey()).add(snp);
                }
                br.close();
            }
        } catch (IOException ex) {
            LOG.error(ex.getMessage());
        }

        this.outputMerge();
    }

    public void outputMerge() {
        try {
            BufferedWriter bw = Factory.getWriter(strOut);

            //Output Head.
            StringBuilder sb = new StringBuilder();
            sb.append(SNPRecord.getVariantHead());
            sb.append("\tTS\tMS\tRate");
            for (String strSample : altSamples) {
                sb.append("\t");
                sb.append(strSample);
            }
            bw.write(sb.toString());
            bw.newLine();
            sb.delete(0, sb.length());

            //Output Body.
            ArrayList<String> lstKey = new ArrayList(hmpMerge.keySet());
            Collections.sort(lstKey);
            for (String key : lstKey) {
//                SNPRecord snp = SNPRecord.mergeSNP(hmpMerge.get(key));
                SNPRecord snp = new SNPRecord(key);
//                sb.append(snp.getVariant());

                sb.append("\t"+altSamples.size()+"\t"+hmpMerge.get(key).size()+"\t"+((float)hmpMerge.get(key).size()/(float)altSamples.size()));
                
                int intC = 0;
                for (String strSample : altSamples) {                    
                    if (intC < hmpMerge.get(key).size()) {
                        SNPRecord tmp = hmpMerge.get(key).get(intC);
                        if (tmp.sample.equals(new File(strSample).getName())) {
                            //output
                            snp.mergeSNP(tmp);
                            sb.append("\t");
                            sb.append("<");
                            sb.append(tmp.getVariant2());
                            sb.append(">");
                            intC++;
                            continue;
                        }
                    }
                    
                    sb.append("\t");
                    sb.append("<*>");
                }
                bw.write(snp.getVariant() + sb.toString());
                bw.newLine();
                sb.delete(0, sb.length());
            }
            bw.close();
        } catch (IOException ex) {
            LOG.error(ex.getMessage());
        }

    }

}
