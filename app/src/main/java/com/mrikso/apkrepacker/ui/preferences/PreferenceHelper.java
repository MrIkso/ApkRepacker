package com.mrikso.apkrepacker.ui.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

import com.jecelyin.common.utils.DLog;
import com.jecelyin.common.utils.StringUtils;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import static com.mrikso.apkrepacker.ui.preferences.PreferenceKeys.KEY_AAPT2_PATH;
import static com.mrikso.apkrepacker.ui.preferences.PreferenceKeys.KEY_AAPT_PATH;
import static com.mrikso.apkrepacker.ui.preferences.PreferenceKeys.KEY_AUTO_THEME;
import static com.mrikso.apkrepacker.ui.preferences.PreferenceKeys.KEY_CERT_PATH;
import static com.mrikso.apkrepacker.ui.preferences.PreferenceKeys.KEY_CONFIRM_BUILD;
import static com.mrikso.apkrepacker.ui.preferences.PreferenceKeys.KEY_COPY_ORIGINAL_FILES;
import static com.mrikso.apkrepacker.ui.preferences.PreferenceKeys.KEY_DEBUG_MODE;
import static com.mrikso.apkrepacker.ui.preferences.PreferenceKeys.KEY_DECODING_FOLDER;
import static com.mrikso.apkrepacker.ui.preferences.PreferenceKeys.KEY_DECODING_MODE;
import static com.mrikso.apkrepacker.ui.preferences.PreferenceKeys.KEY_EXTENSIONS;
import static com.mrikso.apkrepacker.ui.preferences.PreferenceKeys.KEY_FILES_MODE;
import static com.mrikso.apkrepacker.ui.preferences.PreferenceKeys.KEY_FRAMEWORK_PATH;
import static com.mrikso.apkrepacker.ui.preferences.PreferenceKeys.KEY_KEEP_SCREEN_ON;
import static com.mrikso.apkrepacker.ui.preferences.PreferenceKeys.KEY_KEY_TYPE;
import static com.mrikso.apkrepacker.ui.preferences.PreferenceKeys.KEY_MATCH_CASE;
import static com.mrikso.apkrepacker.ui.preferences.PreferenceKeys.KEY_PRIVATE_KEY;
import static com.mrikso.apkrepacker.ui.preferences.PreferenceKeys.KEY_PRIVATE_KEY_PATH;
import static com.mrikso.apkrepacker.ui.preferences.PreferenceKeys.KEY_RECURSIVELY;
import static com.mrikso.apkrepacker.ui.preferences.PreferenceKeys.KEY_REVERSE_TRANSLATED;
import static com.mrikso.apkrepacker.ui.preferences.PreferenceKeys.KEY_SHOW_HIDDEN_FILES;
import static com.mrikso.apkrepacker.ui.preferences.PreferenceKeys.KEY_SIGN_OUT_APK;
import static com.mrikso.apkrepacker.ui.preferences.PreferenceKeys.KEY_SKIP_SUPPORT_LINES;
import static com.mrikso.apkrepacker.ui.preferences.PreferenceKeys.KEY_SKIP_TRANSLATED;
import static com.mrikso.apkrepacker.ui.preferences.PreferenceKeys.KEY_STORE_KEY;
import static com.mrikso.apkrepacker.ui.preferences.PreferenceKeys.KEY_THEME;
import static com.mrikso.apkrepacker.ui.preferences.PreferenceKeys.KEY_TOOLS_INSTALLED;
import static com.mrikso.apkrepacker.ui.preferences.PreferenceKeys.KEY_USE_AAPT2;
import static com.mrikso.apkrepacker.ui.preferences.PreferenceKeys.KEY_USE_CUSTOM_SIGN;
import static com.mrikso.apkrepacker.ui.preferences.PreferenceKeys.KEY_USE_REGEX;
import static com.mrikso.apkrepacker.ui.preferences.PreferenceKeys.KEY_USE_V2_SIGNATURE;
import static com.mrikso.apkrepacker.ui.preferences.PreferenceKeys.KEY_VERBOSE_MODE;
import static com.mrikso.apkrepacker.ui.preferences.PreferenceKeys.KEY_WHOLE_WORDS_ONLY;

