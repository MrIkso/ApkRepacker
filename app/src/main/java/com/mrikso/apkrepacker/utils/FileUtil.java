package com.mrikso.apkrepacker.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.text.format.Formatter;
import android.util.Base64;
import android.util.Log;
import android.webkit.MimeTypeMap;

import androidx.annotation.Nullable;

import com.mrikso.apkrepacker.App;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.filepicker.Utility;
import com.mrikso.apkrepacker.ui.prererence.Preference;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.mrikso.apkrepacker.App.getContext;

public class FileUtil {
    private static final String TAG = "FileUtil";

    private static String projectPath;

    public static String genName(Context ctx, String path, String name, String suff, int cnt) {
        boolean overwrite = true;//Settings.getb(ctx, "overwrite_apk", true);
        if (overwrite) {
            return name + suff;
        } else {
            try {
                String tn = name;
                if (cnt > 0) {
                    tn = name + "(" + cnt + ")";
                }
                File check = new File(path, tn + suff);
                if (check.exists()) {
                    return genName(ctx, path, name, suff, ++cnt);
                } else {
                    return tn + suff;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return name + suff;
            }
        }
    }

    public static String genNameApk(Context ctx, String path, String name, String suff, int cnt) {
        String[] param = parseNameApk(name, suff);
        boolean overwrite = true;// Settings.getb(ctx, "overwrite_apk", true);
        if (overwrite) {
            return param[0] + "." + param[1];
        } else {
            try {
                String tn = param[0];
                if (cnt > 0) {
                    tn = param[0] + "(" + cnt + ")";
                }
                File check = new File(path, tn + "." + param[1]);
                if (check.exists()) {
                    return genNameApk(ctx, path, name, suff, ++cnt);
                } else {
                    return tn + "." + param[1];
                }
            } catch (Exception e) {
                e.printStackTrace();
                return param[0] + "." + param[1];
            }
        }
    }

    public static String[] parseNameApk(String name, String suff) {
        String filename = "out";
        String extension = "apk";

        int i = name.lastIndexOf('.');
        if (i > 0 && name != null) {
            filename = name.substring(0, i) + suff;
            extension = name.substring(i + 1);
        }
        return new String[]{filename, extension};
    }

    public static void installApk(Context c, File apk) {
        Uri data;
        if (Build.VERSION.SDK_INT >= 24) {
            Uri.Builder builder = new Uri.Builder();
            builder.authority(c.getPackageName() + ".fileprovider");
            builder.scheme("content");
            byte[] buf = apk.getAbsolutePath().getBytes();
            builder.path(Base64.encodeToString(buf, Base64.NO_WRAP));
            data = builder.build();
        } else
            data = Uri.fromFile(apk);
        Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
        intent.setFlags(Intent.EXTRA_DOCK_STATE_LE_DESK);
        intent.setData(data);
        c.startActivity(intent);
    }

    public static String getProjectPath() {
        return projectPath;
    }

    public static void setProjectPath(String path) {
        projectPath = path;
    }

    @SuppressLint("DefaultLocale")
    @Nullable
    public static File createBackupFile(PackageMeta packageMeta, String parent) {
        File backupsDir = new File(parent, "backup");
        if (!backupsDir.exists() && !backupsDir.mkdir()) {
            Log.e(TAG, "Unable to mkdir:" + backupsDir.toString());
            return null;
        }

        String packageInfoPart = String.format("%s-v%s", packageMeta.label, packageMeta.versionName).replace('.', '_');
        if (packageInfoPart.length() > 160)
            packageInfoPart = packageInfoPart.substring(0, 160);

        packageInfoPart = escapeFileName(packageInfoPart);

        return new File(backupsDir, packageInfoPart + ".apk");/* String.format("%s-%d.apks", packageInfoPart, System.currentTimeMillis()));*/
    }

    public static String escapeFileName(String name) {
        return name.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    //---------------------------
    public static File copyFile(File src, File path) throws Exception {
        try {
            if (src.isDirectory()) {
                if (src.getPath().equals(path.getPath())) throw new Exception();
                File directory = createDirectory(path, src.getName());
                for (File file : src.listFiles()) copyFile(file, directory);
                return directory;
            } else {
                File file = new File(path, src.getName());
                FileChannel channel = new FileInputStream(src).getChannel();
                channel.transferTo(0, channel.size(), new FileOutputStream(file).getChannel());
                return file;
            }
        } catch (Exception e) {
            throw new Exception(String.format("Error copying %s", src.getName()));
        }
    }

    //----------------------------------------------------------------------------------------------

    public static File createDirectory(File path, String name) throws Exception {
        File directory = new File(path, name);
        if (directory.mkdirs()) return directory;
        if (directory.exists()) throw new Exception(String.format("%s already exists", name));
        throw new Exception(String.format("Error creating %s", name));
    }

    public static File deleteFile(File file) throws Exception {
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                deleteFile(child);
            }
        }

        if (file.delete()) return file;
        throw new Exception(String.format("Error deleting %s", file.getName()));
    }

