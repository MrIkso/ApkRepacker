package com.mrikso.apkrepacker.fragment.dialogs;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;

import com.jecelyin.common.utils.UIUtils;
import com.jecelyin.editor.v2.io.LocalFileWriter;
import com.jecelyin.editor.v2.utils.ExtGrep;
import com.jecelyin.editor.v2.utils.GrepBuilder;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.fragment.dialogs.base.BaseBottomSheetDialogFragment;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class ReplaceInFileDialogFragment extends BaseBottomSheetDialogFragment {

    public static final String TAG = "ReplaceInFileDialogFragment";
    private static final String FILE = "file";
    private static final String SEARCH_TEXT = "search_text";

    private AppCompatEditText mSearchText;
    private AppCompatEditText mReplaceText;
    private CheckBox mCaseSensitiveCheckBox;
    private CheckBox mRegexCheckBox;
    private CheckBox mWholeWordsOnlyCheckBox;

    private String mDefaultSearchText;

    private File mInputFile;

    private OnReplacedInterface mReplacedInterface;

    public static ReplaceInFileDialogFragment newInstance() {
        return new ReplaceInFileDialogFragment();
    }

    public static ReplaceInFileDialogFragment newInstance(String searchText, String file) {
        ReplaceInFileDialogFragment fragment = new ReplaceInFileDialogFragment();

        Bundle args = new Bundle();
        args.putString(SEARCH_TEXT, searchText);
        args.putString(FILE, file);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            mDefaultSearchText = args.getString(SEARCH_TEXT);
            mInputFile = new File(Objects.requireNonNull(args.getString(FILE)));
        }
    }

    @Nullable
    @Override
    protected View onCreateContentView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_dialog_search_replace, container, false);
    }

    @Override
    protected void onContentViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        revealBottomSheet();
        setTitle(R.string.replace_in_file_title);
        getNegativeButton().setOnClickListener(v -> dismiss());
        getPositiveButton().setOnClickListener(v -> startReplace());

        mSearchText = view.findViewById(R.id.search_text);
        mReplaceText = view.findViewById(R.id.replace_text);
        mCaseSensitiveCheckBox = view.findViewById(R.id.ignore_case_cb);
        mRegexCheckBox = view.findViewById(R.id.regular_exp_cb);
        mWholeWordsOnlyCheckBox = view.findViewById(R.id.whole_words_only_cb);

        initView();
    }

    public void setItemClickListener(OnReplacedInterface listener) {
        mReplacedInterface = listener;
    }

    private void initView() {

        mSearchText.setText(mDefaultSearchText);
        /*
        mSearchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //revealBottomSheet();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
             //   revealBottomSheet();
            }

            @Override
            public void afterTextChanged(Editable s) {
                mViewModel.setSearchEditData(s.toString());
               // mClearBtn.setVisibility(s.toString().isEmpty() ? View.GONE : View.VISIBLE);
                //revealBottomSheet();
            }
        });

        mReplaceText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mViewModel.setReplaceEditData(s.toString());
               // mClearBtnReplace.setVisibility(s.toString().isEmpty() ? View.GONE : View.VISIBLE);
               // revealBottomSheet();
            }
        });
        mClearBtn.setOnClickListener(v -> mSearchText.setText(""));
        mClearBtnReplace.setOnClickListener(v -> mReplaceText.setText(""));

        mViewModel.getSearchEditData().observe(this, s -> {
          //  mClearBtn.setVisibility(s.isEmpty() ? View.GONE : View.VISIBLE);
            revealBottomSheet();
        });
        mViewModel.getReplaceEditData().observe(this, s -> mClearBtnReplace.setVisibility(s.isEmpty() ? View.GONE : View.VISIBLE));
*/
    }

    private void startReplace() {
        String findText = mSearchText.getText().toString();
        String replaceText = mReplaceText.getText().toString();
        if (TextUtils.isEmpty(findText)) {
            UIUtils.toast(requireContext(), R.string.cannot_be_empty);
            return;
        }
        GrepBuilder builder = GrepBuilder.start();
        if (!mCaseSensitiveCheckBox.isChecked()) {
            builder.ignoreCase();
        }
        if (mWholeWordsOnlyCheckBox.isChecked()) {
            builder.wordRegex();
        }
        builder.setRegex(findText, mRegexCheckBox.isChecked());
        ExtGrep mGrep = builder.build();

        new Thread(() -> {
            try {
                FileInputStream input = new FileInputStream(mInputFile);
                String result = mGrep.replaceAll(IOUtils.toString(input, StandardCharsets.UTF_8), replaceText);
                LocalFileWriter writer = new LocalFileWriter(mInputFile, StandardCharsets.UTF_8.toString());
                writer.writeToFile(result);
                getActivity().runOnUiThread(() -> {
                    UIUtils.toast(requireContext(), getString(R.string.replaced_successful));
                    if (mReplacedInterface != null) {
                        mReplacedInterface.onReplaced();
                    }
                });
            } catch (IOException io) {
                io.printStackTrace();
                getActivity().runOnUiThread(() -> {
                    UIUtils.toast(requireContext(), getString(R.string.error));
                });

            }
        }).start();
        dismiss();
    }

    public interface OnReplacedInterface{
        void onReplaced();
    }
}
