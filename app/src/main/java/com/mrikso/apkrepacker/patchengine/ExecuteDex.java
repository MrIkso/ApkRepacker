package com.mrikso.apkrepacker.patchengine;

import com.jecelyin.common.utils.IOUtils;
import com.mrikso.apkrepacker.App;
import com.mrikso.apkrepacker.activity.MainActivity;
import com.mrikso.apkrepacker.utils.FileUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import dalvik.system.DexClassLoader;

public class ExecuteDex extends Core {
    private String f1014b;
    private String c;
    private String d;
    private String e;
    private boolean f = false;
    private int g = 1;
    private List h = new ArrayList();

    ExecuteDex() {
        this.h.add("[/EXECUTE_DEX]");
        this.h.add("SCRIPT:");
        this.h.add("INTERFACE_VERSION:");
        this.h.add("SMALI_NEEDED:");
        this.h.add("MAIN_CLASS:");
        this.h.add("ENTRANCE:");
        this.h.add("PARAM:");
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
        if (this.g != 1) {
          //  bVar.print((int) R.string.general_error, "Unsupported interface version: " + this.g);
        } else {
            File dir = App.getContext().getCacheDir();
            ZipEntry entry = zipFile.getEntry(this.f1014b);
            if (entry == null) {
                //bVar.print((int) R.string.general_error, "Cannot find '" + this.f1014b + "' inside the patch.");
            } else {
                try {
                    String str = dir + "/script.dex";
                    bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(str));
                    try {
                        bufferedInputStream = new BufferedInputStream(zipFile.getInputStream(entry));
                        try {
                            IOUtils.copyFile(bufferedInputStream, bufferedOutputStream);
                            a(bufferedInputStream);
                            a(bufferedOutputStream);
                            try {
                                Class loadClass = new DexClassLoader(str, dir.getAbsolutePath(), null, App.getContext().getClassLoader()).loadClass(this.c);
                                loadClass.getMethod(this.d,
                                        new Class[]{String.class, String.class, String.class, String.class})
                                        .invoke(loadClass.newInstance(), new Object[]{FileUtil.getProjectPath(), zipFile.getName(), FileUtil.getProjectPath(), this.e});
                            } catch (Throwable th) {
                                if (th instanceof InvocationTargetException) {
                                    Throwable targetException = ((InvocationTargetException) th).getTargetException();
                                    if (targetException != null) {
                                  //      bVar.print((int) R.string.general_error, a(targetException));
                                    } else {
                                  //      bVar.print((int) R.string.general_error, a(th));
                                    }
                                } else {
                                  //  bVar.print((int) R.string.general_error, a(th));
                                }
                            }
                        } catch (Exception e2) {
                            try {
                              //  bVar.print((int) R.string.general_error, "Cannot extract '" + this.f1014b + "' to SD card.");
                                a(bufferedInputStream);
                                a(bufferedOutputStream);
                                return null;
                            } catch (Throwable th2) {
                                Throwable th3 = th2;
                                bufferedOutputStream2 = bufferedOutputStream;
                                //th = th3;
                                a(bufferedInputStream);
                                a(bufferedOutputStream2);
                             //   throw th;
                            }
                        }
                    } catch (Exception e3) {
                        bufferedInputStream = null;
                       // bVar.a((int) R.string.general_error, "Cannot extract '" + this.f1014b + "' to SD card.");
                        a(bufferedInputStream);
                        a(bufferedOutputStream);
                        return null;
                    } catch (Throwable th4) {
                        Throwable th5 = th4;
                        bufferedInputStream = null;
                        bufferedOutputStream2 = bufferedOutputStream;
                        //th = th5;
                        a(bufferedInputStream);
                        a(bufferedOutputStream2);
                       // throw th;
                    }
                } catch (Exception e4) {
                    bufferedOutputStream = null;
                    bufferedInputStream = null;
                    //bVar.print((int) R.string.general_error, "Cannot extract '" + this.f1014b + "' to SD card.");
                    a(bufferedInputStream);
                    a(bufferedOutputStream);
                    return null;
                } catch (Throwable th6) {
                    //th = th6;
                    bufferedInputStream = null;
                    a(bufferedInputStream);
                    a(bufferedOutputStream2);
                    //throw th;
                }
            }
        }
        return null;
    }

    @Override
    public void start(LineReader cVar) {
        this.line = cVar.getLine();
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
                    this.f1014b = cVar.readLine().trim();
                } else if ("MAIN_CLASS:".equals(trim)) {
                    this.c = cVar.readLine().trim();
                } else if ("ENTRANCE:".equals(trim)) {
                    this.d = cVar.readLine().trim();
                } else if ("PARAM:".equals(trim)) {
                    ArrayList arrayList = new ArrayList();
                    String a2 = a(cVar, arrayList, true, this.h);
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < arrayList.size(); i++) {
                        sb.append((String) arrayList.get(i));
                        if (i != arrayList.size() - 1) {
                            sb.append(10);
                        }
                    }
                    this.e = sb.toString();
                    readLine = a2;
                } else if ("SMALI_NEEDED:".equals(trim)) {
                    this.f = Boolean.valueOf(cVar.readLine().trim()).booleanValue();
                } else if ("INTERFACE_VERSION:".equals(trim)) {
                    this.g = Integer.valueOf(cVar.readLine().trim()).intValue();
                } else {
                  //  bVar.print((int) R.string.patch_error_cannot_parse, Integer.valueOf(cVar.a()), trim);
                }
                readLine = cVar.readLine();
            }
        }
    }

    public final boolean a() {
        return this.f;
    }

    public final boolean a(PrintInterface bVar) {
        if (this.f1014b == null) {
          //  bVar.print((int) R.string.patch_error_no_script_name);
            return false;
        } else // bVar.print((int) R.string.patch_error_no_entrance_func);
            if (this.c == null) {
           // bVar.print((int) R.string.patch_error_no_main_class);
            return false;
        } else return this.d != null;
    }
}
