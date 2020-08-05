package com.mrikso.apkrepacker.task;

import android.os.AsyncTask;

import com.mrikso.apkrepacker.activity.IdeActivity;
import com.jecelyin.common.utils.UIUtils;
import com.mrikso.apkrepacker.App;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.utils.FileUtil;
import com.mrikso.apkrepacker.utils.Smali2Java;

import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenSource;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;
import org.jf.dexlib2.writer.builder.DexBuilder;
import org.jf.smali.LexerErrorInterface;
import org.jf.smali.SmaliOptions;
import org.jf.smali.smaliFlexLexer;
import org.jf.smali.smaliParser;
import org.jf.smali.smaliTreeWalker;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class Smali2JavaTask extends AsyncTask<File, CharSequence, Boolean> {
    private IdeActivity editorActivity;
    private String javaCode;
    private File smali;

    public Smali2JavaTask(IdeActivity editorActivity) {
        this.editorActivity = editorActivity;
    }

    private static boolean assembleSmaliFile(File smaliFile, DexBuilder dexBuilder, SmaliOptions options)
            throws Exception {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(smaliFile);
            InputStreamReader reader = new InputStreamReader(fis, "UTF-8");

            LexerErrorInterface lexer = new smaliFlexLexer(reader, options.apiLevel);
            ((smaliFlexLexer) lexer).setSourceFile(smaliFile);
            CommonTokenStream tokens = new CommonTokenStream((TokenSource) lexer);

            if (options.printTokens) {
                tokens.getTokens();

                for (int i = 0; i < tokens.size(); i++) {
                    Token token = tokens.get(i);
                    if (token.getChannel() == smaliParser.HIDDEN) {
                        continue;
                    }

                    String tokenName;
                    if (token.getType() == -1) {
                        tokenName = "EOF";
                    } else {
                        tokenName = smaliParser.tokenNames[token.getType()];
                    }
                    System.out.println(tokenName + ": " + token.getText());
                }

                System.out.flush();
            }

            smaliParser parser = new smaliParser(tokens);
            parser.setVerboseErrors(options.verboseErrors);
            parser.setAllowOdex(options.allowOdexOpcodes);
            parser.setApiLevel(options.apiLevel);

            smaliParser.smali_file_return result = parser.smali_file();

            if (parser.getNumberOfSyntaxErrors() > 0 || lexer.getNumberOfSyntaxErrors() > 0) {
                return false;
            }

            CommonTree t = result.getTree();

            CommonTreeNodeStream treeStream = new CommonTreeNodeStream(t);
            treeStream.setTokenStream(tokens);

            if (options.printTokens) {
                System.out.println(t.toStringTree());
            }

            smaliTreeWalker dexGen = new smaliTreeWalker(treeStream);
            dexGen.setApiLevel(options.apiLevel);

            dexGen.setVerboseErrors(options.verboseErrors);
            dexGen.setDexBuilder(dexBuilder);
            dexGen.smali_file();

            return dexGen.getNumberOfSyntaxErrors() == 0;
        } finally {
            if (fis != null) {
                fis.close();
            }
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        editorActivity.openJavaText(javaCode, smali.getName()/*FileUtil.getNameVithoutExt(smali)*/);
        //dialog.hideProgress();
        if (!result)
            UIUtils.toast(App.getContext(), R.string.toast_error_decompile_smali_to_java);

    }

    @Override
    protected Boolean doInBackground(File... files) {
        boolean success = true;
        for (File file : files) {
            if (!process(file))
                success = false;
        }
        return success;
    }

    private boolean process(File smali) {
        try {
            this.smali = smali;
           // DexBuilder dexBuilder = new DexBuilder(Opcodes.getDefault());
          //  assembleSmaliFile(smali, dexBuilder, new SmaliOptions());
            javaCode = Smali2Java.translate(smali);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
