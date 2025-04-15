package com.udes.model.il.statements;

import com.udes.model.il.conditions.CallCondition;
import com.udes.model.il.conditions.Condition;
import com.udes.model.il.terms.Bool;
import com.udes.translator.ILTranslator;

import java.util.List;
import java.util.ResourceBundle;

public class ForStatement extends LoopStatement{

    public ForStatement(Condition condition, Statement statement) {
        super(condition, statement);
    }

    @Override
    public String generateCode(String eventType, ILTranslator.Lang lang, ResourceBundle bundle, Bool timed) {
        List<String> condItems = ((CallCondition) getCondition()).getParams();
        StringBuilder out = new StringBuilder();
        out.append(bundle.getString("FOR2")
                .replace(ILTranslator.USYMBOL_1, condItems.get(0))
                .replace(ILTranslator.USYMBOL_2, condItems.get(1))
                .replace(ILTranslator.USYMBOL_3, condItems.get(2)))
                .append(bundle.getString("BRA_BEGIN"))
                .append(getStatement().generateCode(eventType, lang, bundle, timed).replaceAll("(?m)^", "\t"))
                .append(bundle.getString("BRA_END"))
                .append(bundle.getString("NEWLINE"));

        return out.toString();
    }
}
