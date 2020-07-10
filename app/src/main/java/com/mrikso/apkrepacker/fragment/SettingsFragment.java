package com.mrikso.apkrepacker.fragment;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;

import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.ui.keydialog.KeystorePreference;
import com.mrikso.apkrepacker.ui.keydialog.KeystorePreferenceFragmentDialog;
import com.mrikso.apkrepacker.ui.prererence.PreferenceHelper;
import com.mrikso.apkrepacker.ui.prererence.PreferenceKeys;
import com.mrikso.apkrepacker.utils.AppUtils;
import com.mrikso.apkrepacker.utils.Theme;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    private PreferenceHelper mHelper;
    private ListPreference mThemePref;

    private Preference mHomeDirPref;
    private ListPreference mDecodeModePref;
    private SwitchPreference mAutoThemeSwitch;
    private SwitchPreference mUseCustomKeySwitch;
    private KeystorePreference mKeystorePref;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        //Inject current auto theme status since it isn't managed by PreferencesKeys.AUTO_THEME key
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        prefs.edit().putBoolean(PreferenceKeys.KEY_AUTO_THEME, Theme.getInstance(requireContext()).getThemeMode() == Theme.Mode.AUTO_LIGHT_DARK).apply();
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.peference, rootKey);
        mHelper = PreferenceHelper.getInstance(requireContext());
        mThemePref = findPreference(PreferenceKeys.KEY_THEME);
        updateThemeSummary();
        if (Theme.getInstance(requireContext()).getThemeMode() != Theme.Mode.CONCRETE) {
            mThemePref.setVisible(false);
        }
        mAutoThemeSwitch = findPreference(PreferenceKeys.KEY_AUTO_THEME);
        mAutoThemeSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
            boolean value = (boolean) newValue;
            if (value) {
                if (!AppUtils.apiIsAtLeast(Build.VERSION_CODES.Q))
                    Theme.getInstance(requireContext()).setLightTheme(0);
                    Theme.getInstance(requireContext()).setDarkTheme(1);
                   // SimpleAlertDialogFragment.newInstance(requireContext(), R.string.settings_main_auto_theme, R.string.settings_main_auto_theme_pre_q_warning).show(getChildFragmentManager(), null);

                Theme.getInstance(requireContext()).setMode(Theme.Mode.AUTO_LIGHT_DARK);
            } else {
                Theme.getInstance(requireContext()).setMode(Theme.Mode.CONCRETE);
            }

            //Hack to not mess with hiding/showing preferences manually
            requireActivity().recreate();
            return true;
        });

        mDecodeModePref = findPreference(PreferenceKeys.KEY_DECODING_MODE);
        updateDecodeModePrefSummary();

        mHomeDirPref = findPreference(PreferenceKeys.KEY_DECODING_FOLDER);
        updateHomeDirPrefSummary();

        mKeystorePref = findPreference(PreferenceKeys.KEY_KEYSTORE_FILE);
        mKeystorePref.setVisible(mHelper.isCustomSign());

        mUseCustomKeySwitch = findPreference(PreferenceKeys.KEY_USE_CUSTOM_SIGN);
        mUseCustomKeySwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                    boolean value = (boolean) newValue;
                    if (value) {
                        mKeystorePref.setVisible(true);
                    } else {
                        mKeystorePref.setVisible(false);
                    }
                    return true;
                });

        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
       setDividerHeight(0);
        setDivider(null);
       /*
       ListView listView = getActivity().findViewById(android.R.id.list);
       // listView.setDivider(null);
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

        */
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    private void updateThemeSummary(){
        mThemePref.setSummary(Theme.getInstance(requireContext()).getConcreteTheme().getName(requireContext()));
    }

    private void updateHomeDirPrefSummary() {
        mHomeDirPref.setSummary(mHelper.getDecodingPath());
    }

    private void updateDecodeModePrefSummary() {
        mDecodeModePref.setSummary(mDecodeModePref.getEntry());
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case PreferenceKeys.KEY_THEME:
                Theme.getInstance(requireContext()).setConcreteTheme(mThemePref.findIndexOfValue(mThemePref.getValue()));
                updateThemeSummary();
                break;
            case PreferenceKeys.KEY_DECODING_MODE:
                updateDecodeModePrefSummary();
                break;
            case PreferenceKeys.KEY_DECODING_FOLDER:
                updateHomeDirPrefSummary();
                break;
        }
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference){
        if(preference instanceof KeystorePreference){
          KeystorePreferenceFragmentDialog keystorePreferenceFragmentDialog = KeystorePreferenceFragmentDialog.newInstance(preference.getKey());
          keystorePreferenceFragmentDialog.setTargetFragment(this, 0);
          keystorePreferenceFragmentDialog.show(getParentFragmentManager(), null);
        }
        else {
            super.onDisplayPreferenceDialog(preference);
        }
    }
}
