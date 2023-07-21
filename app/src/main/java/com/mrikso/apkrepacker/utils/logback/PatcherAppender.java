package com.mrikso.apkrepacker.utils.logback;

import android.os.Handler;

import androidx.lifecycle.MutableLiveData;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

public class PatcherAppender extends AppenderBase<ILoggingEvent> {

    private static Handler handler;

    public PatcherAppender() {
        super();
        start();
    }

    public static void setHandler(Handler newHandler) {
        handler = newHandler;
    }

    public static void clear() {
        handler = null;
    }

    public void append(ILoggingEvent event) {
        String text;
        Level level = event.getLevel();
        if (level == Level.WARN || level == Level.ERROR) {
            text = level + ": " + event.getFormattedMessage();
        } else {
            text = event.getFormattedMessage();
        }
        if (handler != null) {
            handler.sendMessage(handler.obtainMessage(0, text + "\n"));
        }
    }
}