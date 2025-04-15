package com.udes.utils;

import com.udes.model.astd.base.ASTD;
import com.udes.model.astd.items.Event;
import com.udes.model.astd.items.Variable;

import java.util.*;

public class Constants {

    public static boolean DEBUG = false;
    public static String CASTD_VERSION = "1.";
    public static String CASTD_BUILD_VERSION = "24";
    public static String CASTD_RELEASE_VERSION = "3";
    public static String CASTD_DEBUG_VERSION = "200";
    public static String HAS_VERSION_ARG = "version";
    public static String HAS_HELP_ARG = "help";
    public static String SHORTHANDEVENTS_FORMAT = "shorthandevents";
    public static String JSON_FORMAT = "json";
    public static String SCHEMA = "schema";
    public static String EVT_FORMAT = SHORTHANDEVENTS_FORMAT;

    public static boolean COND_OPT_OPTS = false;
    public static boolean KAPPA_DIRECT_OPTS = false;
    public static boolean STEP_AS_FLOW = false;
    public static boolean TIMED_SIMULATION = false;
    public static String INITIAL_TIME = "0";

    public static String DEFAULT_ASTD_NAME = "MAIN";
    public static String PREFIX_UNKNOW_NAME = "ASTD";
    public static String CASTD_NAME       = "castd";
    public static String ANY_MODE         = "ANY";
    public static String DOM_TYPE         = "";
    public static String EXCEPT_SPECIAL_CHAR = "[^A-Za-z0-9]";

 
    public static List<String> UNARY_ASTD_NAMES = Arrays.asList(
         "Kleene", "Guard", "Call", "PersistentGuard"
    );

    public static List<String> BINARY_ASTD_NAMES = Arrays.asList(
        "Sequence", "Choice", "Interleaving", "Interleave",
        "ParallelComposition", "Flow", "Synchronization", "Interrupt"
    );

    public static List<String> QUANTIFIED_ASTD_NAMES = Arrays.asList(
         "QChoice", "QInterleaving", "QInterleave", "QSynchronization", "QParallelComposition","QFlow"
    );

    public static List<String> TRANSFORM_ASTD_NAMES = Arrays.asList(
            "Delay", "PersistentDelay", "Timeout", "PersistentTimeout", "TimedInterrupt"
    );

    public static List<String> ELEM_ASTD_NAME = Collections.singletonList("Automaton");

    public static String NUMBER_LIST  = "\\{([,.0-9]+)\\}";
    public static String NUMBER_LIST2 = "\\[([,.0-9]+)\\]";
    public static String STRING_LIST  = "\\{([,'@:\".a-z0-9A-Z]+)\\}";
    public static String STRING_LIST2 = "\\[([,'@:\".a-z0-9A-Z]+)\\]";
    public static String INT_INTERVAL = "\\[(\\d+)-(\\d+)\\]";
    public static String ID           = "[^\"]+";
    public static String ACTION_DIV   = "[^;]+";
    public static String BOOLEAN_DIV  = "[^\\s]+";
    public static String BOOLEAN_SEP = "\\b([\\|&!><=]+)";
    public static String BOOLEAN_SEP2 = "\\B([\\|&!><=]+)";
    public static String FUNC_PARAMS  = "([ =a-zA-Z0-9_.:\\->$\\]\\[]+)\\s*\\(\\s*([a-zA-Z0-9_$.\\(\\)]+(?:\\s*,\\s*[a-zA-Z0-9_$.\\(\\)]+)*)\\s*\\)";
    public static String FUNC_SANS_PARAMS  = "([ =a-zA-Z0-9_.:\\->$\\]\\[]+)\\s*\\(\\s*([a-zA-Z0-9_$.]*+(?:\\s*,\\s*[a-zA-Z0-9_$.]+)*)\\s*\\)";
    public static String BOOLEAN_PARAMS = "([a-zA-Z0-9_.:$()]+)\\s(=|!|>|<)\\s([a-zA-Z0-9_.:$()]+)";
    public static int    COUNT        = 0;

    public static String CPP_FILE_EXTENSION   = ".cpp";

    public static String TONUL                 = "2>nul";
    public static String CPP_HDR_EXTENSION    = ".h";
    public static String CPP_HDR2_EXTENSION    = ".hpp";
    public static String FILTER_CPP_HDR_EXTENSION   = "$(wildcard *.h)";
    public static String FILTER_CPP_HDR2_EXTENSION   = "$(wildcard *.hpp)";
    public static String FILTER_CPP_FILE_EXTENSION  = "$(wildcard *.cpp)";
    public static String CPP_BINARY_EXTENSION = ".o";
    public static String JAVA_FILE_EXTENSION  = ".java";
    public static String JAVA_EXEC_EXTENSION  = ".jar";
    public static String JAVA_BINARY          = ".class";
    public static String FIRST_ITEM           = ".first";
    public static String SECOND_ITEM          = ".second";
    public static String SIZE         = ".size()";
    public static String OCAML_FILE_EXTENSION = ".ml";
    public static String REGEX_FILE_EXTENSION = "\\.(cpp|h|java|ml)";
    public static String DEFAULT              = "default";


    public static String MAKEFILENAME = "Makefile";
    public static String HELPER       = "helper";
    public static String BLOCKINGQUEUE= "blockingconcurrentqueue";
    public static String CONCURRENTQUEUE = "concurrentqueue";
    public static String SEMAPHORE = "lightweightsemaphore";
    public static String PTHREAD      = "pthread";
    public static String THREAD      = "thread";
    public static String MUTEX        = "mutex";
    public static String CHRONO       = "chrono";
    public static String CTIME        = "ctime";
    public static String ATOMIC       = "atomic";
    public static String INTTYPES     = "inttypes";
    public static String LOGGER       = "logger";
    public static String CLIENT       = "client";
    public static String EXEC_SCHEMA  = "exec_schema";
    public static String HEADERS      = "HEADERS";
    public static List<String> CLIENT_CPP_FILES;
    public static List<String> CLIENT_HDR_FILES;
    public static List<String> CLIENT_HPP_FILES;

