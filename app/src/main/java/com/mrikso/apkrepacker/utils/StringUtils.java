package com.mrikso.apkrepacker.utils;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.jecelyin.editor.v2.utils.ExtGrep;

public class StringUtils {

    public static ExtGrep extGreps;
    public static Bundle bundle;

    public static void setClipboard(Context context, String text) {
        ClipboardManager clipboard =(ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("", text);
        clipboard.setPrimaryClip(clip);
    }

    @Nullable
    public static CharSequence getClipboard(Context context) {
        ClipboardManager clipboardManager =(ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboardManager == null) {
            return null;
        }
        // Examines the item on the clipboard. If getText() does not return null,
        // the clip item contains the
        // text. Assumes that this application can only handle one item at a time.
        ClipData.Item item = clipboardManager.getPrimaryClip().getItemAt(0);
        // Gets the clipboard as text.
        return item.getText();
    }

    public static void setGreap(ExtGrep extGrep){
        extGreps = extGrep;
    }

    public static void setBundle(Bundle value){
        bundle = value;
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getWindow().getDecorView().getWindowToken(), 0);
    }

    public static void hideKeyboard(Fragment fragment) {
        Activity activity = fragment.getActivity();
        if (activity != null) {
            hideKeyboard(activity);
            return;
        }

        InputMethodManager inputMethodManager = (InputMethodManager) fragment.requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(fragment.requireView().getWindowToken(), 0);
    }
}
