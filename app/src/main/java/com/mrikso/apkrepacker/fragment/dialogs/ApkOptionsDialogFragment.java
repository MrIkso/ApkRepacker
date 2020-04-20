package com.mrikso.apkrepacker.fragment.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.mrikso.apkrepacker.R;

public class ApkOptionsDialogFragment extends BottomSheetDialogFragment implements View.OnClickListener {

    public static final String TAG = "ApkOptionsDialogFragment";

    private ItemClickListener mListener;

    public static ApkOptionsDialogFragment newInstance() {
        return new ApkOptionsDialogFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
       // setStyle(STYLE_NORMAL, );
        return inflater.inflate(R.layout.bottom_sheet_apk_options, container, false);
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.decompile_app).setOnClickListener(this);
        view.findViewById(R.id.simple_edit_apk).setOnClickListener(this);
        view.findViewById(R.id.sign_app).setOnClickListener(this);
        view.findViewById(R.id.install_app).setOnClickListener(this);
        view.findViewById(R.id.set_as_framework_app).setOnClickListener(this);
        view.findViewById(R.id.delete_item).setOnClickListener(this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ItemClickListener) {
            mListener = (ItemClickListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement ItemClickListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override public void onClick(View view) {
        mListener.onApkItemClick(view.getId());
        dismiss();
    }

    public interface ItemClickListener {
        void onApkItemClick(Integer item);
    }
}

