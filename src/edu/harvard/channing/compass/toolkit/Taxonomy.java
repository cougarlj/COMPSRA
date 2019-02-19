/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.toolkit;

import edu.harvard.channing.compass.core.Factory;
import edu.harvard.channing.compass.core.Configuration;
import edu.harvard.channing.compass.utility.FastReader;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Taxonomy will provide some functions in taxonomy operation. 
 * @author Jiang Li
 * @version 1.0 
 * @since 2018-01-12
 */
public class Taxonomy implements ToolKit{
    private static final Logger LOG = LogManager.getLogger(Taxonomy.class.getName());
    
    public String strNode;
    public String strName;
    public String strA2T;
    public String strBlastSeq;
    public String strBlastResult;
    public String strTaxID;
    
    public String taxid="1";
    public String extract="";
    public String strOut;
    public boolean map2tree;
    public int thread=1;
    public int style=1234;


    public ConcurrentHashMap<String, Tax> taxTree;
    public HashMap<String, String> ComUse;
    public ArrayList<String> taxSet;
    public ArrayList<String> accSet;
    public HashMap<String, ArrayList<String>> tax2acc;
    public ConcurrentHashMap<String,String> acc2tax;
    public HashMap<String,ArrayList<Tax>> read2node;
    
    public int intNoMapping=0;
    public int intTotalMapping=0;
    public String[] strSurfix={"_tree.txt","_children.txt","_tax2acc.txt","_tax2acc.txt","_blastseq.fasta","_tree.txt"};
    SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    public ConcurrentHashMap<String,ArrayList<String>> hmpAcc;

    public Taxonomy() {
        taxSet=new ArrayList<String>();
        accSet=new ArrayList<String>();
    }

    public Taxonomy(String names, String nodes, String a2t) {
        this.strName=names;
        this.strNode=nodes;
        this.strA2T=a2t;
        
    }
       
    public void init(){
        //Initial ComUse
        ComUse=new HashMap<String,String>();
        ComUse.put("vira","10239");
//        ComUse.put("viridae", "10239");
        ComUse.put("viruses","10239");
        ComUse.put("bacteria","2");
//        ComUse.put("monera","2");
//        ComUse.put("procaryotae","2");
//        ComUse.put("prokaryota","2");
//        ComUse.put("prokaryotae","2");
//        ComUse.put("eubacteria","2");
//        ComUse.put("prokaryote","2");
//        ComUse.put("prokaryotes","2");
        ComUse.put("fungi","4751");
        ComUse.put("archaea","2157");
        
        this.buildTree(this.strNode, this.strName);
    }

