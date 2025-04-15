package com.udes.parser;

import com.udes.track.ExecSchema;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import com.udes.model.il.terms.Bool;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;


public class ExecSchemaParser implements Parser<ExecSchema> {

    private String jsonSchema;
    private final String DEFINITIONS = "definitions";
    private final String ASTD        = "astd";
    private final String ONEOF       = "oneOf";
    private final String REQUIRED    = "required";
    private final String PROPERTIES  = "properties";
    private final String ITEMS       = "items";
    private final String ENUM        = "enum";

    public static final String AUTOMATON   = "Automaton";
    public static final String SEQUENCE    = "Sequence";
    public static final String KLEENE      = "Kleene";
    public static final String SYNCHRONIZATION   = "Synchronization";
    public static final String QCHOICE           = "QChoice";
    public static final String CHOICE            = "Choice";
    public static final String QSYNCHRONIZATION  = "QSynchronization";
    public static final String QFLOW = "QFlow";
    public static final String QINTERLEAVING  = "QInterleaving";
    public static final String INTERLEAVING  = "Interleaving";
    public static final String GUARD       = "Guard";
    public static final String CALL        = "Call";
    public static final String FLOW        = "Flow";
    public static final String ELEM        = "Elem";

    // Common type properties
    public static final String EXECUTED_EVENT   = "executed_event";
    public static final String TOP_LEVEL_ASTD   = "top_level_astd";
    public static final String NAME             = "name";
    public static final String ATTRIBUTES       = "attributes";
    public static final String CURRENT_VALUE    = "current_value";
    public static final String CURRENT_SUB_STATE = "current_sub_state";
    public static final String TYPE              = "type";

    // Automaton type properties
    public static final String CURRENT_STATE_NAME   = "current_state_name";
    public static final String HISTORY     = "history";
    public static final String STATE       = "state";

    // Sequence type properties
    public static final String STEP   = "step";

    // Choice type properties
    public static final String SIDE  = "side";

    // Kleene type properties
    public static final String STARTED = "started";

    // Synchronization type properties
    public static final String LEFT = "left";
    public static final String RIGHT = "right";

    // QChoice type properties
    public static final String QCHOICE_VAR = "qchoice_var";
    public static final String VALUE = "value";

    // QSynchronization properties
    public static final String SUB_STATES  = "sub_states";
    public static final String QSYNCH_VAR = "qsynch_var";
    public static final String QINTER_VAR = "qinter_var";
    public static final String QFLOW_VAR = "qinter_var";

    //Call properties
    public static final String CALLED_ASTD = "called_astd";


    /* 
     * 
     * @brief Gets the json specification
     * @param JSON specification
     */
    public ExecSchemaParser(String jsonSchema) {
        this.jsonSchema = jsonSchema;
    }

    /*
     * @brief Parses the JSON schema describing the current execution state
     * @return 
     */
    public ExecSchema parse(Bool b) {
        ExecSchema execSchema = new ExecSchema();
        Object rootNode = null;
        try {
            File f   = new File(jsonSchema);
            rootNode = new JSONParser().parse(new FileReader(f));
        }
        catch (IOException e) {}
        catch (ParseException e) {}

        JSONArray topLevelObj = (JSONArray)(((JSONObject)rootNode).get(REQUIRED));
        List<String> topLevelProps =  new ArrayList<>();
        for(Object o1 : topLevelObj) {
            topLevelProps.add((String) o1);
        }
        execSchema.setTopLevelProps(topLevelProps);

        JSONObject defObj  = (JSONObject)((JSONObject)rootNode).get(DEFINITIONS),
                   astdObj = (JSONObject) defObj.get(ASTD),
                   attrObj = (JSONObject) defObj.get(ATTRIBUTES);
        
        JSONObject itObj  = (JSONObject) attrObj.get(ITEMS);
        JSONArray  reList = (JSONArray) itObj.get(REQUIRED);
        List<String> attributeProps = extractProps(reList);

        execSchema.setAttributeProps(attributeProps);

        JSONArray oneOfObj = (JSONArray) astdObj.get(ONEOF);

        List<String> requiredProps = new ArrayList<>(),
                     historyStateProps = new ArrayList<>(),
                     qSubStateProps    = new ArrayList<>();
        Map<String, List<String>> astdProps  = new HashMap<>();
        for(Object o3 : oneOfObj) {
           JSONObject jo          = (JSONObject) o3;
           JSONObject propType    = (JSONObject) jo.get(PROPERTIES);
           JSONObject enumType    = (JSONObject)propType.get(TYPE);
           JSONArray enumVal      = (JSONArray) enumType.get(ENUM);
            String astdType = null;
           if(enumVal != null) {
               Iterator it = enumVal.iterator();

               if (it.hasNext())
                   astdType = (String) it.next();
           }

           JSONArray requiredList = (JSONArray) jo.get(REQUIRED);
           requiredProps = extractProps(requiredList);

           if(astdType != null)
               astdProps.put(astdType, requiredProps);
    
           if(astdType != null && astdType.equals(AUTOMATON)) {
               JSONObject hisObj  = (JSONObject) propType.get(HISTORY);

               JSONObject itemObj = (JSONObject) hisObj.get(ITEMS);
               JSONArray  reqList = (JSONArray)  itemObj.get(REQUIRED);
               historyStateProps = extractProps(reqList);
           }

           if(astdType != null && astdType.equals(QSYNCHRONIZATION)) {
               JSONObject sList = (JSONObject) propType.get(SUB_STATES);
               JSONArray oneOf = (JSONArray) sList.get(ONEOF);
               for (Object o5 : oneOf) {
                   JSONObject tmpObj = (JSONObject) o5;
                   String typ = (String) tmpObj.get(TYPE);
                   if(typ.contains("null"))
                       continue;
                   JSONObject itemObj = (JSONObject) tmpObj.get(ITEMS);
                   JSONArray  rList = (JSONArray)  itemObj.get(REQUIRED);
                   qSubStateProps = extractProps(rList);
               }
           }
        }

        execSchema.setASTDProps(astdProps);
        execSchema.setHistoryStateProps(historyStateProps);
        execSchema.setQSubStateProps(qSubStateProps);

        return execSchema;
    }

    protected List<String> extractProps(JSONArray propObj) {
        List<String> props =  new ArrayList<>();

        if(propObj != null) {
            for (Object o : propObj) {
                props.add((String) o);
            }
        }

        return props;
    }
}