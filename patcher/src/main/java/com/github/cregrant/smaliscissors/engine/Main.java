package com.github.cregrant.smaliscissors.engine;

import java.io.File;
import java.util.ArrayList;

public class Main {
    static final double version = 0.01;
    static OutStream out;

    public static void main(String[] args, OutStream logger) {
        out = logger;
        long startTimeTotal = System.currentTimeMillis();
        ArrayList<String> zipArr = new ArrayList<>();
        ArrayList<String> projectsList = new ArrayList<>();
        if (args.length<2)
            Main.out.println("Usage as module: add String(s) with full path to project and String(s) with full path to zip patches\n" +
                    "Append keepSmaliFilesInRAM or keepXmlFilesInRAM if you want to keep these files in RAM.\n" +
                    "Example ...Main.main(sdcard/ApkEditor/decoded, sdcard/ApkEditor/patches/patch.zip, keepSmaliFilesInRAM");
        for (String str : args) {
            if (str.endsWith(".zip")) zipArr.add(str);
            else if (str.equalsIgnoreCase("keepSmaliFilesInRAM")) Prefs.keepSmaliFilesInRAM = true;
            else if (str.equalsIgnoreCase("keepXmlFilesInRAM")) Prefs.keepXmlFilesInRAM = true;
            else projectsList.add(str);
        }
        if (System.getProperty("os.name").startsWith("Windows"))
            Prefs.run_type = "pc";
        else
            Prefs.run_type = "module";
        Prefs.patchesDir = new File(zipArr.get(0)).getParentFile();
        Prefs.tempDir = new File(Prefs.patchesDir + File.separator + "temp");
        runAsModule(projectsList, zipArr);
        Main.out.println("All done in " + (System.currentTimeMillis() - startTimeTotal) + " ms");
        Main.out.println("Good bye Sir.");
    }

    static void runAsModule(ArrayList<String> projectsList, ArrayList<String> zipArr) {
        if (projectsList.isEmpty() || zipArr.isEmpty()) {
            Main.out.println("Empty project or patch list");
            throw new IndexOutOfBoundsException();
        }

        String patchResult;
        for (String currentProjectPath : projectsList) {
            Prefs.projectPath = currentProjectPath;
            patchResult = new Executor().executePatches(zipArr);
            if (patchResult.equals("error")) {
                IO.deleteAll(Prefs.tempDir);
                Main.out.println("Executor error occurred");
            }
        }
    }
}