    public static String GCC          = "g++";
    public static String CC           = "CC";
    public static String CFLAGS       = "CFLAGS";
    public static String LDLIBS       = "LDLIBS";
    public static String INCLUDE      = "INCLUDE";
    public static String TARGET       = "TARGET";
    public static String OBJECTS      = "OBJECTS";
    public static String OPTMIZATION_OPTS = "-std=c++11 -g -Wall -O1";
    public static String GCC_INPUT_OPTS   = "-c";
    public static String GCC_OUTPUT_OPTS  = "-o";
    public static String GCC_LDFLAGS_OPTS  = "-L";
    public static String GCC_INCLUDE_OPTS  = "-I";
    public static String CLEAN_OPTS       = "-rm -f ";

    public static String CLEAN_OPTS_WIN       = "-del /F /Q ";
    public static String FILTER_OUT_OPTS  = "$(filter-out";
    public static String HEADER_VARS      = "$(HEADERS)";
    public static String CC_VARS          = "$(CC)";
    public static String CFLAG_VARS       = "$(CFLAGS)";
    public static String INCLUDE_VARS       = "$(INCLUDE)";
    public static String LDLIBS_VARS       = "$(LDLIBS)";
    public static String TARGET_VARS      = "$(TARGET)";
    public static String OBJECT_VARS      = "$(OBJECTS)";
    public static String PHONY            = ".PHONY";
    public static String PRECIOUS         = ".PRECIOUS";

    public static String EXECUTABLE       = ".exe";
    public static String CLEAN            = "clean";
    public static String ALL              = "all";
    public static String CLEAN_ALL = DEFAULT + " " + ALL + " "+ CLEAN;
    public static String MAKE             = "make";

    public static String JAVAC        = "javac";
    public static String JAVA         = "java";
    public static String JAVA_OPTS    = "-cp";
    public static String JAVA_OPTS_2  = "-classpath";

    public static String CURRENT_PATH;
    public static HashMap<Variable, String> DUMMY_PREFIX;
    public static LinkedHashMap<Variable, ASTD> DUMMY_PREFIX2;
    public static HashMap<String, HashMap<String, String>> PARAM_OPTM;
    public static HashMap<String, List<String>> ASTD_TREE; //The map is child -> parent;
    public static HashMap<Variable, ArrayList<ASTD>> QUANT_PREF;
    public static String UNBOUNDEDDOMAIN = "UnboundedDomain";
    public static boolean EXEC_STATE_ACTIVATED;

    public static String  CASTD_SUCCESS = "[Success] cASTD successfully generated code. Done !\n";
    public static String  CASTD_FAILED  = "[Failed] cASTD code generation failed because of the following error ->\n";
    public static boolean ERROR_FOUND = false;
    public static boolean PROCESSED_HISTORY_STATE =  false;

    //Clock Constants
    public static String HELPER_CLOCK = "\n" +
            "/*\n" +
            "* @brief Manages clock variables.\n" +
            "*\n" +
            "*/\n" +
            "class Timer {\n" +
            "private:\n" +
            "        std::time_t time_stamp;\n" +
            "\n" +
            "public:\n" +
            "    //constructor definition\n" +
            "    Timer(){\n" +
            "        time_stamp = std::chrono::duration_cast<std::chrono::nanoseconds>((std::chrono::system_clock::now().time_since_epoch())).count();\n" +
            "    }\n" +
            "\n" +
            "    bool expired(double duration){\n" +
            "        return (std::chrono::duration_cast<std::chrono::nanoseconds>((std::chrono::system_clock::now().time_since_epoch())).count() >= time_stamp + duration);\n" +
            "    }\n" +
            "\n" +
            "    std::time_t getPassedTime(){\n" +
            "        return (std::chrono::duration_cast<std::chrono::nanoseconds>((std::chrono::system_clock::now().time_since_epoch())).count() - time_stamp);\n" +
            "    }\n" +
            "\n" +
            "    std::time_t getTimeStamp(){\n" +
            "        return time_stamp;\n" +
            "    }\n" +
            "\n" +
            "    void reset_clock(){\n" +
            "        time_stamp = std::chrono::duration_cast<std::chrono::nanoseconds>((std::chrono::system_clock::now().time_since_epoch())).count();\n" +
            "    }\n" +
            "\n" +
            "    void reset_clock(Timer ts){\n" +
            "        time_stamp = ts.getTimeStamp();\n" +
            "    }\n" +
            "\n" +
            "    void setTimeStamp(){\n" +
            "\n" +
            "    }\n" +
            "};";

