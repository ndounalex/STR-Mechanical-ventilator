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

public class Interleaving extends BinaryASTD {

    public Interleaving(String name,
                        List<Variable> attributes,
                        List<Variable> params,
                        Action astdAction,
                        ASTD left,
                        ASTD right) {
        super(name, attributes, params, astdAction, left, right);
    }

    public Interleaving(String name, ASTD left, ASTD right) {
        super(name, left, right);
    }

    public Interleaving() {
        super();
    }

    @Override
    public Statement init(ArrayList<ASTD> callList, String lets) {
        ArrayList<ASTD> ASTDTree = new ArrayList<>();
        ASTDTree.addAll(callList);
        ASTDTree.add(this);
        List<Statement> seqList = new ArrayList<>();
        //String prfx = getParent().prefix(getName()) + ".";
        String prfx = ASTDTree.get(0).prefixTree(ASTDTree) + ".";
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
        // init left
        seqList.add(getLeft().init(ASTDTree, lets));
        // init right
        seqList.add(getRight().init(ASTDTree, lets));

        return new SeqStatement(seqList);
    }

    @Override
    public Statement initforsub(ArrayList<ASTD> callList, Event e, Bool timed, String lets, boolean forFinal) {
        ArrayList<ASTD> ASTDTree = new ArrayList<>();
        ASTDTree.addAll(callList);
        ASTDTree.add(this);
        List<Statement> seqList = new ArrayList<>();
        //String prfx = getParent().prefix(getName()) + ".";
        String prfx = ASTDTree.get(0).prefixTree(ASTDTree) + ".";
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
        // init left
        seqList.add(getLeft().initforsub(ASTDTree, e, timed, lets, forFinal));
        // init right
        seqList.add(getRight().initforsub(ASTDTree, e, timed, lets, forFinal));

        return new SeqStatement(seqList);
    }

    @Override
    public Condition _final(ArrayList<ASTD> callList) {
        ArrayList<ASTD> ASTDTree = new ArrayList<>();
        ASTDTree.addAll(callList);
        ASTDTree.add(this);
        // Left and right astd should be final
        return new AndCondition(Arrays.asList(getLeft()._final(ASTDTree), getRight()._final(ASTDTree)));
    }

    @Override
    public Condition _finalForSub(ArrayList<ASTD> callList) {
        ArrayList<ASTD> ASTDTree = new ArrayList<>();
        ASTDTree.addAll(callList);
        ASTDTree.add(this);
        // Left and right astd should be final
        return new AndCondition(Arrays.asList(getLeft()._finalForSub(ASTDTree), getRight()._finalForSub(ASTDTree)));
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
            for (int i = 1; i < 3; i++) {
                props.add(new Variable("cond_" + i, Conventions.BOOL_TYPE, null, getName()));
            }
        }
        recordList.add(new Record(structName, props));
        // left struct
        Entry<List<Enum>, List<Record>> recordLeft = getLeft().trans_type();
        if (!recordLeft.getKey().isEmpty())
            enumList.addAll(recordLeft.getKey());
        if (!recordLeft.getValue().isEmpty())
            recordList.addAll(recordLeft.getValue());
        // right struct
        Entry<List<Enum>, List<Record>> recordRight = getRight().trans_type();
        if (!recordRight.getKey().isEmpty())
            enumList.addAll(recordRight.getKey());
        if (!recordRight.getValue().isEmpty())
            recordList.addAll(recordRight.getValue());

