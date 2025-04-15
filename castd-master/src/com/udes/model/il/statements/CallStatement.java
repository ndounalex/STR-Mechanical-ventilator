package com.udes.model.il.statements;

import com.udes.model.astd.base.ASTD;
import com.udes.model.astd.items.Variable;
import com.udes.model.il.conventions.Conventions;
import com.udes.model.il.predicates.BinaryPredicate;
import com.udes.model.il.terms.Term;
import com.udes.translator.ILTranslator;
import com.udes.model.il.terms.Bool;
import com.udes.utils.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;

public class CallStatement extends Statement {

    BinaryPredicate<Object> pred;

    public CallStatement(String name, List<Object> params) {
        pred = new BinaryPredicate<>(name, params);
    }

    public Object getName() {
        return pred.getOp();
    }

    public void setName(String name) {
        pred.setOp(name);
    }

    public List<Object> getParams() {
        return pred.getExpr();
    }

    public void setParams(List<Object> params) {
        pred.setExpr(params);
    }

    @Override
    public List<Variable> decodeInstruction() {
        if(pred.getOp().contains(Conventions.EXISTS+"_")){
            List<Variable> varList = new ArrayList<>();
            varList.add(new Variable(pred.getOp(), null, pred.getExpr().get(0), null));
            return varList;
        }
        else if(pred.getOp().contains(Conventions.FOR_ALL+"_")){
            List<Variable> varList = new ArrayList<>();
            varList.add(new Variable(pred.getOp(), null, pred.getExpr().get(0), null));
            return varList;
        }
        else{
            return null;
        }
    }

    @Override
    public Statement updateInstruction(List<Variable> varList, ArrayList<ASTD> ASTDTree) {
        return this;
    }

