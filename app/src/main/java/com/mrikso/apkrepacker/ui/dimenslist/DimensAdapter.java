package com.mrikso.apkrepacker.ui.dimenslist;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mrikso.apkrepacker.App;
import com.mrikso.apkrepacker.R;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class DimensAdapter extends RecyclerView.Adapter<DimensAdapter.ViewHolder> {
    public boolean changed;
    private LayoutInflater mInflater;
    private List<DimensMeta> mDimens;
    private OnItemInteractionListener mListener;

    public DimensAdapter(Context c) {
        mInflater = LayoutInflater.from(c);
        setHasStableIds(true);
    }

    public void setData(List<DimensMeta> dimens) {
        mDimens = dimens;
        notifyDataSetChanged();
    }

    public void newValue(int position, String dimens, String name) {
        DimensMeta dimensMeta = new DimensMeta.Builder(name)
                .setValue(dimens)
//                .setIcon(dimens)
                .build();
        mDimens.set(position, dimensMeta);
        notifyDataSetChanged();
    }
    /**
     * При фильтре цветов почему-то возвражается null, нужно исправить, но мне как-то уже впадло
     */
    public String getValue(String key) {
        List<DimensMeta> dimens = new ArrayList<>(mDimens);
        for (DimensMeta dimensMeta : dimens) {
            String stringId = dimensMeta.label;
            String stringText = dimensMeta.value;
            if (stringId.equals(key)) {
                return stringText;
            }
        }
        return null;
    }

    public boolean isChanged() {
        return changed;
    }

    public void setInteractionListener(OnItemInteractionListener listener) {
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(mInflater.inflate(R.layout.item_dimens, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        changed = true;
        DimensMeta packageMeta = mDimens.get(position);
        holder.bindTo(packageMeta);
    }

    @Override
    public int getItemCount() {
        return mDimens == null ? 0 : mDimens.size();
    }

    @Override
    public long getItemId(int position) {
        return mDimens.get(position).hashCode();
    }

    public int getDimens(String dimensValue) {
        if (dimensValue.startsWith("@dimen/")) {
            String substring = dimensValue.substring(7);
            Log.d("Dimen","value: " + substring);
            Log.d("Dimen", "dimen: " + getValue(substring));
            String returnedValue = getValue(substring);
            if(returnedValue != null) {
                if (returnedValue.startsWith("@android:dimen/") | returnedValue.startsWith("@dimen/")) {
                    //да зраствует великая рекурсия
                    getDimens(returnedValue);
                } else {
                    return Color.parseColor(returnedValue);
                }
            }
        } else if (dimensValue.startsWith("@android:dimen/")) {
            Object dimens = getAndroidDimen("android.R$dimen", dimensValue.substring(15));
            if (dimens != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    return App.getContext().getColor((Integer) dimens);
                }
            }
        }
        else {
            return Color.parseColor(dimensValue);
        }
        return 0;
    }

    /**
     * C помощью рефлексии досятаем цвет вида @android:color/white
     * @param clazz имя класса android.R$color
     * @param dimensName имя цвета white
     * @return результат #ffffffff
     */

    public  Object getAndroidDimen(String clazz, String dimensName){
        Field declaratedField;
        try {
            declaratedField = Class.forName(clazz).getField(dimensName);
            declaratedField.setAccessible(true);
            return declaratedField.get(null);
        } catch (NoSuchFieldException | ClassNotFoundException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public interface OnItemInteractionListener {
        void onDimensClicked(DimensMeta dimens, int id);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mDimensName;
        private TextView mDimensValue;

        private ViewHolder(@NonNull View itemView) {
            super(itemView);

            mDimensName = itemView.findViewById(R.id.tv_dimen_name);
            mDimensValue = itemView.findViewById(R.id.tv_dimen_value);

            itemView.findViewById(R.id.app_item).setOnClickListener((v) -> {
                int adapterPosition = getAdapterPosition();
                if (adapterPosition == RecyclerView.NO_POSITION)
                    return;

                if (mListener != null)
                    mListener.onDimensClicked(mDimens.get(adapterPosition), adapterPosition);
            });
        }

        @SuppressLint("DefaultLocale")
        void bindTo(DimensMeta dimensMeta) {
            mDimensName.setText(dimensMeta.label);
            mDimensValue.setText(dimensMeta.value);
        }
    }
}