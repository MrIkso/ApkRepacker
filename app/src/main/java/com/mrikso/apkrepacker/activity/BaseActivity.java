package com.mrikso.apkrepacker.activity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.CallSuper;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.jecelyin.common.utils.DLog;
import com.jecelyin.editor.v2.Preferences;
import com.mrikso.apkrepacker.ui.prererence.Preference;
import com.mrikso.apkrepacker.utils.AppUtils;
import com.mrikso.apkrepacker.utils.ThemeWrapper;

@SuppressLint("Registered")
public class BaseActivity extends AppCompatActivity {
    /**
     * Ресивер изменения темы
     */
    private final BroadcastReceiver mThemeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (SettingsActivity.class.equals(BaseActivity.this.getClass())) {
                finish();
                startActivity(getIntent());
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            } else recreate();
        }
    };

    public BaseActivity() {

    }

    private static final String TAG = "BaseActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        // Регистрация ресивера
        LocalBroadcastManager.getInstance(this).registerReceiver(mThemeReceiver,
                new IntentFilter("org.openintents.action.REFRESH_THEME"));
        // Применение текущей темы
        ThemeWrapper.applyTheme(this);
        super.onCreate(savedInstanceState);
        setFullScreenMode(isFullScreenMode());
        AppUtils.toggledScreenOn(this, Preference.getInstance(this).isKeepScreenOn());
    }

    @Override
    protected void onDestroy() {
        // Отписываемся от ресивера
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mThemeReceiver);
        super.onDestroy();
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
        return Preferences.getInstance(this).isFullScreenMode();
    }

    private void setFullScreenMode(boolean fullScreenMode) {
        if (fullScreenMode) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            View decorView = getWindow().getDecorView();
            // Hide the status bar.
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }


}