package com.mrikso.apkrepacker.fragment.dialogs;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.IntegerRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.mrikso.apkrepacker.R;


public class FindFileOptionDialogFragment extends BottomSheetDialogFragment implements View.OnClickListener {

    public static final String TAG = "FindFileOptionDialogFragment";

    private ItemClickListener mListener;
    private boolean mStringsMode;

    public static FindFileOptionDialogFragment newInstance() {
        return new FindFileOptionDialogFragment();
    }

    public void setItemClickListener(ItemClickListener listener) {
        mListener = listener;
    }

    public void setIsStringMode(boolean mode){
        mStringsMode = mode;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_find_file_options, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView tv_openWith = view.findViewById(R.id.replace_in_file);
        tv_openWith.setVisibility(mStringsMode ? View.VISIBLE : View.GONE);
        tv_openWith.setOnClickListener(this);
        view.findViewById(R.id.open_with).setOnClickListener(this);
        view.findViewById(R.id.open_in_editor).setOnClickListener(this);
        view.findViewById(R.id.copy_path).setOnClickListener(this);
    }

    @SuppressLint("ResourceType")
    @Override
    public void onClick(View view) {
        mListener.onFileItemClick(view.getId());
        dismiss();
    }

    public interface ItemClickListener {
        void onFileItemClick(@IntegerRes int item);
    }
}
