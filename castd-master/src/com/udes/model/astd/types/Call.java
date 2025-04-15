package com.udes.model.astd.types;

import com.udes.model.astd.base.ASTD;
import com.udes.model.astd.base.UnaryASTD;
import com.udes.model.astd.items.Action;
import com.udes.model.astd.items.Constant;
import com.udes.model.astd.items.Event;
import com.udes.model.astd.items.Variable;
import com.udes.model.astd.tojson.ToJson;
import com.udes.model.il.conditions.AndCondition;
import com.udes.model.il.conditions.CallCondition;
import com.udes.model.il.conditions.Condition;
import com.udes.model.il.conditions.OrCondition;
import com.udes.model.il.containers.Entry;
import com.udes.model.il.conventions.Conventions;
import com.udes.model.il.record.Enum;
import com.udes.model.il.record.Record;
import com.udes.model.il.statements.*;
import com.udes.model.il.terms.Bool;
import com.udes.model.il.terms.Term;
import com.udes.parser.ExecSchemaParser;
import com.udes.translator.ILTranslator;
import com.udes.utils.Constants;
import com.udes.utils.Utils;

import javax.swing.plaf.nimbus.State;
import java.util.*;

public class Call extends UnaryASTD {

    public Call(String name, List<Variable> attributes, List<Variable> params, Action astdAction, ASTD body) {
        super(name, attributes, params, astdAction, body);
    }

    public Call(String name, ASTD body) {
        super(name, body);
    }

    public Call() {
        super();
    }

