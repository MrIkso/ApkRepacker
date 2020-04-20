package com.mrikso.apkrepacker.autotranslator.translator;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.mrikso.apkrepacker.R;

import java.util.ArrayList;
import java.util.List;

public class TranslateStringsAdapter extends RecyclerView.Adapter<TranslateStringsAdapter.ViewHolder>{

    private List<TranslateItem> mTranslateItemListlist;
    private Context mContext;

    public TranslateStringsAdapter(Context context) {
        this.mContext = context;
        this.mTranslateItemListlist = new ArrayList<>();
    }

    public void setItems(List<TranslateItem> data) {
        this.mTranslateItemListlist.addAll(data);
        notifyDataSetChanged();
    }

    public void setItem(TranslateItem data) {
        this.mTranslateItemListlist.add(data);
        notifyDataSetChanged();
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

        void bindTo (TranslateItem item){
            mKey.setText(item.name);
            mOriginalValue.setText(item.originValue);
            mTranslatedValue.setText(item.translatedValue);
        }
    }
}
