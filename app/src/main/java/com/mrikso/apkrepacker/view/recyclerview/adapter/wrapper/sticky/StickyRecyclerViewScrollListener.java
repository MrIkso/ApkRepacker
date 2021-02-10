package com.mrikso.apkrepacker.view.recyclerview.adapter.wrapper.sticky;

import android.annotation.SuppressLint;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import java.util.LinkedList;

/**
 * @author Created by cz
 * @date 2020-03-24 18:24
 * @email bingo110@126.com
 */
public class StickyRecyclerViewScrollListener<A extends RecyclerView.Adapter& StickyCallback> implements StickyScrollable<RecyclerView> {
    private static final String TAG="StickyRecyclerViewScrollListener";
    private StickyRecyclerBin recyclerBin=new StickyRecyclerBin();
    private StickyOverlayViewGroup stickyOverlay;
    private OrientationHelper orientationHelper;
    private int lastStickyItemPosition = RecyclerView.NO_POSITION;
    private int orientation;
    private A adapter;

    public StickyRecyclerViewScrollListener(RecyclerView recyclerView,@NonNull A adapter,@NonNull StickyOverlayViewGroup stickyOverlay) {
        this.adapter=adapter;
        this.stickyOverlay = stickyOverlay;
        this.orientation=getOrientation(recyclerView);
        this.orientationHelper=OrientationHelper.createOrientationHelper(recyclerView.getLayoutManager(), orientation);
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

    /**
     * Find the first visible position.
     * We support three different layout manager
     * @see LinearLayoutManager#findFirstVisibleItemPosition()
     * @see GridLayoutManager#findFirstVisibleItemPosition()
     * @see StaggeredGridLayoutManager#findFirstVisibleItemPositions(int[])
     *
     */
    private int findFirstVisibleItemPosition(@NonNull RecyclerView recyclerView){
        int firstVisibleItemPosition=RecyclerView.NO_POSITION;
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
            firstVisibleItemPosition = gridLayoutManager.findFirstVisibleItemPosition();
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            StaggeredGridLayoutManager staggeredGridLayoutManager = (StaggeredGridLayoutManager) layoutManager;
            int[] spanArray = new int[]{staggeredGridLayoutManager.getSpanCount()};
            staggeredGridLayoutManager.findFirstVisibleItemPositions(spanArray);
            for(int i=0;i<spanArray.length;i++){
                if(firstVisibleItemPosition<spanArray[i]){
                    firstVisibleItemPosition=spanArray[i];
                }
            }
        } else if(layoutManager instanceof LinearLayoutManager){
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
            firstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition();
        }
        return firstVisibleItemPosition;
    }