    @Override
    public String generateCode(String eventType, ILTranslator.Lang lang, ResourceBundle bundle, Bool timed) {
        StringBuilder out = new StringBuilder();
        String n = (String) getName();
        if (n.compareTo(Conventions.ERROR_LABEL) == 0) {
            if(lang == ILTranslator.Lang.CPP)
                out.append("ERROR_1");
            if(lang == ILTranslator.Lang.JAVA) /*TODO: */;
        }
        else if (n.compareTo(Conventions.ERROR_LABEL1) == 0) {
            if(lang == ILTranslator.Lang.CPP)
                out.append("ERROR_2");
            if(lang == ILTranslator.Lang.JAVA) /*TODO: */;
        }
        else if (n.compareTo(Conventions.ERROR_LABEL3) == 0) {
            if(lang == ILTranslator.Lang.CPP)
                out.append("ERROR_3");
            if(lang == ILTranslator.Lang.JAVA) /*TODO: */;
        }
        else if (n.compareTo(Conventions.ERROR_LABEL4) == 0) {
            if(lang == ILTranslator.Lang.CPP)
                out.append("ERROR_4");
            if(lang == ILTranslator.Lang.JAVA) /*TODO: */;
        }
        else if (n.compareTo(Conventions.ERROR_LABEL5) == 0) {
            if(lang == ILTranslator.Lang.CPP)
                out.append("ERROR_5");
            if(lang == ILTranslator.Lang.JAVA) /*TODO: */;
        }
        else if(n.compareTo(Conventions.RETURN_CALL) == 0) {
            if(lang == ILTranslator.Lang.CPP) {
                String ret_val = (String) getParams().get(0);
                if(ret_val.compareTo(Conventions.TRUE) == 0)
                    out.append(bundle.getString("RETURN_ANY").replace(ILTranslator.USYMBOL_1,
                               bundle.getString("TRUE")));
                else if (ret_val.compareTo(Conventions.FALSE) == 0)
                    out.append(bundle.getString("RETURN_ANY").replace(ILTranslator.USYMBOL_1,
                               bundle.getString("FALSE")));
                else if (ret_val.compareTo(Conventions.NIL) == 0)
                    out.append(bundle.getString("RETURN_ANY").replace(ILTranslator.USYMBOL_1, bundle.getString("NIL")));
                else
                    out.append(bundle.getString("RETURN_ANY").replace(ILTranslator.USYMBOL_1, ret_val));
            }
            if(lang == ILTranslator.Lang.JAVA) /*TODO: */;
        } else if (n.contains(Conventions.CLEAR_VECTOR.replace(ILTranslator.USYMBOL_2,""))) {
            if(lang == ILTranslator.Lang.CPP)
                out.append(n);
            if(lang == ILTranslator.Lang.JAVA) /*TODO: */;
        } else if(n.compareTo(Conventions.EXEC_STATE_SENDTO_EASTD) == 0
                || n.compareTo(Conventions.EXEC_STATE_CLOSE) == 0) {
            if(lang == ILTranslator.Lang.CPP) {
                if(getParams() == null)
                    out.append(n).append(bundle.getString("PAR_BEGIN")).append(bundle.getString("PAR_END"));
            }
            if(lang == ILTranslator.Lang.JAVA) /*TODO: */;
        }
        else if(n.compareTo(Conventions.THREAD_CRE) == 0){
            if (lang == ILTranslator.Lang.CPP) {
                String arg = getParams().get(0).toString();
                if(arg.compareTo("consumer") == 0){
                    out.append(n)
                            .append(bundle.getString("PAR_BEGIN"))
                            .append(bundle.getString("SHARED_PTR")+arg)
                            .append(bundle.getString("COMMA_SEP"))
                            .append(bundle.getString("NIL"))
                            .append(bundle.getString("COMMA_SEP"))
                            .append(arg+Conventions._FUNC)
                            .append(bundle.getString("COMMA_SEP"))
                            .append(bundle.getString("NIL"))
                            .append(bundle.getString("PAR_END"));
                }
                else if(arg.compareTo("producers") == 0){
                    out.append(n)
                            .append(bundle.getString("PAR_BEGIN"))
                            .append(bundle.getString("SHARED_PTR")+arg+Conventions.ARRAY0)
                            .append(bundle.getString("COMMA_SEP"))
                            .append(bundle.getString("NIL"))
                            .append(bundle.getString("COMMA_SEP"))
                            .append(arg+Conventions.DUMMY_PARAMS+Conventions.EVENT_TEXT+Conventions._FUNC)
                            .append(bundle.getString("COMMA_SEP"))
                            .append(bundle.getString("ARG_THREAD"))
                            .append(bundle.getString("PAR_END"));
                    out.append(bundle.getString("SEMI_COLON_SEP"));
                    out.append(n)
                            .append(bundle.getString("PAR_BEGIN"))
                            .append(bundle.getString("SHARED_PTR")+arg+Conventions.ARRAY1)
                            .append(bundle.getString("COMMA_SEP"))
                            .append(bundle.getString("NIL"))
                            .append(bundle.getString("COMMA_SEP"))
                            .append(arg+Conventions.DUMMY_PARAMS+Conventions.STEP+Conventions._FUNC)
                            .append(bundle.getString("COMMA_SEP"))
                            .append(bundle.getString("NIL"))
                            .append(bundle.getString("PAR_END"));
                }
            }
            if (lang == ILTranslator.Lang.JAVA) /*TODO: */;
        }
        else if(n.compareTo(Conventions.THREAD_JOIN) == 0){
            if (lang == ILTranslator.Lang.CPP) {
                String arg = getParams().get(0).toString();
                if(arg.compareTo("consumer") == 0){
                    out.append(n)
                            .append(bundle.getString("PAR_BEGIN"))
                            .append(arg)
                            .append(bundle.getString("COMMA_SEP"))
                            .append(bundle.getString("NIL"))
                            .append(bundle.getString("PAR_END"));
                }
                else if(arg.compareTo("producers") == 0){
                    out.append(n)
                            .append(bundle.getString("PAR_BEGIN"))
                            .append(arg+Conventions.ARRAY0)
                            .append(bundle.getString("COMMA_SEP"))
                            .append(bundle.getString("NIL"))
                            .append(bundle.getString("PAR_END"));
                    out.append(bundle.getString("SEMI_COLON_SEP"));
                    out.append(n)
                            .append(bundle.getString("PAR_BEGIN"))
                            .append(arg+Conventions.ARRAY1)
                            .append(bundle.getString("COMMA_SEP"))
                            .append(bundle.getString("NIL"))
                            .append(bundle.getString("PAR_END"));
                }
            }
            if (lang == ILTranslator.Lang.JAVA) /*TODO: */;
        }
        else if(n.compareTo(Conventions.WAIT_DEQ) == 0){
            if(lang == ILTranslator.Lang.CPP){
                out.append(bundle.getString("WAIT_DEQ"))
                        .append(bundle.getString("PAR_BEGIN"))
                        .append(Conventions.CONSUMED)
                        .append(bundle.getString("PAR_END"));
            }
            if (lang == ILTranslator.Lang.JAVA) /*TODO: */;
        }
        else if(n.compareTo(Conventions.ENQUEUE) == 0){
            String arg = getParams().get(0).toString();
            if(lang == ILTranslator.Lang.CPP){
                out.append(bundle.getString("ENQUEUE"))
                        .append(bundle.getString("PAR_BEGIN"))
                        .append(arg)
                        .append(bundle.getString("PAR_END"));
            }
            if (lang == ILTranslator.Lang.JAVA) /*TODO: */;
        }
        else if(n.compareTo(Conventions.THREAD_SLEEP) == 0){
            if(lang == ILTranslator.Lang.CPP){
                out.append(bundle.getString("THREAD_SLEEP"));
            }
            if (lang == ILTranslator.Lang.JAVA) /*TODO: */;
        }
        else if(n.compareTo(Conventions.CONTINUE) == 0){
            if(lang == ILTranslator.Lang.CPP){
                out.append(n);
            }
            if(lang == ILTranslator.Lang.JAVA) /*TODO: */;
        }
        else if(n.compareTo(Conventions.CLOCK_RESET) == 0){
            if(Constants.TIMED_SIMULATION){
                if(lang == ILTranslator.Lang.CPP){
                    if(getParams().get(1).toString().equals(Conventions.CST)){
                        String arg = getParams().get(0).toString();
                        out.append(bundle.getString("CLOCK_RESET_SIM").replace(ILTranslator.USYMBOL_1, arg));
                    }
                    else{
                        String arg = getParams().get(0).toString();
                        out.append(bundle.getString("CLOCK_RESET_SIM2")
                                .replace(ILTranslator.USYMBOL_1, arg)
                                .replace(ILTranslator.USYMBOL_2, getParams().get(1).toString()));
                    }
                }
                if(lang == ILTranslator.Lang.JAVA) /*TODO: */;
            }
            else{
                if(lang == ILTranslator.Lang.CPP){
                    if(getParams().get(1).toString().equals(Conventions.CST)){
                        String arg = getParams().get(0).toString();
                        out.append(bundle.getString("CLOCK_RESET").replace(ILTranslator.USYMBOL_1, arg));
                    }
                    else{
                        String arg = getParams().get(0).toString();
                        out.append(bundle.getString("CLOCK_RESET2")
                                .replace(ILTranslator.USYMBOL_1, arg)
                                .replace(ILTranslator.USYMBOL_2, getParams().get(1).toString()));
                    }
                }
                if(lang == ILTranslator.Lang.JAVA) /*TODO: */;
            }
        }
        else if(n.compareTo(Conventions.GOTO) == 0) {
            if (lang == ILTranslator.Lang.CPP) {
                String arg = getParams().get(0).toString();
                out.append(bundle.getString("GOTO").replace(ILTranslator.USYMBOL_1, arg));
            }
            if (lang == ILTranslator.Lang.JAVA) /*TODO: */ ;
        }
        else if(n.compareTo(Conventions.GOTOFUNC) == 0) {
            if (lang == ILTranslator.Lang.CPP) {
                String arg = getParams().get(0).toString();
                out.append(bundle.getString("GOTOFUNC").replace(ILTranslator.USYMBOL_1, arg));
            }
            if (lang == ILTranslator.Lang.JAVA) /*TODO: */ ;
        }
        else if(n.compareTo(Conventions.LOCK) == 0) {
            if (lang == ILTranslator.Lang.CPP) {
                String arg = getParams().get(0).toString();
                out.append(bundle.getString("LOCK").replace(ILTranslator.USYMBOL_1, arg));
            }
            if (lang == ILTranslator.Lang.JAVA) /*TODO: */ ;
        }
        else if(n.compareTo(Conventions.UNLOCK) == 0) {
            if (lang == ILTranslator.Lang.CPP) {
                String arg = getParams().get(0).toString();
                out.append(bundle.getString("UNLOCK").replace(ILTranslator.USYMBOL_1, arg));
            }
            if (lang == ILTranslator.Lang.JAVA) /*TODO: */ ;
        }
        else if(!n.isEmpty()){
            List<Object> params = getParams();
            if (params != null && !params.isEmpty()) {
                if(params.size() == 1 && (params.get(0) instanceof List)) {
                    if(lang == ILTranslator.Lang.CPP)
                        out.append(Conventions.SAFE_EXEC_CALL).append(bundle.getString("PAR_BEGIN"))
                           .append(n).append(bundle.getString("PAR_BEGIN"));
                    else
                        out.append(n).append(bundle.getString("PAR_BEGIN"));

                    List<Object> varList = (List<Object>) params.get(0);
                    AtomicReference<Integer> j = new AtomicReference<>(varList.size() - 1),
                                             k = new AtomicReference<>(0);
                    varList.forEach(v -> {
                        Variable _v = (Variable) v;
                        String type = _v.getType();
                        // primitive type calls
                        out.append(callParams(k.get(), type, Conventions.STRING, Conventions.STR_TO_STR, bundle));
                        out.append(callParams(k.get(), type, Conventions.INT, Conventions.STR_TO_INT, bundle));
                        out.append(callParams(k.get(), type, Conventions.BOOL_TYPE, Conventions.STR_TO_BOOL, bundle));
                        out.append(callParams(k.get(), type, Conventions.BOOL_TYPE1, Conventions.STR_TO_BOOL, bundle));
                        out.append(callParams(k.get(), type, Conventions.FLOAT, Conventions.STR_TO_FLOAT, bundle));
                        out.append(callParams(k.get(), type, Conventions.DOUBLE, Conventions.STR_TO_DOUBLE, bundle));

                        k.set(k.get() + 1);
                        if (j.get() != 0) {
                            out.append(bundle.getString("COMMA_SEP"));
                            j.set(j.get() - 1);
                        }
                    });
                    if(lang == ILTranslator.Lang.CPP)
                        out.append(bundle.getString("PAR_END"));
                }
                else {
                    out.append(n).append(bundle.getString("PAR_BEGIN"));
                    AtomicReference<Integer> i = new AtomicReference<>(params.size() - 1);
                    params.forEach(p -> {
                        if (p instanceof Variable)
                            out.append(((Variable) p).getName());
                        else
                            out.append((String) p);

                        if (i.get() != 0) {
                            out.append(bundle.getString("COMMA_SEP"));
                            i.set(i.get() - 1);
                        }
                    });
                }
            }else{
                out.append(n).append(bundle.getString("PAR_BEGIN"));
            }
            out.append(bundle.getString("PAR_END"));
        }
        out.append(bundle.getString("SEMI_COLON_SEP"));

        return out.toString();
    }

    private String callParams(int index, String type1, String type2, String funcCall, ResourceBundle bundle) {
        StringBuilder out = new StringBuilder();
        if(type1.toLowerCase().contains(type2.toLowerCase())) {
            out.append(bundle.getString("MEMBER_ACCESS")
                .replace(ILTranslator.USYMBOL_1, Conventions.TYPES)
                .replace(ILTranslator.USYMBOL_2, funcCall))
                .append(bundle.getString("PAR_BEGIN"))
                .append(bundle.getString("ACCESS_ELEM_VECT2")
                        .replace(ILTranslator.USYMBOL_1, Conventions.EVENT_PARAMS)
                        .replace(ILTranslator.USYMBOL_2, String.valueOf(index)))
                .append(bundle.getString("PAR_END"));
        }

        return out.toString();
    }
}
