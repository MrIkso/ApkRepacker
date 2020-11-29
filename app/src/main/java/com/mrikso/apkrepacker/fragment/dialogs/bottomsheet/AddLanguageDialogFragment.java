package com.mrikso.apkrepacker.fragment.dialogs.bottomsheet;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.jecelyin.common.utils.UIUtils;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.ui.preferences.PreferenceHelper;
import com.mrikso.apkrepacker.utils.translation.LanguageMaps;
import com.mrikso.apkrepacker.utils.translation.Languages;

import java.util.Locale;
import java.util.Objects;

public class AddLanguageDialogFragment extends BottomSheetDialogFragment implements View.OnClickListener {

    public static final String TAG = "AddLanguageDialogFragment";
    private static final String TRANSLATE_MODE = "is_translate_mode";
    private TextInputEditText languageCode, et_lang;
    private AppCompatTextView mTitle;
    private MaterialButton addLang;
    private SwitchMaterial mSkipTranslated, mSkipSupportLines;
    private String[] langCodes;
    private String[] langNames;

    private PreferenceHelper mHelper;
    private ItemClickListener mListener;
    private boolean mAutotranslate;

    public static AddLanguageDialogFragment newInstance(boolean translate) {
        AddLanguageDialogFragment fragment = new  AddLanguageDialogFragment();
        Bundle args = new Bundle();
        args.putBoolean(TRANSLATE_MODE, translate);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_add_new_language, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mHelper = PreferenceHelper.getInstance(requireContext());
        mTitle = view.findViewById(R.id.title_text_view);
        addLang = view.findViewById(R.id.btn_add_lang_ok);
        languageCode = view.findViewById(R.id.language_code);
        et_lang = view.findViewById(R.id.et_lang);
        mSkipTranslated = view.findViewById(R.id.sw_skip_translated);
        mSkipSupportLines = view.findViewById(R.id.sw_skip_support_lines);

        initView(requireContext());
        addLang.setOnClickListener(this);
    }

    private void initView(Context context) {
        mSkipTranslated.setChecked(mHelper.isSkipTranslated());
        mSkipSupportLines.setChecked(mHelper.isSkipSupportLines());
        langNames = Languages.languages;
        this.langCodes = Languages.codes;
        int size = LanguageMaps.getMapSize();
        if (this.langCodes == null || this.langNames == null) {
            this.langCodes = new String[size];
            this.langNames = new String[size];
            LanguageMaps.addLang(this.langCodes, this.langNames);
        }

        String selected = selectedLang("-" + Locale.getDefault().getLanguage());
        int selectedCode = selectedLangCode("-" + Locale.getDefault().getLanguage());
        if (!selected.equals("") && selectedCode != -1) {
            et_lang.setText(selected);
            languageCode.setText(this.langCodes[selectedCode]);
        }
        Bundle args = getArguments();
        if (args != null) {
            mAutotranslate = args.getBoolean(TRANSLATE_MODE, false);
            if(mAutotranslate){
                mTitle.setText(R.string.action_auto_translate_lang);
                mSkipTranslated.setVisibility(View.VISIBLE);
                mSkipSupportLines.setVisibility(View.VISIBLE);
            }
            else
            mTitle.setText(R.string.action_add_new_lang);
        }
        et_lang.setOnClickListener(v -> showListDialog(v.getContext(), langNames));
    }

    private String selectedLang(String lang) {
        for (int i = 0; i < langCodes.length; i++) {
            if (langCodes[i].startsWith(lang)) {
                return langNames[i];
            }
        }
        return "";
    }

    private int selectedLangCode(String lang) {
        for (int i = 0; i < langCodes.length; i++) {
            if (langCodes[i].startsWith(lang)) {
                return i;
            }
        }
        return -1;
    }

    private void showListDialog(Context context, String[] list) {
        UIUtils.showListDialog(context, 0, 0, list, 0, new UIUtils.OnListCallback() {

            @Override
            public void onSelect(MaterialDialog dialog, int which) {
                setCode(which);
            }
        }, null);
    }

    public final void setCode(int i) {
        et_lang.setText(this.langNames[i]);
        languageCode.setText(this.langCodes[i]);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        final Fragment parent = getParentFragment();
        if (parent != null) {
            mListener = (ItemClickListener) parent;
        } else {
            mListener = (ItemClickListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View view) {
        boolean skipTranslated = mSkipTranslated.isChecked();
        boolean skipSupport = mSkipSupportLines.isChecked();
        mHelper.setSkipTranslated(skipTranslated);
        mHelper.setSkipSupportLines(skipSupport);
        mListener.onAddLangClick(Objects.requireNonNull(languageCode.getText()).toString(),mAutotranslate, skipTranslated, skipSupport);
        dismiss();
    }

    public interface ItemClickListener {
        void onAddLangClick(String code, boolean autotranslate, boolean skipTranslated, boolean skipSupport);
    }
}

