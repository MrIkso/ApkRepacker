package com.mrikso.apkrepacker.fragment.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.mrikso.apkrepacker.R;

public class FileOptionsDialogFragment extends BottomSheetDialogFragment implements View.OnClickListener {

    public static final String TAG = "FileOptionsDialogFragment";

    private FileItemClickListener mListener;
    private static boolean openInEditor;

    public static FileOptionsDialogFragment newInstance(boolean openInEditor) {
        FileOptionsDialogFragment.openInEditor = openInEditor;
        return new FileOptionsDialogFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_file_options, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView tv_openInEditor = view.findViewById(R.id.open_in_editor);
        tv_openInEditor.setVisibility(openInEditor ? View.VISIBLE : View.GONE);
        tv_openInEditor.setOnClickListener(this);
        view.findViewById(R.id.rename_file).setOnClickListener(this);
        view.findViewById(R.id.add_new_folder).setOnClickListener(this);
        view.findViewById(R.id.delete_file).setOnClickListener(this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        final Fragment parent = getParentFragment();
        if (parent != null) {
            mListener = (FileItemClickListener) parent;
        } else {
            mListener = (FileItemClickListener) context;
        }
        //  if (context instanceof FileItemClickListener) {

        //  } else {
        //   throw new RuntimeException(context.toString()
        //         + " must implement ItemClickListener");
        // }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View view) {
        mListener.onFileItemClick(view.getId());
        dismiss();
    }

    public interface FileItemClickListener {
        void onFileItemClick(Integer item);
    }
}
