package com.udes.model.il.conditions;

import com.udes.model.astd.base.ASTD;
import com.udes.model.astd.items.Variable;
import com.udes.model.il.predicates.UnaryPredicate;
import com.udes.translator.ILTranslator;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class NotCondition extends Condition {

    private UnaryPredicate<Condition> pred;

    public NotCondition(Condition condition) {
        pred = new UnaryPredicate<>(condition);
    }

    public Condition getCondition() {
        return pred.getExpr();
    }

    public void setCondition(Condition condition) {
        pred.setExpr(condition);
    }

    @Override
    public Condition updateInstruction(List<Variable> varList, ArrayList<ASTD> ASTDTree) {
        return this;
    }

    @Override
    public String generateCode(ILTranslator.Lang lang, ResourceBundle bundle) {
        Condition cond = getCondition();
        StringBuilder out = new StringBuilder();
        if (cond != null) {
            out.append(bundle.getString("NOT"));
            out.append(bundle.getString("PAR_BEGIN"));
            out.append(cond.generateCode(lang, bundle));
            out.append(bundle.getString("PAR_END"));
        }
        return out.toString();
    }

    public boolean isEqualTo(String name) {
        if(getCondition().isEqualTo(name)) {
            return true;
        }
        return false;
    }
}
