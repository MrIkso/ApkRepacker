package com.mrikso.apkrepacker.autotranslator.dictionary;

import com.jecelyin.common.utils.IOUtils;
import com.mrikso.apkrepacker.autotranslator.translator.TranslateItem;

import java.io.File;
import java.util.List;

public class DictionaryWriter {
    private static final String LINE_SEPARATOR_WIN = "\r\n";
    private File mDictionaryFile;

    public DictionaryWriter(File file) {
        mDictionaryFile = file;
    }

    public void writeDictionary(List<TranslateItem> translateItems) {
        new Thread(() -> {
            StringBuilder sb = new StringBuilder(1024);
            for (TranslateItem translateItem : translateItems) {
                String originValue = translateItem.originValue;
                String translatedValue = translateItem.translatedValue;
                if (!(originValue == null || translatedValue == null || originValue.equals(translatedValue))) {
                    sb.append('{');
                    sb.append(LINE_SEPARATOR_WIN);
                    sb.append("  \"");
                    sb.append(eolValue(originValue));
                    sb.append('\"');
                    sb.append(LINE_SEPARATOR_WIN);
                    sb.append("  \"");
                    sb.append(eolValue(translatedValue));
                    sb.append('\"');
                    sb.append(LINE_SEPARATOR_WIN);
                    sb.append('}');
                    sb.append(LINE_SEPARATOR_WIN);
                }
            }
            IOUtils.writeFile(mDictionaryFile, sb.toString());
        }).start();
    }

    private String eolValue(String value) {
        return value.replace("\\", "\\\\").replace("\f", "\\f")
                .replace("\n", "\\n").replace("\r", "\\r")
                .replace("\t", "\\t").replace("\b", "\\b")
                .replace("\"", "\\\"");
    }
}
