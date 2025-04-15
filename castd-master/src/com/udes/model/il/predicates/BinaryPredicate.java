package com.udes.model.il.predicates;

import com.udes.model.il.statements.AssignStatement;
import com.udes.model.il.statements.Statement;
import com.udes.translator.ILTranslator;

import java.util.List;
import java.util.ResourceBundle;

public class BinaryPredicate<T> extends Predicate {
    private String op;
    private List<T> expr;
    private int id;

    public BinaryPredicate(String op, List<T> expr) {
        this.op = op;
        this.expr = expr;
    }
    public BinaryPredicate(String op, List<T> expr, int id) {
        this.op = op;
        this.expr = expr;
        this.id = id;
    }
    public BinaryPredicate(List<T> expr) {
        this (null, expr);
    }

    public String getOp() {
        return op;
    }

    public void setOp(String op) {
        this.op = op;
    }

    public List<T> getExpr() {
        return expr;
    }

    public void setExpr(List<T> expr) {
        this.expr = expr;
    }

    public int getId() { return id; }

    public void setId(int id) {this.id = id;}

    @Override
    public String generateCode(Statement stmt, String eventType, ILTranslator.Lang lang, ResourceBundle bundle) {
        StringBuilder out = new StringBuilder();
        AssignStatement ass = (AssignStatement) stmt;
        BinaryPredicate<String> pred = (BinaryPredicate<String>) ass.getExpr();
        if(lang == ILTranslator.Lang.CPP) {
            if(id == 1) {
                out.append(bundle.getString("EVENT")
                            .replace(ILTranslator.USYMBOL_1,
                                     (eventType == null)
                                     ? bundle.getString("STRING_TYPE")
                                     : eventType))
                        .append(bundle.getString("SEP"))
                        .append(ass.getVar().getId())
                        .append(bundle.getString("ASSIGN"))
                        .append(bundle.getString("READ_EVENT")
                                .replace(ILTranslator.USYMBOL_1,
                                         (eventType == null)
                                          ? bundle.getString("STRING_TYPE")
                                          : eventType));
            }
            else if(id == 2){
                out.append(bundle.getString("PTHREAD_CREATE"));
            }
            else if(id == 3){
                out.append(bundle.getString("EVENT")
                                .replace(ILTranslator.USYMBOL_1,
                                        (eventType == null)
                                                ? bundle.getString("STRING_TYPE")
                                                : eventType))
                        .append(bundle.getString("SEP"))
                        .append(ass.getVar().getId())
                        .append(bundle.getString("ASSIGN"))
                        .append(bundle.getString("READ_EVENT_THREAD")
                                .replace(ILTranslator.USYMBOL_1,
                                        (eventType == null)
                                                ? bundle.getString("STRING_TYPE")
                                                : eventType));
            }
            else {
                out.append(bundle.getString("CONFIG_INPUT_STREAM"));
            }
        }
        else if (lang == ILTranslator.Lang.JAVA) {
            // TODO:
        }
        out.append(bundle.getString("SEMI_COLON_SEP"));

        return out.toString();
    }
}
