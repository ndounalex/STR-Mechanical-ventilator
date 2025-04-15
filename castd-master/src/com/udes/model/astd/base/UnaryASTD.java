package com.udes.model.astd.base;

import com.udes.model.astd.items.*;
import com.udes.model.astd.tojson.ToJson;
import com.udes.model.il.conditions.Condition;
import com.udes.model.il.containers.Entry;
import com.udes.model.il.conventions.Conventions;
import com.udes.model.il.methods.Function;
import com.udes.model.il.statements.Statement;
import com.udes.model.il.terms.Bool;
import com.udes.utils.Utils;

import java.util.*;

public abstract class UnaryASTD extends ASTD{

    private ASTD body;

    public UnaryASTD(String name,
                     List<Variable> attributes,
                     List<Variable> params,
                     Action astdAction,
                     ASTD body) {
        super(name, attributes, params, astdAction);
        this.body = body;
        setToJson(new ToJson());
    }

    public UnaryASTD(String name,
                     ASTD body) {
        super(name);
        this.body = body;
        setToJson(new ToJson());
    }

    public UnaryASTD() {
        super();
        setToJson(new ToJson());
    }

    public ASTD getBody() { return body; }

    public void setBody(ASTD body) { this.body = body; }

    /*
     * @brief Generate external references of the top of the model
     * @param  ASTD model
     * @return List of refs
     */
    @Override
    public Set<String> trans_refs() {
        Set<String> refList = new HashSet<>();
        Set<String> imports = getImports();
        if (imports != null)
            refList.addAll(imports);
        Set<String> importBody = getBody().trans_refs();
        if (importBody != null)
            refList.addAll(importBody);

        return refList;
    }
    /*
     * @brief Generate variable declarations from ASTD states
     * @param  ASTD model
     * @return List of variables
     */
    @Override
    public List<Variable> trans_var() {
        List<Variable> varList = new ArrayList<>();
        // body
        ASTD b = getBody();
        List<Variable> varBody = new ArrayList<>();
        if(b != null)
            varBody = b.trans_var();
        if(varBody != null && !varBody.isEmpty())
            varList.addAll(varBody);

        return varList;
    }
    /*
     * @brief Gets all events appearing in an ASTD
     * @param  ASTD model
     * @return The list of events
     */
    @Override
    public Set<Event> findAllEvents() {
        Set<Event> evts = new HashSet<>();
        ASTD b = getBody();
        Set<Event> evtBody = new HashSet<>();
        if(b != null)
            evtBody = b.findAllEvents();
        if(evtBody!= null && !evtBody.isEmpty())
            evts.addAll(evtBody);

        return evts;
    }
    /*
     * @brief Gets all states appearing in an ASTD
     * @param  ASTD model
     * @return The list of events
     */
    public List<String> findAllStates() {
        List<String> states = new ArrayList<>();

        ASTD b = getBody();
        List<String> stateBody = new ArrayList<>();
        if(b != null)
            stateBody = b.findAllStates();
        if(stateBody != null && !stateBody.isEmpty())
            states.addAll(stateBody);

        return new ArrayList<>(new HashSet<>(states));
    }

    @Override
    public List<Transition> findTransitions(String evtLabel) {
        List<Transition> transList = new ArrayList<>();

        List<Transition> transBody = getBody().findTransitions(evtLabel);
        if(transBody != null && !transBody.isEmpty())
            transList.addAll(transBody);

        return transList;
    }

    /*
     * @brief  Computes all attribute prefixes. It will be used later by guards and actions.
     * @param  ASTD
     * @return The map of var name and their associated prefix
     */
    @Override
    public List<Variable> getAllVariables() {
        List<Variable> prefAttr = new ArrayList<>();
        List<Variable> attrs = getAttributes();
        if(attrs != null) {
            prefAttr.addAll(attrs);
        }
        List<Variable> tmpBody = getBody().getAllVariables();
        if(tmpBody != null && !tmpBody.isEmpty())
            prefAttr.addAll(tmpBody);

        return prefAttr;
    }
    /*
     * @brief Computes the prefix that should be added to access a property of a structure
     * @param  the parent ASTD model
     * @param  the child name to prefix
     * @return A string prefix
     */
    @Override
    public String prefix(String childName) {
        String prefixBase = Conventions.getStructVar(getName());
        if(getName().compareTo(childName) == 0) {
            return prefixBase;
        }
        else {
            ASTD b = getBody();
            String bodyPrefix = null;
            if(b != null)
                bodyPrefix = b.prefix(childName);

            if (bodyPrefix != null) {
                return prefixBase + "." + bodyPrefix;
            }
        }
        return null;
    }

