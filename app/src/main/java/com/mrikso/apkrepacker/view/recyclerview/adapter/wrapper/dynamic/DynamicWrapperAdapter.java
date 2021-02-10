package com.mrikso.apkrepacker.view.recyclerview.adapter.wrapper.dynamic;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.mrikso.apkrepacker.view.recyclerview.adapter.wrapper.header.HeaderWrapperAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * @author Created by cz
 * @date 2020-03-17 11:36
 * @email bingo110@126.com
 *
 * It's a complicated adapter. We call it dynamic adapter cause it makes the adapter change dynamically
 * You could easily understand the common adapter:{@link HeaderWrapperAdapter} It's only add header or footer for the original adapter.
 * This adapter lets you add extra views in anywhere by a mapping position array
 *
 * Here are an example:
 * <pre>
 *     The original adapter was ["a","b","c","d","e","f"]
 *
 *     If we want to insert a word in position:2. Then the list will turn to.
 *     ["a","b",|"xx"|,"c","d","e","f"]
 *
 *     So far it's not very difficult. But remember we are not add the word in this list.
 *     We actually have our own list to store the word.
 *
 *     The list will looks like this one:["xx":2]
 *     The original list didn't change:["a","b","c","d","e","f"]
 *
 *     But when you ask for a position like:2 which the item is "c"
 *     We start to calculate the list. after we cross the position:2. We found there is an extra position.
 *     We subtract the redundant positions. We will get position2 cause we subtract the extra view count.
 *
 *     For you, nothing has changed. But we actually change the adapter without ruin your adapter.
 * <pre/>
 *
 * We could use this special adapter to do a lot of things.
 * Such as insert advertising to the list. or cooperate with the {@link com.mrikso.apkrepacker.view.recyclerview.adapter.wrapper.drag.DragWrapperAdapter} to drag anything in a recycler view.
 *
 */
public class DynamicWrapperAdapter extends HeaderWrapperAdapter {
    private static final int TYPE_DYNAMIC = -1 << 8;
    private List<FixedViewInfo> fixedViewInfoList=new ArrayList<>();
    /**
     * This number only for count how many view we added.
     * Not equal to the fixed view size. To avoid we have the same view type.
     */
    private int dynamicCount=0;

    public DynamicWrapperAdapter(@Nullable RecyclerView.Adapter adapter) {
        super(adapter);
    }

    @Override
    public View findView(int id) {
        //find view from dynamic list.
        View findView = super.findView(id);
        if(null==findView){
            for(int i=0;i<fixedViewInfoList.size();i++){
                FixedViewInfo fixedViewInfo = fixedViewInfoList.get(i);
                View view = fixedViewInfo.view.findViewById(id);
                if(null!=view){
                    findView=view;
                    break;
                }
            }
        }
        return findView;
    }

    /**
     * Add a view to a specific position.
     * @param view
     * @param position
     */
    public void addAdapterView(@NonNull View view, int position){
        for(FixedViewInfo fixedViewInfo:fixedViewInfoList){
            //We offset the position if the fixed item's position more than the insert position.
            if(fixedViewInfo.position >= position){
                fixedViewInfo.position++;
            }
        }
        int viewType = TYPE_DYNAMIC + dynamicCount++;
        fixedViewInfoList.add(new FixedViewInfo(viewType,view,position));
        Collections.sort(fixedViewInfoList,new Comparator<FixedViewInfo>() {
            @Override
            public int compare(FixedViewInfo o1, FixedViewInfo o2) {
                return o1.position-o2.position;
            }
        });
        notifyItemInserted(position);
    }

    public void removeAdapterView(@Nullable View view){
        int index=-1;
        for(int i=0;i<fixedViewInfoList.size();i++){
            FixedViewInfo fixedViewInfo = fixedViewInfoList.get(i);
            if(fixedViewInfo.view==view){
                index=i;
                break;
            }
        }
        if(-1!=index){
            removeAdapterView(index);
        }
    }

    /**
     * Remove a view in a specific position
     */
    public void removeAdapterView(int position){
        //Remove this position.
        FixedViewInfo removeViewInfo = fixedViewInfoList.remove(position);
        for(int i=0;i<fixedViewInfoList.size();i++){
            FixedViewInfo fixedViewInfo = fixedViewInfoList.get(i);
            //We move the position back. Because we removed one item.
            if(fixedViewInfo.position>removeViewInfo.position){
                fixedViewInfo.position--;
            }
        }
        notifyItemRemoved(removeViewInfo.position);
    }


    public int findPosition(int position){
        if(fixedViewInfoList.isEmpty()){
            return RecyclerView.NO_POSITION;
        } else {
            int low = 0;
            int high = fixedViewInfoList.size() - 1;
            while (low <= high) {
                int mid = (low + high) >>> 1;
                FixedViewInfo fixedViewInfo = fixedViewInfoList.get(mid);
                if (position < fixedViewInfo.position) {
                    high = mid - 1;
                } else if (position > fixedViewInfo.position) {
                    low = mid + 1;
                } else {
                    return mid;
                }
            }
            return RecyclerView.NO_POSITION;
        }
    }

    protected void setFixedViewPosition(int index, int position){
        FixedViewInfo fixedViewInfo = fixedViewInfoList.get(index);
        if(null!=fixedViewInfo){
            fixedViewInfo.position=position;
        }
        Collections.sort(fixedViewInfoList,new Comparator<FixedViewInfo>() {
            @Override
            public int compare(FixedViewInfo o1, FixedViewInfo o2) {
                return o1.position-o2.position;
            }
        });
    }

