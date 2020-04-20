package com.mrikso.apkrepacker.patchengine;

import com.jecelyin.common.utils.IOUtils;
import com.mrikso.apkrepacker.App;
import com.mrikso.apkrepacker.utils.FileUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import dalvik.system.DexClassLoader;

public class ExecuteDex extends Core {
    private String scriptName;
    private String mainClassName;
    private String entance;
    private String params;
    private boolean smaliMode = false;
    private int interfaceVersion = 1;
    private List rules = new ArrayList();

    ExecuteDex() {
        rules.add("[/EXECUTE_DEX]");
        rules.add("SCRIPT:");
        rules.add("INTERFACE_VERSION:");
        rules.add("SMALI_NEEDED:");
        rules.add("MAIN_CLASS:");
        rules.add("ENTRANCE:");
        rules.add("PARAM:");
    }

    private static String a(Throwable th) {
        StringWriter stringWriter = new StringWriter();
        th.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }

    @Override
    public String currentRule(ZipFile zipFile) {
        BufferedInputStream bufferedInputStream;
        BufferedOutputStream bufferedOutputStream;
        BufferedOutputStream bufferedOutputStream2 = null;
        if (interfaceVersion != 1) {
            //  bVar.print((int) R.string.general_error, "Unsupported interface version: " + interfaceVersion);
        } else {
            File dir = App.getContext().getCacheDir();
            ZipEntry entry = zipFile.getEntry(scriptName);
            if (entry == null) {
                //bVar.print((int) R.string.general_error, "Cannot find '" + scriptName + "' inside the patch.");
            } else {
                try {
                    String str = dir + "/script.dex";
                    bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(str));
                    bufferedInputStream = new BufferedInputStream(zipFile.getInputStream(entry));
                    IOUtils.copyFile(bufferedInputStream, bufferedOutputStream);
                    a(bufferedInputStream);
                    a(bufferedOutputStream);
                    Class loadClass = new DexClassLoader(str, dir.getAbsolutePath(), null, App.getContext().getClassLoader()).loadClass(mainClassName);
                    loadClass.getMethod(entance,
                            new Class[]{String.class, String.class, String.class, String.class})
                            .invoke(loadClass.newInstance(), new Object[]{FileUtil.getProjectPath(), zipFile.getName(), FileUtil.getProjectPath(), params});
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        return null;
    }

    @Override
    public void start(LineReader cVar) {
        line = cVar.getLine();
        String readLine = cVar.readLine();
        while (readLine != null) {
            String trim = readLine.trim();
            if ("[/EXECUTE_DEX]".equals(trim)) {
                return;
            }
            if (super.checkName(trim, cVar)) {
                readLine = cVar.readLine();
            } else {
                if ("SCRIPT:".equals(trim)) {
                    scriptName = cVar.readLine().trim();
                } else if ("MAIN_CLASS:".equals(trim)) {
                    mainClassName = cVar.readLine().trim();
                } else if ("ENTRANCE:".equals(trim)) {
                    entance = cVar.readLine().trim();
                } else if ("PARAM:".equals(trim)) {
                    ArrayList arrayList = new ArrayList();
                    String a2 = a(cVar, arrayList, true, rules);
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < arrayList.size(); i++) {
                        sb.append((String) arrayList.get(i));
                        if (i != arrayList.size() - 1) {
                            sb.append(10);
                        }
                    }
                    params = sb.toString();
                    readLine = a2;
                } else if ("SMALI_NEEDED:".equals(trim)) {
                    smaliMode = Boolean.valueOf(cVar.readLine().trim()).booleanValue();
                } else if ("INTERFACE_VERSION:".equals(trim)) {
                    interfaceVersion = Integer.valueOf(cVar.readLine().trim()).intValue();
                } else {
                    //  bVar.print((int) R.string.patch_error_cannot_parse, Integer.valueOf(cVar.a()), trim);
                }
                readLine = cVar.readLine();
            }
        }
    }

    @Override
    public boolean inSmali() {
        return smaliMode;
    }

}
