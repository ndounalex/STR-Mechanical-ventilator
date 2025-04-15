package com.udes.model.il.record;

import com.udes.model.astd.items.Variable;

import java.util.List;
import java.util.ResourceBundle;

import com.udes.translator.ILTranslator;
import com.udes.utils.Constants;
import com.udes.utils.Utils;

public class Enum extends Record<String> {

    public Enum(String name, List<String> properties) {
        super(name, properties);
    }

    public String toTarget(ResourceBundle bundle) {
        StringBuilder out = new StringBuilder(),
                      tmp = new StringBuilder();
        out.append(bundle.getString("ENUM") + getName()).append(bundle.getString("BRA_BEGIN"));
        List<String> props = (List<String>) getProperties();
        if (props != null) {
            int size = props.size(), it = 0;
            for (; it < size - 1; ++it) {
                out.append(bundle.getString("INDENT")).append(props.get(it));
                out.append(bundle.getString("COMMA_NEWLINE"));
                if(Constants.EXEC_STATE_ACTIVATED) {
                    tmp.append(bundle.getString("INDENT"))
                       .append(bundle.getString("STRINGIFY").replace(ILTranslator.USYMBOL_1, props.get(it)));
                    tmp.append(bundle.getString("COMMA_NEWLINE"));
                }
            }
            if (size >= 1) {
                out.append(bundle.getString("INDENT")).append(props.get(size - 1));
                if(Constants.EXEC_STATE_ACTIVATED) {
                    tmp.append(bundle.getString("INDENT"))
                       .append(bundle.getString("STRINGIFY").replace(ILTranslator.USYMBOL_1, props.get(size-1)));
                }
            }
        }
        out.append(bundle.getString("BRA_END")).append(bundle.getString("SEMI_COLON_SEP"));
        if(Constants.EXEC_STATE_ACTIVATED) {
            out.append(bundle.getString("CONST_CHAR_PTR2") + Utils.javaCapStyle(getName()))
               .append(bundle.getString("LAMBDA_CAPTURE").replace(ILTranslator.USYMBOL_1, ""))
               .append(bundle.getString("ASSIGN")).append(bundle.getString("BRA_BEGIN"));
            out.append(tmp.toString());
            out.append(bundle.getString("BRA_END")).append(bundle.getString("SEMI_COLON_SEP"));
        }
        return out.toString();
    }
}
