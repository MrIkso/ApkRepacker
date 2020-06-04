package com.mrikso.apkrepacker.utils;

import android.app.Activity;
import android.content.Context;
import android.view.ContextThemeWrapper;

import com.mrikso.apkrepacker.App;
import com.mrikso.apkrepacker.R;
/**
 * @author @SVolf
 */
public abstract class ThemeWrapper {
    /**
     * Apply theme to an Activity
     */
    public static void applyTheme(Activity ctx) {
        int theme;
        switch (Theme.values()[getThemeIndex()]) {
            case LIGHT:
                theme = R.style.AppTheme;
                break;
            case DARK:
                theme = R.style.AppTheme_Dark;
                break;
            default:
                // Force use the light theme
                theme = R.style.AppTheme;
                break;
        }
        ctx.setTheme(theme);
    }

    /**
     * Get a saved theme number
     */

    private static int getThemeIndex() {
        return Integer.parseInt(App.get().getPreferences().getString("ui_theme", String.valueOf(Theme.LIGHT.ordinal())));
    }
    /**
     * Check is Light Theme
     */
    public static boolean isLightTheme() {
        return getThemeIndex() == Theme.LIGHT.ordinal();
    }

    /**
     * Provided themes
     */
    public enum Theme {
        LIGHT,
        DARK
    }
}
