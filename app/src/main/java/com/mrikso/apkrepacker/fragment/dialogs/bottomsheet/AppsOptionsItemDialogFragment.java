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

import org.jetbrains.annotations.NotNull;

public class AppsOptionsItemDialogFragment extends BaseBottomSheetDialogFragment implements View.OnClickListener {

    public static final String TAG = "ApkOptionsDialogFragment";

    private ItemClickListener mListener;

    public static AppsOptionsItemDialogFragment newInstance() {
        return new AppsOptionsItemDialogFragment();
    }

    @Nullable
    @Override
    protected View onCreateContentView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_apps, container, false);
    }

    @Override
    protected void onContentViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onContentViewCreated(view, savedInstanceState);
        setTitle(getString(R.string.title_apk_options));

        getPositiveButton().setVisibility(View.GONE);
        getNegativeButton().setOnClickListener(v -> dismiss());

        view.findViewById(R.id.decompile_app).setOnClickListener(this);
        view.findViewById(R.id.simple_edit_apk).setOnClickListener(this);
        view.findViewById(R.id.goto_settings_app).setOnClickListener(this);
        view.findViewById(R.id.uninstall_app).setOnClickListener(this);
    }

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        final Fragment parent= getParentFragment();
        if(parent!=null){
            mListener = (ItemClickListener) parent;
        }else{
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
        mListener.onAppsItemClick(view.getId());
        dismiss();
    }

    public interface ItemClickListener {
        void onAppsItemClick(Integer item);
    }
}


