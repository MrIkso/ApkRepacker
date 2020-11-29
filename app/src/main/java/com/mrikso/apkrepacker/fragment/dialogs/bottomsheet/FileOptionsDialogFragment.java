package com.mrikso.apkrepacker.fragment.dialogs.bottomsheet;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.activity.CodeEditorActivity;
import com.mrikso.apkrepacker.utils.IntentUtils;
import com.mrikso.apkrepacker.utils.StringUtils;

import java.io.File;

public class FileOptionsDialogFragment extends BottomSheetDialogFragment implements View.OnClickListener {

    public static final String TAG = "FileOptionsDialogFragment";

   // private FileItemClickListener mListener;
    private static String mFilePath;
    private static int mLineNumber;

    public static FileOptionsDialogFragment newInstance(String filePath, int lineNumber) {
        mFilePath = filePath;
        mLineNumber = lineNumber;
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
     /*   tv_openInEditor.setVisibility(openInEditor ? View.VISIBLE : View.GONE);*/
        tv_openInEditor.setOnClickListener(this);

        TextView tv_openWith = view.findViewById(R.id.open_with);
     //   tv_openWith.setVisibility(openWith ? View.VISIBLE : View.GONE);
        tv_openWith.setOnClickListener(this);

        view.findViewById(R.id.open_with).setOnClickListener(this);
        view.findViewById(R.id.action_copy_path).setOnClickListener(this);
    //    view.findViewById(R.id.add_new_folder).setOnClickListener(this);
      //  view.findViewById(R.id.delete_file).setOnClickListener(this);
    }

  /*  @Override
    public void onAttach(@NonNull Context context) {
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
    }*/

    @Override
    public void onDetach() {
        super.onDetach();
       // mListener = null;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.open_in_editor:
                Intent intent = new Intent(getActivity(), CodeEditorActivity.class);
                intent.putExtra("filePath",mFilePath);
                intent.putExtra("offset", mLineNumber);
                requireContext().startActivity(intent);
                dismiss();
                break;
            case R.id.open_with:
                startActivity(IntentUtils.openFileWithIntent(new File(mFilePath)));
                dismiss();
                break;
            case R.id.action_copy_path:
                StringUtils.setClipboard(requireContext(), mFilePath, true);
                break;
        }
       // mListener.onFileItemClick(view.getId());

    }

   /* public interface FileItemClickListener {
        void onFileItemClick(@IdRes int item);
    }*/
}
