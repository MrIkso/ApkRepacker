package com.mrikso.apkrepacker.autotranslator.translator;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.mrikso.apkrepacker.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TranslateStringsAdapter extends RecyclerView.Adapter<TranslateStringsAdapter.ViewHolder> {

    private List<TranslateItem> mTranslateItemListlist;
    private Map<String, String> map;
    private Context mContext;

    public TranslateStringsAdapter(Context context) {
        this.mContext = context;
        this.mTranslateItemListlist = new ArrayList<>();
        this.map = new HashMap<>();
    }

    public void setItems(List<TranslateItem> data) {
        this.mTranslateItemListlist.addAll(data);
        notifyDataSetChanged();
    }

    public void setItem(TranslateItem data) {
        this.mTranslateItemListlist.add(data);
        notifyDataSetChanged();
    }

    public Map<String, String> getTranslatedMap() {
        return map;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_stringvalue_translate, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TranslateItem mTranslateItem = mTranslateItemListlist.get(position);
        holder.bindTo(mTranslateItem);
        holder.mTranslatedValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
             //   if (map.containsKey(mTranslateItem.name))
                    map.put(holder.mKey.getText().toString(), s.toString());
                //else
                //    map.remove(mTranslateItem.name);
              //  map.put(mTranslateItem.name, s.toString());

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return mTranslateItemListlist.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private AppCompatTextView mKey, mOriginalValue;
        private AppCompatEditText mTranslatedValue;

        private ViewHolder(View itemView) {
            super(itemView);
            mKey = itemView.findViewById(R.id.string_name);
            mOriginalValue = itemView.findViewById(R.id.origin_value);
            mTranslatedValue = itemView.findViewById(R.id.translated_value);
        }

        void bindTo(TranslateItem item) {
            mKey.setText(item.name);
            mOriginalValue.setText(item.originValue);
            mTranslatedValue.setText(item.translatedValue);
        }
    }
}
