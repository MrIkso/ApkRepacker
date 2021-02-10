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

package com.jecelyin.editor.v2.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jecelyin.editor.v2.common.TabInfo;
import com.mrikso.apkrepacker.R;

/**
 * @author Jecelyin Peng <jecelyin@gmail.com>
 */
public class TabAdapter extends RecyclerView.Adapter<TabAdapter.ViewHolder> {
    private TabInfo[] list;
    private View.OnClickListener onClickListener;
    private int currentTab = 0;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.tab_item_default, parent, false));
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TabInfo tabInfo = getItem(position);

        if (tabInfo.getTitle().endsWith(".java")) {
            holder.mIcon.setImageDrawable(holder.mIcon.getContext().getDrawable(R.drawable.ic_java));
        } /*else if (tabInfo.getTitle().endsWith(".xml")) {
            viewHolder.mIcon.setImageDrawable(viewHolder.mIcon.getContext().getDrawable(R.drawable.ic_txt));
        }*/
        else{
            holder.mIcon.setImageDrawable(holder.mIcon.getContext().getDrawable(R.drawable.ic_rename));
        }

        holder.itemView.setSelected(position == currentTab);

        holder.mTitleTextView.setText((tabInfo.hasChanged() ? "* " : "") + tabInfo.getTitle());
        holder.mFileTextView.setText(tabInfo.getPath());

        if (onClickListener != null) {
            holder.mCloseImageView.setTag(position);
            holder.mCloseImageView.setOnClickListener(onClickListener);

            holder.itemView.setTag(position);
            holder.itemView.setOnClickListener(onClickListener);
        }
    }

    @Override
    public int getItemCount() {
        if (list == null)
            return 0;
        return list.length;
    }

    public TabInfo getItem(int position) {
        return list[position];
    }


    public void setTabInfoList(TabInfo[] tabInfoList) {
        this.list = tabInfoList;
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public void setCurrentTab(int index) {
        this.currentTab = index;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView mIcon;
        TextView mTitleTextView;
        TextView mFileTextView;
        ImageView mCloseImageView;

        ViewHolder(View itemView) {
            super(itemView);
            mIcon = itemView.findViewById(R.id.list_item_icon);
            mTitleTextView = itemView.findViewById(R.id.title_text_view);
            mFileTextView = itemView.findViewById(R.id.file_text_view);
            mCloseImageView = itemView.findViewById(R.id.btn_close);
        }
    }

}
