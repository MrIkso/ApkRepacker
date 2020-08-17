package com.mrikso.apkrepacker.fragment.dialogs.bottomsheet;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.jecelyin.common.utils.UIUtils;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.utils.translation.LanguageMaps;
import com.mrikso.apkrepacker.utils.translation.Languages;

import java.util.Locale;

public class AddLanguageDialogFragment extends BottomSheetDialogFragment implements View.OnClickListener {

    public static final String TAG = "AddLanguageDialogFragment";
    private TextInputEditText languageCode, et_lang;
    private AppCompatTextView title;
    private MaterialButton addLang;
    private String[] langCodes;
    private String[] langNames;

    private ItemClickListener mListener;

    public static AddLanguageDialogFragment newInstance() {
        return new AddLanguageDialogFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_add_new_language, container, false);
        title = view.findViewById(R.id.title_text_view);
        addLang = view.findViewById(R.id.btn_add_lang_ok);
        languageCode = view.findViewById(R.id.language_code);
        et_lang = view.findViewById(R.id.et_lang);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(requireContext());
        addLang.setOnClickListener(this);
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
        title.setText(mListener.setTitle());
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
        mListener.onAddLangClick(languageCode.getText().toString());
        dismiss();
    }

    public interface ItemClickListener {
        void onAddLangClick(String code);
        @StringRes
        int setTitle();
    }
}

