package com.mrikso.apkrepacker.view.recyclerview.adapter.wrapper.sticky;

import android.view.View;

import androidx.annotation.NonNull;

/**
 * @author Created by cz
 * @date 2020-03-24 18:21
 * @email bingo110@126.com
 */
public interface StickyScrollable<V extends View> {
    void onScrolled(@NonNull V recyclerView, int dx, int dy);
}
