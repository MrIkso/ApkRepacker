package com.mrikso.apkrepacker.fragment.dialogs.bottomsheet;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.fragment.dialogs.base.BaseBottomSheetDialogFragment;

public class ApkOptionsDialogFragment extends BaseBottomSheetDialogFragment implements View.OnClickListener {

    public static final String TAG = "ApkOptionsDialogFragment";

    private ItemClickListener mListener;

    public static ApkOptionsDialogFragment newInstance() {
        return new ApkOptionsDialogFragment();
    }

    @Nullable
    @Override
    protected View onCreateContentView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_apk_options, container, false);
    }

    @Override
    protected void onContentViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onContentViewCreated(view, savedInstanceState);
        setTitle(getString(R.string.title_apk_options));

        getPositiveButton().setVisibility(View.GONE);
        getNegativeButton().setOnClickListener(v -> dismiss());

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

    @Override public void onClick(View view) {
        mListener.onApkItemClick(view.getId());
        dismiss();
    }

    public interface ItemClickListener {
        void onApkItemClick(Integer item);
    }
}

