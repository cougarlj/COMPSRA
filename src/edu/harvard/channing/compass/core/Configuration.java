/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.core;

import edu.harvard.channing.compass.entity.DBTree;
import edu.harvard.channing.compass.entity.DatabaseInfo;
import edu.harvard.channing.compass.utility.Download;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class is used to keep record of the fixed configuration.
 *
 * @author Jiang Li
 * @version 1.0
 * @since 2017-08-29
 */
public final class Configuration {

    private static final Logger LOG = LogManager.getLogger(Configuration.class.getName());

    public static String PREFIX;
    public static String BUNDLE;
    public static String BUNDLE_LOC;
    public static String CONFIG;
    public static String PLUG;
    public static String OUTDIR;
    public static String PREDB;
    public static String DB;
    public static String DB_CHAIN;
    public static String DB_NT;
    public static String DB_REF;
    public static String CONFIG_ENDO_DATABASE;
    public static String ADAPTER;
    public static String DOWNLOAD_HOMEPAGE;
    public static String DOWNLOAD_BUNDLE_VERSION;
    public static String STAR;
    public static String BLAST;
    public static final String[] QC_OUT = {"_LowQualityRead.fastq.gz", "_FitRead.fastq.gz"};
    public static final String[] ANN_OUT = {"_UnMapped.bam", "_UnAnnotated.bam"};
    public static final String[] MIC_OUT = {"_metaphlan.txt"};

    public static final String TAXONOMY = "ldkpcofgst";
    public static final int BUFF1 = 1024 * 1024; //1M
    public static final int BUFF2 = 1024 * 1024 * 1024; //1G
    
    public static String strIndexSurffix=".cps.idx";

    public static HashMap<String, String> UCSC_CONTIG = new HashMap();
    public static HashMap<String, String> ANN_CATEGORY = new HashMap();
    public static HashMap<String, String> PLUG_BUILT_IN = new HashMap();
    public static HashMap<String, String> BOWTIE_REF = new HashMap();
    public static HashMap<String, String> STAR_REF = new HashMap();
    public static HashMap<String, String> METAPHLAN = new HashMap();
    public static HashMap<String, String> NT = new HashMap();
    public static HashMap<String, String> DOWNLOAD = new HashMap();
    public static HashMap<String, String> OTHERS = new HashMap();
    public static HashMap<Character, Integer> BASEORDER=new HashMap();
    public static HashMap<Integer, Character> BASEORDERREV=new HashMap();
    public static HashMap<String, String> REF=new HashMap();         

    public static int N_CPU;
    public static String strCurrDir;

    public static String strFileRefseqAssembly;
    /**
     * hmpEndoDatabase contains the databases used in endogenous alignment.
     */
    public static HashMap<String, DatabaseInfo> hmpEndoDatabase;
    /**
     * hmpExoDatabase contains the database used in exogenous alignment.
     */
    public static HashMap<String, DatabaseInfo> hmpExoDatabase;
    /**
     * STAR_PARAM is used to save the parameters of STAR in different applications.
     */
    public static  String STAR_PARAM;

    /**
     * hmpAdapter contains the adapter sequence information that may be used in
     * circuRNA pipeline.
     */
//    public static HashMap<String,Adapter> hmpAdapter;
    public Configuration() {
        Configuration.strCurrDir = System.getProperty("user.dir");

        Configuration.PREFIX = "COMPASS";
        Configuration.BUNDLE="bundle_v1";
        Configuration.OUTDIR = strCurrDir + "/output";

        Configuration.BUNDLE_LOC = strCurrDir + "/"+Configuration.BUNDLE;
        Configuration.CONFIG = BUNDLE_LOC + "/configuration";
        Configuration.PLUG = BUNDLE_LOC + "/plug";
        Configuration.PREDB = BUNDLE_LOC + "/prebuilt_db";
        Configuration.DB = BUNDLE_LOC + "/db";
        Configuration.DB_CHAIN = DB + "/chain";
        Configuration.DB_NT = DB + "/nt";
        Configuration.DB_REF=DB+"/ref";
        Configuration.CONFIG_ENDO_DATABASE = CONFIG + "/database_record.obj";
        Configuration.ADAPTER = CONFIG + "/adapter.txt";
        Configuration.DOWNLOAD_HOMEPAGE = "https://regepi.bwh.harvard.edu/circurna/";
        Configuration.DOWNLOAD_BUNDLE_VERSION = Configuration.BUNDLE;
        Configuration.N_CPU = Runtime.getRuntime().availableProcessors();

        Configuration.STAR="STAR-2.5.3a.zip";
        Configuration.STAR_PARAM=Configuration.CONFIG+"/STAR.para";
        Configuration.BLAST="ncbi-blast-2.7.1+-x64-linux.tar.gz";
        
//        this.showLogo();
        this.showModule();
//        System.out.println("TEMP : " + System.getProperty("java.io.tmpdir")); 
//        System.out.println("PATH : " + System.getProperty("java.library.path")); 
//        System.out.println("CLASSPATH : " + System.getProperty("java.class.path")); 
//        System.out.println("SYSTEM DIR : " + System.getProperty("user.home")); // ex. c:\windows on Win9x system 
//        System.out.println("CURRENT DIR: " + System.getProperty("user.dir"));  

        System.out.println("\nWorking Directory: " + strCurrDir);
        System.out.println("Bundle Directory: " + BUNDLE_LOC);
        System.out.println("Configuration Directory: " + CONFIG);
        System.out.println("Plug Directory: " + PLUG);
//        System.out.println("Output Directory: " + OUTDIR);
        System.out.println("N_CPU: " + N_CPU);
        System.out.println();

        //Set the Chromosome names.
        this.setConfig();
//        this.buildObj(Configuration.CONFIG+"/database_record.txt");
        //Set the adapter information used in the COMPASS pipeline. 
//        this.setAdapter();
        //Set the endogenous database used in the COMPASS pipeline.
        this.setDB();

        //Set the exogenous database used in the COMPASS pipeline.
        LOG.info("The Configuration was completed!\n");
    }

    public void setConfig() {
        //Set ANN_CATEGORY
        //01:miRNA 02:piRNA 03:tRNA 04:snoRNA 05:snRNA 06:circRNA 11:pre-miRNA. 
        ANN_CATEGORY.put("0", "_gencode");
        ANN_CATEGORY.put("1", "_miRNA");
        ANN_CATEGORY.put("2", "_piRNA");
        ANN_CATEGORY.put("3", "_tRNA");
        ANN_CATEGORY.put("4", "_snoRNA");
        ANN_CATEGORY.put("5", "_snRNA");
        ANN_CATEGORY.put("6", "_circRNA");
        ANN_CATEGORY.put("11", "_pre-miRNA");

        //Set PLUG_BUILT_IN
        PLUG_BUILT_IN.put("bowtie", PLUG + "/bowtie/bowtie-1.2.1.1/bowtie");
        PLUG_BUILT_IN.put("bowtie2", PLUG + "/bowtie2/bowtie2-2.3.3.1-linux-x86_64/bowtie2");
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            PLUG_BUILT_IN.put("star", PLUG + "/star/STAR-2.5.3a/bin/MacOSX_x86_64/STAR");
        } else {
            PLUG_BUILT_IN.put("star", PLUG + "/star/STAR-2.5.3a/bin/Linux_x86_64/STAR");
        }
        PLUG_BUILT_IN.put("blastn", PLUG + "/blast/ncbi-blast-2.7.1+/bin/blastn");
        PLUG_BUILT_IN.put("makeblastdb", PLUG + "/blast/ncbi-blast-2.7.1+/bin/makeblastdb");

        //Set BOWTIE_REF
        BOWTIE_REF.put("bowtie_hg19", DB + "/bowtie/hg19/hg19");
        BOWTIE_REF.put("bowtie_hg38", DB + "/bowtie/hg38/GCA_000001405.15_GRCh38_no_alt_analysis_set");
        BOWTIE_REF.put("bowtie2_hg19", DB + "/bowtie2/hg19/hg19");
        BOWTIE_REF.put("bowtie2_hg38", DB + "/bowtie2/hg38/GCA_000001405.15_GRCh38_no_alt_analysis_set.fna.bowtie_index");

        //Set STAR_REF
        STAR_REF.put("star_hg19", DB + "/star/hg19");
        STAR_REF.put("star_hg38", DB + "/star/hg38");
        STAR_REF.put("star_mm9", DB + "/star/mm9");
        STAR_REF.put("star_mm10", DB + "/star/mm10");
        STAR_REF.put("star_rno5", DB+"/star/rno5");
        STAR_REF.put("star_rno6", DB+"/star/rno6");