    public static String HELPER_CLOCK_SIMULATION = "\n" +
            "\n" +
            ";/*\n" +
            "* @brief Manages clock variables.\n" +
            "*\n" +
            "*/\n" +
            "class Timer {\n" +
            "private:\n" +
            "        std::time_t time_stamp;\n" +
            "\n" +
            "public:\n" +
            "    //constructor definition\n" +
            "    Timer(std::time_t current_time){\n" +
            "        time_stamp = current_time;\n" +
            "    }\n" +
            "\n" +
            "    bool expired(double duration, std::time_t current_time){\n" +
            "        return (current_time >= time_stamp + duration);\n" +
            "    }\n" +
            "\n" +
            "    std::time_t getPassedTime(std::time_t current_time){\n" +
            "        return (current_time - time_stamp);\n" +
            "    }\n" +
            "\n" +
            "    std::time_t getTimeStamp(){\n" +
            "        return time_stamp;\n" +
            "    }\n" +
            "\n" +
            "    void reset_clock(std::time_t current_time){\n" +
            "        time_stamp = current_time;\n" +
            "    }\n" +
            "\n" +
            "    void reset_clock(Timer ts){\n" +
            "        time_stamp = ts.getTimeStamp();\n" +
            "    }\n" +
            "};\n" +
            "\n" +
            "//functions to simulate advance time\n" +
            "\n" +
            "std::time_t convertToNano( std::time_t hour, std::time_t min, std::time_t sec, std::time_t millisec, std::time_t microsec, std::time_t nanosec){\n" +
            "    std::time_t currentTime = 0;\n" +
            "    currentTime = (((hour * 60 * 60) + (min * 60) + sec) * 1000000000) + (millisec * 1000000) + (microsec * 1000) + nanosec;\n" +
            "    return currentTime;\n" +
            "}\n" +
            "\n" +
            "long int advanceToV1(std::time_t time_stamp, std::time_t step_time, std::string mixedvalues){\n" +
            "    \n" +
            "    std::time_t hour = 0;\n" +
            "    std::time_t min = 0;\n" +
            "    std::time_t sec = 0;\n" +
            "    std::time_t millisec = 0;\n" +
            "    std::time_t microsec = 0;\n" +
            "    std::time_t nanosec = 0;\n" +
            "\n" +
            "    const char* data = mixedvalues.c_str();\n" +
            "    char *ep;\n" +
            "\n" +
            "    hour = strtol(data, &ep, 10);\n" +
            "    if (!(!ep || *ep != ':')) {\n" +
            "        min = strtol(ep+1, &ep, 10);\n" +
            "        if (!(!ep || *ep != ':')) {\n" +
            "            sec = strtol(ep+1, &ep, 10);\n" +
            "            if (!(!ep || *ep != ':')) {    \n" +
            "                millisec = strtol(ep+1, &ep, 10);\n" +
            "                if (!(!ep || *ep != ':')) {\n" +
            "                    microsec = strtol(ep+1, &ep, 10);\n" +
            "                    if (!(!ep || *ep != ':')) {\n" +
            "                        nanosec = strtol(ep+1, &ep, 10);\n" +
            "                    }\n" +
            "                }\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "    \n" +
            "    std::time_t calc_time = convertToNano(hour, min, sec, millisec, microsec, nanosec);\n" +
            "\n" +
            "    if(time_stamp < calc_time){    \n" +
            "        long int numberOfSteps = calc_time / step_time;\n" +
            "        return numberOfSteps;\n" +
            "    }\n" +
            "    else{\n" +
            "        return -1;\n" +
            "    }\n" +
            "}\n" +
            "\n" +
            "long int advanceToV2(std::time_t timer, std::time_t step_time, std::string unit, std::time_t value){\n" +
            "\n" +
            "    std::time_t hour = 0;\n" +
            "    std::time_t min = 0;\n" +
            "    std::time_t sec = 0;\n" +
            "    std::time_t millisec = 0;\n" +
            "    std::time_t microsec = 0;\n" +
            "    std::time_t nanosec = 0;\n" +
            "\n" +
            "    if(unit == \"hour\"){\n" +
            "        hour = value;\n" +
            "    }\n" +
            "    else if(unit == \"min\"){\n" +
            "        min = value;\n" +
            "    }\n" +
            "    else if(unit == \"sec\"){\n" +
            "        sec = value;\n" +
            "    }\n" +
            "    else if(unit == \"millisec\"){\n" +
            "        millisec = value;\n" +
            "    }\n" +
            "    else if(unit == \"microsec\"){\n" +
            "        microsec = value;\n" +
            "    }\n" +
            "    else if(unit == \"nanosec\"){\n" +
            "        nanosec = value;\n" +
            "    }\n" +
            "\n" +
            "    std::time_t calc_time = convertToNano(hour, min, sec, millisec, microsec, nanosec);\n" +
            "\n" +
            "    if(timer < calc_time){    \n" +
            "        long int numberOfSteps = calc_time / step_time;\n" +
            "        return numberOfSteps;\n" +
            "    }\n" +
            "    else{\n" +
            "        return -1;\n" +
            "    }\n" +
            "}\n" +
            "\n" +
            "long int advanceOfV1(std::time_t step_time, std::string mixedvalues){\n" +
            "    \n" +
            "    std::time_t hour = 0;\n" +
            "    std::time_t min = 0;\n" +
            "    std::time_t sec = 0;\n" +
            "    std::time_t millisec = 0;\n" +
            "    std::time_t microsec = 0;\n" +
            "    std::time_t nanosec = 0;\n" +
            "\n" +
            "    const char* data = mixedvalues.c_str();\n" +
            "    char *ep;\n" +
            "\n" +
            "    hour = strtol(data, &ep, 10);\n" +
            "    if (!(!ep || *ep != ':')) {\n" +
            "        min = strtol(ep+1, &ep, 10);\n" +
            "        if (!(!ep || *ep != ':')) {\n" +
            "            sec = strtol(ep+1, &ep, 10);\n" +
            "            if (!(!ep || *ep != ':')) {    \n" +
            "                millisec = strtol(ep+1, &ep, 10);\n" +
            "                if (!(!ep || *ep != ':')) {\n" +
            "                    microsec = strtol(ep+1, &ep, 10);\n" +
            "                    if (!(!ep || *ep != ':')) {\n" +
            "                        nanosec = strtol(ep+1, &ep, 10);\n" +
            "                    }\n" +
            "                }\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "    \n" +
            "    std::time_t calc_time = convertToNano(hour, min, sec, millisec, microsec, nanosec);\n" +
            "\n" +
            "    long int numberOfSteps = calc_time / step_time;\n" +
            "    if(numberOfSteps > 0){    \n" +
            "        return numberOfSteps;\n" +
            "    }\n" +
            "    else{\n" +
            "        return -1;\n" +
            "    }\n" +
            "\n" +
            "}\n" +
            "\n" +
            "long int advanceOfV2(std::time_t step_time, std::string unit, std::time_t value){\n" +
            "\n" +
            "    std::time_t hour = 0;\n" +
            "    std::time_t min = 0;\n" +
            "    std::time_t sec = 0;\n" +
            "    std::time_t millisec = 0;\n" +
            "    std::time_t microsec = 0;\n" +
            "    std::time_t nanosec = 0;\n" +
            "\n" +
            "    if(unit == \"hour\"){\n" +
            "        hour = value;\n" +
            "    }\n" +
            "    else if(unit == \"min\"){\n" +
            "        min = value;\n" +
            "    }\n" +
            "    else if(unit == \"sec\"){\n" +
            "        sec = value;\n" +
            "    }\n" +
            "    else if(unit == \"millisec\"){\n" +
            "        millisec = value;\n" +
            "    }\n" +
            "    else if(unit == \"microsec\"){\n" +
            "        microsec = value;\n" +
            "    }\n" +
            "    else if(unit == \"nanosec\"){\n" +
            "        nanosec = value;\n" +
            "    }\n" +
            "\n" +
            "    std::time_t calc_time = convertToNano(hour, min, sec, millisec, microsec, nanosec);\n" +
            "\n" +
            "    long int numberOfSteps = calc_time / step_time;\n" +
            "    if(numberOfSteps > 0){    \n" +
            "        return numberOfSteps;\n" +
            "    }\n" +
            "    else{\n" +
            "        return -1;\n" +
            "    }\n" +
            "}";