public class PreferenceHelper implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static PreferenceHelper instance;
    private final SharedPreferences pm;

    private final Map<String, Object> map;
    private final Context context;
    private final WeakHashMap<SharedPreferences.OnSharedPreferenceChangeListener, Object> mListeners = new WeakHashMap<>();
    private static final Object mContent = new Object();
    public static final Map<String, Boolean> VALUE_EXT = new HashMap<>();//"smali", true, "xml",
          //  "txt", "json");

    public PreferenceHelper(Context context){
        this.context = context;
        pm =  PreferenceManager.getDefaultSharedPreferences(context);
        pm.registerOnSharedPreferenceChangeListener(this);

        //init variable
        map = new HashMap<>();
        map.put(KEY_SHOW_HIDDEN_FILES, false);
        map.put(KEY_USE_AAPT2, false);
        map.put(KEY_DECODING_FOLDER, Environment.getExternalStorageDirectory().getPath() + "/ApkRepacker");
        map.put(KEY_DECODING_MODE, 0);
        map.put(KEY_SIGN_OUT_APK, true);
        map.put(KEY_USE_CUSTOM_SIGN, false);
        map.put(KEY_DEBUG_MODE, false);
        map.put(KEY_VERBOSE_MODE, false);
        map.put(KEY_COPY_ORIGINAL_FILES, false);
        map.put(KEY_USE_V2_SIGNATURE, false);
        map.put(KEY_TOOLS_INSTALLED, false);
        map.put(KEY_PRIVATE_KEY_PATH, "");
        map.put(KEY_CERT_PATH, "");
        map.put(KEY_AAPT2_PATH, "");
        map.put(KEY_AAPT_PATH, "");
        map.put(KEY_FRAMEWORK_PATH, "");
        //
        map.put(KEY_USE_REGEX, false);
        map.put(KEY_MATCH_CASE, false);
        map.put(KEY_KEY_TYPE, 0);
        map.put(KEY_WHOLE_WORDS_ONLY, false);
        map.put(KEY_RECURSIVELY, true);
        map.put(KEY_FILES_MODE, true);
        map.put(KEY_EXTENSIONS, initExt());
        map.put(KEY_KEEP_SCREEN_ON, false);

        map.put(KEY_CONFIRM_BUILD, false);
        map.put(KEY_THEME, 0);
        map.put(KEY_AUTO_THEME, false);
        map.put(KEY_SKIP_TRANSLATED, false);
        map.put(KEY_SKIP_SUPPORT_LINES, false);
        map.put(KEY_REVERSE_TRANSLATED, false);
        Map<String, ?> values = pm.getAll();
        for(String key : map.keySet()) {
            updateValue(key, values);
        }
    }

    public static PreferenceHelper getInstance(Context context) {
        if(instance == null) {
            instance = new PreferenceHelper(context.getApplicationContext());
        }
        return instance;
    }

    public boolean isUseAAPT2() {
        return (boolean) map.get(KEY_USE_AAPT2);
    }

    public boolean isCustomSign() {
        return (boolean) map.get(KEY_USE_CUSTOM_SIGN);
    }

    public boolean isSignResultApk() {
        return (boolean) map.get(KEY_SIGN_OUT_APK);
    }

    public boolean isV2SignatureEnabled() {
        return (boolean) map.get(KEY_USE_V2_SIGNATURE);
    }

    public boolean isDebugModeApk() {
        return (boolean) map.get(KEY_DEBUG_MODE);
    }

    public boolean isVerboseModeApk() {
        return (boolean) map.get(KEY_VERBOSE_MODE);
    }

    public boolean isCopyOriginalFiles() {
        return (boolean) map.get(KEY_COPY_ORIGINAL_FILES);
    }

    public boolean isShowHiddenFiles() {
        return (boolean) map.get(KEY_SHOW_HIDDEN_FILES);
    }

    public boolean isKeepScreenOn() {
        return (boolean) map.get(KEY_KEEP_SCREEN_ON);
    }

    public boolean isConfirmBuild(){
        return (boolean) map.get(KEY_CONFIRM_BUILD);
    }

    //getters
    public String getDecodingPath() {
        return (String)map.get(KEY_DECODING_FOLDER);
    }

    public Integer getDecodingMode() {
        return (Integer) map.get(KEY_DECODING_MODE);
    }

    public int getCurrentTheme() {
        return (int) map.get(KEY_THEME);
    }

    public String getFrameworkPath() {
        return (String)map.get(KEY_FRAMEWORK_PATH);
    }

    public void setFrameworkPath(String path) {
        pm.edit().putString(KEY_FRAMEWORK_PATH, path).apply();
        map.put(KEY_FRAMEWORK_PATH, path);
    }

    public String getAaptPath() {
        return (String)map.get(KEY_AAPT_PATH);
    }

    public void setAaptPath(String path) {
        pm.edit().putString(KEY_AAPT_PATH, path).apply();
        map.put(KEY_AAPT_PATH, path);
    }

    public String getAapt2Path() {
        return (String)map.get(KEY_AAPT2_PATH);
    }

    public void setAapt2Path(String path) {
        pm.edit().putString(KEY_AAPT2_PATH, path).apply();
        map.put(KEY_AAPT2_PATH, path);
    }

    public String getCertPath() {
        return (String)map.get(KEY_CERT_PATH);
    }

    public void setCertPath(String path) {
        pm.edit().putString(KEY_CERT_PATH, path).apply();
        map.put(KEY_CERT_PATH, path);
    }

    public String getPrivateKeyPath() {
        return (String)map.get(KEY_PRIVATE_KEY_PATH);
    }

    public void setPrivateKeyPath(String path) {
        pm.edit().putString(KEY_PRIVATE_KEY_PATH, path).apply();
        map.put(KEY_PRIVATE_KEY_PATH, path);
    }

    public String getPrivateKey() {
        return (String)map.get(KEY_PRIVATE_KEY);
    }

    public void setPrivateKey(String path) {
        pm.edit().putString(KEY_PRIVATE_KEY, path).apply();
        map.put(KEY_PRIVATE_KEY, path);
    }

    public String getStoreKey() {
        return (String)map.get(KEY_STORE_KEY);
    }

    public void setStoreKey(String path) {
        pm.edit().putString(KEY_STORE_KEY, path).apply();
        map.put(KEY_STORE_KEY, path);
    }

    public Integer getKeyType() {
        return (Integer) map.get(KEY_KEY_TYPE);
    }

    public void setKeyType(Integer mode) {
        pm.edit().putInt(KEY_KEY_TYPE, mode).apply();
        map.put(KEY_KEY_TYPE, mode);
    }

    public Boolean isToolsInstalled() {
        return (Boolean) map.get(KEY_TOOLS_INSTALLED);
    }

    public void setToolsInstalled(boolean value) {
        pm.edit().putBoolean(KEY_TOOLS_INSTALLED, value).apply();
        map.put(KEY_TOOLS_INSTALLED, value);
    }

    public void setRegexMode(boolean b) {
        pm.edit().putBoolean(KEY_USE_REGEX, b).apply();
        map.put(KEY_USE_REGEX, b);
    }

    public boolean isRegexMode() {
        return (boolean)map.get(KEY_USE_REGEX);
    }

    public void setMatchCaseMode(boolean b) {
        pm.edit().putBoolean(KEY_MATCH_CASE, b).apply();
        map.put(KEY_MATCH_CASE, b);
    }

    public boolean isMatchCaseMode() {
        return (boolean)map.get(KEY_MATCH_CASE);
    }

    public void setWholeWordsOnlyMode(boolean b) {
        pm.edit().putBoolean(KEY_WHOLE_WORDS_ONLY, b).apply();
        map.put(KEY_WHOLE_WORDS_ONLY, b);
    }

    public boolean isWholeWordsOnlyMode() {
        return (boolean)map.get(KEY_WHOLE_WORDS_ONLY);
    }

    public void setRecursivelyMode(boolean b) {
        pm.edit().putBoolean(KEY_RECURSIVELY, b).apply();
        map.put(KEY_RECURSIVELY, b);
    }

    public boolean isRecursivelyMode() {
        return (boolean)map.get(KEY_RECURSIVELY);
    }

    public void setFilesMode(boolean b) {
        pm.edit().putBoolean(KEY_FILES_MODE, b).apply();
        map.put(KEY_FILES_MODE, b);
    }

    public boolean isFilesMode() {
        return (boolean)map.get(KEY_FILES_MODE);
    }

    public void setSkipTranslated(boolean b) {
        pm.edit().putBoolean(KEY_SKIP_TRANSLATED, b).apply();
        map.put(KEY_SKIP_TRANSLATED, b);
    }

    public boolean isSkipTranslated() {
        return (boolean)map.get(KEY_SKIP_TRANSLATED);
    }

    public void setSkipSupportLines(boolean b) {
        pm.edit().putBoolean(KEY_SKIP_SUPPORT_LINES, b).apply();
        map.put(KEY_SKIP_SUPPORT_LINES, b);
    }

    public boolean isSkipSupportLines() {
        return (boolean)map.get(KEY_SKIP_SUPPORT_LINES);
    }

    public void setReverseDictionary(boolean b) {
        pm.edit().putBoolean(KEY_REVERSE_TRANSLATED, b).apply();
        map.put(KEY_REVERSE_TRANSLATED, b);
    }

    public boolean isReverseDictionary() {
        return (boolean)map.get(KEY_REVERSE_TRANSLATED);
    }

    public void setExt(Map<String,Boolean> inputMap) {
        JSONObject jsonObject = new JSONObject(inputMap);
        String jsonString = jsonObject.toString();
    //    pm.edit().remove(KEY_EXTENSIONS).apply();
        pm.edit().putString(KEY_EXTENSIONS, jsonString).apply();
        map.put(KEY_EXTENSIONS, jsonString);
    }

    public Map<String,Boolean> getExt(){
        Map<String,Boolean> outputMap = new HashMap<>();
       /// SharedPreferences pSharedPref = MyApp.getInstance().getSharedPreferences(USERDATA, Context.MODE_PRIVATE);
        try{
                //String jsonString = (String)map.get(KEY_EXTENSIONS);
                JSONObject jsonObject = new JSONObject((String) map.get(KEY_EXTENSIONS));
                Iterator<String> keysItr = jsonObject.keys();
                while(keysItr.hasNext()) {
                    String k = keysItr.next();
                    Boolean v = (boolean) jsonObject.get(k);
                    outputMap.put(k,v);
                }

        } catch(Exception e){
            e.printStackTrace();
        }
        return outputMap;
    }

    public void registerOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        synchronized(this) {
            mListeners.put(listener, mContent);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updateValue(key, sharedPreferences.getAll());
        Set<SharedPreferences.OnSharedPreferenceChangeListener> listeners = mListeners.keySet();
        for(SharedPreferences.OnSharedPreferenceChangeListener listener : listeners) {
            if (listener != null) {
                listener.onSharedPreferenceChanged(sharedPreferences, key);
            }
        }
    }

    private void updateValue(String key, Map<String, ?> values) {
        Object value = map.get(key);
        if(value == null)
            return;
        Class cls = value.getClass();

        try {
            if(cls == int.class || cls == Integer.class) {
//                value = StringUtils.toInt(pm.getString(key, String.valueOf(value)));
                Object in = values.get(key);
                if (in != null)
                    value = in instanceof Integer ? (int)in : StringUtils.toInt(String.valueOf(in));
            } else if(cls == boolean.class || cls == Boolean.class) {
//                value = pm.getBoolean(key, (boolean)value);
                Boolean b = (Boolean) values.get(key);
                value = b == null ? (boolean)value : b;
            } else {
//                value = pm.getString(key, (String)value);
                String str = (String) values.get(key);
                value = str == null ? (String)value : str;
            }
        } catch (Exception e) {
            DLog.e("key = " + key, e);
            return;
        }
        map.put(key, value);
    }

    public Object getValue(String key) {
        return map.get(key);
    }

    /*private static void saveMap(String key, Map<String,String> inputMap){
        SharedPreferences pSharedPref = MyApp.getInstance().getSharedPreferences(USERDATA, Context.MODE_PRIVATE);
        if (pSharedPref != null){
            JSONObject jsonObject = new JSONObject(inputMap);
            String jsonString = jsonObject.toString();
            SharedPreferences.Editor editor = pSharedPref.edit();
            editor.remove(key).apply();
            editor.putString(key, jsonString);
            editor.commit();
        }
    }

    private static Map<String,String> loadMap(String key){
        Map<String,String> outputMap = new HashMap<String,String>();
        SharedPreferences pSharedPref = MyApp.getInstance().getSharedPreferences(USERDATA, Context.MODE_PRIVATE);
        try{
            if (pSharedPref != null){
                String jsonString = pSharedPref.getString(key, (new JSONObject()).toString());
                JSONObject jsonObject = new JSONObject(jsonString);
                Iterator<String> keysItr = jsonObject.keys();
                while(keysItr.hasNext()) {
                    String k = keysItr.next();
                    String v = (String) jsonObject.get(k);
                    outputMap.put(k,v);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return outputMap;
    }*/

    private String initExt(){
        VALUE_EXT.put("smali", true);
        VALUE_EXT.put("xml", true);
        VALUE_EXT.put("jpg", true);
        VALUE_EXT.put("png", true);
        VALUE_EXT.put("txt", true);
        JSONObject jsonObject = new JSONObject(VALUE_EXT);
        return jsonObject.toString();
    }
}
