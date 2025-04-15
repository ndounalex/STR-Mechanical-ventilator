package com.udes.model.il.conventions;

import com.udes.model.astd.base.ASTD;
import com.udes.model.astd.base.BinaryASTD;
import com.udes.model.astd.base.QuantifiedASTD;
import com.udes.model.astd.base.UnaryASTD;
import com.udes.model.astd.types.*;
import com.udes.model.il.conditions.CallCondition;
import com.udes.model.il.conditions.Condition;
import com.udes.model.il.statements.CallStatement;
import com.udes.model.il.statements.IFFIStatement;
import com.udes.model.il.statements.ForStatement;
import com.udes.model.il.statements.SeqStatement;
import com.udes.model.il.statements.Statement;
import com.udes.translator.ILTranslator;
import com.udes.utils.Utils;

// IL conventions
public class Conventions {

    //Generic ASTD
    private static String STRUCT="TState_$";
    public static String STRUCT_VAR="ts";
    public static String MAIN_FUNCTION="main";
    public static String TRUE="1";
    public static String EVENT="_evt";
    public static String EVENT_TEXT="event";
    public static String EVENT_TYPE = "Event";
    public static String CONFIG_INPUT_STREAM="configInputStream";
    public static String READ_EVENT="read_event";
    public static String EVENT_LABEL="_evt.label";
    public static String LABEL = ".label";
    public static String EVENT_PARAMS="_evt.params";
    public static String EVENT_SOURCE="src";
    public static String ERROR_LABEL="error";
    public static String ERROR_LABEL1="error1";
    public static String ERROR_LABEL3="error3";
    public static String ERROR_LABEL4="error4";

    public static String ERROR_LABEL5="error5";
    public static String EXCEPTION_LABEL="Exception";
    public static String RETURN_CALL = "return";
    public static String EMPTY="empty";
    public static String BREAK="break";
    public static String ERROR_MSG="Event $ is not recognized";
    public static String ERROR_MSG1="Event $ is not executable";
    public static String ERROR_MSG2="Method toString() is undefined in User types";
    public static String ERROR_MSG3="Method parse(...) is undefined in User types";
    public static String ERROR_MSG4="Empty event label, terminating execution";
    public static String SAFE_EXEC_CALL = "_safe_";
    public static String STR_TO_INT = "get_int";
    public static String STR_TO_BOOL = "get_bool";
    public static String STR_TO_STR = "get_str";
    public static String STR_TO_FLOAT = "get_float";
    public static String STR_TO_DOUBLE = "get_double";
    public static String STR_TO_ONTO = "get_user_type";

    public static String STATE="State";
    private static String SET_OF="Set<$>";
    public static String SET="Set";
    private static String MAP_OF="Map<$, #>";
    private static String ENTRY_OF="Entry<$, #>";
    public static String ARRAY_ELEM = "#[$]";
    public static String MAP="Map";
    public static String VECTOR="Vector";
    public static String LIST="List";
    public static String ENTRY="Entry";
    public static String TYPES="Types";

    //Generic TASTD
    public static String STEP="Step";
    public static String CST="current_system_time";
    public static String CST_VAR="std::chrono::duration_cast<std::chrono::nanoseconds>((std::chrono::system_clock::now().time_since_epoch())).count()";
    public static String CLOCK_RESET = ".reset_clock()";
    public static String LETS="last_event_time";
    public static String LST = "last_step_time";
    public static String STEP_VAR="step_time";
    public static String TIME_TYPE="time_type";
    public static String TIME_TYPE2="time_type2";
    public static String TIME_TYPE3="time_type";
    public static String CLOCK_TIMED_INTERRUPT = "clock_timed_interrupt";
    public static String LEFT_CLOCK = "left_clock";
    public static String RIGHT_CLOCK = "right_clock";
    public static String QUANTIFIED_CLOCK = "quantified_clock";
    public static String EXPIRED = "expired";
    public static String PASSEDTIME = ".getPassedTime()";
    public static String CURRENTTIME = ".getTimeStamp()";
    public static String TIME_START = "exec_start";
    public static String CURRENT_TIME = "current_time";
    public static String SIMULATION = "simulation";
    public static String MTX = "mtx";
    public static String LOCK = "lock";
    public static String UNLOCK = "unlock";
    public static String MUTEX = "std::mutex";

