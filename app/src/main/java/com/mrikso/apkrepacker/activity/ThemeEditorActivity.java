package com.mrikso.apkrepacker.activity;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.jecelyin.editor.v2.EditorPreferences;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.fragment.EditorThemeFragment;
import com.mrikso.apkrepacker.ide.editor.theme.model.EditorTheme;

public class ThemeEditorActivity extends BaseActivity implements EditorThemeFragment.EditorThemeAdapter.OnThemeSelectListener{
    private EditorPreferences mEditorPreferences;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor_tehemes);
        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setTitle(R.string.editor_theme);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mEditorPreferences = EditorPreferences.getInstance(this);

//        mEditorPreferences.registerOnSharedPreferenceChangeListener(this);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content, new EditorThemeFragment())
               .commit();
    }


    @Override
    public void onEditorThemeSelected(EditorTheme theme) {
        mEditorPreferences.setEditorTheme(theme.getFileName());
            String text = getString(R.string.selected_editor_theme, theme.getName());
            Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
       // mEditorPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

}
