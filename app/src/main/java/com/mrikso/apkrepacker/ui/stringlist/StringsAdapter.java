package com.mrikso.apkrepacker.ui.stringlist;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.textfield.TextInputLayout;
import com.google.common.collect.Iterables;
import com.jecelyin.common.utils.UIUtils;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.utils.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class StringsAdapter extends RecyclerView.Adapter<StringsAdapter.ViewHolder> implements Filterable {
    private static OnItemClickListener onItemClickListener;
    private Map<String, String> data;
    private Map<String, String> dataFilter;
    private Context context;

    public StringsAdapter(Context context) {
        this.context = context;
        data = new HashMap();
        dataFilter = new HashMap();
    }

    public void setItems(Map<String, String> map) {
        data = map;
        dataFilter = map;
        notifyDataSetChanged();
    }

    public Object getItem(int position) {
        return Iterables.get(dataFilter.entrySet(), position);
    }

    public void setUpdateValue(String key, String value) {
        if (dataFilter.containsKey(key) && data.containsKey(key)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                dataFilter.replace(key, value);
                data.replace(key, value);
            } else {
                dataFilter.remove(key);
                dataFilter.put(key, value);
                data.remove(key);
                data.put(key, value);
            }
        } else {
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
    public int getItemCount() {
        return dataFilter.size();
    }

    @NonNull
    @Override
    public StringsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_string_value, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StringsAdapter.ViewHolder holder, int position) {
        Map.Entry<String, String> item = Iterables.get(dataFilter.entrySet(), position);
        holder.bindTo(item);
    }

    public void setInteractionListener(OnItemClickListener listener) {
        onItemClickListener = listener;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
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
                        if (item.getKey().toLowerCase().contains(searchStr) || item.getValue().toLowerCase().contains(searchStr)) {
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
    }

    public interface OnItemClickListener {
        void onTranslateClicked(String key, String value);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private Map.Entry<String, String> item;
        private TextView mStringName;
        private TextView mStringValue;

        private ViewHolder(@NonNull View itemView) {
            super(itemView);

            mStringName = itemView.findViewById(R.id.string_name);
            mStringValue = itemView.findViewById(R.id.string_value);

            itemView.setOnClickListener((view) -> {
                MaterialDialog.Builder builder = new MaterialDialog.Builder(context);
                builder.title(R.string.translate_string);
                View viewinf = LayoutInflater.from(context).inflate(R.layout.dialog_edit_string_value, null, false);
                EditText oldValue = viewinf.findViewById(R.id.old_value);
                oldValue.setTextIsSelectable(true);
                EditText newValue = viewinf.findViewById(R.id.new_value);
                TextInputLayout textInputLayout = viewinf.findViewById(R.id.text_input_layout_old);
                textInputLayout.setHint(item.getKey());
                oldValue.setText(item.getValue());
                builder.positiveText(android.R.string.ok);
                builder.negativeText(android.R.string.cancel);
                builder.onPositive((dialog, which) -> {
                    if (onItemClickListener != null)
                        onItemClickListener.onTranslateClicked(item.getKey(), newValue.getText().toString());
                });

                builder.customView(viewinf, false);
                // AlertDialog dialog =builder.create();
                MaterialDialog dlg = builder.show();
                dlg.setCanceledOnTouchOutside(true);
                dlg.setCancelable(true);
            });
            itemView.setOnLongClickListener(v -> {
                StringUtils.setClipboard(context, item.getKey());
                UIUtils.toast(context, context.getString(R.string.string_name_copied, item.getKey()));
                return true;
            });

        }

        void bindTo(Map.Entry<String, String> item) {
            this.item = item;
            mStringName.setText(item.getKey());
            mStringValue.setText(item.getValue());
        }
    }
}
