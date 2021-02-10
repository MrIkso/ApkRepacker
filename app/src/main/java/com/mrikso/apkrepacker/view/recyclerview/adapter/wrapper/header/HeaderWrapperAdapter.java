package com.mrikso.apkrepacker.view.recyclerview.adapter.wrapper.header;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.mrikso.apkrepacker.view.recyclerview.adapter.WrapperAdapter;
import com.mrikso.apkrepacker.view.recyclerview.adapter.listener.OnItemClickListener;
import com.mrikso.apkrepacker.view.recyclerview.adapter.listener.OnItemLongClickListener;
import com.mrikso.apkrepacker.view.recyclerview.adapter.wrapper.WrapperAdapterDataObserver;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cz
 * @date 2020-02-29 20:29
 * @email bingo110@126.com
 * A header or footer wrapper adapter.
 * We wrap the original adapter to support the fixed header or footer view type.
 * We also support remove fixed item from the adapter.
 *
 * Take a look at this function.
 * androidx.recyclerview.widget.RecyclerView.Adapter#registerAdapterDataObserver(androidx.recyclerview.widget.RecyclerView.AdapterDataObserver)
 * @see WrapperAdapterDataObserver The header adapter data observe.
 *
 * @see HeaderWrapperAdapter#addHeaderView(View)
 * @see HeaderWrapperAdapter#addFooterView(View)
 * @see HeaderWrapperAdapter#setOnItemClickListener(OnItemClickListener)
 */
public class HeaderWrapperAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements WrapperAdapter {
    private final int TYPE_EXTRAS = -1;//We decrease the number from negative one.
    private final RecyclerView.AdapterDataObserver adapterDataObserve=new WrapperAdapterDataObserver(this);
    private final List<FixedViewInfo> headerViewArray=new ArrayList<>();
    private final List<FixedViewInfo> footerViewArray=new ArrayList<>();
    /**
     * We use this value as a counter.
     */
    private int fixedViewCount=0;
    private RecyclerView.Adapter adapter;
    private OnItemClickListener itemClickListener;
    private OnItemLongClickListener longClickListener;

    public HeaderWrapperAdapter(@Nullable RecyclerView.Adapter adapter) {
        if(null!=adapter){
            this.adapter = adapter;
            this.adapter.registerAdapterDataObserver(adapterDataObserve);
        }
    }

