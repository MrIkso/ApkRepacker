package com.mrikso.patchengine.rules;

import com.mrikso.apkrepacker.R;
import com.mrikso.patchengine.LinedReader;
import com.mrikso.patchengine.PatchRule;
import com.mrikso.patchengine.ProjectHelper;
import com.mrikso.patchengine.interfaces.IPatchContext;
import com.mrikso.patchengine.utils.HexUtil;
import com.mrikso.patchengine.utils.IOUtil;

import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.apache.commons.io.IOUtils.closeQuietly;


public class PatchRuleReviseSig extends PatchRule {

    private static final String TARGET = "TARGET:";
    private static final String strEnd = "[/SIGNATURE_REVISE]";
    private List<String> targetList = new ArrayList();

    public PatchRuleReviseSig() {
    }

    @Override
    public void parseFrom(LinedReader br, IPatchContext logger) throws IOException {
        this.startLine = br.getCurrentLine();
        String line = br.readLine();
        while (line != null) {
            String line2 = line.trim();
            if (!strEnd.equals(line2)) {
                if (!super.parseAsKeyword(line2, br)) {
                    if (TARGET.equals(line2)) {
                        this.targetList.add(br.readLine().trim());
                    } else {
                        logger.error(R.string.patch_error_cannot_parse, br.getCurrentLine(), line2);
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
        String hexRSA = getHexRSA(activity.getApkPath());
        String packageName = activity.getApkPackage();
        String targetFile = logger.getDecodeRootPath() + "/" + this.targetList.get(0);
        try {
            IOUtil.writeToFile(targetFile, readFileContent(targetFile).replace("%PACKAGE_NAME%", packageName).replace("%RSA_DATA%", hexRSA));
            return null;
        } catch (Exception e) {
            logger.error(R.string.patch_error_write_to, targetFile);
            return null;
        }
    }

    private String getHexRSA(String apkPath) {
        ZipEntry ze = null;
        ZipFile zfile = null;
        BufferedInputStream input = null;
        ByteArrayOutputStream output = null;
        try {
            zfile = new ZipFile(apkPath);
            Enumeration<? extends ZipEntry> entries = zfile.entries();
            while (entries.hasMoreElements()) {
                ze = entries.nextElement();
                if (!ze.isDirectory()) {
                    String entryName = ze.getName();
                    if (entryName.endsWith(".RSA") || entryName.endsWith(".rsa") || entryName.endsWith(".DSA") || entryName.endsWith(".dsa")) {
                        input = new BufferedInputStream(zfile.getInputStream(ze));
                        output = new ByteArrayOutputStream();
                        IOUtils.copy(input, output);
                    }
                }
            }
            input = new BufferedInputStream(zfile.getInputStream(ze));
            output = new ByteArrayOutputStream();
            IOUtils.copy(input, output);
        } catch (IOException e) {
            e.printStackTrace();
        }
        closeQuietly(input);
        closeQuietly(output);
        closeQuietly(zfile);
        if (output != null) {
            return HexUtil.bytesToHexString(output.toByteArray());
        }
        return null;
    }

    @Override
    public boolean isValid(IPatchContext logger) {
        return !this.targetList.isEmpty();
    }

    @Override
    public boolean isSmaliNeeded() {
        return true;
    }
}