        //Set METAPHLAN
        METAPHLAN.put("marker", DB + "/MetaPhlAn/markers_info.txt.bz2");
        METAPHLAN.put("exclude", DB + "/MetaPhlAn/markers_exclude.txt");
        METAPHLAN.put("obj", PREDB + "/metaphlan.obj");
        METAPHLAN.put("db", DB + "/MetaPhlAn/db_v20/mpa_v20_m200");
        METAPHLAN.put("taxonomy", DB + "/MetaPhlAn/metaphlan2_taxonomy.txt");

        //Set NCBI NT database. 
        NT.put("nt", DB_NT + "/nt");
        NT.put("archaea", DB_NT + "/nt_archaea.fasta");
        NT.put("bacteria", DB_NT + "/nt_bacteria.fasta");
        NT.put("fungi", DB_NT + "/nt_fungi.fasta");
        NT.put("viruses", DB_NT + "/nt_viruses.fasta");
        NT.put("names", DB_NT + "/names.dmp");
        NT.put("nodes", DB_NT + "/nodes.dmp");
        NT.put("taxonomy", PREDB + "/taxonomy.obj");
        NT.put("A2T", PREDB + "/acc2tax.obj");
        NT.put("acc", PREDB + "/nt_genome.obj");

        //Set REF
        REF.put("hg38", PREDB+"/hg38.cps");
        REF.put("hg38.cps", DB_REF+"/hg38.fa.gz");

        //Set UCSC_CONTIG_hg38       
        UCSC_CONTIG.put("chr1", "chr1");
        UCSC_CONTIG.put("chr2", "chr2");
        UCSC_CONTIG.put("chr3", "chr3");
        UCSC_CONTIG.put("chr4", "chr4");
        UCSC_CONTIG.put("chr5", "chr5");
        UCSC_CONTIG.put("chr6", "chr6");
        UCSC_CONTIG.put("chr7", "chr7");
        UCSC_CONTIG.put("chr8", "chr8");
        UCSC_CONTIG.put("chr9", "chr9");
        UCSC_CONTIG.put("chr10", "chr10");
        UCSC_CONTIG.put("chr11", "chr11");
        UCSC_CONTIG.put("chr12", "chr12");
        UCSC_CONTIG.put("chr13", "chr13");
        UCSC_CONTIG.put("chr14", "chr14");
        UCSC_CONTIG.put("chr15", "chr15");
        UCSC_CONTIG.put("chr16", "chr16");
        UCSC_CONTIG.put("chr17", "chr17");
        UCSC_CONTIG.put("chr18", "chr18");
        UCSC_CONTIG.put("chr19", "chr19");
        UCSC_CONTIG.put("chr20", "chr20");
        UCSC_CONTIG.put("chr21", "chr21");
        UCSC_CONTIG.put("chr22", "chr22");
        UCSC_CONTIG.put("chr23", "chrX");
        UCSC_CONTIG.put("chr24", "chrY");
        UCSC_CONTIG.put("chrx", "chrX");
        UCSC_CONTIG.put("chry", "chrY");
        UCSC_CONTIG.put("chrm", "chrM");
        UCSC_CONTIG.put("chrX", "chrX");
        UCSC_CONTIG.put("chrY", "chrY");
        UCSC_CONTIG.put("chrM", "chrM");

        UCSC_CONTIG.put("1", "chr1");
        UCSC_CONTIG.put("2", "chr2");
        UCSC_CONTIG.put("3", "chr3");
        UCSC_CONTIG.put("4", "chr4");
        UCSC_CONTIG.put("5", "chr5");
        UCSC_CONTIG.put("6", "chr6");
        UCSC_CONTIG.put("7", "chr7");
        UCSC_CONTIG.put("8", "chr8");
        UCSC_CONTIG.put("9", "chr9");
        UCSC_CONTIG.put("10", "chr10");
        UCSC_CONTIG.put("11", "chr11");
        UCSC_CONTIG.put("12", "chr12");
        UCSC_CONTIG.put("13", "chr13");
        UCSC_CONTIG.put("14", "chr14");
        UCSC_CONTIG.put("15", "chr15");
        UCSC_CONTIG.put("16", "chr16");
        UCSC_CONTIG.put("17", "chr17");
        UCSC_CONTIG.put("18", "chr18");
        UCSC_CONTIG.put("19", "chr19");
        UCSC_CONTIG.put("20", "chr20");
        UCSC_CONTIG.put("21", "chr21");
        UCSC_CONTIG.put("22", "chr22");
        UCSC_CONTIG.put("23", "chrX");
        UCSC_CONTIG.put("24", "chrY");
        UCSC_CONTIG.put("X", "chrX");
        UCSC_CONTIG.put("Y", "chrY");
        UCSC_CONTIG.put("M", "chrM");    
        
        //Set Base Order.
        BASEORDER.put('A', 0);
        BASEORDER.put('C', 1);
        BASEORDER.put('G', 2);
        BASEORDER.put('T', 3);
        BASEORDER.put('N', 4);
        BASEORDER.put('.', -1);
        
        BASEORDERREV.put(0, 'A');
        BASEORDERREV.put(1,'C');
        BASEORDERREV.put(2,'G');
        BASEORDERREV.put(3,'T');
        BASEORDERREV.put(4,'N');
        BASEORDERREV.put(-1,'.');
        