    //Automaton
    private static String AUT_STATE_PREFIX="Aut";
    public static String HISTORY_STATE="hState_";
    private static String HISTORY_STATE_CALL="hState_$[$]";
    private static String SF_STATES="shallow_final_$";
    private static String DF_STATES="deep_final_$";
    public static String CAST_FUNCTION="cast";
    public static String EXEC="exec";

    // Guard
    public static String GNOTSTARTED="GUARD_NOTSTARTED";
    public static String GSTARTED="GUARD_STARTED";
    public static String SKIP="skip";

    //Kleene
    public static String KNOTSTARTED="KLEENE_NOTSTARTED";
    public static String KSTARTED="KLEENE_STARTED";

    //Sequence
    public static String FIRST="SEQ_FST";
    public static String SECOND="SEQ_SND";

    //Interrupt
    public static String INT_FIRST="INT_FIRST";
    public static String INT_SECOND="INT_SECOND";

    //Choice
    public static String NONE="NONE";
    public static String LEFT="LEFT";
    public static String RIGHT="RIGHT";

    //Flow
    public static String BOOL_VAR="flag";
    public static String BOOL_TYPE="boolean";
    public static String BOOL_TYPE1="bool";
    public static String FALSE="0";

    // Quantified ASTD
    public  static String FUNC_STATE="f";
    private static String VAR_SET="T_$";
    public static String EXISTS="exists";
    public static String FOR_ALL="forall";
    public static String CHOICE_VAR="c";
    public static String NIL="nil";
    public static String CHOICE_NIL="0";
    public static String GOTO="goto";
    public static String GOTOFUNC="gotofunc";

    //Call
    public static String NOTCALLED="NOTCALLED";
    public static String CALLED="CALLED";
    
    // IL operators
    public static String EQUALS = "eq";
    public static String EQUALS1 = "_eq";
    public static String IN = "in";
    public static String NOT_IN = "not_in";
    public static String IN_MAP = "in_map";
    public static String NOT_IN_MAP = "not_in_map";
    public static String NOT_EQUALS = "neq";
    public static String LESSER = "lesser";
    public static String LESSER_EQUALS = "lesser_eq";
    public static String GREATER = "greater";
    public static String GREATER_EQUALS = "greater_eq";

    // loop
    public static String FOR = "for";
    public static String WHILE = "while";
    public static String CONTINUE = "continue";

    // default types
    public static String INT = "int";
    public static String DOUBLE = "double";
    public static String FLOAT = "float";
    public static String STRING = "string";
    public static String SHORT = "short";
    public static String LONG = "long";
    public static String CLOCK = "Timer";
    public static String AUTO_CONST_TYPE = "auto const";

    // user defined types
    //TODO: define it in a yaml config file
    public static String TIMER = "Code::startTimer";
    public static String JSON = "Json";
    public static String PACKET = "Packet";
    public static String SESSION = "Session";
    public static String FLOW = "Flow";
    public static String LOG = "Log";
    public static String HTTPSESSION = "HTTPSession";
    public static String DNSSESSION = "DNSSession";
    public static String WINEVENTLOG = "WinEventLog";
    public static String SYSLOG= "SysLog";

    // show execution state
    public static String EXEC_STATE_INIT = "exec_state_to_json";
    public static String EXEC_STATE_ACTION = "exec_state_to_json";
    public static String EXEC_STATE_CLOSE = "exec_state_close";
    public static String EXEC_STATE_SENDTO_EASTD = "exec_state_sendto_eastd";
    public static String IS_EXEC_STATE_ENABLED = "is_exec_state_enabled";
    public static String EXEC_STATE_BUFFER = "exec_state";
    public static String TEMP_JSON_VAR_ATTRIBUTE = "attr";
    public static String TEMP_JSON_ITEM_ATTRIBUTE = "item";
    public static String FINAL = "final";

