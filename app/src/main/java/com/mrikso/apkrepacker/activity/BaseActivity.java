package com.mrikso.apkrepacker.activity;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.jecelyin.editor.v2.EditorPreferences;
import com.mrikso.apkrepacker.ui.prererence.PreferenceHelper;
import com.mrikso.apkrepacker.utils.AppUtils;
import com.mrikso.apkrepacker.utils.Theme;

@SuppressLint("Registered")
public class BaseActivity extends AppCompatActivity {

    public BaseActivity() {

    }

    private Theme.ThemeDescriptor mAppliedTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        mAppliedTheme = Theme.apply(this);
        Theme.observe(this, this, theme -> {
            if (!theme.equals(mAppliedTheme))
                recreate();
        });

        super.onCreate(savedInstanceState);
        setFullScreenMode(isFullScreenMode());
        AppUtils.toggledScreenOn(this, PreferenceHelper.getInstance(this).isKeepScreenOn());
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

    private void setFullScreenMode(boolean fullScreenMode) {
        if (fullScreenMode) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            View decorView = getWindow().getDecorView();
            // Hide the status bar.
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }
}