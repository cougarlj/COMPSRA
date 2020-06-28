/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.channing.compass.core;

import edu.harvard.channing.compass.toolkit.ProduceTK;
import edu.harvard.channing.compass.core.fun.Function;
import edu.harvard.channing.compass.core.mic.Microbe;
import edu.harvard.channing.compass.core.ann.Annotation;
import edu.harvard.channing.compass.core.aln.Alignment;
import edu.harvard.channing.compass.core.qc.QualityControl;
import edu.harvard.channing.compass.toolkit.Adapter;
import edu.harvard.channing.compass.entity.CommonParameter;
import edu.harvard.channing.compass.toolkit.BuildDB;
import edu.harvard.channing.compass.toolkit.CallVariant;
import edu.harvard.channing.compass.toolkit.DownloadResource;
import edu.harvard.channing.compass.toolkit.ExtractSNV;
import edu.harvard.channing.compass.toolkit.FastAnnotation;
import edu.harvard.channing.compass.toolkit.UMI;
import edu.harvard.channing.compass.toolkit.Fasta;
import edu.harvard.channing.compass.toolkit.Merge;
import edu.harvard.channing.compass.toolkit.SingleAnn;
import edu.harvard.channing.compass.toolkit.Taxonomy;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class is used to define and parse the options from the command line. 
 * @author rejia
 * @since 2017-08-23
 */
public class Option {

