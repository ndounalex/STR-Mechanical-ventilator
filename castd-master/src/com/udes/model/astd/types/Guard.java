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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

    public class Guard extends UnaryASTD {
        private Condition guard;

        public Guard(String name,
                         List<Variable> attributes,
                         List<Variable> params,
                         Action astdAction,
                         ASTD body,
                         Condition guard) {
            super(name, attributes, params, astdAction, body);
            this.guard = guard;
        }

        public Guard(String name,
                         ASTD body,
                         Condition guard) {
            super(name, body);
            this.guard = guard;
        }

        public Guard() {
            super();
        }

        @Override
        public Statement initforsub(ArrayList<ASTD> callList, Event e, Bool timed, String lets, boolean forFinal) {
            ArrayList<ASTD> ASTDTree = new ArrayList<>();
            ASTDTree.addAll(callList);
            ASTDTree.add(this);
            List<Statement> seqList = new ArrayList<>();
            String  uState = Conventions.getStateVar(com.udes.model.astd.types.Guard.class);

            String prfx = ASTDTree.get(0).prefixTree(ASTDTree) + ".";
            //String prfx = getParent().prefix(getName()) + ".";
            // init state
            seqList.add(new AssignStatement(new Term(prfx + uState), new Term(Conventions.GNOTSTARTED)));
            // init attributes
            List<Variable> vList = getAttributes();
            List<Variable> pList = getParams();
            if(vList != null) {
                vList.forEach( v -> {
                    String init;
                    Boolean b = false;
                    if(pList != null && !pList.isEmpty()) {
                        for (Variable var : pList) {
                            if (var.getName().equals(v.getName())) {
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
        public Statement init(ArrayList<ASTD> callList, String lets) {
            ArrayList<ASTD> ASTDTree = new ArrayList<>();
            ASTDTree.addAll(callList);
            ASTDTree.add(this);
            List<Statement> seqList = new ArrayList<>();
            String  uState = Conventions.getStateVar(com.udes.model.astd.types.Guard.class);

            String prfx = ASTDTree.get(0).prefixTree(ASTDTree) + ".";
            //String prfx = getParent().prefix(getName()) + ".";
            // init state
            seqList.add(new AssignStatement(new Term(prfx + uState), new Term(Conventions.GNOTSTARTED)));
            // init attributes
            List<Variable> vList = getAttributes();
            List<Variable> pList = getParams();
            if(vList != null) {
                vList.forEach( v -> {
                    String init;
                    Boolean b = false;
                    if(pList != null && !pList.isEmpty()) {
                        for (Variable var : pList) {
                            if (var.getName().equals(v.getName())) {
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
                    }
                    else if(v.getType().compareTo(Conventions.STRING) == 0)
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
            ASTD bd = getBody();
            if(bd != null) {
                seqList.add(bd.init(ASTDTree, lets));
            }
            return new SeqStatement(seqList);
        }

        @Override
        public Condition _final(ArrayList<ASTD> callList) {
            ArrayList<ASTD> ASTDTree = new ArrayList<>();
            ASTDTree.addAll(callList);
            ASTDTree.add(this);
            //String  prfx = getParent().prefix(getName()) + ".";
            String prfx = ASTDTree.get(0).prefixTree(ASTDTree) + ".";
            // it's final if not started and the guard is true and the initial state of the body is final
            // or the astd guard already started and the body is final
            String gState = Conventions.getStateVar(com.udes.model.astd.types.Guard.class);
            Bool guard = (Bool) Utils.copyObject(this.getGuard());;
            String g = guard.getStringValue();
            guard.setStringValue(prefixGuard(g, ASTDTree));
            Condition finalCpy = (Condition) Utils.copyObject(getBody()._final(ASTDTree));
            return new OrCondition(Arrays.asList(
                    new AndCondition(Arrays.asList(
                            new CallCondition(Conventions.EQUALS, Arrays.asList(prfx+gState,Conventions.GNOTSTARTED)),
                            new AndCondition(Arrays.asList(finalCpy.substitute(getBody().init(ASTDTree, Conventions.CST), ASTDTree))))),
                    new AndCondition(Arrays.asList(new CallCondition(Conventions.EQUALS,
                            Arrays.asList(prfx + gState, Conventions.GSTARTED)),  getBody()._final(ASTDTree)))));
        }

        @Override
        public Condition _finalForSub(ArrayList<ASTD> callList) {
            ArrayList<ASTD> ASTDTree = new ArrayList<>();
            ASTDTree.addAll(callList);
            ASTDTree.add(this);
            //String  prfx = getParent().prefix(getName()) + ".";
            String prfx = ASTDTree.get(0).prefixTree(ASTDTree) + ".";
            // it's final if not started and the guard is true and the initial state of the body is final
            // or the astd guard already started and the body is final
            String gState = Conventions.getStateVar(com.udes.model.astd.types.Guard.class);
            Bool guard = (Bool) Utils.copyObject(this.getGuard());;
            String g = guard.getStringValue();
            guard.setStringValue(prefixGuard(g, ASTDTree));
            Condition finalCpy = (Condition) Utils.copyObject(getBody()._finalForSub(ASTDTree));
            return new OrCondition(Arrays.asList(
                    new AndCondition(Arrays.asList(
                            new CallCondition(Conventions.EQUALS, Arrays.asList(prfx+gState,Conventions.GNOTSTARTED)),
                            new AndCondition(Arrays.asList(finalCpy.substitute(getBody().init(ASTDTree, Conventions.CST), ASTDTree))))),
                    new AndCondition(Arrays.asList(new CallCondition(Conventions.EQUALS,
                            Arrays.asList(prfx + gState, Conventions.GSTARTED)),  getBody()._finalForSub(ASTDTree)))));
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
            String stateType = Conventions.getStateType(com.udes.model.astd.types.Guard.class);
            props.add(new Variable(Conventions.getStateVar(com.udes.model.astd.types.Guard.class), stateType, null, getName()));
            enumList.add(new Enum(stateType, Arrays.asList(Conventions.GNOTSTARTED, Conventions.GSTARTED)));

            ASTD b = getBody();
            // ignore elem ASTD for optimization
            if(!Conventions.isElem(getBody())) {
                if(b != null) {
                    props.add(new Variable(Conventions.getStructVar(getBody().getName()),
                            Conventions.getStructName(getBody().getName()), null, getName()));
                }
            }
            if(Constants.COND_OPT_OPTS) {
                for (int i = 1; i < 3; i++) {
                    props.add(new Variable("cond_" + i, Conventions.BOOL_TYPE, null, getName()));
                }
            }
            recordList.add(new Record(structName, props));
            // struct body
            Entry<List<Enum>, List<Record>> recordBody = new Entry();
            if(b != null) {
                recordBody = b.trans_type();
                if(recordBody != null && !recordBody.getKey().isEmpty())
                    enumList.addAll(recordBody.getKey());
                if(recordBody != null && !recordBody.getValue().isEmpty())
                    recordList.addAll(recordBody.getValue());
            }

            return new Entry(enumList, recordList);
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
            ASTD b= getBody();
            Statement initBody = null;
            if(b != null) {
                initBody = b.init(ASTDTree, lets);
                // event body
                Entry<Condition, Statement> eBody;
                if(e.getName().equals("Step") && (getBody() instanceof QInterleaving || getBody() instanceof QSynchronization || getBody() instanceof QChoice || getBody() instanceof QFlow)){
                    eBody = getBody().trans_event_step(e,timed, ASTDTree, lets);
                }
                else{
                    eBody = getBody().trans_event(e,timed, ASTDTree, lets);
                }

                if(eBody != null && !eBody.isEmpty()) {
                    String gState = Conventions.getStateVar(com.udes.model.astd.types.Guard.class);
                    // if-fi body block
                    Bool guard = (Bool) Utils.copyObject(this.getGuard());
                    String g = guard.getStringValue();
                    guard.setStringValue(prefixGuard(g, ASTDTree));
                    Condition c1, c2;
                    Condition bdyCpy = (Condition) Utils.copyObject(eBody.getKey());
                    c1 = new AndCondition(Arrays.asList(
                            new CallCondition(Conventions.EQUALS, Arrays.asList(prfx + gState, Conventions.GNOTSTARTED)),
                            new AndCondition(Arrays.asList(guard, bdyCpy.substitute(initBody, ASTDTree)))));
                    c2 = new AndCondition(Arrays.asList(
                            new CallCondition(Conventions.EQUALS, Arrays.asList(prfx + gState, Conventions.GSTARTED)),
                            eBody.getKey()));
                    evtBody.setKey(new OrCondition(Arrays.asList(c1, c2)));
                    List<Statement> stmtList = new ArrayList<>();
                    if(Constants.COND_OPT_OPTS) {
                        if(isRoot()) {
                            stmtList.add(new AssignStatement(new Term(prfx + "cond_1"), c1));
                            stmtList.add(new AssignStatement(new Term(prfx + "cond_2"), c2));
                        }
                        c1 = new CallCondition(-1, Conventions.EQUALS1, Arrays.asList(prfx + "cond_1", c1));
                        //prfx_cond_1 == c1
                        c2 = new CallCondition(-1, Conventions.EQUALS1, Arrays.asList(prfx + "cond_2", c2));
                        evtBody.setKey(new OrCondition(Arrays.asList(c1, c2)));
                        c1 = new Bool(prfx + "cond_1");
                        c2 = new Bool(prfx + "cond_2");
                    }

                    if (eBody.getValue() != null) {
                        Statement bdyStmt = new SeqStatement(Arrays.asList(eBody.getValue(), astdAction)),
                                bdyStmtCpy = (Statement) Utils.copyObject(bdyStmt);
                        Statement stmt1 = new SeqStatement(Arrays.asList(
                                new AssignStatement(new Term(prfx + gState),
                                        new Term(Conventions.GSTARTED)),
                                bdyStmtCpy));
                        Statement stmt2 = new SeqStatement(Arrays.asList(eBody.getValue(), astdAction));
                        if(isRoot()) {
                            stmtList.add(new IFFIStatement(Arrays.asList(new Entry<>(c1, stmt1), new Entry<>(c2, stmt2))));
                            evtBody.setValue(new SeqStatement(stmtList));
                        }
                        else {
                            evtBody.setValue(new IFFIStatement(Arrays.asList(new Entry<>(c1, stmt1),
                                    new Entry<>(c2, stmt2))));
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
            String prfx = ASTDTree.get(0).prefixTree(ASTDTree) + ".";
            //String prfx = getParent().prefix(getName()) + ".";
            Action astdAction = prefixAction(getAstdAction(), ASTDTree);
            if(astdAction != null && Constants.DEBUG) {
                String debugMsg = ";\n" + Constants.LOGGER_MSG.replace(ILTranslator.USYMBOL_1, astdAction.getCode());
                astdAction = new Action(astdAction.getCode() + debugMsg);
            }
            ASTD b= getBody();
            Statement initBody = null;
            if(b != null) {
                initBody = b.init(ASTDTree, lets);
                // event body
                Entry<Condition, Statement> eBody = getBody().trans_event_step(e, timed, ASTDTree, lets);

                if(eBody != null && !eBody.isEmpty()) {
                    String gState = Conventions.getStateVar(com.udes.model.astd.types.Guard.class);
                    // if-fi body block
                    Bool guard = (Bool) Utils.copyObject(this.getGuard());
                    String g = guard.getStringValue();
                    guard.setStringValue(prefixGuard(g, ASTDTree));
                    Condition c1, c2;
                    Condition bdyCpy = (Condition) Utils.copyObject(eBody.getKey());
                    c1 = new AndCondition(Arrays.asList(
                            new CallCondition(Conventions.EQUALS, Arrays.asList(prfx + gState, Conventions.GNOTSTARTED)),
                            new AndCondition(Arrays.asList(guard, bdyCpy.substitute(initBody, ASTDTree)))));
                    c2 = new AndCondition(Arrays.asList(
                            new CallCondition(Conventions.EQUALS, Arrays.asList(prfx + gState, Conventions.GSTARTED)),
                            eBody.getKey()));
                    evtBody.setKey(new OrCondition(Arrays.asList(c1, c2)));
                    List<Statement> stmtList = new ArrayList<>();
                    if(Constants.COND_OPT_OPTS) {
                        if(isRoot()) {
                            stmtList.add(new AssignStatement(new Term(prfx + "cond_1"), c1));
                            stmtList.add(new AssignStatement(new Term(prfx + "cond_2"), c2));
                        }
                        c1 = new CallCondition(-1, Conventions.EQUALS1, Arrays.asList(prfx + "cond_1", c1));
                        c2 = new CallCondition(-1, Conventions.EQUALS1, Arrays.asList(prfx + "cond_2", c2));
                        evtBody.setKey(new OrCondition(Arrays.asList(c1, c2)));
                        c1 = new Bool(prfx + "cond_1");
                        c2 = new Bool(prfx + "cond_2");
                    }

                    if (eBody.getValue() != null) {
                        Statement bdyStmt = new SeqStatement(Arrays.asList(eBody.getValue(), astdAction)),
                                bdyStmtCpy = (Statement) Utils.copyObject(bdyStmt);
                        Statement stmt1 = new SeqStatement(Arrays.asList(
                                new AssignStatement(new Term(prfx + gState),
                                        new Term(Conventions.GSTARTED)),
                                bdyStmtCpy));
                        Statement stmt2 = new SeqStatement(Arrays.asList(eBody.getValue(), astdAction));
                        if(isRoot()) {
                            stmtList.add(new IFFIStatement(Arrays.asList(new Entry<>(c1, stmt1), new Entry<>(c2, stmt2))));
                            evtBody.setValue(new SeqStatement(stmtList));
                        }
                        else {
                            evtBody.setValue(new IFFIStatement(Arrays.asList(new Entry<>(c1, stmt1),
                                    new Entry<>(c2, stmt2))));
                        }
                    }
                }
            }

            return evtBody;
        }

        public Condition getGuard() {
            return guard;
        }

        public void setGuard(Condition guard) {
            this.guard = guard;
        }

        @Override
        public String getInitialStateValue() {
            return Conventions.GNOTSTARTED;
        }

        @Override
        public Statement propertyMapping() {

            List<Statement> stmtList = new ArrayList<>();
            // generate common ASTD properties
            Statement commonProps = super.propertyMapping();
            if(commonProps != null)
                stmtList.add(commonProps);

            // generate Guard properties
            DeclStatement decStmt = new DeclStatement(new Constant((this.getName() + "_" + ExecSchemaParser.STARTED)
                    .toUpperCase(),
                    Conventions.STRING, ExecSchemaParser.STARTED, getName()));
            stmtList.add(decStmt);
            Constants.EXEC_SCHEMA_PROPS = Constants.EXEC_SCHEMA_PROPS.replace(ILTranslator.USYMBOL_1,
                    com.udes.model.astd.types.Guard.class.getSimpleName());


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
            //TODO CHANGE TO PREFIXTREE
            String prfx = getParent().prefix(getName()) + ".";
            stmtList.add(fillGuardProperties(tojson.getNodeIndex(), prfx));

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


        private Statement fillGuardProperties(String index, String prfx) {
            List<Statement> stmtList = new ArrayList<>();

            String guardState = Conventions.getStateVar(com.udes.model.astd.types.Guard.class);

            stmtList.add(new AssignStatement(new Term(Conventions.ARRAY_ELEM.replace(ILTranslator.USYMBOL_2, index)
                    .replace(ILTranslator.USYMBOL_1, ExecSchemaParser.TYPE.toUpperCase())),
                    new Term("\""+ ExecSchemaParser.GUARD +"\"")));
            stmtList.add(new AssignStatement(new Term(Conventions.ARRAY_ELEM.replace(ILTranslator.USYMBOL_2, index)
                    .replace(ILTranslator.USYMBOL_1,
                            (this.getName() + "_" + ExecSchemaParser.STARTED).toUpperCase())),
                    new Term( "(std::string(" + Conventions.ARRAY_ELEM
                            .replace(ILTranslator.USYMBOL_2,
                                    Conventions.getStateVar(com.udes.model.astd.types.Guard.class))
                            .replace(ILTranslator.USYMBOL_1, prfx + guardState) + ").compare(\""
                            + Conventions.GSTARTED + "\") == 0) ? true : false")));


            return new SeqStatement(stmtList);
        }
    }
