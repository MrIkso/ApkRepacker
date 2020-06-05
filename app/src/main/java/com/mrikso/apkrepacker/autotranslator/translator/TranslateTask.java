package com.mrikso.apkrepacker.autotranslator.translator;

import android.os.AsyncTask;

import com.mrikso.apkrepacker.activity.AutoTranslatorActivity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TranslateTask extends AsyncTask<Void, List<TranslateItem>, Boolean> {

    private List<TranslateItem> untranslated;
    private WeakReference<AutoTranslatorActivity> activityRef;

    public TranslateTask(List<TranslateItem> untranslated, AutoTranslatorActivity activity) {
        this.untranslated = untranslated;
        this.activityRef = new WeakReference<>(activity);
    }

    @Override
    protected void onPreExecute() {

    }

    @Override
    protected void onPostExecute(Boolean result) {
        activityRef.get().translateCompleted();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        Random r = new Random(System.currentTimeMillis());
        final int maxItems = 15 + r.nextInt(5);
        final int maxChars = 1000;

        String code = activityRef.get().getGoogleLangCode();
        Translator translator = new Translator(code);

        List<TranslateItem> todo = new ArrayList<>();
        for (TranslateItem item : untranslated) {
            if (this.isCancelled()) {
                return false;
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

        /*// Manually sleep awhile, so that I am not taken as a robot
        try {
            Thread.sleep(300 + r.nextInt(500));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/

        if (this.isCancelled()) {
            return;
        }

        translator.translate(todo);
        checkResult(todo);
        todo.clear();
    }

    // Check how many items are successfully translated
    @SuppressWarnings("unchecked")
    private void checkResult(List<TranslateItem> translatedItems) {
        ArrayList<TranslateItem> copied = new ArrayList<>();
        copied.addAll(translatedItems);
        this.publishProgress(copied);
    }

    @SafeVarargs
    @Override
    protected final void onProgressUpdate(List<TranslateItem>... args) {
        List<TranslateItem> translated = args[0];
        activityRef.get().updateView(translated);
    }
}