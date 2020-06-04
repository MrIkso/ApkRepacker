package com.mrikso.apkrepacker.activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;

import androidx.appcompat.widget.Toolbar;

import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.fragment.SettingsFragment;

public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FragmentManager fm = getFragmentManager();
        Fragment f = fm.findFragmentById(R.id.content);
        if (f == null) {
            fm.beginTransaction().replace(R.id.content, new SettingsFragment()).commit();
        }
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.settings);
    }

    private void setResultAndFinish() {
        Intent data = new Intent();
        FragmentManager fm = getFragmentManager();
        SettingsFragment f = (SettingsFragment) fm.findFragmentById(R.id.content);
      //  data.putExtra("bufferChanged", f.getBufferChanged());
        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            // set result and finish
            setResultAndFinish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                setResultAndFinish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