    /**
     * Return how many extra view we have.
     * @return
     */
    public int getExtraViewCount(){
        return fixedViewInfoList.size();
    }

    /**
     * Find a view by its view type. Because the view type is unique for each fixed view info.
     * @param viewType
     * @return
     */
    @Nullable
    private View findAdapterView(int viewType){
        View findView=null;
        for(FixedViewInfo fixedViewInfo:fixedViewInfoList){
            if(fixedViewInfo.viewType==viewType){
                findView=fixedViewInfo.view;
            }
        }
        return findView;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = findAdapterView(viewType);
        if(null!=view){
            return new RecyclerView.ViewHolder(view) {};
        } else {
            return super.onCreateViewHolder(parent, viewType);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int findPosition = findPosition(position);
        if(RecyclerView.NO_POSITION==findPosition){
            super.onBindViewHolder(holder, position);
        }
    }

    @Override
    public int getItemViewType(int position) {
        int findPosition = findPosition(position);
        if(RecyclerView.NO_POSITION==findPosition){
            return super.getItemViewType(position);
        } else {
            int viewType=0;
            for(FixedViewInfo fixedViewInfo:fixedViewInfoList){
                if(fixedViewInfo.position==position){
                    viewType=fixedViewInfo.viewType;
                }
            }
            return viewType;
        }
    }

    @Override
    public int getItemCount() {
        int itemCount = super.getItemCount();
        return fixedViewInfoList.size()+itemCount;
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    public void itemRangeInsert(int positionStart,int itemCount){
        int position = getOffsetPosition(positionStart);
        for(FixedViewInfo fixedViewInfo:fixedViewInfoList){
            if (position < fixedViewInfo.position) {
                //move forward.
                fixedViewInfo.position += itemCount;
            }
        }
        if(RecyclerView.NO_POSITION!=findPosition(position)){
            notifyItemRangeInserted(position+1, itemCount);
        } else {
            notifyItemRangeInserted(position, itemCount);
        }
    }

    public void itemRangeRemoved(int position,int itemCount){
        int positionStart= getOffsetPosition(position);
        //This is outside already remove the data. So from the function:getItemCount().
        //The total size was not accurate. So we plus the parameter itemCount.
        int adapterItemCount = getItemCount();
        int positionEnd=Math.min(adapterItemCount+itemCount, getOffsetPosition(positionStart+itemCount));
        int acrossItemCount=positionEnd-positionStart;
        //If the position within the range. we remove it.
        for(Iterator<FixedViewInfo> iterator = fixedViewInfoList.iterator(); iterator.hasNext();){
            FixedViewInfo fixedViewInfo = iterator.next();
            if(positionStart <= fixedViewInfo.position &&  fixedViewInfo.position < positionEnd){
                iterator.remove();
            }
        }
        for(FixedViewInfo fixedViewInfo:fixedViewInfoList){
            if(positionEnd <= fixedViewInfo.position){
                fixedViewInfo.position-=acrossItemCount;
            }
        }
        notifyItemRangeRemoved(positionStart, acrossItemCount);
    }

    /**
     * This position comes from the warped data adapter.
     * @see DynamicWrapperAdapter#itemRangeMoved(int, int, int)
     * @see DynamicWrapperAdapter#itemRangeRemoved(int, int)
     * @see DynamicWrapperAdapter#itemRangeInsert(int, int)
     *
     * All the functions above were from the wrapped adapter.
     * It's usually less than the real position.
     *
     * If there is a fixed position in position: 1
     * The wrapped adpater's data was ["a","b","c","d"]. The wrapper adapter was:"a",|"xxx"|,"b","c","d"
     * If the position was 3. Then we will return 4 by traversal from 0 until 3.
     *
     */
    @Override
    public int getOffsetPosition(int position) {
        //plus the extra layout size
        int itemCount = 0;
        int totalCount = position;
        int startPosition = getExtraViewCount(position);
        while(itemCount<startPosition){
            int findPosition = findPosition(totalCount);
            //If it's not a fixed position. we increase the counter.
            if (RecyclerView.NO_POSITION==findPosition) {
                itemCount++;
            }
            totalCount++;
        }
        return totalCount;
    }

    @Override
    public int getExtraViewCount(int position){
        int extraViewCount = super.getExtraViewCount(position);
        int start = 0;
        int result = RecyclerView.NO_POSITION;
        int end = fixedViewInfoList.size() - 1;
        while (start <= end) {
            int middle = (start + end) / 2;
            FixedViewInfo fixedViewInfo = fixedViewInfoList.get(middle);
            if (position == fixedViewInfo.position) {
                result = middle;
                break;
            } else if (position < fixedViewInfo.position) {
                end = middle - 1;
            } else {
                start = middle + 1;
            }
        }
        if (RecyclerView.NO_POSITION == result) {
            result = start;
        }
        return extraViewCount+result;
    }

    @Override
    protected boolean isExtraPosition(int position) {
        return super.isExtraPosition(position)||
                RecyclerView.NO_POSITION != findPosition(position);
    }


    @Override
    protected boolean onItemClick(View v, int position, int adapterPosition) {
        return super.onItemClick(v, position, adapterPosition);
    }

    public static class FixedViewInfo {
        public final int viewType;
        public final View view;
        public int position;

        public FixedViewInfo(int viewType, View view,int position) {
            this.viewType = viewType;
            this.view = view;
            this.position=position;
        }
    }

}
