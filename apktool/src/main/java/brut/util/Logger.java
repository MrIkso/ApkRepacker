package brut.util;

import java.util.logging.Level;

public interface Logger {

    void text(int id, Object... args);

    public void error(int text, Object... args);

    public void log(Level warring, String format, Throwable ex);

    public void fine(int id, Object... args);

    public void warning(int id, Object... args);

    public void info(int id, Object... args);
}
