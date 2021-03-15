package com.github.cregrant.smaliscissors.structures;

import java.util.ArrayList;

public class Rule {
    public enum Type {
        MATCH_ASSIGN,
        MATCH_REPLACE,
        MATCH_GOTO,
        ADD_FILES,
        REMOVE_FILES,
        REMOVE_CODE,
        DUMMY,
        GOTO,
        EXECUTE_DEX
    }

    public int num;
    public Type type;
    public String source;
    public String match;
    public String target;
    public String replacement;
    public String ruleName;
    public String script;
    public String mainClass;
    public String entrance;
    public String param;
    public String goTo;
    public boolean isXml = false;
    public boolean isSmali = false;
    public boolean isRegex = false;
    public boolean extract = false;
    public ArrayList<String> targetArr;
    public ArrayList<String> assignments;
    public ArrayList<Rule> mergedRules = new ArrayList<>(1);

    public boolean checkRuleIntegrity() {
        switch (type) {
            case MATCH_ASSIGN:
                if (target == null || match == null || assignments == null)
                    return false;
                break;
            case ADD_FILES:
                if (target == null || source == null)
                    return false;
                break;
            case MATCH_REPLACE:
                if (target == null || match == null || replacement == null)
                    return false;
                break;
            case REMOVE_FILES:
                if (target==null && targetArr==null)
                    return false;
                break;
            case DUMMY:
                if (ruleName ==null)
                    return false;
                break;
            case EXECUTE_DEX:
                if (script == null || mainClass == null || entrance == null || param == null)
                    return false;
                break;
            case GOTO:
                if (goTo==null)
                    return false;
                break;
            case MATCH_GOTO:
                if (target == null || match == null || goTo == null)
                    return false;
                break;
        }
        return true;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Type:    ").append(type).append('\n');

        if (target != null) {
            sb.append("Target:    ").append(target).append('\n');
        }
        else {
            sb.append("Targets:\n");
            for (String target : targetArr) sb.append("    ").append(target).append("\n");
        }
        switch (type) {
            case MATCH_ASSIGN:
                sb
                        .append("Match:    ").append(match).append('\n')
                        .append("Regex:    ").append(isRegex).append('\n');
                if (assignments.size()==1)
                    sb.append("Assignment:    ").append(assignments.get(0)).append('\n');
                else {
                    sb.append("Assignments:\n");
                    for (String ass : assignments) sb.append("    ").append(ass).append("\n");
                }
                break;
            case ADD_FILES:
                sb
                        .append("Source:    ").append(source).append('\n')
                        .append("Extract:    ").append(extract).append('\n');
                break;
            case MATCH_REPLACE:
                sb
                        .append("Match:    ").append(match).append('\n')
                        .append("Regex:    ").append(isRegex).append('\n')
                        .append("Replacement:    ").append(replacement).append('\n');
                break;
            case DUMMY:
                sb.append(ruleName).append('\n');
                break;
            case EXECUTE_DEX:
                sb
                        .append("Script:    ").append(script).append('\n')
                        .append("DecompiledFile needed:    ").append(isSmali).append('\n')
                        .append("Main class:    ").append(mainClass).append('\n')
                        .append("Entrance:    ").append(entrance).append('\n')
                        .append("Param:    ").append(param).append('\n');
                break;
            case GOTO:
                sb
                        .append("Goto:    ").append(goTo).append('\n');
                break;
            case MATCH_GOTO:
                sb
                        .append("Match:    ").append(match).append('\n')
                        .append("Goto:    ").append(goTo).append('\n');
                break;
        }
        return sb.toString();
    }

    public boolean canBeMerged(Rule OtherRule) {
        return this.isXml==OtherRule.isXml
                && this.isSmali==OtherRule.isSmali
                && this.isRegex==OtherRule.isRegex;
    }
}
