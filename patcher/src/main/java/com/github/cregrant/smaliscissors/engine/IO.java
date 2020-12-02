package com.github.cregrant.smaliscissors.engine;

import java.io.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@SuppressWarnings("ResultOfMethodCallIgnored")
class IO {

    static String currentProjectPathCached = "";

    static Patch loadRules(String zipFile) {
        Pattern patRule = Pattern.compile("(\\[.+?](?:\\RNAME:\\R.++)?(?:\\RGOTO:\\R.++)?(?:\\RSOURCE:\\R.++)?\\R(?:TARGET:[\\s\\S]*?)?\\[/.+?])", Pattern.UNIX_LINES);
        deleteAll(Prefs.tempDir);
        Prefs.tempDir.mkdirs();
        String txtFile = Prefs.tempDir + File.separator + "patch.txt";
        zipExtract(zipFile, Prefs.tempDir.toString());
        if (!new File(txtFile).exists()) {
            Main.out.println("No patch.txt file in patch!");
            System.exit(1);
        }

        ArrayList<String> rulesListArr = Regex.matchMultiLines(Objects.requireNonNull(patRule), read(txtFile), "rules");
        RuleParser parser = new RuleParser();
        Patch patch = new Patch();
        for (String ruleString : rulesListArr) {
            Rule rule = parser.parseRule(ruleString);
            if (rule.isSmali)
                patch.smaliNeeded = true;
            else if (rule.isXml)
                patch.xmlNeeded = true;
            patch.addRule(rule);
        }

        Main.out.println(rulesListArr.size() + " rules found\n");
        return patch;
    }

    static String read(String path) {
        final FileInputStream is;
        String resultString = null;
        try {
            is = new FileInputStream(path);
            byte[] buffer = new byte[is.available()];
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            is.read(buffer);
            os.write(buffer);
            resultString = os.toString();
            os.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
            Main.out.println("Exiting to prevent file corruption");
            System.exit(1);
        }
        return resultString;
    }

