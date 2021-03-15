package com.github.cregrant.smaliscissors.structures;

import java.util.ArrayList;

public class Patch {
    private final ArrayList<Rule> rules = new ArrayList<>();
    private int currentRuleNum = 0;
    public boolean smaliNeeded = false;
    public boolean xmlNeeded = false;

    public void addRule(Rule rule) {
        if (rule!=null)
            rules.add(rule);
    }

    public void setRuleName(String someName) {
        for (Rule r : rules) {
            if (r.ruleName !=null && r.ruleName.equalsIgnoreCase(someName)) {
                currentRuleNum = r.num;
                break;
            }
        }
    }

    public Rule getNextRule() {
        if (currentRuleNum < rules.size()) {
            Rule rule = rules.get(currentRuleNum);
            currentRuleNum++;
            return rule;
        }
        return null;
    }

    public int getRulesCount() {
        return rules.size();
    }
}
