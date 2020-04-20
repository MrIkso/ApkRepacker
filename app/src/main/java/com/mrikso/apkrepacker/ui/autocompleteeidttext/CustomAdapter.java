package com.mrikso.apkrepacker.ui.autocompleteeidttext;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.mrikso.apkrepacker.R;

import java.util.ArrayList;
import java.util.List;

public class CustomAdapter extends BaseAdapter implements Filterable {
/*
    Кастомный адаптер для списка ранеее введеных поисковых запросов
 */
    private List<String> dataList;
    private Context mContext;
    private List<String> dataListAllItems;
    private boolean isDeleted;

    public CustomAdapter(Context context, List<String> storeDataLst) {
        dataList = storeDataLst;
        mContext = context;
    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public String getItem(int position) {
        Log.d("CustomListAdapter", dataList.get(position));
        return dataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View view, @NonNull ViewGroup parent) {
        if (view == null) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.autocomplete_text_view, parent, false);
        }
        TextView strName = view.findViewById(R.id.item_autocomplete);
        TextView removeItem = view.findViewById(R.id.delete_item_autocomplete);
        removeItem.setOnClickListener(v -> {
            dataList.remove(position);
            isDeleted = true;
            notifyDataSetChanged();
        });
        strName.setText(getItem(position));
        return view;
    }

    public List<String> getDataList() {
        return dataList;
    }
    public void addValue(String value){
        dataList.add(value);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Filter getFilter() {
        Object lock = new Object();
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence prefix) {
                FilterResults results = new FilterResults();
                if (dataListAllItems == null) {
                    synchronized (lock) {
                        dataListAllItems = new ArrayList<>(dataList);
                    }
                }
                if (isDeleted) {
                    dataListAllItems = new ArrayList<>(dataList);
                    isDeleted = false;
                }

                if (prefix == null || prefix.length() == 0) {
                    synchronized (lock) {
                        results.values = dataListAllItems;
                        results.count = dataListAllItems.size();
                    }
                } else {
                    final String searchStrLowerCase = prefix.toString().toLowerCase();
                    ArrayList<String> matchValues = new ArrayList<>();
                    for (String dataItem : dataListAllItems) {
                        if (dataItem.toLowerCase().startsWith(searchStrLowerCase)) {
                            matchValues.add(dataItem);
                        }
                    }
                    results.values = matchValues;
                    results.count = matchValues.size();
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results.values != null) {
                    dataList = (ArrayList<String>) results.values;
                } else {
                    dataList = null;
                }
                if (results.count > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }

        };
        return filter;

    }
}

