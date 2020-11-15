package com.github.cregrant.smaliscissors.engine;

import java.io.File;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

class ProcessRule {
    static ArrayList<DecompiledFile> smaliList = new ArrayList<>();
    static ArrayList<DecompiledFile> xmlList = new ArrayList<>();
    static int patchedFilesNum = 0;
    static HashMap<String, String> assignMap = new HashMap<>();

    @SuppressWarnings("RegExpRedundantEscape")
    static void matchReplace(Rule rule) {
        applyAssign(rule);
        //important escape for android
        rule.replacement = rule.replacement.replaceAll("\\$\\{GROUP(\\d{1,2})\\}", "\\$$1");
        Object lock = new Object();

        int totalNum;
        if (rule.isXml)
            totalNum = xmlList.size();
        else
            totalNum = smaliList.size();
        List<Callable<Boolean>> tasks = new ArrayList<>(totalNum);
        for (int num=0; num<totalNum; num++) {
            int finalNum = num;
            Callable<Boolean> r = () -> {
                DecompiledFile dFile;
                if (rule.isXml)
                    dFile = xmlList.get(finalNum);
                else
                    dFile = smaliList.get(finalNum);
                replace(dFile, rule);

                if (!dFile.isNotModified()) {
                    if (Prefs.verbose_level == 0) {
                        Main.out.println(dFile.getPath() + " patched.");
                    }
                    synchronized (lock) {
                        ++patchedFilesNum;
                    }
                }
                return null;
            };
            tasks.add(r);
        }
        try {
            BackgroundWorker.executor.invokeAll(tasks);
        } catch (Exception e) {
            Main.out.println(e.getMessage());
            System.exit(1);
        }


        if (Prefs.verbose_level <= 2) {
            if (rule.isSmali)
                Main.out.println(patchedFilesNum + " smali files patched.");
            else
                Main.out.println(patchedFilesNum + " xml files patched.");
        }
        patchedFilesNum = 0;
    }

    private static void replace(DecompiledFile dFile, Rule rule) {
        if (dFile.getPath().matches(rule.target)) {
            String smaliBody = dFile.getBody();
            String smaliBodyNew;
            if (rule.isRegex)
                smaliBodyNew = smaliBody.replaceAll(rule.match, rule.replacement);
            else
                smaliBodyNew = smaliBody.replace(rule.match, rule.replacement);
            if (!smaliBodyNew.equals(smaliBody)) {
                dFile.setBody(smaliBodyNew);
                dFile.setModified(true);
            }
        }
    }

    static void assign(Rule rule) {
        ArrayList<String> assignArr = new ArrayList<>();
        DecompiledFile dFile;
        int end;
        if (rule.isXml)
            end = xmlList.size();
        else end = smaliList.size();
        for (int k=0; k<end; k++) {
            if (rule.isXml)
                dFile = xmlList.get(k);
            else dFile = smaliList.get(k);
            if (!dFile.getPath().matches(rule.target)) continue;
            for (String variable : rule.assignments) {
                for (String str : variable.split("=")) {
                    if (str.contains("${GROUP")) continue;
                    assignArr.add(str);
                }
            }
            ArrayList<String> valuesArr = new Regex().matchMultiLines(Pattern.compile(rule.match), dFile.getBody(), "replace");
            for (int j = 0; j < valuesArr.size(); ++j) {
                if (Prefs.verbose_level <= 1) {
                    Main.out.println("assigned \"" + valuesArr.get(j) + "\" to \"" + assignArr.get(j) + "\"");
                }
                assignMap.put(assignArr.get(j), valuesArr.get(j));
            }
        }
        if (assignMap.isEmpty()) {
            Main.out.println("Nothing found in assign rule??");
        }
    }

    static void add(Rule rule) {
        String src = Prefs.tempDir + File.separator + rule.source;
        String dst = Prefs.projectPath + File.separator + rule.target;
        IO io = new IO();
        if (rule.extract) io.zipExtract(src, dst);
        else io.copy(src, dst);
        io.scanFolder(Prefs.projectPath, rule.target);
    }

    static void remove(Rule rule) {
        IO io = new IO();
        io.deleteAll(new File(Prefs.projectPath + File.separator + rule.target));
        io.removeLoadedFile(rule.target);
    }

    static void matchGoto(Rule rule, Patch patch) {
        applyAssign(rule);
        AtomicBoolean running = new AtomicBoolean(true);
        Regex regex = new Regex();
        Pattern pattern = Pattern.compile(rule.match);

        int totalNum;
        if (rule.isXml)
            totalNum = xmlList.size();
        else
            totalNum = smaliList.size();
        try {
            List<Callable<Boolean>> tasks = new ArrayList<>(totalNum);
            for (int num=0; num<totalNum; num++) {
                int finalNum = num;
                Callable<Boolean> r = () -> {
                    if (running.get()) {
                        DecompiledFile dFile;
                        if (rule.isXml)
                            dFile = xmlList.get(finalNum);
                        else
                            dFile = smaliList.get(finalNum);
                        if (regex.matchSingleLine(pattern, dFile.getBody()) != null) {
                            patch.setRuleName(rule.goTo);
                            running.set(false);
                        }
                    }
                    return null;
                };
                tasks.add(r);
            }
            BackgroundWorker.executor.invokeAll(tasks);
        } catch (Exception e) {
            Main.out.println(e.getMessage());
            System.exit(1);
        }
    }

    private static void applyAssign(Rule rule) {
        if (!assignMap.isEmpty()) {
            Set<Map.Entry<String, String>> set = assignMap.entrySet();      //replace ${GROUP}
            if (Prefs.verbose_level == 0) {
                Main.out.println("Replacing variables to text:\n" + set);
            }
            for (Map.Entry<String, String> entry : set) {
                String key = "${" + entry.getKey() + "}";
                boolean foundInMatch = rule.match.contains(key);
                boolean foundInReplacement = rule.replacement.contains(key);
                if (!foundInMatch && !foundInReplacement) continue;
                String value = entry.getValue();
                if (Prefs.verbose_level == 0) {
                    Main.out.println(key + " -> " + value);
                }
                if (foundInMatch)
                    rule.match = rule.match.replace(key, value);
                else rule.replacement = rule.replacement.replace(key, value);
            }
        }
    }

    public static void dex() {
        Main.out.println("HAHa very fun");
    }
}