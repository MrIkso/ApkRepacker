package com.mrikso.apkrepacker.view.recyclerview.adapter.wrapper.select;

import androidx.recyclerview.widget.RecyclerView;

/**
 * @author Created by cz
 * @date 2020-03-17 15:32
 * @email bingo110@126.com
 */
public interface Selectable<VH extends RecyclerView.ViewHolder>{
    void onSelectItem(VH holder, int position, boolean selected);
}
