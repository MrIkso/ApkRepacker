package com.mrikso.apkrepacker.ide.editor.lexer;


import com.mrikso.codeeditor.util.LexTask;
import com.mrikso.codeeditor.util.NonProgLexTask;

import org.antlr.v4.runtime.Vocabulary;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LexerUtil {

    public static boolean isText(String type) {
        return type.endsWith("json") ||
                type.endsWith("smali") ||
                type.endsWith("m") ||
                type.endsWith("mm") ||
                type.endsWith("xml") ||
                type.endsWith("html") ||
                type.endsWith("htm") ||
                type.endsWith("txt") ||
                type.endsWith("ini") ||
                type.endsWith("cfg") ||
                type.endsWith("prop") ||
                type.endsWith("js");
    }

    public static String[] getKeywords(Vocabulary tokens) {
        String keywordPattern = "'[a-z_]+'";
        int len = tokens.getMaxTokenType();
        List<String> keywords = new ArrayList<>(len);
        for (int i = 0; i < len; i++) {
            String name = tokens.getLiteralName(i);
            if (name != null && name.matches(keywordPattern))
                keywords.add(name.substring(1, name.length() - 1));
        }
        String[] keywordsArr = new String[keywords.size()];
        return keywords.toArray(keywordsArr);
    }

    public static LexTask createLexer(String name, File item) {
        if (name.endsWith(".java"))
            return new JavaLexTask();
        if (name.endsWith(".smali"))
            return new SmaliLexTask();
        if (name.endsWith(".c") ||
                name.endsWith(".h") ||
                name.endsWith(".cc") ||
                name.endsWith(".cpp") ||
                name.endsWith(".cxx"))
            return new CppLexTask();
        if (name.endsWith(".xml"))
            return new XmlLexTask();
        if (name.endsWith(".html") ||
                name.endsWith(".htm"))
            return new HtmlLexTask();
        if (name.endsWith(".json"))
            return new JsonLexTask();
        if (name.endsWith(".css"))
            return new CssLexTask();
        if (name.endsWith(".js"))
            return new JavascriptLexTask();
        return NonProgLexTask.instance;
    }
}
