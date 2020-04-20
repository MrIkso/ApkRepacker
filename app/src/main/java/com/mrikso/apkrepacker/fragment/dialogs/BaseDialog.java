package com.mrikso.apkrepacker.fragment.dialogs;

import android.content.Context;
import android.view.LayoutInflater;

import android.app.AlertDialog;

public abstract class BaseDialog {
    private Context context;

    public BaseDialog(Context context) {
        this.context = context;
    }

    public abstract AlertDialog.Builder show();

    protected AlertDialog.Builder getBuilder() {
        return new AlertDialog.Builder(context);
    }

    protected LayoutInflater getLayoutInflater() {
        return LayoutInflater.from(context);
    }

    protected Context getContext() {
        return context;
    }

    protected CharSequence getString(int id) {
        return getContext().getString(id);
    }

}
