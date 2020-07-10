/*
 * Copyright (c) 2013 Tah Wei Hoon.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License Version 2.0,
 * with full text available at http://www.apache.org/licenses/LICENSE-2.0.html
 *
 * This software is provided "as is". Use at your own risk.
 */
package com.mrikso.codeeditor.util;

import android.util.Log;

public class TextWarriorException extends Exception {
    private static final boolean NDEBUG = false; // set to true to suppress assertions

    public TextWarriorException(String msg) {
        super(msg);
    }

    static public void fail(final String details) {
        assertVerbose(false, details);
    }

    @SuppressWarnings("all") //suppress dead code warning when NDEBUG == true
    static public void assertVerbose(boolean condition, final String details) {
        if (NDEBUG) {
            return;
        }

        if (!condition) {
            /* For Android, a Context has to be passed into this method
             * to display the error message on the device screen */
            System.err.print("TextWarrior assertion failed: ");
         //   System.err.println(details);
            Log.i("codeeditor", details);
        }
    }
}
