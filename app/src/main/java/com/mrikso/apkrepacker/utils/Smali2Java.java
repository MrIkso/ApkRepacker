package com.mrikso.apkrepacker.utils;

import com.google.common.collect.ImmutableList;

import org.jf.dexlib2.writer.builder.DexBuilder;
import org.jf.dexlib2.writer.io.FileDataStore;

import java.io.File;
import java.io.IOException;

import jadx.api.JadxArgs;
import jadx.api.JadxDecompiler;
import jadx.api.JavaClass;

public class Smali2Java {
    public static String translate(DexBuilder dexBuilder) throws IOException {
        File tmp = File.createTempFile("temp", ".dex");
        try {
            dexBuilder.writeTo(new FileDataStore(tmp));
            JadxArgs args = new JadxArgs();
            args.setSkipResources(true);
            args.setShowInconsistentCode(true);
            args.setInputFiles(ImmutableList.of(tmp));
            JadxDecompiler decompiler = new JadxDecompiler(args);
            decompiler.load();
            JavaClass javaClass = decompiler.getClasses().iterator().next();
            javaClass.decompile();
            return javaClass.getCode();
        } finally {
            tmp.delete();
        }
    }
}

