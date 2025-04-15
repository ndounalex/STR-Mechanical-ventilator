package com.udes.model.astd.types;

import com.udes.model.astd.base.ASTD;
import com.udes.model.astd.base.BinaryASTD;
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

import java.util.*;

public class Choice extends BinaryASTD {

    public Choice(String name, List<Variable> attributes, List<Variable> params, Action astdAction, ASTD left, ASTD right) {
        super(name, attributes, params, astdAction, left, right);
    }

    public Choice(String name, ASTD left, ASTD right) {
        super(name, left, right);
    }

    public Choice() {
        super();
    }

    @Override
    public Statement init(ArrayList<ASTD> callList, String lets) {

        ArrayList<ASTD> ASTDTree = new ArrayList<>();
        ASTDTree.addAll(callList);
        ASTDTree.add(this);
        List<Statement> seqList = new ArrayList<>();
        String prfx = ASTDTree.get(0).prefixTree(ASTDTree) + ".";
        //String prfx = getParent().prefix(getName()) + ".";
        List<Variable> vList = getAttributes();
        String bState = Conventions.getStateVar(Choice.class);
        // init state
        seqList.add(new AssignStatement(new Term(prfx + bState), new Term(Conventions.NONE)));
        // init attributes
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

        //CHOICE DOES NOT INIT THE BODY

        return new SeqStatement(seqList);
    }

    @Override
    public Statement initforsub(ArrayList<ASTD> callList, Event e, Bool timed, String lets, boolean forFinal) {
        //important for quantified ASTD and closure

        ArrayList<ASTD> ASTDTree = new ArrayList<>();
        ASTDTree.addAll(callList);
        ASTDTree.add(this);
        List<Statement> seqList = new ArrayList<>();
        String prfx = ASTDTree.get(0).prefixTree(ASTDTree) + ".";
        //String prfx = getParent().prefix(getName()) + ".";
        List<Variable> vList = getAttributes();
        String bState = Conventions.getStateVar(Choice.class);
        // init state
        seqList.add(new AssignStatement(new Term(prfx + bState), new Term(Conventions.NONE)));
        // init attributes
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

        //IF THERE IS DECLARED VARIABLES INSIDE THE BODY, IT IS NOT TOUCHED YET. GOING DEEPER FOR THOSE VARIABLES
        seqList.add(getLeft().initforsub(ASTDTree, e, timed, lets, forFinal));
        seqList.add(getRight().initforsub(ASTDTree, e, timed, lets, forFinal));

        return new SeqStatement(seqList);
    }

    @Override
    public Condition _final(ArrayList<ASTD> callList) {
        ArrayList<ASTD> ASTDTree = new ArrayList<>();
        ASTDTree.addAll(callList);
        ASTDTree.add(this);
        String prfx = ASTDTree.get(0).prefixTree(ASTDTree) + ".";
        String chState = Conventions.getStateVar(Choice.class);
        // it's final only if no choice is made and the initial state of left or right is final
        // or the left astd or right astd has been chosen and it is final
        Condition finalLeftCpy = (Condition) Utils.copyObject(getLeft()._final(ASTDTree)),
                finalRightCpy = (Condition) Utils.copyObject(getRight()._final(ASTDTree));
        return new OrCondition(Arrays.asList(
                new AndCondition(Arrays.asList(
                        new CallCondition(Conventions.EQUALS, Arrays.asList(prfx + chState, Conventions.NONE)),
                        new OrCondition(Arrays.asList(finalLeftCpy.substitute(getLeft().init(ASTDTree, Conventions.CST), ASTDTree),
                                finalRightCpy.substitute(getRight().init(ASTDTree, Conventions.CST), ASTDTree))))),
                new AndCondition(Arrays.asList(
                        new CallCondition(Conventions.EQUALS, Arrays.asList(prfx + chState, Conventions.LEFT)),
                        getLeft()._final(ASTDTree))),
                new AndCondition(Arrays.asList(
                        new CallCondition(Conventions.EQUALS, Arrays.asList(prfx + chState, Conventions.RIGHT)),
                        getRight()._final(ASTDTree)))));
    }

