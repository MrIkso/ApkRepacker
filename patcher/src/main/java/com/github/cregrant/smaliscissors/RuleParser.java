package com.github.cregrant.smaliscissors;

import com.github.cregrant.smaliscissors.Regex.MatchType;
import com.github.cregrant.smaliscissors.structures.Rule;

import java.util.ArrayList;
import java.util.regex.Pattern;

import static com.github.cregrant.smaliscissors.Regex.matchMultiLines;
import static com.github.cregrant.smaliscissors.Regex.matchSingleLine;

class RuleParser {
    static private final Pattern patSource = Pattern.compile("SOURCE:\\R(.+)");
    static private final Pattern patExtract = Pattern.compile("EXTRACT:\\R(.+)");
    @SuppressWarnings("RegExpRedundantEscape")
    static private final Pattern patAssignment = Pattern.compile("\\R(.+?=\\$\\{GROUP\\d{1,2}\\})");
    static private final Pattern patReplacement = Pattern.compile("REPLACE:\\R([\\S\\s]*?)\\R?\\[/MATCH_REPLACE]");
    static private final Pattern patTarget = Pattern.compile("TARGET:\\R\\s*?([\\s\\S]*?)\\R(?:(?:MATCH|EXTRACT|SOURCE):|\\[/)");
    static private final Pattern patMatch = Pattern.compile("MATCH:\\R(.+)");
    static private final Pattern patName = Pattern.compile("NAME:\\R(.+)");
    static private final Pattern patRegexEnabled = Pattern.compile("REGEX:\\R(.+)");
    static private final Pattern patScript = Pattern.compile("SCRIPT:\\R(.+)");
    static private final Pattern patIsSmaliNeeded = Pattern.compile("SMALI_NEEDED:\\R(.+)");
    static private final Pattern patMainClass = Pattern.compile("MAIN_CLASS:\\R(.+)");
    static private final Pattern patEntrance = Pattern.compile("ENTRANCE:\\R(.+)");
    static private final Pattern patParam = Pattern.compile("PARAM:\\R(.+)");
    static private final Pattern patGoto = Pattern.compile("GOTO:\\R(.+)");
    private Rule rule;
    private String patch;
    static private int num = 0;

    Rule parseRule(String patchStr) {
        rule = new Rule();
        patch = patchStr;
        rule.num = num;
        num++;
        try {
            rule.type = Rule.Type.valueOf(patch.substring(patch.indexOf('[')+1, patch.indexOf(']')));
        } catch (EnumConstantNotPresentException e) {
            Main.out.println("Error parsing type of rule №" + num + ". " + patch.substring(patch.indexOf('[')+1, patch.indexOf(']')));
            return null;
        }

        //noinspection EnhancedSwitchMigration
        switch (rule.type) {
            case MATCH_ASSIGN:
                assignRule();
                break;
            case ADD_FILES:
                addRule();
                break;
            case MATCH_REPLACE:
                matchRule();
                break;
            case REMOVE_FILES:
                removeFilesRule();
                break;
            case DUMMY:
                dummyRule();
                break;
            case EXECUTE_DEX:
                dexRule();
                break;
            case GOTO:
                gotoRule();
                break;
            case MATCH_GOTO:
                matchGotoRule();
                break;
            case REMOVE_CODE:
                removeCodeRule();
                break;
        }
        fixRegex();
        if (rule.ruleIntegrityPassed())
            return rule;
        else {
            Main.out.println("Error parsing rule №" + num);
            return null;
        }
    }

    private void matchRule() {
        rule.ruleName = parseString(patName);
        getTargets();
        rule.match = matchSingleLine(patMatch, patch);
        rule.replacement = matchSingleLine(patReplacement, patch);
        rule.isRegex = parseBoolean(patRegexEnabled);
    }

    private void assignRule() {
        rule.ruleName = parseString(patName);
        getTargets();
        rule.match = matchSingleLine(patMatch, patch);
        rule.isRegex = parseBoolean(patRegexEnabled);
        rule.assignments = matchMultiLines(patAssignment, patch, MatchType.Split);
    }

    private void addRule() {
        rule.ruleName = parseString(patName);
        rule.source = parseString(patSource);
        rule.extract = parseBoolean(patExtract);
        getTargets();
    }

    private void removeCodeRule() {
        rule.ruleName = parseString(patName);
        rule.isSmali = true;
        getTargets();
    }

    private void removeFilesRule() {
        rule.ruleName = parseString(patName);
        getTargets();
    }

    private void dummyRule() {
        rule.ruleName = parseString(patName);
    }

    private void dexRule() {
        rule.ruleName = parseString(patName);
        rule.script = parseString(patScript);
        rule.isSmali = parseBoolean(patIsSmaliNeeded);
        rule.mainClass = parseString(patMainClass);
        rule.entrance = parseString(patEntrance);
        rule.param = parseString(patParam);
    }

    private void gotoRule() {
        rule.ruleName = parseString(patName);
        rule.goTo = parseString(patGoto);
    }

    private void matchGotoRule() {
        rule.ruleName = parseString(patName);
        getTargets();
        rule.match = matchSingleLine(patMatch, patch);
        rule.isRegex = parseBoolean(patRegexEnabled);
        rule.goTo = parseString(patGoto);
    }

    private void getTargets() {
        ArrayList<String> targetsRaw = matchMultiLines(patTarget, patch, MatchType.SplitPath);
        String first = targetsRaw.get(0);
        if (first.endsWith("xml"))
            rule.isXml = true;
        else if (first.endsWith("smali"))
            rule.isSmali = true;

        if (targetsRaw.size() == 1) {
            rule.target = first;
            rule.targetArr = null;
        }
        else rule.targetArr = targetsRaw;
    }

    private String parseString(Pattern pattern) {       //removes some whitespace
        String text = matchSingleLine(pattern, patch);
        if (text!=null)
            return text.trim();
        else
            return null;
    }

    private boolean parseBoolean(Pattern pattern) {
        String text = matchSingleLine(pattern, patch);
        if (text!=null && text.trim().equalsIgnoreCase("true"))
            return true;
        else
            return false;
    }

    private void fixRegex() {   //add compatibility with non-ApkEditor xml style
        if (rule.match!=null)
            rule.match = rule.match.replace("\\n", "\\R");
        if (rule.isXml) {
            if (rule.match!=null)
                rule.match = rule.match.replace("><", ">\\s*?<").replace(" ", "\\s*?");
            if (rule.replacement!=null)
                rule.replacement = rule.replacement.replace("><", ">\n<");
        }
    }
}
