package com.github.cregrant.smaliscissors.engine;

import java.io.File;

import static java.lang.System.currentTimeMillis;

class Executor {
    static void executePatches(Iterable<String> zipArr) {
        long startTime = currentTimeMillis();
        for (String zipFile : zipArr) {
            Prefs.patchesDir = new File(zipFile).getParentFile();
            Prefs.tempDir = new File(Prefs.patchesDir + File.separator + "temp");
            Prefs.zipName = Regex.getEndOfPath(zipFile);
            Main.out.println("\nPatch - " + Prefs.zipName);

            Patch patch = IO.loadRules(zipFile);
            boolean scanXml = patch.xmlNeeded && Scan.xmlList.isEmpty();
            boolean scanSmali = patch.smaliNeeded && Scan.smaliList.isEmpty();
            IO.loadProjectFiles(scanXml, scanSmali);

            try {
                while (true) {
                    Rule rule = patch.getNextRule();
                    if (rule==null)
                        break;
                    preProcessRule(rule, patch);
                }
            } catch (Exception e) {
                Main.out.println("Executor error occurred: " + e.toString());
                e.printStackTrace();
            }

            if (Prefs.verbose_level == 0 && (Prefs.keepXmlFilesInRAM || Prefs.keepSmaliFilesInRAM))
                Main.out.println("Writing changes to disk...");
            IO.writeChanges();
            IO.deleteAll(Prefs.tempDir);
        }
        Main.out.println("------------------\n" + Regex.getEndOfPath(Prefs.projectPath) + " patched in " + (currentTimeMillis() - startTime) + "ms.");
    }

    private static void preProcessRule(Rule rule, Patch patch) {
        printRuleInfo(rule);
        //noinspection EnhancedSwitchMigration
        switch (rule.type) {
            case "MATCH_ASSIGN":
                ProcessRule.assign(rule);
                break;
            case "MATCH_REPLACE":
                ProcessRule.matchReplace(rule);
                break;
            case "ADD_FILES":
                ProcessRule.add(rule);
                break;
            case "REMOVE_FILES":
                ProcessRule.remove(rule);
                break;
            case "EXECUTE_DEX":
                Main.out.println("Executing dex...");
                ProcessRule.dex(rule);
                break;
            case "GOTO":
                patch.setRuleName(rule.goTo);
                break;
            case "MATCH_GOTO":
                ProcessRule.matchGoto(rule, patch);
                break;
        }
        Main.out.println("");
    }

    private static void printRuleInfo(Rule rule) {
        if (Prefs.verbose_level == 0)
            Main.out.println(rule.toString());
        else if (Prefs.verbose_level == 1) {
            Main.out.println("Type - " + rule.type);
            if (!rule.type.equals("EXECUTE_DEX") && !rule.type.equals("DUMMY") ) {

                if (rule.target != null)
                    Main.out.println("Target - " + rule.target);

                else if (rule.targetArr != null) {
                    Main.out.println("Targets:");
                    if (rule.targetArr.size() < 100) {
                        for (String target : rule.targetArr)
                            Main.out.println("    " + target);
                    }
                    else
                        Main.out.println("    " + rule.targetArr.size() + " items");
                }
            }
        }
    }
}