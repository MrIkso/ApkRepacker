package com.mrikso.apkrepacker.view.recyclerview.adapter.wrapper.drag;

import androidx.recyclerview.widget.ItemTouchHelper;

/**
 * @author Created by cz
 * @date 2020-03-19 20:53
 * @email bingo110@126.com
 *
 * It's a default implementation for {@link DragCallback}
 */
public class DefaultDragCallback implements DragCallback {
    @Override
    public boolean isDragEnable(int position) {
        return true;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }

    @Override
    public int getDragFlag() {
        return ItemTouchHelper.DOWN|ItemTouchHelper.UP|ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT;
    }
}