    static void write(String path, String content) {
        try {
            deleteAll(new File(path));
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(path));
            bufferedWriter.write(content);
            bufferedWriter.flush();
            bufferedWriter.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void writeChanges() {
        if (Prefs.keepSmaliFilesInRAM) {
            for (int j = 0; j < ProcessRule.smaliList.size(); ++j) {
                DecompiledFile tmpSmali = ProcessRule.smaliList.get(j);
                if (!tmpSmali.isModified()) continue;
                tmpSmali.setModified(false);
                write(Prefs.projectPath + File.separator + tmpSmali.getPath(), tmpSmali.getBody());
            }
        }
        if (Prefs.keepXmlFilesInRAM) {
            for (int j = 0; j < ProcessRule.xmlList.size(); ++j) {
                DecompiledFile dFile = ProcessRule.xmlList.get(j);
                if (!dFile.isModified()) continue;
                dFile.setModified(false);
                write(Prefs.projectPath + File.separator + dFile.getPath(), dFile.getBody());
            }
        }
    }

    void copy(String src, String dst) {
        src.trim(); dst.trim();
        File dstFolder = new File(dst).getParentFile();
        dstFolder.mkdirs();
        try (FileInputStream is = new FileInputStream(src);
             FileOutputStream os = new FileOutputStream(dst)){
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            os.write(buffer);
            os.flush();
        }
        catch (IOException e) {
            e.printStackTrace();
            Main.out.println("Error during copying file...");
        }
    }

    static void zipExtract(String src, String dst) {
        try (ZipInputStream zip = new ZipInputStream(new FileInputStream(src))) {
            ZipEntry zipEntry;
            while ((zipEntry = zip.getNextEntry()) != null) {
                File filePath = mergePath(dst, zipEntry.getName());  //fix path with merge
                if (!zipEntry.isDirectory()) {
                    filePath.getParentFile().mkdirs();
                    FileOutputStream fout = new FileOutputStream(filePath);
                    int len;
                    byte[] buffer = new byte[65536];
                    while ((len = zip.read(buffer)) != -1) fout.write(buffer, 0, len);
                    fout.flush();
                    fout.close();
                }
                else filePath.mkdirs();
                zip.closeEntry();
            }
        }catch (FileNotFoundException e) {
            Main.out.println("File not found!");
            if (Prefs.verbose_level == 0) e.printStackTrace();
        }
        catch (IOException e) {
            Main.out.println("Error during extracting zip file.");
            if (Prefs.verbose_level == 0) e.printStackTrace();
        }
    }

    static File mergePath(String dstFolder, String toMerge) {
        String[] dstTree;
        String[] srcTree;
        if (dstFolder.contains("/"))
            dstTree = dstFolder.split("/");
        else
            dstTree = dstFolder.split("\\\\");
        if (toMerge.contains("/"))
            srcTree = toMerge.split("/");
        else
            srcTree = toMerge.split("\\\\");
        List<String> fullTree = new ArrayList<>();
        fullTree.addAll(Arrays.asList(dstTree));
        fullTree.addAll(Arrays.asList(srcTree));
        StringBuilder sb = new StringBuilder();
        String prevStr = "";
        for (String str : fullTree) {
            if (str.equals(prevStr) || str.startsWith("smali") || str.equals("res"))
                continue;
            prevStr = str;
            sb.append(str).append(File.separator);
        }
        return new File(sb.toString());
    }

    static void deleteAll(File file) {
        if (file.isDirectory()) {
            if (Objects.requireNonNull(file.list()).length == 0) {
                file.delete();
            }
            else {
                for (File child : Objects.requireNonNull(file.listFiles())) {
                    deleteAll(child);
                }
                if (Objects.requireNonNull(file.list()).length == 0)
                    file.delete();
            }
        }
        else
            file.delete();
    }

    static void loadProjectFiles(boolean xmlNeeded, boolean smaliNeeded) {
        if (!Prefs.projectPath.equals(currentProjectPathCached) || (xmlNeeded && smaliNeeded)) {
            //other project (multiple patching available on pc) or empty files arrays
            ProcessRule.smaliList.clear();
            ProcessRule.xmlList.clear();
            scanProject(xmlNeeded, smaliNeeded);
            currentProjectPathCached = Prefs.projectPath;
        }
        else if (smaliNeeded || xmlNeeded)      //new patch requires smali or xml
            scanProject(xmlNeeded, smaliNeeded);
    }

    private static void scanProject(boolean xmlNeeded, boolean smaliNeeded) {
        long startTime = System.currentTimeMillis();
        List<File> folders = new ArrayList<>();

        if (smaliNeeded) {         //add smali folders
            for (File i : Objects.requireNonNull(new File(Prefs.projectPath).listFiles())) {
                String str = i.toString().replace(Prefs.projectPath + File.separator, "");
                if (str.startsWith("smali"))
                    folders.add(i);
            }
            if (folders.isEmpty())
                Main.out.println("WARNING: no smali folders found inside the project folder \"" + Regex.getEndOfPath(Prefs.projectPath) + '\"');
        }

        if (xmlNeeded) {
            File resFolder = new File(Prefs.projectPath + File.separator + "res");  //add res folders and manifest
            if (new File(Prefs.projectPath + File.separator + "AndroidManifest.xml").exists()) {  //AndroidManifest.xml
                DecompiledFile manifest = new DecompiledFile(true, "AndroidManifest.xml");
                ProcessRule.xmlList.add(manifest);
            }
            if (!resFolder.exists() || Objects.requireNonNull(resFolder.list()).length == 0)
                Main.out.println("WARNING: no resources found inside the res folder.");
            else
                folders.add(resFolder);
        }

        List<Callable<Boolean>> tasks = new ArrayList<>();
        ArrayList<DecompiledFile> bigSmaliList = new ArrayList<>();
        ArrayList<DecompiledFile> bigXmlList = new ArrayList<>();
        for (File folder : folders) {                   //scan res & smali folders
            Callable<Boolean> r = () -> {
                Stack<File> stack = new Stack<>();
                stack.push(folder);
                while (!stack.isEmpty()) {
                    for (File file : Objects.requireNonNull(stack.pop().listFiles())) {
                        if (file.isDirectory())
                            stack.push(file);
                        else {
                            String path = file.toString().replace(Prefs.projectPath + File.separator, "");
                            boolean doNotSkip = true;
                            if (Prefs.skipSomeSmaliFiles) {                  //skip some folders
                                for (String str : Prefs.smaliFoldersToSkip)
                                    if (path.contains(File.separator + str + File.separator)) {
                                        doNotSkip = false;
                                        break;
                                    }
                            }
                            DecompiledFile tmp;
                            boolean isSmali = path.endsWith(".smali");
                            if (doNotSkip && (isSmali || path.endsWith(".xml"))) {
                                if (isSmali)
                                    tmp = new DecompiledFile(false, path);
                                else
                                    tmp = new DecompiledFile(true, path);
                                boolean isBigSize = file.length() > 100000;
                                synchronized (folders) {
                                    if (isSmali) {
                                        if (isBigSize)
                                            bigSmaliList.add(tmp);
                                        else
                                            ProcessRule.smaliList.add(tmp);
                                    }
                                    else {
                                        if (isBigSize)
                                            bigXmlList.add(tmp);
                                        else
                                            ProcessRule.xmlList.add(tmp);
                                    }
                                }
                            }
                        }
                    }
                }
                return true;
            };
            tasks.add(r);
        }
        try {
            BackgroundWorker.executor.invokeAll(tasks);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //moving big files to the start to load more cores at the end
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
                    dFile.setBody(read(Prefs.projectPath + File.separator + dFile.getPath()));
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
                    dFile.setBody(read(Prefs.projectPath + File.separator + dFile.getPath()));
                };
                BackgroundWorker.executor.submit(r);
            }

            BackgroundWorker.executor.shutdown();
            try {
                BackgroundWorker.executor.awaitTermination(60, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(0);
            }
        }
        Main.out.println(ProcessRule.smaliList.size() + " smali & " + ProcessRule.xmlList.size() + " xml files found in " + (System.currentTimeMillis() - startTime) + "ms.");
    }

