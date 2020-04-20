package com.mrikso.apkrepacker.patchengine;

import android.util.Log;

import java.util.List;
import java.util.zip.ZipFile;

public class PatchRuleExecutor extends Thread {

    private List rulesList;
    private ZipFile patch;
    private ParsePatch parsePatch;

    PatchRuleExecutor(ParsePatch parsePatch, List list, ZipFile zipFile) {
        this.parsePatch = parsePatch;
        this.rulesList = list;
        this.patch = zipFile;
    }

    @Override
    public void run() {
        int i = 0;
        while (i < this.rulesList.size()) {
            Core core = (Core) this.rulesList.get(i);
            Log.i("PatchRuleExecutor", "Start Patching");
            String currentRule = core.currentRule(this.patch);
            i = currentRule != null ? parsePatch.nextRule(this.rulesList, currentRule) : i + 1;
        }
        Log.i("PatchRuleExecutor", "Done Patching");
    }

}
