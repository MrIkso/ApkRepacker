package com.mrikso.apkrepacker.recycler;

/**
 * Created by zhang on 2016.08.21.
 */
public interface OnMoveAndSwipedListener {

    boolean onItemMove(int fromPosition, int toPosition);

    void onItemDismiss(int position);

}
