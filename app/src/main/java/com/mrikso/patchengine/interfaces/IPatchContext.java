package com.mrikso.patchengine.interfaces;

import java.util.List;

public interface IPatchContext {

    List<String> getActivities();

    String getApplicationManifest();

    String getDecodeRootPath();

    List<String> getLauncherActivities();

    List<String> getPatchNames();

    List<String> getSmaliFolders();

   // String getString(int resourceId);

    String getVariableValue(String str);

    void error(int resourceId, Object... objArr);

    void info(int resourceId, boolean bold, Object... objArr);

    void info(String str, boolean bold, Object... objArr);

    void patchFinished();

    void setVariableValue(String key, String value);
}
