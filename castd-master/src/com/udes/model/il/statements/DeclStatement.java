package com.udes.model.il.statements;

import com.udes.model.astd.base.ASTD;
import com.udes.model.astd.items.Constant;
import com.udes.model.astd.items.Variable;
import com.udes.model.il.conventions.Conventions;
import com.udes.translator.ILTranslator;
import com.udes.model.il.terms.Bool;
import com.udes.utils.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class DeclStatement extends Statement {

    private Variable decl;

    public DeclStatement(String var, String type) {
        decl = new Variable(var, type, null, null);
    }

    public DeclStatement(Variable decl) {
        this.decl = decl;
    }

    public Variable getDecl() {
        return decl;
    }

    public void setDecl(Variable decl) {
        this.decl = decl;
    }

    @Override
    public List<Variable> decodeInstruction() {
        return null;
    }

    @Override
    public Statement updateInstruction(List<Variable> varList, ArrayList<ASTD> ASTDTree) {
        return this;
    }

    @Override
    public String generateCode(String eventType, ILTranslator.Lang lang, ResourceBundle bundle, Bool timed) {
        Variable v = getDecl();
        StringBuilder out = new StringBuilder();
        if(v != null) {
            String type = v.getType();
            if(type.compareTo(Conventions.BOOL_TYPE) == 0) {
                if (lang == ILTranslator.Lang.CPP) type = bundle.getString("BOOLEAN_TYPE");
                if (lang == ILTranslator.Lang.JAVA) /*TODO: */;

                out.append(type).append(bundle.getString("SEP")).append(v.getName());
                Object init = v.getInit();
                if (init != null) {
                    out.append(bundle.getString("ASSIGN")).append(init.toString());
                }
            }
            else if(type.compareTo(Conventions.STRING) == 0) {
                if (lang == ILTranslator.Lang.CPP) type = bundle.getString("STRING_TYPE");
                if (lang == ILTranslator.Lang.JAVA) /*TODO: */;

                if(v instanceof Constant){
                    out.append(bundle.getString("CONST")).append(type)
                       .append(bundle.getString("SEP")).append(v.getName());
                    if(v.getInit() != null) {
                        out.append(bundle.getString("ASSIGN"))
                           .append(bundle.getString("STRING_QUOTE").replace(ILTranslator.USYMBOL_1, v.getInit().toString()));
                    }
                }
                else{
                    out.append(type).append(bundle.getString("SEP")).append(v.getName());
                }
            }
            else if(type.compareTo(Conventions.JSON) == 0) {
                if (lang == ILTranslator.Lang.CPP) type = Conventions.JSON.toLowerCase();
                if (lang == ILTranslator.Lang.JAVA) /*TODO: */;

                out.append(type).append(bundle.getString("SEP")).append(v.getName());
                if(v.getInit() != null) {
                    out.append(bundle.getString("ASSIGN"))
                       .append(v.getInit().toString());
                }
            }
            else if(type.compareTo(Conventions.CLOCK) == 0 && Constants.TIMED_SIMULATION){
                out.append(type).append(bundle.getString("SEP"))
                        .append(v.getName())
                        .append(bundle.getString("SEP"))
                        .append(bundle.getString("ASSIGN"))
                        .append(bundle.getString("SEP"))
                        .append(Conventions.CURRENT_TIME);
            }
            else if(type.compareTo(Conventions.TIME_TYPE3) == 0){
                out.append(bundle.getString("TIME_TYPE3")).append(bundle.getString("SEP"))
                        .append(v.getName())
                        .append(bundle.getString("SEP"))
                        .append(bundle.getString("ASSIGN"))
                        .append(bundle.getString("SEP"))
                        .append(Conventions.CURRENT_TIME);
            }
            else if(type.compareTo(Conventions.THREAD_DEC) == 0){
                out.append(type).append(bundle.getString("SEP")).append(v.getName());
            }
            else {
                out.append(type).append(bundle.getString("SEP")).append(v.getName());
                if(v.getInit() != null) {
                    out.append(bundle.getString("ASSIGN"))
                            .append(v.getInit().toString());
                }
            }
            out.append(bundle.getString("SEMI_COLON_SEP"));
        }

        return out.toString();
    }
}
