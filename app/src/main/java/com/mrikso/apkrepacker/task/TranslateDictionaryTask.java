package com.mrikso.apkrepacker.task;

import com.mrikso.apkrepacker.autotranslator.dictionary.DictionaryItem;
import com.mrikso.apkrepacker.autotranslator.dictionary.DictionaryReader;
import com.mrikso.apkrepacker.autotranslator.translator.TranslateItem;
import com.mrikso.apkrepacker.fragment.StringsFragment;
import com.mrikso.apkrepacker.task.base.CoroutinesAsyncTask;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TranslateDictionaryTask extends CoroutinesAsyncTask<Void, Void, Boolean> {

    private boolean mSkipTranslated;
    private boolean mSkipSupport;
    private boolean mReverseDictionary;
    private String mDictionaryPath;

    private List<TranslateItem> mTranslateItems;
    private StringsFragment stringsFragment;
    private List<TranslateItem> translatedItems = new ArrayList<>();


    public TranslateDictionaryTask(StringsFragment fragment) {
        mTranslateItems = new ArrayList<>();
        stringsFragment = fragment;
    }

    public void setSkipTranslated(boolean skip) {
        mSkipTranslated = skip;
    }

    public void setReverseDictionary(boolean reverseDictionary) {
        this.mReverseDictionary = reverseDictionary;
    }

    public void setSkipSupport(boolean skipSupport) {
        this.mSkipSupport = skipSupport;
    }

    public void setTranslateItems(List<TranslateItem> translateItems) {
        this.mTranslateItems = translateItems;
    }

    @Override
    public void onPreExecute() {
        super.onPreExecute();
        stringsFragment.showProgress();
    }

    public void setDictionaryPath(String dictionaryPath) {
        this.mDictionaryPath = dictionaryPath;
    }

    @Override
    public Boolean doInBackground(@Nullable Void... voids) {

        DictionaryReader dictionaryReader = new DictionaryReader(new File(mDictionaryPath));
        dictionaryReader.readDictionary();
        for (int i = 0; i < mTranslateItems.size(); i++) {
              TranslateItem item = mTranslateItems.get(i);
            //устанавливаем проверку ли переведён елемент. Если переведен пропускаем его
            if (mSkipTranslated & item.translatedValue != null) {
                i++;
                //translatedItems.add(item);
                continue;
            }
            //устанавливаем проверку ли елемент содержит ключи строк саппортов(androidx, appcompat)
            if (mSkipSupport & item.name.startsWith("abc_")) {
                i++;
               // translatedItems.add(item);
                continue;
            }

            for (Map.Entry<String, DictionaryItem> entry : dictionaryReader.getDictionaryMap().entrySet()) {
                String original = entry.getKey();
                String translated = entry.getValue().getTranslated();
                if (item.originValue.equals(mReverseDictionary ? translated : original)) {
                    int finalI = i;
                    stringsFragment.getActivity().runOnUiThread(() -> {
                        stringsFragment.getStringsAdapter().setUpdateValue(translated, finalI);
                    });
                    //mTranslateItems.get(i).translatedValue = mReverseDictionary ? original : translated;
                    //translatedItems.add(item.name,);
                            ///*new TranslateItem(item.name, item.originValue, mReverseDictionary ? original : translated)*/);
                }
               /* else {
                    translatedItems.add(item);
                }*/
            }

        }
        return true;
    }

    @Override
    public void onPostExecute(Boolean b) {
        super.onPostExecute(b);
        stringsFragment.hideProgress();
      //  stringsFragment.getStringsAdapter().setItems(mTranslateItems);
    }
}
