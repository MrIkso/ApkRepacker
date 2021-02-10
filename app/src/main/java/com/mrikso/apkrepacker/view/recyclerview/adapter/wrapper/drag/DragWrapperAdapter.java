package com.mrikso.apkrepacker.view.recyclerview.adapter.wrapper.drag;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.mrikso.apkrepacker.view.recyclerview.adapter.wrapper.dynamic.DynamicWrapperAdapter;

/**
 * @author Created by cz
 * @date 2020-03-17 19:31
 * @email bingo110@126.com
 *
 * @see DragItemTouchHelperCallback We controll all the drag event in this class.
 * @see DragCallback The drag callback
 *
 */
public class DragWrapperAdapter extends DynamicWrapperAdapter implements DragCallback{
    private final ItemTouchHelper itemTouchHelper=new ItemTouchHelper(new DragItemTouchHelperCallback(this));
    /**
     * If you want to control the drag callback.
     * Such as allowing a few positions that in a specific range to drag.
     */
    private DragCallback delegateCallback;
    /**
     * The drag flag
     * @see ItemTouchHelper#UP
     * @see ItemTouchHelper#DOWN
     * ...
     */
    private int dragFlag =ItemTouchHelper.ACTION_STATE_IDLE;
    /**
     * Allow the fixed view drag.
     */
    private boolean fixedViewDragEnabled=true;

    public DragWrapperAdapter(@NonNull RecyclerView.Adapter adapter) {
        super(adapter);
    }

    @Override
    public boolean isDragEnable(int position) {
        int findPosition = findPosition(position);
        boolean isFixedView=RecyclerView.NO_POSITION!=findPosition;
        if(isFixedView){
            return fixedViewDragEnabled;
        } else if(null!=delegateCallback){
            int extraViewCount = getExtraViewCount(position);
            return delegateCallback.isDragEnable(position-extraViewCount);
        } else {
            return true;
        }
    }

    @Override
    public boolean isLongPressDragEnabled() {
        if(null!=delegateCallback){
            return delegateCallback.isLongPressDragEnabled();
        } else {
            return true;
        }
    }

    public boolean isSwipeDirectionEnabled() {
        if(null!=delegateCallback){
            return delegateCallback.isLongPressDragEnabled();
        } else {
            return true;
        }
    }

    @Override
    public int getDragFlag() {
        if(null!=delegateCallback){
            return delegateCallback.getDragFlag();
        } else {
            return dragFlag;
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        //Attach to the recycler view.
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if(layoutManager instanceof GridLayoutManager||layoutManager instanceof StaggeredGridLayoutManager){
            dragFlag=ItemTouchHelper.UP|ItemTouchHelper.DOWN|ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT;
        } else {
            dragFlag=ItemTouchHelper.UP|ItemTouchHelper.DOWN;
        }
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    /**
     * Move the position from old to a new position
     * @param oldPosition
     * @param newPosition
     */
    public void move(int oldPosition,int newPosition){
        movePosition(oldPosition,newPosition);
        notifyItemMoved(oldPosition, newPosition);
    }

    private void movePosition(int oldPosition, int newPosition){
        if(!isHeaderPosition(oldPosition)&&!isFooterPosition(oldPosition)&&
            !isHeaderPosition(newPosition)&&!isFooterPosition(newPosition)){
            if (oldPosition < newPosition) {
                int i = oldPosition;
                while (i < newPosition) {
                    moveInternal(i, i + 1);
                    i++;
                }
            } else {
                int i = oldPosition;
                while (i > newPosition) {
                    moveInternal(i, i - 1);
                    i--;
                }
            }
        }
    }

    /**
     * Change the position internal.
     * @param oldPosition
     * @param newPosition
     */
    private void moveInternal(int oldPosition, int newPosition) {
        int oldIndex = findPosition(oldPosition);
        int newIndex = findPosition(newPosition);
        boolean startFillColumn = RecyclerView.NO_POSITION != oldIndex;
        boolean endFillColumn =  RecyclerView.NO_POSITION != newIndex;
        if (startFillColumn && endFillColumn) {
            setFixedViewPosition(oldIndex,newPosition);
            setFixedViewPosition(newIndex,oldPosition);
        } else if (startFillColumn) {
            setFixedViewPosition(oldIndex,newPosition);
        } else if (endFillColumn) {
            setFixedViewPosition(newIndex,oldPosition);
        } else {
            RecyclerView.Adapter adapter = getAdapter();
            if(adapter instanceof Moveable){
                Moveable moveable = (Moveable) adapter;
                int oldExtraViewCount = getExtraViewCount(oldPosition);
                int startExtraViewCount = getExtraViewCount(oldPosition);
                moveable.move(oldPosition-oldExtraViewCount,newPosition-startExtraViewCount);
            }
        }
    }

    public void setFixedViewDragEnabled(boolean fixedViewDragEnabled){
        this.fixedViewDragEnabled=fixedViewDragEnabled;
    }

    /**
     * Change the callback. When you want to change how the drag works.
     * @see DragCallback#getDragFlag() Change the drag flag
     * @see DragCallback#isLongPressDragEnabled()
     * @see DragCallback#isDragEnable(int) Allow the position to drag.
     * @param callback
     */
    public void setDragDelegate(DragCallback callback){
        this.delegateCallback=callback;
    }
}
