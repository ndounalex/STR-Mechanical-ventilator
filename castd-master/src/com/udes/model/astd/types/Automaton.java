package com.udes.model.astd.types;

import com.udes.model.astd.base.ASTD;
import com.udes.model.astd.base.BinaryASTD;
import com.udes.model.astd.base.QuantifiedASTD;
import com.udes.model.astd.base.UnaryASTD;
import com.udes.model.astd.items.*;
import com.udes.model.astd.tojson.ToJson;
import com.udes.model.il.conditions.AndCondition;
import com.udes.model.il.conditions.CallCondition;
import com.udes.model.il.conditions.Condition;
import com.udes.model.il.conditions.OrCondition;
import com.udes.model.il.containers.Entry;
import com.udes.model.il.conventions.Conventions;
import com.udes.model.il.methods.Function;
import com.udes.model.il.record.Enum;
import com.udes.model.il.record.Record;
import com.udes.model.il.statements.*;
import com.udes.model.il.terms.Bool;
import com.udes.model.il.terms.Term;
import com.udes.parser.ExecSchemaParser;
import com.udes.translator.ILTranslator;
import com.udes.utils.Constants;
import com.udes.utils.Utils;

import java.util.*;

public class Automaton extends ASTD {

    private List<String> eventNames;
    private List<String> stateNames;
    private Map<String, ASTD> statesToASTDs;
    private Map<String, ActionSet> statesToActions; // 0 = Entry, 1 = Stay, 2 = Exit
    private List<Transition> transitions;
    private List<String> shallowFinalStates;
    private List<String> deepFinalStates;
    private String initialState;

    public Automaton(String name,
                     List<Variable> attributes,
                     List<Variable> params,
                     Action astdAction,
                     List<String> eventNames,
                     List<String> stateNames,
                     Map<String, ASTD> statesToASTDs,
                     Map<String, ActionSet> statesToActions,
                     List<Transition> transitions,
                     List<String> shallowFinalStates,
                     List<String> deepFinalStates,
                     String initialState) {
        super(name, attributes, params, astdAction);
        this.eventNames = eventNames;
        this.stateNames = stateNames;
        this.statesToASTDs = statesToASTDs;
        this.statesToActions =  statesToActions;
        this.transitions = transitions;
        this.shallowFinalStates = shallowFinalStates;
        this.deepFinalStates = deepFinalStates;
        this.initialState = initialState;
        setToJson(new ToJson());
    }

    public Automaton(String name,
                     List<String> eventNames,
                     List<String> stateNames,
                     Map<String, ASTD> statesToASTDs,
                     Map<String, ActionSet> statesToActions,
                     List<Transition> transitions,
                     List<String> shallowFinalStates,
                     List<String> deepFinalStates,
                     String initialState) {
        super(name);
        this.eventNames = eventNames;
        this.stateNames = stateNames;
        this.statesToASTDs = statesToASTDs;
        this.statesToActions = statesToActions;
        this.transitions = transitions;
        this.shallowFinalStates = shallowFinalStates;
        this.deepFinalStates = deepFinalStates;
        this.initialState = initialState;
        setToJson(new ToJson());
    }

    public Automaton() {
        super();
        setToJson(new ToJson());
    }

    public List<String> getEventNames() {
        return eventNames;
    }

    public void setEventNames(List<String> eventNames) {
        this.eventNames = eventNames;
    }

    public List<String> getStateNames() {
        return stateNames;
    }

    public void setStateNames(List<String> stateNames) {
        this.stateNames = stateNames;
    }

    public Map<String, ASTD> getStatesToASTDs() {
        return statesToASTDs;
    }

    public Map<String, ActionSet> getStatesToActions() {
        return statesToActions;
    }

    public void setStatesToASTDs(Map<String, ASTD> statesToASTDs) {
        this.statesToASTDs = statesToASTDs;
    }

    public void setStatesToActions(Map<String, ActionSet> statesToActions) {
        this.statesToActions = statesToActions;
    }

    public List<Transition> getTransitions() {
        return transitions;
    }

    public void setTransitions(List<Transition> transitions) {
        this.transitions = transitions;
    }

    public List<String> getShallowFinalStates() {
        return shallowFinalStates;
    }

    public void setShallowFinalStates(List<String> shallowFinalStates) {
        this.shallowFinalStates = shallowFinalStates;
    }

    public List<String> getDeepFinalStates() {
        return deepFinalStates;
    }

    public void setDeepFinalStates(List<String> deepFinalStates) {
        this.deepFinalStates = deepFinalStates;
    }

    public String getInitialState() {
        return initialState;
    }

    public void setInitialState(String initialState) {
        this.initialState = initialState;
    }

    @Override
    public Statement initforsub(ArrayList<ASTD> callList, Event e, Bool timed, String lets, boolean forFinal) {
        //important for quantified ASTD
        ArrayList<ASTD> ASTDTree = new ArrayList<>(callList);
        ASTDTree.add(this);
        List<Statement> seqList = new ArrayList<>();
        String autState = Conventions.getStateVar(Automaton.class);
        String prfx = ASTDTree.get(0).prefixTree(ASTDTree) + ".";
        // init state
        seqList.add(new AssignStatement(new Term(prfx + autState), new Term(getInitialState())));
        // init attributes
        List<Variable> vList = getAttributes();
        List<Variable> pList = getParams();
        if(vList != null) {
            vList.forEach( v -> {
                String init;
                Boolean b = false;
                if(pList != null && !pList.isEmpty()){
                    for(Variable var: pList){
                        if(var.getName().equals(v.getName())){
                            b = true;
                        }
                    }
                }
                if(!b){
                    init = prefixAction(new Action(v.getInit().toString()), ASTDTree).getCode();
                    if(!init.equals("")){
                        init = init.substring(0, init.length()-1);
                    }
                }
                else{
                    init = v.getInit().toString();
                }
                if(init != null && init.isEmpty())
                    seqList.add(new AssignStatement(new Term(prfx + v.getName()), new Term("\"\"")));
                else if(init.contains("{") && init.contains(":") && init.contains("}"))
                    seqList.add(new AssignStatement(new Term(prfx + v.getName()),
                            new Term(Conventions.JSON_PARSE.replace(ILTranslator.USYMBOL_1,
                                    "\""+init+"\""))));
                else if ( init.contains("(") && init.contains(")")) {
                    seqList.add(new AssignStatement(new Term(prfx + v.getName()),
                            new Term(init)));
                }
                else if(v.getType().compareTo(Conventions.STRING) == 0)
                    seqList.add(new AssignStatement(new Term(prfx + v.getName()), new Term("\""+init+"\"")));
                else if(v.getType().compareTo(Conventions.CLOCK) == 0){
                    seqList.add(new CallStatement(Conventions.CLOCK_RESET, Arrays.asList(prfx + v.getName(), lets)));}
                else
                    seqList.add(new AssignStatement(new Term(prfx + v.getName()), new Term(init)));
            });
        }

        Map<String, ASTD> stateToASTDs = getStatesToASTDs();
        if(stateToASTDs != null) {
            if(hasHistoryState()) {
                for (Map.Entry<String, ASTD> stateASTD : stateToASTDs.entrySet()) {
                    ASTD subASTD = stateASTD.getValue();
                    if (!Conventions.isQuantifiedASTD(subASTD)) {
                        seqList.add(new AssignStatement(new Term(prfx + Conventions.getHistoryCall(stateASTD.getKey())),
                                new Term("init_" + stateASTD.getKey() + "()")));
                    }
                }
            }
            // init the initial state
            String s0 = getInitialState();
            if (stateToASTDs.containsKey(s0))
                seqList.add(stateToASTDs.get(s0).initforsub(ASTDTree, e, timed, lets, forFinal));
        }

        return new SeqStatement(seqList);
    }

    @Override
    public Statement init(ArrayList<ASTD> callList, String lets) {
        ArrayList<ASTD> ASTDTree = new ArrayList<>(callList);
        ASTDTree.add(this);
        List<Statement> seqList = new ArrayList<>();
        String autState = Conventions.getStateVar(Automaton.class);
        //prfx = getParent().prefix(getName()) + ".";
        String prfx = ASTDTree.get(0).prefixTree(ASTDTree) + ".";
        // init state
        seqList.add(new AssignStatement(new Term(prfx + autState), new Term(getInitialState())));
        // init attributes
        List<Variable> vList = getAttributes();
        List<Variable> pList = getParams();
        if(vList != null) {
            vList.forEach( v -> {
                String init;
                Boolean b = false;
                if(pList != null && !pList.isEmpty()){
                    for(Variable var: pList){
                        if(var.getName().equals(v.getName())){
                            b = true;
                        }
                    }
                }
                if(!b){
                    init = prefixAction(new Action(v.getInit().toString()), ASTDTree).getCode();
                    if(!init.equals("")){
                        init = init.substring(0, init.length()-1);
                    }
                }
                else{
                    init = v.getInit().toString();
                }
                if(init != null && init.isEmpty())
                    seqList.add(new AssignStatement(new Term(prfx + v.getName()), new Term("\"\"")));
                else if(init.contains("{") && init.contains(":") && init.contains("}"))
                    seqList.add(new AssignStatement(new Term(prfx + v.getName()),
                            new Term(Conventions.JSON_PARSE.replace(ILTranslator.USYMBOL_1,
                                    "\""+init+"\""))));
                else if ( init.contains("(") && init.contains(")")) {
                    seqList.add(new AssignStatement(new Term(prfx + v.getName()),
                            new Term(init)));
                }
                else if(v.getType().compareTo(Conventions.STRING) == 0)
                    seqList.add(new AssignStatement(new Term(prfx + v.getName()), new Term("\""+init+"\"")));
                else if(v.getType().compareTo(Conventions.CLOCK) == 0){
                    seqList.add(new CallStatement(Conventions.CLOCK_RESET, Arrays.asList(prfx + v.getName(), lets)));}
                else
                    seqList.add(new AssignStatement(new Term(prfx + v.getName()), new Term(init)));
            });
        }

        Map<String, ASTD> stateToASTDs = getStatesToASTDs();
        if(stateToASTDs != null) {
            if(hasHistoryState()) {
                for (Map.Entry<String, ASTD> stateASTD : stateToASTDs.entrySet()) {
                    ASTD subASTD = stateASTD.getValue();
                    if (!Conventions.isQuantifiedASTD(subASTD)) {
                        seqList.add(new AssignStatement(new Term(prfx + Conventions.getHistoryCall(stateASTD.getKey())),
                                new Term("init_" + stateASTD.getKey() + "()")));
                    }
                }
            }
            // init the initial state
            String s0 = getInitialState();
            if (stateToASTDs.containsKey(s0))
                seqList.add(stateToASTDs.get(s0).init(ASTDTree, lets));
        }

        return new SeqStatement(seqList);
    }

