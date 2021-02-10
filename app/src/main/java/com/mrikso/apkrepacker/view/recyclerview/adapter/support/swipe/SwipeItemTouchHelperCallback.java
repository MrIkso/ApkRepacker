package com.mrikso.apkrepacker.view.recyclerview.adapter.support.swipe;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.LinkedList;

import static androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_IDLE;

/**
 * @author Created by cz
 * @date 2020-03-19 15:32
 * @email bingo110@126.com
 */
public class SwipeItemTouchHelperCallback extends ItemTouchHelper.Callback implements View.OnTouchListener {
    private static final String TAG="SwipeItemTouchHelperCallback";
    /**
     * The reference coordinates for the action start. For drag & drop, this is the time long
     * press is completed vs for swipe, this is the initial touch point.
     */
    private float initialTouchX;

    private float initialTouchY;
    private float lastTouchX;
    private float lastTouchY;
    private final RecyclerBin recyclerBin=new RecyclerBin();
    private SwipeOverlayViewGroup overlayViewGroup;
    private RecyclerView recyclerView;
    /**
     * Current selected position.
     */
    private int selectPosition=RecyclerView.NO_POSITION;
    private int orientation=RecyclerView.VERTICAL;
    /**
     * The drag adapter.
     */
    private final SwipeAdapter adapter;

    public SwipeItemTouchHelperCallback(@NonNull SwipeAdapter swipeCallback) {
        this.adapter = swipeCallback;
    }

    public void attachedToRecyclerView(@NonNull RecyclerView recyclerView){
        recyclerView.setOnTouchListener(this);
        this.recyclerView=recyclerView;
        this.overlayViewGroup=new SwipeOverlayViewGroup(recyclerView);
    }

    private int getOrientation(RecyclerView recyclerView){
        int orientation=RecyclerView.VERTICAL;
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if(layoutManager instanceof LinearLayoutManager) {
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
            orientation = linearLayoutManager.getOrientation();
        }
        return orientation;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        int adapterPosition = viewHolder.getAdapterPosition();
        int flag = ItemTouchHelper.Callback.makeFlag(ACTION_STATE_IDLE, ItemTouchHelper.DOWN);
        if (null != adapter && adapter.isSwipeEnable(adapterPosition)) {
            flag = ItemTouchHelper.Callback.makeFlag(ItemTouchHelper.ACTION_STATE_SWIPE, adapter.getMoveFlag());
        }
        return flag;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN) {
            initialTouchX = event.getX();
            initialTouchY = event.getY();
        } else if(action == MotionEvent.ACTION_MOVE){
            float x = event.getX();
            float y = event.getY();
            float diffX=x-lastTouchX;
            float diffY=y-lastTouchY;
            lastTouchX=x;
            lastTouchY=y;
//            Log.i(TAG,"diffX:"+diffX+" diffY:"+diffY);
        }
        return false;
    }


    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        int adapterPosition = viewHolder.getAdapterPosition();
        View itemView = viewHolder.itemView;
        float translationX = dX;
        View adapterView = overlayViewGroup.findAdapterView(adapterPosition);
        if(dX < 0 && actionState == ItemTouchHelper.ACTION_STATE_SWIPE){
            translationX = dX * adapterView.getWidth() / itemView.getWidth();
        }
        adapterView.setTranslationX(translationX);
        overlayViewGroup.getOverlayView().draw(c);
        super.onChildDraw(c, recyclerView, viewHolder, translationX, dY, actionState, isCurrentlyActive);
        Log.i(TAG,"onChildDraw:"+adapterPosition+" dx:"+dX+" dy:"+dY+" actionState:"+actionState+" isCurrentlyActive:"+isCurrentlyActive+" translationX:"+translationX);
    }


    @Override
    public void onChildDrawOver(@NonNull Canvas c, @NonNull RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDrawOver(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    @Override
    @SuppressLint("Range")
    public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
        super.onSelectedChanged(viewHolder, actionState);
        if(ItemTouchHelper.ACTION_STATE_SWIPE==actionState){
            final int adapterPosition = viewHolder.getAdapterPosition();
            if(selectPosition!=RecyclerView.NO_POSITION&&selectPosition!=adapterPosition){
                final RecyclerView.ViewHolder selectViewHolder = recyclerView.findViewHolderForAdapterPosition(selectPosition);
                if(null!=selectViewHolder){
                    selectViewHolder.itemView.animate().translationX(0).
                        setUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                int selectAdapterPosition = selectViewHolder.getAdapterPosition();
                                View adapterView = overlayViewGroup.findAdapterView(selectAdapterPosition);
                                if(null!=adapterView){
                                    adapterView.setTranslationX(selectViewHolder.itemView.getTranslationX());
                                }
                            }
                        }).setListener(new AnimatorListenerAdapter(){
                        @Override
                        public void onAnimationStart(Animator animation) {
                            super.onAnimationStart(animation);
                            selectViewHolder.setIsRecyclable(false);
                        }
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            selectViewHolder.setIsRecyclable(true);
                        }
                    });
                }
            }