    public static String INCLUDE_CLOCK = "#include <chrono>\n" +
            "#include <ctime>\n";

    public static String HELPER_DEFINES;
    public static String HELPER_MARKER            = "%@&";
    public static String CLIENT_HEADER_INCLUDES   = "#include \"exec_schema.h\"\n";
    public static String HELPER_USERTYPE_INCLUDES = "#include <iostream>\n#include <sstream>\n#include <fstream>" +
            "\n#include <regex>\n#include <vector>\n%@&#include <map>\n#include " +
            "<getopt.h>\n#include \"json.hpp\"\n\n/*\n * @brief Regex expresssion used to " +
            "parse\n *        event labels and parameters \n */\n#define LABEL_REGEX std::string(\"([a-z_A-z]+[0-9]*)"+
            "\")\n#define PARAMS_REGEX std::string(LABEL_REGEX + \"\\\\((.*)\\\\)\")\n/*\n * @brief Safely executes" +
            " events by catching exceptions\n *     \n */\n#define ERROR_1 std::cout << \"Event is not recognized\"" +
            " << \"\\n\"\n#define ERROR_2 std::cout << \"Event is not executable\" << \"\\n\"\n#define ERROR_3" +
            " std::cout << \"Method toString() is undefined in User types\" << \"\\n\"\n#define ERROR_4 std::cout" +
            " << \"Method parse(...) is undefined in User types\" << \"\\n\"\n" +
            "#define ERROR_5 std::cout << \"Empty event label, terminating execution\" << \"\\n\""+
            "/*\n * " +
            "@brief External json library to be used for ontology " +
            "types (e.g. Packet, Flow, etc.)\n *     \n */\nusing json = nlohmann::json;\n\n";

    public static String HELPER_DEFAULT_INCLUDES = "#include <iostream>\n#include <sstream>\n#include <fstream>\n" +
            "#include <regex>\n#include <vector>\n%@&#include <map>\n#include " +
            "<getopt.h>\n\n/*\n * @brief Regex expresssion used to parse\n *            " +
            "event labels and parameters \n */\n#define LABEL_REGEX std::string(\"([a-z_A-z]+[0-9]*)\")\n" +
            "#define PARAMS_REGEX std::string(LABEL_REGEX + \"\\\\((.*)\\\\)\")\n/*\n * @brief Safely executes " +
            "events by catching exceptions\n *     \n */\n#define ERROR_1 std::cout << \"Event is not recognized\"" +
            " << \"\\n\"\n#define ERROR_2 std::cout << \"Event is not executable\" << \"\\n\"\n" +
            "#define ERROR_5 std::cout << \"Empty event label, terminating execution\" << \"\\n\""+
            "\n\n";

    public static String HELPER_TYPES_BEGIN = "/*\n * @brief An object used to serialize types and get the " +
                                                  "appropriated values as of\n *        primitive types or complex types."
                                                  + "\n */\nclass Types {\n    \n public:\n    \n";

    public static String HELPER_TYPES_END   = "\n};\n\n";

    public static String HELPER_HASMETHOD_BLOCK = "/*\r\n * @brief Checking whether methods parse(...) and toString()" +
            " exists in User types\r\n *     \r\n */\r\ntemplate <typename T>\r\nclass HasMethod\r\n{\r\nprivate:\r\n" +
            "    typedef char YesType[1];\r\n    typedef char NoType[2];\r\n\r\n    template <typename C> static " +
            "YesType& hasToString(decltype(&C::toString));\r\n    template <typename C> static NoType&  hasToString" +
            "(...);\r\n    template <typename C> static YesType& hasParse(decltype(&C::parse));\r\n    template " +
            "<typename C> static NoType&  hasParse(...);\r\n\r\npublic:\r\n    enum \r\n    { \r\n        " +
            "has_to_string = sizeof(hasToString<T>(0)) == sizeof(YesType),\r\n        has_parse = sizeof(hasParse" +
            "<T>(0)) == sizeof(YesType)  \r\n    };\r\n};\r\n/*\r\n * @brief Call toString() method if it exists\r\n" +
            " *     \r\n */\r\ntemplate<typename T> \r\ntypename std::enable_if<HasMethod<T>::has_to_string, " +
            "std::string>::type\r\ncall_to_string(T * t) \r\n{\r\n    return t->toString();\r\n}\r\n\r\nstd::string" +
            " call_to_string(...)\r\n{\r\n    return \"Method toString() is undefined in User types\";\r\n}\r\n\r\n" +
            "/*\r\n * @brief Call parse(...) method if it exists\r\n *     \r\n */\r\ntemplate<typename T> \r\n" +
            "typename std::enable_if<HasMethod<T>::has_parse, T>::type call_parse(T* t, std::string s)\r\n{\r\n" +
            "    return t->parse(s);\r\n}\r\n\r\nstd::string call_parse(...)\r\n{\r\n    return \"Method parse" +
            "(...) is undefined in User types\";\r\n}\r\n/*\r\n * @brief Checks whether an object is an instance " +
            "of another\r\n *     \r\n */\r\ntemplate<typename Base, typename T>\r\ninline bool instanceof(const T " +
            "*ptr) \r\n{\r\n    return dynamic_cast<const Base*>(ptr) != nullptr;\r\n}\r\n\r\ntemplate<typename Base," +
            " typename T>\r\ninline bool instanceof() \r\n{\r\n    T ptr;\r\n    return dynamic_cast<const Base*>" +
            "(&ptr) != nullptr;\r\n}\r\n";

