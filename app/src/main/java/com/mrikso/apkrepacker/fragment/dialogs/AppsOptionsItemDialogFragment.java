package com.mrikso.apkrepacker.fragment.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.mrikso.apkrepacker.R;

public class AppsOptionsItemDialogFragment extends BottomSheetDialogFragment
        implements View.OnClickListener {

    public static final String TAG = "ApkOptionsDialogFragment";

    private ItemClickListener mListener;

    public static AppsOptionsItemDialogFragment newInstance() {
        return new AppsOptionsItemDialogFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // setStyle(STYLE_NORMAL, );
        return inflater.inflate(R.layout.bottom_sheet_apps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.decompile_app).setOnClickListener(this);
        view.findViewById(R.id.simple_edit_apk).setOnClickListener(this);
        view.findViewById(R.id.goto_settings_app).setOnClickListener(this);
        view.findViewById(R.id.uninstall_app).setOnClickListener(this);
    }

    @Override
    public void onAttach(Context context) {
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


