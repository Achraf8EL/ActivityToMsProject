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
import transform.MermaidActivityExporter;
import transform.MermaidGanttExporter;

public class Main {

    public static void main(String[] args) throws Exception {

        System.out.println("MAIN EXECUTE");

        if (args.length < 2) {
            System.out.println("Usage: java app.Main <inputActivity.xmi> <outputProject.xmi>");
            return;
        }

        String inPath = args[0];
        String outPath = args[1];

        System.out.println("Input  = " + inPath);
        System.out.println("Output = " + outPath);

        // Init packages
        MmactivityPackage.eINSTANCE.eClass();
        MmprojectPackage.eINSTANCE.eClass();

        // XMI factory
        Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap()
                .put("xmi", new XMIResourceFactoryImpl());

        ResourceSet rs = new ResourceSetImpl();

        // 1) Lire le modèle Activity
        Resource inRes = rs.getResource(URI.createFileURI(new File(inPath).getAbsolutePath()), true);
        Object root = inRes.getContents().get(0);

        if (!(root instanceof StateMachine)) {
            throw new IllegalArgumentException("Root element is not a StateMachine. Found: " + root.getClass());
        }

        StateMachine sm = (StateMachine) root;

        // Export Mermaid Activity
        String inputBaseName = new File(inPath).getName().replace(".xmi", "");
        Path activityMermaidOut = Paths.get("model", inputBaseName + "_activity.mmd");
        Files.createDirectories(activityMermaidOut.getParent());

        MermaidActivityExporter.export(sm, activityMermaidOut);
        System.out.println("OK - Mermaid Activity generated at: " + activityMermaidOut);

        // 2) Transformer
        ActivityToMsProjectTransformer transformer = new ActivityToMsProjectTransformer();
        MSProject project = transformer.transform(sm);

        // 3) Sauvegarder le modèle Project
        Resource outRes = rs.createResource(URI.createFileURI(new File(outPath).getAbsolutePath()));
        outRes.getContents().add(project);
        outRes.save(null);

        System.out.println("OK - Project generated at: " + outPath);

        // 4) Export Mermaid Gantt
        String outputBaseName = new File(outPath).getName().replace(".xmi", "");
        Path mermaidOut = Paths.get("model", outputBaseName + "_gantt.mmd");
        Files.createDirectories(mermaidOut.getParent());

        MermaidGanttExporter.export(project, mermaidOut);
        System.out.println("OK - Mermaid Gantt generated at: " + mermaidOut);

        System.out.println("FIN OK");
    }
}