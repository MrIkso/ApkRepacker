package com.mrikso.apkrepacker.fragment.dialogs.base;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.utils.ViewUtils;

public class BaseBottomSheetDialogFragment extends BottomSheetDialogFragment {

    private BottomSheetDialog mDialog;

    private Button mPositiveButton;
    private Button mNegativeButton;
    private TextView mTitle;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        mDialog = new BottomSheetDialog(requireContext());

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.bottom_sheet_dialog_base, null);
        mTitle = dialogView.findViewById(R.id.tv_bottom_sheet_dialog_base_title);
        mPositiveButton = dialogView.findViewById(R.id.button_bottom_sheet_dialog_base_ok);
        mNegativeButton = dialogView.findViewById(R.id.button_bottom_sheet_dialog_base_cancel);
        mDialog.setContentView(dialogView);

        FrameLayout container = dialogView.findViewById(R.id.container_bottom_sheet_dialog_base_content);
        View contentView = onCreateContentView(LayoutInflater.from(requireContext()), container, savedInstanceState);
        if (contentView != null) {
            onContentViewCreated(contentView, savedInstanceState);
            container.addView(contentView);
        }

        return mDialog;
    }

    @Nullable
    @Override
    public final View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Nullable
    protected View onCreateContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return null;
    }

    protected void onContentViewCreated(View view, @Nullable Bundle savedInstanceState) {

    }

    @NonNull
    protected Button getPositiveButton() {
        return mPositiveButton;
    }

    @NonNull
    protected Button getNegativeButton() {
        return mNegativeButton;
    }
    @NonNull

    public void setTitle(@StringRes int title) {
        setTitle(getString(title));
    }
    @NonNull

    public void setTitle(CharSequence title) {
        mTitle.setText(title);
    }

    @NonNull
    protected TextView getTitle(){
        return mTitle;
    }

    public void hideTitle(boolean hide){
        ViewUtils.setVisibleOrGone(mTitle, hide);
    }

    protected void revealBottomSheet() {
        FrameLayout bottomSheet = mDialog.findViewById(R.id.design_bottom_sheet);
        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);

        Object parent = getParentFragment();
        if (parent == null)
            parent = requireActivity();

        if (parent instanceof OnDismissListener && getTag() != null)
            ((OnDismissListener) parent).onDialogDismissed(getTag());

    }

    public interface OnDismissListener {

        void onDialogDismissed(@NonNull String dialogTag);

    }
}
