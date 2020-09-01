package com.mrikso.apkrepacker.utils.common;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

import com.mrikso.apkrepacker.utils.AppUtils;

import java.util.Objects;

public class CommonUtil {

    /**
     * one shot vibrate
     * @param context your Context
     */
    public static void vibrate(Context context) {
        if (AppUtils.apiIsAtLeast(Build.VERSION_CODES.O)) {
            ((Vibrator) Objects.requireNonNull(context.getSystemService(Context.VIBRATOR_SERVICE))).vibrate(VibrationEffect.createOneShot(25, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            ((Vibrator) Objects.requireNonNull(context.getSystemService(Context.VIBRATOR_SERVICE))).vibrate(25);
        }
    }
}
