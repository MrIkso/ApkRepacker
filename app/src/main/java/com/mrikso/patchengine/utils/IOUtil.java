package com.mrikso.patchengine.utils;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.zip.ZipFile;

public class IOUtil {

    public static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[4096];
        while (true) {
            int read = in.read(buffer);
            if (read != -1) {
                out.write(buffer, 0, read);
            } else {
                return;
            }
        }
    }

    public static void copy(File targetDir, File srcDir) {
        File[] files = srcDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    FileInputStream fis = null;
                    FileOutputStream fos = null;
                    try {
                        fis = new FileInputStream(file);
                        fos = new FileOutputStream(new File(targetDir, file.getName()));
                        copy(fis, fos);
                    } catch (Exception ignored) {
                    } catch (Throwable th) {
                        closeQuietly(fis);
                        closeQuietly(fos);
                        throw th;
                    }
                    closeQuietly(fis);
                    closeQuietly(fos);
                } else if (file.isDirectory()) {
                    File subDir = new File(targetDir, file.getName());
                    subDir.mkdir();
                    copy(subDir, file);
                }
            }
        }
    }

    public static byte[] toByteArray(InputStream in) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        copy(in, output);
        return output.toByteArray();
    }

    public static void writeZero(OutputStream out, int size) throws IOException {
        int blocks = size / 1024;
        int remain = size % 1024;
        byte[] buffer = new byte[1024];
        for (int i = 0; i < 1024; i++) {
            buffer[i] = 0;
        }
        for (int i2 = 0; i2 < blocks; i2++) {
            out.write(buffer);
        }
        if (remain > 0) {
            out.write(buffer, 0, remain);
        }
    }

    public static void readFully(InputStream is, byte[] buf) throws IOException {
        int ret;
        int read = 0;
        while (read < buf.length && (ret = is.read(buf, read, buf.length - read)) != -1) {
            read += ret;
        }
    }

    public static boolean writeObjectToFile(String filePath, Object obj) {
        ObjectOutputStream objOut = null;
        try {
            objOut = new ObjectOutputStream(new FileOutputStream(new File(filePath)));
            objOut.writeObject(obj);
            objOut.flush();
            closeQuietly(objOut);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            closeQuietly(objOut);
            return false;
        } catch (Throwable th) {
            closeQuietly(objOut);
            throw th;
        }
    }

    public static String readString(InputStream input) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(input));
        for (String line = br.readLine(); line != null; line = br.readLine()) {
            sb.append(line);
            sb.append("\n");
        }
        return sb.toString();
    }

    public static void closeQuietly(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void closeQuietly(ZipFile zfile) {
        if (zfile != null) {
            try {
                zfile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void writeToFile(String targetFile, String content) throws IOException {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(targetFile);
            fos.write(content.getBytes());
        } finally {
            closeQuietly(fos);
        }
    }

    public static boolean exist() {
        if (Environment.getExternalStorageState().equals("mounted")) {
            return true;
        }
        return false;
    }

    public static String getRootDirectory() {
        return Environment.getExternalStorageDirectory().getPath();
    }

    public static String makeDir(String dirName) throws Exception {
        if (exist()) {
            String subDir = "";
            //String packagePath = ctx.getPackageName();
            subDir = "/ApkRepacker/" + dirName + "/";
            String targetDir = getRootDirectory() + subDir;
            File f = new File(targetDir);
            if (!f.exists()) {
                f.mkdirs();
            }
            return targetDir;
        }
        throw new Exception("Can not find sd card.");
    }

    public static void makeDir(String dirPath, String folderName ) throws Exception {
        File f = new File(dirPath, folderName);
        if (f.exists()) {
            throwExistException(folderName);
        }
         f.mkdir();
    }

    private static void throwExistException(String filename) throws Exception {
        throw new Exception(String.valueOf(Log.e("IOUtils", String.format("Folder %s exits", filename))));
    }

}
