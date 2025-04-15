package com.udes.model.il.statements;

import com.udes.model.astd.base.ASTD;
import com.udes.model.astd.items.Variable;
import com.udes.model.il.conditions.Condition;

import java.util.ArrayList;
import java.util.List;

public abstract class LoopStatement extends Statement {

    private Condition condition;
    private Statement statement;

    public LoopStatement(Condition condition, Statement statement) {
        this.condition = condition;
        this.statement = statement;
    }

    public Condition getCondition() {
        return condition;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    public Statement getStatement() {
        return statement;
    }

    public void setStatement(Statement statement) {
        this.statement = statement;
    }

    @Override
    public List<Variable> decodeInstruction() {
        return null;
    }

    @Override
    public Statement updateInstruction(List<Variable> varList, ArrayList<ASTD> ASTDTree) {
        return null;
    }
}
