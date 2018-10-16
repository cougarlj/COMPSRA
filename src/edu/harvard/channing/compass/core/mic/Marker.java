/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.core.mic;

import edu.harvard.channing.compass.entity.BioNode;
import edu.harvard.channing.compass.utility.StringTools;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is used to save the marker information "markers_info.txt".
 * @author Jiang Li
 * @version 1.0
 * @since 2017-10-15
 */
public class Marker implements Serializable{
    public String gi;
//    public String ref_gb;
//    public char strand;
//    public int start;
//    public int end;
//    public String range;
    public String key;
    public float score;
    public String clade;
    public int len;
    public String taxon;
    public ArrayList<String> ext;
    public int hit=0;
    public BioNode owner;
    public boolean boolDisEq=false;

    public void initMarker(String strLine) {
        String[] strItem=strLine.split("\t");
        
        //Part One.
        this.key=strItem[0].trim();

        //Part Two.
        String[] kvs=StringTools.getKV(strItem[1]);
        Matcher mch=Pattern.compile("\\w+_*\\d+").matcher(kvs[0]);
        if(mch.find()){
            this.ext=new ArrayList<String>();
            this.ext.add(mch.group());
            while(mch.find())   this.ext.add(mch.group());
        }
        this.score=Float.valueOf(kvs[1].split(":")[1].trim());
        this.clade=kvs[2].split(":")[1].trim();
        this.len=Integer.valueOf(kvs[3].split(":")[1].trim());
        this.taxon=kvs[4].split(":")[1].trim();      
    }   
    
    public static void main(String[] args) {
        
//        Matcher mch=Pattern.compile("\\w+_\\d+").matcher("ext: set([GCF_000012825, GCF_000158335, GCF_000155815])");
//        Matcher mch=Pattern.compile("\\w+_\\d+").matcher("ext: set([])");
//
//        while(mch.find()){
//            System.out.println(mch.group());
//        }
        
        String strLine="gi|345651636|ref|NZ_JH114379.1|:c39238-38510	{'ext': set(['PRJNA14546', 'GCF_000158335', 'GCF_000155815']), 'score': 3.0, 'clade': 's__Bacteroides_sp_4_3_47FAA', 'len': 729, 'taxon': 'k__Bacteria|p__Bacteroidetes|c__Bacteroidia|o__Bacteroidales|f__Bacteroidaceae|g__Bacteroides|s__Bacteroides_sp_4_3_47FAA'}";
        Marker mk=new Marker();
        mk.initMarker(strLine);
        
    }
}