    public static String EVENT_CONFIGS1 =  "\r\n    static void configInputStream(int argc, char** argv) \r\n    " +
            "{ \r\n        int opt;\r\n        while((opt = getopt(argc, argv, \"i$V1$F0$P0:h\")) != -1) " +
                "\r\n        { \r\n            switch(opt)\r\n            {\r\n   " +
                "             $V5\r\n                case 'i':\r\n                {\r\n           " +
                "         filename = std::string(optarg); \r\n                    channel = "    +
                "std::ifstream(filename);\r\n                    if(!channel)\r\n             "  +
                "           std::cout << \"Error opening event file !!!\";\r\n                    break;"    +
                "\r\n                }$P2\r\n                $V2\r\n                case 'h'"   +
                ":\r\n                { \r\n                    std::cout << \"This program has "    +
                "been compiled by cASTD.\" << \"\\n\";\r\n                    std::cout << \"./my_program" +
                " [-i <event file>] $V3$F4$P1 [-h]\" << \"\\n\";\r\n                    std::cout << \""     +
                "[OPTIONS]     \t\t\t\t\t\t\t\t     \" << \"\\n\";\r\n                    $F7\r\n      "  +
                "              std::cout << \"                     If an event file is not given, it runs " +
                "in interactive\" << \"\\n\";\r\n                    std::cout << \"                    " +
                " mode from command line\" << \"\\n\";$P4\r\n                    " +
                "$V4\r\n                    $F5\r\n                    $V6\r\n                    std::cout " +
                "<< \"   -h                Show this help\" << \"\\n\";\r\n                    exit(0);\r\n     " +
                "               break;\r\n                }\r\n                $F6\r\n    " +
                "        }   \r\n        }\r\n    }\r\n";


    public static String EVENT_CONFIGS2 = "\n" +
            "    static void configInputStream(int argc, char** argv) \n" +
            "    {\n" +
            "        const char* const short_opts = \"i$V1$F0$P0:h\";\n" +
            "\n" +
            "        const option long_opts[] = {\n" +
            "$P5" +
            "        };\n" +
            "\n" +
            "        while (true)\n" +
            "        {\n" +
            "             const auto opt = getopt_long(argc, argv, short_opts, long_opts, nullptr);\n" +
            "             if (-1 == opt)\n" +
            "                break;\n" +
            "\n" +
            "             switch(opt)\n" +
            "             {\n" +
            "                    \n" +
            "             case 'i':\n" +
            "             {\n" +
            "                filename = std::string(optarg); \n" +
            "                channel = std::ifstream(filename);\n" +
            "                if(!channel)\n" +
            "                    std::cout << \"Error opening event file !!!\";\n" +
            "                break;\n" +
            "             }\n" +
            "             $P2\n" +
            "             case 'h':\n" +
            "             { \n" +
            "                 std::cout << \"This program has been compiled by cASTD.\" << \"\\n\";\n" +
            "                 std::cout << \"./my_program [-i <event file>] $V3$F4$P1 [-h]\" << \"\\n\";\n" +
            "                 std::cout << \"[OPTIONS]     \t\t\t\t\t\t\t\t     \" << \"\\n\";\n" +
            "                 std::cout << \"   -i <event  file>  Read an event file in Shorthand format.\" << \"\\n\";\n" +
            "                 std::cout << \"                     If an event file is not given, it runs in interactive\" << \"\\n\";\n" +
            "                 std::cout << \"                     mode from command line\" << \"\\n\";$P4\n" +
            "                 $V4\n" +
            "                 $F5\n" +
            "                 $V6\n" +
            "                 std::cout << \"   -h                Show this help\" << \"\\n\";\n" +
            "                 exit(0);\n" +
            "                 break;\n" +
            "             }\n" +
            "             $F6\n" +
            "         }   \n" +
            "     }\n" +
            " }\n";

    public static String HELPER_EVENT_BLOCK_TEMPLATE = "$F1/*\r\n * @brief channel to read events\r\n */\r\n" +
            "static std::ifstream channel;\r\nstatic std::string " +
            "filename;\r\n\r\n"+ "/*\r\n * @brief The event data structure containing its "         +
            "label and its parameters\r\n *\r\n */\r\nstruct Event \r\n{\r\n    std::string label;" +
            "\r\n    std::vector<std::string> params;\r\n};\r\n\r\n$P3\r\nclass IO\r\n{\r\npublic:\r\n    /*\r\n     " +
            "* @brief parses event parameters and sets the event object with these params\r\n     " +
            "* @param The event object to be set\r\n     * @param The event string to be parsed "   +
            "\r\n     * @param The input regex to be used for parsing\r\n" +
            "     * @return \r\n     */\r\n    static void get_event_params(Event& e, const std::string in, const"   +
            " std::regex regex) \r\n    {\r\n        $F2\r\n  \r\n        return;   \r\n    }\r\n\r\n    /*\r\n   "  +
            "  * @brief parses event label and returns its value\r\n     * @param The event string to be parsed\r\n" +
            "     * @return The event label\r\n     */\r\n    static std::string get_event_label(const std::string"  +
            " in) \r\n    {\r\n        $F3\r\n\r\n        return \"\"; \r\n    }\r\n\r\n    /*\r\n" +
            "     * @brief" +
            " Reads event from the Input stream\r\n     * @param The cmdline arguments \r\n     * @return The "   +
            "event object\r\n     */\r\n" + EVENT_CONFIGS2 + "\r\n\r\n    static Event read_event(int argc) \r\n " +
            "   {\r\n        Event e;\r\n        std::string input;\r\n\r\n        if(argc > 1) \r\n" +
            "        {\r\n            if(!filename.empty()) \r\n\t        std::getline" +
            "(channel, input); \r\n\t    else \r\n\t        std::getline(std::cin, input); \r\n\r\n\t" +
            "    e.label = get_event_label(input);\r\n\t    get_event_params(e, input, std::regex"     +
            "(PARAMS_REGEX));  \r\n        }\r\n        else \r\n        {\r\n          " +
            "  getline(std::cin, input);\r\n            e.label = get_event_label(input);\r\n            " +
            "get_event_params(e, input, std::regex(PARAMS_REGEX));\r\n        }\r\n     \r\n        "      +
            "return e;\r\n    }\r\n};";

