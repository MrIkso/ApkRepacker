package com.mrikso.apkrepacker.fragment.dialogs;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.Fragment;

import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.utils.BundleUtils;
import com.mrikso.apkrepacker.utils.Theme;

import de.psdev.licensesdialog.LicensesDialog;
import de.psdev.licensesdialog.NoticesXmlParser;
import de.psdev.licensesdialog.model.Notice;
import de.psdev.licensesdialog.model.Notices;

public class LicensesDialogFragment extends AppCompatDialogFragment {

    private static final String KEY_PREFIX = LicensesDialogFragment.class.getName() + '.';

    private static final String STATE_NOTICES = KEY_PREFIX + "NOTICES";

    @NonNull
    private Notices mNotices;

    @NonNull
    public static LicensesDialogFragment newInstance() {
        //noinspection deprecation
        return new LicensesDialogFragment();
    }

    public static void show(@NonNull AppCompatActivity activity) {
        LicensesDialogFragment.newInstance().show(activity.getSupportFragmentManager(), null);
    }

    public static void show(@NonNull Fragment fragment) {
        LicensesDialogFragment.newInstance().show(fragment.getChildFragmentManager(), null);
    }

    /**
     * @deprecated Use {@link #newInstance()} instead.
     */
    public LicensesDialogFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mNotices = BundleUtils.getParcelable(savedInstanceState, STATE_NOTICES);
        } else {
            try {
                mNotices = NoticesXmlParser.parse(requireContext().getResources().openRawResource(R.raw.licenses));
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(STATE_NOTICES, mNotices);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // setIncludeOwnLicense(true) will modify our notices instance.
        Notices notices = new Notices();
        for (Notice notice : mNotices.getNotices()) {
            notices.addNotice(notice);
        }
        boolean isLight = !Theme.getInstance(requireContext()).getCurrentTheme().isDark();
        int htmlStyleRes = isLight ? R.string.about_licenses_html_style_light : R.string.about_licenses_html_style_dark;
        return new LicensesDialog.Builder(requireContext())
                .setThemeResourceId(isLight ? R.style.ColorPickerDialog : R.style.ColorPickerDialog_Dark)
                .setTitle(R.string.about_source_licenses)
                .setNotices(notices)
                .setIncludeOwnLicense(true)
                .setNoticesCssStyle(htmlStyleRes)
                .setCloseText(R.string.close)
                .build()
                .create();
    }
}
