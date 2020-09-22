package com.mrikso.apkrepacker.autotranslator.translator;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mrikso.apkrepacker.autotranslator.browser.WebBrowser;
import com.mrikso.apkrepacker.utils.common.DLog;

import java.net.URLEncoder;
import java.util.List;
import java.util.Objects;


public class Translator {

    private WebBrowser browser;
    private String startUrl;

    // This is a temporary var
    private int jsonIndex = 0;

    private String targetLangCode;

    public Translator(String userAgent, String target) {
        this.browser = new WebBrowser(userAgent);
        this.startUrl = "https://translate.google.com/";
        this.targetLangCode = target;
    }

    private static char decodeChar(char c1, char c2) {
        int i1 = hex2Int(c1);
        int i2 = hex2Int(c2);
        return (char) (i1 * 16 + i2);
    }

    private static int hex2Int(char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        } else if (c >= 'a' && c <= 'f') {
            return c - 'a' + 10;
        } else {
            return c - 'A' + 10;
        }
    }

    protected String encodeToUrl(String str) {
        try {
            return URLEncoder.encode(str, "UTF-8");
        } catch (Exception e) {
            return str;
        }
    }

    //translating item
    public void translate(List<TranslateItem> items) {
        // Concat the string
        StringBuilder queryBuf = new StringBuilder();
        for (TranslateItem item : items) {
            queryBuf.append(item.originValue);
            queryBuf.append('\n');
        }
        queryBuf.deleteCharAt(queryBuf.length() - 1);
        String contentToTranslate = queryBuf.toString();

        String url = "http://translate.google.com/translate_a/single?client=gtx&dt=t&dj=1&ie=UTF-8&sl=auto&tl="
                + targetLangCode + "&q=" + encodeToUrl(contentToTranslate);
        // String url = "https://translate.google.com/translate_a/single?client=webapp&sl=auto&tl="
        //         + targetLangCode + "&hl=en&dt=bd&dt=ex&dt=ld&dt=md&dt=qca&dt=rw&dt=rm&dt=ss&dt=t&otf=1&ssel=0&tsel=0&kc=1&tk="
        //       + encodeToUrl(getToken(tkk, contentToTranslate)) + "&q=" + encodeToUrl(contentToTranslate);
        DLog.d("url=%s", url);

        //create browser and translate strings from google translator and get translated json content
        String content = browser.get(url, startUrl);

        // Succeed
        if(content != null)
        // if (content != null && content.startsWith("[[[\"") && (position = content.lastIndexOf("]")) != -1) {
        parseContent(content, items);
        //  }
        //  DLog.d(String.format("content=%s", content));
    }

    // Parse the content and save result to items
    private void parseContent(String jsonResponse, List<TranslateItem> items) {
        try {
            JsonParser jsonParser = new JsonParser();
            JsonElement element = jsonParser.parse(jsonResponse);
            JsonArray sentences = element.getAsJsonObject().get("sentences").getAsJsonArray();
            // Only one item, all the translation content should save to it
            if (items.size() == 1) {
                items.get(0).translatedValue = checkIsFormattedValue(Objects.requireNonNull(extractAllValueFromJson(sentences)));
                DLog.d("DEBUG", items.get(0).originValue + " ---> " + items.get(0).translatedValue);
            } else {
                this.jsonIndex = 0;
                for (TranslateItem item : items) {
                    item.translatedValue = checkIsFormattedValue(Objects.requireNonNull(extractOneItemFromJson(sentences)));
                    DLog.d("DEBUG", String.format("translated: %s ---> %1s", item.originValue, item.translatedValue));
                }
                /*for (int i = 0; i < items.size(); i++) {
                    TranslateItem item = items.get(i);
                    item.translatedValue = checkIsFormattedValue(Objects.requireNonNull(extractOneItemFromJson(values)));
                    DLog.d("DEBUG", String.format("translated: %s ---> %1s", item.originValue, item.translatedValue));
                }*/
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

    // In Json, one item may be splited into several arrays
    // jsonIndex will be used
    private String extractOneItemFromJson(JsonArray values) {
        StringBuilder sb = new StringBuilder();
        while (jsonIndex < values.size()) {
            JsonElement element1 = values.get(jsonIndex++);
            String curVal = element1.getAsJsonObject().get("trans").getAsString();
            if (curVal == null) {
                break;
            }

            sb.append(curVal);
            if (curVal.endsWith("\n")) {
                sb.deleteCharAt(sb.length() - 1);
                break;
            }
        }
        if (sb.length() > 0) {
            return sb.toString();
        }

        return null;
    }

    private String extractAllValueFromJson(JsonArray values) {
        StringBuilder sb = new StringBuilder();
        int jsonIndex = 0;

        if (values.size() > 0) {
            while (jsonIndex < values.size()) {
                JsonElement element = values.get(jsonIndex++);
                String curVal = element.getAsJsonObject().get("trans").getAsString();
                if (curVal == null) {
                    break;
                }
                sb.append(curVal);
            }
            if (sb.length() > 0) {
                return sb.toString();
            }

        }
        return null;
    }
}
