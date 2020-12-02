package com.github.cregrant.smaliscissors.engine;

import java.util.ArrayList;

class Patch {
    ArrayList<Rule> rules = new ArrayList<>();
    int currentRuleNum = 0;
    boolean smaliNeeded = false;
    boolean xmlNeeded = false;

    public void addRule(Rule rule) {
        if (rule!=null)
            rules.add(rule);
    }

    public void setRuleName(String someName) {
        for (Rule r : rules) {
            if (r.name!=null && r.name.equalsIgnoreCase(someName))
                currentRuleNum = r.num;
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
}
