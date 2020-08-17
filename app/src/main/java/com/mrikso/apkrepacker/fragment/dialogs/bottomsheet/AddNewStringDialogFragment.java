package com.mrikso.apkrepacker.fragment.dialogs.bottomsheet;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.jecelyin.common.utils.UIUtils;
import com.mrikso.apkrepacker.R;


public class AddNewStringDialogFragment extends BottomSheetDialogFragment implements View.OnClickListener {

    public static final String TAG = "AddNewStringDialogFragment";

    private ItemClickListener mListener;
    private TextInputEditText mKeyEdit;
    private TextInputEditText mValueEdit;

    public static AddNewStringDialogFragment newInstance() {
        return new AddNewStringDialogFragment();
    }

    public void setItemClickListener(ItemClickListener listener) {
        mListener = listener;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_add_new_string, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MaterialButton ok = view.findViewById(R.id.btn_add_lang_ok);
        ok.setOnClickListener(this);
        mKeyEdit = view.findViewById(R.id.new_key);
        mValueEdit = view.findViewById(R.id.new_value);
    }

    @SuppressLint("ResourceType")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_add_lang_ok:
                String mKey = mKeyEdit.getText().toString();
                if (!TextUtils.isEmpty(mKey)) {
                    mListener.onAddStringClicked(mKeyEdit.getText().toString(), mValueEdit.getText().toString());
                    dismiss();
                } else {
                    UIUtils.toast(requireContext(), R.string.cannot_be_empty);
                }
                break;
        }

    }

    public interface ItemClickListener {
        void onAddStringClicked(String key, String value);
    }
}
