package transform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.ecore.util.EcoreUtil;

import mmactivity.ActionState;
import mmactivity.FinalState;
import mmactivity.Pseudostate;
import mmactivity.PseudostateKind;
import mmactivity.StateMachine;
import mmactivity.StateVertex;
import mmactivity.Transition;

import mmproject.MSProject;
import mmproject.MmprojectFactory;
import mmproject.Task;

public class ActivityToMsProjectTransformer {

    private int curId = 0;

    // correspondance StateVertex (source) -> Task (cible)
    private final Map<StateVertex, Task> taskOf = new HashMap<>();

    // transitions entrantes pré-calculées (target -> list of incoming transitions)
    private Map<StateVertex, List<Transition>> incomingMap = new HashMap<>();

    public MSProject transform(StateMachine sm) {
        // 3) reset si on réutilise le même transformer
        curId = 0;
        taskOf.clear();

        // 4) pré-calcul incoming transitions
        incomingMap = buildIncomingMap(sm);

        MmprojectFactory pf = MmprojectFactory.eINSTANCE;

        // 1) Créer le projet
        MSProject pro = pf.createMSProject();
        pro.setName(sm.getName() != null ? sm.getName() : "Project");

        // PASS 1 : créer toutes les tâches (initial + action + final)
        for (StateVertex v : sm.getSubvertex()) {
            if (isTaskVertex(v)) {
                Task t = pf.createTask();
                t.setUID(Integer.toString(++curId));
                t.setName(v.getName() != null ? v.getName() : v.eClass().getName());

                pro.getTasks().add(t);
                taskOf.put(v, t);
            }
        }

        // PASS 2 : remplir les predecessors[*]
        for (StateVertex v : sm.getSubvertex()) {
            Task t = taskOf.get(v);
            if (t == null) continue;

            if (isInitial(v)) {
                // initial => pas de prédécesseur
                t.getPredecessors().clear();
            } else {
                // 2) éviter récursion infinie via visited
                Set<StateVertex> predVertices = getPredecessorVertices(sm, v);
                for (StateVertex pv : predVertices) {
                    Task pt = taskOf.get(pv);
                    if (pt != null && !t.getPredecessors().contains(pt)) {
                        t.getPredecessors().add(pt);
                    }
                }
            }
        }

        return pro;
    }

    // 4) incoming map
    private Map<StateVertex, List<Transition>> buildIncomingMap(StateMachine sm) {
        Map<StateVertex, List<Transition>> map = new HashMap<>();
        for (Transition tr : sm.getTransitions()) {
            StateVertex tgt = tr.getTarget();
            map.computeIfAbsent(tgt, k -> new ArrayList<>()).add(tr);
        }
        return map;
    }

    private boolean isTaskVertex(StateVertex v) {
        return (v instanceof ActionState)
                || (v instanceof FinalState)
                || isInitial(v);
    }

    private boolean isInitial(StateVertex v) {
        return (v instanceof Pseudostate) && (((Pseudostate) v).getKind() == PseudostateKind.INITIAL);
    }

    /**
     * Retourne l’ensemble des vertices "tâches" qui précèdent v,
     * en sautant fork/join (récursif).
     */
    private Set<StateVertex> getPredecessorVertices(StateMachine sm, StateVertex v) {
        return getPredecessorVertices(sm, v, new HashSet<>());
    }

    private Set<StateVertex> getPredecessorVertices(StateMachine sm, StateVertex v, Set<StateVertex> visited) {
        // 2) anti-boucle
        if (!visited.add(v)) return Collections.emptySet();

        Set<StateVertex> result = new HashSet<>();

        // transitions entrantes via map (4)
        List<Transition> incoming = incomingMap.getOrDefault(v, Collections.emptyList());
        if (incoming.isEmpty()) return result;

        for (Transition tr : incoming) {
            StateVertex src = tr.getSource();

            // 1) safer equality (au cas où) : on s'assure bien que tr.target == v
            // (utile si tu reconstruis / reload / proxies)
            if (!EcoreUtil.equals(tr.getTarget(), v)) {
                continue;
            }

            if (src instanceof ActionState) {
                result.add(src);

            } else if (isInitial(src)) {
                result.add(src);

            } else if (src instanceof Pseudostate) {
                PseudostateKind k = ((Pseudostate) src).getKind();
                // 5) fork/join : on saute et on remonte
                if (k == PseudostateKind.FORK || k == PseudostateKind.JOIN) {
                    result.addAll(getPredecessorVertices(sm, src, visited));
                }
            }
        }

        return result;
    }
}
