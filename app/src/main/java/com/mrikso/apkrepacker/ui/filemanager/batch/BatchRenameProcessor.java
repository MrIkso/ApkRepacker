package com.mrikso.apkrepacker.ui.filemanager.batch;

import com.mrikso.apkrepacker.ui.filemanager.holder.FileHolder;

import java.util.regex.Pattern;

public class BatchRenameProcessor {
    private String mPattern;
    private VariableMatcher mVariableMatcher;
    private Pattern pattern;
    private String mReplace;

    public BatchRenameProcessor(String pattern, VariableConfig variableConfig) {
        this.mPattern = pattern;
        this.mVariableMatcher = new VariableMatcher(variableConfig);
    }

    public String process(FileHolder fileHolder) {
        int i;
        StringBuilder patternSb = new StringBuilder(this.mPattern);
        int i2 = 0;
        while (i2 < patternSb.length()) {
            char charAt = patternSb.charAt(i2);
            Variable variable = null;
            if (charAt == '%' && (i = i2 + 1) < patternSb.length()) {
                variable = this.mVariableMatcher.match(patternSb.substring(i, i2 + 2));
            } else if (charAt == '#') {
                variable = this.mVariableMatcher.match(patternSb.substring(i2, i2 + 1));
            }
            if (variable != null) {
                int apply = variable.apply(patternSb, i2, fileHolder);
                if (apply == 0) {
                    patternSb.delete(i2, i2 + 2);
                } else {
                    i2 += apply - 1;
                }
            }
            i2++;
        }
        if (this.pattern == null) {
            return patternSb.toString();
        }
        return this.pattern.matcher(patternSb.length() == 0 ? fileHolder.getName() : patternSb.toString()).replaceAll(this.mReplace);
    }

    public void replaceText(String replaceText, String replaceWith, boolean useRegex) {
        if (useRegex) {
            this.pattern = Pattern.compile(replaceText);
        } else {
            this.pattern = Pattern.compile(Pattern.quote(replaceText));
        }
        this.mReplace = replaceWith;
    }
}
