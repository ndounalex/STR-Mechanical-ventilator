package com.udes.model.il.terms;

import com.udes.model.astd.base.ASTD;
import com.udes.model.astd.items.Variable;
import com.udes.model.astd.types.Call;
import com.udes.model.il.conditions.Condition;
import com.udes.model.il.conventions.Conventions;
import com.udes.model.il.statements.AssignStatement;
import com.udes.translator.ILTranslator;
import com.udes.utils.Constants;
import com.udes.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.udes.model.astd.base.ASTD.rootASTD;

public class Bool extends Condition {
    private Boolean value;
    private String svalue;

    public Bool(boolean value) {
        this.value = value;
    }
    public Bool(String value) {this.svalue = value;}

    public Boolean getValue() {
        return value;
    }
    public void setValue(Boolean value) {
        this.value = value;
    }

    public String getStringValue() {
        return svalue;
    }
    public void setStringValue(String value) {
        this.svalue = value;
    }


    @Override
    public Condition updateInstruction(List<Variable> varList, ArrayList<ASTD> ASTDTree) {
        for(Variable v : varList) {
            Object v_init = v.getInit();
            if(v_init != null) {
                String init = v_init.toString();
                //it's a json object
                if(init.contains("{") && init.contains(":") && init.contains("}")) {
                    init = Conventions.JSON_PARSE.replace(ILTranslator.USYMBOL_1, "\""+init+"\"");
                    setStringValue(svalue.replace(v.getName(), init));
                }
                else {
                    if (v.getName().contains(Conventions.STRUCT_VAR))
                        setStringValue(svalue.replace(v.getName(), init));
                    else {
                        if(!(v.getRef() == null)){
                            setStringValue(svalue.replace(
                                    v.getName(), ASTDTree.get(0).prefixTree(ASTDTree, v.getRef()) + "." + init));
                        }
                        else{
                            setStringValue(svalue.replace(v.getName(),
                                    init));

                        }
                    }
                }
            }
        }
        return this;
    }


    @Override
    public String generateCode(ILTranslator.Lang lang, ResourceBundle bundle) {
        StringBuilder out = new StringBuilder();
        String cond = String.valueOf(getValue());
        if (!cond.contains("null")) {
            if(cond.contains(Conventions.TRUE))
                out.append(bundle.getString("TRUE"));
            else
                out.append(bundle.getString("FALSE"));
        }
        cond = getStringValue();
        if (cond != null && !cond.isEmpty()) {
            out.append(cond);
        }

        return out.toString();
    }

    @Override
    public boolean isEqualTo(String name) {
        if(svalue != null) {
            if(svalue.contains(name))
                return true;
        }

        return false;
    }
}
