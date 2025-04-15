package com.udes.model.astd.items;

import com.udes.model.il.conventions.Conventions;
import com.udes.translator.ILTranslator;
import com.udes.utils.Constants;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;

public class Domain {

    private String type;
    private Object val;
    private List dom;
    private String string;

    public Domain(String type, Object val) {
        this.type = type;
        this.val = val;
        try {
            dom = createDomain();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getVal() {
        return val;
    }

    public void setVal(Object val) {
        this.val = val;
    }

    public List getElements() {
        return dom;
    }

    public String toString() {
        return string;
    }

    public Iterator iterate() {
        return dom.iterator();
    }

    public boolean hasNext() {
        return iterate().hasNext();
    }

    public List createDomain() {

        if(val == null)
            return new ArrayList();

        if(val.toString().matches(Constants.NUMBER_LIST)) {
            String val_ = val.toString().replaceAll("\\{|\\}","");
            String[] values = val_.split(",");
            List<Number> out = new ArrayList<>();
            this.string  = "{";
            if(values != null) {
                for (String value : values) {
                    try {
                        out.add(NumberFormat.getInstance().parse(value));
                        string += value + ",";
                    } catch (ParseException e) {}
                }
                string = string.substring(0, string.length() - 1);
            }
            string += "}";

            return out;
        }
        else if(val.toString().matches(Constants.NUMBER_LIST2)) {
            String val_ = val.toString().replaceAll("\\[|\\]","");
            String[] values = val_.split(",");
            List<Number> out = new ArrayList<>();
            this.string  = "{";
            if(values != null) {
                for (String value : values) {
                    try {
                        out.add(NumberFormat.getInstance().parse(value));
                        string += value + ",";
                    } catch (ParseException e) {}
                }
                string = string.substring(0, string.length() - 1);
            }
            string += "}";

            return out;
        }
        else if(val.toString().matches(Constants.STRING_LIST)) {
            String val_ = val.toString().replaceAll("\\{|\\}","");
            String[] values = val_.split(",");
            List<String> out = new ArrayList<>();
            this.string  = "{";
            if(values != null) {
                for (String value : values) {
                    out.add(value);
                    string += value + ",";
                }
                string = string.substring(0, string.length() - 1);
            }
            string += "}";

            return out;
        }
        else if(val.toString().matches(Constants.STRING_LIST2)) {
            String val_ = val.toString().replaceAll("\\[|\\]","");
            String[] values = val_.split(",");
            List<String> out = new ArrayList<>();
            this.string  = "{";
            if(values != null) {
                for (String value : values) {
                    out.add(value);
                    string += value + ",";
                }
                string = string.substring(0, string.length() - 1);
            }
            string += "}";

            return out;
        }
        else if(val.toString().matches(Constants.INT_INTERVAL)) {
            String val_ = val.toString().replaceAll("\\[|\\]","");
            String[] values = val_.split(",");
            List<Integer> out = new ArrayList<>();
            this.string  = "{";
            if(values != null && values.length > 1) {
                int lowBound = Integer.parseInt(values[0]),
                    upperBound = Integer.parseInt(values[1]);
                for (int i = lowBound; i < upperBound; i++) {
                    out.add(i);
                    string += i + ",";
                }
                string = string.substring(0, string.length() - 1);
            }
            string += "}";

            return out;
        }
        else if(val instanceof  JSONArray) {
            JSONArray arrObj = (JSONArray) val;
            List<JSONObject> out = new ArrayList<>();
            this.string  = "{";
            if (arrObj != null) {
                for (Object value : arrObj) {
                    out.add((JSONObject) value);
                    string += Conventions.JSON_PARSE
                               .replace(ILTranslator.USYMBOL_1, "\"" + value.toString() + "\"") + ",";
                }
                string = string.substring(0, string.length()-1);
            }
            string += "}";

            return  out;
        }
        return Arrays.asList(val.toString());
    }
}
