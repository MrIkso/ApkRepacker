package com.mrikso.apkrepacker.autotranslator.translator;

import android.util.Log;

import com.mrikso.apkrepacker.BuildConfig;

import org.jsoup.Jsoup;

import java.net.URLEncoder;
import java.util.List;

public class Translator {

    private static final String GOOGLE_TRANSLATE_URL = "https://translate.google.com/m?hl=en&sl=%s&tl=%s&ie=UTF-8&prev=_m&q=%s";

    private String targetLangCode;

    public Translator(String _target) {
        this.targetLangCode = _target;
    }

    private String encodeToUrl(String str) {
        try {
            return URLEncoder.encode(str, "UTF-8");
        } catch (Exception e) {
            return str;
        }
    }

    //translating item
    public void translate(List<TranslateItem> items) {

        try {
            // Only one item, all the translation content should save to it
            if (items.size() == 1) {
                String content = translateFromGoogle("auto", targetLangCode, items.get(0).originValue);
                items.get(0).translatedValue = checkIsFormattedValue((content));
                if(BuildConfig.DEBUG)
                Log.d("DEBUG", String.format("translated: %s ---> %1s", items.get(0).originValue, items.get(0).translatedValue));
            }
            //extract one value from translated content
            else {
                for (int i = 0; i < items.size(); i++) {
                    TranslateItem item = items.get(i);
                    String content = translateFromGoogle("auto", targetLangCode, item.originValue);
                    item.translatedValue = checkIsFormattedValue(content);
                    if(BuildConfig.DEBUG)
                    Log.d("DEBUG", String.format("translated: %s ---> %1s", item.originValue, item.translatedValue));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String checkIsFormattedValue(String inputString) {
        return (inputString.contains("%") || inputString.contains("$")) ?
                inputString.replaceAll("%\\s{0,2}(\\d)\\s{0,2}\\$\\s{0,2}([dDsSдДсС])", "%$1\\$$2")
                        .replaceAll("%\\s{0,2}(\\d|\\w)", "%$1")
                        .replaceAll("%\\s{0,2}([dDдД])", "%d")
                        .replaceAll("%\\s{0,2}([sSсС])", "%s")
                        .replaceAll("%\\s{0,2}([dDдД])", "\\$d")
                        .replaceAll("%\\s{0,2}([sSсС])", "\\$s")
                        .replaceAll("([a-zA-Za-яА-Я:/.,])%(\\d|\\w)", "$1 %$2")
                : inputString;
    }

    private String translateFromGoogle(String from, String to, String contentToTranslate) {
        String url = String.format(GOOGLE_TRANSLATE_URL, from, to, encodeToUrl(contentToTranslate));
        if(BuildConfig.DEBUG)
        Log.d("DEBUG",String.format("url=%s", url));
        try {
            return Jsoup.connect(url).timeout(15000).get().getElementsByClass("t0").first().text();
        } catch (Exception ex) {
            ex.printStackTrace();
            return "";
        }
    }
}