    private static final Logger LOG = LogManager.getLogger(Option.class.getName());
    /**
     * options attribution contains all the options. 
     */
    public Options options;
    /**
     * comParam saves the basic parameters used for circuRNA.
     */
    public CommonParameter comParam;
    /**
     * qc is the main object for QualityControl.
     */
    public QualityControl qc;
    /**
     * align is the main object for Alignment.
     */
    public Alignment align;
    /**
     * ann is the main object for Annotation.
     */
    public Annotation ann;
    /**
     * mic is the main object for Microbe.
     */
    public Microbe mic;
    /**
     * fun is the main object for Function.
     */
    public Function fun;

    
    
    
    /**
     * This function is used to define the parameters in the command line. 
     */
    public Option() {
        options=new Options();
        
        //Set common options. 
        options.addOption("h", "help", false, "Display the help info.");
        options.addOption("in", "input",true,"Set the input file (.fastq format)."); //The text, gz, bgzip format are supported. 
        options.addOption("out", "output", true, "Set the output directory and prefix of output file.");//The prefix of output file is supported. 
        options.addOption("t", "threads",true, "Set the multithreads mode. （The default value is one.）");
        options.addOption("ref", "ref_genome", true, "Set the reference genome.");
        options.addOption("pro","project_name",true, "Set the project name. This name will be used when merging a set of files. (Default value is COMPASS)");
        options.addOption("inf","in_file",true,"Read the sample name from a file.");
        options.addOption("cr","check_resource",false,"Check the resource needed and download online if not existed.");
        
        
        //Set Quality Control options. 
        options.addOption("qc","quality_control",false,"Open the quality control module.");
        options.addOption("ra", "rm_adapter", true, "Remove adapters.");
        options.addOption("rb", "rm_bias", true, "Remove the randomized bases at the ligation junctions.");
        options.addOption("rh", "rm_low_quality_head",true, "Remove bases with low quality from head.");
        options.addOption("rt", "rm_low_quality_tail", true, "Remove bases with low quality from tail.");
        options.addOption("rr", "rm_low_quality_read", true, "Remove reads with low quality.");
        
        options.addOption("rhh", "rm_head_hard", true, "Remove N bases of the read directly from the head.");
        options.addOption("rth", "rm_tail_hard", true, "Remove N bases of the read directly from the tail.");
        options.addOption("rlh", "rm_read_hard", true, "Remove read with the length smaller(<) than N. ");
        
                
        //Set Alignment options.
        options.addOption("aln", "alignment", false, "Open the alignment module.");
        options.addOption("mt", "mapping_tool", true, "Set the alignment tool used to map reads to the reference genome.(Default value is star.)"); //The default tool for alignment is star.
        options.addOption("mp","mapping_param",true,"Set the main parameters of the aligner.");
        options.addOption("midx","mapping_index",true,"Choose the files that were seperated by read length for alignment.(Default value is last, which means the files with the longest reads set.)");//"last" was default, for others are "1,2,4...". 
        options.addOption("mref","mapping_reference",true,"Set the reference genome for alignment. (Default value is hg38)");
        options.addOption("mbi","mapping_build_index",false,"Build index for the input reference genome. Only needed in the first run.");

        
        //Set Annotation options. 
        options.addOption("ann", "annotation", false, "Open the annotation module.");
        options.addOption("ac", "ann_class", true, "Annotate the results of alignment.");//01:miRNA 02:piRNA 03:tRNA 04:snoRNA 05:snRNA 06:circRNA. 
        options.addOption("aol","ann_overlap",true,"Set the overlap between reads and genes. (Default value is 0.9.)");
        options.addOption("aic","ann_inCluster",false,"Display piRNAs in the clusters. (Default value is false.)");
        options.addOption("atd","ann_threshold",true,"Filter the count of reads. (Default value is 1.)");
        options.addOption("armsm","ann_remove_sam",false,"Remove the sam file with reads mapped to the genome. (Default value is false.)");
        options.addOption("abam","ann_bam",false,"Output bam files for each kind of RNA. (Default value is fasle.)");
        options.addOption("asu","ann_show_unann",false,"Output the alignments without annotations. (Default value is false.)");
        options.addOption("aumi","ann_umi",false,"Use UMI reads for annotation.");
        
        //Set Microbe options. 
        options.addOption("mic","microbe",false,"Open the microbe module.");
        options.addOption("mtool","mic_tool",true,"Choose the microbe profiling tool used. (Default value is Blast.)");//MetaPhlAn, Blast. 
        options.addOption("mdb","mic_database",true,"Set the databases used. (Default value is )");//(10239:viruses|2:bacteria|4751:fungi|2157:archaea)
        
        
        //Set Functional Analysis options. 
        options.addOption("fun", "function", false, "Open the function module.");
            //DEG Analysis.
        options.addOption("fd", "fun_diff_expr", false, "Open the differentially expressed genes analysis.");
        options.addOption("fdclass","fun_diff_class",true,"Set the RNA classes that will be analyzed."); //01:miRNA 02:piRNA 03:tRNA 04:snoRNA 05:snRNA 06:circRNA. 
        options.addOption("fdcase","fun_diff_case",true,"Set the case samples.");
        options.addOption("fdctrl","fun_diff_control",true,"Set the control samples.");
        options.addOption("fdnorm","fun_diff_normalization",true,"Set the method used in normalization.(The default method is cpm.)");
        options.addOption("fdtest","fun_diff_test",true,"Set the statistical test ued in the DEG analysis. (The default test is mwu.)");
        options.addOption("fdmic","fun_diff_mic",false,"Detect the microbal files. (Used when only apply the functional module.)");
        options.addOption("fmtool","fun_mtool",true,"Set the tool(s) used in microbial module.");
        options.addOption("fmdb","fun_mdb",true,"Set the database(s) used in microbial module when blast was applied.");
        options.addOption("fdann","fun_diff_ann",false,"Detect the annotation files.(Used when only apply the functional module.)");
            //Merge Samples.
        options.addOption("fm","fun_merge",false,"Merge all the samples according to differet categories.");
        options.addOption("fms","fun_merge_samples",true,"Set samples to be merged.");
        
            //Set Toolkit options.
        options.addOption("tk","toolkit",false,"Open the toolkit module.");
            //Taxonomy function.
        options.addOption("tax","taxon",false,"Open the taxonomy sub-module.");
        options.addOption("node","nodeFile",true,"Set the node file from NCBI taxonomy database. (ftp://ftp.ncbi.nih.gov/pub/taxonomy/taxdmp.zip)");
        options.addOption("name","nameFile",true,"Set the name file from NCBI taxonomy database. (ftp://ftp.ncbi.nih.gov/pub/taxonomy/taxdmp.zip)");
        options.addOption("a2t", "acc2tax", true, "Set the acc2tax index file for nucleotide sequence from NCBI taxonomy database. (ftp://ftp.ncbi.nih.gov/pub/taxonomy/accession2taxid/nucl_gb.accession2taxid.gz)");
        options.addOption("bs", "blast_seq", true, "Set the blast sequence file from NCBI  database. (ftp://ftp.ncbi.nlm.nih.gov/blast/db/FASTA/nt.gz)");
        options.addOption("taxid","taxonomy_id",true,"Set the taxonomy id of species you want to extract. (10239:viruses|2:bacteria|4751:fungi|2157:archaea|...)");
        options.addOption("taxset","taxid_set",true,"Set the filename of the taxonomy id set, which will be used to extract the acc2id mapping file.");
        options.addOption("extract","extract",true,"Set the content you want to extract from these files. (tree0(id+name) or tree1(id) or tree2(name) or id or acc or seq)");
        options.addOption("out","outPrefix",true,"Set the file prefix for output. (my/file/path/fileprefix)");
        options.addOption("br","blast_result",true,"Set the blast output file. (-outfmt \"7 qacc sacc evalue sstrand bitscore length pident nident qstart qend sstart send\")");
        options.addOption("map2tree",false,"Set the function of mapping the blast results to the tree.");
        options.addOption("nt",true,"Set the number of threads.");
        options.addOption("ts","tree_style",true,"Set the output style of the tree.");
            //Fasta function.
        options.addOption("fa","fasta",true,"Set the input fasta file.");
        options.addOption("out","output",true,"Set the output file.");
        options.addOption("redup","reduplication",false,"Remove the duplicated items in the fasta file.");
        options.addOption("idx","make_index",false,"Build index file and save as object.");
            //Adapter Inquiry.
        options.addOption("adapter","adapter",false,"Look up adapter sequence.");
            //SingleAnn function.
        options.addOption("sa","single_ann",true,"Set one annotation. (e.g. chr2|176150329|176150351|+|hsa-miR-10b-5p|miRBase) ");
        options.addOption("sam","sam_input",true,"Set the input sam file.");
        options.addOption("fq","fq_output",true,"Set the output_sam file.");
        options.addOption("aol","ann_overlap",true,"To set the overlap between reads and genes. (Default value is 1.0.)");
            //Download Resource.
        options.addOption("dr","download_resource",false,"Open the Download Resource toolkit.");
        options.addOption("ck","check",true,"Check the resources listed. If not existed locally, download them on net.");
        options.addOption("lr","list",false,"List all URLs of resources in COMPASS.");
            //ASE function. 
        options.addOption("ase","allele_specific_expression",false,"Open the ASE function.");
        options.addOption("sam","sam_input",true,"Set the input sam/bam file.");
        options.addOption("gm","genome_fasta",true,"Set the input genome reference fasta/fa file.");
        options.addOption("ref","reference_version",true,"Set the reference genome version."); 
        options.addOption("inf","in_files",true,"To read the BAM/SAM files from a file.");
        options.addOption("merge","merge_vt_files",false,"To merge the variant files (*.gvt)");
        options.addOption("gvt","global_vt_files",false,"To output all alleles in the variant file (*.gvt)");
        options.addOption("db","target_db",true,"The database will be used for annotation.");
        options.addOption("out","out_file",true,"To set the output file full name.");
            //ExtractSNV function.
        options.addOption("extractSNV","extract_SNV",false,"Open the extract SNV function.");
        options.addOption("vcf","extract_vcf",true,"The vcf file will be processed.");
        options.addOption("db","target_db",true,"The database will be used for annotation.");
        options.addOption("region","target_region",true,"The GFF3 file with region annotation.");
        options.addOption("ref","ref_genome",true,"The version of reference genome");
        options.addOption("out","out_file",true,"The output file path and name.");     
            //FastAnnotation function.
        options.addOption("FastAnn","Fast_Annotation",false,"Open the fast annotationfunction.");
        options.addOption("in","intput",true,"The file with two columns to be annotated (chrom pos).");
        options.addOption("out","out_file",true,"The output file path and name."); 
        options.addOption("db","target_db",true,"The database will be used for annotation.");
        options.addOption("ref","ref_genome",true,"The version of reference genome");
        options.addOption("gff3","gff3_ann",true,"The GFF3 file with region annotation.");
            //UMI function.
        options.addOption("UMI","Unified_Molecular_Identifier",false,"Open the UMI function.");
        options.addOption("extract_umi","extract_umi",false,"Extract the reads with qualified umi codes.");
        options.addOption("mark_umi","mark_umi",false,"Mark the reads with qualified umi codes.");
        options.addOption("in","intput",true,"The fastq or fastq.gz file as input.");
        options.addOption("adp3p","adapter_3prime",true,"The 3 prime end adapter sequences");
        options.addOption("lumi","length_of_umi_code",true,"The length of UMI code. (The default value is 12.)");
        options.addOption("tol","tolerance",true,"The missing value allowed in the UMI code.(The default value is 2.)");
        options.addOption("out","out_file",true,"The output file path and name.");      
            //Merge function.
        options.addOption("inf","in_files",true,"To read the fastq files from a file.");
        options.addOption("out","out_file",true,"The output file path and name.");
        options.addOption("id","id_column",true,"The column of molecular/species name. (Start from 0)");
        options.addOption("count","count_column",true,"The column of read count. (Start from 0)");
            //Build built-in database.
        options.addOption("build","build_db",false,"Build COMPSRA built-in database.");
        options.addOption("in","input",true,"Annotation files for small RNAs, such as GFF3.");
        options.addOption("db","target_db",true,"The name of annotation databae, such as miRBase.");
        options.addOption("key","key_identifier",true,"The code registered in database_record.obj. ");
        options.addOption("out","output",true,"The full name of the output obj file. This name should be ended with .obj.");
    }
    