    public static String F1_ALL = "/*\r\n * @brief Event format supported\r\n */\r\n" +
            "const std::string SHORTHAND = \"shorthandevents\";\r\nconst std::string JSON      =" +
            " \"json\";\r\nstd::string       format    = SHORTHAND;\r\n\r\n";

    public static String F2_SHORTHAND = "std::smatch matches;\r\n        if(regex_search(in," +
            " matches, regex)) \r\n        {\r\n            std::stringstream ss(matches.str(2));\r"  +
            "\n\t    while(ss.good()) \r\n\t    { \r\n\t        std::string it; \r\n\t        "       +
            "getline(ss, it, ',');\r\n\t        e.params.push_back(it); \r\n\t    }\r\n        }";

    public static String F2_JSON = "try \r\n        {\r\n            if(!in.empty()) \r\n" +
            "            {\r\n                json j_evt = json::parse(in);\r\n                "   +
            "if(!j_evt.is_null() && j_evt.contains(\"arguments\"))\r\n                {\r\n"   +
            "                    for(auto& elem : j_evt[\"arguments\"].items())\r\n          " +
            "          {\r\n                        json obj = elem.value();\r\n             " +
            "           std::string it = obj.dump();\r\n\t                e.params." +
            "push_back(it);\r\n                    }\r\n                }\r\n            }"  +
            "\r\n\t} \r\n        catch(json::parse_error) \r\n        {\r\n\t    std::cout " +
            "<< \"Improper json event arguments !!!\" << std::endl;  \r\n\t} ";

    public static String F2_ALL = "if(format.compare(JSON) == 0)\r\n        {\r\n " +
            "           try \r\n            {\r\n                if(!in.empty()) \r\n     " +
            "           {\r\n                    json j_evt = json::parse(in);\r\n        " +
            "            if(!j_evt.is_null() && j_evt.contains(\"arguments\"))\r\n        " +
            "            {\r\n                        for(auto& elem : j_evt[\"arguments"   +
            "\"].items())\r\n                        {\r\n                            json" +
            " obj = elem.value();\r\n                            std::string it = obj.dump()"  +
            ";\r\n\t                    e.params.push_back(it);\r\n                        "   +
            "}\r\n                    }\r\n                }\r\n\t    } \r\n            "      +
            "catch(json::parse_error) \r\n            {\r\n\t        std::cout << \"Improper"  +
            " json event arguments !!!\" << std::endl;  \r\n\t    } \r\n        }\r\n        " +
            "else\r\n        {\r\n            std::smatch matches;\r\n            " +
            "if(regex_search(in, matches, regex)) \r\n            {\r\n                "    +
            "std::stringstream ss(matches.str(2));\r\n\t        while(ss.good()) \r\n\t"    +
            "        { \r\n\t            std::string it; \r\n\t            getline(ss, it," +
            " ',');\r\n\t            e.params.push_back(it); \r\n\t        }\r\n         "  +
            "   }\r\n        }";

    public static String F3_SHORTHAND = "std::smatch matches;\r\n        if(regex_search(in," +
            " matches, std::regex(LABEL_REGEX)))\r\n\t    return matches.str(1);";

    public static String F3_JSON = "try \r\n        {\r\n            if(!in.empty())\r\n" +
            "            {\r\n\t        json j_evt = json::parse(in);\r\n\t        std::string" +
            " label = j_evt[\"label\"];\r\n\t        return label;\r\n            }\r\n\t}" +
            " \r\n        catch(json::parse_error) \r\n        {\r\n            std::cout" +
            " << \"Improper json event label !!!\" << std::endl;   \r\n\t} ";

    public static String F3_ALL = "if(format.compare(JSON) == 0) \r\n        {\r\n        " +
            "    try \r\n            {\r\n                if(!in.empty())\r\n             " +
            "   {\r\n\t            json j_evt = json::parse(in);\r\n\t            std::string" +
            " label = j_evt[\"label\"];\r\n\t            return label;\r\n                " +
            "}\r\n\t    } \r\n            catch(json::parse_error) \r\n            {\r\n  " +
            "              std::cout << \"Improper json event label !!!\" << std::endl;   " +
            "\r\n\t    } \r\n        }\r\n        else \r\n        {\r\n\t    std::smatch " +
            "matches;\r\n\t    if (regex_search(in, matches, std::regex(PARAMS_REGEX)))\r"  +
            "\n\t        return matches.str(1); \r\n            else if(!in.empty())\r\n "  +
            "               return in;\r\n        }";

    public static String V1 = ":e:v:";

    public static String F0 = ":f";
    public static String P0 = ":$";

    public static String V2 = "case 'e': \r\n                {\r\n               "                  +
                                "     is_exec_state_enabled = 1;\r\n                    "           +
                                "std::string sockfile = std::string(optarg);\r\n\t\t    cli = new " +
                                "Client(sockfile);\r\n\t\t    cli->c_open();\r\n\t\t            \r" +
                                "\n                    if(cli->c_connect()) \r\n\t\t      "         +
                                "  std::cout << \"Connected to eASTD !\" << std::endl;\r\n\t\t   "  +
                                " else \r\n\t\t        std::cout << \"Connection failed !\"  << std::endl;" +
                                "\r\n\t\t    break;\r\n\t\t}";

    public static String V3 = "[-e <socket file>] [-v]";

    public static String F4 = "[-f <event format>] ";

    public static String P1 = "[-$ <value>] ";

