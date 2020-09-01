package com.mrikso.patchengine.rules;

import com.mrikso.apkrepacker.R;
import com.mrikso.patchengine.LinedReader;
import com.mrikso.patchengine.PatchRule;
import com.mrikso.patchengine.ProjectHelper;
import com.mrikso.patchengine.interfaces.IPatchContext;
import com.mrikso.patchengine.utils.IOUtil;
import com.mrikso.patchengine.utils.RandomHelper;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class PatchRuleAddFiles extends PatchRule {

    private static final String EXTRACT = "EXTRACT:";
    private static final String SOURCE = "SOURCE:";
    private static final String TARGET = "TARGET:";
    private static final String strEnd = "[/ADD_FILES]";
    private boolean isExtract;
    private String sourceFile;
    private String targetFile;

    public PatchRuleAddFiles() {
    }

    @Override
    public void parseFrom(LinedReader br, IPatchContext logger) throws IOException {
        startLine = br.getCurrentLine();
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
                        sourceFile = br.readLine().trim();
                        break;
                    case TARGET:
                        targetFile = br.readLine().trim();
                        break;
                    case EXTRACT:
                        isExtract = Boolean.parseBoolean(br.readLine().trim());
                        break;
                    default:
                        logger.error(R.string.patch_error_cannot_parse, br.getCurrentLine(), line2);
                        break;
                }
                line = br.readLine();
            }
        }
        String str = targetFile;
        if (str != null && str.endsWith("/")) {
            String str2 = targetFile;
            targetFile = str2.substring(0, str2.length() - 1);
        }
    }

    @Override
    public String executeRule(ProjectHelper projectHelper, ZipFile patchZip, IPatchContext logger) {
        logger.info("Start Adding new files/folder", false);
        ZipEntry entry = patchZip.getEntry(sourceFile);
        if (entry == null) {
            logger.error(R.string.patch_error_no_entry, sourceFile);
            return null;
        }
        try {
            InputStream input = patchZip.getInputStream(entry);
            if (!isExtract) {
                String path = projectHelper.getProjectPath() + File.separator + targetFile;
                logger.info("Copying files from " + patchZip.getName() + " to " + path, false);
                addFile(path, input);
            } else {
                String path = projectHelper.getAppDataPath() + RandomHelper.getRandomString(6);
                OutputStream fos2 = new FileOutputStream(path);
                IOUtil.copy(input, fos2);
                fos2.close();
                input.close();
                logger.info("Copying files from " + patchZip.getName() + " to " + path, false);
                addFilesInZip(projectHelper, path, null, logger);
            }
        } catch (Exception e) {
            logger.error(R.string.general_error, e.getMessage());
        }
        logger.info("Finish Adding new files/folder", false);
        return null;
    }

    public void addFile(String targetPath, InputStream filePath) throws IOException {
        int pos = targetPath.lastIndexOf(47);
        String fileName = targetPath.substring(pos + 1);
        String dirPath = targetPath.substring(0, pos);
        File newDir = new File(dirPath);
        if (!newDir.exists()) {
            newDir.mkdirs();
        }
        IOUtils.copy(filePath, new FileOutputStream(targetPath));
    }

    @Override
    public boolean isSmaliNeeded() {
        return super.isInSmaliFolder(targetFile);
    }

    @Override
    public boolean isValid(IPatchContext logger) {
        if (sourceFile == null) {
            logger.error(R.string.patch_error_no_source_file);
            return false;
        } else if (targetFile != null) {
            return true;
        } else {
            logger.error(R.string.patch_error_no_target_file);
            return false;
        }
    }
}
