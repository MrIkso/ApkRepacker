package com.mrikso.apkrepacker.view.recyclerview.adapter.wrapper.drag;

/**
 * @author Created by cz
 * @date 2020-03-17 19:56
 * @email bingo110@126.com
 *
 * If you use {@link DragWrapperAdapter}. Since we wrap your adapter.
 * When we drag one position to a new position. We will also move exchange the data.
 * So here is the interface for the wrapped adapter.
 */
public interface Moveable {
    void move(int from, int to);
}
