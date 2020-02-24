package com.mrikso.apkrepacker.activity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Регистрация ресивера
        LocalBroadcastManager.getInstance(this).registerReceiver(mThemeReceiver,
                new IntentFilter("org.openintents.action.REFRESH_THEME"));
        // Применение текущей темы
        ThemeWrapper.applyTheme(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        // Отписываемся от ресивера
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mThemeReceiver);
        super.onDestroy();
    }
}