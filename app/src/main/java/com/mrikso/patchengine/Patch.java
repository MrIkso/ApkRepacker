package com.mrikso.patchengine;

import java.util.ArrayList;
import java.util.List;

public class Patch {

    public static final int engineVersion = 2;
    public String author;
    public String packagename;
    public int requiredEngine;
    public List<PatchRule> rules = new ArrayList();

    public Patch(){

    }

    public String getAuthor() {
        return author;
    }

    public int getRequiredEngine() {
        return requiredEngine;
    }

    public String getPackageName() {
        return packagename;
    }

    public List<PatchRule> getRules() {
        return rules;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setPackageName(String packageName) {
        this.packagename = packageName;
    }

    public void setRequiredEngine(int requiredEngine) {
        this.requiredEngine = requiredEngine;
    }

    public void setRule(PatchRule rule) {
        this.rules.add(rule);
    }
}
