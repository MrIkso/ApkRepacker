/*
 * Copyright (C) 2016 Jecelyin Peng <jecelyin@gmail.com>
 *
 * This file is part of 920 Text Editor.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jecelyin.common.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.annotation.ArrayRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.appcompat.widget.PopupMenu;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.lang.reflect.Field;


/**
 * @author Jecelyin Peng <jecelyin@gmail.com>
 */
public class UIUtils {

    public static void alert(Context context, String message) {
        alert(context, null, message, null);
    }

    public static void alert(Context context, String title,String message) {
        alert(context, title, message, null);
    }

    public static void alert(Context context, String message, final OnClickCallback callback) {
        alert(context, null, message, callback);
    }

    public static void alert(Context context, String title, String message, final OnClickCallback callback) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(context)
                .content(message)
                .positiveText(android.R.string.ok)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if (callback == null)
                            return;
                        callback.onOkClick();
                    }
                });

        if (!TextUtils.isEmpty(title))
            builder.title(title);

        builder.show();
    }

    public static void toast(Context context, int messageResId) {
        toast(context, context.getString(messageResId));
    }

    public static void toast(Context context, int messageResId, Object... args) {
        toast(context, context.getString(messageResId, args));
    }

    public static void toast(Context context, String message) {
        Toast.makeText(context.getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    public static void toast(Context context, Throwable t) {
        DLog.e(t);
        Toast.makeText(context.getApplicationContext(), t.getMessage(), Toast.LENGTH_LONG).show();
    }

    /*
        /**
         * The content type of the text box, whose bits are defined by
         * {@link InputType}.
         *
         * @see InputType
         * @see InputType#TYPE_MASK_CLASS
         * @see InputType#TYPE_MASK_VARIATION
         * @see InputType#TYPE_MASK_FLAGS
         */
    public static void showInputDialog(Context context, @StringRes int titleRes, @StringRes int hintRes, CharSequence value, int inputType, OnShowInputCallback callback) {
        showInputDialog(context, titleRes != 0 ? context.getString(titleRes) : null, hintRes != 0 ? context.getString(hintRes) : null, value, inputType, callback);
    }

    @SuppressLint("RestrictedApi")
    public static void showIconInPopup(PopupMenu popupMenu) {
        try {
            Field mPoup = popupMenu.getClass().getDeclaredField("mPopup");
            mPoup.setAccessible(true);
            MenuPopupHelper menuPopupHelper = (MenuPopupHelper) mPoup.get(popupMenu);
            if (menuPopupHelper != null)
                menuPopupHelper.setForceShowIcon(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showInputDialog(Context context, CharSequence title, CharSequence hint, CharSequence value, int inputType, final OnShowInputCallback callback) {
        MaterialDialog.Builder dialog = new MaterialDialog.Builder(context)
                .title(title)
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .input(hint, value, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        if (callback != null) {
                            callback.onConfirm(input);
                        }
                    }
                })
                .inputType(inputType == 0 ? EditorInfo.TYPE_CLASS_TEXT : inputType);

        MaterialDialog dlg = dialog.show();
        dlg.setCanceledOnTouchOutside(true);
        dlg.setCancelable(true);
    }


    public static void showConfirmDialog(Context context, @StringRes int messageRes, final OnClickCallback callback) {
        showConfirmDialog(context, context.getString(messageRes), callback);
    }

    public static void showConfirmDialog(Context context, CharSequence message, final OnClickCallback callback) {
        showConfirmDialog(context, null, message, callback);
    }

    public static void showConfirmDialog(Context context, CharSequence title, CharSequence message, final OnClickCallback callback) {
        showConfirmDialog(context, title, message, callback, context.getString(android.R.string.ok), context.getString(android.R.string.cancel));
    }

    public static void showConfirmDialog(Context context, @StringRes int title, @StringRes int message, final OnClickCallback callback, @StringRes int postiveRes, @StringRes int negativeRes) {
        showConfirmDialog(context, title == 0 ? null : context.getString(title), context.getString(message), callback, context.getString(postiveRes), context.getString(negativeRes));
    }

    public static void showConfirmDialog(Context context, CharSequence title, CharSequence message, final OnClickCallback callback, String positiveStr, String negativeStr) {
        MaterialDialog.Builder dialog = new MaterialDialog.Builder(context)
                .title(title)
                .content(message)
                .positiveText(positiveStr)
                .negativeText(negativeStr)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if (callback == null)
                            return;
                        callback.onOkClick();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if (callback == null)
                            return;
                        callback.onCancelClick();
                    }
                });

        MaterialDialog dlg = dialog.show();
        dlg.setCanceledOnTouchOutside(true);
        dlg.setCancelable(true);
    }



    /* SingleChoice Dialog */

    public static void showListSingleChoiceDialog(Context context, @StringRes int messageRes, @ArrayRes int items, int selectedIndex, final OnSingleChoiceCallback singleChoiceCallback, final OnClickCallback callback) {
        showListSingleChoiceDialog(context, context.getString(messageRes), context.getResources().getTextArray(items), selectedIndex, singleChoiceCallback, callback);
    }

    public static void showListSingleChoiceDialog(Context context, CharSequence message, CharSequence[] items, int selectedIndex, final OnSingleChoiceCallback singleChoiceCallback, final OnClickCallback callback) {
        showListSingleChoiceDialog(context, null, message, items, selectedIndex, singleChoiceCallback, callback);
    }

    public static void showListSingleChoiceDialog(Context context, CharSequence title, CharSequence message, CharSequence[] items, int selectedIndex, final OnSingleChoiceCallback singleChoiceCallback, final OnClickCallback callback) {
        showListSingleChoiceDialog(context, title, message, items, selectedIndex, singleChoiceCallback, callback, context.getString(android.R.string.ok), context.getString(android.R.string.cancel));
    }

    public static void showListSingleChoiceDialog(Context context, @StringRes int title, @StringRes int message, CharSequence[] items, int selectedIndex, final OnSingleChoiceCallback singleChoiceCallback, final OnClickCallback callback) {
        showListSingleChoiceDialog(context, title == 0 ? null : context.getString(title), message == 0 ? null : context.getString(message), items, selectedIndex, singleChoiceCallback, callback, null, null);
    }

    public static void showListSingleChoiceDialog(Context context, @StringRes int title, @StringRes int message, @ArrayRes int items, int selectedIndex, final OnSingleChoiceCallback singleChoiceCallback, final OnClickCallback callback) {
        showListSingleChoiceDialog(context, title == 0 ? null : context.getString(title), message == 0 ? null : context.getString(message), context.getResources().getTextArray(items), selectedIndex, singleChoiceCallback, callback, null, null);
    }

    public static void showListSingleChoiceDialog(Context context, @StringRes int title, @StringRes int message, @ArrayRes int items, int selectedIndex, final OnSingleChoiceCallback singleChoiceCallback, final OnClickCallback callback, @StringRes int postiveRes, @StringRes int negativeRes) {
        showListSingleChoiceDialog(context, title == 0 ? null : context.getString(title), message == 0 ? null : context.getString(message), context.getResources().getTextArray(items), selectedIndex, singleChoiceCallback, callback, context.getString(postiveRes), context.getString(negativeRes));
    }

    public static void showListSingleChoiceDialog(Context context, CharSequence title, CharSequence message, CharSequence[] items, int selectedIndex, final OnSingleChoiceCallback singleChoiceCallback, final OnClickCallback callback, String positiveStr, String negativeStr) {
        new MaterialDialog.Builder(context)
                .title(title)
                .content(message)
                .items(items)
                .itemsCallbackSingleChoice(selectedIndex, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        if (singleChoiceCallback == null)
                            return false;
                        singleChoiceCallback.onSelect(dialog, which);
                        return true;
                    }
                })
                .positiveText(positiveStr)
                .negativeText(negativeStr)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if (callback == null)
                            return;
                        callback.onOkClick();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if (callback == null)
                            return;
                        callback.onCancelClick();
                    }
                }).show();
    }


    /* SingleChoice Dialog */

    public static void showListDialog(Context context, @StringRes int messageRes, @ArrayRes int items, int selectedIndex, final OnListCallback listCallback, final OnClickCallback callback) {
        showListDialog(context, context.getString(messageRes), context.getResources().getTextArray(items), selectedIndex, listCallback, callback);
    }

    public static void showListDialog(Context context, CharSequence message, CharSequence[] items, int selectedIndex, final OnListCallback listCallback, final OnClickCallback callback) {
        showListDialog(context, null, message, items, selectedIndex, listCallback, callback);
    }

    public static void showListDialog(Context context, CharSequence title, CharSequence message, CharSequence[] items, int selectedIndex, final OnListCallback listCallback, final OnClickCallback callback) {
        showListDialog(context, title, message, items, selectedIndex, listCallback, callback, context.getString(android.R.string.ok), context.getString(android.R.string.cancel));
    }

    public static void showListDialog(Context context, @StringRes int title, @StringRes int message, CharSequence[] items, int selectedIndex, final OnListCallback listCallback, final OnClickCallback callback) {
        showListDialog(context, title == 0 ? null : context.getString(title), message == 0 ? null : context.getString(message), items, selectedIndex, listCallback, callback, null, null);
    }

    public static void showListDialog(Context context, @StringRes int title, @StringRes int message, @ArrayRes int items, int selectedIndex, final OnListCallback listCallback, final OnClickCallback callback) {
        showListDialog(context, title == 0 ? null : context.getString(title), message == 0 ? null : context.getString(message), context.getResources().getTextArray(items), selectedIndex, listCallback, callback, null, null);
    }

    public static void showListDialog(Context context, @StringRes int title, @StringRes int message, @ArrayRes int items, int selectedIndex, final OnListCallback listCallback, final OnClickCallback callback, @StringRes int postiveRes, @StringRes int negativeRes) {
        showListDialog(context, title == 0 ? null : context.getString(title), message == 0 ? null : context.getString(message), context.getResources().getTextArray(items), selectedIndex, listCallback, callback, context.getString(postiveRes), context.getString(negativeRes));
    }

    public static void showListDialog(Context context, CharSequence title, CharSequence message, CharSequence[] items, int selectedIndex, final OnListCallback listCallback, final OnClickCallback callback, String positiveStr, String negativeStr) {
        new MaterialDialog.Builder(context)
                .title(title)
                .content(message)
                .items(items)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        if (listCallback == null)
                            return;
                        listCallback.onSelect(dialog, which);
                    }
                })
                .positiveText(positiveStr)
                .negativeText(negativeStr)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if (callback == null)
                            return;
                        callback.onOkClick();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if (callback == null)
                            return;
                        callback.onCancelClick();
                    }
                }).show();
    }


    public static abstract class OnClickCallback {
        public abstract void onOkClick();

        public void onCancelClick() {
        }
    }

    public static abstract class OnListCallback {
        public abstract void onSelect(MaterialDialog dialog, int which);
    }

    public static abstract class OnSingleChoiceCallback {
        public abstract void onSelect(MaterialDialog dialog, int which);
    }

    public static abstract class OnShowInputCallback {
        public abstract void onConfirm(CharSequence input);
    }
}