package com.mrikso.apkrepacker.view.recyclerview.adapter.listener;

import android.view.View;

import androidx.annotation.Nullable;

import com.mrikso.apkrepacker.view.recyclerview.adapter.wrapper.header.HeaderWrapperAdapter;


/**
 * @author Created by cz
 * @date 2020-03-17 20:21
 * @email bingo110@126.com
 * The adapter item long click listener.
 *
 * @see HeaderWrapperAdapter#setOnItemLongClickListener(OnItemLongClickListener)
 */
public interface OnItemLongClickListener {
    boolean onLongItemClick(@Nullable View v, int position, int adapterPosition);
}