    @Override
    public String prefixTree(ArrayList<ASTD> CallList){
        UnaryASTD astd = (UnaryASTD) CallList.get(0);
        ArrayList<ASTD> ASTDTree = new ArrayList<>();
        for(int i = 1; i < CallList.size(); i++){
            ASTDTree.add(CallList.get(i));
        }
        String prefixBase = Conventions.getStructVar(astd.getName());
        if(ASTDTree.isEmpty()) {
            return prefixBase;
        }
        else {
            String bodyPrefix = ASTDTree.get(0).prefixTree(ASTDTree);

            if (bodyPrefix != null) {
                return prefixBase + "." + bodyPrefix;
            }
        }
        return null;
    }

    @Override
    public String prefixTree(ArrayList<ASTD> CallList, String ref){
        UnaryASTD astd = (UnaryASTD) CallList.get(0);
        ArrayList<ASTD> ASTDTree = new ArrayList<>();
        for(int i = 1; i < CallList.size(); i++){
            ASTDTree.add(CallList.get(i));
        }
        String prefixBase = Conventions.getStructVar(astd.getName());
        if(ASTDTree.isEmpty() || astd.getName().equals(ref)) {
            return prefixBase;
        }
        else {
            String bodyPrefix = ASTDTree.get(0).prefixTree(ASTDTree, ref);

            if (bodyPrefix != null) {
                return prefixBase + "." + bodyPrefix;
            }
        }
        return null;
    }

    /*
     * @brief Get the variables of the enclosing ASTDs of an ASTD
     * @param  the parent ASTD model
     * @param  the child name
     * @return A list of variables
     */
    public List<Variable> enclosingASTDVariables(ArrayList<ASTD> CallList) {
        List<Variable> enclosingBase = getAttributes();

        ArrayList<ASTD> ASTDTree = new ArrayList<>();
        for(int i = 1; i < CallList.size(); i++){
            ASTDTree.add(CallList.get(i));
        }
        List<Variable> enclosingTmp = new ArrayList<>();
        if(enclosingBase != null)
            enclosingTmp.addAll(enclosingBase);
        if(!ASTDTree.isEmpty()) {
            List<Variable> bodyASTDVariables = getBody().enclosingASTDVariables(ASTDTree);
            if (bodyASTDVariables != null && !bodyASTDVariables.isEmpty())
                enclosingTmp.addAll(bodyASTDVariables);
        }
        return enclosingTmp;

    }

    /*
     * @brief Get the variables of the enclosing ASTDs of an ASTD
     * @param  the parent ASTD model
     * @param  the child name
     * @return A list of variables
     */
    public List<Variable> enclosingASTDParams(ArrayList<ASTD> CallList) {
        List<Variable> enclosingBase = new ArrayList<>();
        ArrayList<ASTD> ASTDTree = new ArrayList<>();
        for(int i = 1; i < CallList.size(); i++){
            ASTDTree.add(CallList.get(i));
        }
        List<Variable> enclosingTmp = new ArrayList<>();
        if(enclosingBase != null)
            enclosingTmp.addAll(enclosingBase);
        if(!ASTDTree.isEmpty()) {
            List<Variable> bodyASTDVariables = getBody().enclosingASTDParams(ASTDTree);
            if (bodyASTDVariables != null && !bodyASTDVariables.isEmpty())
                enclosingTmp.addAll(bodyASTDVariables);
        }
        return enclosingTmp;

    }

