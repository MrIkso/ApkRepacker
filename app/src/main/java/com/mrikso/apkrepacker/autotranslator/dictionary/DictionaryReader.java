package com.mrikso.apkrepacker.autotranslator.dictionary;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;

import static jadx.core.utils.files.FileUtils.close;

public class DictionaryReader {

    private HashMap<String, DictionaryItem> mDictionaryItemHashMap = new HashMap<>();
    private File mDictionaryFile;

    public DictionaryReader(File file) {
        mDictionaryFile = file;
    }

    public final void clear() {
        mDictionaryItemHashMap.clear();
    }

    public HashMap<String, DictionaryItem> getDictionaryMap() {
        return mDictionaryItemHashMap;
    }

    public final void addToMap(String original, String translated) {
        if (!original.equals(translated)) {
            DictionaryItem dictionaryItem = mDictionaryItemHashMap.get(original);
            if (dictionaryItem != null) {
                dictionaryItem.setTranslated(translated);
                return;
            }
            DictionaryItem dictionaryItem1 = new DictionaryItem(original, translated);
            mDictionaryItemHashMap.put(original, dictionaryItem1);
        }
    }

    public final void readDictionary() {
        clear();
        try {
            FileInputStream fileInputStream = new FileInputStream(mDictionaryFile);
            Scanner parserHelper = new Scanner(fileInputStream);
            while (true) {
                String stringLine = parserHelper.next_token();
                if (stringLine.length() != 0) {
                    Scanner.checkToken(stringLine, "{");
                    //parserHelper.checkToken("\"");
                    String original = parserHelper.next_token();
                    //лог для теста
                    //DLog.d(original);
                    //parserHelper.checkToken("\"");
                    String translated = parserHelper.next_token();
                    //лог для теста
                    //DLog.d(translated);
                    parserHelper.checkToken("}");
                    addToMap(original, translated);
                } else {
                    close(fileInputStream);
                    return;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
