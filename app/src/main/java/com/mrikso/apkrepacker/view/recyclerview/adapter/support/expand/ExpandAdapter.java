package com.mrikso.apkrepacker.view.recyclerview.adapter.support.expand;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mrikso.apkrepacker.view.recyclerview.adapter.WrapperAdapter;
import com.mrikso.apkrepacker.view.recyclerview.adapter.listener.OnExpandItemClickListener;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by cz
 * @date 2020-03-18 20:58
 * @email bingo110@126.com
 * It's an expand adapter. Actually it's same like the {@link com.mrikso.apkrepacker.view.recyclerview.adapter.support.tree.TreeAdapter}
 * But I just don't know the reason why people likes expand adapter but not tree adapter. Maybe it's more easy to use.
 * We support the same function like the {@link android.widget.ExpandableListAdapter}
 *
 * This adapter could cooperate with {@link com.mrikso.apkrepacker.view.recyclerview.adapter.wrapper.header.HeaderWrapperAdapter}
 * Thus, You could be able to add extra header or footer view.
 *
 * For example
 * <pre>
 *         val headerWrapperAdapter = HeaderWrapperAdapter(adapter)
 *         headerWrapperAdapter.addHeaderView(getHeaderView(headerWrapperAdapter))
 *         recyclerView.adapter=headerWrapperAdapter
 *         buttonExpandAll.setOnClickListener {
 *             adapter.expandAll()
 *         }
 * </pre>
 *
 * All the main function list.
 * @see #expandAll()
 * @see #collapseAll()
 * @see #removeGroup(int)
 * @see #removeGroup(int, int)
 * @see #addGroup(Object, List)
 * @see #addGroup(Object, List, int, boolean)
 * @see #setOnExpandItemClickListener(OnExpandItemClickListener)
 *
 */
public abstract class ExpandAdapter<K, E> extends RecyclerView.Adapter {
    private static final int HEADER_ITEM = 0;
    private static final int CHILD_ITEM = 1;
    private WrapperAdapter wrapperAdapter;
    /**
     * The expand node list.
     */
    private List<Entry<K, List<E>>> expandList;
    private int[] expandStepArray;
    private OnExpandItemClickListener listener;

    /**
     * Create an expand adapter from LinkedHashMap.
     * @param map
     * @return
     */
    public ExpandAdapter(LinkedHashMap<K, List<E>> map) {
        this(map,false);
    }

    /**
     * Create an expand adapter from LinkedHashMap.
     * @param map
     * @param expand
     * @return
     */
    public ExpandAdapter(LinkedHashMap<K, List<E>> map, boolean expand){
        this(convertLinkedHashMap(map),expand);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        //We might put this adapter in a wrapper adapter. {@code HeaderWrapperAdapter}
        //So here we check the adapter and hold the wrapper adapter.
        //If we want to know the mapping position in the recycler view. We could use this function:wrapperAdapter.getExtraCount(position);
        RecyclerView.Adapter adapter = recyclerView.getAdapter();
        if(adapter instanceof WrapperAdapter){
            wrapperAdapter=(WrapperAdapter)adapter;
        }
    }

    /**
     * Convert the linkedHashMap to a arrayList
     * @param items
     * @return
     */
    private static<K,E> List<Entry<K, List<E>>> convertLinkedHashMap(LinkedHashMap<K, List<E>> items) {
        ArrayList<Entry<K, List<E>>> expandList = new ArrayList<>();
        if (!items.isEmpty()) {
            for (Map.Entry<K, List<E>> entry : items.entrySet()) {
                expandList.add(new Entry<>(entry.getKey(), entry.getValue()));
            }
        }
        return expandList;
    }


    private ExpandAdapter(List<Entry<K, List<E>>> items, boolean expand) {
        expandList = new ArrayList<>();
        if (null != items) {
            this.expandList.addAll(items);
            int size = items.size();
            for (int i = 0; i < size; i++) {
                Entry<K, List<E>> entry = expandList.get(i);
                entry.isExpand=expand;
            }
            updateGroupItemInfo(items,true);
        }
    }

