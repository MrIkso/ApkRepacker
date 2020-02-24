package com.mrikso.apkrepacker.fragment.dialogs;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

public class ProgressDialogFragment extends DialogFragment {

    public final static String TITLE = "title";
    public final static String MESSAGE = "message";
    public final static String MAX = "max";
    public final static String CANCELABLE = "cancelable";
    public static final String TAG = "ProgressDialogFragment";

    public static ProgressDialogFragment newInstance() {
        return new ProgressDialogFragment();
    }

    public interface ProgressDialogFragmentListener {
        void onProgressCancelled();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        boolean cancelable = getArguments().getBoolean(CANCELABLE, false);
        setCancelable(cancelable);

        // ProgressDialog
        String title = getArguments().getString(TITLE);
        String message = getArguments().getString(MESSAGE);
        ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setIndeterminate(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
       // dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setMax(getArguments().getInt(MAX));

        return dialog;
    }

    public void updateProgress(int value) {
        ProgressDialog dialog = (ProgressDialog) getDialog();
        if (dialog != null) {
            dialog.setProgress(value);
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        if (getProgressDialogFragmentListener() != null) {
            getProgressDialogFragmentListener().onProgressCancelled();
        }
    }

    public ProgressDialogFragmentListener getProgressDialogFragmentListener() {
        if (getActivity() == null) {
            return null;
        }

        if (getActivity() instanceof ProgressDialogFragmentListener) {
            return (ProgressDialogFragmentListener) getActivity();
        }
        return null;
    }
}
