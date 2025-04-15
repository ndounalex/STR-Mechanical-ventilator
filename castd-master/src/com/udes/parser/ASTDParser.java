package com.udes.parser;

import com.udes.model.astd.base.ASTD;
import com.udes.model.astd.base.BinaryASTD;
import com.udes.model.astd.base.QuantifiedASTD;
import com.udes.model.astd.base.UnaryASTD;
import com.udes.model.astd.items.*;
import com.udes.model.astd.types.*;
import com.udes.model.il.conventions.Conventions;
import com.udes.model.il.terms.Bool;
import com.udes.utils.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import com.udes.utils.Constants;
import org.json.simple.parser.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class ASTDParser implements Parser<ASTD> {

    public enum Type {
        JSONOBJECT,
        JSONARRAY,
        JSONSTRING,
        JSONNUMBER,
        JSONBOOLEAN,
        JSONNULL
    }

    private final String TLA_PROPERTY = "top_level_astds";
    private final String PARAM_PROPERTY = "parameters";
    private final String CALLARG_PROPERTY = "call_arguments";
    private final String NAME_PROPERTY = "name";
    private final String CALLNAME_PROPERTY = "called_astd_name";
    private final String TYPE_PROPERTY = "type";
    private final String TYPES_PROPERTY = "types";
    private final String TRANSLATOR_PROPERTY = "translator";
    private final String SCHEMAS_PROPERTY = "schemas";
    private final String TYPE_DEFINITIONS_PROPERTY = "type_definitions";
    private final String TA_PROPERTY = "typed_astd";
    private final String STATES_PROPERTY = "states";
    private final String ENTRY_PROPERTY = "entry_code";
    private final String STAY_PROPERTY = "stay_code";
    private final String EXIT_PROPERTY = "exit_code";
    private final String ASTD_PROPERTY = "astd";
    private final String ROOT_PROPERTY = "root";
    private final String CALL_PROPERTY = "root";
    private final String SUB_ASTD_PROPERTY = "sub_astd";
    private final String QUANTIFIED_ASTD_PROPERTY = "quantified_astd";
    private final String SYNCHRONIZATION_SET_PROPERTY = "synchronization_set";
    private final String INTERRUPT_ACTION = "interruptCode";
    private final String TIMEOUT_ACTION = "interruptCode";
    private final String LEFT_ASTD_PROPERTY = "left_astd";
    private final String RIGHT_ASTD_PROPERTY = "right_astd";

    private final String ONTCLASS_PROPERTY = "ontology";
    private final String IMPORT_PROPERTY = "imports";
    private final String ATTRIBUTES_PROPERTY = "attributes";
    private final String ASTD_ACTION_PROPERTY = "code";


    private final String TRANSITIONS_PROPERTY = "transitions";
    private final String ARROW_TYPE_PROPERTY = "arrow_type";
    private final String ARROW_PROPERTY = "arrow";
    private final String FROM_STATE_PROPERTY = "from_state_name";
    private final String TO_STATE_PROPERTY = "to_state_name";
    private final String THROUGH_STATE_PROPERTY = "through_state_name";


    private final String ET_PROPERTY = "event_template";
    private final String ET_PARAM_PROPERTY = "parameters";
    private final String ET_PARAM1_PROPERTY = "parameter";
    private final String ET_LABEL_PROPERTY = "label";
    private final String ET_PARAM_KIND_PROPERTY = "parameter_kind";
    private final String ET_PARAM_KIND_CAPTURE_PROPERTY = "Capture";
    private final String ET_PARAM_NAME_PROPERTY = "variable_name";

    private final String GUARD_PROPERTY = "guard";
    private final String DELAY_PROPERTY = "delay";
    private final String DELAY_UNIT_PROPERTY = "delay_unit";
    private final String TIMEOUT_DURATION = "duration";
    private final String TIMEOUT_DURATION_UNIT = "duration_unit";
    private final String WHEN_PROPERTY = "when";
    public static final String FILENAME_PROPERTY = "filename";
    private final String TRANSITION_ACTION_PROPERTY = "action";
    private final String TRANSITION_FINAL_PROPERTY = "from_final_state_only";

    private final String INITIAL_VALUE_PROPERTY = "initial_value";
    private final String INITIAL_STATE_PROPERTY = "initial_state_name";
    private final String SHALLOW_FINAL_STATES_PROPERTY = "shallow_final_state_names";
    private final String DEEP_FINAL_STATES_PROPERTY = "deep_final_state_names";
    private final String QUANTIFIED_VAR_PROPERTY = "quantified_variable";
    private final String DOMAIN_PROPERTY = "domain";
    private final String VALUE_PROPERTY = "value";
    private final String CONTENT_PROPERTY = "content";
    private final String ASTD_TYPE_CLASS_PATH = "com.udes.model.astd.types.";

    private final String VARIABLE_NAME = "Variable";
    private final String EXPRESSION_NAME = "Expression";

    public static ArrayList<Variable> parameters;
    public static Set<String> onto_classes;
    public static Set<String> type_defs;
    private ArrayList<Variable> attributes;

    private String jsonSpec;
    public static ASTD root;
    public static Map<String, ASTD> children;
    private static Map<String, String> changedCalls;
    public static boolean hasComplexType;
    public static boolean hasCallASTD;
    private String mainASTDName;
    private String mainCallASTDName;

    /*
     * @brief Gets the json specification
     * @param JSON specification
     */
    public ASTDParser(String jsonSpec, String mainASTDName) {
        if(mainASTDName == null || mainASTDName.isEmpty())
            this.mainASTDName = "MAIN";
        else
            this.mainASTDName = mainASTDName;
        this.jsonSpec = jsonSpec;
    }

    /*
     * @brief Parse top level ASTDs
     * @param  The node to parse
     * @param The key name to access values
     * @return
     */
    private ASTD parseRootASTD(JSONObject node, Bool timed) {

        Object nod = node.get(TLA_PROPERTY);
        ArrayList<ASTD> astdTree = new ArrayList<>();
        if(nod != null) {
            JSONArray tld_arr = (JSONArray) nod;
            if (tld_arr.size() > 1) {
                children = new HashMap<>();
                changedCalls = new HashMap<>();
                int mainASTDindex = 0;
                for(int i = 0; i < tld_arr.size(); i++) {
                    JSONObject curr_obj = (JSONObject) tld_arr.get(i);
                    String astdName = (String) curr_obj.get(NAME_PROPERTY);
                    if(mainASTDName.equals(astdName)){
                        //mainASTD has to be treated as last
                        mainASTDindex = i;
                    }
                    else {
                        try {
                            HashMap<String, String> oldChanged = (HashMap<String, String>) Utils.copyObject(changedCalls);
                            ASTD child = setupASTD(CALL_PROPERTY, curr_obj, null, timed, tld_arr, null);
                            valueChangeCalls(oldChanged, child);
                            children.put(child.getName(), child);
                        } catch (Exception e) {
                            if (Constants.DEBUG) e.printStackTrace();
                        }
                    }
                }
                // the last astd is the top level
                //IMPORTANT FOR ASTD TREE AND CALLS! IF CHANGED WE CAN CALL AN NON PARSED ASTD RESULTING IN ERROR.
                JSONObject curr_obj = (JSONObject) tld_arr.get(mainASTDindex);
                JSONObject root_obj = curr_obj;
                try {
                    HashMap<String, String> oldChanged = (HashMap<String, String>) Utils.copyObject(changedCalls);
                    root = setupASTD(ROOT_PROPERTY, root_obj, null, timed, tld_arr, astdTree);
                    parseParametersAsAttributes(root, root_obj);
                    valueChangeCalls(oldChanged, root);
                } catch (Exception e) {
                    if (Constants.DEBUG) e.printStackTrace();
                }
            } else {
                // it's a single astd
                JSONObject root_obj = (JSONObject) tld_arr.get(0);
                try {
                    root = setupASTD(ROOT_PROPERTY, root_obj, null, timed, tld_arr, astdTree);
                    parseParametersAsAttributes(root, root_obj);
                } catch (Exception e) {
                    if(Constants.DEBUG) e.printStackTrace();
                }
            }
        }
         return root;
    }


    /*
     * @brief Parse ontology classes
     * @param  The node to parse
     * @param The key name to access values
     * @return
     */
    private void parseOntologyTypes(JSONObject node) {
        Object _nod = node.get(ONTCLASS_PROPERTY);
        if(_nod != null) {
            Object types = ((JSONObject) _nod).get(TYPES_PROPERTY);
            if (types != null) {
                onto_classes = new HashSet<>();
                for (Object cls : (JSONArray) types) {
                    onto_classes.add((String) ((JSONObject) cls).get(TRANSLATOR_PROPERTY));
                }
                if (root != null)
                    root.setOntoClasses(onto_classes);
                if (Constants.DEBUG) System.out.println("Parse Ontology classes");
            }
        }
    }

    /*
     * @brief Parse import files
     * @param  The node to parse
     * @param The key name to access values
     * @return
     */
    private void parseImports(ASTD astd, JSONObject node) {
        Object nod = node.get(IMPORT_PROPERTY);
        if(nod != null) {
            // imports
            Set<String> imports = new HashSet<>();
            for (Object imprt : (JSONArray) nod) {
                imports.add((String) imprt);
            }
            astd.setImports(imports);
            astd.setImports(imports);
            if (Constants.DEBUG) System.out.println("Parse Imports");
        }
    }

    /*
     * @brief Parse type definitions
     * @param  The node to parse
     * @param The key name to access values
     * @return
     */
    private void parseTypeDefinitions(ASTD astd, JSONObject node) {
        Object nod = node.get(TYPE_DEFINITIONS_PROPERTY);

        if(nod != null) {
            Object sub_nod = ((JSONObject)nod).get(SCHEMAS_PROPERTY);
            if(sub_nod != null) {
                type_defs = new HashSet<>();
                // types
                File f;
                for (Object type_def : (JSONArray) sub_nod) {
                    String td = (String) type_def;
                    if(td != null) {
                        if (td.contains(Constants.CURRENT_PATH))
                            f = new File(td);
                        else
                            f = new File(Constants.CURRENT_PATH + File.separator + td);

                        if (f.exists())
                            type_defs.add(Utils.capitalize(f.getName()
                                     .replace("." + Constants.JSON_FORMAT, "")
                                     .replace("_" + Constants.SCHEMA, "")));
                        else {
                            String[] out = td.split("\\\\");
                            if (out != null) {
                                if (out.length == 1) td = out[0];
                                if (out.length  > 1) td = out[out.length-1];
                                type_defs.add(Utils.capitalize(td
                                            .replace("." + Constants.JSON_FORMAT, "")
                                            .replace("_" + Constants.SCHEMA, "")));
                            }
                        }
                    }
                }
                //
                astd.setTypeDefs(type_defs);
                if (Constants.DEBUG) System.out.println("Parse Type Defs");
            }
        }
    }

    /*
     * @brief Parse attributes
     * @param  The node to parse
     * @param The key name to access values
     * @return
     */
    private void parseAttributes(ASTD astd, JSONObject node){
        if (node != null) {
            Object nod = node.get(ATTRIBUTES_PROPERTY);
            if(nod != null) {
                Set<Variable> attributes = new HashSet<>();
                for (Object obj : (JSONArray) nod) {
                    JSONObject attr = (JSONObject) obj;
                    if (attr != null) {
                        String type = (String) attr.get(TYPE_PROPERTY);
                        if(!type.contains(Conventions.INT)
                                && !type.contains(Conventions.STRING)
                                && !type.contains(Conventions.BOOL_TYPE1)
                                && !type.contains(Conventions.DOUBLE)
                                && !type.contains(Conventions.FLOAT)
                                && !type.contains(Conventions.SHORT)
                                && !type.contains(Conventions.LONG)) {
                            type = Utils.capitalize(type);
                        }
                        attributes.add(new Variable((String) attr.get(NAME_PROPERTY), type,
                                JSONValue.escape(attr.get(INITIAL_VALUE_PROPERTY).toString()),
                                astd.getName()));
                    }
                }
                if(this.attributes == null)
                    this.attributes = new ArrayList<>(attributes);
                else
                    this.attributes.addAll(attributes);

                astd.setAttributes(new ArrayList<>(attributes));
                if (Constants.DEBUG) System.out.println("Parse Attributes");
            }
        }
    }

    /*
     * @brief Parse attributes
     * @param  The node to parse
     * @param The key name to access values
     * @return
     */
    private void parseParametersAsAttributes(ASTD astd, JSONObject node){
        if (node != null) {
            Object nod = node.get(PARAM_PROPERTY);
            if(nod != null) {
                Set<Variable> attributes = new HashSet<>();
                for (Object obj : (JSONArray) nod) {
                    JSONObject attr = (JSONObject) obj;
                    if (attr != null) {
                        String type = (String) attr.get(TYPE_PROPERTY);
                        if(!type.contains(Conventions.INT)
                                && !type.contains(Conventions.STRING)
                                && !type.contains(Conventions.BOOL_TYPE1)
                                && !type.contains(Conventions.DOUBLE)
                                && !type.contains(Conventions.FLOAT)
                                && !type.contains(Conventions.SHORT)
                                && !type.contains(Conventions.LONG)) {
                            type = Utils.capitalize(type);
                        }
                        attributes.add(new Variable((String) attr.get(NAME_PROPERTY), type,
                                JSONValue.escape((String) attr.get(NAME_PROPERTY)),
                                astd.getName()));
                    }
                }
                attributes.addAll(astd.getAttributes());
                astd.setAttributes(new ArrayList<>(attributes));
                if (Constants.DEBUG) System.out.println("Parse Attributes");
            }
        }
    }

    private void parseAttributesTimeout(ASTD astd, JSONObject node){
        if(node != null){
            Object nod = node.get(ATTRIBUTES_PROPERTY);
            Set<Variable> attributes = new HashSet<>();
            if(nod != null) {
                for (Object obj : (JSONArray) nod) {
                    JSONObject attr = (JSONObject) obj;
                    if (attr != null) {
                        String type = (String) attr.get(TYPE_PROPERTY);
                        if(!type.contains(Conventions.INT)
                                && !type.contains(Conventions.STRING)
                                && !type.contains(Conventions.BOOL_TYPE1)
                                && !type.contains(Conventions.DOUBLE)
                                && !type.contains(Conventions.FLOAT)
                                && !type.contains(Conventions.SHORT)
                                && !type.contains(Conventions.LONG)) {
                            type = Utils.capitalize(type);
                        }
                        attributes.add(new Variable((String) attr.get(NAME_PROPERTY), type,
                                JSONValue.escape(attr.get(INITIAL_VALUE_PROPERTY).toString()),
                                astd.getName()));
                    }
                }
            }
            attributes.add(new Variable("startedState", Conventions.BOOL_TYPE1, JSONValue.escape("false"), astd.getName()));
            if(this.attributes == null)
                this.attributes = new ArrayList<>(attributes);
            else
                this.attributes.addAll(attributes);

            astd.setAttributes(new ArrayList<>(attributes));
            if (Constants.DEBUG) System.out.println("Parse Attributes");
        }

    }

    private void parseAttributesTimedInterrupt(ASTD astd, JSONObject node){
        if(node != null){
            Object nod = node.get(ATTRIBUTES_PROPERTY);
            Set<Variable> attributes = new HashSet<>();
            if(nod != null) {
                for (Object obj : (JSONArray) nod) {
                    JSONObject attr = (JSONObject) obj;
                    if (attr != null) {
                        String type = (String) attr.get(TYPE_PROPERTY);
                        if(!type.contains(Conventions.INT)
                                && !type.contains(Conventions.STRING)
                                && !type.contains(Conventions.BOOL_TYPE1)
                                && !type.contains(Conventions.DOUBLE)
                                && !type.contains(Conventions.FLOAT)
                                && !type.contains(Conventions.SHORT)
                                && !type.contains(Conventions.LONG)) {
                            type = Utils.capitalize(type);
                        }
                        attributes.add(new Variable((String) attr.get(NAME_PROPERTY), type,
                                JSONValue.escape(attr.get(INITIAL_VALUE_PROPERTY).toString()),
                                astd.getName()));
                    }
                }
            }
            attributes.add(new Variable(Conventions.CLOCK_TIMED_INTERRUPT, Conventions.CLOCK, JSONValue.escape("0"), astd.getName()));
            //attributes.add(new Variable(Conventions.CLOCK_TIMED_INTERRUPT, Conventions.TIME_TYPE, null, astd.getName()));
            if(this.attributes == null)
                this.attributes = new ArrayList<>(attributes);
            else
                this.attributes.addAll(attributes);

            astd.setAttributes(new ArrayList<>(attributes));
            if (Constants.DEBUG) System.out.println("Parse Attributes");
        }

    }

    /*
     * @brief Parse quantified variables
     * @param  The node to parse
     * @param The key name to access values
     * @return
     */
    private void parseQVariable(QuantifiedASTD qastd, JSONObject node) {
        Object nod = node.get(QUANTIFIED_VAR_PROPERTY);
        if(nod != null) {
            JSONObject obj_qvar = (JSONObject) nod;
            Domain dom = retrieveDomainById(obj_qvar);
            Variable qvariable = new Variable((String) obj_qvar.get(NAME_PROPERTY),
                                              (String) obj_qvar.get(TYPE_PROPERTY),
                                              null, qastd.getName());
            qastd.setQvariable(qvariable);
            if(dom != null) {
                Constants.DOM_TYPE = dom.getType();
            }
            else {
                dom = new Domain(Constants.UNBOUNDEDDOMAIN, null);
                Constants.DOM_TYPE = Constants.UNBOUNDEDDOMAIN;
            }
            qastd.setDomain(dom);
            if (Constants.DEBUG) System.out.println("Parse QVariables");
        }
    }

     /*
     * @brief Parse qsynchronization set
     * @param  The node to parse
     * @param The key name to access values
     * @return
     */
    private void parseQSynchronizationSet(QSynchronization qastd, JSONObject node) {
        Object nod = node.get(SYNCHRONIZATION_SET_PROPERTY);
        if(nod != null) {
            List<String> delta = new ArrayList<>();
            for (Object obj : (JSONArray) nod) {
                delta.add((String)obj);
            }
            if(!delta.isEmpty())
                qastd.setDelta(delta);
            if (Constants.DEBUG) System.out.println("Parse QSynchronization Set");
        }
    }

    /*
     * @brief Parse qparallelcomposition set
     * @param  The node to parse
     * @param The key name to access values
     * @return
     */
    private void parseQParallelCompositionSet(QSynchronization qastd, JSONObject node) {
        List<String> delta = new ArrayList<>();
        Set<Event> evts = qastd.findAllEvents();
        evts.forEach(evt -> {delta.add(evt.getName());});
        if (!delta.isEmpty()){
            qastd.setDelta(delta);
        }
        if(Constants.DEBUG) System.out.println("Parse QParallelComposition Set");
    }

    /*
     * @brief Parse synchronization set
     * @param  The node to parse
     * @param The key name to access values
     * @return
     */
    private void parseSynchronizationSet(Synchronization bastd, JSONObject node) {
        Object nod = node.get(SYNCHRONIZATION_SET_PROPERTY);
        if(nod != null) {
            List<String> delta = new ArrayList<>();
            for (Object obj : (JSONArray) nod) {
                delta.add((String)obj);
            }
            if(!delta.isEmpty())
                bastd.setDelta(delta);
            if (Constants.DEBUG) System.out.println("Parse Synchronization Set");
        }
        else{
            List<String> delta = new ArrayList<>();
            delta.add("");
            bastd.setDelta(delta);
        }
    }

    private void parseInterruptAction(Interrupt bastd, JSONObject node){
        Object action = node.get(INTERRUPT_ACTION);
        if(action != null) {
            Action interrupt_action = null;
            if (action instanceof String) {
                String str_action = (String) action;
                if(!str_action.isEmpty())
                    interrupt_action = new Action(str_action.replace("{", "")
                            .replace("}", ""));
            } else {
                JSONObject action_obj = (JSONObject) action;
                interrupt_action = new Action(FILENAME_PROPERTY + ":" + (String) action_obj.get(FILENAME_PROPERTY));
            }
            bastd.setInterruptAction(interrupt_action);
            if(Constants.DEBUG) System.out.println("Parse Interrupt Action");
        }
    }

    private void parseTimeoutAction(Interrupt bastd, JSONObject node){
        Object action = node.get(TIMEOUT_ACTION);
        if(action != null) {
            Action interrupt_action = null;
            if (action instanceof String) {
                String str_action = (String) action;
                if(!str_action.isEmpty())
                    interrupt_action = new Action(str_action.replace("{", "")
                            .replace("}", ""));
            } else {
                JSONObject action_obj = (JSONObject) action;
                interrupt_action = new Action(FILENAME_PROPERTY + ":" + (String) action_obj.get(FILENAME_PROPERTY));
            }
            bastd.setInterruptAction(interrupt_action);
            if(Constants.DEBUG) System.out.println("Parse Interrupt Action");
        }
    }

    /*
     * @brief Parse ASTD parameters
     * @param The astd object
     * @param  The node to parse
     * @return
     */
    private void parseParameters(ASTD astd, JSONObject node) {
        if (node != null){
            Object params = node.get(PARAM_PROPERTY);
            if(params != null) {
                parameters = new ArrayList<>();
                for (Object obj : (JSONArray) params) {
                    JSONObject param = (JSONObject) obj;
                    Domain dom = retrieveDomainById(param);
                    String type = (String) param.get(TYPE_PROPERTY);
                    if(dom != null) {
                        if(!type.contains(Conventions.INT)
                                && !type.contains(Conventions.STRING)
                                && !type.contains(Conventions.BOOL_TYPE1)
                                && !type.contains(Conventions.DOUBLE)
                                && !type.contains(Conventions.FLOAT)
                                && !type.contains(Conventions.SHORT)
                                && !type.contains(Conventions.LONG)) {
                            type = Utils.capitalize(type);
                        }
                        parameters.add(new Variable((String) param.get(NAME_PROPERTY),
                                type, dom, astd.getName()));
                    }
                    else {
                        parameters.add(new Variable((String) param.get(NAME_PROPERTY),
                                type, null, astd.getName()));
                    }
                }
                astd.setParams(parameters);
            }
        }
    }

    /*
     * @brief Parse elementary and complex states
     * @param  The node to parse
     * @param The key name to access values
     * @return
     */
    private void parseComplexStates(Automaton astd, JSONObject node, Bool timed, JSONArray objArray, ArrayList<ASTD> astdTree) {
        Object nod = node.get(STATES_PROPERTY);
        if(nod != null) {
            Set<String> stateNames = new HashSet<>();
            Map<String, ASTD> stateToASTDs = new HashMap<>();
            Map<String, ActionSet> stateToActions =  new HashMap<>();
            for (Object obj : (JSONArray) nod) {
                JSONObject state = (JSONObject) obj;
                if (state != null) {
                    String stateName = (String) state.get(NAME_PROPERTY);
                    String entryCode = (String) state.get(ENTRY_PROPERTY),
                           stayCode  = (String) state.get(STAY_PROPERTY),
                           exitCode  =  (String) state.get(EXIT_PROPERTY);
                    ActionSet actSet = new ActionSet();
                    if(entryCode != null && !entryCode.isEmpty())
                        actSet.setEntry(new Action(entryCode.replace("{", "")
                                                            .replace("}", "")));
                    if(stayCode != null && !stayCode.isEmpty())
                        actSet.setStay(new Action(stayCode.replace("{", "")
                                                          .replace("}", "")));
                    if(exitCode != null && !exitCode.isEmpty())
                        actSet.setExit(new Action(exitCode.replace("{", "")
                                                          .replace("}", "")));
                    stateNames.add(stateName);
                    JSONObject objState = (JSONObject) state.get(ASTD_PROPERTY);
                    if (objState != null) {
                        String astdType = (String) objState.get(TYPE_PROPERTY);
                        if (astdType.compareToIgnoreCase(Elem.class.getSimpleName()) != 0) {
                            // do operation on the child
                            try {
                                HashMap<String, String> oldChanged = (HashMap<String, String>) Utils.copyObject(changedCalls);
                                ASTD stateASTD = setupASTD(SUB_ASTD_PROPERTY, objState, stateName, timed, objArray, astdTree);
                                String newName = valueChangeCalls(oldChanged, astd);
                                if(newName != null){
                                    stateToASTDs.put(newName, stateASTD);
                                    if(stateNames.contains(stateName)){
                                        stateNames.remove(stateName);
                                        stateName = newName;
                                        stateNames.add(newName);
                                    }
                                }
                                else{
                                    stateToASTDs.put(stateName, stateASTD);
                                }
                            }catch(Exception e){
                                e.printStackTrace();
                            }
                        }
                        stateToActions.put(stateName, actSet);
                    }
                }
            }
            astd.setStateNames(new ArrayList<>(stateNames));
            astd.setStatesToASTDs(stateToASTDs);
            astd.setStatesToActions(stateToActions);
            if (Constants.DEBUG) System.out.println("Parse States");
        }
    }

    /*
     * @brief Parse elementary and complex states
     * @param  The node to parse
     * @param The key name to access values
     * @return
     */
    private void parseComplexStatesTimeout(Automaton astd, JSONObject rightASTD, Bool timed, JSONArray objArray, ArrayList<ASTD> astdTree) {
        Set<String> stateNames = new HashSet<>();
        Map<String, ASTD> stateToASTDs = new HashMap<>();
        Map<String, ActionSet> stateToActions =  new HashMap<>();

        //S0, type = "Elem" typed_ASTD = {} entry_code/stay_code/exit_code = ""
        stateNames.add("S0");
        stateToActions.put("S0", new ActionSet());
        JSONObject stateForS0 = new JSONObject();
        stateForS0.put("entry_code","");
        stateForS0.put("stay_code","");
        stateForS0.put("exit_code","");
        JSONObject astdForS0 = new JSONObject();
        astdForS0.put("type", "Elem");
        astdForS0.put("typed_astd", new JSONObject());
        stateForS0.put("astd", astdForS0);
        stateForS0.put("name", "S0");

        //rightASTD
        stateNames.add((String) rightASTD.get("name"));
        JSONObject objState = rightASTD;
        String astdType = (String) objState.get(TYPE_PROPERTY);
        if (astdType.compareToIgnoreCase(Elem.class.getSimpleName()) != 0) {
            // do operation on the child
            try {
                HashMap<String, String> oldChanged = (HashMap<String, String>) Utils.copyObject(changedCalls);
                ASTD stateASTD = setupASTD(SUB_ASTD_PROPERTY, objState, (String) rightASTD.get("name"), timed, objArray, astdTree);
                String newName = valueChangeCalls(oldChanged, astd);
                if(newName != null){
                    stateToASTDs.put(newName, stateASTD);
                }
                else{
                    stateToASTDs.put((String) rightASTD.get("name"), stateASTD);
                }
            }catch(Exception e){
                e.printStackTrace();
                }
        }
        stateToActions.put((String) rightASTD.get("name"), new ActionSet());

        //adding them to the astd
        astd.setStateNames(new ArrayList<>(stateNames));
        astd.setStatesToASTDs(stateToASTDs);
        astd.setStatesToActions(stateToActions);
        if (Constants.DEBUG) System.out.println("Parse States");
    }

    /*
     * @brief Parse transitions if exists
     * @param  The node to parse
     * @return
     */
    private void parseTransitions(Automaton astd, JSONObject node, Bool timed) {
        Object nod = node.get(TRANSITIONS_PROPERTY);
        if(nod != null) {
            Set<String> eventNames = new HashSet<>();
            Set<Transition> transitions = new HashSet<>();
            for (Object obj : (JSONArray) nod) {
                JSONObject trans_obj = (JSONObject) obj;
                String arr_type = (String) trans_obj.get(ARROW_TYPE_PROPERTY);
                JSONObject arr_obj = (JSONObject) trans_obj.get(ARROW_PROPERTY);
                Transition trans = new Transition();
                setArrowById(arr_type, arr_obj, trans);
                setEventFormatById(astd, trans_obj, trans, timed);
                setGuardById(trans_obj, trans);
                setFinalStateById(trans_obj, trans);
                setTransitionActionById(trans_obj, trans);
                eventNames.add(trans.getEvent().getName());
                transitions.add(trans);
            }
            astd.setEventNames(new ArrayList<>(eventNames));
            astd.setTransitions(new ArrayList<>(transitions));
            if (Constants.DEBUG) System.out.println("Parse Transitions");
        }
    }

    /*
     * @brief Parse transitions if exists
     * @param  The node to parse
     * @return
     */
    private void parseTransitionsTimeout(Automaton astd, JSONObject node, Bool timed) {
        Set<String> eventNames = new HashSet<>();
        Set<Transition> transitions = new HashSet<>();
        JSONObject trans_obj = new JSONObject();
        String arr_type = "Local";
        JSONObject arr_obj = new JSONObject();
        arr_obj.put("from_state_name", "S0");
        arr_obj.put("to_state_name", node.get("name"));
        Transition trans = new Transition();
        setArrowById(arr_type, arr_obj, trans);
        trans_obj.put("arrow", arr_obj);
        trans_obj.put("guard", "");
        trans_obj.put("arrow_type", "Local");
        trans_obj.put("action", "");
        trans_obj.put("step", "true");
        trans_obj.put("from_final_state_only", false);

        //event_template
        JSONObject eventTemplate_obj = new JSONObject();
        eventTemplate_obj.put("label", "Step");
        eventTemplate_obj.put("parameters", null);
        eventTemplate_obj.put("when", null);
        trans_obj.put("event_template", eventTemplate_obj);

        setEventFormatById(astd, trans_obj, trans, timed);
        setGuardById(trans_obj, trans);
        setFinalStateById(trans_obj, trans);
        setTransitionActionById(trans_obj, trans);
        eventNames.add(trans.getEvent().getName());
        transitions.add(trans);
        astd.setEventNames(new ArrayList<>(eventNames));
        astd.setTransitions(new ArrayList<>(transitions));
        if (Constants.DEBUG) System.out.println("Parse Transitions");
    }

    /*
     * @brief Parse the condition of the guard ASTD
     * @param  The node to parse
     * @return
     */
    private void parseGuardInfo(Guard astd, JSONObject node) {

        Object guard = node.get(GUARD_PROPERTY);
        if(guard != null) {
            if (guard instanceof String) {
                astd.setGuard(new Bool((String) guard));
            } else {
                JSONObject guard_obj = (JSONObject) guard;
                astd.setGuard(new Bool(FILENAME_PROPERTY + ":" + (String) guard_obj.get(FILENAME_PROPERTY)));
            }
            if(Constants.DEBUG) System.out.println("Parse Guard");
        }
    }

    private void parseGuardInfoTimeoutLeft(Guard astd, JSONObject node) {
        String value = node.get(TIMEOUT_DURATION).toString();
        Object guard;
        try{ //to get a number
            double valueInDouble = Double.parseDouble(value);
            Object delay_unit = node.get(TIMEOUT_DURATION_UNIT);
            if(!delay_unit.toString().equals("nanoseconds")){
                guard = valueInDouble * getInNanoseconds(delay_unit);
            }
            else{
                guard = valueInDouble;
            }
        } catch(NumberFormatException e){
            //it is not a number, it is a parameter
            Object delay_unit = node.get(TIMEOUT_DURATION_UNIT);
            if(!delay_unit.toString().equals("nanoseconds")){
                guard = "(" + value + ")" + "*" + getInNanoseconds(delay_unit);
            }
            else{
                guard = value;
            }
        }
        astd.setGuard(new Bool(Conventions.LETS + "." + Conventions.EXPIRED + "("+guard+")" + " == false && " + "startedState == false"));
        if(Constants.DEBUG) System.out.println("Parse Guard");
    }

    private void parseGuardInfoTimeout(Guard astd, JSONObject node) {
        String value = node.get(TIMEOUT_DURATION).toString();
        Object guard;
        try{ //to get a number
            double valueInDouble = Double.parseDouble(value);
            Object delay_unit = node.get(TIMEOUT_DURATION_UNIT);
            if(!delay_unit.toString().equals("nanoseconds")){
                guard = valueInDouble * getInNanoseconds(delay_unit);
            }
            else{
                guard = valueInDouble;
            }
        } catch(NumberFormatException e){
            //it is not a number, it is a parameter
            Object delay_unit = node.get(TIMEOUT_DURATION_UNIT);
            if(!delay_unit.toString().equals("nanoseconds")){
                guard =  "(" + value + ")" + "*" + getInNanoseconds(delay_unit);
            }
            else{
                guard = value;
            }
        }
        astd.setGuard(new Bool(Conventions.LETS + "." + Conventions.EXPIRED + "("+guard+")" + " && " + "startedState == false"));
        if(Constants.DEBUG) System.out.println("Parse Guard");
    }

    private void parseGuardInfoPersistentTimeoutLeft(PersistentGuard astd, JSONObject node) {
        String value = node.get(TIMEOUT_DURATION).toString();
        Object guard;
        try{ //to get a number
            double valueInDouble = Double.parseDouble(value);
            Object delay_unit = node.get(TIMEOUT_DURATION_UNIT);
            if(!delay_unit.toString().equals("nanoseconds")){
                guard = valueInDouble * getInNanoseconds(delay_unit);
            }
            else{
                guard = valueInDouble;
            }
        } catch(NumberFormatException e){
            //it is not a number, it is a parameter
            Object delay_unit = node.get(TIMEOUT_DURATION_UNIT);
            if(!delay_unit.toString().equals("nanoseconds")){
                guard =  "(" + value + ")" + "*" + getInNanoseconds(delay_unit);
            }
            else{
                guard = value;
            }
        }
        astd.setGuard(new Bool(Conventions.LETS + "." + Conventions.EXPIRED + "("+guard+") == false"));
        if(Constants.DEBUG) System.out.println("Parse Guard");
    }

    private void parseGuardInfoPersistentTimeout(Guard astd, JSONObject node) {
        String value = node.get(TIMEOUT_DURATION).toString();
        Object guard;
        try{ //to get a number
            double valueInDouble = Double.parseDouble(value);
            Object delay_unit = node.get(TIMEOUT_DURATION_UNIT);
            if(!delay_unit.toString().equals("nanoseconds")){
                guard = valueInDouble * getInNanoseconds(delay_unit);
            }
            else{
                guard = valueInDouble;
            }
        } catch(NumberFormatException e){
            //it is not a number, it is a parameter
            Object delay_unit = node.get(TIMEOUT_DURATION_UNIT);
            if(!delay_unit.toString().equals("nanoseconds")){
                guard =  "(" + value + ")" + "*" + getInNanoseconds(delay_unit);
            }
            else{
                guard = value;
            }
        }
        astd.setGuard(new Bool(Conventions.LETS + "." + Conventions.EXPIRED + "("+guard+")"));
        if(Constants.DEBUG) System.out.println("Parse Guard");
    }

    private void parseGuardInfoTimedInterrupt(Guard astd, JSONObject node) {
        String value = node.get(TIMEOUT_DURATION).toString();
        Object guard;
        try{ //to get a number
            double valueInDouble = Double.parseDouble(value);
            Object delay_unit = node.get(TIMEOUT_DURATION_UNIT);
            if(!delay_unit.toString().equals("nanoseconds")){
                guard = valueInDouble * getInNanoseconds(delay_unit);
            }
            else{
                guard = valueInDouble;
            }
        } catch(NumberFormatException e){
            //it is not a number, it is a parameter
            Object delay_unit = node.get(TIMEOUT_DURATION_UNIT);
            if(!delay_unit.toString().equals("nanoseconds")){
                guard =  "(" + value + ")" + "*" + getInNanoseconds(delay_unit);
            }
            else{
                guard = value;
            }
        }
        astd.setGuard(new Bool(Conventions.CLOCK_TIMED_INTERRUPT + "." + Conventions.EXPIRED + "("+guard+")"));
        if(Constants.DEBUG) System.out.println("Parse Guard");
    }

    private void parseGuardInfoTimedInterruptLeft(PersistentGuard astd, JSONObject node) {
        String value = node.get(TIMEOUT_DURATION).toString();
        Object guard;
        try{ //to get a number
            double valueInDouble = Double.parseDouble(value);
            Object delay_unit = node.get(TIMEOUT_DURATION_UNIT);
            if(!delay_unit.toString().equals("nanoseconds")){
                guard = valueInDouble * getInNanoseconds(delay_unit);
            }
            else{
                guard = valueInDouble;
            }
        } catch(NumberFormatException e){
            //it is not a number, it is a parameter
            Object delay_unit = node.get(TIMEOUT_DURATION_UNIT);
            if(!delay_unit.toString().equals("nanoseconds")){
                guard =  "(" + value + ")" + "*" + getInNanoseconds(delay_unit);
            }
            else{
                guard = value;
            }
        }
        astd.setGuard(new Bool(Conventions.LETS + "." + Conventions.EXPIRED + "("+guard+") == false"));
        if(Constants.DEBUG) System.out.println("Parse Guard");
    }

    private double getInNanoseconds(Object unit){
        double nano = 1;
        if(unit.toString().equals("Second")){
            nano = Math.pow(10,9); //nanoseconds in 1 Second = 10^9 = 1/(10^(-9))
        }
        else if(unit.toString().equals("uSecond")){
            nano = Math.pow(10,3); //nanoseconds in 1 uSecond = 10^3 = 10^(-6)/(10^(-9))
        }
        else if(unit.toString().equals("mSecond")){
            nano = Math.pow(10,6); //nanoseconds in 1 mSecond = 10^6 = 10^(-3)/(10^(-9))
        }
        else if(unit.toString().equals("Hour")){
            nano = Math.pow(10,9);
            nano = nano * 3600; //nanoseconds in 1 hour = 10^9 * 3600
        }
        else if(unit.toString().equals("Day")){
            nano = Math.pow(10,9);
            nano = nano * 3600 * 24; //nanoseconds in 1 day = 10^9 * 3600 * 24
        }
        return (nano);
    }

    private void parseDelayInfo(Guard astd, JSONObject node) {
        String value = node.get(DELAY_PROPERTY).toString();
        Object guard;
        try{ //to get a number
            double valueInDouble = Double.parseDouble(value);
            Object delay_unit = node.get(DELAY_UNIT_PROPERTY);
            if(!delay_unit.toString().equals("nanoseconds")){
                guard = valueInDouble * getInNanoseconds(delay_unit);
            }
            else{
                guard = valueInDouble;
            }
        } catch(NumberFormatException e){
            //it is not a number, it is a parameter
            Object delay_unit = node.get(DELAY_UNIT_PROPERTY);
            if(!delay_unit.toString().equals("nanoseconds")){
                guard =  "(" + value + ")" + "*" + getInNanoseconds(delay_unit);
            }
            else{
                guard = value;
            }
        }
        astd.setGuard(new Bool(Conventions.LETS + "." + Conventions.EXPIRED + "("+guard+")"));
        if(Constants.DEBUG) System.out.println("Parse Guard");
    }

    private void parsePersistentDelayInfo(PersistentGuard astd, JSONObject node) {

        String value = node.get(DELAY_PROPERTY).toString();
        Object guard;
        try{ //to get a number
            double valueInDouble = Double.parseDouble(value);
            Object delay_unit = node.get(DELAY_UNIT_PROPERTY);
            if(!delay_unit.toString().equals("nanoseconds")){
                guard = valueInDouble * getInNanoseconds(delay_unit);
            }
            else{
                guard = valueInDouble;
            }
        } catch(NumberFormatException e){
            //it is not a number, it is a parameter
            Object delay_unit = node.get(DELAY_UNIT_PROPERTY);
            if(!delay_unit.toString().equals("nanoseconds")){
                guard =  "(" + value + ")" + "*" + getInNanoseconds(delay_unit);
            }
            else{
                guard = value;
            }
        }
        if(guard != null) {
            astd.setGuard(new Bool(Conventions.LETS + "." + Conventions.EXPIRED + "("+guard+")"));
            if(Constants.DEBUG) System.out.println("Parse Guard");
        }
    }

    private void parsePersistentGuardInfo(PersistentGuard astd, JSONObject node) {

        Object guard = node.get(GUARD_PROPERTY);
        if(guard != null) {
            if (guard instanceof String) {
                astd.setGuard(new Bool((String) guard));
            } else {
                JSONObject guard_obj = (JSONObject) guard;
                astd.setGuard(new Bool(FILENAME_PROPERTY + ":" + (String) guard_obj.get(FILENAME_PROPERTY)));
            }
            if(Constants.DEBUG) System.out.println("Parse Guard");
        }
    }


    /*
     * @brief Parse ASTD properties if exists
     * @param The astd object
     * @param  The node to parse
     * @return
     */

    private void parseASTD(ASTD astd, JSONObject par_node, JSONObject node) {
        parseAttributes(astd, node);
        parseParameters(astd, par_node);
        parseActionCode(astd, node);
    }

    private void parseASTDTimeout(ASTD astd, JSONObject par_node, JSONObject node) {
        parseAttributesTimeout(astd, node);
        parseParameters(astd, par_node);
        parseActionCode(astd, node);
    }

    private void parseASTDTimedInterrupt(ASTD astd, JSONObject par_node, JSONObject node) {
        parseAttributesTimedInterrupt(astd, node);
        parseParameters(astd, par_node);
        parseActionCode(astd, node);
    }

    /*
     * @brief Parse ASTD call
     * @param The astd object
     * @param  The node to parse
     * @return
     */

    private void parseASTDCall(Call astd, JSONObject node, Bool timed, JSONArray ASTDArray, ArrayList<ASTD> callTree) {
        String callName = (String) node.get(CALLNAME_PROPERTY);
        JSONArray callArgs = (JSONArray) node.get(CALLARG_PROPERTY);
        List<Variable> params = new ArrayList<>();
        ASTD bASTD = null;
        if(children.containsKey(callName))
            bASTD = children.get(callName);
        if(bASTD == null && !changedCalls.isEmpty()){
            if(changedCalls.containsKey(callName)){
                bASTD = children.get(changedCalls.get(callName));
            }
        }
        if(bASTD == null) {
            //two options ->
            // 1) The Called ASTD was not parsed -> Parse the Called ASTD and continue from here.
            // Search in the ASTD array for the astd;
            boolean parsed = false;
            for(int i = 0; i < ASTDArray.size(); i++) {
                JSONObject curr_obj = (JSONObject) ASTDArray.get(i);
                String astdName = (String) curr_obj.get(NAME_PROPERTY);
                if(astdName.equals(callName)){
                    try {
                        ArrayList<ASTD> astdTree;
                        if(callTree != null){
                            astdTree = new ArrayList<>(callTree);
                            astdTree.add(bASTD);
                        }
                        else{
                            astdTree = null;
                        }
                        bASTD = setupASTD(CALL_PROPERTY, curr_obj, null, timed, ASTDArray, astdTree);
                        children.put(bASTD.getName(), bASTD);
                        parsed = true;
                    } catch (Exception e) {
                        if (Constants.DEBUG) e.printStackTrace();
                    }
                }
            }
            // 2) The Called ASTD does not exist -> return ;
            if(!parsed) {
                return ;
            }
        }

        for(Object obj : callArgs) {
            JSONObject arg = (JSONObject) obj;
            JSONObject jobj = (JSONObject) arg.get(VALUE_PROPERTY);
            Variable p = new Variable((String) arg.get(NAME_PROPERTY),
                    (String) jobj.get(TYPE_PROPERTY),
                    JSONValue.escape(jobj.get(CONTENT_PROPERTY).toString()),
                    mainCallASTDName);
            params.add(p);
        }
        astd.setParams(params);
        astd.setBody(bASTD);
    }

    /*
     * @brief Parse Automaton properties if exists
     * @param The astd object
     * @param  The node to parse
     * @return
     */

    private void parseAutInfo(Automaton astd, JSONObject node, Bool timed) {
        parseTransitions(astd, node, timed);
        parseShallowFinalStates(astd, node);
        parseDeepFinalStates(astd, node);
        parseInitialState(astd, node);
    }

    private void parseAutInfoTimeout(Automaton astd, JSONObject node, Bool timed){
        parseTransitionsTimeout(astd, node, timed);
//        parseShallowFinalStates(astd);
//        parseDeepFinalStates(astd);
        parseInitialStateTimeout(astd);
    }

   /*
    * @brief is a Synchronization ASTD 
    * @param
    * @return if it is a Synchronization ASTD or not
    */
    private boolean isSynchronization(BinaryASTD bastd) {
        return bastd instanceof Synchronization;
    }

    /*
     * @brief is a Interrupt ASTD
     * @param
     * @return if it is a Interrupt ASTD or not
     */
    private boolean isInterrupt(BinaryASTD bastd){
        return bastd instanceof Interrupt;
    }

    /*
    * @brief is a QSynchronization ASTD 
    * @param
    * @return if it is a QSynchronization ASTD or not
    */
    private boolean isQSynchronization(QuantifiedASTD qastd) {
        return qastd instanceof QSynchronization;
    }
    /*
    * @brief is a QFlow ASTD 
    * @param
    * @return if it is a QFlowASTD or not
    */
    private boolean isQFlow(QuantifiedASTD qastd) {
        return qastd instanceof QFlow;
    }
    /*
     * @brief is a QParallelComposition ASTD
     * @param
     * @return if it is a QParallelComposition ASTD or not
     */
    private boolean isQParallelComposition(QuantifiedASTD qastd) {
        return qastd instanceof QParallelComposition;
    }

    /*
    * @brief is a Call ASTD 
    * @param
    * @return if it is a Call ASTD or not
    */
    private boolean isCall(UnaryASTD uastd) {
        return uastd instanceof Call;
    }

    /*
    * @brief is a Guard ASTD 
    * @param
    * @return if it is a Guard ASTD or not
    */
    private boolean isGuard(UnaryASTD uastd) {
        return uastd instanceof Guard;
    }

    /*
     * @brief is a Persistent Guard ASTD
     * @param
     * @return if it is a Persistent Guard ASTD or not
     */
    private boolean isPersistentGuard(UnaryASTD uastd) {
        return uastd instanceof PersistentGuard;
    }


    /*
     * @brief Creates an instance of an ASTD type and fills it with parsed data
     * @param  The ASTD type
     * @param The ASTD name
     * @param An ASTD container
     * @param the side of an ASTD
     * @return
     */

    private ASTD createPGuardASTDForTimeout(String name, JSONObject typedASTD, Bool timed) throws Exception{
        Class<?> cls;
        cls = Class.forName(ASTD_TYPE_CLASS_PATH + "Guard");
        UnaryASTD uASTD = (UnaryASTD) cls.getDeclaredConstructor().newInstance();
        uASTD.setName("guard1"+name);
        parseASTD(uASTD, null, null);
        mainCallASTDName = uASTD.getName();
        parseGuardInfoTimeoutLeft((Guard) uASTD, typedASTD);
        //no body for this astd for now
        return uASTD;
    }

    private ASTD createGuardASTDForTimeout(String name, JSONObject typedASTD, Bool timed, JSONArray objArray, ArrayList<ASTD> callTree) throws Exception{
        Class<?> cls;
        cls = Class.forName(ASTD_TYPE_CLASS_PATH + "Guard");
        UnaryASTD uASTD = (UnaryASTD) cls.getDeclaredConstructor().newInstance();
        uASTD.setName("guard"+name);
        parseASTD(uASTD, null, null);
        mainCallASTDName = uASTD.getName();
        parseGuardInfoTimeout((Guard) uASTD, typedASTD);
        // ASTD body is a choice
        ArrayList<ASTD> astdTree;
        if(callTree != null){
            astdTree = new ArrayList<>(callTree);
            astdTree.add(uASTD);
        }
        else{
            astdTree = null;
        }
        BinaryASTD choiceASTD = (BinaryASTD) createChoiceASTDForTimeout(name+"_guard", typedASTD, timed, objArray, astdTree);
        uASTD.setBody(choiceASTD);

        return uASTD;
    }

    private ASTD createPGuardASTDForPersistentTimeout(String name, JSONObject typedASTD, Bool timed) throws Exception{
        Class<?> cls;
        cls = Class.forName(ASTD_TYPE_CLASS_PATH + "PersistentGuard");
        UnaryASTD uASTD = (UnaryASTD) cls.getDeclaredConstructor().newInstance();
        uASTD.setName("pguard"+name);
        parseASTD(uASTD, null, null);
        mainCallASTDName = uASTD.getName();
        parseGuardInfoPersistentTimeoutLeft((PersistentGuard) uASTD, typedASTD);
        return uASTD;
    }

    private ASTD createGuardASTDForPersistentTimeout(String name, JSONObject typedASTD, Bool timed, JSONArray objArray, ArrayList<ASTD> callTree) throws Exception{
        Class<?> cls;
        cls = Class.forName(ASTD_TYPE_CLASS_PATH + "Guard");
        UnaryASTD uASTD = (UnaryASTD) cls.getDeclaredConstructor().newInstance();
        uASTD.setName("guard"+name);
        parseASTD(uASTD, null, null);
        mainCallASTDName = uASTD.getName();
        parseGuardInfoPersistentTimeout((Guard) uASTD, typedASTD);
        // ASTD body is a choice
        ArrayList<ASTD> astdTree;
        if(callTree != null){
            astdTree = new ArrayList<>(callTree);
            astdTree.add(uASTD);
        }
        else{
            astdTree = null;
        }
        BinaryASTD choiceASTD = (BinaryASTD) createChoiceASTDForTimeout(name+"_guard", typedASTD, timed, objArray, astdTree);
        uASTD.setBody(choiceASTD);

        return uASTD;
    }

    private ASTD createGuardASTDForTimedInterrupt(String name, JSONObject typedASTD, Bool timed, JSONArray objArray, ArrayList<ASTD> callTree) throws Exception{
        Class<?> cls;
        cls = Class.forName(ASTD_TYPE_CLASS_PATH + "Guard");
        UnaryASTD uASTD = (UnaryASTD) cls.getDeclaredConstructor().newInstance();
        uASTD.setName("guard"+name);
        parseASTD(uASTD, null, null);
        mainCallASTDName = uASTD.getName();
        parseGuardInfoTimedInterrupt((Guard) uASTD, typedASTD);
        // ASTD body is a choice
        ArrayList<ASTD> astdTree;
        if(callTree != null){
            astdTree = new ArrayList<>(callTree);
            astdTree.add(uASTD);
        }
        else{
            astdTree = null;
        }
        BinaryASTD choiceASTD = (BinaryASTD) createChoiceASTDForTimeout(name+"_guard", typedASTD, timed, objArray, astdTree);
        uASTD.setBody(choiceASTD);

        return uASTD;
    }

    private ASTD createPGuardASTDForTimedInterrupt(String name, JSONObject typedASTD, Bool timed) throws Exception{
        Class<?> cls;
        cls = Class.forName(ASTD_TYPE_CLASS_PATH + "PersistentGuard");
        UnaryASTD uASTD = (UnaryASTD) cls.getDeclaredConstructor().newInstance();
        uASTD.setName("pguard"+name);
        parseASTD(uASTD, null, null);
        mainCallASTDName = uASTD.getName();
        parseGuardInfoTimedInterruptLeft((PersistentGuard) uASTD, typedASTD);
        return uASTD;
    }


    private ASTD createChoiceASTDForTimeout(String name, JSONObject typedASTD, Bool timed, JSONArray objArray, ArrayList<ASTD> callTree) throws Exception{
        Class<?> cls;
        cls = Class.forName(ASTD_TYPE_CLASS_PATH + "Choice");
        BinaryASTD bASTD = (BinaryASTD) cls.getDeclaredConstructor().newInstance();
        bASTD.setName("choice"+name);
        mainCallASTDName = bASTD.getName();
        parseASTD(bASTD, null, null);
        // left ASTD
        ArrayList<ASTD> astdTree;
        if(callTree != null){
            astdTree = new ArrayList<>(callTree);
            astdTree.add(bASTD);
        }
        else{
            astdTree = null;
        }
        Automaton autState = (Automaton) createAutomataASTDForTimeout(bASTD.getName(), (JSONObject) typedASTD.get(RIGHT_ASTD_PROPERTY), timed, objArray, astdTree);
        List<String> deepStates = Arrays.asList(autState.getStateNames().get(1));
        autState.setDeepFinalStates(deepStates);
        bASTD.setLeft(autState);
        //right ASTD
        HashMap<String, String> oldChanged = (HashMap<String, String>) Utils.copyObject(changedCalls);
        ASTD newRightASTD = setupASTD(RIGHT_ASTD_PROPERTY, (JSONObject) typedASTD.get(RIGHT_ASTD_PROPERTY), "choice2"+name, timed, objArray, astdTree);
        valueChangeCalls(oldChanged, bASTD);
        bASTD.setRight(newRightASTD);

        return bASTD;
    }

    private ASTD createAutomataASTDForTimeout(String name , JSONObject rightASTD, Bool timed, JSONArray objArray, ArrayList<ASTD> callTree) throws Exception{
        Class<?> cls;
        cls = Class.forName(ASTD_TYPE_CLASS_PATH+"Automaton");
        Automaton autASTD = (Automaton) cls.getDeclaredConstructor().newInstance();
        autASTD.setName("choice_1"+name );
        mainCallASTDName = autASTD.getName();
        parseASTD(autASTD, null, null);
        parseAutInfoTimeout(autASTD, rightASTD, timed);
        // ASTD states
        ArrayList<ASTD> astdTree;
        if(callTree != null){
            astdTree = new ArrayList<>(callTree);
            astdTree.add(autASTD);
        }
        else{
            astdTree = null;
        }
        parseComplexStatesTimeout(autASTD, rightASTD, timed, objArray, astdTree);
        return autASTD;
    }

    public ASTD setupASTD(String side, JSONObject astdObj, String name, Bool timed, JSONArray ASTDArray, ArrayList<ASTD> callTree) throws Exception {
        Object astdName = astdObj.get(NAME_PROPERTY);
        ArrayList<ASTD> astdTree;
        Object astdType = astdObj.get(TYPE_PROPERTY);
        JSONObject typedASTD = (JSONObject) astdObj.get(TA_PROPERTY);
        Class<?> cls;
        if(Constants.TRANSFORM_ASTD_NAMES.contains(astdType)){
            if(astdType.toString().equals("Delay")){
                timed.setValue(true);
                cls = Class.forName(ASTD_TYPE_CLASS_PATH+"Guard");
                UnaryASTD uASTD = (UnaryASTD) cls.getDeclaredConstructor().newInstance();
                if(name != null)
                    uASTD.setName(name);
                else
                    uASTD.setName((String)astdName);
                parseASTD(uASTD, astdObj, typedASTD);
                mainCallASTDName = uASTD.getName();
                parseDelayInfo((Guard) uASTD, typedASTD);
                // ASTD body
                HashMap<String, String> oldChanged = (HashMap<String, String>) Utils.copyObject(changedCalls);
                if(callTree != null){
                    astdTree = new ArrayList<>(callTree);
                    astdTree.add(uASTD);
                }
                else{
                    astdTree = null;
                }
                ASTD bodyASTD = setupASTD(SUB_ASTD_PROPERTY, (JSONObject) typedASTD.get(SUB_ASTD_PROPERTY), null, timed, ASTDArray, astdTree);
                valueChangeCalls(oldChanged, uASTD);
                uASTD.setBody(bodyASTD);

                return uASTD;

            }
            else if(astdType.toString().equals("PersistentDelay")){
                timed.setValue(true);
                cls = Class.forName(ASTD_TYPE_CLASS_PATH+"PersistentGuard");
                UnaryASTD uASTD = (UnaryASTD) cls.getDeclaredConstructor().newInstance();
                if(name != null)
                    uASTD.setName(name);
                else
                    uASTD.setName((String)astdName);
                parseASTD(uASTD, astdObj, typedASTD);
                mainCallASTDName = uASTD.getName();
                parsePersistentDelayInfo((PersistentGuard) uASTD, typedASTD);
                // ASTD body
                HashMap<String, String> oldChanged = (HashMap<String, String>) Utils.copyObject(changedCalls);
                if(callTree != null){
                    astdTree = new ArrayList<>(callTree);
                    astdTree.add(uASTD);
                }
                else{
                    astdTree = null;
                }
                ASTD bodyASTD = setupASTD(SUB_ASTD_PROPERTY, (JSONObject) typedASTD.get(SUB_ASTD_PROPERTY), null, timed, ASTDArray, astdTree);
                valueChangeCalls(oldChanged, uASTD);
                uASTD.setBody(bodyASTD);

                return uASTD;
            }
            else if(astdType.toString().equals("Timeout")){
                timed.setValue(true);
                cls = Class.forName(ASTD_TYPE_CLASS_PATH+"Interrupt");
                BinaryASTD bASTD = (BinaryASTD) cls.getDeclaredConstructor().newInstance();
                if(name != null)
                    bASTD.setName(name);
                else
                    bASTD.setName((String)astdName);
                mainCallASTDName = bASTD.getName();
                //parsing the ASTD and create a boolean to know if it started
                parseASTDTimeout(bASTD, astdObj, typedASTD);
                //Timeout action
                parseTimeoutAction((Interrupt) bASTD, typedASTD);
                //leftASTD
                //left ASTD but every action will also set the variable from the interrupt to true (Add b:=true; to action of ASTD)
                //has a guard that does not allow the ASTD to run if the time of a timeout has passed
                UnaryASTD pguardASTD = (UnaryASTD) createPGuardASTDForTimeout(bASTD.getName(), typedASTD, timed);
                HashMap<String, String> oldChanged = (HashMap<String, String>) Utils.copyObject(changedCalls);
                if(callTree != null){
                    astdTree = new ArrayList<>(callTree);
                    astdTree.add(bASTD);
                }
                else{
                    astdTree = null;
                }
                ASTD leftASTD = setupASTD(LEFT_ASTD_PROPERTY,  (JSONObject) typedASTD.get(LEFT_ASTD_PROPERTY), null, timed, ASTDArray, astdTree);
                valueChangeCalls(oldChanged, pguardASTD);
                pguardASTD.setBody(leftASTD);
                bASTD.setLeft(pguardASTD);
                Action leftAction = new Action(null);
                if(bASTD.getLeft().getAstdAction() != null){
                    leftAction.setCode(bASTD.getLeft().getAstdAction().getCode() + ";\n startedState = true");
                }
                else{
                    leftAction.setCode("startedState = true");
                }
                bASTD.getLeft().setAstdAction(leftAction);
                //right ASTD
                //right ASTD but it is necessary to transform to a guard with a choice as child
                //first choice is an ASTD with step that leads to astd right
                //second choice is direct astd right
                UnaryASTD guardASTD = (UnaryASTD) createGuardASTDForTimeout(bASTD.getName(), typedASTD, timed, ASTDArray, astdTree);
                bASTD.setRight(guardASTD);
                return bASTD;
            }
            else if(astdType.toString().equals("PersistentTimeout")) {
                timed.setValue(true);
                cls = Class.forName(ASTD_TYPE_CLASS_PATH + "Interrupt");
                BinaryASTD bASTD = (BinaryASTD) cls.getDeclaredConstructor().newInstance();
                if (name != null)
                    bASTD.setName(name);
                else
                    bASTD.setName((String) astdName);
                mainCallASTDName = bASTD.getName();
                //parsing the ASTD DOES NOT CREATE A BOOLEAN (Not used in Persistent Timeout)
                parseASTD(bASTD, astdObj, typedASTD);
                //Timeout action
                parseTimeoutAction((Interrupt) bASTD, typedASTD);
                //leftASTD
                //has a guard that does not allow the ASTD to run if the time of a timeout has passed
                UnaryASTD pguardASTD = (UnaryASTD) createPGuardASTDForPersistentTimeout(bASTD.getName(), typedASTD, timed);
                HashMap<String, String> oldChanged = (HashMap<String, String>) Utils.copyObject(changedCalls);
                if(callTree != null){
                    astdTree = new ArrayList<>(callTree);
                    astdTree.add(bASTD);
                }
                else{
                    astdTree = null;
                }
                ASTD leftASTD = setupASTD(LEFT_ASTD_PROPERTY,  (JSONObject) typedASTD.get(LEFT_ASTD_PROPERTY), null, timed, ASTDArray, astdTree);
                valueChangeCalls(oldChanged, pguardASTD);
                pguardASTD.setBody(leftASTD);
                bASTD.setLeft(pguardASTD);
                //right ASTD
                //right ASTD but it is necessary to transform to a guard with a choice as child
                //first choice is an ASTD with step that leads to astd right
                //second choice is direct astd right
                UnaryASTD guardASTD = (UnaryASTD) createGuardASTDForPersistentTimeout(bASTD.getName(), typedASTD, timed, ASTDArray, astdTree);
                bASTD.setRight(guardASTD);
                return bASTD;
            }
            else if(astdType.toString().equals("TimedInterrupt")){
                timed.setValue(true);
                cls = Class.forName(ASTD_TYPE_CLASS_PATH + "Interrupt");
                BinaryASTD bASTD = (BinaryASTD) cls.getDeclaredConstructor().newInstance();
                if (name != null)
                    bASTD.setName(name);
                else
                    bASTD.setName((String) astdName);
                mainCallASTDName = bASTD.getName();
                //parsing the ASTD and create a clock
                parseASTDTimedInterrupt(bASTD, astdObj, typedASTD);
                //Timeout action
                parseTimeoutAction((Interrupt) bASTD, typedASTD);
                //leftASTD
                //has a persistent guard that does not allow the ASTD to run if the time of a timeout has passed
                UnaryASTD pguardASTD = (UnaryASTD) createPGuardASTDForTimedInterrupt(bASTD.getName(), typedASTD, timed);
                HashMap<String, String> oldChanged = (HashMap<String, String>) Utils.copyObject(changedCalls);
                if(callTree != null){
                    astdTree = new ArrayList<>(callTree);
                    astdTree.add(bASTD);
                }
                else{
                    astdTree = null;
                }
                ASTD leftASTD = setupASTD(LEFT_ASTD_PROPERTY,  (JSONObject) typedASTD.get(LEFT_ASTD_PROPERTY), null, timed, ASTDArray, astdTree);
                valueChangeCalls(oldChanged, pguardASTD);
                pguardASTD.setBody(leftASTD);
                bASTD.setLeft(pguardASTD);//right ASTD
                //right ASTD but it is necessary to transform to a guard with a choice as child
                //first choice is an ASTD with step that leads to astd right
                //second choice is direct astd right
                UnaryASTD guardASTD = (UnaryASTD) createGuardASTDForTimedInterrupt(bASTD.getName(), typedASTD, timed, ASTDArray, astdTree);
                bASTD.setRight(guardASTD);
                return bASTD;
            }
        }
        cls = Class.forName(ASTD_TYPE_CLASS_PATH + astdType);
        if(Constants.BINARY_ASTD_NAMES.contains(astdType)) {
            BinaryASTD bASTD = (BinaryASTD) cls.getDeclaredConstructor().newInstance();
            if(name != null)
                bASTD.setName(name);
            else
                bASTD.setName((String)astdName);

            mainCallASTDName = bASTD.getName();
            parseASTD(bASTD, astdObj, typedASTD);
            // parse synchronization set
            if(isSynchronization(bASTD))
                parseSynchronizationSet((Synchronization)bASTD, typedASTD);
            if(isInterrupt(bASTD)){
                parseInterruptAction((Interrupt) bASTD, typedASTD);
            }
            // left ASTD
            HashMap<String, String> oldChanged = (HashMap<String, String>) Utils.copyObject(changedCalls);
            if(callTree != null){
                astdTree = new ArrayList<>(callTree);
                astdTree.add(bASTD);
            }
            else{
                astdTree = null;
            }
            ASTD leftASTD  = setupASTD(LEFT_ASTD_PROPERTY,  (JSONObject) typedASTD.get(LEFT_ASTD_PROPERTY), null, timed, ASTDArray, astdTree);
            HashMap<String, String> newCallsLeft = (HashMap<String, String>) Utils.copyObject(oldChanged);
            if(changedCalls != null) {
                for (String key : changedCalls.keySet()) {
                    if (!oldChanged.containsKey(key)) newCallsLeft.put(key, changedCalls.get(key));
                }
            }
            valueChangeCalls(oldChanged, bASTD);
            bASTD.setLeft(leftASTD);
            //right ASTD
            HashMap<String, String> oldChangedRight = (HashMap<String, String>) Utils.copyObject(changedCalls);
            ASTD rightASTD = setupASTD(RIGHT_ASTD_PROPERTY, (JSONObject) typedASTD.get(RIGHT_ASTD_PROPERTY), null, timed, ASTDArray, astdTree);
            HashMap<String, String> newCallsRight = (HashMap<String, String>) Utils.copyObject(oldChanged);
            if(changedCalls != null) {
                for (String key : changedCalls.keySet()) {
                    if (!oldChangedRight.containsKey(key)) newCallsRight.put(key, changedCalls.get(key));
                }
            }
            ASTD newRight = null;
            if(leftASTD.getName().equals(rightASTD.getName())) {
                //Name conflict, two calls of the same binary ASTD are changing the ASTD to the same name!!
                //Let's create a new instance of the ASTD and copy the values to this new instance.
                for (String key : newCallsRight.keySet()) {
                    if (newCallsLeft.containsValue(newCallsRight.get(key))) {
                        newRight = rightASTD.getClass().newInstance();
                        if (newRight instanceof Automaton) {
                            newRight = new Automaton(
                                    newRight.getName(), rightASTD.getAttributes(), rightASTD.getParams(), rightASTD.getAstdAction(),
                                    ((Automaton) rightASTD).getEventNames(), ((Automaton) rightASTD).getStateNames(),
                                    ((Automaton) rightASTD).getStatesToASTDs(), ((Automaton) rightASTD).getStatesToActions(),
                                    ((Automaton) rightASTD).getTransitions(), ((Automaton) rightASTD).getShallowFinalStates(),
                                    ((Automaton) rightASTD).getDeepFinalStates(), ((Automaton) rightASTD).getInitialState());
                        } else if (newRight instanceof Call) {
                            //String name, List<Variable> attributes, List<Variable> params, Action astdAction, ASTD body
                            newRight = new Call(
                                    newRight.getName(), rightASTD.getAttributes(), rightASTD.getParams(), rightASTD.getAstdAction(),
                                    ((Call) rightASTD).getBody()
                            );
                        } else if (newRight instanceof Choice) {
                            //String name, List<Variable> attributes, List<Variable> params, Action astdAction, ASTD left, ASTD right
                            newRight = new Choice(
                                    newRight.getName(), rightASTD.getAttributes(), rightASTD.getParams(), rightASTD.getAstdAction(),
                                    ((Choice) rightASTD).getLeft(), ((Choice) rightASTD).getRight()
                            );
                        } else if (newRight instanceof Flow) {
                            //String name, List<Variable> attributes, List<Variable> params,
                            //                Action astdAction, ASTD left, ASTD right
                            newRight = new Flow(
                                    newRight.getName(), rightASTD.getAttributes(), rightASTD.getParams(), rightASTD.getAstdAction(),
                                    ((Flow) rightASTD).getLeft(), ((Flow) rightASTD).getRight()
                            );
                        } else if (newRight instanceof Guard) {
                            //String name, List<Variable> attributes, List<Variable> params, Action astdAction, ASTD body,
                            // Condition guard
                            newRight = new Guard(
                                    newRight.getName(), rightASTD.getAttributes(), rightASTD.getParams(), rightASTD.getAstdAction(),
                                    ((Guard) rightASTD).getBody(), ((Guard) rightASTD).getGuard()
                            );
                        } else if (newRight instanceof Interleaving) {
                            //String name, List<Variable> attributes, List<Variable> params, Action astdAction,
                            // ASTD left, ASTD right
                            newRight = new Interleaving(
                                    newRight.getName(), rightASTD.getAttributes(), rightASTD.getParams(), rightASTD.getAstdAction(),
                                    ((Interleaving) rightASTD).getLeft(), ((Interleaving) rightASTD).getRight()
                            );
                        } else if (newRight instanceof Interrupt) {
                            //String name, List<Variable> attributes, List<Variable> params, Action astdAction, Action interruptAction,
                            // ASTD left, ASTD right
                            newRight = new Interrupt(
                                    newRight.getName(), rightASTD.getAttributes(), rightASTD.getParams(), rightASTD.getAstdAction(),
                                    ((Interrupt) rightASTD).getInterruptAction(), ((Interrupt) rightASTD).getLeft(), ((Interrupt) rightASTD).getRight()
                            );
                        } else if (newRight instanceof Kleene) {
                            newRight = new Kleene(
                                    newRight.getName(), rightASTD.getAttributes(), rightASTD.getParams(), rightASTD.getAstdAction(),
                                    ((Kleene) rightASTD).getBody()
                            );
                        } else if (newRight instanceof PersistentGuard) {
                            //String name, List<Variable> attributes, List<Variable> params, Action astdAction, ASTD body,
                            // Condition guard
                            newRight = new PersistentGuard(
                                    newRight.getName(), rightASTD.getAttributes(), rightASTD.getParams(), rightASTD.getAstdAction(),
                                    ((PersistentGuard) rightASTD).getBody(), ((PersistentGuard) rightASTD).getGuard()
                            );
                        } else if (newRight instanceof QChoice) {
                            //String name, List<Variable> attributes, List<Variable> params, Action astdAction, ASTD body,
                            // Variable qvariable, Domain domain
                            newRight = new QChoice(
                                    newRight.getName(), rightASTD.getAttributes(), rightASTD.getParams(), rightASTD.getAstdAction(),
                                    ((QChoice) rightASTD).getBody(), ((QChoice) rightASTD).getQvariable(), ((QChoice) rightASTD).getDomain()
                            );
                        } else if (newRight instanceof QFlow) {
                            //String name, List<Variable> attributes, List<Variable> params, Action astdAction, ASTD body,
                            // Variable qvariable, Domain domain
                            newRight = new QFlow(
                                    newRight.getName(), rightASTD.getAttributes(), rightASTD.getParams(), rightASTD.getAstdAction(),
                                    ((QFlow) rightASTD).getBody(), ((QFlow) rightASTD).getQvariable(), ((QFlow) rightASTD).getDomain()
                            );
                        } else if (newRight instanceof QInterleaving) {
                            //String name, List<Variable> attributes, List<Variable> params, Action astdAction, ASTD body,
                            // Variable qvariable, Domain domain
                            newRight = new QInterleaving(
                                    newRight.getName(), rightASTD.getAttributes(), rightASTD.getParams(), rightASTD.getAstdAction(),
                                    ((QInterleaving) rightASTD).getBody(), ((QInterleaving) rightASTD).getQvariable(),
                                    ((QInterleaving) rightASTD).getDomain()
                            );
                        } else if (newRight instanceof QSynchronization) {
                            //String name, List<Variable> attributes, List<Variable> params, Action astdAction, ASTD body,
                            // Variable qvariable, Domain domain, List<String> delta
                            newRight = new QSynchronization(
                                    newRight.getName(), rightASTD.getAttributes(), rightASTD.getParams(), rightASTD.getAstdAction(),
                                    ((QSynchronization) rightASTD).getBody(), ((QSynchronization) rightASTD).getQvariable(),
                                    ((QSynchronization) rightASTD).getDomain(), ((QSynchronization) rightASTD).getDelta()
                            );
                        } else if (newRight instanceof Sequence) {
                            //String name, List<Variable> attributes, List<Variable> params,
                            //                Action astdAction, ASTD left, ASTD right
                            newRight = new Sequence(
                                    newRight.getName(), rightASTD.getAttributes(), rightASTD.getParams(), rightASTD.getAstdAction(),
                                    ((Sequence) rightASTD).getLeft(), ((Sequence) rightASTD).getRight()
                            );
                        } else if (newRight instanceof Synchronization) {
                            //String name, List<Variable> attributes, List<Variable> params,
                            //                Action astdAction, ASTD left, ASTD right, List<String> delta
                            newRight = new Synchronization(
                                    newRight.getName(), rightASTD.getAttributes(), rightASTD.getParams(), rightASTD.getAstdAction(),
                                    ((Synchronization) rightASTD).getLeft(), ((Synchronization) rightASTD).getRight(),
                                    ((Synchronization) rightASTD).getDelta()
                            );
                        }
                        changedCalls.remove(key);
                        changedCalls.put(key, newRight.getName());
                    }
                }
            }
            valueChangeCalls(oldChangedRight, bASTD);
            if(newRight != null){
                bASTD.setRight(newRight);
            }
            else{
                bASTD.setRight(rightASTD);
            }
            return bASTD;
        }
        else if(Constants.ELEM_ASTD_NAME.contains(astdType)) {
            Automaton autASTD = (Automaton) cls.getDeclaredConstructor().newInstance();
            if(name != null)
                autASTD.setName(name);
            else
                autASTD.setName((String)astdName);

            mainCallASTDName = autASTD.getName();
            parseASTD(autASTD, astdObj, typedASTD);
            parseAutInfo(autASTD, typedASTD, timed);
            // ASTD states
            if(callTree != null){
                astdTree = new ArrayList<>(callTree);
                astdTree.add(autASTD);
            }
            else{
                astdTree = null;
            }
            parseComplexStates(autASTD, typedASTD, timed, ASTDArray, astdTree);

            return autASTD;
        }
        else if(Constants.QUANTIFIED_ASTD_NAMES.contains(astdType)) {
            QuantifiedASTD qASTD = (QuantifiedASTD) cls.getDeclaredConstructor().newInstance();
            if(name != null)
                qASTD.setName(name);
            else
                qASTD.setName((String)astdName);

            mainCallASTDName = qASTD.getName();
            parseASTD(qASTD, astdObj, typedASTD);
            // quantified variable
            parseQVariable(qASTD, typedASTD);
            // qsynchronization set
            if(isQSynchronization(qASTD))
                parseQSynchronizationSet((QSynchronization) qASTD, typedASTD);
            Constants.DUMMY_PREFIX2.put(qASTD.getQvariable(), qASTD);
            if(callTree != null){
                astdTree = new ArrayList<>(callTree);
                astdTree.add(qASTD);
            }
            else{
                astdTree = null;
            }
            Constants.QUANT_PREF.put(qASTD.getQvariable(), astdTree);
            // ASTD body
            HashMap<String, String> oldChanged = (HashMap<String, String>) Utils.copyObject(changedCalls);
            ASTD bodyASTD = setupASTD(SUB_ASTD_PROPERTY, (JSONObject) typedASTD.get(SUB_ASTD_PROPERTY), null, timed, ASTDArray, astdTree);
            valueChangeCalls(oldChanged, qASTD);
            qASTD.setBody(bodyASTD);
            if(isQParallelComposition(qASTD))
                parseQParallelCompositionSet((QSynchronization) qASTD, typedASTD);
            return qASTD;
        }
        else if(Constants.UNARY_ASTD_NAMES.contains(astdType)) {
            UnaryASTD uASTD = (UnaryASTD) cls.getDeclaredConstructor().newInstance();
            if(name != null)
                uASTD.setName(name);
            else
                uASTD.setName((String)astdName);
            parseASTD(uASTD, astdObj, typedASTD);
            if(isCall(uASTD)) {
                if(callTree != null){
                    astdTree = new ArrayList<>(callTree);
                    astdTree.add(uASTD);
                }
                else{
                    astdTree = null;
                }
                parseASTDCall((Call) uASTD, typedASTD, timed, ASTDArray, astdTree);
                //it found a Call, now we need to check if it is an "empty" call
                // -> empty call: attributes and action is null
                // -> empty call will be further replaced for the body
                if(uASTD.getAttributes() == null && (uASTD.getParams() == null || uASTD.getParams().isEmpty()) && uASTD.getAstdAction() == null) {
                    //need to change the values from the setup that arrived here!!!
                    //to keep track, add an oldchangedCalls before each setup, once old is changed, then we change!!
                    if(uASTD.getBody() != null){
                        changedCalls.put(astdName.toString(), uASTD.getBody().getName());
                        return uASTD.getBody();
                    }
                }
                hasCallASTD = true;
            }
            else {
                mainCallASTDName = uASTD.getName();
                if(isGuard(uASTD))
                    parseGuardInfo((Guard) uASTD, typedASTD);
                else if(isPersistentGuard(uASTD)){
                    parsePersistentGuardInfo((PersistentGuard) uASTD, typedASTD);
                }
                // ASTD body
                HashMap<String, String> oldChanged = (HashMap<String, String>) Utils.copyObject(changedCalls);
                if(callTree != null){
                    astdTree = new ArrayList<>(callTree);
                    astdTree.add(uASTD);
                }
                else{
                    astdTree = null;
                }
                ASTD bodyASTD = setupASTD(SUB_ASTD_PROPERTY, (JSONObject) typedASTD.get(SUB_ASTD_PROPERTY), null, timed, ASTDArray, astdTree);
                valueChangeCalls(oldChanged, uASTD);
                uASTD.setBody(bodyASTD);
            }

            return uASTD;
        }
        else if(Elem.class.getSimpleName().compareToIgnoreCase((String)astdType) == 0) {
            Elem eASTD = (Elem) cls.getDeclaredConstructor().newInstance();
            eASTD.setName((String)astdName);
            mainCallASTDName = eASTD.getName();

            return eASTD;
        }
        if (Constants.DEBUG) System.out.println("Parse "+side+" ASTD '" + astdName + "' Of Type '" + astdType + "'");

        return null;
    }

    /*
     * @brief gets the values of an ASTD (name) and changes the mentions of this value to new values.
     * @param map of old values
     * @return
     */
    private String valueChangeCalls(HashMap<String, String> old, ASTD instance){
        //astd is not a call, but inside can have a transition from the empty call.
        //the name of this state don't change from the piece of code when a call is found
        //because at that moment, all that the code saw was an Automata / other ASTD type!!
        //need to change now the call state names and change it on this ASTD.
        String newName = null;
        if(changedCalls != null){
            for(String key : changedCalls.keySet()) {
                if (!old.containsKey(key)) {
                    //A state has been changed, now the course of the actions depends if it is an Automata or other ASTD type:
                    if(instance instanceof Automaton){
                        //It is an automaton!!
                        //1. -> Need to check all transitions
                        //1. -> Transitions making reference to the key shall be changed for the new ASTD found in the map.
                        //2. -> Need to change the deepFinalStates pointing to map value.
                        //3. -> Need to change the shallowFinalStates pointing to map value.
                        //4. -> Need to change initialState
                        //5. -> Need to change stateNames
                        //6. -> Need to change stateToASTDs
                        //7. -> Need to change statesToActions
                        Automaton astd = (Automaton) instance;
                        //Resolving 1.
                        for (Transition transition : astd.getTransitions()) {
                            Arrow arrow = transition.getArrow();
                            if (arrow instanceof Local) {
                                Local loc = (Local) arrow;
                                if (loc.getS1().equals(key)) {
                                    loc.setS1(changedCalls.get(key));
                                }
                                if (loc.getS2().equals(key)) {
                                    loc.setS2(changedCalls.get(key));
                                }
                            } else if (arrow instanceof ToSub) {
                                ToSub tosub = (ToSub) arrow;
                                if (tosub.getS1().equals(key)) {
                                    tosub.setS1(changedCalls.get(key));
                                }
                                if (tosub.getS2().equals(key)) {
                                    tosub.setS2(changedCalls.get(key));
                                }
                                if (tosub.getS2b().equals(key)) {
                                    tosub.setS2b(changedCalls.get(key));
                                }
                            } else if (arrow instanceof FromSub) {
                                FromSub fromsub = (FromSub) arrow;
                                if (fromsub.getS1().equals(key)) {
                                    fromsub.setS1(changedCalls.get(key));
                                }
                                if (fromsub.getS1b().equals(key)) {
                                    fromsub.setS1b(changedCalls.get(key));
                                }
                                if (fromsub.getS2().equals(key)) {
                                    fromsub.setS2(changedCalls.get(key));
                                }
                            }
                        }
                        //Resolving 2.
                        if (astd.getDeepFinalStates() != null) {
                            for (int j = 0; j < astd.getDeepFinalStates().size(); j++) {
                                if (astd.getDeepFinalStates().get(j).equals(key)) {
                                    astd.getDeepFinalStates().set(j, changedCalls.get(key));
                                }
                            }
                        }
                        //Resolving 3.
                        if (astd.getShallowFinalStates() != null) {
                            for (int j = 0; j < astd.getShallowFinalStates().size(); j++) {
                                if (astd.getShallowFinalStates().get(j).equals(key)) {
                                    astd.getShallowFinalStates().set(j, changedCalls.get(key));
                                }
                            }
                        }
                        //Resolving 4.
                        if (astd.getInitialState() != null && astd.getInitialState().equals(key)){
                            astd.setInitialState(changedCalls.get(key));
                        }
                        //Resolving 5.
                        if(astd.getStateNames() != null && astd.getStateNames().contains(key)){
                            for(int j = 0; j < astd.getStateNames().size(); j++){
                                if(astd.getStateNames().get(j).equals(key)){
                                    astd.getStateNames().set(j, changedCalls.get(key));
                                }
                            }
                        }
                        //Resolving 6.
                        if(astd.getStatesToASTDs() != null && astd.getStatesToASTDs().containsKey(key)){
                            ASTD astdState = astd.getStatesToASTDs().get(key);
                            astd.getStatesToASTDs().remove(key);
                            astd.getStatesToASTDs().put(changedCalls.get(key), astdState);
                        }
                        //Resolving 7.
                        if(astd.getStatesToActions() != null && astd.getStatesToActions().containsKey(key)){
                            ActionSet actionset = astd.getStatesToActions().get(key);
                            astd.getStatesToActions().remove(key);
                            astd.getStatesToActions().put(changedCalls.get(key), actionset);
                        }
                        newName = changedCalls.get(key);
                    }
                }
            }
        }
        return newName;
    }

    /*
     * @brief Gets the arrow object (local, tosub, fsub) given a JSON object
     * @param An arrow type
     * @param The JSON object
     * @param The transition to update
     * @return
     */
    private void setArrowById(String arr_type, JSONObject arr_obj, Transition trans) {
        Arrow arrw;
        if (arr_type.compareToIgnoreCase(Local.class.getSimpleName()) == 0) {
            arrw = new Local((String) arr_obj.get(FROM_STATE_PROPERTY),
                             (String) arr_obj.get(TO_STATE_PROPERTY));
            trans.setArrow(arrw);
            if(Constants.DEBUG) System.out.println("Parse Local Arrow");
        } else if (arr_type.compareToIgnoreCase(FromSub.class.getSimpleName()) == 0) {
            arrw = new FromSub((String) arr_obj.get(FROM_STATE_PROPERTY),
                               (String) arr_obj.get(TO_STATE_PROPERTY),
                               (String) arr_obj.get(THROUGH_STATE_PROPERTY));
            trans.setArrow(arrw);
            if(Constants.DEBUG) System.out.println("Parse FromSub Arrow");
        } else if (arr_type.compareToIgnoreCase(ToSub.class.getSimpleName()) == 0) {
            arrw = new ToSub((String) arr_obj.get(FROM_STATE_PROPERTY),
                             (String) arr_obj.get(TO_STATE_PROPERTY),
                             (String) arr_obj.get(THROUGH_STATE_PROPERTY));
            trans.setArrow(arrw);
            if(Constants.DEBUG) System.out.println("Parse ToSub Arrow");
        }
    }

    /*
     * @brief Gets event label and params
     * @param The JSON object containing event info
     * @param The transition to update
     * @return
     */
    private void setEventFormatById(ASTD astd, JSONObject transition, Transition trans, Bool timed) {
        Event evt = new Event();
        List<String> tmpGuards = new ArrayList<>();
        HashMap<String, String> changedVariables = new HashMap<>();
        JSONObject evt_template = (JSONObject) transition.get(ET_PROPERTY);
        evt.setName((String) evt_template.get(ET_LABEL_PROPERTY));
        Object params = evt_template.get(ET_PARAM_PROPERTY);
        if (params != null) {
            ArrayList<Variable> evt_params = new ArrayList<>();
            Constants.COUNT = 0;
            for (Object obj1 : (JSONArray) params) {
                JSONObject item = (JSONObject) obj1;
                String var = (String) item.get(ET_PARAM_KIND_PROPERTY);
                if (var.compareToIgnoreCase(VARIABLE_NAME) == 0
                    || var.compareToIgnoreCase(EXPRESSION_NAME) == 0
                    || var.compareToIgnoreCase(ET_PARAM_KIND_CAPTURE_PROPERTY) == 0) {
                    Object param1 = item.get(ET_PARAM1_PROPERTY);
                    if(param1 instanceof JSONObject) {
                        JSONObject param = (JSONObject) param1;
                        if (param.get(TYPE_PROPERTY) != null) {
                            if (param.get(INITIAL_VALUE_PROPERTY) != null) {
                                evt_params.add(new Variable((String) param.get(ET_PARAM_NAME_PROPERTY),
                                                            (String) param.get(TYPE_PROPERTY),
                                                            param.get(INITIAL_VALUE_PROPERTY), astd.getName()));
                            } else {
                                evt_params.add(new Variable((String) param.get(ET_PARAM_NAME_PROPERTY),
                                                            (String) param.get(TYPE_PROPERTY),
                                                            null, astd.getName()));
                            }
                        }
                        else {
                            evt_params.add(new Variable((String) param.get(ET_PARAM_NAME_PROPERTY),
                                                   null, null, astd.getName()));
                        }
                    }
                    if(param1 instanceof String) {
                        boolean hasParam = false;
                        if(parameters != null) {
                            Iterator<Variable> it = parameters.iterator();
                            while (it.hasNext()) {
                                Variable p = it.next();
                                if(p.getName().compareTo((String)param1) == 0) {
                                    evt_params.add(new Variable("_p" + Constants.COUNT,
                                                                p.getType(), p.getInit(), astd.getName()));
                                    tmpGuards.add("_p" + Constants.COUNT + "==" + param1);
                                    hasParam = true;
                                    Constants.COUNT++;
                                    break;
                                }
                            };
                        }
                        boolean hasAttrParam = false;
                        if(attributes != null) {
                            Iterator<Variable> it = attributes.iterator();
                            while (it.hasNext()) {
                                Variable attr = it.next();
                                if(attr.getName().compareTo((String)param1) == 0) {
                                    changedVariables.put((String) param1, "_w" + Constants.COUNT);
                                    evt_params.add(new Variable("_w" + Constants.COUNT,
                                                                 attr.getType(), attr.getInit(), astd.getName()));
                                    tmpGuards.add("_w" + Constants.COUNT + "==" + param1);
                                    Constants.COUNT++;
                                    hasAttrParam = true;
                                    break;
                                }
                            };
                        }

                        if(hasParam && hasAttrParam) {
                            System.out.println("[Error] Not allowed to use same names for attributes and ASTD parameters !!!");
                            System.exit(0);
                        }
                        if(!hasParam && !hasAttrParam)
                             evt_params.add(new Variable((String) param1, null, null, astd.getName()));
                    }
                    
                } else {
                    // it's a constant parameter
                    // TODO : Parse constant parameters of an event
                }
            }
            evt.setParams(evt_params);
        }
        if(evt_template != null && (evt_template.get(WHEN_PROPERTY) != null || !tmpGuards.isEmpty())) {
            List<String> whenList = new ArrayList<>();
            if(evt_template.get(WHEN_PROPERTY) != null) {
                JSONArray guards = (JSONArray) evt_template.get(WHEN_PROPERTY);
                for (Object obj : guards) {
                    whenList.add((String) obj);
                }
            }
            if(!tmpGuards.isEmpty())
                 whenList.addAll(tmpGuards);

            evt.setWhen(whenList);
        }

        trans.setEvent(evt);
        if(evt.getName().compareTo("Step") == 0){
            timed.setValue(true);
        }
        
        if(!changedVariables.isEmpty()){
            Constants.PARAM_OPTM.put(evt.getName(), changedVariables);
        }
        
        if(Constants.DEBUG) System.out.println("Parse Events");
    }

    /*
     * @brief Gets guard from an ASTD object
     * @param The JSON object containing guard info
     * @param The transition to update
     * @return
     */
    private void setGuardById(JSONObject transition, Transition trans) {
        Object guard = transition.get(GUARD_PROPERTY);
        if(guard != null) {
            if (guard instanceof String) {
                trans.setGuard((String) guard);
            } else {
                JSONObject guard_obj = (JSONObject) guard;
                trans.setGuard(FILENAME_PROPERTY +":"+ guard_obj.get(FILENAME_PROPERTY));
            }
            if(Constants.DEBUG) System.out.println("Parse Transition Guard");
        }
    }

    /*
     * @brief Gets final state from an ASTD object
     * @param The JSON object containing final state info
     * @param The transition to update
     * @return
     */
    private void setFinalStateById(JSONObject transition, Transition trans) {
        Object isFinal = transition.get(TRANSITION_FINAL_PROPERTY);
        if(isFinal != null) {
            trans.setFinal((Boolean) isFinal);
            if(Constants.DEBUG) System.out.println("Parse Transition IsFinal");
        }
    }

    /*
     * @brief Gets the action of the transition from an ASTD object
     * @param The JSON object containing the transition action info
     * @param The transition to update
     * @return
     */
    private void setTransitionActionById(JSONObject trans_obj, Transition trans) {
        Object action = trans_obj.get(TRANSITION_ACTION_PROPERTY);
        if(action != null) {
            if (action instanceof String) {
                String str_action = (String) action;
                if(!str_action.isEmpty()) {
                    final String regex = "(\\{)((.*(\\n)*.*)*)(\\}$)";
                    final String string = str_action;
                    final String subst = "$2";

                    final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
                    final Matcher matcher = pattern.matcher(string);

                    // The substituted value will be contained in the result variable
                    final String result = matcher.replaceAll(subst);
                    trans.setAction(new Action(result));
                }
            } else {
                JSONObject action_obj = (JSONObject) action;
                trans.setAction(new Action(FILENAME_PROPERTY + ":" + (String) action_obj.get(FILENAME_PROPERTY)));
            }
            if(Constants.DEBUG) System.out.println("Parse Transition Action");
        }
    }

    /*
     * @brief  Gets ASTD action from an ASTD object
     * @param  The JSON object containing ASTD action
     * @return
     */
    private void parseActionCode(ASTD astd, Object typed_astd) {
        if (typed_astd != null){
            Object action = ((JSONObject) typed_astd).get(ASTD_ACTION_PROPERTY);
            if(action != null) {
                Action astd_action = null;
                if (action instanceof String) {
                    String str_action = (String) action;
                    if(!str_action.isEmpty()) {
                        final String regex = "(\\{)((.*(\\n)*.*)*)(\\}$)";
                        final String string = str_action;
                        final String subst = "$2";

                        final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
                        final Matcher matcher = pattern.matcher(string);

                        // The substituted value will be contained in the result variable
                        final String result = matcher.replaceAll(subst);
                        astd_action = new Action(result);
                    }
                } else {
                    JSONObject action_obj = (JSONObject) action;
                    astd_action = new Action(FILENAME_PROPERTY + ":" + (String) action_obj.get(FILENAME_PROPERTY));
                }
                astd.setAstdAction(astd_action);
                if(Constants.DEBUG) System.out.println("Parse Action Code");
            }
        }
    }

    /*
     * @brief  Gets the shallow final states from an ASTD object
     * @param  The JSON object contains the shallow final states
     * @return
     */
    private void parseShallowFinalStates(Automaton astd, JSONObject node) {
        Object shallow_obj = node.get(SHALLOW_FINAL_STATES_PROPERTY);
        if(shallow_obj != null) {
            JSONArray shallowStates = (JSONArray) shallow_obj;
            Set<String> shallowFinalStates = new HashSet<>();
            for (Object state : shallowStates) {
                shallowFinalStates.add((String) state);
            }
            if(!shallowFinalStates.isEmpty()) {
                astd.setShallowFinalStates(new ArrayList<>(shallowFinalStates));
                if(Constants.DEBUG) System.out.println("Parse Shallow Final States");
            }
        }
    }

    /*
     * @brief  Gets the deep final states from an ASTD object
     * @param  The JSON object containing the deep final states
     * @return
     */
    private void parseDeepFinalStates(Automaton astd, JSONObject node) {
        Object deep_obj = node.get(DEEP_FINAL_STATES_PROPERTY);
        if(deep_obj != null) {
            JSONArray deepStates = (JSONArray) deep_obj;
            Set<String> deepFinalStates = new HashSet<>();
            for (Object state : deepStates) {
                deepFinalStates.add((String) state);
            }
            if(!deepFinalStates.isEmpty()) {
                astd.setDeepFinalStates(new ArrayList<>(deepFinalStates));
                if (Constants.DEBUG) System.out.println("Parse Deep Final States");
            }
        }
    }

    /*
     * @brief  Gets the domain of values taken by the quantified variable from an ASTD object
     * @param  The JSON object containing that domain of values
     * @return
     */
    private  Domain retrieveDomainById(Object node) {
        Object dom_obj = ((JSONObject) node).get(DOMAIN_PROPERTY);
        if(dom_obj != null) {
            JSONObject obj_domain = (JSONObject) dom_obj;

            String domain_type = (String) obj_domain.get(TYPE_PROPERTY);
            Object domain_value = obj_domain.get(VALUE_PROPERTY);

            if(Constants.DEBUG) System.out.println("Parse Domain");

            return new Domain(domain_type, domain_value);
        }
        return null;
    }

    /*
     * @brief  Gets the initial state from an ASTD object
     * @param  The JSON object containing the intial state
     * @return
     */
    private void parseInitialState(Automaton astd, JSONObject node) {
        Object initial_state_obj = node.get(INITIAL_STATE_PROPERTY);
        if(initial_state_obj != null) {
            String initial_state = (String) initial_state_obj;
            astd.setInitialState(initial_state);
            if(Constants.DEBUG) System.out.println("Parse Initial State");
        }
    }

    private void parseInitialStateTimeout(Automaton astd) {
        String initial_state = "S0";
        astd.setInitialState(initial_state);
        if(Constants.DEBUG) System.out.println("Parse Initial State");
    }

    /*
     * @brief Checks whether the object is traverseable
     * @param  The Object
     * @return
     */
    private boolean isTraverseable(Object node) {
        return getNodeType(node) == Type.JSONOBJECT ||
                getNodeType(node) == Type.JSONARRAY;
    }

    /*
     * @brief Gets node type
     * @param  The object
     * @return
     */
    private Type getNodeType(Object node) {
        if (node instanceof JSONObject) {
            return Type.JSONOBJECT;
        } else if (node instanceof JSONArray) {
            return Type.JSONARRAY;
        } else if (node instanceof String) {
            return Type.JSONSTRING;
        } else if (node instanceof Number) {
            return Type.JSONNUMBER;
        } else if (node instanceof Boolean) {
            return Type.JSONBOOLEAN;
        }
        return Type.JSONNULL;
    }

    /*
     * @brief Parses the JSON spec
     * @return The ASTD object
     */
    public ASTD parse(Bool timed) {
        Object rootNode = null;
        try {
            File f = new File(new File(jsonSpec).getAbsolutePath());
            Constants.CURRENT_PATH = f.getParent();
            Constants.DUMMY_PREFIX = new HashMap<>();
            Constants.DUMMY_PREFIX2 = new LinkedHashMap<>();
            Constants.QUANT_PREF = new HashMap<>();
            Constants.PARAM_OPTM = new HashMap<>();
            rootNode = new JSONParser().parse(new FileReader(f));
        }
        catch (IOException e) {}
        catch (ParseException e) {}

        if(rootNode != null) {
            if(isTraverseable(rootNode)) {
                root = parseRootASTD((JSONObject) rootNode, timed);
                parseImports(root, (JSONObject) rootNode);
                parseTypeDefinitions(root, (JSONObject) rootNode);
                parseOntologyTypes((JSONObject) rootNode);
            }
            if(root != null && root.getName() != null) {
                Constants.DEFAULT_ASTD_NAME = root.getName();
                hasComplexType = false;
                if(root.getOntoClasses() != null)
                    hasComplexType = !root.getOntoClasses().isEmpty();
                if(root.getTypeDefs() != null)
                    hasComplexType = hasComplexType || !root.getTypeDefs().isEmpty();
            }
        }
        if(root != null){
            root.setRoot(true);
            Constants.ASTD_TREE = new HashMap<>();
            makeASTDtree(root);
            Constants.ASTD_TREE.put(root.getName(), null);
        }
        if(timed.getValue()){
            setClocksForParallel(root);
        }
        return root;
    }

    // Flow, Parameterized Synchronization and Quantified Synchronization
    public void setClocksForParallel(ASTD astd){
        if(Constants.BINARY_ASTD_NAMES.contains(astd.getClass().getSimpleName())){
            BinaryASTD bASTD = (BinaryASTD) astd;
            //if it is Flow or Parameterized Synchronization -> add left and right clock
            if(bASTD.getClass().getSimpleName().compareToIgnoreCase(Flow.class.getSimpleName()) == 0 ||
                    bASTD.getClass().getSimpleName().compareToIgnoreCase(Synchronization.class.getSimpleName()) == 0  ||
                    bASTD.getClass().getSimpleName().compareToIgnoreCase(Interleaving.class.getSimpleName()) == 0 ||
                    bASTD.getClass().getSimpleName().compareToIgnoreCase(Interleave.class.getSimpleName()) == 0
            ){
                Set<Variable> attributes = new HashSet<>();
                attributes.add(new Variable(Conventions.LEFT_CLOCK+"_"+bASTD.getLeft().getName(), Conventions.CLOCK, JSONValue.escape("0"), astd.getName()));
                attributes.add(new Variable(Conventions.RIGHT_CLOCK+"_"+bASTD.getRight().getName(), Conventions.CLOCK, JSONValue.escape("0"), astd.getName()));
                List<Variable>  attr = bASTD.getAttributes();
                if(!attr.containsAll(attributes)){
                    attr.addAll(attributes);
                    bASTD.setAttributes(new ArrayList<>(attr));
                    Action actionLeft = bASTD.getLeft().getAstdAction();
                    if(actionLeft != null){
                        bASTD.getLeft().getAstdAction().setCode(actionLeft.getCode()+";\n"+Conventions.LEFT_CLOCK+"_"+bASTD.getLeft().getName()+Conventions.CLOCK_RESET);
                    }
                    else{
                        bASTD.getLeft().setAstdAction(new Action(Conventions.LEFT_CLOCK+"_"+bASTD.getLeft().getName()+Conventions.CLOCK_RESET));
                    }
                    Action actionRight = bASTD.getRight().getAstdAction();
                    if(actionRight != null){
                        bASTD.getRight().getAstdAction().setCode(actionRight.getCode()+";\n"+Conventions.RIGHT_CLOCK+"_"+bASTD.getRight().getName()+Conventions.CLOCK_RESET);
                    }
                    else{
                        bASTD.getRight().setAstdAction(new Action(Conventions.RIGHT_CLOCK+"_"+bASTD.getRight().getName()+Conventions.CLOCK_RESET));
                    }
                }
            }
            //check the children
            setClocksForParallel(bASTD.getLeft());
            setClocksForParallel(bASTD.getRight());
        }
        else if(Constants.UNARY_ASTD_NAMES.contains(astd.getClass().getSimpleName())){
            //check the child
            UnaryASTD uASTD = (UnaryASTD) astd;
            setClocksForParallel(uASTD.getBody());
        }
        else if(Constants.QUANTIFIED_ASTD_NAMES.contains(astd.getClass().getSimpleName())){
            QuantifiedASTD qASTD = (QuantifiedASTD) astd;
            //if it is a ASTD that rolls in parallel, add the clock to the state (add the clock to the child)!
            if(qASTD.getClass().getSimpleName().compareToIgnoreCase(QInterleave.class.getSimpleName()) == 0
                    || qASTD.getClass().getSimpleName().compareToIgnoreCase(QInterleaving.class.getSimpleName()) == 0
                    || qASTD.getClass().getSimpleName().compareToIgnoreCase(QSynchronization.class.getSimpleName()) == 0
                    || qASTD.getClass().getSimpleName().compareToIgnoreCase(QFlow.class.getSimpleName()) == 0){
                Set<Variable> attributes = new HashSet<>();
                attributes.add(new Variable(Conventions.QUANTIFIED_CLOCK+"_"+qASTD.getBody().getName(), Conventions.CLOCK, JSONValue.escape("0"), qASTD.getBody().getName()));
                List<Variable>  attr = qASTD.getBody().getAttributes();
                if(!attr.containsAll(attributes)){
                    attr.addAll(attributes);
                    qASTD.getBody().setAttributes(new ArrayList<>(attr));
                    Action action = qASTD.getBody().getAstdAction();
                    if(action != null){
                        qASTD.getBody().setAstdAction(new Action(action.getCode()+";\n"+Conventions.QUANTIFIED_CLOCK+"_"+qASTD.getBody().getName()+Conventions.CLOCK_RESET));
                    }
                    else{
                        qASTD.getBody().setAstdAction(new Action(Conventions.QUANTIFIED_CLOCK+"_"+qASTD.getBody().getName()+Conventions.CLOCK_RESET));
                    }
                }
            }
            //check the child
            setClocksForParallel(qASTD.getBody());
        }
        else if(Constants.ELEM_ASTD_NAME.contains(astd.getClass().getSimpleName())){
            Automaton autASTD = (Automaton) astd;
            for(Map.Entry<String, ASTD> stateASTD : autASTD.getStatesToASTDs().entrySet()){
                //check the child
                setClocksForParallel(stateASTD.getValue());
            }
        }
    }

    public void makeASTDtree(ASTD astd){
        if(Constants.UNARY_ASTD_NAMES.contains(astd.getClass().getSimpleName())){
            List<String> list = new ArrayList<>();
            UnaryASTD uASTD = (UnaryASTD) astd;
            if(Constants.ASTD_TREE.containsKey(uASTD.getBody().getName())){
                list.addAll(Constants.ASTD_TREE.get(uASTD.getBody().getName()));
            }
            if(Constants.ASTD_TREE.get(uASTD.getBody().getName()) != null) {
                if(!Constants.ASTD_TREE.get(uASTD.getBody().getName()).contains(uASTD.getName())
                && !uASTD.getBody().getName().equals(uASTD.getName())) {
                    list.add(uASTD.getName());
                    Constants.ASTD_TREE.put(uASTD.getBody().getName(), list);
                }
                makeASTDtree(uASTD.getBody());
            }
            else{
                if(!uASTD.getBody().getName().equals(uASTD.getName())){
                    list.add(uASTD.getName());
                    Constants.ASTD_TREE.put(uASTD.getBody().getName(), list);
                }
            }
            makeASTDtree(uASTD.getBody());
        }
        else if(Constants.BINARY_ASTD_NAMES.contains(astd.getClass().getSimpleName())){
            List<String> listRight = new ArrayList<>();
            List<String> listLeft = new ArrayList<>();
            BinaryASTD bASTD = (BinaryASTD) astd;

            if(Constants.ASTD_TREE.containsKey(bASTD.getLeft().getName())){
                listLeft.addAll(Constants.ASTD_TREE.get(bASTD.getLeft().getName()));
            }

            if(Constants.ASTD_TREE.containsKey(bASTD.getRight().getName())){
                listRight.addAll(Constants.ASTD_TREE.get(bASTD.getRight().getName()));
            }

            if(Constants.ASTD_TREE.get(bASTD.getRight().getName()) != null) {
                if (!Constants.ASTD_TREE.get(bASTD.getRight().getName()).contains(bASTD.getName())
                        && !bASTD.getRight().getName().equals(bASTD.getName())) {
                    listRight.add(bASTD.getName());
                    Constants.ASTD_TREE.put(bASTD.getRight().getName(), listRight);
                }
                makeASTDtree(bASTD.getRight());
            }
            else{
                if(!bASTD.getRight().getName().equals(bASTD.getName())){
                    listRight.add(bASTD.getName());
                    Constants.ASTD_TREE.put(bASTD.getRight().getName(), listRight);
                }
            }
            makeASTDtree(bASTD.getRight());

            if(Constants.ASTD_TREE.get(bASTD.getLeft().getName()) != null){
                if(!Constants.ASTD_TREE.get(bASTD.getLeft().getName()).contains(bASTD.getName())
                        && !bASTD.getLeft().getName().equals(bASTD.getName())) {
                    listLeft.add(bASTD.getName());
                    Constants.ASTD_TREE.put(bASTD.getLeft().getName(), listLeft);
                }
                makeASTDtree(bASTD.getLeft());
            }
            else{
                if(!bASTD.getLeft().getName().equals(bASTD.getName())){
                    listLeft.add(bASTD.getName());
                    Constants.ASTD_TREE.put(bASTD.getLeft().getName(), listLeft);
                }
            }
            makeASTDtree(bASTD.getLeft());
        }
        else if(Constants.QUANTIFIED_ASTD_NAMES.contains(astd.getClass().getSimpleName())) {
            List<String> list = new ArrayList<>();
            QuantifiedASTD qASTD = (QuantifiedASTD) astd;
            if (Constants.ASTD_TREE.containsKey(qASTD.getBody().getName())) {
                list.addAll(Constants.ASTD_TREE.get(qASTD.getBody().getName()));
            }
            if (Constants.ASTD_TREE.get(qASTD.getBody().getName()) != null){
                if (!Constants.ASTD_TREE.get(qASTD.getBody().getName()).contains(qASTD.getName())
                        && !qASTD.getBody().getName().equals(qASTD.getName())) {
                    list.add(qASTD.getName());
                    Constants.ASTD_TREE.put(qASTD.getBody().getName(), list);
                }
            }
            else{
                if(!qASTD.getBody().getName().equals(qASTD.getName())) {
                    list.add(qASTD.getName());
                    Constants.ASTD_TREE.put(qASTD.getBody().getName(), list);
                }
            }
            makeASTDtree(qASTD.getBody());
        }
        else if(Constants.ELEM_ASTD_NAME.contains(astd.getClass().getSimpleName())){
            Automaton autASTD = (Automaton) astd;
            for(Map.Entry<String, ASTD> stateASTD : autASTD.getStatesToASTDs().entrySet()){
                List<String> list = new ArrayList<>();
                if(Constants.ASTD_TREE.containsKey(stateASTD.getValue().getName())){
                    list.addAll(Constants.ASTD_TREE.get(stateASTD.getValue().getName()));
                }

                if(Constants.ASTD_TREE.get(stateASTD.getValue().getName()) != null){
                    if(!Constants.ASTD_TREE.get(stateASTD.getValue().getName()).contains(autASTD.getName())
                            && !stateASTD.getValue().getName().equals(autASTD.getName())) {
                        list.add(autASTD.getName());
                        Constants.ASTD_TREE.put(stateASTD.getValue().getName(), list);
                    }
                }
                else{
                    if(!stateASTD.getValue().getName().equals(autASTD.getName())) {
                        list.add(autASTD.getName());
                        Constants.ASTD_TREE.put(stateASTD.getValue().getName(), list);
                    }
                }
                makeASTDtree(stateASTD.getValue());
            }
        }
    }
}