    /**
     * Update the group information.
     */
    private void updateGroupItemInfo(List<Entry<K, List<E>>> items,boolean resizeStepArray) {
        if(resizeStepArray){
            expandStepArray = new int[items.size()];
        }
        int count = 0;
        int size = items.size();
        for (int i = 0; i < size; i++) {
            Entry<K, List<E>> entry = items.get(i);
            List<E> children = entry.children;
            expandStepArray[i]=count;
            int itemSize = null == children || !entry.isExpand ? 0 : children.size();
            count += (itemSize + 1);
        }
    }

    public int getGroupCount() {
        return expandList.size();
    }


    public int getChildrenCount(int position) {
        return expandList.get(position).children.size();
    }

    public K getGroup(int position) {
        return expandList.get(position).k;
    }

    public List<E> getGroupItems(int groupPosition) {
        return expandList.get(groupPosition).children;
    }

    public E getChild(int groupPosition, int childPosition) {
        return getGroupItems(groupPosition).get(childPosition);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder = null;
        switch (viewType) {
            case HEADER_ITEM:
                holder = createGroupHolder(parent);
                break;
            case CHILD_ITEM:
                holder = createChildHolder(parent);
                break;
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        int viewType = getItemViewType(position);
        final int groupPosition = getGroupPosition(position);
        switch (viewType) {
            case HEADER_ITEM:
                onBindGroupHolder(holder, groupPosition);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //We subtract the redundant positions
                        int adapterPosition = holder.getAdapterPosition();
                        if(null!=wrapperAdapter){
                            adapterPosition = adapterPosition-wrapperAdapter.getExtraViewCount(adapterPosition);
                        }
                        int newGroupPosition = getGroupPosition(adapterPosition);
                        Entry<K, List<E>> entry = expandList.get(newGroupPosition);
                        boolean isExpand = entry.isExpand;
                        entry.isExpand=!isExpand;
                        onGroupExpand(holder, !isExpand, newGroupPosition);
                        expandGroup(adapterPosition, newGroupPosition, !isExpand);
                    }
                });
                break;
            case CHILD_ITEM:
                final int childPosition = getChildPosition(position);
                onBindChildHolder(holder, groupPosition, childPosition);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (null != listener) {
                            listener.onItemClick(v, groupPosition, childPosition);
                        }
                    }
                });
                break;
        }
    }

    /**
     * When the group is expand.
     * @param holder
     * @param expand
     * @param groupPosition
     */
    protected void onGroupExpand(RecyclerView.ViewHolder holder, boolean expand, int groupPosition) {
    }

    /**
     * Expand the group
     * @param position
     * @param groupPosition
     * @param expand
     */
    private void expandGroup(int position, int groupPosition, boolean expand) {
        List<E> childItems = getGroupItems(groupPosition);
        int expandCount = (null == childItems) ? 0 : childItems.size();
        updateGroupItemInfo(expandList,false);
        if (expand) {
            notifyItemRangeInserted(position+1, expandCount);
        } else {
            notifyItemRangeRemoved(position+1, expandCount);
        }
    }

    public abstract RecyclerView.ViewHolder createGroupHolder(ViewGroup parent);

    public abstract RecyclerView.ViewHolder createChildHolder(ViewGroup parent);

    public abstract void onBindGroupHolder(RecyclerView.ViewHolder holder, int groupPosition);

    public abstract RecyclerView.ViewHolder onBindChildHolder(RecyclerView.ViewHolder holder, int groupPosition, int position);

    @Override
    public int getItemCount() {
        int itemCount=0;
        for (int i = 0; i < expandList.size(); i++) {
            Entry<K, List<E>> entry = expandList.get(i);
            if (null!=entry.children && entry.isExpand) {
                itemCount += entry.children.size();
            }
            itemCount += 1;
        }
        return itemCount;
    }

    @Override
    public int getItemViewType(int position) {
        int findPosition = getSelectPosition(expandStepArray, position);
        Integer stepPosition = expandStepArray[findPosition];
        int viewType = HEADER_ITEM;
        if (0 < position - stepPosition) {
            viewType = CHILD_ITEM;
        }
        return viewType;
    }

    private int getGroupPosition(int position) {
        return getSelectPosition(expandStepArray, position);
    }

    private int getChildPosition(int position) {
        int findPosition = getSelectPosition(expandStepArray, position);
        return position - expandStepArray[findPosition] - 1;
    }

    public static int getSelectPosition(int[] positions, int firstVisiblePosition) {
        int start = 0, end = positions.length;
        while (end - start > 1) {
            int middle = (start + end) >> 1;
            int middleValue = positions[middle];
            if (firstVisiblePosition > middleValue) {
                start = middle;
            } else if (firstVisiblePosition < middleValue) {
                end = middle;
            } else {
                start = middle;
                break;
            }
        }
        return start;
    }

    public void addGroup(K item, List<E> items) {
        int index = getGroupCount();
        addGroup(item, items, index,false);
    }

    public void addGroup(K item, List<E> children, int index, boolean expand) {
        Entry entry = new Entry(item, children);
        entry.isExpand=expand;
        expandList.add(index, entry);
        updateGroupItemInfo(expandList,true);
        int itemSize = null == children || !expand ? 0 : children.size();//添加个数
        int startIndex = expandStepArray[index];
        notifyItemRangeInserted(startIndex, itemSize+1);
    }


    public void removeGroup(int position) {
        if(0 <= position && position < expandList.size()){
            int index = expandStepArray[position];
            int itemSize =0;
            Entry<K, List<E>> entry = expandList.remove(position);
            if(entry.isExpand){
                itemSize=null==entry.children ? 0 :entry.children.size();
            }
            updateGroupItemInfo(expandList,true);
            notifyItemRangeRemoved(index, itemSize+1);
        }
    }

    public void removeGroup(int position, int childPosition) {
        if (expandList.isEmpty()) return;
        int groupPosition = expandStepArray[position];
        Entry<K, List<E>> entry = expandList.get(position);
        List children = entry.children;
        if (!children.isEmpty()){
            children.remove(childPosition);
            if (entry.isExpand) {
                updateGroupItemInfo(expandList,true);
                int startIndex = groupPosition + 1;
                int removePosition = startIndex + childPosition;
                notifyItemRemoved(removePosition);
            }
        }
        //notify this group has changed.
        notifyItemChanged(groupPosition);
    }

    public void swap(LinkedHashMap<K, List<E>> items) {
        swap(convertLinkedHashMap(items),false);
    }

    public void swap(List<Entry<K, List<E>>> items, boolean expand) {
        if (!items.isEmpty()) {
            expandStepArray=null;
            expandList.clear();
            expandList.addAll(items);
            for (int i = 0; i < expandList.size(); i++) {
                Entry<K, List<E>> entry = expandList.get(i);
                entry.isExpand=expand;
            }
            updateGroupItemInfo(expandList,true);
            notifyDataSetChanged();
        }
    }

    public void expandAll() {
        for (int i = 0; i < expandList.size(); i++) {
            Entry<K, List<E>> entry = expandList.get(i);
            entry.isExpand=true;
        }
        updateGroupItemInfo(expandList,false);
        notifyDataSetChanged();
    }

    public void collapseAll() {
        for (int i = 0; i < expandList.size(); i++) {
            Entry<K, List<E>> entry = expandList.get(i);
            entry.isExpand=false;
        }
        updateGroupItemInfo(expandList,false);
        notifyDataSetChanged();
    }

    public boolean getGroupExpand(int position) {
        Entry<K, List<E>> entry = expandList.get(position);
        return entry.isExpand;
    }

    public void setOnExpandItemClickListener(OnExpandItemClickListener listener) {
        this.listener = listener;
    }

    /**
     * The expand list entry.
     * @param <K>
     * @param <E>
     */
    static class Entry<K, E> {
        /**
         * It's the header item object.
         */
        private final K k;
        /**
         * The sub-children.
         */
        private final E children;
        /**
         * The bool that determines if we close or open the node.
         */
        private boolean isExpand=false;

        public Entry(K k, E children) {
            this.k = k;
            this.children = children;
        }
    }

}