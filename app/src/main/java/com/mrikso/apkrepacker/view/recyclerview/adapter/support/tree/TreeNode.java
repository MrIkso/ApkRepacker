package com.mrikso.apkrepacker.view.recyclerview.adapter.support.tree;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Created by cz
 * @date 2020-03-17 20:20
 * @email bingo110@126.com
 *
 * @see TreeAdapter
 */
public class TreeNode<E> {
    /**
     * All the sub-nodes.
     */
    public List<TreeNode<E>> children=new ArrayList<>(1);
    /**
     * The parent node.
     */
    public TreeNode<E> parent;
    /**
     * The data.
     */
    public E item;
    /**
     * The depth of tree.
     */
    public int depth = 0;
    /**
     * Check if the node is expanded or close.
     */
    public boolean isExpand;
    /**
     * If we want to load data lazily. we use this check if it's already loaded.
     */
    public boolean isLoad;

    public TreeNode(@NonNull E item) {
        this(null,item);
    }

    public TreeNode(TreeNode<E> parent,E item){
        this.parent=parent;
        if(null!=parent){
            this.depth=parent.depth+1;
        }
        this.item=item;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TreeNode<?> treeNode = (TreeNode<?>) o;
        return depth == treeNode.depth &&
                Objects.equals(parent, treeNode.parent) &&
                Objects.equals(item, treeNode.item);
    }

    @Override
    public int hashCode() {
        return Objects.hash(children, parent, item, depth, isExpand, isLoad);
    }
}
