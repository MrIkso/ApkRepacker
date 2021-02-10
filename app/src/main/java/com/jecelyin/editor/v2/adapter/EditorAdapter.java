/*
 * Copyright (C) 2016 Jecelyin Peng <jecelyin@gmail.com>
 *
 * This file is part of 920 Text Editor.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jecelyin.editor.v2.adapter;

import android.content.Context;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;
import com.jecelyin.editor.v2.common.ClusterCommand;
import com.jecelyin.editor.v2.common.TabCloseListener;
import com.jecelyin.editor.v2.common.TabInfo;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.ide.editor.EditorDelegate;
import com.mrikso.apkrepacker.ide.editor.IEditorDelegate;
import com.mrikso.apkrepacker.utils.common.DLog;
import com.mrikso.apkrepacker.view.EditorView;

import java.io.File;
import java.util.ArrayList;

/**
 * @author Jecelyin Peng <jecelyin@gmail.com>
 */
public class EditorAdapter extends PagerAdapter {
    private final Context context;
    private ArrayList<EditorDelegate> list = new ArrayList<>(20);
    private int currentPosition;

    public EditorAdapter(Context context) {
        this.context = context;
    }

    public View getView(int position, ViewGroup pager) {
        EditorView view = (EditorView) LayoutInflater.from(context).inflate(R.layout.fragment_code_editor, pager, false);
        setEditorView(position, view);
        return view;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position)
    {
        View view = getView(position, container);
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, @NonNull Object object)
    {
        DLog.d("Editor Adapter", String.format("destroy view called, %d", position));
        container.removeView((View)object);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    /**
     * @param file 一个路径或标题
     */
    public void newEditor(@Nullable File file, int offset, String encoding) {
        newEditor(true, file, offset, encoding);
    }

    public void newEditor(boolean notify, @Nullable File file, int offset, String encoding) {
        list.add(new EditorDelegate(list.size(), file, offset, encoding));
        if (notify)
            notifyDataSetChanged();
    }

    public void newEditor(String title, @Nullable CharSequence content) {
        list.add(new EditorDelegate(list.size(), title, content));
        notifyDataSetChanged();
    }

    /**
     * 当View被创建或是内存不足重建时，如果不更新list的内容，就会链接到旧的View
     *
     * @param index
     * @param editorView
     */
    public void setEditorView(int index, EditorView editorView) {
        if (index >= getCount()) {
            return;
        }
        EditorDelegate delegate = list.get(index);
        if (delegate != null)
            delegate.onCreate(editorView);
    }

    @Override
    public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        super.setPrimaryItem(container, position, object);
        currentPosition = position;
        setEditorView(position, (EditorView) object);
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    public EditorDelegate getCurrentEditorDelegate() {
        if (list == null || list.isEmpty() || currentPosition >= list.size())
            return null;
        return list.get(currentPosition);
    }

    public int countNoFileEditor() {
        int count = 0;
        for (EditorDelegate f : list) {
            if (f.getPath() == null) {
                count++;
            }
        }
        return count;
    }

    public TabInfo[] getTabInfoList() {
        int size = list.size();
        TabInfo[] arr = new TabInfo[size];
        EditorDelegate f;
        for (int i = 0; i < size; i++) {
            f = list.get(i);
            arr[i] = new TabInfo(f.getTitle(), f.getPath(), f.isChanged());
        }

        return arr;
    }

    public ArrayList<IEditorDelegate> getAllEditor() {
        ArrayList<IEditorDelegate> delegates = new ArrayList<>();
        for (int i = 0; i < getCount(); i++) {
            delegates.add(getItem(i));
        }
        return delegates;
    }

    public boolean removeEditor(final int position, final TabCloseListener listener, boolean closeUnchanged) {
        EditorDelegate delegate = list.get(position);
        if (delegate == null) {
            //not init
            return false;
        }
        final String encoding = delegate.getEncoding();
        final int offset = delegate.getCursorOffset();
        final String path = delegate.getPath();

        if(closeUnchanged){
            if(delegate.isChanged())
                return true;
        }
        remove(position);
        if (listener != null)
            listener.onClose(path, encoding, offset);
        return true;

    }

    public void remove(int position) {
        EditorDelegate delegate = list.remove(position);
        delegate.setRemoved();
        notifyDataSetChanged();
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return ((EditorView) object).isRemoved() ? POSITION_NONE : POSITION_UNCHANGED;
    }

    public ClusterCommand makeClusterCommand() {
        return new ClusterCommand(new ArrayList<>(list));
    }

    public void removeAll(TabCloseListener tabCloseListener, boolean closeUnchanged) {
        int position = list.size() - 1;
        if (position >= 0) {
            removeEditor(position, tabCloseListener, closeUnchanged);
        }
    }

    public EditorDelegate getItem(int i) {
        //TabManager调用时，可能程序已经退出，updateToolbar时就不需要做处理了
        if (i >= list.size())
            return null;
        return list.get(i);
    }

    @Override
    public Parcelable saveState() {
        SavedState ss = new SavedState();
        ss.states = new EditorDelegate.SavedState[list.size()];
        for (int i = list.size() - 1; i >= 0; i--) {
            ss.states[i] = (EditorDelegate.SavedState) list.get(i).onSaveInstanceState();
        }
        return ss;
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
        if (!(state instanceof SavedState))
            return;
        EditorDelegate.SavedState[] ss = ((SavedState) state).states;
        list.clear();
        for (EditorDelegate.SavedState s : ss) {
            list.add(new EditorDelegate(s));
        }
        notifyDataSetChanged();
    }

}
