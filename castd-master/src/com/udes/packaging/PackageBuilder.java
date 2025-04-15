package com.udes.packaging;

import com.udes.model.il.terms.Bool;
import com.udes.model.astd.items.Domain;
import com.udes.model.astd.items.Variable;
import com.udes.model.il.conventions.Conventions;
import com.udes.parser.ASTDParser;
import com.udes.translator.ILTranslator;
import com.udes.utils.*;

import java.io.*;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PackageBuilder {
    public enum TargetCompiler {
        GCC,
        JAVAC,
        LLVM
        //more
    }
    private String targetCode;
    private ILTranslator.Lang targetLang;
    private String currOS;
    private String targetDir;
    private String libDir;
    private String includeDir;
    private TargetCompiler targetCompiler;
    private boolean hasDifferentLocation;

    public PackageBuilder(String targetCode, ILTranslator.Lang targetLang, String targetDir, String libDir, String includeDir) {
         this.targetCode = targetCode;
         this.targetLang = targetLang;
         this.currOS = Utils.getOperatingSystemType();
         this.targetDir = targetDir;
         this.libDir = libDir;
         this.includeDir = includeDir;
         // first clean the output folder
         cleanAll();
         //only these two are supported for now
         if(targetLang == ILTranslator.Lang.CPP)
             targetCompiler = TargetCompiler.GCC;
         if(targetLang == ILTranslator.Lang.JAVA)
             targetCompiler = TargetCompiler.JAVAC;

         hasDifferentLocation = false;
    }

    public void build(Bool timed) {
        generateJSONLibraryIfNecessary();
        generateHelperFile(timed);
        generateMainFile();
        copyUserFileToBuildDir();
        if(targetLang == ILTranslator.Lang.CPP)
            generateCPPMakefile();
        if(targetLang == ILTranslator.Lang.JAVA)
            generateJavaMakefile();
        generateExecutableCode();
    }

    private void cleanAll() {
        StringBuilder out = new StringBuilder();
        // make clean all
        out.append(Constants.MAKE).append(" ").append(Constants.CLEAN);
        Process proc;
        try {
            proc = Runtime.getRuntime().exec(out.toString(), null, new File(targetDir));
            proc.waitFor();
            catchErrors(proc);
        }
        catch(Exception e) { e.printStackTrace(); }
    }


    private StringBuilder helperASTDParameters(String in)
    {
        String res = in;
        StringBuilder p0, p1, p2, p3, p4, p5, outRes;
        p0 = new StringBuilder(); p1 = new StringBuilder();
        p2 = new StringBuilder(); p3 = new StringBuilder();
        p4 = new StringBuilder(); p5 = new StringBuilder(); outRes = new StringBuilder();

        //if(ASTDParser.parameters != null && !ASTDParser.hasCallASTD) { -> Ancient if from LIONEL
        //was wrong because would not see if we had a callASTD
        if(ASTDParser.parameters != null) {
            int count = 0;
            for (Variable v : ASTDParser.parameters) {
                count++;
                p0.append(Constants.P0.replace(ILTranslator.USYMBOL_1, String.valueOf(count)));
                p1.append(Constants.P1.replace(ILTranslator.USYMBOL_1, v.getName()));
                p4.append(Constants.P4.replace(ILTranslator.USYMBOL_1, v.getName()));
                p5.append(Constants.P5.replace(ILTranslator.USYMBOL_1, v.getName()).replace(ILTranslator.USYMBOL_5, String.valueOf(count)));

                String type = v.getType();
                if (!type.contains(Conventions.INT)
                        && !type.contains(Conventions.STRING)
                        && !type.contains(Conventions.BOOL_TYPE1)
                        && !type.contains(Conventions.DOUBLE)
                        && !type.contains(Conventions.FLOAT)
                        && !type.contains(Conventions.SHORT)
                        && !type.contains(Conventions.LONG)) {
                    if (v.getInit() == null) {
                        String[] output = generateParamsUnboundedDomain(v, Utils.capitalize(v.getType()), "_type", count);
                        p2.append(output[0]); p3.append(output[1]);
                    } else {
                        Domain dom = (Domain) v.getInit();
                        List lst = new ArrayList(dom.createDomain());
                        if (lst != null && !lst.isEmpty()) {
                            String[] output = generateParamsFiniteDomain(v, dom, Utils.capitalize(v.getType()), "_type", count);
                            p2.append(output[0]); p3.append(output[1]); p3.append(output[2]);
                        }
                        else {
                            String[] output = generateParamsUnboundedDomain(v, Utils.capitalize(v.getType()), "_type", count);
                            p2.append(output[0]); p3.append(output[1]);
                        }
                    }
                } else {
                    if (v.getInit() == null) {
                        String[] output = generateParamsUnboundedDomain(v, v.getType(), "", count);
                        p2.append(output[0]);
                        p3.append(output[1]);
                    } else {
                        Domain dom = (Domain) v.getInit();
                        List lst = new ArrayList(dom.createDomain());
                        if (lst != null && !lst.isEmpty()) {
                            String[] output = generateParamsFiniteDomain(v, dom, v.getType(), "", count);
                            p2.append(output[0]); p3.append(output[1]); p3.append(output[2]);
                        }
                        else {
                            String[] output = generateParamsUnboundedDomain(v, v.getType(), "", count);
                            p2.append(output[0]); p3.append(output[1]);
                        }
                    }
                }
            }
            outRes.append(res.replace("$P0", p0.toString())
                    .replace("$P1", p1.toString())
                    .replace("$P2", p2.toString())
                    .replace("$P3", p3.toString())
                    .replace("$P4", p4.toString())
                    .replace("$P5", p5.toString()));
        }
        else {
            outRes = new StringBuilder(res.replace("$P0", "")
                    .replace("$P1", "")
                    .replace("$P2", "")
                    .replace("$P3", "")
                    .replace("$P4", p4.toString())
                    .replace("$P5", p5.toString()));
        }

        return outRes;
    }

    private String[] generateParamsUnboundedDomain(Variable v, String type, String suffix, int count)
    {
        String[] res = new String[2];
        if(type.equals("string")){
            res[0] = Constants.P2_UNBOUNDED_DOM
                    .replace(ILTranslator.USYMBOL_1, v.getName())
                    .replace(ILTranslator.USYMBOL_5, String.valueOf(count))
                    .replace(ILTranslator.USYMBOL_4, "str"+ suffix);
            res[1] = Constants.P3.replace(ILTranslator.USYMBOL_4, "std::string")
                    .replace(ILTranslator.USYMBOL_1, v.getName());
        }
        else{
            res[0] = Constants.P2_UNBOUNDED_DOM
                    .replace(ILTranslator.USYMBOL_1, v.getName())
                    .replace(ILTranslator.USYMBOL_5, String.valueOf(count))
                    .replace(ILTranslator.USYMBOL_4, type + suffix);
            res[1] = Constants.P3.replace(ILTranslator.USYMBOL_4, type)
                    .replace(ILTranslator.USYMBOL_1, v.getName());
        }

        return res;
    }

    private String[] generateParamsFiniteDomain(Variable v, Domain dom, String type, String suffix, int count)
    {
        String[] res = new String[3];
        if(type.equals("string")){
            res[0] = Constants.P2_DOM
                    .replace(ILTranslator.USYMBOL_1, v.getName())
                    .replace(ILTranslator.USYMBOL_5, String.valueOf(count))
                    .replace(ILTranslator.USYMBOL_2, "Dom_" + v.getName())
                    .replace(ILTranslator.USYMBOL_4, "str" + suffix);
            res[1] = Constants.P3.replace(ILTranslator.USYMBOL_4, "std::string")
                    .replace(ILTranslator.USYMBOL_1, v.getName());
            res[2] = Constants.P3_DOM.replace(ILTranslator.USYMBOL_4, "std::string")
                    .replace(ILTranslator.USYMBOL_2, "Dom_" + v.getName())
                    .replace(ILTranslator.USYMBOL_3, dom.toString());
        }
        else {
            res[0] = Constants.P2_DOM
                    .replace(ILTranslator.USYMBOL_1, v.getName())
                    .replace(ILTranslator.USYMBOL_5, String.valueOf(count))
                    .replace(ILTranslator.USYMBOL_2, "Dom_" + v.getName())
                    .replace(ILTranslator.USYMBOL_4, type + suffix);
            res[1] = Constants.P3.replace(ILTranslator.USYMBOL_4, type)
                    .replace(ILTranslator.USYMBOL_1, v.getName());
            res[2] = Constants.P3_DOM.replace(ILTranslator.USYMBOL_4, type)
                    .replace(ILTranslator.USYMBOL_2, "Dom_" + v.getName())
                    .replace(ILTranslator.USYMBOL_3, dom.toString());
        }
        return res;
    }

    private void generateFiles(String out, Bool timed)
    {
        if(targetLang == ILTranslator.Lang.CPP) {
            Utils.writeFile(out, targetDir + File.separator
                    + Constants.HELPER + Constants.CPP_HDR_EXTENSION);
            if(Constants.DEBUG) {
                Utils.writeFile(Constants.LOGGER_BLOCK, targetDir + File.separator
                        + Constants.LOGGER + Constants.CPP_HDR_EXTENSION);
            }
            if(Constants.EXEC_STATE_ACTIVATED) {
                Utils.writeFile(Constants.CLIENT_BLOCK, targetDir + File.separator
                        + Constants.CLIENT + Constants.CPP_HDR_EXTENSION);
                Utils.writeFile(Constants.EXEC_SCHEMA_HDR + Constants.EXEC_SCHEMA_PROPS
                        + Constants.ASTD_PROPS_MAPPING
                        + Constants.EXEC_SCHEMA_END, targetDir + File.separator
                        + Constants.EXEC_SCHEMA + Constants.CPP_HDR_EXTENSION);
            }
//            if(timed.getValue() && !Constants.TIMED_SIMULATION){
//                //lightweightsemaphore
//                Utils.writeFile(Queue.lightweightsemaphore, targetDir + File.separator
//                        + Constants.SEMAPHORE + Constants.CPP_HDR_EXTENSION);
//                //concurrentqueue
//                Utils.writeFile(Queue.concurrentqueue + Queue.concurrentqueue2 + Queue.concurrentqueue3 + Queue.concurrentqueue4, targetDir + File.separator
//                        + Constants.CONCURRENTQUEUE + Constants.CPP_HDR_EXTENSION);
//                //blockingconcurrentqueue
//                Utils.writeFile(Queue.blockingconcurrentqueue, targetDir + File.separator
//                        + Constants.BLOCKINGQUEUE + Constants.CPP_HDR_EXTENSION);
//            }
            if(currOS.contains("Windows")){
                Utils.writeFile(getopt4windows.getopt4windows, targetDir + File.separator + "getopt" + Constants.CPP_HDR_EXTENSION);
            }
        }
        if(targetLang == ILTranslator.Lang.JAVA) {
            Utils.writeFile(targetCode, targetDir + File.separator
                    + Constants.HELPER
                    + Constants.JAVA_FILE_EXTENSION);
            if(Constants.DEBUG) {
                Utils.writeFile(targetCode, targetDir + File.separator
                        + Constants.LOGGER
                        + Constants.JAVA_FILE_EXTENSION);
            }
            if(Constants.EXEC_STATE_ACTIVATED) {
                Utils.writeFile(targetCode, targetDir + File.separator
                        + Constants.CLIENT + Constants.JAVA_FILE_EXTENSION);
                Utils.writeFile(targetCode, targetDir + File.separator
                        + Constants.EXEC_SCHEMA + Constants.JAVA_FILE_EXTENSION);
            }
        }
        System.out.println("[stage 1] Helper code generation succeeded ...");
    }

    private String helperComplexTypes(Bool timed) {
        StringBuilder out = new StringBuilder();
        //helper header
        if(ASTDParser.hasComplexType
                || Constants.EVT_FORMAT.equals(Constants.JSON_FORMAT)
                || Constants.EVT_FORMAT.equals(Constants.ALL)) {
            if(Constants.EXEC_STATE_ACTIVATED) {
                out.append(Constants.HELPER_DEFINES)
                        .append(Constants.HELPER_USERTYPE_INCLUDES
                                .replace(Constants.HELPER_MARKER, Constants.CLIENT_HEADER_INCLUDES));
            }
            else {
                out.append(Constants.HELPER_DEFINES)
                        .append(Constants.HELPER_USERTYPE_INCLUDES.replace(Constants.HELPER_MARKER, ""));
            }
        }
        else {
            if(Constants.EXEC_STATE_ACTIVATED) {
                out.append(Constants.HELPER_DEFINES)
                        .append(Constants.HELPER_DEFAULT_INCLUDES
                                .replace(Constants.HELPER_MARKER, Constants.CLIENT_HEADER_INCLUDES));
            }
            else {
                out.append(Constants.HELPER_DEFINES)
                        .append(Constants.HELPER_DEFAULT_INCLUDES.replace(Constants.HELPER_MARKER, ""));
            }
        }
        if(ASTDParser.hasComplexType)
            out.append(Constants.HELPER_HASMETHOD_BLOCK);
        // begin
        out.append(Constants.HELPER_TYPES_BEGIN);
        //helper types
        out.append(Constants.HELPER_GET_PRIM_TYPE_METHODS);
        //add get json method for complex type
        if(ASTDParser.hasComplexType) {
            if(ASTDParser.type_defs != null) {
                ASTDParser.type_defs.forEach(it ->
                        out.append(Constants.HELPER_GET_USER_TYPE_METHOD.replace(ILTranslator.USYMBOL_1, it)));
            }
        }
        //end
        out.append(Constants.HELPER_TYPES_END);

        return out.toString();
    }

    private String helperStateViz() {
        StringBuilder out = new StringBuilder();
        if(Constants.EXEC_STATE_ACTIVATED) {
            if(Constants.EVT_FORMAT.equals(Constants.JSON_FORMAT)) {
                out.append(Constants.HELPER_EVENT_BLOCK_TEMPLATE.replace("$F1", "")
                        .replace("$F2", Constants.F2_JSON).replace("$F3", Constants.F3_JSON)
                        .replace("$F0", "").replace("$V1", Constants.V1)
                        .replace("$V2", Constants.V2).replace("$V3", Constants.V3)
                        .replace("$F4", "").replace("$V4", Constants.V4)
                        .replace("$F5", "").replace("$F6", "")
                        .replace("$V5", Constants.V5).replace("$V6", Constants.V6)
                        .replace("$F7", Constants.F7_JSON));
            }
            else if(Constants.EVT_FORMAT.equals(Constants.SHORTHANDEVENTS_FORMAT)) {
                out.append(Constants.HELPER_EVENT_BLOCK_TEMPLATE.replace("$F1", "")
                        .replace("$F2", Constants.F2_SHORTHAND).replace("$F3", Constants.F3_SHORTHAND)
                        .replace("$F0", "").replace("$V1", Constants.V1)
                        .replace("$V2", Constants.V2).replace("$V3", Constants.V3)
                        .replace("$F4", "").replace("$V4", Constants.V4)
                        .replace("$F5", "").replace("$F6", "")
                        .replace("$V5", Constants.V5).replace("$V6", Constants.V6)
                        .replace("$F7", Constants.F7_SHORTHAND));
            }
            else {
                out.append(Constants.HELPER_EVENT_BLOCK_TEMPLATE.replace("$F1", Constants.F1_ALL)
                        .replace("$F2", Constants.F2_ALL).replace("$F3", Constants.F3_ALL)
                        .replace("$F0", Constants.F0).replace("$V1", Constants.V1)
                        .replace("$V2", Constants.V2).replace("$V3", Constants.V3)
                        .replace("$F4", Constants.F4).replace("$V4", Constants.V4)
                        .replace("$F5", Constants.F5).replace("$F6", Constants.F6)
                        .replace("$V5", Constants.V5).replace("$V6", Constants.V6)
                        .replace("$F7", Constants.F7_ALL));
            }
        }
        else {
            if(Constants.EVT_FORMAT.equals(Constants.JSON_FORMAT)) {
                out.append(Constants.HELPER_EVENT_BLOCK_TEMPLATE.replace("$F1", "")
                        .replace("$F2", Constants.F2_JSON).replace("$F3", Constants.F3_JSON)
                        .replace("$F0", "").replace("$V1", "")
                        .replace("$V2", "").replace("$V3", "")
                        .replace("$F4", "").replace("$V4", "")
                        .replace("$F5", "").replace("$F6", "")
                        .replace("$V5", "").replace("$V6", "")
                        .replace("$F7", Constants.F7_JSON));
            }
            else if(Constants.EVT_FORMAT.equals(Constants.SHORTHANDEVENTS_FORMAT)) {
                out.append(Constants.HELPER_EVENT_BLOCK_TEMPLATE.replace("$F1", "")
                        .replace("$F2", Constants.F2_SHORTHAND).replace("$F3", Constants.F3_SHORTHAND)
                        .replace("$F0", "").replace("$V1", "")
                        .replace("$V2", "").replace("$V3", "")
                        .replace("$F4", "").replace("$V4", "")
                        .replace("$F5", "").replace("$F6", "")
                        .replace("$V5", "").replace("$V6", "")
                        .replace("$F7", Constants.F7_SHORTHAND));
            }
            else {
                out.append(Constants.HELPER_EVENT_BLOCK_TEMPLATE.replace("$F1", Constants.F1_ALL)
                        .replace("$F2", Constants.F2_ALL).replace("$F3", Constants.F3_ALL)
                        .replace("$F0", Constants.F0).replace("$V1", "")
                        .replace("$V2", "").replace("$V3", "")
                        .replace("$F4", Constants.F4).replace("$V4", "")
                        .replace("$F5", Constants.F5).replace("$F6", Constants.F6)
                        .replace("$V5", "").replace("$V6", "")
                        .replace("$F7", Constants.F7_ALL));
            }
        }

        return out.toString();
    }


    private void generateHelperFile(Bool timed) {
        StringBuilder out = new StringBuilder();
        //add chrono et ctime on includes
        if(timed.getValue()){
            out.append(includeForClock());
        }
        //helper complex types
        out.append(helperComplexTypes(timed));
        // helper state viz
        out.append(helperStateViz());
        // helper Clock Parameters
        if(timed.getValue() || Constants.TIMED_SIMULATION){
            out.append(helperClockClass());
        }
        // helper ASTD Parameters
        String in = out.toString();
        out = helperASTDParameters(in);

        // out client, exec_schema. and helper files
        generateFiles(out.toString(), timed);
    }

    private String helperClockClass(){
        StringBuilder out = new StringBuilder();
        if(Constants.TIMED_SIMULATION){
            out.append(Constants.HELPER_CLOCK_SIMULATION);
        }
        else{
            out.append(Constants.HELPER_CLOCK);
        }
        return out.toString();
    }

    private String includeForClock(){
        StringBuilder out = new StringBuilder();
        out.append(Constants.INCLUDE_CLOCK);
        return out.toString();
    }

    private void generateMainFile() {
        if(targetLang == ILTranslator.Lang.CPP)
            Utils.writeFile(targetCode, targetDir + File.separator
                    + Constants.DEFAULT_ASTD_NAME.toLowerCase()
                    + Constants.CPP_FILE_EXTENSION);
        if(targetLang == ILTranslator.Lang.JAVA)
            Utils.writeFile(targetCode, targetDir + File.separator
                    + Constants.DEFAULT_ASTD_NAME.toLowerCase()
                    + Constants.JAVA_FILE_EXTENSION);
        System.out.println("[stage 2] Main code generation succeeded ...");
    }


    private void copyUserFileToBuildDir() {
        copyUserFileToBuildDir(Constants.CLIENT_HDR_FILES, Constants.CPP_HDR_EXTENSION);
        copyUserFileToBuildDir(Constants.CLIENT_HPP_FILES, Constants.CPP_HDR2_EXTENSION);
        copyUserFileToBuildDir(Constants.CLIENT_CPP_FILES, Constants.CPP_FILE_EXTENSION);
    }

    private void copyUserFileToBuildDir(List<String> refFiles, String fileExt)
    {
        refFiles.forEach(chf -> {
            String client_path = Constants.CURRENT_PATH + File.separator + chf + fileExt;
            File f = new File(client_path);
            if(Constants.CURRENT_PATH.equals(f.getParent())) {
                String out = Utils.readFile(client_path);
                Utils.writeFile(out, targetDir + File.separator + chf + fileExt);
            }
            else {
                String client_dir = (chf + fileExt).replace(File.separator + f.getName(), "");
                Utils.copyFolder(new File(Constants.CURRENT_PATH + File.separator + client_dir),
                                 new File(targetDir + File.separator + client_dir));
                hasDifferentLocation = true;
            }
        });
    }

    private void generateCPPMakefile() {
        StringBuilder out = new StringBuilder();
        //variables
        out.append(varOpts());
        //includes
        out.append(includeOpts());
        //clean all
        out.append(cleanAllOpts());
        // header and object definitions
        out.append(headObjDefOpts());
        // compile operation
        out.append(compileOpts());
        //clean operation
        out.append(cleanOpts());
        //generate make file
        Utils.writeFile(out.toString(), targetDir + File.separator + Constants.MAKEFILENAME);
        System.out.println("[stage 3] Makefile successfully generated ...");
    }

    private String varOpts()
    {
        StringBuilder out = new StringBuilder();
        out.append(Constants.TARGET).append(" = ")
                .append(Constants.DEFAULT_ASTD_NAME.toLowerCase()).append("\n")
                .append(Constants.CC).append(" = ")
                .append(Constants.GCC).append("\n")
                .append(Constants.CFLAGS).append(" = ")
                .append(Constants.OPTMIZATION_OPTS).append("\n");

        return out.toString();
    }

    private String includeOpts()
    {
        StringBuilder out = new StringBuilder();
        // includes
        if(includeDir != null) {
            out.append(Constants.INCLUDE).append(" = ")
                    .append(Constants.GCC_INCLUDE_OPTS).append(includeDir).append("\n");
        }

        // include libraries
        if(libDir != null) {
            out.append(Constants.LDLIBS).append(" = ")
                    .append(Constants.GCC_LDFLAGS_OPTS).append(libDir).append("\n");
        }
        out.append("\n");

        return out.toString();
    }

    private String cleanAllOpts()
    {
        StringBuilder out = new StringBuilder();
        out.append(Constants.PHONY).append(": ")
                .append(Constants.CLEAN_ALL).append("\n")
                .append(Constants.DEFAULT).append(": ")
                .append(Constants.TARGET_VARS).append("\n")
                .append(Constants.ALL).append(": ")
                .append(Constants.DEFAULT).append("\n\n");

        return out.toString();
    }

    private String headObjDefOpts()
    {
        StringBuilder out = new StringBuilder();
        if(!Constants.CLIENT_CPP_FILES.isEmpty()) {
            out.append(Constants.HEADERS).append(" =");
            if(!Constants.CLIENT_HDR_FILES.isEmpty()) {
                if(hasDifferentLocation) {
                    for(int i = 0 ; i < Constants.CLIENT_CPP_FILES.size(); i++)
                        out.append(" ").append(Constants.CLIENT_CPP_FILES.get(i)).append(Constants.CPP_HDR_EXTENSION);
                }
            }
            if(!Constants.CLIENT_HPP_FILES.isEmpty()) {
                if(hasDifferentLocation) {
                    for(int i = 0 ; i < Constants.CLIENT_CPP_FILES.size(); i++)
                        out.append(" ").append(Constants.CLIENT_CPP_FILES.get(i)).append(Constants.CPP_HDR2_EXTENSION);
                }
            }
            if(!hasDifferentLocation && !Constants.CLIENT_HPP_FILES.isEmpty()) {
                out.append(" *").append(Constants.CPP_HDR2_EXTENSION);
            }
            out.append(" *").append(Constants.CPP_HDR_EXTENSION);

            if(!hasDifferentLocation) {
                if(!Constants.CLIENT_HDR_FILES.isEmpty() || !Constants.CLIENT_HPP_FILES.isEmpty()) {
                    out.append("\n")
                            .append(Constants.OBJECTS)
                            .append(" = ").append(Constants.CLIENT_CPP_FILES.get(0))
                            .append(Constants.CPP_BINARY_EXTENSION).append(" ");

                    for (int i = 1; i < Constants.CLIENT_CPP_FILES.size(); i++)
                        out.append(Constants.CLIENT_CPP_FILES.get(i)).append(Constants.CPP_BINARY_EXTENSION).append(" ");
                }
                else {
                    out.append("\n").append(Constants.OBJECTS).append(" = ");
                }
            }
            else {
                if(!Constants.CLIENT_HDR_FILES.isEmpty() || !Constants.CLIENT_HPP_FILES.isEmpty()) {
                    String client_file_name = new File(targetDir + File.separator
                            + Constants.CLIENT_CPP_FILES.get(0)
                            + Constants.CPP_FILE_EXTENSION)
                            .getName().replace(Constants.CPP_FILE_EXTENSION, "");
                    out.append("\n")
                            .append(Constants.OBJECTS)
                            .append(" = ").append(client_file_name)
                            .append(Constants.CPP_BINARY_EXTENSION).append(" ");
                    for (int i = 1; i < Constants.CLIENT_CPP_FILES.size(); i++) {
                        client_file_name = new File(targetDir + File.separator
                                + Constants.CLIENT_CPP_FILES.get(i)
                                + Constants.CPP_FILE_EXTENSION)
                                .getName().replace(Constants.CPP_FILE_EXTENSION, "");
                        out.append(client_file_name).append(Constants.CPP_BINARY_EXTENSION).append(" ");
                    }
                }
                else {
                    out.append("\n").append(Constants.OBJECTS).append(" = ");
                }
            }
        }
        else {
            out.append(Constants.HEADERS).append(" = ")
                    .append("*").append(Constants.CPP_HDR_EXTENSION)
                    .append("\n").append(Constants.OBJECTS).append(" = ");
        }
        out.append(Constants.DEFAULT_ASTD_NAME.toLowerCase()).append(Constants.CPP_BINARY_EXTENSION).append("\n\n");

        return out.toString();
    }

    private String compileOpts()
    {
        StringBuilder out = new StringBuilder();
        // compile
        out.append("%").append(Constants.CPP_BINARY_EXTENSION).append(": ");
        if(hasDifferentLocation) {
            if (!Constants.CLIENT_CPP_FILES.isEmpty()) {
                for (int i = 0; i < Constants.CLIENT_CPP_FILES.size(); i++)
                    out.append(Constants.CLIENT_CPP_FILES.get(i)).append(Constants.CPP_FILE_EXTENSION).append(" ");
            }
            out.append(Constants.DEFAULT_ASTD_NAME.toLowerCase()).append(Constants.CPP_FILE_EXTENSION);
        }
        else {
            out.append("%").append(Constants.CPP_FILE_EXTENSION);
        }
        out.append(" ");
        out.append(Constants.HEADER_VARS).append("\n")
                .append("\t").append(Constants.CC_VARS).append(" ")
                .append(Constants.CFLAG_VARS).append(" ");
        if(includeDir != null) {
            out.append(Constants.INCLUDE_VARS).append(" ");
        }
        if(libDir != null) {
            out.append(Constants.LDLIBS_VARS).append(" ");
        }
        out.append(Constants.GCC_INPUT_OPTS);

        if(!hasDifferentLocation) {
            out.append(" $< ");
        }
        else {
            out.append(" ");
            if (!Constants.CLIENT_CPP_FILES.isEmpty()) {
                for (int i = 0; i < Constants.CLIENT_CPP_FILES.size(); i++)
                    out.append(Constants.CLIENT_CPP_FILES.get(i)).append(Constants.CPP_FILE_EXTENSION).append(" ");
            }
            out.append(Constants.DEFAULT_ASTD_NAME.toLowerCase()).append(Constants.CPP_FILE_EXTENSION);
        }
        out.append("\n\n");
        //tell compiler target
        out.append(Constants.PRECIOUS).append(": ")
                .append(Constants.TARGET_VARS)
                .append(" ").append(Constants.OBJECT_VARS).append("\n\n");
        // generate binary file
        out.append(Constants.TARGET_VARS).append(": ")
                .append(Constants.OBJECT_VARS).append("\n")
                .append("\t").append(Constants.CC_VARS).append(" ")
                .append(Constants.OBJECT_VARS).append(" ")
                .append(Constants.GCC_OUTPUT_OPTS).append(" $@").append("\n\n");

        return out.toString();
    }

    private String cleanOpts()
    {
        // clean all
        StringBuilder out = new StringBuilder();
        if(this.currOS.contains("windows")){
            out.append(Constants.CLEAN).append(": ").append("\n")
                    .append("\t").append(Constants.CLEAN_OPTS_WIN)
                    .append("*").append(Constants.CPP_BINARY_EXTENSION).append(" ").append(Constants.TONUL).append("\n")
                    .append("\t").append(Constants.CLEAN_OPTS_WIN);

            for(int i = 0 ; i < Constants.CLIENT_HDR_FILES.size(); i++)
                out.append(" ").append(Constants.CLIENT_HDR_FILES.get(i)).append(Constants.CPP_HDR_EXTENSION);

            out.append("*").append(Constants.CPP_HDR_EXTENSION).append(" ").append(Constants.TONUL).append("\n")
                    .append("\t").append(Constants.CLEAN_OPTS_WIN);

            for(int i = 0 ; i < Constants.CLIENT_HPP_FILES.size(); i++)
                out.append(" ").append(Constants.CLIENT_HPP_FILES.get(i)).append(Constants.CPP_HDR2_EXTENSION);

            out.append("*").append(Constants.CPP_HDR2_EXTENSION).append(" ").append(Constants.TONUL).append("\n");

            out.append("\t").append(Constants.CLEAN_OPTS_WIN);
            for(int i = 0 ; i < Constants.CLIENT_CPP_FILES.size(); i++)
                out.append(" ").append(Constants.CLIENT_CPP_FILES.get(i)).append(Constants.CPP_FILE_EXTENSION);
            out.append("*").append(Constants.CPP_FILE_EXTENSION).append(" ").append(Constants.TONUL).append("\n");

            out.append("\t").append(Constants.CLEAN_OPTS_WIN)
                    .append(Constants.TARGET_VARS).append(Constants.EXECUTABLE).append(" ").append(Constants.TONUL);
        }
        else{
            out.append(Constants.CLEAN).append(": ").append("\n")
                    .append("\t").append(Constants.CLEAN_OPTS)
                    .append("*").append(Constants.CPP_BINARY_EXTENSION).append("\n")
                    .append("\t").append(Constants.CLEAN_OPTS)
                    .append(Constants.FILTER_OUT_OPTS);

            for(int i = 0 ; i < Constants.CLIENT_HDR_FILES.size(); i++)
                out.append(" ").append(Constants.CLIENT_HDR_FILES.get(i)).append(Constants.CPP_HDR_EXTENSION);

            out.append(",").append(Constants.FILTER_CPP_HDR_EXTENSION).append(")\n")
                    .append("\t").append(Constants.CLEAN_OPTS)
                    .append(Constants.FILTER_OUT_OPTS);

            for(int i = 0 ; i < Constants.CLIENT_HPP_FILES.size(); i++)
                out.append(" ").append(Constants.CLIENT_HPP_FILES.get(i)).append(Constants.CPP_HDR2_EXTENSION);

            out.append(",").append(Constants.FILTER_CPP_HDR2_EXTENSION).append(")\n");

            out.append("\t").append(Constants.CLEAN_OPTS).append(Constants.FILTER_OUT_OPTS);
            for(int i = 0 ; i < Constants.CLIENT_CPP_FILES.size(); i++)
                out.append(" ").append(Constants.CLIENT_CPP_FILES.get(i)).append(Constants.CPP_FILE_EXTENSION);
            out.append(",").append(Constants.FILTER_CPP_FILE_EXTENSION).append(")\n");

            out.append("\t").append(Constants.CLEAN_OPTS)
                    .append(Constants.TARGET_VARS);
        }

        return out.toString();
    }

    private void generateJavaMakefile() {
         //TODO:
    }

    private void generateExecutableCode() {
        //String rootPath= System.getProperty("user.dir");
        StringBuilder out = new StringBuilder();
        Process proc;
        // make
        out.append(Constants.MAKE);
        try {
            proc = Runtime.getRuntime().exec(out.toString(), null, new File(targetDir));
            catchInput(proc);
            catchErrors(proc);
        }catch(Exception e) { e.printStackTrace(); }
        //wait for 3 seconds
        try {
            TimeUnit.SECONDS.sleep(3);
        }
        catch(InterruptedException e) {e.printStackTrace();};
        if(!Constants.ERROR_FOUND)
            System.out.println(Constants.CASTD_SUCCESS);
    }

    private void catchInput(Process proc) {
        new Thread(() -> {
            BufferedReader input = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line = null;
            try {
                while ((line = input.readLine()) != null)
                    System.out.println(line);
            } catch (IOException e) { e.printStackTrace(); }
        }).start();
    }

    private void catchErrors(Process proc) {
        new Thread(() -> {
            BufferedReader input = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
            String line = null;
            try {
                while ((line = input.readLine()) != null) {
                    if(line.contains(Conventions.ERROR_LABEL.toLowerCase())
                        || line.contains(Conventions.EXCEPTION_LABEL.toLowerCase())) {
                        System.out.println(Constants.CASTD_FAILED);
                        Constants.ERROR_FOUND = true;
                    }
                    System.out.println(line);
                }
            } catch (IOException e) { e.printStackTrace(); }
        }).start();
    }

    private void generateJSONLibraryIfNecessary() {
        // generates folder `json,hpp`
        if(ASTDParser.hasComplexType
            || Constants.EXEC_STATE_ACTIVATED
            || Constants.EVT_FORMAT.equals(Constants.JSON_FORMAT)
            || Constants.EVT_FORMAT.equals(Constants.ALL)) {
            String castdPath = null;
            try {
                castdPath = (Utils.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
            } catch (URISyntaxException e) {
                if(Constants.DEBUG) e.printStackTrace();
            }
            String zipFilePath = "";
            if(castdPath != null) {
                zipFilePath = castdPath.replace(Constants.CASTD_NAME + Constants.JAVA_EXEC_EXTENSION,
                        "libs" + File.separator + "json.zip");
                UnzipUtility unzipper = new UnzipUtility();
                try {
                    unzipper.unzip(zipFilePath, this.targetDir);
                } catch (Exception ex) {
                    if(Constants.DEBUG) ex.printStackTrace();
                }
            }
            else {
                System.out.println("[Error] JSON C++ library is not available !!");
                System.out.println("hints: Provide file CASTD_PATH/libs/json.zip");
            }
        }
    }
}
