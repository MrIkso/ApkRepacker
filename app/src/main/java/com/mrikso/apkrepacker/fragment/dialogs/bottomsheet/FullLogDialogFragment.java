package com.mrikso.apkrepacker.fragment.dialogs.bottomsheet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.jecelyin.common.utils.UIUtils;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.fragment.dialogs.base.BaseBottomSheetDialogFragment;
import com.mrikso.apkrepacker.ui.preferences.PreferenceHelper;
import com.mrikso.apkrepacker.utils.StringUtils;

import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class FullLogDialogFragment extends BottomSheetDialogFragment {

    public static final String TAG = "FullLogDialogFragment";
    private String mLog;

    public static FullLogDialogFragment newInstance(){
        return new FullLogDialogFragment();
    }

    public static FullLogDialogFragment newInstance(String log) {
        FullLogDialogFragment fullLogDialogFragment  = new FullLogDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString("log", log);
        fullLogDialogFragment.setArguments(bundle);
        return fullLogDialogFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_dialog_log, container, false);
    }

    @Override
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            mLog = args.getString("log");
        }
        else {
            try {
                mLog = IOUtils.toString(new FileInputStream(new File(PreferenceHelper.getInstance(requireContext()).getDecodingPath() + File.separator + "compile_log.txt")), StandardCharsets.UTF_8);
            }
            catch (IOException io){
                io.printStackTrace();
            }
        }
        Button button = view.findViewById(R.id.btn_copy_log);
        button.setOnClickListener(v -> {
            StringUtils.setClipboard(requireContext(), mLog, true);
        });
        AppCompatTextView log = view.findViewById(R.id.log);
        log.setText(mLog);
    }


    @Override
    public void onDetach() {
        super.onDetach();
    }

}

