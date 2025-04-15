package com.udes.model.astd.items;

import com.udes.model.il.conventions.Conventions;
import com.udes.translator.ILTranslator;
import com.udes.utils.Constants;
import org.json.simple.JSONValue;

import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class Constant extends Variable {

    public Constant(String name, String type, Object init, String ref) {
        super(name, type, init, ref);
    }

    public String toTarget(ResourceBundle bundle) {
        StringBuilder out = new StringBuilder();
        if(getType().contains(Conventions.SET)) {
            out.append(bundle.getString("CONST"))
                    .append(getType().replace(Conventions.SET, bundle.getString("VECTOR_TYPE")))
                    .append(bundle.getString("SEP")).append(getName())
                    .append(bundle.getString("ASSIGN"));
            Object init = getInit();
            try {
                List<Object> lst = (init instanceof Domain) ? ((Domain) init).getElements()
                        : ((init instanceof List) ? (List) init
                        : Arrays.asList(init.toString()));
                StringBuilder tmp = new StringBuilder("");
                if (lst != null) {
                    int size = lst.size(), it = 0;
                    for (; it < size - 1; ++it) {
                        String item = lst.get(it).toString();
                        if(item.matches(Constants.ID)){
                            tmp.append(item).append(bundle.getString("COMMA_SEP"));
                        }
                        else {
                            tmp.append("json::parse(\"" + JSONValue.escape(item) + "\")");
                            tmp.append(bundle.getString("COMMA_SEP"));
                        }
                    }
                    if (size >= 1) {
                        String item = lst.get(size-1).toString();
                        if(item.matches(Constants.ID)) {
                            tmp.append(item);
                        }
                        else {
                            tmp.append("json::parse(\""+JSONValue.escape(item)+"\")");
                        }
                    }
                }
                out.append(bundle.getString("ARRAYS")
                        .replace(ILTranslator.USYMBOL_1, tmp.toString().replaceAll("\\[|\\]", "")));
            } catch (Exception e) {
                if (Constants.DEBUG) e.printStackTrace();
            }
        }
        return out.toString();
    }
}