    public static String V4 = "std::cout << \"   -e <socket file>  Socket file to send the execution state "
                              + "to eASTD\" << std::endl;";

    public static String F5 = "std::cout << \"   -f <event format> Event format. It can be a JSON or"
                              + " Shorthand format\" << std::endl;";

    public static String P4 = "\r\n                    std::cout << \"   -$ <value> Parameter of the ASTD\"" +
                              " << std::endl;";

    public static String P5 = "            {\"$\", required_argument, nullptr, '@'},\n";

    public static String F6 = "case 'f': \r\n                    format = std::string(optarg);" +
                              " \r\n                    break;";

    public static String V5 = "case 'v': \r\n                    json_debug_enabled = 1; \r\n" +
                              "                    break;";

    public static String P2_UNBOUNDED_DOM = "\r\n             case '@': \r\n                 " +
                                            "$ = Types::get_&(optarg); \r\n                 break;";
    public static String P2_DOM = "\r\n             case '@': \r\n                 "               +
                                  "if(std::find(#.begin(), #.end(), Types::get_&(optarg)) !=#.end()) \n" +
                                  "                 $ = Types::get_&(optarg);"                     +
                                  " \r\n                break;";

    public static String P3 = "& $;\r\n";

    public static String P3_DOM = "const std::vector<&> # = %;\r\n";

    public static String V6 = "std::cout << \"   -v                " +
            "Print the current execution state in console (verbose)\" << std::endl;";

    public static String F7_SHORTHAND = "std::cout << \"   -i <event  file>  Read an event file in Shorthand format." +
            "\" << \"\\n\";";

    public static String F7_JSON = "std::cout << \"   -i <event  file>  Read an event file in JSON format.\" << " +
            "\"\\n\";";

    public static String F7_ALL = "std::cout << \"   -i <event  file>  Read an event file in JSON or Shorthand format."
            + "\" << \"\\n\";";

    //primitive types
    public static String HELPER_GET_STR_METHOD = "    static std::string get_str(const std::string s) { return s; }\n";
    public static String HELPER_GET_INT_METHOD = "    static int get_int(const std::string s) " +
                                                 "{ return std::stoi(s); }\n";
    public static String HELPER_GET_FLOAT_METHOD = "    static float get_float(const std::string s) { return " +
                                                   "std::stof(s); }\n";
    public static String HELPER_GET_DOUBLE_METHOD = "    static double get_double(const std::string s) { return" +
                                                    " std::stod(s); }\n";
    public static String HELPER_GET_BOOL_METHOD = "    static bool get_bool(const std::string s) { \n" +
            "        if(s == \"0\" || s == \"false\"){\n" +
            "            return false;\n" +
            "        }\n" +
            "        return true;\n" +
            "    }";
    public static String HELPER_GET_PRIM_TYPE_METHODS = HELPER_GET_STR_METHOD + HELPER_GET_INT_METHOD
                                                       + HELPER_GET_FLOAT_METHOD + HELPER_GET_DOUBLE_METHOD
                                                       + HELPER_GET_BOOL_METHOD;
    //complex json types
    public static String HELPER_GET_USER_TYPE_METHOD = "    static $ get_$_type(const std::string s) \r\n" +
            "    {\r\n        if(instanceof<json, $>())\r\n        {\r\n            return json::parse(s);\r\n" +
            "        } \r\n        else\r\n        {\r\n            $ o;\r\n            if(HasMethod<$>::" +
            "has_parse)\r\n                return call_parse(&o, s);\r\n        }\r\n    }\n";

    public static String LOGGER_BLOCK = "#include <fstream>\r\n#include <string>\r\n#include <time.h>\r\n\r\n" +
            "#define MAX_BUFFER_SIZE 50\r\n#define ACTION_LOG_FILE \"actions.log\"\r\n\r\n\r\nclass Log " +
            "\r\n{\r\n    public:\r\n        enum Level \r\n        {\r\n            FATAL,\r\n          " +
            "  ERROR,\r\n            WARNING,\r\n            INFO,\r\n            DEBUG\r\n        };\r\n" +
            "        \r\n        Log(const std::string& filepath) : m_logfile{} \r\n        {\r\n            " +
            "m_logfile.open(filepath);\r\n            addMsg(Level::INFO, \"Started logging system.\");\r\n  " +
            "      }\r\n    \r\n        void addMsg(Level s, const std::string& msg) \r\n        {\r\n       " +
            "     if (m_logfile.is_open()) \r\n            {\r\n                m_logfile << timestamp() << " +
            "levels[static_cast<int>(s)] << \": \" << msg << std::endl;\r\n            }\r\n        }\r\n" +
            "        \r\n        ~Log() \r\n        {\r\n            addMsg(Level::INFO, \"Stopped logging " +
            "system.\");\r\n            m_logfile.close();\r\n        }\r\n    \r\n    protected:\r\n    " +
            "    std::string timestamp() const\r\n        {\r\n            char buffer[MAX_BUFFER_SIZE];\r\n      " +
            "      time_t t = time(NULL);\r\n            struct tm *lt = localtime(&t);\r\n            " +
            "snprintf(buffer, MAX_BUFFER_SIZE, \"%02d/%02d/%02d %02d:%02d:%02d \", \r\n                    " +
            " lt->tm_mon+1, lt->tm_mday, lt->tm_year%100, lt->tm_hour, \r\n                     lt->tm_min, " +
            "lt->tm_sec);\r\n            \r\n            return std::string(buffer);\r\n        }  \r\n    " +
            "    \r\n    private:\r\n        std::ofstream m_logfile;\r\n        const std::string levels[5] = " +
            "{\"Fatal\", \"Error\", \"Warning\", \"Info\", \"Debug\"};\r\n};\r\n\r\n" +
            "Log _log(ACTION_LOG_FILE);";

    public static String  LOGGER_MSG = "_log.addMsg(Log::INFO, \"Action '$' is executed.\")";

