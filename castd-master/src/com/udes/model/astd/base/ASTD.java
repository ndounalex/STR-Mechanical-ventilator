package com.udes.model.astd.base;

import com.udes.model.astd.items.*;
import com.udes.model.astd.tojson.ToJson;
import com.udes.model.il.conditions.CallCondition;
import com.udes.model.il.conditions.Condition;
import com.udes.model.il.conditions.NotCondition;
import com.udes.model.il.containers.Entry;
import com.udes.model.il.conventions.Conventions;
import com.udes.model.il.methods.Function;
import com.udes.model.il.predicates.BinaryPredicate;
import com.udes.model.il.record.Record;
import com.udes.model.il.record.Enum;
import com.udes.model.il.statements.*;
import com.udes.model.il.terms.Bool;
import com.udes.model.il.terms.Term;
import com.udes.optimizer.KappaOptimizer;
import com.udes.parser.ASTDParser;
import com.udes.parser.ExecSchemaParser;
import com.udes.translator.ILTranslator;
import com.udes.utils.Constants;
import com.udes.utils.Utils;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class  ASTD implements IASTD {

    private Set<String> onto_classes;
    private Set<String> imports;
    private Set<String> type_defs;
    private String name;
    private List<Variable> attributes;
    private List<Variable> params;
    private Action astdAction;
    private ToJson toJson;
    private boolean isRoot;
    private boolean isPrefixModeEnabled;
    public static ASTD rootASTD;


    public ASTD(String name, List<Variable> attributes, List<Variable> params, Action astdAction) {
        this.name = Utils.generateNameIfNotExists(name);
        this.attributes = attributes;
        this.params = params;
        this.astdAction = astdAction;
        setToJson(new ToJson());
    }

    public ASTD(String name) {
        this(Utils.generateNameIfNotExists(name), null, null, null);
        setToJson(new ToJson());
    }

    public ASTD() {
        this(Utils.generateNameIfNotExists(""), null, null, null);
        setToJson(new ToJson());
    }

    /*
     * @brief Returns name
     * @param
     * @return The ASTD name
     */
    @Override
    public String getName() {
        return name;
    }

    /*
     * @brief Sets the name
     * @param The ASTD name
     * @return
     */
    @Override
    public void setName(String name) {
        this.name = Utils.generateNameIfNotExists(name);
    }

    /*
     * @brief Returns ontology classes
     * @param
     * @return The ontology classes
     */
    @Override
    public Set<String> getOntoClasses() {
        return onto_classes;
    }

    /*
     * @brief Sets the ontology classes
     * @param The ontology classes
     * @return
     */
    @Override
    public void setOntoClasses(Set<String> onto_classes) {
        this.onto_classes = onto_classes;
    }

    /*
     * @brief Returns imports
     * @param
     * @return imports
     */
    @Override
    public Set<String> getImports() {
        return imports;
    }

    /*
     * @brief Sets imports
     * @param imports
     * @return
     */
    @Override
    public void setImports(Set<String> imports) {
        this.imports = imports;
    }

    /*
     * @brief Returns type defs
     * @param
     * @return type_defs
     */
    @Override
    public Set<String> getTypeDefs() {
        return type_defs;
    }

    /*
     * @brief Sets type definitions
     * @param type definitions
     * @return
     */
    @Override
    public void setTypeDefs(Set<String> type_defs) {
        this.type_defs = type_defs;
    }

    /*
     * @brief Returns attributes
     * @param
     * @return attributes
     */
    @Override
    public List<Variable> getAttributes() {
        return attributes;
    }

    /*
     * @brief Sets attributes
     * @param attributes
     * @return
     */
    @Override
    public void setAttributes(List<Variable> attributes) {
        this.attributes = attributes;
    }

    /*
     * @brief Returns ASTD params for a called ASTD
     * @param
     * @return param list
     */
    @Override
    public List<Variable> getParams() {
        return params;
    }

    /*
     * @brief Sets params
     * @param params
     * @return
     */
    @Override
    public void setParams(List<Variable> params) {
        this.params = params;
    }

    /*
     * @brief Returns ASTD action
     * @param
     * @return ASTD action
     */
    @Override
    public Action getAstdAction() {
        return astdAction;
    }

    /*
     * @brief Sets ASTD action
     * @param ASTD action
     * @return
     */
    @Override
    public void setAstdAction(Action astdAction) {
        this.astdAction = astdAction;
    }

    /*
     * @brief Generate the code used to initialize an ASTD
     * @param  ASTD model
     * @return The statement block for initialization
     */
    @Override
    public abstract Statement init(ArrayList<ASTD> callList, String lets);

    @Override
    public abstract Statement initforsub(ArrayList<ASTD> callList, Event e, Bool timed, String lets, boolean forFinal);

    /*
     * @brief Generate the condition that checks if an ASTD is final
     * @param  ASTD model
     * @return  A condition
     */
    @Override
    public abstract Condition _final(ArrayList<ASTD> callList);

    /*
     * @brief Generate the condition that checks if an ASTD is final
     * @param  ASTD model
     * @return  A condition
     */
    @Override
    public abstract Condition _finalForSub(ArrayList<ASTD> callList);

    /*
     * @brief Generate external references of the top of the model
     * @param  ASTD model
     * @return List of refs
     */
    @Override
    public abstract Set<String> trans_refs();

    /*
     * @brief Generate type structures from ASTDs excepts Elem type (since it's unused)
     * @param  ASTD model
     * @return List of type structures
     */
    @Override
    public abstract Entry<List<Enum>, List<Record>> trans_type();

    /*
     * @brief Generate variable declarations from ASTD states
     * @param  ASTD model
     * @return List of variables
     */
    @Override
    public abstract List<Variable> trans_var();

    /*
     * @brief Generate type structures from ASTD states
     * @param  ASTD model
     * @param  Event label
     * @return The container with the disjunction of conditions and the if-fi statement
     */
    @Override
    public abstract Entry<Condition, Statement> trans_event(Event e, Bool timed, ArrayList<ASTD> callList, String lets);

    @Override
    public abstract Entry<Condition, Statement> trans_event_step(Event e, Bool timed, ArrayList<ASTD> callList, String lets);

    /*
     * @brief Generate the main function of the IL model
     * @param  ASTD model
     * @return The main function
     */
    @Override
    public Function trans_main(Set<Event> evt_collections, ArrayList<Event> evts) {
        // init ASTD
        Statement initStmt = init(new ArrayList<>(), Conventions.CST);
        // if-fi statement
        List<Entry<Condition, Statement>> entries = new ArrayList<>();
        // event is not empty
        entries.add(new Entry<>(new CallCondition(Conventions.EMPTY,
                Arrays.asList(Conventions.EVENT_LABEL)), new Action(Conventions.BREAK)));
        // event statements
        evts.forEach(e -> {
            if (Constants.EXEC_STATE_ACTIVATED) {
                List<Entry<Condition, Statement>> subEntries = new ArrayList<>();
                subEntries.add(new Entry<>(new CallCondition(0, e.getName(),
                        Arrays.asList(getEvtParams(e))),
                        new CallStatement(Conventions.EXEC_STATE_ACTION,
                                Arrays.asList("\"" + e.getName() + "\""))));
                subEntries.add(new Entry<>(null, new CallStatement(Conventions.ERROR_LABEL1,
                        Collections.singletonList(Conventions.getErrorMsg(null, 1)))));

                IFFIStatement iffiStmt = new IFFIStatement(subEntries);
                entries.add(new Entry<>(new CallCondition(Conventions.EQUALS,
                        Arrays.asList(Conventions.EVENT_LABEL, e.getName())), iffiStmt));
            } else if (e.getName().equals(Conventions.STEP)) {
                List<Entry<Condition, Statement>> subEntries = new ArrayList<>();
                subEntries.add(new Entry<>(new CallCondition(0, e.getName(),
                        Arrays.asList(getEvtParams(e))), null));
                IFFIStatement iffiStmt = new IFFIStatement(subEntries);
                entries.add(new Entry<>(new CallCondition(Conventions.EQUALS,
                        Arrays.asList(Conventions.EVENT_LABEL, e.getName())), iffiStmt));
            } else {
                List<Entry<Condition, Statement>> subEntries = new ArrayList<>();
                subEntries.add(new Entry<>(new CallCondition(0, e.getName(),
                        Arrays.asList(getEvtParams(e))), null));
                subEntries.add(new Entry<>(null, new CallStatement(Conventions.ERROR_LABEL1,
                        Collections.singletonList(Conventions.getErrorMsg(null, 1)))));

                IFFIStatement iffiStmt = new IFFIStatement(subEntries);
                entries.add(new Entry<>(new CallCondition(Conventions.EQUALS,
                        Arrays.asList(Conventions.EVENT_LABEL, e.getName())), iffiStmt));

            }
        });

        if(Constants.TIMED_SIMULATION){
            entries.add(new Entry<>(new CallCondition(Conventions.SIMULATION,
                    Arrays.asList(Conventions.NIL)), null));
        }

        // else throw error
        entries.add(new Entry<>(null, new CallStatement(Conventions.ERROR_LABEL,
                Collections.singletonList(Conventions.getErrorMsg(null, 0)))));


        IFFIStatement ifStmt = new IFFIStatement(entries);
        // while loop statement

        CallStatement retStmt = new CallStatement(Conventions.RETURN_CALL, Arrays.asList(Conventions.FALSE));

        if (Constants.EXEC_STATE_ACTIVATED) {
            CallStatement execStateStop = new CallStatement(Conventions.EXEC_STATE_CLOSE, null),
                    execStateInit = new CallStatement(Conventions.EXEC_STATE_INIT, Arrays.asList("\"init\""));
            // main function
            // read event statement
            AssignStatement assStmt = new AssignStatement(new Term(Conventions.EVENT),
                    new BinaryPredicate<>(
                            Conventions.READ_EVENT,
                            Collections.singletonList(Conventions.EVENT_SOURCE), 1));
            //if there is a while, then it is While{While{}}
            //STEP declaration and While for Step
            WhileStatement whileStmt = new WhileStatement(null, null);
            whileStmt.setCondition(new Bool(Conventions.TRUE));
            whileStmt.setStatement(new SeqStatement(Arrays.asList(assStmt, ifStmt)));
            assStmt = new AssignStatement(new Term(Conventions.EVENT),
                    new BinaryPredicate<>(Conventions.CONFIG_INPUT_STREAM, null, 0));
            return new Function(Conventions.MAIN_FUNCTION, getParams(), null,
                        new SeqStatement(Arrays.asList(assStmt, initStmt, execStateInit,
                                whileStmt, execStateStop, retStmt)));
        } else {
            // main function
            // read event statement
            AssignStatement assStmt = new AssignStatement(new Term(Conventions.EVENT),
                    new BinaryPredicate<>(Conventions.READ_EVENT,
                            Collections.singletonList(Conventions.EVENT_SOURCE), 1));
            //if there is a while, then it is While{While{}}
            //STEP declaration and While for Step
            WhileStatement whileStmt = new WhileStatement(null, null);
            whileStmt.setCondition(new Bool(Conventions.TRUE));
            whileStmt.setStatement(new SeqStatement(Arrays.asList(assStmt, ifStmt)));

            assStmt = new AssignStatement(new Term(Conventions.EVENT),
                    new BinaryPredicate<>(Conventions.CONFIG_INPUT_STREAM, null, 0));
            return new Function(Conventions.MAIN_FUNCTION, getParams(), null,
                        new SeqStatement(Arrays.asList(assStmt, initStmt,
                                whileStmt, retStmt)));
        }
    }

    /*
     * @brief Generate the main function of the IL model
     * @param  ASTD model
     * @return The main function when step exists
     */
    @Override
    public Function trans_main_step() {
        // init ASTD
        Statement initStmt = init(new ArrayList<>(), Conventions.CST);
        //IO configInputStream.
        AssignStatement assStmt = new AssignStatement(new Term(Conventions.EVENT),
                new BinaryPredicate<>(Conventions.CONFIG_INPUT_STREAM, null, 0));
        //Add the declaration of the threads.
        //DeclStatement threadDeclCons = new DeclStatement(new Variable(Conventions.CONSUMER, Conventions.THREAD_DEC, null, null));
        DeclStatement threadDeclProd = new DeclStatement(new Variable(Conventions.PRODUCER+Conventions.ARRAY2, Conventions.THREAD_DEC, null, null));
//        CallStatement thread_create1 = new CallStatement(Conventions.THREAD_CRE, Arrays.asList(Conventions.CONSUMER));
        CallStatement thread_create2 = new CallStatement(Conventions.THREAD_CRE, Arrays.asList(Conventions.PRODUCER));
//        CallStatement thread_join1 = new CallStatement(Conventions.THREAD_JOIN, Arrays.asList(Conventions.CONSUMER));
        CallStatement thread_join2 = new CallStatement(Conventions.THREAD_JOIN, Arrays.asList(Conventions.PRODUCER));

        CallStatement retStmt = new CallStatement(Conventions.RETURN_CALL, Arrays.asList(Conventions.FALSE));
//        return new Function(Conventions.MAIN_FUNCTION, getParams(), null,
//                new SeqStatement(Arrays.asList(assStmt, initStmt, threadDeclCons, threadDeclProd,
//                        thread_create1, thread_create2, thread_join1, thread_join2, retStmt)));

        return new Function(Conventions.MAIN_FUNCTION, getParams(), null,
                new SeqStatement(Arrays.asList(assStmt, initStmt, threadDeclProd,
                        thread_create2, thread_join2, retStmt)));
    }

    @Override
    public  Function trans_producer_events(ArrayList<Event> evts){

        List<Entry<Condition, Statement>> entries = new ArrayList<>();

        // event is not empty
        entries.add(new Entry<>(new CallCondition(Conventions.EMPTY,
                Arrays.asList(Conventions.EVENT_LABEL)),
                new SeqStatement(Arrays.asList(
                        new AssignStatement(new Term (Conventions.SIGNAL), new Term( Conventions.FALSE)),
                        new CallStatement(Conventions.ERROR_LABEL5,
                                Collections.singletonList(Conventions.getErrorMsg(null, 4)))))));

        //read
        AssignStatement assStmt = new AssignStatement(new Term(Conventions.EVENT),
                new BinaryPredicate<>(Conventions.READ_EVENT,
                        Collections.singletonList(Conventions.EVENT_SOURCE), 3));

        WhileStatement whileStmt = new WhileStatement(null, null);
        whileStmt.setCondition(new Bool(Conventions.SIGNAL));

        //argument
        Variable argc = new Variable("argc", Conventions.INT+"*", "(int*) arg", null);
        DeclStatement argcStmt = new DeclStatement(argc);

        // event statements
        evts.forEach(e -> {
            if (Constants.EXEC_STATE_ACTIVATED && !e.getName().equals(Conventions.STEP)) {
                List<Entry<Condition, Statement>> subEntries = new ArrayList<>();
                subEntries.add(new Entry<>(new NotCondition(new CallCondition(0, e.getName(),
                        Arrays.asList(getEvtParams(e)))),
                        new CallStatement(Conventions.EXEC_STATE_ACTION,
                                Arrays.asList("\"" + e.getName() + "\""))));
                subEntries.add(new Entry<>(null, new CallStatement(Conventions.ERROR_LABEL1,
                        Collections.singletonList(Conventions.getErrorMsg(null, 1)))));

                IFFIStatement iffiStmt = new IFFIStatement(subEntries);
                entries.add(new Entry<>(new NotCondition( new CallCondition(Conventions.EQUALS,
                        Arrays.asList(Conventions.EVENT_LABEL, e.getName()))), iffiStmt));
            } else if(!e.getName().equals(Conventions.STEP)) {
                List<Entry<Condition, Statement>> subEntries = new ArrayList<>();
                subEntries.add(new Entry<>(new NotCondition( new CallCondition(0, e.getName(),
                        Arrays.asList(getEvtParams(e)))), new CallStatement(Conventions.ERROR_LABEL1,
                        Collections.singletonList(Conventions.getErrorMsg(null, 1)))));

                IFFIStatement iffiStmt = new IFFIStatement(subEntries);
                entries.add(new Entry<>(new CallCondition(Conventions.EQUALS,
                        Arrays.asList(Conventions.EVENT_LABEL, e.getName())), iffiStmt));

            }
        });
        // else throw error
        entries.add(new Entry<>(null, new CallStatement(Conventions.ERROR_LABEL,
                Collections.singletonList(Conventions.getErrorMsg(null, 0)))));


        IFFIStatement ifStmt = new IFFIStatement(entries);

        //return void
        CallStatement retStmt = new CallStatement(Conventions.RETURN_CALL, Arrays.asList(Conventions.NIL));

        //lock mutex
        CallStatement lock = new CallStatement(Conventions.LOCK, Arrays.asList(Conventions.MTX));

        //unlock mutex
        CallStatement unlock = new CallStatement(Conventions.UNLOCK, Arrays.asList(Conventions.MTX));

        whileStmt.setStatement(new SeqStatement(Arrays.asList(argcStmt, lock, assStmt, ifStmt, unlock)));
        return new Function(Conventions.PRODUCER+Conventions.DUMMY_PARAMS+Conventions.EVENT_TEXT+Conventions._FUNC, getParams(), null,
                new SeqStatement(Arrays.asList(whileStmt, retStmt)));
    }

    @Override
    public  Function trans_producer_step(boolean hasStep){
        List<Entry<Condition, Statement>> entries = new ArrayList<>();

        //lock mutex
        CallStatement lock = new CallStatement(Conventions.LOCK, Arrays.asList(Conventions.MTX));

        //unlock mutex
        CallStatement unlock = new CallStatement(Conventions.UNLOCK, Arrays.asList(Conventions.MTX));

        CallStatement step = new CallStatement("Step", new ArrayList<>());
        //For step there isn't error2.
        //subEntries.add(new Entry<>(null, new CallStatement(Conventions.ERROR_LABEL1,
        //        Collections.singletonList(Conventions.getErrorMsg(null, 1)))));

        //if now >= lst + step value then produce a step event
        entries.add(new Entry<>(new CallCondition(Conventions.EXPIRED,
                Arrays.asList(Conventions.LST, Conventions.STEP_VAR)),
                new SeqStatement(Arrays.asList(new AssignStatement(new Term (Conventions.LST), new Term(Conventions.CST)), lock, step, unlock))));

        IFFIStatement ifStmt = new IFFIStatement(entries);

        WhileStatement whileStmt = new WhileStatement(null, null);
        whileStmt.setCondition(new Bool(Conventions.SIGNAL));

        //thread sleep
        CallStatement thdSleepStmt = new CallStatement(Conventions.THREAD_SLEEP, null);

        //return void
        CallStatement retStmt = new CallStatement(Conventions.RETURN_CALL, Arrays.asList(Conventions.NIL));

        whileStmt.setStatement(new SeqStatement(Arrays.asList(ifStmt, thdSleepStmt)));
        return new Function(Conventions.PRODUCER+Conventions.DUMMY_PARAMS+Conventions.STEP+Conventions._FUNC, getParams(), null,
                new SeqStatement(Arrays.asList(whileStmt, retStmt)));
    }

    @Override
    public  Function  trans_consumer(ArrayList<Event> evts){
        Variable consumed_Event = new Variable(Conventions.CONSUMED, Conventions.EVENT_TYPE,null, null);
        DeclStatement initStmt = new DeclStatement(consumed_Event);


        // if-fi statement
        List<Entry<Condition, Statement>> entries = new ArrayList<>();
        //wait dequeue to consume event
        CallStatement waitdeq = new CallStatement(Conventions.WAIT_DEQ, Arrays.asList(consumed_Event));

        entries.add(new Entry<>(new CallCondition(Conventions.EMPTY,
                Arrays.asList(Conventions.EVENT_LABEL)),
                new CallStatement(Conventions.ERROR_LABEL5,
                        Collections.singletonList(Conventions.getErrorMsg(null, 4)))));

        // event statements
        evts.forEach(e -> {
            if (Constants.EXEC_STATE_ACTIVATED) {
                List<Entry<Condition, Statement>> subEntries = new ArrayList<>();
                subEntries.add(new Entry<>(new CallCondition(0, e.getName(),
                        Arrays.asList(getEvtParams(e))),
                        new CallStatement(Conventions.EXEC_STATE_ACTION,
                                Arrays.asList("\"" + e.getName() + "\""))));
                subEntries.add(new Entry<>(null, new CallStatement(Conventions.ERROR_LABEL1,
                        Collections.singletonList(Conventions.getErrorMsg(null, 1)))));

                IFFIStatement iffiStmt = new IFFIStatement(subEntries);
                entries.add(new Entry<>(new CallCondition(Conventions.EQUALS,
                        Arrays.asList(Conventions.EVENT_LABEL, e.getName())), iffiStmt));
           } else if (e.getName().equals(Conventions.STEP)) {
                List<Entry<Condition, Statement>> subEntries = new ArrayList<>();
                subEntries.add(new Entry<>(new CallCondition(0, e.getName(),
                        Arrays.asList(getEvtParams(e))), null));
                //For step there isn't error2.
                //subEntries.add(new Entry<>(null, new CallStatement(Conventions.ERROR_LABEL1,
                //        Collections.singletonList(Conventions.getErrorMsg(null, 1)))));
                IFFIStatement iffiStmt = new IFFIStatement(subEntries);
                entries.add(new Entry<>(new CallCondition(Conventions.EQUALS,
                        Arrays.asList(Conventions.EVENT_LABEL, e.getName())), iffiStmt));

            } else {
                List<Entry<Condition, Statement>> subEntries = new ArrayList<>();
                subEntries.add(new Entry<>(new CallCondition(0, e.getName(),
                        Arrays.asList(getEvtParams(e))), null));
                subEntries.add(new Entry<>(null, new CallStatement(Conventions.ERROR_LABEL1,
                        Collections.singletonList(Conventions.getErrorMsg(null, 1)))));

                IFFIStatement iffiStmt = new IFFIStatement(subEntries);
                entries.add(new Entry<>(new CallCondition(Conventions.EQUALS,
                        Arrays.asList(Conventions.EVENT_LABEL, e.getName())), iffiStmt));

            }
        });
        // else throw error
        entries.add(new Entry<>(null, new CallStatement(Conventions.ERROR_LABEL,
                Collections.singletonList(Conventions.getErrorMsg(null, 0)))));


        IFFIStatement ifStmt = new IFFIStatement(entries);
        // while loop statement

        CallStatement retStmt = new CallStatement(Conventions.RETURN_CALL, Arrays.asList(Conventions.NIL));

        if (Constants.EXEC_STATE_ACTIVATED) {
            CallStatement execStateStop = new CallStatement(Conventions.EXEC_STATE_CLOSE, null),
                    execStateInit = new CallStatement(Conventions.EXEC_STATE_INIT, Arrays.asList("\"init\""));
            // main function
            WhileStatement whileStmt = new WhileStatement(null, null);
            whileStmt.setCondition(new Bool(Conventions.SIGNAL));
            whileStmt.setStatement(new SeqStatement(Arrays.asList(waitdeq, ifStmt)));
            return new Function(Conventions.CONSUMER+Conventions._FUNC, getParams(), null,
                        new SeqStatement(Arrays.asList(execStateInit,
                                whileStmt, execStateStop, retStmt)));
        } else {
            // main function
            WhileStatement whileStmt = new WhileStatement(null, null);
            whileStmt.setCondition(new Bool(Conventions.SIGNAL));
            whileStmt.setStatement(new SeqStatement(Arrays.asList(waitdeq, ifStmt)));
            return new Function(Conventions.CONSUMER+Conventions._FUNC, getParams(), null,
                        new SeqStatement(Arrays.asList(initStmt,
                                whileStmt, retStmt)));
        }
    }

    /*
     * @brief  Gets all events appearing in an ASTD
     * @param  ASTD model
     * @return The list of events
     */
    public abstract Set<Event> findAllEvents();

    /*
     * @brief  Gets all states appearing in an ASTD
     * @param  ASTD model
     * @return The list of events
     */
    public abstract List<String> findAllStates();

    /*
     * @brief  Gets if ASTD has an history State
     * @param  ASTD model
     * @return If ASTD has an history State
     */
    public abstract boolean hasHistoryState();

    /*
     * @brief  List of init functions
     * @param  ASTD model
     * @return A list of init functions
     */
    public abstract List<Function> initHistoryState();

    /*
     * @brief  Gets all transitions where an event appears
     * @param  An event label
     * @return The list of events
     */
    public abstract List<Transition> findTransitions(String evtLabel);

    /*
     * @brief  Computes all attribute prefixes. It will be used later by guards and actions.
     * @param  ASTD
     * @return The map of var name and their associated prefix
     */
    public abstract List<Variable> getAllVariables();

    /*
     * @brief  Computes the prefix that should be added to access a property of a structure
     * @param  The parent ASTD model
     * @param  The child name to prefix
     * @return A string prefix
     */
    public abstract String prefix(String childName);

    public abstract String prefixTree(ArrayList<ASTD> ASTDTree);

    public abstract String prefixTree(ArrayList<ASTD> ASTDTree, String ref);

    /*
     * @brief Get the variables of the enclosing ASTDs of an ASTD
     * @param  the parent ASTD model
     * @param  the child name
     * @return A list of variables
     */
    public abstract List<Variable> enclosingASTDVariables(ArrayList<ASTD> ASTDTree);

    /*
     * @brief Get the parameters of the enclosing ASTDs of an ASTD
     * @param  the parent ASTD model
     * @param  the child name
     * @return A list of variables
     */
    public abstract List<Variable> enclosingASTDParams(ArrayList<ASTD> ASTDTree);


    /*
     * @brief  Computes all qvariables
     * @param
     * @return A list of qvariables
     */
    public abstract List<Variable> getAllQVariables(ArrayList<ASTD> callList);

    /*
     * @brief  Computes all qvariables
     * @param
     * @return A list of qvariables
     */
    public abstract List<Variable> getAllEventQVariables(Event e);

    /*
     * @brief  Computes all parameters of an event
     * @param  An event
     * @return A list of parameters
     */
    public abstract List<Variable> getAllEventParams(Event e);

    /*
     * @brief  Returns the initial state value
     * @param  ASTD model
     * @return A string value
     */
    public abstract String getInitialStateValue();


    /*
     * @brief  Returns the update function call
     * @param  Regex pattern and code
     * @return A string value
     */
    private String updateFunctionCall(ArrayList<String> arrayWParams, ArrayList<ASTD> ASTDTree,
                                      HashMap<String, ArrayList<String>> mapFunctionsParamsInitial,
                                      HashMap<String, String> mapFunctionsParamsInitial2, HashMap<String,String> mapFunctionsInitial,
                                      HashMap<String,String> mapFunctionsInitial2) {
        String func_name = arrayWParams.get(0);
        String func_params = "";
        if(arrayWParams.get(1) != null){
            func_params = arrayWParams.get(1);
        }
        Matcher m2, m3;
        //check if a parameter is a function with parameters
        m2 = Pattern.compile(Constants.FUNC_PARAMS).matcher(func_params); //function with params
        HashMap<String, ArrayList<String>> mapFunctionsParams1 = new HashMap<>(mapFunctionsParamsInitial);
        HashMap<String,String> mapFunctions = new HashMap<>(mapFunctionsInitial);
        //first: functions with params
        if(m2.find()){
            func_params = Utils.maskFunction(func_params, mapFunctionsParams1);
            for(Map.Entry<String, ArrayList<String>> entry : mapFunctionsParams1.entrySet()){
                if(!mapFunctionsParamsInitial.containsKey(entry.getKey())){
                    //add new values
                    mapFunctionsParamsInitial.put(entry.getKey(), mapFunctionsParams1.get(entry.getKey()));
                    mapFunctionsParamsInitial2.put(entry.getKey(), updateFunctionCall(
                            entry.getValue(), ASTDTree,
                            mapFunctionsParamsInitial, mapFunctionsParamsInitial2,
                            mapFunctionsInitial, mapFunctionsInitial2));
                }
            }
        }
        //second: functions without params
        m3 = Pattern.compile(Constants.FUNC_SANS_PARAMS).matcher(func_params); //function without params
        if(m3.find()){
            func_params = Utils.maskFunctionWithoutParams(func_params, mapFunctions);
            for(Map.Entry<String, String> entry : mapFunctions.entrySet()){
                if(!mapFunctionsInitial.containsKey(entry.getKey())){
                    ArrayList<String> fake = new ArrayList<>();
                    fake.add(entry.getValue());
                    fake.add(null);
                    mapFunctionsInitial.put(entry.getKey(), mapFunctions.get(entry.getKey()));
                    mapFunctionsInitial2.put(entry.getKey(), updateFunctionCall(
                            fake, ASTDTree,
                            mapFunctionsParamsInitial, mapFunctionsParamsInitial2,
                            mapFunctionsInitial, mapFunctionsInitial2));
                }
            }
        }
        //treat what is left
        String[] params = func_params.split(",");
        String new_params = "";
        List<Variable> attributes = rootASTD.enclosingASTDVariables(ASTDTree);
        List<Variable> keys = new ArrayList<>();
        keys.addAll(attributes);
        List<Variable> parameters = rootASTD.enclosingASTDParams(ASTDTree);
        for(Variable parameter : parameters){
            boolean newName = true;
            for(Variable key : keys){
                if(key.getName().equals(parameter.getName())){
                    newName = false;
                }
            }
            if(newName){
                keys.add(parameter);
            }
        }
        Collections.reverse(keys);
        List<ASTD> astdNames = new ArrayList<>();
        astdNames.addAll(ASTDTree);
        if(func_params != ""){
            for (String p : params) {
                String prev_prefix = "";
                int i = 0;

                List<String> previous_prefixes = new ArrayList<>();
                List<String> previous_prefixesREG = new ArrayList<>();

                String paramsout = p;
                if (keys != null) {
                    for (Variable key : keys) {
                        List<Variable> tmp = new ArrayList<>(keys);
                        Variable outKey = tmp.remove(i);
                        String attr = key.getName();
                        //curr_prefix = rootASTD.prefix(key.getRef()) + "." + attr;
                        String curr_prefix = ASTDTree.get(0).prefixTree(ASTDTree, key.getRef()) + "." + attr;

                        // mask the previous code
                        String prev_prefixREG = "";
                        if (!prev_prefix.isEmpty()) {
                            if(prev_prefix.contains("[")){
                                //previous prefix used in a quantified ASTD, need to replace [ and ] for \[ and \]
                                //with this replace, regex can find the previous prefix
                                prev_prefixREG = prev_prefix.replace("[", "\\[").replace("]", "\\]");
                            }
                            else{
                                prev_prefixREG = prev_prefix;
                            }

                            //change for the previous prefixes
                            for(int j = 0; (!previous_prefixes.isEmpty()) && j < previous_prefixes.size(); j++){
                                Pattern patternpreviousprefix = Pattern.compile("\\b"+previous_prefixesREG.get(j)+"\\b");
                                Matcher mpreviousprefix = patternpreviousprefix.matcher(paramsout);
                                paramsout = mpreviousprefix.replaceAll(String.valueOf(350000000 + j));
                            }

                            if(!prev_prefixREG.equals("")){
                                String axiomCopy = paramsout;
                                Pattern patternprefix = Pattern.compile("\\b"+prev_prefixREG+"\\b");
                                Matcher mprefix = patternprefix.matcher(paramsout);
                                paramsout = mprefix.replaceAll(String.valueOf(0x0EFFFF));
                                if(!paramsout.equals(axiomCopy) && prev_prefix.contains("[")){
                                    //this prefix changed the ASTD, quantified ASTD in use, need to add this prefix to the prefix list
                                    //adding to the list, means we can keep track of it and change accordingly when necessary
                                    previous_prefixes.add(prev_prefix);
                                    previous_prefixesREG.add(prev_prefixREG);
                                }
                            }

                            Pattern pattern2 = Pattern.compile("\\b" + Conventions.STRUCT_VAR + "\\b");
                            Matcher mParams2 = pattern2.matcher(paramsout);
                            paramsout = mParams2.replaceAll(String.valueOf(0x1EFFFF));

                            //it is also necessary to mask the ASTD names
                            paramsout = Utils.maskASTDNames(paramsout, astdNames);
                            paramsout = Utils.maskAttributes(paramsout, attributes, outKey);
                            paramsout = Utils.maskParams(paramsout, parameters, outKey);
                        }
                        if (!paramsout.contains(curr_prefix)) {
                            //Only change action if it doesn't contain the prefix.
                            Pattern pattern = Pattern.compile("\\b" + attr + "\\b");
                            Matcher mParams = pattern.matcher(paramsout);
                            paramsout = mParams.replaceAll(curr_prefix);
                        }
                        // unmask the previous code
                        if (!prev_prefix.isEmpty()) {
                            paramsout = paramsout.replace(String.valueOf(0x0EFFFF), prev_prefix);
                            paramsout = paramsout.replace(String.valueOf(0x1EFFFF), Conventions.STRUCT_VAR);
                            //unmaskASTDNames
                            paramsout = Utils.unmaskASTDNames(paramsout, astdNames);
                            paramsout = Utils.unmaskAttributes(paramsout, attributes, outKey);
                            paramsout = Utils.unmaskParams(paramsout, parameters, outKey);

                            //unmask prefixes
                            for(int j = 0; !previous_prefixes.isEmpty() && j < previous_prefixes.size(); j++){
                                Pattern patternpreviousprefix = Pattern.compile("\\b"+(350000000 + j)+"\\b");
                                Matcher mpreviousprefix = patternpreviousprefix.matcher(paramsout);
                                paramsout = mpreviousprefix.replaceAll(previous_prefixes.get(j));
                            }
                        }
                        prev_prefix = curr_prefix;
                        i++;
                    }
                }
                new_params += paramsout + ",";
            }
        }
        if (new_params.length() > 0)
            new_params = new_params.substring(0, new_params.length() - 1);
        else
            new_params = func_params;
        String func_name_out = func_name;
        String prev_prefix = "";
        int i = 0;
        if (keys != null) {

            List<String> previous_prefixes = new ArrayList<>();
            List<String> previous_prefixesREG = new ArrayList<>();

            for (Variable key : keys) {
                List<Variable> tmp = new ArrayList<>(keys);
                Variable outKey = tmp.remove(i);
                String attr = key.getName();
                //curr_prefix = rootASTD.prefix(key.getRef()) + "." + attr;
                String curr_prefix = ASTDTree.get(0).prefixTree(ASTDTree, key.getRef()) + "." + attr;
                // mask the previous code
                String prev_prefixREG = "";
                if (!prev_prefix.isEmpty()) {
                    if(prev_prefix.contains("[")){
                        //previous prefix used in a quantified ASTD, need to replace [ and ] for \[ and \]
                        //with this replace, regex can find the previous prefix
                        prev_prefixREG = prev_prefix.replace("[", "\\[").replace("]", "\\]");
                    }
                    else{
                        prev_prefixREG = prev_prefix;
                    }

                    //change for the previous prefixes
                    for(int j = 0; (!previous_prefixes.isEmpty()) && j < previous_prefixes.size(); j++){
                        Pattern patternpreviousprefix = Pattern.compile("\\b"+previous_prefixesREG.get(j)+"\\b");
                        Matcher mpreviousprefix = patternpreviousprefix.matcher(func_name_out);
                        func_name_out = mpreviousprefix.replaceAll(String.valueOf(350000000 + j));
                    }

                    if(!prev_prefixREG.equals("")){
                        String axiomCopy = func_name_out;
                        Pattern patternprefix = Pattern.compile("\\b"+prev_prefixREG+"\\b");
                        Matcher mprefix = patternprefix.matcher(func_name_out);
                        func_name_out = mprefix.replaceAll(String.valueOf(0x0EFFFF));
                        if(!func_name_out.equals(axiomCopy) && prev_prefix.contains("[")){
                            //this prefix changed the ASTD, quantified ASTD in use, need to add this prefix to the prefix list
                            //adding to the list, means we can keep track of it and change accordingly when necessary
                            previous_prefixes.add(prev_prefix);
                            previous_prefixesREG.add(prev_prefixREG);
                        }
                    }

                    Pattern pattern2 = Pattern.compile("\\b" + Conventions.STRUCT_VAR + "\\b");
                    Matcher mParams2 = pattern2.matcher(func_name_out);
                    func_name_out = mParams2.replaceAll(String.valueOf(0x1EFFFF));

                    //it is also necessary to mask the ASTD names
                    func_name_out = Utils.maskASTDNames(func_name_out, astdNames);
                    func_name_out = Utils.maskAttributes(func_name_out, attributes, outKey);
                    func_name_out = Utils.maskParams(func_name_out, parameters, outKey);
                    func_name_out = Utils.maskTimerFunctions(func_name_out, outKey);
                }
                if (!func_name_out.contains(curr_prefix)) {
                    //Only change action if it doesn't contain the prefix.
                    Pattern pattern = Pattern.compile("\\b" + attr + "\\b");
                    Matcher mParams = pattern.matcher(func_name_out);
                    func_name_out = mParams.replaceAll(curr_prefix);
                }
                // unmask the previous code
                if (!prev_prefix.isEmpty()) {
                    func_name_out = func_name_out.replace(String.valueOf(0x0EFFFF), prev_prefix);
                    func_name_out = func_name_out.replace(String.valueOf(0x1EFFFF), Conventions.STRUCT_VAR);
                    //unmaskASTDNames
                    func_name_out = Utils.unmaskASTDNames(func_name_out, astdNames);
                    func_name_out = Utils.unmaskAttributes(func_name_out, attributes, outKey);
                    func_name_out = Utils.unmaskParams(func_name_out, parameters, outKey);
                    func_name_out = Utils.unmaskTimerFunctions(func_name_out, outKey);

                    //unmask prefixes
                    for(int j = 0; !previous_prefixes.isEmpty() && j < previous_prefixes.size(); j++){
                        Pattern patternpreviousprefix = Pattern.compile("\\b"+(350000000 + j)+"\\b");
                        Matcher mpreviousprefix = patternpreviousprefix.matcher(func_name_out);
                        func_name_out = mpreviousprefix.replaceAll(previous_prefixes.get(j));
                    }
                }
                prev_prefix = curr_prefix;
                i++;
            }
        }
        func_name = func_name_out;
        if(new_params.equals("")){
            return func_name;
        }
        return func_name + "(" + new_params + ")";
    }

    /*
     * @brief  Returns the update code
     * @param  code
     * @return A string value
     */
    private String decodeCode(String code, ArrayList<ASTD> ASTDTree) {
        //parse assignment
        String code1 = code;
        List<String> actions = new ArrayList<>();
        AtomicReference<String> returnAction = new AtomicReference<>("");
        Matcher m1;
        m1 = Pattern.compile(Constants.ACTION_DIV, Pattern.DOTALL).matcher(code1);
        while(m1.find()){
            actions.add(m1.group());
        }
        actions.forEach(action -> {
            Matcher m2, m3;
            HashMap<String, String> mapString = new HashMap<>();
            HashMap<String, ArrayList<String>> mapFunctionsParams1 = new HashMap<>();
            HashMap<String,String> mapFunctionsParams2 = new HashMap<>();
            HashMap<String,String> mapFunctions = new HashMap<>();
            HashMap<String,String> mapFunctions2 = new HashMap<>();
            //mask everything that is text -> enclosed with "
            action = Utils.maskText(action, mapString);
            //mask and treat every function
            //first: functions with params
            m2 = Pattern.compile(Constants.FUNC_PARAMS).matcher(action); //function with params
            if(m2.find()){
                action = Utils.maskFunction(action, mapFunctionsParams1);
                for(Map.Entry<String, ArrayList<String>> entry : mapFunctionsParams1.entrySet()){
                    mapFunctionsParams2.put(entry.getKey(), updateFunctionCall(entry.getValue(), ASTDTree, mapFunctionsParams1, mapFunctionsParams2, mapFunctions, mapFunctions2));
                }
            }
            //second: functions without params
            m3 = Pattern.compile(Constants.FUNC_SANS_PARAMS).matcher(action); //function without params
            if(m3.find()){
                action = Utils.maskFunctionWithoutParams(action, mapFunctions);
                for(Map.Entry<String, String> entry : mapFunctions.entrySet()){
                    ArrayList<String> fake = new ArrayList<>();
                    fake.add(entry.getValue());
                    fake.add(null);
                    mapFunctions2.put(entry.getKey(), updateFunctionCall(fake, ASTDTree, mapFunctionsParams1, mapFunctionsParams2, mapFunctions, mapFunctions2));
                }
            }
            //treat what is left
            //mask code
            action = Utils.maskKeywords(action);
            action = Utils.maskQuotes(action);
            List<Variable> attributes = rootASTD.enclosingASTDVariables(ASTDTree);
            List<Variable> keys = new ArrayList<>();
            keys.addAll(attributes);
            List<Variable> params = rootASTD.enclosingASTDParams(ASTDTree);
            for(Variable param : params){
                boolean newName = true;
                for(Variable key : keys){
                    if(key.getName().equals(param.getName())){
                        newName = false;
                    }
                }
                if(newName){
                    keys.add(param);
                }
            }
            Collections.reverse(keys);
            ArrayList<ASTD> astdNames  = ASTDTree;
            String prev_prefix = "";

            List<String> previous_prefixes = new ArrayList<>();
            List<String> previous_prefixesREG = new ArrayList<>();

            int i = 0;
            if (keys != null) {
                for (Variable key : keys) {
                    List<Variable> tmp = new ArrayList<>(keys);
                    Variable out = tmp.remove(i);
                    String attr = key.getName();
                    //curr_prefix = rootASTD.prefix(key.getRef()) + "." + attr;
                    String curr_prefix = astdNames.get(0).prefixTree(astdNames, key.getRef()) + "." + attr;
                    // mask the previous code
                    String prev_prefixREG = "";
                    if (!prev_prefix.isEmpty()) {
                        if(prev_prefix.contains("[")){
                            //previous prefix used in a quantified ASTD, need to replace [ and ] for \[ and \]
                            //with this replace, regex can find the previous prefix
                            prev_prefixREG = prev_prefix.replace("[", "\\[").replace("]", "\\]");
                        }
                        else{
                            prev_prefixREG = prev_prefix;
                        }

                        //change for the previous prefixes
                        for(int j = 0; (!previous_prefixes.isEmpty()) && j < previous_prefixes.size(); j++){
                            Pattern patternpreviousprefix = Pattern.compile("\\b"+previous_prefixesREG.get(j)+"\\b");
                            Matcher mpreviousprefix = patternpreviousprefix.matcher(action);
                            action= mpreviousprefix.replaceAll(String.valueOf(350000000 + j));
                        }

                        if(!prev_prefixREG.equals("")){
                            String axiomCopy = action;
                            Pattern patternprefix = Pattern.compile("\\b"+prev_prefixREG+"\\b");
                            Matcher mprefix = patternprefix.matcher(action);
                            action = mprefix.replaceAll(String.valueOf(0x0EFFFF));
                            if(!action.equals(axiomCopy) && prev_prefix.contains("[")){
                                //this prefix changed the ASTD, quantified ASTD in use, need to add this prefix to the prefix list
                                //adding to the list, means we can keep track of it and change accordingly when necessary
                                previous_prefixes.add(prev_prefix);
                                previous_prefixesREG.add(prev_prefixREG);
                            }
                        }

                        Pattern pattern2 = Pattern.compile("\\b" + Conventions.STRUCT_VAR + "\\b");
                        Matcher mParams2 = pattern2.matcher(action);
                        action = mParams2.replaceAll(String.valueOf(0x1EFFFF));

                        //it is also necessary to mask the ASTD names
                        action = Utils.maskASTDNames(action, astdNames);
                        action = Utils.maskAttributes(action, attributes, out);
                        action = Utils.maskParams(action, params, out);
                    }

                    Pattern pattern = Pattern.compile("\\b" + attr + "\\b");
                    Matcher mParams = pattern.matcher(action);
                    action = mParams.replaceAll(curr_prefix);

                    // unmask the previous code
                    if (!prev_prefix.isEmpty()) {
                        action = action.replace(String.valueOf(0x0EFFFF), prev_prefix);
                        action = action.replace(String.valueOf(0x1EFFFF), Conventions.STRUCT_VAR);
                        //unmaskASTDNames
                        action = Utils.unmaskASTDNames(action, astdNames);
                        action = Utils.unmaskAttributes(action, attributes, out);
                        action = Utils.unmaskParams(action, params, out);

                        //unmask prefixes
                        for(int j = 0; !previous_prefixes.isEmpty() && j < previous_prefixes.size(); j++){
                            Pattern patternpreviousprefix = Pattern.compile("\\b"+(350000000 + j)+"\\b");
                            Matcher mpreviousprefix = patternpreviousprefix.matcher(action);
                            action = mpreviousprefix.replaceAll(previous_prefixes.get(j));
                        }
                    }
                    prev_prefix = curr_prefix;
                    i++;
                }
            }
            // unmask code
            action = Utils.unmaskKeywords(action);
            action = Utils.unmaskQuotes(action);

            //unmask text
            action = Utils.unmaskText(action, mapString);
            action = Utils.unmaskFunction(action, mapFunctionsParams2);
            action = Utils.unmaskFunctionWithoutParams(action, mapFunctions2);

            if(action.contains(".expired(") && Constants.TIMED_SIMULATION){
                final String regex = "(\\.expired)(\\(.+\\b)(\\))";
                final String subst = "$1$2, current_time$3";

                final Pattern pattern4 = Pattern.compile(regex, Pattern.MULTILINE);
                final Matcher matcher4 = pattern4.matcher(action);

                // The substituted value will be contained in the result variable
                action = (matcher4.replaceAll(subst));

                final String regex1 = "(\\.expired)(\\(\\S+\\b)(\\))";
                final String subst1 = "$1$2, current_time$3";

                final Pattern pattern5 = Pattern.compile(regex1, Pattern.MULTILINE);
                final Matcher matcher5 = pattern5.matcher(action);

                // The substituted value will be contained in the result variable
                action = (matcher5.replaceAll(subst1));
            }

            if(Constants.TIMED_SIMULATION && action.contains(".getPassedTime(")){
                final String regex = "(\\.getPassedTime)(\\()(\\))";
                final String subst = "$1$2current_time$3";

                final Pattern pattern5 = Pattern.compile(regex, Pattern.MULTILINE);
                final Matcher matcher5 = pattern5.matcher(action);

                // The substituted value will be contained in the result variable
                action = (matcher5.replaceAll(subst));
            }

            if(Constants.TIMED_SIMULATION && action.contains(".reset_clock(")){
                final String regex = "(\\.reset_clock)(\\()(\\))";
                final String subst = "$1$2current_time$3";

                final Pattern pattern6 = Pattern.compile(regex, Pattern.MULTILINE);
                final Matcher matcher6 = pattern6.matcher(action);

                // The substituted value will be contained in the result variable
                action = (matcher6.replaceAll(subst));
            }

            if(Constants.TIMED_SIMULATION){
                if(action.contains(Conventions.CST)){
                    final String regex = "(?<!\\.)(\\b"+Conventions.CST+"\\b)";
                    final String subst = Conventions.CURRENT_TIME;

                    final Pattern pattern6 = Pattern.compile(regex, Pattern.MULTILINE);
                    final Matcher matcher6 = pattern6.matcher(action);

                    // The substituted value will be contained in the result variable
                    action = (matcher6.replaceAll(subst));
                }
            }
            else{
                if(action.contains(Conventions.CST)){
                    final String regex = "(?<!\\.)(\\b"+Conventions.CST+"\\b)";
                    final String subst = Conventions.CST_VAR;

                    final Pattern pattern6 = Pattern.compile(regex, Pattern.MULTILINE);
                    final Matcher matcher6 = pattern6.matcher(action);

                    // The substituted value will be contained in the result variable
                    action = (matcher6.replaceAll(subst));
                }
            }

            returnAction.set(returnAction + action + ";");
        });

        return returnAction.get();
    }

    private String decodeGuardCode(String code, ArrayList<ASTD> ASTDTree) {
        //parse assignment
        String code1 = code;
        List<String> axioms = new ArrayList<>();
        final Pattern pattern = Pattern.compile(Constants.BOOLEAN_SEP);
        final Matcher matcher = pattern.matcher(code1);
        code1 = matcher.replaceAll(" $1 ");
        final Pattern pattern2 = Pattern.compile(Constants.BOOLEAN_SEP2);
        final Matcher matcher2 = pattern2.matcher(code1);
        code1 = matcher2.replaceAll(" $1 ");
        AtomicReference<String> returnAxioms = new AtomicReference<>("");
        Matcher m1;
        m1 = Pattern.compile(Constants.BOOLEAN_DIV, Pattern.DOTALL).matcher(code1);
        while(m1.find()){
            axioms.add(m1.group());
        }
        axioms.forEach(axiom -> {
            //mask everything that is enclosed with " (mask the text)
            Matcher m2, m3;
            HashMap<String, String> mapString = new HashMap<>();
            HashMap<String, ArrayList<String>> mapFunctionsParams1 = new HashMap<>();
            HashMap<String,String> mapFunctionsParams2 = new HashMap<>();
            HashMap<String,String> mapFunctions = new HashMap<>();
            HashMap<String,String> mapFunctions2 = new HashMap<>();
            //mask everything that is text -> enclosed with "
            axiom = Utils.maskText(axiom, mapString);
            //mask and treat every function
            //first functions with params
            m2 = Pattern.compile(Constants.FUNC_PARAMS).matcher(axiom); //function with params
            if(m2.find()){
                axiom = Utils.maskFunction(axiom, mapFunctionsParams1);
                for(Map.Entry<String, ArrayList<String>> entry : mapFunctionsParams1.entrySet()){
                    mapFunctionsParams2.put(entry.getKey(), updateFunctionCall(
                            entry.getValue(), ASTDTree,
                            mapFunctionsParams1, mapFunctionsParams2,
                            mapFunctions, mapFunctions2));
                }
            }
            //second functions without params
            m3 = Pattern.compile(Constants.FUNC_SANS_PARAMS).matcher(axiom); //function without params
            if(m3.find()){
                axiom = Utils.maskFunctionWithoutParams(axiom, mapFunctions);
                for(Map.Entry<String, String> entry : mapFunctions.entrySet()){
                    ArrayList<String> fake = new ArrayList<>();
                    fake.add(entry.getValue());
                    fake.add(null);
                    mapFunctions2.put(entry.getKey(), updateFunctionCall(
                            fake, ASTDTree,
                            mapFunctionsParams1, mapFunctionsParams2,
                            mapFunctions, mapFunctions2));
                }
            }
            //treat the rest
        //mask code
        axiom = Utils.maskKeywords(axiom);
        axiom = Utils.maskQuotes(axiom);
        List<Variable> attributes = rootASTD.enclosingASTDVariables(ASTDTree);
        List<Variable> keys = new ArrayList<>();
        keys.addAll(attributes);
        List<Variable> params = rootASTD.enclosingASTDParams(ASTDTree);
        for(Variable param : params){
            boolean newName = true;
            for(Variable key : keys){
                if(key.getName().equals(param.getName())){
                    newName = false;
                }
            }
            if(newName){
                keys.add(param);
            }
        }
        Collections.reverse(keys);
        ArrayList<ASTD> astdNames = new ArrayList<>(ASTDTree);
        List<String> previous_prefixes = new ArrayList<>();
        List<String> previous_prefixesREG = new ArrayList<>();
        String prev_prefix = "";
        int i = 0;
        if (keys != null) {
            for (Variable key : keys) {
                List<Variable> tmp = new ArrayList<>(keys);
                Variable out = tmp.remove(i);
                String attr = key.getName();
                //curr_prefix = rootASTD.prefix(key.getRef()) + "." + attr;
                String curr_prefix = astdNames.get(0).prefixTree(astdNames, key.getRef()) + "." + attr;
                // mask the previous code
                String prev_prefixREG = "";
                if (!prev_prefix.isEmpty()) {
                    if(prev_prefix.contains("[")){
                        //previous prefix used in a quantified ASTD, need to replace [ and ] for \[ and \]
                        //with this replace, regex can find the previous prefix
                        prev_prefixREG = prev_prefix.replace("[", "\\[").replace("]", "\\]");
                    }
                    else{
                        prev_prefixREG = prev_prefix;
                    }

                    //change for the previous prefixes
                    for(int j = 0; (!previous_prefixes.isEmpty()) && j < previous_prefixes.size(); j++){
                        Pattern patternpreviousprefix = Pattern.compile("\\b"+previous_prefixesREG.get(j)+"\\b");
                        Matcher mpreviousprefix = patternpreviousprefix.matcher(axiom);
                        axiom = mpreviousprefix.replaceAll(String.valueOf(350000000 + j));
                    }

                    if(!prev_prefixREG.equals("")){
                        String axiomCopy = axiom;
                        Pattern patternprefix = Pattern.compile("\\b"+prev_prefixREG+"\\b");
                        Matcher mprefix = patternprefix.matcher(axiom);
                        axiom = mprefix.replaceAll(String.valueOf(0x0EFFFF));
                        if(!axiom.equals(axiomCopy) && prev_prefix.contains("[")){
                            //this prefix changed the ASTD, quantified ASTD in use, need to add this prefix to the prefix list
                            //adding to the list, means we can keep track of it and change accordingly when necessary
                            previous_prefixes.add(prev_prefix);
                            previous_prefixesREG.add(prev_prefixREG);
                        }
                    }

                    Pattern pattern3 = Pattern.compile("\\b" + Conventions.STRUCT_VAR + "\\b");
                    Matcher mParams3 = pattern3.matcher(axiom);
                    axiom = mParams3.replaceAll(String.valueOf(0x1EFFFF));

                    //mask ASTD names
                    axiom = Utils.maskASTDNames(axiom, astdNames);
                    axiom = Utils.maskAttributes(axiom, attributes, out);
                    axiom = Utils.maskParams(axiom, params, out);
                }

                //change the code
                Pattern pattern4 = Pattern.compile("\\b" + attr + "\\b");
                Matcher mParams = pattern4.matcher(axiom);
                axiom = mParams.replaceAll(curr_prefix);

                // unmask the previous code
                if (!prev_prefix.isEmpty()) {
                    axiom = axiom.replace(String.valueOf(0x0EFFFF), prev_prefix);
                    axiom = axiom.replace(String.valueOf(0x1EFFFF), Conventions.STRUCT_VAR);
                    //unmask ASTD names
                    axiom = Utils.unmaskASTDNames(axiom, astdNames);
                    axiom = Utils.unmaskAttributes(axiom, attributes, out);
                    axiom = Utils.unmaskParams(axiom, params, out);

                    //unmask prefixes
                    for(int j = 0; !previous_prefixes.isEmpty() && j < previous_prefixes.size(); j++){
                        Pattern patternpreviousprefix = Pattern.compile("\\b"+(350000000 + j)+"\\b");
                        Matcher mpreviousprefix = patternpreviousprefix.matcher(axiom);
                        axiom = mpreviousprefix.replaceAll(previous_prefixes.get(j));
                    }

                }
                prev_prefix = curr_prefix;
                i++;
            }
        }
        // unmask code
        axiom = Utils.unmaskKeywords(axiom);
        axiom = Utils.unmaskQuotes(axiom);

        //unmask text
        axiom = Utils.unmaskText(axiom, mapString);
        axiom = Utils.unmaskFunction(axiom, mapFunctionsParams2);
        axiom = Utils.unmaskFunctionWithoutParams(axiom, mapFunctions2);

        if(axiom.contains(".expired(") && Constants.TIMED_SIMULATION){
            final String regex = "(\\.expired)(\\(.+\\b)(\\))";
            final String subst = "$1$2, current_time$3";

            final Pattern pattern4 = Pattern.compile(regex, Pattern.MULTILINE);
            final Matcher matcher4 = pattern4.matcher(axiom);

            // The substituted value will be contained in the result variable
            axiom = matcher4.replaceAll(subst);

            if(!axiom.contains(", current_time")){
                final String regex1 = "(\\.expired)(\\(\\S+\\b)(\\))";
                final String subst1 = "$1$2, current_time$3";

                final Pattern pattern5 = Pattern.compile(regex1, Pattern.MULTILINE);
                final Matcher matcher5 = pattern5.matcher(axiom);

                // The substituted value will be contained in the result variable
                axiom = (matcher5.replaceAll(subst1));
            }
        }

        if(Constants.TIMED_SIMULATION && axiom.contains(".getPassedTime(")){
            final String regex = "(\\.getPassedTime)(\\()(\\))";
            final String subst = "$1$2current_time$3";

            final Pattern pattern5 = Pattern.compile(regex, Pattern.MULTILINE);
            final Matcher matcher5 = pattern5.matcher(axiom);

            // The substituted value will be contained in the result variable
            axiom = (matcher5.replaceAll(subst));
        }

        if(Constants.TIMED_SIMULATION && axiom.contains(".reset_clock(")){
            final String regex = "(\\.reset_clock)(\\()(\\))";
            final String subst = "$1$2current_time$3";

            final Pattern pattern6 = Pattern.compile(regex, Pattern.MULTILINE);
            final Matcher matcher6 = pattern6.matcher(axiom);

            // The substituted value will be contained in the result variable
            axiom = (matcher6.replaceAll(subst));
        }
        if(axiom.contains("cst")){
            final String regex = "\\bcst\\b";
            final String subst;
            if(!Constants.TIMED_SIMULATION) {
                subst = Conventions.CST_VAR;
            }
            else{
                subst = Conventions.CURRENT_TIME;
            }
            final Pattern pattern7 = Pattern.compile(regex, Pattern.MULTILINE);
            final Matcher matcher7 = pattern7.matcher(axiom);

            // The substituted value will be contained in the result variable
            axiom = (matcher7.replaceAll(subst));
        }

        returnAxioms.set(returnAxioms + axiom + " ");
        });

        return returnAxioms.get();
    }

    /*
     * @brief  Prefix action code
     * @param  The action
     * @param  The prefix
     * @return The prefixed action
     */
    public Action prefixAction(Action _act, ArrayList<ASTD> ASTDTree) {
        Action act = (Action) Utils.copyObject(_act);
        if (act != null && act.getCode() != null && act.getCode().contains(ASTDParser.FILENAME_PROPERTY)) {
            act.setCode(Utils.readFile(Constants.CURRENT_PATH +
                    File.separator + act.getCode().split(":")[1]));
        } else if (act != null && act.getCode() != null && act.getCode().endsWith(".txt")) {
            act.setCode(Utils.readFile(Constants.CURRENT_PATH + File.separator + act.getCode()));
        }
        if (act != null && act.getCode() != null) {
            String code = act.getCode();
            act.setCode(decodeCode(code, ASTDTree));
        }
        return act;
    }

    /*
     * @brief  Prefix condition
     * @param  The condition
     * @param  The prefix
     * @return The prefixed condition
     */
    public String prefixGuard(String guard, ArrayList<ASTD> ASTDTree) {
        String guard1 = guard;
        if (guard1 != null && guard1.contains(ASTDParser.FILENAME_PROPERTY)) {
            guard1 = Utils.readFile(Constants.CURRENT_PATH + File.separator + guard1.split(":")[1]);
            guard1 = guard1.trim();
        } else if (guard1 != null && guard1.endsWith(".txt")) {
            guard1 = Utils.readFile(Constants.CURRENT_PATH + File.separator + guard1);
            guard1 = guard1.trim();
        }
        if (guard1 != null) {
            guard1 = decodeGuardCode(guard1, ASTDTree);
        }

        return guard1;
    }

    /*
     * @brief Generate the exists and forall function for a quantified ASTD in the IL model
     * @param  ASTD model
     * @return The main function
     */
    public abstract List<Function> trans_quantified_condition(Event e, Bool timed, List<Variable> varList, ArrayList<ASTD> CallList);

    public abstract List<Function> trans_quantified_condition_step(Event e, Bool timed, List<Variable> varList, ArrayList<ASTD> CallList);

    /*
     * @brief  Computes all event paramaters including quantified parameters
     * @param  Event
     * @return The list of parameters
     */

    public abstract List<Variable> getEvtParams(Event e);

    /*
     * @brief  Gets all quantfied ASTDs
     * @param
     * @return
     */
    public abstract List<QuantifiedASTD> findAllQASTDs();

    /* @brief sets the parent ASTD
     * @param the parent
     */
    public void setParent(ASTD parent) {
        rootASTD = parent;
    }

    public ASTD getParent() {
        return rootASTD;
    }

    /* @brief sets the parent ASTD
     * @param the parent
     */
    public void setRoot(boolean isRoot) {
        this.isRoot = isRoot;
    }

    /* @brief returns the parent ASTD
     * @param the parent
     */
    public boolean isRoot() {
        return isRoot;
    }

    /* @brief sets prefix mode
     * @param the mode
     */
    public void setPrefixModeEnabled(boolean mode) {
        isPrefixModeEnabled = mode;
    }

    /* @brief get prefix mode
     * @param the mode
     */
    public boolean getPrefixMode() {
        return isPrefixModeEnabled;
    }

    /* @brief kappa optimization
     * @param the analysis mode (direct, indirect)
     */
    public void kappaOptimize(KappaOptimizer.AnalysisMode kappaOpt) {
    }

    /*
     * @brief Mapping ASTD properties in the exec schema file
     * @param no parameter
     */
    public Statement propertyMapping() {
        List<Statement> stmtList = new ArrayList<>();
        if (isRoot) {
            // generate common properties
            List<String> commonProps = Arrays.asList(ExecSchemaParser.EXECUTED_EVENT, ExecSchemaParser.TOP_LEVEL_ASTD,
                    ExecSchemaParser.ATTRIBUTES, ExecSchemaParser.NAME,
                    ExecSchemaParser.TYPE, ExecSchemaParser.CURRENT_VALUE,
                    ExecSchemaParser.CURRENT_SUB_STATE);
            commonProps.forEach(prop -> {
                DeclStatement decStmt = new DeclStatement(new Constant(prop.toUpperCase(), Conventions.STRING,
                        prop, getName()));
                stmtList.add(decStmt);
            });
        }
        if (!stmtList.isEmpty())
            return new SeqStatement(stmtList);
        else
            return null;
    }

    /*
     * @brief Showing the current state of the ASTD in JSON format
     * @param no parameter
     */
    public abstract Statement currentStateToJson();

    public Statement topLevelStateToJson() {
        List<Statement> stmtList = new ArrayList<>();
        Utils.topLevelASTDIndex = Conventions.ARRAY_ELEM
                .replace(ILTranslator.USYMBOL_2, Conventions.EXEC_STATE_BUFFER)
                .replace(ILTranslator.USYMBOL_1, ExecSchemaParser.TOP_LEVEL_ASTD.toUpperCase());

        stmtList.add(new AssignStatement(new Term(Conventions.EXEC_STATE_BUFFER),
                new Term(Conventions.JSON_OBJECT_INSTANCE)));

        stmtList.add(new AssignStatement(new Term(Conventions.ARRAY_ELEM
                .replace(ILTranslator.USYMBOL_2, Conventions.EXEC_STATE_BUFFER)
                .replace(ILTranslator.USYMBOL_1, ExecSchemaParser.EXECUTED_EVENT.toUpperCase())),
                new Term(Conventions.EVENT_TEXT)));

        stmtList.add(fillJSONState(Utils.topLevelASTDIndex));
        if (!stmtList.isEmpty())
            return new SeqStatement(stmtList);
        else
            return null;
    }

    /*
     * @brief Just to fill the attributes and name of the current astd by its json prefix
     * @param no parameter
     * @return a statement
     */
    public Statement fillJSONState(String index) {

        List<Statement> stmtList = new ArrayList<>();
        stmtList.add(new AssignStatement(new Term(index),
                (!Conventions.isQSynchronization(this) || !Conventions.isQInterleaving(this))
                        ? new Term(Conventions.JSON_OBJECT_INSTANCE)
                        : new Term(Conventions.JSON_ARRAY_INSTANCE)));
        stmtList.add(new AssignStatement(
                new Term(Conventions.ARRAY_ELEM
                        .replace(ILTranslator.USYMBOL_2, index)
                        .replace(ILTranslator.USYMBOL_1, ExecSchemaParser.NAME.toUpperCase())),
                new Term("\"" + getName() + "\"")));

        stmtList.add(new AssignStatement(new Term(Conventions.ARRAY_ELEM.replace(ILTranslator.USYMBOL_2, index)
                .replace(ILTranslator.USYMBOL_1, ExecSchemaParser.ATTRIBUTES.toUpperCase())),
                new Term(Conventions.JSON_ARRAY_INSTANCE)));

        if (attributes != null && attributes.size() >= 1) {
            //TODO: CHANGE FOR PREFIX TREE
            String prfx = getParent().prefix(getName()) + ".";
            Variable v = attributes.get(0);

            if (isRoot()) {
                stmtList.add(new DeclStatement(new Variable(Conventions.TEMP_JSON_VAR_ATTRIBUTE,
                        Conventions.JSON,
                        Utils.jsonAttributeBuilder(prfx, v), this.getName())));
                Utils.attributeUpdated = true;
            } else {
                if (!Utils.attributeUpdated) {
                    stmtList.add(new DeclStatement(new Variable(Conventions.TEMP_JSON_VAR_ATTRIBUTE,
                            Conventions.JSON,
                            Utils.jsonAttributeBuilder(prfx, v), this.getName())));
                    Utils.attributeUpdated = true;
                } else {
                    stmtList.add(new AssignStatement(new Term(Conventions.TEMP_JSON_VAR_ATTRIBUTE),
                            new Term(Utils.jsonAttributeBuilder(prfx, v))));
                }
            }

            stmtList.add(new CallStatement(Conventions.ADD_ELEM_TO_VECTOR
                    .replace(ILTranslator.USYMBOL_2,
                            (Conventions.ARRAY_ELEM
                                    .replace(ILTranslator.USYMBOL_2, index)
                                    .replace(ILTranslator.USYMBOL_1,
                                            ExecSchemaParser.ATTRIBUTES.toUpperCase()))),
                    Arrays.asList(Conventions.TEMP_JSON_VAR_ATTRIBUTE)));

            attributes.forEach(attr -> {
                if (attr.getName() != v.getName()) {
                    stmtList.add(new AssignStatement(new Term(Conventions.TEMP_JSON_VAR_ATTRIBUTE),
                            new Term(Utils.jsonAttributeBuilder(prfx, attr))));
                    stmtList.add(new CallStatement(Conventions.ADD_ELEM_TO_VECTOR
                            .replace(ILTranslator.USYMBOL_2,
                                    (Conventions.ARRAY_ELEM
                                            .replace(ILTranslator.USYMBOL_2, index)
                                            .replace(ILTranslator.USYMBOL_1,
                                                    ExecSchemaParser.ATTRIBUTES.toUpperCase()))),
                            Arrays.asList(Conventions.TEMP_JSON_VAR_ATTRIBUTE)));
                }
            });
        }

        return new SeqStatement(stmtList);
    }

    /*
     * @brief update json indexes for sub-ASTDs
     * @param ToJson  a json index object
     * @return a list of string indexes
     */
    public abstract List<String> updateSubIndexes(ToJson obj);

    /*
     * @brief generates json index in the IL language
     * @return a json object index
     */
    public ToJson toJson() {
        return toJson;
    }

    /*
     * @brief updates json index in the IL language
     * @return
     */
    public void setToJson(ToJson toJson) {
        this.toJson = toJson;
    }

    @Override
    public List<Function> generateFinalFunc(ArrayList<ASTD> callList) { return null;}
}