package com.mrikso.apkrepacker.view.recyclerview.adapter.wrapper.sticky.group;

import androidx.annotation.NonNull;

/**
 * Created by cz on 2017/5/20.
 */
public interface CompareGroupCondition<T> {
    boolean group(@NonNull T t1, @NonNull T t2);
}
