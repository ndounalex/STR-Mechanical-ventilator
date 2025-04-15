package com.udes.model.astd.base;
import com.udes.model.astd.items.*;
import com.udes.model.astd.tojson.ToJson;
import com.udes.model.il.conventions.Conventions;
import com.udes.model.il.methods.Function;
import com.udes.model.il.statements.SeqStatement;
import com.udes.model.il.statements.Statement;
import com.udes.model.il.terms.Bool;
import com.udes.utils.Constants;
import com.udes.utils.Utils;

import java.util.*;

public abstract class BinaryASTD extends ASTD {

    private ASTD left;
    private ASTD right;
    public String leftASTDIndex;
    public String rightASTDIndex;

    public BinaryASTD(String name,
                      List<Variable> attributes,
                      List<Variable> params,
                      Action astdAction,
                      ASTD left,
                      ASTD right) {
        super(name, attributes, params, astdAction);
        this.left = left;
        this.right = right;
        setToJson(new ToJson());
    }

    public BinaryASTD(String name, ASTD left, ASTD right) {
        super(name);
        this.left = left;
        this.right = right;
        setToJson(new ToJson());
    }

    public BinaryASTD() {
        super();
        setToJson(new ToJson());
    }
    /*
     * @brief Returns ASTD left
     * @param
     * @return ASTD left
     */
    public ASTD getLeft() { return left; }
    /*
     * @brief Sets ASTD left
     * @param ASTD left
     * @return
     */
    public void setLeft(ASTD left) { this.left = left; }
    /*
     * @brief Returns ASTD right
     * @param
     * @return ASTD right
     */
    public ASTD getRight() { return right; }
    /*
     * @brief Sets ASTD right
     * @param ASTD right
     * @return
     */
    public void setRight(ASTD right) { this.right = right; }

    /*
     * @brief Generate external references of the top of the model
     * @param  ASTD model
     * @return List of refs
     */
    @Override
    public Set<String> trans_refs() {
        Set<String> refList = new HashSet<>();
        Set<String> imports = getImports();
        if(imports != null)
            refList.addAll(imports);
        Set<String> importLeft  = getLeft().trans_refs();
        if(importLeft != null)
            refList.addAll(importLeft);
        Set<String> importRight = getRight().trans_refs();
        if(importRight != null)
            refList.addAll(importRight);

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
        // ASTD left
        List<Variable> varLeft = getLeft().trans_var();
        if(!varLeft.isEmpty())
            varList.addAll(varLeft);
        // ASTD right
        List<Variable> varRight = getRight().trans_var();
        if(!varRight.isEmpty())
            varList.addAll(varRight);

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
        Set<Event> evtLeft = getLeft().findAllEvents();
        if(!evtLeft.isEmpty())
            evts.addAll(evtLeft);
        Set<Event> evtRight = getRight().findAllEvents();
        if(!evtRight.isEmpty()){
            for(Event ev2 : evtRight){
                boolean contain = false;
                for(Event ev1 : evts){
                    if(ev1.getName().equals(ev2.getName())){
                        contain = true;
                    }
                }
                if(!contain){
                    evts.add(ev2);
                }
            }
        }
        return evts;
    }
    /*
     * @brief Gets all states appearing in an ASTD
     * @param  ASTD model
     * @return The list of events
     */
    public List<String> findAllStates() {
        List<String> states = new ArrayList<>();

        List<String> stateLeft = getLeft().findAllStates();
        if(!stateLeft.isEmpty())
            states.addAll(new HashSet<>(stateLeft));
        List<String> stateRight = getRight().findAllStates();
        if(!stateRight.isEmpty())
            states.addAll(new HashSet<>(stateRight));


        return new ArrayList<>(new HashSet<>(states));
    }

