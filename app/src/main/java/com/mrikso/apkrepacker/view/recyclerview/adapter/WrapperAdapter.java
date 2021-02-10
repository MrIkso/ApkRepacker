package com.mrikso.apkrepacker.view.recyclerview.adapter;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;


/**
 * @author Created by cz
 * @date 2020-03-17 20:39
 * @email bingo110@126.com
 *
 * This interface marks the object as a wrapper adapter.
 * So you are able to return your original adapter.
 */
public interface WrapperAdapter {
    /**
     * Return the original adapter.
     * @return
     */
    RecyclerView.Adapter getAdapter();

    /**
     * This function return a position that plus the extra view count.
     * If your position was:1. When you use {@link HeaderWrapperAdapter} we have 2 header views. We will return position:3.
     * @see RecyclerView.AdapterDataObserver
     * @param position
     * @return
     */
    int getOffsetPosition(int position);

    /**
     * Return the extra view count that behind this position
     * For example when you are use the {@link HeaderWrapperAdapter} when your position is:1
     * But we have two extra layout as the header layouts.
     * So the parameter was:1 we will return 2 cause they are two extra header layouts.
     *
     * It's so important for a wrapper adapter. We also use this function for {@link DynamicWrapperAdapter}
     * This function let the original adapter knows how much extra view that behind this position.
     * @param position
     * @return
     */
    int getExtraViewCount(int position);

    void onChanged();

    void itemRangeInsert(int positionStart,int itemCount);

    void itemRangeChanged(int positionStart, int itemCount);

    void itemRangeChanged(int positionStart, int itemCount, @Nullable Object payload);

    void itemRangeRemoved(int positionStart,int itemCount);

    void itemRangeMoved(int fromPosition,int toPosition,int itemCount);
}