    /*
     * @brief Gets if ASTD has an history State
     * @param  ASTD model
     * @return if ASTD has an history State
     */
    public boolean hasHistoryState() {
        boolean boolBody = false;
        try {
            boolBody = getBody().hasHistoryState();
            if(boolBody)
                return boolBody;
        }catch(Exception e){}

        return boolBody;
    }

    /*
     * @brief list of init functions
     * @param  ASTD model
     * @return a list of init functions
     */
    public List<Function> initHistoryState() {
        List<Function> transBody = getBody().initHistoryState();
        if(transBody != null && !transBody.isEmpty())
            return transBody;

        return null;
    }

    @Override
    public List<Variable> getAllQVariables(ArrayList<ASTD> CallList) {
        ArrayList<ASTD> ASTDTree = new ArrayList<>();
        for(int i = 1; i < CallList.size(); i++){
            ASTDTree.add(CallList.get(i));
        }
        List<Variable> list = new ArrayList<>();
        if(!ASTDTree.isEmpty()) {
            list.addAll(getBody().getAllQVariables(ASTDTree));
        }
        return list;
    }

    @Override
    public List<Variable> getAllEventQVariables(Event e) {
        List<Variable> list = new ArrayList<>();
        List<Variable> bList = getBody().getAllEventQVariables(e);
        if(!bList.isEmpty())
            list.addAll(bList);

        return list;
    }

    @Override
    public List<Variable> getAllEventParams(Event e) {
        List<Variable> list = new ArrayList<>();
        Set<Event> bSet = getBody().findAllEvents();
        if(!bSet.isEmpty()) {
            for(Event _e :  bSet) {
                if(e.getName().compareTo(_e.getName()) == 0) {
                    List<Variable> e_params = e.getParams();
                    if(e_params != null)
                        list.addAll(e_params);
                }
            }
        }

        return new ArrayList<>(list);
    }

    @Override
    public List<String> updateSubIndexes(ToJson obj) {
        return Arrays.asList(obj.getNodeIndex());
    }

    public List<Function> trans_quantified_condition(Event e, Bool timed, List<Variable> varList, ArrayList<ASTD> CallList) {
        ArrayList<ASTD> ASTDTree = new ArrayList<>();
        ASTDTree.addAll(CallList);
        ASTDTree.add(this);
        List<Function> list = new ArrayList<>();
        List<Function> bList = getBody().trans_quantified_condition(e, timed, varList, ASTDTree);
        if(bList != null && !bList.isEmpty())
            list.addAll(bList);

        return list;
    }

    public List<Function> trans_quantified_condition_step(Event e, Bool timed, List<Variable> varList, ArrayList<ASTD> CallList) {
        ArrayList<ASTD> ASTDTree = new ArrayList<>();
        ASTDTree.addAll(CallList);
        ASTDTree.add(this);
        List<Function> list = new ArrayList<>();
        List<Function> bList = getBody().trans_quantified_condition_step(e, timed, varList, ASTDTree);
        if(bList != null && !bList.isEmpty())
            list.addAll(bList);

        return list;
    }

    public List<QuantifiedASTD> findAllQASTDs() {
         List<QuantifiedASTD> qList = new ArrayList<>(),
                              bList = getBody().findAllQASTDs();
         if(bList != null && !bList.isEmpty())
              qList.addAll(bList);

         return qList;
    }

    public List<Variable>  getEvtParams(Event e) {
        if(Utils.qASTDList != null && !Utils.qASTDList.isEmpty()){
            return getBody().getEvtParams(e);
        }
        else {
            return getParent().getAllEventParams(e);
        }
    }

    @Override
    public List<Function> generateFinalFunc(ArrayList<ASTD> CallList) {
        ArrayList<ASTD> ASTDTree = new ArrayList<>();
        ASTDTree.addAll(CallList);
        ASTDTree.add(this);
        List<Function> list = new ArrayList<>();
        List<Function> bList = getBody().generateFinalFunc(ASTDTree);
        if (bList != null && !bList.isEmpty())
            list.addAll(bList);

        return list;
    }
}
