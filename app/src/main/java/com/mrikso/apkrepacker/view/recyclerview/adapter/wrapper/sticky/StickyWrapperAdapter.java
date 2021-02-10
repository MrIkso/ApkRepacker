package com.mrikso.apkrepacker.view.recyclerview.adapter.wrapper.sticky;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mrikso.apkrepacker.view.recyclerview.adapter.wrapper.header.HeaderWrapperAdapter;

/**
 * Created by cz
 * @date 2020-03-25 22:34
 * @email bingo110@126.com
 *
 * It's a simple sticky header wrapper adapter.
 * This is a funny wrapper adapter. After I've finished the {@link StickyAdapter}.
 * I felt there is something wrong.
 * Let is review the how we implement a sticky header adapter.
 * We return a sticky view by call this function:{@link StickyCallback#onCreateStickyView(ViewGroup, int)}
 * And you have to have the same sticky view in all of your adapter view. It's hard to make me to feel that's a good idea.
 *
 * So that's why we have this wrapper adapter. This adapter takes care of your sticky header view.
 *
 * Here is how we did it.
 * First we change the grouping strategy. We add the sticky as a different view type, treat this sticky view as a view it should in the adapter.
 * But not in your adapter's view. That you need to hide or show in your adapter.
 *
 * It's won't change your code when you use {@link StickyAdapter}. We just wrapped your adapter and change everything.
 *
 * Because it extend from {@link HeaderWrapperAdapter} So you will be able to add extra header or footer view.
 *
 * @see StickyCallback The class support you initialize your sticky view and bind data to the sticky view.
 */
public class StickyWrapperAdapter<A extends RecyclerView.Adapter&StickyCallback> extends HeaderWrapperAdapter {
    private static final int STICKY_HEADER_ITEM = -1 << 7;
    private A adapter;

    public StickyWrapperAdapter(@NonNull A adapter) {
        super(adapter);
        this.adapter=adapter;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        StickyGroupingStrategy groupingStrategy = adapter.getGroupingStrategy();
        groupingStrategy.setOnAdapterGroupingListener(new StickyGroupingStrategy.OnAdapterGroupingListener() {
            @Override
            public int onAdapterGroup(int position) {
                int extraViewCount = getExtraViewCount(position);
                return position+extraViewCount;
            }
        });
        groupingStrategy.updateAdapterGroup();

    }

    @Override
    public void setAdapter(@NonNull RecyclerView.Adapter adapter) {
        super.setAdapter(adapter);
        // throw new IllegalArgumentException("If the adapter not a sub-class of StickyCallback. We thrown an exception!")
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType<=STICKY_HEADER_ITEM){
            //We return the sticky header view.
            return new RecyclerView.ViewHolder(adapter.onCreateStickyView(parent,STICKY_HEADER_ITEM-viewType)){};
        } else {
            return super.onCreateViewHolder(parent,viewType);
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        int viewType = getItemViewType(position);
        if(viewType<=STICKY_HEADER_ITEM){
            int stickyViewType = adapter.getStickyViewType(position);
            adapter.onBindStickyView(holder.itemView,stickyViewType,position);
        } else {
            super.onBindViewHolder(holder,position);
        }
    }

    @Override
    public int getItemCount() {
        int itemCount = super.getItemCount();
        StickyGroupingStrategy groupingStrategy = adapter.getGroupingStrategy();
        return itemCount+groupingStrategy.getGroupCount();
    }

    @Override
    public int getItemViewType(int position) {
        StickyGroupingStrategy groupingStrategy = adapter.getGroupingStrategy();
        if(groupingStrategy.isGroupPosition(position)){
            //We return a different view type from the number:STICKY_HEADER_ITEM
            int stickyViewType = adapter.getStickyViewType(position);
            return STICKY_HEADER_ITEM-stickyViewType;
        } else {
            return super.getItemViewType(position);
        }
    }

    @Override
    public int getExtraViewCount(int position){
        int extraViewCount = super.getExtraViewCount(position);
        StickyGroupingStrategy groupingStrategy = adapter.getGroupingStrategy();
        int groupCount = groupingStrategy.getGroupCount(position);
        return extraViewCount+groupCount;
    }

    @Override
    public int getOffsetPosition(int position) {
        //plus the extra layout size
        int extraViewCount = getExtraViewCount(position);
        return position+extraViewCount;
    }
}