    // json properties
    public static String JSON_ARRAY_INSTANCE = "json::array()";
    public static String JSON_OBJECT_INSTANCE = "json::object()";
    public static String JSON_PARSE = "json::parse($)";
    public static String ADD_ELEM_TO_VECTOR = "#.push_back";
    public static String REMOVE_ELEM_TO_VECTOR = "#.remove";
    public static String DUMP_JSON_OBJECT = "#.dump()";
    public static String COMPLEX_TYPE_TO_STRING = "call_to_string($)";
    public static String TO_STRING     = "std::to_string($)";
    public static String INSTANCE_OF = "instanceof<json, $>";
    public static String HAS_TOSTRING = "HasMethod<$>::has_to_string";
    public static String HAS_PARSE = "HasMethod<$>::has_parse";
    public static String CLEAR_VECTOR  = "#.clear()";

    public static String DUMMY_PARAMS = "_";

    //threads
    public static String THREAD_DEC="pthread_t";
    public static String CONSUMER="consumer";
    public static String ARRAY0 ="[0]";
    public static String ARRAY1 ="[1]";
    public static String ARRAY2 ="[2]";
    public static String PRODUCER="producers";
    public static String THREAD_CRE="pthread_create";
    public static String THREAD_JOIN="pthread_join";
    public static String _FUNC = "_func";
    public static String SIGNAL = "continue_signal";
    public static String THREAD_SLEEP = "thread_sleep";

    //queue
    public static String QUEUE = "queue";
    public static String QUEUE_T = "queue_type";
    public static String CONSUMED = "_evt";
    public static String INPUT_EVENT = "input_event";
    public static String ENQUEUE = "enqueue";
    public static String WAIT_DEQ = "wait_dequeue";


    public static String getStateType(Class c) {
        String astdName=c.getSimpleName();
        if(astdName.startsWith(AUT_STATE_PREFIX))
            return  AUT_STATE_PREFIX + STATE;
        else
            return astdName + STATE;
    }

    public static String getStateVar(Class c) {
        String astdName=c.getSimpleName().toLowerCase();
        String prefix= AUT_STATE_PREFIX.toLowerCase();
        if(astdName.startsWith(prefix))
            return prefix + STATE;
        else
            return astdName + STATE;
    }

    public static String getStateCall(String qvar) {
        return  FUNC_STATE + "[" + qvar + "]";
    }
    
    public static String getStructName(String name) {
        return STRUCT.replace(ILTranslator.USYMBOL_1, name);
    }

    public static String getStructVar(String name) {
        return STRUCT_VAR + "_" + name;
    }
    
    public static String getMapType(String type1, String type2){
        return MAP_OF.replace(ILTranslator.USYMBOL_1, type1).replace(ILTranslator.USYMBOL_2, type2);
    }

    public static String getSetType(String type){
        return SET_OF.replace(ILTranslator.USYMBOL_1, type);
    }

    public static String hasToStringMethodInClass(String type){
        return HAS_TOSTRING.replace(ILTranslator.USYMBOL_1, type);
    }

    public static String hasParseMethodInClass(String type) {
        return HAS_PARSE.replace(ILTranslator.USYMBOL_1, type);
    }

    public static String getVarSet(String name){
        return VAR_SET.replace(ILTranslator.USYMBOL_1, name);
    }

    public static String getErrorMsg(String evtName, int idx) {
        if(idx == 0)
            return (evtName != null) ? ERROR_MSG.replace(ILTranslator.USYMBOL_1, evtName)  : ERROR_MSG;
        else if(idx == 1)
            return (evtName != null) ? ERROR_MSG1.replace(ILTranslator.USYMBOL_1, evtName) : ERROR_MSG1;
        else if(idx == 2)
            return ERROR_MSG2;
        else if(idx == 3)
            return ERROR_MSG3;
        else if(idx == 4)
            return ERROR_MSG4;
        else
            return null;
    }

