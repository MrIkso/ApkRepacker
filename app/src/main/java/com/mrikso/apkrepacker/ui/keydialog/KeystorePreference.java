package com.mrikso.apkrepacker.ui.keydialog;

import android.content.Context;
import android.util.AttributeSet;

import androidx.preference.DialogPreference;

public class KeystorePreference extends DialogPreference {
    public KeystorePreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public KeystorePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public KeystorePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public KeystorePreference(Context context) {
        super(context);
    }
}