    void scanFolder(String projectPath, String folder) {
        File targetFolder = new File(folder);
        Stack<File> stack = new Stack<>();
        stack.add(targetFolder);
        while (!stack.isEmpty()) {
            if (stack.peek().isDirectory()) {
                for (File file : Objects.requireNonNull(stack.pop().listFiles())) {
                    if (file.isDirectory()) {
                        stack.push(file);
                        continue;
                    }
                    scanFile(projectPath, file);
                }
            }
            else scanFile(projectPath, stack.pop());
        }
    }

    private void scanFile(String projectPath, File file) {
        String fullPath = projectPath + File.separator + file.toString().replace(projectPath + File.separator, "");
        if (fullPath.endsWith(".smali") | fullPath.endsWith(".xml")) {
            String shortPath = fullPath.replace(projectPath + File.separator, "");
            boolean isXml = shortPath.endsWith(".xml");
            DecompiledFile addedFile = new DecompiledFile(isXml, shortPath);
            if ((isXml && Prefs.keepXmlFilesInRAM) || (!isXml && Prefs.keepSmaliFilesInRAM))
                addedFile.setBody(read(fullPath));
            removeLoadedFile(shortPath);
            if (isXml)
                ProcessRule.xmlList.add(addedFile);
            else
                ProcessRule.smaliList.add(addedFile);
            if (Prefs.verbose_level == 0) {
                Main.out.println(shortPath + " added.");
            }
        }
    }

    static void removeLoadedFile(String shortPath) {
        if (Prefs.verbose_level == 0) {
            Main.out.println(shortPath + " removed.");
        }
        boolean isXml = shortPath.endsWith("xml");
        int size;
        if (isXml)
            size = ProcessRule.xmlList.size();
        else
            size = ProcessRule.smaliList.size();

        for (int i = 0; i < size; i++) {
            if (isXml && ProcessRule.xmlList.get(i).getPath().contains(shortPath)) {
                ProcessRule.xmlList.remove(i);
                i--;
                size--;
            }
            else if (ProcessRule.smaliList.get(i).getPath().contains(shortPath)) {
                ProcessRule.smaliList.remove(i);
                i--;
                size--;
            }
        }
    }
}