    @Override
    public Condition _final(ArrayList<ASTD> callList) {
        ArrayList<ASTD> ASTDTree = new ArrayList<>();
        ASTDTree.addAll(callList);
        ASTDTree.add(this);
        String  name = getName(),
                autState = Conventions.getStateVar(Automaton.class),
                sf = Conventions.getShallowFinalVar(name), df = Conventions.getDeepFinalVar(name);

        String prfx = ASTDTree.get(0).prefixTree(ASTDTree) + ".";

        List<String> sfStates = getShallowFinalStates(),
                dfStates = getDeepFinalStates();
        // it's final only if the final state is in the shallow final set
        // or the final state is in the deep final set
        if((dfStates == null || dfStates.isEmpty()) && (sfStates == null || sfStates.isEmpty())) {
            //No deep final or shallow final state. -> Return false.
            return new Bool(Conventions.FALSE);
        }
        else if((sfStates != null && !sfStates.isEmpty()) && (dfStates == null || dfStates.isEmpty())) {
            //There is a shallow final state.
            return new OrCondition(Arrays.asList(new CallCondition(Conventions.IN, Arrays.asList(prfx + autState, sf))));
        }
        else {
            Map<String, ASTD> stateToASTDs = getStatesToASTDs();
            List<Condition> cond = new ArrayList<>();
            if (stateToASTDs != null) {
                //final inside another ASTD
                for (Map.Entry<String, ASTD> stateASTD : stateToASTDs.entrySet()) {
                    cond.add(stateToASTDs.get(stateASTD.getKey())._final(ASTDTree));
                }
                if((dfStates != null && !dfStates.isEmpty()) && (sfStates == null || sfStates.isEmpty())) {
                    //If final is inside another ASTD, prfx + autState points to an ASTD, not to the final STATE!
                    //To know if it is a final state, it is necessary to check only the final state of the ASTD inside!
                    //Deep final means that the transition can only fire from the final state.
                    return new AndCondition(cond);
                }
                if ((sfStates != null && !sfStates.isEmpty()) && (dfStates != null && !dfStates.isEmpty())) {
                    //If final is inside another ASTD, prfx + autState points to an ASTD, not to the final STATE!
                    //To know if it is a final state, it is necessary to check only the final state of the ASTD inside!
                    //Shallow final means that any state of the sub-ASTD is final.

                    return new OrCondition(Arrays.asList(
                            new CallCondition(Conventions.IN,Arrays.asList(prfx + autState, sf)),
                            new AndCondition(cond)
                    )
                    );
                }
            }

            return null;
        }
    }

    @Override
    public Condition _finalForSub(ArrayList<ASTD> callList) {
        ArrayList<ASTD> ASTDTree = new ArrayList<>();
        ASTDTree.addAll(callList);
        ASTDTree.add(this);
        String  name = getName(),
                autState = Conventions.getStateVar(Automaton.class),
                sf = Conventions.getShallowFinalVar(name), df = Conventions.getDeepFinalVar(name);

        String prfx = ASTDTree.get(0).prefixTree(ASTDTree) + ".";

        List<String> sfStates = getShallowFinalStates(),
                dfStates = getDeepFinalStates();
        // it's final only if the final state is in the shallow final set
        // or the final state is in the deep final set
        if((dfStates == null || dfStates.isEmpty()) && (sfStates == null || sfStates.isEmpty())) {
            //No deep final or shallow final state. -> Return false.
            return new Bool(Conventions.FALSE);
        }
        else if((sfStates != null && !sfStates.isEmpty()) && (dfStates == null || dfStates.isEmpty())) {
            //There is a shallow final state.
            return new OrCondition(Arrays.asList(new CallCondition(Conventions.IN, Arrays.asList(prfx + autState, sf))));
        }
        else {
            Map<String, ASTD> stateToASTDs = getStatesToASTDs();
            List<Condition> cond = new ArrayList<>();
            if (stateToASTDs != null) {
                //final inside another ASTD
                for (Map.Entry<String, ASTD> stateASTD : stateToASTDs.entrySet()) {
                    cond.add(stateToASTDs.get(stateASTD.getKey())._finalForSub(ASTDTree));
                }
                if((dfStates != null && !dfStates.isEmpty()) && (sfStates == null || sfStates.isEmpty())) {
                    //If final is inside another ASTD, prfx + autState points to an ASTD, not to the final STATE!
                    //To know if it is a final state, it is necessary to check only the final state of the ASTD inside!
                    //Deep final means that the transition can only fire from the final state.
                    return new AndCondition(cond);
                }
                if ((sfStates != null && !sfStates.isEmpty()) && (dfStates != null && !dfStates.isEmpty())) {
                    //If final is inside another ASTD, prfx + autState points to an ASTD, not to the final STATE!
                    //To know if it is a final state, it is necessary to check only the final state of the ASTD inside!
                    //Shallow final means that any state of the sub-ASTD is final.

                    return new OrCondition(Arrays.asList(
                            new CallCondition(Conventions.IN,Arrays.asList(prfx + autState, sf)),
                            new AndCondition(cond)
                    )
                    );
                }
            }

            return null;
        }
    }

    /*
     * @brief Generate external references of the top of the model
     * @param  ASTD model
     * @return List of refs
     */
    @Override
    public Set<String> trans_refs() {
        Set<String> refList = new HashSet<>();
        try {
            Set<String> imports = getImports();
            if(imports != null)
                refList.addAll(imports);
            Map<String, ASTD> stateToASTDs = getStatesToASTDs();
            if(stateToASTDs != null)
                for (Map.Entry<?, ASTD> entry : stateToASTDs.entrySet()) {
                    ASTD subASTD = entry.getValue();
                    Set<String> imprts = subASTD.getImports();
                    if(imprts != null)
                        refList.addAll(imprts);
                    Set<String>  tmpList = entry.getValue().trans_refs();
                    if (tmpList != null)
                        refList.addAll(tmpList);
                }
        }catch(NullPointerException e) {}

        return refList;
    }
    /*
     * @brief Generate type structures from ASTDs excepts Elem type (since it's unused)
     * @param  ASTD model
     * @return List of type structures
     */
    @Override
    public Entry<List<Enum>, List<Record>> trans_type() {
        List<Record> recordList = new ArrayList<>();
        List<Enum> enumList = new ArrayList<>();
        // struct name
        String structName = Conventions.getStructName(getName());
        List<Variable> props = new ArrayList<>();
        // astd attributes
        List<Variable> attrs = getAttributes();
        if (attrs != null)
            props.addAll(attrs);
        // struct var for ASTD states
        Map<String, ASTD> stateToASTDs = getStatesToASTDs();
        // astd state
        String stateType = Conventions.getStateType(Automaton.class);
        props.add(new Variable(Conventions.getStateVar(Automaton.class), stateType, null, getName()));

        if (Constants.COND_OPT_OPTS) {
            List<Transition> transitions = getTransitions();
            int i;
            for (i = 0; i < transitions.size(); i ++) {
                props.add(new Variable("cond_" + i, Conventions.BOOL_TYPE, null, getName()));
            }
        }
        // struct var for ASTD states
        if (stateToASTDs != null) {
            for (Map.Entry<String, ASTD> entry : stateToASTDs.entrySet()) {
                if(entry.getValue() != null) {
                    if (entry.getValue().hasHistoryState()) {
                        // astd history state
                        props.add(new Variable(Conventions.HISTORY_STATE + entry.getKey(),
                                                    Conventions.getMapType1(stateType, entry.getValue()), null,
                                                    getName()));
                    }
                    props.add(new Variable(Conventions.getStructVar(entry.getKey()),
                                           Conventions.getStructName(entry.getKey()), null, getName()));
                    if (Constants.COND_OPT_OPTS) {
                        props.add(new Variable("cond_" + entry.getKey(), Conventions.BOOL_TYPE, null, getName()));
                    }
                }
            }

            recordList.add(new Record(structName, props));

            for (Map.Entry<?, ASTD> entry : stateToASTDs.entrySet()) {
                Entry<List<Enum>, List<Record>> tmpList = entry.getValue().trans_type();
                if (!tmpList.getKey().isEmpty())
                    enumList.addAll(tmpList.getKey());
                if (!tmpList.getValue().isEmpty())
                    recordList.addAll(tmpList.getValue());
            }

            return new Entry(enumList, recordList);
        } else {
            enumList.add(new Enum(stateType, getStateNames()));
            recordList.add(new Record(structName, props));

            return new Entry(enumList, recordList);
        }
    }
    /*
     * @brief Generate variable declarations from ASTD states
     * @param  ASTD model
     * @return List of variables
     */
    @Override
    public List<Variable> trans_var() {
        List<Variable> varList = new ArrayList<>();
        //constant set of shallow and deep final states
        String type = Conventions.getSetType(Conventions.getStateType(Automaton.class));
        if(getShallowFinalStates() != null) {
            if(!getShallowFinalStates().isEmpty())
                 varList.add(new Constant(Conventions.getShallowFinalVar(getName()),
                                          type, getShallowFinalStates(), getName()));
        }
        if(getDeepFinalStates() != null) {
            if(!getDeepFinalStates().isEmpty())
                 varList.add(new Constant(Conventions.getDeepFinalVar(getName()), type, getDeepFinalStates(), getName()));
        }
        // body
        Map<?, ASTD> stateToASTDs = getStatesToASTDs();
        if(stateToASTDs != null) {
            for (Map.Entry<?, ASTD> stateASTD : stateToASTDs.entrySet()) {
                List<Variable> tmpList = stateASTD.getValue().trans_var();
                if (!tmpList.isEmpty())
                    varList.addAll(tmpList);
            }
        }

        return varList;
    }

