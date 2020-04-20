package com.mrikso.apkrepacker.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.duy.ide.editor.theme.EditorThemeFragment;
import com.duy.ide.editor.theme.model.EditorTheme;
import com.jecelyin.editor.v2.Preferences;
import com.mrikso.apkrepacker.R;

public class ThemeEditorActivity extends BaseActivity
        implements EditorThemeFragment.EditorThemeAdapter.OnThemeSelectListener, SharedPreferences.OnSharedPreferenceChangeListener {
    private Preferences mPreferences;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor_tehemes);
        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setTitle(R.string.editor_theme);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mPreferences = Preferences.getInstance(this);

        mPreferences.registerOnSharedPreferenceChangeListener(this);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content, new EditorThemeFragment())
                .commit();
    }

    @Override
    public void onEditorThemeSelected(EditorTheme theme) {
            mPreferences.setEditorTheme(theme.getFileName());
            String text = getString(R.string.selected_editor_theme, theme.getName());
            Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
       // super.onSharedPreferenceChanged(sharedPreferences, key);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

}
