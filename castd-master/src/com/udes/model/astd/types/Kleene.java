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

import java.io.Serializable;
import java.util.*;

public class Kleene extends UnaryASTD {

    public Kleene(String name,
                  List<Variable> attributes,
                  List<Variable> params,
                  Action astdAction,
                  ASTD body) {
        super(name, attributes, params, astdAction, body);
    }

    public Kleene(String name, ASTD body) {
        super(name, body);
    }

    public Kleene() {
        super();
    }

    @Override
    public Statement init(ArrayList<ASTD> callList, String lets) {
        ArrayList<ASTD> ASTDTree = new ArrayList<>();
        ASTDTree.addAll(callList);
        ASTDTree.add(this);
        List<Statement> seqList = new ArrayList<>();
        String  uState = Conventions.getStateVar(Kleene.class);

        String prfx = ASTDTree.get(0).prefixTree(ASTDTree) + ".";
        //String prfx = getParent().prefix(getName()) + ".";

        // init state
        seqList.add(new AssignStatement(new Term(prfx + uState), new Term(Conventions.KNOTSTARTED)));
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
                    //it's a json init value
                else if(init.contains("{") && init.contains(":") && init.contains("}"))
                    seqList.add(new AssignStatement(new Term(prfx + v.getName()),
                            new Term(Conventions.JSON_PARSE
                                    .replace(ILTranslator.USYMBOL_1,
                                            "\""+init+"\""))));
                else if ( init.contains("(") && init.contains(")")) {
                    seqList.add(new AssignStatement(new Term(prfx + v.getName()),
                            new Term(init)));
                }else if(v.getType().compareTo(Conventions.STRING) == 0)
                    seqList.add(new AssignStatement(new Term(prfx + v.getName()), new Term("\""+init+"\"")));
                else if(v.getType().compareTo(Conventions.CLOCK) == 0) {
                    seqList.add(new CallStatement(Conventions.CLOCK_RESET, Arrays.asList(prfx + v.getName(), lets)));
                }
                else if(v.getType().compareTo(Conventions.TIME_TYPE3) == 0 && Constants.TIMED_SIMULATION){
                    seqList.add(new CallStatement(Conventions.CLOCK_RESET, Arrays.asList(prfx + v.getName(), lets)));
                }
                else
                    seqList.add(new AssignStatement(new Term(prfx + v.getName()), new Term(init)));
            });
        }
        // init body
        seqList.add(getBody().init(ASTDTree, lets));

        return new SeqStatement(seqList);
    }

    @Override
    public Statement initforsub(ArrayList<ASTD> callList, Event e, Bool timed, String lets, boolean forFinal) {
        ArrayList<ASTD> ASTDTree = new ArrayList<>();
        ASTDTree.addAll(callList);
        ASTDTree.add(this);
        List<Statement> seqList = new ArrayList<>();
        String  uState = Conventions.getStateVar(Kleene.class);

        String prfx = ASTDTree.get(0).prefixTree(ASTDTree) + ".";
        //String prfx = getParent().prefix(getName()) + ".";

        // init state
        seqList.add(new AssignStatement(new Term(prfx + uState), new Term(Conventions.KNOTSTARTED)));
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
                    //it's a json init value
                else if(init.contains("{") && init.contains(":") && init.contains("}"))
                    seqList.add(new AssignStatement(new Term(prfx + v.getName()),
                            new Term(Conventions.JSON_PARSE
                                    .replace(ILTranslator.USYMBOL_1,
                                            "\""+init+"\""))));
                else if ( init.contains("(") && init.contains(")")) {
                    seqList.add(new AssignStatement(new Term(prfx + v.getName()),
                            new Term(init)));
                }else if(v.getType().compareTo(Conventions.STRING) == 0)
                    seqList.add(new AssignStatement(new Term(prfx + v.getName()), new Term("\""+init+"\"")));
                else if(v.getType().compareTo(Conventions.CLOCK) == 0) {
                    seqList.add(new CallStatement(Conventions.CLOCK_RESET, Arrays.asList(prfx + v.getName(), lets)));
                }
                else if(v.getType().compareTo(Conventions.TIME_TYPE3) == 0 && Constants.TIMED_SIMULATION){
                    seqList.add(new CallStatement(Conventions.CLOCK_RESET, Arrays.asList(prfx + v.getName(), lets)));
                }
                else
                    seqList.add(new AssignStatement(new Term(prfx + v.getName()), new Term(init)));
            });
        }
        // init body
        seqList.add(getBody().initforsub(ASTDTree, e, timed, lets, forFinal));

        return new SeqStatement(seqList);
    }

    @Override
    public Condition _final(ArrayList<ASTD> callList) {
        ArrayList<ASTD> ASTDTree = new ArrayList<>();
        ASTDTree.addAll(callList);
        ASTDTree.add(this);
        String prfx = ASTDTree.get(0).prefixTree(ASTDTree) + ".";
        //String prfx = getParent().prefix(getName()) + ".";
        // it's final if not started or the body is final
        String kState = Conventions.getStateVar(Kleene.class);
        return new OrCondition(Arrays.asList(new CallCondition(Conventions.EQUALS, Arrays.asList(prfx + kState,
                        Conventions.KNOTSTARTED)),
                getBody()._final(ASTDTree)));
    }

    @Override
    public Condition _finalForSub(ArrayList<ASTD> callList) {
        ArrayList<ASTD> ASTDTree = new ArrayList<>();
        ASTDTree.addAll(callList);
        ASTDTree.add(this);
        String prfx = ASTDTree.get(0).prefixTree(ASTDTree) + ".";
        //String prfx = getParent().prefix(getName()) + ".";
        // it's final if not started or the body is final
        String kState = Conventions.getStateVar(Kleene.class);
        return new OrCondition(Arrays.asList(new CallCondition(Conventions.EQUALS, Arrays.asList(prfx + kState,
                        Conventions.KNOTSTARTED)),
                getBody()._finalForSub(ASTDTree)));
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
        String stateType = Conventions.getStateType(Kleene.class);
        props.add(new Variable(Conventions.getStateVar(Kleene.class), stateType, null, getName()));
        enumList.add(new Enum(stateType, Arrays.asList(Conventions.KNOTSTARTED, Conventions.KSTARTED)));

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

    @Override
    public Entry<Condition, Statement> trans_event(Event e, Bool timed, ArrayList<ASTD> callList, String lets) {
        ArrayList<ASTD> ASTDTree = new ArrayList<>();
        ASTDTree.addAll(callList);
        ASTDTree.add(this);
        Entry<Condition, Statement> evtBody = new Entry();
        String prfx = ASTDTree.get(0).prefixTree(ASTDTree) + ".";
        //String prfx = getParent().prefix(getName()) + ".";
        Action astdAction = prefixAction(getAstdAction(), ASTDTree);
        if (astdAction != null && Constants.DEBUG) {
            String debugMsg = ";\n" + Constants.LOGGER_MSG.replace(ILTranslator.USYMBOL_1, astdAction.getCode());
            astdAction = new Action(astdAction.getCode() + debugMsg);
        }
        //Statement initBody
        // event body
        ASTD b = this.getBody();
        Entry<Condition, Statement> eBody;
        if(e.getName().equals("Step") && (getBody() instanceof QInterleaving || 
        getBody() instanceof QSynchronization || 
        getBody() instanceof QChoice ||
        getBody() instanceof QFlow)){
            eBody = b.trans_event_step(e,timed, ASTDTree, lets);
        }
        else{
            eBody = b.trans_event(e,timed, ASTDTree, lets);
        }
        if (eBody != null && !eBody.isEmpty()) {
            String kState = Conventions.getStateVar(Kleene.class);
            // if-fi body block
            Condition c1, c2;
            c2 = eBody.getKey();
            Statement b_init = b.init(ASTDTree, lets);
            Condition bodyCopy = (Condition) Utils.copyObject(eBody.getKey());
            Condition _init = bodyCopy.substitute(b.initforsub(ASTDTree, e, timed, lets, false), ASTDTree);
            Condition _final = new OrCondition(Arrays.asList(
                    b._final(ASTDTree),
                    new CallCondition(Conventions.EQUALS, Arrays.asList(Conventions.KNOTSTARTED, prfx + kState))));
            c1 = new AndCondition(Arrays.asList(_final, _init));
            evtBody.setKey(new OrCondition(Arrays.asList(c1, c2)));
            List<Statement> stmtList = new ArrayList<>();
            //global condition of the event function
            if (Constants.COND_OPT_OPTS) {
                if (eBody.getValue() != null) {
                    if(isRoot()) {
                        stmtList.add(new AssignStatement(new Term(prfx + "cond_1"), c1));
                        stmtList.add(new AssignStatement(new Term(prfx + "cond_2"), c2));
                    }
                    c1 = new CallCondition(-1, Conventions.EQUALS1, Arrays.asList(prfx + "cond_1", c1));
                    c2 = new CallCondition(-1, Conventions.EQUALS1, Arrays.asList(prfx + "cond_2", c2));
                    evtBody.setKey(new OrCondition(Arrays.asList(c1, c2)));
                } else {
                    if(isRoot()) {
                        stmtList.add(new AssignStatement(new Term(prfx + "cond_1"), c1));
                    }
                    evtBody.setKey(c1);
                }
                c1 = new Bool(prfx + "cond_1");
                c2 = new Bool(prfx + "cond_2");
            }

            SeqStatement stmt1 = new SeqStatement(Arrays.asList(
                    new AssignStatement(new Term(prfx + kState), new Term(Conventions.KSTARTED)),
                    b_init,
                    eBody.getValue(),
                    astdAction
            ));
            SeqStatement stmt2 = new SeqStatement(Arrays.asList(
                    new AssignStatement(new Term(prfx + kState), new Term(Conventions.KSTARTED)),
                    eBody.getValue(),
                    astdAction));
            if (eBody.getValue() != null) {
                if(isRoot()) {
                    stmtList.add(new IFFIStatement(Arrays.asList(new Entry<>(c1, stmt1), new Entry<>(c2, stmt2))));
                    evtBody.setValue(new SeqStatement(stmtList));
                }
                else {
                    evtBody.setValue(new IFFIStatement(Arrays.asList(new Entry<>(c1, stmt1), new Entry<>(c2, stmt2))));
                }
            } else {
                if(isRoot()) {
                    stmtList.add(new IFFIStatement(Arrays.asList(new Entry<>(c1, stmt1))));
                    evtBody.setValue(new SeqStatement(stmtList));
                }
                else {
                    evtBody.setValue(new IFFIStatement(Arrays.asList(new Entry<>(c1, stmt1))));
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
        String prfx = ASTDTree.get(0).prefixTree(ASTDTree) + ".";
        //String prfx = getParent().prefix(getName()) + ".";
        Action astdAction = prefixAction(getAstdAction(), ASTDTree);
        if (astdAction != null && Constants.DEBUG) {
            String debugMsg = ";\n" + Constants.LOGGER_MSG.replace(ILTranslator.USYMBOL_1, astdAction.getCode());
            astdAction = new Action(astdAction.getCode() + debugMsg);
        }
        //Statement initBody
        // event body
        ASTD b = this.getBody();
        Entry<Condition, Statement> eBody = b.trans_event_step(e, timed, ASTDTree, lets);
        if (eBody != null && !eBody.isEmpty()) {
            String kState = Conventions.getStateVar(Kleene.class);
            // if-fi body block
            Condition c1, c2;
            c2 = eBody.getKey();
            //TODO INIT FOR SUB STEP
            Statement b_init = b.initforsub(ASTDTree, e, timed, lets, false);
            Condition bodyCopy = (Condition) Utils.copyObject(eBody.getKey());
            Condition init = bodyCopy.substitute(b_init, ASTDTree);
            Condition _final = new OrCondition(Arrays.asList(
                    b._final(ASTDTree),
                    new CallCondition(Conventions.EQUALS, Arrays.asList(Conventions.KNOTSTARTED, prfx + kState))));
            c1 = new AndCondition(Arrays.asList(_final, init));
            evtBody.setKey(new OrCondition(Arrays.asList(c1, c2)));
            List<Statement> stmtList = new ArrayList<>();
            //global condition of the event function
            if (Constants.COND_OPT_OPTS) {
                if (eBody.getValue() != null) {
                    if(isRoot()) {
                        stmtList.add(new AssignStatement(new Term(prfx + "cond_1"), c1));
                        stmtList.add(new AssignStatement(new Term(prfx + "cond_2"), c2));
                    }
                    c1 = new CallCondition(-1, Conventions.EQUALS1, Arrays.asList(prfx + "cond_1", c1));
                    c2 = new CallCondition(-1, Conventions.EQUALS1, Arrays.asList(prfx + "cond_2", c2));
                    evtBody.setKey(new OrCondition(Arrays.asList(c1, c2)));
                } else {
                    if(isRoot()) {
                        stmtList.add(new AssignStatement(new Term(prfx + "cond_1"), c1));
                    }
                    evtBody.setKey(c1);
                }
                c1 = new Bool(prfx + "cond_1");
                c2 = new Bool(prfx + "cond_2");
            }

            SeqStatement stmt1 = new SeqStatement(Arrays.asList(
                    new AssignStatement(new Term(prfx + kState), new Term(Conventions.KSTARTED)),
                    b_init,
                    eBody.getValue(),
                    astdAction
            ));
            SeqStatement stmt2 = new SeqStatement(Arrays.asList(
                    new AssignStatement(new Term(prfx + kState), new Term(Conventions.KSTARTED)),
                    eBody.getValue(),
                    astdAction));
            if (eBody.getValue() != null) {
                if(isRoot()) {
                    stmtList.add(new IFFIStatement(Arrays.asList(new Entry<>(c1, stmt1), new Entry<>(c2, stmt2))));
                    evtBody.setValue(new SeqStatement(stmtList));
                }
                else {
                    evtBody.setValue(new IFFIStatement(Arrays.asList(new Entry<>(c1, stmt1), new Entry<>(c2, stmt2))));
                }
            } else {
                if(isRoot()) {
                    stmtList.add(new IFFIStatement(Arrays.asList(new Entry<>(c1, stmt1))));
                    evtBody.setValue(new SeqStatement(stmtList));
                }
                else {
                    evtBody.setValue(new IFFIStatement(Arrays.asList(new Entry<>(c1, stmt1))));
                }
            }
        }

        return evtBody;
    }

    /*
     * @brief Returns the initial state value
     * @param  ASTD model
     * @return  A string value
     */
    @Override
    public String getInitialStateValue() {
        return Conventions.KNOTSTARTED;
    }

    @Override
    public Statement propertyMapping() {

        List<Statement> stmtList = new ArrayList<>();
        // generate common ASTD properties
        Statement commonProps = super.propertyMapping();
        if(commonProps != null)
            stmtList.add(commonProps);

        // generate Kleene properties
        DeclStatement decStmt = new DeclStatement(new Constant((this.getName() + "_" + ExecSchemaParser.STARTED).toUpperCase(),
                Conventions.STRING, ExecSchemaParser.STARTED, getName()));
        stmtList.add(decStmt);
        Constants.EXEC_SCHEMA_PROPS = Constants.EXEC_SCHEMA_PROPS.replace(ILTranslator.USYMBOL_1, Kleene.class.getSimpleName());

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
        //TODO: CHANGE FOR PREFIXTREE
        String prfx = getParent().prefix(getName()) + ".";
        stmtList.add(fillKleeneProperties(tojson.getNodeIndex(), prfx));

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
                    .replace(ILTranslator.USYMBOL_1, ExecSchemaParser.CURRENT_SUB_STATE.toUpperCase()));
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


    private Statement fillKleeneProperties(String index, String prfx) {
        List<Statement> stmtList = new ArrayList<>();

        String kleeneState = Conventions.getStateVar(Kleene.class);

        stmtList.add(new AssignStatement(new Term(Conventions.ARRAY_ELEM.replace(ILTranslator.USYMBOL_2, index)
                .replace(ILTranslator.USYMBOL_1, ExecSchemaParser.TYPE.toUpperCase())),
                new Term("\""+ ExecSchemaParser.KLEENE +"\"")));
        stmtList.add(new AssignStatement(new Term(Conventions.ARRAY_ELEM.replace(ILTranslator.USYMBOL_2, index)
                .replace(ILTranslator.USYMBOL_1,
                        (this.getName() + "_" + ExecSchemaParser.STARTED).toUpperCase())),
                new Term("(std::string(" + Conventions.ARRAY_ELEM
                        .replace(ILTranslator.USYMBOL_2,
                                Conventions.getStateVar(Kleene.class))
                        .replace(ILTranslator.USYMBOL_1, prfx + kleeneState) + ").compare(\"" + Conventions.KSTARTED + "\") == 0) ? true : false")));

        return new SeqStatement(stmtList);
    }

}
