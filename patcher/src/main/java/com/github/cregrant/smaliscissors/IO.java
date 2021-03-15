package com.github.cregrant.smaliscissors;

import com.github.cregrant.smaliscissors.structures.DecompiledFile;
import com.github.cregrant.smaliscissors.structures.Patch;
import com.github.cregrant.smaliscissors.structures.Rule;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class IO {

    private static String loadedProject = "";
    private static final Pattern patRule = Pattern.compile("(\\[.+?](?:\\R(?:NAME|GOTO|SOURCE|SCRIPT|TARGET):)[\\s\\S]*?\\[/.+?])");

    static Patch loadRules(String zipFile) {
        delete(Prefs.tempDir);
        //noinspection ResultOfMethodCallIgnored
        Prefs.tempDir.mkdirs();
        zipExtract(zipFile, Prefs.tempDir.toString());
        Patch patch = new Patch();
        String txtFile = Prefs.tempDir + "/patch.txt";
        if (!new File(txtFile).exists()) {
            Main.out.println("No patch.txt file in patch!");
            return patch;
        }

        RuleParser parser = new RuleParser();
        ArrayList<String> tempArr = Regex.matchMultiLines(Objects.requireNonNull(patRule), read(txtFile), Regex.MatchType.Full);
        ArrayList<Rule> rawRulesArr = new ArrayList<>(tempArr.size());

        for (String singleRule : tempArr) {
            rawRulesArr.add(parser.parseRule(singleRule));
        }

        for (int i = 0; i < rawRulesArr.size(); i++) {
            Rule rule = rawRulesArr.get(i);
            int next = i + 1;
            while (true) {
                if (next < rawRulesArr.size() && Prefs.optimizeRules) {  //check if next rule exists and optimization enabled
                    Rule nextRule = rawRulesArr.get(next);
                    if ((nextRule.type == Rule.Type.MATCH_REPLACE) && rule.canBeMerged(nextRule)) {   //check for next rule type
                        rule.mergedRules.add(nextRule);
                        next++;
                        i++;
                    }
                    else
                        break;
                }
                else
                    break;
            }
            if (rule.isSmali)
                patch.smaliNeeded = true;
            else if (rule.isXml)
                patch.xmlNeeded = true;
            patch.addRule(rule);
        }

        if (Prefs.optimizeRules && (rawRulesArr.size()!=patch.getRulesCount()))
            Main.out.println(rawRulesArr.size() + " rules " + "shrunk to " + patch.getRulesCount() + ".\n");
        else
            Main.out.println(rawRulesArr.size() + " rules found.\n");

        return patch;
    }

    public static String read(String path) {
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
        } catch (FileNotFoundException e) {
            Main.out.println(path + ':' + " not found.");
            return "";
        } catch (IOException e) {
            e.printStackTrace();
            Main.out.println("Exiting to prevent file corruption");
            System.exit(1);
        }
        return resultString;
    }

    public static void write(String path, String content) {
        delete(new File(path));
        try {
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
            for (int j = 0; j < Scan.smaliList.size(); ++j) {
                DecompiledFile tmpSmali = Scan.smaliList.get(j);
                if (!tmpSmali.isModified()) continue;
                tmpSmali.setModified(false);
                write(Prefs.projectPath + File.separator + tmpSmali.getPath(), tmpSmali.getBody());
            }
        }
        if (Prefs.keepXmlFilesInRAM) {
            for (int j = 0; j < Scan.xmlList.size(); ++j) {
                DecompiledFile dFile = Scan.xmlList.get(j);
                if (!dFile.isModified()) continue;
                dFile.setModified(false);
                write(Prefs.projectPath + File.separator + dFile.getPath(), dFile.getBody());
            }
        }
    }

    static void copy(String src, String dst) {
        File dstFile = new File(dst);
        if (dstFile.exists())
            dstFile.delete();
        src = src.trim(); dst = dst.trim();
        File dstFolder = dstFile.getParentFile();
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
                if (zipEntry.isDirectory())
                    filePath.mkdirs();
                else {  //file
                    filePath.getParentFile().mkdirs();
                    FileOutputStream fout = new FileOutputStream(filePath);
                    int len;
                    byte[] buffer = new byte[65536];
                    while ((len = zip.read(buffer)) != -1) fout.write(buffer, 0, len);
                    fout.flush();
                    fout.close();
                }
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

    private static File mergePath(String dstFolder, String toMerge) {
        String[] dstTree = dstFolder.replace('\\', '/').split("/");
        String[] srcTree = toMerge.replace('\\', '/').split("/");
        Collection<String> fullTree = new ArrayList<>();
        fullTree.addAll(Arrays.asList(dstTree));
        fullTree.addAll(Arrays.asList(srcTree));
        StringBuilder sb = new StringBuilder();
        String prevStr = "";
        for (String str : fullTree) {
            if (str.equals(prevStr))
                continue;
            sb.append(str).append(File.separator);
            prevStr = str;
        }
        return new File(sb.toString());
    }

    static void delete(File file) {
        if (file.isDirectory()) {
            if (Objects.requireNonNull(file.list()).length == 0) {
                file.delete();
            }
            else {
                for (File child : Objects.requireNonNull(file.listFiles())) {
                    delete(child);
                }
                if (Objects.requireNonNull(file.list()).length == 0)
                    file.delete();
            }
        }
        else
            file.delete();
    }

    static void loadProjectFiles(boolean xmlNeeded, boolean smaliNeeded) {
        if (Prefs.projectPath.equals(loadedProject) && (xmlNeeded || smaliNeeded)) {
            //new patch requires smali or xml
            Scan.scanProject(xmlNeeded, smaliNeeded);
        }
        else if (smaliNeeded || xmlNeeded) {
            //other project (multiple patching available on pc) or empty files arrays
            Scan.scanProject(xmlNeeded, smaliNeeded);
            loadedProject = Prefs.projectPath;
        }
    }
}