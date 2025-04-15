package com.udes.model.il.conditions;

import com.udes.model.astd.base.ASTD;
import com.udes.model.astd.items.Variable;
import com.udes.model.il.conventions.Conventions;
import com.udes.model.il.predicates.BinaryPredicate;
import com.udes.translator.ILTranslator;
import com.udes.utils.Constants;

import javax.management.ImmutableDescriptor;
import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class CallCondition extends Condition {

    BinaryPredicate<String> pred;
    BinaryPredicate<Object> pred1;

    public CallCondition(String name, List<String> params) {
        pred = new BinaryPredicate<>(name, params);
    }
    public CallCondition(String name, String var, String domain, Condition cond) {
        pred1 = new BinaryPredicate<>(name, Arrays.asList(var, domain, cond));
    }

    public CallCondition(int idx, String name, List<Object> params) {
        pred1 = new BinaryPredicate<>(name, params);
    }

    public String getName() {
        return (pred != null) ? pred.getOp() : null;
    }

    public void setName(String name) {
        if (pred != null) pred.setOp(name);
    }

    public List<String> getParams() {
        return (pred != null) ? pred.getExpr() : null;
    }

    public void setParams(List<String> params) {
        if (pred != null)
            pred.setExpr(params);
    }

    public String getName2() {
        return (pred1 != null) ? pred1.getOp() : null;
    }

    public void setName2(String name) {
        if(pred1 != null)
            pred1.setOp(name);
    }

    public List<Object> getParam2() {
        return (pred1 != null) ? pred1.getExpr() : null;
    }

    public void setParams2(List<Object> params) {
        if (pred1 != null)
            pred1.setExpr(params);
    }

    @Override
    public Condition updateInstruction(List<Variable> varList, ArrayList<ASTD> ASTDTree) {
        if(pred.getOp().contains(Conventions.EXISTS+"_") || pred.getOp().contains(Conventions.FOR_ALL+"_")){
            for(Variable var : varList){
                if(var.getName().equals(pred.getOp())){
                    return (OrCondition) var.getInit();
                }
            }
        }
        if(getParams() != null) {
            List<String> params = new ArrayList<>(getParams());
            if (params != null) {
                if (varList != null) {
                    String stateVar = params.get(0);
                    varList.forEach(v -> {
                        if (v.getName().compareTo(stateVar) == 0) {
                            Object v_init = v.getInit();
                            if (v_init != null)
                                params.set(0, v_init.toString());
                            //TODO CHECK IF INSIDE A GUARD IT CHANGES THE VALUE FOR A PARAMETER FUNCTION GUARD(x) -> GUARD(x.init)
                            return;
                        }
                    });
                    setParams(params);
                    return this;
                }
            }
        }
        else if(pred1 != null){
            //there is a call inside a call
            if(pred1.getExpr().get(1) instanceof Condition){
                ((Condition) pred1.getExpr().get(1)).updateInstruction(varList, ASTDTree);
            }
            if(pred1.getExpr().get(0) instanceof Condition){
                ((Condition) pred1.getExpr().get(0)).updateInstruction(varList, ASTDTree);
            }
        }
        return this;
    }

    @Override
    public String generateCode(ILTranslator.Lang lang, ResourceBundle bundle) {
        String n = getName();
        StringBuilder out = new StringBuilder();
        List<String> params = getParams();
        if (params != null && params.size() >= 2) {
            String a = params.get(0), b = params.get(1);
            if (n.equals(Conventions.IN)) {
                if (lang == ILTranslator.Lang.CPP)
                    out.append(bundle.getString("IN")
                            .replace(ILTranslator.USYMBOL_1, b)
                            .replace(ILTranslator.USYMBOL_2, a));
                if (lang == ILTranslator.Lang.JAVA) /*TODO:*/ ;
            }
            else if (n.equals(Conventions.NOT_IN)) {
                if (lang == ILTranslator.Lang.CPP)
                    out.append(bundle.getString("NOTIN")
                            .replace(ILTranslator.USYMBOL_1, b)
                            .replace(ILTranslator.USYMBOL_2, a));
                if (lang == ILTranslator.Lang.JAVA) /*TODO:*/ ;
            }
            else if (n.equals(Conventions.IN_MAP)) {
                if (lang == ILTranslator.Lang.CPP)
                    out.append(bundle.getString("IN_MAP")
                            .replace(ILTranslator.USYMBOL_1, b)
                            .replace(ILTranslator.USYMBOL_2, a));
                if (lang == ILTranslator.Lang.JAVA) /*TODO:*/ ;
            } else if (n.equals(Conventions.NOT_IN_MAP)) {
                if (lang == ILTranslator.Lang.CPP)
                    out.append(bundle.getString("NOT_IN_MAP")
                            .replace(ILTranslator.USYMBOL_1, b)
                            .replace(ILTranslator.USYMBOL_2, a));
                if (lang == ILTranslator.Lang.JAVA) /*TODO:*/ ;

            } else if (n.equals(Conventions.EQUALS)) {
                if (a.compareTo(Conventions.EVENT_LABEL) == 0 || a.compareTo(Conventions.CONSUMED) == 0) {
                    if (lang == ILTranslator.Lang.CPP) {
                        out.append(bundle.getString("COMPARE")
                                .replace(ILTranslator.USYMBOL_1, a)
                                .replace(ILTranslator.USYMBOL_2, bundle.getString("STRING_QUOTE")
                                        .replace(ILTranslator.USYMBOL_1, b)))
                                .append(bundle.getString("EQUALS"))
                                .append("0");
                    }
                    if (lang == ILTranslator.Lang.JAVA) /*TODO:*/ ;
                }
                else {
                    if (lang == ILTranslator.Lang.CPP) {
                        if (b.compareTo(Conventions.NIL) != 0) {
                            out.append(a).append(bundle.getString("EQUALS")).append(b);
                        }
                        else {
                            out.append(a);
                        }
                    }
                    if (lang == ILTranslator.Lang.JAVA) /*TODO:*/ ;
                }
            }
            else if (n.equals(Conventions.NOT_EQUALS)) {
                if (lang == ILTranslator.Lang.CPP) {
                    if (b.compareTo(Conventions.NIL) != 0) {
                        out.append(bundle.getString("PAR_BEGIN"))
                                .append(a).append(bundle.getString("NOT_EQUALS")).append(b)
                                .append(bundle.getString("PAR_END"));
                    }
                    else {
                        out.append(bundle.getString("NOT")).append(a);
                    }
                }
                if (lang == ILTranslator.Lang.JAVA) /*TODO:*/ ;
            }
            else if (n.equals(Conventions.LESSER)) {
                if (a.compareTo(Conventions.EVENT_LABEL) == 0) {
                    if (lang == ILTranslator.Lang.CPP) {
                        out.append(bundle.getString("COMPARE")
                                        .replace(ILTranslator.USYMBOL_1, a)
                                        .replace(ILTranslator.USYMBOL_2, bundle.getString("STRING_QUOTE")
                                                .replace(ILTranslator.USYMBOL_1, b)))
                                .append(bundle.getString("LESSER"))
                                .append("0");
                    }
                    if (lang == ILTranslator.Lang.JAVA) /*TODO:*/ ;
                }
                else {
                    if (lang == ILTranslator.Lang.CPP) {
                        if (b.compareTo(Conventions.NIL) != 0) {
                            out.append(a).append(bundle.getString("LESSER")).append(b);
                        }
                        else {
                            out.append(a);
                        }
                    }
                    if (lang == ILTranslator.Lang.JAVA) /*TODO:*/ ;
                }
            }
            else if (n.equals(Conventions.GREATER_EQUALS)) {
                if (a.compareTo(Conventions.CST) == 0) {
                    if (lang == ILTranslator.Lang.CPP) {
                        out.append(bundle.getString("ASSIGN_TIME"))
                                .append(bundle.getString("GREATER_EQUALS"))
                                .append(b);
                    }
                    if (lang == ILTranslator.Lang.JAVA) /*TODO:*/ ;
                }
                else {
                    if (lang == ILTranslator.Lang.CPP) {
                        if (b.compareTo(Conventions.NIL) != 0) {
                            out.append(a).append(bundle.getString("GREATER_EQUALS")).append(b);
                        }
                        else {
                            out.append(a);
                        }
                    }
                    if (lang == ILTranslator.Lang.JAVA) /*TODO:*/ ;
                }
            }
            else if (n.equals(Conventions.EXPIRED)) {
               if (lang == ILTranslator.Lang.CPP) {
                        if (b.compareTo(Conventions.NIL) != 0) {
                            out.append(bundle.getString("EXPIRED")
                                    .replace(ILTranslator.USYMBOL_1, b)
                                    .replace(ILTranslator.USYMBOL_2, a));
                        }
               }
               if (lang == ILTranslator.Lang.JAVA) /*TODO:*/ ;

            }
            else {
                out.append(n).append(bundle.getString("PAR_BEGIN"));
                if(params != null && !params.isEmpty()) {
                    AtomicReference<Integer> i = new AtomicReference<>(params.size() - 1);
                    params.forEach(p -> {
                        out.append(p);
                        if (i.get() != 0) {
                            out.append(bundle.getString("COMMA_SEP"));
                            i.set(i.get() - 1);
                        }
                    });
                }
                out.append(bundle.getString("PAR_END"));
            }
        }
        else if (params != null && params.size() == 1) {

            if (n.equals(Conventions.SIMULATION)) {
                if (lang == ILTranslator.Lang.CPP) {

                        out.append(Constants.SIMULATION_BLOCK);
                }
                if (lang == ILTranslator.Lang.JAVA) /*TODO:*/ ;

            }
            else if (n.compareTo(Conventions.EMPTY) == 0) {
                if (lang == ILTranslator.Lang.CPP) {
                    out.append(bundle.getString("EMPTY")
                            .replace(ILTranslator.USYMBOL_1, params.get(0)));
                }
                if (lang == ILTranslator.Lang.JAVA) /*TODO:*/ ;
            }
            else {
                out.append(n).append(bundle.getString("PAR_BEGIN")).append(params.get(0))
                            .append(bundle.getString("PAR_END"));
            }
        }
        else {
            List<Object> params2 = getParam2();
            if(getName2() != null && (params2 == null || params2.isEmpty()))
               out.append(getName2()).append(bundle.getString("PAR_BEGIN")).append(bundle.getString("PAR_END"));
        }
        List<Object> params2 = getParam2();
        if(params2 != null && !params2.isEmpty()){
            n = getName2();
            if (n.equals(Conventions.EQUALS1)) {
                String a = (String) params2.get(0);
                Condition b = (Condition) params2.get(1);
                out.append(bundle.getString("PAR_BEGIN") + a).append(" = ")
                        .append(bundle.getString("PAR_BEGIN")
                                + b.generateCode(lang, bundle) + bundle.getString("PAR_END")
                                + bundle.getString("PAR_END"));
            }
            else {
                if (params2 != null && !params2.isEmpty()) {
                    if (params2.size() == 1 && (params2.get(0) instanceof List)) {
                        out.append(n).append(bundle.getString("PAR_BEGIN"));
                        List<Object> varList = (List<Object>) params2.get(0);
                        if(varList != null && !varList.isEmpty()) {
                            AtomicReference<Integer> j = new AtomicReference<>(varList.size() - 1),
                                    k = new AtomicReference<>(0);
                            varList.forEach(v -> {
                                Variable _v = (Variable) v;
                                String type = _v.getType();
                                // primitive type calls
                                out.append(callParams(k.get(), type, Conventions.STRING, Conventions.STR_TO_STR, bundle));
                                out.append(callParams(k.get(), type, Conventions.INT, Conventions.STR_TO_INT, bundle));
                                out.append(callParams(k.get(), type, Conventions.BOOL_TYPE1, Conventions.STR_TO_BOOL, bundle));
                                out.append(callParams(k.get(), type, Conventions.FLOAT, Conventions.STR_TO_FLOAT, bundle));
                                out.append(callParams(k.get(), type, Conventions.DOUBLE, Conventions.STR_TO_DOUBLE, bundle));
                                out.append(callParams(k.get(), type, Conventions.SHORT, Conventions.STR_TO_INT, bundle));
                                out.append(callParams(k.get(), type, Conventions.LONG, Conventions.STR_TO_INT, bundle));
                                // complex type call
                                if(type != null && !type.isEmpty() && !type.contains(Conventions.STRING)
                                    && !type.contains(Conventions.INT)
                                    && !type.contains(Conventions.BOOL_TYPE1) && !type.contains(Conventions.FLOAT)
                                    && !type.contains(Conventions.DOUBLE) && !type.contains(Conventions.SHORT)) {
                                    out.append(bundle.getString("MEMBER_ACCESS")
                                            .replace(ILTranslator.USYMBOL_1, Conventions.TYPES)
                                            .replace(ILTranslator.USYMBOL_2, Conventions.STR_TO_ONTO))
                                            .append(bundle.getString("PAR_BEGIN"))
                                            .append(bundle.getString("ACCESS_ELEM_VECT2")
                                                    .replace(ILTranslator.USYMBOL_1, Conventions.EVENT_PARAMS)
                                                    .replace(ILTranslator.USYMBOL_2, String.valueOf(k.get())))
                                            .append(bundle.getString("PAR_END"));
                                }
                                k.set(k.get() + 1);
                                if (j.get() != 0) {
                                    out.append(bundle.getString("COMMA_SEP"));
                                    j.set(j.get() - 1);
                                }
                            });
                        }
                    } else {
                        out.append(n).append(bundle.getString("PAR_BEGIN"));
                        AtomicReference<Integer> i = new AtomicReference<>(params2.size() - 1);
                        params2.forEach(p -> {
                            if (p instanceof Variable) {
                                out.append(((Variable) p).getName());
                            }
                            else {
                                out.append((String) p);
                            }
                            if (i.get() != 0) {
                                out.append(bundle.getString("COMMA_SEP"));
                                i.set(i.get() - 1);
                            }
                        });
                    }
                    out.append(bundle.getString("PAR_END"));
                }
            }
        }
        else {
            if(getName2() != null && (params2 != null))
                out.append(getName2()).append(bundle.getString("PAR_BEGIN")).append(bundle.getString("PAR_END"));
        }

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

    public boolean isEqualTo(String name) {
        if(getName() == null)
            return false;

        return this.getName().equals(name);
    }
}
