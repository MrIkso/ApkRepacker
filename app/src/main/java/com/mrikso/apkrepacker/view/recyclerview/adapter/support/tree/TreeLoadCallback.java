package com.mrikso.apkrepacker.view.recyclerview.adapter.support.tree;

import androidx.annotation.NonNull;

import java.util.List;

/**
 * @author Created by cz
 * @date 2020-03-17 20:54
 * @email bingo110@126.com
 * This callback if for the adapter to load tree node lazily.
 * If you have huge data to load to the tree. You could just load it lazily like traversal the file system.
 * @see TreeAdapter#setLoadCallback(TreeLoadCallback)
 */
public interface TreeLoadCallback<E> {
    /**
     * Return node list from the giving tree node.
     * @param node
     * @return
     */
    @NonNull
    List<TreeNode<E>> onLoad(TreeNode<E> node);
}
