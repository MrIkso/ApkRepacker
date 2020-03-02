package com.mrikso.apkrepacker.patchengine;

import android.util.Log;

import com.mrikso.apkrepacker.utils.FileUtil;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class AddFilesRule extends Core {
    private String source;
    private String target;
    private boolean extract;

    AddFilesRule(){

    }

    /*
        Парсим нужные значения для патчинга
     */
    @Override
    public void start(LineReader lineReader) {
        line = lineReader.getLine();
        String readLine = lineReader.readLine();
        while (readLine != null) {
            String trim = readLine.trim();
            if ("[/ADD_FILES]".equals(trim)) {
                break;
            } else if (super.checkName(trim, lineReader)) {
                readLine = lineReader.readLine();
            } else {
                switch (trim) {
                    case "SOURCE:":
                        source = lineReader.readLine().trim();
                        break;
                    case "TARGET:":
                        target = lineReader.readLine().trim();
                        Log.i("Reader", String.format("target  %s", target));
                        break;
                    case "EXTRACT:":
                        extract = Boolean.valueOf(lineReader.readLine().trim()).booleanValue();
                        break;
                    default:
                        Log.i("Reader", String.format("Нельзя спарсить %d,  %s", Integer.valueOf(lineReader.getLine()), line));
                        break;
                }
                readLine = lineReader.readLine();
            }
        }
        if (target != null && target.endsWith("/")) {
            target = target.substring(0, target.length() - 1);
        }
    }

    @Override
    public boolean inSmali() {
        return Core.checkIsSmali(target);
    }

    /*
        Происходит процесс патчинга
     */
    @Override
    public String currentRule(ZipFile zipFile) {
        InputStream inputStream;
        ZipEntry entry = getEntry(zipFile, source);
      //  ZipEntry entry = zipFile.getEntry(source);
        if (entry == null) {
            Log.i("Reader","патч пустой");
        } else {
         //   if (!extract) {

         //   }
        //else {
                try {
                    inputStream = zipFile.getInputStream(entry);
                    File file = new File(FileUtil.getProjectPath() + "/" + "t.smali");
                   if (!file.exists() && !file.mkdirs()) {
                        return null;
                   }
                    IOUtils.copy(inputStream, new FileOutputStream(file));
                    inputStream.close();
                    Log.i("Reader", file.getAbsolutePath());
                } catch (IOException e) {
                    Log.i("Reader", String.format("Не скопировалось в %s", target));
                    e.printStackTrace();
                }
            }
       // }
        return null;
    }
    public static  ZipEntry getEntry(ZipFile zf, String patch){
        return zf.getEntry(patch);
    }
}
