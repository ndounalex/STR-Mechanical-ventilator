package com.udes.model.il.conditions;

import com.udes.model.astd.base.ASTD;
import com.udes.model.astd.items.Variable;
import com.udes.model.il.predicates.BinaryPredicate;
import com.udes.translator.ILTranslator;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class OrCondition extends Condition {

    private BinaryPredicate<Condition> pred;

    public OrCondition(List<Condition> condition) {
        pred = new BinaryPredicate<>(condition);
    }

    public List<Condition> getCondition() {
        return pred.getExpr();
    }

    public void setCondition(List<Condition> condition) {
        pred.setExpr(condition);
    }

    @Override
    public Condition updateInstruction(List<Variable> varList, ArrayList<ASTD> ASTDTree) {
        List<Condition> cList = getCondition(),
                newList = new ArrayList<>();
        if(cList != null) {
            cList.forEach( c ->
                    newList.add(c.updateInstruction(varList, ASTDTree)));
            return new OrCondition(newList);
        }

        return this;
    }

    @Override
    public String generateCode(ILTranslator.Lang lang, ResourceBundle bundle) {
        return junction(getCondition(),"OR", lang, bundle);
    }

    public boolean isEqualTo(String name) {
        List<Condition> tmp = new ArrayList<>();
        for(Condition c : getCondition()) {
            if(c.isEqualTo(name))
               return true;
            else
               tmp.add(c);
        }
        setCondition(tmp);
        return false;
    }
}