    private void trans_local(String prfx,
                             Arrow arrow,
                             String autState,
                             Map<String, ASTD> stateToASTDs,
                             Map<String, ActionSet> statesToActions,
                             Action t_action,
                             Action astdAction,
                             Transition t,
                             List<Condition> newList,
                             List<Entry<Condition, Statement>> autStmt,
                             List<Condition> cond,
                             List<Statement> stmtList,
                             ArrayList<ASTD> callList,
                             int i,
                             String lets) {
        // local transition
        Local loc = (Local) arrow;
        String s1 = loc.getS1(), s2 = loc.getS2();
        //TODO REMOVE THIS PREFIX AND ADD PREFIXTREE
        String prfxS1 = getParent().prefix(s1);

        ArrayList<ASTD> ASTDTreeExit = new ArrayList<>();
        ASTDTreeExit.addAll(callList);
        ArrayList<ASTD> ASTDTree = new ArrayList<>();
        ASTDTree.addAll(callList);
        //ASTDTreeExit.add(s1);

        Condition c_loc;
        SeqStatement t_loc = new SeqStatement(Arrays.asList(
                new AssignStatement(new Term(prfx + autState), new Term(s2)),
                (stateToASTDs != null && stateToASTDs.get(s2) != null)
                        ? stateToASTDs.get(s2).init(ASTDTree, lets) : null,
                new AssignStatement(new Term(Conventions.EXEC), new Term(Conventions.TRUE))));
        SeqStatement omega_loc = null;
        //  it's a loop
        if(s1 == s2) {
            if(hasHistoryState()) {
                if(statesToActions != null) {
                    omega_loc = new SeqStatement(Arrays.asList(
                            ((statesToASTDs.get(s1) != null) && statesToASTDs.get(s1).hasHistoryState()) ?
                                    new AssignStatement(
                                        new Term(prfx + Conventions.getHistoryCall(s1)), new Term(prfxS1)) : null,
                            t_action,
                            (statesToActions.get(s1) != null) ?
                                    this.prefixAction(statesToActions.get(s1).getStay(), ASTDTree) : null,
                            astdAction));
                }
                else {
                    omega_loc = new SeqStatement(Arrays.asList(
                                    new AssignStatement(new Term(prfx + Conventions.getHistoryCall(s1)),
                                                        new Term(prfxS1)),
                                    t_action, astdAction));
                }
            }
            else {
                if(statesToActions != null) {
                    omega_loc = new SeqStatement(Arrays.asList(
                                    t_action,
                                    ((statesToActions.get(s1) != null)
                                     ? this.prefixAction(statesToActions.get(s1).getStay(), ASTDTree) : null),
                                    astdAction));
                }
                else {
                    omega_loc = new SeqStatement(Arrays.asList(t_action, astdAction));
                }
            }
        }
        else {
            if(hasHistoryState()) {
                if(statesToActions != null) {
                    omega_loc = new SeqStatement(Arrays.asList(
                            ((statesToASTDs.get(s1) != null) && statesToASTDs.get(s1).hasHistoryState()) ?
                                    new AssignStatement(
                                        new Term(prfx + Conventions.getHistoryCall(s1)), new Term(prfxS1)) : null,
                            ((statesToActions.get(s1) != null)
                                    ? this.prefixAction(statesToActions.get(s1).getExit(), ASTDTreeExit) : null),
                            t_action,
                            ((statesToActions.get(s2) != null)
                                    ? this.prefixAction(statesToActions.get(s2).getEntry(), ASTDTree) : null),
                            astdAction));
                }
                else {
                    omega_loc = new SeqStatement(Arrays.asList(
                                    new AssignStatement(new Term(prfx + Conventions.getHistoryCall(s1)),
                                            new Term(prfxS1)),
                                    t_action, astdAction));
                }
            }
            else {
                if(statesToActions != null) {
                    omega_loc = new SeqStatement(Arrays.asList(
                            ((statesToActions.get(s1) != null)
                                    ? this.prefixAction(statesToActions.get(s1).getExit(), ASTDTreeExit) : null),
                            t_action,
                            ((statesToActions.get(s2) != null)
                                    ? this.prefixAction(statesToActions.get(s2).getEntry(), ASTDTree) : null),
                            astdAction));
                }
                else {
                    omega_loc = new SeqStatement(Arrays.asList(t_action, astdAction));
                }
            }
        }

        List<String> whenList = t.getEvent().getWhen();
        String bool = this.prefixGuard(t.getGuard(), ASTDTree);
        if(whenList != null && !whenList.isEmpty()) {
            newList.clear();
            whenList.forEach(when -> newList.add(new Bool(prefixGuard(when, ASTDTree))));
            if(bool != null || !bool.isEmpty())
                newList.add(new Bool(bool));
        }

        //calculate the final condition for an ASTD
        Condition finalCond = null;
        if(t.isFinal()){
            //if the transition is final (isFinal == true)
            //needs to check if the transition is from an ASTD or an STATE
            if(statesToASTDs.containsKey(s1)){
//                //s1 is a ASTD
//                //if s1 is deep final -> Deep final means that the transition can only fire from the final state.
//                if(deepFinalStates != null && deepFinalStates.contains(s1)){
//                    Condition finalCpy = (Condition) Utils.copyObject(stateToASTDs.get(s1)._final(ASTDTree));
//                    finalCond = new AndCondition(Arrays.asList(
//                            new CallCondition(Conventions.EQUALS, Arrays.asList(prfx + autState, s1)),
//                            finalCpy
//                    ));
//                }
//                //if s1 is not deep final, then it is shallow final
//                //Shallow final means that any state of the sub-ASTD is final, so it only need to be in the state.
//                if(shallowFinalStates != null && shallowFinalStates.contains(s1)){
//                    finalCond = new AndCondition(Arrays.asList(
//                            new CallCondition(Conventions.EQUALS, Arrays.asList(prfx + autState, s1))
//                    ));
//                }

                //s1 is a ASTD
                //transition shall fire from a final state.
                Condition finalCpy = (Condition) Utils.copyObject(stateToASTDs.get(s1)._final(ASTDTree));
                finalCond = new AndCondition(Arrays.asList(
                        new CallCondition(Conventions.EQUALS, Arrays.asList(prfx + autState, s1)),
                        finalCpy
                ));
            }
            else{
                //s1 is an elementary STATE, check if it is final
                if(this.shallowFinalStates != null && this.shallowFinalStates.contains(s1)){
                    finalCond = new AndCondition(Arrays.asList(
                            new CallCondition(Conventions.EQUALS, Arrays.asList(prfx + autState, s1))
                    ));
                }
                else{
                    //impossible transition
                    finalCond = new AndCondition(Arrays.asList(
                            new CallCondition(Conventions.EQUALS, Arrays.asList(prfx + autState, s1)),
                            new CallCondition(Conventions.EQUALS, Arrays.asList("1", "0"))
                    ));
                }
            }
        }
        if(finalCond == null){
            c_loc = new CallCondition(Conventions.EQUALS, Arrays.asList(prfx + autState, s1));
        }
        else{
            c_loc = finalCond;
        }
        if(bool == null || bool.isEmpty()) {
            if(Constants.COND_OPT_OPTS) {
                c_loc = new Bool(prfx + "cond_" + i);
            }
            if(!newList.isEmpty()) {
                if (omega_loc == null) {
                    autStmt.add(new Entry<>(new AndCondition(Arrays.asList(c_loc, new AndCondition(newList))), t_loc));
                } else {
                    autStmt.add(new Entry<>(new AndCondition(Arrays.asList(c_loc, new AndCondition(newList))),
                                                new SeqStatement(Arrays.asList(omega_loc, t_loc))));
                }
            }
            else {
                if (omega_loc == null) {
                    autStmt.add(new Entry<>(c_loc, t_loc));
                } else {
                    autStmt.add(new Entry<>(c_loc, new SeqStatement(Arrays.asList(omega_loc, t_loc))));
                }
            }
            if(!Constants.COND_OPT_OPTS) {
                if(!newList.isEmpty()) {
                    cond.add(new AndCondition(Arrays.asList(c_loc, new AndCondition(newList))));
                }
                else {
                    cond.add(c_loc);
                }
            }
            else {
                if(!newList.isEmpty()) {
                    cond.add(new CallCondition(-1, Conventions.EQUALS1, Arrays.asList(prfx + "cond_" + i,
                                  new AndCondition(Arrays.asList(c_loc, new AndCondition(newList))))));
                    if (isRoot()) {
                        stmtList.add(new AssignStatement(new Term(prfx + "cond_" + i),
                                         new AndCondition(Arrays.asList(c_loc, new AndCondition(newList)))));
                    }
                }
                else {
                    cond.add(new CallCondition(-1, Conventions.EQUALS1, Arrays.asList(prfx + "cond_" + i, c_loc)));
                    if (isRoot()) {
                        stmtList.add(new AssignStatement(new Term(prfx + "cond_" + i), c_loc));
                    }
                }
            }
        }
        else  {
            if(Constants.COND_OPT_OPTS) {
                c_loc = new Bool(prfx + "cond_" + i);
            }
            if (omega_loc == null) {
                autStmt.add(new Entry<>(new AndCondition(Arrays.asList(c_loc,
                                           (!newList.isEmpty()) ? new AndCondition(newList) : new Bool(bool))), t_loc));
            } else {
                autStmt.add(new Entry<>(new AndCondition(Arrays.asList(c_loc,
                                            (!newList.isEmpty()) ? new AndCondition(newList) : new Bool(bool))),
                        new SeqStatement(Arrays.asList(omega_loc, t_loc))));
            }
            if(!Constants.COND_OPT_OPTS) {
                cond.add(new AndCondition(Arrays.asList(c_loc,
                             (!newList.isEmpty()) ? new AndCondition(newList) : new Bool(bool))));
            }
            else {
                c_loc = new CallCondition(Conventions.EQUALS, Arrays.asList(prfx + autState, s1));
                cond.add(new CallCondition(-1, Conventions.EQUALS1, Arrays.asList(prfx + "cond_" + i,
                             new AndCondition(Arrays.asList(c_loc,
                                 (!newList.isEmpty()) ? new AndCondition(newList) : new Bool(bool))))));
                if(isRoot()) {
                    stmtList.add(new AssignStatement(new Term(prfx + "cond_" + i),
                            new AndCondition(Arrays.asList(c_loc,
                                    (!newList.isEmpty()) ? new AndCondition(newList) : new Bool(bool)))));
                }
            }
        }
    }

