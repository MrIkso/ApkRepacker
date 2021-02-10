package com.mrikso.apkrepacker.view.recyclerview.adapter.wrapper.sticky;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.mrikso.apkrepacker.view.recyclerview.adapter.wrapper.sticky.group.CompareGroupCondition;
import com.mrikso.apkrepacker.view.recyclerview.adapter.wrapper.sticky.group.GroupCondition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Created by cz
 * @date 2020-03-23 21:13
 * @email bingo110@126.com
 */
public class StickyGroupingStrategy<A extends RecyclerView.Adapter& StickyCallback<T>,T> {
    private A adapter;
    private int[] adapterIndexArray;
    private CompareGroupCondition<T> compareCondition;
    private GroupCondition<T> condition;
    private OnAdapterGroupingListener listener;
    private OnAdapterDataChangeListener adapterDataChangelistener;

    public static<A extends RecyclerView.Adapter& StickyCallback<T>,T> StickyGroupingStrategy<A,T> of(@NonNull A adapter){
        return new StickyGroupingStrategy<>(adapter);
    }

    public StickyGroupingStrategy(@NonNull A adapter) {
        this.adapter = adapter;
        registerAdapterDataObserver(adapter);
    }

    public void setCompareCondition(CompareGroupCondition<T> compareCondition) {
        this.compareCondition = compareCondition;
        updateAdapterGroup();
    }

    public void setCondition(GroupCondition<T> condition){
        this.condition = condition;
        updateAdapterGroup();
    }

    void updateAdapterGroup(){
        if (null == compareCondition && null == condition) {
            throw new NullPointerException("condition is null!");
        } else if (null != compareCondition) {
            updateAdapterGroup(compareCondition);
        } else if (null != condition) {
            updateAdapterGroup(condition);
        }
    }

    private void updateAdapterGroup(@NonNull CompareGroupCondition<T> compareCondition){
        T lastItem=null;
        List<Integer> list=new ArrayList<>();
        int itemCount = adapter.getItemCount();
        for(int i=0;i<itemCount;i++){
            if(null==lastItem){
                lastItem=adapter.getItem(i);
            } else {
                T item = adapter.getItem(i);
                if(compareCondition.group(lastItem,item)){
                    int position=i;
                    if(null!=listener){
                        position=listener.onAdapterGroup(i);
                    }
                    list.add(position);
                }
            }
        }
        adapterIndexArray=new int[list.size()];
        for(int i=0;i<list.size();i++){
            adapterIndexArray[i]=list.get(i);
        }
    }

    private void updateAdapterGroup(@NonNull GroupCondition<T> condition){
        List<Integer> list=new ArrayList<>();
        int itemCount = adapter.getItemCount();
        for(int i=0;i<itemCount;i++){
            T item = adapter.getItem(i);
            if(condition.group(item,i)){
                int position=i;
                if(null!=listener){
                    position=listener.onAdapterGroup(i);
                }
                list.add(position);
            }
        }
        adapterIndexArray=new int[list.size()];
        for(int i=0;i<list.size();i++){
            adapterIndexArray[i]=list.get(i);
        }
    }

    public boolean isGroupPosition(int position) {
        if(null==adapterIndexArray){
            return false;
        } else {
            return 0 <= Arrays.binarySearch(adapterIndexArray, position);
        }
    }

    public int getGroupCount(){
        int groupCount=0;
        if(null!=adapterIndexArray){
            groupCount=adapterIndexArray.length;
        }
        return groupCount;
    }

    /**
     * Return the group position by an adapter position.
     * For example:If you want to find position:8 in list:[1,5,10,15]
     * The first I will find the group:5~9. It actually in group: 1(start from zero)
     * Then I will return the start position:5
     * @param adapterPosition
     * @return
     */
    public int getGroupStartPosition(int adapterPosition) {
        int position = 0;
        int start = getGroupIndex(adapterPosition);
        if (-1 < start && start < adapterIndexArray.length) {
            position = adapterIndexArray[start];
        }
        return position;
    }

    /**
     * Return the group start position the gavin group index.
     * @param groupIndex
     * @return
     */
    public int getGroupPosition(int groupIndex) {
        int position = 0;
        if (-1 < groupIndex && groupIndex < adapterIndexArray.length) {
            position = adapterIndexArray[groupIndex];
        }
        return position;
    }

    /**
     * Return the start position. The position belong to the group.
     * [1,5,10...] If you search position:3 the number:3 belong to group:[1~4]. it will return 0.
     * @param position
     * @return
     */
    public int getGroupIndex(int position) {
        int start = 0;
        int end = adapterIndexArray.length;
        while (end - start > 1) {
            int middle = (start + end)/2;
            int middleValue = adapterIndexArray[middle];
            if (position > middleValue) {
                start = middle;
            } else if (position < middleValue) {
                end = middle;
            } else {
                start = middle;
                break;
            }
        }
        return start;
    }

    /**
     * Return the group count that behind the position.
     * For example:[1,5,7] when your position is:6 It will return 2. Because they are two groups behind you.
     * @param position
     * @return
     */
    public int getGroupCount(int position) {
        int start = 0;
        int result = RecyclerView.NO_POSITION;
        int end = adapterIndexArray.length - 1;
        while (start <= end) {
            int middle = (start + end) / 2;
            int middleValue = adapterIndexArray[middle];
            if (position == middleValue) {
                result = middle;
                break;
            } else if (position < middleValue) {
                end = middle - 1;
            } else {
                start = middle + 1;
            }
        }
        if (RecyclerView.NO_POSITION == result) {
            result = start;
        }
        return result;
    }

    private void dispatchDataChange() {
        if(null!=adapterDataChangelistener){
            adapterDataChangelistener.onDataChanged();
        }
    }

    private void registerAdapterDataObserver(RecyclerView.Adapter adapter){
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                updateAdapterGroup();
                dispatchDataChange();
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                super.onItemRangeChanged(positionStart, itemCount);
                updateAdapterGroup();
                dispatchDataChange();
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload) {
                super.onItemRangeChanged(positionStart, itemCount, payload);
                updateAdapterGroup();
                dispatchDataChange();
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                updateAdapterGroup();
                dispatchDataChange();
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                updateAdapterGroup();
                dispatchDataChange();
            }

            @Override
            public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                super.onItemRangeMoved(fromPosition, toPosition, itemCount);
                updateAdapterGroup();
                dispatchDataChange();
            }
        });
    }

    void setOnAdapterGroupingListener(@NonNull OnAdapterGroupingListener listener){
        this.listener=listener;
    }

    void setOnAdapterDataChangeListener(OnAdapterDataChangeListener listener){
        this.adapterDataChangelistener=listener;
    }

    interface OnAdapterGroupingListener{
        int onAdapterGroup(int position);
    }

    interface OnAdapterDataChangeListener{
        void onDataChanged();
    }

}