        //Set Other information.
        OTHERS.put("piRBaseID_hg.txt", DB + "/piRBase/piRBaseID.txt");
    }

    public void showLogo() {
        ArrayList<String> altLogo = new ArrayList();
        String[] strLogo = new String[]{"10,11,12", "7,8,9,10,11,14,15,16,17,21,22,23,71,72,73,74,109,110,111,112,113,136,137,138", "5,6,7,8,16,17,21,22,23,110,111,112,136,137,138",
            "4,5,6,7,17,21,22,23,38,51,94,110,111,112,128,136,137,138,141,142", "3,4,5,6,21,22,23,24,26,27,28,29,34,35,38,39,40,41,45,46,47,48,49,50,51,52,53,54,58,59,60,61,62,63,64,65,66,67,71,72,73,74,78,79,80,81,82,83,84,85,86,91,92,93,96,97,98,99,110,111,112,124,125,126,129,130,131,136,137,138,139,141,142,143,144",
            "3,4,5,6,21,22,23,27,28,29,33,34,35,39,40,41,46,47,48,52,53,54,59,60,61,65,66,67,72,73,74,78,79,80,81,84,85,86,87,90,91,92,97,98,99,110,111,112,124,125,126,129,130,131,132,136,137,138,143,144,145", "3,4,5,6,7,21,22,23,27,28,29,37,39,40,41,46,47,48,52,53,54,59,60,61,65,66,67,72,73,74,78,79,80,81,84,85,86,87,90,91,92,93,96,97,98,110,111,112,127,129,130,131,136,137,138,143,144,145",
            "4,5,6,7,21,22,23,27,28,29,34,35,36,39,40,41,46,47,48,52,53,54,59,60,61,65,66,67,72,73,74,78,79,80,81,84,85,86,87,92,93,96,97,110,111,112,120,121,125,126,129,130,131,136,137,138,143,144,145", "5,6,7,8,16,17,21,22,23,27,28,29,33,34,35,36,39,40,41,46,47,48,52,53,54,59,60,61,65,66,67,72,73,74,78,79,80,81,84,85,86,87,91,92,109,110,111,112,119,120,121,124,125,126,129,130,131,132,136,137,138,142,143,144",
            "8,9,10,11,12,13,14,15,21,22,23,24,27,28,29,30,34,35,36,37,39,40,41,42,45,46,47,48,49,51,52,53,54,55,58,59,60,61,62,64,65,66,67,68,71,72,73,74,75,78,79,80,81,84,85,86,87,91,92,93,94,95,96,97,98,99,109,110,111,112,113,114,115,116,117,118,119,120,124,125,126,127,130,131,132,136,137,138,139,140,141,142,143", "91,92,93,94,95,96,97,98,99,100", "90,91,99", "92,93,94,95,96,97,98"
        };

        System.out.println();
        for (int i = 0; i < 15; i++) {
            if (i == 0 || i == 14) {
                for (int j = 0; j < 150; j++) {
                    System.out.print("#");
                }
            } else {
                String[] tmp = strLogo[i - 1].split(",");
                int k = 0;
                for (int j = 0; j < 150; j++) {
                    if (k < tmp.length) {
                        if (j == Integer.valueOf(tmp[k])) {
                            System.out.print(" ");
                            k++;
                            continue;
                        }
                    }
                    System.out.print("#");
                }
            }
            System.out.println();
        }
    }

    public void showModule() {
        System.out.println("\n+++++++++++++++++++++++++++++++");
        System.out.println("+            COMPSRA          +");
        System.out.println("+     (V1.0.1 2020-06-26)     +");
        System.out.println("+  rejia@channing.harvard.edu +");
        System.out.println("+++++++++++++++++++++++++++++++\n");
    }

    public void setDB() {
        //Set Configuration file.
        DOWNLOAD.put("database_record.obj", DOWNLOAD_HOMEPAGE+Configuration.BUNDLE+"/configuration/database_record.obj");

        //Set EndoDatabase.
        File fleConfig=new File(CONFIG_ENDO_DATABASE);
        if(!fleConfig.exists()){
            String strURL=DOWNLOAD.get("database_record.obj");
            Download dldConfig=new Download(strURL,CONFIG_ENDO_DATABASE,false);
            boolean boolFlag=dldConfig.download();
            if(!boolFlag){
                LOG.info("The configuration file doesn't exist! Please download it first and run the pipeline!");
                System.exit(0);
            }
        }
        hmpEndoDatabase = this.readObj(CONFIG_ENDO_DATABASE);
        System.out.println("Configuration Info: The endogenous database configuration has been set!");
        System.out.println("Configuration Info: " + hmpEndoDatabase.size() + " databases are set!");

        
        //Set Adapter file.
        DOWNLOAD.put("adapter.txt", DOWNLOAD_HOMEPAGE+DOWNLOAD_BUNDLE_VERSION+"/configuration/adapter.txt");
        
        //Set Reference file. 
        DOWNLOAD.put("hg38.cps", DOWNLOAD_HOMEPAGE+DOWNLOAD_BUNDLE_VERSION+"/prebuilt_db/hg38.cps");
        
        //Set Download HashMap.
        for (DatabaseInfo dbi : Configuration.hmpEndoDatabase.values()) {
            String strKey = new File(dbi.strLeafDownloadAnn).getName();
            if(strKey.startsWith("NA")) continue;
            else{
                DOWNLOAD.put(strKey, DOWNLOAD_HOMEPAGE + DOWNLOAD_BUNDLE_VERSION+ dbi.strLeafDownloadAnn);
            }
            
            String[] strKeys= dbi.strLeafObj.split(";");
            for(String key:strKeys){
                if(key.startsWith("NA"))    continue;
                else{
                    strKey=new File(key).getName();
                    DOWNLOAD.put(strKey, DOWNLOAD_HOMEPAGE + DOWNLOAD_BUNDLE_VERSION+ key);
                }
            }
        }
        DOWNLOAD.put("star", DOWNLOAD_HOMEPAGE+DOWNLOAD_BUNDLE_VERSION+"/plug/star/"+STAR);
        DOWNLOAD.put("blast", DOWNLOAD_HOMEPAGE+DOWNLOAD_BUNDLE_VERSION+"/plug/blast/"+BLAST);
        DOWNLOAD.put("blast2", "ftp://ftp.ncbi.nlm.nih.gov/blast/executables/blast+/2.7.1/"+BLAST);
        DOWNLOAD.put(STAR,DOWNLOAD_HOMEPAGE+DOWNLOAD_BUNDLE_VERSION+"/plug/star/"+STAR);
//        DOWNLOAD.put("hg19.fa.gz", DOWNLOAD_HOMEPAGE+DOWNLOAD_BUNDLE_VERSION+"/plug/star/hg19.fa.gz");
        DOWNLOAD.put("hg19.fa.gz", "http://hgdownload.cse.ucsc.edu/goldenPath/hg19/bigZips/chromFa.tar.gz");
//        DOWNLOAD.put("hg38.fa.gz", DOWNLOAD_HOMEPAGE+DOWNLOAD_BUNDLE_VERSION+"/plug/star/hg38.fa.gz");
        DOWNLOAD.put("hg38.fa.gz", "http://hgdownload.soe.ucsc.edu/goldenPath/hg38/bigZips/hg38.fa.gz");
        DOWNLOAD.put("mm9.fa.gz", DOWNLOAD_HOMEPAGE+DOWNLOAD_BUNDLE_VERSION+"/plug/star/mm9.fa.gz");
        DOWNLOAD.put("mm10.fa.gz", DOWNLOAD_HOMEPAGE+DOWNLOAD_BUNDLE_VERSION+"/plug/star/mm10.fa.gz");
        DOWNLOAD.put("rn5.fa.gz", "ftp://hgdownload.soe.ucsc.edu/goldenPath/rn5/bigZips/rn5.fa.gz");
        DOWNLOAD.put("rn6.fa.gz", "ftp://hgdownload.soe.ucsc.edu/goldenPath/rn6/bigZips/rn6.fa.gz");
        
        DOWNLOAD.put(BLAST, "ftp://ftp.ncbi.nlm.nih.gov/blast/executables/blast+/2.7.1/"+BLAST);
        DOWNLOAD.put("nt_archaea.fasta.gz",DOWNLOAD_HOMEPAGE+DOWNLOAD_BUNDLE_VERSION+"/db/nt/nt_archaea.fasta.gz");
        DOWNLOAD.put("nt_bacteria.fasta.gz",DOWNLOAD_HOMEPAGE+DOWNLOAD_BUNDLE_VERSION+"/db/nt/nt_bacteria.fasta.gz");
        DOWNLOAD.put("nt_fungi.fasta.gz",DOWNLOAD_HOMEPAGE+DOWNLOAD_BUNDLE_VERSION+"/db/nt/nt_fungi.fasta.gz");
        DOWNLOAD.put("nt_viruses.fasta.gz",DOWNLOAD_HOMEPAGE+DOWNLOAD_BUNDLE_VERSION+"/db/nt/nt_viruses.fasta.gz");
        DOWNLOAD.put("names.dmp",DOWNLOAD_HOMEPAGE+DOWNLOAD_BUNDLE_VERSION+"/db/nt/names.dmp");
        DOWNLOAD.put("nodes.dmp",DOWNLOAD_HOMEPAGE+DOWNLOAD_BUNDLE_VERSION+"/db/nt/nodes.dmp");
        DOWNLOAD.put("acc2tax.obj",DOWNLOAD_HOMEPAGE+DOWNLOAD_BUNDLE_VERSION+"/prebuilt_db/acc2tax.obj");
        DOWNLOAD.put("nt_genome.obj",DOWNLOAD_HOMEPAGE+DOWNLOAD_BUNDLE_VERSION+"/prebuilt_db/nt_genome.obj");
        DOWNLOAD.put("taxonomy.obj",DOWNLOAD_HOMEPAGE+DOWNLOAD_BUNDLE_VERSION+"/prebuilt_db/taxonomy.obj");
        //For test purpose.
//        DOWNLOAD.put("test","https://www.dropbox.com/s/ci8o91zm1xw4nuk/nodes.dmp?dl=0");
    }

    
    public void writeObj(String strObj, HashMap<String, DatabaseInfo> hmpDB) {
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(strObj)));
            oos.writeObject(hmpDB);
            oos.close();
        } catch (IOException ex) {
            LOG.error(ex.getMessage());
        } finally {
            try {
                oos.close();
            } catch (IOException ex) {
                LOG.error(ex.getMessage());
            }
        }
    }

    public HashMap<String, DatabaseInfo> readObj(String strFile) {
        ObjectInputStream ois = null;
        HashMap<String, DatabaseInfo> hmpConfig = null;
        try {
            File fleObj = new File(strFile);
            if (!fleObj.exists()) {
                return null;
            }
            ois = new ObjectInputStream(new GZIPInputStream(new FileInputStream(strFile)));
            hmpConfig = (HashMap<String, DatabaseInfo>) ois.readObject();
            ois.close();
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            LOG.error("Fail to find the prebuilt configuration obj file!");
        } finally {
            return hmpConfig;
        }
    }

    
    public void buildObj(String strFile) {
        BufferedReader brDB = null;
        try {
            hmpEndoDatabase = new <String, DatabaseInfo>HashMap();
            brDB = new BufferedReader(new FileReader(strFile));
            String strLine;
            while ((strLine = brDB.readLine()) != null) {
                if (strLine.startsWith("#")) {
                    continue;
                }
                String[] strColumns = strLine.split("\t");

                DatabaseInfo dbiEntity = new DatabaseInfo();
                dbiEntity.strKeyID = strColumns[0];
                dbiEntity.strDatabaseID = strColumns[1];
                dbiEntity.strDatabaseName = strColumns[2];
                dbiEntity.strDatabaseRelease = strColumns[3];
                dbiEntity.strDatabaseWebpage = strColumns[4];
                dbiEntity.strLeafID = strColumns[5];
                dbiEntity.strRefVersion = strColumns[6];
                dbiEntity.strLeafSpecies = strColumns[7];
                dbiEntity.strLeafTaxonomy = strColumns[8];
                dbiEntity.strLeafDownloadGenome = strColumns[9];
                dbiEntity.boolIsLeafBuildIn = Boolean.valueOf(strColumns[10]);
                dbiEntity.strLeafBuildPath = strColumns[11];
                dbiEntity.strLeafDependency = strColumns[12];
                dbiEntity.strLeafDownloadAnn = strColumns[13];
                dbiEntity.strLeafObj = strColumns[14];
                hmpEndoDatabase.put(strColumns[0], dbiEntity);
            }
            System.out.println("Configuration Info: The endogenous database configuration has been set!");
            System.out.println("Configuration Info: " + hmpEndoDatabase.size() + " databases are set!");
        } catch (FileNotFoundException ex) {
            System.out.println("Error Info: The database configuration file doesn't exist! ");
            System.exit(0);
        } catch (IOException ex) {
            System.out.println("Error Info: Something wrong in the database configuration file!");
            System.exit(0);
        }
        this.writeObj(strFile.replace(".txt", ".obj"), hmpEndoDatabase);
    }

    public static String getContig(String key, String contig) {

        try {
            if (UCSC_CONTIG.containsKey(contig)) {
                return UCSC_CONTIG.get(contig);
            } else {
                throw new Exception();
            }
        } catch (Exception e) {
            if (Configuration.hmpEndoDatabase.get(key).strRefVersion.contains("hg38")) {
                if (contig.contains("ki270721")) {
                    return "chr11_KI270721v1_random";
                }
                if (contig.contains("gl000009")) {
                    return "chr14_GL000009v2_random";
                }
                if (contig.contains("gl000225")) {
                    return "chr14_GL000225v1_random";
                }
                if (contig.contains("ki270722")) {
                    return "chr14_KI270722v1_random";
                }
                if (contig.contains("gl000194")) {
                    return "chr14_GL000194v1_random";
                }
                if (contig.contains("ki270723")) {
                    return "chr14_KI270723v1_random";
                }
                if (contig.contains("ki270724")) {
                    return "chr14_KI270724v1_random";
                }
                if (contig.contains("ki270725")) {
                    return "chr14_KI270725v1_random";
                }
                if (contig.contains("ki270726")) {
                    return "chr14_KI270726v1_random";
                }
                if (contig.contains("ki270727")) {
                    return "chr15_KI270727v1_random";
                }
                if (contig.contains("ki270728")) {
                    return "chr16_KI270728v1_random";
                }
                if (contig.contains("gl000205")) {
                    return "chr17_GL000205v2_random";
                }
                if (contig.contains("ki270729")) {
                    return "chr17_KI270729v1_random";
                }
                if (contig.contains("ki270730")) {
                    return "chr17_KI270730v1_random";
                }
                if (contig.contains("ki270706")) {
                    return "chr1_KI270706v1_random";
                }
                if (contig.contains("ki270707")) {
                    return "chr1_KI270707v1_random";
                }
                if (contig.contains("ki270708")) {
                    return "chr1_KI270708v1_random";
                }
                if (contig.contains("ki270709")) {
                    return "chr1_KI270709v1_random";
                }
                if (contig.contains("ki270710")) {
                    return "chr1_KI270710v1_random";
                }
                if (contig.contains("ki270711")) {
                    return "chr1_KI270711v1_random";
                }
                if (contig.contains("ki270712")) {
                    return "chr1_KI270712v1_random";
                }
                if (contig.contains("ki270713")) {
                    return "chr1_KI270713v1_random";
                }
                if (contig.contains("ki270714")) {
                    return "chr1_KI270714v1_random";
                }
                if (contig.contains("ki270731")) {
                    return "chr22_KI270731v1_random";
                }
                if (contig.contains("ki270732")) {
                    return "chr22_KI270732v1_random";
                }
                if (contig.contains("ki270733")) {
                    return "chr22_KI270733v1_random";
                }
                if (contig.contains("ki270734")) {
                    return "chr22_KI270734v1_random";
                }
                if (contig.contains("ki270735")) {
                    return "chr22_KI270735v1_random";
                }
                if (contig.contains("ki270736")) {
                    return "chr22_KI270736v1_random";
                }
                if (contig.contains("ki270737")) {
                    return "chr22_KI270737v1_random";
                }
                if (contig.contains("ki270738")) {
                    return "chr22_KI270738v1_random";
                }
                if (contig.contains("ki270739")) {
                    return "chr22_KI270739v1_random";
                }
                if (contig.contains("ki270715")) {
                    return "chr2_KI270715v1_random";
                }
                if (contig.contains("ki270716")) {
                    return "chr2_KI270716v1_random";
                }
                if (contig.contains("gl000221")) {
                    return "chr3_GL000221v1_random";
                }
                if (contig.contains("gl000008")) {
                    return "chr4_GL000008v2_random";
                }
                if (contig.contains("gl000208")) {
                    return "chr5_GL000208v1_random";
                }
                if (contig.contains("ki270717")) {
                    return "chr9_KI270717v1_random";
                }
                if (contig.contains("ki270718")) {
                    return "chr9_KI270718v1_random";
                }
                if (contig.contains("ki270719")) {
                    return "chr9_KI270719v1_random";
                }
                if (contig.contains("ki270720")) {
                    return "chr9_KI270720v1_random";
                }
                if (contig.contains("ki270762")) {
                    return "chr1_KI270762v1_alt";
                }
                if (contig.contains("ki270766")) {
                    return "chr1_KI270766v1_alt";
                }
                if (contig.contains("ki270760")) {
                    return "chr1_KI270760v1_alt";
                }
                if (contig.contains("ki270765")) {
                    return "chr1_KI270765v1_alt";
                }
                if (contig.contains("gl383518")) {
                    return "chr1_GL383518v1_alt";
                }
                if (contig.contains("gl383519")) {
                    return "chr1_GL383519v1_alt";
                }
                if (contig.contains("gl383520")) {
                    return "chr1_GL383520v2_alt";
                }
                if (contig.contains("ki270764")) {
                    return "chr1_KI270764v1_alt";
                }
                if (contig.contains("ki270763")) {
                    return "chr1_KI270763v1_alt";
                }
                if (contig.contains("ki270759")) {
                    return "chr1_KI270759v1_alt";
                }
                if (contig.contains("ki270761")) {
                    return "chr1_KI270761v1_alt";
                }
                if (contig.contains("ki270770")) {
                    return "chr2_KI270770v1_alt";
                }
                if (contig.contains("ki270773")) {
                    return "chr2_KI270773v1_alt";
                }
                if (contig.contains("ki270774")) {
                    return "chr2_KI270774v1_alt";
                }
                if (contig.contains("ki270769")) {
                    return "chr2_KI270769v1_alt";
                }
                if (contig.contains("gl383521")) {
                    return "chr2_GL383521v1_alt";
                }
                if (contig.contains("ki270772")) {
                    return "chr2_KI270772v1_alt";
                }
                if (contig.contains("ki270775")) {
                    return "chr2_KI270775v1_alt";
                }
                if (contig.contains("ki270771")) {
                    return "chr2_KI270771v1_alt";
                }
                if (contig.contains("ki270768")) {
                    return "chr2_KI270768v1_alt";
                }
                if (contig.contains("gl582966")) {
                    return "chr2_GL582966v2_alt";
                }
                if (contig.contains("gl383522")) {
                    return "chr2_GL383522v1_alt";
                }
                if (contig.contains("ki270776")) {
                    return "chr2_KI270776v1_alt";
                }
                if (contig.contains("ki270767")) {
                    return "chr2_KI270767v1_alt";
                }
                if (contig.contains("jh636055")) {
                    return "chr3_JH636055v2_alt";
                }
                if (contig.contains("ki270783")) {
                    return "chr3_KI270783v1_alt";
                }
                if (contig.contains("ki270780")) {
                    return "chr3_KI270780v1_alt";
                }
                if (contig.contains("gl383526")) {
                    return "chr3_GL383526v1_alt";
                }
                if (contig.contains("ki270777")) {
                    return "chr3_KI270777v1_alt";
                }
                if (contig.contains("ki270778")) {
                    return "chr3_KI270778v1_alt";
                }
                if (contig.contains("ki270781")) {
                    return "chr3_KI270781v1_alt";
                }
                if (contig.contains("ki270779")) {
                    return "chr3_KI270779v1_alt";
                }
                if (contig.contains("ki270782")) {
                    return "chr3_KI270782v1_alt";
                }
                if (contig.contains("ki270784")) {
                    return "chr3_KI270784v1_alt";
                }
                if (contig.contains("ki270790")) {
                    return "chr4_KI270790v1_alt";
                }
                if (contig.contains("gl383528")) {
                    return "chr4_GL383528v1_alt";
                }
                if (contig.contains("ki270787")) {
                    return "chr4_KI270787v1_alt";
                }
                if (contig.contains("gl000257")) {
                    return "chr4_GL000257v2_alt";
                }
                if (contig.contains("ki270788")) {
                    return "chr4_KI270788v1_alt";
                }
                if (contig.contains("gl383527")) {
                    return "chr4_GL383527v1_alt";
                }
                if (contig.contains("ki270785")) {
                    return "chr4_KI270785v1_alt";
                }
                if (contig.contains("ki270789")) {
                    return "chr4_KI270789v1_alt";
                }
                if (contig.contains("ki270786")) {
                    return "chr4_KI270786v1_alt";
                }
                if (contig.contains("ki270793")) {
                    return "chr5_KI270793v1_alt";
                }
                if (contig.contains("ki270792")) {
                    return "chr5_KI270792v1_alt";
                }
                if (contig.contains("ki270791")) {
                    return "chr5_KI270791v1_alt";
                }
                if (contig.contains("gl383532")) {
                    return "chr5_GL383532v1_alt";
                }
                if (contig.contains("gl949742")) {
                    return "chr5_GL949742v1_alt";
                }
                if (contig.contains("ki270794")) {
                    return "chr5_KI270794v1_alt";
                }
                if (contig.contains("gl339449")) {
                    return "chr5_GL339449v2_alt";
                }
                if (contig.contains("gl383530")) {
                    return "chr5_GL383530v1_alt";
                }
                if (contig.contains("ki270796")) {
                    return "chr5_KI270796v1_alt";
                }
                if (contig.contains("gl383531")) {
                    return "chr5_GL383531v1_alt";
                }
                if (contig.contains("ki270795")) {
                    return "chr5_KI270795v1_alt";
                }
                if (contig.contains("gl000250")) {
                    return "chr6_GL000250v2_alt";
                }
                if (contig.contains("ki270800")) {
                    return "chr6_KI270800v1_alt";
                }
                if (contig.contains("ki270799")) {
                    return "chr6_KI270799v1_alt";
                }
                if (contig.contains("gl383533")) {
                    return "chr6_GL383533v1_alt";
                }
                if (contig.contains("ki270801")) {
                    return "chr6_KI270801v1_alt";
                }
                if (contig.contains("ki270802")) {
                    return "chr6_KI270802v1_alt";
                }
                if (contig.contains("kb021644")) {
                    return "chr6_KB021644v2_alt";
                }
                if (contig.contains("ki270797")) {
                    return "chr6_KI270797v1_alt";
                }
                if (contig.contains("ki270798")) {
                    return "chr6_KI270798v1_alt";
                }
                if (contig.contains("ki270804")) {
                    return "chr7_KI270804v1_alt";
                }
                if (contig.contains("ki270809")) {
                    return "chr7_KI270809v1_alt";
                }
                if (contig.contains("ki270806")) {
                    return "chr7_KI270806v1_alt";
                }
                if (contig.contains("gl383534")) {
                    return "chr7_GL383534v2_alt";
                }
                if (contig.contains("ki270803")) {
                    return "chr7_KI270803v1_alt";
                }
                if (contig.contains("ki270808")) {
                    return "chr7_KI270808v1_alt";
                }
                if (contig.contains("ki270807")) {
                    return "chr7_KI270807v1_alt";
                }
                if (contig.contains("ki270805")) {
                    return "chr7_KI270805v1_alt";
                }
                if (contig.contains("ki270818")) {
                    return "chr8_KI270818v1_alt";
                }
                if (contig.contains("ki270812")) {
                    return "chr8_KI270812v1_alt";
                }
                if (contig.contains("ki270811")) {
                    return "chr8_KI270811v1_alt";
                }
                if (contig.contains("ki270821")) {
                    return "chr8_KI270821v1_alt";
                }
                if (contig.contains("ki270813")) {
                    return "chr8_KI270813v1_alt";
                }
                if (contig.contains("ki270822")) {
                    return "chr8_KI270822v1_alt";
                }
                if (contig.contains("ki270814")) {
                    return "chr8_KI270814v1_alt";
                }
                if (contig.contains("ki270810")) {
                    return "chr8_KI270810v1_alt";
                }
                if (contig.contains("ki270819")) {
                    return "chr8_KI270819v1_alt";
                }
                if (contig.contains("ki270820")) {
                    return "chr8_KI270820v1_alt";
                }
                if (contig.contains("ki270817")) {
                    return "chr8_KI270817v1_alt";
                }
                if (contig.contains("ki270816")) {
                    return "chr8_KI270816v1_alt";
                }
                if (contig.contains("ki270815")) {
                    return "chr8_KI270815v1_alt";
                }
                if (contig.contains("gl383539")) {
                    return "chr9_GL383539v1_alt";
                }
                if (contig.contains("gl383540")) {
                    return "chr9_GL383540v1_alt";
                }
                if (contig.contains("gl383541")) {
                    return "chr9_GL383541v1_alt";
                }
                if (contig.contains("gl383542")) {
                    return "chr9_GL383542v1_alt";
                }
                if (contig.contains("ki270823")) {
                    return "chr9_KI270823v1_alt";
                }
                if (contig.contains("gl383545")) {
                    return "chr10_GL383545v1_alt";
                }
                if (contig.contains("ki270824")) {
                    return "chr10_KI270824v1_alt";
                }
                if (contig.contains("gl383546")) {
                    return "chr10_GL383546v1_alt";
                }
                if (contig.contains("ki270825")) {
                    return "chr10_KI270825v1_alt";
                }
                if (contig.contains("ki270832")) {
                    return "chr11_KI270832v1_alt";
                }
                if (contig.contains("ki270830")) {
                    return "chr11_KI270830v1_alt";
                }
                if (contig.contains("ki270831")) {
                    return "chr11_KI270831v1_alt";
                }
                if (contig.contains("ki270829")) {
                    return "chr11_KI270829v1_alt";
                }
                if (contig.contains("gl383547")) {
                    return "chr11_GL383547v1_alt";
                }
                if (contig.contains("jh159136")) {
                    return "chr11_JH159136v1_alt";
                }
                if (contig.contains("jh159137")) {
                    return "chr11_JH159137v1_alt";
                }
                if (contig.contains("ki270827")) {
                    return "chr11_KI270827v1_alt";
                }
                if (contig.contains("ki270826")) {
                    return "chr11_KI270826v1_alt";
                }
                if (contig.contains("gl877875")) {
                    return "chr12_GL877875v1_alt";
                }
                if (contig.contains("gl877876")) {
                    return "chr12_GL877876v1_alt";
                }
                if (contig.contains("ki270837")) {
                    return "chr12_KI270837v1_alt";
                }
                if (contig.contains("gl383549")) {
                    return "chr12_GL383549v1_alt";
                }
                if (contig.contains("ki270835")) {
                    return "chr12_KI270835v1_alt";
                }
                if (contig.contains("gl383550")) {
                    return "chr12_GL383550v2_alt";
                }
                if (contig.contains("gl383552")) {
                    return "chr12_GL383552v1_alt";
                }
                if (contig.contains("gl383553")) {
                    return "chr12_GL383553v2_alt";
                }
                if (contig.contains("ki270834")) {
                    return "chr12_KI270834v1_alt";
                }
                if (contig.contains("gl383551")) {
                    return "chr12_GL383551v1_alt";
                }
                if (contig.contains("ki270833")) {
                    return "chr12_KI270833v1_alt";
                }
                if (contig.contains("ki270836")) {
                    return "chr12_KI270836v1_alt";
                }
                if (contig.contains("ki270840")) {
                    return "chr13_KI270840v1_alt";
                }
                if (contig.contains("ki270839")) {
                    return "chr13_KI270839v1_alt";
                }
                if (contig.contains("ki270843")) {
                    return "chr13_KI270843v1_alt";
                }
                if (contig.contains("ki270841")) {
                    return "chr13_KI270841v1_alt";
                }
                if (contig.contains("ki270838")) {
                    return "chr13_KI270838v1_alt";
                }
                if (contig.contains("ki270842")) {
                    return "chr13_KI270842v1_alt";
                }
                if (contig.contains("ki270844")) {
                    return "chr14_KI270844v1_alt";
                }
                if (contig.contains("ki270847")) {
                    return "chr14_KI270847v1_alt";
                }
                if (contig.contains("ki270845")) {
                    return "chr14_KI270845v1_alt";
                }
                if (contig.contains("ki270846")) {
                    return "chr14_KI270846v1_alt";
                }
                if (contig.contains("ki270852")) {
                    return "chr15_KI270852v1_alt";
                }
                if (contig.contains("ki270851")) {
                    return "chr15_KI270851v1_alt";
                }
                if (contig.contains("ki270848")) {
                    return "chr15_KI270848v1_alt";
                }
                if (contig.contains("gl383554")) {
                    return "chr15_GL383554v1_alt";
                }
                if (contig.contains("ki270849")) {
                    return "chr15_KI270849v1_alt";
                }
                if (contig.contains("gl383555")) {
                    return "chr15_GL383555v2_alt";
                }
                if (contig.contains("ki270850")) {
                    return "chr15_KI270850v1_alt";
                }
                if (contig.contains("ki270854")) {
                    return "chr16_KI270854v1_alt";
                }
                if (contig.contains("ki270856")) {
                    return "chr16_KI270856v1_alt";
                }
                if (contig.contains("ki270855")) {
                    return "chr16_KI270855v1_alt";
                }
                if (contig.contains("ki270853")) {
                    return "chr16_KI270853v1_alt";
                }
                if (contig.contains("gl383556")) {
                    return "chr16_GL383556v1_alt";
                }
                if (contig.contains("gl383557")) {
                    return "chr16_GL383557v1_alt";
                }
                if (contig.contains("gl383563")) {
                    return "chr17_GL383563v3_alt";
                }
                if (contig.contains("ki270862")) {
                    return "chr17_KI270862v1_alt";
                }
                if (contig.contains("ki270861")) {
                    return "chr17_KI270861v1_alt";
                }
                if (contig.contains("ki270857")) {
                    return "chr17_KI270857v1_alt";
                }
                if (contig.contains("jh159146")) {
                    return "chr17_JH159146v1_alt";
                }
                if (contig.contains("jh159147")) {
                    return "chr17_JH159147v1_alt";
                }
                if (contig.contains("gl383564")) {
                    return "chr17_GL383564v2_alt";
                }
                if (contig.contains("gl000258")) {
                    return "chr17_GL000258v2_alt";
                }
                if (contig.contains("gl383565")) {
                    return "chr17_GL383565v1_alt";
                }
                if (contig.contains("ki270858")) {
                    return "chr17_KI270858v1_alt";
                }
                if (contig.contains("ki270859")) {
                    return "chr17_KI270859v1_alt";
                }
                if (contig.contains("gl383566")) {
                    return "chr17_GL383566v1_alt";
                }
                if (contig.contains("ki270860")) {
                    return "chr17_KI270860v1_alt";
                }
                if (contig.contains("ki270864")) {
                    return "chr18_KI270864v1_alt";
                }
                if (contig.contains("gl383567")) {
                    return "chr18_GL383567v1_alt";
                }
                if (contig.contains("gl383570")) {
                    return "chr18_GL383570v1_alt";
                }
                if (contig.contains("gl383571")) {
                    return "chr18_GL383571v1_alt";
                }
                if (contig.contains("gl383568")) {
                    return "chr18_GL383568v1_alt";
                }
                if (contig.contains("gl383569")) {
                    return "chr18_GL383569v1_alt";
                }
                if (contig.contains("gl383572")) {
                    return "chr18_GL383572v1_alt";
                }
                if (contig.contains("ki270863")) {
                    return "chr18_KI270863v1_alt";
                }
                if (contig.contains("ki270868")) {
                    return "chr19_KI270868v1_alt";
                }
                if (contig.contains("ki270865")) {
                    return "chr19_KI270865v1_alt";
                }
                if (contig.contains("gl383573")) {
                    return "chr19_GL383573v1_alt";
                }
                if (contig.contains("gl383575")) {
                    return "chr19_GL383575v2_alt";
                }
                if (contig.contains("gl383576")) {
                    return "chr19_GL383576v1_alt";
                }
                if (contig.contains("gl383574")) {
                    return "chr19_GL383574v1_alt";
                }
                if (contig.contains("ki270866")) {
                    return "chr19_KI270866v1_alt";
                }
                if (contig.contains("ki270867")) {
                    return "chr19_KI270867v1_alt";
                }
                if (contig.contains("gl949746")) {
                    return "chr19_GL949746v1_alt";
                }
                if (contig.contains("gl383577")) {
                    return "chr20_GL383577v2_alt";
                }
                if (contig.contains("ki270869")) {
                    return "chr20_KI270869v1_alt";
                }
                if (contig.contains("ki270871")) {
                    return "chr20_KI270871v1_alt";
                }
                if (contig.contains("ki270870")) {
                    return "chr20_KI270870v1_alt";
                }
                if (contig.contains("gl383578")) {
                    return "chr21_GL383578v2_alt";
                }
                if (contig.contains("ki270874")) {
                    return "chr21_KI270874v1_alt";
                }
                if (contig.contains("ki270873")) {
                    return "chr21_KI270873v1_alt";
                }
                if (contig.contains("gl383579")) {
                    return "chr21_GL383579v2_alt";
                }
                if (contig.contains("gl383580")) {
                    return "chr21_GL383580v2_alt";
                }
                if (contig.contains("gl383581")) {
                    return "chr21_GL383581v2_alt";
                }
                if (contig.contains("ki270872")) {
                    return "chr21_KI270872v1_alt";
                }
                if (contig.contains("ki270875")) {
                    return "chr22_KI270875v1_alt";
                }
                if (contig.contains("ki270878")) {
                    return "chr22_KI270878v1_alt";
                }
                if (contig.contains("ki270879")) {
                    return "chr22_KI270879v1_alt";
                }
                if (contig.contains("ki270876")) {
                    return "chr22_KI270876v1_alt";
                }
                if (contig.contains("ki270877")) {
                    return "chr22_KI270877v1_alt";
                }
                if (contig.contains("gl383583")) {
                    return "chr22_GL383583v2_alt";
                }
                if (contig.contains("gl383582")) {
                    return "chr22_GL383582v2_alt";
                }
                if (contig.contains("ki270880")) {
                    return "chrX_KI270880v1_alt";
                }
                if (contig.contains("ki270881")) {
                    return "chrX_KI270881v1_alt";
                }
                if (contig.contains("ki270882")) {
                    return "chr19_KI270882v1_alt";
                }
                if (contig.contains("ki270883")) {
                    return "chr19_KI270883v1_alt";
                }
                if (contig.contains("ki270884")) {
                    return "chr19_KI270884v1_alt";
                }
                if (contig.contains("ki270885")) {
                    return "chr19_KI270885v1_alt";
                }
                if (contig.contains("ki270886")) {
                    return "chr19_KI270886v1_alt";
                }
                if (contig.contains("ki270887")) {
                    return "chr19_KI270887v1_alt";
                }
                if (contig.contains("ki270888")) {
                    return "chr19_KI270888v1_alt";
                }
                if (contig.contains("ki270889")) {
                    return "chr19_KI270889v1_alt";
                }
                if (contig.contains("ki270890")) {
                    return "chr19_KI270890v1_alt";
                }
                if (contig.contains("ki270891")) {
                    return "chr19_KI270891v1_alt";
                }
                if (contig.contains("ki270892")) {
                    return "chr1_KI270892v1_alt";
                }
                if (contig.contains("ki270894")) {
                    return "chr2_KI270894v1_alt";
                }
                if (contig.contains("ki270893")) {
                    return "chr2_KI270893v1_alt";
                }
                if (contig.contains("ki270895")) {
                    return "chr3_KI270895v1_alt";
                }
                if (contig.contains("ki270896")) {
                    return "chr4_KI270896v1_alt";
                }
                if (contig.contains("ki270897")) {
                    return "chr5_KI270897v1_alt";
                }
                if (contig.contains("ki270898")) {
                    return "chr5_KI270898v1_alt";
                }
                if (contig.contains("gl000251")) {
                    return "chr6_GL000251v2_alt";
                }
                if (contig.contains("ki270899")) {
                    return "chr7_KI270899v1_alt";
                }
                if (contig.contains("ki270901")) {
                    return "chr8_KI270901v1_alt";
                }
                if (contig.contains("ki270900")) {
                    return "chr8_KI270900v1_alt";
                }
                if (contig.contains("ki270902")) {
                    return "chr11_KI270902v1_alt";
                }
                if (contig.contains("ki270903")) {
                    return "chr11_KI270903v1_alt";
                }
                if (contig.contains("ki270904")) {
                    return "chr12_KI270904v1_alt";
                }
                if (contig.contains("ki270906")) {
                    return "chr15_KI270906v1_alt";
                }
                if (contig.contains("ki270905")) {
                    return "chr15_KI270905v1_alt";
                }
                if (contig.contains("ki270907")) {
                    return "chr17_KI270907v1_alt";
                }
                if (contig.contains("ki270910")) {
                    return "chr17_KI270910v1_alt";
                }
                if (contig.contains("ki270909")) {
                    return "chr17_KI270909v1_alt";
                }
                if (contig.contains("jh159148")) {
                    return "chr17_JH159148v1_alt";
                }
                if (contig.contains("ki270908")) {
                    return "chr17_KI270908v1_alt";
                }
                if (contig.contains("ki270912")) {
                    return "chr18_KI270912v1_alt";
                }
                if (contig.contains("ki270911")) {
                    return "chr18_KI270911v1_alt";
                }
                if (contig.contains("gl949747")) {
                    return "chr19_GL949747v2_alt";
                }
                if (contig.contains("kb663609")) {
                    return "chr22_KB663609v1_alt";
                }
                if (contig.contains("ki270913")) {
                    return "chrX_KI270913v1_alt";
                }
                if (contig.contains("ki270914")) {
                    return "chr19_KI270914v1_alt";
                }
                if (contig.contains("ki270915")) {
                    return "chr19_KI270915v1_alt";
                }
                if (contig.contains("ki270916")) {
                    return "chr19_KI270916v1_alt";
                }
                if (contig.contains("ki270917")) {
                    return "chr19_KI270917v1_alt";
                }
                if (contig.contains("ki270918")) {
                    return "chr19_KI270918v1_alt";
                }
                if (contig.contains("ki270919")) {
                    return "chr19_KI270919v1_alt";
                }
                if (contig.contains("ki270920")) {
                    return "chr19_KI270920v1_alt";
                }
                if (contig.contains("ki270921")) {
                    return "chr19_KI270921v1_alt";
                }
                if (contig.contains("ki270922")) {
                    return "chr19_KI270922v1_alt";
                }
                if (contig.contains("ki270923")) {
                    return "chr19_KI270923v1_alt";
                }
                if (contig.contains("ki270924")) {
                    return "chr3_KI270924v1_alt";
                }
                if (contig.contains("ki270925")) {
                    return "chr4_KI270925v1_alt";
                }
                if (contig.contains("gl000252")) {
                    return "chr6_GL000252v2_alt";
                }
                if (contig.contains("ki270926")) {
                    return "chr8_KI270926v1_alt";
                }
                if (contig.contains("ki270927")) {
                    return "chr11_KI270927v1_alt";
                }
                if (contig.contains("gl949748")) {
                    return "chr19_GL949748v2_alt";
                }
                if (contig.contains("ki270928")) {
                    return "chr22_KI270928v1_alt";
                }
                if (contig.contains("ki270929")) {
                    return "chr19_KI270929v1_alt";
                }
                if (contig.contains("ki270930")) {
                    return "chr19_KI270930v1_alt";
                }
                if (contig.contains("ki270931")) {
                    return "chr19_KI270931v1_alt";
                }
                if (contig.contains("ki270932")) {
                    return "chr19_KI270932v1_alt";
                }
                if (contig.contains("ki270933")) {
                    return "chr19_KI270933v1_alt";
                }
                if (contig.contains("gl000209")) {
                    return "chr19_GL000209v2_alt";
                }
                if (contig.contains("ki270934")) {
                    return "chr3_KI270934v1_alt";
                }
                if (contig.contains("gl000253")) {
                    return "chr6_GL000253v2_alt";
                }
                if (contig.contains("gl949749")) {
                    return "chr19_GL949749v2_alt";
                }
                if (contig.contains("ki270935")) {
                    return "chr3_KI270935v1_alt";
                }
                if (contig.contains("gl000254")) {
                    return "chr6_GL000254v2_alt";
                }
                if (contig.contains("gl949750")) {
                    return "chr19_GL949750v2_alt";
                }
                if (contig.contains("ki270936")) {
                    return "chr3_KI270936v1_alt";
                }
                if (contig.contains("gl000255")) {
                    return "chr6_GL000255v2_alt";
                }
                if (contig.contains("gl949751")) {
                    return "chr19_GL949751v2_alt";
                }
                if (contig.contains("ki270937")) {
                    return "chr3_KI270937v1_alt";
                }
                if (contig.contains("gl000256")) {
                    return "chr6_GL000256v2_alt";
                }
                if (contig.contains("gl949752")) {
                    return "chr19_GL949752v1_alt";
                }
                if (contig.contains("ki270758")) {
                    return "chr6_KI270758v1_alt";
                }
                if (contig.contains("gl949753")) {
                    return "chr19_GL949753v2_alt";
                }
                if (contig.contains("ki270938")) {
                    return "chr19_KI270938v1_alt";
                }
                if (contig.contains("ki270302")) {
                    return "chrUn_KI270302v1";
                }
                if (contig.contains("ki270304")) {
                    return "chrUn_KI270304v1";
                }
                if (contig.contains("ki270303")) {
                    return "chrUn_KI270303v1";
                }
                if (contig.contains("ki270305")) {
                    return "chrUn_KI270305v1";
                }
                if (contig.contains("ki270322")) {
                    return "chrUn_KI270322v1";
                }
                if (contig.contains("ki270320")) {
                    return "chrUn_KI270320v1";
                }
                if (contig.contains("ki270310")) {
                    return "chrUn_KI270310v1";
                }
                if (contig.contains("ki270316")) {
                    return "chrUn_KI270316v1";
                }
                if (contig.contains("ki270315")) {
                    return "chrUn_KI270315v1";
                }
                if (contig.contains("ki270312")) {
                    return "chrUn_KI270312v1";
                }
                if (contig.contains("ki270311")) {
                    return "chrUn_KI270311v1";
                }
                if (contig.contains("ki270317")) {
                    return "chrUn_KI270317v1";
                }
                if (contig.contains("ki270412")) {
                    return "chrUn_KI270412v1";
                }
                if (contig.contains("ki270411")) {
                    return "chrUn_KI270411v1";
                }
                if (contig.contains("ki270414")) {
                    return "chrUn_KI270414v1";
                }
                if (contig.contains("ki270419")) {
                    return "chrUn_KI270419v1";
                }
                if (contig.contains("ki270418")) {
                    return "chrUn_KI270418v1";
                }
                if (contig.contains("ki270420")) {
                    return "chrUn_KI270420v1";
                }
                if (contig.contains("ki270424")) {
                    return "chrUn_KI270424v1";
                }
                if (contig.contains("ki270417")) {
                    return "chrUn_KI270417v1";
                }
                if (contig.contains("ki270422")) {
                    return "chrUn_KI270422v1";
                }
                if (contig.contains("ki270423")) {
                    return "chrUn_KI270423v1";
                }
                if (contig.contains("ki270425")) {
                    return "chrUn_KI270425v1";
                }
                if (contig.contains("ki270429")) {
                    return "chrUn_KI270429v1";
                }
                if (contig.contains("ki270442")) {
                    return "chrUn_KI270442v1";
                }
                if (contig.contains("ki270466")) {
                    return "chrUn_KI270466v1";
                }
                if (contig.contains("ki270465")) {
                    return "chrUn_KI270465v1";
                }
                if (contig.contains("ki270467")) {
                    return "chrUn_KI270467v1";
                }
                if (contig.contains("ki270435")) {
                    return "chrUn_KI270435v1";
                }
                if (contig.contains("ki270438")) {
                    return "chrUn_KI270438v1";
                }
                if (contig.contains("ki270468")) {
                    return "chrUn_KI270468v1";
                }
                if (contig.contains("ki270510")) {
                    return "chrUn_KI270510v1";
                }
                if (contig.contains("ki270509")) {
                    return "chrUn_KI270509v1";
                }
                if (contig.contains("ki270518")) {
                    return "chrUn_KI270518v1";
                }
                if (contig.contains("ki270508")) {
                    return "chrUn_KI270508v1";
                }
                if (contig.contains("ki270516")) {
                    return "chrUn_KI270516v1";
                }
                if (contig.contains("ki270512")) {
                    return "chrUn_KI270512v1";
                }
                if (contig.contains("ki270519")) {
                    return "chrUn_KI270519v1";
                }
                if (contig.contains("ki270522")) {
                    return "chrUn_KI270522v1";
                }
                if (contig.contains("ki270511")) {
                    return "chrUn_KI270511v1";
                }
                if (contig.contains("ki270515")) {
                    return "chrUn_KI270515v1";
                }
                if (contig.contains("ki270507")) {
                    return "chrUn_KI270507v1";
                }
                if (contig.contains("ki270517")) {
                    return "chrUn_KI270517v1";
                }
                if (contig.contains("ki270529")) {
                    return "chrUn_KI270529v1";
                }
                if (contig.contains("ki270528")) {
                    return "chrUn_KI270528v1";
                }
                if (contig.contains("ki270530")) {
                    return "chrUn_KI270530v1";
                }
                if (contig.contains("ki270539")) {
                    return "chrUn_KI270539v1";
                }
                if (contig.contains("ki270538")) {
                    return "chrUn_KI270538v1";
                }
                if (contig.contains("ki270544")) {
                    return "chrUn_KI270544v1";
                }
                if (contig.contains("ki270548")) {
                    return "chrUn_KI270548v1";
                }
                if (contig.contains("ki270583")) {
                    return "chrUn_KI270583v1";
                }
                if (contig.contains("ki270587")) {
                    return "chrUn_KI270587v1";
                }
                if (contig.contains("ki270580")) {
                    return "chrUn_KI270580v1";
                }
                if (contig.contains("ki270581")) {
                    return "chrUn_KI270581v1";
                }
                if (contig.contains("ki270579")) {
                    return "chrUn_KI270579v1";
                }
                if (contig.contains("ki270589")) {
                    return "chrUn_KI270589v1";
                }
                if (contig.contains("ki270590")) {
                    return "chrUn_KI270590v1";
                }
                if (contig.contains("ki270584")) {
                    return "chrUn_KI270584v1";
                }
                if (contig.contains("ki270582")) {
                    return "chrUn_KI270582v1";
                }
                if (contig.contains("ki270588")) {
                    return "chrUn_KI270588v1";
                }
                if (contig.contains("ki270593")) {
                    return "chrUn_KI270593v1";
                }
                if (contig.contains("ki270591")) {
                    return "chrUn_KI270591v1";
                }
                if (contig.contains("ki270330")) {
                    return "chrUn_KI270330v1";
                }
                if (contig.contains("ki270329")) {
                    return "chrUn_KI270329v1";
                }
                if (contig.contains("ki270334")) {
                    return "chrUn_KI270334v1";
                }
                if (contig.contains("ki270333")) {
                    return "chrUn_KI270333v1";
                }
                if (contig.contains("ki270335")) {
                    return "chrUn_KI270335v1";
                }
                if (contig.contains("ki270338")) {
                    return "chrUn_KI270338v1";
                }
                if (contig.contains("ki270340")) {
                    return "chrUn_KI270340v1";
                }
                if (contig.contains("ki270336")) {
                    return "chrUn_KI270336v1";
                }
                if (contig.contains("ki270337")) {
                    return "chrUn_KI270337v1";
                }
                if (contig.contains("ki270363")) {
                    return "chrUn_KI270363v1";
                }
                if (contig.contains("ki270364")) {
                    return "chrUn_KI270364v1";
                }
                if (contig.contains("ki270362")) {
                    return "chrUn_KI270362v1";
                }
                if (contig.contains("ki270366")) {
                    return "chrUn_KI270366v1";
                }
                if (contig.contains("ki270378")) {
                    return "chrUn_KI270378v1";
                }
                if (contig.contains("ki270379")) {
                    return "chrUn_KI270379v1";
                }
                if (contig.contains("ki270389")) {
                    return "chrUn_KI270389v1";
                }
                if (contig.contains("ki270390")) {
                    return "chrUn_KI270390v1";
                }
                if (contig.contains("ki270387")) {
                    return "chrUn_KI270387v1";
                }
                if (contig.contains("ki270395")) {
                    return "chrUn_KI270395v1";
                }
                if (contig.contains("ki270396")) {
                    return "chrUn_KI270396v1";
                }
                if (contig.contains("ki270388")) {
                    return "chrUn_KI270388v1";
                }
                if (contig.contains("ki270394")) {
                    return "chrUn_KI270394v1";
                }
                if (contig.contains("ki270386")) {
                    return "chrUn_KI270386v1";
                }
                if (contig.contains("ki270391")) {
                    return "chrUn_KI270391v1";
                }
                if (contig.contains("ki270383")) {
                    return "chrUn_KI270383v1";
                }
                if (contig.contains("ki270393")) {
                    return "chrUn_KI270393v1";
                }
                if (contig.contains("ki270384")) {
                    return "chrUn_KI270384v1";
                }
                if (contig.contains("ki270392")) {
                    return "chrUn_KI270392v1";
                }
                if (contig.contains("ki270381")) {
                    return "chrUn_KI270381v1";
                }
                if (contig.contains("ki270385")) {
                    return "chrUn_KI270385v1";
                }
                if (contig.contains("ki270382")) {
                    return "chrUn_KI270382v1";
                }
                if (contig.contains("ki270376")) {
                    return "chrUn_KI270376v1";
                }
                if (contig.contains("ki270374")) {
                    return "chrUn_KI270374v1";
                }
                if (contig.contains("ki270372")) {
                    return "chrUn_KI270372v1";
                }
                if (contig.contains("ki270373")) {
                    return "chrUn_KI270373v1";
                }
                if (contig.contains("ki270375")) {
                    return "chrUn_KI270375v1";
                }
                if (contig.contains("ki270371")) {
                    return "chrUn_KI270371v1";
                }
                if (contig.contains("ki270448")) {
                    return "chrUn_KI270448v1";
                }
                if (contig.contains("ki270521")) {
                    return "chrUn_KI270521v1";
                }
                if (contig.contains("gl000195")) {
                    return "chrUn_GL000195v1";
                }
                if (contig.contains("gl000219")) {
                    return "chrUn_GL000219v1";
                }
                if (contig.contains("gl000220")) {
                    return "chrUn_GL000220v1";
                }
                if (contig.contains("gl000224")) {
                    return "chrUn_GL000224v1";
                }
                if (contig.contains("ki270741")) {
                    return "chrUn_KI270741v1";
                }
                if (contig.contains("gl000226")) {
                    return "chrUn_GL000226v1";
                }
                if (contig.contains("gl000213")) {
                    return "chrUn_GL000213v1";
                }
                if (contig.contains("ki270743")) {
                    return "chrUn_KI270743v1";
                }
                if (contig.contains("ki270744")) {
                    return "chrUn_KI270744v1";
                }
                if (contig.contains("ki270745")) {
                    return "chrUn_KI270745v1";
                }
                if (contig.contains("ki270746")) {
                    return "chrUn_KI270746v1";
                }
                if (contig.contains("ki270747")) {
                    return "chrUn_KI270747v1";
                }
                if (contig.contains("ki270748")) {
                    return "chrUn_KI270748v1";
                }
                if (contig.contains("ki270749")) {
                    return "chrUn_KI270749v1";
                }
                if (contig.contains("ki270750")) {
                    return "chrUn_KI270750v1";
                }
                if (contig.contains("ki270751")) {
                    return "chrUn_KI270751v1";
                }
                if (contig.contains("ki270752")) {
                    return "chrUn_KI270752v1";
                }
                if (contig.contains("ki270753")) {
                    return "chrUn_KI270753v1";
                }
                if (contig.contains("ki270754")) {
                    return "chrUn_KI270754v1";
                }
                if (contig.contains("ki270755")) {
                    return "chrUn_KI270755v1";
                }
                if (contig.contains("ki270756")) {
                    return "chrUn_KI270756v1";
                }
                if (contig.contains("ki270757")) {
                    return "chrUn_KI270757v1";
                }
                if (contig.contains("gl000214")) {
                    return "chrUn_GL000214v1";
                }
                if (contig.contains("ki270742")) {
                    return "chrUn_KI270742v1";
                }
                if (contig.contains("gl000216")) {
                    return "chrUn_GL000216v2";
                }
                if (contig.contains("gl000218")) {
                    return "chrUn_GL000218v1";
                }
                if (contig.contains("ki270740")) {
                    return "chrY_KI270740v1_random";
                }
            } else {
                //To be added!
                return contig;
            }
        }

        return contig;

        //R Code for this:        
//contig<-readLines("E:\\01Work\\miRNA\\project\\COMPASS\\configuration\\contig.txt")
//lj<-contig[grepl("_",contig)]
//tt<-sapply(lj,fun<-function(x){strsplit(x,"_")[[1]][2]},USE.NAMES = FALSE)
//db<-sapply(lj,fun<-function(x){gsub("v\\d$","",strsplit(x,"_")[[1]][2])},USE.NAMES = FALSE)
//
//for(i in 1:length(lj)){
//  cat("if(contig.contains(\"",tolower(db[i]),"\"))\treturn \"",lj[i],"\";",sep="")
//  cat("\n")
//}    
    }

    
    public static void main(String[] args){
        Configuration config=new Configuration();
    }
}
