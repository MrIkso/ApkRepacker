package com.mrikso.apkrepacker.view.recyclerview.adapter.support.swipe;

import androidx.recyclerview.widget.ItemTouchHelper;

/**
 * @author Created by cz
 * @date 2020-03-19 22:35
 * @email bingo110@126.com
 */
public interface SwipeCallback {

    boolean isSwipeEnable(int position);

    boolean isSwipeDirectionEnabled(int direction);
    /**
     * Return the drag flag
     * @see ItemTouchHelper#DOWN
     * @see ItemTouchHelper#UP
     * @see ItemTouchHelper#LEFT
     * @see ItemTouchHelper#RIGHT
     * @return
     */
    int getMoveFlag();

}
