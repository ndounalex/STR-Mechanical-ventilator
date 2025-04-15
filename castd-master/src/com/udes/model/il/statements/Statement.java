package com.udes.model.il.statements;

import com.udes.model.astd.items.Variable;
import com.udes.translator.ILTranslator;
import com.udes.model.astd.base.ASTD;
import com.udes.model.il.terms.Bool;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public abstract class Statement implements Serializable {

    /*
     * @brief substitute Action with the precondition Pred
     * @param  The precondition
     * @param  The action
     * @return The substitued Action
     */
    public Statement substitute(Statement pred, ArrayList<ASTD> ASTDTree) {
        // Substitutions only apply on the right side for assignments
        // and all sides for conditions
        // init(astd) = {v1:= u1, v2:= u2, ...}
        List<Variable> varList = (pred != null) ? pred.decodeInstruction() : new ArrayList<>();

        return updateInstruction(varList, ASTDTree);
    }

    /*
     * @brief substitute Action with the precondition Pred
     * @param  The precondition
     * @param  The action
     * @return The substitued Action
     */
    public Statement substitute(List<Variable> varList, ArrayList<ASTD> ASTDTree) {
        // Substitutions only apply on the right side for assignments
        // and all sides for conditions
        // init(astd) = {v1:= u1, v2:= u2, ...}
        return updateInstruction(varList, ASTDTree);
    }
    /*
     * @brief split statements into a list of variables
     * @param  The precondition statement
     * @return The list of variables
     */
    public abstract List<Variable> decodeInstruction();

    /*
     * @brief  Updates the IL model after substitution
     * @param  The precondition
     * @param  The action
     * @return The substitued Action
     */
    public abstract Statement updateInstruction(List<Variable> varList, ArrayList<ASTD> ASTDTree);
    /*
     * @brief Generates code in the target language
     * @param The statement
     * @param The type of event
     * @param The language
     * @param The resource bundle
     * @param The string builder
     */


    public abstract String generateCode(String eventType, ILTranslator.Lang lang, ResourceBundle bundle, Bool timed);

}
