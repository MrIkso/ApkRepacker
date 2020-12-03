package com.github.cregrant.smaliscissors.engine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Stack;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Scan {
    static void scanProject(boolean xmlNeeded, boolean smaliNeeded) {
        long startTime = System.currentTimeMillis();
        List<Future<ArrayList<DecompiledFile>>> results = new ArrayList<>();
        Future<ArrayList<DecompiledFile>> task;
        BackgroundWorker.createIfTerminated();

        if (smaliNeeded) {         //add smali folders
            File[] list = new File(Prefs.projectPath).listFiles();
            if (list.length == 0)
                Main.out.println("WARNING: no smali folders found inside the project folder \"" + Regex.getEndOfPath(Prefs.projectPath) + '\"');
            for (File folder : list) {
                String name = folder.toString().replace(Prefs.projectPath + File.separator, "");
                if (folder.isDirectory() && name.startsWith("smali")) {
                    task = BackgroundWorker.executor.submit(() -> scanFolder(folder));
                    results.add(task);
                }
            }
        }
        //add AndroidManifest.xml & res folder
        if (xmlNeeded) {
            if (new File(Prefs.projectPath + File.separator + "AndroidManifest.xml").exists()) {
                DecompiledFile manifest = new DecompiledFile(true, "AndroidManifest.xml");
                ProcessRule.xmlList.add(manifest);
            }
            File resFolder = new File(Prefs.projectPath + File.separator + "res");
            if (resFolder.exists() || Objects.requireNonNull(resFolder.list()).length != 0) {
                task = BackgroundWorker.executor.submit(() -> scanFolder(resFolder));
                results.add(task);
            }
            else
                Main.out.println("WARNING: no resources found inside the res folder.");
        }
        ArrayList<DecompiledFile> bigSmaliList = new ArrayList<>();
        ArrayList<DecompiledFile> bigXmlList = new ArrayList<>();

        try {
            for (Future<ArrayList<DecompiledFile>> array : results) {
                for (DecompiledFile scannedFile : array.get()) {
                    if (scannedFile.isXML()) {
                        if (scannedFile.isBigSize())
                            bigXmlList.add(scannedFile);            //grab big xml
                        else
                            ProcessRule.xmlList.add(scannedFile);   //grab tiny xml
                    }
                    else {
                        if (scannedFile.isBigSize())
                            bigSmaliList.add(scannedFile);          //grab big smali
                        else
                            ProcessRule.smaliList.add(scannedFile); //grab tiny smali
                    }
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return;
        }

        //moving large files to the beginning to load more cores at the end
        bigXmlList.addAll(ProcessRule.xmlList);
        bigSmaliList.addAll(ProcessRule.smaliList);
        ProcessRule.smaliList = bigSmaliList;
        ProcessRule.xmlList = bigXmlList;

        if (Prefs.keepSmaliFilesInRAM) {
            int totalNum = ProcessRule.smaliList.size();
            AtomicInteger currentNum = new AtomicInteger(0);
            int num;
            while ((num = currentNum.getAndIncrement()) < totalNum) {
                int finalNum = num;
                Runnable r = () -> {
                    DecompiledFile dFile = ProcessRule.smaliList.get(finalNum);
                    dFile.setBody(IO.read(Prefs.projectPath + File.separator + dFile.getPath()));
                };
                BackgroundWorker.executor.submit(r);
            }
        }

        if (Prefs.keepXmlFilesInRAM) {
            int totalNum = ProcessRule.xmlList.size();
            AtomicInteger currentNum = new AtomicInteger(0);
            int num;
            while ((num = currentNum.getAndIncrement()) < totalNum) {
                int finalNum = num;
                Runnable r = () -> {
                    DecompiledFile dFile = ProcessRule.xmlList.get(finalNum);
                    dFile.setBody(IO.read(Prefs.projectPath + File.separator + dFile.getPath()));
                };
                BackgroundWorker.executor.submit(r);
            }

            try {
                BackgroundWorker.executor.shutdown();
                BackgroundWorker.executor.awaitTermination(60, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(0);
            }
        }
        Main.out.println(ProcessRule.smaliList.size() + " smali & " + ProcessRule.xmlList.size() + " xml files found in " + (System.currentTimeMillis() - startTime) + "ms.");
    }

    static ArrayList<DecompiledFile> scanFolder(File folder) {
        ArrayList<DecompiledFile> decompiledFiles = new ArrayList<>();
        Stack<File> stack = new Stack<>();
        stack.add(folder);
        while (!stack.isEmpty()) {
            if (stack.peek().isDirectory()) {
                for (File file : Objects.requireNonNull(stack.pop().listFiles())) {
                    if (file.isDirectory()) {
                        boolean doNotSkip = true;
                        if (Prefs.skipSomeSmaliFiles) {                  //skip some folders
                            for (String str : Prefs.smaliFoldersToSkip)
                                if (file.getPath().contains(str)) {
                                    doNotSkip = false;
                                    break;
                                }
                        }
                        if (doNotSkip)
                            stack.push(file);
                    }
                    else {
                        DecompiledFile tmp;
                        if ((tmp = scanFile(file)) != null) {
                            tmp.setBigSize(file.length() > 100000);
                            decompiledFiles.add(tmp);
                        }
                    }
                }
            }
            else
                scanFile(stack.pop());
        }
        return decompiledFiles;
    }

    private static DecompiledFile scanFile(File fileToScan) {
        String path = fileToScan.getPath().replace(Prefs.projectPath + File.separator, "");
        boolean isSmali = path.endsWith(".smali");
        boolean isXml = path.endsWith(".xml");

        if (!(isSmali || isXml))   //not xml nor smali: skip
            return null;

        return new DecompiledFile(isXml, path);
    }

    static void removeLoadedFile(String shortPath) {
        if (Prefs.verbose_level == 0) {
            Main.out.println(shortPath + " removed.");
        }
        boolean isXml = shortPath.endsWith(".xml");
        int size;
        if (isXml)
            size = ProcessRule.xmlList.size();
        else
            size = ProcessRule.smaliList.size();

        for (int i = 0; i < size; i++) {
            if (isXml && ProcessRule.xmlList.get(i).getPath().equals(shortPath)) {
                ProcessRule.xmlList.remove(i);
                i--;
                size--;
            }
            else if (ProcessRule.smaliList.get(i).getPath().equals(shortPath)) {
                ProcessRule.smaliList.remove(i);
                i--;
                size--;
            }
        }
    }
}
