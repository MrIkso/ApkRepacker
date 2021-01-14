package com.github.cregrant.smaliscissors.engine;

import java.util.ArrayList;

class Patch {
    private final ArrayList<Rule> rules = new ArrayList<>();
    private int currentRuleNum = 0;
    boolean smaliNeeded = false;
    boolean xmlNeeded = false;

    void addRule(Rule rule) {
        if (rule!=null)
            rules.add(rule);
    }

    void setRuleName(String someName) {
        for (Rule r : rules) {
            if (r.name!=null && r.name.equalsIgnoreCase(someName)) {
                currentRuleNum = r.num;
                break;
            }
        }
    }

    Rule getNextRule() {
        if (currentRuleNum < rules.size()) {
            Rule rule = rules.get(currentRuleNum);
            currentRuleNum++;
            return rule;
        }
        return null;
    }

    int getRulesCount() {
        return rules.size();
    }
}