    private void trans_tosub(String prfx,
                             Arrow arrow,
                             String autState,
                             Automaton autS2,
                             Map<String, ASTD> stateToASTDs,
                             Map<String, ActionSet> statesToActions,
                             Action t_action,
                             Action astdAction,
                             Transition t,
                             List<Condition> newList,
                             List<Entry<Condition, Statement>> autStmt,
                             List<Condition> cond,
                             List<Statement> stmtList,
                             ArrayList<ASTD> callList,
                             int i,
                             String lets) {

        ToSub tosub = (ToSub) arrow;
        String s1 = tosub.getS1(), s2b = tosub.getS2b(), s2 = tosub.getS2();
        Condition c_tosub;

        //TODO: CHANGE THIS TO PREFIX TREE
        String prfxS1 = getParent().prefix(s1);

        ArrayList<ASTD> ASTDTreeExit = new ArrayList<>();
        ASTDTreeExit.addAll(callList);
        ArrayList<ASTD> ASTDTree = new ArrayList<>();
        ASTDTree.addAll(callList);
        //ASTDTreeExit.add(s1);

        Map<String, ASTD> stateToASTDb = autS2.getStatesToASTDs();
        Map<String, ActionSet> stateToActionb = autS2.getStatesToActions();

        //TODO:CHANGE THIS PREFIX TO PREFIXTREE
        String pfxS2 = getParent().prefix(s2) + ".";

        SeqStatement t_tosub = new SeqStatement(Arrays.asList(
                                    new AssignStatement(new Term(prfx + autState), new Term(s2)),
                                    new AssignStatement(new Term(pfxS2 + autState), new Term(s2b)),
                                    (stateToASTDb != null && stateToASTDb.get(s2b) != null)
                                            ? stateToASTDb.get(s2b).init(ASTDTree, lets) : null,
                                    new AssignStatement(new Term(Conventions.EXEC), new Term(Conventions.TRUE))));
        //it's a shallow history state
        if(s2.compareTo("H") == 0) {
            t_tosub = new SeqStatement(Arrays.asList(
                            new AssignStatement(new Term(prfx + autState), new Term(s2)),
                            (stateToASTDb != null && stateToASTDb.get(s2b) != null)
                                    ? stateToASTDb.get(s2b).init(ASTDTree, lets) : null,
                            new AssignStatement(new Term(Conventions.EXEC), new Term(Conventions.TRUE))));
        }
        //it's a deep history state
        else if(s2.contains("H"))  {
            t_tosub = new SeqStatement(Arrays.asList(
                            new AssignStatement(new Term(prfx + autState), new Term(s2)),
                            new AssignStatement(new Term(pfxS2), new Term(prfx + Conventions.getHistoryCall(s1))),
                            new AssignStatement(new Term(Conventions.EXEC), new Term(Conventions.TRUE))));
        }
        Action autS2Action = autS2.prefixAction(autS2.getAstdAction(), ASTDTree);
        if(autS2Action != null && Constants.DEBUG) {
            String debugMsg = ";\n" + Constants.LOGGER_MSG.replace("$", autS2Action.getCode());
            autS2Action = new Action(autS2Action.getCode() + debugMsg);
        }
        SeqStatement omega_tosub = null;
        if(s1 == s2) {
            if(hasHistoryState()) {
                if(statesToActions != null) {
                    if(!s2b.contains("H")) {
                        omega_tosub = new SeqStatement(Arrays.asList(
                                        ((statesToASTDs.get(s1) != null) && statesToASTDs.get(s1).hasHistoryState()) ?
                                                new AssignStatement(new Term(prfx + Conventions.getHistoryCall(s1)),
                                                                    new Term(prfxS1)) : null,
                                        ((statesToActions.get(s1) != null)
                                                ? this.prefixAction(statesToActions.get(s1).getStay(), ASTDTree) : null),
                                        autS2.init(ASTDTree, lets),
                                        ((stateToActionb.get(s2b) != null)
                                                ? autS2.prefixAction(stateToActionb.get(s2b).getEntry(), ASTDTree) : null),
                                        autS2.prefixAction(t_action, ASTDTree),
                                        autS2Action, astdAction));
                    }
                }
                else {
                    omega_tosub = new SeqStatement(Arrays.asList(
                                        new AssignStatement(new Term(prfx + Conventions.getHistoryCall(s1)),
                                                new Term(prfxS1)),
                                        autS2.init(ASTDTree, lets), autS2.prefixAction(t_action, ASTDTree), autS2Action, astdAction));
                }
            }
            else {
                if(statesToActions != null) {
                    omega_tosub = new SeqStatement(Arrays.asList(
                                        ((statesToActions.get(s1) != null)
                                                ? this.prefixAction(statesToActions.get(s1).getStay(), ASTDTree) : null),
                                        autS2.init(ASTDTree, lets),
                                        ((stateToActionb.get(s2b) != null)
                                                ? autS2.prefixAction(stateToActionb.get(s2b).getEntry(), ASTDTree) : null), autS2.prefixAction(t_action, ASTDTree),
                                        autS2Action, astdAction));
                }
                else {
                    omega_tosub = new SeqStatement(Arrays.asList( autS2.init(ASTDTree, lets),autS2.prefixAction(t_action, ASTDTree),
                                        autS2Action, astdAction));
                }
            }
        }
        else {
            if(hasHistoryState()) {
                if(statesToActions != null) {
                    if(!s2b.contains("H")) {
                        omega_tosub = new SeqStatement(Arrays.asList(
                                        ((statesToASTDs.get(s1) != null) && statesToASTDs.get(s1).hasHistoryState()) ?
                                                new AssignStatement(new Term(prfx + Conventions.getHistoryCall(s1)),
                                                                    new Term(prfxS1)) : null,
                                        ((statesToActions.get(s1) != null)
                                                ? this.prefixAction(statesToActions.get(s1).getExit(), ASTDTreeExit) : null),

                                        ((statesToActions.get(s2) != null)
                                                ? this.prefixAction(statesToActions.get(s2).getEntry(), ASTDTree) : null),
                                        autS2.init(ASTDTree, lets),
                                        ((stateToActionb.get(s2b) != null)
                                                ? autS2.prefixAction(stateToActionb.get(s2b).getEntry(), ASTDTree) : null),
                                autS2.prefixAction(t_action, ASTDTree),
                                        autS2Action, astdAction));
                    }
                }
                else {
                    omega_tosub = new SeqStatement(Arrays.asList(
                                        new AssignStatement(new Term(prfx + Conventions.getHistoryCall(s1)),
                                                new Term(prfxS1)),
                                        autS2.init(ASTDTree, lets),autS2.prefixAction(t_action, ASTDTree), autS2Action, astdAction));
                }
            }
            else {
                if(statesToActions != null) {
                    omega_tosub = new SeqStatement(Arrays.asList(
                                        ((statesToActions.get(s1) != null)
                                                ? this.prefixAction(statesToActions.get(s1).getExit(), ASTDTreeExit) : null),
                                        ((statesToActions.get(s2) != null)
                                                ? this.prefixAction(statesToActions.get(s2).getEntry(), ASTDTree) : null),
                                        autS2.init(ASTDTree, lets),
                                        ((stateToActionb.get(s2b) != null)
                                                ? autS2.prefixAction(stateToActionb.get(s2b).getEntry(), ASTDTree) : null),
                            t_action,autS2Action, astdAction));
                }
                else {
                    omega_tosub = new SeqStatement(Arrays.asList(autS2.init(ASTDTree, lets), autS2.prefixAction(t_action, ASTDTree),
                                        autS2Action, astdAction));
                }
            }
        }
        List<String> whenList = t.getEvent().getWhen();
        String bool = autS2.prefixGuard(t.getGuard(), ASTDTree);
        if(whenList != null && !whenList.isEmpty()) {
            newList.clear();
            whenList.forEach(when -> newList.add(new Bool(autS2.prefixGuard(when, ASTDTree))));
            if(bool != null || !bool.isEmpty())
                newList.add(new Bool(bool));
        }

        //calculate the final condition for an ASTD
        Condition finalCond = null;
        if(t.isFinal()){
            //if the transition is final (isFinal == true)
            //needs to check if the transition is from an ASTD or an STATE
            if(statesToASTDs.containsKey(s1)){
//                //s1 is a ASTD
//                //if s1 is deep final -> Deep final means that the transition can only fire from the final state.
//                if(deepFinalStates != null && deepFinalStates.contains(s1)){
//                    Condition finalCpy = (Condition) Utils.copyObject(stateToASTDs.get(s1)._final(ASTDTree));
//                    finalCond = new AndCondition(Arrays.asList(
//                            new CallCondition(Conventions.EQUALS, Arrays.asList(prfx + autState, s1)),
//                            finalCpy
//                    ));
//                }
//                //if s1 is not deep final, then it is shallow final
//                //Shallow final means that any state of the sub-ASTD is final, so it only need to be in the state.
//                if(shallowFinalStates != null && shallowFinalStates.contains(s1)){
//                    finalCond = new AndCondition(Arrays.asList(
//                            new CallCondition(Conventions.EQUALS, Arrays.asList(prfx + autState, s1))
//                    ));
//                }

                //s1 is a ASTD
                //transition shall fire from a final state.
                Condition finalCpy = (Condition) Utils.copyObject(stateToASTDs.get(s1)._final(ASTDTree));
                finalCond = new AndCondition(Arrays.asList(
                        new CallCondition(Conventions.EQUALS, Arrays.asList(prfx + autState, s1)),
                        finalCpy
                ));
            }
            else{
                //s1 is an elementary STATE, check if it is final
                if(this.shallowFinalStates != null && this.shallowFinalStates.contains(s1)){
                    finalCond = new AndCondition(Arrays.asList(
                            new CallCondition(Conventions.EQUALS, Arrays.asList(prfx + autState, s1))
                    ));
                }
                else{
                    //impossible transition
                    finalCond = new AndCondition(Arrays.asList(
                            new CallCondition(Conventions.EQUALS, Arrays.asList(prfx + autState, s1)),
                            new CallCondition(Conventions.EQUALS, Arrays.asList("1", "0"))
                    ));
                }
            }
        }
        if(finalCond == null){
            c_tosub = new CallCondition(Conventions.EQUALS, Arrays.asList(prfx + autState, s1));
        }
        else{
            c_tosub = finalCond;
        }


        if(bool == null || bool.isEmpty()) {
            if (!Constants.COND_OPT_OPTS) {
                if(!newList.isEmpty()) {
                    autStmt.add(new Entry<>(new AndCondition(Arrays.asList(c_tosub, new AndCondition(newList))),
                                            new SeqStatement(Arrays.asList(omega_tosub, t_tosub))));
                    cond.add(new AndCondition(Arrays.asList(c_tosub, new AndCondition(newList))));
                }
                else{
                    autStmt.add(new Entry<>(c_tosub, new SeqStatement(Arrays.asList(omega_tosub, t_tosub))));
                    cond.add(c_tosub);
                }
            } else {
                c_tosub = new Bool(prfx + "cond_" + i);
                if(!newList.isEmpty()) {
                    autStmt.add(new Entry<>(new AndCondition(Arrays.asList(c_tosub, new AndCondition(newList))),
                                            new SeqStatement(Arrays.asList(omega_tosub, t_tosub))));
                    c_tosub = new CallCondition(Conventions.EQUALS, Arrays.asList(prfx + autState, s1));
                    cond.add(new CallCondition(-1, Conventions.EQUALS1,
                            Arrays.asList(prfx + "cond_" + i, new AndCondition(Arrays.asList(c_tosub,
                                                                                new AndCondition(newList))))));
                    if (isRoot()) {
                        stmtList.add(new AssignStatement(new Term(prfx + "cond_" + i),
                                         new AndCondition(Arrays.asList(c_tosub, new AndCondition(newList)))));
                    }
                }
                else {
                    autStmt.add(new Entry<>(c_tosub, new SeqStatement(Arrays.asList(omega_tosub, t_tosub))));
                    c_tosub = new CallCondition(Conventions.EQUALS, Arrays.asList(prfx + autState, s1));
                    cond.add(new CallCondition(-1, Conventions.EQUALS1, Arrays.asList(prfx + "cond_" + i, c_tosub)));
                    if (isRoot()) {
                        stmtList.add(new AssignStatement(new Term(prfx + "cond_" + i), c_tosub));
                    }
                }
            }
        }
        else {
            if (!Constants.COND_OPT_OPTS) {
                autStmt.add(new Entry<>(new AndCondition(Arrays.asList(c_tosub,
                                            (!newList.isEmpty()) ? new AndCondition(newList) : new Bool(bool))),
                        new SeqStatement(Arrays.asList(omega_tosub, t_tosub))));
                cond.add(new AndCondition(Arrays.asList(c_tosub,
                             (!newList.isEmpty()) ? new AndCondition(newList) : new Bool(bool))));
            } else {
                c_tosub = new Bool(prfx + "cond_" + i);
                if(!newList.isEmpty()) {
                    autStmt.add(new Entry<>(new AndCondition(Arrays.asList(c_tosub, new AndCondition(newList))),
                                            new SeqStatement(Arrays.asList(omega_tosub, t_tosub))));
                }
                else {
                    autStmt.add(new Entry<>(c_tosub, new SeqStatement(Arrays.asList(omega_tosub, t_tosub))));
                }
                c_tosub = new CallCondition(Conventions.EQUALS, Arrays.asList(prfx + autState, s1));
                cond.add(new CallCondition(-1, Conventions.EQUALS1, Arrays.asList(prfx + "cond_" + i,
                              new AndCondition(Arrays.asList(c_tosub,
                                  (!newList.isEmpty()) ? new AndCondition(newList) : new Bool(bool))))));
                if (isRoot()) {
                    stmtList.add(new AssignStatement(new Term(prfx + "cond_" + i),
                                     new AndCondition(Arrays.asList(c_tosub,
                                         (!newList.isEmpty()) ? new AndCondition(newList) : new Bool(bool)))));
                }
            }
        }
    }