    public void buildTree(String node,String name){
//        //Initial Tree
//        taxTree=new ConcurrentHashMap<String,Tax>();
        try {
            //Read prebuilt database. If not, build right away. 
            File fleTaxonomy=new File(Configuration.NT.get("taxonomy"));
            if(fleTaxonomy.exists()){
                ObjectInputStream ois=new ObjectInputStream(new GZIPInputStream(new FileInputStream(fleTaxonomy)));
                this.taxTree=(ConcurrentHashMap<String, Tax>) ois.readObject();
                return;
            }
            
            //Initial Tree
            taxTree=new ConcurrentHashMap<String,Tax>();
            
            //Read nodes.
            BufferedReader br=Factory.getReader(node);
            String line=null;
            while((line=br.readLine())!=null){
                String[] items=line.split("\\|");
                Tax tax=new Tax(items[0].trim(),items[1].trim(),items[2].trim());
                taxTree.put(items[0].trim(), tax);
            }
            br.close();
            
            //Set names.
            br=Factory.getReader(name);
            while((line=br.readLine())!=null){
                String[] items=line.split("\\|");
                taxTree.get(items[0].trim()).names.add(items[1].trim());
            }                    
            
            //Set relationship.
            for(Tax tax:taxTree.values()){
                if(tax.tax_id.equals(tax.parent_id))    continue;
                tax.parent=taxTree.get(tax.parent_id);
                tax.parent.children.put(tax.tax_id, tax);
            }
            
            //Write mapping file to obj.
            ObjectOutputStream oos = null;
            oos = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(Configuration.NT.get("taxonomy"))));
            oos.writeObject(this.taxTree);
            oos.close();
      
        } catch (IOException ex) {
//            System.out.println(ex.getCause().getMessage());
            LOG.error(ex.getMessage());
        } catch (ClassNotFoundException ex) {
            LOG.error(ex.getMessage());
        }        
    }
    
    public void outputTree(Tax node, int style, String cum, BufferedWriter bw) {
        try {
            if (node.children.isEmpty()) {

                cum += node.makeRecored(style);
                bw.write(cum);
                bw.newLine();
                return;

            }

            cum += node.makeRecored(style) + "|";
            bw.write(cum);
            bw.newLine();

            for (Tax tax : node.children.values()) {
                this.outputTree(tax, style, cum, bw);
            }
        } catch (IOException ex) {
            LOG.error(ex.getMessage());
        }
    }
 
    public void outputMappingTree(Tax node, int style, String cum, BufferedWriter bw) {
        try {
            if (node.children.isEmpty()) {
                cum += node.makeRecored(style);
                if (!node.reads.isEmpty()) {
                    bw.write(cum);
                    bw.newLine();
                }
                return;
            }

            cum += node.makeRecored(style) + "|";
            if (!node.reads.isEmpty()) {
                bw.write(cum);
                bw.newLine();
            }

            for (Tax tax : node.children.values()) {
                this.outputMappingTree(tax, style, cum, bw);
            }
        } catch (IOException ex) {
            LOG.error(ex.getMessage());
        }
    }
    
    /**
     * This function is overwrite for the special output. 
     * @param node
     * @param cum
     * @param bw 
     */
    public void outputMappingTree(Tax node, String cum, BufferedWriter bw) {
        try {
            if (node.children.isEmpty()) {
                cum += node.makeRecored(666);
                if (!node.reads.isEmpty()) {
                    bw.write(cum);
                    bw.newLine();
                }
                return;
            }
            
            if (!node.reads.isEmpty()) {
                bw.write(cum+node.makeRecored(666));
                bw.newLine();
            }
            cum += node.makeRecored(123) + "|";

            for (Tax tax : node.children.values()) {
                this.outputMappingTree(tax, cum, bw);
            }
        } catch (IOException ex) {
            LOG.error(ex.getMessage());
        }
    }    
    
    public String outputChildren(Tax node){
        if(node.children.isEmpty()){
            this.taxSet.add(node.tax_id);
            return node.tax_id+"\n";
        }
        String output=node.tax_id+"\n";
        this.taxSet.add(node.tax_id);
        for(Tax tax:node.children.values()) output+=this.outputChildren(tax);
        return output;
    }
    
    public String getChildrenInfo(Tax node, int style){
        String record="";
        if(node.children.isEmpty()){
            record=node.makeRecored(style);
        }else{
            for(Tax tax:node.children.values()){
                record=node.makeRecored(style)+"|"+this.getChildrenInfo(node, style);
            }
        }       
        return record;
    }

    public void setTax2Acc(String strMap) {
        this.tax2acc = new HashMap<String, ArrayList<String>>();
        try {
            BufferedReader br = Factory.getReader(strMap);
            String strLine = br.readLine();
            while ((strLine = br.readLine()) != null) {
                String[] items = strLine.split("\t");
                String strTax = items[2].trim();
                String strAcc = items[1].trim();
                if (!this.tax2acc.keySet().contains(strTax)) {
                    this.tax2acc.put(strTax, new ArrayList<String>());
                    this.tax2acc.get(strTax).add(strAcc);
                } else {
                    this.tax2acc.get(strTax).add(strAcc);
                }
            }
        } catch (IOException ex) {
            LOG.error(ex.getMessage());
        }
    }
    
    public void setAcc2Tax(String strMap) {
//        System.out.println(df.format(new Date())+" ---> ("+this.strBlastResult+") Start to build map: Acc2Tax. ");
        
        try {
            //Read obj directly.
            File fleA2T=new File(strMap);
            if(fleA2T.exists()){
                ObjectInputStream ois=new ObjectInputStream(new GZIPInputStream(new FileInputStream(strMap)));
                this.acc2tax=(ConcurrentHashMap<String, String>) ois.readObject();
                return;
            }
            
            //Make mapping file. 
            this.acc2tax=new ConcurrentHashMap<>();
            BufferedReader br = Factory.getReader(Configuration.NT.get("A2T"));
            String strLine = br.readLine();
            while ((strLine = br.readLine()) != null) {
                String[] items = strLine.split("\t");
                this.acc2tax.put(items[1].trim(), items[2].trim());
            }
            System.out.println(df.format(new Date())+" ---> ("+this.strBlastResult+") Acc2Tax map was finished!");
            
            //Write mapping file to obj.
            ObjectOutputStream oos = null;
            oos = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(strMap)));
            oos.writeObject(this.acc2tax);
            oos.close();
            
        } catch (IOException ex) {
            LOG.error(ex.getMessage());
        } catch (ClassNotFoundException ex) {
            LOG.error(ex.getMessage());
        }
    }
    
    public void getAcc(BufferedWriter bw) {
//        String strOut="";
//        StringBuilder sb=new StringBuilder();
        for (String tax : this.taxSet) {
            if (!this.tax2acc.containsKey(tax)) {
                continue;
            }
            ArrayList<String> acc = this.tax2acc.get(tax);
            for (String id : acc) {
                try {
                    //                strOut+=tax+"\t"+id+"\n";
//                sb.append(tax+"\t"+id+"\n");
                    bw.write(tax + "\t" + id);
                    bw.newLine();
                    this.accSet.add(id);
                } catch (IOException ex) {
                    LOG.error(ex.getMessage());
                }
            }
        }
//        return sb;
//        return strOut;
    }

    public void extractSeq(String strIn, String strOut) {
        try {
            FastReader fr = new FastReader(strIn, this.thread,(byte)' ');
            fr.adjustPos();
            fr.creatFRS();
            ExtractSeq[] es = new ExtractSeq[this.thread];

            String message;
            ExecutorService exe = Executors.newCachedThreadPool();
            ArrayList<Future<String>> lstResult = new ArrayList<Future<String>>();
            for (int i = 0; i < this.thread; i++) {
                es[i] = new ExtractSeq(fr.frs[i], i, strOut + i);
//            accSet.add("K03166");
//            accSet.add("K03169");
                es[i].setAcc(accSet);
                Future<String> future = exe.submit(es[i]);
                lstResult.add(future);
            }

            for (Future<String> ft : lstResult) {
                try {
                    while (!ft.isDone());
                    message = ft.get();
                    System.out.println(message);
                } catch (InterruptedException ex) {
                    LOG.info(ex.getMessage());
                } catch (ExecutionException ex) {
                    LOG.info(ex.getMessage());
                } finally {
                    exe.shutdown();
                }
            }

            FileChannel outChannel = new FileOutputStream(strOut).getChannel();
            FileChannel inChannel;
            for (int i = 0; i < thread; i++) {
                File fle = new File(strOut + i);
                inChannel = new FileInputStream(fle).getChannel();
                inChannel.transferTo(0, inChannel.size(), outChannel);
                inChannel.close();
                fle.delete();
            }
            outChannel.close();

            System.out.println("The file " + strOut + " was set!");
        } catch (IOException ex) {
            LOG.error(ex.getMessage());
        }
    }

    @Override
    public int runKit() {
        
        this.showModule();
        
        System.out.println(df.format(new Date())+" ---> "+"Programme starts!");
        
        if (this.extract.equalsIgnoreCase("acc2tax")) {
            try {
                this.setTax2Acc(this.strA2T);
                BufferedReader br = Factory.getReader(this.strTaxID);
                BufferedWriter bw = Factory.getWriter(this.strOut);
                bw.write("accession\taccession.version\ttaxid\tgi");
                bw.newLine();

                String strTax;
                ArrayList<String> altTax = new ArrayList();
                while ((strTax = br.readLine()) != null) {
                    ArrayList<String> altAcc = this.tax2acc.get(strTax.trim());
                    if(altAcc==null || altAcc.isEmpty())    continue;
                    for (String str : altAcc) {
                        bw.write("-\t"+str + "\t" + strTax.trim()+"\t-");
                        bw.newLine();
                    }
                }
                br.close();
                bw.close();
            } catch (IOException ex) {
                LOG.error(ex.getMessage());
                return 0;
            }
        }       
               
        if(this.taxTree==null)  this.init();
        System.out.println(df.format(new Date())+" ---> "+"Initial part was finished!");
        
        if(this.extract.equalsIgnoreCase("tree0")){
            try {
                BufferedWriter bw=Factory.getWriter(this.strOut+this.strSurfix[0]);
                this.outputTree(this.taxTree.get(this.taxid), 0, "", bw);
                bw.close();
            } catch (IOException ex) {
                LOG.error(ex.getMessage());
                return 0;
            }
        }else if(this.extract.equalsIgnoreCase("tree1")){
            try {
                BufferedWriter bw=Factory.getWriter(this.strOut+this.strSurfix[0]);
                this.outputTree(this.taxTree.get(this.taxid), 1, "", bw);
                bw.close();
            } catch (IOException ex) {
                LOG.error(ex.getMessage());
                return 0;
            }            
        }else if(this.extract.equalsIgnoreCase("tree2")){
            try {
                BufferedWriter bw=Factory.getWriter(this.strOut+this.strSurfix[0]);
                this.outputTree(this.taxTree.get(this.taxid), 2, "", bw);
                bw.close();
            } catch (IOException ex) {
                LOG.error(ex.getMessage());
                return 0;
            }            
        }else if(this.extract.equalsIgnoreCase("id")){
            try {
                String strChildren=this.outputChildren(this.taxTree.get(this.taxid));
                BufferedWriter bw=Factory.getWriter(this.strOut+this.strSurfix[1]);
                bw.write(strChildren);
                bw.close();
            } catch (IOException ex) {
                LOG.error(ex.getMessage());
                return 0;
            }
        }else if(this.extract.equalsIgnoreCase("acc")){
            try {
                this.outputChildren(this.taxTree.get(this.taxid));
                this.setTax2Acc(this.strA2T);
                BufferedWriter bw=Factory.getWriter(this.strOut+this.strSurfix[2]);
                this.getAcc(bw);
                
//                bw.write(sbMap.toString());            
                bw.close();
            } catch (IOException ex) {
                LOG.error(ex.getMessage());
                return 0;
            }
        }else if(this.extract.equalsIgnoreCase("seq")){
            try {
                Runtime run=Runtime.getRuntime();
                System.out.println("Memory--->(total: "+run.totalMemory()+") (free: "+run.freeMemory()+") (used: "+(run.totalMemory()-run.freeMemory())+") ");
                this.outputChildren(this.taxTree.get(this.taxid));                
                System.out.println(df.format(new Date())+" ---> "+"Children nodes output were finished!");
                System.out.println("Memory--->(total: "+run.totalMemory()+") (free: "+run.freeMemory()+") (used: "+(run.totalMemory()-run.freeMemory())+") ");
                
                this.setTax2Acc(this.strA2T);
                System.out.println(df.format(new Date())+" ---> "+"Tax2Acc was finished!");
                System.out.println("Memory--->(total: "+run.totalMemory()+") (free: "+run.freeMemory()+") (used: "+(run.totalMemory()-run.freeMemory())+") ");
                
                BufferedWriter bw = Factory.getWriter(this.strOut + this.strSurfix[3]);
                this.getAcc(bw);
                System.out.println(df.format(new Date())+" ---> "+"Tax2Acc was output!");
                System.out.println("Memory--->(total: "+run.totalMemory()+") (free: "+run.freeMemory()+") (used: "+(run.totalMemory()-run.freeMemory())+") ");
                
//                bw.write(sbMap.toString());
                bw.close();
                this.extractSeq(this.strBlastSeq, this.strOut + this.strSurfix[4]);
                System.out.println(df.format(new Date())+" ---> "+"Targeted blast seqence was finished!");
                System.out.println("Memory--->(total: "+run.totalMemory()+") (free: "+run.freeMemory()+") (used: "+(run.totalMemory()-run.freeMemory())+") ");
            } catch (IOException ex) {
                LOG.error(ex.getMessage());
                return 0;
            }           
        }else if(this.map2tree){
            try {
                System.out.println(df.format(new Date())+" ---> ("+this.strBlastResult+") Start to build map: Acc2Tax. ");
                if(this.acc2tax==null || this.acc2tax.isEmpty())  this.setAcc2Tax(this.strA2T); //Only fit for single thread.
                System.out.println(df.format(new Date())+" ---> ("+this.strBlastResult+") Acc2Tax map was finished!");
                
                System.out.println(df.format(new Date())+" ---> ("+this.strBlastResult+") Start to map reads to the taxonomy tree. ");
                this.mapReads(this.strBlastResult,Configuration.NT.get("acc"));
                System.out.println(df.format(new Date())+" ---> ("+this.strBlastResult+") Marker reads were mapped to the taxonomy tree!");
                
                System.out.println(df.format(new Date())+" ---> ("+this.strBlastResult+") Start to output the taxonomy tree. ");
                BufferedWriter bw=Factory.getWriter(this.strOut+this.strSurfix[5]);
                if(this.style==666){
                    this.outputMappingTree(this.taxTree.get(this.taxid), "", bw);
                }else{
                    this.outputMappingTree(this.taxTree.get(this.taxid), this.style, "", bw);
                }
                            
                bw.close();
                System.out.println(df.format(new Date())+" ---> ("+this.strBlastResult+") The tree was outputed!");
                
            } catch (IOException ex) {
                LOG.error(ex.getMessage());
            }
        }else{
            LOG.info("Taxonomy: no function is set!");
            return -1;
        }
           
        return 1;
    }
    
    public void mapReads(String strBlast,String strAcc ) {
        try {

            if (this.taxTree == null || this.taxTree.isEmpty()) {
                this.buildTree(this.strNode, this.strName);
            }
            if (this.acc2tax == null || this.acc2tax.isEmpty()) {
                this.setAcc2Tax(this.strA2T);
            }
            if (this.hmpAcc == null) {
                try {
                    ObjectInputStream ois = new ObjectInputStream(new GZIPInputStream(new FileInputStream(strAcc)));
                    this.hmpAcc = (ConcurrentHashMap<String, ArrayList<String>>) ois.readObject();
                    ois.close();
                }catch (Exception ex) {
                    this.hmpAcc=new ConcurrentHashMap<>();
                    LOG.error(ex.getMessage());
                    LOG.warn("The file "+strAcc+" doesn't exist.");
                }
            }

            FastReader fr = new FastReader(strBlast, this.thread,(byte)'\t');
            int realSeg=fr.adjustPos();
            fr.creatFRS();
            MapReads[] es = new MapReads[realSeg];

            String message;
            ExecutorService exe = Executors.newCachedThreadPool();
            ArrayList<Future<String>> lstResult = new ArrayList<Future<String>>();
            for (int i = 0; i < realSeg; i++) {
                es[i] = new MapReads(i,fr.frs[i], this.taxTree, this.acc2tax,this.hmpAcc);
                Future<String> future = exe.submit(es[i]);
                lstResult.add(future);
            }

            for (Future<String> ft : lstResult) {
                try {
                    while (!ft.isDone());
                    message = ft.get();
                    System.out.println(message);
                } catch (InterruptedException ex) {
                    LOG.info(ex.getMessage());
                } catch (ExecutionException ex) {
                    LOG.info(ex.getMessage());
                } finally {
                    exe.shutdown();
                }
            }
            
            int NoMapping=0;
            int NoTax=0;
            for(int i=0;i<es.length;i++){
                NoMapping+=es[i].intNoMapping;
                NoTax+=es[i].intNotInTree;
            }
            
            System.out.println(NoMapping+" accession ids do not have taxion ids.");
            System.out.println(NoTax+" taxion ids do not in the taxonomy tree. ");

        } catch (IOException ex) {
            LOG.error(ex.getMessage());
        }

    }
 
    public void map2tree() {
        try {
            System.out.println(df.format(new Date()) + " ---> (" + this.strBlastResult + ") Start to map reads to the taxonomy tree. ");
            this.mapReads(this.strBlastResult,Configuration.NT.get("acc"));
            System.out.println(df.format(new Date()) + " ---> (" + this.strBlastResult + ") Marker reads were mapped to the taxonomy tree!");

            System.out.println(df.format(new Date()) + " ---> (" + this.strBlastResult + ") Start to output the taxonomy tree. ");
            BufferedWriter bw = Factory.getWriter(this.strOut + this.strSurfix[5]);
            this.outputMappingTree(this.taxTree.get(this.taxid),  "", bw);
            bw.close();
            System.out.println(df.format(new Date()) + " ---> (" + this.strBlastResult + ") The tree was outputed!");

        } catch (IOException ex) {
           LOG.error(ex.getMessage());
        }
    }
    
    public void freshTree() {
        for(Tax tax:this.taxTree.values()){
            tax.reads=new ConcurrentHashMap<>();
        }
    }

    public void showModule(){
        StringBuilder sb = new StringBuilder();
        sb.append("\n+++++++++++++++++++++++++++++++\n");
        sb.append("+        ToolKit (V1.0)       +");
        sb.append("\n+++++++++++++++++++++++++++++++\n");
        System.out.println(sb.toString());
    } 

    
    public static void main(String[] args) throws IOException{
//       try {
           Taxonomy taxon=new Taxonomy();
           taxon.strA2T="E:\\01Work\\miRNA\\project\\COMPASS\\bundle_v1\\db\\nt\\nucl_gb.accession2taxid.gz";
           taxon.setAcc2Tax("E:\\01Work\\miRNA\\project\\COMPASS\\bundle_v1\\prebuilt_db\\acc2tax.obj");
           taxon.buildTree("E:\\01Work\\miRNA\\project\\COMPASS\\bundle_v1\\db\\nt\\nodes.dmp", "E:\\01Work\\miRNA\\project\\COMPASS\\bundle_v1\\db\\nt\\names.dmp");
           
//           BufferedWriter bw=Factory.getWriter("E:\\01Work\\microbe\\data\\fungi_Tree_1.txt");
//           taxon.outputTree(taxon.taxTree.get(taxon.ComUse.get("fungi")), 2, "", bw);
//           String strChildren=taxon.outputChildren(taxon.taxTree.get(taxon.ComUse.get("fungi")));
//           BufferedWriter bw=Factory.getWriter("E:\\01Work\\microbe\\data\\fungi_children.txt");
//           bw.write(strChildren);
//           bw.close();
           
//           taxon.setTax2Acc("E:\\01Work\\microbe\\data\\nucl_gb.accession2taxid_part.gz");
//           String strMap=taxon.getAcc();
//           bw=Factory.getWriter("E:\\01Work\\microbe\\data\\fungi_tax2acc.txt");
//           bw.write(strMap);
//           bw.close();
//            taxon.accSet.add("K03166");
//            taxon.accSet.add("K03169");             
//           
//           taxon.accSet.add("M24300");
//           taxon.accSet.add("X00414");
//           taxon.accSet.add("1P85_9");          
//           
//           taxon.accSet.add("NR_000383");
//           taxon.extractSeq("E:\\01Work\\microbe\\data\\nt_test1.fasta", "E:\\01Work\\microbe\\data\\nt_test_result.fasta");

//           taxon.setAcc2Tax("E:\\01Work\\microbe\\data\\nucl_gb.accession2taxid_part.gz");
           taxon.mapReads("C:\\Users\\rejia\\Desktop\\S-001570900_GATCAG_L001_R1_17to50_FitRead_STAR_Aligned_UnMapped_blast_viruses_tree.tmp","E:\\01Work\\miRNA\\project\\COMPASS\\bundle_v1\\prebuilt_db\\nt_viruses.obj");
           BufferedWriter bw=Factory.getWriter("C:\\Users\\rejia\\Desktop\\S-001570900_viruses.txt");
           taxon.outputMappingTree(taxon.taxTree.get("1"),1234, "", bw);
           bw.close();
           
           System.out.println("HeHeDa!");
//       } catch (IOException ex) {
//           LogManager.getLogger(Taxonomy.class.getName()).log(Level.SEVERE, null, ex);
//       }
        
    }



}
