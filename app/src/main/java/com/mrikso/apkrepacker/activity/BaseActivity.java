package com.mrikso.apkrepacker.activity;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.jecelyin.editor.v2.EditorPreferences;
import com.mrikso.apkrepacker.ui.preferences.PreferenceHelper;
import com.mrikso.apkrepacker.utils.Theme;
import com.mrikso.apkrepacker.utils.ViewDeviceUtils;

@SuppressLint("Registered")
public class BaseActivity extends AppCompatActivity {

    private Theme.ThemeDescriptor mAppliedTheme;

    public BaseActivity() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        mAppliedTheme = Theme.apply(this);
        Theme.observe(this, this, theme -> {
            if (!theme.equals(mAppliedTheme))
                recreate();
        });

        super.onCreate(savedInstanceState);
        ViewDeviceUtils.setFullScreenMode(this, isFullScreenMode());
        ViewDeviceUtils.toggledScreenOn(this, PreferenceHelper.getInstance(this).isKeepScreenOn());
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected boolean isFullScreenMode() {
        return EditorPreferences.getInstance(this).isFullScreenMode();
    }

}