package com.github.cregrant.smaliscissors;

import java.util.ArrayList;
import java.util.List;

public class Main {
    static final double version = 0.01;
    public static OutStream out;
    static DexExecutor dex;

    public static void main(String[] args, OutStream logger, DexExecutor dexExecutor) {
        if (args.length<2) {
            Main.out.println("Usage as module: add String(s) with full path to project and String(s) with full path to zip patches\n" +
                    "Append keepSmaliFilesInRAM or keepXmlFilesInRAM if you want to keep these files in RAM.\n" +
                    "Example ...Main.main(sdcard/ApkEditor/decoded, sdcard/ApkEditor/patches/patch.zip, keepSmaliFilesInRAM");
            return;
        }

        out = logger;
        dex = dexExecutor;
        long startTimeTotal = System.currentTimeMillis();
        ArrayList<String> zipArr = new ArrayList<>(2);
        ArrayList<String> projectsList = new ArrayList<>(1);

        for (String str : args) {
            if (str.endsWith(".zip"))
                zipArr.add(str);
            else if (str.equalsIgnoreCase("keepSmaliFilesInRAM"))
                Prefs.keepSmaliFilesInRAM = true;
            else if (str.equalsIgnoreCase("keepXmlFilesInRAM"))
                Prefs.keepXmlFilesInRAM = true;
            else projectsList.add(str);
        }
        patchProjects(projectsList, zipArr);
        Scan.smaliList = new ArrayList<>(1);
        Scan.xmlList = new ArrayList<>(1);
        Main.out.println("All done in " + (System.currentTimeMillis() - startTimeTotal) + " ms");
        Main.out.println("Good bye Sir.");
    }

    private static void patchProjects(List<String> projectsList, ArrayList<String> zipArr) {
        if (projectsList.isEmpty() || zipArr.isEmpty()) {
            Main.out.println("Empty project or patch list");
            throw new IndexOutOfBoundsException();
        }

        try {
            for (String currentProjectPath : projectsList) {
                Prefs.projectPath = currentProjectPath;
                Executor.executePatches(zipArr);
            }
        } catch (Exception e) {
            StackTraceElement[] stack = e.getStackTrace();
            StringBuilder sb = new StringBuilder();
            sb.append("\nUnexpected error occured:\n\n");
            for (int i=0; i<6; i++) {
                sb.append(stack[i].toString()).append('\n');
            }
            Main.out.println(sb.toString());
        }

    }
}