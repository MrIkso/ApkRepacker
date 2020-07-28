package com.mrikso.apkrepacker.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;

import androidx.fragment.app.Fragment;

import com.jecelyin.editor.v2.utils.ExtGrep;

public class StringUtils {

    public static ExtGrep extGreps;
    public static Bundle bundle;

    public static void setClipboard(Context context, String text) {
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", text);
        assert clipboard != null;
        clipboard.setPrimaryClip(clip);
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