    private void trans_fsub(String prfx,
                             Arrow arrow,
                             String autState,
                             Automaton autS1,
                             Map<String, ASTD> stateToASTDs,
                             Map<String, ActionSet> statesToActions,
                             Action t_action,
                             Action astdAction,
                             Transition t,
                             List<Condition> newList,
                             List<Entry<Condition, Statement>> autStmt,
                             List<Condition> cond,
                             List<Statement> stmtList,
                            ArrayList<ASTD> callList,
                             int i,
                            String lets) {
        FromSub fsub = (FromSub) arrow;
        String s1 = fsub.getS1(), s1b = fsub.getS1b(), s2 = fsub.getS2();
        Map<String, ActionSet> stateToActionb = autS1.getStatesToActions();

        ArrayList<ASTD> ASTDTreeExit = new ArrayList<>();
        ASTDTreeExit.addAll(callList);
        ArrayList<ASTD> ASTDTree = new ArrayList<>();
        ASTDTree.addAll(callList);
        //ASTDTreeExit.add(s1);

        //TODO: CHANGE THOSE PREFIXES TO PREFIX TREE
        String pfxS1b = getParent().prefix(s1b);
        String pfxS1 = getParent().prefix(s1) + ".";
        Condition c_fsub;

        SeqStatement t_fsub = new SeqStatement(Arrays.asList(
                new AssignStatement(new Term(prfx + autState), new Term(s2)),
                (stateToASTDs != null && stateToASTDs.get(s2) != null)
                        ? stateToASTDs.get(s2).init(ASTDTree, lets) : null,
                new AssignStatement(new Term(Conventions.EXEC), new Term(Conventions.TRUE))));

        Action autS1Action = autS1.prefixAction(autS1.getAstdAction(), ASTDTree);
        if(autS1Action != null && Constants.DEBUG) {
            String debugMsg = ";\n" + Constants.LOGGER_MSG.replace("$", autS1Action.getCode());
            autS1Action = new Action(autS1Action.getCode() + debugMsg);
        }
        SeqStatement omega_fsub;
        if(hasHistoryState()) {
            if(statesToActions != null) {
                omega_fsub = new SeqStatement(Arrays.asList(
                                ((stateToActionb.get(s1b) != null)
                                        ? stateToActionb.get(s1b).getExit() : null),
                                ((statesToASTDs.get(s1) != null) && statesToASTDs.get(s1).hasHistoryState()) ?
                                        new AssignStatement(new Term(prfx + Conventions.getHistoryCall(s1)),
                                                            new Term(pfxS1b)) : null,
                                autS1Action,
                                ((statesToActions.get(s1) != null)
                                        ? this.prefixAction(statesToActions.get(s1).getExit(), ASTDTreeExit) : null),
                        t_action,
                                ((statesToActions.get(s2) != null)
                                        ? this.prefixAction(statesToActions.get(s2).getEntry(), ASTDTree) : null),
                                astdAction));
            }
            else {
                omega_fsub = new SeqStatement(Arrays.asList(
                                    new AssignStatement(new Term(prfx + Conventions.getHistoryCall(s1)),
                                            new Term(pfxS1b)),
                                    autS1Action, autS1.prefixAction(t_action, ASTDTree), astdAction));
            }
        }
        else {
            if(statesToActions != null) {
                omega_fsub = new SeqStatement(Arrays.asList(
                                    ((stateToActionb.get(s1b) != null)
                                            ? autS1.prefixAction(stateToActionb.get(s1b).getExit(), ASTDTreeExit) : null),
                                    autS1Action,
                                    ((statesToActions.get(s1) != null)
                                            ? this.prefixAction(statesToActions.get(s1).getExit(), ASTDTreeExit) : null),
                                    t_action,
                                    ((statesToActions.get(s2) != null)
                                            ? this.prefixAction(statesToActions.get(s2).getEntry(), ASTDTree) : null),
                                    astdAction));
            }
            else {
                omega_fsub = new SeqStatement(Arrays.asList(autS1Action, t_action, astdAction));
            }
        }

        List<String> whenList = t.getEvent().getWhen();
        String bool = autS1.prefixGuard(t.getGuard(), ASTDTree);
        if(whenList != null && !whenList.isEmpty()) {
            newList.clear();
            whenList.forEach(when -> newList.add(new Bool(autS1.prefixGuard(when, ASTDTree))));
            if(bool != null || !bool.isEmpty())
                newList.add(new Bool(bool));
        }

        //calculate the final condition for an ASTD
        Condition finalCond = null;
        if(t.isFinal()){
            //if the transition is final (isFinal == true)
            //needs to check if the transition is from an ASTD or an STATE
            if(statesToASTDs.containsKey(s1)){
//                //s1 is a ASTD
//                //if s1 is deep final -> Deep final means that the transition can only fire from the final state.
//                if(deepFinalStates != null && deepFinalStates.contains(s1)){
//                    Condition finalCpy = (Condition) Utils.copyObject(stateToASTDs.get(s1)._final(ASTDTree));
//                    finalCond = new AndCondition(Arrays.asList(
//                            new CallCondition(Conventions.EQUALS, Arrays.asList(prfx + autState, s1)),
//                            finalCpy
//                    ));
//                }
//                //if s1 is not deep final, then it is shallow final
//                //Shallow final means that any state of the sub-ASTD is final, so it only need to be in the state.
//                if(shallowFinalStates != null && shallowFinalStates.contains(s1)){
//                    finalCond = new AndCondition(Arrays.asList(
//                            new CallCondition(Conventions.EQUALS, Arrays.asList(prfx + autState, s1))
//                    ));
//                }

                //s1 is a ASTD
                //transition shall fire from a final state.
                Condition finalCpy = (Condition) Utils.copyObject(stateToASTDs.get(s1)._final(ASTDTree));
                finalCond = new AndCondition(Arrays.asList(
                        new CallCondition(Conventions.EQUALS, Arrays.asList(prfx + autState, s1)),
                        finalCpy
                ));
            }
            else{
                //s1 is an elementary STATE, check if it is final
                if(this.shallowFinalStates != null && this.shallowFinalStates.contains(s1)){
                    finalCond = new AndCondition(Arrays.asList(
                            new CallCondition(Conventions.EQUALS, Arrays.asList(prfx + autState, s1))
                    ));
                }
                else{
                    //impossible transition
                    finalCond = new AndCondition(Arrays.asList(
                            new CallCondition(Conventions.EQUALS, Arrays.asList(prfx + autState, s1)),
                            new CallCondition(Conventions.EQUALS, Arrays.asList("1", "0"))
                    ));
                }
            }
        }
        if(finalCond == null){
            c_fsub = new AndCondition(Arrays.asList(new CallCondition(Conventions.EQUALS,
                            Arrays.asList(prfx + autState, s1)),
                    new CallCondition(Conventions.EQUALS, Arrays.asList(pfxS1 + autState, s1b))));
        }
        else{
            c_fsub = finalCond;
        }


        if(bool == null || bool.isEmpty()) {
            if (!Constants.COND_OPT_OPTS) {
                if(!newList.isEmpty()) {
                    autStmt.add(new Entry<>(new AndCondition(Arrays.asList(c_fsub, new AndCondition(newList))),
                                    new SeqStatement(Arrays.asList(omega_fsub, t_fsub))));
                    cond.add(new AndCondition(Arrays.asList(c_fsub, new AndCondition(newList))));
                }
                else {
                    autStmt.add(new Entry<>(c_fsub, new SeqStatement(Arrays.asList(omega_fsub, t_fsub))));
                    cond.add(c_fsub);
                }
            } else {
                if(!newList.isEmpty()) {
                    c_fsub = new Bool(prfx + "cond_" + i);
                    autStmt.add(new Entry<>(new AndCondition(Arrays.asList(c_fsub, new AndCondition(newList))),
                                                new SeqStatement(Arrays.asList(omega_fsub, t_fsub))));
                    c_fsub = new AndCondition(Arrays.asList(new CallCondition(Conventions.EQUALS,
                                            Arrays.asList(prfx + autState, s1)),
                                    new CallCondition(Conventions.EQUALS, Arrays.asList(pfxS1 + autState, s1b))));
                    cond.add(new CallCondition(-1, Conventions.EQUALS1, Arrays.asList(prfx + "cond_" + i,
                                  new AndCondition(Arrays.asList(c_fsub, new AndCondition(newList))))));
                    if (isRoot()) {
                        stmtList.add(new AssignStatement(new Term(prfx + "cond_" + i),
                                         new AndCondition(Arrays.asList(c_fsub, new AndCondition(newList)))));
                    }
                }
                else {
                    c_fsub = new Bool(prfx + "cond_" + i);
                    autStmt.add(new Entry<>(c_fsub, new SeqStatement(Arrays.asList(omega_fsub, t_fsub))));
                    c_fsub = new AndCondition(Arrays.asList(new CallCondition(Conventions.EQUALS,
                                            Arrays.asList(prfx + autState, s1)),
                                    new CallCondition(Conventions.EQUALS, Arrays.asList(pfxS1 + autState, s1b))));
                    cond.add(new CallCondition(-1, Conventions.EQUALS1,
                                               Arrays.asList(prfx + "cond_" + i, c_fsub)));
                    if (isRoot()) {
                        stmtList.add(new AssignStatement(new Term(prfx + "cond_" + i), c_fsub));
                    }
                }
            }
        }
        else {
            if (!Constants.COND_OPT_OPTS) {
                autStmt.add(new Entry<>(new AndCondition(Arrays.asList(c_fsub,
                                        (!newList.isEmpty()) ? new AndCondition(newList) : new Bool(bool))),
                                new SeqStatement(Arrays.asList(omega_fsub, t_fsub))));
                cond.add(new AndCondition(Arrays.asList(c_fsub,
                             (!newList.isEmpty()) ? new AndCondition(newList) : new Bool(bool))));
            } else {
                c_fsub = new Bool(prfx + "cond_" + i);
                if(!newList.isEmpty()) {
                    autStmt.add(new Entry<>(new AndCondition(Arrays.asList(c_fsub, new AndCondition(newList))),
                                    new SeqStatement(Arrays.asList(omega_fsub, t_fsub))));
                }
                else {
                    autStmt.add(new Entry<>(c_fsub, new SeqStatement(Arrays.asList(omega_fsub, t_fsub))));
                }
                c_fsub = new AndCondition(Arrays.asList(new CallCondition(Conventions.EQUALS,
                                        Arrays.asList(prfx + autState, s1)),
                                new CallCondition(Conventions.EQUALS, Arrays.asList(pfxS1 + autState, s1b))));
                cond.add(new CallCondition(-1, Conventions.EQUALS1, Arrays.asList(prfx + "cond_" + i,
                                new AndCondition(Arrays.asList(c_fsub,
                                    (!newList.isEmpty()) ? new AndCondition(newList) : new Bool(bool))))));
                if (isRoot()) {
                    stmtList.add(new AssignStatement(new Term(prfx + "cond_" + i),
                                     new AndCondition(Arrays.asList(c_fsub,
                                         (!newList.isEmpty()) ? new AndCondition(newList) : new Bool(bool)))));
                }
            }
        }
    }

    private Automaton getAutomatonInState(ASTD s2, List<Condition> cond){
        Automaton aut;
        if(s2 instanceof Guard){
            Guard guardS2 = (Guard) s2;
            cond.add(guardS2.getGuard());
            aut = getAutomatonInState(guardS2.getBody(), cond);
            return aut;
        }
        if(s2 instanceof PersistentGuard){
            PersistentGuard pguardS2 = (PersistentGuard) s2;
            cond.add(pguardS2.getGuard());
            aut = getAutomatonInState(pguardS2.getBody(), cond);
            return aut;
        }
        return (Automaton) s2;
    }

