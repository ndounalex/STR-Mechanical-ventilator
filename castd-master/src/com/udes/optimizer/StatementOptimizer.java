package com.udes.optimizer;

import com.udes.model.il.statements.Statement;
import com.udes.optimizer.base.Optimizer;

public class StatementOptimizer implements Optimizer {

    Statement stmt;

    public StatementOptimizer(Statement stmt) {
        this.stmt = stmt;
    }

    @Override
    public void optimize() {

    }
}
