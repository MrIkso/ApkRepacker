package com.mrikso.apkrepacker.view.recyclerview.adapter.wrapper.sticky.group;

import androidx.annotation.NonNull;

/**
 * Created by cz on 2017/5/20.
 */
public interface GroupCondition<T> {
    boolean group(@NonNull T t, int position);
}
