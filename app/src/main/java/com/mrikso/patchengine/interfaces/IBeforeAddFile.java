package com.mrikso.patchengine.interfaces;

import com.mrikso.patchengine.ProjectHelper;

import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public interface IBeforeAddFile {

    boolean consumeAddedFile(ProjectHelper projectHelper, ZipFile zipFile, ZipEntry zipEntry) throws Exception;
}
