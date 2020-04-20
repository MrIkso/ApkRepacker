package com.mrikso.apkrepacker.ui.stringlist;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.google.common.collect.Iterables;
import com.jecelyin.common.utils.UIUtils;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.utils.StringUtils;

import java.util.HashMap;
import java.util.Map;

//import androidx.appcompat.app.AlertDialog;

public class StringsAdapter extends BaseAdapter implements Filterable {
    private Map<String, String> data;
    private Map<String, String> dataFilter;
    private Context context;
    private OnItemClickListener onItemClickListener;

    public StringsAdapter(Map<String, String> map) {
        data = new HashMap();
        dataFilter = new HashMap();
        data = map;
        dataFilter = map;
    }

    @Override
    public int getCount() {
        return dataFilter.size();
    }

    @Override
    public Object getItem(int position) {
        return Iterables.get(dataFilter.entrySet(), position);
    }

    /*
    @Override
    public Map.Entry<String, String> getItem(int position) {
        return (Map.Entry) data.get(position);
    }

     */

    public void setUpdateValue(String key, String value){
        if(dataFilter.containsKey(key) && data.containsKey(key)){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                dataFilter.replace(key, value);
                data.replace(key, value);
            }
            else{
                dataFilter.remove(key);
                dataFilter.put(key, value);
                data.remove(key);
                data.put(key, value);
            }
        }
        else {
            dataFilter.put(key, value);
            data.put(key, value);
        }
        notifyDataSetChanged();
    }
    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final View result;
        context = parent.getContext();
        if (convertView == null) {
            result = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_string_value, parent, false);
        } else {
            result = convertView;
        }
        Map.Entry<String, String> item = Iterables.get(dataFilter.entrySet(), position);
        ((TextView) result.findViewById(R.id.string_name)).setText(item.getKey());
        ((TextView) result.findViewById(R.id.string_value)).setText(item.getValue());
        result.setOnClickListener((view) -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.translate_string);
            View viewinf = LayoutInflater.from(context).inflate(R.layout.dialog_edit_string_value,
                    parent, false);
            EditText oldValue = viewinf.findViewById(R.id.old_value);
            oldValue.setTextIsSelectable(true);
            EditText newValue = viewinf.findViewById(R.id.new_value);
            TextView key = viewinf.findViewById(R.id.key);
            key.setText(item.getKey());
            key.setOnClickListener((v) -> {
                StringUtils.setClipboard(context, key.getText().toString());
            });
            oldValue.setText(item.getValue());
            builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                dialog.dismiss();
            });
            builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
                if (onItemClickListener != null)
                    onItemClickListener.onTranslateClicked(item.getKey(), newValue.getText().toString());
                //trastaleValue(item.getKey(), newValue.getText().toString());
                // Toast.makeText(context, "click", Toast.LENGTH_LONG).show();
            });
            builder.setView(viewinf);
            // AlertDialog dialog =builder.create();
            builder.show();
        });
        result.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                StringUtils.setClipboard(context, item.getKey());
                UIUtils.toast(context, context.getString(R.string.string_name_copied, item.getKey()));
                return true;
            }
        });
        return result;
    }
    public void setInteractionListener(OnItemClickListener listener) {
        onItemClickListener = listener;
    }

    public interface OnItemClickListener {
        void onTranslateClicked(String key, String value);
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                FilterResults filterResults = new FilterResults();
                if (constraint == null || constraint.length() == 0) {
                    filterResults.count = data.size();
                    filterResults.values = data;

                } else {
                    Map<String, String> resultsModel = new HashMap<>();
                    String searchStr = constraint.toString().toLowerCase();

                    for (Map.Entry<String, String> item : data.entrySet()) {
                        if (item.getKey().contains(searchStr) || item.getValue().contains(searchStr)) {
                            resultsModel.put(item.getKey(), item.getValue());

                        }
                        filterResults.count = resultsModel.size();
                        filterResults.values = resultsModel;
                    }

                }

                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {

                dataFilter = (Map<String, String>) results.values;
                notifyDataSetChanged();

            }
        };
        return filter;
    }

}
