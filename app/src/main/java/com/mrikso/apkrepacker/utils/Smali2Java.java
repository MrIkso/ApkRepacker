package com.mrikso.apkrepacker.utils;

import org.jf.dexlib2.writer.builder.DexBuilder;
import org.jf.dexlib2.writer.io.FileDataStore;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jadx.api.JadxArgs;
import jadx.api.JadxDecompiler;
import jadx.api.JavaClass;

public class Smali2Java {

    public static String translate(DexBuilder dexBuilder) throws IOException {
        File tmp = File.createTempFile("temp", ".dex");
        //File tmp1 = File.createTempFile("temp", ".arsc");
        try {
            dexBuilder.writeTo(new FileDataStore(tmp));
            // ZipFile zipFile = new ZipFile(ProjectItem.getApkPatch());
            //  ZipEntry entry = ZipUtils.getEntry(zipFile, "resources.arsc");
            //  InputStream in = zipFile.getInputStream(entry);
            // FileOutputStream fileOutputStream = new  FileOutputStream(tmp1);
            //  IOUtils.copy(in, fileOutputStream);
            List<File> files = new ArrayList<>();
            files.add(tmp);
            //  files.add(tmp1);
            JadxArgs args = new JadxArgs();
            args.setSkipResources(true);
            args.setShowInconsistentCode(true);
            args.setInputFiles(files);
            //   args.setOutDirRes(new File(FileUtil.getProjectPath()));
            JadxDecompiler decompiler = new JadxDecompiler(args);
            decompiler.load();
            JavaClass javaClass = decompiler.getClasses().iterator().next();
            javaClass.decompile();
            return javaClass.getCode();
        } finally {
            tmp.delete();
            //tmp1.delete();
        }
    }

    public static String translate(File smali) {
        JadxArgs args = new JadxArgs();
        args.setSkipResources(true);
        args.setShowInconsistentCode(true);
        args.setInputFile(smali);
        JadxDecompiler decompiler = new JadxDecompiler(args);
        decompiler.load();
        JavaClass javaClass = decompiler.getClasses().iterator().next();
        javaClass.decompile();
        return javaClass.getCode();
    }
}

