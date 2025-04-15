package com.udes.model.astd.items;

import com.udes.model.astd.base.ASTD;
import com.udes.model.il.conventions.Conventions;
import com.udes.model.il.statements.Statement;
import com.udes.translator.ILTranslator;
import com.udes.utils.Constants;
import com.udes.utils.Utils;
import com.udes.model.il.terms.Bool;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.udes.model.astd.base.ASTD.rootASTD;

public class Action extends Statement {

    private String code;

    public Action(String code) {
      this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public List<Variable> decodeInstruction() {
        return null;
    }

    private String updateFunctionCall(List<Variable> varList, Matcher m, ArrayList<ASTD> ASTDTree) {
        String func_name = m.group(1), func_params = m.group(2);
        String[] params = func_params.split(",");
        String new_params = "";
        for (String p : params) {
            for(Variable v : varList) {
                Object  v_init = v.getInit();
                if(v_init != null) {
                    String init =  v_init.toString();
                    if(p.contains(v.getName())) {
                        if (init.contains("{") && init.contains(":") && init.contains("}")) {
                            init = Conventions.JSON_PARSE.replace(ILTranslator.USYMBOL_1,
                                                       "\"" + init + "\"");
                            new_params += p.replace(v.getName(), init) + ",";
                        } else {
                            if (v.getName().contains(Conventions.STRUCT_VAR)) {
                                new_params += p.replace(v.getName(), init) + ",";
                            } else {
                                new_params += p.replace(v.getName(),
                                            ASTDTree.get(0).prefixTree(ASTDTree, v.getRef()) + "." + init) + ",";
                            }
                        }
                    }
                }
            }
        }
        if(new_params.length() > 0)
            new_params = new_params.substring(0, new_params.length() - 1);
        else
            new_params = func_params;

        return func_name + "(" + new_params + ")";
    }

    @Override
    public Statement updateInstruction(List<Variable> varList, ArrayList<ASTD> ASTDTree) {
        String code = getCode();
        if(code != null) {
            Matcher m;
            boolean isFunction = (m = Pattern.compile(Constants.FUNC_PARAMS).matcher(code)) != null;
            isFunction = isFunction && m.find();
            if (isFunction) {
                setCode(updateFunctionCall(varList, m, ASTDTree));
            }
            else {
                code = Utils.maskKeywords(code);
                code = Utils.maskQuotes(code);
                code = code.replace("_w", String.valueOf(0x2EFFFF));
                String prev = "";
                for(Variable v  : varList) {
                    Object v_init = v.getInit();
                    if(v_init != null) {
                        String init = v_init.toString();
                        List<Variable> tmp = new ArrayList<>(varList);
                        tmp.remove(v);
                        // mask the previous code
                        if (!prev.isEmpty()) {
                            code = code.replace(prev, String.valueOf(0x0EFFFF));
                            code = code.replace(Conventions.STRUCT_VAR, String.valueOf(0x1EFFFF));
                            code = Utils.maskCallAttributes(code, tmp);
                        }
                        if (v.getName().contains(Conventions.STRUCT_VAR))
                        {
                            code = code.replace(v.getName(), init);
                        }
                        else {
                            code = code.replace(v.getName(),
                                     ASTDTree.get(0).prefixTree(ASTDTree, v.getRef()) + "." + init);
                        }
                        // unmask the previous code
                        if (!prev.isEmpty()) {
                            code = code.replace(String.valueOf(0x0EFFFF), prev);
                            code = code.replace(String.valueOf(0x1EFFFF), Conventions.STRUCT_VAR);
                            code = Utils.unmaskCallAttributes(code, tmp);
                        }
                        prev = init;
                    }
                }
                // unmask code
                code = Utils.unmaskKeywords(code);
                code = Utils.unmaskQuotes(code);
                code = code.replace(String.valueOf(0x2EFFFF), "_w");

                setCode(code);
            }
        }
        return this;
    }

    @Override
    public String generateCode(String eventType, ILTranslator.Lang lang, ResourceBundle bundle, Bool timed) {
        String code = getCode();
        StringBuilder out = new StringBuilder();
        if(code != null) {
            out.append(code);
            if(!code.endsWith(";"))
                out.append(bundle.getString("SEMI_COLON_SEP"));
        }

        return out.toString();
    }
}
