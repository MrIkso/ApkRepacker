package com.mrikso.apkrepacker.ui.stringlist;

import androidx.annotation.Nullable;

import java.io.File;
import java.net.URI;
import java.util.Locale;
import java.util.Objects;

public class StringFile extends File {

    /**
     * the lang for which we store this file
     */
    private String mLangCode;
    private Locale mLocale;
    /**
     * Creates a new File instance from a parent abstract pathname and a child pathname string.
     */
    public StringFile(File parent, String child, String la) {
        super(parent, child);
        mLangCode = la;
    }//CTOR

    /**
     * Creates a new File instance by converting the given pathname string into an abstract pathname.
     */
    public StringFile(String pathname, String la) {
        super(pathname);
        mLangCode = la;
        mLocale = buildLocaleFromStringName(la);
    }//CTOR

    /**
     * Creates a new File instance from a parent pathname string and a child pathname string.
     */
    public StringFile(String parent, String child, String la) {
        super(parent, child);
        mLangCode = la;
        mLocale = Objects.requireNonNull(buildLocaleFromStringName(la));
    }//CTOR

    /**
     * Creates a new File instance by converting the given file: URI into an abstract pathname.
     */
    public StringFile(URI uri, String la) {
        super(uri);
        mLangCode = la;
        mLocale = Objects.requireNonNull(buildLocaleFromStringName(la));
    }//CTOR

    /**
     * setter getter for the language
     */
    public void lang(String lang) {
        this.mLangCode = lang;
    }//public void lang(String lang)

    /**
     * getter
     */
    public String lang() {
        return mLangCode;
    }//public String lang()

    public Locale locale() {
        return mLocale;
    }

    @Nullable
    private static Locale buildLocaleFromStringName(String stringName) {
        if(stringName.equals("default")) {
            //return new Locale.Builder().setLanguage("default").build();
            return null;
        }
        else {
            /*int configPartIndex = stringName.lastIndexOf("-");
            if (configPartIndex == -1 || (configPartIndex != 0 && stringName.charAt(configPartIndex - 1) != '-'))
                return null;

            String localeTag = stringName.substring(configPartIndex + ("-".length()));*/
            return new Locale.Builder().setLanguageTag(stringName).build();
        }
    }

    public StringFile getParentFile() {
        File parent = super.getParentFile();
        return new StringFile(parent, "", mLangCode);
    }
}