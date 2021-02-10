package com.mrikso.apkrepacker.task;

import android.os.AsyncTask;

import com.mrikso.apkrepacker.autotranslator.translator.TranslateItem;
import com.mrikso.apkrepacker.autotranslator.translator.Translator;
import com.mrikso.apkrepacker.fragment.StringsFragment;
import com.mrikso.apkrepacker.task.base.CoroutinesAsyncTask;
import com.mrikso.apkrepacker.utils.StringUtils;
import com.mrikso.apkrepacker.utils.common.DLog;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TranslateTask extends CoroutinesAsyncTask<Void, ArrayList<TranslateItem>, Boolean> {

    private Random r = new Random();
    private List<TranslateItem> untranslated;
    private List<TranslateItem> translatedItems = new ArrayList<>();

    private StringsFragment fragment;
    private String targetLanguageCode;
    private boolean mSkipTranslated;
    private boolean mSkipSupport;

    public TranslateTask(List<TranslateItem> untranslated, StringsFragment fragment) {
        this.untranslated = untranslated;
        this.fragment = fragment;
    }

    public void setSkipTranslated(boolean skip) {
        mSkipTranslated = skip;
    }

    public void setSkipSupport(boolean skipSupport) {
        this.mSkipSupport = skipSupport;
    }

    public void setTranslateItems(List<TranslateItem> translateItems) {
        this.untranslated = translateItems;
    }

    public void setTargetLanguageCode(String targetLanguageCode) {
        this.targetLanguageCode = targetLanguageCode;
    }

    @Override
    public void onPreExecute() {
        fragment.showProgress();
    }

    @Override
    public void onPostExecute(Boolean result) {
        fragment.hideProgress();
        //activityRef.get().translateCompleted();
        if (result) {
            fragment.getStringsAdapter().setItems(translatedItems);
        }
    }

    @Override
    public Boolean doInBackground(Void... params) {
        Random r = new Random(System.currentTimeMillis());
        final int maxItems = 10 + r.nextInt(5);
        final int maxChars = 900;

        String code = StringUtils.getGoogleLangCode(targetLanguageCode);

        Translator translator = new Translator(code);

        List<TranslateItem> todo = new ArrayList<>();
        for (TranslateItem item : untranslated) {
            if (isCancelled()) {
                return false;
            }
            //устанавливаем проверку ли переведён елемент. Если переведен пропускаем его
            if (mSkipTranslated & item.translatedValue != null && !item.translatedValue.isEmpty()) {
                translatedItems.add(item);
                DLog.d("Skip translated");
                continue;
            }
            //устанавливаем проверку ли елемент содержит ключи строк саппортов(androidx, appcompat)
            if (mSkipSupport & item.name.startsWith("abc_")) {
                translatedItems.add(item);
                DLog.d("Skip support");
                continue;
            }

            // Multiple lines
            if (item.originValue.contains("\n")) {
                if (!todo.isEmpty()) {
                    doTranslate(translator, todo);
                }

                todo.add(item);
                doTranslate(translator, todo);
            }
            //переводим по 900 слов
            else if (todo.size() >= maxItems || (getTotalCharaters(todo) + item.originValue.length()) > maxChars) {
                doTranslate(translator, todo);
                todo.add(item);
            } else {
                todo.add(item);
            }

        }

        if (this.isCancelled()) {
            return todo.isEmpty();
        }

        // Remaining
        if (!todo.isEmpty()) {
            doTranslate(translator, todo);
        }

        return true;
    }

    // Compute how many chars
    private int getTotalCharaters(List<TranslateItem> todo) {
        int totalLen = 0;
        for (TranslateItem item : todo) {
            totalLen += item.originValue.length();
        }
        return totalLen;
    }

    private void doTranslate(Translator translator, List<TranslateItem> todo) {
        // No string need to be translated
        if (todo.isEmpty()) {
            return;
        }

        // Manually sleep awhile, so that I am not taken as a robot
        try {
            Thread.sleep(300 + r.nextInt(700));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (this.isCancelled()) {
            return;
        }

        translator.translate(todo);
        checkResult(todo);
        todo.clear();
    }

    // Check how many items are successfully translated
    @SuppressWarnings("unchecked")
    private void checkResult(List<TranslateItem> items) {
        //  ArrayList<TranslateItem> copied = new ArrayList<>();
        translatedItems.addAll(items);
        //  this.publishProgress(copied);
    }

}