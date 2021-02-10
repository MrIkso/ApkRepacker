package com.mrikso.apkrepacker.view.recyclerview.adapter.wrapper.sticky;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

/**
 * @author Created by cz
 * @date 2020-03-24 17:02
 * @email bingo110@126.com
 *
 * It's a sticky callback to initialize your sticky view.
 * @see StickyAdapter The implementation for this interface
 */
public interface StickyCallback<E> {
    /**
     * Return the object by position
     * @param position
     * @return
     */
    E getItem(int position);

    StickyGroupingStrategy getGroupingStrategy();
    /**
     * For a GridLayoutManager. You may want to have a fill sticky header.
     * The position is not the adapter position. It's a setCompareCondition position.
     * @param position
     * @return
     */
    boolean isFillStickyHeader(int position);
    /**
     * For different headers. We use view type to determine which one we should ask for.
     * @param position
     * @return
     */
    int getStickyViewType(int position);
    /**
     * Create the sticky header view.
     * @param view
     * @param viewType
     * @return
     */
    View onCreateStickyView(@NonNull ViewGroup view, int viewType);

    /**
     * Create the sticky header view.
     * @param view
     * @param viewType
     * @param position
     * @return
     */
    void onBindStickyView(@NonNull View view, int viewType, int position);
}
