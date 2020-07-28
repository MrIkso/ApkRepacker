package com.mrikso.apkrepacker.fragment.dialogs.bottomsheet;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.fragment.dialogs.base.BaseBottomSheetDialogFragment;

public class DecompileOptionsDialogFragment extends BaseBottomSheetDialogFragment implements View.OnClickListener {

    public static final String TAG = "DecompileOptionsDialogFragment";

    private ItemClickListener mListener;

    public static DecompileOptionsDialogFragment newInstance() {
        return new DecompileOptionsDialogFragment();
    }

    @Nullable
    @Override
    protected View onCreateContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_decompile_options, container, false);
    }

    @Override
    protected void onContentViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onContentViewCreated(view, savedInstanceState);
        setTitle(getString(R.string.pref_decode_mode));

        getPositiveButton().setVisibility(View.GONE);
        getNegativeButton().setOnClickListener(v -> dismiss());

        view.findViewById(R.id.decompile_all).setOnClickListener(this);
        view.findViewById(R.id.decompile_all_res).setOnClickListener(this);
        view.findViewById(R.id.decompile_all_dex).setOnClickListener(this);
    }
   /* @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_decompile_options, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.decompile_all).setOnClickListener(this);
        view.findViewById(R.id.decompile_all_res).setOnClickListener(this);
        view.findViewById(R.id.decompile_all_dex).setOnClickListener(this);
    }*/

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
        mListener.onModeItemClick(view.getId());
        dismiss();
    }

    public interface ItemClickListener {
        void onModeItemClick(Integer item);
    }
}