    /**
     * This function is used to parse the command line and different processes will be created according to the options. 
     * @param args contains the command line which the user input. 
     */
    public Produce parseOption(String[] args){
        
        CommandLine comm=null;
        try{
            comm=new DefaultParser().parse(options,args);
        }catch(ParseException e){
            e.printStackTrace();
            System.out.println("Options ERROR! Please check them again!\n");
            return null;
        }
        
        //If no parameter input. 
        if(comm.getOptions().length==0){
            HelpFormatter hfHelp=new HelpFormatter();
            hfHelp.printHelp("COMPASS.jar", options);
            return null;
        }

        //The ToolKit module can run independently. 
        if(comm.hasOption("tk")){
            ProduceTK ptk=new ProduceTK();
            //Taxonomy function.
            if(comm.hasOption("tax")){
                Taxonomy tax=new Taxonomy();
                if(comm.hasOption("node")){
                    tax.strNode=comm.getOptionValue("node");
                }else{
                    tax.strNode=Configuration.NT.get("nodes");
                }
                if(comm.hasOption("name")){
                    tax.strName=comm.getOptionValue("name");
                }else{
                    tax.strName=Configuration.NT.get("names");
                }
                if(comm.hasOption("a2t")){
                    tax.strA2T=comm.getOptionValue("a2t");
                }else{
                    tax.strA2T=Configuration.NT.get("A2T");
                }
                if(comm.hasOption("bs")){
                    tax.strBlastSeq=comm.getOptionValue("bs");
                }
                if(comm.hasOption("taxid")){
                    tax.taxid=comm.getOptionValue("taxid");
                }else{
                    tax.taxid="1";
                }
                if(comm.hasOption("taxset")){
                    tax.strTaxID=comm.getOptionValue("taxset");
                }else{
                    tax.strTaxID=null;
                }
                if(comm.hasOption("extract")){
                    tax.extract=comm.getOptionValue("extract");
                }
                if(comm.hasOption("out")){
                    tax.strOut=comm.getOptionValue("out");
                }
                if(comm.hasOption("br")){
                    tax.strBlastResult=comm.getOptionValue("br");
                }
                if(comm.hasOption("map2tree")){
                    tax.map2tree=true;
                }
                if(comm.hasOption("nt")){
                    tax.thread=Integer.valueOf(comm.getOptionValue("nt"));
                }
                if(comm.hasOption("ts")){
                    tax.style=Integer.valueOf(comm.getOptionValue("ts"));
                }
                
                ptk.setTK(tax);               
            }else if(comm.hasOption("fa")){
                Fasta fa=new Fasta();
                if(comm.hasOption("fa")){
                    fa.fa=comm.getOptionValue("fa");
                }
                if(comm.hasOption("out")){
                    fa.strOut=comm.getOptionValue("out");
                }
                if(comm.hasOption("redup")){
                    fa.boolRedup=true;
                }
                if(comm.hasOption("idx")){
                    fa.boolBuildIdx=true;
                }
                
                ptk.setTK(fa);
            }else if(comm.hasOption("adapter")){
                Adapter adp=new Adapter();
                ptk.setTK(adp);
            }else if(comm.hasOption("sa")){
                SingleAnn sa=new SingleAnn();
                
                if(comm.hasOption("sa")){
                    sa.strAnn=comm.getOptionValue("sa");
                }
                if(comm.hasOption("sam")){
                    sa.strSamFile=comm.getOptionValue("sam");
                }
                if(comm.hasOption("fq")){
                    sa.strFQFile=comm.getOptionValue("fq");
                }
                if(comm.hasOption("aol")){
                    sa.fltOverlap=Float.valueOf(comm.getOptionValue("aol"));
                }
                
                ptk.setTK(sa);
            }else if(comm.hasOption("dr")){
                DownloadResource dr=new DownloadResource();
                
                if(comm.hasOption("lr")){
                    dr.boolListResource=true;
                }
                
                if(comm.hasOption("ck")){
                    dr.strResource=comm.getOptionValue("ck");                    
                }

                ptk.setTK(dr);
            }else if(comm.hasOption("ase")){
                CallVariant cv=new CallVariant();
                if(comm.hasOption("sam")){
                    cv.strSAM=comm.getOptionValue("sam");
                }
                if(comm.hasOption("gm")){
                    cv.strFa=comm.getOptionValue("gm");
                }
                if(comm.hasOption("ref")){
                    cv.strRefID=comm.getOptionValue("ref");
                }
                if(comm.hasOption("inf")){
                    cv.strFileList=comm.getOptionValue("inf");
                }
                if(comm.hasOption("merge")){
                    cv.needMerge=true;
                }
                if(comm.hasOption("gvt")){
                    cv.isGVT=true;
                }
                if(comm.hasOption("db")){
                    cv.strDB=comm.getOptionValue("db");
                }
                if(comm.hasOption("out")){
                    cv.strOut=comm.getOptionValue("out");
                }
                ptk.setTK(cv);
            }else if(comm.hasOption("extractSNV")){
                ExtractSNV esnv=new ExtractSNV();
                if (comm.hasOption("vcf")) {
                    esnv.strVCF=comm.getOptionValue("vcf");
                }
                if (comm.hasOption("db")) {
                    esnv.strDB=comm.getOptionValue("db");
                }
                if (comm.hasOption("region")) {
                    esnv.strRegion=comm.getOptionValue("region");
                }
                if (comm.hasOption("ref")) {
                    esnv.strRef=comm.getOptionValue("ref");
                }
                if(comm.hasOption("out")){
                    esnv.strOut=comm.getOptionValue("out");
                }
                ptk.setTK(esnv);
            }else if(comm.hasOption("UMI")){
                UMI eumi=new UMI();
                if(comm.hasOption("in")){
                    eumi.strInput=comm.getOptionValue("in");
                }
                if(comm.hasOption("extract_umi")){
                    eumi.boolExtract=true;
                }
                if(comm.hasOption("mark_umi")){
                    eumi.boolMark=true;
                }
                if(comm.hasOption("adp3p")){
                    eumi.strAdapter=comm.getOptionValue("adp3p");
                }
                if(comm.hasOption("lumi")){
                    eumi.intUMI=Integer.valueOf(comm.getOptionValue("lumi"));
                }
                if(comm.hasOption("tol")){
                    eumi.tolerance=Integer.valueOf(comm.getOptionValue("tol"));
                }
                if(comm.hasOption("out")){
                    eumi.strOutput=comm.getOptionValue("out");
                }
                ptk.setTK(eumi);
            }else if(comm.hasOption("FastAnn")){
                FastAnnotation fa = new FastAnnotation();
                if (comm.hasOption("in")) {
                    fa.strIn = comm.getOptionValue("in");
                }
                if (comm.hasOption("db")) {
                    fa.strDB = comm.getOptionValue("db");
                }
                if (comm.hasOption("ref")) {
                    fa.strRef = comm.getOptionValue("ref");
                }
                if (comm.hasOption("gff3")) {
                    fa.strGFF3 = comm.getOptionValue("gff3");
                }
                if (comm.hasOption("out")) {
                    fa.strOut = comm.getOptionValue("out");
                }
                ptk.setTK(fa);
            } else if (comm.hasOption("merge")) {
                Merge merge = new Merge();
                if (comm.hasOption("inf")) {
                    merge.strFile = comm.getOptionValue("inf");
                }
                if (comm.hasOption("out")) {
                    merge.strOutput = comm.getOptionValue("out");
                }
                if (comm.hasOption("id")) {
                    merge.intKey = Integer.valueOf(comm.getOptionValue("id"));
                }
                if (comm.hasOption("count")) {
                    merge.intCount = Integer.valueOf(comm.getOptionValue("count"));
                }
                ptk.setTK(merge);
            }else if(comm.hasOption("build")){
                BuildDB bdb=new BuildDB();
                if(comm.hasOption("in")){
                    bdb.strIn=comm.getOptionValue("in");
                }
                if(comm.hasOption("out")){
                    bdb.strOut=comm.getOptionValue("out");
                }
                if(comm.hasOption("db")){
                    bdb.strDB=comm.getOptionValue("db");
                }
                if(comm.hasOption("key")){
                    bdb.strKey=comm.getOptionValue("key");
                }
                ptk.setTK(bdb);
            }
            return ptk;
        }        
        
        
        //Parse the common parameters. 
        comParam=new CommonParameter();
        if(comm.hasOption("h")){
            HelpFormatter hfHelp=new HelpFormatter();
            hfHelp.printHelp("COMPASS.jar", options);
            return null;
        }
        
        if(comm.hasOption("in")){
            comParam.input=comm.getOptionValue("in");
            comParam.setInput();
        }else if(comm.hasOption("inf")){
            comParam.input=comm.getOptionValue("inf");
            comParam.setInput(comParam.input);
        }else{
            System.out.println("Input Error! No file is set.");
            return null;
        }
        
        
        if(comm.hasOption("out")){
            comParam.setOutput(comm.getOptionValue("out"));
        }else{
            //The prefix is better to be the same with the input file. 
            comParam.setOutput();
        }
        
        if(comm.hasOption("ref")){
            comParam.setRefGenome(comm.getOptionValue("ref"));
            System.out.println("The "+comParam.strRefGenome+" reference genome was set.");
        }else{
            comParam.setRefGenome("hg38");           
        }
        
        if(comm.hasOption("t")){
            comParam.intThread=Integer.parseInt(comm.getOptionValue("t"));                      
        }else{
            comParam.intThread=1;
        }
        
        if(comm.hasOption("pro")){
            comParam.strPro=comm.getOptionValue("pro");
        }else{
            comParam.strPro="COMPSRA";
        }
        
        if(comm.hasOption("cr")){
            comParam.boolCheckResource=true;
        }
        
        
        //Parse the QualityControl parameters. 
        qc = new QualityControl(comParam);
        if (comm.hasOption("qc")) {
            qc.isOpen = true;
            
            if (comm.hasOption("ra")) {
                qc.boolRmAdapter = true;
                qc.strRmAdapter = comm.getOptionValue("ra");
            } else {
                System.out.println("Adapter Info: COMPASS will not check the adapter!");
            }

            if (comm.hasOption("rh")) {
                qc.boolRmQualityHead = true;
                qc.intRmQualityHead = Integer.parseInt(comm.getOptionValue("rh"));
            }

            if (comm.hasOption("rt")) {
                qc.boolRmQualityTail = true;
                qc.intRmQualityTail = Integer.parseInt(comm.getOptionValue("rt"));
            }

            if (comm.hasOption("rr")) {
                qc.boolRmQualityRead = true;
                qc.intRmQualityRead = Integer.parseInt(comm.getOptionValue("rr"));
            }

            if (comm.hasOption("rhh")) {
                qc.boolRmBaseHead = true;
                qc.intRmBaseHead = Integer.parseInt(comm.getOptionValue("rhh"));
            }

            if (comm.hasOption("rth")) {
                qc.boolRmBaseTail = true;
                qc.intRmBaseTail = Integer.parseInt(comm.getOptionValue("rth"));
            }

            if (comm.hasOption("rlh")) {
                qc.boolRmLengthRead = true;
                qc.strRmLengthRead = comm.getOptionValue("rlh");
            }

            if (comm.hasOption("rb")) {
                qc.boolRmBias = true;
                qc.intRmBias = Integer.parseInt(comm.getOptionValue("rb"));
            }
        } else {
            System.out.println("QC module will not be performed.");
        }


        //Parse the Alignment parameters. 
        align = new Alignment(comParam);
        if (comm.hasOption("aln")) {
            align.isOpen = true;
            
            if (comm.hasOption("mt")) {
                align.strAlignTool = comm.getOptionValue("mt");
            } else {
                System.out.println("Alignment Info: The STAR will be set in default.");
            }
            
            if(comm.hasOption("mp")){
                align.strParam=comm.getOptionValue("mp");
            }
            
            if(comm.hasOption("mref")){
                comParam.strRefGenome=align.strRefGenome=comm.getOptionValue("mref");               
            }else{
                align.strRefGenome=comParam.strRefGenome;
            }
            System.out.println("The "+comParam.strRefGenome+" was set in default.");

            if(comm.hasOption("midx")){
                align.strIndex=comm.getOptionValue("midx");
            }else{
                align.strIndex="last";
            }
            
            if(comm.hasOption("mbi")){
                align.needIndex=true;
            }
//            if (comm.hasOption("mr")) {
//                align.strRefGenome = comm.getOptionValue("mr");
//            } else {
//                LOG.info("Reference Genome Info: The hg38 will be set in default.");
//            }

//            if (comm.hasOption("mend")) {
//                align.boolEndogenous = true;
////            align.strEndogenous=comm.getOptionValue("mend");
//            } else {
//                LOG.info("Alignment Info: The endogenous alignment will not be performed.");
////            LOG.info("Alignemeng Info: The databases 1-6 are used in default.");
//            }

        } else {
            System.out.println("Alignment module will not be performed.");
        }     
        

//        if(comm.hasOption("mexo")){
//            align.boolExogenous=true;
//            align.strEndogenous=comm.getOptionValue("mexo");
//        }else{
//            LOG.info("Alignment Info: The exogenous alignment will not be performed.");
//        }
        
        //Parse the Annotation parameters. 
        ann = new Annotation(comParam);
        if (comm.hasOption("ann")) {
            ann.isOpen = true;
            
            if (comm.hasOption("ac")) {
                ann.strAnn = comm.getOptionValue("ac");
            } else {
                System.out.println("The code for annotation is " + ann.strAnn + ".");
            }
            
            if(comm.hasOption("aol")){
                ann.fltOverlap=Float.valueOf(comm.getOptionValue("aol"));
            }
            if(comm.hasOption("aic")){
                ann.isInCluster=true;
            }
            if(comm.hasOption("atd")){
                ann.intTd=Integer.valueOf(comm.getOptionValue("atd"));
            }
            if(comm.hasOption("armsm")){
                ann.boolRmSamMap=true;
            }
            if(comm.hasOption("abam")){
                ann.needBAMOutput=true;
            }
            if(comm.hasOption("asu")){
                ann.boolShowUnAnn=true;
            }
            if(comm.hasOption("aumi")){
                ann.useUMI=true;
            }
            
        } else {
            System.out.println("Annotation module will not be performed.");
        }
        
        //Parse the Microbe parameters.
        mic=new Microbe(comParam);
        if(comm.hasOption("mic")){
            mic.isOpen=true;
            
            if(comm.hasOption("mtool")){
                mic.strTool=comm.getOptionValue("mtool");
            }
            if(comm.hasOption("mdb")){
                mic.strBlastDB=comm.getOptionValue("mdb");
            }
            
        }else{
            System.out.println("Microbe module will not be performed.");
        }
        
        //Parse the Functional Analysis parameters.
        fun = new Function(comParam);
        if (comm.hasOption("fun")) {
            fun.isOpen = true;
            
            if (comm.hasOption("fd")) {
                fun.boolDEG = true;
                
                if(comm.hasOption("fdclass")){
                    fun.strClass=comm.getOptionValue("fdclass");
                }else{
                    if(comm.hasOption("ac"))    fun.strClass=comm.getOptionValue("ac");
                }
                
                if(comm.hasOption("fdcase")){
                    fun.strCase=comm.getOptionValue("fdcase");
                }
                
                if(comm.hasOption("fdctrl")){
                    fun.strCtrl=comm.getOptionValue("fdctrl");
                }
                
                if(comm.hasOption("fdnorm")){
                    fun.strNorm=comm.getOptionValue("fdnorm");
                }
                
                if(comm.hasOption("fdtest")){
                    fun.strTest=comm.getOptionValue("fdtest");
                }
                
                if(comm.hasOption("fdmic")){
                    fun.boolMic=true;
                }

                if (comm.hasOption("fmtool")) {
                    fun.strTool = comm.getOptionValue("fmtool");
                }
                if (comm.hasOption("fmdb")) {
                    fun.strBlastDB = comm.getOptionValue("fmdb");
                }
                
                if(comm.hasOption("fdann")){
                    fun.boolAnn=true;
                }
                
            } 
            
            if(comm.hasOption("fm")){
                fun.boolMerge=true;
                
                if(comm.hasOption("fms")){
                    fun.strCase=comm.getOptionValue("fms");
                }                
                
                if(comm.hasOption("fdclass")){
                    fun.strClass=comm.getOptionValue("fdclass");
                }else{
                    if(comm.hasOption("ac"))    fun.strClass=comm.getOptionValue("ac");
                }
                
                if(comm.hasOption("fdmic")){
                    fun.boolMic=true;
                }

                if (comm.hasOption("fmtool")) {
                    fun.strTool = comm.getOptionValue("fmtool");
                }
                if (comm.hasOption("fmdb")) {
                    fun.strBlastDB = comm.getOptionValue("fmdb");
                }
                
                if(comm.hasOption("fdann")){
                    fun.boolAnn=true;
                }                
            }
        } else {
            System.out.println("Function module will not be performed.");
        }
         
        
        //Return the process
        return(new Produce(qc,align,ann,mic,fun));
    }
    
}
