package com.mrikso.apkrepacker.view.recyclerview.adapter.listener;

import android.view.View;

import androidx.annotation.Nullable;

import com.mrikso.apkrepacker.view.recyclerview.adapter.support.tree.TreeAdapter;
import com.mrikso.apkrepacker.view.recyclerview.adapter.support.tree.TreeNode;


/**
 * @author Created by cz
 * @date 2020-03-17 22:04
 * @email bingo110@126.com
 *
 * The tree node click listener. It's responsible for the tree node click event.
 * So you could receive the event. when you try to do something.
 * But actually it's not for the parent node. Cause we help you close or open the sub-nodes.
 *
 * If you want to receive the click event from parent node. Take a look at {@link TreeAdapter# onNodeExpand(com.mrikso.apkrepacker.view.recyclerview.adapter.support.tree.TreeNode, java.lang.Object, androidx.recyclerview.widget.RecyclerView.ViewHolder, boolean)}
 *
 * @see TreeAdapter#setOnTreeNodeClickListener(OnTreeNodeClickListener)
 */
public interface OnTreeNodeClickListener<E> {
    void onNodeItemClick(@Nullable TreeNode<E> node, @Nullable E item, @Nullable View v, int position);
}
