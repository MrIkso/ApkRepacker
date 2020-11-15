package com.github.cregrant.smaliscissors.engine;

import java.util.ArrayList;

class Rule {
    int num;
    String type;
    String source;
    String match;
    String target;
    String replacement;
    String name;
    String script;
    String mainClass;
    String entrance;
    String param;
    String goTo;
    boolean isXml = false;
    boolean isSmali = false;
    boolean isRegex = false;
    boolean extract = false;
    ArrayList<String> targetArr;
    ArrayList<String> assignments;

    boolean checkRuleIntegrity() {
        switch (type) {
            case "MATCH_ASSIGN":
                if (target==null | match==null | assignments==null)
                    return false;
                break;
            case "ADD_FILES":
                if (target==null | source==null)
                    return false;
                break;
            case "MATCH_REPLACE":
                if (target==null | match==null | replacement==null)
                    return false;
                break;
            case "REMOVE_FILES":
                if (target==null)
                    return false;
                break;
            case "DUMMY":
                if (name==null)
                    return false;
                break;
            case "EXECUTE_DEX":
                if (script==null | mainClass==null | entrance==null | param==null)
                    return false;
                break;
            case "GOTO":
                if (goTo==null)
                    return false;
                break;
            case "MATCH_GOTO":
                if (target==null | match==null | goTo==null)
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
            case "MATCH_ASSIGN":
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
            case "ADD_FILES":
                sb
                        .append("Source:    ").append(source).append('\n')
                        .append("Extract:    ").append(extract).append('\n');
                break;
            case "MATCH_REPLACE":
                sb
                        .append("Match:    ").append(match).append('\n')
                        .append("Regex:    ").append(isRegex).append('\n')
                        .append("Replacement:    ").append(replacement).append('\n');
                break;
            case "DUMMY":
                sb.append(name).append('\n');
                break;
            case "EXECUTE_DEX":
                sb
                        .append("Script:    ").append(script).append('\n')
                        .append("DecompiledFile needed:    ").append(isSmali).append('\n')
                        .append("Main class:    ").append(mainClass).append('\n')
                        .append("Entrance:    ").append(entrance).append('\n')
                        .append("Param:    ").append(param).append('\n');
                break;
            case "GOTO":
                sb
                        .append("Goto:    ").append(goTo).append('\n');
                break;
            case "MATCH_GOTO":
                sb
                        .append("Match:    ").append(match).append('\n')
                        .append("Goto:    ").append(goTo).append('\n');
                break;
        }
        return sb.toString();
    }
}