    public static File renameFile(File file, String name) throws Exception {

        //  String extension = getExtension(file.getName());
        //  if (!extension.isEmpty()) name += "." + extension;
        File newFile = new File(file.getParent(), name);
        if (file.renameTo(newFile)) return newFile;
        throw new Exception(String.format("Error renaming %s", file.getName()));
    }

    public static File unzip(File zip) throws Exception {
        File directory = createDirectory(zip.getParentFile(), removeExtension(zip.getName()));
        FileInputStream fileInputStream = new FileInputStream(zip);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
        try (ZipInputStream zipInputStream = new ZipInputStream(bufferedInputStream)) {
            ZipEntry zipEntry;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                byte[] buffer = new byte[1024];
                File file = new File(directory, zipEntry.getName());
                if (zipEntry.isDirectory()) {
                    if (!file.mkdirs()) throw new Exception("Error uncompressing");
                } else {
                    int count;
                    try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                        while ((count = zipInputStream.read(buffer)) != -1) {
                            fileOutputStream.write(buffer, 0, count);
                        }
                    }
                }
            }
        }
        return directory;
    }

    public static File getInternalStorage() {
        //returns the path to the internal storage
        return Environment.getExternalStorageDirectory();
    }

    //----------------------------------------------------------------------------------------------

    public static File getExternalStorage() {
        //returns the path to the external storage or null if it doesn't exist
        String path = Utility.getExternalStoragePath(getContext(), true);
        return path != null ? new File(path) : null;
    }

    public static File getPublicDirectory(String type) {
        //returns the path to the public directory of the given type
        return Environment.getExternalStoragePublicDirectory(type);
    }

    public static String getFileExtension(File file) {
        String fileName = file.getName();
        if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
            return fileName.substring(fileName.lastIndexOf(".") + 1);
        else return "";
    }

    public static String getLastModified(File file) {

        //returns the last modified date of the given file as a formatted string
        return DateFormat.format("dd MMM yyy, HH:mm", new Date(file.lastModified())).toString();
    }

    public static String getMimeType(File file) {

        //returns the mime type for the given file or null iff there is none
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(getExtension(file.getName()));
    }

    public static String getName(File file) {

        //returns the name of the file hiding extensions of known file types
        return file.getName();
       /* switch (FileType.getFileType(file)) {

            case DIRECTORY:
                return file.getName();

            case MISC_FILE:
                return file.getName();

            default:
                return removeExtension(file.getName());
        }*/
    }

    public static String getNameVithoutExt(File file) {

        //returns the name of the file hiding extensions of known file types
        //return file.getName();
       /* switch (FileType.getFileType(file)) {

            case DIRECTORY:
                return file.getName();

            case MISC_FILE:
                return file.getName();

            default:

        */
        return removeExtension(file.getName());

    }

    public static String getCreateTime(File file) {
        BasicFileAttributes attributes = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            try {
                attributes = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
                return DateFormat.format("dd MMM yyy, HH:mm", new Date(attributes.creationTime().toMillis())).toString();
                // FileTime time = attributes.creationTime();
                // SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyy HH:mm");
                // return dateFormat.format(time.toMillis());
            } catch (IOException e) {

                e.printStackTrace();

            }

        } else {
            Date lastmod = new Date(file.lastModified());
            return lastmod.toString();
        }
        return "";
    }

    public static String getPath(File file) {

        //returns the path of the given file or null if the file is null

        return file != null ? file.getPath() : null;
    }

    public static String getSize(Context context, File file) {

        if (file.isDirectory()) {
            //returns the size folder
            // return Formatter.formatShortFileSize(context,getFolderSize(file));
            File[] children = getChildren(file);
            if (children == null) return null;
            return context.getResources().getString(R.string.items, children.length);
        } else {
            return Formatter.formatShortFileSize(context, file.length());
        }
    }

    public static String getFolderSize(Context context, File file) {
        if (file.isDirectory())
            //returns the size folder
            return Formatter.formatShortFileSize(context, getFolderSize(file));
        return null;
    }

    public static long getFolderSize(File dir) {
        long size = 0;
        for (File file : Objects.requireNonNull(dir.listFiles())) {
            if (file.isFile()) {
                size += file.length();
            } else
                size += getFolderSize(file);
        }
        return size;
    }

    public static String getStorageUsage(Context context) {
        File internal = getInternalStorage();
        File external = getExternalStorage();
        long f = internal.getFreeSpace();
        long t = internal.getTotalSpace();
        if (external != null) {
            f += external.getFreeSpace();
            t += external.getTotalSpace();
        }
        String use = Formatter.formatShortFileSize(context, t - f);
        String tot = Formatter.formatShortFileSize(context, t);
        return String.format("%s used of %s", use, tot);
    }

    public static String getTitle(File file) {
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(file.getPath());
            return retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        } catch (Exception e) {
            return null;
        }
    }

    public static String getExtension(String filename) {

        //returns the file extension or an empty string iff there is no extension
        return filename.contains(".") ? filename.substring(filename.lastIndexOf(".") + 1) : "";
    }

    //----------------------------------------------------------------------------------------------

    public static String removeExtension(String filename) {
        int index = filename.lastIndexOf(".");
        return index != -1 ? filename.substring(0, index) : filename;
    }

    public static int compareDate(File file1, File file2) {
        long lastModified1 = file1.lastModified();
        long lastModified2 = file2.lastModified();
        return Long.compare(lastModified2, lastModified1);
    }

    //----------------------------------------------------------------------------------------------

    public static int compareName(File file1, File file2) {
        String name1 = file1.getName();
        String name2 = file2.getName();
        return name1.compareToIgnoreCase(name2);
    }

    public static int compareSize(File file1, File file2) {
        long length1 = file1.length();
        long length2 = file2.length();
        return Long.compare(length2, length1);
    }

    public static int getColorResource(File file) {
        switch (FileType.getFileType(file)) {
            case DIRECTORY:
                return R.color.directory;
            case MISC_FILE:
                return R.color.misc_file;
            case AUDIO:
                return R.color.audio;
            case IMAGE:
                return R.color.image;
            case VIDEO:
                return R.color.video;
            case DOC:
                return R.color.doc;
            case PPT:
                return R.color.ppt;
            case XLS:
                return R.color.xls;
            case PDF:
                return R.color.pdf;
            case TXT:
                return R.color.txt;
            case ZIP:
                return R.color.zip;
            case APK:
                return R.color.apk;
            case DEX:
                return R.color.dex;
            case SMALI:
                return R.color.smali;
            case XML:
                return R.color.xml;
            default:
                return R.color.misc_file;
        }
    }

    //----------------------------------------------------------------------------------------------

    public static int getImageResource(File file) {
        switch (FileType.getFileType(file)) {
            case DIRECTORY:
                return R.drawable.ic_directory;
            case MISC_FILE:
                return R.drawable.ic_misc_file;
            case AUDIO:
                return R.drawable.ic_audio;
            case IMAGE:
                return R.drawable.ic_image;
            case VIDEO:
                return R.drawable.ic_video;
            case DOC:
                return R.drawable.ic_doc;
            case PPT:
                return R.drawable.ic_ppt;
            case XLS:
                return R.drawable.ic_xls;
            case PDF:
                return R.drawable.ic_pdf;
            case TXT:
                return R.drawable.ic_txt;
            case ZIP:
                return R.drawable.ic_zip;
            case DEX:
                return R.drawable.ic_txt;
            case SMALI:
            case JS:
            case JSON:
            case XML:
                return R.drawable.ic_txt;
            case APK:
                return R.drawable.ic_txt;
            case APKS:
                return R.drawable.ic_zip;
            default:
                return R.drawable.ic_misc_file;
        }
    }

    public static boolean isStorage(File dir) {
        return dir == null || dir.equals(getInternalStorage()) || dir.equals(getExternalStorage());
    }

    //----------------------------------------------------------------------------------------------

    public static File[] getChildren(File directory) {
        if (!directory.canRead())
            return null;
            if (showIsHidden()) {
                return directory.listFiles(pathname -> pathname.exists());
            } else {
                return directory.listFiles(pathname -> pathname.exists() && !pathname.isHidden());
            }
    }

    private static boolean showIsHidden() {
        boolean show = Preference.getInstance(App.getContext()).isShowHiddenFiles();
        return show;
    }
    //----------------------------------------------------------------------------------------------

    public static ArrayList<File> searchFilesName(Context context, String name) {
        ArrayList<File> list = new ArrayList<>();
        Uri uri = MediaStore.Files.getContentUri("external");
        String data[] = new String[]{MediaStore.Files.FileColumns.DATA};
        Cursor cursor = new CursorLoader(context, uri, data, null, null, null).loadInBackground();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                File file = new File(cursor.getString(cursor.getColumnIndex(data[0])));
                if (file.exists() && file.getName().startsWith(name)) list.add(file);
            }
            cursor.close();
        }
        return list;
    }

    public enum FileType {

        DIRECTORY, MISC_FILE, AUDIO, IMAGE, VIDEO, DOC, PPT, XLS, PDF, TXT, ZIP, APK, DEX, BAK, APKS,
        XML, SMALI, JSON, HTML, HTM, INI, JS;

        public static FileType getFileType(File file) {

            if (file.isDirectory())
                return FileType.DIRECTORY;

            String mime = FileUtil.getMimeType(file);
            String ext = FileUtil.getExtension(file.getName());
            if (ext.startsWith("zip")) {
                // Log.i("EXT", "returned zip");
                return FileType.ZIP;
            } else if (ext.startsWith("apks")) {
                //  Log.i("EXT", "returned apks file");
                return FileType.APKS;
            } else if (ext.startsWith("apk")) {
                // Log.i("EXT", "returned apk file");
                return FileType.APK;
            } else if (ext.startsWith("dex")) {
                // Log.i("EXT", "returned dex file");
                return FileType.DEX;
            } else if (ext.startsWith("bak")) {
                // Log.i("EXT", "returned bak file");
                return FileType.BAK;
            }
            //текстовые файлы
            else if (ext.startsWith("xml")) {
                return FileType.XML;
            } else if (ext.startsWith("ini")) {
                return FileType.INI;
            } else if (ext.startsWith("smali")) {
                return FileType.SMALI;
            } else if (ext.startsWith("json")) {
                return FileType.JSON;
            } else if (ext.startsWith("html")) {
                return FileType.HTML;
            } else if (ext.startsWith("htm")) {
                return FileType.HTM;
            } else if (ext.startsWith("js")) {
                return FileType.JS;
            } else if (mime == null)
                return FileType.MISC_FILE;

            else if (mime.startsWith("audio"))
                return FileType.AUDIO;

            else if (mime.startsWith("image"))
                return FileType.IMAGE;

            else if (mime.startsWith("video"))
                return FileType.VIDEO;

            else if (mime.startsWith("application/ogg"))
                return FileType.AUDIO;

            else if (mime.startsWith("application/msword"))
                return FileType.DOC;

            else if (mime.startsWith("application/vnd.ms-word"))
                return FileType.DOC;

            else if (mime.startsWith("application/vnd.ms-powerpoint"))
                return FileType.PPT;

            else if (mime.startsWith("application/vnd.ms-excel"))
                return FileType.XLS;

            else if (mime.startsWith("application/vnd.openxmlformats-officedocument.wordprocessingml"))
                return FileType.DOC;

            else if (mime.startsWith("application/vnd.openxmlformats-officedocument.presentationml"))
                return FileType.PPT;

            else if (mime.startsWith("application/vnd.openxmlformats-officedocument.spreadsheetml"))
                return FileType.XLS;

            else if (mime.startsWith("application/pdf"))
                return FileType.PDF;

            else if (mime.startsWith("text"))
                return FileType.TXT;

            else
                return FileType.MISC_FILE;
        }


    }
}