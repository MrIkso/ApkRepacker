package com.github.cregrant.smaliscissors.engine;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
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

    static void copy(String src, String dst) {
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
            Scan.scanProject(xmlNeeded, smaliNeeded);
            currentProjectPathCached = Prefs.projectPath;
        }
        else if (smaliNeeded || xmlNeeded)      //new patch requires smali or xml
            Scan.scanProject(xmlNeeded, smaliNeeded);
    }

}