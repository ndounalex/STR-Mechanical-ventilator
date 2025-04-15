package com.udes.model.il.statements;

import com.udes.model.astd.base.ASTD;
import com.udes.model.astd.items.Variable;
import com.udes.model.il.conditions.Condition;
import com.udes.model.il.conventions.Conventions;
import com.udes.model.il.predicates.Predicate;
import com.udes.model.il.terms.Term;
import com.udes.translator.ILTranslator;
import com.udes.utils.Constants;
import com.udes.model.il.terms.Bool;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class AssignStatement extends Statement {

    private Term var;
    private Predicate expr;

    public AssignStatement(Term var, Predicate expr) {
        this.var = var;
        this.expr = expr;
    }

    public Term getVar() {
        return var;
    }

    public void setVar(Term var) {
        this.var = var;
    }

    public Predicate getExpr() {
        return expr;
    }

    public void setExpr(Predicate expr) {
        this.expr = expr;
    }

    @Override
    public List<Variable> decodeInstruction() {
        List<Variable> varList = new ArrayList<>();
        varList.add(new Variable(getVar().getId(), null, ((Term) getExpr()).getId(), null));

        return varList;
    }

    @Override
    public Statement updateInstruction(List<Variable> varList, ArrayList<ASTD> ASTDTree) {
        return this;
    }

    @Override
    public String generateCode(String eventType, ILTranslator.Lang lang, ResourceBundle bundle, Bool timed) {
        Predicate expr = getExpr();
        if (expr instanceof Condition) {
            Condition c = (Condition) expr;
            String res = c.generateCode(lang, bundle);
            return getVar().getId() + bundle.getString("ASSIGN")
                    + ((res == null || res.isEmpty()) ? bundle.getString("TRUE") : res)
                    + bundle.getString("SEMI_COLON_SEP");
        } else if (expr instanceof Term) {
            Term term1 = getVar();
            Term term2 = (Term) expr;
            StringBuilder builder = new StringBuilder();
            if (term1.getId().contains(Conventions.HISTORY_STATE.replace("_", ""))) {
                if (lang == ILTranslator.Lang.CPP) {
                    if (term1.getId() != null) {
                        builder.append(term1.getId().replace("[", Constants.FIRST_ITEM + " = ")
                                .replace("]", bundle.getString("SEMI_COLON_SEP")));
                    }
                    if (term2.getId() != null) {
                        String tmp = term1.getId().replace("[", Constants.SECOND_ITEM + " = ")
                                .replace("]", "");
                        builder.append(tmp.split(" = ")[0]).append(" = ")
                                .append(term2.getId()).append(bundle.getString("SEMI_COLON_SEP"));
                    }
                }

                return builder.toString();

            } else if ((term1.getId().equals(Conventions.CST) || term2.getId().equals(Conventions.CST)) && !Constants.TIMED_SIMULATION) {
                if (lang == ILTranslator.Lang.CPP) {
                    if (term2.getId().equals(Conventions.CST)) {
                        return builder.append(bundle.getString("CLOCK_RESET").replace(ILTranslator.USYMBOL_1, term1.getId())).append(bundle.getString("SEMI_COLON_SEP")).toString();
                    }
                }
                return builder.toString();
            } else if ((term1.getId().equals(Conventions.CST) || term2.getId().equals(Conventions.CST)) && Constants.TIMED_SIMULATION) {
                if (lang == ILTranslator.Lang.CPP) {
                    if (term2.getId().equals(Conventions.CST)) {
                        return builder.append(bundle.getString("CLOCK_RESET_SIM")
                                .replace(ILTranslator.USYMBOL_1, term1.getId()))
                                .append(bundle.getString("SEMI_COLON_SEP")).toString();
                    }
                    else {
                        return builder.append(bundle.getString("CLOCK_RESET_SIM")
                                .replace(ILTranslator.USYMBOL_1, term2.getId()))
                                .append(bundle.getString("SEMI_COLON_SEP")).toString();
                    }
                }
                return builder.toString();
            }else {
                return getExpr().generateCode(this, eventType, lang, bundle);
            }
        }
        else {
            return getExpr().generateCode(this, eventType, lang, bundle);
        }
    }
}