    /**
     * Find the last visible position
     * We support three different layout manager
     * @see LinearLayoutManager#findLastVisibleItemPosition()
     * @see GridLayoutManager#findLastVisibleItemPosition()
     * @see StaggeredGridLayoutManager#findLastVisibleItemPositions(int[])
     */
    private int findLastVisibleItemPosition(@NonNull RecyclerView recyclerView){
        int lastVisibleItemPosition=RecyclerView.NO_POSITION;
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
            lastVisibleItemPosition = gridLayoutManager.findLastVisibleItemPosition();
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            StaggeredGridLayoutManager staggeredGridLayoutManager = (StaggeredGridLayoutManager) layoutManager;
            int[] spanArray = new int[]{staggeredGridLayoutManager.getSpanCount()};
            staggeredGridLayoutManager.findFirstVisibleItemPositions(spanArray);
            for(int i=0;i<spanArray.length;i++){
                if(lastVisibleItemPosition<spanArray[i]){
                    lastVisibleItemPosition=spanArray[i];
                }
            }
        } else if(layoutManager instanceof LinearLayoutManager){
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
            lastVisibleItemPosition = linearLayoutManager.findLastVisibleItemPosition();
        }
        return lastVisibleItemPosition;
    }

    private int getLayoutSpanCount(RecyclerView recyclerView){
        int spanCount=1;
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if(layoutManager instanceof GridLayoutManager){
            GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
            spanCount=gridLayoutManager.getSpanCount();
        } else if(layoutManager instanceof StaggeredGridLayoutManager){
            StaggeredGridLayoutManager staggeredGridLayoutManager = (StaggeredGridLayoutManager) layoutManager;
            spanCount=staggeredGridLayoutManager.getSpanCount();
        }
        return spanCount;
    }

    @Override
    @SuppressLint("Range")
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        int layoutSpanCount = getLayoutSpanCount(recyclerView);
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        StickyGroupingStrategy groupingStrategy = adapter.getGroupingStrategy();
        int firstVisibleItemPosition = findFirstVisibleItemPosition(recyclerView);
        View stickyHeaderView = stickyOverlay.findStickyView(lastStickyItemPosition);
        int fromPosition,toPosition;
        if(lastStickyItemPosition<firstVisibleItemPosition+1){
            //Move forward
            fromPosition=lastStickyItemPosition;
            toPosition=firstVisibleItemPosition+layoutSpanCount;
        } else {
            //Move backward
            fromPosition=groupingStrategy.getGroupStartPosition(lastStickyItemPosition-1);
            toPosition=lastStickyItemPosition;
        }
        //When we scroll back but there is nothing exist or the sticky group information has changed.
        // gvc  we check if we need to remove the sticky header view.
        if(fromPosition==toPosition||!groupingStrategy.isGroupPosition(lastStickyItemPosition)){
            //There is nothing in the group.
            View lastStickyView = stickyOverlay.findStickyView(lastStickyItemPosition);
            if (null != lastStickyView) {
                int stickyViewType = adapter.getStickyViewType(lastStickyItemPosition);
                recyclerBin.recycler(stickyViewType,lastStickyView);
            }
            lastStickyItemPosition=RecyclerView.NO_POSITION;
        }
        for(int position=fromPosition;position<=toPosition;position++){
            if (lastStickyItemPosition != position&&groupingStrategy.isGroupPosition(position)) {
                int startPosition = groupingStrategy.getGroupStartPosition(position);
                View stickAdapterView = layoutManager.findViewByPosition(startPosition);
                if(null!=stickAdapterView){
                    int decoratedStart = orientationHelper.getDecoratedStart(stickAdapterView);
                    if (0 >= decoratedStart) {
                        View lastStickyView = stickyOverlay.findStickyView(lastStickyItemPosition);
                        if (null != lastStickyView) {
                            int stickyViewType = adapter.getStickyViewType(lastStickyItemPosition);
                            recyclerBin.recycler(stickyViewType,lastStickyView);
                        }
                        Log.i(TAG,"move forward firstVisibleItemPosition:"+firstVisibleItemPosition+" lastStickyItemPosition:"+lastStickyItemPosition+" newPosition:"+position);
                        stickyHeaderView = createStickyView(layoutManager, startPosition);
                        lastStickyItemPosition = startPosition;
                    }
                } else {
                    //The last one. The one when you move backward.
                    View firstVisibleAdapterView = layoutManager.findViewByPosition(firstVisibleItemPosition);
                    if(null!=firstVisibleAdapterView){
                        int decoratedEnd = orientationHelper.getDecoratedEnd(firstVisibleAdapterView);
                        if (0 <= decoratedEnd) {
                            View lastStickyView = stickyOverlay.findStickyView(lastStickyItemPosition);
                            if (null != lastStickyView) {
                                int stickyViewType = adapter.getStickyViewType(lastStickyItemPosition);
                                recyclerBin.recycler(stickyViewType,lastStickyView);
                            }
                            Log.i(TAG,"move backward firstVisibleItemPosition:"+firstVisibleItemPosition+" lastStickyItemPosition:"+lastStickyItemPosition+" newPosition:"+position);
                            stickyHeaderView=createStickyView(layoutManager, startPosition);
                            lastStickyItemPosition = startPosition;
                        }
                    }
                }
                break;
            }
        }
        if(null!=stickyHeaderView){
            stickyHeaderView.setTranslationY(0);
            //Here I don't know the reason. Sometimes the top wasn't zero. So we double-check this. make sure it's on the top of the view.
            if(0!=stickyHeaderView.getTop()){
                stickyHeaderView.offsetTopAndBottom(-stickyHeaderView.getTop());
            }
            int lastVisibleItemPosition = findLastVisibleItemPosition(recyclerView);
            int nextStickyPosition = findGroupPosition(groupingStrategy,firstVisibleItemPosition+1, lastVisibleItemPosition);
            if (RecyclerView.NO_POSITION != nextStickyPosition) {
                View nextAdapterView = layoutManager.findViewByPosition(nextStickyPosition);
                if (null != nextAdapterView && nextAdapterView.getTop() < stickyHeaderView.getHeight()) {
                    stickyHeaderView.setTranslationY((nextAdapterView.getTop() - stickyHeaderView.getHeight()));
                }
            }
        }

    }

    @SuppressLint("Range")
    private View createStickyView(RecyclerView.LayoutManager layoutManager, int position) {
        //Try to take the sticky header view from view group.
        View stickyView = stickyOverlay.findStickyView(position);
        int stickyViewType = adapter.getStickyViewType(position);
        if(null!=stickyView){
            adapter.onBindStickyView(stickyView,stickyViewType,position);
        } else {
            //Create a new sticky header view from adapter.
            stickyView = recyclerBin.getViewFromScrap(position,stickyViewType);
            if(null!=stickyView){
                //Here we don't have to measure and layout again.
                adapter.onBindStickyView(stickyView,stickyViewType,position);
                stickyOverlay.addView(stickyView);
            } else {
                //We created a new sticky view from adapter.
                View stickAdapterView = layoutManager.findViewByPosition(position);
                if(null!=stickAdapterView){
                    ViewGroup overlayView = stickyOverlay.getOverlayView();
                    int viewType = adapter.getStickyViewType(position);
                    stickyView = recyclerBin.getView(overlayView,position, viewType);
                    int decoratedMeasurementInOther = orientationHelper.getDecoratedMeasurementInOther(stickAdapterView);
                    if(RecyclerView.HORIZONTAL==orientation){
                        stickyOverlay.measureChild(stickyView,
                                View.MeasureSpec.makeMeasureSpec(ViewGroup.LayoutParams.WRAP_CONTENT, View.MeasureSpec.AT_MOST),
                                View.MeasureSpec.makeMeasureSpec(decoratedMeasurementInOther, View.MeasureSpec.EXACTLY));
                        stickyView.layout(stickAdapterView.getLeft(), stickAdapterView.getTop(),
                                stickAdapterView.getLeft()+stickyView.getMeasuredWidth(),stickAdapterView.getBottom());
                    } else {
                        stickyOverlay.measureChild(stickyView,
                                View.MeasureSpec.makeMeasureSpec(decoratedMeasurementInOther, View.MeasureSpec.EXACTLY),
                                View.MeasureSpec.makeMeasureSpec(ViewGroup.LayoutParams.WRAP_CONTENT, View.MeasureSpec.AT_MOST));
                        stickyView.layout(stickAdapterView.getLeft(), stickAdapterView.getTop(),
                                stickAdapterView.getRight(),stickAdapterView.getTop()+stickyView.getMeasuredHeight());
                    }
                    adapter.onBindStickyView(stickyView,stickyViewType,position);
                    stickyOverlay.addView(stickyView);
                }
            }
        }
        return stickyView;
    }

    private int findGroupPosition(StickyGroupingStrategy groupingStrategy, int position, int lastVisibleItemPosition){
        int findPosition=RecyclerView.NO_POSITION;
        for(int i=position;i <= lastVisibleItemPosition;i++){
            if(groupingStrategy.isGroupPosition(i)){
                findPosition=i;
                break;
            }
        }
        return findPosition;
    }

    public class StickyRecyclerBin {

        public SparseArray<LinkedList<View>> scrapArray = new SparseArray<>();

        public void clear(){
            scrapArray.clear();
        }

        public void addScrapView(int stickyViewType,@NonNull View view){
            LinkedList<View> viewList = scrapArray.get(stickyViewType);
            if(null==viewList){
                viewList=new LinkedList<>();
                scrapArray.put(stickyViewType,viewList);
            }
            viewList.add(view);
        }

        public void recycler(int viewType,View view){
            stickyOverlay.remove(view);
            recyclerBin.addScrapView(viewType,view);
        }

        /**
         * Try to take a view from scrap list.
         * @param position
         * @return
         */
        @Nullable
        private View getViewFromScrap(int position,int stickyViewType){
            View stickyView=null;
            LinkedList<View> viewList = scrapArray.get(stickyViewType);
            if(null!=viewList&&!viewList.isEmpty()){
                stickyView=viewList.pollFirst();
            }
            //Update the position.
            if(null!=stickyView){
                StickyOverlayViewGroup.LayoutParams layoutParams = (StickyOverlayViewGroup.LayoutParams) stickyView.getLayoutParams();
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
                stickyView = adapter.onCreateStickyView(parent, stickyViewType);
            } else {
                stickyView=viewList.pollFirst();
            }
            //Update the position.
            StickyOverlayViewGroup.LayoutParams layoutParams = (StickyOverlayViewGroup.LayoutParams) stickyView.getLayoutParams();
            layoutParams.position=position;
            return stickyView;
        }
    }

}
