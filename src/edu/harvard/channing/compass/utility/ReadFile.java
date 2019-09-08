/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.utility;

import edu.harvard.channing.compass.core.Factory;
import edu.harvard.channing.compass.entity.BioNode;
import edu.harvard.channing.compass.core.Configuration;
import edu.harvard.channing.compass.entity.DBLeaf;
import edu.harvard.channing.compass.entity.DBTree;
import edu.harvard.channing.compass.core.mic.Marker;
import htsjdk.samtools.liftover.LiftOver;
import htsjdk.samtools.util.Interval;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.zip.GZIPInputStream;
import org.apache.logging.log4j.Level;

/**
 * This class is used to read in different kinds of data files.
 *
 * @author Jiang Li
 * @version 1.0
 * @since 2017-09-25
 */
public class ReadFile {

    private static final Logger LOG = LogManager.getLogger(ReadFile.class.getName());

    public static DBTree readColumnFile(String key, String input, boolean hasHeader, int chr, int start, int end, int name, int strand, int other) {
        String db = Configuration.hmpEndoDatabase.get(key).strDatabaseName;
        BufferedReader br;
        DBTree dis = new DBTree();
        File fleIn = new File(input);

        if (!fleIn.exists()) {
            LOG.info("Read File Error: Fail to read file: " + input);
        } else {
            try {
//                if (fleIn.getName().endsWith(".gz")) {
//                    br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(fleIn))));
//                } else {
//                    br = new BufferedReader(new InputStreamReader(new FileInputStream(fleIn)));
//                }
                br=Factory.getReader(input);
                String strLine;
                dis.initTree(db);

                if (hasHeader) {
                    strLine = br.readLine();
                }
                while ((strLine = br.readLine()) != null) {
                    String[] strCol = strLine.split("\t");
                    if(strCol.length<=1){
                        LOG.warn("The strange line: "+strLine);
                        continue;
                    }
                    DBLeaf dif = Factory.createLeaf(key);
                    dif.chr = Configuration.getContig(key, strCol[chr].toLowerCase());
                    dif.start = Integer.parseInt(strCol[start]);
                    dif.end = Integer.parseInt(strCol[end]);
                    dif.name = strCol[name];
                    dif.strand = strCol[strand];
                    dif.other = strCol[other];

                    boolean flag = dis.addLeaf(dif.chr, dif);
                    if (!flag) {
                        System.out.println(dif.toRecord());
                    }
                }
            } catch (FileNotFoundException ex) {
                LOG.error(ex.getMessage());
            } catch (IOException ex) {
                LOG.error(ex.getMessage());
            }
        }

        for (ArrayList<Integer> alt : dis.hmpStart.values()) {
            Collections.sort(alt);
        }

        return dis;
    }

    public static DBTree readColumnFile(String key, String input, boolean hasHeader, int chr, int start, int end, int name, int strand, int other,int adj) {
        String db = Configuration.hmpEndoDatabase.get(key).strDatabaseName;
        BufferedReader br;
        DBTree dis = new DBTree();
        File fleIn = new File(input);

        if (!fleIn.exists()) {
            LOG.info("Read File Error: Fail to read file: " + input);
        } else {
            try {
                br=Factory.getReader(input);
                String strLine;
                dis.initTree(db);

                if (hasHeader) {
                    strLine = br.readLine();
                }
                while ((strLine = br.readLine()) != null) {
                    String[] strCol = strLine.split("\t");
                    DBLeaf dif = Factory.createLeaf(key);
                    dif.chr = Configuration.getContig(key, strCol[chr].toLowerCase());
                    dif.start = Integer.parseInt(strCol[start])+adj;
                    dif.end = Integer.parseInt(strCol[end]);
                    dif.name = strCol[name];
                    dif.strand = strCol[strand];
                    dif.other = strCol[other];

                    boolean flag = dis.addLeaf(dif.chr, dif);
                    if (!flag) {
                        System.out.println(dif.toRecord());
                    }
                }
            } catch (FileNotFoundException ex) {
                LOG.error(ex.getMessage());
            } catch (IOException ex) {
                LOG.error(ex.getMessage());
            }
        }

        for (ArrayList<Integer> alt : dis.hmpStart.values()) {
            Collections.sort(alt);
        }

        return dis;
    }
    
    
    public static DBTree readColumnFile(String key, String liftover, String input, boolean hasHeader, int chr, int start, int end, int name, int strand, int other) {
        String db = Configuration.hmpEndoDatabase.get(key).strDatabaseName;
        BufferedReader br;
        DBTree dis = new DBTree();

        LiftOver lorTrans = null;
        File fleIn = null;

        File fleLO = new File(liftover);
        if (!fleLO.exists()) {
            LOG.info("Lift Over Error: Fail to read the liftover file: " + liftover);
            return null;
        }
        lorTrans = new LiftOver(fleLO);

        fleIn = new File(input);
        if (!fleIn.exists()) {
            LOG.info("Read File Error: Fail to read file: " + input);
        } else {
            try {
                if (fleIn.getName().endsWith(".gz")) {
                    br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(fleIn))));
                } else {
                    br = new BufferedReader(new InputStreamReader(new FileInputStream(fleIn)));
                }

