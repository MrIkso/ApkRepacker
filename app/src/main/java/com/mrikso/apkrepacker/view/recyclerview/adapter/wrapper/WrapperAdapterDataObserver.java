package com.mrikso.apkrepacker.view.recyclerview.adapter.wrapper;


import androidx.recyclerview.widget.RecyclerView;

import com.mrikso.apkrepacker.view.recyclerview.adapter.WrapperAdapter;

/**
 * The wrapper adapter data observer.
 * We won't do anything here. It's just dispatching any change to the wrapper adapter.w
 * The implementation of {@link WrapperAdapter} will do something if they need.
 *
 * @see com.mrikso.apkrepacker.view.recyclerview.adapter.wrapper.header.HeaderWrapperAdapter
 * @see com.mrikso.apkrepacker.view.recyclerview.adapter.wrapper.dynamic.DynamicWrapperAdapter
 * @see WrapperAdapter
 */
public class WrapperAdapterDataObserver<E extends WrapperAdapter> extends RecyclerView.AdapterDataObserver {
    private E wrapperAdapter;

    public WrapperAdapterDataObserver(E mWrapAdapter) {
        this.wrapperAdapter = mWrapAdapter;
    }

    @Override
    public void onChanged() {
        wrapperAdapter.onChanged();
    }

    @Override
    public void onItemRangeInserted(int positionStart, int itemCount) {
        wrapperAdapter.itemRangeInsert(positionStart, itemCount);
    }

    @Override
    public void onItemRangeChanged(int positionStart, int itemCount) {
        wrapperAdapter.itemRangeChanged(positionStart, itemCount);
    }

    @Override
    public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
        wrapperAdapter.itemRangeChanged(positionStart, itemCount, payload);
    }

    @Override
    public void onItemRangeRemoved(int positionStart, int itemCount) {
        wrapperAdapter.itemRangeRemoved(positionStart, itemCount);
    }

    @Override
    public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
        wrapperAdapter.itemRangeMoved(fromPosition,toPosition,itemCount);
    }
}