    @Override
    public Entry<Condition, Statement> trans_event_step(Event e, Bool timed, ArrayList<ASTD> callList, String lets) {
        ArrayList<ASTD> ASTDTree = new ArrayList<>();
        ASTDTree.addAll(callList);
        ASTDTree.add(this);
        Entry<Condition, Statement> evtBody = new Entry();
        String prfx = ASTDTree.get(0).prefixTree(ASTDTree) + ".";
        //String prfx = getParent().prefix(getName()) + ".";
        Action astdAction = this.prefixAction(getAstdAction(), ASTDTree);
        if(astdAction != null && Constants.DEBUG) {
            String debugMsg = ";\n" + Constants.LOGGER_MSG.replace(ILTranslator.USYMBOL_1, astdAction.getCode());
            astdAction = new Action(astdAction.getCode() + debugMsg);
        }

        String evtLabel = e.getName();
        String autState = Conventions.getStateVar(Automaton.class);
        Map<String, ASTD>   stateToASTDs   = getStatesToASTDs();
        Map<String, ActionSet> statesToActions = getStatesToActions();

        List<Transition> transitions = getTransitions();
        if(transitions == null)
            transitions = new ArrayList<>();
        List<Entry<Condition, Statement>> autStmt = new ArrayList<>();
        List<Condition> cond = new ArrayList<>(), newList = new ArrayList<>();
        List<Statement> stmtList = new ArrayList<>();

        int i = 0;
        for(Transition t : transitions) {
            // event appear on this transition
            Arrow arrow = t.getArrow();
            if(evtLabel.compareTo(t.getEvent().getName()) == 0) {
                Action t_action = this.prefixAction(t.getAction(), ASTDTree);
                if(t_action != null && Constants.DEBUG) {
                    String debugMsg = ";\n" + Constants.LOGGER_MSG.replace(ILTranslator.USYMBOL_1, t_action.getCode());
                    t_action = new Action(t_action.getCode() + debugMsg);
                }
                if (arrow instanceof Local) {
                    trans_local(prfx, arrow, autState, stateToASTDs, statesToActions,
                                t_action, astdAction, t, newList, autStmt, cond, stmtList, ASTDTree, i, lets);
                } else if (arrow instanceof ToSub) {
                    // to sub transition
                    ToSub tosub = (ToSub) arrow;
                    String s1 = tosub.getS1(), s2b = tosub.getS2b(), s2 = tosub.getS2();
                    Automaton autS2 = getAutomatonInState(statesToASTDs.get(s2), cond);
                    if(autS2 == null) {
                        String out = Utils.get_input_stream("[Error] Usage of ToSub is wrong. " +
                                                                  "The destination is an elementary state !");
                        if(out.compareTo("Y") == 0) {
                            Local loc = new Local(s1, s2b);
                            trans_local(prfx, loc, autState, stateToASTDs, statesToActions,
                                        t_action, astdAction, t, newList, autStmt, cond, stmtList, ASTDTree, i, lets);
                        }
                        else
                            System.exit(0);
                    }
                    else {
                        trans_tosub(prfx, tosub, autState, autS2, stateToASTDs, statesToActions,
                                    t_action, astdAction, t, newList, autStmt, cond, stmtList, ASTDTree, i, lets);
                    }
                } else if (arrow instanceof FromSub) {
                    // from sub transition
                    FromSub fsub = (FromSub) arrow;
                    String s1 = fsub.getS1(), s1b = fsub.getS1b(), s2 = fsub.getS2();
                    Automaton autS1 = getAutomatonInState(statesToASTDs.get(s1), cond);
                    if(autS1 == null) {
                        String out = Utils.get_input_stream("[Error] Usage of FromSub is wrong. " +
                                                                 "The orginator is an elementary state !");
                        if(out.compareTo("Y") == 0) {
                            Local loc = new Local(s1b, s2);
                            trans_local(prfx, loc, autState, stateToASTDs, statesToActions,
                                        t_action, astdAction, t, newList, autStmt, cond, stmtList, ASTDTree, i, lets);
                        }
                        else
                            System.exit(0);
                    }
                    else {
                        trans_fsub(prfx, arrow, autState, autS1, stateToASTDs, statesToActions,
                                   t_action, astdAction, t, newList, autStmt, cond, stmtList, ASTDTree, i, lets);
                    }
                }
                ++i;
            }
        }
        // disjunction of conditional statements
        if(!autStmt.isEmpty()) {
            evtBody.setKey(new OrCondition(cond));
            if(isRoot()) {
                stmtList.add(new IFFIStatement(autStmt));
                evtBody.setValue(new SeqStatement(stmtList));
            }
            else {
                evtBody.setValue(new IFFIStatement(autStmt));
            }
        }

        if (stateToASTDs != null && !stateToASTDs.isEmpty()) {
            for (Map.Entry<String, ASTD> stateASTD : stateToASTDs.entrySet()) {
                // condition
                Entry<Condition, Statement> evtState = stateASTD.getValue().trans_event_step(e, timed, ASTDTree, lets);
                if (!evtState.isEmpty()) {
                    if(evtBody.getValue() != null) {
                        Condition c_sub = new AndCondition(Arrays.asList(
                                              new CallCondition(Conventions.EQUALS,   
                                                                Arrays.asList(prfx + autState,
                                                                stateASTD.getKey())), evtState.getKey()));
                        Statement vStmt = evtBody.getValue();
                        if(Conventions.isIFFI(vStmt)) {
                            IFFIStatement stmt = (IFFIStatement) vStmt;
                            List<Entry<Condition, Statement>> stmtBody = stmt.getIFFIStatement();
                            stmtBody.add(new Entry(c_sub, evtState.getValue()));

                            if (!Constants.COND_OPT_OPTS) {
                                evtBody.setKey(new AndCondition(Arrays.asList(evtBody.getKey(), c_sub)));
                            } else {
                                evtBody.setKey(new CallCondition(-1, Conventions.EQUALS1,
                                                   Arrays.asList(prfx + "cond_" + stateASTD.getKey(),
                                               new AndCondition(Arrays.asList(evtBody.getKey(), c_sub)))));
                            }
                            evtBody.setValue(new IFFIStatement(stmtBody));
                        }
                        else if(Conventions.isSeqStatement(vStmt)) {
                            SeqStatement stmt = (SeqStatement) vStmt;
                            List<Statement> stmtList1 = stmt.getStatement();
                            int size = stmtList1.size();
                            IFFIStatement iffiStmt = (IFFIStatement) stmtList1.get(size-1);
                            List<Entry<Condition, Statement>> stmtBody = iffiStmt.getIFFIStatement();
                            stmtBody.add(new Entry(c_sub, evtState.getValue()));

                            if (!Constants.COND_OPT_OPTS) {
                                evtBody.setKey(new AndCondition(Arrays.asList(evtBody.getKey(), c_sub)));
                            } else {
                                evtBody.setKey(new CallCondition(-1, Conventions.EQUALS1,
                                                   Arrays.asList(prfx + "cond_" + stateASTD.getKey(),
                                               new AndCondition(Arrays.asList(evtBody.getKey(), c_sub)))));
                            }

                            stmtList1.set(size-1, new IFFIStatement(stmtBody));
                            evtBody.setValue(new SeqStatement(stmtList1));  
                        } 
                    }
                    else {
                        List<Entry<Condition, Statement>> stmtBody =  new ArrayList<>();
                        Condition c_sub = new AndCondition(Arrays.asList(
                                              new CallCondition(Conventions.EQUALS, Arrays.asList(prfx + autState,
                                                                stateASTD.getKey())), evtState.getKey()));
                        stmtBody.add(new Entry(c_sub, evtState.getValue()));

                        if(!Constants.COND_OPT_OPTS) {
                            evtBody.setKey(c_sub);
                        }
                        else {
                            evtBody.setKey(new CallCondition(-1, Conventions.EQUALS1,
                                               Arrays.asList(prfx + "cond_" + stateASTD.getKey(), c_sub)));
                        }
                        evtBody.setValue(new IFFIStatement(stmtBody));
                    }
                }
            }
        }

        return evtBody;
    }

    @Override
    public Entry<Condition, Statement> trans_event(Event e, Bool timed, ArrayList<ASTD> callList, String lets) {
        ArrayList<ASTD> ASTDTree = new ArrayList<>();
        ASTDTree.addAll(callList);
        ASTDTree.add(this);
        Entry<Condition, Statement> evtBody = new Entry();
        String prfx = ASTDTree.get(0).prefixTree(ASTDTree) + ".";
        //String prfx = getParent().prefix(getName()) + ".";
        Action astdAction = this.prefixAction(getAstdAction(), ASTDTree);
        if(astdAction != null && Constants.DEBUG) {
            String debugMsg = ";\n" + Constants.LOGGER_MSG.replace(ILTranslator.USYMBOL_1, astdAction.getCode());
            astdAction = new Action(astdAction.getCode() + debugMsg);
        }

        String evtLabel = e.getName();
        String autState = Conventions.getStateVar(Automaton.class);
        Map<String, ASTD>   stateToASTDs   = getStatesToASTDs();
        Map<String, ActionSet> statesToActions = getStatesToActions();

        List<Transition> transitions = getTransitions();
        if(transitions == null)
            transitions = new ArrayList<>();
        List<Entry<Condition, Statement>> autStmt = new ArrayList<>();
        List<Condition> cond = new ArrayList<>(), newList = new ArrayList<>();
        List<Statement> stmtList = new ArrayList<>();

        int i = 0;
        for(Transition t : transitions) {
            // event appear on this transition
            Arrow arrow = t.getArrow();
            if(evtLabel.compareTo(t.getEvent().getName()) == 0) {
                Action t_action = this.prefixAction(t.getAction(), ASTDTree);
                if(t_action != null && Constants.DEBUG) {
                    String debugMsg = ";\n" + Constants.LOGGER_MSG.replace(ILTranslator.USYMBOL_1, t_action.getCode());
                    t_action = new Action(t_action.getCode() + debugMsg);
                }
                if (arrow instanceof Local) {
                    trans_local(prfx, arrow, autState, stateToASTDs, statesToActions,
                            t_action, astdAction, t, newList, autStmt, cond, stmtList, ASTDTree, i, lets);
                } else if (arrow instanceof ToSub) {
                    // to sub transition
                    ToSub tosub = (ToSub) arrow;
                    String s1 = tosub.getS1(), s2b = tosub.getS2b(), s2 = tosub.getS2();
                    Automaton autS2 = getAutomatonInState(statesToASTDs.get(s2), cond);
                    if(autS2 == null) {
                        String out = Utils.get_input_stream("[Error] Usage of ToSub is wrong. " +
                                "The destination is an elementary state !");
                        if(out.compareTo("Y") == 0) {
                            Local loc = new Local(s1, s2b);
                            trans_local(prfx, loc, autState, stateToASTDs, statesToActions,
                                    t_action, astdAction, t, newList, autStmt, cond, stmtList, ASTDTree, i, lets);
                        }
                        else
                            System.exit(0);
                    }
                    else {
                        trans_tosub(prfx, tosub, autState, autS2, stateToASTDs, statesToActions,
                                t_action, astdAction, t, newList, autStmt, cond, stmtList, ASTDTree, i, lets);
                    }
                } else if (arrow instanceof FromSub) {
                    // from sub transition
                    FromSub fsub = (FromSub) arrow;
                    String s1 = fsub.getS1(), s1b = fsub.getS1b(), s2 = fsub.getS2();
                    Automaton autS1 = getAutomatonInState(statesToASTDs.get(s1), cond);
                    if(autS1 == null) {
                        String out = Utils.get_input_stream("[Error] Usage of FromSub is wrong. " +
                                "The originator is an elementary state !");
                        if(out.compareTo("Y") == 0) {
                            Local loc = new Local(s1b, s2);
                            trans_local(prfx, loc, autState, stateToASTDs, statesToActions,
                                    t_action, astdAction, t, newList, autStmt, cond, stmtList, ASTDTree, i, lets);
                        }
                        else
                            System.exit(0);
                    }
                    else {
                        trans_fsub(prfx, arrow, autState, autS1, stateToASTDs, statesToActions,
                                t_action, astdAction, t, newList, autStmt, cond, stmtList, ASTDTree, i, lets);
                    }
                }
                ++i;
            }
        }
        // disjunction of conditional statements
        if(!autStmt.isEmpty()) {
            evtBody.setKey(new OrCondition(cond));
            if(isRoot()) {
                stmtList.add(new IFFIStatement(autStmt));
                evtBody.setValue(new SeqStatement(stmtList));
            }
            else {
                evtBody.setValue(new IFFIStatement(autStmt));
            }
        }

        if (stateToASTDs != null) {
            for (Map.Entry<String, ASTD> stateASTD : stateToASTDs.entrySet()) {
                // condition
                Entry<Condition, Statement> evtState;
                if(e.getName().equals("Step") && (stateASTD.getValue() instanceof QInterleaving || stateASTD.getValue() instanceof QSynchronization || stateASTD.getValue() instanceof QChoice || stateASTD.getValue() instanceof QFlow)){
                    evtState = stateASTD.getValue().trans_event_step(e,timed, ASTDTree, lets);
                }
                else{
                    evtState = stateASTD.getValue().trans_event(e,timed, ASTDTree, lets);
                }
                if (!evtState.isEmpty()) {
                    if(evtBody.getValue() != null) {
                        Condition c_sub = new AndCondition(Arrays.asList(
                                new CallCondition(Conventions.EQUALS,
                                        Arrays.asList(prfx + autState,
                                                stateASTD.getKey())), evtState.getKey()));
                        Statement vStmt = evtBody.getValue();
                        if(Conventions.isIFFI(vStmt)) {
                            IFFIStatement stmt = (IFFIStatement) vStmt;
                            List<Entry<Condition, Statement>> stmtBody = stmt.getIFFIStatement();
                            stmtBody.add(new Entry(c_sub, new SeqStatement(Arrays.asList(evtState.getValue(), astdAction))));

                            if (!Constants.COND_OPT_OPTS) {
                                evtBody.setKey(new OrCondition(Arrays.asList(evtBody.getKey(), c_sub)));
                            } else {
                                evtBody.setKey(new CallCondition(-1, Conventions.EQUALS1,
                                        Arrays.asList(prfx + "cond_" + stateASTD.getKey(),
                                                new OrCondition(Arrays.asList(evtBody.getKey(), c_sub)))));
                            }
                            evtBody.setValue(new IFFIStatement(stmtBody));
                        }
                        else if(Conventions.isSeqStatement(vStmt)) {
                            SeqStatement stmt = (SeqStatement) vStmt;
                            List<Statement> stmtList1 = stmt.getStatement();
                            int size = stmtList1.size();
                            IFFIStatement iffiStmt = (IFFIStatement) stmtList1.get(size-1);
                            List<Entry<Condition, Statement>> stmtBody = iffiStmt.getIFFIStatement();
                            stmtBody.add(new Entry(c_sub, new SeqStatement(Arrays.asList(evtState.getValue(), astdAction))));

                            if (!Constants.COND_OPT_OPTS) {
                                evtBody.setKey(new AndCondition(Arrays.asList(evtBody.getKey(), c_sub)));
                            } else {
                                evtBody.setKey(new CallCondition(-1, Conventions.EQUALS1,
                                        Arrays.asList(prfx + "cond_" + stateASTD.getKey(),
                                                new AndCondition(Arrays.asList(evtBody.getKey(), c_sub)))));
                            }

                            stmtList1.set(size-1, new IFFIStatement(stmtBody));
                            evtBody.setValue(new SeqStatement(stmtList1));
                        }
                    }
                    else {
                        List<Entry<Condition, Statement>> stmtBody =  new ArrayList<>();
                        Condition c_sub = new AndCondition(Arrays.asList(
                                new CallCondition(Conventions.EQUALS, Arrays.asList(prfx + autState,
                                        stateASTD.getKey())), evtState.getKey()));
                        stmtBody.add(new Entry(c_sub, new SeqStatement(Arrays.asList(evtState.getValue(), astdAction))));

                        if(!Constants.COND_OPT_OPTS) {
                            evtBody.setKey(c_sub);
                        }
                        else {
                            evtBody.setKey(new CallCondition(-1, Conventions.EQUALS1,
                                    Arrays.asList(prfx + "cond_" + stateASTD.getKey(), c_sub)));
                        }
                        evtBody.setValue(new IFFIStatement(stmtBody));
                    }
                }
            }
        }

        return evtBody;
    }

