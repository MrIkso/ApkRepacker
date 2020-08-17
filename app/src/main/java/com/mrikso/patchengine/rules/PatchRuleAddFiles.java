package com.mrikso.patchengine.rules;

import com.mrikso.apkrepacker.R;
import com.mrikso.patchengine.LinedReader;
import com.mrikso.patchengine.PatchRule;
import com.mrikso.patchengine.ProjectHelper;
import com.mrikso.patchengine.interfaces.IPatchContext;
import com.mrikso.patchengine.utils.RandomHelper;

import org.apache.commons.io.IOUtils;

import java.io.Closeable;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.apache.commons.io.IOUtils.closeQuietly;

public class PatchRuleAddFiles extends PatchRule {

    private static final String EXTRACT = "EXTRACT:";
    private static final String SOURCE = "SOURCE:";
    private static final String TARGET = "TARGET:";
    private static final String strEnd = "[/ADD_FILES]";
    private boolean bExtract;
    private String sourceFile;
    private String targetFile;

    public PatchRuleAddFiles() {
    }

    @Override
    public void parseFrom(LinedReader br, IPatchContext logger) throws IOException {
        this.startLine = br.getCurrentLine();
        String line = br.readLine();
        while (line != null) {
            String line2 = line.trim();
            if (strEnd.equals(line2)) {
                break;
            } else if (super.parseAsKeyword(line2, br)) {
                line = br.readLine();
            } else {
                switch (line2) {
                    case SOURCE:
                        this.sourceFile = br.readLine().trim();
                        break;
                    case TARGET:
                        this.targetFile = br.readLine().trim();
                        break;
                    case EXTRACT:
                        this.bExtract = Boolean.parseBoolean(br.readLine().trim());
                        break;
                    default:
                        logger.error(R.string.patch_error_cannot_parse, br.getCurrentLine(), line2);
                        break;
                }
                line = br.readLine();
            }
        }
        String str = this.targetFile;
        if (str != null && str.endsWith("/")) {
            String str2 = this.targetFile;
            this.targetFile = str2.substring(0, str2.length() - 1);
        }
    }

    @Override
    public String executeRule(ProjectHelper projectHelper, ZipFile patchZip, IPatchContext logger) {
        ZipEntry entry = patchZip.getEntry(this.sourceFile);
        if (entry == null) {
            logger.error(R.string.patch_error_no_entry, this.sourceFile);
            return null;
        }
        InputStream input = null;
        try {
            input = patchZip.getInputStream(entry);
            if (!this.bExtract) {
                //activity.getResListAdapter().addFile(activity.getDecodeRootPath() + "/" + this.targetFile, input);
            } else {
                String path = projectHelper.getAppDataPath() + RandomHelper.getRandomString(6);
                FileOutputStream fos2 = new FileOutputStream(path);
                IOUtils.copy(input, fos2);
                fos2.close();
                addFilesInZip(projectHelper, path, null, logger);
            }
        } catch (Exception e) {
            logger.error(R.string.general_error, e.getMessage());
        } catch (Throwable th) {
            closeQuietly((Closeable) null);
            closeQuietly((Closeable) null);
            throw th;
        }
        closeQuietly(input);
        return null;
    }

    @Override
    public boolean isSmaliNeeded() {
        return super.isInSmaliFolder(this.targetFile);
    }

    @Override
    public boolean isValid(IPatchContext logger) {
        if (this.sourceFile == null) {
            logger.error(R.string.patch_error_no_source_file);
            return false;
        } else if (this.targetFile != null) {
            return true;
        } else {
            logger.error(R.string.patch_error_no_target_file);
            return false;
        }
    }
}
