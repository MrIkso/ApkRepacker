package com.mrikso.apkrepacker.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.ui.prererence.Preference;

import static com.mrikso.apkrepacker.ui.prererence.PreferenceKeys.KEY_DECODING_FOLDER;
import static com.mrikso.apkrepacker.ui.prererence.PreferenceKeys.KEY_DECODING_MODE;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static void dependBindPreference(PreferenceGroup pg) {
        int count = pg.getPreferenceCount();
        android.preference.Preference preference;
        String key;
        Object value;

        Preference pref = Preference.getInstance(pg.getContext());

        for (int i = 0; i < count; i++) {
            preference = pg.getPreference(i);
            key = preference.getKey();

            if (preference instanceof PreferenceGroup) {
                dependBindPreference((PreferenceGroup) preference);
                continue;
            }

            Class<? extends android.preference.Preference> cls = preference.getClass();
            if (cls.equals(android.preference.Preference.class))
                continue;

            value = pref.getValue(key);

            if (preference instanceof EditTextPreference) {
                ((EditTextPreference) preference).setText(String.valueOf(value));
            } else if (preference instanceof CheckBoxPreference) {
                ((CheckBoxPreference) preference).setChecked((boolean) value);
            }

        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.peference);
        dependBindPreference(getPreferenceScreen());
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        setCurrentValue((ListPreference) findPreference("ui_theme"));
        setCurrentValue((ListPreference) findPreference(KEY_DECODING_MODE));
        setCurrentValue((EditTextPreference) findPreference(KEY_DECODING_FOLDER));
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ListView listView = getActivity().findViewById(android.R.id.list);
        listView.setDivider(null);
        listView.setDividerHeight(0);
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                getActivity().findViewById(R.id.app_bar).setSelected(listView.canScrollVertically(-1));
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    private void setCurrentValue(ListPreference listPreference) {
        listPreference.setSummary(listPreference.getEntry());
    }

    private void setCurrentValue(EditTextPreference listPreference) {
        listPreference.setSummary(listPreference.getText());
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case "ui_theme":
                setCurrentValue((ListPreference) findPreference(key));
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(new Intent("org.openintents.action.REFRESH_THEME"));
                break;
            case KEY_DECODING_MODE:
                setCurrentValue((ListPreference) findPreference(KEY_DECODING_MODE));
                break;
            case KEY_DECODING_FOLDER:
                setCurrentValue((EditTextPreference) findPreference(KEY_DECODING_FOLDER));
                break;
        }
    }
}

