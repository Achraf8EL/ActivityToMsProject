package transform;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import mmproject.MSProject;
import mmproject.Task;

public final class MermaidGanttExporter {

    private MermaidGanttExporter() {}

    public static void export(MSProject project, Path outFile) throws IOException {
        String mermaid = toMermaid(project);

        // Java 8: pas de Files.writeString, on utilise Files.write
        Files.write(outFile, mermaid.getBytes(StandardCharsets.UTF_8));
    }

    public static String toMermaid(MSProject project) {
        String projectName = safe(project != null ? project.getName() : null, "Project");

        List<Task> tasks = new ArrayList<>();
        if (project != null && project.getTasks() != null) {
            tasks.addAll(project.getTasks());
        }

        // Ordre stable: UID numérique si possible, sinon lexical
        Collections.sort(tasks, new Comparator<Task>() {
            @Override
            public int compare(Task a, Task b) {
                return Integer.compare(parseIntSafe(a.getUID()), parseIntSafe(b.getUID()));
            }
        });

        // Mermaid Gantt: on va produire un graphe "simple et joli"
        // (Sans dates réelles: on met des durées fixes, et on utilise after <id>)
        StringBuilder sb = new StringBuilder();
        sb.append("gantt\n");
        sb.append("  title ").append(escapeMermaidText(projectName)).append("\n");
        sb.append("  dateFormat  YYYY-MM-DD\n");
        sb.append("  axisFormat  %d/%m\n");
        sb.append("  excludes    weekends\n\n");
        sb.append("  section Tasks\n");

        // IDs Mermaid: t1, t2, ...
        Map<Task, String> idOf = new HashMap<>();
        for (int i = 0; i < tasks.size(); i++) {
            idOf.put(tasks.get(i), "t" + (i + 1));
        }

        // Date de départ arbitraire (Mermaid exige un start)
        String startDate = "2026-02-16"; // demain par rapport à ton contexte, mais tu peux changer

        for (Task t : tasks) {
            String name = safe(t.getName(), "Task");
            String id = idOf.get(t);

            // Récupérer predecessors (selon ton méta-modèle, ça peut être:
            // - un seul Task (reference simple)
            // - ou une liste EList<Task> (multi)
            // Ici on gère les 2 cas via réflexion légère.
            List<Task> preds = getPredecessorsAsList(t);

            sb.append("  ")
              .append(escapeMermaidText(name))
              .append(" :")
              .append(id)
              .append(", ");

            if (preds.isEmpty()) {
                // Tâche racine: start + durée fixe
                sb.append(startDate).append(", 1d");
            } else {
                // Mermaid accepte: after <id>
                // Si plusieurs prédécesseurs, Mermaid ne gère pas "after a b" officiellement,
                // donc on choisit une approche simple: after le dernier prédécesseur (ordre UID),
                // ce qui reste lisible pour ton rendu.
                Task chosen = chooseLatestByUID(preds);
                String afterId = idOf.get(chosen);
                if (afterId == null) afterId = id; // fallback
                sb.append("after ").append(afterId).append(", 1d");
            }

            sb.append("\n");
        }

        return sb.toString();
    }

    // ---------- Helpers Java 8 ----------

    private static String safe(String s, String fallback) {
        if (s == null) return fallback;
        String trimmed = s.trim();
        return trimmed.isEmpty() ? fallback : trimmed;
    }

    private static int parseIntSafe(String s) {
        if (s == null) return Integer.MAX_VALUE;
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return Integer.MAX_VALUE;
        }
    }

    private static String escapeMermaidText(String s) {
        // Mermaid: éviter ":" qui casse parfois la syntaxe des tâches
        return s.replace(":", "-").replace("\n", " ").replace("\r", " ");
    }

    private static Task chooseLatestByUID(List<Task> preds) {
        Task best = preds.get(0);
        int bestUid = parseIntSafe(best.getUID());
        for (Task t : preds) {
            int uid = parseIntSafe(t.getUID());
            if (uid > bestUid) {
                best = t;
                bestUid = uid;
            }
        }
        return best;
    }

    @SuppressWarnings("unchecked")
    private static List<Task> getPredecessorsAsList(Task t) {
        List<Task> res = new ArrayList<>();
        if (t == null) return res;

        try {
            // Cas 1: predecessors est une liste (EList<Task>)
            Object o = t.getClass().getMethod("getPredecessors").invoke(t);
            if (o instanceof List) {
                res.addAll((List<Task>) o);
                return res;
            }
            // Cas 2: predecessors est une référence simple Task
            if (o instanceof Task) {
                res.add((Task) o);
                return res;
            }
        } catch (Exception ignore) {
            // Si jamais le getter n'existe pas ou autre, on renvoie vide
        }
        return res;
    }
}
