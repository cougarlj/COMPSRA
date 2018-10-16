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
import edu.harvard.channing.compass.entity.Adapter;
import edu.harvard.channing.compass.entity.CommonParameter;
import edu.harvard.channing.compass.toolkit.DownloadResource;
import edu.harvard.channing.compass.toolkit.Fasta;
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
        options.addOption("h", "help", false, "To display the help info.");
        options.addOption("in", "input",true,"To set the input file (.fastq)."); //The text, gz, bgzip format are supported. 
        options.addOption("out", "output", true, "To set the output directory and prefix of output file.");//The prefix of output file is supported. 
        options.addOption("t", "threads",true, "To set the multithreads mode.");
        options.addOption("ref", "ref_genome", true, "To set the reference genome.");
        options.addOption("pro","project_name",true, "Set the project name.");
        options.addOption("inf","in_file",true,"To read the sample name from a file.");
        options.addOption("cr","check_resource",false,"To check the resource needed and download online if not existed.");
        
        
        //Set Quality Control options. 
        options.addOption("qc","quality_control",false,"TO open the quality control module.");
        options.addOption("ra", "rm_adapter", true, "To remove adapters.");
        options.addOption("rb", "rm_bias", true, "To remove the randomized bases at the ligation junctions.");
        options.addOption("rh", "rm_low_quality_head",true, "To remove bases with low quality from head.");
        options.addOption("rt", "rm_low_quality_tail", true, "To remove bases with low quality from tail.");
        options.addOption("rr", "rm_low_quality_read", true, "To remove reads with low quality.");
        
        options.addOption("rhh", "rm_head_hard", true, "To remove N bases of the read directly from the head.");
        options.addOption("rth", "rm_tail_hard", true, "To remove N bases of the read directly from the tail.");
        options.addOption("rlh", "rm_read_hard", true, "To remove read with the length smaller(<) than N. ");
        
                
        //Set Alignment options.
        options.addOption("aln", "alignment", false, "To open the alignment module.");
        options.addOption("mt", "mapping_tool", true, "To set the alignment tool used to map reads to the reference genome."); //The default tool for alignment is star.
        options.addOption("mp","mapping_param",true,"To set the main parameters of the aligner.");
        options.addOption("midx","mapping_index",true,"To choose the files");//"last" was default, for others are "1,2,4...". 
        options.addOption("mref","mapping_reference",true,"To set the reference genome for alignment.");

        
        //Set Annotation options. 
        options.addOption("ann", "annotation", false, "To open the annotation module.");
        options.addOption("ac", "ann_class", true, "To annotate the results of alignment.");//01:miRNA 02:piRNA 03:tRNA 04:snoRNA 05:snRNA 06:circRNA. 
        options.addOption("aol","ann_overlap",true,"To set the overlap between reads and genes. (Default value is 0.9.)");
        options.addOption("aic","ann_inCluster",false,"To display piRNAs in the clusters. (Default value is false.)");
        options.addOption("atd","ann_threshold",true,"To filter the count of reads. (Default value is 1.)");
        options.addOption("armsm","ann_remove_sam",false,"To remove the sam file with reads mapped to the genome. (Default value is false.)");
        options.addOption("abam","ann_bam",false,"To outptu bam files for each kind of RNA. (Default value is fasle.)");
        
        //Set Microbe options. 
        options.addOption("mic","microbe",false,"To open the microbe module.");
        options.addOption("mtool","mic_tool",true,"To choose the microbe profiling tool used.");//MetaPhlAn, Blast. 
        options.addOption("mdb","mic_database",true,"To set the databases used.");//(10239:viruses|2:bacteria|4751:fungi|2157:archaea)
        
        
        //Set Functional Analysis options. 
        options.addOption("fun", "function", false, "To open the function module.");
            //DEG Analysis.
        options.addOption("fd", "fun_diff_expr", false, "To open the differentially expressed genes analysis.");
        options.addOption("fdclass","fun_diff_class",true,"To set the RNA classes that will be analyzed."); //01:miRNA 02:piRNA 03:tRNA 04:snoRNA 05:snRNA 06:circRNA. 
        options.addOption("fdcase","fun_diff_case",true,"To set the case samples.");
        options.addOption("fdctrl","fun_diff_control",true,"To set the control samples.");
        options.addOption("fdnorm","fun_diff_normalization",true,"To set the method used in normalization.");
        options.addOption("fdtest","fun_diff_test",true,"To set the statistical test ued in the DEG analysis.");
        options.addOption("fdmic","fun_diff_mic",false,"To detect the microbal files. (Used when only apply the functional module.)");
        options.addOption("fmtool","fun_mtool",true,"To set the tool(s) used in microbial module.");
        options.addOption("fmdb","fun_mdb",true,"To set the database(s) used in microbial module when blast was applied.");
        options.addOption("fdann","fun_diff_ann",false,"To detect the annotation files.(Used when only apply the functional module.)");
            //Merge Samples.
        options.addOption("fm","fun_merge",false,"To merge all the samples according to differet categories.");
        options.addOption("fms","fun_merge_samples",true,"Set samples to be merged.");
        
        //Set Toolkit options.
        options.addOption("tk","toolkit",false,"To open the toolkit module.");
            //Taxonomy function.
        options.addOption("tax","taxon",false,"To open the taxonomy sub-module.");
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
        options.addOption("adp","adapter",false,"Look up adapter sequence.");
            //SingleAnn function.
        options.addOption("sa","single_ann",true,"Set one annotation. (e.g. chr2|176150329|176150351|+|hsa-miR-10b-5p|miRBase) ");
        options.addOption("sam","sam_input",true,"Set the input sam file.");
        options.addOption("fq","fq_output",true,"Set the output_sam file.");
        options.addOption("aol","ann_overlap",true,"To set the overlap between reads and genes. (Default value is 1.0.)");
            //Download Resource.
        options.addOption("dr","download_resource",false,"Open the Download Resource toolkit.");
        options.addOption("ck","check",true,"Check the resources listed. If not existed locally, download them on net.");
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
            }else if(comm.hasOption("adp")){
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
                
                if(comm.hasOption("ck")){
                    dr.strResource=comm.getOptionValue("ck");                    
                }

                ptk.setTK(dr);
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
            System.out.println("The "+comParam.strRefGenome+" is set.");
        }else{
            comParam.setRefGenome("hg38");           
        }
        
        if(comm.hasOption("t")){
            comParam.intThread=Integer.parseInt(comm.getOptionValue("t"));                      
        }else{
            comParam.intThread=1;
//            System.out.println("Thread Info: The machine has "+Runtime.getRuntime().availableProcessors()+" CPUs.");          
        }
        
        if(comm.hasOption("pro")){
            comParam.strPro=comm.getOptionValue("pro");
        }else{
            comParam.strPro="COMPASS";
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
