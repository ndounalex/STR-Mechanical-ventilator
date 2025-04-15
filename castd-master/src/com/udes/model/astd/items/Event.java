package com.udes.model.astd.items;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class Event {

    private String name;
    private List<String> when;
    private List<Variable> params;

    public Event(String name, List<Variable> params) {
        this.name = name;
        this.params = params;
    }

    public Event() {}

    public String getName() { return name; }

    public void setName(String name) {
        this.name = name;
    }

    public List<Variable> getParams() {
        return params;
    }

    public void setParams(List<Variable> params) {
        this.params = params;
    }

    public List<String> getWhen() { return when; }

    public void setWhen(List<String> conditions) {
        this.when = conditions;
    }

    @Override
    public int hashCode() {
        final int iConstant = 31;
        int iTotal = 1;
        iTotal = iTotal * iConstant + ((name == null) ? 0 : name.hashCode()) + ((params == null) ? 0 : params.hashCode());
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
        Event evt = (Event) obj;
        if (name == null) {
            if (evt.name != null) {
                return false;
            }
        }
        // they don't have same name no need to merge their parameters
        else if (!name.equals(evt.name)) {
            return false;
        }
        else {
            //they have same name, we should merge their parameters
            if(params == null) {
               if(evt.params != null) {
                   params = evt.params;
               }
            }
            else if (!params.equals(evt.params)) {
                Set<Variable> evtSet = new LinkedHashSet<>(params);
                if(evt.params != null) {
                    evtSet.addAll(evt.params);
                    params = new ArrayList<>(evtSet);
                }
            }
        }
        return true;
    }
}
