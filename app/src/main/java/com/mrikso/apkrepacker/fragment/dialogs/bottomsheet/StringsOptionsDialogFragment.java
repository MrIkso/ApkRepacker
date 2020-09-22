package com.mrikso.apkrepacker.fragment.dialogs.bottomsheet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.IntegerRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.mrikso.apkrepacker.R;

import org.jetbrains.annotations.NotNull;

public class StringsOptionsDialogFragment extends BottomSheetDialogFragment implements View.OnClickListener {

    public static final String TAG = "StringsOptionsDialogFragment";

    private ItemClickListener mListener;

    public static StringsOptionsDialogFragment newInstance() {
        return new StringsOptionsDialogFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_strings_options, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.open_with).setOnClickListener(this);
        view.findViewById(R.id.add_new_string).setOnClickListener(this);
        view.findViewById(R.id.add_new_language).setOnClickListener(this);
        view.findViewById(R.id.auto_translate_language).setOnClickListener(this);
        view.findViewById(R.id.auto_translate_language_with).setOnClickListener(this);
        view.findViewById(R.id.save_as_dictionary).setOnClickListener(this);

    }

    @Override
    public void onAttach(@NotNull Context context) {
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

    @SuppressLint("ResourceType")
    @Override
    public void onClick(View view) {
        mListener.onItemClick(view.getId());
        dismiss();
    }

    public interface ItemClickListener {
        void onItemClick(@IntegerRes int item);
    }
}

