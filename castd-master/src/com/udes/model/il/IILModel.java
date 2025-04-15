package com.udes.model.il;

import com.udes.model.astd.items.Variable;
import com.udes.model.il.methods.Function;
import com.udes.model.il.record.Record;

import java.util.List;

public interface IILModel {
    /*
     * @brief Returns external references
     * @param
     * @return External references
     */
    List<String> getExtrefs();
    /*
     * @brief Sets the external references
     * @param The external references
     * @return
     */
    void setExtrefs(List<String> extrefs);
    /*
     * @brief Returns type definitions
     * @param
     * @return Type definitions
     */
    List<String> getTypeDefs();
    /*
     * @brief Sets type definitions
     * @param Type definitions
     * @return
     */
    void setTypeDefs(List<String> typedefs);
    /*
     * @brief Returns name
     * @param
     * @return The ASTD name
     */
    String getName();
    /*
     * @brief Sets name
     * @param The name
     * @return
     */
    void setName(String name);
    /*
     * @brief Returns the declarations of types
     * @param
     * @return The list of declarations
     */
    List<Record> getTypedecls();
    /*
     * @brief Sets the declarations of types
     * @param The declarations of types
     * @return
     */
    void setTypedecls(List<Record> typedecls);
    /*
     * @brief Returns the declarations of variables and constants
     * @param
     * @return The list of declarations
     */
    List<Variable> getVardecls();
    /*
     * @brief Sets the declarations of variables
     * @param The declarations of variables
     * @return
     */
    void setVardecls(List<Variable> vardecls);
    /*
     * @brief Returns the list of functions/methods
     * @param
     * @return The list of functions/methods
     */
    List<Function> getFunctions();
    /*
     * @brief Sets the list of functions
     * @param The list of functions
     * @return
     */
    void setFunctions(List<Function> functions);
}