    public static String  CLIENT_BLOCK = "#include <unistd.h>         \r\n#include <stdlib.h>        \r\n" +
                                         "#include <stdio.h>         \r\n#include <string.h>        \r\n" +
                                         "#include <sys/socket.h>     \r\n#include <sys/un.h> \r\n#include" +
                                         " <sys/stat.h>\r\n\r\nclass Client \r\n{\r\n\r\npublic:\r\n    " +
                                         "    Client(const std::string sock_file) : socket_path(sock_file)," +
                                         " connected(false) {}\r\n        \r\n        ~Client() \r\n        {" +
                                         " \r\n            c_close();\r\n        }\r\n\r\n\tint c_open() \r\n" +
                                         "        {\r\n\t    fd = socket(PF_UNIX, SOCK_STREAM, 0); \r\n      " +
                                         "      if(fd < 0)\r\n               return 0;\r\n\r\n\t    memset(&addr," +
                                         " 0, sizeof(addr));\r\n\t    addr.sun_family = PF_UNIX;\r\n\t    " +
                                         "strncpy(addr.sun_path, socket_path.c_str(), sizeof(addr.sun_path) - 1);" +
                                         "\r\n            \r\n            return 1;\r\n\t}\r\n\r\n\tint c_connect()" +
                                         " \r\n        {\r\n\t    if(connect(fd, (struct sockaddr*)&addr, " +
                                         "sizeof(addr)) == -1) \r\n               return 0;\r\n\r\n            " +
                                         "connected = true;\r\n            return 1;\r\n\t}\r\n\r\n        int" +
                                         " c_send(const std::string msg) const \r\n        {\r\n            " +
                                         "const char *c_msg = msg.c_str();\r\n\r\n            return send(fd, " +
                                         "c_msg, strlen(c_msg), 0); \r\n        }\r\n \r\n        int is_connected()" +
                                         " const \r\n        {\r\n            return connected;\r\n        }\r\n" +
                                         "\r\n\tint c_close() const \r\n        {\r\n\t    return close(fd);\r\n\t}" +
                                         "    \r\n\r\nprivate: \r\n        std::string socket_path;\r\n        int" +
                                         " fd;\r\n        struct sockaddr_un addr;\r\n        bool connected;\r\n};";

    public static String  EXEC_SCHEMA_HDR = "#include \"json.hpp\"\r\n#include <iostream>\r\n#include " +
                                            "\"client.h\"\r\nusing json = nlohmann::json;\r\n\r\n";

    public static String  EXEC_SCHEMA_PROPS  = "/*\r\n * @brief $ props\r\n */\r\n";

    public static String  ASTD_PROPS_MAPPING = "";

    public static String  EXEC_SCHEMA_END = "/*\r\n * @brief Execution state per event\r\n */\r\njson  exec_state;" +
                                            "\r\n\r\n/*\r\n * @brief Checks whether the execution state mode is " +
                                            "activated\r\n *         \r\n */\r\nint  is_exec_state_enabled = 0;\r\n" +
                                            "int  json_debug_enabled = 0;\r\n\r\n/*\r\n * @brief Contains the " +
                                            "socket file descriptor to send the" +
                                            " \r\n *        current execution state to the eASTD editor         " +
                                            "\r\n */\r\nClient *cli = NULL;";

    public static String  EXEC_STATE_SENDTO_EASTD = "void exec_state_sendto_eastd() \r\n{\r\n        std::string " +
                                            "out = exec_state.dump(4);\r\n        if(cli->is_connected()) " +
                                            "\r\n        {\r\n           cli->c_send(out);\r\n        " +
                                            "}\r\n        if(json_debug_enabled) " +
                                            "\r\n           std::cout << \"Sent event: \" << out << std::endl;\r\n}\n";

    public static String  EXEC_STATE_CLOSE = "void exec_state_close() \r\n{\r\n        if(cli != NULL && " +
                                             "is_exec_state_enabled) \r\n        {\r\n\t\tcli->c_close();" +
                                             "\r\n\t\tdelete cli;\r\n        }\r\n\r\n}\n";

    public static String SIMULATION_BLOCK = "else if(_evt.label.compare(\"advanceTo\") == 0){\n" +
            "\tif(Types::get_str(_evt.params[0]).find(\":\") != std::string::npos){\n" +
            "\t\tlong int numberOfSteps = advanceToV1(current_time, step_time, Types::get_str(_evt.params[0]));\n" +
            "\t\twhile (numberOfSteps > 0)\n" +
            "\t\t{\n" +
            "\t\t\tStep();\n" +
            "\t\t\tnumberOfSteps--;\n" +
            "\t\t}\n" +
            "\t}\n" +
            "\telse{\n" +
            "\t\tlong int numberOfSteps = advanceToV2(current_time, step_time, Types::get_str(_evt.params[0]), (std::time_t) Types::get_double(_evt.params[1]));\n" +
            "\t\twhile (numberOfSteps > 0)\n" +
            "\t\t{\n" +
            "\t\t\tStep();\n" +
            "\t\t\tnumberOfSteps--;\n" +
            "\t\t}\n" +
            "\t}\n" +
            "}else if(_evt.label.compare(\"advanceOf\") == 0){\n" +
            "\tif(Types::get_str(_evt.params[0]).find(\":\") != std::string::npos){\n" +
            "\t\tlong int numberOfSteps = advanceOfV1(step_time, Types::get_str(_evt.params[0]));\n" +
            "\t\twhile (numberOfSteps > 0)\n" +
            "\t\t{\n" +
            "\t\t\tStep();\n" +
            "\t\t\tnumberOfSteps--;\n" +
            "\t\t}\n" +
            "\t}\n" +
            "\telse{\n" +
            "\t\tlong int numberOfSteps = advanceOfV2(step_time, Types::get_str(_evt.params[0]), (std::time_t) Types::get_double(_evt.params[1]));\n" +
            "\t\twhile (numberOfSteps > 0)\n" +
            "\t\t{\n" +
            "\t\t\tStep();\n" +
            "\t\t\tnumberOfSteps--;\n" +
            "\t\t}\n" +
            "\t}\t\n" +
            "}\n";
}
