ActivityToMsProject(in mA : MMActivity ; out mP : MMProject)

Pour tout mA.StateMachine sm faire
    pro := MMProject.MSProject.créer(sm.name)
    mP.lesProjets.add(pro)

    curId := 0
    taskOf := Dictionnaire<StateVertex, Task>   // correspondance source → task

    Pour tout sm.subvertex v faire
        Si (v.class = ActionState) OU (v.class = FinalState) OU (v.class = Pseudostate ET v.kind = initial) alors
            curId := curId + 1
            t := MMProject.Task.créer(v.name)
            t.UID := curId.toString()
            t.predecessors := ∅
            pro.tasks.add(t)
            taskOf.put(v, t)
        Sinon
            // fork/join : pas de Task créée
        FinSi
    FinPour

    Pour tout sm.subvertex v faire
        Si taskOf.containsKey(v) alors
            t := taskOf.get(v)

            Si (v.class = Pseudostate ET v.kind = initial) alors
                t.predecessors := ∅
            Sinon
                predVertices := getPredecessorVertices(v)   // helper
                predTasks := ∅

                Pour tout pv dans predVertices faire
                    predTasks := predTasks ∪ { taskOf.get(pv) }
                FinPour

                t.predecessors := predTasks
            FinSi
        FinSi
    FinPour
FinPour