    @Override
    public List<Function> initHistoryState() {
        if(hasHistoryState()) {
            Map<String, ASTD> stateToASTDs = getStatesToASTDs();
            List<Function> initStateList = new ArrayList<>();
            if (stateToASTDs != null) {
                for (Map.Entry<String, ASTD> stateASTD : stateToASTDs.entrySet()) {
                    ASTD subASTD = stateASTD.getValue();
                    //TODO: CHANGE THIS PREFIX TO PREFIXTREE
                    String prfx = getParent().prefix(stateASTD.getKey());
                    if (!Conventions.isQuantifiedASTD(subASTD)) {
                        //TODO: CHANGE THE NEW ARRAYLIST<> TO ASTDTree
                        SeqStatement seqStmt = new SeqStatement(Arrays.asList(subASTD.init(new ArrayList<>(), Conventions.CST),
                                                   new CallStatement(Conventions.RETURN_CALL, Arrays.asList(prfx))));
                        initStateList.add(new Function("init_" + stateASTD.getKey(),
                                                       null, Conventions.getStructName(stateASTD.getKey()),
                                                        seqStmt));
                    }
                }
            }
            return initStateList;
        }
        return null;
    }

    @Override
    public Set<Event> findAllEvents() {
        Set<Event> evts = new HashSet<>();
        List<Transition> transitions = getTransitions();
        if(transitions != null) {
            for (Transition t : transitions) {
                evts.add(t.getEvent());
            }

            Map<?, ASTD> stateToASTDs = getStatesToASTDs();
            if (stateToASTDs != null) {
                for (Map.Entry<?, ASTD> stateASTD : stateToASTDs.entrySet()) {
                    Set<Event> tmpList = stateASTD.getValue().findAllEvents();
                    if (tmpList != null && !tmpList.isEmpty())
                        evts.addAll(tmpList);
                }
            }
        }

        return evts;
    }

    @Override
    public List<String> findAllStates() {
        List<String> states = new ArrayList<>();

        states.addAll(getStateNames());
        Map<?, ASTD> stateToASTDs = getStatesToASTDs();
        if(stateToASTDs != null) {
            for (Map.Entry<?, ASTD> stateASTD : stateToASTDs.entrySet()) {
                List<String> tmpList = stateASTD.getValue().findAllStates();
                if (!tmpList.isEmpty())
                    states.addAll(tmpList);
            }
        }

        return new ArrayList<>(new HashSet<>(states));
    }

    @Override
    public boolean hasHistoryState() {
        List<String> states = getParent().findAllStates();
        for (String s : states) {
            if(s.equals("H"))
                return true;

        };
        return false;
    }


    private List<String> getHistoryStates() {
        List<Transition> trans = getTransitions();
        Set<String> hisStates = new HashSet<>();
        for (Transition t : trans) {
            Arrow arr = t.getArrow();
            if(arr instanceof Local) {
                Local loc = (Local) arr;
                if(loc.getS1().contains("H")) hisStates.add(loc.getS1());
                if(loc.getS2().contains("H")) hisStates.add(loc.getS2());
            }
            else if(arr instanceof  ToSub) {
                ToSub ts = (ToSub) arr;
                if(ts.getS1().contains("H")) hisStates.add(ts.getS1());
                if(ts.getS2().contains("H")) hisStates.add(ts.getS2());
                if(ts.getS2b().contains("H")) hisStates.add(ts.getS2b());
            }
            else if(arr instanceof  FromSub) {
                FromSub fs = (FromSub) arr;
                if(fs.getS1().contains("H")) hisStates.add(fs.getS1());
                if(fs.getS1b().contains("H")) hisStates.add(fs.getS1b());
                if(fs.getS2().contains("H")) hisStates.add(fs.getS2());
            }
        }
        return new ArrayList<>(hisStates);
    }

    @Override
    public List<Transition> findTransitions(String evtLabel) {
        List<Transition> transList = new ArrayList<>();
        List<Transition> transitions = getTransitions();
        if(transitions != null) {
            for(Transition t : transitions) {
                if(t.getEvent().getName().compareTo(evtLabel) == 0)
                    transList.addAll(transitions);
            }
        }
        Map<?, ASTD> stateToASTDs = getStatesToASTDs();
        if(stateToASTDs != null) {
            for (Map.Entry<?, ASTD> stateASTD : stateToASTDs.entrySet()) {
                List<Transition> tmpList = stateASTD.getValue().findTransitions(evtLabel);
                if (tmpList != null && !tmpList.isEmpty())
                    transList.addAll(tmpList);
            }
        }

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

        Map<?, ASTD> stateToASTDs = getStatesToASTDs();
        if (stateToASTDs != null) {
            for (Map.Entry<?, ASTD> stateASTD : stateToASTDs.entrySet()) {
                List<Variable> tmpVar = stateASTD.getValue().getAllVariables();
                if (tmpVar != null && !tmpVar.isEmpty())
                    prefAttr.addAll(tmpVar);
            }
        }

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
        if(childName == null)
            return null;

        String prefixBase = Conventions.getStructVar(getName());
        if(getName().compareTo(childName) == 0) {
            return prefixBase;
        }
        else {
            Map<String, ASTD> stateToASTDs = getStatesToASTDs();
            if(!stateToASTDs.isEmpty()) {
                for (Map.Entry<String, ASTD> stateASTD : stateToASTDs.entrySet()) {
                    String subPrefix = stateASTD.getValue().prefix(childName);
                    if (subPrefix != null) {
                        return prefixBase + "." + subPrefix;
                    }
                }
            }
            return null;
        }
    }

    @Override
    public String prefixTree(ArrayList<ASTD> CallList) {
        Automaton astd = (Automaton) CallList.get(0);
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
        Automaton astd = (Automaton) CallList.get(0);
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
        if(!ASTDTree.isEmpty()){
            Map<String, ASTD> stateToASTDs = getStatesToASTDs();
            if(stateToASTDs != null) {
                for (Map.Entry<String, ASTD> stateASTD : stateToASTDs.entrySet()) {
                    if(stateASTD.getKey().compareTo(ASTDTree.get(0).getName()) == 0) {
                        List<Variable> subASTDVariables = stateASTD.getValue().enclosingASTDVariables(ASTDTree);
                        if (subASTDVariables != null && !subASTDVariables.isEmpty()) {
                            for (Variable var : subASTDVariables) {
                                if (!enclosingTmp.contains(var)) {
                                    enclosingTmp.add(var);
                                }
                            }
                        }
                    }
                }
            }
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
            Map<String, ASTD> stateToASTDs = getStatesToASTDs();
            if (stateToASTDs != null) {
                for (Map.Entry<String, ASTD> stateASTD : stateToASTDs.entrySet()) {
                    List<Variable> subASTDVariables = stateASTD.getValue().enclosingASTDParams(ASTDTree);
                    if (subASTDVariables != null && !subASTDVariables.isEmpty()) {
                        for (Variable var : subASTDVariables) {
                            if (!enclosingTmp.contains(var)) {
                                enclosingTmp.add(var);
                            }
                        }
                    }
                }
            }
        }
        return enclosingTmp;
    }

    /*
     * @brief Returns the initial state value
     * @param  ASTD model
     * @return  A string value
     */
    @Override
    public String getInitialStateValue() {
        return getInitialState();
    }

