package com.mrikso.apkrepacker.view.recyclerview.adapter.listener;

import android.view.View;

import androidx.annotation.Nullable;

import com.mrikso.apkrepacker.view.recyclerview.adapter.support.expand.ExpandAdapter;


/**
 * Created by cz
 * @date 2020-03-28 11:21
 * @email bingo110@126.com
 * The expand item click listener.
 * It's only work for {@link ExpandAdapter}
 */
public interface OnExpandItemClickListener {
    void onItemClick(@Nullable View v, int groupPosition, int childPosition);
}