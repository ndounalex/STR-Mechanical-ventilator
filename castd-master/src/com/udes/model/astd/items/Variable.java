package com.udes.model.astd.items;

import com.udes.model.il.conventions.Conventions;
import com.udes.model.il.statements.Statement;

import java.io.Serializable;
import java.util.*;

public class Variable implements Serializable{

    private String name;
    private String type;
    private Object init;
    private String ref;

    public Variable( String name, String type, Object init, String ref) {
        this.name =  name;
        this.type =  type;
        this.init =  init;
        this.ref = ref;
    }

    public String getName() {
        return (name != null) ? name : new String();
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getInit() {
        return init;
    }

    public void setInit(String init) {
        this.init = init;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    @Override
    public int hashCode() {
        final int iConstant = 31;
        int iTotal = 1;
        iTotal = iTotal * iConstant
                + ((name == null) ? 0 : name.hashCode())
                + ((type == null) ? 0 : type.hashCode())
                + ((init == null) ? 0 : init.hashCode());
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

        Variable var = (Variable) obj;
        if (name == null || type == null) {
            if (var.name != null) {
                return false;
            }
            if (var.type != null) {
                return false;
            }
        }
        if (!name.equals(var.name) || !type.equals(var.type)) {
            return false;
        }
        if (init == null) {
            if (var.init != null) {
                return false;
            }
        }
        else if (!init.equals(var.init)) {
                return false;
            }
        if(ref == null){
            if (var.ref != null){
                return false;
            }
        }
       else if(!ref.equals(var.ref)){
            return false;
        }
        return true;
    }

    public String toTarget(ResourceBundle bundle) {
        StringBuilder out = new StringBuilder();
        if(getInit() != null) {
            if(getType().equals(Conventions.INT)
                || getType().contains(Conventions.DOUBLE)
                || getType().contains(Conventions.BOOL_TYPE1)
                || getType().contains(Conventions.FLOAT)
                || getType().contains(Conventions.SHORT)
                || getType().contains(Conventions.LONG)) {
                out.append(getType()).append(bundle.getString("SEP")).append(getName())
                        .append(bundle.getString("ASSIGN")).append(getInit().toString())
                        .append(bundle.getString("SEMI_COLON_SEP"));
            }
            else if(getType().equals(Conventions.TIME_TYPE)){
                if (getInit().equals(Conventions.CST)){
                    out.append(bundle.getString("TIME_TYPE"))
                            .append(bundle.getString("SEP"))
                            .append(getName())
                            .append(bundle.getString("ASSIGN"))
                            .append(bundle.getString("ASSIGN_TIME"))
                            .append(bundle.getString("SEMI_COLON_SEP"));
                }
                else {
                    out.append(bundle.getString("TIME_TYPE"))
                            .append(bundle.getString("SEP"))
                            .append(getName())
                            .append(bundle.getString("ASSIGN"))
                            .append(getInit().toString())
                            .append(bundle.getString("SEMI_COLON_SEP"));
                }
            }
            else if(getType().equals(Conventions.TIME_TYPE2)){
                if (getInit().equals(Conventions.CST)){
                    out.append(bundle.getString("TIME_TYPE2"))
                            .append(bundle.getString("SEP"))
                            .append(getName())
                            .append(bundle.getString("ASSIGN"))
                            .append(bundle.getString("ASSIGN_TIME"))
                            .append(bundle.getString("SEMI_COLON_SEP"));
                }
                else {
                    out.append(bundle.getString("TIME_TYPE2"))
                            .append(bundle.getString("SEP"))
                            .append(getName())
                            .append(bundle.getString("ASSIGN"))
                            .append(getInit().toString())
                            .append(bundle.getString("SEMI_COLON_SEP"));
                }
            }
            else if(getType().equals(Conventions.TIME_TYPE3)){
                //time in simulation!
                if(getName().equals(Conventions.CURRENT_TIME)){
                    out.append(bundle.getString("TIME_TYPE"))
                            .append(bundle.getString("SEP"))
                            .append(getName())
                            .append(bundle.getString("PAR_BEGIN"))
                            .append(getInit().toString())
                            .append(bundle.getString("PAR_END"))
                            .append(bundle.getString("SEMI_COLON_SEP"));
                }
                else{
                    out.append(bundle.getString("TIME_TYPE"))
                            .append(bundle.getString("SEP"))
                            .append(getName())
                            .append(bundle.getString("PAR_BEGIN"))
                            .append(Conventions.CURRENT_TIME)
                            .append(bundle.getString("PAR_END"))
                            .append(bundle.getString("SEMI_COLON_SEP"));
                }
            }
            else if(getType().equals(Conventions.QUEUE_T)){
                out.append(bundle.getString("QUEUE_TYPE"))
                        .append(bundle.getString("SEP"))
                        .append(getName())
                        .append(bundle.getString("ASSIGN"))
                        .append(getInit().toString())
                        .append(bundle.getString("SEMI_COLON_SEP"));
            }
            else if(getType().equals(Conventions.MUTEX)){
                out.append(bundle.getString("MUTEX"))
                        .append(bundle.getString("SEP"))
                        .append(getName())
                        .append(bundle.getString("SEMI_COLON_SEP"));
            }
            else {
                out.append(getType().replace(Conventions.SET, bundle.getString("SET_TYPE"))).append(bundle.getString("SEP")).append(getName())
                        .append(bundle.getString("SEMI_COLON_SEP"));
            }
        }
        else if(getType().equals(Conventions.TIME_TYPE)){
            out.append(bundle.getString("TIME_TYPE"))
                    .append(bundle.getString("SEP"))
                    .append(getName())
                    .append(bundle.getString("SEMI_COLON_SEP"));
        }
        else if(getType().equals(Conventions.TIME_TYPE2)){
            out.append(bundle.getString("TIME_TYPE2"))
                    .append(bundle.getString("SEP"))
                    .append(getName())
                    .append(bundle.getString("SEMI_COLON_SEP"));
        }
        else if(getType().equals(Conventions.QUEUE_T)) {
            out.append(bundle.getString("QUEUE_TYPE"))
                    .append(bundle.getString("SEP"))
                    .append(getName())
                    .append(bundle.getString("SEMI_COLON_SEP"));
        }
        else if(getType().equals(Conventions.MUTEX)){
            out.append(bundle.getString("MUTEX"))
                    .append(bundle.getString("SEP"))
                    .append(getName())
                    .append(bundle.getString("SEMI_COLON_SEP"));
        }
        else {
            out.append(getType()).append(bundle.getString("SEP")).append(getName())
                    .append(bundle.getString("SEMI_COLON_SEP"));
        }
        return out.toString();
    }
}
