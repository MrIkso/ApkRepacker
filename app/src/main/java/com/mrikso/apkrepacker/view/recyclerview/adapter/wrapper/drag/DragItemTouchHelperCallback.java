package com.mrikso.apkrepacker.view.recyclerview.adapter.wrapper.drag;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @author Created by cz
 * @date 2020-03-19 15:32
 * @email bingo110@126.com
 *
 * It's a default drag item touch helper callback. Only for drag not for swipe.
 *
 * @see DragWrapperAdapter
 * @see DragCallback
 */
public class DragItemTouchHelperCallback extends ItemTouchHelper.Callback {

    /**
     * The drag adapter.
     */
    private final DragWrapperAdapter adapter;

    public DragItemTouchHelperCallback(@NonNull DragWrapperAdapter adapter) {
        this.adapter=adapter;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        boolean longPressDragEnabled = super.isLongPressDragEnabled();
        if (null != adapter) {
            longPressDragEnabled=adapter.isLongPressDragEnabled();
        }
        return longPressDragEnabled;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        int adapterPosition = viewHolder.getAdapterPosition();
        int flag = ItemTouchHelper.Callback.makeFlag(ItemTouchHelper.ACTION_STATE_IDLE, ItemTouchHelper.DOWN);
        if (null != adapter&&adapter.isDragEnable(adapterPosition)) {
            flag = ItemTouchHelper.Callback.makeFlag(ItemTouchHelper.ACTION_STATE_DRAG,adapter.getDragFlag());
        }
        return flag;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull final RecyclerView.ViewHolder target) {
        final int targetPosition = target.getAdapterPosition();
        boolean itemEnable = false;
        if (null != adapter) {
            if (adapter.isDragEnable(targetPosition)) {
                itemEnable = true;
                int adapterPosition = viewHolder.getAdapterPosition();
                adapter.move(adapterPosition,targetPosition);
            }
        }
        return itemEnable;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
    }

}
