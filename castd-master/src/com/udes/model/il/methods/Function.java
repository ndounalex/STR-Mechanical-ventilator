package com.udes.model.il.methods;

import com.udes.model.astd.items.Variable;
import com.udes.model.il.statements.Statement;

import java.util.List;

public class Function {

    private String name;
    private List<Variable> params;
    private String type;
    private Statement blockstmt;

    public Function(String name,
                    List<Variable> params,
                    String type,
                    Statement blockstmt) {
        this.name = name;
        this.params = params;
        this.type = type;
        this.blockstmt = blockstmt;
    }

    public String getName() { return name; }

    public void setName(String name) {
        this.name = name;
    }

    public List<Variable> getParams() {
        return params;
    }

    public void setParams(List<Variable> params) {
        this.params = params;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Statement getBlock() {
        return blockstmt;
    }

    public void setBlock(Statement blockstmt) {
        this.blockstmt = blockstmt;
    }
}
