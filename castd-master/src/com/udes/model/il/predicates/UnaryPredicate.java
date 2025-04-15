package com.udes.model.il.predicates;

import com.udes.model.il.statements.Statement;
import com.udes.translator.ILTranslator;

import java.util.ResourceBundle;

public class UnaryPredicate<T> extends Predicate {

    private String op;
    private T expr;

    public UnaryPredicate(String op, T expr) {
        this.expr = expr;
        this.op = op;
    }

    public UnaryPredicate(T expr) {
        this (null, expr);
    }

    public T getExpr() {
        return expr;
    }

    public void setExpr(T expr) {
        this.expr = expr;
    }

    public String getOp() {
        return op;
    }

    public void setOp(String op) {
        this.op = op;
    }

    @Override
    public String generateCode(Statement stmt, String eventType, ILTranslator.Lang lang, ResourceBundle bundle) {
        return null;
    }
}
