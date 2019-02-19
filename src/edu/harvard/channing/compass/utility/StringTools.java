/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.utility;

import java.util.ArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * StringTools will provide some functions in string operation. 
 * @author Jiang Li
 * @version 1.0 
 * @since 2017-09-12
 */
public class StringTools {
    private static final Logger LOG = LogManager.getLogger(StringTools.class.getName());
    /**
     * FindLongestSuffix is used to find the reasonable position of adapter.
     * @param read The read sequence.
     * @param adapter The adapter sequence.
     * @return The start point of the suffix backwards. 
     */
    public static int FindLongestSuffix(String read,String adapter){
        int intPointer=0;
        for(int i=read.length()-1;i>=0;i--){
            int j;
            for(j=0;j<Math.min(read.length()-i,adapter.length());j++){
                if(read.charAt(i+j)!=adapter.charAt(j))   break;             
            }
            if(j==adapter.length()){
                return i;
            }else if(j==(read.length()-i)){
                intPointer=i;
            }
        }
        return intPointer;
    }
    
    /**
     * @param read  The read sequence. 
     * @param adapter   The adapter sequence. 
     * @param threshold The threshold of tolerable sequencing errors. 
     * @return 
     */
    public static int FindLongestSuffix(String read,String adapter,int threshold){
        int intPointer=0;
        for(int i=read.length()-1;i>=0;i--){
            int j;
            int error=0;
            for(j=0;j<Math.min(read.length()-i,adapter.length());j++){               
                if(read.charAt(i+j)!=adapter.charAt(j)){
                    error++;
                    if(error>threshold) break;
                }             
            }
            if(j==adapter.length()){
                return i;
            }else if(j==(read.length()-i)){
                intPointer=i;
            }
        }
        return intPointer;
    }
    
    public static String[] getKV(String str){
        Pattern ptn=Pattern.compile("\\'\\w+\\'\\:");
        Matcher mch=ptn.matcher(str);
        ArrayList<Integer> altIndex=new ArrayList();
        while(mch.find()){
            altIndex.add(mch.start());
//            System.out.println(mch.start());
//            System.out.println(mch.group());
//            System.out.println("----------------------");
        }
        altIndex.add(str.length()-1);
        String[] kv=new String[altIndex.size()];
        for(int i=0;i<altIndex.size()-1;i++){
            kv[i]=str.substring(altIndex.get(i), altIndex.get(i+1)).replaceAll("\\'|,\\s*$", "");
//            System.out.println(kv[i]);
        }        
        return kv;       
    }
   
    public static ArrayList<String> splitCIGAR(String cigarString) {
        //One cigar component is a number of any digit, followed by a letter or =
        Pattern cigarPattern = Pattern.compile("[\\d]+[a - zA - Z |=]");
        ArrayList<String> cigarElems = new ArrayList<String>();
        Matcher matcher = cigarPattern.matcher(cigarString);
        while (matcher.find()) {
            cigarElems.add(matcher.group());
        }
        return cigarElems;
    } 
    
    public static String padCIGAR(String cigarString){
        Pattern cigarPattern = Pattern.compile("[\\d]+[a - zA - Z |=]");
        Matcher matcher = cigarPattern.matcher(cigarString);
        StringBuilder sb=new StringBuilder();
        while (matcher.find()) {
            String strGroup=matcher.group();
            int intN=Integer.parseInt(strGroup.substring(0, strGroup.length()-1));
            char charLetter=strGroup.toUpperCase().charAt(intN-1);
            for(int j=0;j<intN;j++) sb.append(charLetter);
        }
        return sb.toString();
    }
    
    public static String byte2string(ArrayList<Byte> byt){
        StringBuilder sb=new StringBuilder();
        for(byte b:byt) sb.append((char)b);
        return sb.toString();        
    }
    
    public static void main(String[] args) {
//        String read1="CTGCTCCCTGGTGGTCTAGTGGTTAGGATTCGGCGCTCTTCGTGGAATTC"; //partly overlap. return 42
//        String read2="CCACTCCTGACACCAGGGCTGGAATTCTCGGGTGCCAAGGTGGAATTCTC"; //totally overlap. return 19
//        String read3="TCTTATAGAGGAGACAAGTCGTAACATGGTAAGTGTACTGGAAAGTGCAC"; //no overlap. return 0
        String read4="TCCTGTACTGAGCTGCCCCGATGGAATTCTCGAGTGCCAAGGAACTCCAGT"; // one error in the middle. return 21
        String read5="TCCTGTACTGAGCTGCCCCGACGGAATTCTCGGGTGCCAAGGAACTCCAGT"; // one error at the first base. return 21
        String read6="AGTACTAATAGACCGAGGGCTTGACCTGGAATTCTCGGGTGCCAACGAACT"; //one error at the end. return 26
        String read7="TCCTGTACTGAGCTGCCCCGATGGAATTCTCGGGTGTTAAGGAACTCCAGT"; // two error in the middle. return 21
        String adapter="TGGAATTCTCGGGTGCCAAGG"; 
        int index=FindLongestSuffix(read7,adapter,2);
        System.out.println(index);
//       String str="'ext': set(['GCF_000012825', 'GCF_000158335', 'GCF_000155815']), 'score': 3.0, 'clade': 's__Bacteroides_sp_4_3_47FAA', 'len': 729, 'taxon': 'k__Bacteria|p__Bacteroidetes|c__Bacteroidia|o__Bacteroidales|f__Bacteroidaceae|g__Bacteroides|s__Bacteroides_sp_4_3_47FAA'";
//       getKV(str);
       System.out.println("HeHe");
    }
}
