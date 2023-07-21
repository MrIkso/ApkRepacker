package com.mrikso.apkrepacker.utils.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import org.slf4j.LoggerFactory;

import java.io.File;

public class LogbackPatcherConfig {

    private static final String FILE_APPENDER_NAME = "PATCHER_FILE_DEBUG";

    public static void shutdown() {
        getProjectLogger().getLoggerContext().stop();
    }

    public static void setLoggingLevel(String level) {
        if (level != null) {
            getProjectLogger().setLevel(Level.toLevel(level));
        }
    }

    public static void setLoggingFile(File file) {
        if (file == null) {
            return;
        }
        FileAppender<ILoggingEvent> appender =
                (FileAppender<ILoggingEvent>) getProjectLogger().getAppender(FILE_APPENDER_NAME);
        if (appender != null) {
            appender.stop();
            appender.setFile(file.getPath());
            appender.start();
        } else {
            printError();
        }
    }

    private static void printError() {
        String msg = "File appender is not defined";
        getProjectLogger().error(msg);
        System.err.println(msg);
    }

    private static Logger getProjectLogger() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        return loggerContext.getLogger("com.github.cregrant.smaliscissors");
    }

}
