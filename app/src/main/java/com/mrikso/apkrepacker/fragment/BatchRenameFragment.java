package com.mrikso.apkrepacker.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.ui.filemanager.batch.BatchRenameProcessor;
import com.mrikso.apkrepacker.ui.filemanager.batch.Variable;
import com.mrikso.apkrepacker.ui.filemanager.batch.VariableConfig;
import com.mrikso.apkrepacker.ui.filemanager.batch.VariableMatcher;
import com.mrikso.apkrepacker.utils.ViewUtils;

import java.util.ArrayList;
import java.util.Objects;

public class BatchRenameFragment extends Fragment implements  View.OnClickListener{

    private TextInputEditText mNumberStart;
    private TextInputEditText mPattern;
    private TextInputEditText mReplaceText;
    private TextInputEditText mReplaceWith;
    private MaterialCheckBox mUseRegex;
    private ChipGroup mPatternGroup;

    public BatchRenameFragment(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_batch_rename, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialButton mPreview = view.findViewById(R.id.button_preview);
        mPreview.setOnClickListener(this);
        MaterialButton mStartRename = view.findViewById(R.id.button_rename);
        mStartRename.setOnClickListener(this);
        mNumberStart = view.findViewById(R.id.numbering_start);
        mPattern = view.findViewById(R.id.pattern);
        mReplaceText = view.findViewById(R.id.replace_text);
        mReplaceWith = view.findViewById(R.id.replace_with);
        mUseRegex = view.findViewById(R.id.cb_regex);
        mPatternGroup = view.findViewById(R.id.pattern_help);
        generateHelp();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button_preview:
                showPreview();
                break;
            case R.id.button_rename:
                rename();
                break;
            default:
            String tag = (String) v.getTag();
            if (tag != null) {
                Objects.requireNonNull(mPattern.getText()).insert(mPattern.getSelectionStart(), tag);
            }

        }
    }

    private void generateHelp() {
        ArrayList<Variable> arrayList = new ArrayList<>(new VariableMatcher(VariableConfig.builder().
                setContext(requireContext()).withNumberingStartAt(getNumberingStart())
                .build()).getAll());
        for (Variable val : arrayList) {
            Chip chip = new Chip(requireContext());
            int paddingDp = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 10,
                    getResources().getDisplayMetrics()
            );
            chip.setPadding(paddingDp, paddingDp, paddingDp, paddingDp);
            chip.setText(val.describe());
            chip.setTag(val.pattern());
            chip.setCheckable(true);
            //chip.setChecked(false);
            chip.setClickable(true);
            chip.setOnClickListener(this);
            chip.setTextColor(getResources().getColor(R.color.white));
            chip.setBackgroundColor(ViewUtils.getThemeColor(requireContext(), R.attr.colorAccent));
            //add chips to view
            mPatternGroup.addView(chip);
        }
    }

    private int getNumberingStart() {
        String number = Objects.requireNonNull(mNumberStart.getText()).toString();
        if (!TextUtils.isEmpty(number)) {
            try {
                return Integer.parseInt(number);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    BatchRenameProcessor createProcessor() {
        String pattern = Objects.requireNonNull(mPattern.getText()).toString();
        String replaceText = Objects.requireNonNull(mReplaceText.getText()).toString();
        if (TextUtils.isEmpty(pattern) && TextUtils.isEmpty(replaceText)) {
            return null;
        }
        String replaceWith = Objects.requireNonNull(mReplaceWith.getText()).toString();
        BatchRenameProcessor batchRenameProcessor = new BatchRenameProcessor(pattern, VariableConfig.builder().withNumberingStartAt(getNumberingStart()).build());
        if (!TextUtils.isEmpty(replaceText)) {
            batchRenameProcessor.replaceText(replaceText, replaceWith, mUseRegex.isChecked());
        }
        return batchRenameProcessor;
    }

    private void showPreview(){

    }

    private void rename(){

    }
}
