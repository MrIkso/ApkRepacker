package com.mrikso.apkrepacker.view.recyclerview.adapter.support.cursor;

import android.database.Cursor;
import android.widget.Filter;

/**
 * @author Created by cz
 * @date 2020-03-17 22:37
 * @email bingo110@126.com
 */
public class CursorFilter extends Filter {
    private final CursorFilterClient filterClient;

    public CursorFilter(CursorFilterClient cursorFilterClient) {
        this.filterClient = cursorFilterClient;
    }

    @Override
    public CharSequence convertResultToString(Object resultValue) {
        return filterClient.convertToString((Cursor) resultValue);
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        Cursor cursor = filterClient.runQueryOnBackgroundThread(constraint);

        FilterResults results = new FilterResults();
        if (cursor != null) {
            results.count = cursor.getCount();
            results.values = cursor;
        } else {
            results.count = 0;
            results.values = null;
        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        Cursor oldCursor = filterClient.getCursor();
        if (results.values != null && results.values != oldCursor) {
            filterClient.changeCursor((Cursor) results.values);
        }
    }
}
