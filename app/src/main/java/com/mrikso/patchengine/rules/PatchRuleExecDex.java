package com.mrikso.patchengine.rules;

import com.mrikso.apkrepacker.R;
import com.mrikso.patchengine.LinedReader;
import com.mrikso.patchengine.PatchRule;
import com.mrikso.patchengine.ProjectHelper;
import com.mrikso.patchengine.interfaces.IPatchContext;

import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import dalvik.system.DexClassLoader;

import static org.apache.commons.io.IOUtils.closeQuietly;


public class PatchRuleExecDex extends PatchRule {

    private static final String ENTRANCE = "ENTRANCE:";
    private static final String INTERFACE_VERSION = "INTERFACE_VERSION:";
    private static final String MAIN_CLASS = "MAIN_CLASS:";
    private static final String PARAM = "PARAM:";
    private static final String SCRIPT = "SCRIPT:";
    private static final String SMALI_NEEDED = "SMALI_NEEDED:";
    private static final String strEnd = "[/EXECUTE_DEX]";
    private String entranceFunc;
    private int ifVersion = 1;
    private List<String> keywords = new ArrayList<>();
    private String mainClass;
    private String param;
    private String scriptName;
    private boolean smaliNeeded = false;

    public PatchRuleExecDex() {
        this.keywords.add(SCRIPT);
        this.keywords.add(INTERFACE_VERSION);
        this.keywords.add(SMALI_NEEDED);
        this.keywords.add(MAIN_CLASS);
        this.keywords.add(ENTRANCE);
        this.keywords.add(PARAM);
        this.keywords.add(strEnd);
    }

    @Override
    public void parseFrom(LinedReader br, IPatchContext logger) throws IOException {
        this.startLine = br.getCurrentLine();
        String line = br.readLine();
        while (line != null) {
            line = line.trim();
            if (!strEnd.equals(line)) {
                if (!super.parseAsKeyword(line, br)) {
                    switch (line) {
                        case SCRIPT:
                            this.scriptName = br.readLine().trim();
                            break;
                        case MAIN_CLASS:
                            this.mainClass = br.readLine().trim();
                            break;
                        case ENTRANCE:
                            this.entranceFunc = br.readLine().trim();
                            break;
                        case SMALI_NEEDED:
                            this.smaliNeeded = Boolean.parseBoolean(br.readLine().trim());
                            break;
                        case INTERFACE_VERSION:
                            this.ifVersion = Integer.parseInt(br.readLine().trim());
                            break;
                        default:
                            if (PARAM.equals(line)) {
                                List<String> lines = new ArrayList<>();
                                line = readMultiLines(br, lines, true, this.keywords);
                                StringBuilder sb = new StringBuilder();
                                for (int i = 0; i < lines.size(); i++) {
                                    sb.append(lines.get(i));
                                    if (i != lines.size() - 1) {
                                        sb.append(10);
                                    }
                                }
                                this.param = sb.toString();
                                continue;
                            }
                            logger.error(R.string.patch_error_cannot_parse, br.getCurrentLine(), line);
                            break;
                    }
                }
                line = br.readLine();
            } else {
                return;
            }
        }
    }

    @Override
    public String executeRule(ProjectHelper activity, ZipFile patchZip, IPatchContext logger) {
        if (this.ifVersion != 1) {
            logger.error(R.string.general_error, "Unsupported interface version: " + this.ifVersion);
            return null;
        }
        File optimizedDexOutputPath = activity.getCacheDir();

        ZipEntry ze = patchZip.getEntry(this.scriptName);
        if (ze == null) {
            logger.error(R.string.general_error, "Cannot find '" + this.scriptName + "' inside the patch.");
            return null;
        }
        InputStream is = null;
        OutputStream os = null;
        try {
            String dexPath = optimizedDexOutputPath + "/script.dex";
            os = new BufferedOutputStream(new FileOutputStream(dexPath));
            is = new BufferedInputStream(patchZip.getInputStream(ze));
            IOUtils.copy(is, os);
            closeQuietly(is);
            closeQuietly(os);
            try {
                logger.info("Executing dex..", true);
                Class<?> entryClass = new DexClassLoader(dexPath, optimizedDexOutputPath.getAbsolutePath(), null, activity.getContext().getClassLoader()).loadClass(this.mainClass);
                entryClass.getMethod(this.entranceFunc, String.class, String.class, String.class, String.class).invoke(entryClass.newInstance(), activity.getApkPath(), patchZip.getName(), activity.getProjectPath(), this.param);
                return null;
            } catch (Throwable th) {
                if (th instanceof InvocationTargetException) {
                    Throwable t = ((InvocationTargetException) th).getTargetException();
                    if (t != null) {
                        logger.error(R.string.general_error, getStackTrace(t));
                        return null;
                    }
                    logger.error(R.string.general_error, getStackTrace(th));
                    return null;
                }
                logger.error(R.string.general_error, getStackTrace(th));
                return null;
            }
        } catch (Exception e) {
            logger.error(R.string.general_error, "Cannot extract '" + this.scriptName + "' to SD card.");
            closeQuietly(is);
            closeQuietly(os);
            return null;
        } catch (Throwable th2) {
            closeQuietly(is);
            closeQuietly(os);
            throw th2;
        }
    }

    private String getStackTrace(Throwable e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    @Override
    public boolean isValid(IPatchContext logger) {
        if (this.scriptName == null) {
            logger.error(R.string.patch_error_no_script_name);
            return false;
        } else if (this.mainClass == null) {
            logger.error(R.string.patch_error_no_main_class);
            return false;
        } else if (this.entranceFunc != null) {
            return true;
        } else {
            logger.error(R.string.patch_error_no_entrance_func);
            return false;
        }
    }

    @Override
    public boolean isSmaliNeeded() {
        return this.smaliNeeded;
    }
}
