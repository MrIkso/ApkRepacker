package com.mrikso.apkrepacker.autotranslator.translator;

public class TranslateItem  {

    public String name;
    public String originValue;
    public String translatedValue;

    public TranslateItem(String name, String originValue) {
        this.name = name;
        this.originValue = originValue;
    }

    public TranslateItem(String name, String originValue, String translatedValue) {
        this.name = name;
        this.originValue = originValue;
        this.translatedValue = translatedValue;
    }
}
