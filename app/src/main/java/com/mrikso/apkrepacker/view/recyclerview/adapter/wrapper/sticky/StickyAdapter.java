package com.mrikso.apkrepacker.view.recyclerview.adapter.wrapper.sticky;

import android.graphics.Canvas;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.mrikso.apkrepacker.view.recyclerview.adapter.BaseAdapter;
import com.mrikso.apkrepacker.view.recyclerview.adapter.wrapper.sticky.group.CompareGroupCondition;
import com.mrikso.apkrepacker.view.recyclerview.adapter.wrapper.sticky.group.GroupCondition;

import java.util.List;

/**
 * @author Created by cz
 * @date 2020-03-24 16:58
 * @email bingo110@126.com
 */
public abstract class StickyAdapter<VH extends RecyclerView.ViewHolder,E> extends BaseAdapter<VH,E> implements StickyCallback<E>{
    /**
     * The recycler view. We won't keep the layout manager. Because maybe We will change the layout manager somehow.
     */
    private RecyclerView recyclerView;
    /**
     * The setCompareCondition strategy.
     */
    private StickyGroupingStrategy<StickyAdapter<VH,E>,E> groupingStrategy= StickyGroupingStrategy.of(this);

    public StickyAdapter(@NonNull List<E> itemList) {
        super(itemList);
    }

    public void setCompareCondition(CompareGroupCondition<E> compareCondition) {
        groupingStrategy.setCompareCondition(compareCondition);
    }

    public void setCondition(GroupCondition<E> condition){
        groupingStrategy.setCondition(condition);
    }

    @Override
    public void onAttachedToRecyclerView(final RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView=recyclerView;
        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
        if (manager instanceof GridLayoutManager) {
            final GridLayoutManager gridLayoutManager = ((GridLayoutManager) manager);
            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    return isFillStickyHeader(position) ? gridLayoutManager.getSpanCount() : 1;
                }
            });
        }
        StickyOverlayViewGroup stickyOverlayViewGroup = new StickyOverlayViewGroup(recyclerView);
        final ViewGroup overlayView = stickyOverlayViewGroup.getOverlayView();
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                super.onDrawOver(c, parent, state);
                overlayView.draw(c);
            }
        });
        final StickyRecyclerViewScrollListener recyclerViewScrollListener = new StickyRecyclerViewScrollListener(recyclerView,this, stickyOverlayViewGroup);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                recyclerViewScrollListener.onScrolled(recyclerView,dx,dy);
            }
        });
        //When the data has changed. We update the sticky views.
        groupingStrategy.setOnAdapterDataChangeListener(new StickyGroupingStrategy.OnAdapterDataChangeListener() {
            @Override
            public void onDataChanged() {
                recyclerViewScrollListener.onScrolled(recyclerView,0,0);
            }
        });
    }

    /**
     * For The GridLayoutManager or the StaggeredGridLayoutManager. We maybe have more than one group sticky.
     * So we could check if it's the same sticky group
     * @return*/
    public int getSpanCount(){
        int spanCount=1;
        if(null!=recyclerView){
            RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
            if(null!=layoutManager){
                if(layoutManager instanceof GridLayoutManager){
                    GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
                    spanCount=gridLayoutManager.getSpanCount();
                } else if(layoutManager instanceof StaggeredGridLayoutManager){
                    StaggeredGridLayoutManager staggeredGridLayoutManager = (StaggeredGridLayoutManager) layoutManager;
                    spanCount=staggeredGridLayoutManager.getSpanCount();
                }
            }
        }
        return spanCount;
    }

    @Override
    public boolean isFillStickyHeader(int position) {
        return groupingStrategy.isGroupPosition(position);
    }

    @Override
    public int getStickyViewType(int position) {
        return 0;
    }

    @Override
    public int getItemViewType(int position) {
         return super.getItemViewType(position);
    }

    @Override
    public StickyGroupingStrategy<StickyAdapter<VH,E>,E> getGroupingStrategy() {
        return groupingStrategy;
    }
}
