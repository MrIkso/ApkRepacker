package com.github.cregrant.smaliscissors.engine;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Stack;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

class Scan {
    static ArrayList<DecompiledFile> smaliList = new ArrayList<>();
    static ArrayList<DecompiledFile> xmlList = new ArrayList<>();

    static void scanProject(boolean xmlNeeded, boolean smaliNeeded) {
        long startTime = System.currentTimeMillis();
        Collection<Future<ArrayList<DecompiledFile>>> results = new ArrayList<>();
        Future<ArrayList<DecompiledFile>> task;
        BackgroundWorker.createIfTerminated();

        if (smaliNeeded) {         //add smali folders
            File[] list = new File(Prefs.projectPath).listFiles();
            if (Objects.requireNonNull(list).length == 0)
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
                xmlList.add(manifest);
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
                            xmlList.add(scannedFile);   //grab tiny xml
                    }
                    else {
                        if (scannedFile.isBigSize())
                            bigSmaliList.add(scannedFile);          //grab big smali
                        else
                            smaliList.add(scannedFile); //grab tiny smali
                    }
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return;
        }

        //moving large files to the beginning to load more cores at the end
        bigXmlList.addAll(xmlList);
        bigSmaliList.addAll(smaliList);
        smaliList = bigSmaliList;
        xmlList = bigXmlList;

        if (Prefs.keepSmaliFilesInRAM)
            loadToRam(smaliList);

        if (Prefs.keepXmlFilesInRAM)
            loadToRam(xmlList);

        try {
            BackgroundWorker.executor.shutdown();
            BackgroundWorker.executor.awaitTermination(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(0);
        }
        Main.out.println(smaliList.size() + " smali & " + xmlList.size() + " xml files found in " + (System.currentTimeMillis() - startTime) + "ms.\n");
    }

    private static void loadToRam(ArrayList<DecompiledFile> array) {
        for (DecompiledFile dFile : array) {
            Runnable r = () -> dFile.setBody(IO.read(Prefs.projectPath + File.separator + dFile.getPath()));
            BackgroundWorker.executor.submit(r);
        }
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
                                if (file.getPath().startsWith(str)) {
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
                decompiledFiles.add(scanFile(stack.pop()));
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
        if (Prefs.isWindows)
            shortPath = shortPath.replace("\\\\", "\\");  //simple regex-to-path convert
        if (Prefs.verbose_level == 0) {
            Main.out.println(shortPath + " removed.");
        }
        boolean isXml = shortPath.endsWith(".xml");
        int size;
        ArrayList<DecompiledFile> files;
        if (isXml) {
            files = xmlList;
            size = xmlList.size();
        }
        else {
            files = smaliList;
            size = smaliList.size();
        }

        for (int i = 0; i < size; i++) {
            if (files.get(i).getPath().equals(shortPath)) {
                files.remove(i);
                i--;
                size--;
            }
        }
    }

    static String getApkPath() {
        File[] files = new File(Prefs.projectPath).listFiles();
        if (files != null) {
            for (File str : files) {
                if (str.getName().startsWith("apktool."))
                    return parseConfig(str);
            }
        }
        return null;
    }

    private static String parseConfig(File config) {
        Pattern pattern = Pattern.compile(".{0,5}apkFile.+?(?:\": \"|: )(.+?\\.apk)(?:\",)?");
        String scannedPath = Regex.matchSingleLine(pattern, IO.read(config.getPath()));
        File outerDir = new File(Prefs.projectPath).getParentFile();
        File apkFile = new File(outerDir + File.separator + scannedPath);
        if (scannedPath != null) {
            if (apkFile.exists())
                return apkFile.getPath();
            apkFile = new File(scannedPath);
            if (apkFile.exists())
                return apkFile.getPath();
        }
        return null;
    }
}
