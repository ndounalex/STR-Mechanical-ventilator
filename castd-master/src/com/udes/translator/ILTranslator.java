package com.udes.translator;

import com.udes.model.astd.items.Variable;
import com.udes.model.il.ILModel;
import com.udes.model.il.conventions.Conventions;
import com.udes.model.il.methods.Function;
import com.udes.model.il.record.Record;
import com.udes.model.il.statements.SeqStatement;
import com.udes.model.il.statements.Statement;
import com.udes.model.il.terms.Bool;

import java.io.File;
import java.io.InputStream;

import com.udes.parser.ASTDParser;
import com.udes.parser.ExecSchemaParser;
import com.udes.track.ExecSchema;
import com.udes.utils.Constants;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class ILTranslator {

    private static String CPP_LANG = "/clang_CPP.properties";
    private static String JAVA_LANG = "/jlang_JAVA.properties";
    public static String USYMBOL_1 = "$";
    public static String USYMBOL_2 = "#";
    public static String USYMBOL_3 = "%";
    public static String USYMBOL_4 = "&";
    public static String USYMBOL_5 = "@";


    public enum Lang {
        C99,
        CPP,
        JAVA,
        PYTHON,
        OCAML,
        Bro
    }

    private ResourceBundle bundle;
    private ILModel im_code;
    private StringBuilder out;
    private Lang lang;

    public ILTranslator(ILModel im_code, Lang currLang) {

        this.im_code = im_code;
        this.lang  = currLang;
        if(currLang == Lang.CPP)
            bundle = createBundle(CPP_LANG);
        else if(currLang == Lang.JAVA) {
            bundle = createBundle(JAVA_LANG);
        }
        out = new StringBuilder();

        Constants.CLIENT_HDR_FILES = new ArrayList<>();
        Constants.CLIENT_HPP_FILES = new ArrayList<>();
        Constants.CLIENT_CPP_FILES = new ArrayList<>();
    }

    public ResourceBundle createBundle(String path) {
        try (InputStream is = getClass().getResourceAsStream(path)) {
            return new PropertyResourceBundle(is);
        } catch (Exception e) {
            if(Constants.DEBUG) e.printStackTrace();
        }
        return null;
    }

    public String translate() {
        return out.toString();
    }

    public ILTranslator header(Bool timed) {
        if(im_code.getExtrefs() != null) {
            Set<String> refs = new HashSet<>();
            refs.addAll(im_code.getExtrefs());

            if (refs != null) {
                genCPPHeaders(refs);
                detectCPPFromHeaderNames();
            }
        }
        if(lang == Lang.CPP) {
            if(Constants.EXEC_STATE_ACTIVATED) {
                Constants.ASTD_PROPS_MAPPING = buildExecSchema(timed);
            }
            Constants.HELPER_DEFINES = buildDefines();
            out.append(bundle.getString("INCLUDE2")
                             .replace(USYMBOL_1, Constants.HELPER + Constants.CPP_HDR_EXTENSION));
            if(Constants.DEBUG)
                out.append(bundle.getString("INCLUDE2")
                   .replace(USYMBOL_1, Constants.LOGGER + Constants.CPP_HDR_EXTENSION));
            if(timed.getValue() || Constants.TIMED_SIMULATION){
                if(Constants.TIMED_SIMULATION){
                    //#include <chrono>
                    out.append(bundle.getString("INCLUDE")
                            .replace(USYMBOL_1, Constants.CHRONO));
                    //#include <ctime>
                    out.append(bundle.getString("INCLUDE")
                            .replace(USYMBOL_1, Constants.CTIME));
                    //#include <inttypes.h>
                    out.append(bundle.getString("INCLUDE2")
                            .replace(USYMBOL_1, Constants.INTTYPES + Constants.CPP_HDR_EXTENSION));
                }
                else{
//                    //#include "blockingconcurrentqueue.h"
//                    out.append(bundle.getString("INCLUDE2")
//                            .replace(USYMBOL_1, Constants.BLOCKINGQUEUE + Constants.CPP_HDR_EXTENSION));
                    //#include <thread>
                    out.append(bundle.getString("INCLUDE")
                            .replace(USYMBOL_1, Constants.THREAD));
                    //#include <mutex>
                    out.append(bundle.getString("INCLUDE")
                            .replace(USYMBOL_1, Constants.MUTEX));
                    //#include <chrono>
                    out.append(bundle.getString("INCLUDE")
                            .replace(USYMBOL_1, Constants.CHRONO));
                    //#include <ctime>
                    out.append(bundle.getString("INCLUDE")
                            .replace(USYMBOL_1, Constants.CTIME));
                    //#include <atomic>
                    out.append(bundle.getString("INCLUDE")
                            .replace(USYMBOL_1, Constants.ATOMIC));
                }
            }
        }
        if(lang == Lang.JAVA)
            out.append(bundle.getString("INCLUDE").replace(USYMBOL_1, Constants.HELPER));

        if(Constants.EXEC_STATE_ACTIVATED) {
            genVizStateHeader();
        }

        return this;
    }

    private void genCPPHeaders(Set<String> refs)
    {
        AtomicReference<String> chf = new AtomicReference<>();
        refs.forEach( rf -> {
            if(rf.endsWith(Constants.CPP_HDR_EXTENSION)) {
                chf.set(rf.replace(Constants.CPP_HDR_EXTENSION, ""));
                Constants.CLIENT_HDR_FILES.add(chf.get());
                if (lang == Lang.CPP) {
                    out.append(bundle.getString("INCLUDE2").replace(USYMBOL_1,
                            chf.get() + Constants.CPP_HDR_EXTENSION));
                }
            }
            if(rf.endsWith(Constants.CPP_HDR2_EXTENSION)) {
                chf.set(rf.replace(Constants.CPP_HDR2_EXTENSION, ""));
                Constants.CLIENT_HPP_FILES.add(chf.get());
                if (lang == Lang.CPP) {
                    out.append(bundle.getString("INCLUDE2").replace(USYMBOL_1,
                            chf.get() + Constants.CPP_HDR2_EXTENSION));
                }
            }
            if(rf.endsWith(Constants.CPP_FILE_EXTENSION)) {
                chf.set(rf.replace(Constants.CPP_FILE_EXTENSION, ""));
                Constants.CLIENT_CPP_FILES.add(chf.get());
                if (lang == Lang.CPP) {
                    out.append(bundle.getString("INCLUDE2").replace(USYMBOL_1,
                            chf.get() + Constants.CPP_FILE_EXTENSION));
                }
            }
        });
    }

    private void genVizStateHeader()
    {
        out.append(bundle.getString("DEFINE")
                .replace(ILTranslator.USYMBOL_1,
                        bundle.getString("STRINGIFY")
                                .replace(ILTranslator.USYMBOL_1, ExecSchemaParser.NAME))
                .replace(ILTranslator.USYMBOL_3, ILTranslator.USYMBOL_2
                        + bundle.getString("SEP")
                        + ExecSchemaParser.NAME
                        + bundle.getString("NEWLINE")));
    }

    private void detectCPPFromHeaderNames()
    {
        if(!Constants.CLIENT_HDR_FILES.isEmpty() || !Constants.CLIENT_HPP_FILES.isEmpty()) {
            Constants.CLIENT_HDR_FILES.forEach(chf_ -> {
                File curr_file = new File(Constants.CURRENT_PATH + File.separator
                        + chf_ + Constants.CPP_FILE_EXTENSION);
                if(curr_file.exists()) {
                    Constants.CLIENT_CPP_FILES.add(chf_);
                }
            });
            Constants.CLIENT_HPP_FILES.forEach(chf_ -> {
                File curr_file = new File(Constants.CURRENT_PATH + File.separator
                        + chf_ + Constants.CPP_FILE_EXTENSION);
                if(curr_file.exists()) {
                    Constants.CLIENT_CPP_FILES.add(chf_);
                }
            });
        }
    }

    public ILTranslator types() {
        if(Constants.TIMED_SIMULATION){
            Variable current_time = new Variable(Conventions.CURRENT_TIME, Conventions.TIME_TYPE2, Constants.INITIAL_TIME, null);
            out.append(current_time.toTarget(bundle));
        }
        List<Record> records = im_code.getTypedecls();
        if(records != null) {
            for (Record rec : records) {
                out.append(rec.toTarget(bundle));
            }
        }
        return this;
    }

    public ILTranslator variables() {
        List<Variable> vars = im_code.getVardecls();
        if(vars != null) {
            vars.forEach( v -> {
                try {
                    out.append(v.toTarget(bundle));
                }
                catch(Exception e) {
                    if(Constants.DEBUG) e.printStackTrace();
                }
            });
        }
        return this;
    }

    public ILTranslator functions(Bool timed) {
        List<Function> functions = im_code.getFunctions();
        if(functions != null) {
            AtomicReference<String> eventType = new AtomicReference<>();
            functions.forEach( func -> {
                String n = func.getName();
                if(n.equals(Conventions.MAIN_FUNCTION)) {
                    if(lang == Lang.CPP) {
                        out.append(bundle.getString("MAIN_FUNCTION"));
                    }
                    else if (lang == Lang.JAVA) {/* TODO:*/ }

                    out.append(bundle.getString("BRA_BEGIN"));
                    out.append(func.getBlock().generateCode(eventType.get(), lang, bundle, timed)
                                   .replaceAll("(?m)^", "\t"));
                    out.append(bundle.getString("BRA_END")).append("\n");
                }
                else if(n.equals(Conventions.EXEC_STATE_SENDTO_EASTD)) {
                    if(lang == Lang.CPP) {
                        out.append(Constants.EXEC_STATE_SENDTO_EASTD);
                    }
                    else if (lang == Lang.JAVA) {/* TODO:*/ }
                }
                else if(n.equals(Conventions.EXEC_STATE_CLOSE)) {
                    if(lang == Lang.CPP) {
                        out.append(Constants.EXEC_STATE_CLOSE);
                    }
                    else if (lang == Lang.JAVA) {/* TODO:*/ }
                }
                else if(n.equals(Conventions.CONSUMER+Conventions._FUNC)){
                    if(lang == Lang.CPP) {
                        out.append(bundle.getString("CONSUMER_FUNCTION"));

                    }
                    else if (lang == Lang.JAVA) {/* TODO:*/ }
                    out.append(bundle.getString("BRA_BEGIN"));
                    out.append(func.getBlock().generateCode(eventType.get(), lang, bundle, timed)
                            .replaceAll("(?m)^", "\t"));
                    out.append(bundle.getString("BRA_END")).append("\n");
                }
                else if(n.equals(Conventions.PRODUCER+Conventions.DUMMY_PARAMS+Conventions.EVENT_TEXT+Conventions._FUNC)){
                    if(lang == Lang.CPP) {
                        out.append(bundle.getString("PRODUCER_EVENTS_FUNCTION"));

                    }
                    else if (lang == Lang.JAVA) {/* TODO:*/ }
                    out.append(bundle.getString("BRA_BEGIN"));
                    out.append(func.getBlock().generateCode(eventType.get(), lang, bundle, timed)
                            .replaceAll("(?m)^", "\t"));
                    out.append(bundle.getString("BRA_END")).append("\n");
                }
                else if(n.equals(Conventions.PRODUCER+Conventions.DUMMY_PARAMS+Conventions.STEP+Conventions._FUNC)){
                    if(lang == Lang.CPP) {
                        out.append(bundle.getString("PRODUCER_STEP_FUNCTION"));

                    }
                    else if (lang == Lang.JAVA) {/* TODO:*/ }
                    out.append(bundle.getString("BRA_BEGIN"));
                    out.append(func.getBlock().generateCode(eventType.get(), lang, bundle, timed)
                            .replaceAll("(?m)^", "\t"));
                    out.append(bundle.getString("BRA_END")).append("\n");
                }
                else {
                    List<Variable> params = func.getParams();
                    Statement block = func.getBlock();
                    if (block != null) {
                        out.append("\n");
                        out.append( ((n.contains(Conventions.EXISTS) || n.contains(Conventions.FOR_ALL))
                                    ? bundle.getString("BOOLEAN_TYPE") + bundle.getString("SEP")
                                      : ((n.contains("init"))
                                          ? func.getType() + bundle.getString("SEP")
                                            : ((n.contains(Conventions.EXEC_STATE_ACTION))
                                              ? bundle.getString("EMPTY_RETURN")
                                                : (func.getType().contains(Conventions.BOOL_TYPE)
                                                    ? (bundle.getString("BOOLEAN_TYPE")
                                                        + bundle.getString("SEP"))
                                                    : bundle.getString("EMPTY_RETURN"))
                                                   + bundle.getString("SEP")))));

                        out.append(n).append(bundle.getString("PAR_BEGIN"));
                        if(params != null) {
                            AtomicReference<Integer> i = new AtomicReference<>(params.size() - 1);
                            params.forEach(p -> {
                                String type = p.getType(), name = p.getName();
                                if ((type != null) && !type.isEmpty() && (name != null) && (!name.isEmpty())) {
                                    if(type.contains(Conventions.STRING))
                                        type = type.replace(Conventions.STRING, bundle.getString("STRING_TYPE"));

                                    if (n.contains(Conventions.EXISTS) || n.contains(Conventions.FOR_ALL)) {
                                        if (!name.contains("_")) {
                                            type = type + USYMBOL_4;
                                        } else {
                                            eventType.set(type);
                                        }
                                    }
                                    out.append(type).append(bundle.getString("SEP")).append(name);
                                    if (i.get() != 0) {
                                        out.append(bundle.getString("COMMA_SEP"));
                                    }
                                }
                                i.set(i.get() - 1);
                            });
                        }
                        out.append(bundle.getString("PAR_END"));
                        out.append(bundle.getString("BRA_BEGIN"));
                        out.append(func.getBlock().generateCode(eventType.get(), lang, bundle, timed)
                                .replaceAll("(?m)^", "\t"));
                        out.append(bundle.getString("BRA_END")).append("\n");
                    }
                }
            });
        }
        return this;
    }

    private String buildDefines() {
        if(ASTDParser.onto_classes != null){
            StringBuilder out = new StringBuilder();
            ASTDParser.onto_classes.forEach( cls -> {
                    out.append(bundle.getString("DEFINE").replace("$", cls)
                            .replace("%", Conventions.JSON.toLowerCase()));
                    out.append("\n");
            });
            return out.toString();
        }
        else {
            return "";
        }
    }

    private String buildExecSchema(Bool timed) {
        StringBuilder out =  new StringBuilder();
        ExecSchema es = im_code.getExecSchema();
        if(es != null) {
            SeqStatement seqStmt = (SeqStatement) es.getExecSchemaIL();
            List<Statement> stmtList = seqStmt.getStatement();

            stmtList.forEach(s -> {
                out.append(s.generateCode(null, lang, bundle, timed)).append("\n");
            });
        }

        return out.toString();
    }
}
