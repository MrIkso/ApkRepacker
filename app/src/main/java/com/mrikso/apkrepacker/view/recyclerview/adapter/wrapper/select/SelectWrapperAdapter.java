package com.mrikso.apkrepacker.view.recyclerview.adapter.wrapper.select;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.mrikso.apkrepacker.view.recyclerview.adapter.wrapper.header.HeaderWrapperAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Created by cz
 * @date 2020-03-17 21:48
 * @email bingo110@126.com
 *
 * This is a select wrapper adapter.
 * We support three different kinds of selector.
 * Single-choice.
 * Multi-choice
 * Range. This is a little wired. But it's okay.
 *
 * This class expend from {@link HeaderWrapperAdapter} So we don't have to worry about the header or the footer.
 *
 * Take a look at:https://proandroiddev.com/a-guide-to-recyclerview-selection-3ed9f2381504
 * recyclerview-selection:1.1.0 is an alternate for selection.
 */
public class SelectWrapperAdapter extends HeaderWrapperAdapter {
    public static final int  INVALID_POSITION = -1;
    public static final int  CLICK = 0;
    public static final int  SINGLE_SELECT = 1;
    public static final int  MULTI_SELECT = 2;
    public static final int  RECTANGLE_SELECT = 3;
    /**
     * The single-choice position.
     */
    private int selectPosition = INVALID_POSITION;
    /**
     * The start of the range.
     */
    private int start = INVALID_POSITION;
    /**
     * The end of the range.
     */
    private int end = INVALID_POSITION;
    /**
     * The select mode.
     */
    private int mode = 0;
    /**
     * The multi-choice result list.
     */
    private List<Integer> multiSelectItems = new ArrayList<>();
    /**
     * The maximum value you could operate.
     * It's only work for multi-choice.
     */
    private int selectMaxCount = Integer.MAX_VALUE;

    private OnSingleSelectListener singleSelectListener = null;
    private OnMultiSelectListener multiSelectListener = null;
    private OnRectangleSelectListener rectangleSelectListener = null;

    public SelectWrapperAdapter(RecyclerView.Adapter adapter) {
        super(adapter);
    }

    /**
     * Change the choice mode. The default mode is click.
     *
     * @see SelectWrapperAdapter#CLICK
     * @see SelectWrapperAdapter#SINGLE_SELECT
     * @see SelectWrapperAdapter#MULTI_SELECT
     * @see SelectWrapperAdapter#RECTANGLE_SELECT
     * @param newMode
     */
    public void setSelectMode(int newMode) {
        int oldMode=mode;
        switch (oldMode){
            case SINGLE_SELECT:{
                int lastSelectPosition = selectPosition;
                selectPosition = INVALID_POSITION;
                if (INVALID_POSITION != lastSelectPosition) {
                    notifyItemChanged(lastSelectPosition);
                }
                break;
            }
            case MULTI_SELECT:{
                for(int position:multiSelectItems){
                    notifyItemChanged(position);
                }
                multiSelectItems.clear();
                break;
            }
            case RECTANGLE_SELECT:{
                int s = Math.min(start, end);
                int e = Math.max(start, end);
                notifyItemRangeChanged(s, e - s + 1);
                start = 0;
                end = 0;
                break;
            }
        }
        start = INVALID_POSITION;
        end = INVALID_POSITION;
        selectPosition=INVALID_POSITION;
        mode = newMode;
    }

    @Override
    public void addHeaderView(View view) {
        super.addHeaderView(view);
        //If we add a header view. We should move all the positions.
        updateSelectPosition(1);
    }


    @Override
    public void removeHeaderView(View view) {
        super.removeHeaderView(view);
        //If we add a header view. We should move all the positions.
        updateSelectPosition(-1);
    }

    private void updateSelectPosition(int offset) {
        switch (mode){
            case SINGLE_SELECT:{
                selectPosition+=offset;
                break;
            }
            case MULTI_SELECT:{
                for(int i=0;i<multiSelectItems.size();i++){
                    Integer position = multiSelectItems.get(i);
                    multiSelectItems.set(i,position+offset);
                }
                break;
            }
            case RECTANGLE_SELECT:{
                if(start!=INVALID_POSITION){
                    start+=offset;
                }
                if(end!=INVALID_POSITION){
                    end+=offset;
                }
                break;
            }
        }
    }

    public void setSelectMaxCount(int count) {
        this.selectMaxCount = count;
    }

    public void setMultiSelectItems(List<Integer> list) {
        multiSelectItems.clear();
        if(null!=list){
            multiSelectItems.addAll(list);
        }
        for(int position:multiSelectItems){
            notifyItemChanged(position);
        }
    }

