package com.udes.optimizer;

import com.udes.model.astd.base.QuantifiedASTD;
import com.udes.model.astd.items.Event;
import com.udes.model.astd.items.Transition;
import com.udes.model.astd.items.Variable;
import com.udes.model.il.conventions.Conventions;
import com.udes.optimizer.base.Optimizer;
import com.udes.utils.Utils;

import java.util.*;
import java.util.List;


public class KappaOptimizer implements Optimizer {

    public static String EQUALS = "==";
    public static String AND = "&&";
    public static String OR = "||";
    private boolean isOptimizable = false;

    public enum AnalysisMode {
        INDIRECT_KAPPA,
        DIRECT_KAPPA
    }

    public class Report {
        String topASTD;
        QuantifiedASTD qASTD;
        String evtLabel;
        List<List<String>> paramList;
    }

    private String rootName;
    private QuantifiedASTD astd;
    private AnalysisMode mode;

    public KappaOptimizer(String rootName, QuantifiedASTD a, AnalysisMode mode) {
        this.rootName = rootName;
        this.astd = a;
        this.mode = mode;
    }

    @Override
    public void optimize() {
        if(mode == AnalysisMode.DIRECT_KAPPA)
            printResult(kappaDirectReport());
        if(mode == AnalysisMode.INDIRECT_KAPPA) /*TODO: */;
    }

    private List<Report> kappaDirectReport() {
        List<Report> repList = new ArrayList<>();
        ArrayList<Event> evts = Utils.mergeEvents(astd.findAllEvents());
        if(evts != null) {
            evts.forEach(e -> {
                Report r = new Report();
                r.topASTD = rootName;
                r.qASTD = astd;
                r.paramList = new ArrayList<>();
                r.evtLabel = e.getName();
                List<Transition> transList = astd.findTransitions(r.evtLabel);
                if(transList != null) {
                    transList.forEach(trans -> {
                        List<List<String>> params = extractDependencies(astd.getQvariable(), trans);
                        if(params != null && !params.isEmpty()) {
                            r.paramList.addAll(params);
                            isOptimizable = true;
                        }
                    });
                }
                repList.add(r);
            });
        }
        return repList;
    }

    private List<List<String>> extractDependencies(Variable qvar, Transition trans) {
        List<List<String>> deps = new ArrayList<>();
        List<String> tmp = new ArrayList<>(), _tmp = new ArrayList<>();
        Event e = trans.getEvent();
        List<Variable> params = e.getParams();

        if(params == null)
            return deps;
        else {
            for(int i = 0; i < params.size(); ++i) {
                //checks whether a list of quantified variable is in parameter
                if(qvar.getName().compareTo(params.get(i).getName()) == 0
                   && (params.get(i).getType() == null || params.get(i).getType().isEmpty())){
                    tmp.add(Conventions.EVENT_PARAMS + "[" + i + "]");
                }
                else if(qvar.getName().compareTo(params.get(i).getName()) != 0
                        && params.get(i).getType() != null) {
                    // checks whether ?x:T in parameter
                    // and the quantified variable is in the disjunction of constraints
                    List<String> lst = findDisjunctionExpressions(i, trans, qvar);
                    if(!lst.isEmpty())
                        deps.add(lst);
                    // we have one constraint, just continue to avoid redundant dependency
                    if(lst.size() == 1)
                        continue;
                    // checks whether ?x:T in parameter
                    // and the quantified variable is in the conjunction of constraints
                    String dep = findConjunctionExpressions(i, trans, qvar);
                    if(!dep.isEmpty())
                        _tmp.add(dep);
                }
            }
            //a list of quantified variable is in parameter
            if(!tmp.isEmpty())
                deps.add(tmp);
            if(!_tmp.isEmpty())
                deps.add(_tmp);
        }

        return deps;
    }

    private List<String> findDisjunctionExpressions(int index, Transition trans, Variable qvar) {
        List<String> deps = new ArrayList<>();
        String g = trans.getGuard();
        if(g != null) {
            g = g.trim();
            Variable param = trans.getEvent().getParams().get(index);
            // p = x
            String tmp = param.getName() + EQUALS + qvar.getName();
            if(g.contains(tmp + OR) || g.contains(OR + tmp))
                deps = Arrays.asList(Conventions.EVENT_PARAMS + "[" + index + "]");
            //x = p
            tmp = qvar.getName() + EQUALS + param.getName();
            if(g.contains(tmp + OR) || g.contains(OR + tmp))
                deps = Arrays.asList(Conventions.EVENT_PARAMS + "[" + index + "]");
        }
        return deps;
    }

    private String findConjunctionExpressions(int index, Transition trans, Variable qvar) {
        String dep = "", g = trans.getGuard();
        if(g != null) {
            g =g.trim();
            Variable param = trans.getEvent().getParams().get(index);
            // p = x
            String tmp = param.getName() + EQUALS + qvar.getName();
            if(g.contains(tmp + AND) || g.contains(AND + tmp))
                dep = Conventions.EVENT_PARAMS + "[" + index + "]";
            // x = p
            tmp = qvar.getName() + EQUALS + param.getName();
            if(g.contains(tmp + AND) || g.contains(AND + tmp))
                dep = Conventions.EVENT_PARAMS + "[" + index + "]";
        }
        return dep;
    }

    public void printResult(List<Report> reps) {
        if (reps != null && !reps.isEmpty() && isOptimizable) {
            System.out.println("================ Kappa report =================");
            System.out.println("Result: the specification is Kappa optimizable.");
            for(Report r : reps) {
                System.out.println("Top ASTD: " + r.topASTD);
                System.out.println("Quantified ASTD: " + r.qASTD.getName());
                System.out.println("Event: " + r.evtLabel);
                System.out.println("Parameters: {");
                if(r.paramList != null) {
                    for (List<String> lst : r.paramList) {
                        System.out.print("{");
                        int s = lst.size();
                        for (int i = 0; i < s - 1; i++) {
                            System.out.print(lst.get(i) + ",");
                        }
                        System.out.print(lst.get(s - 1) + "}");
                        System.out.println();
                    }
                    System.out.println("}");
                }
            }
            QuantifiedASTD qastd = reps.get(0).qASTD;
            if(astd != null)
                Utils.print(qastd);
        }
        else {
            System.out.println("================ Kappa report =================");
            System.out.println("Result: the specification is not Kappa optimizable.");
        }
    }
}
