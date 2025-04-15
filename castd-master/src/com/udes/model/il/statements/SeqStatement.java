package com.udes.model.il.statements;

import com.udes.model.astd.base.ASTD;
import com.udes.model.astd.items.Variable;
import com.udes.translator.ILTranslator;
import com.udes.model.il.terms.Bool;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class SeqStatement extends Statement {

    public SeqStatement(List<Statement> statement) {
        this.statement = statement;
    }

    public List<Statement> getStatement() {
        return statement;
    }

    public void setStatement(List<Statement> statement) {
        this.statement = statement;
    }

    private List<Statement> statement;

    @Override
    public List<Variable> decodeInstruction() {
        List<Variable> varList = new ArrayList<>();
        List<Statement> stmtList = getStatement();
        if(stmtList != null) {
            stmtList.forEach( stmt -> {
                    if(!(stmt.decodeInstruction() == null)){
                        varList.addAll(stmt.decodeInstruction());
                    }
                }
            );
        }

        return varList;
    }

    @Override
    public Statement updateInstruction(List<Variable> varList, ArrayList<ASTD> ASTDTree) {
        List<Statement> sList = getStatement(),
                seqList = new ArrayList<>();
        if(sList != null) {
            sList.forEach(st -> {
                if(varList != null && st != null)
                    seqList.add(st.updateInstruction(varList, ASTDTree));
            });
            return new SeqStatement(seqList);
        }

        return this;
    }

    @Override
    public String generateCode(String eventType, ILTranslator.Lang lang, ResourceBundle bundle, Bool timed) {
        StringBuilder out = new StringBuilder();
        List<Statement> seqList = getStatement();
        if(seqList != null) {
            seqList.forEach( st -> {
                if(st != null)
                   out.append(st.generateCode(eventType, lang, bundle, timed));
            });
        }

        return out.toString();
    }
}
