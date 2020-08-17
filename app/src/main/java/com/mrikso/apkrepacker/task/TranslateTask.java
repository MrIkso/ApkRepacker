package com.mrikso.apkrepacker.task;

import com.mrikso.apkrepacker.autotranslator.translator.TranslateItem;
import com.mrikso.apkrepacker.autotranslator.translator.Translator;
import com.mrikso.apkrepacker.fragment.StringsFragment;
import com.mrikso.apkrepacker.task.base.CoroutinesAsyncTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class TranslateTask extends CoroutinesAsyncTask<Void, List<TranslateItem>, List<TranslateItem>> {

    private List<TranslateItem> untranslated;
    private StringsFragment activityRef;

    public TranslateTask(List<TranslateItem> untranslated, StringsFragment activity) {
        this.untranslated = untranslated;
        this.activityRef = activity;
    }

    @Override
    public void onPreExecute() {

    }

    @Override
    public void onPostExecute(List<TranslateItem> result) {
        if(result != null)
        activityRef.mStringsAdapter.setUpdatedItems(result);
    }

    @Override
    public List<TranslateItem> doInBackground(Void... params) {
        String code = activityRef.getGoogleLangCode();
        Translator translator = new Translator(code);
        return translator.translate(untranslated);

    }


    @SafeVarargs
    @Override
    public final void onProgressUpdate(List<TranslateItem>... args) {
      //  List<TranslateItem> translated  = (List<TranslateItem>) Arrays.asList(args);
      //  Collections.addAll(translated, args);
      //  activityRef.mStringsAdapter.setItems(translated);
    }
}