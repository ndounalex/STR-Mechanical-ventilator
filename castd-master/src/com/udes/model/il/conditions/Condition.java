package com.udes.model.il.conditions;

import com.udes.model.astd.base.ASTD;
import com.udes.model.astd.items.Variable;
import com.udes.model.il.predicates.Predicate;
import com.udes.model.il.statements.Statement;
import com.udes.translator.ILTranslator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public abstract class Condition extends Predicate {
    /*
     * @brief substitute Action with the precondition Pred
     * @param  The precondition
     * @param  The action
     * @return The substitued Action
     */
    public Condition substitute(Statement pred, ArrayList<ASTD> ASTDTree) {
        // Substitutions only apply on the right side for assignments
        // and all sides for conditions
        // init(astd) = {v1:= u1, v2:= u2, ...}
        List<Variable> varList = pred.decodeInstruction();

        return updateInstruction(varList, ASTDTree);
    }
    /*
     * @brief substitute Action with the precondition (Call)
     * @param  The precondition
     * @param  The action
     * @return The substitued Action
     */
    public Condition callSubstitute(List<Variable> varList, ArrayList<ASTD> ASTDTree) {
        // Substitutions only apply on the right side for assignments
        // and all sides for conditions
        // it's a call astd
        // call_params= {v1:= u1, v2:= u2, ...}
        return updateInstruction(varList, ASTDTree);
    }
    /*
     * @brief  Updates the IL model after substitution
     * @param  The precondition
     * @param  The action
     * @return The substitued Action
     */
    public abstract Condition updateInstruction(List<Variable> varList, ArrayList<ASTD> ASTDTree);

    /*
     * @brief Generates code in the target language
     * @param The statement
     * @param The type of event
     * @param The language
     * @param The resource bundle
     * @param The string builder
     */
    public String generateCode(Statement stmt, String eventType, ILTranslator.Lang lang, ResourceBundle bundle) {
        return "";
    }
    /*
     * @brief Generates code in the target language
     * @param The statement
     * @param The type of event
     * @param The language
     * @param The resource bundle
     * @param The string builder
     */
    public abstract String generateCode(ILTranslator.Lang lang, ResourceBundle bundle);
    /*
     * @brief Generates junction
     * @param The statement
     * @param The type of event
     * @param The language
     * @param The resource bundle
     * @param The string builder
     */
    protected String junction(List<Condition> lst, String op, ILTranslator.Lang lang, ResourceBundle bundle) {
        StringBuilder out = new StringBuilder();
        if(lst != null) {
            int size = lst.size();
            if(size == 1) {
                Condition _c = lst.iterator().next();
                String c = toString(_c, lang, bundle);

                if(c != null && !c.isEmpty()) {
                    out.append(c);
                }
            }
            else if (size > 1){
                boolean flag = false;
                String c_i, c_j = null;
                for(int i = 1; i < size; i++) {
                    c_i = (c_j != null) ? c_j : toString(lst.get(i-1), lang, bundle);
                    if(c_i != null && !c_i.isEmpty()) {
                        if(!flag) {
                            out.append(bundle.getString("PAR_BEGIN"));
                            flag = true;
                        }
                        out.append(c_i);
                        if(lst.get(i) != null)
                            c_j = toString(lst.get(i), lang, bundle);
                        if(c_j != null && !c_j.isEmpty())
                            out.append(bundle.getString(op));
                    }
                }
                if(c_j != null && !c_j.isEmpty()) {
                    out.append(c_j);
                }
                if(flag) out.append(bundle.getString("PAR_END"));
            }
        }

        return out.toString();
    }

    public String toString(Condition _c, ILTranslator.Lang lang, ResourceBundle bundle) {
        String c = "";
        if(_c instanceof AndCondition)
            c = junction(((AndCondition)_c).getCondition(), "AND", lang, bundle);
        else if(_c instanceof OrCondition)
            c = junction(((OrCondition)_c).getCondition(), "OR", lang, bundle);   
        else if (_c != null) {
            c = _c.generateCode(lang, bundle);
        }

        return c;
    }

    public abstract boolean isEqualTo(String name);
}
