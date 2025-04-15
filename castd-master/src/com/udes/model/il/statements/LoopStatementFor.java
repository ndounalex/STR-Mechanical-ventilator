package com.udes.model.il.statements;

import com.udes.model.astd.base.ASTD;
import com.udes.model.astd.items.Variable;
import com.udes.model.il.conditions.Condition;

import java.util.ArrayList;
import java.util.List;

public abstract class LoopStatementFor extends Statement {

    private Statement  statement1;
    private Condition condition;
    private Statement statement2;
    private Statement statement3;

    public LoopStatementFor(Statement statement1, Condition condition, Statement statement2, Statement statement3) {
        this.condition = condition;
        this.statement1 = statement1;
        this.statement2 = statement1;
        this.statement3 = statement1;
    }

    public Condition getCondition() {
        return condition;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    public Statement getStatement() {
        return statement1;
    }

    public void setStatement(Statement statement) {
        this.statement1 = statement;
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