    @Override
    public List<Transition> findTransitions(String evtLabel) {
        List<Transition> transList = new ArrayList<>();

        List<Transition> transLeft = getLeft().findTransitions(evtLabel);
        if(!transLeft.isEmpty())
            transList.addAll(new HashSet<>(transLeft));
        List<Transition> transRight = getRight().findTransitions(evtLabel);
        if(!transRight.isEmpty())
            transList.addAll(transRight);

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
        List<Variable> tmpLeft = getLeft().getAllVariables();
        if(tmpLeft != null && !tmpLeft.isEmpty())
            prefAttr.addAll(tmpLeft);
        List<Variable> tmpRight = getRight().getAllVariables();
        if(tmpRight != null && !tmpRight.isEmpty())
            prefAttr.addAll(tmpRight);

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

            String leftPrefix = getLeft().prefix(childName);
            if(leftPrefix != null) {
                return prefixBase + "." + leftPrefix;
            }
            String rightPrefix = getRight().prefix(childName);
            if(rightPrefix != null) {
                return prefixBase + "." + rightPrefix;
            }
        }
        return null;
    }

    @Override
    public String prefixTree(ArrayList<ASTD> CallList){
        BinaryASTD astd = (BinaryASTD) CallList.get(0);
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
        BinaryASTD astd = (BinaryASTD) CallList.get(0);
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
            if (getLeft().getName().equals(ASTDTree.get(0).getName())) {
                List<Variable> leftASTDVariables = getLeft().enclosingASTDVariables(ASTDTree);
                if (leftASTDVariables != null && !leftASTDVariables.isEmpty()) {
                    enclosingTmp.addAll(leftASTDVariables);
                }
            }
            if (getRight().getName().equals(ASTDTree.get(0).getName())) {
                List<Variable> rightASTDVariables = getRight().enclosingASTDVariables(ASTDTree);
                if (rightASTDVariables != null && !rightASTDVariables.isEmpty()) {
                    enclosingTmp.addAll(rightASTDVariables);
                }
            }
        }
        return enclosingTmp;
    }

    /*
     * @brief Get the params of the enclosing ASTDs of an ASTD
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
            if (getLeft().getName().equals(ASTDTree.get(0).getName())) {
                List<Variable> leftASTDVariables = getLeft().enclosingASTDParams(ASTDTree);
                if (leftASTDVariables != null && !leftASTDVariables.isEmpty()) {
                    enclosingTmp.addAll(leftASTDVariables);
                }
            }
            if (getRight().getName().equals(ASTDTree.get(0).getName())) {
                List<Variable> rightASTDVariables = getRight().enclosingASTDParams(ASTDTree);
                if (rightASTDVariables != null && !rightASTDVariables.isEmpty()) {
                    enclosingTmp.addAll(rightASTDVariables);
                }
            }
        }
        return enclosingTmp;
    }

    /*
     * @brief Gets if ASTD has an history State
     * @param  ASTD model
     * @return if ASTD has an history State
     */
    public boolean hasHistoryState() {
        boolean boolLeft = getLeft().hasHistoryState();
        if(boolLeft)
            return boolLeft;

        boolean boolRight = getRight().hasHistoryState();
        if(boolRight)
            return boolRight;

        return false;
    }


    /*
     * @brief list of init functions
     * @param  ASTD model
     * @return a list of init functions
     */
    public List<Function> initHistoryState() {
        List<Function> transList = new ArrayList<>();
        List<Function> transLeft = getLeft().initHistoryState();
        if(transLeft != null && !transLeft.isEmpty())
            transList.addAll(transLeft);

        List<Function> transRight = getRight().initHistoryState();
        if(transRight != null && !transRight.isEmpty())
            transList.addAll(transRight);

        return transList;
    }

    @Override
    public List<Variable> getAllQVariables(ArrayList<ASTD> CallList) {
        ArrayList<ASTD> ASTDTree = new ArrayList<>();
        for(int i = 1; i < CallList.size(); i++){
            ASTDTree.add(CallList.get(i));
        }
        List<Variable> list = new ArrayList<>();
        if(!ASTDTree.isEmpty()) {
            List<Variable> lList = getLeft().getAllQVariables(ASTDTree);
            if(!lList.isEmpty())
                list.addAll(lList);
            List<Variable> rList = getRight().getAllQVariables(ASTDTree);
            if(!rList.isEmpty())
                list.addAll(rList);
        }
        return list;
    }

    @Override
    public List<Variable> getAllEventQVariables(Event e) {
        List<Variable> list = new ArrayList<>();
        List<Variable> lList = getLeft().getAllEventQVariables(e);
        Set<Event> leftEvents = getLeft().findAllEvents();
        for(Event event : leftEvents){
            if(event.getName().equals(e.getName())){
                if (lList != null && !lList.isEmpty())
                    list.addAll(lList);
            }
        }
        List<Variable> rList = getRight().getAllEventQVariables(e);
        Set<Event> rightEvents = getRight().findAllEvents();
        for(Event event : rightEvents) {
            if (event.getName().equals(e.getName())) {
                if (rList != null && !rList.isEmpty())
                    list.addAll(rList);
            }
        }
        return list;
    }

    @Override
    public List<Variable> getAllEventParams(Event e) {
        List<Variable> list = new ArrayList<>();
        
        Set<Event> lSet = getLeft().findAllEvents();
        if(!lSet.isEmpty()) {
            for(Event _e :  lSet) {
                if(e.getName().compareTo(_e.getName()) == 0) {
                    List<Variable> e_params = e.getParams();
                    if(e_params != null) {
                        for (int i = 0; i < e_params.size(); i++) {
                            list.add(e_params.get(i));
                        }
                    }
                }
            }
        }

        Set<Event> rSet = getRight().findAllEvents();
        if(!rSet.isEmpty()) {
            for(Event _e :  rSet) {
                if(e.getName().compareTo(_e.getName()) == 0) {
                    List<Variable> e_params = e.getParams();
                    if(e_params != null){
                        for (int i = 0; i < e_params.size(); i++) {
                            list.add(e_params.get(i));
                        }
                    }
                }
            }
        }

        List<Variable> mockList = new ArrayList<>();

        for(Variable param : list){
            boolean contains = false;
            for(Variable mockParam : mockList){
                if(param.getName().equals(mockParam.getName()) && param.getType().equals(mockParam.getType())){
                    contains = true;
                }
            }
            if(!contains){
                mockList.add(param);
            }
        }

        return new ArrayList<>(mockList);
    }

    @Override
    public List<String> updateSubIndexes(ToJson obj) {
        return Arrays.asList(obj.getNodeIndex(), obj.getNodeIndex());
    }

    @Override
    public List<Function> trans_quantified_condition(Event e, Bool timed, List<Variable> varList, ArrayList<ASTD> CallList) {
        ArrayList<ASTD> ASTDTree = new ArrayList<>();
        ASTDTree.addAll(CallList);
        ASTDTree.add(this);
        List<Function> qList = new ArrayList<>();
        boolean containInLeft = false;
        boolean containInRight = false;
        for(Event event : getLeft().findAllEvents()){
            if(event.getName().equals(e.getName())){
                containInLeft = true;
            }
        }
        if (containInLeft) {
            List<Function> lList = getLeft().trans_quantified_condition(e, timed, varList, ASTDTree);
            if (!lList.isEmpty())
                qList.addAll(lList);
        }
        for(Event event : getRight().findAllEvents()){
            if(event.getName().equals(e.getName())){
                containInRight = true;
            }
        }
        if (containInRight) {
            List<Function> rList = getRight().trans_quantified_condition(e, timed, varList, ASTDTree);
            if (!rList.isEmpty())
                qList.addAll(rList);
        }
        return qList;
    }

    @Override
    public List<Function> trans_quantified_condition_step(Event e, Bool timed, List<Variable> varList, ArrayList<ASTD> CallList) {
        ArrayList<ASTD> ASTDTree = new ArrayList<>();
        ASTDTree.addAll(CallList);
        ASTDTree.add(this);
        List<Function> qList = new ArrayList<>();
        boolean containInLeft = false;
        boolean containInRight = false;
        for(Event event : getLeft().findAllEvents()){
            if(event.getName().equals(e.getName())){
                containInLeft = true;
            }
        }
        if (containInLeft) {
            List<Function> lList = getLeft().trans_quantified_condition_step(e, timed, varList, ASTDTree);
            if (!lList.isEmpty())
                qList.addAll(lList);
        }
        for(Event event : getRight().findAllEvents()){
            if(event.getName().equals(e.getName())){
                containInRight = true;
            }
        }
        if (containInRight) {
            List<Function> rList = getRight().trans_quantified_condition_step(e, timed, varList, ASTDTree);
            if (!rList.isEmpty())
                qList.addAll(rList);
        }
        return qList;
    }

    @Override
    public List<Function> generateFinalFunc(ArrayList<ASTD> CallList) {
        ArrayList<ASTD> ASTDTree = new ArrayList<>(CallList);
        ASTDTree.add(this);
        List<Function> qList = new ArrayList<>();

        List<Function> lList = getLeft().generateFinalFunc(ASTDTree);
        if (!lList.isEmpty()) qList.addAll(lList);

       List<Function> rList = getRight().generateFinalFunc(ASTDTree);
       if (!rList.isEmpty()) qList.addAll(rList);
        return qList;
    }

    @Override
    public List<QuantifiedASTD> findAllQASTDs() {
        List<QuantifiedASTD> qList = new ArrayList<>();

        List<QuantifiedASTD> lList = getLeft().findAllQASTDs();
        if(lList != null && !lList.isEmpty())
            qList.addAll(lList);

        List<QuantifiedASTD> rList = getRight().findAllQASTDs();
        if(rList != null && !rList.isEmpty())
            qList.addAll(rList);

        return qList;
    }

    public List<Variable>  getEvtParams(Event e) {
        if (Utils.qASTDList != null && !Utils.qASTDList.isEmpty()) {
            List<Variable> aList = new ArrayList<>(),
                    lList = getLeft().getEvtParams(e);
            if (getLeft().findAllEvents().contains(e)) {
                if (lList != null && !lList.isEmpty()){
                    for(Variable varNew : lList){
                        boolean contain = false;
                        for(Variable varOld : aList){
                            if(varNew.getName().equals(varOld.getName())){
                                contain = true;
                            }
                        }
                        if(!contain){
                            aList.add(varNew);
                        }
                    }
                }
            }

            if (getRight().findAllEvents().contains(e)) {
                List<Variable> rList = getRight().getEvtParams(e);
                if (rList != null && !rList.isEmpty()){
                    for(Variable varNew : rList){
                        boolean contain = false;
                        for(Variable varOld : aList){
                            if(varNew.getName().equals(varOld.getName())){
                                contain = true;
                            }
                        }
                        if(!contain){
                            aList.add(varNew);
                        }
                    }
                }
            }

            return aList;
        } else {
            return getParent().getAllEventParams(e);
        }
    }
}
