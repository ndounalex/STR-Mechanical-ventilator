package com.udes.model.astd.types;

import com.udes.model.astd.base.ASTD;
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
import com.udes.model.il.record.Enum;
import com.udes.model.il.record.Record;
import com.udes.model.il.statements.*;
import com.udes.model.il.terms.Bool;
import com.udes.model.il.terms.Term;
import com.udes.parser.ExecSchemaParser;
import com.udes.translator.ILTranslator;
import com.udes.utils.Constants;
import com.udes.utils.Utils;

import java.lang.reflect.Array;
import java.util.*;

public class QChoice extends QuantifiedASTD {

    public class PredicateHolder {
        public Condition cond;
        public String key;
    }

    public PredicateHolder pred;

    public QChoice(String name, List<Variable> attributes, List<Variable> params, Action astdAction, ASTD body,
                   Variable qvariable, Domain domain) {
        super(name, attributes, params, astdAction, body, qvariable, domain);
        pred = new PredicateHolder();
    }

    public QChoice(String name, ASTD body, Variable qvariable, Domain domain) {
        super(name, body, qvariable, domain);
        pred = new PredicateHolder();
    }

    public QChoice() {
        super();
        pred = new PredicateHolder();
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

    @Override
    public Statement init(ArrayList<ASTD> callList, String lets) {
        ArrayList<ASTD> ASTDTree = new ArrayList<>();
        ASTDTree.addAll(callList);
        ASTDTree.add(this);

        List<Statement> seqList = new ArrayList<>();
        String prfx = ASTDTree.get(0).prefixTree(ASTDTree) + ".";
        //String prfx = getParent().prefix(getName()) + ".";
        String qState = Conventions.getStateVar(QChoice.class);
        seqList.add(new AssignStatement(new Term(prfx + qState), new Term(Conventions.FALSE)));
        // init variables
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
                else if(init.contains("{") && init.contains(":") && init.contains("}"))
                    seqList.add(new AssignStatement(new Term(prfx + v.getName()),
                            new Term(Conventions.JSON_PARSE.replace(ILTranslator.USYMBOL_1,
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

        return new SeqStatement(seqList);
    }

    @Override
    public Statement initforsub(ArrayList<ASTD> callList, Event e, Bool timed, String lets, boolean forFinal) {
        ArrayList<ASTD> ASTDTree = new ArrayList<>();
        ASTDTree.addAll(callList);
        ASTDTree.add(this);

        List<Statement> seqList = new ArrayList<>();
        String prfx = ASTDTree.get(0).prefixTree(ASTDTree) + ".";
        //String prfx = getParent().prefix(getName()) + ".";
        String qState = Conventions.getStateVar(QChoice.class);
        seqList.add(new AssignStatement(new Term(prfx + qState), new Term(Conventions.FALSE)));
        // init variables
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
                else if(init.contains("{") && init.contains(":") && init.contains("}"))
                    seqList.add(new AssignStatement(new Term(prfx + v.getName()),
                            new Term(Conventions.JSON_PARSE.replace(ILTranslator.USYMBOL_1,
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

        //HERE WE ALLOW THE SUBSTITUTION OF THE EXISTS_NAMEASTDNAMEEVENT_QUANTIFIEDVARIABLE(PARAMS) FOR THE BODY OF THE CHILD.
        //WE also allow the substitution of the Forall_NAMEASTDNAMEEVENT_QuantifiedVariable(Params) for the body of the child.
        if(!forFinal){
            if(e.getName().equals("Step")){
                seqList.add(new CallStatement(
                        Conventions.EXISTS + "_" + getName() + e.getName() + "_" + getQvariable().getName(),
                        Arrays.asList(getBody().trans_event_step(e, timed, ASTDTree, lets).getKey().substitute(getBody().initforsub(ASTDTree, e, timed, lets, forFinal), ASTDTree))
                ));
                seqList.add(new CallStatement(
                        Conventions.FOR_ALL + "_" + getName() + e.getName() + "_" + getQvariable().getName(),
                        Arrays.asList(getBody().trans_event_step(e, timed, ASTDTree, lets).getKey().substitute(getBody().initforsub(ASTDTree, e, timed, lets, forFinal), ASTDTree))
                ));
            }
            else{
                seqList.add(new CallStatement(
                        Conventions.EXISTS + "_" + getName() + e.getName() + "_" + getQvariable().getName(),
                        Arrays.asList(getBody().trans_event(e, timed, ASTDTree, lets).getKey().substitute(getBody().initforsub(ASTDTree, e, timed, lets, forFinal), ASTDTree))
                ));
                seqList.add(new CallStatement(
                        Conventions.FOR_ALL + "_" + getName() + e.getName() + "_" + getQvariable().getName(),
                        Arrays.asList(getBody().trans_event(e, timed, ASTDTree, lets).getKey().substitute(getBody().initforsub(ASTDTree, e, timed, lets, forFinal), ASTDTree))
                ));
            }
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
        // it's a quantified choice. It's final if no choice is made or there exists a quantified variable for which
        // the initial state of the body is final
        // or a choice is made and the body is final for a given instance of the quantified variable
        String prfx = ASTDTree.get(0).prefixTree(ASTDTree) + ".";
        String qchState = Conventions.getStateVar(QChoice.class), qvar = getQvariable().getName();
        Condition finalCpy = (Condition) Utils.copyObject(getBody()._final(ASTDTree)),
                finalCpy2 = (Condition) Utils.copyObject(finalCpy);
        return new OrCondition(Arrays.asList(
                new AndCondition(Arrays.asList(new CallCondition(Conventions.EQUALS,
                                Arrays.asList(prfx + qchState, Conventions.CHOICE_NIL)),
                        new AndCondition(Arrays.asList(finalCpy.substitute(getBody().init(ASTDTree, Conventions.CST), ASTDTree))))),
                new AndCondition(Arrays.asList(new CallCondition(Conventions.NOT_EQUALS,
                                Arrays.asList(prfx + qchState, Conventions.CHOICE_NIL)),
                        finalCpy2.substitute(new AssignStatement(new Term(qvar), new Term(prfx + qchState)), ASTDTree)))));
    }

    @Override
    public Condition _finalForSub(ArrayList<ASTD> callList) {
        ArrayList<ASTD> ASTDTree = new ArrayList<>();
        ASTDTree.addAll(callList);
        ASTDTree.add(this);
        // it's a quantified choice. It's final if no choice is made or there exists a quantified variable for which
        // the initial state of the body is final
        // or a choice is made and the body is final for a given instance of the quantified variable
        String prfx = ASTDTree.get(0).prefixTree(ASTDTree) + ".";
        String qchState = Conventions.getStateVar(QChoice.class), qvar = getQvariable().getName();
        Condition finalCpy = (Condition) Utils.copyObject(getBody()._finalForSub(ASTDTree)),
                finalCpy2 = (Condition) Utils.copyObject(finalCpy);
        return new OrCondition(Arrays.asList(
                new AndCondition(Arrays.asList(new CallCondition(Conventions.EQUALS,
                                Arrays.asList(prfx + qchState, Conventions.CHOICE_NIL)),
                        new AndCondition(Arrays.asList(finalCpy.substitute(getBody().init(ASTDTree, Conventions.CST), ASTDTree))))),
                new AndCondition(Arrays.asList(new CallCondition(Conventions.NOT_EQUALS,
                                Arrays.asList(prfx + qchState, Conventions.CHOICE_NIL)),
                        finalCpy2.substitute(new AssignStatement(new Term(qvar), new Term(prfx + qchState)), ASTDTree)))));
    }

    @Override
    public Entry<List<Enum>, List<Record>> trans_type() {
        List<Record> recordList = new ArrayList<>(),
                     enumList   = new ArrayList<>();
        // struct name
        String structName = Conventions.getStructName(getName());
        List<Variable> props = new ArrayList<>();
        // quantified variable
        props.add(getQvariable());
        // astd attributes
        List<Variable> attrs = getAttributes();
        if(attrs != null)
            props.addAll(attrs);
        // astd state
        props.add(new Variable(Conventions.getStateVar(QChoice.class), Conventions.INT, null, getName()));
        if(!(getBody() instanceof Elem)) {
            props.add(new Variable(Conventions.getStructVar(getBody().getName()),
                                   Conventions.getStructName(getBody().getName()),null, getName()));
        }
        if(Constants.COND_OPT_OPTS) {
            for (int i = 1; i < 3; i++) {
                props.add(new Variable("cond_" + i, Conventions.BOOL_TYPE, null, getName()));
            }
        }
        recordList.add(new Record(structName, props));
        // struct body
        Entry<List<Enum>, List<Record>> recordBody = getBody().trans_type();
        if(recordBody != null && !recordBody.getKey().isEmpty())
            enumList.addAll(recordBody.getKey());
        if(recordBody != null && !recordBody.getValue().isEmpty())
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
        Action astdAction = prefixAction(getAstdAction(), ASTDTree);
        if(astdAction != null && Constants.DEBUG) {
            String debugMsg = ";\n" + Constants.LOGGER_MSG.replace(ILTranslator.USYMBOL_1, astdAction.getCode());
            astdAction = new Action(astdAction.getCode() + debugMsg);
        }
        List<Variable> evtParams = new ArrayList<>((getParent().getAllEventParams(e)));
        //List<Variable> qVarList = new ArrayList<>((getParent().getAllEventQVariables(e)));
        List<Variable> qVarList = new ArrayList<>(getAllQVariables(ASTDTree));;

        Entry<Condition, Statement> eventBody;
        if(e.getName().equals("Step") && (getBody() instanceof QInterleaving || getBody() instanceof QSynchronization || getBody() instanceof QChoice || getBody() instanceof QFlow)){
            eventBody = getBody().trans_event_step(e,timed, ASTDTree, lets);
        }
        else{
            eventBody = getBody().trans_event(e,timed, ASTDTree, lets);
        }

        Variable qvar = getQvariable();
        if (eventBody != null && !eventBody.isEmpty()) {

            if(Conventions.isIFFI(eventBody.getValue())) {
                IFFIStatement iffiStmt = (IFFIStatement) eventBody.getValue();
                List<Entry<Condition, Statement>> iffiList = new ArrayList<>(iffiStmt.getIFFIStatement());
                eventBody.setValue(new IFFIStatement(iffiList));
            }
            else if(Conventions.isSeqStatement(eventBody.getValue())) {
                SeqStatement seqStmt = (SeqStatement) eventBody.getValue();
                List<Statement> stmtList = seqStmt.getStatement();
                if(stmtList != null) {
                    if (stmtList.size() == 1) {
                        IFFIStatement iffiStmt = (IFFIStatement) stmtList.get(0);
                        List<Entry<Condition, Statement>> iffiList = new ArrayList<>(iffiStmt.getIFFIStatement());
                        eventBody.setValue(new IFFIStatement(iffiList));
                    }
                    if (stmtList.size() > 1) {
                        List<Statement> stmtList1 = new ArrayList<>();
                        int idx = -1;
                        for (int i = 0; i < stmtList.size(); i++) {
                            Statement st = stmtList.get(i);
                            if (Conventions.isIFFI(st))
                                idx = i;
                            else
                                stmtList1.add(st);
                        }
                        if (idx >= 0) {
                            IFFIStatement iffiStmt = (IFFIStatement) stmtList.get(idx);
                            List<Entry<Condition, Statement>> iffiList = new ArrayList<>(iffiStmt.getIFFIStatement());;
                            stmtList1.add(idx, new IFFIStatement(iffiList));
                            eventBody.setValue(new SeqStatement(stmtList1));
                        }
                    }
                }
            }

            String qchoiceState = Conventions.getStateVar(QChoice.class);
            Statement init_b = getBody().init(ASTDTree, lets);
            Condition c1, c2, c3;
            c1 = new CallCondition(Conventions.EQUALS,
                    Arrays.asList(prfx + qchoiceState, Conventions.FALSE));
            List<Condition> orCond = new ArrayList<>();

            List<String> tmp = new ArrayList<>();
            //String _prfx = getParent().prefix(Constants.DUMMY_PREFIX2.get(qvar).getName()) + "." + qvar.getName();
            String _qvar = Constants.DUMMY_PREFIX.get(qvar);

            tmp.add(_qvar);

            boolean found2 = false;
            for(Variable param : evtParams){
                if(qvar.getName().equals(param.getName())){
                    found2 = true;
                }
            }

            if(!found2){
                //the qvar variable is hidden with optmization
                if(!Constants.PARAM_OPTM.isEmpty() &&
                        Constants.PARAM_OPTM.containsKey(e.getName())){
                    orCond.add(new CallCondition(Conventions.EQUALS,
                            Arrays.asList(
                                    evtParams.get(0).getName(),
                                    _qvar)));
                }
            }
            else{
                orCond.add(new CallCondition(Conventions.EQUALS,
                        Arrays.asList(Conventions.DUMMY_PARAMS + qvar.getName(), _qvar)));
            }
            Condition condCpy = (Condition) Utils.copyObject(eventBody.getKey());
            orCond.add(condCpy);

            c3 = new AndCondition(Arrays.asList(new CallCondition(Conventions.NOT_EQUALS,
                            Arrays.asList(prfx + qchoiceState, Conventions.FALSE)),
                    new AndCondition(orCond)));
            List<Statement> stmtList = new ArrayList<>();

            if(evtParams != null && !evtParams.isEmpty()) {
                List<Variable> varList = new ArrayList<>();
                List<Variable> otherparams = new ArrayList<>();
                boolean found;
                for (Variable _v : evtParams) {
                    found = false;
                    for(Variable v : qVarList) {
                        if (_v.getName().compareTo(v.getName()) == 0) {
                            tmp.add(Conventions.DUMMY_PARAMS + _v.getName());
                            varList.add(new Variable(Conventions.DUMMY_PARAMS + _v.getName(),
                                    v.getType(), "ANY", getName()));
                            found = true;
                        }
                    }
                    if(!found)
                        otherparams.add(_v);
                }
                for (Variable _v : otherparams) {
                    _v.setInit(Conventions.DUMMY_PARAMS + getQvariable().getName());
                    tmp.add(_v.getName());
                    varList.add(_v);
                }
                evtParams = varList;
                pred.key = Conventions.EXISTS + "_" + getName() + e.getName() + "_" + qvar.getName();
                pred.cond = new CallCondition(pred.key, tmp);
                c2 = new AndCondition(Arrays.asList(c1, pred.cond));

                if(Constants.COND_OPT_OPTS) {
                    if(isRoot()) {
                        stmtList.add(new AssignStatement(new Term(prfx + "cond_1"), c2));
                        stmtList.add(new AssignStatement(new Term(prfx + "cond_2"), c3));
                    }
                    c2 = new CallCondition(-1, Conventions.EQUALS1, Arrays.asList(prfx + "cond_1", c2));
                    c3 = new CallCondition(-1, Conventions.EQUALS1, Arrays.asList(prfx + "cond_2", c3));
                }

                if (eventBody.getValue() == null && astdAction == null) {
                    evtBody.setKey(c2);
                }
                else {
                    evtBody.setKey(new OrCondition(Arrays.asList(c2, c3)));
                }

                if(Constants.COND_OPT_OPTS) {
                    c2 = new Bool(prfx + "cond_1");
                    c3 = new Bool(prfx + "cond_2");
                }
            }
            else {
                pred.key = Conventions.FOR_ALL + "_" + getName() + e.getName() + "_" + qvar.getName();
                pred.cond = new CallCondition(pred.key, tmp);
                c2 = new AndCondition(Arrays.asList(c1, pred.cond));

                if(Constants.COND_OPT_OPTS) {
                    if(isRoot()) {
                        stmtList.add(new AssignStatement(new Term(prfx + "cond_1"), c2));
                        stmtList.add(new AssignStatement(new Term(prfx + "cond_2"), c3));
                    }
                    c2 = new CallCondition(-1, Conventions.EQUALS1, Arrays.asList(prfx + "cond_1", c2));
                    c3 = new CallCondition(-1, Conventions.EQUALS1, Arrays.asList(prfx + "cond_2", c3));
                }

                if (eventBody.getValue() == null && astdAction == null) {
                    evtBody.setKey(c2);
                }
                else {
                    evtBody.setKey(new OrCondition(Arrays.asList(c2, c3)));
                }

                if(Constants.COND_OPT_OPTS) {
                    c2 = new Bool(prfx + "cond_1");
                    c3 = new Bool(prfx + "cond_2");
                }
            }
            Statement stmtCpy1 = (Statement) Utils.copyObject(new SeqStatement(Arrays.asList(eventBody.getValue(),
                    astdAction)));
            Statement act1 = new SeqStatement(Arrays.asList(new AssignStatement(
                            new Term(prfx + qchoiceState), new Term(Conventions.TRUE)),
                    init_b, stmtCpy1)),
                    act2 = new SeqStatement(Arrays.asList(eventBody.getValue(), astdAction));

            if(evtParams != null) {
                if (eventBody.getValue() == null && astdAction == null) {
                    if(isRoot()) {
                        stmtList.add(new IFFIStatement(Arrays.asList(new Entry<>(c2, act1))));
                        evtBody.setValue(new SeqStatement(stmtList));
                    }
                    else {
                        evtBody.setValue(new IFFIStatement(Arrays.asList(new Entry<>(c2, act1))));
                    }
                } else {
                    if(isRoot()) {
                        stmtList.add(new IFFIStatement(
                                Arrays.asList(new Entry<>(c2, act1), new Entry<>(c3, act2))));
                        evtBody.setValue(new SeqStatement(stmtList));
                    }
                    else {
                        evtBody.setValue(new IFFIStatement(
                                Arrays.asList(new Entry<>(c2, act1), new Entry<>(c3, act2))));
                    }
                }
            }

            if(pred.key != null) {
                Utils.qvarCond.put(Constants.DUMMY_PREFIX.get(qvar), eventBody.getKey());
                Event _e = new Event(e.getName(), evtParams);
                Utils.qvarDic.put(pred.key, _e);
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
        if(astdAction != null && Constants.DEBUG) {
            String debugMsg = ";\n" + Constants.LOGGER_MSG.replace(ILTranslator.USYMBOL_1, astdAction.getCode());
            astdAction = new Action(astdAction.getCode() + debugMsg);
        }
        List<Variable> evtParams = new ArrayList<>((getParent().getAllEventParams(e)));
        //List<Variable> qVarList = new ArrayList<>((getParent().getAllEventQVariables(e)));
        List<Variable> qVarList = new ArrayList<>(getAllQVariables(ASTDTree));;

        Entry<Condition, Statement> eventBody = getBody().trans_event_step(e,timed, ASTDTree, lets);
        Variable qvar = getQvariable();
        if (eventBody != null && !eventBody.isEmpty()) {

            if(Conventions.isIFFI(eventBody.getValue())) {
                IFFIStatement iffiStmt = (IFFIStatement) eventBody.getValue();
                List<Entry<Condition, Statement>> iffiList = new ArrayList<>(iffiStmt.getIFFIStatement());
                eventBody.setValue(new IFFIStatement(iffiList));
            }
            else if(Conventions.isSeqStatement(eventBody.getValue())) {
                SeqStatement seqStmt = (SeqStatement) eventBody.getValue();
                List<Statement> stmtList = seqStmt.getStatement();
                if(stmtList != null) {
                    if (stmtList.size() == 1) {
                        IFFIStatement iffiStmt = (IFFIStatement) stmtList.get(0);
                        List<Entry<Condition, Statement>> iffiList = new ArrayList<>(iffiStmt.getIFFIStatement());
                        eventBody.setValue(new IFFIStatement(iffiList));
                    }
                    if (stmtList.size() > 1) {
                        List<Statement> stmtList1 = new ArrayList<>();
                        int idx = -1;
                        for (int i = 0; i < stmtList.size(); i++) {
                            Statement st = stmtList.get(i);
                            if (Conventions.isIFFI(st))
                                idx = i;
                            else
                                stmtList1.add(st);
                        }
                        if (idx >= 0) {
                            IFFIStatement iffiStmt = (IFFIStatement) stmtList.get(idx);
                            List<Entry<Condition, Statement>> iffiList = new ArrayList<>(iffiStmt.getIFFIStatement());
                            stmtList1.add(idx, new IFFIStatement(iffiList));
                            eventBody.setValue(new SeqStatement(stmtList1));
                        }
                    }
                }
            }

            String qchoiceState = Conventions.getStateVar(QChoice.class);
            Statement init_b = getBody().init(ASTDTree ,lets);
            Condition c1, c2, c3;
            c1 = new CallCondition(Conventions.EQUALS,
                    Arrays.asList(prfx + qchoiceState, Conventions.FALSE));
            List<Condition> orCond = new ArrayList<>();

            List<String> tmp = new ArrayList<>();
            //String _prfx = getParent().prefix(Constants.DUMMY_PREFIX2.get(qvar).getName()) + "." + qvar.getName();
            String _qvar = Constants.DUMMY_PREFIX.get(qvar);
            tmp.add(_qvar);


            //STEP IN A QUANTIFIED CHOICE DOES NOT SEARCH FOR _X!
            //orCond.add(new CallCondition(Conventions.EQUALS,
            //            Arrays.asList(Conventions.DUMMY_PARAMS + qvar.getName(), _qvar)));
            Condition condCpy = (Condition) Utils.copyObject(eventBody.getKey());
            orCond.add(condCpy);


            c3 = new AndCondition(Arrays.asList(new CallCondition(Conventions.NOT_EQUALS,
                            Arrays.asList(prfx + qchoiceState, Conventions.FALSE)),
                    new AndCondition(orCond)));
            List<Statement> stmtList = new ArrayList<>();

            if(evtParams != null && !evtParams.isEmpty()) {
                List<Variable> varList = new ArrayList<>();
                List<Variable> otherparams = new ArrayList<>();
                boolean found;
                for (Variable _v : evtParams) {
                    found = false;
                    for(Variable v : qVarList) {
                        if (_v.getName().compareTo(v.getName()) == 0) {
                            tmp.add(Conventions.DUMMY_PARAMS + _v.getName());
                            varList.add(new Variable(Conventions.DUMMY_PARAMS + _v.getName(),
                                    v.getType(), "ANY", getName()));
                            found = true;
                        }
                    }
                    if(!found)
                        otherparams.add(_v);
                }
                for (Variable _v : otherparams) {
                    _v.setInit(Conventions.DUMMY_PARAMS + getQvariable().getName());
                    tmp.add(_v.getName());
                    varList.add(_v);
                }
                evtParams = varList;
                pred.key = Conventions.EXISTS + "_" + getName() + e.getName() + "_" + qvar.getName();
                pred.cond = new CallCondition(pred.key, tmp);
                c2 = new AndCondition(Arrays.asList(c1, pred.cond));

                if(Constants.COND_OPT_OPTS) {
                    if(isRoot()) {
                        stmtList.add(new AssignStatement(new Term(prfx + "cond_1"), c2));
                        stmtList.add(new AssignStatement(new Term(prfx + "cond_2"), c3));
                    }
                    c2 = new CallCondition(-1, Conventions.EQUALS1, Arrays.asList(prfx + "cond_1", c2));
                    c3 = new CallCondition(-1, Conventions.EQUALS1, Arrays.asList(prfx + "cond_2", c3));
                }

                if (eventBody.getValue() == null && astdAction == null) {
                    evtBody.setKey(c2);
                }
                else {
                    evtBody.setKey(new OrCondition(Arrays.asList(c2, c3)));
                }

                if(Constants.COND_OPT_OPTS) {
                    c2 = new Bool(prfx + "cond_1");
                    c3 = new Bool(prfx + "cond_2");
                }
            }
            else {
                pred.key = Conventions.EXISTS + "_" + getName() + e.getName() + "_" + qvar.getName();
                pred.cond = new CallCondition(pred.key, tmp);
                c2 = new AndCondition(Arrays.asList(c1, pred.cond));

                if(Constants.COND_OPT_OPTS) {
                    if(isRoot()) {
                        stmtList.add(new AssignStatement(new Term(prfx + "cond_1"), c2));
                        stmtList.add(new AssignStatement(new Term(prfx + "cond_2"), c3));
                    }
                    c2 = new CallCondition(-1, Conventions.EQUALS1, Arrays.asList(prfx + "cond_1", c2));
                    c3 = new CallCondition(-1, Conventions.EQUALS1, Arrays.asList(prfx + "cond_2", c3));
                }

                if (eventBody.getValue() == null && astdAction == null) {
                    evtBody.setKey(c2);
                }
                else {
                    evtBody.setKey(new OrCondition(Arrays.asList(c2, c3)));
                }

                if(Constants.COND_OPT_OPTS) {
                    c2 = new Bool(prfx + "cond_1");
                    c3 = new Bool(prfx + "cond_2");
                }
            }
            Statement stmtCpy1 = (Statement) Utils.copyObject(new SeqStatement(Arrays.asList(eventBody.getValue(),
                    astdAction)));
            Statement act1 = new SeqStatement(Arrays.asList(new AssignStatement(
                            new Term(prfx + qchoiceState), new Term(Conventions.TRUE)),
                    init_b, stmtCpy1)),
                    act2 = new SeqStatement(Arrays.asList(eventBody.getValue(), astdAction));

            if(evtParams != null && !evtParams.isEmpty()) {
                if (eventBody.getValue() == null && astdAction == null) {
                    if(isRoot()) {
                        stmtList.add(new IFFIStatement(Arrays.asList(new Entry<>(c2, act1))));
                        evtBody.setValue(new SeqStatement(stmtList));
                    }
                    else {
                        evtBody.setValue(new IFFIStatement(Arrays.asList(new Entry<>(c2, act1))));
                    }
                } else {
                    if(isRoot()) {
                        stmtList.add(new IFFIStatement(
                                Arrays.asList(new Entry<>(c2, act1), new Entry<>(c3, act2))));
                        evtBody.setValue(new SeqStatement(stmtList));
                    }
                    else {
                        evtBody.setValue(new IFFIStatement(
                                Arrays.asList(new Entry<>(c2, act1), new Entry<>(c3, act2))));
                    }
                }
            }
            else{
                //NO PARAMETERS -> WITH STEP IT MEANS A SEARCHING FOR A INITIALIZED STATE
                //FOR AND GOTO
                //DURING FOR _qvar = c_qvar AND ts_qvar = c_qvar
                String domType  = (getDomain() != null) ? getDomain().getType() : "";
                if(!domType.equals(Constants.UNBOUNDEDDOMAIN)){
                    //Defined domain
                    IFFIStatement goif = new IFFIStatement(Arrays.asList(new Entry<>(
                            new CallCondition(Conventions.EQUALS, Arrays.asList(Conventions.EXEC, Conventions.TRUE)),
                            new CallStatement(Conventions.GOTO, Collections.singletonList(Conventions.GOTO+getName()+getQvariable().getName()))
                            )));
                    SeqStatement seqStmt = new SeqStatement(Arrays.asList(
                            new DeclStatement(new Variable(
                                    Conventions.DUMMY_PARAMS + qvar.getName(),
                                    getQvariable().getType(),
                                    Conventions.CHOICE_VAR + "_" + qvar.getName(),
                                    null)),
                            new AssignStatement(
                                    new Term(prfx + qvar.getName()),
                                    new Term(Conventions.CHOICE_VAR + "_" + qvar.getName())),
                            new SeqStatement(stmtList),
                            goif
                    ));
                    ForStatement forStmt = new ForStatement(new CallCondition(Conventions.FOR,
                            Arrays.asList(getQvariable().getType(),
                                    Conventions.CHOICE_VAR + "_" + qvar.getName(),
                                    Conventions.getVarSet(getName()))),
                            seqStmt);
                    if (eventBody.getValue() == null && astdAction == null) {
                        stmtList.add(new IFFIStatement(Arrays.asList(new Entry<>(c2, act1))));
                        evtBody.setValue(new SeqStatement(Arrays.asList(
                                forStmt,
                                new CallStatement(Conventions.GOTOFUNC, Collections.singletonList(Conventions.GOTO+getName()+getQvariable().getName()))
                        )));
                    } else {
                        stmtList.add(new IFFIStatement(Arrays.asList(new Entry<>(c2, act1), new Entry<>(c3, act2))));
                        evtBody.setValue(new SeqStatement(Arrays.asList(
                                forStmt,
                                new CallStatement(Conventions.GOTOFUNC, Collections.singletonList(Conventions.GOTO+getName()+getQvariable().getName()))
                        )));
                    }
                }
                else{
                    //unbounded domain
                    SeqStatement seqStmt = new SeqStatement(Arrays.asList(
                            new DeclStatement(new Variable(
                                    Conventions.DUMMY_PARAMS + qvar.getName(),
                                    getQvariable().getType(),
                                    prfx + qvar.getName(),
                                    null)),
                            new SeqStatement(stmtList)
                    ));
                    IFFIStatement ifInit = new IFFIStatement(Arrays.asList(new Entry<>(
                            new CallCondition(Conventions.NOT_EQUALS,
                                    Arrays.asList(prfx + qchoiceState, Conventions.FALSE)),
                            seqStmt)));
                    if (eventBody.getValue() == null && astdAction == null) {
                        stmtList.add(new IFFIStatement(Arrays.asList(new Entry<>(c2, act1))));
                        evtBody.setValue(new SeqStatement(Arrays.asList(
                                ifInit
                        )));
                    } else {
                        stmtList.add(new IFFIStatement(Arrays.asList(new Entry<>(c2, act1), new Entry<>(c3, act2))));
                        evtBody.setValue(new SeqStatement(Arrays.asList(
                                ifInit
                        )));
                    }
                }
            }

            if(pred.key != null && !pred.key.isEmpty()) {
                Utils.qvarCond.put(Constants.DUMMY_PREFIX.get(qvar), eventBody.getKey());
                Event _e = new Event(e.getName(), evtParams);
                Utils.qvarDic.put(pred.key, _e);
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
        return Conventions.NIL;
    }

    @Override
    public String prefix(String childName) {
        String prefixBase = Conventions.getStructVar(getName());
        if(getName().compareTo(childName) == 0) {
            return prefixBase;
        }
        else {
            String bodyPrefix = getBody().prefix(childName);

            if (bodyPrefix != null) {
                return prefixBase + "." + bodyPrefix;
            }
        }
        return null;
    }

    @Override
    public Statement propertyMapping() {

        List<Statement> stmtList = new ArrayList<>();
        // generate common ASTD properties
        Statement commonProps = super.propertyMapping();
        if(commonProps != null)
            stmtList.add(commonProps);

        // generate QSynchronization properties
        List<String> qchoiceProps = Arrays.asList(ExecSchemaParser.QCHOICE_VAR, ExecSchemaParser.VALUE);
        qchoiceProps.forEach(prop -> {
            DeclStatement decStmt1 = new DeclStatement(new Constant((this.getName() + "_" + prop).toUpperCase(),
                                                                     Conventions.STRING, prop, getName()));
            stmtList.add(decStmt1);
        });
        Constants.EXEC_SCHEMA_PROPS = Constants.EXEC_SCHEMA_PROPS.replace(ILTranslator.USYMBOL_1,
                                                                          QChoice.class.getSimpleName());

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
        stmtList.add(fillQChoiceProperties(tojson.getNodeIndex(), prfx));

        List<String> subIndexes = updateSubIndexes(tojson);
        if(subIndexes != null) {
            tojson.setSubNodeIndex(subIndexes);
            setToJson(tojson);
        }

        ASTD subASTD = getBody();
        ToJson subToJson = subASTD.toJson();
        /*
         * We assume that the first element is choosen by default
         */
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


    private Statement fillQChoiceProperties(String index, String prfx) {
        List<Statement> stmtList = new ArrayList<>();

        String qchoiceState = Conventions.getStateVar(QChoice.class);

        stmtList.add(new AssignStatement(new Term(Conventions.ARRAY_ELEM.replace(ILTranslator.USYMBOL_2, index)
                .replace(ILTranslator.USYMBOL_1, ExecSchemaParser.TYPE.toUpperCase())),
                new Term("\""+ ExecSchemaParser.QCHOICE +"\"")));
        stmtList.add(new AssignStatement(new Term(Conventions.ARRAY_ELEM.replace(ILTranslator.USYMBOL_2, index)
                .replace(ILTranslator.USYMBOL_1, (this.getName() + "_" + ExecSchemaParser.QCHOICE_VAR).toUpperCase())),
                new Term("\""+ this.getQvariable().getName() +"\"")));


        stmtList.add(new AssignStatement(new Term(Conventions.ARRAY_ELEM.replace(ILTranslator.USYMBOL_2, index)
                        .replace(ILTranslator.USYMBOL_1,
                                (this.getName() + "_" + ExecSchemaParser.VALUE).toUpperCase())),
                       new Term(Conventions.TO_STRING.replace(ILTranslator.USYMBOL_1,prfx + qchoiceState))));

        String qvar_type = getQvariable().getType().toLowerCase();
        if(qvar_type.contains(Conventions.INT)
                || qvar_type.contains(Conventions.DOUBLE)
                || qvar_type.contains(Conventions.BOOL_TYPE1)
                || qvar_type.contains(Conventions.FLOAT)
                || qvar_type.contains(Conventions.SHORT)
                || qvar_type.contains(Conventions.LONG)) {
            Term term = new Term(Conventions.TO_STRING.replace(ILTranslator.USYMBOL_1, prfx + qchoiceState));
            stmtList.add(new AssignStatement(new Term(Conventions.ARRAY_ELEM
                                .replace(ILTranslator.USYMBOL_2, index)
                                .replace(ILTranslator.USYMBOL_1,
                                        (this.getName() + "_" + ExecSchemaParser.VALUE).toUpperCase())), term));
        }
        else {

            Statement subStmt1 = new AssignStatement(new Term(Conventions.ARRAY_ELEM
                                     .replace(ILTranslator.USYMBOL_2, index)
                                     .replace(ILTranslator.USYMBOL_1,
                                             (this.getName() + "_" + ExecSchemaParser.VALUE).toUpperCase())),
                                     new Term(Conventions.DUMP_JSON_OBJECT.replace(ILTranslator.USYMBOL_2,
                                            prfx + qchoiceState))),
                      subStmt2 = new AssignStatement(new Term(Conventions.ARRAY_ELEM
                                    .replace(ILTranslator.USYMBOL_2, index)
                                    .replace(ILTranslator.USYMBOL_1,
                                            (this.getName() + "_" + ExecSchemaParser.VALUE).toUpperCase())),
                                    new Term(Conventions.COMPLEX_TYPE_TO_STRING.replace(ILTranslator.USYMBOL_1,
                                            ILTranslator.USYMBOL_4 + prfx + qchoiceState)));

            stmtList.add(new IFFIStatement(Arrays.asList(
                                new Entry<>(new Bool(Conventions.hasToStringMethodInClass(getQvariable().getType())),
                                            subStmt2),
                                new Entry<>(new CallCondition(Conventions.INSTANCE_OF
                                        .replace(ILTranslator.USYMBOL_1, getQvariable().getType()),
                                        Arrays.asList(ILTranslator.USYMBOL_4
                                                + this.getQvariable().getName())), subStmt1),
                                new Entry<>(null, new CallStatement(Conventions.ERROR_LABEL3,
                                            Collections.singletonList(Conventions.getErrorMsg(null, 2))))
                         )));
        }


        return new SeqStatement(stmtList);
    }

    @Override
    public List<String> updateSubIndexes(ToJson obj) {
        /*
         * We assume that the first element is choosen by default
         */
        return Arrays.asList(obj.getNodeIndex());
    }
}
