package com.mrikso.apkrepacker.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroup;
import androidx.preference.SwitchPreference;

import com.jecelyin.editor.v2.EditorPreferences;
import com.mrikso.apkrepacker.R;

/**
 * @author Jecelyin Peng <jecelyin@gmail.com>
 */
public class SettingsFragmentEditor extends PreferenceFragmentCompat {
    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = (preference, value) -> {
        if (value == null)
            return true;
        String stringValue = value.toString();
        String key = preference.getKey();

        if (preference instanceof ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list.
            ListPreference listPreference = (ListPreference) preference;
            int index = listPreference.findIndexOfValue(stringValue);

            // Set the summary to reflect the new value.
            preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
        } else if (preference instanceof SwitchPreference) {
            ((SwitchPreference) preference).setChecked((boolean) value);
        } else if ("pref_highlight_file_size_limit".equals(key)) {
            preference.setSummary(stringValue + " KB");
        } else {
            // For all other preferences, set the summary to the value's
            // simple string representation.
            preference.setSummary(String.valueOf(stringValue));
        }

        return true;
    };

    private static void dependBindPreference(PreferenceGroup pg) {
        int count = pg.getPreferenceCount();
        Preference preference;
        String key;
        Object value;

        EditorPreferences prefercence = EditorPreferences.getInstance(pg.getContext());

        for (int i = 0; i < count; i++) {
            preference = pg.getPreference(i);
            key = preference.getKey();

            if (preference instanceof PreferenceGroup) {
                dependBindPreference((PreferenceGroup) preference);
                continue;
            }

            Class<? extends androidx.preference.Preference> cls = preference.getClass();
            if (cls.equals(androidx.preference.Preference.class))
                continue;

            value = prefercence.getValue(key);

            if (preference instanceof ListPreference) {
            } else if (preference instanceof EditTextPreference) {
                ((EditTextPreference) preference).setText(String.valueOf(value));
            } else if (preference instanceof SwitchPreference) {
                ((SwitchPreference) preference).setChecked(Boolean.parseBoolean(String.valueOf(value)));
            }

            if (!EditorPreferences.KEY_SYMBOL.equals(key))
                bindPreferenceSummaryToValue(preference);
        }
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        String key = preference.getKey();
        Object value = EditorPreferences.getInstance(preference.getContext()).getValue(key);
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, value);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preference_editor, rootKey);
        dependBindPreference(getPreferenceScreen());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ListView listView = view.findViewById(android.R.id.list);
        setDivider(null);
        setDividerHeight(0);
        /*
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {}

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
               view.findViewById(R.id.editor_appbar).setSelected(listView.canScrollVertically(-1));
            }
        });

         */
    }

}
