package com.udes.model.il.record;

import com.udes.model.astd.items.Variable;
import com.udes.model.il.conventions.Conventions;
import com.udes.utils.Constants;

import java.util.*;

public class Record<T> {
    private String name;
    private List<T> properties;

    public Record(String name, List<T> properties) {
        this.name = name;
        this.properties = properties;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<T> getProperties() {
        return properties;
    }

    public void setProperties(List<T> properties) {
        this.properties = properties;
    }

    @Override
    public int hashCode() {
        final int iConstant = 31;
        int iTotal = 1;
        iTotal = iTotal * iConstant + ((name == null) ? 0 : name.hashCode());
        return iTotal;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Record<T> rec = (Record<T>) obj;
        if (name == null) {
            if (rec.name != null) {
                return false;
            }
        }
        // they don't have same name
        else if (!name.equals(rec.name)) {
            return false;
        }
        return true;
    }

    public String toTarget(ResourceBundle bundle) {
        StringBuilder out = new StringBuilder();
        out.append(bundle.getString("RECORD") + getName()).append(bundle.getString("BRA_BEGIN"));
        List<Variable> props = (List<Variable>) getProperties();
        if (props != null) {
            props.forEach( p -> {
                if(Constants.TIMED_SIMULATION){
                    if(p.getType().equals(Conventions.CLOCK)){
                        out.append(bundle.getString("INDENT"))
                                .append(p.getType()
                                        .replace(Conventions.CLOCK, bundle.getString("TIME_TYPE3")).replace("$", p.getName()))
                                .append(bundle.getString("SEMI_COLON_SEP"));
                    }
                    else if(p.getType().equals(Conventions.TIME_TYPE)){
                        out.append(bundle.getString("INDENT"))
                                .append(Conventions.CLOCK
                                        .replace(Conventions.CLOCK, bundle.getString("TIME_TYPE3")).replace("$", p.getName()))
                                .append(bundle.getString("SEMI_COLON_SEP"));
                    }
                    else{
                        out.append(bundle.getString("INDENT"))
                                .append(p.getType().replace(Conventions.MAP, bundle.getString("MAP_TYPE"))
                                        .replace(Conventions.BOOL_TYPE, bundle.getString("INT_TYPE"))
                                        .replace(Conventions.ENTRY, bundle.getString("ENTRY_TYPE"))
                                        .replace(Conventions.STRING, bundle.getString("STRING_TYPE")))
                                .append(bundle.getString("SEP")).append(p.getName())
                                .append(bundle.getString("SEMI_COLON_SEP"));
                    }
                }
                else{
                    out.append(bundle.getString("INDENT"))
                            .append(p.getType().replace(Conventions.MAP, bundle.getString("MAP_TYPE"))
                                    .replace(Conventions.BOOL_TYPE, bundle.getString("INT_TYPE"))
                                    .replace(Conventions.ENTRY, bundle.getString("ENTRY_TYPE"))
                                    .replace(Conventions.STRING, bundle.getString("STRING_TYPE"))
                                    .replace(Conventions.CLOCK, bundle.getString("TIME_TYPE")))
                            .append(bundle.getString("SEP")).append(p.getName())
                            .append(bundle.getString("SEMI_COLON_SEP"));
                }
            });
        }
        out.append(bundle.getString("BRA_END")).append(bundle.getString("SEMI_COLON_SEP"));
        return out.toString();
    }
}
