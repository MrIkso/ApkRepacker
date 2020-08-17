package com.mrikso.patchengine;

import com.mrikso.apkrepacker.R;
import com.mrikso.patchengine.interfaces.IPatchContext;
import com.mrikso.patchengine.rules.PatchRuleAddFiles;
import com.mrikso.patchengine.rules.PatchRuleDummy;
import com.mrikso.patchengine.rules.PatchRuleExecDex;
import com.mrikso.patchengine.rules.PatchRuleFuncReplace;
import com.mrikso.patchengine.rules.PatchRuleGoto;
import com.mrikso.patchengine.rules.PatchRuleMatchAssign;
import com.mrikso.patchengine.rules.PatchRuleMatchGoto;
import com.mrikso.patchengine.rules.PatchRuleMatchReplace;
import com.mrikso.patchengine.rules.PatchRuleMerge;
import com.mrikso.patchengine.rules.PatchRuleRemoveFiles;
import com.mrikso.patchengine.rules.PatchRuleReviseSig;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class PatchParser {

    //правила в patch.txt
    private static final String ADD_FILES = "[ADD_FILES]";
    private static final String AUTHOR = "[AUTHOR]";
    private static final String DUMMY = "[DUMMY]";
    private static final String EXECUTE_DEX = "[EXECUTE_DEX]";
    private static final String FUNCTION_REPLACE = "[FUNCTION_REPLACE]";
    private static final String GOTO = "[GOTO]";
    private static final String MATCH_ASSIGN = "[MATCH_ASSIGN]";
    private static final String MATCH_GOTO = "[MATCH_GOTO]";
    private static final String MATCH_REPLACE = "[MATCH_REPLACE]";
    private static final String MERGE = "[MERGE]";
    private static final String MIN_ENGINE_VER = "[MIN_ENGINE_VER]";
    private static final String PACKAGE = "[PACKAGE]";
    private static final String REMOVE_FILES = "[REMOVE_FILES]";
    private static final String SIGNATURE_REVISE = "[SIGNATURE_REVISE]";

    //парсим patch.txt
    public static Patch parse(InputStream input, IPatchContext logger) throws Exception {
        logger.info(R.string.patch_start_parse, true);
        Patch result = new Patch();
        LinedReader br = new LinedReader(new InputStreamReader(input));
        for (String line = br.readLine(); line != null; line = br.readLine()) {
            String line2 = line.trim();
            if (line2.startsWith("[")) {
                switch (line2) {
                    case MIN_ENGINE_VER:
                        result.setRequiredEngine(Integer.parseInt(br.readLine()));
                        break;
                    case AUTHOR:
                        result.setAuthor(br.readLine());
                        break;
                    case PACKAGE:
                        result.setPackageName(br.readLine());
                        break;
                    default:
                        PatchRule rule = parseRule(br, line2, logger);
                        if (rule != null) {
                            result.setRule(rule);
                        }
                        break;
                }
            } else if (!line2.startsWith("#") && !"".equals(line2)) {
                logger.error(R.string.patch_error_unknown_rule, br.getCurrentLine(), line2);
            }
        }
        return result;
    }

    //запускаем парсинг правил внутри patch.txt
    private static PatchRule parseRule(LinedReader linedReader, String startLine, IPatchContext logger) throws IOException {
        PatchRule rule = null;
        switch (startLine) {
            case ADD_FILES:
                rule = new PatchRuleAddFiles();
                break;
            case REMOVE_FILES:
                rule = new PatchRuleRemoveFiles();
                break;
            case MERGE:
                rule = new PatchRuleMerge();
                break;
            case MATCH_REPLACE:
                rule = new PatchRuleMatchReplace();
                break;
            case MATCH_GOTO:
                rule = new PatchRuleMatchGoto();
                break;
            case MATCH_ASSIGN:
                rule = new PatchRuleMatchAssign();
                break;
            case FUNCTION_REPLACE:
                rule = new PatchRuleFuncReplace();
                break;
            case SIGNATURE_REVISE:
                rule = new PatchRuleReviseSig();
                break;
            case GOTO:
                rule = new PatchRuleGoto();
                break;
            case DUMMY:
                rule = new PatchRuleDummy();
                break;
            case EXECUTE_DEX:
                rule = new PatchRuleExecDex();
                break;
            default:
                logger.error(R.string.patch_error_unknown_rule, linedReader.getCurrentLine(), startLine);
                break;
        }
        if (rule != null) {
            rule.parseFrom(linedReader, logger);
        }
        return rule;
    }
}
