package com.mrikso.apkrepacker.ui.stringlist;

import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DirectoryScanner {

    public ArrayList<StringFile> findStringFiles(String pathToLoad) {
        Log.i("langs", "Start load");
        ArrayList<StringFile> toLoad = new ArrayList<>();
        File resValuesDir = new File(pathToLoad + "/res/values");

        if (resValuesDir.exists()) {
            boolean found = false;
            //found default language
            for (File aFile : resValuesDir.listFiles()) {
                Log.i("langs", aFile.getAbsolutePath());
                if ("strings.xml".equals(aFile.getName())) {
                    found = true;
                    toLoad.add(new StringFile(resValuesDir, aFile.getName(), "default"));
                    break;
                }
            }
            if (found) {
                File resDir = new File(resValuesDir, "../");
                final Pattern p = Pattern.compile("values-[a-z\\-]{2,}");
                //select allready selected languages
                String[] files = resDir.list((dir, name) -> {
                    Matcher m = p.matcher(name);
                    return m.find();
                });
                Pattern onlyLang = Pattern.compile("^values-(.*)$");
                for (String aLang : files) {
                    Matcher mlang = onlyLang.matcher(aLang);
                    if (mlang.find()) {
                        String sanitized = mlang.group(1);
                        StringFile resLangFile = new StringFile(resDir, aLang + "/strings.xml", sanitized);
                        if (resLangFile.exists()) toLoad.add(resLangFile);
                    }
                }
            }
        }
        Log.i("langs", "end load");
        return (toLoad);
    }
}