    @Override
    public List<Variable> getAllQVariables(ArrayList<ASTD> CallList) {
        ArrayList<ASTD> ASTDTree = new ArrayList<>();
        for(int i = 1; i < CallList.size(); i++){
            ASTDTree.add(CallList.get(i));
        }
        List<Variable> list = new ArrayList<>();
        if(!ASTDTree.isEmpty()) {
            Map<String, ASTD> stateToASTDs = getStatesToASTDs();
            if(stateToASTDs != null) {
                for (Map.Entry<String, ASTD> stateASTD : stateToASTDs.entrySet()) {
                    List<Variable> tmpList = stateASTD.getValue().getAllQVariables(ASTDTree);
                    if (tmpList != null) {
                        list.addAll(tmpList);
                    }
                }
            }
        }

        return list;
    }

    @Override
    public List<Variable> getAllEventQVariables(Event e) {
        List<Variable> list = new ArrayList<>();

        Map<String, ASTD> stateToASTDs = getStatesToASTDs();
        if(stateToASTDs != null) {
            for (Map.Entry<String, ASTD> stateASTD : stateToASTDs.entrySet()) {
                List<Variable> tmpList = stateASTD.getValue().getAllEventQVariables(e);
                if (tmpList != null) {
                    list.addAll(tmpList);
                }
            }
        }

        return list;
    }

    @Override
    public List<Variable> getAllEventParams(Event e) { //MODIFIED
        List<Variable> list = new ArrayList<>();
        List<Transition> transitions = getTransitions();
        if(transitions != null) {
            for(Transition t : transitions) {
                if(t.getEvent().getName().compareTo(e.getName()) == 0) {
                    List<Variable> e_params = e.getParams();
                    for (int i = 0; i < e_params.size(); i++) {
                        list.add(e_params.get(i));
                    }
                }
            }
        }
        Map<String, ASTD> stateToASTDs = getStatesToASTDs();
        if(stateToASTDs != null) {
            for (Map.Entry<String, ASTD> stateASTD : stateToASTDs.entrySet()) {
                Set<Event> bSet = stateASTD.getValue().findAllEvents();
                if(!bSet.isEmpty()) {
                    for(Event _e : bSet) {
                        if(e.getName().compareTo(_e.getName()) == 0) {
                            List<Variable> e_params = e.getParams();
                            for (int i = 0; i < e_params.size(); i++) {
                                list.add(e_params.get(i));
                            }
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
    public Statement propertyMapping() {

        List<Statement> stmtList = new ArrayList<>();
        // generate common ASTD properties
        Statement commonProps = super.propertyMapping();
        if(commonProps != null)
            stmtList.add(commonProps);
        // generate Automaton properties
        DeclStatement decStmt = new DeclStatement(new Constant((this.getName() + "_"
                                                                + ExecSchemaParser.CURRENT_STATE_NAME).toUpperCase(),
                                                               Conventions.STRING, ExecSchemaParser.CURRENT_STATE_NAME,
                                                               getName()));
        stmtList.add(decStmt);
        if(!Constants.PROCESSED_HISTORY_STATE && hasHistoryState()) {
            List<String> hisProps = Arrays.asList(ExecSchemaParser.HISTORY, ExecSchemaParser.STATE);
            hisProps.forEach(prop -> {
                DeclStatement decStmt1 = new DeclStatement(new Constant((this.getName() + "_" + prop).toUpperCase(),
                                                                         Conventions.STRING, prop, getName()));
                stmtList.add(decStmt1);
            });
            Constants.PROCESSED_HISTORY_STATE = true;
        }
        Constants.EXEC_SCHEMA_PROPS = Constants.EXEC_SCHEMA_PROPS.replace(ILTranslator.USYMBOL_1,
                                                                          Automaton.class.getSimpleName());

        Map<String, ASTD> stateToASTDs = getStatesToASTDs();
        if(stateToASTDs != null) {
            for(Map.Entry<String, ASTD> stateASTD : stateToASTDs.entrySet()) {
                Statement subStmt = stateASTD.getValue().propertyMapping();
                stmtList.add(subStmt);
            }
        }

        return new SeqStatement(stmtList);
    }

    @Override
    public Statement currentStateToJson() {
        List<Statement> stmtList = new ArrayList<>();
        ToJson aut2json = toJson();
        Statement stmt;
        if(isRoot()) {
            stmt = topLevelStateToJson();
            aut2json.setNodeIndex(Utils.topLevelASTDIndex);
        }
        else {
            stmt = fillJSONState(aut2json.getNodeIndex());
        }
        if(stmt != null) {
            stmtList.add(stmt);
        }
        //TODO CHANGE THIS PREFIX TO PREFIXTREE
        String prfx = getParent().prefix(getName()) + ".";
        stmtList.add(fillAutProperties(aut2json.getNodeIndex(), prfx));

        Map<String, ASTD> stateToASTDs = getStatesToASTDs();
        //TODO: history state

        List<String> subIndexes = updateSubIndexes(aut2json);
        if(subIndexes != null) {
            aut2json.setSubNodeIndex(subIndexes);
            setToJson(aut2json);
        }

        if(stateToASTDs != null && !stateToASTDs.isEmpty()) {
            int it = 0;
            for(Map.Entry<String, ASTD> stateASTD : stateToASTDs.entrySet()) {
                if(stateASTD.getValue() != null) {
                    ASTD subASTD = stateASTD.getValue();
                    ToJson subToJson = subASTD.toJson();
                    subToJson.setNodeIndex(Conventions.ARRAY_ELEM
                                            .replace(ILTranslator.USYMBOL_2, aut2json.getSubNodeIndex().get(it))
                                            .replace(ILTranslator.USYMBOL_1,
                                                     ExecSchemaParser.CURRENT_SUB_STATE.toUpperCase()));
                    List<String> subIdx = subASTD.updateSubIndexes(subToJson);
                    if(subIdx != null) {
                        subToJson.setSubNodeIndex(subIdx);
                        subASTD.setToJson(subToJson);
                    }
                    Statement subStmt = subASTD.currentStateToJson();
                    if(subStmt != null)
                       stmtList.add(subStmt);
                }
                it++;
            }
        }
        else {
            stmtList.add(new AssignStatement(new Term(Conventions.ARRAY_ELEM.replace(ILTranslator.USYMBOL_2,
                                                                                     aut2json.getNodeIndex())
                                                 .replace(ILTranslator.USYMBOL_1,
                                                          ExecSchemaParser.CURRENT_SUB_STATE.toUpperCase())),
                         new Term(Conventions.JSON_OBJECT_INSTANCE)));
            stmtList.add(new AssignStatement(new Term(
                    Conventions.ARRAY_ELEM.replace(ILTranslator.USYMBOL_2,
                                                    Conventions.ARRAY_ELEM.replace(ILTranslator.USYMBOL_2,
                                                                                   aut2json.getNodeIndex())
                                                    .replace(ILTranslator.USYMBOL_1,
                                                             ExecSchemaParser.CURRENT_SUB_STATE.toUpperCase()))
                                          .replace(ILTranslator.USYMBOL_1, ExecSchemaParser.TYPE.toUpperCase())
                     ),
                    new Term("\"" + ExecSchemaParser.ELEM + "\"")));
        }
        return new SeqStatement(stmtList);
    }

    @Override
    public List<String> updateSubIndexes(ToJson obj) {
        Map<String, ASTD> stateToASTDs = getStatesToASTDs();
        ArrayList<String> arrList = new ArrayList<>();
        if(stateToASTDs != null) {
            stateToASTDs.entrySet().forEach(o -> {
                arrList.add(obj.getNodeIndex());
            });
        }
        return arrList;
    }

    private Statement fillAutProperties(String index, String prfx) {
        List<Statement> stmtList = new ArrayList<>();

        String autState = Conventions.getStateVar(Automaton.class);

        stmtList.add(new AssignStatement(new Term(Conventions.ARRAY_ELEM.replace(ILTranslator.USYMBOL_2, index)
                .replace(ILTranslator.USYMBOL_1, ExecSchemaParser.TYPE.toUpperCase())),
                new Term("\""+ ExecSchemaParser.AUTOMATON +"\"")));
        stmtList.add(new AssignStatement(new Term(Conventions.ARRAY_ELEM.replace(ILTranslator.USYMBOL_2, index)
                .replace(ILTranslator.USYMBOL_1,
                        (this.getName() + "_" + ExecSchemaParser.CURRENT_STATE_NAME).toUpperCase())),
                new Term(Conventions.ARRAY_ELEM
                        .replace(ILTranslator.USYMBOL_2,
                                Conventions.getStateVar(Automaton.class))
                        .replace(ILTranslator.USYMBOL_1, prfx + autState))));

        return new SeqStatement(stmtList);
    }

    @Override
    public List<Function> trans_quantified_condition(Event e, Bool timed, List<Variable> varList, ArrayList<ASTD> CallList) {
        ArrayList<ASTD> ASTDTree = new ArrayList<>();
        ASTDTree.addAll(CallList);
        ASTDTree.add(this);
        List<Function> aList = new ArrayList<>();
        Map<String, ASTD> stateToASTDs = getStatesToASTDs();
        if (stateToASTDs != null) {
            for (Map.Entry<?, ASTD> stateASTD : stateToASTDs.entrySet()) {
                List<Function> qList = stateASTD.getValue().trans_quantified_condition(e, timed, varList, ASTDTree);
                if (qList != null && !qList.isEmpty())
                    aList.addAll(qList);
            }
        }
        return aList;
    }

    @Override
    public List<Function> trans_quantified_condition_step(Event e, Bool timed, List<Variable> varList, ArrayList<ASTD> CallList) {
        ArrayList<ASTD> ASTDTree = new ArrayList<>();
        ASTDTree.addAll(CallList);
        ASTDTree.add(this);
        List<Function> aList = new ArrayList<>();
        Map<String, ASTD> stateToASTDs = getStatesToASTDs();
        if (stateToASTDs != null) {
            for (Map.Entry<?, ASTD> stateASTD : stateToASTDs.entrySet()) {
                List<Function> qList = stateASTD.getValue().trans_quantified_condition_step(e, timed, varList, ASTDTree);
                if (qList != null && !qList.isEmpty())
                    aList.addAll(qList);
            }
        }
        return aList;
    }

    @Override
    public List<QuantifiedASTD> findAllQASTDs() {
        List<QuantifiedASTD> aList = new ArrayList<>();
        Map<?, ASTD> stateToASTDs = getStatesToASTDs();
        if (stateToASTDs != null) {
            for (Map.Entry<?, ASTD> stateASTD : stateToASTDs.entrySet()) {
                List<QuantifiedASTD> qList = stateASTD.getValue().findAllQASTDs();
                if (qList != null && !qList.isEmpty())
                    aList.addAll(qList);
            }
        }

        return aList;
    }

    @Override
    public List<Variable> getEvtParams(Event e) {
        if(Utils.qASTDList != null && !Utils.qASTDList.isEmpty()){
            Map<?, ASTD> stateToASTDs = getStatesToASTDs();
            List<Variable> aList = new ArrayList<>();
            if (stateToASTDs != null) {
                for (Map.Entry<?, ASTD> stateASTD : stateToASTDs.entrySet()) {
                    List<Variable> pList = stateASTD.getValue().getEvtParams(e);
                    if (pList != null && !pList.isEmpty())
                        aList.addAll(pList);
                }
            }
            return aList;
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
        List<Function> bList = new ArrayList<>();
        Map<String, ASTD> stateToASTDs = getStatesToASTDs();
        if (stateToASTDs != null) {
            //final inside another ASTD
            for (Map.Entry<String, ASTD> stateASTD : stateToASTDs.entrySet()) {
                bList.addAll(stateToASTDs.get(stateASTD.getKey()).generateFinalFunc(ASTDTree));
            }
        }
        if (!bList.isEmpty())
            list.addAll(bList);

        return list;
    }
}