                String strLine;
                dis.initTree(db);
                if (hasHeader) {
                    strLine = br.readLine();
                }
                while ((strLine = br.readLine()) != null) {
                    String[] strCol = strLine.split("\t|,");
                    if (strCol.length <= 1) {
                        LOG.warn("The strange line: " + strLine);
                        continue;
                    }
                                        
                    Interval itvRegion = new Interval(Configuration.UCSC_CONTIG.get(strCol[chr].toLowerCase()), Integer.parseInt(strCol[start]), Integer.parseInt(strCol[end]), "-".equals(strCol[strand]), strCol[name]);
//                    System.out.println(itvRegion.toString());
                    Interval itvNew = lorTrans.liftOver(itvRegion);
                    if (itvNew == null) {
                        continue;
                    }

                    DBLeaf dif = Factory.createLeaf(key);
                    dif.chr = Configuration.getContig(key, strCol[chr].toLowerCase());
                    dif.start = itvNew.getStart();
                    dif.end = itvNew.getEnd();
                    dif.strand = itvNew.isPositiveStrand() ? "+" : "-";
                    dif.name = itvNew.getName();
                    dif.other = strCol[other];

                    boolean flag = dis.addLeaf(dif.chr, dif);
                    if (!flag) {
//                        System.out.println("Fail to add: "+dif.toRecord());
                    }
                }
                for (ArrayList<Integer> alt : dis.hmpStart.values()) {
                    Collections.sort(alt);
                }
            } catch (FileNotFoundException ex) {
                LOG.error(ex.getMessage());
            } catch (Exception ex) {
                LOG.error(ex.getMessage());
//                System.out.println(input);
            }
        }



        return dis;
    }

    public static DBTree readColumnFile(String key, String liftover, String input, boolean hasHeader, int chr, int start, int end, int name, int strand, int other,int adj) {
        String db = Configuration.hmpEndoDatabase.get(key).strDatabaseName;
        BufferedReader br;
        DBTree dis = new DBTree();

        LiftOver lorTrans = null;
        File fleIn = null;


        fleIn = new File(input);
        if (!fleIn.exists()) {
            LOG.info("Read File Error: Fail to read file: " + input);           
        } else {
            try {
                File fleLO = new File(liftover);
                if (!fleLO.exists()) {
                    LOG.info("Lift Over Error: Fail to read the liftover file: " + liftover);
                    return null;
                }
                lorTrans = new LiftOver(fleLO);

                if (fleIn.getName().endsWith(".gz")) {
                    br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(fleIn))));
                } else {
                    br = new BufferedReader(new InputStreamReader(new FileInputStream(fleIn)));
                }

                String strLine;
                dis.initTree(db);
                if (hasHeader) {
                    strLine = br.readLine();
                }
                while ((strLine = br.readLine()) != null) {
                    String[] strCol = strLine.split("\t|,");

                    Interval itvRegion = new Interval(Configuration.UCSC_CONTIG.get(strCol[chr].toLowerCase()), Integer.parseInt(strCol[start]), Integer.parseInt(strCol[end]), "-".equals(strCol[strand]), strCol[name]);
//                    System.out.println(itvRegion.toString());
                    Interval itvNew = lorTrans.liftOver(itvRegion);
                    if (itvNew == null) {
                        continue;
                    }

                    DBLeaf dif = Factory.createLeaf(key);
                    dif.chr = Configuration.getContig(key, strCol[chr].toLowerCase());
                    dif.start = itvNew.getStart()+adj;
                    dif.end = itvNew.getEnd();
                    dif.strand = itvNew.isPositiveStrand() ? "+" : "-";
                    dif.name = itvNew.getName();
                    dif.other = strCol[other];

                    boolean flag = dis.addLeaf(dif.chr, dif);
                    if (!flag) {
//                        System.out.println("Fail to add: "+dif.toRecord());
                    }
                }
                for (ArrayList<Integer> alt : dis.hmpStart.values()) {
                    Collections.sort(alt);
                }
            } catch (FileNotFoundException ex) {
                LOG.error(ex.getMessage());
            } catch (Exception ex) {
                LOG.error(ex.getMessage());
//                System.out.println(input);
            }
        }

        return dis;
    }
    
    
    /**
     * This function is used to set the UCSC liftover file.
     * @param db From this version.
     * @param ref To this version.
     * @return The file name of the liftover file used.
     */
    public static String getLiftOverFile(String db, String ref) {
        if (db == ref) {
            return null;
        }
        if (ref.contains("hg")) {
            switch (ref) {
                case "hg18":
                    switch (db) {
                        case "hg19":
                            return Configuration.DB_CHAIN + "/hg19ToHg18.over.chain.gz";
                        case "hg38":
                            return Configuration.DB_CHAIN + "/hg38ToHg18.over.chain.gz";
                    }
                case "hg19":
                    switch (db) {
                        case "hg18":
                            return Configuration.DB_CHAIN + "/hg18ToHg19.over.chain.gz";
                        case "hg38":
                            return Configuration.DB_CHAIN + "/hg38ToHg19.over.chain.gz";//usually
                    }
                case "hg38":
                    switch (db) {
                        case "hg18":
                            return Configuration.DB_CHAIN + "/hg18ToHg38.over.chain.gz";
                        case "hg19":
                            return Configuration.DB_CHAIN + "/hg19ToHg38.over.chain.gz";//usually                
                    }
            }
        } else {
            switch (ref) {
                case "mm9":
                    switch (db) {
                        case "mm8":
                            return Configuration.DB_CHAIN + "/mm8ToMm9.over.chain.gz";
                        case "mm10":
                            return Configuration.DB_CHAIN + "/mm10ToMm9.over.chain.gz";
                    }
                case "mm10":
                    switch (db) {
                        case "mm8":
                            return Configuration.DB_CHAIN + "/mm8ToMm10.over.chain.gz";
                        case "mm9":
                            return Configuration.DB_CHAIN + "/mm9ToMm10.over.chain.gz";                
                    }
            }

        }
        return null;
    }

    /**
     * This function is used to read the gff3 annotation files.
     *
     * @param key
     * @param input The path of gff3 annotation file.
     * @return The DBTree object of this gff3 annotation file.
     */
    public static DBTree readGFF3(String key, String input) {
        String db = Configuration.hmpEndoDatabase.get(key).strDatabaseName;
        BufferedReader br;
        DBTree dbt = new DBTree();
        File fleIn = new File(input);

        if (!fleIn.exists()) {
            LOG.info("Read File Error: Fail to read file: " + input + ".");
        } else {
            try {
                br = Factory.getReader(input);
                String strLine;
                dbt.initTree(db);

                while ((strLine = br.readLine()) != null) {
                    if (strLine.startsWith("#")) {
                        continue;
                    }
//                    String[] strCol=strLine.split("\t");
                    DBLeaf dif = Factory.createLeaf(key);
                    dif.initLeaf(strLine, key);
                    boolean flag = dbt.addLeaf(dif.chr, dif);
                    if (!flag) {
                        System.out.println("Duplicated: " + dif.toRecord());
                    }
                }
            } catch (FileNotFoundException ex) {
                LOG.error(ex.getMessage());
            } catch (IOException ex) {
                LOG.error(ex.getMessage());
            }
        }

        for (ArrayList<Integer> alt : dbt.hmpStart.values()) {
            Collections.sort(alt);
        }

        return dbt;
    }

    public static DBTree readGFF3(String key, String liftover, String input) {
        String db = Configuration.hmpEndoDatabase.get(key).strDatabaseName;
        BufferedReader br;
        DBTree dis = new DBTree();

        LiftOver lorTrans = null;
        File fleLO = new File(liftover);
        if (!fleLO.exists()) {
            LOG.info("Lift Over Error: Fail to read the liftover file: " + liftover);
            return null;
        }
        lorTrans = new LiftOver(fleLO);

        try {
            String strLine;
            dis.initTree(db);
            br = Factory.getReader(input);
            while ((strLine = br.readLine()) != null) {
                if (strLine.startsWith("#")) {
                    continue;
                }
//                DBLeaf_GFF3 dif = new DBLeaf_GFF3(strLine);
                DBLeaf dif = Factory.createLeaf(key);
                dif.initLeaf(strLine, key);
                Interval itvRegion = new Interval(Configuration.UCSC_CONTIG.get(dif.chr.toLowerCase()), dif.start, dif.end, "-".equals(dif.strand), dif.name);
                Interval itvNew = lorTrans.liftOver(itvRegion);
                if (itvNew == null) {
                    continue;
                }
                dif.chr = itvNew.getContig();
                dif.start = itvNew.getStart();
                dif.end = itvNew.getEnd();
                dif.strand = itvNew.isPositiveStrand() ? "+" : "-";
                boolean flag = dis.addLeaf(dif.chr, dif);
                if (!flag) {
                    System.out.println("Duplicated: " + dif.toRecord());
                }
            }
        } catch (FileNotFoundException ex) {
            LOG.error(ex.getMessage());
        } catch (IOException ex) {
            LOG.error(ex.getMessage());
        }

        for (ArrayList<Integer> alt : dis.hmpStart.values()) {
            Collections.sort(alt);
        }

        return dis;
    }

    public static DBTree readGFF3(String input) {
        String db = "Temp";
        BufferedReader br;
        DBTree dbt = new DBTree();
        File fleIn = new File(input);

        if (!fleIn.exists()) {
            LOG.info("Read File Error: Fail to read file: " + input + ".");
        } else {
            try {
                br = Factory.getReader(input);
                String strLine;
                dbt.initTree(db);

                while ((strLine = br.readLine()) != null) {
                    if (strLine.startsWith("#")) {
                        continue;
                    }
//                    String[] strCol=strLine.split("\t");
                    DBLeaf dif = Factory.createLeaf("-1");
                    dif.initLeaf(strLine, "-1");
                    boolean flag = dbt.addLeaf(dif.chr, dif);
                    if (!flag) {
                        System.out.println("Duplicated: " + dif.toRecord());
                    }
                }
            } catch (FileNotFoundException ex) {
                LOG.error(ex.getMessage());
            } catch (IOException ex) {
                LOG.error(ex.getMessage());
            }
        }

        for (ArrayList<Integer> alt : dbt.hmpStart.values()) {
            Collections.sort(alt);
        }

        return dbt;
    }    
    
    
    public static HashMap<String, Marker> readMarker() {
        HashMap<String, Marker> bio = new HashMap<String, Marker>();
        try {
            String db = Configuration.METAPHLAN.get("marker");
            String db_exclude = Configuration.METAPHLAN.get("exclude");
            BufferedReader br;
            BufferedReader br_exclude;
            
            String strLine;            
            br_exclude = Factory.getReader(db_exclude);
            ArrayList<String> altExclude = new ArrayList();
            while ((strLine = br_exclude.readLine()) != null) {
                altExclude.add(strLine.trim());
            }
            
            br = Factory.getReader(db);
            while ((strLine = br.readLine()) != null) {
                Marker m = new Marker();
                m.initMarker(strLine);
                if (altExclude.contains(m.key)) {
                    continue;
                }
                bio.put(m.key,m);
            }      
        } catch (IOException ex) {
            LOG.error(ex.getMessage());
        }  
        return bio;
    }

    public static BioNode readMarker(HashMap<String, Marker> bio) {
        File db_pre = new File(Configuration.METAPHLAN.get("obj"));
        if (db_pre.exists()) {
            try {
                ObjectInputStream ois = new ObjectInputStream(new GZIPInputStream(new FileInputStream(db_pre)));
                bio.putAll((HashMap) ois.readObject());
                BioNode bnd = (BioNode) ois.readObject();
                ois.close();
                return bnd;
            } catch (FileNotFoundException ex) {
                LOG.error(ex.getMessage());
            } catch (IOException ex) {
                LOG.error(ex.getMessage());
            } catch (ClassNotFoundException ex) {
                LOG.error(ex.getMessage());
            }
        }

        String db = Configuration.METAPHLAN.get("db");
        String db_exclude = Configuration.METAPHLAN.get("exclude");
        File fleIn = new File(db);
        BufferedReader br;
        BufferedReader br_exclude;
        BioNode domain = new BioNode("domain", 'd', null);

        if (!fleIn.exists()) {
            LOG.info("Read File Error: Fail to read file: " + db + ".");
        } else {
            try {
                String strLine;

                br_exclude = Factory.getReader(db_exclude);
                ArrayList<String> altExclude = new ArrayList();
                while ((strLine = br_exclude.readLine()) != null) {
                    altExclude.add(strLine.trim());
                }

                br = Factory.getReader(db);
                while ((strLine = br.readLine()) != null) {
                    Marker m = new Marker();
                    m.initMarker(strLine);
                    if (altExclude.contains(m.key)) {
                        continue;
                    }
//                    if(!bio.containsKey(m.gi))  bio.put(m.gi, new ArrayList<Marker>());
//                    bio.get(m.gi).add(m);       
//                    if(!bio.containsKey(m.key)) bio.put(m.key, new ArrayList<Marker>());
//                      if(bio.containsKey(m.key))   System.out.println(m.key);
//                    bio.get(m.key).add(m);
                    bio.put(m.key, m);
                    boolean flag = domain.add2PheloTree(m.taxon, m);
                    if (!flag) {
                        System.out.println("This marker is failed to added: " + m.clade + " !");
                    }
//                    System.out.println(m.gi);
                }
            } catch (FileNotFoundException ex) {
                LOG.error(ex.getMessage());
            } catch (IOException ex) {
                LOG.error(ex.getMessage());
            }
        }
        return domain;
    }

    public static DBTree readObj(String strFile) {
        ObjectInputStream ois = null;
        DBTree dbt = null;
        try {
            File fleObj = new File(strFile);
            if (!fleObj.exists()) {
                return null;
            }
            ois = new ObjectInputStream(new GZIPInputStream(new FileInputStream(strFile)));
            dbt = (DBTree) ois.readObject();
            ois.close();
        }catch (Exception ex) {
            LOG.error(ex.getMessage());
            LOG.error("Fail to find the prebuilt database obj file!");
        }finally{
            return dbt;
        }        
    }

    public static BioNode buildTree_MetaPhlAn(HashMap<String,BioNode> clades) {
        BioNode domain = new BioNode("domain", 'd', null);
        String strTree = Configuration.METAPHLAN.get("taxonomy");
        BufferedReader br = Factory.getReader(strTree);
        String strLine;
        try {
            while ((strLine = br.readLine()) != null) {
                String[] strItem = strLine.split(",");
                boolean flag = domain.add2PheloTree(strItem[0], strItem[1],clades);
                if (!flag) {
                    System.out.println("This marker is failed to added: " + strItem[0] + " !");
                }
            }
        } catch (IOException ex) {
            LOG.error(ex.getMessage());
        }
        return domain;
    }    
    
    
    public static void readMetaPhlAn_Pickle(DBTree dbt) {
//        String strFile = "E:\\01Work\\miRNA\\tool\\metaphlan2\\mpa_v20_m200.pkl.out";
//        File fIn = new File(strFile);
//
//        BufferedReader bufR;
//        StringBuilder strBuilder = new StringBuilder();
//        try {
//            bufR = new BufferedReader(new FileReader(fIn));
//            String aLine;
//            while (null != (aLine = bufR.readLine())) {
//                strBuilder.append(aLine).append("\n");
//            }
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        
//        InputStream is=null;
//        try{
//            is=new FileInputStream(fIn);
//        }catch(FileNotFoundException e){
//            e.printStackTrace();
//        }
//        
//        PyString pyStr = new PyString(strBuilder.toString());
//        PyDictionary pyd = (PyDictionary) cPickle.load(pyStr);
//        ConcurrentMap<PyObject, PyObject> comp = pyd.getMap();
//
//        PyDictionary pydTax = (PyDictionary) comp.get("taxonomy");
//        ConcurrentMap<PyObject, PyObject> compTax = pydTax.getMap();
//        for (Map.Entry<PyObject, PyObject> entry : compTax.entrySet()) {
//            String strClades = entry.getKey().toString();
//            int intLen = entry.getValue().asInt();
//            System.out.println(strClades + "--->" + intLen);
//        }
    }

    public static void main(String[] argv) {
//        ObjectOutputStream oos=null;
//        ObjectInputStream ois=null;
        //        String liftover="E:\\01Work\\miRNA\\project\\COMPASS\\database\\chain\\hg19ToHg38.over.chain.gz";
//        String input="E:\\01Work\\miRNA\\project\\COMPASS\\database\\ann\\piR_hg19_v1.0.bed.gz";
//        String liftover2="E:\\01Work\\miRNA\\project\\COMPASS\\database\\chain\\hg18ToHg38.over.chain.gz";
//        String input2="E:\\01Work\\miRNA\\project\\COMPASS\\database\\seq\\piRNABank\\human_all.zip";
//        String input="E:\\01Work\\miRNA\\project\\COMPASS\\database\\ann\\miRBase_hg19.gff3";
//        String input="E:\\01Work\\miRNA\\project\\COMPASS\\database\\ann\\GENCODE\\gencode.v27.chr_patch_hapl_scaff.annotation.gff3.gz";
//        DBTree dis;
////        dbt=readColumnFile(liftover, input, false, 0, 1, 2, 3, 5);
//        dis=readDB_piRNABank(liftover2,input2);
//        dis=readGFF3(liftover,input,"miRNA");
//        System.out.println("Test Finished!");
//            Configuration con = new Configuration();
//            HashMap<String, Marker> bio = new HashMap<String, Marker>();
//            BioNode bnd = readMarker(bio);
//            oos = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream("E:\\01Work\\miRNA\\project\\COMPASS\\database\\ann\\MetaPhlAn\\metaphlan.obj")));
//            oos.writeObject(bio);
//            oos.writeObject(bnd);
//            ois=new ObjectInputStream(new GZIPInputStream(new FileInputStream("E:\\01Work\\miRNA\\project\\COMPASS\\database\\ann\\MetaPhlAn\\metaphlan.obj")));
//            HashMap<String, Marker> bio2=(HashMap)ois.readObject();
//            BioNode bnd2=(BioNode) ois.readObject();
//            oos.close();
//            ois.close();
//        System.out.println("HeHeHeHe"); 

        

    }


    public static HashMap<String, String> getHMP(String strInput, int key, int value) {
        HashMap<String,String> hmpK2V=new HashMap<String,String>();
        
        try {
            File fleIn=new File(strInput);
            if(!fleIn.exists()) return null;
            BufferedReader br=Factory.getReader(strInput);
            for(String strLine=br.readLine();strLine!=null;strLine=br.readLine()){
                String[] strItem=strLine.split("\t");
                hmpK2V.put(strItem[key].trim(), strItem[value].trim());
            }
        } catch (IOException ex) {
            LOG.error(ex.getMessage());
        }
        return hmpK2V;
    }

    public static ArrayList<String> readFileList(String f) {
        BufferedReader br = Factory.getReader(f);
        ArrayList<String> altFiles=new ArrayList();
        try {
            String line;
            while ((line = br.readLine()) != null) {
                altFiles.add(line.trim());
            }
        } catch (IOException ex) {
            LOG.error(ex.getMessage());
        }
        return altFiles;
    }


}
