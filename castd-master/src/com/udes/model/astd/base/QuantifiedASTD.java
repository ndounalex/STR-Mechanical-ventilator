package com.udes.model.astd.base;

import com.udes.model.astd.items.*;
import com.udes.model.astd.tojson.ToJson;
import com.udes.model.astd.types.Automaton;
import com.udes.model.il.conditions.AndCondition;
import com.udes.model.il.conditions.CallCondition;
import com.udes.model.il.conditions.Condition;
import com.udes.model.il.containers.Entry;
import com.udes.model.il.conventions.Conventions;
import com.udes.model.il.methods.Function;
import com.udes.model.il.statements.*;
import com.udes.model.il.terms.Bool;
import com.udes.model.il.terms.Term;
import com.udes.optimizer.KappaOptimizer;
import com.udes.parser.ASTDParser;
import com.udes.utils.Constants;
import com.udes.utils.Utils;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class QuantifiedASTD extends UnaryASTD {

    private Variable qvariable;
    private Domain domain;

    public QuantifiedASTD(String name,
                          List<Variable> attributes,
                          List<Variable> params,
                          Action astdAction,
                          ASTD body,
                          Variable qvariable,
                          Domain domain) {
        super(name, attributes, params, astdAction, body);
        this.qvariable = qvariable;
        this.domain = domain;
        Utils.qvarDic = new Hashtable<>();
    }

    public QuantifiedASTD(String name,
                          ASTD body,
                          Variable qvariable,
                          Domain domain) {
        super(name, body);
        this.qvariable = qvariable;
        this.domain = domain;
        Utils.qvarDic = new Hashtable<>();
    }

    public QuantifiedASTD() {
        super();
        Utils.qvarDic = new Hashtable<>();
    }

    public Variable getQvariable() { return qvariable; }

    public void setQvariable(Variable qvariable) { this.qvariable = qvariable; }

    public Domain getDomain() { return domain; }

    public void setDomain(Domain domain) { this.domain = domain; }

    @Override
    public List<Function> generateFinalFunc(ArrayList<ASTD> ASTDTree){
        return new ArrayList<>();
    }

    /*
     * @brief Generate variable declarations from ASTD states
     * @param  ASTD model
     * @return List of variables
     */
    @Override
    public List<Variable> trans_var() {
        List<Variable> varList = new ArrayList<>();
        // const set of values taken by the quantified variable
        String domType = (domain != null) ? domain.getType() : "";
        if(!domType.equals(Constants.UNBOUNDEDDOMAIN)) {
            varList.add(new Constant(Conventions.getVarSet(getName()),
                                     Conventions.getSetType(getQvariable().getType()),
                                     getDomain(), getName()));
        }
        else {
            // Nothing to be done here - play with Map
        }
        // body
        List<Variable> varBody = getBody().trans_var();
        if(varBody!= null && !varBody.isEmpty())
            varList.addAll(varBody);

        return varList;
    }
    /*
     * @brief  Computes all attribute prefixes. It will be used later by guards and actions.
     * @param  ASTD
     * @return The map of var name and their associated prefix
     */
    @Override
    public List<Variable> getAllVariables() {
        List<Variable> prefAttr = new ArrayList<>();
        Variable qvar = getQvariable();
        prefAttr.add(qvar);
        List<Variable> attrs = getAttributes();
        if(attrs != null) {
            prefAttr.addAll(attrs);
        }
        List<Variable> tmpBody = getBody().getAllVariables();
        if(tmpBody != null && !tmpBody.isEmpty())
            prefAttr.addAll(tmpBody);

        return prefAttr;
    }
    /*
     * @brief Computes the prefix that should be added to access a property of a structure
     * @param  the parent ASTD model
     * @param  the child name to prefix
     * @return A string prefix
     */
    @Override
    public String prefix(String childName) {
        String prefixBase = Conventions.getStructVar(getName());
        if(getName().compareTo(childName) == 0) {
            return prefixBase;
        }
        else {

            String bodyPrefix = getBody().prefix(childName);

            //No prefix initiated yet, return the body prefix and the childName
            if(Constants.DUMMY_PREFIX.size() == 0){
                return bodyPrefix;
            }

            if (bodyPrefix != null) {
                Variable qvar = getQvariable();
                String prfx_var = Constants.DUMMY_PREFIX.get(qvar);
                //Unknown bug - FROM LIONEL
                if(prfx_var.contains(Conventions.STRUCT_VAR + "_" + Conventions.STRUCT_VAR + "_")) {
                    // Temporary fix - need to update this - FROM LIONEL
                    String prfx1 = prfx_var.replaceAll("(" + Conventions.STRUCT_VAR + "\\_)+",
                                        Conventions.STRUCT_VAR + "_");
                    Constants.DUMMY_PREFIX.replace(qvar, prfx_var, prfx1);
                    prfx_var = prfx1;
                }

                String src = Conventions.getStructVar(getBody().getName());
                String srcprefix = prfx_var.substring(0, prfx_var.length() - (prefixBase.length() + qvar.getName().length() + 1));
                String dst = Conventions.getStateCall(prfx_var);

                //Need to check if .f[src exists, if we don't check it will add .f[f[src
                // what happens when there is nested quantified ASTD.
                String res;
                if(bodyPrefix.contains(Conventions.FUNC_STATE+"["+srcprefix+src)){

                    res = bodyPrefix.replace(Conventions.FUNC_STATE+"["+srcprefix+src, Conventions.FUNC_STATE+"["+srcprefix+prefixBase + "."+ dst);

                    Pattern srcPattern = Pattern.compile("\\b"+src+"\\b");
                    Matcher matcher = srcPattern.matcher(res);
                    res = matcher.replaceAll(dst);
//                    res = res.replace(src, dst);
                }
                else{
                    res = bodyPrefix.replace(src, dst);
                }

                String funcIdx = Conventions.FUNC_STATE + "[";
                if(res.contains(funcIdx + getName())) {
                    // Temporary fix - need to update this
                    res = res.replace(funcIdx + getName(),
                                  funcIdx + Conventions.getStructVar(getName()));
                }
                return prefixBase + "." + res;
            }
        }
        return null;
    }

    @Override
    public String prefixTree(ArrayList<ASTD> CallList) {
        QuantifiedASTD astd = (QuantifiedASTD) CallList.get(0);
        ArrayList<ASTD> ASTDTree = new ArrayList<>();
        for(int i = 1; i < CallList.size(); i++){
            ASTDTree.add(CallList.get(i));
        }
        String prefixBase = Conventions.getStructVar(astd.getName());
        if(ASTDTree.isEmpty()) {
            return prefixBase;
        }
        else {

            String bodyPrefix = ASTDTree.get(0).prefixTree(ASTDTree);

            //No prefix initiated yet, return the body prefix and the childName
            if(Constants.DUMMY_PREFIX.size() == 0){
                return bodyPrefix;
            }

            if (bodyPrefix != null) {
                Variable qvar = getQvariable();
                String prfx = Constants.DUMMY_PREFIX.get(qvar);
                //Unknown bug - FROM LIONEL
                if(prfx.contains(Conventions.STRUCT_VAR + "_" + Conventions.STRUCT_VAR + "_")) {
                    // Temporary fix - need to update this - FROM LIONEL
                    String prfx1 = prfx.replaceAll("(" + Conventions.STRUCT_VAR + "\\_)+",
                            Conventions.STRUCT_VAR + "_");
                    Constants.DUMMY_PREFIX.replace(qvar, prfx, prfx1);
                    prfx = prfx1;
                }

                String src = Conventions.getStructVar(getBody().getName());
                String srcprefix = prfx.substring(0, prfx.length() - (prefixBase.length() + qvar.getName().length() + 1));
                String dst = Conventions.getStateCall(prfx);

                //Need to check if .f[src exists, if we don't check it will add .f[f[src
                // what happens when there is nested quantified ASTD.
                String res;
                if(bodyPrefix.contains(Conventions.FUNC_STATE+"["+srcprefix+src)){

                    res = bodyPrefix.replace(Conventions.FUNC_STATE+"["+srcprefix+src, Conventions.FUNC_STATE+"["+srcprefix+prefixBase + "."+ dst);

                    Pattern srcPattern = Pattern.compile("\\b"+src+"\\b");
                    Matcher matcher = srcPattern.matcher(res);
                    res = matcher.replaceAll(dst);
//                    res = res.replace(src, dst);
                }
                else{
                    res = bodyPrefix.replace(src, dst);
                }

                String funcIdx = Conventions.FUNC_STATE + "[";
                if(res.contains(funcIdx + getName())) {
                    // Temporary fix - need to update this
                    res = res.replace(funcIdx + getName(),
                            funcIdx + Conventions.getStructVar(getName()));
                }
                return prefixBase + "." + res;
            }
        }
        return null;
    }

    @Override
    public String prefixTree(ArrayList<ASTD> CallList, String ref) {
        QuantifiedASTD astd = (QuantifiedASTD) CallList.get(0);
        ArrayList<ASTD> ASTDTree = new ArrayList<>();
        for(int i = 1; i < CallList.size(); i++){
            ASTDTree.add(CallList.get(i));
        }
        String prefixBase = Conventions.getStructVar(astd.getName());
        if(ASTDTree.isEmpty() || astd.getName().equals(ref)) {
            return prefixBase;
        }
        else {

            String bodyPrefix = ASTDTree.get(0).prefixTree(ASTDTree, ref);

            //No prefix initiated yet, return the body prefix and the childName
            if(Constants.DUMMY_PREFIX.size() == 0){
                return bodyPrefix;
            }

            if (bodyPrefix != null) {
                Variable qvar = getQvariable();
                String prfx_var = Constants.DUMMY_PREFIX.get(qvar);
                //Unknown bug - FROM LIONEL
                if(prfx_var.contains(Conventions.STRUCT_VAR + "_" + Conventions.STRUCT_VAR + "_")) {
                    // Temporary fix - need to update this - FROM LIONEL
                    String prfx1 = prfx_var.replaceAll("(" + Conventions.STRUCT_VAR + "\\_)+",
                            Conventions.STRUCT_VAR + "_");
                    Constants.DUMMY_PREFIX.replace(qvar, prfx_var, prfx1);
                    prfx_var = prfx1;
                }

                String src = Conventions.getStructVar(getBody().getName());
                String srcprefix = prfx_var.substring(0, prfx_var.length() - (prefixBase.length() + qvar.getName().length() + 1));
                String dst = Conventions.getStateCall(prfx_var);

                //Need to check if .f[src exists, if we don't check it will add .f[f[src
                // what happens when there is nested quantified ASTD.
                String res;
                if(bodyPrefix.contains(Conventions.FUNC_STATE+"["+srcprefix+src)){

                    res = bodyPrefix.replace(Conventions.FUNC_STATE+"["+srcprefix+src, Conventions.FUNC_STATE+"["+srcprefix+prefixBase + "."+ dst);

                    Pattern srcPattern = Pattern.compile("\\b"+src+"\\b");
                    Matcher matcher = srcPattern.matcher(res);
                    res = matcher.replaceAll(dst);
//                    res = res.replace(src, dst);
                }
                else{
                    res = bodyPrefix.replace(src, dst);
                }

                String funcIdx = Conventions.FUNC_STATE + "[";
                if(res.contains(funcIdx + getName())) {
                    // Temporary fix - need to update this
                    res = res.replace(funcIdx + getName(),
                            funcIdx + Conventions.getStructVar(getName()));
                }
                return prefixBase + "." + res;
            }
        }
        return null;
    }

    /*
     * @brief Get the variables of the enclosing ASTDs of an ASTD
     * @param  the parent ASTD model
     * @param  the child name
     * @return A list of variables
     */
    public List<Variable> enclosingASTDVariables(ArrayList<ASTD> CallList) {
        List<Variable> enclosingBase = new ArrayList<>();
        enclosingBase.add(this.getQvariable());
        List<Variable> attributes = getAttributes();
        if(attributes != null)
        enclosingBase.addAll(getAttributes());
        ArrayList<ASTD> ASTDTree = new ArrayList<>();
        for(int i = 1; i < CallList.size(); i++){
            ASTDTree.add(CallList.get(i));
        }
        List<Variable> enclosingTmp = new ArrayList<>();
        if(!enclosingBase.isEmpty())
            enclosingTmp.addAll(enclosingBase);
        if(!ASTDTree.isEmpty()) {
            List<Variable> bodyASTDVariables = getBody().enclosingASTDVariables(ASTDTree);
            if (bodyASTDVariables != null && !bodyASTDVariables.isEmpty())
                enclosingTmp.addAll(bodyASTDVariables);
        }
        return enclosingTmp;
    }

    /*
     * @brief Get the variables of the enclosing ASTDs of an ASTD
     * @param  the parent ASTD model
     * @param  the child name
     * @return A list of variables
     */
    public List<Variable> enclosingASTDParams(ArrayList<ASTD> CallList) {
        List<Variable> enclosingBase = new ArrayList<>();
        List<Variable> attributes = new ArrayList<>();
        if(attributes != null)
            enclosingBase.addAll(getAttributes());

        ArrayList<ASTD> ASTDTree = new ArrayList<>();
        for(int i = 1; i < CallList.size(); i++){
            ASTDTree.add(CallList.get(i));
        }
        List<Variable> enclosingTmp = new ArrayList<>();
        if(!enclosingBase.isEmpty())
            enclosingTmp.addAll(enclosingBase);
        if(!ASTDTree.isEmpty()) {
            List<Variable> bodyASTDVariables = getBody().enclosingASTDParams(ASTDTree);
            if (bodyASTDVariables != null && !bodyASTDVariables.isEmpty())
                enclosingTmp.addAll(bodyASTDVariables);
        }
        return enclosingTmp;
    }

    @Override
    public void kappaOptimize(KappaOptimizer.AnalysisMode kappaOpt) {
        if(ASTDParser.root != null) {
            KappaOptimizer opt = new KappaOptimizer(ASTDParser.root.getName(), this, kappaOpt);
            opt.optimize();
        }
    }

    @Override
    public List<Variable> getAllQVariables(ArrayList<ASTD> CallList) {
        ArrayList<ASTD> ASTDTree = new ArrayList<>();

        List<Variable> list = new ArrayList<>();
        list.add(getQvariable());
        list.addAll(getBody().getAllQVariables(ASTDTree));

        return list;
    }

    @Override
    public List<Variable> getAllEventQVariables(Event e) {
        List<Variable> list = new ArrayList<>();
        list.add(getQvariable());
        List<Variable> bList = getBody().getAllEventQVariables(e);
        if(!bList.isEmpty())
            list.addAll(bList);

        return list;
    }

    @Override
    public List<Variable> getAllEventParams(Event e) {
        List<Variable> list = new ArrayList<>();
        Set<Event> bSet = getBody().findAllEvents();
        if(!bSet.isEmpty()) {
            for(Event _e : bSet) {
                if(e.getName().compareTo(_e.getName()) == 0) {
                    List<Variable> e_params = e.getParams();
                    if(e_params != null)
                        list.addAll(e_params);
                }
            }
        }

        return list;
    }


    public List<Variable> getAllParameters(ASTD astd, String targetName){
        List<Variable> list = new ArrayList<>();
        if(astd.getName().equals(targetName)){
            QuantifiedASTD q = (QuantifiedASTD) astd;
            list.add(q.getQvariable());
            list.addAll(getAllParameters(q.getBody(), targetName));
        }
        else if(Conventions.isUnaryASTD(astd) && !Conventions.isQuantifiedASTD(astd)){
            UnaryASTD u = (UnaryASTD) astd;
            list.addAll(getAllParameters(u.getBody(), targetName));

        }
        else if(Conventions.isBinaryASTD(astd)){
            BinaryASTD b = (BinaryASTD) astd;
            List<Variable> listLeft = new ArrayList<>();
            List<Variable> listRight = new ArrayList<>();
            listLeft.addAll(getAllParameters(b.getLeft(), targetName));
            if(listLeft != null && !listLeft.isEmpty()){
                //if(listLeft.get(listLeft.size()-1) == null){
                    list.addAll(listLeft);
                //}
            }
            listRight.addAll(getAllParameters(b.getRight(), targetName));
            if(listRight != null && !listRight.isEmpty()){
                //if(listRight.get(listRight.size()-1) == null){
                    list.addAll(listRight);
                //}
            }
        }
        else if(Conventions.isQuantifiedASTD(astd)){
            QuantifiedASTD q = (QuantifiedASTD) astd;
            list.add(q.getQvariable());
            list.addAll(getAllParameters(q.getBody(), targetName));
        }

        else if(Conventions.isAutomaton(astd)){
            Automaton aut = (Automaton) astd;
            Map<String, ASTD> mapState = aut.getStatesToASTDs();
            for(String key : mapState.keySet()){
                list.addAll(getAllParameters(mapState.get(key), targetName));
            }
        }

        return list;
    }

    private Statement updateStatement(QuantifiedASTD q, ArrayList<ASTD> callList) {
        ASTD b = q.getBody();
        return new SeqStatement(Arrays.asList(
                b.init(callList, Conventions.CST),
                new IFFIStatement(Arrays.asList(
                        new Entry<>(Utils.qvarCond.get(Constants.DUMMY_PREFIX.get(getQvariable())),
                                new AssignStatement(new Term(Conventions.EXEC), new Term((Conventions.EXEC +"+"+ Conventions.TRUE))))
                    ))
            ));
    }

    private Statement updateStatementEXISTS(QuantifiedASTD q, ArrayList<ASTD> callList) {
        ASTD b = q.getBody();
        return new SeqStatement(Arrays.asList(
                b.init(callList, Conventions.CST),
                new IFFIStatement(Arrays.asList(
                        new Entry<>(Utils.qvarCond.get(Constants.DUMMY_PREFIX.get(getQvariable())),
                                new CallStatement(Conventions.RETURN_CALL, Arrays.asList((Conventions.TRUE))))
                ))
        ));
    }

    private Statement updateStatementWithParam(QuantifiedASTD q, String varName, ArrayList<ASTD> callList) {
        ASTD b = q.getBody();
        return new SeqStatement(Arrays.asList(
                b.init(callList, Conventions.CST),
                new IFFIStatement(Arrays.asList(
                        new Entry<>(new AndCondition(Arrays.asList(Utils.qvarCond.get(Constants.DUMMY_PREFIX.get(getQvariable())), new CallCondition(Conventions.EQUALS, Arrays.asList(varName, Conventions.DUMMY_PARAMS+varName)))),
                                new AssignStatement(new Term(Conventions.EXEC), new Term((Conventions.EXEC +"+"+ Conventions.TRUE))))
                ))
        ));
    }

    private Statement updateStatementWithParamFORALL(QuantifiedASTD q, String varName, ArrayList<ASTD> callList) {
        ASTD b = q.getBody();
        return new SeqStatement(Arrays.asList(
                b.init(callList, Conventions.CST),
                new IFFIStatement(Arrays.asList(
                        new Entry<>(new AndCondition(Arrays.asList(Utils.qvarCond.get(Constants.DUMMY_PREFIX.get(getQvariable())))),
                                new AssignStatement(new Term(Conventions.EXEC), new Term((Conventions.EXEC +"+"+ Conventions.TRUE))))
                ))
        ));
    }

    private Statement updateStatementWithParamEXISTS(QuantifiedASTD q, String varName, ArrayList<ASTD> callList) {
        ASTD b = q.getBody();
        return new SeqStatement(Arrays.asList(
                b.init(callList, Conventions.CST),
                new IFFIStatement(Arrays.asList(
                        new Entry<>(Utils.qvarCond.get(Constants.DUMMY_PREFIX.get(getQvariable())),
                                new CallStatement(Conventions.RETURN_CALL, Collections.singletonList(Conventions.TRUE)))
                ))
        ));
    }

    @Override
    public List<String> updateSubIndexes(ToJson obj) {
        return Arrays.asList(obj.getNodeIndex());
    }

    private Statement caseWithOnlyQVarEXISTS(String varName, String mapState, Variable p,
                                             String domType, Condition c, QuantifiedASTD q,
                                             String domain, ArrayList<ASTD> ASTDTree) {
        SeqStatement seqSt = new SeqStatement(Arrays.asList());
        seqSt.setStatement(Arrays.asList(
                new AssignStatement(new Term(varName), new Term(p.getName())),
                (!Conventions.isQChoice(this))
                        ? new IFFIStatement(Arrays.asList(
                        new Entry<>(
                                new CallCondition(Conventions.IN_MAP,
                                        Arrays.asList(varName, mapState)),
                                new IFFIStatement(Arrays.asList(
                                        new Entry<>(
                                                (!domType.equals(Constants.UNBOUNDEDDOMAIN))
                                                        ? new AndCondition(Arrays.asList(
                                                        new CallCondition(Conventions.IN,
                                                                Arrays.asList(varName, domain)),
                                                        c))
                                                        : c,
                                                new CallStatement(Conventions.RETURN_CALL, Collections.singletonList(Conventions.TRUE))
                                        )
                                ))),
                        new Entry<>(null, updateStatementWithParamEXISTS(q, varName, ASTDTree))
                ))
                        : new IFFIStatement(Arrays.asList(
                        new Entry<>(
                                (!domType.equals(Constants.UNBOUNDEDDOMAIN))
                                        ? new AndCondition(Arrays.asList(
                                        new CallCondition(Conventions.IN,
                                                Arrays.asList(varName, domain))
                                        ))
                                        : c,
                                new CallStatement(Conventions.RETURN_CALL, Collections.singletonList(Conventions.TRUE))
                        )
                ))
        ));
        return seqSt;
    }


    private Statement caseFORALLParams(String varName, String mapState,
                                              String domType, Condition c, QuantifiedASTD q,
                                              String domain, ArrayList<ASTD> ASTDTree) {
        return new SeqStatement(Arrays.asList(
                (!Conventions.isQChoice(this))
                        ? new IFFIStatement(Arrays.asList(
                        new Entry<>(
                                new CallCondition(Conventions.IN_MAP,
                                        Arrays.asList(varName, mapState)),
                                new IFFIStatement(Arrays.asList(
                                        new Entry<>(
                                                (!domType.equals(Constants.UNBOUNDEDDOMAIN))
                                                        ? new AndCondition(Arrays.asList(
                                                        new CallCondition(Conventions.IN,
                                                                Arrays.asList(varName, domain)),
                                                        c))
                                                        : c,
                                                new AssignStatement(new Term(Conventions.EXEC), new Term((Conventions.EXEC +"+"+ Conventions.TRUE)))
                                        )
                                ))),
                        new Entry<>(null, updateStatementWithParamFORALL(q, varName, ASTDTree))
                ))
                        : new IFFIStatement(Arrays.asList(
                        new Entry<>(
                                (!domType.equals(Constants.UNBOUNDEDDOMAIN))
                                        ? new AndCondition(Arrays.asList(
                                        new CallCondition(Conventions.IN,
                                                Arrays.asList(varName, domain)),
                                        c))
                                        : c,
                                new AssignStatement(new Term(Conventions.EXEC), new Term((Conventions.EXEC +"+"+ Conventions.TRUE)))
                        )
                ))
        ));
    }

    private Statement caseWithNestedParams(String varName, String mapState, Variable p,
                                           String domType, Condition c, QuantifiedASTD q,
                                           String domain, ArrayList<ASTD> ASTDTree) {

        Term randomName = new Term(Utils.generateNameIfNotExists(Conventions.DUMMY_PARAMS+varName+Conventions.DUMMY_PARAMS));
        return new SeqStatement(Arrays.asList(
                            (!domType.equals(Constants.UNBOUNDEDDOMAIN))
                            ? new AssignStatement(new Term(varName), randomName)
                            : new AssignStatement(new Term(varName), new Term(p.getName())),
                            (!Conventions.isQChoice(this))
                            ? new IFFIStatement(Arrays.asList(
                                    new Entry<>(new CallCondition(Conventions.IN_MAP,
                                            Arrays.asList(varName, mapState)),
                                            new IFFIStatement(Arrays.asList(
                                                    new Entry<>(
                                                            (!domType.equals(Constants.UNBOUNDEDDOMAIN))
                                                                    ? new AndCondition(Arrays.asList(
                                                                            new CallCondition(Conventions.IN,
                                                                                    Arrays.asList(varName, domain)),
                                                                    new CallCondition(Conventions.EQUALS, Arrays.asList(varName, Conventions.DUMMY_PARAMS+varName)),
                                                                    c))
                                                                    : c,
                                                            new CallStatement(Conventions.RETURN_CALL,
                                                                    Collections.singletonList(Conventions.TRUE))
                                                    )
                                            ))),
                                    new Entry<>(null, updateStatementWithParam(q, varName, ASTDTree))
                              ))
                            : new IFFIStatement(Arrays.asList(
                                    new Entry<>(
                                        (!domType.equals(Constants.UNBOUNDEDDOMAIN))
                                                ? new AndCondition(Arrays.asList(
                                                        new CallCondition(Conventions.IN,
                                                                Arrays.asList(varName, domain)),
                                                new CallCondition(Conventions.EQUALS, Arrays.asList(varName, Conventions.DUMMY_PARAMS+varName)),
                                                c))
                                                : c,
                                        new CallStatement(Conventions.RETURN_CALL,
                                                Collections.singletonList(Conventions.TRUE))
                                    )
                            ))
                    ));
    }

    private Statement caseWithNestedParamsEXISTS(String varName, String mapState, Variable p,
                                           String domType, Condition c, QuantifiedASTD q,
                                           String domain, ArrayList<ASTD> ASTDTree) {
        Term randomName = new Term(Utils.generateNameIfNotExists(Conventions.DUMMY_PARAMS+varName+Conventions.DUMMY_PARAMS));
        return new SeqStatement(Arrays.asList(
                (!domType.equals(Constants.UNBOUNDEDDOMAIN))
                        ? new AssignStatement(new Term(varName), randomName)
                        : new AssignStatement(new Term(varName), new Term(p.getName())),
                (!Conventions.isQChoice(this))
                        ? new IFFIStatement(Arrays.asList(
                        new Entry<>(new CallCondition(Conventions.IN_MAP,
                                Arrays.asList(varName, mapState)),
                                new IFFIStatement(Arrays.asList(
                                        new Entry<>(
                                                (!domType.equals(Constants.UNBOUNDEDDOMAIN))
                                                        ? new AndCondition(Arrays.asList(
                                                        new CallCondition(Conventions.IN,
                                                                Arrays.asList(varName, domain)),
                                                        new CallCondition(Conventions.EQUALS, Arrays.asList(varName, Conventions.DUMMY_PARAMS+varName)),
                                                        c))
                                                        : c,
                                                new CallStatement(Conventions.RETURN_CALL, Arrays.asList((Conventions.EXEC +"+"+ Conventions.TRUE)))
                                        )
                                ))),
                        new Entry<>(null, updateStatementWithParamEXISTS(q, varName, ASTDTree))
                ))
                        : new IFFIStatement(Arrays.asList(
                        new Entry<>(
                                (!domType.equals(Constants.UNBOUNDEDDOMAIN))
                                        ? new AndCondition(Arrays.asList(
                                        new CallCondition(Conventions.IN,
                                                Arrays.asList(varName, domain)),
                                        new CallCondition(Conventions.EQUALS, Arrays.asList(varName, Conventions.DUMMY_PARAMS+varName)),
                                        c))
                                        : c,
                                new CallStatement(Conventions.RETURN_CALL, Arrays.asList((Conventions.EXEC +"+"+ Conventions.TRUE)))
                        )
                ))
        ));
    }

    private Statement caseWithoutParams(String varName, String mapState, Variable p,
                                        String domType, Condition c, QuantifiedASTD q,
                                        String domain, ArrayList<ASTD> ASTDTree) {
        return  new SeqStatement(Arrays.asList(
                    (!Conventions.isQChoice(this))
                    ? new IFFIStatement(Arrays.asList(
                            new Entry<>(new CallCondition(Conventions.IN_MAP,
                                    Arrays.asList(varName, mapState)),
                                    new IFFIStatement(Arrays.asList(
                                            new Entry<>(
                                                    new AndCondition(Arrays.asList(
                                                            new CallCondition(Conventions.IN,
                                                                    Arrays.asList(varName, domain)),
                                                            c)),
                                                    new AssignStatement(new Term(Conventions.EXEC), new Term((Conventions.EXEC +"+"+ Conventions.TRUE)))
                                            )
                                    ))),
                            new Entry<>(null, updateStatement(q, ASTDTree))
                    ))
                    : new IFFIStatement(Arrays.asList(
                            new Entry<>(
                                    new AndCondition(Arrays.asList(
                                            new CallCondition(Conventions.IN,
                                                    Arrays.asList(varName, domain)),
                                            c)),
                                    new AssignStatement(new Term(Conventions.EXEC), new Term((Conventions.EXEC +" + "+ Conventions.TRUE)))
                            )
                    ))
        ));
    }

    private Statement caseWithoutParamsEXISTS(String varName, String mapState, Variable p,
                                              String domType, Condition c, QuantifiedASTD q,
                                              String domain, ArrayList<ASTD> ASTDTree) {
        return  new SeqStatement(Arrays.asList(
                (!Conventions.isQChoice(this))
                        ? new IFFIStatement(Arrays.asList(
                        new Entry<>(new CallCondition(Conventions.IN_MAP,
                                Arrays.asList(varName, mapState)),
                                new IFFIStatement(Arrays.asList(
                                        new Entry<>(
                                                new AndCondition(Arrays.asList(
//                                                        new CallCondition(Conventions.IN,
//                                                                Arrays.asList(varName, domain)),
                                                        c)),
                                                new CallStatement(Conventions.RETURN_CALL, Arrays.asList((Conventions.TRUE)))
                                        )
                                ))),
                        new Entry<>(null, updateStatementEXISTS(q, ASTDTree))
                ))
                        : new IFFIStatement(Arrays.asList(
                        new Entry<>(
                                new AndCondition(Arrays.asList(
//                                        new CallCondition(Conventions.IN,
//                                                Arrays.asList(varName, domain)),
                                        c)),
                                new CallStatement(Conventions.RETURN_CALL, Arrays.asList((Conventions.TRUE)))
                        )
                ))
        ));
    }

    private Statement caseWithoutParamsEXISTSforStep(String varName, String mapState, Variable p,
                                                     String domType, Condition c, QuantifiedASTD q,
                                                     String domain, ArrayList<ASTD> ASTDTree) {
        return  new SeqStatement(Arrays.asList(
                (!Conventions.isQChoice(this))
                        ? new IFFIStatement(Arrays.asList(
                        new Entry<>(new CallCondition(Conventions.IN_MAP,
                                Arrays.asList(varName, mapState)),
                                new IFFIStatement(Arrays.asList(
                                        new Entry<>(
                                                new AndCondition(Arrays.asList(
//                                                        new CallCondition(Conventions.IN,
//                                                                Arrays.asList(varName, domain)),
                                                        c)),
                                                new CallStatement(Conventions.RETURN_CALL, Arrays.asList((Conventions.TRUE)))
                                        )
                                ))),
                        new Entry<>(null, updateStatementEXISTS(q, ASTDTree))
                ))
                        : new IFFIStatement(Arrays.asList(
                        new Entry<>(
                                new AndCondition(Arrays.asList(
//                                        new CallCondition(Conventions.IN,
//                                                Arrays.asList(varName, domain)),
                                        c)),
                                new CallStatement(Conventions.RETURN_CALL, Arrays.asList((Conventions.TRUE)))
                        )
                ))
        ));
    }

    private Statement caseEXISTSforStepUNBOUNDED(String varName, String mapState, Variable p,
                                              String domType, Condition c, QuantifiedASTD q,
                                              String domain) {
        return  new SeqStatement(Arrays.asList(
                (!Conventions.isQChoice(this))
                        ? new IFFIStatement(Arrays.asList(
                        new Entry<>(new CallCondition(Conventions.IN_MAP,
                                Arrays.asList(varName, mapState)),
                                new IFFIStatement(Arrays.asList(
                                        new Entry<>(
                                                new AndCondition(Arrays.asList(
//                                                        new CallCondition(Conventions.IN,
//                                                                Arrays.asList(varName, domain)),
                                                        c)),
                                                new CallStatement(Conventions.RETURN_CALL, Arrays.asList((Conventions.TRUE)))
                                        )
                                )))
                        //, new Entry<>(null, updateStatementEXISTS(q))
                        //FOR STEP DOES NOT INITIALIZE THE INSTANCE
                ))
                        : new IFFIStatement(Arrays.asList(
                        new Entry<>(
                                new AndCondition(Arrays.asList(
//                                        new CallCondition(Conventions.IN,
//                                                Arrays.asList(varName, domain)),
                                        c)),
                                new CallStatement(Conventions.RETURN_CALL, Arrays.asList((Conventions.TRUE)))
                        )
                ))
        ));
    }

    @Override
    public List<Function> trans_quantified_condition(Event e, Bool timed, List<Variable> varList, ArrayList<ASTD> CallList) {
        ArrayList<ASTD> ASTDTree = new ArrayList<>();
        ASTDTree.addAll(CallList);
        ASTDTree.add(this);
        List<Function>  funcList = new ArrayList<>();

        QuantifiedASTD q = this;

        funcList.addAll(q.getBody().trans_quantified_condition(e, timed, varList, ASTDTree));

        Function func;
        String existsName = Conventions.EXISTS + "_" + q.getName()
                + e.getName() + "_" + q.getQvariable().getName(),
                forallName = Conventions.FOR_ALL + "_" + q.getName()
                        + e.getName() + "_" + q.getQvariable().getName();
        String funcId = null;
        if (Utils.qvarDic.containsKey(forallName)) {
            funcId = forallName;
        }
        if (Utils.qvarDic.containsKey(existsName)) {
            funcId = existsName;
        }
        if(funcId != null) {
            Event ev = Utils.qvarDic.get(funcId);

            String domain   = Conventions.getVarSet(q.getName());
            String qvarType = q.getQvariable().getType();
            String evtName  = ev.getName();
            String domType  = (q.getDomain() != null) ? q.getDomain().getType() : "";

            if (evtName.equals(e.getName())) {
                SeqStatement outSeq;
                List<Variable> parameters = new ArrayList<>();
                List<Variable> _parameters = new ArrayList<>();
                List<Variable> otherparameters = new ArrayList<>();
                List<Statement> stmt = new ArrayList<>();

                if(funcId.equals(forallName)){
                    if (varList != null && !varList.isEmpty()) {
                        AtomicReference<String> cached_funcId = new AtomicReference<>(funcId);

                        //do the forAll for the quantified variable
                        String k = q.getQvariable().getName();
                        Condition val = Utils.qvarCond.get(Constants.DUMMY_PREFIX.get(getQvariable()));

                        SeqStatement seqStmt;
                        ASTD quantifiedASTD = Constants.DUMMY_PREFIX2.get(q.getQvariable());

                        String prfx = ASTDTree.get(0).prefixTree(ASTDTree)+".";
                        //String prfx = getParent().prefix(quantifiedASTD.getName()) + ".";

                        String mapState = prfx
                                + Conventions.FUNC_STATE;
                        Condition valCopy = (Condition) Utils.copyObject(val);
                        seqStmt = (SeqStatement) caseFORALLParams(k, mapState, domType, valCopy, q, domain, ASTDTree);
                        Term randomName = new Term(Utils.generateNameIfNotExists(Conventions.DUMMY_PARAMS+k+Conventions.DUMMY_PARAMS));
                        seqStmt = new SeqStatement(Arrays.asList(new AssignStatement(new Term(k), randomName), seqStmt));
                        ForStatement forStmt = new ForStatement(new CallCondition(Conventions.FOR,
                                Arrays.asList(qvarType, randomName.getId(), domain)), seqStmt);

                        stmt.add(new SeqStatement(Arrays.asList(new DeclStatement(
                                        new Variable(Conventions.EXEC, Conventions.BOOL_TYPE, Conventions.FALSE, null)), forStmt,
                                new IFFIStatement(Arrays.asList(new Entry<>(new CallCondition(Conventions.EQUALS,
                                        Arrays.asList(Conventions.EXEC, domain+Constants.SIZE)),
                                        new CallStatement(Conventions.RETURN_CALL, Arrays.asList(Conventions.TRUE))))))));

                        List<Variable> listQParams = getAllEventQVariables(e);
                        for(int i = 0; i < listQParams.size(); i++){
                            for(int j = 0; j < Utils.qvarDic.get(funcId).getParams().size(); j++){
                                if((Conventions.DUMMY_PARAMS+listQParams.get(i).getName()).equals(Utils.qvarDic.get(funcId).getParams().get(j).getName())){
                                    _parameters.add(new Variable(Conventions.DUMMY_PARAMS+listQParams.get(i).getName(), Utils.qvarDic.get(funcId).getParams().get(j).getType(),Utils.qvarDic.get(funcId).getParams().get(j).getInit(), getName()));
                                    break;
                                }
                            }
                        }
                        parameters.addAll(listQParams);

                        parameters.addAll(_parameters);

                        for(int i = 0; i < Utils.qvarDic.get(funcId).getParams().size(); i++){
                            boolean found = false;
                            for(int j = 0; j < parameters.size(); j++){
                                if((parameters.get(j).getName()).equals(Utils.qvarDic.get(funcId).getParams().get(i))){
                                    found = true;
                                }
                            }
                            if(!found){
                                if(!parameters.contains(Utils.qvarDic.get(funcId).getParams().get(i))){
                                    otherparameters.add(Utils.qvarDic.get(funcId).getParams().get(i));
                                }
                            }
                        }

                        parameters.addAll(otherparameters);

                        if(stmt.isEmpty()) {
                            outSeq = new SeqStatement(Arrays.asList(new CallStatement(Conventions.RETURN_CALL,
                                    Collections.singletonList(Conventions.TRUE))));
                        }
                        else {
                            outSeq = new SeqStatement(Arrays.asList(new SeqStatement(stmt),
                                    new CallStatement(Conventions.RETURN_CALL,
                                            Collections.singletonList(Conventions.FALSE))));
                        }

                        if (!Utils.qvarCond.isEmpty()) {
                            func = new Function(funcId, parameters, Conventions.BOOL_TYPE, outSeq);
                            funcList.add(func);
                        }
                    } else {
                        if (!domType.equals(Constants.UNBOUNDEDDOMAIN)) {

                            //do the forAll for the quantified variable
                            String k = q.getQvariable().getName();
                            Condition val = Utils.qvarCond.get(Constants.DUMMY_PREFIX.get(getQvariable()));

                            SeqStatement seqStmt;
                            ASTD quantifiedASTD = Constants.DUMMY_PREFIX2.get(q.getQvariable());

                            //String prfx = getParent().prefix(quantifiedASTD.getName()) + ".";
                            String prfx = ASTDTree.get(0).prefixTree(ASTDTree, quantifiedASTD.getName()) + ".";

                            String mapState = prfx
                                    + Conventions.FUNC_STATE;
                            Condition valCopy = (Condition) Utils.copyObject(val);
                            seqStmt = (SeqStatement) caseWithoutParams(k, mapState, null, null,
                                    valCopy, q, domain, ASTDTree);
                            Term randomName = new Term(Utils.generateNameIfNotExists(Conventions.DUMMY_PARAMS+k+Conventions.DUMMY_PARAMS));
                            seqStmt = new SeqStatement(Arrays.asList(new AssignStatement(new Term(k), randomName), seqStmt));
                            ForStatement forStmt = new ForStatement(new CallCondition(Conventions.FOR,
                                    Arrays.asList(qvarType, randomName.getId(), domain)), seqStmt);

                            stmt.add(new SeqStatement(Arrays.asList(new DeclStatement(
                                            new Variable(Conventions.EXEC, Conventions.BOOL_TYPE, Conventions.FALSE, null)), forStmt,
                                    new IFFIStatement(Arrays.asList(new Entry<>(new CallCondition(Conventions.EQUALS,
                                            Arrays.asList(Conventions.EXEC, domain+Constants.SIZE)),
                                            new CallStatement(Conventions.RETURN_CALL, Arrays.asList(Conventions.TRUE))))))));

                            List<Variable> listQParams = getAllEventQVariables(e);
                            for(int i = 0; i < listQParams.size(); i++){
                                for(int j = 0; j < Utils.qvarDic.get(funcId).getParams().size(); j++){
                                    if((Conventions.DUMMY_PARAMS+listQParams.get(i).getName()).equals(Utils.qvarDic.get(funcId).getParams().get(j).getName())){
                                        _parameters.add(new Variable(Conventions.DUMMY_PARAMS+listQParams.get(i).getName(), Utils.qvarDic.get(funcId).getParams().get(j).getType(),Utils.qvarDic.get(funcId).getParams().get(j).getInit(), getName()));
                                        break;
                                    }
                                }
                            }
                            parameters.addAll(listQParams);

                            for(int i = 0; i < Utils.qvarDic.get(funcId).getParams().size(); i++){
                                boolean found = false;
                                for(int j = 0; j < parameters.size(); j++){
                                    if((parameters.get(j).getName()).equals(Utils.qvarDic.get(funcId).getParams().get(i))){
                                        found = true;
                                    }
                                }
                                if(!found){
                                    if(!parameters.contains(Utils.qvarDic.get(funcId).getParams().get(i))){
                                        otherparameters.add(Utils.qvarDic.get(funcId).getParams().get(i));
                                    }
                                }
                            }

                            parameters.addAll(otherparameters);

                            if (stmt.isEmpty()) {
                                outSeq = new SeqStatement(Arrays.asList(new CallStatement(Conventions.RETURN_CALL,
                                        Collections.singletonList(Conventions.TRUE))));
                            } else {
                                outSeq = new SeqStatement(Arrays.asList(new SeqStatement(stmt),
                                        new CallStatement(Conventions.RETURN_CALL,
                                                Collections.singletonList(Conventions.FALSE))));
                            }

                            func = new Function(funcId, parameters, Conventions.BOOL_TYPE, outSeq);
                            funcList.add(func);
                        }
                        else {
                            System.out.println("[Error] Can not execute event "
                                    + evtName
                                    +" on unbounded "
                                    + "domains without parameter(s) !!");
                            System.exit(0);
                        }
                    }
                }
                else if(funcId.equals(existsName)){ //IT IS A EXISTS
                    if (varList != null && !varList.isEmpty()) {
                        AtomicReference<String> cached_funcId = new AtomicReference<>(funcId);
                        //only qvar parameters
                        if(q.getQvariable()!= null){
                            int index = -1;
                            for(int i = 0; i < varList.size(); i++){
                                if(varList.get(i).getName().replace(Conventions.DUMMY_PARAMS, "").equals(q.getQvariable().getName())){
                                    index = i;
                                    break;
                                }
                            }
                            if(index != -1){
                                Variable p = varList.get(index);
                                String p_init = p.getInit().toString();
                                String p_name = p.getName().replace(Conventions.DUMMY_PARAMS, "");
                                if (p_init.compareTo(Constants.ANY_MODE) == 0) {
                                    Condition cSub     = Utils.qvarCond.get(Constants.DUMMY_PREFIX.get(getQvariable())),
                                            cSubCopy = (Condition) Utils.copyObject(cSub);
                                    if (cSub != null
                                            && !cSub.isEqualTo(cached_funcId.get())
                                            && p_name.compareTo(q.getQvariable().getName()) == 0) {
                                        ASTD quantifiedASTD = Constants.DUMMY_PREFIX2.get(q.getQvariable());
                                        //String prfx = getParent().prefix(quantifiedASTD.getName()) + ".";
                                        String prfx = ASTDTree.get(0).prefixTree(ASTDTree, quantifiedASTD.getName()) + ".";
                                        String mapState = prfx
                                                + Conventions.FUNC_STATE;
                                        IFFIStatement seqCall = null;
                                        if(!this.getClass().getSimpleName().equals("QChoice")) {
                                            if(!domType.equals(Constants.UNBOUNDEDDOMAIN)){
                                                seqCall = new IFFIStatement(Arrays.asList(new Entry<>(new AndCondition(Arrays.asList(
                                                        new CallCondition(Conventions.NOT_IN,
                                                                Arrays.asList(p.getName(), domain)))), new CallStatement(Conventions.RETURN_CALL, Arrays.asList(Conventions.FALSE)))));
                                            }
                                            SeqStatement seqStmt = (SeqStatement) caseWithOnlyQVarEXISTS(p_name, mapState, p, domType, cSubCopy, q, domain, ASTDTree);
                                            if(seqCall != null){
                                                stmt.add(seqCall);
                                            }
                                            stmt.add(seqStmt);
                                        }
                                        else{
                                            if(!domType.equals(Constants.UNBOUNDEDDOMAIN)){
                                                seqCall = new IFFIStatement(Arrays.asList(new Entry<>(new AndCondition(Arrays.asList(
                                                        new CallCondition(Conventions.NOT_IN,
                                                                Arrays.asList(p.getName(), domain)))), new CallStatement(Conventions.RETURN_CALL, Arrays.asList(Conventions.FALSE)))));
                                            }
                                            else{
                                                seqCall =
                                                        new IFFIStatement(Arrays.asList(
                                                                new Entry<>(
                                                                        new AndCondition(Arrays.asList(cSubCopy.substitute(q.initforsub(CallList, e, timed, Conventions.CST, false), CallList))
                                                                ),
                                                                new CallStatement(Conventions.RETURN_CALL, Collections.singletonList(Conventions.TRUE))
                                                        )));
                                            }
                                            stmt.add(seqCall);
                                        }
                                    }
                                    List<Variable> listQParams = getAllEventQVariables(e);
                                    for(int i = 0; i < listQParams.size(); i++){
                                        for(int j = 0; j < Utils.qvarDic.get(funcId).getParams().size(); j++){
                                            if((Conventions.DUMMY_PARAMS+listQParams.get(i).getName()).equals(Utils.qvarDic.get(funcId).getParams().get(j).getName())){
                                                _parameters.add(new Variable(Conventions.DUMMY_PARAMS+listQParams.get(i).getName(), Utils.qvarDic.get(funcId).getParams().get(j).getType(),Utils.qvarDic.get(funcId).getParams().get(j).getInit(), getName()));
                                                break;
                                            }
                                        }
                                    }
                                    parameters.addAll(listQParams);
                                }
                            }else{
                                //varList (list of parameters of a event) does not contain the quantified variable
                                //that means the function shall behaviour as no parameter was given, but need to check the parent (for quantified variables)
                                //the quantified parameters from the parents shall be added to the call of the exists.
                                //if it is qChoice and no parameter was given, then we abort
                                Variable qVar = q.getQvariable();
                                SeqStatement seqStmt;
                                ASTD quantifiedASTD = Constants.DUMMY_PREFIX2.get(q.getQvariable());
                                //String prfx = getParent().prefix(quantifiedASTD.getName()) + ".";
                                String prfx = ASTDTree.get(0).prefixTree(ASTDTree, quantifiedASTD.getName()) + ".";
                                String mapState = prfx
                                        + Conventions.FUNC_STATE;
                                Condition valCopy = Utils.qvarCond.get(Constants.DUMMY_PREFIX.get(getQvariable()));
                                if(!this.getClass().getSimpleName().equals("QChoice")){
                                    seqStmt = (SeqStatement) caseWithoutParamsEXISTS(qVar.getName(), mapState, null, null,
                                            valCopy, q, domain, ASTDTree);
                                    Term randomName = new Term(Utils.generateNameIfNotExists(Conventions.DUMMY_PARAMS+qVar.getName()+Conventions.DUMMY_PARAMS));
                                    seqStmt = new SeqStatement(Arrays.asList(new AssignStatement(new Term(qVar.getName()), randomName), seqStmt));
                                    ForStatement forStmt = new ForStatement(new CallCondition(Conventions.FOR,
                                            Arrays.asList(qvarType, randomName.getId(), domain)),seqStmt);
                                    stmt.add(new SeqStatement(Arrays.asList(forStmt)));
                                }
                                else{
                                    System.out.println("ATTENTION!! QCHOICE ON UNBOUNDED DOMAIN, THE VALUE OF " + q.getQvariable().getName()+ " SHOULD BE INITIALISED BEFORE USING IT ON UNBOUNDED DOMAIN");
                                    seqStmt = (SeqStatement) caseWithoutParamsEXISTS(qVar.getName(), mapState, null, null,
                                            valCopy, q, domain, ASTDTree);
                                    stmt.add(seqStmt);
                                }
                                List<Variable> listQParams = getAllEventQVariables(e);
                                for(int i = 0; i < listQParams.size(); i++){
                                    for(int j = 0; j < Utils.qvarDic.get(funcId).getParams().size(); j++){
                                        if((Conventions.DUMMY_PARAMS+listQParams.get(i).getName()).equals(Utils.qvarDic.get(funcId).getParams().get(j).getName())){
                                            _parameters.add(new Variable(Conventions.DUMMY_PARAMS+listQParams.get(i).getName(), Utils.qvarDic.get(funcId).getParams().get(j).getType(),Utils.qvarDic.get(funcId).getParams().get(j).getInit(), getName()));
                                            break;
                                        }
                                    }
                                }
                                parameters.addAll(listQParams);
                            }
                        }

                        if (!_parameters.isEmpty()) {
                            parameters.addAll(_parameters);
                        }

                        for(int i = 0; i < Utils.qvarDic.get(funcId).getParams().size(); i++){
                            boolean found = false;
                            for(int j = 0; j < parameters.size(); j++){
                                if((parameters.get(j).getName()).equals(Utils.qvarDic.get(funcId).getParams().get(i))){
                                    found = true;
                                }
                            }
                            if(!found){
                                if(!parameters.contains(Utils.qvarDic.get(funcId).getParams().get(i))){
                                    otherparameters.add(Utils.qvarDic.get(funcId).getParams().get(i));
                                }
                            }
                        }

                        parameters.addAll(otherparameters);

                        if(stmt.isEmpty()) {
                            outSeq = new SeqStatement(Arrays.asList(new CallStatement(Conventions.RETURN_CALL,
                                    Collections.singletonList(Conventions.FALSE))));
                        }
                        else {
                            outSeq = new SeqStatement(Arrays.asList(new SeqStatement(stmt),
                                    new CallStatement(Conventions.RETURN_CALL, Collections.singletonList(Conventions.FALSE))));
                        }

                        if (!Utils.qvarCond.isEmpty()) {
                            func = new Function(funcId, parameters, Conventions.BOOL_TYPE, outSeq);
                            funcList.add(func);
                        }
                    } else {
                        if (!domType.equals(Constants.UNBOUNDEDDOMAIN)) {
                            //no parameters
                            Variable qVar = q.getQvariable();
                            SeqStatement seqStmt;
                            ASTD quantifiedASTD = Constants.DUMMY_PREFIX2.get(q.getQvariable());
                            //String prfx = getParent().prefix(quantifiedASTD.getName()) + ".";
                            String prfx = ASTDTree.get(0).prefixTree(ASTDTree, quantifiedASTD.getName()) + ".";
                            String mapState = prfx + Conventions.FUNC_STATE;
                            Condition valCopy = Utils.qvarCond.get(Constants.DUMMY_PREFIX.get(getQvariable()));
                            seqStmt = (SeqStatement) caseWithoutParamsEXISTS(qVar.getName(), mapState, null, null,
                                    valCopy, q, domain, ASTDTree);
                            Term randomName = new Term(Utils.generateNameIfNotExists(Conventions.DUMMY_PARAMS+qVar.getName()+Conventions.DUMMY_PARAMS));
                            seqStmt = new SeqStatement(Arrays.asList(new AssignStatement(new Term(qVar.getName()), randomName), seqStmt));
                            if(!this.getClass().getSimpleName().equals("QChoice")){
                                ForStatement forStmt = new ForStatement(new CallCondition(Conventions.FOR,
                                        Arrays.asList(qvarType, randomName.getId(), domain)),seqStmt);
                                stmt.add(new SeqStatement(Arrays.asList(forStmt,
                                        new CallStatement(Conventions.RETURN_CALL, Arrays.asList(Conventions.FALSE)))));

                            }
                            else{
                                stmt.add(new SeqStatement(Arrays.asList(seqStmt,
                                        new CallStatement(Conventions.RETURN_CALL, Arrays.asList(Conventions.FALSE)))));

                            }
                            List<Variable> listQParams = getAllEventQVariables(e);
                            for(int i = 0; i < listQParams.size(); i++){
                                for(int j = 0; j < Utils.qvarDic.get(funcId).getParams().size(); j++){
                                    if((Conventions.DUMMY_PARAMS+listQParams.get(i).getName()).equals(Utils.qvarDic.get(funcId).getParams().get(j).getName())){
                                        _parameters.add(new Variable(Conventions.DUMMY_PARAMS+listQParams.get(i).getName(), Utils.qvarDic.get(funcId).getParams().get(j).getType(),Utils.qvarDic.get(funcId).getParams().get(j).getInit(), getName()));
                                        break;
                                    }
                                }
                            }
                            parameters.addAll(listQParams);

                            for(int i = 0; i < Utils.qvarDic.get(funcId).getParams().size(); i++){
                                boolean found = false;
                                for(int j = 0; j < parameters.size(); j++){
                                    if((parameters.get(j).getName()).equals(Utils.qvarDic.get(funcId).getParams().get(i))){
                                        found = true;
                                    }
                                }
                                if(!found){
                                    if(!parameters.contains(Utils.qvarDic.get(funcId).getParams().get(i))){
                                        otherparameters.add(Utils.qvarDic.get(funcId).getParams().get(i));
                                    }
                                }
                            }

                            parameters.addAll(otherparameters);

                            if (stmt.isEmpty()) {
                                outSeq = new SeqStatement(Arrays.asList(new CallStatement(Conventions.RETURN_CALL,
                                        Collections.singletonList(Conventions.TRUE))));
                            } else {
                                outSeq = new SeqStatement(Arrays.asList(new SeqStatement(stmt)));
                            }

                            func = new Function(funcId, parameters, Conventions.BOOL_TYPE, outSeq);
                            funcList.add(func);
                        }
                        else {
                            //UNBOUNDED DOMAIN!
                            //no parameters -> UNBOUNDED -> NOT A FOR
                            Variable qVar = q.getQvariable();
                            SeqStatement seqStmt;
                            ASTD quantifiedASTD = Constants.DUMMY_PREFIX2.get(q.getQvariable());
                            //String prfx = getParent().prefix(quantifiedASTD.getName()) + ".";
                            String prfx = ASTDTree.get(0).prefixTree(ASTDTree, quantifiedASTD.getName()) + ".";
                            String mapState = prfx + Conventions.FUNC_STATE;
                            Condition valCopy = Utils.qvarCond.get(Constants.DUMMY_PREFIX.get(getQvariable()));
                            seqStmt = (SeqStatement) caseWithoutParamsEXISTS(qVar.getName(), mapState, null, null,
                                    valCopy, q, domain, ASTDTree);
                            Term randomName = new Term(Utils.generateNameIfNotExists(Conventions.DUMMY_PARAMS+qVar.getName()+Conventions.DUMMY_PARAMS));
                            seqStmt = new SeqStatement(Arrays.asList(new AssignStatement(new Term(qVar.getName()), new Term(randomName.getId()+Constants.FIRST_ITEM)), seqStmt));
                            ForStatement forStmt = new ForStatement(new CallCondition(Conventions.FOR,
                                    Arrays.asList(Conventions.AUTO_CONST_TYPE+"&", randomName.getId(), prfx+Conventions.FUNC_STATE)),seqStmt);
                            stmt.add(new SeqStatement(Arrays.asList(forStmt,
                                    new CallStatement(Conventions.RETURN_CALL, Arrays.asList(Conventions.FALSE)))));
                            List<Variable> listQParams = getAllEventQVariables(e);
                            for(int i = 0; i < listQParams.size(); i++){
                                for(int j = 0; j < Utils.qvarDic.get(funcId).getParams().size(); j++){
                                    if((Conventions.DUMMY_PARAMS+listQParams.get(i).getName()).equals(Utils.qvarDic.get(funcId).getParams().get(j).getName())){
                                        _parameters.add(new Variable(Conventions.DUMMY_PARAMS+listQParams.get(i).getName(), Utils.qvarDic.get(funcId).getParams().get(j).getType(),Utils.qvarDic.get(funcId).getParams().get(j).getInit(), getName()));
                                        break;
                                    }
                                }
                            }
                            parameters.addAll(listQParams);

                            for(int i = 0; i < Utils.qvarDic.get(funcId).getParams().size(); i++){
                                boolean found = false;
                                for(int j = 0; j < parameters.size(); j++){
                                    if((parameters.get(j).getName()).equals(Utils.qvarDic.get(funcId).getParams().get(i))){
                                        found = true;
                                    }
                                }
                                if(!found){
                                    if(!parameters.contains(Utils.qvarDic.get(funcId).getParams().get(i))){
                                        otherparameters.add(Utils.qvarDic.get(funcId).getParams().get(i));
                                    }
                                }
                            }

                            parameters.addAll(otherparameters);

                            if (stmt.isEmpty()) {
                                outSeq = new SeqStatement(Arrays.asList(new CallStatement(Conventions.RETURN_CALL,
                                        Collections.singletonList(Conventions.TRUE))));
                            } else {
                                outSeq = new SeqStatement(Arrays.asList(new SeqStatement(stmt)));
                            }

                            func = new Function(funcId, parameters, Conventions.BOOL_TYPE, outSeq);
                            funcList.add(func);
                        }
                    }
                }

                }

        }
        return funcList;
    }

    @Override
    public List<Function> trans_quantified_condition_step(Event e, Bool timed, List<Variable> varList, ArrayList<ASTD> CallList) {
        ArrayList<ASTD> ASTDTree = new ArrayList<>();
        ASTDTree.addAll(CallList);
        ASTDTree.add(this);
        List<Function>  funcList = new ArrayList<>();
        QuantifiedASTD q = this;

        funcList.addAll(q.getBody().trans_quantified_condition_step(e, timed, varList, ASTDTree));

        Function func;
        String existsName = Conventions.EXISTS + "_" + q.getName()
                + e.getName() + "_" + q.getQvariable().getName(),
                forallName = Conventions.FOR_ALL + "_" + q.getName()
                        + e.getName() + "_" + q.getQvariable().getName();
        String funcId = null;
        if (Utils.qvarDic.containsKey(forallName)) {
            funcId = forallName;
        }
        if (Utils.qvarDic.containsKey(existsName)) {
            funcId = existsName;
        }
        if(funcId != null) {
            Event ev = Utils.qvarDic.get(funcId);

            String domain   = Conventions.getVarSet(q.getName());
            String qvarType = q.getQvariable().getType();
            String evtName  = ev.getName();
            String domType  = (q.getDomain() != null) ? q.getDomain().getType() : "";

            if (evtName.equals(e.getName())) {
                SeqStatement outSeq;
                List<Variable> parameters = new ArrayList<>();
                List<Variable> _parameters = new ArrayList<>();
                List<Statement> stmt = new ArrayList<>();

                if(funcId.equals(forallName)){
                    if (varList != null && !varList.isEmpty()) {
                        AtomicReference<String> cached_funcId = new AtomicReference<>(funcId);
                        //qvar + others parameters

                        String k = getQvariable().getName();
                        Condition val = Utils.qvarCond.get(Constants.DUMMY_PREFIX.get(getQvariable()));
                        SeqStatement seqStmt;
                        //String prfx = getParent().prefix(Constants.DUMMY_PREFIX2.get(q.getQvariable()).getName());
                        String prfx_var = Constants.DUMMY_PREFIX.get(q.getQvariable());
                        String prfx = prfx_var.substring(0 , prfx_var.length() - (getQvariable().getName().length() + 1));
                        String mapState = prfx+ "."
                                + Conventions.FUNC_STATE;
                        Condition valCopy = (Condition) Utils.copyObject(val);
                        seqStmt = (SeqStatement) caseFORALLParams(k, mapState, domType, valCopy, q, domain, ASTDTree);
                        Term randomName = new Term(Utils.generateNameIfNotExists(Conventions.DUMMY_PARAMS+k+Conventions.DUMMY_PARAMS));
                        seqStmt = new SeqStatement(Arrays.asList(new AssignStatement(new Term(k), randomName), seqStmt));
                        ForStatement forStmt = new ForStatement(new CallCondition(Conventions.FOR,
                                Arrays.asList(qvarType, randomName.getId(), domain)), seqStmt);

                        stmt.add(new SeqStatement(Arrays.asList(new DeclStatement(
                                        new Variable(Conventions.EXEC, Conventions.BOOL_TYPE, Conventions.FALSE, null)), forStmt,
                                new IFFIStatement(Arrays.asList(new Entry<>(new CallCondition(Conventions.EQUALS,
                                        Arrays.asList(Conventions.EXEC, domain+Constants.SIZE)),
                                        new CallStatement(Conventions.RETURN_CALL, Arrays.asList(Conventions.TRUE))))))));
                        parameters.add(new Variable(k, qvarType, null, getName()));

                        if(stmt.isEmpty()) {
                            outSeq = new SeqStatement(Arrays.asList(new CallStatement(Conventions.RETURN_CALL,
                                    Collections.singletonList(Conventions.TRUE))));
                        }
                        else {
                            outSeq = new SeqStatement(Arrays.asList(new SeqStatement(stmt),
                                    new CallStatement(Conventions.RETURN_CALL,
                                            Collections.singletonList(Conventions.FALSE))));
                        }

                        if (!Utils.qvarCond.isEmpty()) {
                            func = new Function(funcId, parameters, Conventions.BOOL_TYPE, outSeq);
                            funcList.add(func);
                        }
                    } else {
                        if (!domType.equals(Constants.UNBOUNDEDDOMAIN)) {
                            //no parameters
                            String k = getQvariable().getName();
                            Condition val = Utils.qvarCond.get(Constants.DUMMY_PREFIX.get(getQvariable()));
                            SeqStatement seqStmt;
                            //String prfx = getParent().prefix(Constants.DUMMY_PREFIX2.get(q.getQvariable()).getName());
                            String prfx_var = Constants.DUMMY_PREFIX.get(q.getQvariable());
                            String prfx = prfx_var.substring(0 , prfx_var.length() - (getQvariable().getName().length() + 1));
                            String mapState = prfx+ "."
                                    + Conventions.FUNC_STATE;
                            Condition valCopy = (Condition) Utils.copyObject(val);
                            seqStmt = (SeqStatement) caseWithoutParams(k, mapState, null, null,
                                    valCopy, q, domain, ASTDTree);
                            Term randomName = new Term(Utils.generateNameIfNotExists(Conventions.DUMMY_PARAMS+k+Conventions.DUMMY_PARAMS));
                            seqStmt = new SeqStatement(Arrays.asList(new AssignStatement(new Term(k), randomName), seqStmt));
                            ForStatement forStmt = new ForStatement(new CallCondition(Conventions.FOR,
                                    Arrays.asList(qvarType, randomName.getId(), domain)), seqStmt);

                            stmt.add(new SeqStatement(Arrays.asList(new DeclStatement(
                                            new Variable(Conventions.EXEC, Conventions.BOOL_TYPE, Conventions.FALSE, null)), forStmt,
                                    new IFFIStatement(Arrays.asList(new Entry<>(new CallCondition(Conventions.EQUALS,
                                            Arrays.asList(Conventions.EXEC, domain+Constants.SIZE)),
                                            new CallStatement(Conventions.RETURN_CALL, Arrays.asList(Conventions.TRUE))))))));
                            parameters.add(new Variable(k, qvarType, null, getName()));

                            if (stmt.isEmpty()) {
                                outSeq = new SeqStatement(Arrays.asList(new CallStatement(Conventions.RETURN_CALL,
                                        Collections.singletonList(Conventions.TRUE))));
                            } else {
                                outSeq = new SeqStatement(Arrays.asList(new SeqStatement(stmt),
                                        new CallStatement(Conventions.RETURN_CALL,
                                                Collections.singletonList(Conventions.FALSE))));
                            }

                            func = new Function(funcId, parameters, Conventions.BOOL_TYPE, outSeq);
                            funcList.add(func);
                        }
                        else {
                            System.out.println("[Error] Can not execute event "
                                    + evtName
                                    +" on unbounded "
                                    + "domains without parameter(s) !!");
                            System.exit(0);
                        }
                    }
                }
                else{ //IT IS A EXISTS
                    if (varList != null && !varList.isEmpty()) {
                        AtomicReference<String> cached_funcId = new AtomicReference<>(funcId);
                        //only qvar parameters
                        if(q.getQvariable()!= null){
                            int index = -1;
                            for(int i = 0; i < varList.size(); i++){
                                if(varList.get(i).getName().replace(Conventions.DUMMY_PARAMS, "").equals(q.getQvariable().getName())){
                                    index = i;
                                    break;
                                }
                            }
                            if(index != -1){
                                Variable p = varList.get(index);
                                String p_init = p.getInit().toString();
                                String p_name = p.getName().replace(Conventions.DUMMY_PARAMS, "");
                                if (p_init.compareTo(Constants.ANY_MODE) == 0) {
                                    Condition cSub     = Utils.qvarCond.get(Constants.DUMMY_PREFIX.get(getQvariable())),
                                            cSubCopy = (Condition) Utils.copyObject(cSub);
                                    if (cSub != null
                                            && !cSub.isEqualTo(cached_funcId.get())
                                            && p_name.compareTo(q.getQvariable().getName()) == 0) {


                                        //String prfx = getParent().prefix(Constants.DUMMY_PREFIX2.get(q.getQvariable()).getName());
                                        String prfx_var = Constants.DUMMY_PREFIX.get(q.getQvariable());
                                        String prfx = prfx_var.substring(0 , prfx_var.length() - (getQvariable().getName().length() + 1));
                                        String mapState = prfx+ "."
                                                + Conventions.FUNC_STATE;
                                        IFFIStatement seqCall = null;
                                        if(!domType.equals(Constants.UNBOUNDEDDOMAIN)){
                                            seqCall = new IFFIStatement(Arrays.asList(new Entry<>(new AndCondition(Arrays.asList(
                                                    new CallCondition(Conventions.NOT_IN,
                                                            Arrays.asList(p.getName(), domain)))), new CallStatement(Conventions.RETURN_CALL, Arrays.asList(Conventions.FALSE)))));
                                        }
                                        SeqStatement seqStmt = (SeqStatement) caseWithOnlyQVarEXISTS(p_name, mapState, p, domType, cSubCopy, q, domain, ASTDTree);
                                        if(seqCall != null){
                                            stmt.add(seqCall);
                                        }
                                        stmt.add(seqStmt);
                                    }
                                    List<Variable> listQParams = getAllEventQVariables(e);

                                    parameters.addAll(listQParams);
                                }
                            }else{
                                //varList (list of parameters of a event) does not contain the quantified variable
                                //that means the function shall behaviour as no parameter was given, but need to check the parent (for quantified variables)
                                //the quantified parameters from the parents shall be added to the call of the exists.
                                Variable qVar = q.getQvariable();
                                SeqStatement seqStmt;
                                SeqStatement seqCall;
                                //String prfx = getParent().prefix(Constants.DUMMY_PREFIX2.get(q.getQvariable()).getName());
                                String prfx_var = Constants.DUMMY_PREFIX.get(q.getQvariable());
                                String prfx = prfx_var.substring(0 , prfx_var.length() - (getQvariable().getName().length() + 1));
                                String mapState = prfx+ "."
                                        + Conventions.FUNC_STATE;
                                Condition valCopy = Utils.qvarCond.get(Constants.DUMMY_PREFIX.get(getQvariable()));
                                seqStmt = (SeqStatement) caseWithoutParamsEXISTS(qVar.getName(), mapState, null, null,
                                        valCopy, q, domain, ASTDTree);
                                Term randomName = new Term(Utils.generateNameIfNotExists(Conventions.DUMMY_PARAMS+qVar.getName()+Conventions.DUMMY_PARAMS));
                                seqStmt = new SeqStatement(Arrays.asList(new AssignStatement(new Term(qVar.getName()), randomName), seqStmt));
                                ForStatement forStmt = new ForStatement(new CallCondition(Conventions.FOR,
                                        Arrays.asList(qvarType, randomName.getId(), domain)),seqStmt);
                                stmt.add(new SeqStatement(Arrays.asList(forStmt)));
                                List<Variable> listQParams = getAllEventQVariables(e);
                                parameters.addAll(listQParams);
                            }
                        }

                        if(stmt.isEmpty()) {
                            outSeq = new SeqStatement(Arrays.asList(new CallStatement(Conventions.RETURN_CALL,
                                    Collections.singletonList(Conventions.FALSE))));
                        }
                        else {
                            outSeq = new SeqStatement(Arrays.asList(new SeqStatement(stmt),
                                    new CallStatement(Conventions.RETURN_CALL, Collections.singletonList(Conventions.FALSE))));
                        }

                        if (!Utils.qvarCond.isEmpty()) {
                            func = new Function(funcId, parameters, Conventions.BOOL_TYPE, outSeq);
                            funcList.add(func);
                        }
                    } else {
                        if (!domType.equals(Constants.UNBOUNDEDDOMAIN)) {
                            //no parameters (but for step there is the quantified variable as parameter)
                            Variable qVar = q.getQvariable();
                            SeqStatement seqStmt;
                            //String prfx = getParent().prefix(Constants.DUMMY_PREFIX2.get(q.getQvariable()).getName());
                            String prfx_var = Constants.DUMMY_PREFIX.get(q.getQvariable());
                            String prfx = prfx_var.substring(0 , prfx_var.length() - (getQvariable().getName().length() + 1));
                            String mapState = prfx+ "."
                                    + Conventions.FUNC_STATE;
                            Condition valCopy = Utils.qvarCond.get(Constants.DUMMY_PREFIX.get(getQvariable()));
                            seqStmt = (SeqStatement) caseWithoutParamsEXISTSforStep(qVar.getName(), mapState, null, null,
                                    valCopy, q, domain, ASTDTree);
                            Term randomName = new Term(Utils.generateNameIfNotExists(Conventions.DUMMY_PARAMS+qVar.getName()+Conventions.DUMMY_PARAMS));
                            seqStmt = new SeqStatement(Arrays.asList(new AssignStatement(new Term(qVar.getName()), randomName), seqStmt));
                            ForStatement forStmt = new ForStatement(new CallCondition(Conventions.FOR,
                                    Arrays.asList(qvarType, randomName.getId(), domain)),seqStmt);
                            stmt.add(new SeqStatement(Arrays.asList(forStmt,
                                    new CallStatement(Conventions.RETURN_CALL, Arrays.asList(Conventions.FALSE)))));
                            List<Variable> listQParams = getAllEventQVariables(e);
                            //getAllParameters last element is always null, remove it.
                            //listQParams.remove(listQParams.size()-1);
                            parameters.addAll(listQParams);

                            if (stmt.isEmpty()) {
                                outSeq = new SeqStatement(Arrays.asList(new CallStatement(Conventions.RETURN_CALL,
                                        Collections.singletonList(Conventions.TRUE))));
                            } else {
                                outSeq = new SeqStatement(Arrays.asList(new SeqStatement(stmt)));
                            }

                            func = new Function(funcId, parameters, Conventions.BOOL_TYPE, outSeq);
                            funcList.add(func);
                        }
                        else {
                            //no parameters (but for step there is the quantified variables as parameter)
                            //with unbounded domain, we gonna check the intances that are initialized.
                            //The ones not initialized, do nothing.
                            //The ones initialized, do the transition.
                            //The values of the instantiated ASTDs are passed by the Step() function.
                            //exists for STEP does NOT initialize the instance.
                            Variable qVar = q.getQvariable();
                            SeqStatement seqStmt;
                            //String prfx = getParent().prefix(Constants.DUMMY_PREFIX2.get(q.getQvariable()).getName());
                            String prfx_var = Constants.DUMMY_PREFIX.get(q.getQvariable());
                            String prfx = prfx_var.substring(0 , prfx_var.length() - (getQvariable().getName().length() + 1));
                            String mapState = prfx + "."
                                    + Conventions.FUNC_STATE;
                            Condition valCopy = Utils.qvarCond.get(Constants.DUMMY_PREFIX.get(getQvariable()));
                            seqStmt = (SeqStatement) caseEXISTSforStepUNBOUNDED(qVar.getName(), mapState, null, null,
                                    valCopy, q, domain);
                            stmt.add(new SeqStatement(Arrays.asList(seqStmt,
                                    new CallStatement(Conventions.RETURN_CALL, Arrays.asList(Conventions.FALSE)))));
                            List<Variable> listQParams = getAllEventQVariables(e);
                            //getAllParameters last element is always null, remove it.
                            //listQParams.remove(listQParams.size()-1);
                            //adding the quantified variables before qvar
                            parameters.addAll(listQParams);

                            if (stmt.isEmpty()) {
                                outSeq = new SeqStatement(Arrays.asList(new CallStatement(Conventions.RETURN_CALL,
                                        Collections.singletonList(Conventions.TRUE))));
                            } else {
                                outSeq = new SeqStatement(Arrays.asList(new SeqStatement(stmt)));
                            }

                            func = new Function(funcId, parameters, Conventions.BOOL_TYPE, outSeq);
                            funcList.add(func);

                        }
                    }
                }
            }

        }
        return funcList;
    }

    public List<QuantifiedASTD> findAllQASTDs() {
        List<QuantifiedASTD> qList = new ArrayList<>();
        qList.add(this);
        List<QuantifiedASTD> bList = getBody().findAllQASTDs();
        if(bList != null && !bList.isEmpty())
            qList.addAll(bList);

        return qList;
    }

    public List<Variable> getEvtParams(Event e) {
        List<Variable> params = new ArrayList<>();
        if(Utils.qASTDList != null && !Utils.qASTDList.isEmpty()) {
            for (QuantifiedASTD q : Utils.qASTDList) {
                String  existsName = Conventions.EXISTS + "_" + q.getName() + e.getName()
                                     + "_" + q.getQvariable().getName(),
                        forallName = Conventions.FOR_ALL + "_" + q.getName() + e.getName()
                                     + "_" + q.getQvariable().getName();
                String keyName = null;
                if (!Utils.qvarDic.isEmpty() && Utils.qvarDic.containsKey(forallName)) {
                    keyName = forallName;
                }
                if (!Utils.qvarDic.isEmpty() && Utils.qvarDic.containsKey(existsName)) {
                    keyName = existsName;
                }
                if (keyName != null) {
                    Event ev = Utils.qvarDic.get(keyName);
                    params.addAll(ev.getParams());
                    Utils.qvarDic.replace(keyName, ev);

                    return params;
                }
            }
        }
        if(params.isEmpty())
            params.addAll(getParent().getAllEventParams(e));

        return params;
    }

    private boolean isQuantifiedASTD(ASTD a) {
        return a instanceof QuantifiedASTD;
    }
}
