package com.mrikso.apkrepacker.view.recyclerview.adapter.listener;

import android.view.View;

import androidx.annotation.NonNull;

import com.mrikso.apkrepacker.view.recyclerview.adapter.wrapper.header.HeaderWrapperAdapter;

/**
 * @author Created by cz
 * @date 2020-03-17 20:20
 * @email bingo110@126.com
 *
 * The adapter item click listener.
 * @see HeaderWrapperAdapter#setOnItemClickListener(OnItemClickListener)
 */
public interface OnItemClickListener {
    void onItemClick(@NonNull View v,@NonNull int position, int adapterPosition);
}

