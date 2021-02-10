package com.mrikso.apkrepacker.view.recyclerview.adapter;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;


import com.mrikso.apkrepacker.view.recyclerview.adapter.wrapper.drag.Moveable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Created by cz
 * @date 2020-03-17 21:55
 * @email bingo110@126.com
 *
 * This is a basic adapter for {@link RecyclerView.Adapter}
 * The only thing we do was keep all the data in the adapter and help the programmer to manage the data.
 *
 * All the functions above were in the adapter So do the data list.
 * So if you want to change something. we recommend you call the functions. Do not change the data list by yourself.
 *
 * The main function list:
 * @see BaseAdapter#add(Object)
 * @see BaseAdapter#addAll(List)
 * @see BaseAdapter#remove(Object)
 * @see BaseAdapter#set(int, Object)
 * @see BaseAdapter#clear()
 * @see BaseAdapter#removeList(List)
 *
 */
public abstract class BaseAdapter<VH extends RecyclerView.ViewHolder,E> extends RecyclerView.Adapter<VH> implements Moveable {
    private final List<E> itemList=new ArrayList<>();

    public BaseAdapter(@NonNull List<E> itemList){
        if(null!=itemList){
            this.itemList.addAll(itemList);
        }
    }

    @Override
    @CallSuper
    public void onBindViewHolder(@NonNull final VH holder, int position) {
    }

    public E getItem(int position){
        return itemList.get(position);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public void removeList(@Nullable List<E> list){
        removeList(list,true);
    }

    public void removeList(@Nullable List<E> list,boolean notifyDataSetChanged){
        if(null!=list){
            this.itemList.removeAll(list);
            if(notifyDataSetChanged){
                notifyDataSetChanged();
            }
        }
    }

    public void clear(){
        clear(true);
    }

    public void clear(boolean notifyDataSetChanged){
        int itemCount = getItemCount();
        this.itemList.clear();
        if(notifyDataSetChanged){
            notifyDataSetChanged();
            notifyItemRangeRemoved(0,itemCount);
        }
    }

    public void add(@Nullable E item){
        int index = getItemCount();
        add(item,index,true);
    }

    public void add(@Nullable E item,int index){
        add(item,index,true);
    }

    public void add(@Nullable E item,int index,boolean notifyDataSetChanged) {
        if (null != item) {
            this.itemList.add(index, item);
            if(notifyDataSetChanged){
                notifyItemInserted(index);
            }
        }
    }

    public int indexOf(@Nullable E item){
        return this.itemList.indexOf(item);
    }

    public void set(int index,@Nullable E item){
        set(index,item,true);
    }

    public void set(int index,@Nullable E item,boolean notifyDataSetChanged){
        this.itemList.set(index,item);
        if(notifyDataSetChanged){
            notifyItemChanged(index);
        }
    }
    public void addAll(List<E> list){
        addAll(list,true);
    }

    public void addAll(List<E> list,boolean notifyDataSetChanged){
        int itemCount = getItemCount();
        addAll(itemCount,list,notifyDataSetChanged);
    }

    public void addAll(int index,@Nullable List<E> list){
        addAll(index,list,true);
    }

    public void addAll(int index,@Nullable List<E> list,boolean notifyDataSetChanged){
        if(null!=list){
            this.itemList.addAll(index,list);
            if(notifyDataSetChanged){
                notifyItemRangeInserted(index, list.size());
            }
        }
    }

    public void remove(int start,int count){
        remove(start,count,true);
    }

    public void remove(int start,int count,boolean notifyDataSetChanged){
        int index=0;
        while (index++ < count) {
            itemList.remove(start);
        }
        if(notifyDataSetChanged){
            notifyItemRangeRemoved(start,count);
        }
    }

    public void remove(@Nullable E item){
        remove(item,true);
    }

    public void remove(@Nullable E item,boolean notifyDataSetChanged){
        remove(indexOf(item),notifyDataSetChanged);
    }

    public void remove(int position,boolean notifyDataSetChanged){
        if(-1!=position){
            itemList.remove(position);
            if(notifyDataSetChanged){
                notifyItemRemoved(position);
            }
        }
    }

    public void swap(@Nullable List<E> list){
        clear(true);
        if(null!=list){
            addAll(list,false);
        }
    }

    public void swap(@Nullable List<E> list,boolean notifyDataSetChanged){
        clear(true);
        if(null!=list){
            addAll(list,notifyDataSetChanged);
        }
    }

    @Override
    public void move(int from,int to){
        Collections.swap(itemList, from, to);
    }

    @NonNull
    public List<E> getItemList() {
        return itemList;
    }
}