        return new Entry(enumList, recordList);
    }

    @Override
    public Entry<Condition, Statement> trans_event(Event e, Bool timed, ArrayList<ASTD> callList, String lets) {
        ArrayList<ASTD> ASTDTree = new ArrayList<>();
        ASTDTree.addAll(callList);
        ASTDTree.add(this);
        Entry<Condition, Statement> evtBody = new Entry();
        Action astdAction = prefixAction(getAstdAction(), ASTDTree);
        if(astdAction != null && Constants.DEBUG) {
            String debugMsg = ";\n" + Constants.LOGGER_MSG.replace(ILTranslator.USYMBOL_1, astdAction.getCode());
            astdAction = new Action(astdAction.getCode() + debugMsg);
        }
        //String prfx = getParent().prefix(getName()) + ".";
        String prfx = ASTDTree.get(0).prefixTree(ASTDTree) + ".";
        Entry<Condition, Statement> eventLeft, eventRight;
        if(e.getName().equals("Step") && (getLeft() instanceof QInterleaving ||
                getLeft() instanceof QSynchronization ||
                getRight() instanceof QInterleaving ||
                getRight() instanceof  QSynchronization || 
                getLeft() instanceof QChoice || 
                getRight() instanceof QChoice ||
                getLeft() instanceof QFlow ||
                getRight() instanceof QFlow)){
            // event body left
            eventLeft = getLeft().trans_event_step(e, timed, ASTDTree, prfx+Conventions.LEFT_CLOCK+"_"+getLeft().getName());
            // event body right
            eventRight = getRight().trans_event_step(e, timed, ASTDTree, prfx+Conventions.RIGHT_CLOCK+"_"+getRight().getName());
        }
        else{
            // event body left
            eventLeft = getLeft().trans_event(e, timed, ASTDTree, prfx+Conventions.LEFT_CLOCK+"_"+getLeft().getName());
            // event body right
            eventRight = getRight().trans_event(e, timed, ASTDTree, prfx+Conventions.RIGHT_CLOCK+"_"+getRight().getName());
        }

        // changing clock variables
        if(timed.getValue()){
            if(!eventLeft.isEmpty()){
                eventLeft.getKey().substitute(new AssignStatement(new Term(Conventions.LETS),new Term(prfx+Conventions.LEFT_CLOCK+"_"+getLeft().getName())), ASTDTree);
            }
            if(!eventRight.isEmpty()) {
                eventRight.getKey().substitute(new AssignStatement(new Term(Conventions.LETS),new Term(prfx+Conventions.RIGHT_CLOCK+"_"+getRight().getName())), ASTDTree);
            }
        }

        if(!eventLeft.isEmpty() && eventRight.isEmpty()){
            // if-fi left block
            Condition c_l;
            c_l = eventLeft.getKey();

            evtBody.setKey(c_l);
            List<Statement> stmtList = new ArrayList<>();
            if(Constants.COND_OPT_OPTS) {
                if(isRoot()) {
                    stmtList.add(new AssignStatement(new Term(prfx + "cond_1"), c_l));
                }
                c_l = new CallCondition(-1, Conventions.EQUALS1, Arrays.asList(prfx + "cond_1",
                            eventLeft.getKey()));
                evtBody.setKey(c_l);
                c_l = new Bool(prfx + "cond_1");
            }

            Entry<Condition, Statement> iffiLeft = new Entry(c_l, new SeqStatement(Arrays.asList(
                    eventLeft.getValue(), astdAction)));
            if(isRoot()) {
                stmtList.add(new IFFIStatement(Collections.singletonList(iffiLeft)));
                evtBody.setValue(new SeqStatement(stmtList));
            }
            else {
                evtBody.setValue(new IFFIStatement(Collections.singletonList(iffiLeft)));
            }
        }
        else if(eventLeft.isEmpty() && !eventRight.isEmpty()) {
            // if-fi right block
            Condition c_r;
            c_r = eventRight.getKey();

            evtBody.setKey(c_r);
            List<Statement> stmtList = new ArrayList<>();
            if(Constants.COND_OPT_OPTS) {
                if(isRoot()) {
                    stmtList.add(new AssignStatement(new Term(prfx + "cond_2"), c_r));
                }
                c_r = new CallCondition(-1, Conventions.EQUALS1, Arrays.asList(prfx + "cond_2",
                            eventRight.getKey()));

                evtBody.setKey(c_r);
                c_r = new Bool(prfx + "cond_2");
            }
            Entry<Condition, Statement> iffiRight = new Entry(c_r, new SeqStatement(Arrays.asList(
                    eventRight.getValue(), astdAction)));
            if(isRoot()) {
                stmtList.add(new IFFIStatement(Collections.singletonList(iffiRight)));
                evtBody.setValue(new SeqStatement(stmtList));
            }
            else {
                evtBody.setValue(new IFFIStatement(Collections.singletonList(iffiRight)));
            }
        }
        else if(!eventLeft.isEmpty() && !eventRight.isEmpty()) {
            // interleaving
            Condition c_l, c_r;
            c_r = eventRight.getKey();
            c_l = eventLeft.getKey();

            evtBody.setKey(new OrCondition(Arrays.asList(c_l, c_r)));
            List<Statement> stmtList = new ArrayList<>();
            if(Constants.COND_OPT_OPTS) {
                if(isRoot()) {
                    stmtList.add(new AssignStatement(new Term(prfx + "cond_1"), c_l));
                    stmtList.add(new AssignStatement(new Term(prfx + "cond_2"), c_r));
                }
                c_l = new CallCondition(-1, Conventions.EQUALS1, Arrays.asList(prfx + "cond_1",
                            eventLeft.getKey()));
                    c_r = new CallCondition(-1, Conventions.EQUALS1, Arrays.asList(prfx + "cond_2",
                            eventRight.getKey()));

                evtBody.setKey(new OrCondition(Arrays.asList(c_l, c_r)));
                c_l = new Bool(prfx + "cond_1");
                c_r = new Bool(prfx + "cond_2");
            }
            Entry<Condition, Statement> iffiLeft = new Entry(c_l, new SeqStatement(Arrays.asList(
                    eventLeft.getValue(), astdAction)));
            Entry<Condition, Statement> iffiRight = new Entry(c_r, new SeqStatement(Arrays.asList(
                    eventRight.getValue(), astdAction)));
            if(isRoot()) {
                stmtList.add(new IFFIStatement(Arrays.asList(iffiLeft, iffiRight)));
                evtBody.setValue(new SeqStatement(stmtList));
            }
            else {
                evtBody.setValue(new IFFIStatement(Arrays.asList(iffiLeft, iffiRight)));
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
        Action astdAction = prefixAction(getAstdAction(), ASTDTree);
        if(astdAction != null && Constants.DEBUG) {
            String debugMsg = ";\n" + Constants.LOGGER_MSG.replace(ILTranslator.USYMBOL_1, astdAction.getCode());
            astdAction = new Action(astdAction.getCode() + debugMsg);
        }
        //String prfx = getParent().prefix(getName()) + ".";
        String prfx = ASTDTree.get(0).prefixTree(ASTDTree) + ".";
        // event body left
        Entry<Condition, Statement> eventLeft = getLeft().trans_event_step(e, timed, ASTDTree, prfx+Conventions.LEFT_CLOCK+"_"+getLeft().getName());
        // event body right
        Entry<Condition, Statement> eventRight = getRight().trans_event_step(e, timed, ASTDTree, prfx+Conventions.RIGHT_CLOCK+"_"+getRight().getName());

        // changing clock variables
        if(timed.getValue()){
            if(!eventLeft.isEmpty()){
                eventLeft.getKey().substitute(new AssignStatement(new Term(Conventions.LETS),new Term(prfx+Conventions.LEFT_CLOCK+"_"+getLeft().getName())), ASTDTree);
            }
            if(!eventRight.isEmpty()) {
                eventRight.getKey().substitute(new AssignStatement(new Term(Conventions.LETS),new Term(prfx+Conventions.RIGHT_CLOCK+"_"+getRight().getName())), ASTDTree);
            }
        }

        if(!eventLeft.isEmpty() && eventRight.isEmpty()){
            // if-fi left block
            Condition c_l;
            c_l = eventLeft.getKey();
            evtBody.setKey(c_l);
            List<Statement> stmtList = new ArrayList<>();
            if(Constants.COND_OPT_OPTS) {
                if(isRoot()) {
                    stmtList.add(new AssignStatement(new Term(prfx + "cond_1"), c_l));
                }
                c_l = new CallCondition(-1, Conventions.EQUALS1, Arrays.asList(prfx + "cond_1",
                            eventLeft.getKey()));
                evtBody.setKey(c_l);
                c_l = new Bool(prfx + "cond_1");
            }

            Entry<Condition, Statement> iffiLeft = new Entry(c_l, new SeqStatement(Arrays.asList(
                    eventLeft.getValue(), astdAction)));
            if(isRoot()) {
                stmtList.add(new IFFIStatement(Collections.singletonList(iffiLeft)));
                evtBody.setValue(new SeqStatement(stmtList));
            }
            else {
                evtBody.setValue(new IFFIStatement(Collections.singletonList(iffiLeft)));
            }
        }
        else if(eventLeft.isEmpty() && !eventRight.isEmpty()) {
            // if-fi right block
            Condition c_r;
            c_r = eventRight.getKey();
            evtBody.setKey(c_r);
            List<Statement> stmtList = new ArrayList<>();
            if(Constants.COND_OPT_OPTS) {
                if(isRoot()) {
                    stmtList.add(new AssignStatement(new Term(prfx + "cond_2"), c_r));
                }
                c_r = new CallCondition(-1, Conventions.EQUALS1, Arrays.asList(prfx + "cond_2",
                            eventRight.getKey()));

                evtBody.setKey(c_r);
                c_r = new Bool(prfx + "cond_2");
            }
            Entry<Condition, Statement> iffiRight = new Entry(c_r, new SeqStatement(Arrays.asList(
                    eventRight.getValue(), astdAction)));
            if(isRoot()) {
                stmtList.add(new IFFIStatement(Collections.singletonList(iffiRight)));
                evtBody.setValue(new SeqStatement(stmtList));
            }
            else {
                evtBody.setValue(new IFFIStatement(Collections.singletonList(iffiRight)));
            }
        }
        else if(!eventLeft.isEmpty() && !eventRight.isEmpty()) {
            // interleaving
            Condition c_l, c_r;

            c_l = eventLeft.getKey();
            c_r = eventRight.getKey();

            evtBody.setKey(new OrCondition(Arrays.asList(c_l, c_r)));
            List<Statement> stmtList = new ArrayList<>();
            if(Constants.COND_OPT_OPTS) {
                if(isRoot()) {
                    stmtList.add(new AssignStatement(new Term(prfx + "cond_1"), c_l));
                    stmtList.add(new AssignStatement(new Term(prfx + "cond_2"), c_r));
                }

                c_l = new CallCondition(-1, Conventions.EQUALS1, Arrays.asList(prfx + "cond_1", eventLeft.getKey()));
                c_r = new CallCondition(-1, Conventions.EQUALS1, Arrays.asList(prfx + "cond_2", eventRight.getKey()));
                evtBody.setKey(new OrCondition(Arrays.asList(c_l, c_r)));
                c_l = new Bool(prfx + "cond_1");
                c_r = new Bool(prfx + "cond_2");

            }

            //global condition of the event function
            evtBody.setKey(new OrCondition(Arrays.asList(c_l, c_r)));
            // statement of the event function
            Entry<Condition, Statement> iffiLeft;
            Entry<Condition, Statement> iffiRight;
            iffiLeft = new Entry<>(c_l, new SeqStatement(Arrays.asList(eventLeft.getValue())));
            iffiRight = new Entry<>(c_r, new SeqStatement(Arrays.asList(eventRight.getValue())));
            if(astdAction != null) {
                // if(eq b false) -> A.A_astd; b = true
                SeqStatement actionSeq = new SeqStatement(Arrays.asList(astdAction));
                if(isRoot()) {
                    stmtList.add(new SeqStatement(Arrays.asList(
                            new IFFIStatement(Collections.singletonList(iffiLeft)),
                            new IFFIStatement(Collections.singletonList(iffiRight)),
                            actionSeq)));
                    evtBody.setValue(new SeqStatement(stmtList));
                }
                else {
                    evtBody.setValue(new SeqStatement(Arrays.asList(
                            new IFFIStatement(Collections.singletonList(iffiLeft)),
                            new IFFIStatement(Collections.singletonList(iffiRight)),
                            actionSeq)));
                }
            }
            else {
                if(isRoot()) {
                    stmtList.add(new SeqStatement(Arrays.asList(
                            new IFFIStatement(Collections.singletonList(iffiLeft)),
                            new IFFIStatement(Collections.singletonList(iffiRight)))));
                    evtBody.setValue(new SeqStatement(stmtList));
                }
                else {
                    evtBody.setValue(new SeqStatement(Arrays.asList(
                            new IFFIStatement(Collections.singletonList(iffiLeft)),
                            new IFFIStatement(Collections.singletonList(iffiRight)))));
                }
            }
        }

        return evtBody;
    }

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

        // generate Interleaving properties
        List<String> interProps = Arrays.asList(ExecSchemaParser.LEFT,ExecSchemaParser.RIGHT);

        interProps.forEach(prop -> {
            DeclStatement decStmt = new DeclStatement(new Constant((this.getName() + "_" + prop).toUpperCase(),
                                                     Conventions.STRING, prop, getName()));
            stmtList.add(decStmt);
        });
        Constants.EXEC_SCHEMA_PROPS = Constants.EXEC_SCHEMA_PROPS.replace(ILTranslator.USYMBOL_1, Interleaving.class.getSimpleName());

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
        //TODO CHANGE FOR PREFIXTREE
        String prfx = getParent().prefix(getName()) + ".";
        stmtList.add(fillInterProperties(tojson.getNodeIndex(), prfx));

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
                    .replace(ILTranslator.USYMBOL_1, (this.getName() + "_" + ExecSchemaParser.LEFT).toUpperCase()));
        }
        List<String> leftIdx = leftASTD.updateSubIndexes(leftToJson);
        if(leftIdx != null) {
            leftToJson.setSubNodeIndex(leftIdx);
            leftASTD.setToJson(leftToJson);
        }
        Statement leftStmt = leftASTD.currentStateToJson();
        if(leftStmt != null)
            stmtList.add(leftStmt);

        ASTD rightASTD = getRight();
        ToJson rightToJson = rightASTD.toJson();
        if(tojson.getSubNodeIndex() != null) {
            rightToJson.setNodeIndex(Conventions.ARRAY_ELEM
                    .replace(ILTranslator.USYMBOL_2, tojson.getSubNodeIndex().get(1))
                    .replace(ILTranslator.USYMBOL_1, (this.getName() + "_" + ExecSchemaParser.RIGHT).toUpperCase()));
        }
        List<String> rightIdx = rightASTD.updateSubIndexes(rightToJson);
        if(rightIdx != null) {
            rightToJson.setSubNodeIndex(rightIdx);
            rightASTD.setToJson(rightToJson);
        }
        Statement rightStmt = rightASTD.currentStateToJson();
        if(rightStmt != null)
            stmtList.add(rightStmt);

        return new SeqStatement(stmtList);
    }


    private Statement fillInterProperties(String index, String prfx) {
        return new AssignStatement(new Term(Conventions.ARRAY_ELEM.replace(ILTranslator.USYMBOL_2, index)
                .replace(ILTranslator.USYMBOL_1, ExecSchemaParser.TYPE.toUpperCase())),
                new Term("\""+ ExecSchemaParser.SYNCHRONIZATION +"\""));
    }
}
