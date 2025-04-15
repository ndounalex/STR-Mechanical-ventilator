package com.udes.optimizer;

import com.udes.model.il.conditions.Condition;
import com.udes.optimizer.base.Optimizer;

public class ConditionOptimizer implements Optimizer {

    private Condition c;

    public ConditionOptimizer(Condition c) {
        this.c = c;
    }

    @Override
    public void optimize() {

    }
}