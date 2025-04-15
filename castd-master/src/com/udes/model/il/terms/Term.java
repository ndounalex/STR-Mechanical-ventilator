package com.udes.model.il.terms;

import com.udes.model.il.conventions.Conventions;
import com.udes.model.il.predicates.Predicate;
import com.udes.model.il.statements.AssignStatement;
import com.udes.model.il.statements.Statement;
import com.udes.translator.ILTranslator;

import java.util.ResourceBundle;

public class Term extends Predicate {
    String id;

    public Term(String id) { this.id = id; }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String generateCode(Statement stmt, String eventType, ILTranslator.Lang lang, ResourceBundle bundle) {
        StringBuilder out = new StringBuilder();
        AssignStatement ass = (AssignStatement) stmt;
        String var = ass.getVar().getId();
        if(var.contains("hState")){
            if(lang == ILTranslator.Lang.CPP)  var = var.replace("(","[")
                                                        .replace(")", "]");
            if(lang == ILTranslator.Lang.JAVA) /*TODO: */;
        }
        out.append(var).append(bundle.getString("ASSIGN"));
        String expr = ((Term) ass.getExpr()).getId();
        if(expr.compareTo(Conventions.NIL) == 0) {
            if(lang == ILTranslator.Lang.CPP) {
                if(eventType != null) {
                    if (eventType.compareTo(Conventions.BOOL_TYPE) == 0) expr = bundle.getString("FALSE");
                    else if (eventType.compareTo(Conventions.INT) == 0) expr = "-1";
                    else if (eventType.compareToIgnoreCase(Conventions.STRING) == 0) expr = "";
                    else expr = bundle.getString("NIL"); // assign NULL to unknow types
                }
                if(expr == null) {
                    if(var.contains(Conventions.STATE) || var.contains(Conventions.HISTORY_STATE)) expr = "-1";
                    else expr = bundle.getString("NIL");
                }
            }
            if(lang == ILTranslator.Lang.JAVA) /*TODO:*/ ;
        }
        out.append(expr);
        out.append(bundle.getString("SEMI_COLON_SEP"));

        return out.toString();
    }
}
