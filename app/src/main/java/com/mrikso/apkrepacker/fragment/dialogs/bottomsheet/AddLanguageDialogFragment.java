package com.mrikso.apkrepacker.fragment.dialogs.bottomsheet;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.jecelyin.common.utils.UIUtils;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.fragment.dialogs.base.BaseBottomSheetDialogFragment;
import com.mrikso.apkrepacker.utils.translation.LanguageMaps;
import com.mrikso.apkrepacker.utils.translation.Languages;

import java.util.Locale;
import java.util.Objects;

public class AddLanguageDialogFragment extends BaseBottomSheetDialogFragment implements View.OnClickListener {

    public static final String TAG = "AddLanguageDialogFragment";
    private TextInputEditText languageCode, et_lang;
    private String[] langCodes;
    private String[] langNames;

    private ItemClickListener mListener;

    public static AddLanguageDialogFragment newInstance() {
        return new AddLanguageDialogFragment();
    }

    @Nullable
    @Override
    protected View onCreateContentView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_add_new_language, container, false);
    }

    @Override
    protected void onContentViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onContentViewCreated(view, savedInstanceState);

        getPositiveButton().setOnClickListener(this);
        getNegativeButton().setOnClickListener(v -> dismiss());

        languageCode = view.findViewById(R.id.language_code);
        et_lang = view.findViewById(R.id.et_lang);

        initView(requireContext());

    }

    private void initView(Context context) {
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
        setTitle(mListener.setTitle());
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
    public void onAttach(Context context) {
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
        mListener.onAddLangClick(Objects.requireNonNull(languageCode.getText()).toString());
        dismiss();
    }

    public interface ItemClickListener {
        void onAddLangClick(String code);
        @StringRes int setTitle();
    }
}

