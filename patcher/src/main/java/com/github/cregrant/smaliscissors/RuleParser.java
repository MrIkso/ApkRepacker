package com.github.cregrant.smaliscissors;

import com.github.cregrant.smaliscissors.Regex.MatchType;
import com.github.cregrant.smaliscissors.structures.Rule;

import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Pattern;

import static com.github.cregrant.smaliscissors.Regex.matchMultiLines;
import static com.github.cregrant.smaliscissors.Regex.matchSingleLine;

class RuleParser {
    static private final Pattern patSource = Pattern.compile("SOURCE:\\R(.+)");
    static private final Pattern patExtract = Pattern.compile("EXTRACT:\\R\\s*?(.+)");
    @SuppressWarnings("RegExpRedundantEscape")
    static private final Pattern patAssignment = Pattern.compile("\\R\\s*?(.+?=\\$\\{GROUP\\d{1,2}\\})");
    static private final Pattern patReplacement = Pattern.compile("REPLACE:\\R([\\S\\s]*?)\\R?\\[/MATCH_REPLACE]");
    static private final Pattern patTarget = Pattern.compile("TARGET:\\R\\s*?([\\s\\S]*?)\\R(?:(?:MATCH|EXTRACT|SOURCE):|\\[/)");
    static private final Pattern patMatch = Pattern.compile("MATCH:\\R(.+)");
    static private final Pattern patName = Pattern.compile("NAME:\\R\\s*?(.+)");
    static private final Pattern patRegexEnabled = Pattern.compile("REGEX:\\R(.+)");
    static private final Pattern patScript = Pattern.compile("SCRIPT:\\R(.+)");
    static private final Pattern patIsSmaliNeeded = Pattern.compile("SMALI_NEEDED:\\R(.+)");
    static private final Pattern patMainClass = Pattern.compile("MAIN_CLASS:\\R(.+)");
    static private final Pattern patEntrance = Pattern.compile("ENTRANCE:\\R(.+)");
    static private final Pattern patParam = Pattern.compile("PARAM:\\R(.+)");
    static private final Pattern patGoto = Pattern.compile("GOTO:\\R\\s*?(.+)");
    private Rule rule;
    private String patch;
    static private int num = 0;

    Rule parseRule(String patchStr) {
        rule = new Rule();
        patch = patchStr;
        rule.num = num;
        num++;
        getType();
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
        if (rule.checkRuleIntegrity())
            return rule;
        else {
            Main.out.println("Error parsing rule " + num);
            return null;
        }
    }

    private void getType() {
        int k = 1;
        StringBuilder sb = new StringBuilder(17);
        char ch;
        while ((ch = patch.charAt(k))!=']') {
            sb.append(ch);
            k++;
        }
        rule.type = Rule.Type.valueOf(sb.toString());

    }

    private void matchRule() {
        rule.ruleName = matchSingleLine(patName, patch);
        getTargets();
        rule.match = matchSingleLine(patMatch, patch);
        rule.replacement = matchSingleLine(patReplacement, patch);
        rule.isRegex = Objects.requireNonNull(matchSingleLine(patRegexEnabled, patch)).trim().equalsIgnoreCase("true");
    }

    private void assignRule() {
        rule.ruleName = matchSingleLine(patName, patch);
        getTargets();
        rule.match = matchSingleLine(patMatch, patch);
        rule.isRegex = Objects.requireNonNull(matchSingleLine(patRegexEnabled, patch)).trim().equalsIgnoreCase("true");
        rule.assignments = matchMultiLines(patAssignment, patch, MatchType.Split);
    }

    private void addRule() {
        rule.ruleName = matchSingleLine(patName, patch);
        rule.source = matchSingleLine(patSource, patch);
        try {
            rule.extract = Objects.requireNonNull(matchSingleLine(patExtract, patch)).trim().equalsIgnoreCase("true");
        } catch (NullPointerException ignored) {}
        getTargets();
    }

    private void removeCodeRule() {
        rule.ruleName = matchSingleLine(patName, patch);
        rule.isSmali = true;
        getTargets();
    }

    private void removeFilesRule() {
        rule.ruleName = matchSingleLine(patName, patch);
        getTargets();
    }

    private void dummyRule() {
        rule.ruleName = matchSingleLine(patName, patch);
    }

    private void dexRule() {
        rule.ruleName = matchSingleLine(patName, patch);
        rule.script = Objects.requireNonNull(matchSingleLine(patScript, patch)).trim();
        rule.isSmali = Boolean.getBoolean(Objects.requireNonNull(matchSingleLine(patIsSmaliNeeded, patch)).trim());
        rule.mainClass = Objects.requireNonNull(matchSingleLine(patMainClass, patch)).trim();
        rule.entrance = Objects.requireNonNull(matchSingleLine(patEntrance, patch)).trim();
        rule.param = Objects.requireNonNull(matchSingleLine(patParam, patch)).trim();
    }

    private void gotoRule() {
        rule.ruleName = matchSingleLine(patName, patch);
        rule.goTo = matchSingleLine(patGoto, patch);
    }

    private void matchGotoRule() {
        rule.ruleName = matchSingleLine(patName, patch);
        getTargets();
        rule.match = matchSingleLine(patMatch, patch);
        rule.isRegex = Boolean.getBoolean(Objects.requireNonNull(matchSingleLine(patRegexEnabled, patch)).trim());
        rule.goTo = matchSingleLine(patGoto, patch);
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

    private void fixRegex() {
        if (rule.match!=null)
            rule.match = rule.match.replace("\\n", "\\R");  //compatibility with windows
        if (rule.isXml) {
            if (rule.match!=null)
                rule.match = rule.match.replace("><", ">\\s*?<").replace(" ", "\\s*?");
            if (rule.replacement!=null)
                rule.replacement = rule.replacement.replace("><", ">\n<");
        }
    }
}
