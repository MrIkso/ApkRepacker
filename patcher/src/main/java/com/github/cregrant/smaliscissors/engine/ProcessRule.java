package com.github.cregrant.smaliscissors.engine;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;


class ProcessRule {
    private static int patchedFilesNum;
    private static final Map<String, String> assignMap = new HashMap<>();

    @SuppressWarnings("RegExpRedundantEscape")
    static void matchReplace(Rule replaceRule) {
        ArrayList<Rule> mergedRules = new ArrayList<>();
        mergedRules.add(replaceRule);

        if (!replaceRule.mergedRules.isEmpty()) {
            mergedRules.addAll(replaceRule.mergedRules);
        }

        for (Rule rule : mergedRules) {
            applyAssign(rule);
            //important escape for android
            rule.replacement = rule.replacement.replaceAll("\\$\\{GROUP(\\d{1,2})\\}", "\\$$1");
        }
        BackgroundWorker.createIfTerminated();
        patchedFilesNum = 0;
        Object lock = new Object();

        ArrayList<DecompiledFile> files;
        if (replaceRule.isXml)
            files = Scan.xmlList;
        else
            files = Scan.smaliList;
        int totalNum = files.size();

        for (int num=0; num<totalNum; num++) {
            int finalNum = num;
            Runnable r = () -> {
                DecompiledFile dFile = files.get(finalNum);
                replace(dFile, mergedRules);
                if (dFile.isModified()) {
                    dFile.setModified(false);
                    if (Prefs.verbose_level == 0)
                        Main.out.println(dFile.getPath() + " patched.");
                    synchronized (lock) {
                        patchedFilesNum++;
                    }
                }
            };
            BackgroundWorker.executor.submit(r);
        }

        try {
            BackgroundWorker.executor.shutdown();
            BackgroundWorker.executor.awaitTermination(15, TimeUnit.MINUTES);
        } catch (Exception e) {
            Main.out.println(e.getMessage());
            System.exit(1);
        }

        if (Prefs.verbose_level <= 2) {
            if (replaceRule.isSmali)
                Main.out.println(patchedFilesNum + " smali files patched.");
            else
                Main.out.println(patchedFilesNum + " xml files patched.");
        }
    }

    private static void replace(DecompiledFile dFile, ArrayList<Rule> rules) {
        String smaliBody = null;
        String smaliBodyNew = null;
        for (Rule rule : rules) {
            if (dFile.getPath().matches(rule.target)) {
                if (smaliBody==null)
                    smaliBody = dFile.getBody();  //loading file body for a first time
                else
                    smaliBody = smaliBodyNew;     //get body from RAM

                if (rule.isRegex)
                    smaliBodyNew = smaliBody.replaceAll(rule.match, rule.replacement);
                else
                    smaliBodyNew = smaliBody.replace(rule.match, rule.replacement);

                if (!smaliBodyNew.equals(smaliBody)) {
                    dFile.setModified(true);
                }
            }
        }
        if (dFile.isModified())
            dFile.setBody(smaliBodyNew);
    }

    static void assign(Rule rule) {
        ArrayList<String> assignArr = new ArrayList<>();
        DecompiledFile dFile;
        int end;
        if (rule.isXml)
            end = Scan.xmlList.size();
        else
            end = Scan.smaliList.size();
        for (int k=0; k<end; k++) {
            if (rule.isXml)
                dFile = Scan.xmlList.get(k);
            else dFile = Scan.smaliList.get(k);
            if (!dFile.getPath().matches(rule.target))
                continue;
            for (String variable : rule.assignments) {
                for (String str : variable.split("=")) {
                    if (str.contains("${GROUP"))
                        continue;
                    assignArr.add(str);
                }
            }
            ArrayList<String> valuesArr = Regex.matchMultiLines(Pattern.compile(rule.match), dFile.getBody(), "replace");
            if (assignArr.size() > valuesArr.size())
                Main.out.println("WARNING: MATCH_ASSIGN found multiple results...");
            else if (assignArr.size() < valuesArr.size())
                Main.out.println("WARNING: MATCH_ASSIGN not found enough results...");
            for (int j = 0; j < assignArr.size(); ++j) {
                String value = valuesArr.get(j);
                assignMap.put(assignArr.get(j), value);
                if (Prefs.verbose_level <= 1) {
                    if (value.length()>500)
                        value = value.substring(0, 60) + " ... " + value.substring(value.length()-60);
                    Main.out.println("assigned \"" + value + "\" to \"" + assignArr.get(j) + "\"");
                }
            }
        }
        if (assignMap.isEmpty()) {
            Main.out.println("Nothing found in assign rule??");
        }
    }

