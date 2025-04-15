package com.udes.optimizer.base;

public interface Optimizer {
    /*
     * @brief optimizes quantifications, if-fi branches, and dead conditions
     *        Ex: if (Cond && true = false) {}, (notstarted = started), (started = started)
     */
    void optimize();
}
