package com.udes.model.il.statements;

import com.udes.model.il.conditions.Condition;
import com.udes.translator.ILTranslator;
import com.udes.model.il.terms.Bool;

import java.util.ResourceBundle;

public class WhileStatement extends LoopStatement{

    public WhileStatement(Condition condition, Statement statement) {
        super(condition, statement);
    }

    @Override
    public String generateCode(String eventType, ILTranslator.Lang lang, ResourceBundle bundle, Bool timed) {
        StringBuilder out = new StringBuilder();
        out.append(bundle.getString("WHILE")
                .replace(ILTranslator.USYMBOL_1, getCondition().generateCode(lang, bundle)))
                .append(bundle.getString("BRA_BEGIN"))
                .append(getStatement().generateCode(eventType, lang, bundle, timed).replaceAll("(?m)^", "\t"))
                .append(bundle.getString("BRA_END"))
                .append(bundle.getString("NEWLINE"));

        return out.toString();
    }
}
