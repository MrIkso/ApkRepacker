package com.mrikso.apkrepacker.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.duy.common.DLog;
import com.google.android.material.button.MaterialButton;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.autotranslator.common.ActivityUtil;
import com.mrikso.apkrepacker.autotranslator.common.TranslateStringsHelper;
import com.mrikso.apkrepacker.autotranslator.translator.TranslateItem;
import com.mrikso.apkrepacker.autotranslator.translator.TranslateStringsAdapter;
import com.mrikso.apkrepacker.autotranslator.translator.TranslateTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.zhanghai.android.fastscroll.FastScrollerBuilder;

public class AutoTranslatorActivity extends BaseActivity implements View.OnClickListener {

    // Standard code like "-zh-rCN"
    private String targetLanguageCode;
    private RecyclerView stringList;
    private TranslateStringsAdapter stringsAdapter;

    // All the string views
    //private Map<String, String> etMap = new HashMap<>();
    private LinearLayout translatingLayout;
    private LinearLayout translatedLayout;
    private AppCompatTextView translatingMsg;
    private AppCompatTextView translatedMsg;
    private MaterialButton stopOrSaveBtn;

    private boolean translateFinished = false;
    private boolean translationSaved = false;

    // Translate list includes items already translated
    //private List<TranslateItem> translatedList;
    private List<TranslateItem> untranslatedList;
    private TranslateTask translatingTask;

    // How many items need to be translated
    private int numToTranslate = 0;
    private int numSucceed = 0;
    private int numFailed = 0;
    private Context mContext;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        Intent intent = getIntent();
        setContentView(R.layout.activity_autotranslate);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initData(intent);

        initView(untranslatedList);

        // Start the translating
        startTranslating();
    }

    private void initData(Intent intent) {
        if (intent.getExtras() != null) {
            targetLanguageCode = ActivityUtil.getParam(intent, "targetLanguageCode");
            Log.d("TranslateActivity",targetLanguageCode );
            untranslatedList = TranslateStringsHelper.getDefaultStrings();
        }
    }

    private void initView(List<TranslateItem> strings) {
        stringList = findViewById(R.id.translated_list);
        stringList.setLayoutManager(new LinearLayoutManager(mContext));
        //stringList.setHasFixedSize(true);
        stringsAdapter = new TranslateStringsAdapter(mContext);
        new FastScrollerBuilder(stringList).useMd2Style().build();
        stringList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                findViewById(R.id.app_bar).setSelected(stringList.canScrollVertically(-1));
            }
        });
        translatingLayout = findViewById(R.id.translating_layout);
        translatedLayout = findViewById(R.id.translated_layout);
        translatingMsg = translatingLayout.findViewById(R.id.translating_msg);
        translatedMsg = translatedLayout.findViewById(R.id.translated_msg);

        if (strings != null) {
            stringsAdapter.setItems(strings);
            stringList.setAdapter(stringsAdapter);
        }

        stopOrSaveBtn = findViewById(R.id.btn_stop_or_save);
        stopOrSaveBtn.setOnClickListener(this);
    }

    // Transfer modified files to parent activity
    private void setResult(List<TranslateItem> stringValues) {
        TranslateStringsHelper.setTranslatedStrings(stringValues);
        setResult(10);
        finish();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_stop_or_save) {
            if (translateFinished) {
                saveStringAsResource();
                translationSaved = true;
                finish();
            } else {
                stopTranslating();
            }
        }
    }

    // Forcely stop it
    private void stopTranslating() {
        translatingTask.cancel(true);
        translateCompleted();
    }

    // To save the translation result
    private void saveStringAsResource() {
        // Prepare translated values (collect from the edit text)
        List<TranslateItem> stringValues = new ArrayList<>();
       // etMap.putAll(stringsAdapter.getTranslatedMap());
        for (Map.Entry<String, String> entry : stringsAdapter.getTranslatedMap().entrySet()) {
            String name = entry.getKey();
            String translatedVal = entry.getValue();
            if (!"".equals(translatedVal)) {
                DLog.d("translated",String.format("%s : %s", name, translatedVal));
                stringValues.add(new TranslateItem(name,null, translatedVal));
            }
        }

        // No translated string
        if (stringValues.isEmpty()) {
            Toast.makeText(this, R.string.error_no_string_tosave, Toast.LENGTH_LONG).show();
            return;
        }

        TranslateStringsHelper.setTranslatedStrings(stringValues);
        setResult(10);
        //finish();
    }

    // Update views
    @SuppressLint("DefaultLocale")
    public void updateView(List<TranslateItem> items) {
        for (TranslateItem item : items) {
            stringsAdapter.notifyDataSetChanged();
            if (item.translatedValue != null) {
                numSucceed += 1;
            } else {
                numFailed += 1;
            }
        }

        String strTranslated = getString(R.string.translated);
        int total = numToTranslate - numFailed;
        String msg = String.format("%d / %d " + strTranslated, numSucceed, total);
        translatingMsg.setText(msg);
    }

    // Naturally completed, not by force stop
    public void translateCompleted() {
        translateFinished = true;
        translatingLayout.setVisibility(View.GONE);
        translatedLayout.setVisibility(View.VISIBLE);

        String msg;
        if (numToTranslate == 0) {
            msg = getString(R.string.error_no_string_totranslate);
        } else {
            msg = String.format(getString(R.string.translated_format), numSucceed);
        }

        // Failed number
        if (numFailed > 0) {
            msg += String.format(", " + getString(R.string.failed_format), numFailed);
        }

        // Untranslated number
        int untranslatedNum = numToTranslate - numSucceed - numFailed;
        if (untranslatedNum > 0) {
            msg += String.format(", " + getString(R.string.untranslated_format), untranslatedNum);
        }

        translatedMsg.setText(msg);

        // Change the button text
        if (numSucceed > 0) {
            stopOrSaveBtn.setText(R.string.save_and_close);
        } else {
            stopOrSaveBtn.setText(R.string.close);
        }
    }

    private void startTranslating() {

        // Update the view
        translatingMsg.setText(R.string.translating);
        translatingLayout.setVisibility(View.VISIBLE);
        translatedLayout.setVisibility(View.GONE);
        stopOrSaveBtn.setText(R.string.stop);

        translationSaved = false;
        translateFinished = false;
        numToTranslate = (untranslatedList != null ? untranslatedList.size() : 0);
        numSucceed = 0;
        numFailed = 0;

        if (numToTranslate > 0) {
            translatingTask = new TranslateTask(untranslatedList, this);
            translatingTask.execute();
        } else {
            translateCompleted();
        }
    }

    // Get the google language code
    // Convert -zh-rCN to zh-CN
    public String getGoogleLangCode() {
        String code = targetLanguageCode.substring(1);
        int pos = code.indexOf("-");
        if (pos != -1) {
            code = code.substring(0, pos + 1) + code.substring(pos + 2);
        }
        return code;
    }
}
