package com.udes.model.astd.types;

import com.udes.model.astd.base.ASTD;
import com.udes.model.astd.base.QuantifiedASTD;
import com.udes.model.astd.items.*;
import com.udes.model.astd.tojson.ToJson;
import com.udes.model.il.conditions.AndCondition;
import com.udes.model.il.conditions.CallCondition;
import com.udes.model.il.conditions.Condition;
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

import javax.swing.plaf.nimbus.State;
import java.security.CodeSource;
import java.util.*;

public class QInterleaving extends QuantifiedASTD {

    public class PredicateHolder {
        public Condition cond;
        public String key;
    }

    public PredicateHolder pred;

    public QInterleaving(String name,
                         List<Variable> attributes,
                         List<Variable> params,
                         Action astdAction,
                         ASTD body,
                         Variable qvariable,
                         Domain domain) {
        super(name, attributes, params, astdAction, body, qvariable, domain);
        pred = new PredicateHolder();
    }

    public QInterleaving(String name, ASTD body, Variable qvariable, Domain domain) {
        super(name, body, qvariable, domain);
        pred = new PredicateHolder();
    }

    public QInterleaving() {
        super();
        pred = new PredicateHolder();
    }

    public Statement replaceVariable(Statement stmt, String var1, String var2){
        if(Conventions.isIFFI(stmt)){
            IFFIStatement iffiStmt = (IFFIStatement) stmt;
            List<Entry<Condition, Statement>> iffiList = new ArrayList<>(iffiStmt.getIFFIStatement());
            iffiList.forEach(iffi -> {
            });

        }else{
            SeqStatement seqStmt = (SeqStatement) stmt;
            List<Statement> stmtList = seqStmt.getStatement();
            stmtList.forEach(st -> {

            });
        }
        return stmt;
    }