    @Override
    public Statement init(ArrayList<ASTD> callList, String lets) {
        ArrayList<ASTD> ASTDTree = new ArrayList<>();
        ASTDTree.addAll(callList);
        ASTDTree.add(this);
        List<Statement> seqList = new ArrayList<>();
        String uState = Conventions.getStateVar(Call.class);
        String prfx = ASTDTree.get(0).prefixTree(ASTDTree) + ".";
        //String prfx = getParent().prefix(getName()) + ".";
        // init state
        seqList.add(new AssignStatement(new Term(prfx + uState), new Term(Conventions.NOTCALLED)));
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
                if( init != null && init.isEmpty())
                    seqList.add(new AssignStatement(new Term(prfx + v.getName()), new Term("\"\"")));
                    //it's a json init valua
                else if(init.contains("{") && init.contains(":") && init.contains("}"))
                    seqList.add(new AssignStatement(new Term(prfx + v.getName()),
                            new Term(Conventions.JSON_PARSE
                                    .replace(ILTranslator.USYMBOL_1,
                                            "\""+init+"\""))));
                else if ( init.contains("(") && init.contains(")")) {
                    seqList.add(new AssignStatement(new Term(prfx + v.getName()),
                            new Term(init)));
                }
                else if(v.getType().compareTo(Conventions.STRING) == 0)
                    seqList.add(new AssignStatement(new Term(prfx + v.getName()), new Term("\""+init+"\"")));
                else if(v.getType().compareTo(Conventions.CLOCK) == 0){
                    seqList.add(new CallStatement(Conventions.CLOCK_RESET, Arrays.asList(prfx + v.getName(), lets)));}
                else if(v.getType().compareTo(Conventions.TIME_TYPE3) == 0 && Constants.TIMED_SIMULATION){
                    seqList.add(new CallStatement(Conventions.CLOCK_RESET, Arrays.asList(prfx + v.getName(), lets)));
                }
                else
                    seqList.add(new AssignStatement(new Term(prfx + v.getName()), new Term(init)));
            });
        }
        // init body -> Call does not initialize! Rule from TR-25 / TR-27
        //seqList.add(getBody().init());

        return new SeqStatement(seqList);
    }

    @Override
    public Statement initforsub(ArrayList<ASTD> callList, Event e, Bool timed, String lets, boolean forFinal) {
        //important for quantified ASTD with closure
        ArrayList<ASTD> ASTDTree = new ArrayList<>();
        ASTDTree.addAll(callList);
        ASTDTree.add(this);
        List<Statement> seqList = new ArrayList<>();
        String uState = Conventions.getStateVar(Call.class);
        String prfx = ASTDTree.get(0).prefixTree(ASTDTree) + ".";
        //String prfx = getParent().prefix(getName()) + ".";
        // init state
        seqList.add(new AssignStatement(new Term(prfx + uState), new Term(Conventions.NOTCALLED)));
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
                if( init != null && init.isEmpty())
                    seqList.add(new AssignStatement(new Term(prfx + v.getName()), new Term("\"\"")));
                    //it's a json init valua
                else if(init.contains("{") && init.contains(":") && init.contains("}"))
                    seqList.add(new AssignStatement(new Term(prfx + v.getName()),
                            new Term(Conventions.JSON_PARSE
                                    .replace(ILTranslator.USYMBOL_1,
                                            "\""+init+"\""))));
                else if ( init.contains("(") && init.contains(")")) {
                    seqList.add(new AssignStatement(new Term(prfx + v.getName()),
                            new Term(init)));
                }
                else if(v.getType().compareTo(Conventions.STRING) == 0)
                    seqList.add(new AssignStatement(new Term(prfx + v.getName()), new Term("\""+init+"\"")));
                else if(v.getType().compareTo(Conventions.CLOCK) == 0){
                    seqList.add(new CallStatement(Conventions.CLOCK_RESET, Arrays.asList(prfx + v.getName(), lets)));}
                else if(v.getType().compareTo(Conventions.TIME_TYPE3) == 0 && Constants.TIMED_SIMULATION){
                    seqList.add(new CallStatement(Conventions.CLOCK_RESET, Arrays.asList(prfx + v.getName(), lets)));
                }
                else
                    seqList.add(new AssignStatement(new Term(prfx + v.getName()), new Term(init)));
            });
        }

        //IF THERE IS DECLARED VARIABLES INSIDE THE BODY, IT IS NOT TOUCHED YET. GOING DEEPER FOR THOSE VARIABLES
        seqList.add(getBody().initforsub(ASTDTree, e, timed, lets, forFinal));

        return new SeqStatement(seqList);
    }

    @Override
    public Condition _final(ArrayList<ASTD> callList) {
        ArrayList<ASTD> ASTDTree = new ArrayList<>();
        ASTDTree.addAll(callList);
        ASTDTree.add(this);
        String prfx = ASTDTree.get(0).prefixTree(ASTDTree) + ".";
        //String  prfx = getParent().prefix(getName()) + ".";
        String callState = Conventions.getStateVar(Call.class);
        Condition finalCpy1 = (Condition) Utils.copyObject(getBody()._final(ASTDTree));
        Condition finalCpy2 = (Condition) Utils.copyObject(getBody()._final(ASTDTree));
        return new OrCondition(Arrays.asList(
                new AndCondition(Arrays.asList(new CallCondition(Conventions.EQUALS,
                                Arrays.asList(prfx + callState, Conventions.NOTCALLED)),
                        finalCpy1.substitute(getBody().init(ASTDTree, Conventions.CST), ASTDTree)
                                .callSubstitute(getParams(), ASTDTree))),
                new AndCondition(Arrays.asList(new CallCondition(Conventions.EQUALS,
                                Arrays.asList(prfx + callState, Conventions.CALLED)),
                        finalCpy2.callSubstitute(getParams(), ASTDTree)))));
    }

    @Override
    public Condition _finalForSub(ArrayList<ASTD> callList) {
        ArrayList<ASTD> ASTDTree = new ArrayList<>();
        ASTDTree.addAll(callList);
        ASTDTree.add(this);
        String prfx = ASTDTree.get(0).prefixTree(ASTDTree) + ".";
        //String  prfx = getParent().prefix(getName()) + ".";
        String callState = Conventions.getStateVar(Call.class);
        Condition finalCpy1 = (Condition) Utils.copyObject(getBody()._finalForSub(ASTDTree));
        Condition finalCpy2 = (Condition) Utils.copyObject(getBody()._finalForSub(ASTDTree));
        return new OrCondition(Arrays.asList(
                new AndCondition(Arrays.asList(new CallCondition(Conventions.EQUALS,
                                Arrays.asList(prfx + callState, Conventions.NOTCALLED)),
                        finalCpy1.substitute(getBody().init(ASTDTree, Conventions.CST), ASTDTree)
                                .callSubstitute(getParams(), ASTDTree))),
                new AndCondition(Arrays.asList(new CallCondition(Conventions.EQUALS,
                                Arrays.asList(prfx + callState, Conventions.CALLED)),
                        finalCpy2.callSubstitute(getParams(), ASTDTree)))));
    }

    @Override
    public Entry<List<Enum>, List<Record>> trans_type() {
        List<Record> recordList = new ArrayList<>(),
                     enumList   = new ArrayList<>();
        // struct name
        String structName = Conventions.getStructName(getName());
        List<Variable> props = new ArrayList<>();
        // astd attributes
        List<Variable> attrs = getAttributes();
        if(attrs != null)
            props.addAll(attrs);
        //astd state
        String stateType = Conventions.getStateType(Call.class);
        props.add(new Variable(Conventions.getStateVar(Call.class), stateType, null, getName()));
        enumList.add(new Enum(stateType, Arrays.asList(Conventions.NOTCALLED, Conventions.CALLED)));

        // ignore elem ASTD for optimization
        if(!Conventions.isElem(getBody())) {
            props.add(new Variable(Conventions.getStructVar(getBody().getName()),
                    Conventions.getStructName(getBody().getName()), null, getName()));
        }
        if(Constants.COND_OPT_OPTS) {
            for (int i = 1; i < 3; i++) {
                props.add(new Variable("cond_" + i, Conventions.BOOL_TYPE, null, getName()));
            }
        }
        recordList.add(new Record(structName, props));
        // struct body
        Entry<List<Enum>, List<Record>> recordBody = getBody().trans_type();
        if(!recordBody.getKey().isEmpty())
            enumList.addAll(recordBody.getKey());
        if(!recordBody.getValue().isEmpty())
            recordList.addAll(recordBody.getValue());

        return new Entry(enumList, recordList);
    }

    /*
     * @brief Get the variables of the enclosing ASTDs of an ASTD
     * @param  the parent ASTD model
     * @param  the child name
     * @return A list of variables
     */
    public List<Variable> enclosingASTDParams(ArrayList<ASTD> CallList) {
        List<Variable> enclosingBase = getParams();
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

    @Override
    public Entry<Condition, Statement> trans_event(Event e, Bool timed, ArrayList<ASTD> callList, String lets) {
        ArrayList<ASTD> ASTDTree = new ArrayList<>();
        ASTDTree.addAll(callList);
        ASTDTree.add(this);
        Entry<Condition, Statement> evtBody = new Entry();
        //String prfx = getParent().prefix(getName()) + ".";
        String prfx = ASTDTree.get(0).prefixTree(ASTDTree) + ".";
        Action astdAction = prefixAction(getAstdAction(), ASTDTree);

        if(astdAction != null && Constants.DEBUG) {
            String debugMsg = ";\n" + Constants.LOGGER_MSG.replace(ILTranslator.USYMBOL_1, astdAction.getCode());
            astdAction = new Action(astdAction.getCode() + debugMsg);
        }

        Statement initBody = getBody().init(ASTDTree, lets);

        // event body
        Entry<Condition, Statement> eBody;
        if(e.getName().equals("Step") && (getBody() instanceof QInterleaving || getBody() instanceof QSynchronization || getBody() instanceof QChoice || getBody() instanceof QFlow)){
            eBody = getBody().trans_event_step(e,timed, ASTDTree, lets);
        }
        else{
            eBody = getBody().trans_event(e,timed, ASTDTree, lets);
        }

        if(eBody != null && !eBody.isEmpty()) {
            String callState = Conventions.getStateVar(Call.class);
            // if-fi body block
            Condition c1, c2;
            Statement bdyStmt = new SeqStatement(Arrays.asList(eBody.getValue(), astdAction)),
                      bdyStmtCpy = (Statement) Utils.copyObject(bdyStmt);
            Statement stmt1 = new SeqStatement(Arrays.asList(
                                    new AssignStatement(new Term(prfx + callState), new Term(Conventions.CALLED)), initBody,
                                    bdyStmtCpy));
            Condition bdyCpy = (Condition) Utils.copyObject(eBody.getKey());
            Statement init_call = getBody().init(ASTDTree, lets);
            c1 = new AndCondition(Arrays.asList(
                    new CallCondition(Conventions.EQUALS, Arrays.asList(prfx + callState, Conventions.NOTCALLED)),
                    bdyCpy.substitute(init_call, ASTDTree)));
            c2 = new AndCondition(Arrays.asList(
                     new CallCondition(Conventions.EQUALS, Arrays.asList(prfx + callState, Conventions.CALLED)),
                    eBody.getKey()));
            Condition c1_subst;
            Condition c2_subst = null;
            Statement stmt1_subst;
            Statement stmt2_subst = null;

            if (eBody.getValue() != null) {

                c1_subst = (Condition) Utils.copyObject(c1);
                c2_subst = (Condition) Utils.copyObject(c2);

                Statement stmt2 = new SeqStatement(Arrays.asList(eBody.getValue(), astdAction));
                stmt1_subst = (Statement) Utils.copyObject(stmt1);
                stmt2_subst = (Statement) Utils.copyObject(stmt2);
            }
            else {

                c1_subst = (Condition) Utils.copyObject(c1);

                stmt1_subst = (Statement) Utils.copyObject(stmt1);
            }

            if(!Constants.COND_OPT_OPTS) {
                if (eBody.getValue() != null) {
                    evtBody.setKey(new OrCondition(Arrays.asList(c1_subst, c2_subst)));
                    evtBody.setValue(new IFFIStatement(Arrays.asList(new Entry<>(c1_subst, stmt1_subst),
                                                                     new Entry<>(c2_subst, stmt2_subst))));
                } else {
                    evtBody.setKey(c1_subst);
                    evtBody.setValue(new IFFIStatement(Arrays.asList(new Entry<>(c1_subst, stmt1_subst))));
                }
            }
            else {
                List<Statement> stmtList = new ArrayList<>();
                if (eBody.getValue() != null) {
                    if(isRoot()) {
                        stmtList.add(new AssignStatement(new Term(prfx + "cond_1"), c1_subst));
                        stmtList.add(new AssignStatement(new Term(prfx + "cond_2"), c2_subst));
                    }
                    c1_subst = new CallCondition(-1, Conventions.EQUALS1, Arrays.asList(prfx + "cond_1", c1_subst));
                    c2_subst = new CallCondition(-1, Conventions.EQUALS1, Arrays.asList(prfx + "cond_2", c2_subst));
                    evtBody.setKey(new OrCondition(Arrays.asList(c1_subst, c2_subst)));
                    c1_subst = new Bool(prfx + "cond_1");
                    c2_subst = new Bool(prfx + "cond_2");
                    if(isRoot()) {
                        stmtList.add(new IFFIStatement(Arrays.asList(new Entry<>(c1_subst, stmt1_subst),
                                                                     new Entry<>(c2_subst, stmt2_subst))));
                        evtBody.setValue(new SeqStatement(stmtList));
                    }
                    else {
                        evtBody.setValue(new IFFIStatement(Arrays.asList(new Entry<>(c1_subst, stmt1_subst),
                                                                         new Entry<>(c2_subst, stmt2_subst))));
                    }
                } else {
                    if(isRoot()) {
                        stmtList.add(new AssignStatement(new Term(prfx + "cond_1"), c1_subst));
                    }
                    c1_subst = new CallCondition(-1, Conventions.EQUALS1, Arrays.asList(prfx + "cond_1", c1_subst));
                    evtBody.setKey(c1_subst);
                    c1_subst = new Bool(prfx + "cond_1");
                    if(isRoot()) {
                        stmtList.remove(stmtList.size()-1);
                        stmtList.add(new IFFIStatement(Arrays.asList(new Entry<>(c1_subst, stmt1_subst))));
                    }
                    else {
                        evtBody.setValue(new IFFIStatement(Arrays.asList(new Entry<>(c1_subst, stmt1_subst))));
                    }
                }
            }
        }

        return evtBody;
    }

    @Override
    public Entry<Condition, Statement> trans_event_step(Event e, Bool timed, ArrayList<ASTD> callList, String lets) {
        ArrayList<ASTD> ASTDTree = new ArrayList<>();
        ASTDTree.addAll(callList);
        ASTDTree.add(this);
        Entry<Condition, Statement> evtBody = new Entry();
        //String prfx = getParent().prefix(getName()) + ".";
        String prfx = ASTDTree.get(0).prefixTree(ASTDTree) + ".";
        Action astdAction = prefixAction(getAstdAction(), ASTDTree);

        if(astdAction != null && Constants.DEBUG) {
            String debugMsg = ";\n" + Constants.LOGGER_MSG.replace(ILTranslator.USYMBOL_1, astdAction.getCode());
            astdAction = new Action(astdAction.getCode() + debugMsg);
        }

        Statement initBody = getBody().init(ASTDTree, lets);
        List<Variable> callParams = getParams();
        Statement callStmt = createStatementFromParams(callParams);

        // event body
        Entry<Condition, Statement> eBody = getBody().trans_event_step(e, timed, ASTDTree, lets);

        if(eBody != null && !eBody.isEmpty()) {
            String callState = Conventions.getStateVar(Call.class);
            // if-fi body block
            Condition c1, c2;
            Statement bdyStmt = new SeqStatement(Arrays.asList(eBody.getValue(), astdAction)),
                    bdyStmtCpy = (Statement) Utils.copyObject(bdyStmt);
            Statement stmt1 = new SeqStatement(Arrays.asList(
                    new AssignStatement(new Term(prfx + callState), new Term(Conventions.CALLED)), initBody,
                    bdyStmtCpy));
            Condition bdyCpy = (Condition) Utils.copyObject(eBody.getKey());
            Statement init_call = getBody().init(ASTDTree, lets);
            c1 = new AndCondition(Arrays.asList(
                    new CallCondition(Conventions.EQUALS, Arrays.asList(prfx + callState, Conventions.NOTCALLED)),
                    bdyCpy.substitute(init_call, ASTDTree)));
            c2 = new AndCondition(Arrays.asList(
                    new CallCondition(Conventions.EQUALS, Arrays.asList(prfx + callState, Conventions.CALLED)),
                    eBody.getKey()));
            Condition c1_copy;
            Condition c2_copy;
            Condition c1_subst;
            Condition c2_subst = null;
            Statement stmt1_copy;
            Statement stmt2_copy;
            Statement stmt1_subst;
            Statement stmt2_subst = null;

            if (eBody.getValue() != null) {

                c1_subst = ((Condition) Utils.copyObject(c1)).callSubstitute(callParams, ASTDTree);
                c2_subst = ((Condition) Utils.copyObject(c2)).callSubstitute(callParams, ASTDTree);

                Statement stmt2 = new SeqStatement(Arrays.asList(eBody.getValue(), astdAction));
                stmt1_subst = ((Statement) Utils.copyObject(stmt1)).substitute(callParams, ASTDTree);
                stmt2_subst = ((Statement) Utils.copyObject(stmt2)).substitute(callParams, ASTDTree);
            }
            else {

                c1_subst = ((Condition) Utils.copyObject(c1)).callSubstitute(callParams, ASTDTree);

                stmt1_subst = ((Statement) Utils.copyObject(stmt1)).substitute(callParams, ASTDTree);
            }

            if(!Constants.COND_OPT_OPTS) {
                if (eBody.getValue() != null) {
                    evtBody.setKey(new OrCondition(Arrays.asList(c1_subst, c2_subst)));
                    evtBody.setValue(new IFFIStatement(Arrays.asList(new Entry<>(c1_subst, stmt1_subst),
                            new Entry<>(c2_subst, stmt2_subst))));
                } else {
                    evtBody.setKey(c1_subst);
                    evtBody.setValue(new IFFIStatement(Arrays.asList(new Entry<>(c1_subst, stmt1_subst))));
                }
            }
            else {
                List<Statement> stmtList = new ArrayList<>();
                if (eBody.getValue() != null) {
                    if(isRoot()) {
                        stmtList.add(new AssignStatement(new Term(prfx + "cond_1"), c1_subst));
                        stmtList.add(new AssignStatement(new Term(prfx + "cond_2"), c2_subst));
                    }
                    c1_subst = new CallCondition(-1, Conventions.EQUALS1, Arrays.asList(prfx + "cond_1", c1_subst));
                    c2_subst = new CallCondition(-1, Conventions.EQUALS1, Arrays.asList(prfx + "cond_2", c2_subst));
                    evtBody.setKey(new OrCondition(Arrays.asList(c1_subst, c2_subst)));
                    c1_subst = new Bool(prfx + "cond_1");
                    c2_subst = new Bool(prfx + "cond_2");
                    if(isRoot()) {
                        stmtList.add(new IFFIStatement(Arrays.asList(new Entry<>(c1_subst, stmt1_subst),
                                new Entry<>(c2_subst, stmt2_subst))));
                        evtBody.setValue(new SeqStatement(stmtList));
                    }
                    else {
                        evtBody.setValue(new IFFIStatement(Arrays.asList(new Entry<>(c1_subst, stmt1_subst),
                                new Entry<>(c2_subst, stmt2_subst))));
                    }
                } else {
                    if(isRoot()) {
                        stmtList.add(new AssignStatement(new Term(prfx + "cond_1"), c1_subst));
                    }
                    c1_subst = new CallCondition(-1, Conventions.EQUALS1, Arrays.asList(prfx + "cond_1", c1_subst));
                    evtBody.setKey(c1_subst);
                    c1_subst = new Bool(prfx + "cond_1");
                    if(isRoot()) {
                        stmtList.remove(stmtList.size()-1);
                        stmtList.add(new IFFIStatement(Arrays.asList(new Entry<>(c1_subst, stmt1_subst))));
                    }
                    else {
                        evtBody.setValue(new IFFIStatement(Arrays.asList(new Entry<>(c1_subst, stmt1_subst))));
                    }
                }
            }
        }

        return evtBody;
    }

    private Statement createStatementFromParams(List<Variable> params) {
        List<Statement> stmtList = new ArrayList<>();
        params.forEach(v -> {
            stmtList.add(new AssignStatement(new Term(v.getName()), new Term(v.getInit().toString())));
        });

        return new SeqStatement(stmtList);
    }

    @Override
    public String getInitialStateValue() {
        return Conventions.NOTCALLED;
    }

    @Override
    public Statement propertyMapping() {

        List<Statement> stmtList = new ArrayList<>();
        // generate common ASTD properties
        Statement commonProps = super.propertyMapping();
        if(commonProps != null)
            stmtList.add(commonProps);

        // generate Call properties
        List<String> callProps = Arrays.asList(ExecSchemaParser.STARTED, ExecSchemaParser.CALLED_ASTD);

        callProps.forEach(prop -> {
            DeclStatement decStmt = new DeclStatement(new Constant((this.getName() + "_" + prop).toUpperCase(),
                                                      Conventions.STRING, prop, getName()));
            stmtList.add(decStmt);
        });

        Constants.EXEC_SCHEMA_PROPS = Constants.EXEC_SCHEMA_PROPS.replace(ILTranslator.USYMBOL_1,
                                                                          Call.class.getSimpleName());
        Statement body = getBody().propertyMapping();
        stmtList.add(body);

        return new SeqStatement(stmtList);
    }

    @Override
    public Statement currentStateToJson() {
        List<Statement> stmtList = new ArrayList<>();
        ToJson tojson = toJson();
        Statement stmt;
        if(isRoot()) {
            stmt = topLevelStateToJson();
            tojson.setNodeIndex(Utils.topLevelASTDIndex);
        }
        else {
            stmt = fillJSONState(tojson.getNodeIndex());
        }
        if(stmt != null) {
            stmtList.add(stmt);
        }
        //TODO: CHANGE THIS PREFIX TO PREFIXTREE
        String prfx = getParent().prefix(getName()) + ".";
        stmtList.add(fillCallProperties(tojson.getNodeIndex(), prfx));

        List<String> subIndexes = updateSubIndexes(tojson);
        if(subIndexes != null) {
            tojson.setSubNodeIndex(subIndexes);
            setToJson(tojson);
        }

        ASTD subASTD = getBody();
        ToJson subToJson = subASTD.toJson();
        if(tojson.getSubNodeIndex() != null) {
            subToJson.setNodeIndex(Conventions.ARRAY_ELEM
                    .replace(ILTranslator.USYMBOL_2, tojson.getSubNodeIndex().get(0))
                    .replace(ILTranslator.USYMBOL_1,
                             (this.getName() + "_" + ExecSchemaParser.CALLED_ASTD).toUpperCase()));
        }
        List<String> subIdx = subASTD.updateSubIndexes(subToJson);
        if(subIdx != null) {
            subToJson.setSubNodeIndex(subIdx);
            subASTD.setToJson(subToJson);
        }
        Statement subStmt = subASTD.currentStateToJson();
        if(subStmt != null)
            stmtList.add(subStmt);

        return new SeqStatement(stmtList);
    }

    private Statement fillCallProperties(String index, String prfx) {
        List<Statement> stmtList = new ArrayList<>();

        String callState = Conventions.getStateVar(Call.class);

        stmtList.add(new AssignStatement(new Term(Conventions.ARRAY_ELEM.replace(ILTranslator.USYMBOL_2, index)
                .replace(ILTranslator.USYMBOL_1, ExecSchemaParser.TYPE.toUpperCase())),
                new Term("\""+ ExecSchemaParser.CALL +"\"")));
        stmtList.add(new AssignStatement(new Term(Conventions.ARRAY_ELEM.replace(ILTranslator.USYMBOL_2, index)
                .replace(ILTranslator.USYMBOL_1,
                        (this.getName() + "_" + ExecSchemaParser.STARTED).toUpperCase())),
                new Term("(std::string(" + Conventions.ARRAY_ELEM
                         .replace(ILTranslator.USYMBOL_2,
                                  Conventions.getStateVar(Call.class))
                         .replace(ILTranslator.USYMBOL_1, prfx + callState)
                         + ").compare(\"" + Conventions.CALLED + "\") == 0) ? true : false")));

        return new SeqStatement(stmtList);
    }
}


