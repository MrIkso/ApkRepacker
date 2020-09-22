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
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.textfield.TextInputLayout;
import com.google.common.collect.Iterables;
import com.jecelyin.common.utils.UIUtils;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.autotranslator.translator.TranslateItem;
import com.mrikso.apkrepacker.utils.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StringsAdapter extends RecyclerView.Adapter<StringsAdapter.ViewHolder> /*implements Filterable*/ {
    private static OnItemClickListener onItemClickListener;
   // private List<TranslateItem> data;
    private List<TranslateItem> dataFilter;
    private Context context;

    public StringsAdapter(Context context) {
        this.context = context;
//        data = new ArrayList<>();
        dataFilter = new ArrayList<>();
    }

    public void setUpdatedItems(List<TranslateItem> map) {
        final DiffUtil.DiffResult result = DiffUtil.calculateDiff(new StringsDiffCallback(dataFilter, map), false);
        dataFilter.clear();
        dataFilter.addAll(map);
        result.dispatchUpdatesTo(this);
     //   notifyDataSetChanged();
    }

    public void setItems(List<TranslateItem> map) {
        dataFilter.clear();
        dataFilter.addAll(map);
        notifyDataSetChanged();
    }

    public void addItem(TranslateItem item){
        dataFilter.add(item);
        notifyDataSetChanged();
    }

    public void setUpdateValue(String value, int position) {
        dataFilter.get(position).translatedValue = value;
        notifyItemChanged(position);
    }

    public void remove(int position) {
        dataFilter.remove(position);
        notifyItemRemoved(position);
    }

    public List<TranslateItem> getData(){
        return dataFilter;
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
        TranslateItem translateItem = dataFilter.get(position);
        holder.bindTo(translateItem, position);
    }

    public void setInteractionListener(OnItemClickListener listener) {
        onItemClickListener = listener;
    }

   /* @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                FilterResults filterResults = new FilterResults();
                if (constraint == null || constraint.length() == 0) {
                    filterResults.count = data.size();
                    filterResults.values = data;

                } else {
                    List<TranslateItem> resultsModel = new ArrayList<>();
                    String searchStr = constraint.toString().toLowerCase();

                    for (TranslateItem item : data) {
                        if (item.name.toLowerCase().contains(searchStr) || item.originValue.toLowerCase().contains(searchStr)) {
                            resultsModel.add(new TranslateItem(item.name, item.originValue, item.translatedValue));
                        }
                        filterResults.count = resultsModel.size();
                        filterResults.values = resultsModel;
                    }

                }

                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {

                dataFilter = (List<TranslateItem>) results.values;
                notifyDataSetChanged();

            }
        };
    }*/

    public interface OnItemClickListener {
        void onTranslateClicked(TranslateItem item, int position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TranslateItem mItem;
        private TextView mStringName;
        private TextView mStringValue;
        private int mPosition;

        private ViewHolder(@NonNull View itemView) {
            super(itemView);

            mStringName = itemView.findViewById(R.id.string_name);
            mStringValue = itemView.findViewById(R.id.string_value);

            itemView.setOnClickListener((view) -> {
                if (onItemClickListener != null)
                    onItemClickListener.onTranslateClicked(mItem, mPosition);
            });
            itemView.setOnLongClickListener(v -> {
                StringUtils.setClipboard(context, mItem.name, false);
                UIUtils.toast(context, context.getString(R.string.string_name_copied, mItem.name));
                return true;
            });

        }

        void bindTo(TranslateItem item, int position) {
            mItem = item;
            mPosition = position;
            mStringName.setText(item.originValue);
            String translated = item.translatedValue;
            if(translated != null)
                //если строка будет переведена, то цвет фона будет изменен и перевён текст будет отображаться
                mStringValue.setBackgroundColor(0x20888888);
                mStringValue.setText(translated);
        }
    }
}
