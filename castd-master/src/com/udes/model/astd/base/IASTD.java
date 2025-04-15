package com.udes.model.astd.base;

import com.udes.model.astd.items.Action;
import com.udes.model.astd.items.Event;
import com.udes.model.astd.items.Variable;
import com.udes.model.il.conditions.Condition;
import com.udes.model.il.containers.Entry;
import com.udes.model.il.methods.Function;
import com.udes.model.il.record.Record;
import com.udes.model.il.record.Enum;
import com.udes.model.il.statements.Statement;
import com.udes.model.il.terms.Bool;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public interface IASTD {
    /*
     * @brief Returns name
     * @param
     * @return The ASTD name
     */
    String getName();
    /*
     * @brief Sets the name
     * @param The ASTD name
     * @return
     */
    void setName(String name);
    /*
     * @brief  Returns ontology classes
     * @param
     * @return The ontology classes
     */
    Set<String> getOntoClasses();
    /*
     * @brief Sets the ontology classes
     * @param The ontolology classes
     * @return
     */
    void setOntoClasses(Set<String> onto_classes);
    /*
     * @brief  Returns imports
     * @param
     * @return imports
     */
    Set<String> getImports();
    /*
     * @brief  Sets imports
     * @param  imports
     * @return
     */
    void setImports(Set<String> imports);
    /*
     * @brief Returns type defs
     * @param
     * @return type_defs
     */
    Set<String> getTypeDefs();
    /*
     * @brief Sets type definitions
     * @param type definitions
     * @return
     */
    void setTypeDefs(Set<String> type_defs);

    /*
     * @brief  Returns attributes
     * @param
     * @return attributes
     */
    List<Variable> getAttributes();
    /*
     * @brief Sets attributes
     * @param attributes
     * @return
     */
    void setAttributes(List<Variable> attributes);
    /*
     * @brief Returns ASTD params for a called ASTD
     * @param
     * @return param list
     */
    List<Variable> getParams();
    /*
     * @brief Sets params
     * @param params
     * @return
     */
    void setParams(List<Variable> params);
    /*
     * @brief Returns ASTD action
     * @param
     * @return ASTD action
     */
    Action getAstdAction();
    /*
     * @brief Sets ASTD action
     * @param ASTD action
     * @return
     */
    void setAstdAction(Action astdAction);
    /*
     * @brief Generate the code used to initialize an ASTD
     * @param  ASTD model
     * @return The statement block for initialization
     */

    Statement init(ArrayList<ASTD> callList, String lets);

    Statement initforsub(ArrayList<ASTD> callList, Event e, Bool timed, String lets, boolean forFinal);

    /*
     * @brief Generate the condition that checks if an ASTD is final
     * @param  ASTD model
     * @return  A condition
     */
    Condition _final(ArrayList<ASTD> callList);

    /*
     * @brief Generate the condition that checks if an ASTD is final
     * @param  ASTD model
     * @return  A condition
     */
    Condition _finalForSub(ArrayList<ASTD> callList);
    /*
     * @brief Generate external references of the top of the model
     * @param  ASTD model
     * @return List of refs
     */
    Set<String> trans_refs();
    /*
     * @brief Generate type structures from ASTDs excepts Elem type (since it's unused)
     * @param  ASTD model
     * @return List of type structures
     */
    Entry<List<Enum>, List<Record>> trans_type();
    /*
     * @brief Generate variable declarations from ASTD states
     * @param  ASTD model
     * @return List of variables
     */
    List<Variable> trans_var();
    /*
     * @brief Generate type structures from ASTD states
     * @param  ASTD model
     * @param  Event label
     * @return The container with the disjunction of conditions and the if-fi statement
     */
    Entry<Condition, Statement> trans_event(Event e, Bool timed, ArrayList<ASTD> callList, String lets);

    Entry<Condition, Statement> trans_event_step(Event e, Bool timed, ArrayList<ASTD> callList, String lets);

    /*
     * @brief Generate the main function of the IL model
     * @param  ASTD model
     * @return The main function
     */
    Function trans_main(Set<Event> evt_collections, ArrayList<Event> evts);
    /*
     * @brief Generate the main function of the IL model
     * @param  ASTD model
     * @return The main function
     */
    Function trans_main_step();

    List<Function> generateFinalFunc(ArrayList<ASTD> callList);

    Function trans_consumer(ArrayList<Event> evts);
    Function trans_producer_events(ArrayList<Event> evts);
    Function trans_producer_step(boolean hasStep);
}
