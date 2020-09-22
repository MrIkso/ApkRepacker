package com.mrikso.apkrepacker.fragment.dialogs.bottomsheet;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.IntegerRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatTextView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.autotranslator.translator.TranslateItem;
import com.mrikso.apkrepacker.ide.editor.content.ClipboardCompat;
import com.mrikso.apkrepacker.ui.publicxml.PublicXmlParser;
import com.mrikso.apkrepacker.utils.FileUtil;
import com.mrikso.apkrepacker.utils.ProjectUtils;
import com.mrikso.apkrepacker.utils.StringUtils;

import java.io.File;


public class TranslateStringDialogFragment extends BottomSheetDialogFragment implements View.OnClickListener {

    public static final String TAG = "TranslateStringDialogFragment";

    private ItemClickListener mListener;
    private String mKey;
    private String mNewValue;
    private String mOldValue;
    private TextInputEditText mOldEdit;
    private TextInputEditText mNewEdit;
    private AppCompatImageButton mDeleteString;
    private int mItemPosition;

    public static TranslateStringDialogFragment newInstance() {
        return new TranslateStringDialogFragment();
    }

    public void setItemClickListener(ItemClickListener listener) {
        mListener = listener;
    }

    public void setData(TranslateItem item, int position){
        mKey = item.name;
        mOldValue = item.originValue;
        mNewValue = item.translatedValue;
        mItemPosition = position;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_translate_string, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MaterialButton ok = view.findViewById(R.id.btn_add_lang_ok);
        ok.setOnClickListener(this);
        AppCompatImageButton copyOldValue = view.findViewById(R.id.button_copy);
        copyOldValue.setOnClickListener(this);
        copyOldValue.setOnLongClickListener(view1 -> {
            new Thread(() -> {
                PublicXmlParser mXmlParser = new PublicXmlParser(new File(ProjectUtils.getProjectPath() + "/res/values/public.xml"));
                getActivity().runOnUiThread(() -> {
                    StringUtils.setClipboard(requireContext(), mXmlParser.getIdByName(mKey), true);
                });
            }).start();
            return true;
        });
        AppCompatImageButton pasteValue = view.findViewById(R.id.button_paste);
        pasteValue.setOnClickListener(this);
        AppCompatImageButton clearNewValue = view.findViewById(R.id.button_clear);
        clearNewValue.setOnClickListener(this);
        TextInputLayout textInputLayout = view.findViewById(R.id.text_input_layout_old);
        textInputLayout.setHint(mKey);
        mDeleteString = view.findViewById(R.id.delete_string);
        mDeleteString.setOnClickListener(this);
        mOldEdit = view.findViewById(R.id.old_value);
        mOldEdit.setText(mOldValue);
        mNewEdit = view.findViewById(R.id.new_value);
        mNewEdit.setText(mNewValue);
    }

    @SuppressLint("ResourceType")
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_add_lang_ok:
                mListener.onTranslateClicked(mNewEdit.getText().toString(), mItemPosition);
                dismiss();
                break;
            case R.id.button_clear:
                mNewEdit.setText("");
                break;
            case R.id.button_paste:
                mNewEdit.setText(StringUtils.getClipboard(requireContext()));
                break;
            case R.id.button_copy:
                StringUtils.setClipboard(requireContext(), mOldValue, true);
                break;
            case R.id.delete_string:
                mListener.onDeleteString(mItemPosition);
                dismiss();
                break;
        }

    }

    public interface ItemClickListener {
        void onTranslateClicked(String value, int key);

        void onDeleteString(int key);
    }
}
