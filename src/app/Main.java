package app;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import mmactivity.MmactivityPackage;
import mmactivity.StateMachine;

import mmproject.MSProject;
import mmproject.MmprojectPackage;

import transform.ActivityToMsProjectTransformer;
import transform.MermaidGanttExporter;

public class Main {

    public static void main(String[] args) throws Exception {

        if (args.length < 2) {
            System.out.println("Usage: java app.Main <inputActivity.xmi> <outputProject.xmi>");
            return;
        }

        String inPath = args[0];
        String outPath = args[1];

        // Init packages (important EMF)
        MmactivityPackage.eINSTANCE.eClass();
        MmprojectPackage.eINSTANCE.eClass();

        // XMI factory
        Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap()
                .put("xmi", new XMIResourceFactoryImpl());

        ResourceSet rs = new ResourceSetImpl();

        // 1) Lire le modèle Activity (XMI)
        Resource inRes = rs.getResource(URI.createFileURI(new File(inPath).getAbsolutePath()), true);
        Object root = inRes.getContents().get(0);

        if (!(root instanceof StateMachine)) {
            throw new IllegalArgumentException("Root element is not a StateMachine. Found: " + root.getClass());
        }

        StateMachine sm = (StateMachine) root;

        // 2) Transformer
        ActivityToMsProjectTransformer transformer = new ActivityToMsProjectTransformer();
        MSProject project = transformer.transform(sm);

        // 3) Sauvegarder le modèle Project (XMI)
        Resource outRes = rs.createResource(URI.createFileURI(new File(outPath).getAbsolutePath()));
        outRes.getContents().add(project);
        outRes.save(null);

        System.out.println("OK - Project generated at: " + outPath);

        // 4) Export Mermaid Gantt (optionnel mais utile)
        // -> on écrit dans model/gantt.mmd (à côté de tes modèles)
        Path mermaidOut = Paths.get("model", "gantt.mmd");
        Files.createDirectories(mermaidOut.getParent()); // crée "model/" si absent

        MermaidGanttExporter.export(project, mermaidOut);
        System.out.println("OK - Mermaid Gantt generated at: " + mermaidOut.toString());
    }
}
