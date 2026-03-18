package transform;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import mmactivity.ActionState;
import mmactivity.FinalState;
import mmactivity.Pseudostate;
import mmactivity.PseudostateKind;
import mmactivity.StateMachine;
import mmactivity.StateVertex;
import mmactivity.Transition;

public final class MermaidActivityExporter {

    private MermaidActivityExporter() {}

    public static void export(StateMachine sm, Path outFile) throws IOException {
        String mermaid = toMermaid(sm);
        Files.write(outFile, mermaid.getBytes(StandardCharsets.UTF_8));
    }

    public static String toMermaid(StateMachine sm) {
        StringBuilder sb = new StringBuilder();

        String title = safe(sm != null ? sm.getName() : null, "Activity");

        sb.append("---\n");
        sb.append("title: ").append(title).append("\n");
        sb.append("---\n");
        sb.append("flowchart LR\n");

        if (sm == null) {
            return sb.toString();
        }

        // Associer chaque sommet à un identifiant Mermaid stable
        Map<StateVertex, String> idOf = new HashMap<>();
        int i = 1;
        for (StateVertex v : sm.getSubvertex()) {
            idOf.put(v, "n" + i++);
        }

        // Déclaration des noeuds
        for (StateVertex v : sm.getSubvertex()) {
            String id = idOf.get(v);
            String label = safe(v.getName(), v.eClass().getName());

            if (v instanceof ActionState) {
                sb.append("    ").append(id)
                  .append("[\"").append(escape(label)).append("\"]\n");

            } else if (v instanceof FinalState) {
                sb.append("    ").append(id)
                  .append("((\"").append(escape(label)).append("\"))\n");

            } else if (v instanceof Pseudostate) {
                PseudostateKind kind = ((Pseudostate) v).getKind();

                if (kind == PseudostateKind.INITIAL) {
                    sb.append("    ").append(id)
                      .append("([\"").append(escape(label)).append(" / initial\"])\n");

                } else if (kind == PseudostateKind.FORK) {
                    sb.append("    ").append(id)
                      .append("{\"").append(escape(label)).append(" / fork\"}\n");

                } else if (kind == PseudostateKind.JOIN) {
                    sb.append("    ").append(id)
                      .append("{\"").append(escape(label)).append(" / join\"}\n");

                } else {
                    sb.append("    ").append(id)
                      .append("[\"").append(escape(label)).append("\"]\n");
                }
            } else {
                sb.append("    ").append(id)
                  .append("[\"").append(escape(label)).append("\"]\n");
            }
        }

        sb.append("\n");

        // Déclaration des transitions
        for (Transition tr : sm.getTransitions()) {
            StateVertex src = tr.getSource();
            StateVertex tgt = tr.getTarget();

            String srcId = idOf.get(src);
            String tgtId = idOf.get(tgt);

            if (srcId != null && tgtId != null) {
                sb.append("    ")
                  .append(srcId)
                  .append(" --> ")
                  .append(tgtId)
                  .append("\n");
            }
        }

        return sb.toString();
    }

    private static String safe(String s, String fallback) {
        if (s == null) return fallback;
        String trimmed = s.trim();
        return trimmed.isEmpty() ? fallback : trimmed;
    }

    private static String escape(String s) {
        return s.replace("\"", "'")
                .replace("\n", " ")
                .replace("\r", " ");
    }
}