    @Override
    public Statement init(ArrayList<ASTD> callList, String lets) {
        ArrayList<ASTD> ASTDTree = new ArrayList<>();
        ASTDTree.addAll(callList);
        ASTDTree.add(this);
        List<Statement> seqList = new ArrayList<>();
        String prfx = ASTDTree.get(0).prefixTree(ASTDTree) + ".";
        //String prfx = getParent().prefix(getName()) + ".";
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
                else
                    seqList.add(new AssignStatement(new Term(prfx + v.getName()), new Term(init)));
            });
        }
        seqList.add(new CallStatement(Conventions.CLEAR_VECTOR.replace(ILTranslator.USYMBOL_2,prfx+Conventions.FUNC_STATE),
                new ArrayList<>()));
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
        // it's a quantified interleaving or quantified synchronization
        // it's final only if there exists a variable x such that final(A.b)(f(x)) is final -> LIONEL
        // it's final if all the states of the quantification are final. For all c, c : T . final(A.b)(f(c)) is final -> TR-25
        String forallName = Conventions.FOR_ALL + "_" + getName() + Conventions.FINAL;
        List<String> variable = new ArrayList<>();
        String prfx = ASTDTree.get(0).prefixTree(ASTDTree, getQvariable().getRef())+".";
        //String prfx = getParent().prefix(getQvariable().getRef())+".";
        variable.add(prfx+getQvariable().getName());
        CallCondition finalcond = new CallCondition(forallName, variable);

        return new AndCondition(Arrays.asList(finalcond, getBody()._final(ASTDTree)));
    }

    @Override
    public Condition _finalForSub(ArrayList<ASTD> callList) {
        ArrayList<ASTD> ASTDTree = new ArrayList<>();
        ASTDTree.addAll(callList);
        ASTDTree.add(this);

        return new AndCondition(Arrays.asList(getBody()._finalForSub(ASTDTree)));
    }

    @Override
    public List<Function> generateFinalFunc(ArrayList<ASTD> callList) {
        ArrayList<ASTD> ASTDTree = new ArrayList<>();
        ASTDTree.addAll(callList);
        ASTDTree.add(this);
        String forallName = Conventions.FOR_ALL + "_" + getName() + Conventions.FINAL;
        SeqStatement seqStmt;

        List<Function> bodyFunc = getBody().generateFinalFunc(ASTDTree);

        String domain = Conventions.getVarSet(getName());
        String type = getQvariable().getType();
        String domType  = (getDomain() != null) ? getDomain().getType() : "";

        String prfx = ASTDTree.get(0).prefixTree(ASTDTree, getQvariable().getRef()) + ".";

        if(!domType.equals(Constants.UNBOUNDEDDOMAIN)){
            seqStmt = new SeqStatement(Arrays.asList(new AssignStatement(new Term(getQvariable().getName()), new Term(Conventions.CHOICE_VAR+Constants.FIRST_ITEM)),
                    new IFFIStatement(Arrays.asList(
                            new Entry<>(
                                    new AndCondition(Arrays.asList(
                                            getBody()._final(ASTDTree))),
                                    new AssignStatement(new Term(Conventions.EXEC), new Term((Conventions.EXEC +" + "+ Conventions.TRUE)))
                            ),
                            new Entry<>(null, new CallStatement(Conventions.RETURN_CALL, Arrays.asList(Conventions.FALSE)))
                    ))
            ));

            ForStatement forStmt = new ForStatement(new CallCondition(Conventions.FOR,
                    Arrays.asList(Conventions.AUTO_CONST_TYPE+"&", Conventions.CHOICE_VAR, prfx+Conventions.FUNC_STATE)), seqStmt);

            Condition finalCopy = (Condition) Utils.copyObject(getBody()._final(ASTDTree));
            Condition init = finalCopy.substitute(getBody().init(ASTDTree, Conventions.CST), ASTDTree);

            SeqStatement finalStmt = new SeqStatement(Arrays.asList(new DeclStatement(
                            new Variable(Conventions.EXEC, Conventions.BOOL_TYPE, Conventions.FALSE, null)), forStmt,
                    new IFFIStatement(Arrays.asList(
                            new Entry<>(
                                    new CallCondition(Conventions.EQUALS, Arrays.asList(Conventions.EXEC, domain+Constants.SIZE)),
                                    new CallStatement(Conventions.RETURN_CALL, Arrays.asList(Conventions.TRUE))),
                            new Entry<>(
                                    new CallCondition(Conventions.LESSER, Arrays.asList(Conventions.EXEC, domain+Constants.SIZE)),
                                    new IFFIStatement(Arrays.asList(new Entry<>(
                                            init ,
                                            new CallStatement(Conventions.RETURN_CALL, Arrays.asList(Conventions.TRUE)))
                                    ))
                            ))),
                    new CallStatement(Conventions.RETURN_CALL, Arrays.asList(Conventions.FALSE))));

            List<Variable> params = new ArrayList<>(getAllQVariables(ASTDTree));

            Function func = new Function(forallName, params, Conventions.BOOL_TYPE, finalStmt);
            List<Function> funcList = new ArrayList<>();
            funcList.add(func);

            if(bodyFunc != null && !bodyFunc.isEmpty()){
                funcList.addAll(bodyFunc);
            }

            return funcList;

        }
        else{
            seqStmt = new SeqStatement(Arrays.asList(new AssignStatement(new Term(getQvariable().getName()), new Term(Conventions.CHOICE_VAR+Constants.FIRST_ITEM)),
                    new IFFIStatement(Arrays.asList(
                            new Entry<>(
                                    new AndCondition(Arrays.asList(
                                            getBody()._final(ASTDTree))),
                                    new AssignStatement(new Term(Conventions.EXEC), new Term((Conventions.EXEC +" + "+ Conventions.TRUE)))
                            )
                    ))
            ));

            Condition finalCopy = (Condition) Utils.copyObject(getBody()._final(ASTDTree));
            Condition init = finalCopy.substitute(getBody().init(ASTDTree, Conventions.CST), ASTDTree);

            ForStatement forStmt = new ForStatement(new CallCondition(Conventions.FOR,
                    Arrays.asList(Conventions.AUTO_CONST_TYPE+"&", Conventions.CHOICE_VAR, prfx+Conventions.FUNC_STATE)), seqStmt);

            SeqStatement finalStmt = new SeqStatement(Arrays.asList(new DeclStatement(
                            new Variable(Conventions.EXEC, Conventions.BOOL_TYPE, Conventions.FALSE, null)), forStmt,
                    new IFFIStatement(Arrays.asList(new Entry<>(
                            new AndCondition(Arrays.asList(
                                    new CallCondition(Conventions.EQUALS, Arrays.asList(Conventions.EXEC, prfx+Conventions.FUNC_STATE+Constants.SIZE))
                                    ,init
                                    )),
                            new CallStatement(Conventions.RETURN_CALL, Arrays.asList(Conventions.TRUE))))),
                    new CallStatement(Conventions.RETURN_CALL, Arrays.asList(Conventions.FALSE))));

            List<Variable> params = new ArrayList<>(getAllQVariables(ASTDTree));

            Function func = new Function(forallName, params, Conventions.BOOL_TYPE, finalStmt);
            List<Function> funcList = new ArrayList<>();
            funcList.add(func);

            if(bodyFunc != null && !bodyFunc.isEmpty()){
                funcList.addAll(bodyFunc);
            }

            return funcList;

        }
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
        //we add the map f<qvaribleType, states>
        props.add(new Variable(Conventions.FUNC_STATE, Conventions.getMapType(getQvariable().getType(),
                               Conventions.getStructName(getBody().getName())),null, getName()));
        if(Constants.COND_OPT_OPTS) {
            props.add(new Variable("cond_1", Conventions.BOOL_TYPE, null, getName()));
        }
        //add the record
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
        if(astdAction != null && Constants.DEBUG) {
            String debugMsg = ";\n" + Constants.LOGGER_MSG.replace(ILTranslator.USYMBOL_1, astdAction.getCode());
            astdAction = new Action(astdAction.getCode() + debugMsg);
        }
        List<Variable> evtParams = new ArrayList<>((getParent().getAllEventParams(e)));
        //List<Variable> qVarList = new ArrayList<>((getParent().getAllEventQVariables(e)));
        List<Variable> qVarList = new ArrayList<>(getAllQVariables(ASTDTree));

        Entry<Condition, Statement> eventBody;
        if(e.getName().equals("Step") && (getBody() instanceof QInterleaving || getBody() instanceof QSynchronization || getBody() instanceof QChoice)){
            eventBody = getBody().trans_event_step(e,timed, ASTDTree, lets);
        }
        else{
            eventBody = getBody().trans_event(e,timed, ASTDTree, lets);
        }
        // changing clock variables
        if (timed.getValue() && !eventBody.isEmpty()) {
            eventBody.getKey().substitute(new AssignStatement(new Term(Conventions.LETS), new Term(prfx + Conventions.QUANTIFIED_CLOCK + getBody().getName())), ASTDTree);
        }

        Variable qvar = getQvariable();

        if (eventBody != null && !eventBody.isEmpty()) {
            if (evtParams != null && !evtParams.isEmpty()) {
                List<String> tmp = new ArrayList<>();
                List<Variable> varList = new ArrayList<>();
                for (Variable v : qVarList) {
                    //tmp.add(getParent().prefix(Constants.DUMMY_PREFIX2.get(v).getName()) + "." + v.getName());
                    tmp.add(Constants.DUMMY_PREFIX.get(v));
                }
                List<Variable> otherparams = new ArrayList<>();
                for (Variable _v : evtParams) {
                    boolean found = false;
                    for (Variable v : qVarList) {
                        if (_v.getName().compareTo(v.getName()) == 0) {
                            tmp.add(Conventions.DUMMY_PARAMS + _v.getName());
                            varList.add(new Variable(Conventions.DUMMY_PARAMS + _v.getName(),
                                    v.getType(), "ANY", getName()));
                            found = true;
                        }
                    }
                    if (!found)
                        otherparams.add(_v);
                }
//                for (Variable _v : otherparams) {
//                    _v.setInit(Conventions.DUMMY_PARAMS + getQvariable().getName());
//                    tmp.add(_v.getName());
//                    varList.add(_v);
//                }
                evtParams = varList;
                pred.key = Conventions.EXISTS + "_" + getName() + e.getName() + "_" + qvar.getName();
                pred.cond = new CallCondition(pred.key, tmp);
                evtBody.setKey(pred.cond);
                List<Statement> stmtList = new ArrayList<>();
                if (Constants.COND_OPT_OPTS) {
                    if (isRoot()) {
                        stmtList.add(new AssignStatement(new Term(prfx + "cond_1"), pred.cond));
                    }
                    pred.cond = new CallCondition(-1, Conventions.EQUALS1,
                            Arrays.asList(prfx + "cond_1", pred.cond));
                    evtBody.setKey(pred.cond);
                    pred.cond = new Bool(prfx + "cond_1");
                }
                SeqStatement act = new SeqStatement(Arrays.asList());
                act.setStatement(Arrays.asList(eventBody.getValue(), astdAction));
                if (isRoot()) {
                    stmtList.add(new IFFIStatement(Collections.singletonList(new Entry<>(pred.cond, act))));
                    evtBody.setValue(new SeqStatement(stmtList));
                } else {
                    evtBody.setValue(new IFFIStatement(Collections.singletonList(new Entry<>(pred.cond, act))));
                }
                if (pred.key != null) {
                    Utils.qvarCond.put(Constants.DUMMY_PREFIX.get(qvar), eventBody.getKey());
                    Event _e = new Event(e.getName(), evtParams);
                    Utils.qvarDic.put(pred.key, _e);
                }
            } else {
                List<String> tmp = new ArrayList<>();
                for (Variable v : qVarList) {
                    //tmp.add(getParent().prefix(Constants.DUMMY_PREFIX2.get(v).getName()) + "." + v.getName());
                    tmp.add(Constants.DUMMY_PREFIX.get(v));
                }


                pred.key = Conventions.EXISTS + "_" + getName() + e.getName() + "_" + qvar.getName();

                pred.cond = new CallCondition(pred.key, tmp);
                evtBody.setKey(pred.cond);
                List<Statement> stmtList = new ArrayList<>();
                if (Constants.COND_OPT_OPTS) {
                    if (isRoot()) {
                        stmtList.add(new AssignStatement(new Term(prfx + "cond_1"), pred.cond));
                    }
                    pred.cond = new CallCondition(-1, Conventions.EQUALS1,
                            Arrays.asList(prfx + "cond_1", pred.cond));
                    evtBody.setKey(pred.cond);
                    pred.cond = new Bool(prfx + "cond_1");
                }
                Statement act = new SeqStatement(Arrays.asList(eventBody.getValue(), astdAction));
                if (isRoot()) {
                    stmtList.add(new IFFIStatement(Collections.singletonList(new Entry<>(pred.cond, act))));
                    evtBody.setValue(new SeqStatement(stmtList));
                } else {
                    evtBody.setValue(new IFFIStatement(Collections.singletonList(new Entry<>(pred.cond, act))));
                }
                if (pred.key != null) {
                    Utils.qvarCond.put(Constants.DUMMY_PREFIX.get(qvar), eventBody.getKey());
                    Event _e = new Event(e.getName(), evtParams);
                    Utils.qvarDic.put(pred.key, _e);
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
        if(astdAction != null && Constants.DEBUG) {
            String debugMsg = ";\n" + Constants.LOGGER_MSG.replace(ILTranslator.USYMBOL_1, astdAction.getCode());
            astdAction = new Action(astdAction.getCode() + debugMsg);
        }
        List<Variable> evtParams = new ArrayList<>((getParent().getAllEventParams(e)));
        //List<Variable> qVarList = new ArrayList<>((getParent().getAllEventQVariables(e)));
        List<Variable> qVarList = new ArrayList<>(getAllQVariables(ASTDTree));;

        Entry<Condition, Statement> eventBody = getBody().trans_event_step(e,timed, ASTDTree, lets);

        // changing clock variables
        if (timed.getValue() && !eventBody.isEmpty()) {
            eventBody.getKey().substitute(new AssignStatement(new Term(Conventions.LETS), new Term(prfx + Conventions.QUANTIFIED_CLOCK + getBody().getName())), ASTDTree);
        }
        Variable qvar = getQvariable();

        if (eventBody != null && !eventBody.isEmpty()) {
            //STEP DOES NOT HAVE PARAMETERS -> BUT THE EXISTS HAS THE QUANTIFIED VARIABLES!!
            List<String> tmp = new ArrayList<>();
            for (Variable v : qVarList) {
                //tmp.add(getParent().prefix(Constants.DUMMY_PREFIX2.get(v).getName()) + "." + v.getName());
                tmp.add(Constants.DUMMY_PREFIX.get(v));
            }

            pred.key = Conventions.EXISTS + "_" + getName() + e.getName() + "_" + qvar.getName();

            pred.cond = new CallCondition(pred.key, tmp);
            evtBody.setKey(pred.cond);
            List<Statement> stmtList = new ArrayList<>();
            if (Constants.COND_OPT_OPTS) {
                if (isRoot()) {
                    stmtList.add(new AssignStatement(new Term(prfx + "cond_1"), pred.cond));
                }
                pred.cond = new CallCondition(-1, Conventions.EQUALS1,
                        Arrays.asList(prfx + "cond_1", pred.cond));
                evtBody.setKey(pred.cond);
                pred.cond = new Bool(prfx + "cond_1");
            }
            Statement act = new SeqStatement(Arrays.asList(eventBody.getValue(), astdAction));
            String domType = (getDomain() != null) ? getDomain().getType() : "";
            if (!domType.equals(Constants.UNBOUNDEDDOMAIN)) {
                //defined domain
                if (isRoot()) {
                    Term randomName = new Term(Utils.generateNameIfNotExists(Conventions.DUMMY_PARAMS+getQvariable().getName()+Conventions.DUMMY_PARAMS));
                    SeqStatement seqStmt = new SeqStatement(Arrays.asList(new AssignStatement(
                            new Term(prfx + qvar.getName()),
                            randomName), new SeqStatement(stmtList)));
                    ForStatement forStmt = new ForStatement(new CallCondition(Conventions.FOR,
                            Arrays.asList(getQvariable().getType(),randomName.getId(), Conventions.getVarSet(getName()))), seqStmt);
                    stmtList.add(new IFFIStatement(Collections.singletonList(new Entry<>(pred.cond, act))));
                    evtBody.setValue(new SeqStatement(Arrays.asList(forStmt)));
                } else {
                    Term randomName = new Term(Utils.generateNameIfNotExists(Conventions.DUMMY_PARAMS+getQvariable().getName()+Conventions.DUMMY_PARAMS));

                    SeqStatement seqStmt = new SeqStatement(Arrays.asList(new AssignStatement(new Term(prfx + qvar.getName()),
                            randomName), new SeqStatement(stmtList)));
                    ForStatement forStmt = new ForStatement(new CallCondition(Conventions.FOR,
                            Arrays.asList(getQvariable().getType(),
                                    randomName.getId(),
                                    Conventions.getVarSet(getName()))),
                            seqStmt);
                    stmtList.add(new IFFIStatement(Collections.singletonList(new Entry<>(pred.cond, act))));
                    evtBody.setValue(new SeqStatement(Arrays.asList(forStmt)));
                }
            } else {
                //unbounded domain
                if (isRoot()) {
                    Term randomName = new Term(Utils.generateNameIfNotExists(Conventions.DUMMY_PARAMS+getQvariable().getName()+Conventions.DUMMY_PARAMS));
                    SeqStatement seqStmt = new SeqStatement(Arrays.asList(new AssignStatement(
                            new Term(prfx + qvar.getName()),
                            randomName), new SeqStatement(stmtList)));
                    ForStatement forStmt = new ForStatement(new CallCondition(Conventions.FOR,
                            Arrays.asList(
                                    Conventions.AUTO_CONST_TYPE + "&",
                                    randomName.getId(),
                                    ASTDTree.get(0).prefixTree(ASTDTree, getQvariable().getRef()) + "." + Conventions.FUNC_STATE)),
                            seqStmt);
                    stmtList.add(new IFFIStatement(Collections.singletonList(new Entry<>(pred.cond, act))));
                    evtBody.setValue(new SeqStatement(Arrays.asList(forStmt)));
                } else {
                    Term randomName = new Term(Utils.generateNameIfNotExists(Conventions.DUMMY_PARAMS+getQvariable().getName()+Conventions.DUMMY_PARAMS));
                    SeqStatement seqStmt = new SeqStatement(Arrays.asList(new AssignStatement(new Term(prfx + qvar.getName()),
                            randomName), new SeqStatement(stmtList)));
                    ForStatement forStmt = new ForStatement(new CallCondition(Conventions.FOR,
                            Arrays.asList(
                                    Conventions.AUTO_CONST_TYPE + "&",
                                    randomName.getId(),
                                    ASTDTree.get(0).prefixTree(ASTDTree, getQvariable().getRef()) + "." + Conventions.FUNC_STATE)),
                            seqStmt);
                    stmtList.add(new IFFIStatement(Collections.singletonList(new Entry<>(pred.cond, act))));
                    evtBody.setValue(new SeqStatement(Arrays.asList(forStmt)));
                }
            }
            if (pred.key != null) {
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
        return "";
    }

    @Override
    public Statement propertyMapping() {
        List<Statement> stmtList = new ArrayList<>();
        // generate common ASTD properties

        Statement commonProps = super.propertyMapping();
        if(commonProps != null)
            stmtList.add(commonProps);

        // generate QInterleaving properties
        List<String> qinterProps = Arrays.asList(ExecSchemaParser.SUB_STATES,
                                                 ExecSchemaParser.QSYNCH_VAR, ExecSchemaParser.VALUE);
        qinterProps.forEach(prop -> {
            DeclStatement decStmt1 = new DeclStatement(new Constant((this.getName() + "_" + prop).toUpperCase(),
                                                                     Conventions.STRING, prop, getName()));
            stmtList.add(decStmt1);
        });
        Constants.EXEC_SCHEMA_PROPS = Constants.EXEC_SCHEMA_PROPS.replace(ILTranslator.USYMBOL_1,
                                                                          QInterleaving.class.getSimpleName());

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
        //TODO CHANGE THIS TO PREFIXTREE
        String prfx = getParent().prefix(getName()) + ".";
        stmtList.add(fillQInterleavingProperties(tojson.getNodeIndex(), prfx));

        List<String> subIndexes = updateSubIndexes(tojson);
        if(subIndexes != null) {
            tojson.setSubNodeIndex(subIndexes);
            setToJson(tojson);
        }

        ASTD subASTD = getBody();
        ToJson subToJson = subASTD.toJson();

        if(tojson.getSubNodeIndex() != null) {
            String var_tmp = getName().toLowerCase() + "_" + Conventions.CHOICE_VAR;
            stmtList.add(new AssignStatement(new Term(Conventions.ARRAY_ELEM
                    .replace(ILTranslator.USYMBOL_2, tojson.getSubNodeIndex().get(0))
                    .replace(ILTranslator.USYMBOL_1, (this.getName() + "_"
                                                      + ExecSchemaParser.SUB_STATES).toUpperCase())),
                    new Term(Conventions.JSON_ARRAY_INSTANCE)));

            String subASTDCopiesIdx = Conventions.ARRAY_ELEM
                        .replace(ILTranslator.USYMBOL_2, Conventions.ARRAY_ELEM
                                .replace(ILTranslator.USYMBOL_2, tojson.getSubNodeIndex().get(0))
                                .replace(ILTranslator.USYMBOL_1, (this.getName() + "_"
                                                                  + ExecSchemaParser.SUB_STATES).toUpperCase()))
                        .replace(ILTranslator.USYMBOL_1, var_tmp);

            subToJson.setNodeIndex(Conventions.ARRAY_ELEM
                    .replace(ILTranslator.USYMBOL_2, subASTDCopiesIdx)
                    .replace(ILTranslator.USYMBOL_1, ExecSchemaParser.CURRENT_SUB_STATE.toUpperCase()));

            List<String> subIdx = subASTD.updateSubIndexes(subToJson);
            if (subIdx != null) {
                subToJson.setSubNodeIndex(subIdx);
                subASTD.setToJson(subToJson);
            }
            List<Statement> subSeq = new ArrayList<>();
            String qvar_type = getQvariable().getType().toLowerCase();

            subSeq.add(new AssignStatement(new Term(prfx + this.getQvariable().getName()),
                                           new Term(this.getQvariable().getName() + Constants.FIRST_ITEM)));

            Statement subStmt = new AssignStatement(new Term(Conventions.ARRAY_ELEM
                                    .replace(ILTranslator.USYMBOL_2, subASTDCopiesIdx)
                                    .replace(ILTranslator.USYMBOL_1,
                                            (this.getName() + "_" + ExecSchemaParser.QSYNCH_VAR).toUpperCase())),
                                    new Term("\"" + this.getQvariable().getName() + "\""));
            subSeq.add(subStmt);
            String  funcState = Conventions.FUNC_STATE,
                    funcIdx   = Conventions.ARRAY_ELEM
                                .replace(ILTranslator.USYMBOL_2, prfx + funcState)
                                .replace(ILTranslator.USYMBOL_1, this.getQvariable().getName());
            Term term;
            if(qvar_type.contains(Conventions.INT)
                    || qvar_type.contains(Conventions.DOUBLE)
                    || qvar_type.contains(Conventions.BOOL_TYPE1)
                    || qvar_type.contains(Conventions.FLOAT)
                    || qvar_type.contains(Conventions.SHORT)
                    || qvar_type.contains(Conventions.LONG)) {
                 term = new Term(Conventions.TO_STRING.replace(ILTranslator.USYMBOL_1,
                         this.getQvariable().getName() + Constants.FIRST_ITEM));
                subStmt = new AssignStatement(new Term(Conventions.ARRAY_ELEM
                              .replace(ILTranslator.USYMBOL_2, subASTDCopiesIdx)
                              .replace(ILTranslator.USYMBOL_1,
                                       (this.getName() + "_" + ExecSchemaParser.VALUE).toUpperCase())), term);
            }
            else {
                // Manage for C++ types (std::map, std::list, std::vector, set, custom types)
                // Need to reimplement those types with method toString()

                Statement subStmt1 = new AssignStatement(new Term(Conventions.ARRAY_ELEM
                                        .replace(ILTranslator.USYMBOL_2, subASTDCopiesIdx)
                                        .replace(ILTranslator.USYMBOL_1,
                                                (this.getName() + "_" + ExecSchemaParser.VALUE).toUpperCase())),
                                        new Term(Conventions.DUMP_JSON_OBJECT.replace(ILTranslator.USYMBOL_2,
                                                this.getQvariable().getName() + Constants.FIRST_ITEM))),
                          subStmt2 = new AssignStatement(new Term(Conventions.ARRAY_ELEM
                                         .replace(ILTranslator.USYMBOL_2, subASTDCopiesIdx)
                                         .replace(ILTranslator.USYMBOL_1,
                                                  (this.getName() + "_" + ExecSchemaParser.VALUE).toUpperCase())),
                                     new Term(Conventions.COMPLEX_TYPE_TO_STRING.replace(ILTranslator.USYMBOL_1,
                                        ILTranslator.USYMBOL_4 + this.getQvariable().getName()
                                                + Constants.FIRST_ITEM)));

                subStmt = new IFFIStatement(Arrays.asList(
                              new Entry<>(new Bool(Conventions.hasToStringMethodInClass(getQvariable().getType())),
                                          subStmt2),
                              new Entry<>(new CallCondition(Conventions.INSTANCE_OF
                                          .replace(ILTranslator.USYMBOL_1, getQvariable().getType()),
                                          Arrays.asList(ILTranslator.USYMBOL_4
                                                        + this.getQvariable().getName())), subStmt1),
                              new Entry<>(null, new CallStatement(Conventions.ERROR_LABEL3,
                                          Collections.singletonList(Conventions.getErrorMsg(null, 2))))
                ));
            }

            subSeq.add(subStmt);

            subStmt = subASTD.currentStateToJson();
            if (subStmt != null)
                subSeq.add(subStmt);

            subSeq.add(new AssignStatement(new Term(var_tmp), new Term(var_tmp + " + 1")));
            String prfx_q = Constants.DUMMY_PREFIX.get(getQvariable());
            prfx_q = prfx_q.substring(0, prfx_q.length() - getQvariable().getName().length());
            String domain = prfx_q
                    + Conventions.FUNC_STATE;
            DeclStatement declStmt = new DeclStatement(new Variable(var_tmp, Conventions.INT, "0", getName()));
            ForStatement forStmt = new ForStatement(new CallCondition(Conventions.FOR,
                                                Arrays.asList(Conventions.AUTO_CONST_TYPE,
                                                        ILTranslator.USYMBOL_4 + getQvariable().getName(), domain)),
                                                new SeqStatement(subSeq));
            stmtList.add(new SeqStatement(Arrays.asList(declStmt, forStmt)));
        }

        return new SeqStatement(stmtList);
    }


    private Statement fillQInterleavingProperties(String index, String prfx) {

        return new AssignStatement(new Term(Conventions.ARRAY_ELEM.replace(ILTranslator.USYMBOL_2, index)
                    .replace(ILTranslator.USYMBOL_1, ExecSchemaParser.TYPE.toUpperCase())),
                    new Term("\"" + ExecSchemaParser.QSYNCHRONIZATION + "\""));
    }
}


