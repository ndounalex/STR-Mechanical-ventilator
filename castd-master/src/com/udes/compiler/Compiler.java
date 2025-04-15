package com.udes.compiler;

import com.udes.args.CmdManager;
import com.udes.model.astd.items.*;
import com.udes.model.il.conditions.CallCondition;
import com.udes.model.il.containers.Entry;
import com.udes.model.il.conditions.Condition;
import com.udes.model.il.record.Enum;
import com.udes.model.il.statements.*;
import com.udes.model.astd.base.ASTD;
import com.udes.model.astd.types.*;
import com.udes.model.il.ILModel;
import com.udes.model.il.conventions.Conventions;
import com.udes.model.il.methods.Function;
import com.udes.model.il.record.Record;
import com.udes.model.il.terms.Bool;
import com.udes.model.il.terms.Term;
import com.udes.optimizer.KappaOptimizer;
import com.udes.packaging.PackageBuilder;
import com.udes.parser.ASTDParser;
import com.udes.track.ExecSchema;
import com.udes.translator.ILTranslator;
import com.udes.utils.Constants;
import com.udes.utils.Utils;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Compiler {

    private ASTD rootASTD;

    public Compiler(ASTD rootASTD) {
        if(rootASTD != null) {
            this.rootASTD = rootASTD;
        }
        else {
            throw new NullPointerException("The ASTD object is null");
        }
    }

    /*
     * @brief compile the ASTD object into the IL model
     * @return the IL model
     */
    public ILModel compile(Bool timed, String step_value, String initial_time) {

        ILModel imModel = new ILModel();
        setName(imModel);
        setExternalReferences(imModel);
        setTypeDefinitions(imModel);
        setTypeDeclarations(imModel);
        setVariableDeclarations(imModel, timed, step_value, initial_time);
        setFunctions(imModel, timed);
        setExecSchema(imModel);

        return imModel;
    }

    /*
     * @brief  Updates the IL model with external references
     * @param  The IL model
     * @return
     */
    private void setExternalReferences(ILModel imModel) {
        rootASTD.setParent(rootASTD);
        // add external references (ontologies, imports)
        Set<String> ontoList = rootASTD.getOntoClasses();
        List<String> refList = new ArrayList<>();
        if (ontoList != null)
            refList.addAll(ontoList);
        Set<String> trRefs = rootASTD.trans_refs();
        if (!trRefs.isEmpty())
            refList.addAll(trRefs);
        if (!refList.isEmpty())
            imModel.setExtrefs(refList);
    }

    /*
     * @brief  Updates the IL model with external references
     * @param  The IL model
     * @return
     */
    private void setTypeDefinitions(ILModel imModel) {
        // add type definitions
        Set<String> typeList = rootASTD.getTypeDefs();
        List<String> refList = new ArrayList<>();
        if (!typeList.isEmpty())
            refList.addAll(typeList);
        if (!refList.isEmpty())
            imModel.setTypeDefs(refList);
    }

    /*
     * @brief  Updates the IL model name
     * @param  The IL model
     * @return
     */
    private void setName(ILModel imModel) {
        // generate the name of the intermediate model
        imModel.setName(rootASTD.getName());
        Utils.qvarCond = new HashMap<>();
        Utils.qASTDList = new ArrayList<>();
        initPrefixes();
    }

    /*
     * @brief  Updates the IL model with type declarations
     * @param  The IL model
     * @return
     */
    private void setTypeDeclarations(ILModel imModel) {
        // generate type declarations of the intermediate model
        Entry<List<Enum>, List<Record>> typedecls = rootASTD.trans_type();
        if(!typedecls.isEmpty()) {
            // reverse in the right order and merge types
            List<Record> recList = new ArrayList<>(typedecls.getKey());
            List<String> states = rootASTD.findAllStates();
            if(states != null)
                recList.add(new Enum(Conventions.getStateType(Automaton.class), states));
            Collections.reverse(recList);
            recList = new ArrayList<>(new HashSet<>(recList));
            List<Record> tmpList = new ArrayList<>();
            //the next for is to exclude duplicated state declarations (happens with a call ASTD being called twice)
            //NOTE: To the point that this for was added, it was not possible to declare two ASTD's with the same name.
            for (Record decl : typedecls.getValue()) {
                if (!tmpList.contains(decl)) {
                    tmpList.add(decl);
                }
                else{
                    tmpList.remove(decl);
                    tmpList.add(decl);
                }
            }
            //tmpList = new ArrayList<>(typedecls.getValue());
            Collections.reverse(tmpList);
            recList.addAll(tmpList);
            imModel.setTypedecls(recList);
        }
    }

    /*
     * @brief  Updates the IL model with variable declarations
     * @param  The IL model
     * @return
     */
    private void setVariableDeclarations(ILModel imModel, Bool timed, String step_value, String initial_time) {
        // generate variable declarations of the intermediate model
        List<Variable> tpmvardecls = rootASTD.trans_var();
        // shallow and depp final states are two different variables in vardecls, but they shall only create one
        // initiation (the same value for both)
        // it is necessary to hide one of them while doing the declaration
        List<Variable> vardecls_hide = new ArrayList<>();

        //the next for is to exclude duplicated final declarations (happens with a call ASTD being called twice)
        //NOTE: To the point that this for was added, it was not possible to declare two ASTD's with the same name.
        List<Variable> vardecls = new ArrayList<>();
        for(Variable var : tpmvardecls){
            if(!vardecls.contains(var)){
                vardecls.add(var);
            }
        }

        if(vardecls != null){
            for(int i = 0; i < vardecls.size(); i++) {
                boolean duplicate = false;
                for (int j = 0; j < vardecls.size(); j++) {
                    if (vardecls.get(i).getName().replace("shallow_final_", "").equals(
                            vardecls.get(j).getName().replace("deep_final_", ""))) {
                        duplicate = true;
                        break;
                    }
                }
                if(!duplicate){
                    vardecls_hide.add(vardecls.get(i));
                }
            }
        }
        if(vardecls != null) {
            vardecls.add(new Variable(Conventions.getStructVar(rootASTD.getName()),
                                      Conventions.getStructName(rootASTD.getName()),
                                      null, rootASTD.getName()));

            //declarations for TASTD
            if(Constants.TIMED_SIMULATION){
                //timed simulation
                if(step_value == null){
                    step_value = "10000000";
                }
                else{
                    double step_value_in_nano = Double.parseDouble(step_value) * 1000000000;
                    step_value = String.valueOf(step_value_in_nano);
                }

                if(initial_time != null){
                    double inital_time_in_nano = Double.parseDouble(initial_time) * 1000000000;
                    Constants.INITIAL_TIME = String.valueOf(inital_time_in_nano);
                }

                // add step time, last_event_time, last_step_time, exec_start
                Variable step_time = new Variable(Conventions.STEP_VAR, Conventions.TIME_TYPE2, step_value, rootASTD.getName());
                Variable last_event_time = new Variable(Conventions.LETS, Conventions.TIME_TYPE3, Constants.INITIAL_TIME, rootASTD.getName());
                vardecls.add(step_time);
                vardecls.add(last_event_time);
            }
            else if(timed.getValue()){
                // add the queue
                //Variable queue = new Variable(Conventions.QUEUE, Conventions.QUEUE_T, null, null);

                // add step time, last_event_time, last_step_time, exec_start, and mutex
                if(step_value==null){
                    step_value = "10000000";
                }
                else{
                    double step_value_in_nano = Double.parseDouble(step_value) * 1000000000;
                    step_value = String.valueOf(step_value_in_nano);
                }

                Variable mutex = new Variable(Conventions.MTX, Conventions.MUTEX, "0", rootASTD.getName());
                Variable step_time = new Variable(Conventions.STEP_VAR, Conventions.TIME_TYPE2, step_value, rootASTD.getName());
                Variable last_event_time = new Variable(Conventions.LETS, Conventions.TIME_TYPE, null, rootASTD.getName());
                Variable last_step_time = new Variable(Conventions.LST, Conventions.TIME_TYPE, null, rootASTD.getName());
                Variable start_time = new Variable(Conventions.TIME_START, Conventions.TIME_TYPE, null, rootASTD.getName());

                // add continue_signal
                Variable continue_signal = new Variable(Conventions.SIGNAL, Conventions.BOOL_TYPE1, Conventions.TRUE, rootASTD.getName());

                //vardecls.add(queue);
                vardecls.add(mutex);
                vardecls.add(step_time);
                vardecls.add(last_event_time);
                vardecls.add(last_step_time);
                vardecls.add(start_time);
                vardecls.add(continue_signal);
            }

            imModel.setVardecls(vardecls);
        }
    }

    /*
     * @brief  Updates the IL model with functions
     * @param  The IL model
     * @return
     */
    private void setFunctions(ILModel imModel, Bool timed) {
        // generate the list of event functions
        Set<Event> evt_collections = rootASTD.findAllEvents();
        ArrayList<Event> evts = Utils.mergeEvents(evt_collections);

        Utils.qASTDList = rootASTD.findAllQASTDs();

        List<Function> funcList = new ArrayList<>();

        AtomicBoolean hasStep = new AtomicBoolean(false);

        for(int i = 0; i < evts.size(); i++){
            //change the position of the events, to step be the calculated event
            if(evts.get(i).getName().compareTo("Step") == 0 && i != 0){
                Event aux1 = evts.get(0);
                Event aux2 = evts.get(i);
                evts.set(0, aux2);
                evts.set(i, aux1);
            }
        }

        if(evts != null) {
            evts.forEach(evt -> {
                Statement stmtBody;
                List<Variable> params;
                List<Function> qvarCond;
                //Step has a different version if it is quantified, otherwise it is the same than the other events
                if(evt.getName().compareTo("Step") == 0 && Constants.STEP_AS_FLOW){
                    stmtBody = rootASTD.trans_event_step(evt, timed, new ArrayList<>(), Conventions.LETS).getValue();
                    // get list of all parameters of the event (including quantified parameters)
                    params = rootASTD.getEvtParams(evt);
                    // if the root astd contains (is a) quantified ASTD(s), generates exists/forall functions but if it is step,
                    // then generate a different version
                    qvarCond = rootASTD.trans_quantified_condition_step(evt, timed, params, new ArrayList<>());
                    hasStep.set(true);
                }
                else {
                    if(evt.getName().compareTo("Step") == 0 && (rootASTD instanceof QSynchronization || rootASTD instanceof QInterleaving || rootASTD instanceof QChoice || rootASTD instanceof QFlow)){
                        stmtBody = rootASTD.trans_event_step(evt, timed, new ArrayList<>(), Conventions.LETS).getValue();
                        // get list of all parameters of the event (including quantified parameters)
                        params = rootASTD.getEvtParams(evt);
                        // if the root astd contains (is a) quantified ASTD(s), generates exists/forall functions but if it is step,
                        hasStep.set(true);
                        qvarCond = rootASTD.trans_quantified_condition_step(evt, timed, params, new ArrayList<>());
                    }
                    else{
                        //STEP or NOT STEP, but not necessary any treatment.
                            //NO SYNC OVER STEP OR STEP AS FLOW
                        stmtBody = rootASTD.trans_event(evt, timed, new ArrayList<>(), Conventions.LETS).getValue();
                        // get list of all parameters of the event (including quantified parameters)
                        params = rootASTD.getEvtParams(evt);
                        // if the root astd contains (is a) quantified ASTD(s), generates exists/forall functions but if it is step,
                        if(evt.getName().compareTo("Step") == 0){
                            hasStep.set(true);
                            qvarCond = rootASTD.trans_quantified_condition_step(evt, timed, params, new ArrayList<>());
                        }
                        else{
                            qvarCond = rootASTD.trans_quantified_condition(evt, timed, params, new ArrayList<>());
                        }
                    }
                }
                if (qvarCond != null && !qvarCond.isEmpty())
                    funcList.addAll(qvarCond);

                // find all occurences of the event and generate the event function
                if(timed.getValue() || Constants.TIMED_SIMULATION){
                    if(Constants.TIMED_SIMULATION){
                        if(evt.getName().equals(Conventions.STEP)){
                            stmtBody = showErrorEventNotExecutableWithTimedSimulation(stmtBody, rootASTD, true);
                        }
                        else{
                            stmtBody = showErrorEventNotExecutableWithTimedSimulation(stmtBody, rootASTD, false);
                        }
                    }
                    else{
                        if(evt.getName().equals(Conventions.STEP)){
                            stmtBody = showErrorEventNotExecutableWithTimer(stmtBody, rootASTD, true);
                        }
                        else{
                            stmtBody = showErrorEventNotExecutableWithTimer(stmtBody, rootASTD, false);
                        }
                    }
                }
                else{
                    stmtBody = showErrorEventNotExecutable(stmtBody, rootASTD);
                }
                Function evtFunc;
                evtFunc = new Function(evt.getName(), params, Conventions.BOOL_TYPE, stmtBody);
                funcList.add(evtFunc);
            });
            if(Constants.EXEC_STATE_ACTIVATED) {
                funcList.addAll(genExecStateFunctions());
            }
        }

        if(Constants.TIMED_SIMULATION && !hasStep.get()){
            //AN timed ASTD that doens't have STEP has to add Step in simulation, otherwise, time does not change
            //ex: There is an delay ASTD but no STEP
            //Solution: Add Step that does only pass time.
            Entry<Condition, Statement> timerStmt = new Entry<>(new CallCondition(Conventions.EQUALS, Arrays.asList(Conventions.EXEC, Conventions.TRUE)),
                    new AssignStatement(new Term(Conventions.LETS), new Term(Conventions.CST)));
            Function stepFunc = new Function(
                    "Step",
                    null,
                    Conventions.BOOL_TYPE,
                    new SeqStatement(Arrays.asList(
                            new DeclStatement(new Variable(Conventions.EXEC, Conventions.BOOL_TYPE, Conventions.TRUE, null)),
                            new AssignStatement(new Term(Conventions.CURRENT_TIME), new Term(Conventions.STEP_VAR+"+"+Conventions.CURRENT_TIME)),
                            new IFFIStatement(Collections.singletonList(timerStmt)),
                            new CallStatement(Conventions.RETURN_CALL, Arrays.asList(Conventions.EXEC))))
                    );
            funcList.add(stepFunc);
        }

        //Adding final state function for quantified ASTD
        if(!Utils.qASTDList.isEmpty()) {
            List<Function> finalfunc = rootASTD.generateFinalFunc(new ArrayList<>());
            for (Function function : finalfunc) {
                funcList.add(0, function);
            }
        }

        // add two threads -> one for environment and one for step
        if (timed.getValue() && !Constants.TIMED_SIMULATION) {
            //create a producer for events.
            funcList.add(rootASTD.trans_producer_events(evts));
            //create a producer for step.
            funcList.add(rootASTD.trans_producer_step(hasStep.get()));
        }

        // add init functions for the history state
        funcList.addAll(genInitHistoryFunctions());
        // add the main function
        if(timed.getValue() && !Constants.TIMED_SIMULATION){
            funcList.add(rootASTD.trans_main_step());
        }
        else{
            funcList.add(rootASTD.trans_main(evt_collections, evts));
        }
        //bake all
        if(!funcList.isEmpty())
            imModel.setFunctions(funcList);
    }

    /*
     * @brief  Updates the IL model with type declarations
     * @param  The IL model
     * @return
     */
    private void setExecSchema(ILModel imModel) {
        // generate type declarations of the intermediate model
        try {
            ExecSchema es = new ExecSchema();
            es.setExecSchemaIL(rootASTD.propertyMapping());
            imModel.setExecSchema(es);
        }catch(Exception e) {}
    }

    /*
     * @brief execution state function generation
     */
    private List<Function> genInitHistoryFunctions() {
        List<Function> funcList = new ArrayList<>();
        List<Function> initHistoryState = rootASTD.initHistoryState();
        if(initHistoryState != null && !initHistoryState.isEmpty())
            funcList.addAll(initHistoryState);

        return funcList;
    }
    /*
     * @brief execution state function generation
     */
    private List<Function> genExecStateFunctions() {
        List<Function> funcList = new ArrayList<>();
        // execute state send to eASTD
        Function execStateToeASTD = new Function(Conventions.EXEC_STATE_SENDTO_EASTD,null,
                                            null,null);
        funcList.add(execStateToeASTD);

        // execution state action function
        SeqStatement seqStmt = new SeqStatement(Arrays.asList(rootASTD.currentStateToJson(),
                                   new CallStatement(Conventions.EXEC_STATE_SENDTO_EASTD, null)));

        Function execStateFunc = new Function(Conventions.EXEC_STATE_ACTION,
                                    Arrays.asList(new Constant(Conventions.EVENT_TEXT, Conventions.STRING, null, null)),
                                    null,
                                    new IFFIStatement(Arrays.asList(
                                        new Entry<>(new Bool(Conventions.IS_EXEC_STATE_ENABLED), seqStmt))));
        funcList.add(execStateFunc);
        // execution state close function
        Function execStateClose = new Function(Conventions.EXEC_STATE_CLOSE,null,null,null);
        funcList.add(execStateClose);

        return funcList;
    }

    /*
    * @brief Initialize the prefix functions. It takes into account the prefix 
    *        for quantified ASTDs.
    * @param
    * @return
    */
    private void initPrefixes() {
        if(Constants.DUMMY_PREFIX2 != null){
            HashMap<String, String> tmp1 = new HashMap<>();
            // create two prefix maps
            for( Map.Entry<Variable, ASTD> entry : Constants.DUMMY_PREFIX2.entrySet()) {
                //This is used for quantified ASTD
                //We need to go through the ASTD tree and find the prefix of each quantified variable
                String k = entry.getKey().getName(), v = entry.getValue().getName();
                tmp1.put(v + "." + k, k);

                Constants.DUMMY_PREFIX.put(entry.getKey(), rootASTD.prefixTree(Constants.QUANT_PREF.get(entry.getKey())) + "." + k);
            }

        }
    }

    /*
     * @brief print error msg "not executable event" when an event is not executable
     * @param  stmt
     * @param  astd
     * @param  b
     * @return  the updated statement with error message
     */
    public Statement showErrorEventNotExecutableWithTimer(Statement stmt, ASTD astd, boolean b) {
        //This method is going to add an assignment exec = 1 after an instruction is executed,
        //with timer, it declares exec_start and assign it value to last_event_time when the execution works
        //the declaration of exec and the return of exec.
        Entry<Condition, Statement> timerStmt = new Entry<>(new CallCondition(Conventions.EQUALS, Arrays.asList(Conventions.EXEC, Conventions.TRUE)),
                new AssignStatement(new Term(Conventions.LETS), new Term(Conventions.TIME_START)));
        if (Conventions.isFlow(astd)) {
            SeqStatement seqStmt = (SeqStatement) stmt;
            List<Statement> stmtList = seqStmt.getStatement();
            if(b){
                return new SeqStatement(Arrays.asList(
                        new DeclStatement(new Variable(Conventions.EXEC, Conventions.BOOL_TYPE, Conventions.FALSE, null)),
                        new AssignStatement(new Term(Conventions.TIME_START), new Term(Conventions.CST)),
                        new SeqStatement(stmtList),
                        new IFFIStatement(Collections.singletonList(timerStmt)),
                        new CallStatement(Conventions.RETURN_CALL, Arrays.asList(Conventions.EXEC))));
            }
            return new SeqStatement(Arrays.asList(
                    new DeclStatement(new Variable(Conventions.EXEC, Conventions.BOOL_TYPE, Conventions.FALSE, null)),
                    new AssignStatement(new Term(Conventions.TIME_START), new Term(Conventions.CST)),
                    new SeqStatement(stmtList),
                    new IFFIStatement(Collections.singletonList(timerStmt)),
                    new CallStatement(Conventions.RETURN_CALL, Arrays.asList(Conventions.EXEC))));
        } else {
            if (Conventions.isIFFI(stmt)) {
                IFFIStatement iffiStmt = (IFFIStatement) stmt;
                List<Entry<Condition, Statement>> iffiList = new ArrayList<>(iffiStmt.getIFFIStatement());
                if(b){
                    return new SeqStatement(Arrays.asList(new DeclStatement(
                                    new Variable(Conventions.EXEC, Conventions.BOOL_TYPE, Conventions.FALSE, null)),
                            new AssignStatement(new Term(Conventions.TIME_START), new Term(Conventions.CST)),
                            new IFFIStatement(iffiList),
                            new IFFIStatement(Collections.singletonList(timerStmt)),
                            new CallStatement(Conventions.RETURN_CALL, Arrays.asList(Conventions.EXEC))));
                }
                return new SeqStatement(Arrays.asList(new DeclStatement(
                                new Variable(Conventions.EXEC, Conventions.BOOL_TYPE, Conventions.FALSE, null)),
                        new AssignStatement(new Term(Conventions.TIME_START), new Term(Conventions.CST)),
                        new IFFIStatement(iffiList),
                        new IFFIStatement(Collections.singletonList(timerStmt)),
                        new CallStatement(Conventions.RETURN_CALL, Arrays.asList(Conventions.EXEC))));
            } else {
                SeqStatement seqStmt = (SeqStatement) stmt;
                List<Statement> stmtList = seqStmt.getStatement();
                if(b){
                    return new SeqStatement(Arrays.asList(new DeclStatement(
                                    new Variable(Conventions.EXEC, Conventions.BOOL_TYPE, Conventions.FALSE, null)),
                            new AssignStatement(new Term(Conventions.TIME_START), new Term(Conventions.CST)),
                            new SeqStatement(stmtList),
                            new IFFIStatement(Collections.singletonList(timerStmt)),
                            new CallStatement(Conventions.RETURN_CALL, Arrays.asList(Conventions.EXEC))));
                }
                return new SeqStatement(Arrays.asList(new DeclStatement(
                                new Variable(Conventions.EXEC, Conventions.BOOL_TYPE, Conventions.FALSE, null)),
                        new AssignStatement(new Term(Conventions.TIME_START), new Term(Conventions.CST)),
                        new SeqStatement(stmtList),
                        new IFFIStatement(Collections.singletonList(timerStmt)),
                        new CallStatement(Conventions.RETURN_CALL, Arrays.asList(Conventions.EXEC))));
            }
        }
    }

    /*
     * @brief print error msg "not executable event" when an event is not executable
     * @param  stmt
     * @param  astd
     * @param  b
     * @return  the updated statement with error message
     */
    public Statement showErrorEventNotExecutableWithTimedSimulation(Statement stmt, ASTD astd, Boolean b) {
        //This method is going to add an assignment exec = 1 after an instruction is executed,
        //with timer, it declares exec_start and assign it value to last_event_time when the execution works
        //the declaration of exec and the return of exec.
        Entry<Condition, Statement> timerStmt = new Entry<>(new CallCondition(Conventions.EQUALS, Arrays.asList(Conventions.EXEC, Conventions.TRUE)),
                new AssignStatement(new Term(Conventions.LETS), new Term(Conventions.CST)));
        if (Conventions.isFlow(astd)) {
            SeqStatement seqStmt = (SeqStatement) stmt;
            List<Statement> stmtList = seqStmt.getStatement();
            if(b){
                return new SeqStatement(Arrays.asList(
                        new DeclStatement(new Variable(Conventions.EXEC, Conventions.BOOL_TYPE, Conventions.FALSE, null)),
                        new AssignStatement(new Term(Conventions.CURRENT_TIME), new Term(Conventions.STEP_VAR+"+"+Conventions.CURRENT_TIME)),
                        new SeqStatement(stmtList),
                        new IFFIStatement(Collections.singletonList(timerStmt)),
                        new CallStatement(Conventions.RETURN_CALL, Arrays.asList(Conventions.EXEC))));
            }
            else{
                return new SeqStatement(Arrays.asList(
                        new DeclStatement(new Variable(Conventions.EXEC, Conventions.BOOL_TYPE, Conventions.FALSE, null)),
                        new SeqStatement(stmtList),
                        new IFFIStatement(Collections.singletonList(timerStmt)),
                        new CallStatement(Conventions.RETURN_CALL, Arrays.asList(Conventions.EXEC))));
            }
        } else {
            if (Conventions.isIFFI(stmt)) {
                IFFIStatement iffiStmt = (IFFIStatement) stmt;
                List<Entry<Condition, Statement>> iffiList = new ArrayList<>(iffiStmt.getIFFIStatement());
                if(b){
                    return new SeqStatement(Arrays.asList(new DeclStatement(
                                    new Variable(Conventions.EXEC, Conventions.BOOL_TYPE, Conventions.FALSE, null)),
                            new AssignStatement(new Term(Conventions.CURRENT_TIME), new Term(Conventions.STEP_VAR+"+"+Conventions.CURRENT_TIME)),
                            new IFFIStatement(iffiList),
                            new IFFIStatement(Collections.singletonList(timerStmt)),
                            new CallStatement(Conventions.RETURN_CALL, Arrays.asList(Conventions.EXEC))));
                }
                else{
                    return new SeqStatement(Arrays.asList(new DeclStatement(
                                    new Variable(Conventions.EXEC, Conventions.BOOL_TYPE, Conventions.FALSE, null)),
                            new IFFIStatement(iffiList),
                            new IFFIStatement(Collections.singletonList(timerStmt)),
                            new CallStatement(Conventions.RETURN_CALL, Arrays.asList(Conventions.EXEC))));
                }

            } else {
                SeqStatement seqStmt = (SeqStatement) stmt;
                List<Statement> stmtList = seqStmt.getStatement();
                if(b){
                    return new SeqStatement(Arrays.asList(new DeclStatement(
                                    new Variable(Conventions.EXEC, Conventions.BOOL_TYPE, Conventions.FALSE, null)),
                            new AssignStatement(new Term(Conventions.CURRENT_TIME), new Term(Conventions.STEP_VAR + "+" + Conventions.CURRENT_TIME)),
                            new SeqStatement(stmtList),
                            new IFFIStatement(Collections.singletonList(timerStmt)),
                            new CallStatement(Conventions.RETURN_CALL, Arrays.asList(Conventions.EXEC))));
                }
                else{
                    return new SeqStatement(Arrays.asList(new DeclStatement(
                                    new Variable(Conventions.EXEC, Conventions.BOOL_TYPE, Conventions.FALSE, null)),
                            new SeqStatement(stmtList),
                            new IFFIStatement(Collections.singletonList(timerStmt)),
                            new CallStatement(Conventions.RETURN_CALL, Arrays.asList(Conventions.EXEC))));
                }

            }
        }
    }

    /*
     * @brief print error msg "not executable event" when an event is not executable
     * @param  stmt
     * @param  astd
     * @param  b
     * @return  the updated statement with error message
     */
    public Statement showErrorEventNotExecutable(Statement stmt, ASTD astd) {
        //This method is going to add an assignment exec = 1 after an instruction is executed,
        //the declaration of exec and the return of exec.
        if (Conventions.isFlow(astd)) {
            //If enter here, needs to check if the flag is 0, is yes, then something went wrong. Return 0.
            //The flag is declared inside the flow.java
            //The assignment of the flag is inside flow.java
            //Needs to call showErrorEventNotExecutable for the stmts inside stmt
            //Also needs to check b
            SeqStatement seqStmt = (SeqStatement) stmt;
            List<Statement> stmtList = seqStmt.getStatement();
            return new SeqStatement(Arrays.asList(new DeclStatement(
                            new Variable(Conventions.EXEC, Conventions.BOOL_TYPE, Conventions.FALSE, null)),
                    new SeqStatement(stmtList),
                    //new IFFIStatement(Collections.singletonList(errorStmt)),
                    new CallStatement(Conventions.RETURN_CALL, Arrays.asList(Conventions.EXEC))));


        } else {
            if (Conventions.isIFFI(stmt)) {
                IFFIStatement iffiStmt = (IFFIStatement) stmt;
                List<Entry<Condition, Statement>> iffiList = new ArrayList<>(iffiStmt.getIFFIStatement());
                return new SeqStatement(Arrays.asList(new DeclStatement(
                                new Variable(Conventions.EXEC, Conventions.BOOL_TYPE, Conventions.FALSE, null)),
                        new IFFIStatement(iffiList),
                        new CallStatement(Conventions.RETURN_CALL, Arrays.asList(Conventions.EXEC))));
            } else {
                SeqStatement seqStmt = (SeqStatement) stmt;
                List<Statement> stmtList = seqStmt.getStatement();
                return new SeqStatement(Arrays.asList(new DeclStatement(
                                new Variable(Conventions.EXEC, Conventions.BOOL_TYPE, Conventions.FALSE, null)),
                        new SeqStatement(stmtList),
                        new CallStatement(Conventions.RETURN_CALL, Arrays.asList(Conventions.EXEC))));
            }
        }
    }

    public static void main(String[] args) {
        // Parse Command line Options
        Bool timed = new Bool(false);
        String[] outArgs = CmdManager.handler(args);
        String help = outArgs[3], version = outArgs[2];

        if(help == null && version == null) {
            try {
                //Extracts ASTD object from JSON file
                String specPath = outArgs[0];
                if (specPath == null) {
                    System.out.println("[Error] Missing ASTD specification !!");
                    return;
                }

                String mainAstdName = outArgs[5];
                ASTDParser p = new ASTDParser(specPath, mainAstdName);
                ASTD rootASTD = p.parse(timed);
                if (Constants.DEBUG) {
                    System.out.println("=======================");
                    Utils.print(rootASTD);
                }

                // Applies Kappa optimization on the ASTD object
                if (Constants.KAPPA_DIRECT_OPTS) {
                    rootASTD.kappaOptimize(KappaOptimizer.AnalysisMode.DIRECT_KAPPA);
                }

                String eventFormat = outArgs[4];
                if (eventFormat != null)
                    Constants.EVT_FORMAT = eventFormat;

                // compile the ASTD object in the intermediate language
                Compiler c = new Compiler(rootASTD);
                String step_value = outArgs[8];
                String initial_time = outArgs[9];
                ILModel ilm = c.compile(timed, step_value, initial_time);
                if (Constants.DEBUG) {
                    System.out.println("=======================");
                    Utils.print(ilm);
                }

                // Generate target code from the Intermediate code
                ILTranslator.Lang lang = ILTranslator.Lang.CPP;
                ILTranslator t = new ILTranslator(ilm, lang);
                String targetCode = t.header(timed) // headers: includes, defines, etc.
                        .types()        // types
                        .variables()    // variable declaration
                        .functions(timed)    // list of functions
                        .translate();   // generate code
                if (Constants.DEBUG) System.out.println("targetCode = \n" + targetCode);

                // generate the executable program
                String outputPath = outArgs[1];
                if (outputPath == null) {
                    System.out.println("[Error] Missing Output directory !!");
                    return;
                }
                String libPath = outArgs[6];
                String includePath = outArgs[7];
                PackageBuilder pb = new PackageBuilder(targetCode, lang, outputPath, libPath, includePath);
                pb.build(timed);

                //free the garbage collector
                System.gc();
            }
            catch(Exception e) {
                System.out.println("[Error] Something wrong !!");
                System.out.println("Please look at the following error : ");
                e.printStackTrace();
            }
        }
    }
}