    /**
     * Change the adapter.
     * @param adapter
     */
    public void setAdapter(@NonNull RecyclerView.Adapter adapter) {
        RecyclerView.Adapter oldAdapter = this.adapter;
        if(null!=oldAdapter){
            oldAdapter.unregisterAdapterDataObserver(adapterDataObserve);
        }
        this.adapter = adapter;
        this.adapter.registerAdapterDataObserver(adapterDataObserve);
        notifyDataSetChanged();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        if(null!=adapter){
            adapter.onAttachedToRecyclerView(recyclerView);
        }
        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
        if (manager instanceof GridLayoutManager) {
            final GridLayoutManager gridLayoutManager = ((GridLayoutManager) manager);
            final GridLayoutManager.SpanSizeLookup spanSizeLookup = gridLayoutManager.getSpanSizeLookup();
            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    int spanSize = isExtraPosition(position) ? gridLayoutManager.getSpanCount() : 1;
                    if(null!=spanSizeLookup){
                        //For a grid layout manager, we usually set one special position to fill the column.
                        return Math.max(spanSize,spanSizeLookup.getSpanSize(position));
                    } else {
                        return spanSize;
                    }
                }
            });
        }
    }

    /**
     * For a {@link GridLayoutManager} You might want to add a special column that is full.
     * So override this function and return true
     * @param position
     * @return
     */
    protected boolean isExtraPosition(int position){
        return isHeaderPosition(position) || isFooterPosition(position);
    }

    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
        if (lp != null && lp instanceof StaggeredGridLayoutManager.LayoutParams && isExtraPosition(holder.getAdapterPosition())) {
            StaggeredGridLayoutManager.LayoutParams p = (StaggeredGridLayoutManager.LayoutParams) lp;
            p.setFullSpan(true);
        }
    }

    /**
     * Check if the position if header
     * @param position
     * @return
     */
    protected boolean isHeaderPosition(int position) {
        int headerViewCount = getHeaderViewCount();
        return position < headerViewCount;
    }

    /**
     * Check if the position if footer.
     * @param position
     * @return
     */
    protected boolean isFooterPosition(int position) {
        int startPosition = getFooterStartPosition();
        return startPosition <= position;
    }

    /**
     * Return the start position of the footer.
     * @return
     */
    private int getFooterStartPosition() {
        int itemCount = getItemCount();
        int footerViewCount = getFooterViewCount();
        return itemCount-footerViewCount;
    }

    public int getHeaderViewCount() {
        return headerViewArray.size();
    }

    public int getFooterViewCount() {
        return footerViewArray.size();
    }

    /**
     * Add a header view.
     * @param view
     */
    public void addHeaderView(@NonNull View view) {
        int viewType = TYPE_EXTRAS - fixedViewCount++;
        headerViewArray.add(new FixedViewInfo(viewType, view));
        notifyItemInserted(headerViewArray.size()-1);
    }

    /**
     * Add a header view by index. We only use this method to manipulate some special functions.
     * Such as fixed a refreshing footer or bottom line.
     * @param view
     * @param index
     */
    protected void addFooterView(@NonNull View view, int index) {
        int footerViewCount = getFooterViewCount();
        if(0 > index|| index > footerViewCount){
            throw new IllegalArgumentException("The footer index is out of bound!");
        }
        int viewType = TYPE_EXTRAS - fixedViewCount++;
        this.footerViewArray.add(index,new FixedViewInfo(viewType,view));
        int startPosition = getFooterStartPosition();
        notifyItemInserted(startPosition + index);
    }

    /**
     * Add a footer view.
     * @param view
     */
    public void addFooterView(@NonNull View view) {
        int footerViewCount = getFooterViewCount();
        addFooterView(view, footerViewCount);
    }

    /**
     * Return a header view by index.
     * @param index
     * @return
     */
    @Nullable
    public View getHeaderView(int index) {
        View view = null;
        int headerViewCount = getHeaderViewCount();
        if (0 <= index && index < headerViewCount) {
            view = headerViewArray.get(index).view;
        }
        return view;
    }

    /**
     * Return a footer view by index
     * @param index
     * @return
     */
    @Nullable
    public View getFooterView(int index) {
        View view = null;
        int footerViewCount = getFooterViewCount();
        if (0 <= index && index < footerViewCount) {
            view = footerViewArray.get(index).view;
        }
        return view;
    }

    /**
     * If you want to find a view from the fixed view list.
     * The sub-class could override this function to support find the view from somewhere.
     * @param id
     * @return
     */
    public View findView(@IdRes int id){
        View findView = findViewInternal(headerViewArray,id);
        if(null==findView){
            findView=findViewInternal(footerViewArray,id);
        }
        return findView;
    }

    private View findViewInternal(List<FixedViewInfo> viewArray,@IdRes int id){
        View findView = null;
        for (int i = 0; i < viewArray.size(); i++) {
            FixedViewInfo viewItem = viewArray.get(i);
            View view = viewItem.view.findViewById(id);
            if (null!=view) {
                findView=view;
                break;
            }
        }
        return findView;
    }

    /**
     * Remove a specific header view
     *
     * @param view
     */
    public void removeHeaderView(View view) {
        if (null == view) return;
        removeHeaderView(indexOfView(headerViewArray, view));
    }

    public int indexOfHeaderView(View view){
        return indexOfView(headerViewArray, view);
    }

    /**
     * 移除指定的HeaderView对象
     *
     * @param position
     */
    public void removeHeaderView(int position) {
        if (isHeaderPosition(position)){
            headerViewArray.remove(position);
            notifyItemRemoved(position);
        }
    }

    /**
     * 移除指定的HeaderView对象
     *
     * @param view
     */
    public void removeFooterView(View view) {
        if (null == view) return;
        removeFooterView(indexOfView(footerViewArray, view));
    }

    /**
     * Remove all the header views.
     */
    public void clearHeaderViews(){
        int headerViewCount = getHeaderViewCount();
        headerViewArray.clear();
        notifyItemRangeRemoved(0,headerViewCount);
    }

    /**
     * Remove footer view by index.
     * @param position
     */
    public void removeFooterView(int position) {
        int footerViewCount = getFooterViewCount();
        if (0 <= position && position < footerViewCount){
            footerViewArray.remove(position);
            int startFooterStartPosition = getFooterStartPosition();
            notifyItemRemoved(startFooterStartPosition+position);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder;
        if (TYPE_EXTRAS >= viewType) {
            View extraView = getExtraView(headerViewArray, footerViewArray, viewType);
            holder = new RecyclerView.ViewHolder(extraView) {};
        } else {
            holder = adapter.onCreateViewHolder(parent, viewType);
        }
        return holder;
    }

    private int indexOfView(List<FixedViewInfo> items, View view) {
        int index = -1;
        for (int i = 0; i < items.size(); i++) {
            FixedViewInfo viewItem = items.get(i);
            if (viewItem.view == view) {
                index = i;
                break;
            }
        }
        return index;
    }

    /**
     * Find a view by its view type. We try to find the view from the header view array and footer view array.
     * @param headerViews
     * @param footerViews
     * @param type
     * @return
     */
    private View getExtraView(List<FixedViewInfo> headerViews,List<FixedViewInfo> footerViews, int type) {
        View view = null;
        for (int i = 0; i < headerViews.size(); i++) {
            FixedViewInfo viewItem = headerViews.get(i);
            if (viewItem.viewType == type) {
                view = viewItem.view;
                break;
            }
        }
        if(null==view){
            for (int i = 0; i < footerViews.size(); i++) {
                FixedViewInfo viewItem = footerViews.get(i);
                if (viewItem.viewType == type) {
                    view = viewItem.view;
                    break;
                }
            }
        }
        return view;
    }

//    @Override
//    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List<Object> payloads) {
//        // todoif we want to have a payload...
//    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (!isExtraPosition(position)) {
            //the start position of the original adapter.
            if (null != adapter) {
                int extraViewCount = getExtraViewCount(position);
                //Let the original adapter bind the ViewHolder.
                adapter.onBindViewHolder(holder, position-extraViewCount);
                //Here is our default View click listener.

                //Make sure we won't override the user's user's click listener.
                if(!holder.itemView.hasOnClickListeners()){
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            int adapterPosition = holder.getAdapterPosition();
                            int itemPosition = adapterPosition - getExtraViewCount(adapterPosition);
                            //Here we should call the method:onItemClick first.
                            if (onItemClick(v, itemPosition,adapterPosition)&&null != itemClickListener) {
                                itemClickListener.onItemClick(v, itemPosition,adapterPosition);
                            }
                        }
                    });
                }
                if(!holder.itemView.isLongClickable()){
                    holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            int adapterPosition = holder.getAdapterPosition();
                            int itemPosition = adapterPosition - getExtraViewCount(adapterPosition);
                            if (null!=longClickListener) {
                                return longClickListener.onLongItemClick(v, itemPosition,adapterPosition);
                            }
                            return false;
                        }
                    });
                }
            }
        }
    }

    /**
     * The item click listener. If the sub-class want to consume it. Then return false.
     * @param v
     * @param position
     * @param adapterPosition
     * @return
     */
    protected boolean onItemClick(View v, int position,int adapterPosition) {
        return true;
    }

    public int getAdapterItemCount(){
        int itemCount=0;
        if(null!=adapter){
            itemCount = adapter.getItemCount();
        }
        return itemCount;
    }

    @Override
    public int getItemCount() {
        int headerViewCount = getHeaderViewCount();
        int adapterItemCount = getAdapterItemCount();
        int footerViewCount = getFooterViewCount();
        return headerViewCount+adapterItemCount+footerViewCount;
    }

    @Override
    public int getItemViewType(int position) {
        int itemType = TYPE_EXTRAS;
        if (isHeaderPosition(position)) {
            itemType = headerViewArray.get(position).viewType;
        } else if (isFooterPosition(position)) {
            int startFooterStartPosition = getFooterStartPosition();
            itemType = footerViewArray.get(position-startFooterStartPosition).viewType;
        } else {
            if (adapter != null) {
                int adapterPosition = position - getExtraViewCount(position);
                int adapterItemCount = getAdapterItemCount();
                if (adapterPosition < adapterItemCount) {
                    itemType = adapter.getItemViewType(adapterPosition);
                }
            }
        }
        return itemType;
    }

    @Override
    public long getItemId(int position) {
        if (adapter != null && !isExtraPosition(position)) {
            position = -getExtraViewCount(position);
            int itemCount = adapter.getItemCount();
            if (position < itemCount) {
                return adapter.getItemId(position);
            }
        }
        return -1;
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if(null!=adapter){
            adapter.onViewRecycled(holder);
        }
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        if(null!=adapter){
            adapter.onDetachedFromRecyclerView(recyclerView);
        }
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.itemClickListener=listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener){
        this.longClickListener=listener;
    }

    @Override
    public RecyclerView.Adapter getAdapter() {
        return adapter;
    }

    @Override
    public int getOffsetPosition(int position) {
        int extraViewCount = getHeaderViewCount();
        return extraViewCount+position;
    }

    @Override
    public int getExtraViewCount(int position) {
        return getHeaderViewCount();
    }

    @Override
    public void onChanged() {
        notifyDataSetChanged();
    }

    @Override
    public void itemRangeInsert(int positionStart, int itemCount) {
        int offsetPosition = getOffsetPosition(positionStart);
        notifyItemRangeInserted(offsetPosition, itemCount);
    }

    @Override
    public void itemRangeChanged(int positionStart, int itemCount) {
        int offsetPosition = getOffsetPosition(positionStart);
        notifyItemRangeChanged(offsetPosition, itemCount);
    }

    @Override
    public void itemRangeChanged(int positionStart, int itemCount, @Nullable Object payload) {
        int offsetPosition = getOffsetPosition(positionStart);
        notifyItemRangeChanged(offsetPosition, itemCount, payload);
    }

    @Override
    public void itemRangeRemoved(int positionStart, int itemCount) {
        int offsetPosition = getOffsetPosition(positionStart);
        notifyItemRangeRemoved(offsetPosition, itemCount);
    }

    @Override
    public void itemRangeMoved(int fromPosition, int toPosition, int itemCount) {
        int offsetPosition = getOffsetPosition(fromPosition);
        notifyItemMoved(offsetPosition,toPosition);
    }

    public static class FixedViewInfo {
        public final int viewType;
        public final View view;

        public FixedViewInfo(int viewType, View view) {
            this.viewType = viewType;
            this.view = view;
        }
    }
}