//        Log.i(TAG,"onSwiped"+adapterPosition+" "+viewHolder.itemView.getLeft());
            if (null != adapter && adapter.isSwipeEnable(adapterPosition)) {
                selectPosition=adapterPosition;
                //Create new swipe menu layout if it doesn't exist.
                View adapterView = overlayViewGroup.findAdapterView(adapterPosition);
                if(null==adapterView){
                    int viewType = adapter.getSwipeMenuViewType(adapterPosition);
                    View scrapView = recyclerBin.getViewFromScrap(adapterPosition, viewType);
                    if(null!=scrapView){
                        //View from scrap list don't have to measure again.
                        overlayViewGroup.add(scrapView);
                        layoutSwipeView(viewHolder.itemView,scrapView,ItemTouchHelper.RIGHT);
                    } else {
                        View view = recyclerBin.getView(overlayViewGroup.getOverlayView(),adapterPosition, viewType);
                        int widthMeasureSpec=0;
                        int heightMeasureSpec =0;
                        switch (ItemTouchHelper.RIGHT) {
                            case ItemTouchHelper.LEFT:
                            case ItemTouchHelper.RIGHT:
                            case ItemTouchHelper.START:
                            case ItemTouchHelper.END:
                                //measure horizontally
                                widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(ViewGroup.LayoutParams.WRAP_CONTENT, View.MeasureSpec.AT_MOST);
                                heightMeasureSpec =View.MeasureSpec.makeMeasureSpec(viewHolder.itemView.getHeight(),View.MeasureSpec.EXACTLY);
                                break;
                            case ItemTouchHelper.UP:
                            case ItemTouchHelper.DOWN:
                                //measure vertically
                                widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(viewHolder.itemView.getWidth(), View.MeasureSpec.AT_MOST);
                                heightMeasureSpec =View.MeasureSpec.makeMeasureSpec(ViewGroup.LayoutParams.WRAP_CONTENT,View.MeasureSpec.EXACTLY);
                                break;
                            default:
                        }
                        overlayViewGroup.measureChild(view,widthMeasureSpec,heightMeasureSpec);
                        layoutSwipeView(viewHolder.itemView,view,ItemTouchHelper.RIGHT);
                        overlayViewGroup.add(view);
                    }
                }
            }
        }
        if(ACTION_STATE_IDLE==actionState){
        }
    }
    @Override
    @SuppressLint("Range")
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        int adapterPosition = viewHolder.getAdapterPosition();
//        adapter.notifyItemChanged(adapterPosition);
    }

    @Override
    public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
        int adapterPosition = viewHolder.getAdapterPosition();
        View adapterView = overlayViewGroup.findAdapterView(adapterPosition);
        if(null!=adapterView) {
            return 1f;
        }
        return super.getSwipeThreshold(viewHolder);
    }

    /**
     * Layout the swipe menu view by the list child view.
     * @param view
     * @param menuLayout
     */
    public void layoutSwipeView(View view,View menuLayout,int direction){
        int left = view.getLeft();
        int top = view.getTop();
        int right = view.getRight();
        int bottom = view.getBottom();
        switch (direction) {
            case ItemTouchHelper.LEFT:
            case ItemTouchHelper.START:
                //layout from left
                menuLayout.layout(left-menuLayout.getMeasuredWidth(),top,left,bottom);
                break;
            case ItemTouchHelper.RIGHT:
            case ItemTouchHelper.END:
                //layout from right
                menuLayout.layout(right,top,right+menuLayout.getMeasuredWidth(),bottom);
                break;
            case ItemTouchHelper.UP:
                //layout from top;
                menuLayout.layout(left,top-menuLayout.getMeasuredHeight(),right,top);
                break;
            case ItemTouchHelper.DOWN:
                //layout from bottom;
                menuLayout.layout(left,bottom,right,bottom+menuLayout.getMeasuredHeight());
                break;
            default:
        }
    }

    private class RecyclerBin {
        public SparseArray<LinkedList<View>> scrapArray = new SparseArray<>();

        public void clear(){
            scrapArray.clear();
        }

        public void addScrapView(int viewType,@NonNull View view){
            LinkedList<View> viewList = scrapArray.get(viewType);
            if(null==viewList){
                viewList=new LinkedList<>();
                scrapArray.put(viewType,viewList);
            }
            viewList.add(view);
        }

        public void recycler(int viewType,View view){
            addScrapView(viewType,view);
        }

        /**
         * Try to take a view from scrap list.
         * @param position
         * @return
         */
        @Nullable
        private View getViewFromScrap(int position,int viewType){
            View stickyView=null;
            LinkedList<View> viewList = scrapArray.get(viewType);
            if(null!=viewList&&!viewList.isEmpty()){
                stickyView=viewList.pollFirst();
            }
            if(null!=stickyView){
                SwipeOverlayViewGroup.LayoutParams layoutParams = (SwipeOverlayViewGroup.LayoutParams) stickyView.getLayoutParams();
                layoutParams.position=position;
            }
            return stickyView;
        }

        /**
         * Return a view. If the cache doesn't exist. We create a new one from the adapter.
         * @return
         */
        @NonNull
        public View getView(ViewGroup parent,int position, int stickyViewType){
            if(null== adapter){
                throw new NullPointerException("The adapter was null, Make sure you set up the adapter!");
            }
            View stickyView;
            LinkedList<View> viewList = scrapArray.get(stickyViewType);
            if(null==viewList||viewList.isEmpty()){
                stickyView = adapter.onCreateSwipeMenuView(parent.getContext(),parent, stickyViewType);
            } else {
                stickyView=viewList.pollFirst();
            }
            SwipeOverlayViewGroup.LayoutParams layoutParams = (SwipeOverlayViewGroup.LayoutParams) stickyView.getLayoutParams();
            layoutParams.position=position;
            return stickyView;
        }
    }
}