    static void add(Rule rule) {
        String src = Prefs.tempDir + File.separator + rule.source;
        String dst = Prefs.projectPath + File.separator + rule.target;
        if (rule.extract)
            IO.zipExtract(src, dst);
        else
            IO.copy(src, dst);

        BackgroundWorker.createIfTerminated();
        Iterable<DecompiledFile> newFiles = Scan.scanFolder(new File(Prefs.projectPath + File.separator + rule.target));
        try {
            BackgroundWorker.executor.shutdown();
            BackgroundWorker.executor.awaitTermination(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (DecompiledFile df : newFiles) {
            if (df.isXML()) {
                Scan.removeLoadedFile(df.getPath());
                Scan.xmlList.add(df);
            }
            else {
                Scan.removeLoadedFile(df.getPath());
                Scan.smaliList.add(df);
            }
        }
    }

    static void remove(Rule rule) {
        if (rule.target!=null) {
            IO.deleteAll(new File(Prefs.projectPath + File.separator + rule.target));
            Scan.removeLoadedFile(rule.target);
        }
        else {
            for (String target : rule.targetArr) {
                IO.deleteAll(new File(Prefs.projectPath + File.separator + target));
                Scan.removeLoadedFile(target);
            }
        }
    }

    static void matchGoto(Rule rule, Patch patch) {
        applyAssign(rule);
        AtomicBoolean running = new AtomicBoolean(true);
        Pattern pattern = Pattern.compile(rule.match);
        BackgroundWorker.createIfTerminated();

        List<DecompiledFile> files = rule.isXml ? Scan.xmlList : Scan.smaliList;
        int totalNum = files.size();
        try {
            for (int num=0; num<totalNum; num++) {
                int finalNum = num;
                Runnable r = () -> {
                    if (running.get()) {
                        String body = files.get(finalNum).getBody();
                        if (Regex.matchSingleLine(pattern, body) != null) {
                            patch.setRuleName(rule.goTo);
                            running.set(false);
                            Main.out.println("Match found!");
                        }
                    }
                };
                BackgroundWorker.executor.submit(r);
            }
            BackgroundWorker.executor.shutdown();
            BackgroundWorker.executor.awaitTermination(15, TimeUnit.MINUTES);
        } catch (Exception e) {
            Main.out.println(e.getMessage());
            System.exit(1);
        }
    }

    private static void applyAssign(Rule rule) {        //replacing ${GROUP} to some text
        if (!assignMap.isEmpty()) {
            Set<Map.Entry<String, String>> set = assignMap.entrySet();
            if (Prefs.verbose_level == 0)
                Main.out.println("Replacing variables to text:\n" + set);

            for (Map.Entry<String, String> entry : set) {
                String key = "${" + entry.getKey() + "}";
                boolean foundInMatch = rule.match.contains(key);
                boolean foundInReplacement = rule.replacement.contains(key);  //fixme
                if (foundInMatch || foundInReplacement) {
                    String value = entry.getValue();
                    if (Prefs.verbose_level == 0)
                        Main.out.println(key + " -> " + value);
                    if (foundInMatch)
                        rule.match = rule.match.replace(key, value);
                    else
                        rule.replacement = rule.replacement.replace(key, value);
                }
            }
        }
    }

    public static void dex(Rule rule) {
        String apkPath = Scan.getApkPath();
        if (apkPath == null)
            Main.out.println("ERROR: apk file not found.");
        String zipPath = Prefs.patchesDir + File.separator + Prefs.zipName;
        String projectPath = Prefs.projectPath;
        String dexPath = Prefs.tempDir + File.separator + rule.script;
        Main.dex.runDex(dexPath, rule.entrance, rule.mainClass, apkPath, zipPath, projectPath, rule.param, Prefs.tempDir.toString());
    }
}