package com.mrikso.apkrepacker.fragment.dialogs.bottomsheet;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.ui.preferences.PreferenceHelper;

import java.util.Objects;

public class TranslateOptionsDialogFragment extends BottomSheetDialogFragment implements View.OnClickListener {

    public static final String TAG = "TranslateOptionsDialogFragment";
    private static final String DICTIONARY_PATH = "key_dictionary_path";
    private SwitchMaterial mSkipTranslated, mSkipSupportLines, mReverseTranslated;

    private PreferenceHelper mHelper;
    private ItemClickListener mListener;

    public static TranslateOptionsDialogFragment newInstance(String path) {
        TranslateOptionsDialogFragment fragment = new  TranslateOptionsDialogFragment();
        Bundle args = new Bundle();
        args.putString(DICTIONARY_PATH, path);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_translate_options, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mHelper = PreferenceHelper.getInstance(requireContext());
        mSkipTranslated = view.findViewById(R.id.sw_skip_translated);
        mSkipSupportLines = view.findViewById(R.id.sw_skip_support_lines);
        mReverseTranslated = view.findViewById(R.id.sw_reverse_dictionary);
        MaterialButton mOkBtn = view.findViewById(R.id.btn_ok);
        mOkBtn.setOnClickListener(this);
        mSkipTranslated.setChecked(mHelper.isSkipTranslated());
        mSkipSupportLines.setChecked(mHelper.isSkipSupportLines());
        mReverseTranslated.setChecked(mHelper.isReverseDictionary());
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
    public void onClick(View view) {
        Bundle args = getArguments();
        boolean skipTranslated = mSkipTranslated.isChecked();
        boolean skipSupport = mSkipSupportLines.isChecked();
        boolean reverseDictionary = mReverseTranslated.isChecked();
        mHelper.setSkipTranslated(skipTranslated);
        mHelper.setSkipSupportLines(skipSupport);
        mHelper.setReverseDictionary(reverseDictionary);
        mListener.onOkClick(Objects.requireNonNull(args).getString(DICTIONARY_PATH));
        dismiss();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface ItemClickListener {
        void onOkClick(String path);
    }
}
