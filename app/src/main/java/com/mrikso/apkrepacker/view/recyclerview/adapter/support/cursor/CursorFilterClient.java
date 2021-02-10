package com.mrikso.apkrepacker.view.recyclerview.adapter.support.cursor;

import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * @author Created by cz
 * @date 2020-03-17 22:39
 * @email bingo110@126.com
 */
public interface CursorFilterClient {

    CharSequence convertToString(Cursor cursor);

    @Nullable
    Cursor runQueryOnBackgroundThread(CharSequence constraint);

    Cursor getCursor();

    void changeCursor(@NonNull Cursor cursor);
}
