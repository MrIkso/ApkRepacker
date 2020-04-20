package com.mrikso.apkrepacker.autotranslator.common;

import com.mrikso.apkrepacker.autotranslator.translator.TranslateItem;

import java.util.ArrayList;
import java.util.List;

public class TranslateStringsHelper {

    private static List<TranslateItem> defaultStrings = new ArrayList();
    private static List<TranslateItem> translatedStrings = new ArrayList();

    public static List<TranslateItem> getDefaultStrings(){
        return defaultStrings;
    }

    public static void setDefaultStrings(List<TranslateItem> list){
        defaultStrings = list;
    }

    public static List<TranslateItem> getTranslatedStrings(){
        return translatedStrings;
    }

    public static void setTranslatedStrings(List<TranslateItem> list){
        translatedStrings = list;
    }
}
