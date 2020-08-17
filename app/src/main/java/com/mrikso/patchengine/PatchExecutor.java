package com.mrikso.patchengine;

import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.utils.common.DLog;
import com.mrikso.patchengine.interfaces.IPatchContext;
import com.mrikso.patchengine.interfaces.IRulesInfo;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class PatchExecutor {

    private ProjectHelper mProjectHelper;
    private IPatchContext mPatchContext;
    private IRulesInfo mRulesInfo;
    private Patch mPatch;
    private String mPatchPath;
    private ZipFile mSourceZip;

    public PatchExecutor(ProjectHelper projectHelper, String patchPath, IRulesInfo rulesInfo, IPatchContext logger) {
        mProjectHelper = projectHelper;
        mPatchPath = patchPath;
        mRulesInfo = rulesInfo;
        mPatchContext = logger;
    }

    //применяем патч
    public void applyPatch() {
        try {
            ZipFile zipFile = new ZipFile(mPatchPath);
            mSourceZip = zipFile;
            ZipEntry entry = zipFile.getEntry("patch.txt");
            if (entry == null) {
                mSourceZip.close();
                mSourceZip = null;
                mPatchContext.error(R.string.patch_error_no_entry, "patch.txt");
            }
            InputStream input = mSourceZip.getInputStream(entry);
            mPatch = PatchParser.parse(input, mPatchContext);
            input.close();
            /*
            boolean needToDecode = false;
            if (!mProjectHelper.smaliClicked()) {
                Iterator<PatchRule> it = patch.rules.iterator();
                while (it.hasNext() && !(needToDecode = it.next().isSmaliNeeded())) {
                    while (it.hasNext() && !(needToDecode = it.next().isSmaliNeeded())) {
                    }
                }
                if (needToDecode) {
                    patchContext.info(R.string.decode_dex_file, true, new Object[0]);
                    ((ApkInfoActivity) activityRef.get()).decodeDex(this);
                }
            }
            if (!needToDecode) {

             */
            applyRules(mPatch.getRules(), mSourceZip);
            // }
        } catch (Exception e) {
            mPatchContext.error(R.string.general_error, e.getMessage());
            e.printStackTrace();
        }
    }

   /* private void applyRules(final List<PatchRule> rules, final ZipFile sourceZip) {
        new Thread() {
            public void run() {
                int index = 0;
                while (index < rules.size()) {
                    PatchRule rule = rules.get(index);
                    PatchExecutor.this.mPatchContext.info(R.string.patch_start_apply, true, rule.startLine);
                    String nextRule = null;
                    if (rule.isValid(PatchExecutor.this.mPatchContext)) {
                        nextRule = rule.executeRule(PatchExecutor.this.mProjectHelper, sourceZip, PatchExecutor.this.mPatchContext);
                    }
                    if (nextRule != null) {
                        index = PatchExecutor.this.findTargetRule(rules, nextRule);
                    } else {
                        index++;
                    }
                }
                PatchExecutor.this.mPatchContext.info(R.string.all_rules_applied, true);
                PatchExecutor.this.mPatchContext.patchFinished();
            }
        }.start();
    }*/
    //применяем правила патча
    private void applyRules(final List<PatchRule> rules, final ZipFile sourceZip) {
        new Thread() {
            public void run() {
                int index = 0;
                int totalRules = rules.size();
                mRulesInfo.allRules(totalRules);
                while (index < rules.size()) {
                    PatchRule rule = rules.get(index);
                    mRulesInfo.currentRules(index);
                    mPatchContext.info(R.string.patch_start_apply, true, rule.startLine);
                    DLog.d("patchexecutor", totalRules + " "+ index);
                    String nextRule = null;
                    if (rule.isValid(mPatchContext)) {
                        nextRule = rule.executeRule(mProjectHelper, sourceZip, mPatchContext);
                    }
                    if (nextRule != null) {
                        index = findTargetRule(rules, nextRule);
                    } else {
                        index++;
                    }
                }
                mPatchContext.info(R.string.all_rules_applied, true);
                mPatchContext.patchFinished();
            }
        }.start();
    }

    public int findTargetRule(List<PatchRule> rules, String name) {
        for (int i = 0; i < rules.size(); i++) {
            if (name.equals(rules.get(i).getRuleName())) {
                return i;
            }
        }
        return -1;
    }

    public void callbackFunc() {
      //  Patch patch2 = mPatch;
        if (mPatch != null && mPatch.rules != null && mSourceZip != null) {
            applyRules(mPatch.rules, mSourceZip);
        }
    }

    //получаем список правил патча
    public List<String> getRuleNames() {
        List<String> names = new ArrayList<>();
        if (!(mPatch == null || mPatch.getRules() == null)) {
            for (PatchRule rule : mPatch.getRules()) {
                names.add(rule.getRuleName());
            }
        }
        return names;
    }
}
