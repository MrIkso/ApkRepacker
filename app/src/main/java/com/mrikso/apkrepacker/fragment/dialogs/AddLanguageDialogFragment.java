package com.mrikso.apkrepacker.fragment.dialogs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.jecelyin.common.view.StatusBarUtil;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.utils.translation.LanguageMaps;
import com.mrikso.apkrepacker.utils.translation.Languages;

import java.util.Locale;

public class AddLanguageDialogFragment extends BottomSheetDialogFragment implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    public static final String TAG = "AddLanguageDialogFragment";
    private AppCompatSpinner languageSpinner;
    private TextInputEditText languageCode;
    private MaterialButton addLang;
    private String[] langCodes;
    private String[] langNames;

    private ItemClickListener mListener;

    public static AddLanguageDialogFragment newInstance() {
        return new AddLanguageDialogFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_add_new_language, container, false);
        addLang = view.findViewById(R.id.btn_add_lang_ok);
        languageSpinner = view.findViewById(R.id.language_spinner);
        languageCode = view.findViewById(R.id.language_code);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view.getContext(), Languages.languages, Languages.codes);
        //  view.findViewById(R.id.decompile_app).setOnClickListener(this);
        // view.findViewById(R.id.install_app).setOnClickListener(this);
        addLang.setOnClickListener(this);
    }


    private void initView(Context context, String[] lang, String[] langCodes) {
        langNames = lang;
        this.langCodes = langCodes;
        int size = LanguageMaps.getMapSize();
        if (this.langCodes == null || this.langNames == null) {
            this.langCodes = new String[size];
            this.langNames = new String[size];
            LanguageMaps.addLang(this.langCodes, this.langNames);
        }

        ArrayAdapter arrayAdapter = new ArrayAdapter(context, android.R.layout.simple_spinner_item, langNames);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languageSpinner.setAdapter(arrayAdapter);
        int selected = selectedLang("-" + Locale.getDefault().getLanguage());
        if (selected != -1) {
            languageSpinner.setSelection(selected);
        }
        languageSpinner.setOnItemSelectedListener(this);
    }

    private int selectedLang(String lang) {
        for (int i = 0; i < langCodes.length; i++) {
            if (langCodes[i].startsWith(lang)) {
                return i;
            }
        }
        return -1;
    }

    public final void setCode(int i) {
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

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        setCode(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public interface ItemClickListener {
        void onAddLangClick(String code);
    }
}

