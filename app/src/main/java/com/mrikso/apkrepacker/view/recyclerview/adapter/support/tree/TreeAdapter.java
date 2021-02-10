package com.mrikso.apkrepacker.view.recyclerview.adapter.support.tree;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.mrikso.apkrepacker.view.recyclerview.adapter.WrapperAdapter;
import com.mrikso.apkrepacker.view.recyclerview.adapter.listener.OnTreeNodeClickListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * @author Created by cz
 * @date 2020-03-17 20:19
 * @email bingo110@126.com
 * It's a tree adapter. It for the user who wants to demonstrate an infinite tree structure.
 * It could be a alternate of {@link com.mrikso.apkrepacker.view.recyclerview.adapter.support.expand.ExpandAdapter}
 *
 * This adapter could cooperate with {@link com.mrikso.apkrepacker.view.recyclerview.adapter.wrapper.header.HeaderWrapperAdapter}
 * Thus, You could be able to add extra header or footer view.
 *
 * The main function list.
 * @see #expandAll()
 * @see #collapseAll()
 * @see #add(Object)
 * @see #add(TreeNode)
 * @see #addFirst(Object)
 * @see #addFirst(TreeNode)
 * @see #setLoadCallback(TreeLoadCallback)
 * @see #setOnTreeNodeClickListener(OnTreeNodeClickListener)
 *
 *
 * If you want to lazily load data. You should use this callback.
 * Sometime when we want to load a tan of data like traversal the file system.
 * For example:
 * <pre>
 * TreeLoadCallback{
 *   void onLoad(TreeNode node){
 *       //Here if we assume this data as a file
 *       File file=node.item
 *       List<TreeNode> children=new ArrayList()
 *       for(File f:file.filelist(){
 *          children.add(xxx)
 *       }
 *       return children;
 *   }
 * }
 * </pre>
 *
 * Warning:
 *  Need to support add node to any position. Now we only allow you to add the node to start or end of the tree.
 */
public abstract class TreeAdapter<E> extends RecyclerView.Adapter {
    /**
     * All the expend node list.
     */
    private final List<TreeNode<E>> nodeList=new ArrayList<>();
    /**
     * The root node.
     */
    private final TreeNode<E> rootNode;
    private WrapperAdapter wrapperAdapter;
    /**
     * The lazily load callback.
     */
    private TreeLoadCallback<E> callback;

    private OnTreeNodeClickListener<E> listener;

    public TreeAdapter(TreeNode<E> rootNode) {
         this.rootNode=rootNode;
         this.rootNode.isExpand=true;
         //First step. We add all the data from root node.
        List<TreeNode<E>> nodeList = getNodeList(rootNode);
        if(null!=nodeList){
            this.nodeList.addAll(nodeList);
        }
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        //We might put this adapter in a wrapper adapter. {@code HeaderWrapperAdapter}
        //So if we check the adapter and keep the wrapper adapter.
        RecyclerView.Adapter adapter = recyclerView.getAdapter();
        if(adapter instanceof WrapperAdapter){
            wrapperAdapter=(WrapperAdapter)adapter;
        }
    }

    /**
     * Expand all the tree nodes.
     */
    public void expandAll(){
        expandNode(rootNode,false);
        notifyDataSetChanged();
    }

    /**
     * Expand the specific tree node
     * @param parentNode
     */
    public void expandNode(TreeNode<E> parentNode){
        expandNode(parentNode,true);
    }

    private void expandNode(TreeNode<E> parentNode,boolean notifyDataSetChanged){
        if(!parentNode.isExpand){
            //We add all the sub-node to the node list.
            parentNode.isExpand=true;
            if(!parentNode.children.isEmpty()){
                int index = nodeList.indexOf(parentNode);
                nodeList.addAll(index+1,parentNode.children);
            }
        }
        //Continue to loop
        for(TreeNode<E> node:parentNode.children){
            expandNode(node);
        }
        if(notifyDataSetChanged){
            notifyDataSetChanged();
        }
    }

    /**
     * We close all the expand node.
     */
    public void collapseAll(){
        collapseNode(rootNode,false);
        notifyDataSetChanged();
    }

    /**
     * Collapse the specific tree node
     * @param parentNode
     */
    public void collapseNode(TreeNode<E> parentNode){
        collapseNode(parentNode,true);
    }

    public void collapseNode(TreeNode<E> parentNode,boolean notifyDataSetChanged){
        if(parentNode.isExpand&&parentNode!=rootNode){
            //We add all the sub-node to the node list.
            parentNode.isExpand=false;
            nodeList.removeAll(parentNode.children);
        }
        //Continue to loop
        for(TreeNode<E> node:parentNode.children){
            collapseNode(node);
        }
        if(notifyDataSetChanged){
            notifyDataSetChanged();
        }
    }

    /**
     * Return the item list from the giving root node.
     * We will collect all the expend node and data.
     * @param rootNode
     * @return
     */
    private List<TreeNode<E>> getNodeList(@NonNull TreeNode<E> rootNode) {
        List<TreeNode<E>> nodeList=new ArrayList<>();
        Stack<TreeNode<E>> nodes = new Stack<>();
        nodes.add(rootNode);
        while (!nodes.isEmpty()) {
            TreeNode<E> node = nodes.pop();
            if (this.rootNode == node || node.isExpand && !node.children.isEmpty()) {
                List<TreeNode<E>> children = node.children;
                for(int i=children.size()-1;i>=0;i--){
                    TreeNode<E> childNode = children.get(i);
                    nodes.push(childNode);
                }
            }
            if (node != rootNode) {
                nodeList.add(node);
            }
        }
        return nodeList;
    }

    public abstract void onBindViewHolder(RecyclerView.ViewHolder holder,TreeNode<E> node,E item,int viewType,int position);

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        //We might put this adapter in a wrapper adapter. {@code HeaderWrapperAdapter}
        //So if we check the position not equal to the ViewHolder's adapter position.
        //We assume that you are wrapped by other adapter.
        TreeNode<E> node = getNode(position);
        onBindViewHolder(holder, node, node.item, getItemViewType(position), position);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //We subtract the redundant positions
                int adapterPosition = holder.getAdapterPosition();
                if(null!=wrapperAdapter){
                    adapterPosition = adapterPosition-wrapperAdapter.getExtraViewCount(adapterPosition);
                }
                TreeNode<E> node = getNode(adapterPosition);
                //We try to load the data from somewhere.
                if(null!=callback&&!node.isExpand&&!node.isLoad){
                    List<TreeNode<E>> children=callback.onLoad(node);
                    if(null!=children){
                        for(TreeNode child:children){
                            child.parent=node;
                        }
                        node.children.addAll(children);
                    }
                    node.isLoad=true;
                }
                boolean isExpand = node.isExpand;
                //We expand the node and return all the expend sub-node.
                node.isExpand = true;
                List<TreeNode<E>> childNodeList = getNodeList(node);
                //We switch the open state.
                node.isExpand = !isExpand;
                if (!childNodeList.isEmpty()) {
                    int size = childNodeList.size();
                    onNodeExpand(node,node.item, holder, !isExpand);
                    if (isExpand) {
                        nodeList.removeAll(childNodeList);
                        notifyItemRangeRemoved(adapterPosition + 1, size);
                    } else {
                        nodeList.addAll(adapterPosition + 1, childNodeList);
                        notifyItemRangeInserted(adapterPosition+1, size);
                    }
                } else if (null != listener) {
                    listener.onNodeItemClick(node,node.item, v, adapterPosition);
                }
            }
        });
    }

    /**
     * This function is for sub-class.
     * When the node expand or closed. It will called. Then you will be able to do something.
     * @param node
     * @param holder
     * @param expand
     */
    protected void onNodeExpand(TreeNode<E> node, E item,RecyclerView.ViewHolder holder,boolean expand){
    }

    public TreeNode<E> getNode(int position){
        return nodeList.get(position);
    }

    @Nullable
    public E getItem(int position){
        E item=null;
        TreeNode<E> treeNode = nodeList.get(position);
        if(null!=treeNode){
            item = treeNode.item;
        }
        return item;
    }

    @Override
    public int getItemCount() {
        return nodeList.size();
    }

    /**
     * Remove the specific position from node list.
     * @param position
     */
    public void remove(int position) {
        TreeNode<E> node = nodeList.get(position);
        if(null!=node){
            removeNode(node);
        }
    }

    /**
     * Remove the giving node from the tree.
     * @param node
     */
    public void removeNode(@NonNull TreeNode<E> node) {
        if (null != node) {
            List<TreeNode<E>> childNodes = node.children;
            if (node.isExpand && !childNodes.isEmpty()) {
                int size = childNodes.size();
                //We reverse to remove the node from the list. So I don't have to use The Iterator.
                for (int i=size-1;i>=0;i--) {
                    TreeNode<E> treeNode = childNodes.get(i);
                    //Recurse to remove all the children.
                    removeNode(treeNode);
                }
            }
            //Remove this node from the list.
            int position = nodeList.indexOf(node);
            if (0 <= position) {
                TreeNode<E> childNode = nodeList.remove(position);
                notifyItemRemoved(position);
                TreeNode<E> parent = childNode.parent;
                if (null != parent) {
                    parent.children.remove(childNode);
                }
            }
        }
    }

    public void add(E e) {
        TreeNode<E> treeNode = new TreeNode<>(rootNode, e);
        add(treeNode);
    }

    /**
     * Add the new node to the root node.
     * @param node
     */
    public void add(TreeNode<E> node) {
        node.parent = rootNode;
        rootNode.children.add(node);
        List<TreeNode<E>> nodeItems = new ArrayList<>();
        nodeItems.add(node);
        List<TreeNode<E>> nodeList = getNodeList(node);
        if (!nodeList.isEmpty()) {
            nodeItems.addAll(nodeList);
        }
        int itemCount = getItemCount();
        this.nodeList.addAll(nodeItems);
        notifyItemRangeInserted(itemCount, nodeItems.size());
    }

    public void addFirst(E e) {
        TreeNode<E> treeNode = new TreeNode<>(rootNode, e);
        addFirst(treeNode);
    }

    /**
     * Add the new node to the root node.
     * @param node
     */
    public void addFirst(TreeNode<E> node) {
        node.parent = rootNode;
        rootNode.children.add(0,node);
        List<TreeNode<E>> nodeItems = new ArrayList<>();
        nodeItems.add(node);
        List<TreeNode<E>> nodeList = getNodeList(node);
        if (!nodeList.isEmpty()) {
            nodeItems.addAll(nodeList);
        }
        this.nodeList.addAll(0,nodeItems);
        notifyItemRangeInserted(0, nodeItems.size());
    }

    /**
     * If you want to lazily load data. You should use this callback.
     * Sometime when we want to load a tan of data like traversal the file system.
     * For example:
     * <pre>
     *     TreeLoadCallback{
     *         void onLoad(TreeNode node){
     *              //Here if we assume this data as a file
     *             File file=node.item
     *             List<TreeNode> children=new ArrayList()
     *             for(File f:file.filelist(){
     *                 children.add(xxx)
     *             }
     *             return children;
     *         }
     *     }
     * </pre>
     *
     * @param callback
     */
    public void setLoadCallback(TreeLoadCallback<E> callback) {
        this.callback = callback;
    }

    public void setOnTreeNodeClickListener(OnTreeNodeClickListener<E> listener){
        this.listener=listener;
    }

}
