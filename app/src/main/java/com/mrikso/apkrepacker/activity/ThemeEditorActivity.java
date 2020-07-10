package com.mrikso.apkrepacker.activity;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.jecelyin.editor.v2.EditorPreferences;
import com.mrikso.apkrepacker.R;

public class ThemeEditorActivity extends BaseActivity{
     //   implements EditorThemeFragment.EditorThemeAdapter.OnThemeSelectListener, SharedPreferences.OnSharedPreferenceChangeListener {
    private EditorPreferences mEditorPreferences;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor_tehemes);
        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setTitle(R.string.editor_theme);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mEditorPreferences = EditorPreferences.getInstance(this);

     //   mPreferences.registerOnSharedPreferenceChangeListener(this);
       // getSupportFragmentManager()
        //        .beginTransaction()
               // .replace(R.id.content, new EditorThemeFragment())
          //      .commit();
    }

    /*
    @Override
    public void onEditorThemeSelected(EditorTheme theme) {
            mPreferences.setEditorTheme(theme.getFileName());
            String text = getString(R.string.selected_editor_theme, theme.getName());
            Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
    }


     */


    @Override
    protected void onDestroy() {
        super.onDestroy();
       // mPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

}
