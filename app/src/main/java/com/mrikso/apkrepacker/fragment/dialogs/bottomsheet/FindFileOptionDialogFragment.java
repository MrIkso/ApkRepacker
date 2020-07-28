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

import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.fragment.dialogs.base.BaseBottomSheetDialogFragment;


public class FindFileOptionDialogFragment extends BaseBottomSheetDialogFragment implements View.OnClickListener {

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
    protected View onCreateContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_find_file_options, container, false);
    }

    @Override
    protected void onContentViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onContentViewCreated(view, savedInstanceState);

        setTitle(R.string.title_file_options);
        getPositiveButton().setVisibility(View.GONE);
        getNegativeButton().setOnClickListener(v -> dismiss());

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
