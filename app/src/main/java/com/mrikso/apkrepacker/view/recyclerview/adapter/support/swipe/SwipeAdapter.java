package com.mrikso.apkrepacker.view.recyclerview.adapter.support.swipe;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.mrikso.apkrepacker.view.recyclerview.adapter.BaseAdapter;

import java.util.List;

/**
 * @author Created by cz
 * @date 2020-03-25 18:27
 * @email bingo110@126.com
 *
 * Something I didn't expect. So I was stop to finished this adapter.
 * No worry. It's not very important.
 */
public abstract class SwipeAdapter<VH extends RecyclerView.ViewHolder,E> extends BaseAdapter<VH,E> implements SwipeCallback {
    /**
     * If you want to control the swipe swipeItemTouchHelperCallback.
     * Such as allowing a few positions that in a specific range to drag.
     */
    private SwipeCallback delegateCallback;
    /**
     * The drag flag
     * @see ItemTouchHelper#UP
     * @see ItemTouchHelper#DOWN
     * ...
     */
    private int dragFlag =ItemTouchHelper.ACTION_STATE_IDLE;

    public SwipeAdapter(@NonNull List<E> itemList) {
        super(itemList);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        //Attach to the recycler view.
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if(layoutManager instanceof GridLayoutManager ||layoutManager instanceof StaggeredGridLayoutManager){
            dragFlag=ItemTouchHelper.UP|ItemTouchHelper.DOWN|ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT;
        } else if(layoutManager instanceof LinearLayoutManager) {
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
            int orientation = linearLayoutManager.getOrientation();
            if(RecyclerView.VERTICAL==orientation){
                dragFlag = ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT;
            } else {
                dragFlag = ItemTouchHelper.UP|ItemTouchHelper.DOWN;
            }
        }
        SwipeItemTouchHelperCallback swipeItemTouchHelperCallback =new SwipeItemTouchHelperCallback(this);
        ItemTouchHelper itemTouchHelper=new ItemTouchHelper(swipeItemTouchHelperCallback);
        swipeItemTouchHelperCallback.attachedToRecyclerView(recyclerView);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    /**
     * Return the swipe menu view type.
     * The default was zero. If there are more then one menu layout.
     * We could override this function and determine what kind of swipe menu you want.
     * @param position
     * @return
     */
    public int getSwipeMenuViewType(int position){
        return 0;
    }

    /**
     * Create a swipe menu view by the given view type.
     * @see #getSwipeMenuViewType(int)
     * @param context
     * @param parent
     * @param viewType
     * @return
     */
    public abstract View onCreateSwipeMenuView(@NonNull Context context,@NonNull ViewGroup parent,int viewType);

    /**
     * Bind data with the swipe menu view.
     * @param context
     * @param view
     * @param viewType
     * @param position
     */
    public abstract void onBindSwipeMenuView(@NonNull Context context,@NonNull View view,int viewType,int position);

    @Override
    public int getMoveFlag() {
        if(null!=delegateCallback){
            return delegateCallback.getMoveFlag();
        } else {
            return dragFlag;
        }
    }

    @Override
    public boolean isSwipeEnable(int position) {
        if(null!=delegateCallback){
            return delegateCallback.isSwipeEnable(position);
        } else {
            return true;
        }
    }

    @Override
    public boolean isSwipeDirectionEnabled(int direction) {
        if(null!=delegateCallback){
            return delegateCallback.isSwipeDirectionEnabled(direction);
        } else {
            return true;
        }
    }

    public void setDelegateCallback(SwipeCallback delegateCallback) {
        this.delegateCallback = delegateCallback;
    }
}