    @Override
    public Condition _finalForSub(ArrayList<ASTD> callList) {
        ArrayList<ASTD> ASTDTree = new ArrayList<>();
        ASTDTree.addAll(callList);
        ASTDTree.add(this);
        String prfx = ASTDTree.get(0).prefixTree(ASTDTree) + ".";
        String chState = Conventions.getStateVar(Choice.class);
        // it's final only if no choice is made and the initial state of left or right is final
        // or the left astd or right astd has been chosen and it is final
        Condition finalLeftCpy = (Condition) Utils.copyObject(getLeft()._finalForSub(ASTDTree)),
                finalRightCpy = (Condition) Utils.copyObject(getRight()._finalForSub(ASTDTree));
        return new OrCondition(Arrays.asList(
                new AndCondition(Arrays.asList(
                        new CallCondition(Conventions.EQUALS, Arrays.asList(prfx + chState, Conventions.NONE)),
                        new OrCondition(Arrays.asList(finalLeftCpy.substitute(getLeft().init(ASTDTree, Conventions.CST), ASTDTree),
                                finalRightCpy.substitute(getRight().init(ASTDTree, Conventions.CST), ASTDTree))))),
                new AndCondition(Arrays.asList(
                        new CallCondition(Conventions.EQUALS, Arrays.asList(prfx + chState, Conventions.LEFT)),
                        getLeft()._final(ASTDTree))),
                new AndCondition(Arrays.asList(
                        new CallCondition(Conventions.EQUALS, Arrays.asList(prfx + chState, Conventions.RIGHT)),
                        getRight()._final(ASTDTree)))));
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
        // astd state
        String stateType = Conventions.getStateType(Choice.class);
        props.add(new Variable(Conventions.getStateVar(Choice.class), stateType, null, getName()));
        enumList.add(new Enum(stateType, Arrays.asList(Conventions.NONE, Conventions.LEFT, Conventions.RIGHT)));
        // ignore elem ASTD for optimization
        if(!Conventions.isElem(getLeft())) {
            props.add(new Variable(Conventions.getStructVar(getLeft().getName()),
                                   Conventions.getStructName(getLeft().getName()), null, getName()));
        }
        //right struct var
        // ignore elem ASTD for optimization
        if(!Conventions.isElem(getRight())) {
            props.add(new Variable(Conventions.getStructVar(getRight().getName()),
                                   Conventions.getStructName(getRight().getName()), null, getName()));
        }
        if(Constants.COND_OPT_OPTS) {
            for (int i = 1; i < 5; i++) {
                props.add(new Variable("cond_" + i, Conventions.BOOL_TYPE, null, getName()));
            }
        }
        recordList.add(new Record(structName, props));
        // left struct
        Entry<List<Enum>, List<Record>> recordLeft = getLeft().trans_type();
        if (recordLeft != null && !recordLeft.getKey().isEmpty())
            enumList.addAll(recordLeft.getKey());
        if (recordLeft != null && !recordLeft.getValue().isEmpty())
            recordList.addAll(recordLeft.getValue());
        // right struct
        Entry<List<Enum>, List<Record>> recordRight = getRight().trans_type();
        if (recordLeft != null && !recordRight.getKey().isEmpty())
            enumList.addAll(recordRight.getKey());
        if (recordLeft != null && !recordRight.getValue().isEmpty())
            recordList.addAll(recordRight.getValue());

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
        Entry<Condition, Statement> eventLeft, eventRight;
        if(e.getName().equals("Step") && (getLeft() instanceof QInterleaving ||
                getLeft() instanceof QSynchronization ||
                getRight() instanceof QInterleaving ||
                getRight() instanceof  QSynchronization || getLeft() instanceof QChoice || getRight() instanceof QChoice || getLeft() instanceof QFlow || getRight() instanceof QFlow)){
            // event body left
            eventLeft = getLeft().trans_event_step(e, timed, ASTDTree, lets);
            // event body right
            eventRight = getRight().trans_event_step(e, timed, ASTDTree, lets);
        }
        else{
            // event body left
            eventLeft = getLeft().trans_event(e, timed, ASTDTree, lets);
            // event body right
            eventRight = getRight().trans_event(e, timed, ASTDTree, lets);
        }
        // state & prefix
        String choiceState = Conventions.getStateVar(Choice.class);

        if(!eventLeft.isEmpty() && !eventRight.isEmpty()) {
            // left & right translation rules for conditions
            Statement init_l = getLeft().init(ASTDTree, lets), init_r = getRight().init(ASTDTree, lets);
            Condition c_1, c_2, c_3, c_4;
            Condition leftCpy = (Condition) Utils.copyObject(eventLeft.getKey());
            c_1 = new AndCondition(Arrays.asList(new CallCondition(Conventions.EQUALS, Arrays.asList(
                        prfx + choiceState, Conventions.NONE)), leftCpy.substitute(init_l, ASTDTree)));
            c_2 = new AndCondition(Arrays.asList(new CallCondition(Conventions.EQUALS, Arrays.asList(
                        prfx + choiceState, Conventions.LEFT)),
                        eventLeft.getKey()));
            Condition rightCpy = (Condition) Utils.copyObject(eventRight.getKey());
            c_3 = new AndCondition(Arrays.asList(new CallCondition(Conventions.EQUALS, Arrays.asList(
                        prfx + choiceState, Conventions.NONE)),
                        rightCpy.substitute(init_r, ASTDTree)));
            c_4 = new AndCondition(Arrays.asList(new CallCondition(Conventions.EQUALS, Arrays.asList(
                        prfx + choiceState, Conventions.RIGHT)),
                        eventRight.getKey()));
            evtBody.setKey(new OrCondition(Arrays.asList(c_1, c_2, c_3, c_4)));
            List<Statement> stmtList = new ArrayList<>();
            if(Constants.COND_OPT_OPTS) {
                if(isRoot()) {
                    stmtList.add(new AssignStatement(new Term(prfx + "cond_1"), c_1));
                    stmtList.add(new AssignStatement(new Term(prfx + "cond_2"), c_2));
                    stmtList.add(new AssignStatement(new Term(prfx + "cond_3"), c_3));
                    stmtList.add(new AssignStatement(new Term(prfx + "cond_4"), c_4));
                }
                c_1 = new CallCondition(-1, Conventions.EQUALS1, Arrays.asList(prfx + "cond_1", c_1));
                c_2 = new CallCondition(-1, Conventions.EQUALS1, Arrays.asList(prfx + "cond_2", c_2));
                c_3 = new CallCondition(-1, Conventions.EQUALS1, Arrays.asList(prfx + "cond_3", c_3));
                c_4 = new CallCondition(-1, Conventions.EQUALS1, Arrays.asList(prfx + "cond_4", c_4));
                evtBody.setKey(new OrCondition(Arrays.asList(c_1, c_2, c_3, c_4)));
                c_1 = new Bool(prfx + "cond_1");
                c_2 = new Bool(prfx + "cond_2");
                c_3 = new Bool(prfx + "cond_3");
                c_4 = new Bool(prfx + "cond_4");
            }

            // left & right translation rules for actions
            Statement leftStmt = new SeqStatement(Arrays.asList(eventLeft.getValue(), astdAction)),
                      leftStmtCpy = (Statement) Utils.copyObject(leftStmt),
                      rightStmt = new SeqStatement(Arrays.asList(eventRight.getValue(), astdAction)),
                      rightStmtCpy = (Statement) Utils.copyObject(rightStmt);
            Statement act_1 = new SeqStatement(Arrays.asList(new AssignStatement(new Term(prfx + choiceState),
                                        new Term(Conventions.LEFT)), init_l, leftStmtCpy)),
                      act_2 = new SeqStatement(Arrays.asList(eventLeft.getValue(), astdAction)),
                      act_3 = new SeqStatement(Arrays.asList(new AssignStatement(new Term(prfx + choiceState),
                                        new Term(Conventions.RIGHT)), init_r, rightStmtCpy)),
                      act_4 = new SeqStatement(Arrays.asList(eventRight.getValue(), astdAction));

            if(isRoot()) {
                stmtList.add(new IFFIStatement(Arrays.asList(new Entry<>(c_1, act_1), new Entry<>(c_2, act_2),
                                                             new Entry<>(c_3, act_3), new Entry<>(c_4, act_4))));
                evtBody.setValue(new SeqStatement(stmtList));
            }
            else {
                evtBody.setValue(new IFFIStatement(Arrays.asList(new Entry<>(c_1, act_1), new Entry<>(c_2, act_2),
                                                                 new Entry<>(c_3, act_3), new Entry<>(c_4, act_4))));
            }
        }
        else if(!eventLeft.isEmpty() && eventRight.isEmpty()) {
            // left translation rules for conditions
            Statement init_l = getLeft().init(ASTDTree, lets);
            Condition c_1, c_2;
            Condition leftCpy = (Condition) Utils.copyObject(eventLeft.getKey());
            c_1 = new AndCondition(Arrays.asList(new CallCondition(Conventions.EQUALS, Arrays.asList(
                      prfx + choiceState, Conventions.NONE)), leftCpy.substitute(init_l, ASTDTree)));
            c_2 = new AndCondition(Arrays.asList(new CallCondition(Conventions.EQUALS, Arrays.asList(
                      prfx + choiceState, Conventions.LEFT)), eventLeft.getKey()));
            evtBody.setKey(new OrCondition(Arrays.asList(c_1, c_2)));
            List<Statement> stmtList = new ArrayList<>();
            if(Constants.COND_OPT_OPTS) {
                if(isRoot()) {
                    stmtList.add(new AssignStatement(new Term(prfx + "cond_1"), c_1));
                    stmtList.add(new AssignStatement(new Term(prfx + "cond_2"), c_2));
                }
                c_1 = new CallCondition(-1, Conventions.EQUALS1, Arrays.asList(prfx + "cond_1", c_1));
                c_2 = new CallCondition(-1, Conventions.EQUALS1, Arrays.asList(prfx + "cond_2", c_2));
                evtBody.setKey(new OrCondition(Arrays.asList(c_1, c_2)));
                c_1 = new Bool(prfx + "cond_1");
                c_2 = new Bool(prfx + "cond_2");
            }
            // left translation rules for actions
            Statement leftStmt = new SeqStatement(Arrays.asList(eventLeft.getValue(), astdAction)),
                      leftStmtCpy = (Statement) Utils.copyObject(leftStmt);
            Statement act_1 = new SeqStatement(Arrays.asList(new AssignStatement(new Term(prfx + choiceState),
                                  new Term(Conventions.LEFT)), init_l, leftStmtCpy)),
                      act_2 = new SeqStatement(Arrays.asList(eventLeft.getValue(), astdAction));
            if(isRoot()) {
                stmtList.add(new IFFIStatement(Arrays.asList(new Entry<>(c_1, act_1), new Entry<>(c_2, act_2))));
                evtBody.setValue(new SeqStatement(stmtList));
            }
            else {
                evtBody.setValue(new IFFIStatement(Arrays.asList(new Entry<>(c_1, act_1), new Entry<>(c_2, act_2))));
            }
        }
        else if(eventLeft.isEmpty() && !eventRight.isEmpty()) {
            // right translation rules for conditions
            Statement init_r = getRight().init(ASTDTree, lets);
            Condition c_3, c_4;
            Condition rightCpy = (Condition) Utils.copyObject(eventRight.getKey());
            c_3 = new AndCondition(Arrays.asList(new CallCondition(Conventions.EQUALS, Arrays.asList(
                      prfx + choiceState, Conventions.NONE)), rightCpy.substitute(init_r, ASTDTree)));
            c_4 = new AndCondition(Arrays.asList(new CallCondition(Conventions.EQUALS, Arrays.asList(
                      prfx + choiceState, Conventions.RIGHT)), eventRight.getKey()));
            evtBody.setKey(new OrCondition(Arrays.asList(c_3, c_4)));
            List<Statement> stmtList = new ArrayList<>();
            if(Constants.COND_OPT_OPTS) {
                if(isRoot()) {
                    stmtList.add(new AssignStatement(new Term(prfx + "cond_3"), c_3));
                    stmtList.add(new AssignStatement(new Term(prfx + "cond_4"), c_4));
                }
                c_3 = new CallCondition(-1, Conventions.EQUALS1, Arrays.asList(prfx + "cond_3", c_3));
                c_4 = new CallCondition(-1, Conventions.EQUALS1, Arrays.asList(prfx + "cond_4", c_4));
                evtBody.setKey(new OrCondition(Arrays.asList(c_3, c_4)));
                c_3 = new Bool(prfx + "cond_3");
                c_4 = new Bool(prfx + "cond_4");
            }
            // right translation rules for actions
            Statement rightStmt = new SeqStatement(Arrays.asList(eventRight.getValue(), astdAction)),
                      rightStmtCpy = (Statement) Utils.copyObject(rightStmt);
            Statement act_3 = new SeqStatement(Arrays.asList(new AssignStatement(new Term(prfx + choiceState),
                                  new Term(Conventions.RIGHT)), init_r, rightStmtCpy)),
                      act_4 = new SeqStatement(Arrays.asList(eventRight.getValue(), astdAction));
            if(isRoot()) {
                stmtList.add(new IFFIStatement(Arrays.asList(new Entry<>(c_3, act_3), new Entry<>(c_4, act_4))));
                evtBody.setValue(new SeqStatement(stmtList));
            }
            else {
                evtBody.setValue(new IFFIStatement(Arrays.asList(new Entry<>(c_3, act_3), new Entry<>(c_4, act_4))));
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
        // event body left
        Entry<Condition, Statement> eventLeft = getLeft().trans_event_step(e, timed, ASTDTree, lets);
        // event body right
        Entry<Condition, Statement> eventRight = getRight().trans_event_step(e, timed, ASTDTree, lets);
        // state & prefix
        String choiceState = Conventions.getStateVar(Choice.class);

        if(!eventLeft.isEmpty() && !eventRight.isEmpty()) {
            // left & right translation rules for conditions
            Statement init_l = getLeft().init(ASTDTree, lets), init_r = getRight().init(ASTDTree, lets);
            Condition c_1, c_2, c_3, c_4;
            Condition leftCpy = (Condition) Utils.copyObject(eventLeft.getKey());
            c_1 = new AndCondition(Arrays.asList(new CallCondition(Conventions.EQUALS, Arrays.asList(
                    prfx + choiceState, Conventions.NONE)), leftCpy.substitute(init_l, ASTDTree)));
            c_2 = new AndCondition(Arrays.asList(new CallCondition(Conventions.EQUALS, Arrays.asList(
                            prfx + choiceState, Conventions.LEFT)),
                    eventLeft.getKey()));
            Condition rightCpy = (Condition) Utils.copyObject(eventRight.getKey());
            c_3 = new AndCondition(Arrays.asList(new CallCondition(Conventions.EQUALS, Arrays.asList(
                            prfx + choiceState, Conventions.NONE)),
                    rightCpy.substitute(init_r, ASTDTree)));
            c_4 = new AndCondition(Arrays.asList(new CallCondition(Conventions.EQUALS, Arrays.asList(
                            prfx + choiceState, Conventions.RIGHT)),
                    eventRight.getKey()));
            evtBody.setKey(new OrCondition(Arrays.asList(c_1, c_2, c_3, c_4)));
            List<Statement> stmtList = new ArrayList<>();
            if(Constants.COND_OPT_OPTS) {
                if(isRoot()) {
                    stmtList.add(new AssignStatement(new Term(prfx + "cond_1"), c_1));
                    stmtList.add(new AssignStatement(new Term(prfx + "cond_2"), c_2));
                    stmtList.add(new AssignStatement(new Term(prfx + "cond_3"), c_3));
                    stmtList.add(new AssignStatement(new Term(prfx + "cond_4"), c_4));
                }
                c_1 = new CallCondition(-1, Conventions.EQUALS1, Arrays.asList(prfx + "cond_1", c_1));
                c_2 = new CallCondition(-1, Conventions.EQUALS1, Arrays.asList(prfx + "cond_2", c_2));
                c_3 = new CallCondition(-1, Conventions.EQUALS1, Arrays.asList(prfx + "cond_3", c_3));
                c_4 = new CallCondition(-1, Conventions.EQUALS1, Arrays.asList(prfx + "cond_4", c_4));
                evtBody.setKey(new OrCondition(Arrays.asList(c_1, c_2, c_3, c_4)));
                c_1 = new Bool(prfx + "cond_1");
                c_2 = new Bool(prfx + "cond_2");
                c_3 = new Bool(prfx + "cond_3");
                c_4 = new Bool(prfx + "cond_4");
            }

            // left & right translation rules for actions
            Statement leftStmt = new SeqStatement(Arrays.asList(eventLeft.getValue(), astdAction)),
                    leftStmtCpy = (Statement) Utils.copyObject(leftStmt),
                    rightStmt = new SeqStatement(Arrays.asList(eventRight.getValue(), astdAction)),
                    rightStmtCpy = (Statement) Utils.copyObject(rightStmt);
            Statement act_1 = new SeqStatement(Arrays.asList(new AssignStatement(new Term(prfx + choiceState),
                    new Term(Conventions.LEFT)), init_l, leftStmtCpy)),
                    act_2 = new SeqStatement(Arrays.asList(eventLeft.getValue(), astdAction)),
                    act_3 = new SeqStatement(Arrays.asList(new AssignStatement(new Term(prfx + choiceState),
                            new Term(Conventions.RIGHT)), init_r, rightStmtCpy)),
                    act_4 = new SeqStatement(Arrays.asList(eventRight.getValue(), astdAction));

            if(isRoot()) {
                stmtList.add(new IFFIStatement(Arrays.asList(new Entry<>(c_1, act_1), new Entry<>(c_2, act_2),
                        new Entry<>(c_3, act_3), new Entry<>(c_4, act_4))));
                evtBody.setValue(new SeqStatement(stmtList));
            }
            else {
                evtBody.setValue(new IFFIStatement(Arrays.asList(new Entry<>(c_1, act_1), new Entry<>(c_2, act_2),
                        new Entry<>(c_3, act_3), new Entry<>(c_4, act_4))));
            }
        }
        else if(!eventLeft.isEmpty() && eventRight.isEmpty()) {
            // left translation rules for conditions
            Statement init_l = getLeft().init(ASTDTree, lets);
            Condition c_1, c_2;
            Condition leftCpy = (Condition) Utils.copyObject(eventLeft.getKey());
            c_1 = new AndCondition(Arrays.asList(new CallCondition(Conventions.EQUALS, Arrays.asList(
                    prfx + choiceState, Conventions.NONE)), leftCpy.substitute(init_l, ASTDTree)));
            c_2 = new AndCondition(Arrays.asList(new CallCondition(Conventions.EQUALS, Arrays.asList(
                    prfx + choiceState, Conventions.LEFT)), eventLeft.getKey()));
            evtBody.setKey(new OrCondition(Arrays.asList(c_1, c_2)));
            List<Statement> stmtList = new ArrayList<>();
            if(Constants.COND_OPT_OPTS) {
                if(isRoot()) {
                    stmtList.add(new AssignStatement(new Term(prfx + "cond_1"), c_1));
                    stmtList.add(new AssignStatement(new Term(prfx + "cond_2"), c_2));
                }
                c_1 = new CallCondition(-1, Conventions.EQUALS1, Arrays.asList(prfx + "cond_1", c_1));
                c_2 = new CallCondition(-1, Conventions.EQUALS1, Arrays.asList(prfx + "cond_2", c_2));
                evtBody.setKey(new OrCondition(Arrays.asList(c_1, c_2)));
                c_1 = new Bool(prfx + "cond_1");
                c_2 = new Bool(prfx + "cond_2");
            }
            // left translation rules for actions
            Statement leftStmt = new SeqStatement(Arrays.asList(eventLeft.getValue(), astdAction)),
                    leftStmtCpy = (Statement) Utils.copyObject(leftStmt);
            Statement act_1 = new SeqStatement(Arrays.asList(new AssignStatement(new Term(prfx + choiceState),
                    new Term(Conventions.LEFT)), init_l, leftStmtCpy)),
                    act_2 = new SeqStatement(Arrays.asList(eventLeft.getValue(), astdAction));
            if(isRoot()) {
                stmtList.add(new IFFIStatement(Arrays.asList(new Entry<>(c_1, act_1), new Entry<>(c_2, act_2))));
                evtBody.setValue(new SeqStatement(stmtList));
            }
            else {
                evtBody.setValue(new IFFIStatement(Arrays.asList(new Entry<>(c_1, act_1), new Entry<>(c_2, act_2))));
            }
        }
        else if(eventLeft.isEmpty() && !eventRight.isEmpty()) {
            // right translation rules for conditions
            Statement init_r = getRight().init(ASTDTree, lets);
            Condition c_3, c_4;
            Condition rightCpy = (Condition) Utils.copyObject(eventRight.getKey());
            c_3 = new AndCondition(Arrays.asList(new CallCondition(Conventions.EQUALS, Arrays.asList(
                    prfx + choiceState, Conventions.NONE)), rightCpy.substitute(init_r, ASTDTree)));
            c_4 = new AndCondition(Arrays.asList(new CallCondition(Conventions.EQUALS, Arrays.asList(
                    prfx + choiceState, Conventions.RIGHT)), eventRight.getKey()));
            evtBody.setKey(new OrCondition(Arrays.asList(c_3, c_4)));
            List<Statement> stmtList = new ArrayList<>();
            if(Constants.COND_OPT_OPTS) {
                if(isRoot()) {
                    stmtList.add(new AssignStatement(new Term(prfx + "cond_3"), c_3));
                    stmtList.add(new AssignStatement(new Term(prfx + "cond_4"), c_4));
                }
                c_3 = new CallCondition(-1, Conventions.EQUALS1, Arrays.asList(prfx + "cond_3", c_3));
                c_4 = new CallCondition(-1, Conventions.EQUALS1, Arrays.asList(prfx + "cond_4", c_4));
                evtBody.setKey(new OrCondition(Arrays.asList(c_3, c_4)));
                c_3 = new Bool(prfx + "cond_3");
                c_4 = new Bool(prfx + "cond_4");
            }
            // right translation rules for actions
            Statement rightStmt = new SeqStatement(Arrays.asList(eventRight.getValue(), astdAction)),
                    rightStmtCpy = (Statement) Utils.copyObject(rightStmt);
            Statement act_3 = new SeqStatement(Arrays.asList(new AssignStatement(new Term(prfx + choiceState),
                    new Term(Conventions.RIGHT)), init_r, rightStmtCpy)),
                    act_4 = new SeqStatement(Arrays.asList(eventRight.getValue(), astdAction));
            if(isRoot()) {
                stmtList.add(new IFFIStatement(Arrays.asList(new Entry<>(c_3, act_3), new Entry<>(c_4, act_4))));
                evtBody.setValue(new SeqStatement(stmtList));
            }
            else {
                evtBody.setValue(new IFFIStatement(Arrays.asList(new Entry<>(c_3, act_3), new Entry<>(c_4, act_4))));
            }
        }

        return evtBody;
    }

    @Override
    public String getInitialStateValue() {
        return Conventions.NONE;
    }

    @Override
    public Statement propertyMapping() {

        List<Statement> stmtList = new ArrayList<>();
        // generate common ASTD properties
        Statement commonProps = super.propertyMapping();
        if(commonProps != null)
            stmtList.add(commonProps);

        // generate Choice properties
        DeclStatement decStmt = new DeclStatement(new Constant((this.getName() + "_" + ExecSchemaParser.SIDE).toUpperCase(),
                                                               Conventions.STRING, ExecSchemaParser.SIDE, getName()));
        stmtList.add(decStmt);

        Constants.EXEC_SCHEMA_PROPS = Constants.EXEC_SCHEMA_PROPS.replace(ILTranslator.USYMBOL_1,
                                                                          Choice.class.getSimpleName());

        Statement left = getLeft().propertyMapping();
        stmtList.add(left);

        Statement right = getRight().propertyMapping();
        stmtList.add(right);

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
        stmtList.add(fillChoiceProperties(tojson.getNodeIndex(), prfx));

        List<String> subIndexes = updateSubIndexes(tojson);
        if(subIndexes != null) {
            tojson.setSubNodeIndex(subIndexes);
            setToJson(tojson);
        }

        ASTD leftASTD = getLeft();
        ToJson leftToJson = leftASTD.toJson();
        if(tojson.getSubNodeIndex() != null) {
            leftToJson.setNodeIndex(Conventions.ARRAY_ELEM
                    .replace(ILTranslator.USYMBOL_2, tojson.getSubNodeIndex().get(0))
                    .replace(ILTranslator.USYMBOL_1, ExecSchemaParser.CURRENT_SUB_STATE.toUpperCase()));
        }
        List<String> leftIdx = leftASTD.updateSubIndexes(leftToJson);
        if(leftIdx != null) {
            leftToJson.setSubNodeIndex(leftIdx);
            leftASTD.setToJson(leftToJson);
        }
        Statement leftStmt = leftASTD.currentStateToJson();
        if(leftStmt != null)
            stmtList.add(new IFFIStatement(Arrays.asList(
                             new Entry(new CallCondition(Conventions.EQUALS,
                                           Arrays.asList(prfx + Conventions.getStateVar(Choice.class),
                                                         Conventions.LEFT)),
                                       leftStmt))));

        ASTD rightASTD = getRight();
        ToJson rightToJson = rightASTD.toJson();
        if(tojson.getSubNodeIndex() != null) {
            rightToJson.setNodeIndex(Conventions.ARRAY_ELEM
                    .replace(ILTranslator.USYMBOL_2, tojson.getSubNodeIndex().get(1))
                    .replace(ILTranslator.USYMBOL_1, ExecSchemaParser.CURRENT_SUB_STATE.toUpperCase()));
        }
        List<String> rightIdx = rightASTD.updateSubIndexes(rightToJson);
        if(rightIdx != null) {
            rightToJson.setSubNodeIndex(rightIdx);
            rightASTD.setToJson(rightToJson);
        }
        Statement rightStmt = rightASTD.currentStateToJson();
        if(rightStmt != null)
            stmtList.add(new IFFIStatement(Arrays.asList(
                             new Entry(new CallCondition(Conventions.EQUALS,
                                           Arrays.asList(prfx + Conventions.getStateVar(Choice.class),
                                                         Conventions.RIGHT)),
                                      rightStmt))));

        return new SeqStatement(stmtList);
    }


    private Statement fillChoiceProperties(String index, String prfx) {
        List<Statement> stmtList = new ArrayList<>();

        String choiceState = Conventions.getStateVar(Choice.class);

        stmtList.add(new AssignStatement(new Term(Conventions.ARRAY_ELEM.replace(ILTranslator.USYMBOL_2, index)
                .replace(ILTranslator.USYMBOL_1, ExecSchemaParser.TYPE.toUpperCase())),
                new Term("\""+ ExecSchemaParser.CHOICE +"\"")));
        stmtList.add(new AssignStatement(new Term(Conventions.ARRAY_ELEM.replace(ILTranslator.USYMBOL_2, index)
                .replace(ILTranslator.USYMBOL_1,
                        (this.getName() + "_" + ExecSchemaParser.SIDE).toUpperCase())),
                new Term(
                        "(" + prfx + choiceState + " == " +
                                Conventions.RIGHT + ") ? \"Right\" : (" + "(" + prfx + choiceState +
                                " == " + Conventions.LEFT + ") ? \"Left\" : \"null\")"
                        )));

        return new SeqStatement(stmtList);
    }

}
