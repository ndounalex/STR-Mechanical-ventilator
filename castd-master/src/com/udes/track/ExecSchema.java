package com.udes.track;

import com.udes.model.il.statements.Statement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExecSchema {
    
    private List<String>              topLevelProps;
    private Map<String, List<String>> astdProps;
    private List<String>              historyStateProps;
    private List<String>              qSubStateProps;
    private List<String>              attributeProps;
    private Statement                 execSchemaIL;

    public ExecSchema() {
        topLevelProps     = new ArrayList<>();
        astdProps         = new HashMap<>();
        historyStateProps = new ArrayList<>();
        qSubStateProps    = new ArrayList<>();
        attributeProps    = new ArrayList<>();
    }

    public List<String> getTopLevelProps() {
        return topLevelProps;
    }

    public void setTopLevelProps(List<String> topLevelProps) {
        this.topLevelProps = topLevelProps;
    }

    public Map<String, List<String>> getASTDProps() {
        return astdProps;
    }

    public void setASTDProps(Map<String, List<String>> astdProps) {
        this.astdProps = astdProps;
    }

    public List<String> getHistoryStateProps() {
        return historyStateProps;
    }

    public void setHistoryStateProps(List<String> historyStateProps) {
        this.historyStateProps = historyStateProps;
    }

    public List<String> getQSubStateProps() {
        return qSubStateProps;
    }

    public void setQSubStateProps(List<String> qSubStateProps) {
        this.qSubStateProps = qSubStateProps;
    }

    public List<String> getAttributeProps() {
        return attributeProps;
    }

    public void setAttributeProps(List<String> attributeProps) {
        this.attributeProps = attributeProps;
    }

    public void setExecSchemaIL(Statement execSchemaIL) {this.execSchemaIL = execSchemaIL; }

    public Statement getExecSchemaIL() {   return execSchemaIL; }

    public boolean isEmpty() {
        return topLevelProps.isEmpty() && astdProps.isEmpty() && qSubStateProps.isEmpty() && attributeProps.isEmpty();
    }


}