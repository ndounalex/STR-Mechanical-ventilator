package com.udes.model.astd.types;

import com.udes.model.astd.base.ASTD;
import com.udes.model.astd.base.QuantifiedASTD;
import com.udes.model.astd.base.UnaryASTD;
import com.udes.model.astd.items.Constant;
import com.udes.model.astd.items.Event;
import com.udes.model.astd.items.Transition;
import com.udes.model.astd.items.Variable;
import com.udes.model.astd.tojson.ToJson;
import com.udes.model.il.conditions.Condition;
import com.udes.model.il.containers.Entry;
import com.udes.model.il.conventions.Conventions;
import com.udes.model.il.methods.Function;
import com.udes.model.il.record.Enum;
import com.udes.model.il.record.Record;
import com.udes.model.il.statements.DeclStatement;
import com.udes.model.il.statements.SeqStatement;
import com.udes.model.il.statements.Statement;
import com.udes.model.il.terms.Bool;
import com.udes.parser.ExecSchemaParser;

import java.util.*;

public class Elem extends ASTD {

    @Override
    public Statement init(ArrayList<ASTD> callList, String lets) {
        return null;
    }

    @Override
    public Statement initforsub(ArrayList<ASTD> callList, Event e, Bool timed, String lets, boolean forFinal) {
        return null;
    }

    public Statement initAutState(){return null;}

    @Override
    public Condition _final(ArrayList<ASTD> ASTDTree) {
        return null;
    }

    @Override
    public Condition _finalForSub(ArrayList<ASTD> ASTDTree) {
        return null;
    }

    @Override
    public Set<String> trans_refs() {
        return null;
    }

    @Override
    public Entry<List<Enum>, List<Record>> trans_type() {
        return null;
    }

    @Override
    public List<Variable> trans_var() {
        return null;
    }

    @Override
    public Entry<Condition, Statement> trans_event(Event e, Bool timed, ArrayList<ASTD> callList, String lets) {
        return null;
    }

    @Override
    public Entry<Condition, Statement> trans_event_step(Event e, Bool timed, ArrayList<ASTD> callList, String lets) {
        return null;
    }

    @Override
    public Set<Event> findAllEvents() {
        return null;
    }

    @Override
    public List<String> findAllStates() {
        return null;
    }

    @Override
    public boolean hasHistoryState() {
        return false;
    }

    @Override
    public List<Function> initHistoryState() {
        return null;
    }

    @Override
    public List<Transition> findTransitions(String evtLabel) {
        return null;
    }

    @Override
    public List<Variable> getAllVariables() {
        return null;
    }

    @Override
    public String prefix(String childName) { return null; }

    @Override
    public String prefixTree(ArrayList<ASTD> CallList) { return  null; }

    @Override
    public String prefixTree(ArrayList<ASTD> CallList, String childName) { return null; }

    @Override
    public String getInitialStateValue() { return null; }

    @Override
    public List<Variable> getAllQVariables(ArrayList<ASTD> CallList) {
        return null;
    }

    @Override
    public List<Variable> getAllEventQVariables(Event e) {
        return null;
    }

    @Override
    public List<Variable> getAllEventParams(Event e) {
        return null;
    }

    @Override
    public Statement propertyMapping() { return null; }

    @Override
    public Statement currentStateToJson() { return null; }

    @Override
    public List<String> updateSubIndexes(ToJson obj) {return null; }

    @Override
    public List<Function> trans_quantified_condition(Event e, Bool timed, List<Variable> varList, ArrayList<ASTD> CallList) { return null; }

    @Override
    public List<Function> trans_quantified_condition_step(Event e, Bool timed, List<Variable> varList, ArrayList<ASTD> CallList) { return null; }

    @Override
    public List<QuantifiedASTD> findAllQASTDs() { return null; };

    @Override
    public List<Variable> getEvtParams(Event e) { return null; };

    public List<Variable> enclosingASTDVariables(ArrayList<ASTD> ASTDTree) {return null;}

    public List<Variable> enclosingASTDParams(ArrayList<ASTD> ASTDTree) {return null;}
}
