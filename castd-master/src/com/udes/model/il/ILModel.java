package com.udes.model.il;

import com.udes.model.astd.items.Variable;
import com.udes.model.il.methods.Function;
import com.udes.model.il.record.Record;
import com.udes.track.ExecSchema;

import java.util.List;

public class ILModel implements IILModel{

    private List<String> extrefs;
    private List<String> typedefs;
    private String name;
    private List<Record> typedecls;
    private List<Variable> vardecls;
    private List<Function> functions;
    private ExecSchema      execSchema;

    public ILModel(List<String> extrefs, List<String> typedefs, String name, List<Record> typedecls,
                   List<Variable> vardecls, List<Function> functions) {
        this.extrefs = extrefs;
        this.typedefs = typedefs;
        this.name = name;
        this.typedecls = typedecls;
        this.vardecls = vardecls;
        this.functions = functions;
    }

    public ILModel() {}
    /*
     * @brief Returns external references
     * @param
     * @return External references
     */
    @Override
    public List<String> getExtrefs() {
        return extrefs;
    }
    /*
     * @brief Sets the external references
     * @param The external references
     * @return
     */
    @Override
    public void setExtrefs(List<String> extrefs) {
        this.extrefs = extrefs;
    }
    /*
     * @brief Returns type definitions
     * @param
     * @return Type definitions
     */
    @Override
    public List<String> getTypeDefs() {
        return typedefs;
    }
    /*
     * @brief Sets type definitions
     * @param Type definitions
     * @return
     */
    @Override
    public void setTypeDefs(List<String> typedefs) {
        this.typedefs = typedefs;
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
     * @brief Sets name
     * @param The name
     * @return
     */
    @Override
    public void setName(String name) {
        this.name = name;
    }
    /*
     * @brief Returns the declarations of types
     * @param
     * @return The list of declarations
     */
    @Override
    public List<Record> getTypedecls() {
        return typedecls;
    }
    /*
     * @brief Sets the declarations of types
     * @param The declarations of types
     * @return
     */
    @Override
    public void setTypedecls(List<Record> typedecls) {
        this.typedecls = typedecls;
    }
    /*
     * @brief Returns the declarations of variables and constants
     * @param
     * @return The list of declarations
     */
    @Override
    public List<Variable> getVardecls() {
        return vardecls;
    }
    /*
     * @brief Sets the declarations of variables
     * @param The declarations of variables
     * @return
     */
    @Override
    public void setVardecls(List<Variable> vardecls) {
        this.vardecls = vardecls;
    }
    /*
     * @brief Returns the list of functions/methods
     * @param
     * @return The list of functions/methods
     */
    @Override
    public List<Function> getFunctions() {
        return functions;
    }
    /*
     * @brief Sets the list of functions
     * @param The list of functions
     * @return
     */
    @Override
    public void setFunctions(List<Function> functions) {
        this.functions = functions;
    }
   /*
     * @brief Returns the execution state schema
     * @param
     * @return Returns the execution state schema
     */
    public ExecSchema  getExecSchema() {return execSchema; }
    /*
     * @brief Sets the execution state schema
     * @param the execution state schema
     * @return
     */
    public void setExecSchema(ExecSchema execSchema) { this.execSchema = execSchema; }
}
