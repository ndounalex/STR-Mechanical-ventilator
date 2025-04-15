package com.udes.utils;

import com.udes.model.astd.items.Action;
import com.udes.model.astd.items.Variable;
import com.udes.model.il.conditions.AndCondition;
import com.udes.model.il.conditions.CallCondition;
import com.udes.model.il.conditions.Condition;
import com.udes.model.il.conditions.OrCondition;
import com.udes.model.il.containers.Entry;
import com.udes.model.il.statements.AssignStatement;
import com.udes.model.il.statements.IFFIStatement;
import com.udes.model.il.statements.SeqStatement;
import com.udes.model.il.statements.Statement;
import com.udes.model.il.terms.Bool;
import com.udes.model.il.terms.Term;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class Trash {

    /*
     * @brief substitute Action with the precondition Pred
     * @param  The precondition
     * @param  The action
     * @return The substitued Action
     */
    public static Condition substitute(Statement pred, Condition _action) {

        // Substitutions only apply on the right side for assignments
        // and all sides for conditions
        // init(astd) = {v1:= u1, v2:= u2, ...}
        List<Variable> varList = splitPrecondition(pred);
        final Condition action = _action;
        return updateILModel(varList, action);
    }
    /*
     * @brief substitute Action with the precondition Pred
     * @param  The precondition
     * @param  The action
     * @return The substitued Action
     */
    public static Statement substitute(Statement pred, Statement _action) {

        // Substitutions only apply on the right side for assignments
        // and all sides for conditions
        // init(astd) = {v1:= u1, v2:= u2, ...}
        List<Variable> varList = splitPrecondition(pred);
        final Statement action = _action;
        return updateILModel(varList, action);
    }

    /*
     * @brief split statements into a list of variables
     * @param  The precondition statement
     * @return The list of variables
     */
    private static List<Variable> splitPrecondition(Statement _pred) {
        final Statement pred = _pred;
        List<Variable> varList = new ArrayList<>();
        if(pred instanceof SeqStatement) {
            SeqStatement seqStmt = (SeqStatement) pred;
            List<Statement> stmtList = seqStmt.getStatement();
            if(stmtList != null) {
                stmtList.forEach( stmt ->
                        varList.addAll(splitPrecondition(stmt))
                );
            }
        }
        else if(pred instanceof AssignStatement) {
            AssignStatement assStmt = (AssignStatement) pred;
            varList.add(new Variable(assStmt.getVar().getId(),
                    null, ((Term) assStmt.getExpr()).getId(), null));
        }
        return varList;
    }

    /*
     * @brief substitute Action with the precondition (Call)
     * @param  The precondition
     * @param  The action
     * @return The substitued Action
     */
    public static Condition callSubstitute(List<Variable> varList, Condition action) {

        // Substitutions only apply on the right side for assignments
        // and all sides for conditions
        // it's a call astd
        // call_params= {v1:= u1, v2:= u2, ...}
        return updateILModel(varList, action);
    }

    /*
     * @brief  Updates the IL model after substitution
     * @param  The precondition
     * @param  The action
     * @return The substitued Action
     */
    private static Condition updateILModel(List<Variable> varList, Condition _action) {
        final Condition action = _action;
        if(action instanceof AndCondition) {
            AndCondition cond = (AndCondition) action;
            List<Condition> cList = cond.getCondition(),
                    newList = new ArrayList<>();

            if(cList != null) {
                cList.forEach(c ->
                        newList.add(updateILModel(varList, c))
                );
                return  new AndCondition(newList);
            }
        }
        else if (action instanceof CallCondition) {
            CallCondition cond = (CallCondition) action;
            List<String> params = new ArrayList<>(cond.getParams());

            if(params != null) {
                if(varList != null) {
                    String stateVar = params.get(0);
                    varList.forEach( v -> {
                        if (v.getName().compareTo(stateVar) == 0) {
                            Object v_init = v.getInit();
                            if(v_init != null)
                                params.set(0, v_init.toString());
                            return;
                        }
                    });
                    cond.setParams(params);
                    return cond;
                }
            }
        }
        else if (action instanceof OrCondition) {
            OrCondition cond = (OrCondition) action;
            List<Condition> cList = cond.getCondition(),
                    newList = new ArrayList<>();

            if(cList != null) {
                cList.forEach( c ->
                        newList.add(updateILModel(varList, c))
                );
                return new OrCondition(newList);
            }
        }
        else if (action instanceof Bool) {

        }
        return action;
    }

    /*
     * @brief  Updates the IL model after substitution
     * @param  The precondition
     * @param  The action
     * @return The substitued Action
     */

    private static Statement updateILModel(List<Variable> varList, Statement _action) {
        final Statement action = _action;
        if(action instanceof Action) {
            Action act = (Action) action;
            String code = act.getCode();
            if(code != null) {
                String[] res = code.split("=");
                if(res.length == 2) {
                    AtomicReference<String> cached_code = new AtomicReference<>();
                    cached_code.set(code);
                    varList.forEach( v -> {
                        Object v_init = v.getInit();
                        if(v_init != null)
                            cached_code.set(res[1].replace(v.getName(), v_init.toString()));
                    });
                    if(cached_code.get().compareTo(code) != 0)
                        cached_code.set(res[0] + "=" + cached_code.get());

                    act.setCode(cached_code.get());
                }
                return act;
            }
        }
        else if(action instanceof IFFIStatement) {
            IFFIStatement iffiStmt = (IFFIStatement) action;
            List<Entry<Condition, Statement>> ifBody = iffiStmt.getIFFIStatement(),
                    newBody = new ArrayList<>();
            if(ifBody != null) {
                for(Entry<Condition, Statement> entry : ifBody) {
                    newBody.add(new Entry<>(updateILModel(varList, entry.getKey()),
                            updateILModel(varList, entry.getValue())));
                }

                return new IFFIStatement(newBody);
            }
        }
        else if (action instanceof SeqStatement) {
            SeqStatement seqStmt = (SeqStatement) action;
            List<Statement> sList = seqStmt.getStatement(),
                    seqList = new ArrayList<>();
            if(sList != null) {
                sList.forEach(st -> seqList.add(updateILModel(varList, st)));
                return new SeqStatement(seqList);
            }
        }

        return action;
    }

}

 /*List<String> hisStates = getHistoryStates();
        if(!hisStates.isEmpty()) {
            aut2json.setHistoryIndex(Conventions.ARRAY_ELEM
                    .replace(ILTranslator.USYMBOL_2, Conventions.TEMP_JSON_ITEM_ATTRIBUTE)
                    .replace(ILTranslator.USYMBOL_1, (this.getName() + "_" + ExecSchemaParser.STATE).toUpperCase()));
        }

        if(!hisStates.isEmpty() && hisStates.size() >= 1) {
            stmtList.add(new AssignStatement(new Term(Conventions.ARRAY_ELEM.replace(ILTranslator.USYMBOL_2,
                             aut2json.getNodeIndex()).replace(ILTranslator.USYMBOL_1,
                            (this.getName() + "_" + ExecSchemaParser.HISTORY).toUpperCase())), new Term(Conventions.JSON_ARRAY_INSTANCE)));

            for(int i=0; i < hisStates.size(); i++) {
                String hist0 = hisStates.get(i);
                if(i == 0)
                   stmtList.add(new DeclStatement(new Variable(Conventions.TEMP_JSON_ITEM_ATTRIBUTE,
                                                               Conventions.JSON, Conventions.JSON_OBJECT_INSTANCE)));
                else
                   stmtList.add(new AssignStatement(new Term(Conventions.TEMP_JSON_ITEM_ATTRIBUTE),
                                                    new Term(Conventions.JSON_OBJECT_INSTANCE)));

                stmtList.add(new AssignStatement(new Term(Conventions.ARRAY_ELEM
                        .replace(ILTranslator.USYMBOL_2, Conventions.TEMP_JSON_ITEM_ATTRIBUTE)
                        .replace(ILTranslator.USYMBOL_1, ExecSchemaParser.NAME.toUpperCase())),
                                 new Term("\"" + hist0 + "\"")));

                stmtList.add(new AssignStatement(new Term(aut2json.getHistoryIndex()),
                                 new Term(Conventions.JSON_OBJECT_INSTANCE)));

                if (stateToASTDs != null && stateToASTDs.containsKey(hist0)) {
                    ASTD hist0ASTD = stateToASTDs.get(hist0);
                    stmtList.add(hist0ASTD.fillJSONState(aut2json.getHistoryIndex()));
                    String prfxHis = astdObj.prefix(hist0) + ".";
                    stmtList.add(fillAutProperties(aut2json.getHistoryIndex(), prfxHis));
                    //historyASTDIndex = Conventions.ARRAY_ELEM
                    //        .replace(ILTranslator.USYMBOL_2, historyASTDIndex)
                    //        .replace(ILTranslator.USYMBOL_1, ExecSchemaParser.CURRENT_SUB_STATE.toUpperCase());
                    //List<Statement> subStmt = hist0ASTD.currentStateToJson();
                }
                stmtList.add(new CallStatement(Conventions.ADD_ELEM_TO_VECTOR
                                 .replace(ILTranslator.USYMBOL_2, (Conventions.ARRAY_ELEM.replace(ILTranslator.USYMBOL_2,
                                         aut2json.getNodeIndex()).replace(ILTranslator.USYMBOL_1,
                                         (this.getName() + "_" + ExecSchemaParser.HISTORY).toUpperCase()))),
                                 Arrays.asList(Conventions.TEMP_JSON_VAR_ATTRIBUTE)));

            }

        }*/