    /**
     * Select a position manually.
     * @param position
     */
    public void setSingleSelectPosition(int position) {
        int lastPosition = selectPosition;
        this.selectPosition = position;
        int headerViewCount = getHeaderViewCount();
        if(0 <=lastPosition && lastPosition < getItemCount()){
            notifyItemChanged(lastPosition+headerViewCount);
        }
        if(0 <=position && position < getItemCount()){
            notifyItemChanged(position+headerViewCount);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        int headersCount = getHeaderViewCount();
        int footerViewCount = getFooterViewCount();
        switch (mode) {
            case SINGLE_SELECT: {
                setSelectPosition(holder, position, headersCount, footerViewCount, selectPosition  == position);
                break;
            }
            case MULTI_SELECT:{
                setSelectPosition(holder, position, headersCount, footerViewCount, multiSelectItems.contains(position));
                break;
            }
            case RECTANGLE_SELECT: {
                int s = Math.min(start, end);
                int e = Math.max(start, end);
                boolean isSelected=s <= position && position <= e;
                setSelectPosition(holder, position, headersCount, footerViewCount, isSelected);
                break;
            }
            default: {
                setSelectPosition(holder, position, headersCount, footerViewCount, false);
            }
        }
    }

    protected void setSelectPosition(RecyclerView.ViewHolder holder, int position,int headerCount,int footerCount,boolean select) {
        int start=headerCount;
        int end=getItemCount() - footerCount;
        RecyclerView.Adapter adapter = getAdapter();
        if (null != adapter && adapter instanceof Selectable && start <= position && position <= end) {
            Selectable selectable = (Selectable)adapter;
            selectable.onSelectItem(holder, position-headerCount, select);
        }
    }

    @Override
    protected boolean onItemClick(View v, int position, int adapterPosition) {
        switch (mode) {
            case MULTI_SELECT:{
                int lastSize = multiSelectItems.size();
                if (multiSelectItems.contains(adapterPosition)) {
                    lastSize--;
                    multiSelectItems.remove(Integer.valueOf(adapterPosition));
                    notifyItemChanged(adapterPosition);
                } else if (multiSelectItems.size() < selectMaxCount) {
                    multiSelectItems.add(Integer.valueOf(adapterPosition));
                    notifyItemChanged(adapterPosition);
                }
                if(null!=multiSelectListener){
                    multiSelectListener.onMultiSelect(v, multiSelectItems, lastSize, selectMaxCount);
                }
                break;
            }
            case RECTANGLE_SELECT:{
                if (INVALID_POSITION != start && INVALID_POSITION != end) {
                    int s=start, e=end;
                    start = end = INVALID_POSITION;
                    notifyItemRangeChanged(Math.min(s, e), Math.abs(s-e)+1);
                } else if (INVALID_POSITION == start) {
                    start = adapterPosition;
                    notifyItemChanged(adapterPosition);
                } else if (INVALID_POSITION == end) {
                    end = adapterPosition;
                    if(null!=rectangleSelectListener){
                        rectangleSelectListener.onRectangleSelect(start, end);
                    }
                    notifyItemRangeChanged(Math.min(start, end), Math.abs(start-end)+1);
                }
                break;
            }
            case SINGLE_SELECT: {
                int last = selectPosition;
                selectPosition = adapterPosition;
                if(null!=singleSelectListener){
                    singleSelectListener.onSingleSelect(v, adapterPosition, last);
                }
                if (0 <= selectPosition && INVALID_POSITION != last) {
                    notifyItemChanged(last);//通知上一个取消
                }
                notifyItemChanged(adapterPosition);//本次选中
                break;
            }
        }
        return CLICK == mode;
    }

    /**
     * Return the single-choice position.
     * @return
     */
    public int getSelectPosition() {
        return selectPosition;
    }

    /**
     * Select a range.
     * @param start
     * @param end
     */
    public void setRectangleSelectPosition(int start,int end) {
        this.start = start;
        this.end = end;
        notifyItemRangeChanged(start, end - start);
    }


    public void setOnSingleSelectListener(OnSingleSelectListener singleSelectListener) {
        this.singleSelectListener = singleSelectListener;
    }

    public void setOnMultiSelectListener(OnMultiSelectListener multiSelectListener) {
        this.multiSelectListener = multiSelectListener;
    }

    public void setOnRectangleSelectListener(OnRectangleSelectListener rectangleSelectListener) {
        this.rectangleSelectListener = rectangleSelectListener;
    }

    public interface OnSingleSelectListener {
        void onSingleSelect(View v, int newPosition, int oldPosition);
    }

    public interface OnMultiSelectListener {
        void onMultiSelect(View v, List<Integer> selectPositions, int lastSelectCount, int maxCount);
    }

    public interface OnRectangleSelectListener {
        void onRectangleSelect(int startPosition, int endPosition);
    }
}
