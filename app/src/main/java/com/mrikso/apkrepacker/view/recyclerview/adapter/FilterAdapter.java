package com.mrikso.apkrepacker.view.recyclerview.adapter;

import android.text.TextUtils;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Created by cz
 * @date 2020-03-19 21:43
 * @email bingo110@126.com
 * An simple filter adapter.
 * Here I will keep another temporary list. If user search for a specific list.
 * we will put all the query data into this list.
 *
 * @see FilterAdapter#filterObject(Object, CharSequence) When the object match the word. It will return true
 */
public abstract class FilterAdapter<VH extends RecyclerView.ViewHolder,E> extends BaseAdapter<VH,E> implements Filterable {
    private final ObjectFilter objectFilter=new ObjectFilter();
    /**
     * The temporary query list.
     */
    private final List<E> queryList=new ArrayList<>();
    /**
     * The query word.
     */
    private String queryWord;

    public FilterAdapter(@NonNull List<E> itemList) {
        super(itemList);
    }

    public String getQueryWord() {
        return queryWord;
    }

    @Override
    public Filter getFilter() {
        return objectFilter;
    }

    @Override
    public int getItemCount() {
        if(!TextUtils.isEmpty(queryWord)){
            return queryList.size();
        } else {
            return super.getItemCount();
        }
    }

    @Override
    public E getItem(int position) {
        if(!TextUtils.isEmpty(queryWord)){
            return queryList.get(position);
        } else {
            return super.getItem(position);
        }
    }

    /**
     * If this object match the search text. You could return true.
     * @param item
     * @return
     */
    protected abstract boolean filterObject(@NonNull E item,@NonNull CharSequence word);

    class ObjectFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence word) {
            FilterResults filterResults=new FilterResults();
            List<E> resultItems=new ArrayList<>();
            List<E> itemList = getItemList();
            if (TextUtils.isEmpty(word)) {
                queryWord=null;
            } else {
                queryWord=word.toString();
                for(E item:itemList){
                    if(filterObject(item,word)){
                        resultItems.add(item);
                    }
                }
            }
            filterResults.count=resultItems.size();
            filterResults.values=resultItems;
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            publishItems(constraint, (List<E>) results.values);
        }

        private void publishItems(CharSequence constraint, List<E> resultItems) {
            if(!TextUtils.isEmpty(constraint)&&null!=resultItems) {
                queryList.clear();
                queryList.addAll(resultItems);
            }
            notifyDataSetChanged();
        }

    }

}
