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

package com.jecelyin.common.utils;

import android.os.Debug;
import android.text.TextUtils;

import java.util.Arrays;

/**
 * @author Jecelyin Peng <jecelyin@gmail.com>
 */
public class DLog {
    private static final String TAG = "JecLog";
    public static boolean DEBUG = true;

    public static void startTracing(String name) {
        if (!DEBUG)
            return;
        if (!TextUtils.isEmpty(name))
            Debug.startMethodTracing(name);
    }

    public static int v(String tag, String msg) {
        if (!DEBUG)
            return 0;
        return android.util.Log.v(tag, msg);
    }

    public static int v(String tag, String msg, Throwable tr) {
        if (!DEBUG)
            return 0;
        return android.util.Log.v(tag, msg, tr);
    }

    /**
     * 非格式化的字符串，避免有%等字符时出错
     *
     * @param msg
     * @return
     */
    public static int d(String msg) {
        return d(TAG, msg);
    }

    public static int d(String tag, String msg) {
        if (!DEBUG)
            return 0;
        return android.util.Log.d(tag, msg);
    }

    public static int d(String tag, String msg, Throwable tr) {
        if (!DEBUG)
            return 0;
        return android.util.Log.d(tag, msg, tr);
    }

    public static int d(String format, Object... args) {
        return d(TAG, String.format(format, args));
    }

    public static int d(Throwable t) {
        return d(TAG, t.getMessage(), t);
    }

    public static int i(String tag, String msg) {
        if (!DEBUG)
            return 0;
        return android.util.Log.i(tag, msg);
    }

    public static int i(String tag, String msg, Throwable tr) {
        if (!DEBUG)
            return 0;
        return android.util.Log.i(tag, msg, tr);
    }

    public static int w(String msg) {
        return android.util.Log.w(TAG, msg);
    }

    public static int w(String tag, String msg) {
        return android.util.Log.w(tag, msg);
    }

    public static int w(String tag, String msg, Throwable tr) {
        return android.util.Log.w(tag, msg, tr);
    }

    public static int w(String tag, Throwable tr) {
        return android.util.Log.w(tag, tr);
    }

    public static int e(String tag, String msg) {
        return logError(tag, msg, null);
    }

    public static int e(String tag, String msg, Throwable tr) {
        return logError(tag, msg, tr);
    }

    /**
     * 非格式化的字符串，避免有%等字符时出错
     *
     * @param msg
     * @return
     */
    public static int e(String msg) {
        return e(TAG, msg);
    }

    public static int e(String msg, Throwable t) {
        return e(TAG, msg, t);
    }

    public static int e(String format, Object... args) {
        return e(TAG, String.format(format, args));
    }

    public static int e(Throwable t) {
        if (t == null)
            return 0;
        return logError(TAG, t.getMessage(), t);
    }

    private static int logError(String tag, String msg, Throwable t) {
        return android.util.Log.e(tag, msg, t);
    }

    public static void log(Object... params) {
        if (DLog.DEBUG) DLog.d(TAG, "log: " + Arrays.toString(params));
    }
}
