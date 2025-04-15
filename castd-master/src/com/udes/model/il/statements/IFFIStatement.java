package com.udes.model.il.statements;

import com.udes.model.astd.items.Variable;
import com.udes.model.il.conditions.CallCondition;
import com.udes.model.il.conditions.Condition;
import com.udes.model.il.containers.Entry;
import com.udes.model.il.terms.Bool;
import com.udes.parser.Parser;
import com.udes.translator.ILTranslator;
import com.udes.model.astd.base.ASTD;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;
import com.udes.model.il.conventions.Conventions;
import com.udes.utils.Constants;

import java.util.*;

public class IFFIStatement extends Statement {

    private List<Entry<Condition, Statement>> collections;

    public IFFIStatement(List<Entry<Condition, Statement>> collections) {
           this.collections = collections;
    }

    public List<Entry<Condition, Statement>> getIFFIStatement() {
        return collections;
    }

    public void setIFFIStatement(List<Entry<Condition, Statement>> collections) {
        this.collections = collections;
    }

    @Override
    public List<Variable> decodeInstruction() {
        return null;
    }

    @Override
    public Statement updateInstruction(List<Variable> varList, ArrayList<ASTD> ASTDTree) {
        List<Entry<Condition, Statement>> ifBody = getIFFIStatement(),
                newBody = new ArrayList<>();
        if(ifBody != null) {
            for (Entry<Condition, Statement> entry : ifBody) {
                if(entry.getKey() != null) {
                    newBody.add(new Entry<>(entry.getKey().updateInstruction(varList, ASTDTree),
                            entry.getValue().updateInstruction(varList, ASTDTree)));
                }
                else {
                    newBody.add(new Entry<>(null, entry.getValue().updateInstruction(varList, ASTDTree)));
                }
            }

            return new IFFIStatement(newBody);
        }

        return this;
    }

    @Override
    public String generateCode(String eventType, ILTranslator.Lang lang, ResourceBundle bundle, Bool timed) {
        List<Entry<Condition, Statement>> iffi = getIFFIStatement();
        StringBuilder out = new StringBuilder();
        if(iffi != null) {
            if(iffi.size() == 1) {
                Entry<Condition, Statement> ifblock = iffi.iterator().next();
                String c1 = ifblock.getKey().generateCode(lang, bundle);
                if(c1 != null && !c1.isEmpty() && ifblock.getValue() != null) {
                    out.append(bundle.getString("IF")
                                    .replace(ILTranslator.USYMBOL_1, c1))
                            .append(bundle.getString("BRA_BEGIN"))
                            .append(ifblock.getValue().generateCode(eventType, lang, bundle, timed)
                                    .replaceAll("(?m)^", "\t"))
                            .append(bundle.getString("BRA_END"))
                            .append(bundle.getString("NEWLINE"));
                }
                else if(timed.getValue()){
                    out.append(bundle.getString("IF")
                                    .replace(ILTranslator.USYMBOL_1, c1))
                            .append(bundle.getString("BRA_BEGIN"))
//                            .append(bundle.getString("CLOCK_RESET")
//                                    .replace(ILTranslator.USYMBOL_1, Conventions.LETS)
//                                    .replaceAll("(?m)^", "\t"))
//                            .append(bundle.getString("SEMI_COLON_SEP"))
                            .append(bundle.getString("CONTINUE")
                                    .replaceAll("(?m)^", "\t"))
                            .append(bundle.getString("BRA_END"));
                }
            }
            else if (iffi.size() > 1) {
                AtomicReference<Integer> i = new AtomicReference<>(0);
                AtomicReference<Statement> j = new AtomicReference<>(null);
                iffi.forEach(entry -> {
                    if(i.get() == 0) {
                        String c = entry.getKey().generateCode(lang, bundle);
                        if(c != null && !c.isEmpty()) {
                            if(entry.getValue() != null) {
                                out.append(bundle.getString("IF")
                                        .replace(ILTranslator.USYMBOL_1, c))
                                        .append(bundle.getString("BRA_BEGIN"))
                                        .append(entry.getValue().generateCode(eventType, lang, bundle, timed)
                                                     .replaceAll("(?m)^", "\t"))
                                        .append(bundle.getString("BRA_END"));
                            }
                            else if(timed.getValue()){
                                out.append(bundle.getString("IF")
                                                .replace(ILTranslator.USYMBOL_1, c))
                                        .append(bundle.getString("BRA_BEGIN"))
                                        //.append(bundle.getString("CLOCK_RESET")
                                        //        .replace(ILTranslator.USYMBOL_1, Conventions.LETS)
                                        //        .replaceAll("(?m)^", "\t"))
                                        //.append(bundle.getString("SEMI_COLON_SEP"))
                                        .append(bundle.getString("CONTINUE")
                                                .replaceAll("(?m)^", "\t"))
                                        .append(bundle.getString("BRA_END"));
                            }
                            else {
                                out.append(bundle.getString("IF")
                                        .replace(ILTranslator.USYMBOL_1, c))
                                        .append(bundle.getString("BRA_BEGIN"))
                                        .append(bundle.getString("CONTINUE")
                                                      .replaceAll("(?m)^", "\t"))
                                        .append(bundle.getString("BRA_END"));
                            }
                        }
                    }
                    else {
                        if(entry.getKey() != null) {
                            String c = entry.getKey().generateCode(lang, bundle);
                            if(c.equals(Constants.SIMULATION_BLOCK)){
                                out.append(c);
                            }
                            else if (c != null && !c.isEmpty()) {
                                out.append(bundle.getString("ELSE"));
                                out.append(bundle.getString("IF").replace(ILTranslator.USYMBOL_1, c));
                                out.append(bundle.getString("BRA_BEGIN"))
                                        .append(entry.getValue().generateCode(eventType, lang, bundle, timed)
                                                     .replaceAll("(?m)^", "\t"))
                                        .append(bundle.getString("BRA_END"));
                            } else {
                                j.set(entry.getValue());
                            }
                        }
                        else {
                            j.set(entry.getValue());
                        }
                    }
                    i.set(i.get() + 1);
                });
                if(j.get() != null) {
                    out.append(bundle.getString("ELSE"));
                    out.append(bundle.getString("BRA_BEGIN"))
                            .append(j.get().generateCode(eventType, lang, bundle, timed).replaceAll("(?m)^", "\t"))
                            .append(bundle.getString("BRA_END"));
                }
                out.append(bundle.getString("NEWLINE"));
            }
        }

        return out.toString();
    }
}
