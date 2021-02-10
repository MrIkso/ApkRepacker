package com.mrikso.apkrepacker.view.recyclerview.adapter.wrapper.drag;

import androidx.recyclerview.widget.ItemTouchHelper;
/**
 * @author Created by cz
 * @date 2020-03-19 22:35
 * @email bingo110@126.com
 */
public interface DragCallback{

    /**
     * Determines whether the specified position can be dragged.
     * @param position
     * @return
     */
    boolean isDragEnable(int position);

    /**
     * If a long pressed event enables the drag.
     * @return
     */
    boolean isLongPressDragEnabled();

    /**
     * Return the drag flag
     * @see ItemTouchHelper#DOWN
     * @see ItemTouchHelper#UP
     * @see ItemTouchHelper#LEFT
     * @see ItemTouchHelper#RIGHT
     * @return
     */
    int getDragFlag();
}