    public static String getShallowFinalVar(String name){
        return SF_STATES.replace(ILTranslator.USYMBOL_1, name);
    }

    public static String getDeepFinalVar(String name){
        return DF_STATES.replace(ILTranslator.USYMBOL_1, name);
    }

    public static String getHistoryCall(String id){
        return HISTORY_STATE_CALL.replace(ILTranslator.USYMBOL_1, id);
    }

    public static String cast(String name) {
        return CAST_FUNCTION + "(" + getStructName(name) + "," + STRUCT_VAR + ")";
    }

    public static String getMapType1(String type1, ASTD astd) {
        if (isQChoice(astd))
            return MAP_OF.replace(ILTranslator.USYMBOL_1, type1).replace("#", INT);
        else if (isQuantifiedASTD(astd))
            return getMapType(type1, Conventions.getMapType(((QInterleaving) astd).getQvariable().getType(),
                    Conventions.getStructName(((QInterleaving) astd).getBody().getName())));
        else if (isAutomaton(astd))
            return ENTRY_OF.replace(ILTranslator.USYMBOL_1, type1).replace(ILTranslator.USYMBOL_2, getStructName(astd.getName()));
        else
            return MAP_OF.replace(ILTranslator.USYMBOL_1, type1).replace(ILTranslator.USYMBOL_2, getStructName(astd.getName()));
    }

    public static boolean isQChoice(ASTD astd) {
        return astd instanceof QChoice;
    }

    public static boolean isQuantifiedASTD(ASTD astd) {
        return astd instanceof QuantifiedASTD;
    }

    public static boolean isQSynchronization(ASTD astd) {
        return astd instanceof QSynchronization;
    }

    public static boolean isQFlow(ASTD astd) {
        return astd instanceof QFlow;
    }
    public static boolean isQInterleaving(ASTD astd) {
        return astd instanceof QInterleaving;
    }

    public static boolean isAutomaton(ASTD astd) {
        return astd instanceof Automaton;
    }

    public static boolean isUnaryASTD(ASTD astd){return astd instanceof UnaryASTD;}

    public static boolean isBinaryASTD(ASTD astd){return astd instanceof BinaryASTD;}

    public static boolean isSequence(ASTD astd) {
        return astd instanceof Sequence;
    }

    public static boolean isFlow(ASTD astd) {
        return astd instanceof Flow;
    }

    public static boolean isChoice(ASTD astd) {
        return astd instanceof Choice;
    }

    public static boolean isSynchronization(ASTD astd) {
        return astd instanceof Synchronization;
    }

    public static boolean isInterleaving(ASTD astd) {
        return astd instanceof Interleaving;
    }

    public static boolean isCall(ASTD astd) {
        return astd instanceof Call;
    }

    public static boolean isGuard(ASTD astd) {
        return astd instanceof Guard;
    }

    public static boolean isKleene(ASTD astd) {
        return astd instanceof Kleene;
    }

    public static boolean isIFFI(Statement stmt) {
        return stmt instanceof IFFIStatement;
    }

    public static boolean isFor(Statement stmt) {
        return stmt instanceof ForStatement;
    }

    public static boolean isSeqStatement(Statement stmt) {
        return stmt instanceof SeqStatement;
    }

    public static boolean isElem(ASTD astd) { return astd instanceof Elem; }

    public static String getTypeOf(ASTD astd, String idx) {
        if(isAutomaton(astd))
            return Conventions.getStateVar(Automaton.class);
        if(isSequence(astd))
            return Conventions.getStateVar(Sequence.class);
        if(isKleene(astd))
            return Conventions.getStateVar(Kleene.class);
        if(isGuard(astd))
            return Conventions.getStateVar(Guard.class);
        if(isCall(astd))
            return Conventions.getStateVar(Call.class);
        if(isChoice(astd))
            return Conventions.getStateVar(Choice.class);
        if(isQChoice(astd))
            return Conventions.TO_STRING.replace(ILTranslator.USYMBOL_1, idx);
        return null;
    }
}
